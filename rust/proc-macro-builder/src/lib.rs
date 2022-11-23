use proc_macro2::{Ident, TokenStream};

use quote::{quote, ToTokens};
use syn::{parse_macro_input, Data, DataStruct, DeriveInput, Field, Fields, FieldsNamed};

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
        panic!("Only structs supported");
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
            Some((same_idents, builder_setter)) => {
                if same_idents {
                    builder_setter
                } else {
                    quote! {
                        #default_setter
                        #builder_setter
                    }
                }
            }
            None => default_setter,
        }
    });

    let build_fields = struct_fields.iter().map(|field| {
        let ident = &field.ident;
        let ty = &field.ty;
        let value = if is_option(ty) || has_builder_attr(field) {
            quote! { self.#ident.clone() }
        } else {
            quote! { self.#ident.take().ok_or(anyhow::anyhow!("{} is not set", stringify!(#ident)))? }
        };
        quote! { #ident: #value }
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

    proc_macro::TokenStream::from(tokens)
}

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
        return path.segments.len() == 1 && path.segments.first().unwrap().ident == "builder";
    })
}

fn get_vec_inner_ty(field_type: &syn::Type) -> &syn::Type {
    match get_inner_ty("Vec", field_type) {
        Some(inner_ty) => inner_ty,
        None => panic!("expected Vec"),
    }
}

fn get_option_inner_ty_or_else_ty(field_type: &syn::Type) -> &syn::Type {
    match get_inner_ty("Option", field_type) {
        Some(inner_ty) => inner_ty,
        None => field_type,
    }
}

fn is_option(field_type: &syn::Type) -> bool {
    get_inner_ty("Option", field_type).is_some()
}

fn generate_default_setter(field: &&Field) -> TokenStream {
    let ident = &field.ident;
    let ident_ty = get_option_inner_ty_or_else_ty(&field.ty);
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

fn make_error_msg<T: ToTokens>(tokens: &T) -> Option<(bool, TokenStream)> {
    Some((
        false,
        syn::Error::new_spanned(tokens, "expected `builder(each = \"...\")`").to_compile_error(),
    ))
}

fn generate_builder_attr_setters(field: &Field) -> Option<(bool, TokenStream)> {
    for attr in &field.attrs {
        match attr.parse_meta() {
            Ok(syn::Meta::List(ref meta_list)) => {
                let segments = &meta_list.path.segments;
                if segments.len() != 1 {
                    return None;
                }
                if segments.first().unwrap().ident != "builder" {
                    return None;
                }
                let nested = &meta_list.nested;
                return match nested.first() {
                    Some(syn::NestedMeta::Meta(syn::Meta::NameValue(ref meta_name_value))) => {
                        let segments = &meta_name_value.path.segments;
                        if segments.len() != 1 {
                            return None;
                        }
                        if segments.first().unwrap().ident != "each" {
                            return make_error_msg(meta_list);
                        }
                        let lit = &meta_name_value.lit;
                        let attr_ident = match lit {
                            syn::Lit::Str(lit_str) => Ident::new(&lit_str.value(), lit_str.span()),
                            _ => return make_error_msg(lit),
                        };
                        let ident = &field.ident;
                        let vec_inner_ty = get_vec_inner_ty(&field.ty);
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
                    _ => make_error_msg(nested),
                };
            }
            Ok(ref meta) => make_error_msg(meta),
            Err(_) => continue,
        };
    }
    None
}
