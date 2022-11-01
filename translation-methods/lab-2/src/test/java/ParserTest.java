import org.junit.Test;
import ru.itmo.parser.Parser;
import ru.itmo.parser.Token;
import ru.itmo.parser.node.*;
import ru.itmo.parser.node.assignment.EmptyAssignment;
import ru.itmo.parser.node.assignment.ValidAssignment;
import ru.itmo.parser.node.type.Type;
import ru.itmo.parser.node.value.IntValue;
import ru.itmo.parser.node.value.StringValue;
import ru.itmo.parser.node.value.Value;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;

import static org.junit.Assert.assertEquals;
import static ru.itmo.parser.node.type.KotlinType.INT;
import static ru.itmo.parser.node.type.KotlinType.STRING;

public class ParserTest {
    private final Parser parser = new Parser();
    private final Modifier var = new Modifier(Token.VAR);
    private final Modifier val = new Modifier(Token.VAL);
    private final Type Int = new Type(INT);
    private final Type String = new Type(STRING);

    private static <T> Statement st(Modifier mod, String id, Type type, T value) {
        return new Statement(mod, new Identifier(id), type, new ValidAssignment(specifyValue(value)));
    }

    private static Statement st(Modifier mod, String id, Type type) {
        return new Statement(mod, new Identifier(id), type, new EmptyAssignment());
    }

    private static Code code(Statement... statements) {
        return new Code(statements);
    }

    private static <T> Value<?> specifyValue(T value) {
        return switch (value) {
            case Integer i -> new IntValue(i);
            case String s -> new StringValue(s);
            default -> throw new IllegalArgumentException();
        };
    }

    @Test
    public void testVarDeclWithAssignment() {
        assertEquals(code(st(var, "a", Int, 1)), test("var a: Int = 1;"));
    }

    @Test
    public void testTwoVarDeclsWithAssignment() {
        assertEquals(
                code(st(var, "a", Int, 1), st(var, "b", Int, 2)),
                test("var a: Int = 1; var b: Int = 2;")
        );
    }

    @Test
    public void testVarDeclsWithoutAssignment() {
        assertEquals(code(st(var, "a", Int)), test("var a: Int;"));
    }

    @Test
    public void testValDeclWithAssignment() {
        assertEquals(code(st(val, "a", Int, 1)), test("val a: Int = 1;"));
    }

    @Test
    public void testStringType() {
        assertEquals(code(st(var, "a", String)), test("var a: String;"));
    }

    @Test(expected = ParseException.class)
    public void testIncorrectAssignmentStringTypeWithIntValue() throws ParseException {
        build("var a: String = 1;");
    }

    @Test
    public void testIdentifier() {
        assertEquals(code(st(var, "a1", Int)), test("var a1: Int;"));
    }

    @Test
    public void testManyDifferentDecls() {
        assertEquals(code(st(var, "a", Int), st(var, "b", Int, 2)), test("var a: Int; var b: Int = 2;"));
    }

    @Test
    public void testStringAssignment() {
        assertEquals(code(st(var, "a", String, "Hello")), test("var a: String = \"Hello\";"));
    }

    @Test(expected = ParseException.class)
    public void testMissingType() throws ParseException {
        build("var a = 1;");
    }

    @Test(expected = ParseException.class)
    public void testMissingModifier() throws ParseException {
        build("a: Int = 1;");
    }

    @Test(expected = ParseException.class)
    public void testMissingEqualSign() throws ParseException {
        build("var a: Int 1;");
    }

    @Test(expected = ParseException.class)
    public void testIncorrectValDecl() throws ParseException {
        build("val x: Int;");
    }

    Node build(String input) throws ParseException {
        return parser.parse(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
    }

    Node test(String input) {
        try {
            return build(input);
        } catch (ParseException e) {
            System.out.println("Exception message:\n\t" + e.getMessage());
        }
        return null;
    }
}