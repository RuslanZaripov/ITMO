package info.kgeorgiy.ja.zaripov.walk;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import static java.nio.file.FileVisitResult.*;

public class FileVisitor extends SimpleFileVisitor<Path> {
    private final static int BUFFER_SIZE = 1024;
    private final static String HASHING_ALGORITHM = "SHA-1";
    private final static String HASHING_FAILED_RESULT = "0".repeat(40);

    private final BufferedWriter out;

    public FileVisitor(BufferedWriter out) {
        this.out = out;
    }

    public FileVisitResult handleHashingFailure(String file) throws IOException {
        write(HASHING_FAILED_RESULT, file);
        return CONTINUE;
    }

    private FileVisitResult write(String hash, String file) throws IOException {
        out.write(hash + ' ' + file);
        out.newLine();
        return CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return handleHashingFailure(dir.toString());
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return handleHashingFailure(file.toString());
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        MessageDigest md = getMessageDigest();
        if (Objects.isNull(md)) {
            return handleHashingFailure(file.toString());
        }

        return write(getHash(file, md), file.toString());
    }

    private static MessageDigest getMessageDigest() {
        try {
            return MessageDigest.getInstance(HASHING_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    private String getHash(Path file, MessageDigest md) throws IOException {
        byte[] byteArray = new byte[BUFFER_SIZE];
        int bytesCount;

        try (InputStream is = Files.newInputStream(file)) {
            while ((bytesCount = is.read(byteArray)) != -1) {
                md.update(byteArray, 0, bytesCount);
            }
        }

        return toHex(md.digest());
    }


    private static String toHex(byte[] bytes) {
        return String.format("%0" + (bytes.length << 1) + "x", new BigInteger(1, bytes));
    }
}
