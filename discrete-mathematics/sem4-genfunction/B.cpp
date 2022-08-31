#include <iostream>
#include <vector>
#include <sstream>
#include <string>

#define ll long long

const ll MOD = 998244353LL;
const int POLYNOMIAL_SIZE = 1000;
const int COEFFICIENTS_SIZE = 101;
const int FACTORIAL_SIZE = 301;

std::vector<ll> ln_k;
std::vector<ll> exp_k;
std::vector<ll> sqrt_k;
std::vector<ll> factorial;

void calculate_factorials(int size) {
    factorial.assign(size, 1);
    for (int i = 1; i < size; ++i) {
        factorial[i] = (i * factorial[i - 1]) % MOD;
    }
}

ll pow(int a, ll n) {
    if (n == 0) {
        return 1;
    }
    if (n % 2 == 1) {
        return (pow(a, n - 1) * a) % MOD;
    }
    else {
        ll b = pow(a, n / 2);
        return (b * b) % MOD;
    }
}

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

ll get_reverse_element(ll a) {
    ll x, y;
    gcd(a, MOD, x, y);
    return (x % MOD + MOD) % MOD;
}

ll C(ll n, ll r) {
    if (r == 0) {
        return 1;
    }
    return ((factorial[n] * get_reverse_element(factorial[r])) % MOD
        * get_reverse_element(factorial[n - r])) % MOD;
}

void calculate_coefficients(unsigned int size) {
    ln_k.assign(size, 0);
    exp_k.assign(size, 0);
    sqrt_k.assign(size, 0);

    exp_k[0] = 1;
    sqrt_k[0] = 1;
    for (int k = 1; k < size; ++k) {
        ll sign = (k % 2 == 0 ? -1 : 1);
        ll elem = (pow(2, 2 * k - 1) * k) % MOD;
        elem = (elem * sign + MOD) % MOD;
        elem = (get_reverse_element(elem) * C(2 * k - 2, k - 1)) % MOD;
        sqrt_k[k] = elem;

        exp_k[k] = get_reverse_element(factorial[k]);

        elem = (sign * k + MOD) % MOD;
        ln_k[k] = get_reverse_element(elem);
    }
}

class Polynomial {
public:
    explicit Polynomial(unsigned int size) {
        this->coefficients.assign(size, 0);
    }

    explicit Polynomial(std::vector<ll> coefficients)
        : coefficients(std::move(coefficients))
    {
        if (this->size() != POLYNOMIAL_SIZE) {
            this->trim();
        }
    };

    ll operator[](unsigned int index) const {
        return index < coefficients.size() ? coefficients[index] : 0;
    }

    ll& operator[](unsigned int index) {
        return coefficients[index];
    }

    __attribute__((unused)) unsigned int degree() const {
        return size() - 1;
    }

    unsigned int size() const {
        return coefficients.size();
    }

    friend std::ostream& operator<<(std::ostream& out, Polynomial& p) {
        std::stringstream ss;
        for (ll coefficient : p.coefficients) {
            ss << coefficient << ' ';
        }
        out << ss.str();
        return out;
    }

    friend std::istream& operator>>(std::istream& input, Polynomial& p) {
        for (int i = 0; i < p.size(); i++) {
            input >> p.coefficients[i];
        }
        return input;
    }

private:
    std::vector<ll> coefficients;

    void trim() {
        while (coefficients.size() > 1 && coefficients.back() == 0) {
            coefficients.pop_back();
        }
    }
};

Polynomial operator+(const Polynomial& A, const Polynomial& B) {
    Polynomial C(std::max(A.size(), B.size()));
    for (int i = 0; i < C.size(); ++i) {
        C[i] = (A[i] + B[i]) % MOD;
    }
    return C;
}

Polynomial operator*(const Polynomial& A, const Polynomial& B) {
    Polynomial C(A.size() + B.size());
    for (int i = 0; i < A.size(); ++i) {
        for (int j = 0; j < B.size(); ++j) {
            C[i + j] += (A[i] * B[j]) % MOD;
            C[i + j] %= MOD;
        }
    }
    return C;
}

Polynomial operator/(const Polynomial& A, const Polynomial& B) {
    Polynomial C(POLYNOMIAL_SIZE);
    for (int i = 0; i < POLYNOMIAL_SIZE; ++i) {
        ll counter = 0;
        for (int j = 0; j < i; ++j) {
            counter += (C[j] * B[i - j]) % MOD;
            counter %= MOD;
        }
        C[i] += (A[i] - counter + MOD) % MOD;
        C[i] %= MOD;
    }
    return C;
}


Polynomial mod(const Polynomial& A, int precision) {
    std::vector<ll> C(precision, 0);
    for (int i = 0; i < precision; ++i) {
        C[i] = A[i] % MOD;
    }
    return Polynomial(C);
}

Polynomial operator+(const ll number, const Polynomial& A) {
    Polynomial C(A);
    C[0] = (number + C[0]) % MOD;
    return C;
}

Polynomial operator*(const ll scalar, const Polynomial& A) {
    Polynomial C(A);
    for (int i = 0; i < C.size(); ++i) {
        C[i] = (scalar * C[i]) % MOD;
    }
    return C;
}

std::vector<Polynomial> pow_A;

Polynomial exp(const int precision) {
    Polynomial C(precision);
    for (int i = 0; i < precision; ++i) {
        C = C + exp_k[i] * pow_A[i];
    }
    return C;
}

Polynomial ln(const int precision) {
    Polynomial C(precision);
    for (int i = 0; i < precision; ++i) {
        C = C + ln_k[i] * pow_A[i];
    }
    return C;
}

Polynomial sqrt(const int precision) {
    Polynomial C(precision);
    for (int i = 0; i < precision; ++i) {
        C = C + sqrt_k[i] * pow_A[i];
    }
    return C;
}

void calculate_pow_A(const Polynomial& A, const int precision) {
    pow_A.assign(precision, Polynomial(std::vector<long long>{1}));
    for (int i = 1; i < pow_A.size(); ++i) {
        pow_A[i] = mod(pow_A[i - 1] * A, precision);
    }
}

int main(int argc, char* argv[]) {
    std::string path = R"(C:\Users\rusla\Desktop\fourth-sem\discret-math\gen-func-2\B\output1-test)" + std::string(argv[1]) + ".txt";
    std::freopen(path.c_str(), "w", stdout);

    std::ios_base::sync_with_stdio(false);
    std::cin.tie(nullptr);

    calculate_factorials(FACTORIAL_SIZE);
    calculate_coefficients(COEFFICIENTS_SIZE);

    int n, m;
    std::cin >> n >> m;
    Polynomial A(n + 1);
    std::cin >> A;
    calculate_pow_A(A, m);

    Polynomial B(m);
    B = sqrt(m);
    std::cout << B << '\n';
    B = exp(m);
    std::cout << B << '\n';
    B = ln(m);
    std::cout << B << '\n';
    return 0;
}