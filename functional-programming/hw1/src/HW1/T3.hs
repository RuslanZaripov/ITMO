module HW1.T3 where

data Tree a = Leaf | Branch (Int, Int) (Tree a) a (Tree a)

--Tip 1: in order to maintain the CachedSize invariant, define a helper function:

makeBranch :: Tree a -> a -> Tree a -> Tree a
makeBranch l x r = Branch (1 + tsize l + tsize r, 1 + max (tdepth l) (tdepth r)) l x r

-- Tip 2: the Balanced invariant is the hardest to maintain, so implement it last. Search for "tree rotation".

-- | Size of the tree, O(1).
tsize :: Tree a -> Int
tsize Leaf = 0
tsize (Branch (size, _) _ _ _) = size

-- | Depth of the tree.
tdepth :: Tree a -> Int
tdepth Leaf = 0
tdepth (Branch (_, depth) _ _ _) = depth

-- | Check if the element is in the tree, O(log n)
tmember :: Ord a => a -> Tree a -> Bool
tmember _ Leaf = False
tmember x (Branch _ l y r) = case compare x y of
    LT -> tmember x l
    EQ -> True
    GT -> tmember x r

-- | Insert an element into the tree, O(log n)
tinsert :: Ord a => a -> Tree a -> Tree a
tinsert x Leaf = makeBranch Leaf x Leaf
tinsert x (Branch meta l y r) = case compare x y of
    LT -> makeBranch (tinsert x l) y r
    EQ -> Branch meta l x r
    GT -> makeBranch l y (tinsert x r)

-- | Build a tree from a list, O(n log n)
tFromList :: Ord a => [a] -> Tree a
tFromList = foldl (flip tinsert) Leaf
