package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Arrays;

import static org.example.Token.*;

public class LexicalAnalyzer {
    InputStream is;
    int curChar;
    int curPos;
    Token curToken;
    String curStr;

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
            case ':' -> nextCharAndReturn(COLON);
            case ';' -> nextCharAndReturn(SEMICOLON);
            case '=' -> nextCharAndReturn(EQUAL);
            case -1 -> nextCharAndReturn(EOF);
            default -> parseToken();
        };
    }

    private Token nextCharAndReturn(Token token) throws ParseException {
        nextChar();
        return token;
    }

    private Token parseToken() throws ParseException {
        curStr = collectChars();
        return Arrays.stream(values())
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