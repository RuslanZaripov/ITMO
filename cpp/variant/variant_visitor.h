#pragma once

#include <array>
#include <utility>

namespace impl {

// ------------------------ Custom Classes and Constants ----------------------------- //

template <std::size_t N, typename... Vs>
constexpr std::size_t nth_variant_size_v = variant_size_v<std::decay_t<nth_t<0, Vs...>>>;

template <std::size_t N, bool out_of_bounds, typename... Vs>
struct custom_index_sequence {
  using type = std::index_sequence<>;
};

template <std::size_t N, typename... Vs>
struct custom_index_sequence<N, true, Vs...> {
  using type = std::make_index_sequence<nth_variant_size_v<N, Vs...>>;
};

template <std::size_t N, typename... Vs>
using custom_index_sequence_t = typename custom_index_sequence<N, (N < sizeof...(Vs)), Vs...>::type;

template <class... Types>
constexpr std::array<typename std::common_type_t<Types...>, sizeof...(Types)> make_array(Types&&... t) {
  return {std::forward<Types>(t)...};
}

// --------------------------- Visit Implementation -------------------------------- //

template <bool Indexed, typename F, std::size_t VIndex, typename HeadSeq, typename TailSeq, typename... Vs>
struct make_fmatrix_impl;

template <typename F, std::size_t VIndex, std::size_t... Index, typename... Vs>
struct make_fmatrix_impl<true, F, VIndex, std::index_sequence<Index...>, std::index_sequence<>, Vs...> {
  static constexpr auto generate() {
    return [](F&& f) { return std::forward<F>(f)(in_place_index<Index>...); };
  }
};

template <typename F, std::size_t VIndex, std::size_t... Index, typename... Vs>
struct make_fmatrix_impl<false, F, VIndex, std::index_sequence<Index...>, std::index_sequence<>, Vs...> {
  static constexpr auto generate() {
    return [](F&& f, Vs&&... vs) { return std::forward<F>(f)(get<Index>(std::forward<Vs>(vs))...); };
  }
};

template <bool Indexed, typename F, std::size_t VIndex, std::size_t... HeadSeq, std::size_t... TailNum, typename... Vs>
struct make_fmatrix_impl<Indexed, F, VIndex, std::index_sequence<HeadSeq...>, std::index_sequence<TailNum...>, Vs...> {
  static constexpr auto generate() {
    return make_array(make_fmatrix_impl<Indexed, F, VIndex + 1, std::index_sequence<HeadSeq..., TailNum>,
                                        custom_index_sequence_t<VIndex + 1, Vs...>, Vs...>::generate()...);
  }
};

template <bool Indexed, typename F, typename... Vs>
constexpr auto make_fmatrix() {
  return make_fmatrix_impl<Indexed, F, 0, std::index_sequence<>, custom_index_sequence_t<0, Vs...>, Vs...>::generate();
}

template <typename T>
constexpr T&& at_impl(T&& elem) {
  return std::forward<T>(elem);
}

template <typename T, typename... Is>
constexpr auto&& at_impl(T&& elems, std::size_t i, Is... is) {
  return at_impl(std::forward<T>(elems)[i], is...);
}

template <typename T, typename... Is>
constexpr auto&& at(T&& elems, Is... is) {
  return at_impl(std::forward<T>(elems), is...);
}

// ------------------------------ Visit Functions ------------------------------ //

template <class Visitor, class... Variants>
constexpr decltype(auto) visit(Visitor&& visitor, Variants&&... variants) {
  if ((variants.valueless_by_exception() || ...)) {
    throw bad_variant_access{};
  }
  constexpr auto fmatrix = make_fmatrix<false, Visitor&&, Variants&&...>();
  return at(fmatrix, variants.index()...)(std::forward<Visitor>(visitor), std::forward<Variants>(variants)...);
}

template <class Visitor, class... Variants>
constexpr decltype(auto) visit_by_index(Visitor&& visitor, Variants&&... variants) {
  if ((variants.valueless_by_exception() || ...)) {
    throw bad_variant_access{};
  }
  constexpr auto fmatrix = make_fmatrix<true, Visitor&&, Variants&&...>();
  return at(fmatrix, variants.index()...)(std::forward<Visitor>(visitor));
}

} // namespace impl
