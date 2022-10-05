package org.example;

import org.example.node.*;

import java.io.InputStream;
import java.text.ParseException;

public class Parser {
    LexicalAnalyzer lex;

    // StatementList -> Statement ListPrime
    private Code StatementList() throws ParseException {
        return switch (lex.curToken()) {
            case VAR, VAL -> getCode();
            default -> throw new ParseException("Expected 'var' or 'val'", lex.curPos);
        };
    }

    // ListPrime -> Statement ListPrime | eps
    private Code ListPrime() throws ParseException {
        return switch (lex.curToken()) {
            case VAR, VAL -> getCode();
            case EOF -> null;
            default -> throw new ParseException("Expected 'var' or 'val' or 'EOF'", lex.curPos);
        };
    }

    private Code getCode() throws ParseException {
        Statement statement = Statement(Modifier());
        lex.nextToken();
        Code code = ListPrime();
        if (code == null) {
            return new Code(statement);
        }
        return new Code(statement, code);
    }

    // Statement -> Modifier ID Type Assignment SEMICOLON
    private Statement Statement(Modifier modifier) throws ParseException {
        switch (lex.curToken()) {
            case VAR, VAL -> {
                lex.nextToken();
                expect(Token.ID);
                Identifier id = new Identifier(lex.curStr());
                lex.nextToken();
                Type type = Type();
                lex.nextToken();
                Value value = Assignment();
                if (value != null) {
                    if (!value.getType().equals(type.getType())) {
                        throw new ParseException("Type mismatch", lex.curPos);
                    }
                    lex.nextToken();
                    expect(Token.SEMICOLON);
                    return new StatementWithAssignment(modifier, id, type, value);
                }
                return new StatementWithoutAssignment(modifier, id, type);
            }
            default -> throw new ParseException("Expected 'var' or 'val'", lex.curPos);
        }
    }

    // Modifier -> VAR | VAL
    private Modifier Modifier() throws ParseException {
        return switch (lex.curToken()) {
            case VAR, VAL -> new Modifier(lex.curStr());
            default -> throw new ParseException("Expected 'var' or 'val'", lex.curPos);
        };
    }

    // Type -> COLON TYPE
    private Type Type() throws ParseException {
        if (lex.curToken() == Token.COLON) {
            lex.nextToken();
            expect(Token.TYPE);
            return new Type(lex.curStr());
        }
        throw new ParseException("Expected ':'", lex.curPos);
    }

    // Assignment -> = VALUE | eps
    private Value Assignment() throws ParseException {
        switch (lex.curToken()) {
            case EQUAL -> {
                lex.nextToken();
                expect(Token.VALUE);
                return new Value(lex.curStr());
            }
            case SEMICOLON -> {
                return null;
            }
            default -> throw new ParseException("Expected '=' or ';'", lex.curPos);
        }
    }

    private void expect(Token token) throws ParseException {
        if (lex.curToken() != token) {
            throw new ParseException(
                    String.format("Expected token <%s>, actual token <%s>", lex.curToken(), token.getRegex()),
                    lex.curPos());
        }
    }


    public Node parse(InputStream in) throws ParseException {
        lex = new LexicalAnalyzer(in);
        lex.nextToken();
        final Code code = StatementList();
        if (lex.curToken != Token.EOF) {
            throw new ParseException("Unexpected token: " + lex.curToken, lex.curPos);
        }
        return code;
    }
}