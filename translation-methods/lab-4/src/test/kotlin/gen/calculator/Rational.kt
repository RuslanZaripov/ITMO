package gen.calculator

class Rational(val numerator: Int, val denominator: Int = 1) {
    operator fun plus(rhs: Rational) =
        numerator.times(rhs.denominator).plus(denominator.times(rhs.numerator))
            .divBy(denominator.times(rhs.denominator))

    operator fun minus(rhs: Rational) = this.plus(rhs.unaryMinus())

    operator fun times(rhs: Rational) = numerator.times(rhs.numerator).divBy(denominator.times(rhs.denominator))

    operator fun div(rhs: Rational) = this.times(Rational(rhs.denominator, rhs.numerator))

    operator fun unaryMinus() = Rational(-numerator, denominator)

    override fun toString(): String {
        val r = simplify(this)
        return "${r.numerator}/${r.denominator}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Rational) return false

        val thisSimplified = simplify(this)
        val otherSimplified = simplify(other)

        if (thisSimplified.numerator != otherSimplified.numerator) return false
        if (thisSimplified.denominator != otherSimplified.denominator) return false

        return true
    }
}

fun String.toRational(): Rational {
    val number = split("/")
    return when (number.size) {
        1 -> Rational(number[0].toInt())
        else -> Rational(number[0].toInt(), number[1].toInt())
    }
}

infix fun Int.divBy(rhs: Int) = Rational(this, rhs)

private fun valueOf(value: String) = value.toRational()

fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

fun simplify(r: Rational): Rational {
    val isNegative = (r.numerator < 0).xor(r.denominator < 0)
    val gcd = kotlin.math.abs(gcd(r.numerator, r.denominator))
    val tempNumerator = kotlin.math.abs(r.numerator).div(gcd)
    val newDenominator = kotlin.math.abs(r.denominator).div(gcd)
    return Rational(if (isNegative) -tempNumerator else tempNumerator, newDenominator)
}
