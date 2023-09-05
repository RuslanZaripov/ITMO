#pragma once

#include "variant_traits.h"
#include "variant_utils.h"
#include "variant_visitor.h"
#include <utility>

namespace impl {

// --------------------------- uninitialized wrapper --------------------------- //

template <typename T>
struct uninitialized {
  template <typename... Args>
  constexpr uninitialized(in_place_index_t<0>, Args&&... args) {
    new (&storage) T(std::forward<Args>(args)...);
  }

  constexpr T& get() & noexcept {
    return *reinterpret_cast<T*>(&storage);
  }

  constexpr const T& get() const& noexcept {
    return *reinterpret_cast<const T*>(&storage);
  }

  constexpr T&& get() && noexcept {
    return std::move(*reinterpret_cast<T*>(&storage));
  }

  constexpr const T&& get() const&& noexcept {
    return std::move(*reinterpret_cast<const T*>(&storage));
  }

  std::aligned_storage_t<sizeof(T), alignof(T)> storage;
};

template <typename T>
requires(std::is_trivially_destructible_v<T>) struct uninitialized<T> {
  template <typename... Args>
  constexpr uninitialized(in_place_index_t<0>, Args&&... args) : storage(std::forward<Args>(args)...) {}

  constexpr T& get() & noexcept {
    return storage;
  }

  constexpr const T& get() const& noexcept {
    return storage;
  }

  constexpr T&& get() && noexcept {
    return std::move(storage);
  }

  constexpr const T&& get() const&& noexcept {
    return std::move(storage);
  }

  T storage;
};

// ------------------------------ variadic_union ----------------------------- //

template <typename... Types>
union variadic_union {};

template <typename First, typename... Rest>
union variadic_union<First, Rest...> {

  using wrapper_t = uninitialized<First>;
  using rest_t = variadic_union<Rest...>;

  constexpr variadic_union() : rest() {}

  template <typename... Args>
  constexpr variadic_union(in_place_index_t<0>, Args&&... args)
      : wrapper(in_place_index<0>, std::forward<Args>(args)...) {}

  template <std::size_t N, typename... Args>
  constexpr variadic_union(in_place_index_t<N>, Args&&... args)
      : rest(in_place_index<N - 1>, std::forward<Args>(args)...) {}

  template <std::size_t N, typename... Args>
  constexpr void construct(Args&&... args) {
    if constexpr (N == 0) {
      new (std::addressof(wrapper)) wrapper_t(in_place_index<0>, std::forward<Args>(args)...);
    } else {
      rest.template construct<N - 1>(std::forward<Args>(args)...);
    }
  }

  wrapper_t wrapper;
  rest_t rest;
};

template <typename Union>
constexpr decltype(auto) get(in_place_index_t<0>, Union&& u) noexcept {
  return std::forward<Union>(u).wrapper.get();
}

template <std::size_t N, typename Union>
constexpr decltype(auto) get(in_place_index_t<N>, Union&& u) noexcept {
  return get(in_place_index<N - 1>, std::forward<Union>(u).rest);
}

// ------------------------------ variant_storage ---------------------------- //

template <typename... Types>
struct variant_storage;

template <typename... Types>
struct variant_storage {
  using storage_t = variadic_union<Types...>;

  constexpr variant_storage() : m_storage() {}

  template <std::size_t N, typename... Args>
  constexpr variant_storage(in_place_index_t<N>, Args&&... args)
      : m_index{N}, m_storage(in_place_index<N>, std::forward<Args>(args)...) {}

  constexpr bool valueless_by_exception() const noexcept {
    return m_index == variant_npos;
  }

  constexpr void reset() noexcept {
    if (!this->valueless_by_exception()) {
      impl::visit([this]<typename T>(T& value) { value.~T(); }, static_cast<variant<Types...>&>(*this));
    }
    this->m_index = variant_npos;
  }

  std::size_t m_index{0};
  storage_t m_storage;
};

} // namespace impl
