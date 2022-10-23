module HW0.T6 where

import Data.Char (isSpace)
import HW0.T1 (distrib)

a = distrib (Left ("AB" ++ "CD" ++ "EF"))
b = map isSpace "Hello, World"
c = if 1 > 0 || error "X" then "Y" else "Z"

a_whnf = (Left "ABCDEF", Left "ABCDEF")
b_whnf = [False, False, False, False, False, False, True, False, False, False, False, False]
c_whnf = "Y"
