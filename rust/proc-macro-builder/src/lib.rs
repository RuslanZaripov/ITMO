use proc_macro2::{Group, Literal, TokenTree};
use proc_macro2::Ident;

use quote::quote;
use syn::{parse_macro_input, Data, DataStruct, DeriveInput, Fields, FieldsNamed};

fn get_inner_ty<'a>(wrapper: &'a str, field_type: &'a syn::Type) -> Option<&'a syn::Type> {
    if let syn::Type::Path(syn::TypePath { path, .. }) = field_type {
        if path.segments.len() != 1 {
            return None;
        }
        let segment = path.segments.first().unwrap();
        if segment.ident != wrapper {
            return None;
        }
        if let syn::PathArguments::AngleBracketed(syn::AngleBracketedGenericArguments { args, .. }) = &segment.arguments {
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

fn is_builder_attr_field(field: &syn::Field) -> Option<proc_macro2::TokenStream> {
    for attr in &field.attrs {
        if let syn::Attribute { path, tokens, .. } = attr {
            if path.segments.len() == 1 && path.segments.first().unwrap().ident == "builder" {
                return Some(tokens.clone());
            }
        }
    }
    None
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

    // eprintln!("input: {:#?}", input.data);

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
        if is_option(ty) {
            quote! {
                #ident: #ty
            }
        } else {
            quote! {
                #ident: std::option::Option<#ty>
            }
        }
    });

    let b_setters = input_fields.iter().map(|field| {
        let ident = &field.ident;
        let ty = match get_option_inner_ty(&field.ty) {
            Some(inner_type) => inner_type,
            None => &field.ty
        };
        // eprintln!("{:#?}", &field);
        quote! {
            pub fn #ident(&mut self, #ident: #ty) -> &mut Self {
                self.#ident = Some(#ident);
                self
            }
        }
    });

    let builder_attr_setters = input_fields.iter().filter_map(|field| {
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
                // println!("g: {:#?}", g);
                let vec_inner_ty = match get_vec_inner_ty(&field.ty) {
                    Some(inner_ty) => inner_ty,
                    None => panic!("builder attribute can only be used on Vec fields")
                };
                match syn::Lit::new(parse_attr(group)) {
                    syn::Lit::Str(s) => {
                        let attr_ident = Ident::new(&s.value(), s.span());
                        return Some(quote! {
                            pub fn #attr_ident(&mut self, #attr_ident: #vec_inner_ty) -> &mut Self {
                                if self.#ident.is_none() {
                                    self.#ident = Some(vec![]);
                                }
                                self.#ident.as_mut().unwrap().push(#attr_ident);
                                self
                            }
                        });
                    }
                    lit => panic!("Unexpected literal: {:#?}", lit)
                };
            }
        }
        None
    });

    let build_fields = input_fields.iter().map(|field| {
        let ident = &field.ident;
        let ty = &field.ty;
        if is_option(ty) {
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
        quote!(
            #ident: None
        )
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
            #(#b_setters)*
            #(#builder_attr_setters)*

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

fn parse_attr(g: Group) -> Literal {
    let mut iter = g.stream().into_iter();
    match iter.next().unwrap() {
        TokenTree::Ident(id) => id,
        token => panic!("Unexpected token: {:#?}", token)
    };
    match iter.next().unwrap() {
        TokenTree::Punct(p) => p,
        token => panic!("Unexpected token: {:#?}", token)
    };
    let literal = match iter.next().unwrap() {
        TokenTree::Literal(l) => l,
        token => panic!("Unexpected token: {:#?}", token)
    };
    literal
}
