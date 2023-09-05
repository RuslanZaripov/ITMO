package expression.mode;

import expression.exceptions.*;

public class ModeInt implements Mode<Integer> {
    @Override
    public Integer add(Integer first, Integer second) {
        return first + second;
    }

    @Override
    public Integer subtract(Integer first, Integer second) {
        return first - second;
    }

    @Override
    public Integer divide(Integer first, Integer second) {
        if (second == 0) {
            throw new DBZException("Divided by zero");
        }
        return first / second;
    }

    @Override
    public Integer multiply(Integer first, Integer second) {
        return first * second;
    }

    @Override
    public Integer parse(String num) {
        return Integer.parseInt(num);
    }

    @Override
    public Integer negate(Integer value) {
        return -value;
    }

    @Override
    public Integer valueOf(Number number) {
        return number.intValue();
    }
}
