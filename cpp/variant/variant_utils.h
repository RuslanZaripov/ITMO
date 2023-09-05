#pragma once

template <typename... Types>
struct variant;

// ------------------------------ constants ------------------------------ //

inline constexpr std::size_t variant_npos = -1;

// ------------------------------ Tags ----------------------------------- //

template <typename>
struct in_place_type_t {};

template <class T>
inline constexpr in_place_type_t<T> in_place_type{};

template <std::size_t>
struct in_place_index_t {};

template <std::size_t I>
inline constexpr in_place_index_t<I> in_place_index{};

// ------------------------ Helper Classes ----------------------------- //

// bad variant access

struct bad_variant_access : std::exception {
  const char* what() const noexcept override {
    return "bad_variant_access";
  }
};

// variant_size

template <typename T>
struct variant_size;

template <typename... Types>
struct variant_size<variant<Types...>> : std::integral_constant<std::size_t, sizeof...(Types)> {};

template <typename... Types>
struct variant_size<const variant<Types...>> : std::integral_constant<std::size_t, sizeof...(Types)> {};

template <typename T>
inline constexpr std::size_t variant_size_v = variant_size<T>::value;

// variant_alternative

template <std::size_t N, typename Variant>
struct variant_alternative;

template <std::size_t N, typename First, typename... Rest>
struct variant_alternative<N, variant<First, Rest...>> : variant_alternative<N - 1, variant<Rest...>> {};

template <typename First, typename... Rest>
struct variant_alternative<0, variant<First, Rest...>> {
  using type = First;
};

template <std::size_t N, typename Variant>
using variant_alternative_t = typename variant_alternative<N, Variant>::type;

template <std::size_t N, typename Variant>
struct variant_alternative<N, const Variant> {
  using type = std::add_const_t<variant_alternative_t<N, Variant>>;
};

namespace impl {

// ------------------------ Custom Classes ----------------------------- //

// get nth type in a Types parameter pack

template <std::size_t N, typename... Types>
struct nth;

template <std::size_t N, typename First, typename... Rest>
struct nth<N, First, Rest...> : nth<N - 1, Rest...> {};

template <typename First, typename... Rest>
struct nth<0, First, Rest...> {
  using type = First;
};

template <std::size_t N, typename... Types>
using nth_t = typename nth<N, Types...>::type;

// return the first appearance of T in a Types parameter pack

template <typename T, typename... Types>
struct index_of : std::integral_constant<std::size_t, 0> {};

template <typename T, typename... Types>
inline constexpr std::size_t index_of_v = index_of<T, Types...>::value;

template <typename T, typename First, typename... Rest>
struct index_of<T, First, Rest...>
    : std::integral_constant<std::size_t, std::is_same_v<T, First> ? 0 : index_of_v<T, Rest...> + 1> {};

// same as index_of_v but input typename is Variant

template <typename T, typename Variant>
struct variant_index_of;

template <typename T, typename... Types>
struct variant_index_of<T, variant<Types...>> {
  static constexpr std::size_t value = impl::index_of<T, Types...>::value;
};

template <typename T, typename Variant>
constexpr std::size_t variant_index_of_v = variant_index_of<T, Variant>::value;

// determine if a type T is one of in_place tags

template <typename T>
struct is_in_place_tag : std::false_type {};

template <typename T>
struct is_in_place_tag<in_place_type_t<T>> : std::true_type {};

template <std::size_t N>
struct is_in_place_tag<in_place_index_t<N>> : std::true_type {};

template <typename T>
concept not_in_place_tag = !is_in_place_tag<std::remove_cvref_t<T>>::value;

// count occurrences of type T in a Tuple

template <typename T, typename Tuple>
struct count_occurrences;

template <typename T, typename Tuple>
inline constexpr std::size_t occurrences = count_occurrences<T, Tuple>::value;

template <typename T, typename... Types>
struct count_occurrences<T, std::tuple<Types...>> : std::integral_constant<std::size_t, 0> {};

template <typename T, typename First, typename... Rest>
struct count_occurrences<T, std::tuple<First, Rest...>>
    : std::integral_constant<std::size_t, occurrences<T, std::tuple<Rest...>> + std::is_same_v<T, First>> {};

template <typename T, typename... Types>
concept occurs_once = occurrences<T, std::tuple<Types...>> == 1;

// a sane variant converting constructor impl

template <typename T, typename Ti>
concept constraint =
    (!std::is_same_v<std::remove_cv_t<Ti>, bool> || std::is_same_v<std::remove_cvref_t<T>, bool>)&&requires(T t) {
  Ti{std::forward<T>(t)};
};

template <std::size_t I, typename T, typename Ti>
struct generate_function {
  static std::integral_constant<std::size_t, I> function(Ti) requires(constraint<T, Ti>);
};

template <typename T, typename Variant, typename = std::make_index_sequence<variant_size_v<Variant>>>
struct generate_functions;

template <typename T, typename... Ti, std::size_t... I>
struct generate_functions<T, variant<Ti...>, std::index_sequence<I...>> : generate_function<I, T, Ti>... {
  using generate_function<I, T, Ti>::function...;
};

template <typename T, typename Variant>
using function_t = decltype(generate_functions<T, Variant>::function(std::declval<T>()));

template <typename T, typename Variant>
struct accepted_index : std::integral_constant<std::size_t, variant_npos> {};

template <typename T>
concept has_type_member = requires {
  typename T::type;
};

template <typename T, typename Variant>
requires(has_type_member<function_t<T, Variant>>) struct accepted_index<T, Variant> : function_t<T, Variant> {};

template <typename T, typename Variant>
inline constexpr std::size_t accepted_index_v = accepted_index<T, Variant>::value;

} // namespace impl
