#include <iostream>
#include <valarray>

#define ll long long

ll gcd(ll a, ll b, ll& x, ll& y) {
    if (a == 0) {
        x = 0;
        y = 1;
        return b;
    }
    ll x1, y1;
    ll d = gcd(b % a, a, x1, y1);
    x = y1 - (b / a) * x1;
    y = x1;
    return d;
}

ll pow(ll number, ll degree, ll n) {
    if (degree == 0) {
        return 1;
    }
    if (degree % 2 == 1) {
        return (pow(number, degree - 1, n) * number) % n;
    }
    else {
        ll b = (pow(number, degree >>= 1, n)) % n;
        return (b * b) % n;
    }
}

ll get_reverse_element(ll a, ll MOD) {
    ll x, y;
    gcd(a, MOD, x, y);
    return (x % MOD + MOD) % MOD;
}

ll getPhi(ll n) {
    for (int i = 2; i * i <= n; i++) {
        if (n % i == 0) {
            return (i - 1) * (n / i - 1);
        }
    }
    return -1;
}

int main() {
    ll n, e, C;
    std::cin >> n >> e >> C;
    ll d = get_reverse_element(e, getPhi(n));
    std::cout << pow(C, d, n) << '\n';
    return 0;
}
