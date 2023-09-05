package expression.mode;

public interface Mode<T extends Number> {
    T add(T first, T second);

    T subtract(T first, T second);

    T divide(T first, T second);

    T multiply(T first, T second);

    T parse(String num);

    T negate(T value);

    T valueOf(Number number);
}
