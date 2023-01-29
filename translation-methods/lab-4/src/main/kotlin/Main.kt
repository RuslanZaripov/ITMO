import gen.calculator.CalculatorLexer
import gen.calculator.CalculatorParser

fun main() {
    println(System.getProperty("user.dir"))

    val grammar = GrammarBuilder.build("./src/main/resources/Calculator.txt")

//    println(grammar)

    grammar.buildFirst()
    grammar.buildFollow()

//    println(grammar.groupRulesByName)

    val generatePath = "./src/main/kotlin"
    GenerateEnum(grammar, generatePath).generate()
    GenerateLexer(grammar, generatePath).generate()
    GenerateParser(grammar, generatePath).generate()
    val ast = CalculatorParser(CalculatorLexer("  (  10 / 3) - 10 -2*( 3 * 4-5)  ")).parse()
    println(ast.res)
}