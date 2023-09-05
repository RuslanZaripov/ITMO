package expression.mode;

import java.math.BigInteger;

import expression.exceptions.DBZException;

public class ModeBigInteger implements Mode<BigInteger> {
    @Override
    public BigInteger add(BigInteger first, BigInteger second) {
        return first.add(second);
    }

    @Override
    public BigInteger subtract(BigInteger first, BigInteger second) {
        return first.subtract(second);
    }

    @Override
    public BigInteger divide(BigInteger first, BigInteger second) {
        if (second.signum() == 0) {
            throw new DBZException("Divided by zero");
        }
        return first.divide(second);
    }

    @Override
    public BigInteger multiply(BigInteger first, BigInteger second) {
        return first.multiply(second);
    }

    @Override
    public BigInteger parse(String num) {
        return new BigInteger(num);
    }

    @Override
    public BigInteger negate(BigInteger value) {
        return value.negate();
    }

    @Override
    public BigInteger valueOf(Number number) {
        return new BigInteger(String.valueOf(number.longValue()));
    }
}
