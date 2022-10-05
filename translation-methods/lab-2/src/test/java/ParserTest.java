import org.example.Parser;
import org.example.node.*;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;

import static org.junit.Assert.assertEquals;

public class ParserTest {
    Parser parser = new Parser();

    private final Modifier var = new Modifier("var");
    private final Modifier val = new Modifier("val");
    private final Type Int = new Type("Int");
    private final Type String = new Type("String");

    
    @Test
    public void testParse1() {
        String input = "var a: Int = 1;";
        assertEquals(new Code(st(var, "a", Int, "1")), test(input));
    }

    @Test
    public void testParse2() {
        String input = "var a: Int = 1; var b: Int = 2;";
        assertEquals(new Code(st(var, "a", Int, "1"), st(var, "b", Int, "2")), test(input));
    }

    // test without assignment
    @Test
    public void testParse3() {
        String input = "var a: Int;";
        assertEquals(new Code(st(var, "a", Int)), test(input));
    }

    // test for val
    @Test
    public void testParse4() {
        String input = "val a: Int = 1;";
        assertEquals(new Code(st(val, "a", Int, "1")), test(input));
    }

    // test for val without assignment
    @Test
    public void testParse5() {
        String input = "val a: Int;";
        assertEquals(new Code(st(val, "a", Int)), test(input));
    }

    @Test
    public void testParse6() {
        String input = "var a: String;";
        assertEquals(new Code(st(var, "a", String)), test(input));
    }

    // test assignment int to string
    @Test(expected = ParseException.class)
    public void testParse7() throws ParseException {
        build("var a: String = 1;");
    }

    // test assign identifier with characters and numbers
    @Test
    public void testParse8() {
        String input = "var a1: Int;";
        assertEquals(new Code(st(var, "a1", Int)), test(input));
    }

    @Test
    public void testParse9() {
        String input = "var a: Int; var b: Int = 2;";
        assertEquals(new Code(st(var, "a", Int), st(var, "b", Int, "2")), test(input));
    }


    private static Statement st(Modifier mod, String id, Type type, String value) {
        return new StatementWithAssignment(
                mod,
                new Identifier(id),
                type,
                new Value(value)
        );
    }

    private static Statement st(Modifier mod, String id, Type type) {
        return new StatementWithoutAssignment(
                mod,
                new Identifier(id),
                type
        );
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