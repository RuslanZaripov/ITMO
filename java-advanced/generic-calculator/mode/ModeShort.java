package expression.mode;

import expression.exceptions.*;

public class ModeShort implements Mode<Short> {
    @Override
    public Short add(Short first, Short second) {
        return (short) (first + second);
    }

    @Override
    public Short subtract(Short first, Short second) {
        return (short) (first - second);
    }

    @Override
    public Short divide(Short first, Short second) {
        if (second == 0) {
            throw new DBZException("Divided by zero");
        }
        return (short) (first / second);
    }

    @Override
    public Short multiply(Short first, Short second) {
        return (short) (first * second);
    }

    @Override
    public Short parse(String num) {
        return Short.parseShort(num);
    }

    @Override
    public Short negate(Short value) {
        return (short) (-value);
    }

    @Override
    public Short valueOf(Number number) {
        return number.shortValue();
    }
}