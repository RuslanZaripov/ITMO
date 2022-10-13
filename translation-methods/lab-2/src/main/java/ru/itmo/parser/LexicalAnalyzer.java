package ru.itmo.parser;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Arrays;

public class LexicalAnalyzer {
    private final InputStream is;
    int curPos;
    Token curToken;
    String curStr;
    private int curChar;

    public LexicalAnalyzer(InputStream is) throws ParseException {
        this.is = is;
        curPos = 0;
        nextChar();
    }

    private boolean isBlank(int c) {
        return Character.isWhitespace(c);
    }

    private void nextChar() throws ParseException {
        curPos++;
        try {
            curChar = is.read();
        } catch (IOException e) {
            throw new ParseException(e.getMessage(), curPos);
        }
    }

    public void nextToken() throws ParseException {
        skipWhitespaces();
        curToken = switch (curChar) {
            case ':' -> parseChar(Token.COLON);
            case ';' -> parseChar(Token.SEMICOLON);
            case '=' -> parseChar(Token.EQUAL);
            case -1 -> parseChar(Token.EOF);
            default -> parseToken();
        };
    }

    private Token parseChar(Token token) throws ParseException {
        nextChar();
        return token;
    }

    private Token parseToken() throws ParseException {
        curStr = collectChars();
        return Arrays.stream(Token.values())
                .filter(token -> curStr.matches(token.getRegex()))
                .findFirst()
                .orElseThrow(() -> new ParseException("Unexpected token", curPos));
    }

    private String collectChars() throws ParseException {
        StringBuilder sb = new StringBuilder();
        while (!isBlank(curChar) && !isTokenCharacter(curChar)) {
            sb.append((char) curChar);
            nextChar();
        }
        return sb.toString();
    }

    private boolean isTokenCharacter(int c) {
        return c == -1 || c == ':' || c == ';' || c == '=';
    }

    private void skipWhitespaces() throws ParseException {
        while (isBlank(curChar)) {
            nextChar();
        }
    }

    public Token curToken() {
        return curToken;
    }

    public int curPos() {
        return curPos;
    }

    public String curStr() {
        return curStr;
    }
}
