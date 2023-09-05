#pragma once

#include "variant_destructor.h"
#include <utility>

using impl::visit;

template <typename... Types>
struct variant : impl::destructor_base<Types...> {

  static_assert(sizeof...(Types) > 0, "variant must have at least one alternative");
  static_assert(!(std::is_reference_v<Types> || ...), "variant must have no reference alternative");
  static_assert(!(std::is_void_v<Types> || ...), "variant must have no void alternative");

private:
  using base = impl::destructor_base<Types...>;

  template <typename T>
  static constexpr bool occurs_once = impl::occurs_once<T, Types...>;

  template <typename T>
  static constexpr std::size_t accepted_index_v = impl::accepted_index_v<T, variant>;

  template <typename T>
  static constexpr bool not_self = !std::is_same_v<std::remove_cvref_t<T>, variant>;

  template <std::size_t N>
  requires(N < variant_size_v<variant>) using nth_variant_alternative_t = variant_alternative_t<N, variant>;

  template <typename T>
  requires(not_self<T>) using accepted_t = nth_variant_alternative_t<accepted_index_v<T>>;

  template <std::size_t N>
  using nth_t = impl::nth_t<N, Types...>;

  template <std::size_t N>
  static constexpr bool in_bounds = N < sizeof...(Types);

  template <typename T>
  static constexpr bool index_of_v = impl::index_of_v<T, Types...>;

public:
  // --------------------------------- Default constructor --------------------------------- //

  constexpr variant() noexcept(impl::NothrowDefaultConstructible<nth_t<0>>)
      requires(impl::DefaultConstructible<nth_t<0>>)
      : base(in_place_index<0>){};

  // --------------------------------- Copy constructor ---------------------------------- //

  constexpr variant(const variant&) requires(!impl::CopyConstructible<Types...>) = delete;

  constexpr variant(const variant&) requires(impl::TriviallyCopyConstructible<Types...>) = default;

  constexpr variant(const variant& other) noexcept(impl::NothrowCopyConstructible<Types...>)
      requires(impl::CopyConstructible<Types...> && !impl::TriviallyCopyConstructible<Types...>) {
    if (other.valueless_by_exception()) {
      this->m_index = variant_npos;
      return;
    }
    impl::visit_by_index([this, &other]<std::size_t I>(in_place_index_t<I>) { emplace<I>(get<I>(other)); }, other);
  }

  // --------------------------------- Move constructor ---------------------------------- //

  constexpr variant(variant&&) requires(!impl::MoveConstructible<Types...>) = delete;

  constexpr variant(variant&&) requires(impl::TriviallyMoveConstructible<Types...>) = default;

  constexpr variant(variant&& other) noexcept(impl::NothrowMoveConstructible<Types...>)
      requires(impl::MoveConstructible<Types...> && !impl::TriviallyMoveConstructible<Types...>) {
    if (other.valueless_by_exception()) {
      this->m_index = variant_npos;
      return;
    }
    impl::visit_by_index([this, &other]<std::size_t I>(in_place_index_t<I>) { emplace<I>(std::move(get<I>(other))); },
                         other);
  }

  // --------------------------------- Copy assignment --------------------------------- //

  constexpr variant& operator=(const variant&) requires(!impl::CopyAssignable<Types...>) = delete;

  constexpr variant& operator=(const variant&) requires(impl::TriviallyCopyAssignable<Types...>) = default;

  constexpr variant& operator=(const variant& other) noexcept(impl::NothrowCopyAssignable<Types...>)
      requires(impl::CopyAssignable<Types...> && !impl::TriviallyCopyAssignable<Types...>) {
    if (this == &other || (this->valueless_by_exception() && other.valueless_by_exception())) {
      return *this;
    }

    if (other.valueless_by_exception()) {
      this->reset();
      return *this;
    }

    impl::visit_by_index(
        [this, &other]<std::size_t I>(in_place_index_t<I>) {
          if (index() == I) {
            get<I>(*this) = get<I>(other);
          } else {
            emplace<I>(get<I>(other));
          }
        },
        other);

    return *this;
  }

  // --------------------------------- Move assignment ------------------------------------ //

