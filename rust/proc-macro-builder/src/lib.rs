use proc_macro2::Ident;
use proc_macro2::{Group, TokenStream, TokenTree};

use quote::quote;
use syn::{parse_macro_input, Data, DataStruct, DeriveInput, Field, Fields, FieldsNamed};

fn get_inner_ty<'a>(wrapper: &'a str, field_type: &'a syn::Type) -> Option<&'a syn::Type> {
    if let syn::Type::Path(syn::TypePath { path, .. }) = field_type {
        if path.segments.len() != 1 {
            return None;
        }
        let segment = path.segments.first().unwrap();
        if segment.ident != wrapper {
            return None;
        }
        if let syn::PathArguments::AngleBracketed(syn::AngleBracketedGenericArguments {
            args,
            ..
        }) = &segment.arguments
        {
            if args.len() != 1 {
                return None;
            }
            if let syn::GenericArgument::Type(ty) = &args.first().unwrap() {
                return Some(ty);
            }
        }
    }
    None
}

fn has_builder_attr(field: &Field) -> bool {
    field.attrs.iter().any(|attr| {
        let syn::Attribute { path, .. } = attr;
        if path.segments.len() == 1 && path.segments.first().unwrap().ident == "builder" {
            return true;
        }
        false
    })
}

fn get_vec_inner_ty(field_type: &syn::Type) -> Option<&syn::Type> {
    get_inner_ty("Vec", field_type)
}

fn get_option_inner_ty(field_type: &syn::Type) -> Option<&syn::Type> {
    get_inner_ty("Option", field_type)
}

fn is_option(field_type: &syn::Type) -> bool {
    get_option_inner_ty(field_type).is_some()
}

#[proc_macro_derive(Builder, attributes(builder))]
pub fn derive(_input: proc_macro::TokenStream) -> proc_macro::TokenStream {
    let input = parse_macro_input!(_input as DeriveInput);

    let name = input.ident;

    let b_ident = Ident::new(&format!("{}Builder", name), name.span());

    let input_fields = if let Data::Struct(DataStruct {
        fields: Fields::Named(FieldsNamed { ref named, .. }),
        ..
    }) = input.data
    {
        named
    } else {
        // TODO: write better error message, in case Builder derived for enum
        unimplemented!();
    };

    let b_fields = input_fields.iter().map(|field| {
        let ident = &field.ident;
        let ty = &field.ty;
        if is_option(ty) || has_builder_attr(field) {
            quote! {
                #ident: #ty
            }
        } else {
            quote! {
                #ident: std::option::Option<#ty>
            }
        }
    });

    let get_builder_attr_setters = |field: &Field| -> Option<(bool, TokenStream)> {
        let ident = &field.ident;
        for attr in &field.attrs {
            let syn::Attribute { path, tokens, .. } = attr;
            if path.segments.len() != 1 {
                return None;
            }
            let segment = path.segments.first().unwrap();
            if segment.ident != "builder" {
                return None;
            }
            if let TokenTree::Group(group) = tokens.clone().into_iter().next().unwrap() {
                let vec_inner_ty = match get_vec_inner_ty(&field.ty) {
                    Some(inner_ty) => inner_ty,
                    None => panic!("builder attribute can only be used on Vec fields"),
                };
                let attr_ident = parse_attr(&group);
                return Some((
                    ident.as_ref().unwrap() == &attr_ident,
                    quote! {
                        pub fn #attr_ident(&mut self, #attr_ident: #vec_inner_ty) -> &mut Self {
                            self.#ident.push(#attr_ident);
                            self
                        }
                    },
                ));
            }
        }
        None
    };

    let setters = input_fields.iter().map(|field| {
        let default_setter = generate_default_setter(&field);
        match get_builder_attr_setters(&field) {
            Some((true, builder_setter)) => builder_setter,
            Some((false, builder_setter)) => {
                quote! {
                    #default_setter
                    #builder_setter
                }
            }
            None => default_setter,
        }
    });

    let build_fields = input_fields.iter().map(|field| {
        let ident = &field.ident;
        let ty = &field.ty;
        if is_option(ty) || has_builder_attr(field) {
            quote! {
                #ident: self.#ident.clone()
            }
        } else {
            quote! {
                #ident: self.#ident.take().ok_or(anyhow::anyhow!("{} is not set", stringify!(#ident)))?
            }
        }
    });

    let none_fields = input_fields.iter().map(|field| {
        let ident = &field.ident;
        if has_builder_attr(field) {
            quote! {
                #ident: vec![]
            }
        } else {
            quote! {
                #ident: std::option::Option::None
            }
        }
    });

    let tokens = quote! {
        pub struct #b_ident {
            #(#b_fields,)*
        }

        impl #name {
            pub fn builder() -> #b_ident {
                #b_ident {
                    #(#none_fields,)*
                }
            }
        }

        impl #b_ident {
            #(#setters)*

            fn build(&mut self) -> anyhow::Result<#name> {
                Ok(#name {
                    #(#build_fields,)*
                })
            }
        }
    };
    // eprintln!("TOKENS:\n {}", tokens);
    proc_macro::TokenStream::from(tokens)
}

fn generate_default_setter(field: &&Field) -> TokenStream {
    let ident = &field.ident;
    let ident_ty = match get_option_inner_ty(&field.ty) {
        Some(inner_ty) => inner_ty,
        None => &field.ty,
    };
    return if has_builder_attr(field) {
        quote! {
            pub fn #ident(&mut self, #ident: #ident_ty) -> &mut Self {
                self.#ident = #ident;
                self
            }
        }
    } else {
        quote! {
            pub fn #ident(&mut self, #ident: #ident_ty) -> &mut Self {
                self.#ident = Some(#ident);
                self
            }
        }
    };
}

fn parse_attr(g: &Group) -> Ident {
    let mut iter = g.stream().into_iter();
    match iter.next().unwrap() {
        TokenTree::Ident(id) => id,
        token => panic!("Unexpected token: {:#?}", token),
    };
    match iter.next().unwrap() {
        TokenTree::Punct(p) => p,
        token => panic!("Unexpected token: {:#?}", token),
    };
    let literal = match iter.next().unwrap() {
        TokenTree::Literal(l) => l,
        token => panic!("Unexpected token: {:#?}", token),
    };
    match syn::Lit::new(literal) {
        syn::Lit::Str(s) => Ident::new(&s.value(), s.span()),
        lit => panic!("Unexpected literal: {:#?}", lit),
    }
}
