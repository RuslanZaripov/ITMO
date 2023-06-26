package gen.calculator

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

class `CalculatorParserTest-mod2` {
    private val path = "src/test/resources/images/calculator/%s.dot"

    @Test
    fun expr1() {
        val ast = CalculatorParser(CalculatorLexer("(---3*(4--5)+10)/-2")).parse()
        assertEquals(8.5, ast.res)
        writeToFile("expr1", Visualizer().visualize(ast))
    }

    @Test
    fun expr2() {
        val ast = CalculatorParser(CalculatorLexer("10-2*(3*4-5)")).parse()
        assertEquals(-4.0, ast.res)
    }

    @Test
    fun expr3() {
        val ast = CalculatorParser(CalculatorLexer("  (  10 / 3) - 10 -2*( 3 * 4-5)  ")).parse()
        assertEquals(-20.666666666666664, ast.res)
    }

    @Test
    fun expr4() {
        val ast = CalculatorParser(CalculatorLexer("  (  10 / 3) - (5/ 2)  ")).parse()
        assertEquals(0.8333333333333335, ast.res)
    }

    @Test
    fun expr5() {
        val ast = CalculatorParser(CalculatorLexer("2 ** 3 ** 2")).parse()
        assertEquals(512.0, ast.res)
    }

    private fun writeToFile(testName: String, content: String) {
        val file = File(String.format(path, testName))
        file.parentFile.mkdirs()
        file.writeText(content)
    }
}