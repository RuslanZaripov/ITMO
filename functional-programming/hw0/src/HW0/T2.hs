module HW0.T2 where

import Data.Void (Void)

type Not a = a -> Void

doubleNeg :: a -> Not (Not a)
-- a -> (a -> Void) -> Void
doubleNeg a f = f a

reduceTripleNeg :: Not (Not (Not a)) -> Not a
-- (Not (Not a) -> Void) -> a -> Void
-- f :: (Not (Not a) -> Void)
-- x :: a
-- doubleNeg x :: Not (Not a)
-- f (doubleNeg x) :: Void
reduceTripleNeg f x = f (doubleNeg x)