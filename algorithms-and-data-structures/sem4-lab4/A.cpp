#include <iostream>
#include <vector>
#include <sstream>
#include <cmath>

#define ll long long

std::string primeFactorization(ll n) {
    std::stringstream ss;
    ll num = 2;
    while (std::pow(num, 2) <= n) {
        while (n % num == 0) {
            ss << num << " ";
            n /= num;
        }
        num += 1;
    }
    if (n > 1) {
        ss << n << " ";
    }
    return ss.str();
}

int main() {
    ll n;
    std::cin >> n;
    std::cout << primeFactorization(n) << std::endl;
    return 0;
}