#include <iostream>
#include <vector>

void zFunction(std::string &s, std::vector<int> &z)
{
    int left = 0, right = 0, strSize = s.size();
    for (int i = 1; i < strSize; ++i)
    {
        z[i] = std::max(0, std::min(right - i, z[i - left]));
        while (i + z[i] < strSize && s[z[i]] == s[i + z[i]])
        {
            ++z[i];
        }
        if (i + z[i] > right)
        {
            left = i;
            right = i + z[i];
        }
    }
}

int main()
{
    std::ios_base::sync_with_stdio(false);
    std::cin.tie(NULL);

    std::string s;
    std::cin >> s;

    int strSize = s.size();

    std::vector<int> z(strSize, 0);
    zFunction(s, z);

    for (int i = 1; i < z.size(); ++i)
    {
        if (strSize % i == 0 && i + z[i] == strSize)
        {
            std::cout << i << std::endl;
            return 0;
        }
    }

    std::cout << strSize << std::endl;
}