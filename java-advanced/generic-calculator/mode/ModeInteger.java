package expression.mode;

import expression.exceptions.*;

public class ModeInteger implements Mode<Integer> {
    @Override
    public Integer add(Integer first, Integer second) {
        if (((first ^ (first + second)) & (second ^ (first + second))) < 0) {
            throw new OverflowException("Overflow");
        }
        return first + second;
    }

    @Override
    public Integer subtract(Integer first, Integer second) {
        if (((first ^ second) & (first ^ (first - second))) < 0) {
            throw new OverflowException("Overflow");
        }
        return first - second;
    }

    @Override
    public Integer divide(Integer first, Integer second) {
        if (second == 0) {
            throw new DBZException("Divided by zero");
        }
        if (first == Integer.MIN_VALUE && second == -1) {
            throw new OverflowException("Overflow");
        }
        return first / second;
    }

    @Override
    public Integer multiply(Integer first, Integer second) {
        if (second > 0) {
            if (first > Integer.MAX_VALUE / second || first < Integer.MIN_VALUE / second) {
                throw new OverflowException("Overflow");
            }
        } else {
            if (second < -1) {
                if (first > Integer.MIN_VALUE / second || first < Integer.MAX_VALUE / second) {
                    throw new OverflowException("Overflow");
                }
            } else {
                if (second == -1 && first == Integer.MIN_VALUE) {
                    throw new OverflowException("Overflow");
                }
            }
        }
        return first * second;
    }

    @Override
    public Integer parse(String num) {
        try {
            return Integer.parseInt(num);
        } catch (NumberFormatException e) {
            throw new OverflowException("Overflow");
        }
    }

    @Override
    public Integer negate(Integer value) {
        if (value == Integer.MIN_VALUE) {
            throw new OverflowException("Overflow");
        }
        return -value;
    }

    @Override
    public Integer valueOf(Number number) {
        return number.intValue();
    }
}
