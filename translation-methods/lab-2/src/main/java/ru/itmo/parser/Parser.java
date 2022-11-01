package ru.itmo.parser;

import ru.itmo.parser.node.*;
import ru.itmo.parser.node.assignment.Assignment;
import ru.itmo.parser.node.assignment.EmptyAssignment;
import ru.itmo.parser.node.assignment.ValidAssignment;
import ru.itmo.parser.node.type.Type;
import ru.itmo.parser.node.value.IntValue;
import ru.itmo.parser.node.value.StringValue;
import ru.itmo.parser.node.value.Value;

import java.io.InputStream;
import java.text.ParseException;

import static ru.itmo.parser.node.type.KotlinType.fromName;

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
        return code == null ? new Code(statement) : new Code(statement, code);
    }

    // Statement -> Modifier ID Type Assignment SEMICOLON
    private Statement Statement(Modifier modifier) throws ParseException {
        switch (lex.curToken()) {
            case VAR -> {
                lex.nextToken();
                expect(Token.ID);
                Identifier id = new Identifier(lex.curStr());
                lex.nextToken();
                Type type = Type();
                lex.nextToken();
                Assignment assignment = Assignment(type);
                return new Statement(modifier, id, type, assignment);
            }
            case VAL -> {
                lex.nextToken();
                expect(Token.ID);
                Identifier id = new Identifier(lex.curStr());
                lex.nextToken();
                Type type = Type();
                lex.nextToken();
                if (lex.curToken == Token.SEMICOLON) {
                    throw new ParseException("Expected '='", lex.curPos);
                }
                Assignment assignment = Assignment(type);
                return new Statement(modifier, id, type, assignment);
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
    private Assignment Assignment(Type type) throws ParseException {
        switch (lex.curToken()) {
            case EQUAL -> {
                lex.nextToken();
                expect(Token.VALUE);
                lex.nextToken();
                expect(Token.SEMICOLON);
                return new ValidAssignment(parseValue(type));
            }
            case SEMICOLON -> {
                return new EmptyAssignment();
            }
            default -> throw new ParseException("Expected '=' or ';'", lex.curPos);
        }
    }

    private Value<?> parseValue(Type type) throws ParseException {
        final String value = lex.curStr();
        if (!value.matches(type.getType().getRegex())) {
            throw new ParseException("Value doesn't match type", lex.curPos);
        }
        try {
            return switch (type.getType()) {
                case INT -> new IntValue(Integer.parseInt(value));
                case STRING -> new StringValue(value.replaceAll("^\"|\"$", ""));
            };
        } catch (IllegalArgumentException ex) {
            throw new ParseException("Expected '%s'".formatted(type.getType().getName()), lex.curPos);
        }
    }

    private void expect(Token token) throws ParseException {
        if (lex.curToken() != token) {
            throw new ParseException(
                    "Token <%s> doesn't match regex <%s>".formatted(lex.curToken(), token.getRegex()),
                    lex.curPos()
            );
        }
    }


    public Node parse(InputStream in) throws ParseException {
        lex = new LexicalAnalyzer(in);
        lex.nextToken();
        return StatementList();
    }
}