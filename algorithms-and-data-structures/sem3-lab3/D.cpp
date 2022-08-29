#include <iostream>
#include <vector>

void prefFunction(std::string &s, std::vector<int> &pref)
{
    pref[0] = -1;
    for (int i = 1; i <= s.length(); ++i)
    {
        int k = pref[i - 1];
        while (k != -1 && s[k] != s[i - 1])
        {
            k = pref[k];
        }
        pref[i] = k + 1;
    }
}

int main()
{
    std::ios_base::sync_with_stdio(false);
    std::cin.tie(NULL);

    std::string p, t;
    std::cin >> p >> t;

    std::string line = p + "#" + t;

    std::vector<int> pref(line.size() + 1);
    prefFunction(line, pref);

    std::vector<int> pos;

    int pSize = p.size();
    for (int i = p.size() + 2; i < pref.size(); ++i)
    {
        if (pref[i] == pSize)
        {
            pos.push_back(i - 2 * pSize);
        }
    }

    std::cout << pos.size() << std::endl;
    for (int p : pos)
    {
        std::cout << p << " ";
    }
}