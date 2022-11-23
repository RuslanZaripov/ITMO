use proc_macro2::{Ident, TokenStream};

use quote::quote;
use syn::{parse_macro_input, Data, DataStruct, DeriveInput, Field, Fields, FieldsNamed};

fn get_inner_ty<'a>(wrapper: &'a str, field_type: &'a syn::Type) -> Option<&'a syn::Type> {
    if let syn::Type::Path(syn::TypePath { path, .. }) = field_type {
        // eprintln!("path: {:#?}", path);
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
        return path.segments.len() == 1 && path.segments.first().unwrap().ident == "builder";
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
    let input_struct = parse_macro_input!(_input as DeriveInput);

    let name = input_struct.ident;

    let b_ident = Ident::new(&format!("{}Builder", name), name.span());

    let struct_fields = if let Data::Struct(DataStruct {
        fields: Fields::Named(FieldsNamed { ref named, .. }),
        ..
    }) = input_struct.data
    {
        named
    } else {
        // TODO: write better error message, in case Builder derived for enum
        unimplemented!();
    };

    let b_fields = struct_fields.iter().map(|field| {
        let ident = &field.ident;
        let ty = &field.ty;
        let value = if is_option(ty) || has_builder_attr(field) {
            quote! { #ty }
        } else {
            quote! { std::option::Option<#ty> }
        };
        quote! { #ident: #value }
    });

    let b_setters = struct_fields.iter().map(|field| {
        let default_setter = generate_default_setter(&field);
        match generate_builder_attr_setters(&field) {
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

    let build_fields = struct_fields.iter().map(|field| {
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

    let none_fields = struct_fields.iter().map(|field| {
        let ident = &field.ident;
        let value = if has_builder_attr(field) {
            quote! { std::vec::Vec::new() }
        } else {
            quote! { std::option::Option::None }
        };
        quote! {
            #ident: #value
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
            #(#b_setters)*

            pub fn build(&mut self) -> anyhow::Result<#name> {
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
    let ty = &field.ty;
    let ident_ty = match get_option_inner_ty(ty) {
        Some(inner_ty) => inner_ty,
        None => ty,
    };
    let value = if has_builder_attr(field) {
        quote! { #ident }
    } else {
        quote! { std::option::Option::Some(#ident) }
    };
    quote! {
        pub fn #ident(&mut self, #ident: #ident_ty) -> &mut Self {
            self.#ident = #value;
            self
        }
    }
}

fn generate_builder_attr_setters(field: &Field) -> Option<(bool, TokenStream)> {
    for attr in &field.attrs {
        match attr.parse_meta() {
            Ok(syn::Meta::List(meta_list)) => {
                if meta_list.path.segments.len() != 1 {
                    return None;
                }
                if meta_list.path.segments.first().unwrap().ident != "builder" {
                    return None;
                }
                return match meta_list.nested.first() {
                    Some(syn::NestedMeta::Meta(syn::Meta::NameValue(meta_name_value))) => {
                        if meta_name_value.path.segments.len() != 1 {
                            return None;
                        }
                        if meta_name_value.path.segments.first().unwrap().ident != "each" {
                            return Some((
                                false,
                                syn::Error::new_spanned(
                                    meta_list,
                                    "expected `builder(each = \"...\")`",
                                )
                                .to_compile_error(),
                            ));
                        }
                        let ident = &field.ident;
                        let attr_ident = match &meta_name_value.lit {
                            syn::Lit::Str(lit_str) => Ident::new(&lit_str.value(), lit_str.span()),
                            _ => panic!("builder attribute can only be used on Vec fields"),
                        };
                        let vec_inner_ty = match get_vec_inner_ty(&field.ty) {
                            Some(inner_ty) => inner_ty,
                            None => panic!("builder attribute can only be used on Vec fields"),
                        };
                        Some((
                            ident.as_ref().unwrap() == &attr_ident,
                            quote! {
                                pub fn #attr_ident(&mut self, #attr_ident: #vec_inner_ty) -> &mut Self {
                                    self.#ident.push(#attr_ident);
                                    self
                                }
                            },
                        ))
                    }
                    _ => None,
                };
            }
            Ok(_meta) => todo!(),
            Err(_) => continue,
        };
    }
    None
}
