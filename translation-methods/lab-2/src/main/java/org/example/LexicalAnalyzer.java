package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Arrays;

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
        switch (curChar) {
            case ':' -> {
                nextChar();
                curToken = Token.COLON;
            }
            case ';' -> {
                nextChar();
                curToken = Token.SEMICOLON;
            }
            case '=' -> {
                nextChar();
                curToken = Token.EQUAL;
            }
            case -1 -> {
                nextChar();
                curToken = Token.EOF;
            }
            default -> curToken = getToken();
        }
    }

    private Token getToken() throws ParseException {
        curStr = collectChars();
        return Arrays.stream(Token.values())
                .filter(token -> curStr.matches(token.getRegex()))
                .findFirst()
                .orElseThrow(() -> new ParseException("Unexpected token", curPos));
    }

    // TODO: try getting rid of hard-coded values
    private String collectChars() throws ParseException {
        StringBuilder sb = new StringBuilder();
        while (!isBlank(curChar)
                && curChar != -1
                && curChar != ':'
                && curChar != ';'
                && curChar != '=') {
            sb.append((char) curChar);
            nextChar();
        }
        return sb.toString();
    }

    public void skipWhitespaces() throws ParseException {
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