#pragma once

#include "variant_storage.h"

namespace impl {

template <typename... Types>
struct destructor_base : variant_storage<Types...> {
  using variant_storage<Types...>::variant_storage;

  ~destructor_base() {
    this->reset();
  }
};

template <typename... Types>
requires(TriviallyDestructible<Types...>) struct destructor_base<Types...> : variant_storage<Types...> {
  using variant_storage<Types...>::variant_storage;

  ~destructor_base() = default;
};

} // namespace impl
