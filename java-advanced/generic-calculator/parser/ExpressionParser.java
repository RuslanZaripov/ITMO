package expression.parser;

import expression.*;
import expression.operations.binary.*;
import expression.operations.unary.*;
import expression.operations.*;

public class ExpressionParser {

    public GenericExpression parse(final String source) {
        return parse(new StringSource(source));
    }

    public GenericExpression parse(final CharSource source) {
        return new ExprParser(source).parseExpression();
    }

    private static class ExprParser extends BaseParser {
        private ExprParser(final CharSource source) {
            super(source);
            nextChar();
        }

        private GenericExpression parseExpression() {
            final GenericExpression result = parseElement();
            if (eof()) {
                return result;
            }
            throw error("End of expression expected");
        }

        private GenericExpression parseElement() {
            skipWhitespace();
            final GenericExpression result = expr();
            return result;
        }

        private GenericExpression expr() {
            GenericExpression expression = term();
            while (true) {
                skipWhitespace();
                if (test('+')) {
                    expression = new Add(expression, term());
                } else if (test('-')) {
                    expression = new Subtract(expression, term());
                } else {
                    return expression;
                }
            }
        }

        private GenericExpression term() {
            GenericExpression expression = prim();
            while (true) {
                skipWhitespace();
                if (test('*')) {
                    expression = new Multiply(expression, prim());
                } else if (test('/')) {
                    expression = new Divide(expression, prim());
                } else {
                    return expression;
                }
            }
        }

        private GenericExpression prim() {
            GenericExpression expression;
            boolean isNegative = isNegative();
            skipWhitespace();
            if (test('(')) {
                expression = parseElement();
                if (!test(')')) {
                    throw error("Closing bracket not found");
                }
            } else if (between('a', 'z')) {
                expression = parseVariable();
            } else if (between('0', '9')) {
                return parseNumber(isNegative);
            } else if (test(')')) {
                throw error("Empty brackets");
            } else {
                throw error("Symbol expected");
            }
            return isNegative ? new Negate(expression) : expression;
        }

        private GenericExpression parseVariable() {
            final StringBuilder sb = new StringBuilder();
            copyChar(sb);
            if (sb.toString().equals("x") || sb.toString().equals("y") || sb.toString().equals("z")) {
                return new Variable(sb.toString());
            } else {
                throw error("Invalid variable: " + sb.toString());
            }
        }

        private GenericExpression parseNumber(boolean isNegative) {
            final StringBuilder sb = new StringBuilder();
            copyInteger(sb);
            try {
                return new Const(sb.toString());
            } catch (NumberFormatException e) {
                throw error("Invalid number: " + sb.toString());
            }
        }

        private boolean isNegative() {
            skipWhitespace();
            int k = 0;
            while (test('-')) {
                skipWhitespace();
                k++;
            }
            return k % 2 == 1;
        }

        private void copyChar(final StringBuilder sb) {
            while (between('a', 'z')) {
                sb.append(ch);
                nextChar();
            }
        }

        private void copyInteger(StringBuilder sb) {
            if (test('-')) {
                sb.append('-');
            }
            if (test('0')) {
                sb.append('0');
                copyFractions(sb);
            } else if (between('1', '9')) {
                copyDigits(sb);
            } else {
                throw error("Invalid number: " + sb.toString());
            }
        }

        private void copyFractions(final StringBuilder sb) {
            if (test('.')) {
                sb.append('.');
                copyDigits(sb);
            }
            return;
        }

        private void copyDigits(final StringBuilder sb) {
            while (between('0', '9')) {
                sb.append(ch);
                nextChar();
            }
        }

        private void skipWhitespace() {
            while (test(' ') || test('\r') || test('\n') || test('\t')) {
            }
        }
    }
}
