package expression.mode;

public class ModeDouble implements Mode<Double> {
    @Override
    public Double add(Double first, Double second) {
        return first + second;
    }

    @Override
    public Double subtract(Double first, Double second) {
        return first - second;
    }

    @Override
    public Double divide(Double first, Double second) {
        return first / second;
    }

    @Override
    public Double multiply(Double first, Double second) {
        return first * second;
    }

    @Override
    public Double parse(String num) {
        return Double.parseDouble(num);
    }

    @Override
    public Double negate(Double value) {
        return -value;
    }

    @Override
    public Double valueOf(Number number) {
        return number.doubleValue();
    }
}
