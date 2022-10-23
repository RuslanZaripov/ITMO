module HW1.T2 where

import Numeric.Natural

data N = Z | S N deriving (Show)

nplus :: N -> N -> N
nplus Z n = n
nplus (S m) n = S (nplus m n)

nmult :: N -> N -> N
nmult Z _ = Z
nmult (S m) n = nplus n (nmult m n)

-- subtraction     (Nothing if result is negative)
nsub :: N -> N -> Maybe N
nsub Z     Z     = Just Z
nsub Z     (S _) = Nothing
nsub (S m) Z     = Just (S m)
nsub (S m) (S n) = nsub m n

-- comparison      (Do not derive Ord)
ncmp :: N -> N -> Ordering
ncmp Z     Z     = EQ
ncmp Z     (S _) = LT
ncmp (S _) Z     = GT
ncmp (S m) (S n) = ncmp m n

nFromNatural :: Natural -> N
nFromNatural 0 = Z
nFromNatural n = S (nFromNatural (n-1))

nToNum :: Num a => N -> a
nToNum Z     = 0
nToNum (S n) = 1 + nToNum n

nEven, nOdd :: N -> Bool
nEven Z     = True
nEven (S n) = nOdd n
nOdd Z     = False
nOdd (S n) = nEven n

-- In ndiv and nmod, the behavior in case of division by zero is not specified.
-- integer division
ndiv :: N -> N -> N
ndiv m n = case nsub m n of
             Nothing -> Z
             Just m' -> S (ndiv m' n)

-- modulo operation
nmod :: N -> N -> N
nmod m n = case nsub m n of
             Nothing -> m
             Just m' -> nmod m' n
