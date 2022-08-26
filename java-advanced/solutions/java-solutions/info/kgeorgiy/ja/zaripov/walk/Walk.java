package info.kgeorgiy.ja.zaripov.walk;

public class Walk {
    public static void main(String[] args) {
        if (validate(args)) {
            System.err.println("Expected two arguments: <input file> <output file>");
            return;
        }

        try {
            new Walker(args[0], args[1]).walk();
        } catch (WalkerException e) {
            System.err.println("Exception has occurred:" + e.getMessage());
        }
    }

    private static boolean validate(String[] args) {
        return args == null || args.length != 2 || args[0] == null || args[1] == null;
    }
}