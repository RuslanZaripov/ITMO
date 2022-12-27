import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.io.File

fun main(args: Array<String>) {
    println(System.getProperty("user.dir"))

    File("./src/main/resources/output.txt").bufferedWriter().use { out ->
        val input = CharStreams.fromFileName("./src/main/resources/input.txt")
        val parser = CPPParser(CommonTokenStream(CPPLexer(input)))
        val parsedTree = parser.translationUnit()
        val visitor = Visitor()
        visitor.visit(parsedTree)
        println("Code: <\n${visitor.getCode()}\n>")
        out.write(visitor.getCode())
    }
}