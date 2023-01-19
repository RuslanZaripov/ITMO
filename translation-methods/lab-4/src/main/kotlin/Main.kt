fun main(args: Array<String>) {
    println(System.getProperty("user.dir"))

    val grammar = GrammarBuilder.build("./src/main/resources/Test.txt")
    println(grammar)

    grammar.buildFirst()
    grammar.buildFollow()
}