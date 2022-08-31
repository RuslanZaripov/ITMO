//
// Created by rusla on 29.05.2022.
//

#include <iostream>
#include <vector>
#include <sstream>

#define ll long long

const ll MOD = 104857601;

ll mod(ll number) {
    return (number + MOD) % MOD;
}

template <class T>
std::istream& operator>>(std::istream& input, std::vector<T>& v) {
    for (ll i = 0; i < v.size(); i++) {
        input >> v[i];
    }
    return input;
}

template <class T>
std::ostream& operator<<(std::ostream& output, std::vector<T>& v) {
    for (ll i = 0; i < v.size(); i++) {
        output << v[i] << ' ';
    }
    return output;
}

template<class T>
class Polynomial {
private:
    std::vector<T> coefficients;

public:
    explicit Polynomial(ll size) {
        this->coefficients.assign(size, 0);
    }

    explicit Polynomial(std::vector<T> coefficients)
            : coefficients(std::move(coefficients)) {};

    T operator[](ll index) const {
        return index < coefficients.size() ? coefficients[index] : 0;
    }

    T& operator[](ll index) {
        if (coefficients.size() - 1 < index) {
            coefficients.resize(index + 1, 0);
        }
        return coefficients[index];
    }

    __attribute__((unused)) __attribute__((unused)) ll degree() const {
        return size() - 1;
    }

    ll size() const {
        return coefficients.size();
    }

    friend std::ostream& operator<<(std::ostream& out, Polynomial& p) {
        std::stringstream ss;
        ss << '[' << ' ';
        for (T coefficient : p.coefficients) {
            ss << coefficient << ' ';
        }
        ss << ']';
        out << ss.str();
        return out;
    }

    friend std::istream& operator>>(std::istream& input, Polynomial& p) {
        for (ll i = 0; i < p.size(); i++) {
            input >> p.coefficients[i];
        }
        return input;
    }
};

template<class T>
Polynomial<T> operator+(const Polynomial<T>& A, const Polynomial<T>& B) {
    ll size = std::max(A.size(), B.size());
    std::vector<T> C(size, 0);
    for (ll i = 0; i < C.size(); ++i) {
        C[i] = mod(A[i] + B[i]);
        C[i] %= MOD;
    }
    return Polynomial<T>(C);
}

template<class T>
Polynomial<T> operator*(const Polynomial<T>& A, const Polynomial<T>& B) {
    ll size = A.size() + B.size();
    std::vector<T> C(size, 0);
    for (ll i = 0; i < A.size(); ++i) {
        for (ll j = 0; j < B.size(); ++j) {
            C[i + j] += mod(A[i] * B[j]);
            C[i + j] %= MOD;
        }
    }
    return Polynomial<T>(C);
}

template<class T>
Polynomial<T> invert(const Polynomial<T>& A) {
    std::vector<T> C;
    C.reserve(A.size());
    for (ll i = 0; i < A.size(); ++i) {
        C.push_back(i % 2 == 1 ? -A[i] : A[i]);
    }
    return Polynomial<T>(C);
}

template<class T>
Polynomial<T> filter(const Polynomial<T>& A, ll value) {
    std::vector<T> C;
    C.reserve(A.size());
    for (ll i = 0; i < A.size(); ++i) {
        if (i % 2 == value) {
            C.push_back(A[i]);
        }
    }
    return Polynomial<T>(C);
}

template<class T>
Polynomial<T> sqrt(const Polynomial<T>& A) {
    return filter<T>(A, 0);
}


ll get_nth(ll n, Polynomial<ll> &A, Polynomial<ll> &Q) {
    ll k = A.size();
    for (; n >= k; n /= 2) {
        for (ll i = k; i < 2 * k; ++i) {
            for (ll j = 1; j <= k; ++j) {
                A[i] = mod(A[i] + mod(-Q[j] * A[i - j]));
            }
        }
        A = filter<ll>(A, n % 2);
        Polynomial<ll> R = Q * invert<ll>(Q);
        Q = sqrt<ll>(R);
    }
    return A[n];
}

int main() {
    ll k, n;
    std::cin >> k >> n;
    Polynomial<ll> A(k);
    std::cin >> A;

    std::vector<ll> C(k + 1, 1);
    for (ll i = 1; i < k + 1; i++) {
        ll number;
        std::cin >> number;
        C[i] = -number;
    }
    Polynomial<ll> Q(C);
    std::cout << get_nth(n - 1, A, Q) << '\n';
}
