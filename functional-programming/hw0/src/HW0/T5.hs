module HW0.T5 where

import GHC.Natural

type Nat a = (a -> a) -> a -> a

nz :: Nat a
nz _ x = x

ns :: Nat a -> Nat a
ns f g x = g (f g x)

nplus, nmult :: Nat a -> Nat a -> Nat a
nplus n m f x = n f (m f x)
nmult n m f x = n (m f) x

nFromNatural :: Natural -> Nat a
nFromNatural 0 = nz
nFromNatural n = ns (nFromNatural (n-1))

nToNum :: Num a => Nat a -> a
nToNum n = n (+1) 0
