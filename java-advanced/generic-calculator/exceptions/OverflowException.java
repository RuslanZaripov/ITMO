package expression.exceptions;

public class OverflowException extends ExpressionException {
    public OverflowException(String message) {
        super(message);
    }
    
    @Override
    public String getMessage() {
        return reason;
    }
}
