package expression.operations.binary;

import java.util.Objects;

import expression.GenericExpression;
import expression.mode.Mode;

public abstract class BinaryOperation implements GenericExpression {
    private final GenericExpression operation1;
    private final GenericExpression operation2;
    private final String symbol;

    protected BinaryOperation(GenericExpression operation1, GenericExpression operation2, String symbol) {
        this.operation1 = operation1;
        this.operation2 = operation2;
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return "(" + operation1.toString() + " " + symbol + " " + operation2.toString() + ")";
    }

    @Override
    public <T extends Number> T evaluate(T x, T y, T z, Mode<T> mode) {
        return apply(operation1.evaluate(x, y, z, mode), operation2.evaluate(x, y, z, mode), mode);
    }

    protected abstract <T extends Number> T apply(T first, T second, Mode<T> mode);

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null && obj.getClass() == this.getClass()) {
            BinaryOperation that = (BinaryOperation) obj;
            return that.operation1.equals(this.operation1) && that.operation2.equals(this.operation2);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(operation1, operation2, symbol);
    }
}