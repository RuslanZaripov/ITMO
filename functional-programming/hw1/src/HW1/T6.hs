module HW1.T6 where

-- foldMap :: (Foldable t, Monoid m) => (a -> m) -> t a -> m

mcat :: Monoid a => [Maybe a] -> a
mcat = foldMap toMonoid
  where
    toMonoid (Just x) = x
    toMonoid Nothing  = mempty

epart :: (Monoid a, Monoid b) => [Either a b] -> (a, b)
epart = foldMap f
  where
    f (Left a) = (a, mempty)
    f (Right b) = (mempty, b)
