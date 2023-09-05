package expression.operations.binary;

import expression.GenericExpression;
import expression.mode.Mode;

public class Subtract extends BinaryOperation {
    public Subtract(GenericExpression operation1, GenericExpression operation2) {
        super(operation1, operation2, "-");
    }

    @Override
    protected <T extends Number> T apply(T first, T second, Mode<T> mode) {
        return mode.subtract(first, second);
    }
}
