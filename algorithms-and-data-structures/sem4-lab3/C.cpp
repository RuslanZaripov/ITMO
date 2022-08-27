#include <iostream>
#include <algorithm>

int main() {
    std::ios_base::sync_with_stdio(false);
    std::cin.tie(NULL);

    long long r1, s1, p1;
    std::cin >> r1 >> s1 >> p1;

    long long  r2, s2, p2;
    std::cin >> r2 >> s2 >> p2;

    std::cout << std::max<long long>({ 0, r1 - p2 - r2, p1 - s2 - p2, s1 - r2 - s2 }) << '\n';

    return 0;
}