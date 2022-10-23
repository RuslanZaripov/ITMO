module HW1.T5 where

import Data.List.NonEmpty (NonEmpty(..), toList, fromList)

splitOn :: Eq a => a -> [a] -> NonEmpty [a]
splitOn _ [] = [] :| []
splitOn c xs = case break (== c) xs of
  (ys, []) -> ys :| []
  (ys, _:zs) -> ys :| (toList $ splitOn c zs)

joinWith :: a -> NonEmpty [a] -> [a]
joinWith _ (x :| []) = x
joinWith c (x :| xs) = x ++ (c : joinWith c (fromList xs))
