package expression.mode;

import expression.exceptions.*;

public class ModeLong implements Mode<Long> {
    @Override
    public Long add(Long first, Long second) {
        return first + second;
    }

    @Override
    public Long subtract(Long first, Long second) {
        return first - second;
    }

    @Override
    public Long divide(Long first, Long second) {
        if (second == 0) {
            throw new DBZException("Divided by zero");
        }
        return first / second;
    }

    @Override
    public Long multiply(Long first, Long second) {
        return first * second;
    }

    @Override
    public Long parse(String num) {
        return Long.parseLong(num);
    }

    @Override
    public Long negate(Long value) {
        return -value;
    }

    @Override
    public Long valueOf(Number number) {
        return number.longValue();
    }
}
