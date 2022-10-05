package org.example;

import org.example.node.Code;
import org.example.node.Identifier;
import org.example.node.Modifier;
import org.example.node.Node;
import org.example.node.statement.Statement;
import org.example.node.statement.StatementWithAssignment;
import org.example.node.statement.StatementWithoutAssignment;
import org.example.node.type.Type;
import org.example.node.value.IntValue;
import org.example.node.value.StringValue;
import org.example.node.value.Value;

import java.io.InputStream;
import java.text.ParseException;

import static org.example.node.type.KotlinType.fromName;

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
                Value<?> value = Assignment(type);
                if (value != null) {
                    lex.nextToken();
                    expect(Token.SEMICOLON);
                    return new StatementWithAssignment(modifier, id, type, value);
                } else {
                    return new StatementWithoutAssignment(modifier, id, type);
                }
            }
            default -> throw new ParseException("Expected 'var' or 'val'", lex.curPos);
        }
    }

    // Modifier -> VAR | VAL
    private Modifier Modifier() throws ParseException {
        return switch (lex.curToken()) {
            case VAR, VAL -> new Modifier(lex.curToken());
            default -> throw new ParseException("Expected 'var' or 'val'", lex.curPos);
        };
    }

    // Type -> COLON TYPE
    private Type Type() throws ParseException {
        if (lex.curToken() == Token.COLON) {
            lex.nextToken();
            expect(Token.TYPE);
            return new Type(fromName(lex.curStr()));
        }
        throw new ParseException("Expected ':'", lex.curPos);
    }

    // Assignment -> = VALUE | eps
    private Value<?> Assignment(Type type) throws ParseException {
        switch (lex.curToken()) {
            case EQUAL -> {
                lex.nextToken();
                expect(Token.VALUE);
                return parseValue(type);
            }
            case SEMICOLON -> {
                return null;
            }
            default -> throw new ParseException("Expected '=' or ';'", lex.curPos);
        }
    }

    private Value<?> parseValue(Type type) throws ParseException {
        final String value = lex.curStr();
        if (!value.matches(type.type().getRegex())) {
            throw new ParseException("Value doesn't match type", lex.curPos);
        }
        try {
            return switch (type.type()) {
                case INT -> new IntValue(Integer.parseInt(value));
                case STRING -> new StringValue(value.replaceAll("^\"|\"$", ""));
            };
        } catch (IllegalArgumentException ex) {
            throw new ParseException(String.format("Expected '%s'", type.type().getName()), lex.curPos);
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