package expression.operations;

import java.util.Objects;

import expression.GenericExpression;
import expression.mode.Mode;

public class Const implements GenericExpression {
    private final String num;

    public Const(String num) {
        this.num = num;
    }

    @Override
    public String toString() {
        return num;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Const) {
            Const that = (Const) obj;
            return that.num == this.num;
        }
        return false;
    }

    @Override
    public <T extends Number> T evaluate(T x, T y, T z, Mode<T> mode) {
        return mode.parse(num);
    }

    @Override
    public int hashCode() {
        return Objects.hash(num);
    }
}