#include <iostream>

int main() {
    std::ios::sync_with_stdio(false);
    std::cin.tie(nullptr);
    std::cout.tie(nullptr);
    long long a, b, n, m;
    std::cin >> a >> b >> n >> m;
    if (m > n) {
        std::swap(a, b);
        std::swap(n, m);
    }
    long long num = a % m;
    long long add = n % m;
    long long i = 0;
    for (; num % m != b; num += add, i++) {}
    std::cout << a + i * n << '\n';
    return 0;
}