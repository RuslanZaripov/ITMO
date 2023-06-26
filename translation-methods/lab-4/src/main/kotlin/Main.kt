import gen.calculator.CalculatorLexer
import gen.calculator.CalculatorParser

fun main() {
    println(System.getProperty("user.dir"))

//    change grammar location and run tests
    val grammar = GrammarBuilder.build("./src/main/resources/Calculator.txt")

//    println(grammar)

    grammar.buildFirst()
    grammar.buildFollow()

//    println(grammar.groupRulesByName)

    val generatePath = "./src/main/kotlin"
    GenerateEnum(grammar, generatePath).generate()
    GenerateLexer(grammar, generatePath).generate()
    GenerateParser(grammar, generatePath).generate()

//    first modification - custom classes
    val ast = CalculatorParser(CalculatorLexer("(---3*(4--5)+10)/-2")).parse()
    println(ast.res)

//    second modification - pow operation
//    val ast = CalculatorParser(CalculatorLexer("2**3**2")).parse()
//    println(ast.res)
}