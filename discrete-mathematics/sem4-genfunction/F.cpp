#include <iostream>
#include <vector>
#include <sstream>

template <class T>
std::istream& operator>>(std::istream& input, std::vector<T>& v) {
    for (int i = 0; i < v.size(); i++) {
        input >> v[i];
    }
    return input;
}

const long long MOD = 1e9 + 7;
const int SIZE = 1000;

class Polynomial {
public:
    explicit Polynomial(int size) {
        this->coefficients.assign(size, 0);
    }

    explicit Polynomial(std::vector<long long> coefficients)
        : coefficients(std::move(coefficients)) {};

    long long operator[](int index) const {
        return index < coefficients.size() ? coefficients[index] : 0;
    }

    long long& operator[](int index) {
        return coefficients[index];
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

int main() {
    std::ios_base::sync_with_stdio(false);
    std::cin.tie(nullptr);

    int k, m;
    std::cin >> k >> m;

    std::vector<int> c(k);
    std::cin >> c;

    Polynomial T(m + 1), temp(m + 1);
    T[0] = temp[0] = 1;
    for (int tree_weight = 1; tree_weight <= m; tree_weight++) {
        for (int node_weight : c) {
            int residual_weight = tree_weight - node_weight;
            if (residual_weight >= 0) {
                T[tree_weight] = (T[tree_weight] + temp[residual_weight]) % MOD;
            }
        }
        for (int w = 0; w <= tree_weight; w++) {
            temp[tree_weight] += (T[w] * T[tree_weight - w]) % MOD;
            temp[tree_weight] %= MOD;
        }
    }

    std::stringstream ss;
    for (int i = 1; i < T.size(); i++) {
        ss << T[i] << ' ';
    }
    std::cout << ss.str() << '\n';
    return 0;
}
