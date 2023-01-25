fun main() {
    println(System.getProperty("user.dir"))

    val grammar = GrammarBuilder.build("./src/main/resources/Calculator.txt")
    println(grammar)

    grammar.buildFirst()
    grammar.buildFollow()

    println(grammar.groupRulesByName)

    val generatePath = "./src/main/kotlin"
    GenerateEnum(grammar, generatePath).generate()
    GenerateLexer(grammar, generatePath).generate()
    GenerateParser(grammar, generatePath).generate()
}