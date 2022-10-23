module HW0.T4 where

import Data.Function (fix)
import GHC.Natural

-- fix :: (a -> a) -> a

repeat' :: a -> [a]
map' :: (a -> b) -> [a] -> [b]
fib :: Natural -> Natural
fac :: Natural -> Natural

repeat' x = fix (x:)
map' f = fix (\g xs -> case xs of
                         [] -> []
                         (y:ys) -> f y : g ys)
fib = fix (\f n -> case n of
                      0 -> 0
                      1 -> 1
                      _ -> f (n-1) + f (n-2))
fac = fix (\f n -> case n of
                      0 -> 1
                      _ -> n * f (n-1))
