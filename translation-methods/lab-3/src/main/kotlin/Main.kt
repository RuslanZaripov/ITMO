import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.io.File

fun main(args: Array<String>) {
    println("Hello World!")

    // print current directory
    println(System.getProperty("user.dir"))

//    val input = CharStreams.fromFileName("./src/test/antlr/input.txt")
//    val parser = CPPParser(CommonTokenStream(CPPLexer(input)))
//    val tree = parser.translationUnit()
//    val res = tree.toStringTree(parser)
//    println(res)

    File("./src/test/antlr/output.txt").bufferedWriter().use { out ->
        val input = CharStreams.fromFileName("./src/test/antlr/input.txt")
        val parser = CPPParser(CommonTokenStream(CPPLexer(input)))
        val parsedTree = parser.translationUnit()
        val visitor = Visitor()
        visitor.visit(parsedTree)
        println("Code: <\n${visitor.getCode()}\n>")
        out.write(visitor.getCode())
    }
}