#include <iostream>
#include <cmath>
#include <random>
#include <algorithm>
#include <functional>
#include <sstream>

#define ui128 __uint128_t

ui128 mulByMod(ui128 x, ui128 y, ui128 n) {
    return x * y % n;
}

ui128 power(ui128 base, ui128 d, ui128 mod) {
    ui128 result = 1;
    for (; d > 0; d >>= 1) {
        if (d & 1) { result = mulByMod(result, base, mod); }
        base = mulByMod(base, base, mod);
    }
    return result;
}

bool isComposite(ui128 mod, ui128 a, ui128 d, int degree) {
    ui128 num = power(a % mod, d, mod);
    if (num == 1 || num == mod - 1) return false;
    for (int i = 1; i < degree; i++) {
        if ((num = mulByMod(num, num, mod)) == mod - 1) return false;
    }
    return true;
}

std::string isPrime(ui128 number) {
    std::vector<int> primeNumbers{ 2, 3, 5, 7, 11, 13, 17, 19, 23, 29 };

    if (number < 2) return "NO";
    if (std::any_of(primeNumbers.cbegin(), primeNumbers.cend(),
        [&number](int primeNumber) { return primeNumber == number; }
    )) {
        return "YES";
    }

    int degree = 0;
    ui128 k = number - 1;
    for (; (k & 1) == 0; k >>= 1, degree++) {}

    if (std::any_of(primeNumbers.cbegin(), primeNumbers.cend(),
        [&number, &k, &degree](int primeNumber) { return isComposite(number, primeNumber, k, degree); }
    )) {
        return "NO";
    }

    return "YES";
}

int main() {
    int n;
    std::cin >> n;
    std::stringstream ss;
    for (int i = 0; i < n; i++) {
        unsigned long long number;
        std::cin >> number;
        ss << isPrime(number) << '\n';
    }
    std::cout << ss.str() << '\n';
}