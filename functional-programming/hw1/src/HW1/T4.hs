module HW1.T4 where

import HW1.T3

tfoldr :: (a -> b -> b) -> b -> Tree a -> b

treeToList :: Tree a -> [a]
treeToList = tfoldr (:) []

tfoldr _ z Leaf = z
tfoldr f z (Branch _ l x r) = tfoldr f (f x (tfoldr f z r)) l
