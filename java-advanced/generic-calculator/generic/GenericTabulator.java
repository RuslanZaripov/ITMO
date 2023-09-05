package expression.generic;

import java.util.Arrays;

import expression.*;
import expression.exceptions.*;
import expression.mode.*;
import expression.parser.*;

public class GenericTabulator implements Tabulator {
    public Object[][][] tabulate(String stringMode, String expression, int x1, int x2, int y1, int y2, int z1, int z2)
            throws Exception {
        Mode<?> mode;
        if ((mode = getMode(stringMode)) == null) {
            throw new ExpressionException("Wrong input");
        }
        return tabulateGeneric(mode, expression, x1, x2, y1, y2, z1, z2);
    }

    private static Mode<?> getMode(String mode) {
        return switch (mode) {
        case "i" -> new ModeInteger();
        case "d" -> new ModeDouble();
        case "bi" -> new ModeBigInteger();
        case "l" -> new ModeLong();
        case "u" -> new ModeInt();
        case "s" -> new ModeShort();
        default -> null;
        };
    }

    public <T extends Number> Object[][][] tabulateGeneric(Mode<T> mode, String expression, int x1, int x2, int y1,
            int y2, int z1, int z2) throws Exception {
        Object[][][] result = new Object[x2 - x1 + 1][y2 - y1 + 1][z2 - z1 + 1];
        GenericExpression expr = new ExpressionParser().parse(new StringSource(expression));
        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result[i].length; j++) {
                for (int k = 0; k < result[i][j].length; k++) {
                    try {
                        result[i][j][k] = expr.evaluate(mode.valueOf(x1 + i), mode.valueOf(y1 + j),
                                mode.valueOf(z1 + k), mode);
                    } catch (ExpressionException e) {
                        result[i][j][k] = null;
                    }
                }
            }
        }
        return result;
    }

    public static String toString(Object[][][] result, int l, int r) {
        if (result == null) {
            return null;
        }
        int bound = r - l + 1;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bound; i++) {
            sb.append("x=" + (i - r) + "\n");

            sb.append("y\\z \t");
            for (int k = 0; k < bound; k++) {
                sb.append((k - r) + " \t");
            }
            sb.append("\n");

            for (int j = 0; j < bound; j++) {
                sb.append((j - r) + " \t");
                for (int k = 0; k < bound; k++) {
                    sb.append(result[i][j][k] + " \t");
                }
                sb.append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Wrong input");
            return;
        }
        Mode<?> mode = getMode(args[0].substring(1));
        if (mode == null) {
            System.out.println("Wrong input");
            return;
        }
        int l = -2, r = 2;
        System.out.println(Arrays.toString(args));
        GenericTabulator tabulator = new GenericTabulator();
        try {
            Object[][][] result = tabulator.tabulateGeneric(mode, args[1], l, r, l, r, l, r);
            System.out.println(toString(result, l, r));
        } catch (ParseException e) {
            System.out.println("Invalid expression: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Something went wrong: " + e.getMessage());
        }
    }
}
