package info.kgeorgiy.ja.zaripov.implementor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Generates class implementation of specified interface using Java Programming Language.
 *
 * @author Ruslan Zaripov (zaripovruslan864@gmail.com)
 */
public class CodeGenerator {
    /**
     * Generates class implementation of specified interface using Java Programming Language.
     * Class contains {@link #getPackageName(Class)} package name, {@link #getClassDeclaration(Class)} class declaration
     * and {@link #getMethod(Method)} methods. A {@link Utils#CURLY_CLOSE_BRACKET} curly close bracket added at the end.
     *
     * @param token {@link Class} type token
     * @return {@link String} interface implementation code
     */
    public static String generate(final Class<?> token) {
        return String.join(Utils.LINE,
                getPackageName(token), getClassDeclaration(token), getMethods(token), Utils.CURLY_CLOSE_BRACKET);
    }

    /**
     * Generates expression containing package name using {@link Utils#PACKAGE} and {@link Utils#SEMICOLON}.
     *
     * @param token {@link Class} type token
     * @return {@link String} package name if package exists.
     *         {@link Utils#EMPTY} otherwise.
     */
    private static String getPackageName(final Class<?> token) {
        final Package pkg = token.getPackage();
        return Objects.isNull(pkg)
                ? Utils.EMPTY
                : String.join(Utils.SPACE, Utils.PACKAGE, pkg.getName()).concat(Utils.SEMICOLON);
    }

    /**
     * Generates class declaration containing
     * {@link Utils#PUBLIC}, {@link Utils#CLASS} reserved-keywords
     * and {@link #getClassName(Class)} class name.
     * A name of the implemented interface is specified after {@link Utils#IMPLEMENTS}.
     * A {@link Utils#CURLY_OPEN_BRACKET} added at the end.
     *
     * @param token {@link Class} type token
     * @return {@link String} class declaration
     */
    private static String getClassDeclaration(final Class<?> token) {
        return String.join(Utils.SPACE,
                Utils.PUBLIC,
                Utils.CLASS,
                getClassName(token),
                Utils.IMPLEMENTS,
                token.getCanonicalName(),
                Utils.CURLY_OPEN_BRACKET
        );
    }

    /**
     * Generates string containing class name with added {@link Utils#IMPL_SUFFIX}.
     *
     * @param token {@link Class} type token
     * @return {@link String} class name
     */
    private static String getClassName(final Class<?> token) {
        return token.getSimpleName() + Utils.IMPL_SUFFIX;
    }

    /**
     * Simply concatenates {@link #getMethod(Method)} all methods' implementation code using {@link Utils#LINE}.
     *
     * @param token {@link Class} type token which methods will be generated
     * @return {@link String} methods
     */
    private static String getMethods(final Class<?> token) {
        return Arrays.stream(token.getMethods())
                .map(CodeGenerator::getMethod)
                .collect(Collectors.joining(Utils.LINE));
    }

    /**
     * Generates method implementation containing {@link #getMethodDeclaration(Method)} method declaration and
     * {@link #getMethodBody(Method)}. At the end {@link Utils#CURLY_CLOSE_BRACKET} bracket added.
     * In order to observe code formatting conventions,
     * at the beginning each line is concatenated with a {@link Utils#TAB}.
     *
     * @param method {@link Method} instance which method implementation will be generated
     * @return {@link String} method implementation
     */
    private static String getMethod(final Method method) {
        return String.join(Utils.LINE_SEPARATOR,
                Utils.TAB + getMethodDeclaration(method),
                Utils.DOUBLE_TAB + getMethodBody(method),
                Utils.TAB + Utils.CURLY_CLOSE_BRACKET
        );
    }

    /**
     * Generates method declaration containing
     * {@link Utils#PUBLIC}, return type, method name and {@link #getArguments(Method)} parameter list.
     * A {@link Utils#CURLY_OPEN_BRACKET} added at the end.
     *
     * @param method {@link Method} instance which method declaration will be generated
     * @return {@link String} method declaration
     */
    private static String getMethodDeclaration(final Method method) {
        return String.join(Utils.SPACE,
                Utils.PUBLIC,
                method.getReturnType().getCanonicalName(),
                method.getName() + getArguments(method),
                Utils.CURLY_OPEN_BRACKET
        );
    }

