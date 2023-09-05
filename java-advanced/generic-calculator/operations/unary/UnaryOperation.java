package expression.operations.unary;

import java.util.Objects;

import expression.GenericExpression;
import expression.mode.Mode;

public abstract class UnaryOperation implements GenericExpression {
    private final GenericExpression operation;
    private final String symbol;

    protected UnaryOperation(GenericExpression operation, String symbol) {
        this.operation = operation;
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return symbol + "(" + operation.toString() + ")";
    }

    @Override
    public <T extends Number> T evaluate(T x, T y, T z, Mode<T> mode) {
        return apply(operation.evaluate(x, y, z, mode), mode);
    }

    protected abstract <T extends Number> T apply(T value, Mode<T> mode);

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null && obj.getClass() == this.getClass()) {
            UnaryOperation that = (UnaryOperation) obj;
            return that.operation.equals(this.operation);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(operation, symbol);
    }
}
