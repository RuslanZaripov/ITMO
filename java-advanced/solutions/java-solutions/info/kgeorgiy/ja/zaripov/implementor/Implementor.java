package info.kgeorgiy.ja.zaripov.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import static info.kgeorgiy.ja.zaripov.implementor.CodeGenerator.Utils.*;

/**
 * Generates a .jar file with the implementation of the corresponding interface.
 *
 * @author Ruslan Zaripov (zaripovruslan864@gmail.com)
 */
public class Implementor extends BaseImplementor implements JarImpler {
    private static final String TEMP_DIR_NAME = "temp";

    /**
     * A visitor of files which recursively deletes each file in a file tree.
     */
    private static final SimpleFileVisitor<Path> DELETE_VISITOR = new SimpleFileVisitor<>() {
        /**
         * Invoked for a file in a directory.
         *
         * @param file      a reference to the file
         * @param attrs     ignored
         * @return {@link FileVisitResult#CONTINUE CONTINUE} the visit result
         * @throws IOException if an I/O error occurs while deleting a file
         */
        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        /**
         * Invoked for a directory after entries in the directory, and all of their
         * descendants, have been visited.
         *
         * @param dir   a reference to the directory
         * @param e     ignored
         * @return {@link FileVisitResult#CONTINUE CONTINUE} the visit result
         * @throws IOException if an I/O error occurs while deleting directory
         */
        @Override
        public FileVisitResult postVisitDirectory(final Path dir, final IOException e) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }
    };

    /**
     * Generates a .jar file with the implementation of the corresponding interface.
     * Generating .jar file means writing code via {@link BaseImplementor#implement(Class, Path)},
     * compiling constructed .java files with {@link Implementor#compile(Class, Path)},
     * then writing .class files to the specified .jar file via {@link Implementor#write(Class, Path, Path)}.
     * The temporary directory is used to store and compile files.
     * At the end temporary directory is deleted with {@link Implementor#clean(Path)}.
     *
     * @param token     {@link Class} class type token to create implementation for
     * @param jarFile   {@link Path} a reference to the .jar file
     */
    @Override
    public void implementJar(final Class<?> token, final Path jarFile) throws ImplerException {
        validate(token);
        createDirectoriesOf(jarFile);

        final Path tempDir = createTempDir(jarFile.getParent());

        implement(token, tempDir);
        compile(token, tempDir);
        write(token, jarFile, tempDir);
        clean(tempDir);
    }

    /**
     * Creates temporary directory.
     *
     * @param path {@link Path} location where temporary directory should be created
     * @return {@link Path} location of newly created temporary directory
     * @throws ImplerException if an I/O error occurs or dir does not exist
     */
    private Path createTempDir(final Path path) throws ImplerException {
        if (path == null) {
            throw new ImplerException("Temporary directory path is not specified");
        }

        try {
            return Files.createTempDirectory(path, TEMP_DIR_NAME);
        } catch (final IOException e) {
            throw new ImplerException("Cannot create temp directory: " + path, e);
        }
    }

    /**
     * Compiles specified class.
     *
     * @param token     {@link Class} type token for class to be compiled
     * @param root      {@link Path} path where class located
     * @throws ImplerException if compiler is not specified or compiling is not successful
     */
    private void compile(final Class<?> token, final Path root) throws ImplerException {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (Objects.isNull(compiler)) {
            throw new ImplerException("Could not find java compiler");
        }

        final String filePath = root.resolve(getClassRelativePath(token, JAVA_FILE_FORMAT)).toString();
        final String classPath = root + File.pathSeparator + getClassPath(token);

        final int exitCode = compiler.run(null, null, null,
                List.of(filePath, "-cp", classPath).toArray(String[]::new));

        if (exitCode != 0) {
            throw new ImplerException("Compiler exit code: " + exitCode);
        }
    }

    /**
     * Returns code source location.
     *
     * @param token {@link Class} type token
     * @return {@link Path} class source code location
     * @throws ImplerException if class code source location cannot be converted to a URI
     */
    private Path getClassPath(final Class<?> token) throws ImplerException {
        try {
            return Path.of(token.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (final URISyntaxException e) {
            throw new ImplerException("URISyntaxException has occurred: " + e.getMessage());
        }
    }

    /**
     * Writes specified class to the specified jar-file.
     *
     * @param token     {@link Class} class type token to be written to jar-file
     * @param jarFile   {@link Path} path to the specified jar-file
     * @param tempDir   {@link Path} path where already compiled .class files located
     * @throws ImplerException if an I/O error occurs while writing jar-file
     */
    private void write(final Class<?> token, final Path jarFile, final Path tempDir) throws ImplerException {
        final Path classPath = getClassRelativePath(token, CLASS_FILE_FORMAT);

        try (JarOutputStream jarOutputStream = new JarOutputStream(Files.newOutputStream(jarFile))) {
            jarOutputStream.putNextEntry(new ZipEntry(classPath.toString()
                    .replace(File.separatorChar, PATH_SEPARATOR_CHAR)));
            Files.copy(tempDir.resolve(classPath), jarOutputStream);
        } catch (final IOException e) {
            throw new ImplerException("Cannot write jar-file: " + jarFile, e);
        }
    }

    /**
     * Removes files from a specified directory.
     *
     * @param root {@link Path} path to the directory to be deleted
     * @throws ImplerException if an I/O error is thrown by a visitor method
     */
    void clean(final Path root) throws ImplerException {
        if (Files.exists(root)) {
            try {
                Files.walkFileTree(root, DELETE_VISITOR);
            } catch (final IOException e) {
                throw new ImplerException("Cannot delete files: " + root, e);
            }
        }
    }
}