  constexpr variant& operator=(variant&&) requires(!impl::MoveAssignable<Types...>) = delete;

  constexpr variant& operator=(variant&&) requires(impl::TriviallyMoveAssignable<Types...>) = default;

  constexpr variant& operator=(variant&& other) noexcept(impl::NothrowMoveAssignable<Types...>)
      requires(impl::MoveAssignable<Types...> && !impl::TriviallyMoveAssignable<Types...>) {
    if (this == &other && (this->valueless_by_exception() && other.valueless_by_exception())) {
      return *this;
    }

    if (other.valueless_by_exception()) {
      this->reset();
      return *this;
    }

    impl::visit_by_index(
        [this, &other]<std::size_t I>(in_place_index_t<I>) {
          if (index() == I) {
            get<I>(*this) = std::move(get<I>(other));
          } else {
            emplace<I>(std::move(get<I>(other)));
          }
        },
        other);

    return *this;
  }

  // --------------------------------- In place constructors --------------------------------- //

  template <class T, class... Args>
  constexpr explicit variant(in_place_type_t<T>, Args&&... args)
      requires(occurs_once<T>&& std::is_constructible_v<T, Args...>)
      : base(in_place_index<impl::index_of_v<T, Types...>>, std::forward<Args>(args)...) {}

  template <std::size_t N, class... Args, typename T = nth_variant_alternative_t<N>>
  constexpr explicit variant(in_place_index_t<N>, Args&&... args)
      requires(in_bounds<N>&& std::is_constructible_v<T, Args...>)
      : base(in_place_index<N>, std::forward<Args>(args)...) {}

  // --------------------------------- Converting constructors --------------------------------- //

  template <typename T, typename Tj = accepted_t<T&&>>
  constexpr variant(T&& t) noexcept(std::is_nothrow_constructible_v<Tj, T>)
      requires(impl::not_in_place_tag<T>&& occurs_once<Tj>&& std::is_constructible_v<Tj, T>)
      : variant(in_place_index<accepted_index_v<T&&>>, std::forward<T>(t)) {}

  template <typename T, typename Tj = accepted_t<T&&>>
  constexpr variant&
  operator=(T&& t) noexcept(std::is_nothrow_constructible_v<Tj, T>&& std::is_nothrow_assignable_v<Tj&, T>)
      requires(occurs_once<Tj>&& std::is_constructible_v<Tj, T>&& std::is_assignable_v<Tj&, T>) {
    constexpr std::size_t index = accepted_index_v<T&&>;
    if (index == this->index()) {
      get<index>(*this) = std::forward<T>(t);
    } else {
      emplace<index>(Tj(std::forward<T>(t)));
    }
    return *this;
  }

  // ------------------------------------- Destructor ------------------------------------------ //

  constexpr ~variant() = default;

  // --------------------------------------- Index --------------------------------------------- //

  constexpr std::size_t index() const noexcept {
    return this->m_index;
  }

  // --------------------------------------- Emplace ------------------------------------------- //

  template <class T, class... Args>
  constexpr T& emplace(Args&&... args) requires(std::is_constructible_v<T, Args...>&& occurs_once<T>) {
    constexpr std::size_t index = index_of_v<T>;
    return emplace<index>(std::forward<Args>(args)...);
  }

  template <std::size_t N, class... Args>
  constexpr nth_variant_alternative_t<N>& emplace(Args&&... args)
      requires(std::is_constructible_v<nth_variant_alternative_t<N>, Args...>&& in_bounds<N>) {
    this->reset();
    try {
      this->m_storage.template construct<N>(std::forward<Args>(args)...);
      this->m_index = N;
    } catch (...) {
      this->m_index = variant_npos;
    }
    return get<N>(*this);
  }

  // --------------------------------------- Swap --------------------------------------------- //

  void swap(variant& other) noexcept(((std::is_nothrow_move_constructible_v<Types> &&
                                       std::is_nothrow_swappable_v<Types>)&&...)) {
    if (this == &other || (this->valueless_by_exception() && other.valueless_by_exception())) {
      return;
    }

    if (this->valueless_by_exception()) {
      *this = std::move(other);
      other.reset();
      return;
    }

    if (other.valueless_by_exception()) {
      other.swap(*this);
      return;
    }

    if (index() == other.index()) {
      impl::visit_by_index(
          [this, &other]<std::size_t I>(in_place_index_t<I>) {
            using std::swap;
            swap(get<I>(*this), get<I>(other));
          },
          *this);
      return;
    }

    variant tmp(std::move(*this));
    *this = std::move(other);
    other = std::move(tmp);
  }
};

