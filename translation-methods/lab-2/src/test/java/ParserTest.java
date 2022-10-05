import org.example.Parser;
import org.example.Token;
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
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;

import static org.example.node.type.KotlinType.INT;
import static org.example.node.type.KotlinType.STRING;
import static org.junit.Assert.assertEquals;

public class ParserTest {
    private final Parser parser = new Parser();
    private final Modifier var = new Modifier(Token.VAR);
    private final Modifier val = new Modifier(Token.VAL);
    private final Type Int = new Type(INT);
    private final Type String = new Type(STRING);

    private static <T> Statement st(Modifier mod, String id, Type type, T value) {
        return new StatementWithAssignment(mod, new Identifier(id), type, specifyValue(value));
    }

    private static Statement st(Modifier mod, String id, Type type) {
        return new StatementWithoutAssignment(mod, new Identifier(id), type);
    }

    private static <T> Value<?> specifyValue(T value) {
        return switch (value) {
            case Integer i -> new IntValue(i);
            case String s -> new StringValue(s);
            default -> throw new IllegalArgumentException();
        };
    }

    @Test
    public void testParse1() {
        assertEquals(new Code(st(var, "a", Int, 1)), test("var a: Int = 1;"));
    }

    @Test
    public void testParse2() {
        assertEquals(
                new Code(st(var, "a", Int, 1), st(var, "b", Int, 2)),
                test("var a: Int = 1; var b: Int = 2;")
        );
    }

    @Test
    public void testParse3() {
        assertEquals(new Code(st(var, "a", Int)), test("var a: Int;"));
    }

    @Test
    public void testParse4() {
        assertEquals(new Code(st(val, "a", Int, 1)), test("val a: Int = 1;"));
    }

    @Test
    public void testParse5() {
        assertEquals(new Code(st(val, "a", Int)), test("val a: Int;"));
    }

    @Test
    public void testParse6() {
        assertEquals(new Code(st(var, "a", String)), test("var a: String;"));
    }

    @Test(expected = ParseException.class)
    public void testParse7() throws ParseException {
        build("var a: String = 1;");
    }

    @Test
    public void testParse8() {
        assertEquals(new Code(st(var, "a1", Int)), test("var a1: Int;"));
    }

    @Test
    public void testParse9() {
        assertEquals(new Code(st(var, "a", Int), st(var, "b", Int, 2)), test("var a: Int; var b: Int = 2;"));
    }

    @Test
    public void testParse10() {
        assertEquals(new Code(st(var, "a", String, "Hello")), test("var a: String = \"Hello\";"));
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