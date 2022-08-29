#include <iostream>
#include <vector>

void prefFunction(std::string& s, std::vector<int>& pref)
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
    std::string s;
    std::cin >> s;

    std::vector<int> pref(s.size() + 1);
    prefFunction(s, pref);

    for (int i = 1; i < pref.size(); ++i)
    {
        std::cout << pref[i] << " ";
    }
}