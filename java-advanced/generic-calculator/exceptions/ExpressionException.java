package expression.exceptions;

public class ExpressionException extends RuntimeException {
    final String reason;

    public ExpressionException(String reason) {
        this.reason = reason;
    }

    public String getMessage() {
        return reason;
    }
}
