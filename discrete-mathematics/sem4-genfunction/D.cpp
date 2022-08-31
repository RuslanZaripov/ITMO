#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunknown-pragmas"
#pragma ide diagnostic ignored "google-explicit-constructor"
#include <iostream>
#include <vector>
#include <sstream>

#define ll __int128_t

const int SIZE = 18;

ll gcd (ll a, ll b) {
    return b ? gcd (b, a % b) : a;
}

struct Fraction {
    ll num;
    ll den;

    Fraction() : num(1L), den(1L) {}
    Fraction(ll numerator)
            : num(numerator), den(1L) {}
    Fraction(ll numerator, ll denominator)
            : num(numerator), den(denominator) {}
    Fraction(Fraction& other) = default;
    Fraction(const Fraction& other) = default;

    friend std::ostream& operator<<(std::ostream& out, Fraction& f) {
        out << static_cast<long long>(f.num) << '/' << static_cast<long long>(f.den);
        return out;
    }

    friend Fraction operator/(const Fraction& f1, const Fraction& f2) {
        return simplify(f1 * Fraction(f2.den, f2.num));
    }

    friend Fraction operator+(const Fraction& f1, const Fraction& f2) {
        return simplify({f1.num * f2.den + f1.den * f2.num, f1.den * f2.den});
    }

    friend Fraction operator*(const Fraction& f1, const Fraction& f2) {
        return simplify({f1.num * f2.num, f1.den * f2.den});
    }

    friend Fraction operator-(const Fraction& f1, const Fraction& f2) {
        return simplify(f1 + Fraction(-f2.num, f2.den));
    }

    Fraction operator+=(const Fraction& f) {
        Fraction result = simplify((*this) + f);
        this->den = result.den;
        this->num = result.num;
        return (*this);
    }

    Fraction operator-=(const Fraction& f) {
        Fraction result = simplify((*this) - f);
        this->den = result.den;
        this->num = result.num;
        return (*this);
    }

    Fraction operator*=(const Fraction& f) {
        Fraction result = simplify((*this) * f);
        this->den = result.den;
        this->num = result.num;
        return (*this);
    }

    Fraction operator/=(const Fraction& f) {
        Fraction result = simplify((*this) / f);
        this->den = result.den;
        this->num = result.num;
        return (*this);
    }

    static bool isPositive(const Fraction& f) {
        return (f.num < 0 && f.den < 0) || (f.num > 0 && f.den > 0);
    }

    static bool isNegative(const Fraction& f) {
        return !isPositive(f);
    }

    static Fraction simplify(const Fraction& f) {
        ll pos_num = std::abs(f.num);
        ll pos_den = std::abs(f.den);
        ll g = gcd(pos_num, pos_den);
        ll num = pos_num / g;
        return {isNegative(f) ? -num : num, pos_den / g};
    }
};

template<class T>
class Polynomial {
private:
    std::vector<T> coefficients;

public:
    Polynomial(int size) {
        this->coefficients.assign(size, 0);
    }

    Polynomial(std::vector<T> coefficients)
        : coefficients(std::move(coefficients)) {};

    T operator[](int index) const {
        return index < coefficients.size() ? coefficients[index] : 0;
    }

    T& operator[](int index) {
        return coefficients[index];
    }

    __attribute__((unused)) __attribute__((unused)) int degree() const {
        return size() - 1;
    }

    int size() const {
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
        for (int i = 0; i < p.size(); i++) {
            input >> p.coefficients[i];
        }
        return input;
    }

    Polynomial<T> operator*=(const Polynomial<T>& A) {
        this->coefficients = ((*this) * A).coefficients;
        return (*this);
    }

    Polynomial<T> operator+=(const Polynomial<T>& A) {
        this->coefficients = ((*this) + A).coefficients;
        return (*this);
    }
};

template<class T>
Polynomial<T> operator+(const Polynomial<T>& A, const Polynomial<T>& B) {
    int size = std::max(A.size(), B.size());
    std::vector<T> C(size, 0);
    for (int i = 0; i < C.size(); ++i) {
        C[i] = A[i] + B[i];
    }
    return C;
}

template<class T>
Polynomial<T> operator*(const T scalar, const Polynomial<T>& A) {
    Polynomial<T> C(A);
    for (int i = 0; i < C.size(); ++i) {
        C[i] = scalar * C[i];
    }
    return C;
}

template<class T>
Polynomial<T> operator*(const Polynomial<T>& A, const Polynomial<T>& B) {
    int size = A.size() + B.size();
    std::vector<T> C(size, 0);
    for (int i = 0; i < A.size(); ++i) {
        for (int j = 0; j < B.size(); ++j) {
            C[i + j] += A[i] * B[j];
        }
    }
    return C;
}

template<class T>
Polynomial<T> operator/(const Polynomial<T>& A, const Polynomial<T>& B) {
    std::vector<T> C(SIZE, 0);
    for (int i = 0; i < SIZE; ++i) {
        T counter = 0;
        for (int j = 0; j < i; ++j) {
            counter += C[j] * B[i - j];
        }
        C[i] += (A[i] - counter);
    }
    return C;
}

template <class T>
std::istream& operator>>(std::istream& input, std::vector<T>& v) {
    for (int i = 0; i < v.size(); i++) {
        input >> v[i];
    }
    return input;
}

template <class T>
std::ostream& operator<<(std::ostream& output, std::vector<T>& v) {
    for (int i = 0; i < v.size(); i++) {
        output << v[i] << ' ';
    }
    return output;
}

std::vector<ll> rDegree;
std::vector<ll> factorials;

void calculate(int r) {
    for (int i = 1; i < rDegree.size(); i++) {
        rDegree[i] = rDegree[i - 1] * r;
    }
    factorials[0] = 1;
    for (int i = 1; i < factorials.size(); i++) {
        factorials[i] = factorials[i - 1] * i;
    }
}

int main() {
//    // числовой тип double
//    int r, k;
//    std::cin >> r >> k;
//
//     factorials.assign(r + k, 1);
//     rDegree.assign(r, 1);
//     calculate(r);
//
//     std::vector<int> coefficients(k + 1);
//     std::cin >> coefficients;
//     std::cout << coefficients << '\n';
//
//     Polynomial<double> result(r);
//     for (int degree = 0; degree < k + 1; degree++) {
//         Polynomial<double> pol(std::vector<double>{1});
//         for (int number = k; number > 0; number--) {
//             pol = pol * Polynomial<double>({static_cast<double>(number - degree), 1});
//         }
//         pol = (1 / static_cast<double>(factorials[k] * rDegree[degree])) * pol;
//         std::cout << pol << std::endl;
//         result = result + static_cast<double>(coefficients[degree]) * pol ;
//     }
//     for (int i = 0; i < k + 1; i++) {
//         std::cout << result[i] << ' ';
//     }

    int r, k;
    std::cin >> r >> k;

    factorials.assign(SIZE, 1);
    rDegree.assign(SIZE, 1);
    calculate(r);

    std::vector<int> coefficients(k + 1);
    std::cin >> coefficients;

    Polynomial<Fraction> result(k + 1);
    for (int degree = 0; degree < k + 1; degree++) {
        Polynomial<Fraction> term(std::vector<Fraction>{1});
        for (int number = k; number > 0; number--) {
            term *= Polynomial<Fraction>({number - degree, 1});
        }
        result += Fraction(coefficients[degree], factorials[k] * rDegree[degree]) * term;
    }
    for (int i = 0; i < k + 1; i++) {
        std::cout << result[i] << ' ';
    }
}

#pragma clang diagnostic pop