// --------------------------------------- Holds Alternative -------------------------------- //

template <typename T, typename... Types>
constexpr bool holds_alternative(const variant<Types...>& v) noexcept {
  static_assert(impl::occurs_once<T, Types...>, "T must occur exactly once in Types...");
  return v.index() == impl::index_of_v<T, Types...>;
}

// ----------------------------------------- Getters --------------------------------------- //

template <class T, typename... Types>
constexpr std::add_pointer_t<T> get_if(variant<Types...>* pv) noexcept {
  static_assert(impl::occurs_once<T, Types...>, "T must occur exactly once in Types...");
  return get_if<impl::index_of_v<T, Types...>>(pv);
}

template <class T, typename... Types>
constexpr std::add_pointer_t<const T> get_if(const variant<Types...>* pv) noexcept {
  static_assert(impl::occurs_once<T, Types...>, "T must occur exactly once in Types...");
  return get_if<impl::index_of_v<T, Types...>>(pv);
}

template <std::size_t N, typename... Types>
constexpr std::add_pointer_t<variant_alternative_t<N, variant<Types...>>> get_if(variant<Types...>* pv) noexcept {
  static_assert(N < sizeof...(Types), "N must be less than sizeof...(Types)");
  return pv && pv->index() == N ? std::addressof(get<N>(*pv)) : nullptr;
}

template <std::size_t N, typename... Types>
constexpr std::add_pointer_t<const variant_alternative_t<N, variant<Types...>>>
get_if(const variant<Types...>* pv) noexcept {
  static_assert(N < sizeof...(Types), "N must be less than sizeof...(Types)");
  return pv && pv->index() == N ? std::addressof(get<N>(*pv)) : nullptr;
}

template <typename T, typename Variant>
constexpr decltype(auto) get(Variant&& v) {
  return get<impl::variant_index_of_v<T, std::decay_t<Variant>>>(std::forward<Variant>(v));
}

template <std::size_t N, typename Variant>
constexpr decltype(auto) get(Variant&& v) {
  if (v.index() != N) {
    throw bad_variant_access();
  }
  return get(in_place_index<N>, std::forward<Variant>(v).m_storage);
}

// --------------------------------- Relational operators --------------------------------- //

template <typename... Types>
constexpr bool operator==(const variant<Types...>& lhs, const variant<Types...>& rhs) {
  if (lhs.index() != rhs.index()) {
    return false;
  }
  if (lhs.valueless_by_exception()) {
    return true;
  }
  return impl::visit_by_index([&lhs, &rhs]<std::size_t I>(in_place_index_t<I>) { return get<I>(lhs) == get<I>(rhs); },
                              lhs);
}

template <class... Types>
constexpr bool operator!=(const variant<Types...>& lhs, const variant<Types...>& rhs) {
  return !(lhs == rhs);
}

template <typename... Types>
constexpr bool operator<(const variant<Types...>& lhs, const variant<Types...>& rhs) {
  if (rhs.valueless_by_exception()) {
    return false;
  }
  if (lhs.valueless_by_exception()) {
    return true;
  }
  if (lhs.index() != rhs.index()) {
    return lhs.index() < rhs.index();
  }
  return impl::visit_by_index([&lhs, &rhs]<std::size_t I>(in_place_index_t<I>) { return get<I>(lhs) < get<I>(rhs); },
                              lhs);
}

template <class... Types>
constexpr bool operator>(const variant<Types...>& lhs, const variant<Types...>& rhs) {
  return rhs < lhs;
}

template <class... Types>
constexpr bool operator<=(const variant<Types...>& lhs, const variant<Types...>& rhs) {
  return !(rhs < lhs);
}

template <class... Types>
constexpr bool operator>=(const variant<Types...>& lhs, const variant<Types...>& rhs) {
  return !(lhs < rhs);
}
