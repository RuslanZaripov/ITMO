#pragma once

namespace impl {

template <typename... Types>
concept TriviallyDestructible = (std::is_trivially_destructible_v<Types> && ...);

template <typename T>
concept NothrowDefaultConstructible = std::is_nothrow_default_constructible_v<T>;

template <typename T>
concept DefaultConstructible = std::is_default_constructible_v<T>;

// copy ctor concepts

template <typename... Types>
concept CopyConstructible = (std::is_copy_constructible_v<Types> && ...);

template <typename... Types>
concept NothrowCopyConstructible = (std::is_nothrow_copy_constructible_v<Types> && ...);

template <typename... Types>
concept TriviallyCopyConstructible = (std::is_trivially_copy_constructible_v<Types> && ...);

// move ctor concepts

template <typename... Types>
concept MoveConstructible = (std::is_move_constructible_v<Types> && ...);

template <typename... Types>
concept NothrowMoveConstructible = (std::is_nothrow_move_constructible_v<Types> && ...);

template <typename... Types>
concept TriviallyMoveConstructible = (std::is_trivially_move_constructible_v<Types> && ...);

// copy assignment concepts

template <typename... Types>
concept TriviallyCopyAssignable = (std::is_trivially_copy_assignable_v<Types> && ...) &&
                                  TriviallyCopyConstructible<Types...> && TriviallyDestructible<Types...>;

template <typename... Types>
concept CopyAssignable = (std::is_copy_assignable_v<Types> && ...) && CopyConstructible<Types...>;

template <typename... Types>
concept NothrowCopyAssignable = (std::is_nothrow_copy_assignable_v<Types> && ...) && NothrowCopyConstructible<Types...>;

// move assignment concepts

template <typename... Types>
concept TriviallyMoveAssignable = (std::is_trivially_move_assignable_v<Types> && ...) &&
                                  TriviallyMoveConstructible<Types...> && TriviallyDestructible<Types...>;

template <typename... Types>
concept MoveAssignable = (std::is_move_assignable_v<Types> && ...) && MoveConstructible<Types...>;

template <typename... Types>
concept NothrowMoveAssignable = (std::is_nothrow_move_assignable_v<Types> && ...) && NothrowMoveConstructible<Types...>;

} // namespace impl
