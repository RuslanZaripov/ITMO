package info.kgeorgiy.ja.zaripov.implementor;

import static info.kgeorgiy.ja.zaripov.implementor.CodeGenerator.Utils.IMPL_SUFFIX;
import static info.kgeorgiy.ja.zaripov.implementor.CodeGenerator.Utils.JAVA_FILE_FORMAT;
import static info.kgeorgiy.ja.zaripov.implementor.CodeGenerator.Utils.PACKAGE_SEPARATOR_CHAR;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

/**
 * Generates .java file with an interface implementation.
 *
 * @author Ruslan Zaripov (zaripovruslan864@gmail.com)
 */
public class BaseImplementor implements Impler {
    /**
     * Validates specified token.
     *
     * @param token {@link Class} type token to be validated
     * @throws ImplerException if type token is not interface or has private modifiers
     */
    protected void validate(final Class<?> token) throws ImplerException {
        if (!token.isInterface()) {
            throw new ImplerException("Expected interface");
        }

        if (Modifier.isPrivate(token.getModifiers())) {
            throw new ImplerException("Incorrect token: includes private modifier");
        }
    }

    /**
     * Writes java code implementation for specified {@link Class} type token.
     * Writing to file carried out via {@link Files#newBufferedReader(Path, java.nio.charset.Charset)}.
     * {@link StandardCharsets#UTF_8} is specified for encoding.
     * Code is generated with {@link CodeGenerator#generate(Class)} method.
     *
     * @param token class type token to create implementation for
     * @param root  directory generated class to be located
     * @throws ImplerException if an I/O error occurs opening or creating the file
     */
    @Override
    public void implement(final Class<?> token, final Path root) throws ImplerException {
        validate(token);
        final Path classFile = root.resolve(getClassRelativePath(token, JAVA_FILE_FORMAT));
        createDirectoriesOf(classFile);

        try (BufferedWriter writer = Files.newBufferedWriter(classFile, StandardCharsets.UTF_8)) {
            writer.write(toUnicode(CodeGenerator.generate(token)));
        } catch (final IOException e) {
            throw new ImplerException("Cannot write file: " + classFile, e);
        }
    }

    /**
     * Returns class relative path for the specified token.
     *
     * @param token {@link Class} type token for class which relative path will be generated
     * @param fileFormat constant which specifies file extension
     * @return {@link Path} class relative path
     */
    protected Path getClassRelativePath(final Class<?> token, final String fileFormat) {
        final String dir = token.getPackageName().replace(PACKAGE_SEPARATOR_CHAR, File.separatorChar);
        final String file = token.getSimpleName().concat(IMPL_SUFFIX).concat(fileFormat);
        return Path.of(dir).resolve(file);
    }

    /**
     * Create subdirectories of the specified path.
     *
     * @param path {@link Path} location of file which subdirectories will be created
     * @throws ImplerException if an I/O error occurs while creating directory
     */
    protected void createDirectoriesOf(final Path path) throws ImplerException {
        final Path outFileParent = path.getParent();
        if (outFileParent != null) {
            try {
                Files.createDirectories(outFileParent);
            } catch (final IOException e) {
                throw new ImplerException("Cannot create directories: " + outFileParent, e);
            }
        }
    }

    /**
     * Converts string to unicode representation.
     *
     * @param str given string
     * @return unicode representation
     */
    protected String toUnicode(final String str) {
        return str.chars()
                .mapToObj(c -> String.format("\\u%04X", c))
                .collect(Collectors.joining());
    }

    /**
     * Main method for {@link BaseImplementor} and {@link Implementor}.
     * Correct usage: {@code [-jar] <class-name> [output-file]}.
     * If first argument equals {@code -jar} runs {@link Implementor#implementJar(Class, Path)},
     * {@link BaseImplementor#implement(Class, Path)} otherwise.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        if (!checkArgs(args)) {
            printUsage();
            return;
        }

        try {
            final String option = args[0];
            if (!option.equals("-jar")) {
                final BaseImplementor implementor = new BaseImplementor();

                final Class<?> clazz = Class.forName(args[0]);
                switch (args.length) {
                    case 1 -> implementor.implement(clazz, Path.of(getCurrentWorkingDir()));
                    case 2 -> implementor.implement(clazz, Path.of(args[1]));
                    default -> printUsage();
                }
            } else {
                args = removeOption(args);
                if (args.length != 2) {
                    printUsage();
                    return;
                }
                new Implementor().implementJar(Class.forName(args[0]), Path.of(args[1]));
            }
        } catch (final InvalidPathException e) {
            System.err.println("Cannot create path instance: " + args[1]);
        } catch (final ClassNotFoundException e) {
            System.err.println("Class cannot be located: " + args[0]);
        } catch (final ImplerException e) {
            System.err.println("Implementor exception has occurred: " + e.getMessage());
        }
    }

    /**
     * Validates command line arguments.
     *
     * @param args  command line arguments
     * @return      <code>true</code> if array and its' elements exist.
     *              <code>false</code> otherwise.
     */
    private static boolean checkArgs(final String[] args) {
        return !Objects.isNull(args) && Arrays.stream(args).noneMatch(Objects::isNull) && args.length >= 1;
    }

    /**
     * Prints valid command line arguments.
     */
    private static void printUsage() {
        System.err.println("Usage:\n<class-name> [output-file]\n-jar <class-name> [jar-output-file]");
    }

    /**
     * Returns "user.dir" system property.
     *
     * @return "user.dir" system property
     */
    private static String getCurrentWorkingDir() {
        return System.getProperty("user.dir");
    }

    /**
     * Removes first element from the array.
     *
     * @param args array which first element will be removed
     * @return returns copy of the array with the first element removed
     */
    private static String[] removeOption(final String[] args) {
        return Arrays.copyOfRange(args, 1, args.length);
    }
}
