package gen.labsecondgrammar

import java.io.File

class LabSecondGrammarParserTest {
    private val path = "src/test/resources/images/labsecondgrammar/%s.dot"

    @org.junit.jupiter.api.Test
    fun expr1() {
        val ast = LabSecondGrammarParser(LabSecondGrammarLexer("val a: Int; var a: Int = 4;")).parse()
        writeToFile("expr1", Visualizer().visualize(ast))
    }

    private fun writeToFile(testName: String, content: String) {
        val file = File(String.format(path, testName))
        file.parentFile.mkdirs()
        file.writeText(content)
    }
}