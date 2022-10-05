package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

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
            throw new ParseException(e.getMessage(), curPos) ;
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
        for (Token token : Token.values()) {
            if (curStr.matches(token.getRegex())) {
                return token;
            }
        }
        throw new ParseException(String.format("Unexpected token <%s>", curStr), curPos - curStr.length());
    }

    private String collectChars() throws ParseException {
        StringBuilder sb = new StringBuilder();
        while(!Character.isWhitespace(curChar) && Character.isLetterOrDigit(curChar)) {
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

    public String curStr() { return curStr; }
}