    /**
     * Generates parameter list separated by comma.
     * Adds an {@link Utils#OPEN_BRACKET} at the beginning and a {@link Utils#CLOSE_BRACKET} at the end.
     *
     * @param method {@link Method} instance which parameter list will be generated
     * @return {@link String} parameter list
     */
    private static String getArguments(final Method method) {
        return Arrays.stream(method.getParameters())
                .map(parameter -> String.join(Utils.SPACE, parameter.getType().getCanonicalName(), parameter.getName()))
                .collect(Collectors.joining(", ", Utils.OPEN_BRACKET, Utils.CLOSE_BRACKET));
    }

    /**
     * Generates return statement containing {@link Utils#RETURN}, default return value and {@link Utils#SEMICOLON}.
     *
     * @param method {@link Method} instance which method body will be generated
     * @return {@link String} method body
     */
    private static String getMethodBody(final Method method) {
        return String.join(Utils.EMPTY,
                Utils.RETURN, Utils.SPACE, getDefaultValue(method.getReturnType()), Utils.SEMICOLON);
    }

    /**
     * Returns default value of the methods' return type.
     *
     * @param returnType {@link Class} type token of methods' returning value
     * @return {@link String} default value dependent on type token
     */
    private static String getDefaultValue(final Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            return Utils.NULL;
        } else if (returnType.equals(void.class)) {
            return Utils.EMPTY;
        } else if (returnType.equals(boolean.class)) {
            return Utils.FALSE;
        } else {
            return Utils.ZERO;
        }
    }

    /**
     * Utility class for generating classes and paths.
     */
    public static final class Utils {
        /**
         * An "Impl" suffix is used to emphasize that the generated class is an implementation of specified interface.
         */
        public static final String IMPL_SUFFIX = "Impl";

        /**
         * A {@code package} reserved-keyword {@link String}.
         */
        public static final String PACKAGE = "package";
        /**
         * A {@code public} reserved-keyword {@link String}.
         */
        public static final String PUBLIC = "public";
        /**
         * A {@code class} reserved-keyword {@link String}.
         */
        public static final String CLASS = "class";
        /**
         * An {@code implements} reserved-keyword {@link String}.
         */
        public static final String IMPLEMENTS = "implements";
        /**
         * A {@code return} reserved-keyword {@link String}.
         */
        public static final String RETURN = "return";
        /**
         * A {@code null} reserved-keyword {@link String}.
         */
        public static final String NULL = "null";
        /**
         * A {@code false} reserved-keyword {@link String}.
         */
        public static final String FALSE = "false";
        /**
         * A {@link String} containing symbol '0'.
         */
        public static final String ZERO = "0";

        /**
         *  The system-dependent line separator {@link String}.
         */
        public static final String LINE_SEPARATOR = System.lineSeparator();

        /**
         * An empty {@link String}.
         */
        public static final String EMPTY = "";
        /**
         * A whitespace symbol {@link String}.
         */
        public static final String SPACE = " ";
        /**
         * A tab symbol {@link String}. Allows you to customize padding.
         */
        public static final String TAB = SPACE.repeat(4);
        /**
         * A semicolon symbol {@link String}. Often used to separate multiple statements.
         */
        public static final String SEMICOLON = ";";

        /**
         * A curly open bracket symbol {@link String}. Used to define the start of a code block.
         */
        public static final String CURLY_OPEN_BRACKET = "{";
        /**
         * A curly close bracket symbol {@link String}. Used to define the end of a code block.
         */
        public static final String CURLY_CLOSE_BRACKET = "}";
        /**
         * An open bracket symbol {@link String}. Indicates the beginning of a method's argument enumeration.
         */
        public static final String OPEN_BRACKET = "(";
        /**
         * A close bracket symbol {@link String}. Marks the end of a method's argument enumeration.
         */
        public static final String CLOSE_BRACKET = ")";

        /**
         * A {@link Utils#TAB} repeated twice {@link String}. Used to customize padding inside methods' body.
         */
        public static final String DOUBLE_TAB = TAB.repeat(2);
        /**
         * A {@link Utils#LINE_SEPARATOR} repeated twice {@link String}.
         * An empty line, used as a separator of instructions in a code.
         */
        public static final String LINE = LINE_SEPARATOR.repeat(2);

        /**
         * Java source code file extension. {@code .java} file contains Java Programming Language code.
         */
        public static final String JAVA_FILE_FORMAT = ".java";
        /**
         * Java class file extension. {@code .class} files contains Java Bytecode.
         */
        public static final String CLASS_FILE_FORMAT = ".class";

        /**
         * A dot symbol {@link String}. Used to separate hierarchy of packages.
         */
        public static final Character PACKAGE_SEPARATOR_CHAR = '.';
        /**
         * A path-separator symbol {@link String}.
         */
        public static final Character PATH_SEPARATOR_CHAR = '/';
    }
}
