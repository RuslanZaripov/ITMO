package info.kgeorgiy.ja.zaripov.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class Walker {
    private final Charset CHARSET = StandardCharsets.UTF_8;
    private final Path inputFile;
    private final Path outputFile;

    public Walker(String inputFile, String outputFile) throws WalkerException {
        this.inputFile = getPath(inputFile);
        this.outputFile = getPath(outputFile);
        createDirectoriesOf(this.outputFile);
    }

    public void walk() throws WalkerException {
        try (BufferedReader in = Files.newBufferedReader(inputFile, CHARSET)) {
            try (BufferedWriter out = Files.newBufferedWriter(outputFile, CHARSET)) {
                FileVisitor fileVisitor = new FileVisitor(out);
                String fileName;
                while ((fileName = in.readLine()) != null) {
                    try {
                        Files.walkFileTree(Path.of(fileName), fileVisitor);
                    } catch (InvalidPathException | IOException e) {
                        fileVisitor.handleHashingFailure(fileName);
                    }
                }
            } catch (IOException e) {
                throw new WalkerException("Cannot write file " + outputFile);
            }
        } catch (IOException e) {
            throw new WalkerException("Cannot read file " + inputFile);
        }
    }

    private void createDirectoriesOf(Path outputFile) throws WalkerException {
        Path outputFileParent = outputFile.getParent();
        if (outputFileParent != null) {
            try {
                Files.createDirectories(outputFileParent);
            } catch (FileAlreadyExistsException e) {
                throw new WalkerException("File already exists: " + outputFile);
            } catch (IOException e) {
                throw new WalkerException("Cannot create directories of " + outputFileParent);
            }
        }
    }

    private static Path getPath(String file) throws WalkerException {
        try {
            return Path.of(file);
        } catch (InvalidPathException e) {
            throw new WalkerException("Cannot create path of " + file);
        }
    }
}
