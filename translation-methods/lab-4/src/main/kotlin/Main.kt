import gen.calculator.*

fun main() {
    println(System.getProperty("user.dir"))

    val grammar = GrammarBuilder.build("./src/main/resources/Calculator.txt")
    println(grammar)

    grammar.buildFirst()
    grammar.buildFollow()

    println(grammar.group)

    val generatePath = "./src/main/kotlin"
    GenerateEnum(grammar, generatePath).generate()
    GenerateLexer(grammar, generatePath).generate()
    GenerateParser(grammar, generatePath).generate()
//    val lexer = CalculatorLexer("(---3  *(4--5) + 10 ) /  -  2")
//    val parser = CalculatorParser(lexer)
//    val ast = parser.parse()
//    println(ast.res)
}