package expression.operations;

import java.util.Objects;

import expression.GenericExpression;
import expression.mode.Mode;

public class Variable implements GenericExpression {
    private String st;

    public Variable(String st) {
        this.st = st;
    }

    @Override
    public String toString() {
        return st;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Variable) {
            Variable that = (Variable) obj;
            return that.st.equals(this.st);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(st);
    }

    @Override
    public <T extends Number> T evaluate(T x, T y, T z, Mode<T> mode) {
        switch (st) {
        case "x":
            return x;
        case "y":
            return y;
        case "z":
            return z;
        }
        throw new IllegalArgumentException("Wrong input");
    }
}
