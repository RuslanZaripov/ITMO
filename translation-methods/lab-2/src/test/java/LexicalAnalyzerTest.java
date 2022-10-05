import org.example.LexicalAnalyzer;
import org.example.Token;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class LexicalAnalyzerTest {

    // test var token
    @Test
    public void testVarToken() throws ParseException {
        test("var", Token.VAR);
    }

    // test val token
    @Test
    public void testValToken() throws ParseException {
        test("val", Token.VAL);
    }

    // test id token
    @Test
    public void testIdToken() throws ParseException {
        test("qwerty12345", Token.ID);
    }

    // test type token
    @Test
    public void testTypeToken() throws ParseException {
        test("Int", Token.TYPE);
    }

    @Test
    public void testStringType() throws ParseException {
        test("String", Token.TYPE);
    }

    @Test
    public void testSemicolonToken() throws ParseException {
        test(";", Token.SEMICOLON);
    }

    @Test
    public void testColonToken() throws ParseException {
        test(":", Token.COLON);
    }

    @Test
    public void testEqualToken() throws ParseException {
        test("=", Token.EQUAL);
    }

    // test value
    @Test
    public void testValueToken() throws ParseException {
        test("5", Token.VALUE);
    }

    @Test
    public void testVarDeclarationWithAssignment() throws ParseException {
        test("var x: Int = 5;", varDeclarationWithAssignment());
    }

    @Test
    public void testValDeclarationWithAssignment() throws ParseException {
        test("val x: Int = 5;", valDeclarationWithAssignment());
    }

    @Test
    public  void testVarDeclarationWithoutAssignment() throws ParseException {
        test("var x: Int;", varDeclarationWithoutAssignment());
    }

    @Test
    public  void testValDeclarationWithoutAssignment() throws ParseException {
        test("val x: Int;", valDeclarationWithoutAssignment());
    }

    @Test
    public void testMultipleDeclarations() throws ParseException {
        test("var x : Int = 5; val y : Int = 6;",
                combine(varDeclarationWithAssignment(), valDeclarationWithAssignment())
        );
    }

    private Token[] combine(Token[]... lists) {
        return Arrays.stream(lists).flatMap(Arrays::stream).toArray(Token[]::new);
    }

    public void test(String input, Token... tokens) throws ParseException {
        LexicalAnalyzer lex = new LexicalAnalyzer(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        lex.nextToken();
        for (Token token : tokens) {
            if (token == Token.EOF) {
                break;
            }
            assertEquals(lex.curToken(), token);
            lex.nextToken();
        }
        assertEquals(lex.curToken(), Token.EOF);
    }

    public Token[] pattern(Token declarationKeyword, boolean withAssignment) {
        List<Token> tokens = new ArrayList<>(List.of(declarationKeyword, Token.ID, Token.COLON, Token.TYPE));
        if (withAssignment) {
            tokens.addAll(List.of(Token.EQUAL, Token.VALUE));
        }
        tokens.add(Token.SEMICOLON);
        return tokens.toArray(Token[]::new);
    }

    public Token[] varDeclarationWithoutAssignment() {
        return pattern(Token.VAR, false);
    }

    public Token[] valDeclarationWithAssignment() {
        return pattern(Token.VAL, true);
    }

    public Token[] valDeclarationWithoutAssignment() {
        return pattern(Token.VAL, false);
    }

    public Token[] varDeclarationWithAssignment() {
        return pattern(Token.VAR, true);
    }
}