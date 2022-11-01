import org.junit.Test;
import ru.itmo.parser.LexicalAnalyzer;
import ru.itmo.parser.Token;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class LexicalAnalyzerTest {
    @Test
    public void testVarToken() throws ParseException {
        test("var", Token.VAR);
    }

    @Test
    public void testValToken() throws ParseException {
        test("val", Token.VAL);
    }

    @Test
    public void testIdToken() throws ParseException {
        test("qwerty12345", Token.ID);
    }

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
    public void testVarDeclarationWithoutAssignment() throws ParseException {
        test("var x: Int;", varDeclarationWithoutAssignment());
    }

    @Test
    public void testValDeclarationWithoutAssignment() throws ParseException {
        test("val x: Int;", valDeclarationWithoutAssignment());
    }

    @Test
    public void testMultipleDeclarations() throws ParseException {
        test("var x : Int = 5; val y : Int = 6;", combine(varDeclarationWithAssignment(), valDeclarationWithAssignment()));
    }

    @Test
    public void testMultipleDeclarationsWithoutAssignment() throws ParseException {
        test("var x : Int; var y : Int;", combine(repeat(2, varDeclarationWithoutAssignment())));
    }

    private Token[] combine(Token[]... lists) {
        return Arrays.stream(lists).flatMap(Arrays::stream).toArray(Token[]::new);
    }

    private Token[] repeat(int count, Token[] expr) {
        Token[] result = new Token[0];
        for (int i = 0; i < count; i++) {
            result = combine(result, expr);
        }
        return result;
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

    public Token[] constructExpr(Token declarationKeyword, boolean withAssignment) {
        List<Token> tokens = new ArrayList<>(List.of(declarationKeyword, Token.ID, Token.COLON, Token.TYPE));
        if (withAssignment) {
            tokens.addAll(List.of(Token.EQUAL, Token.VALUE));
        }
        tokens.add(Token.SEMICOLON);
        return tokens.toArray(Token[]::new);
    }

    public Token[] varDeclarationWithoutAssignment() {
        return constructExpr(Token.VAR, false);
    }

    public Token[] valDeclarationWithAssignment() {
        return constructExpr(Token.VAL, true);
    }

    public Token[] valDeclarationWithoutAssignment() {
        return constructExpr(Token.VAL, false);
    }

    public Token[] varDeclarationWithAssignment() {
        return constructExpr(Token.VAR, true);
    }
}