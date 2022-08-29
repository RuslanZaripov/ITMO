#include <unordered_set>
#include <algorithm>
#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include <set>

namespace std
{
    template <>
    struct hash<std::set<int>>
    {
        size_t operator()(const std::set<int> &set) const
        {
            size_t hash = 89341275641807 * set.size();
            for (int elem : set)
            {
                hash ^= std::hash<std::string>()(std::to_string(elem));
            }
            return hash;
        }
    };
}

bool containsEmpty = false;
std::unordered_set<std::set<int>, std::hash<std::set<int>>> I;

std::unordered_set<std::set<int>, std::hash<std::set<int>>> independent;
bool testSecondAxiom(std::set<int> &A)
{
    std::set<int> B = A;
    if (independent.count(B))
    {
        return true;
    }
    for (auto elem : A)
    {
        B.erase(elem);
        if (B.empty())
        {
            return containsEmpty;
        }
        if (!I.count(B) || !testSecondAxiom(B))
        {
            return false;
        }
        independent.insert(B);
        B.insert(elem);
    }
    independent.insert(B);
    return true;
}

bool testThirdAxiom(std::set<int> &A, std::set<int> &B)
{
    std::set<int> diff;
    std::set_difference(A.begin(), A.end(), B.begin(), B.end(), std::inserter(diff, diff.begin()));

    for (auto elem : diff)
    {
        B.insert(elem);
        if (I.count(B))
        {
            return true;
        }
        B.erase(elem);
    }
    return false;
}

int main()
{
    std::ifstream in("check.in");
    std::ofstream out("check.out");

    int n, m;
    in >> n >> m;

    for (int i = 0; i < m; ++i)
    {
        int size = 0;
        in >> size;

        if (size == 0)
        {
            containsEmpty = true;
            continue;
        }

        std::set<int> subset;
        for (int j = 0; j < size; ++j)
        {
            int elem;
            in >> elem;
            subset.insert(elem);
        }
        I.insert(subset);
    }

    // ∅ ∈ I
    if (!containsEmpty)
    {
        out << "NO";
        return 0;
    }

    // if A ∈ I and B ⊂ A, then B ∈ I
    for (auto A : I)
    {
        if (!testSecondAxiom(A))
        {
            out << "NO";
            return 0;
        }
    }

    // if A,B ∈ I and |A|>|B|, then ∃x ∈ A∖B ∣ B∪{x} ∈ I
    for (auto B : I)
    {
        for (auto A : I)
        {
            if (A.size() > B.size())
            {
                if (!testThirdAxiom(A, B))
                {
                    out << "NO";
                    return 0;
                }
            }
        }
    }

    out << "YES";
}