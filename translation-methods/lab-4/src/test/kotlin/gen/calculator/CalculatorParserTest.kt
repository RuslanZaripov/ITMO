package gen.calculator

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

class CalculatorParserTest {
    private val path = "src/test/resources/images/calculator/%s.dot"

    @Test
    fun expr1() {
        val ast = CalculatorParser(CalculatorLexer("(---3*(4--5)+10)/-2")).parse()
        assertEquals(8, ast.res)
        writeToFile("expr1", Visualizer().visualize(ast))
    }
    @Test
    fun expr2() {
        val ast = CalculatorParser(CalculatorLexer("10-2*(3*4-5)")).parse()
        assertEquals(-4, ast.res)
    }

    private fun writeToFile(testName: String, content: String) {
        val file = File(String.format(path, testName))
        file.parentFile.mkdirs()
        file.writeText(content)
    }
}