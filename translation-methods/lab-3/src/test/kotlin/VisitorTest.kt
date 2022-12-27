import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals

class VisitorTest {

    companion object {
        private var OUTPUT_FILE = "./src/test/resources/tests/%s/output.txt"
        private var INPUT_FILE = "./src/test/resources/tests/%s/input.txt"
    }

    private fun format(stream: CharStream): String {
        val parser = CPPParser(CommonTokenStream(CPPLexer(stream)))
        val parsedTree = parser.translationUnit()
        val visitor = Visitor()
        visitor.visit(parsedTree)
        return visitor.getCode()
    }

    private fun formatViaString(input: String): String {
        return format(CharStreams.fromString(input))
    }

    private fun formatViaFile(fileName: String): String {
        return format(CharStreams.fromFileName(fileName))
    }

    private fun test(name: String) {
        val output = formatViaFile(INPUT_FILE.format(name))
        val expected = File(OUTPUT_FILE.format(name)).readText()
        assertEquals(expected, output)
    }

    @Test
    fun testInitial() {
        test("initial")
    }

    @Test
    fun testSelectionStatement() {
        test("selectionStatement")
    }

    @Test
    fun testExpressions() {
        test("expressions")
    }

    @Test
    fun testRandom() {
        test("random")
    }

    @Test
    fun testIterationStatement() {
        test("iterationStatement")
    }
}