package expression.operations.unary;

import expression.*;
import expression.mode.*;

public class Negate extends UnaryOperation {
    public Negate(GenericExpression operation) {
        super(operation, "-");
    }

    @Override
    protected <T extends Number> T apply(T value, Mode<T> mode) {
        return mode.negate(value);
    }

}
