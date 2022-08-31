#include <iostream>
#include <vector>
#include <sstream>

const int MOD = 998244353;
const int SIZE = 1000;

class Polynomial {
public:
    explicit Polynomial(int size) {
        this->coefficients.assign(size, 0);
    }

    explicit Polynomial(std::vector<long long> coefficients)
        : coefficients(std::move(coefficients))
    {
        if (this->size() != SIZE) {
            this->trim();
        }
    };

    long long operator[](int index) const {
        return index < coefficients.size() ? coefficients[index] : 0;
    }

    __attribute__((unused)) int degree() const {
        return size() - 1;
    }

    int size() const {
        return static_cast<int>(coefficients.size());
    }

    friend std::ostream& operator<<(std::ostream& out, Polynomial& p) {
        std::stringstream ss;
        for (long long coefficient : p.coefficients) {
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
    std::vector<long long> coefficients;

    void trim() {
        while (coefficients.size() > 1 && coefficients.back() == 0) {
            coefficients.pop_back();
        }
    }
};

Polynomial operator+(const Polynomial& A, const Polynomial& B) {
    int size = std::max(A.size(), B.size());
    std::vector<long long> C(size, 0);
    for (int i = 0; i < C.size(); ++i) {
        C[i] = (A[i] + B[i]) % MOD;
        C[i] %= MOD;
    }
    return Polynomial(C);
}

Polynomial operator*(const Polynomial& A, const Polynomial& B) {
    int size = A.size() + B.size();
    std::vector<long long> C(size, 0);
    for (int i = 0; i < A.size(); ++i) {
        for (int j = 0; j < B.size(); ++j) {
            C[i + j] += (A[i] * B[j]) % MOD;
            C[i + j] %= MOD;
        }
    }
    return Polynomial(C);
}

Polynomial operator/(const Polynomial& A, const Polynomial& B) {
    std::vector<long long> C(SIZE, 0);
    for (int i = 0; i < SIZE; ++i) {
        long long counter = 0;
        for (int j = 0; j < i; ++j) {
            counter += (C[j] * B[i - j]) % MOD;
            counter %= MOD;
        }
        C[i] += (A[i] - counter + MOD) % MOD;
        C[i] %= MOD;
    }
    return Polynomial(C);
}

Polynomial mod(const Polynomial& A, int k) {
    std::vector<long long> C(k, 0);
    for (int i = 0; i < k; ++i) {
        C[i] = A[i];
    }
    return Polynomial(C);
}

int main() {
    std::ios_base::sync_with_stdio(false);
    std::cin.tie(nullptr);

    int k;
    std::cin >> k;

    Polynomial A(k);
    std::cin >> A;

    std::vector<long long> q(k + 1);
    q[0] = 1;
    for (int i = 0; i < k; i++) {
        long long element;
        std::cin >> element;
        q[i + 1] = -element;
    }
    Polynomial Q(q);

    Polynomial T = mod(A * Q, k);
    std::cout << T.degree() << '\n' << T << '\n';
    std::cout << Q.degree() << '\n' << Q << '\n';
}