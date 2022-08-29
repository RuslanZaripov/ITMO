#include <iostream>
#include <vector>
#include <cmath>

const int PRIME = 31;
const long long MOD = 1e9 + 7;

std::vector<long long> POW;

long long hash(int l, int r, std::vector<long long>& p)
{
    return (p[r] - (p[r - 1] * POW[r - l + 1]) % MOD) % MOD;
}

int main()
{
    std::ios_base::sync_with_stdio(false);
    std::cin.tie(NULL);

    std::string str;
    std::cin >> str;

    int strSize = str.size();

    POW.assign(strSize + 1, 0);
    POW[0] = 1;
    for (int i = 1; i <= strSize; ++i)
    {
        POW[i] = (POW[i - 1] * PRIME) % MOD;
    }

    std::vector<long long> p(strSize + 1, 0);
    for (int i = 1; i <= strSize; ++i)
    {
        p[i] = ((p[i - 1] * PRIME) % MOD + (str[i - 1] - 'a' + 1)) % MOD;
    }

    int n;
    std::cin >> n;
    for (int i = 0; i < n; ++i)
    {
        int a, b, c, d;
        std::cin >> a >> b >> c >> d;

        std::cout << (hash(a, b, p) == hash(c, d, p) || str.substr(a - 1, b - a + 1) == str.substr(c - 1, d - c + 1) ? "Yes" : "No") << std::endl;
    }
}