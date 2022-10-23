module HW0.T3 where

s :: (a -> b -> c) -> (a -> b) -> (a -> c)
-- f :: a -> b -> c
-- g :: a -> b
-- x :: a
-- g x :: b
-- f x :: b -> c
-- f x (g x) :: c
s f g x = f x (g x)

k :: a -> b -> a
-- x :: a
-- y :: b
-- k x y == x :: a
k x y = x

i :: a -> a
-- x :: a
-- i x :: a
i x = x

compose :: (b -> c) -> (a -> b) -> (a -> c)
-- f :: b -> c
-- g :: a -> b
-- x :: a
-- g x :: b
-- f (g x) :: c
compose f g x = f (g x)

contract :: (a -> a -> b) -> (a -> b)
-- f :: a -> a -> b
-- x :: a
-- f x x :: b
contract f x = f x x

permute :: (a -> b -> c) -> (b -> a -> c)
-- f :: a -> b -> c
-- x :: b
-- y :: a
-- f y x :: c
permute f x y = f y x
