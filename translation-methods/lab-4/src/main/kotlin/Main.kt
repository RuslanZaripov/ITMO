import gen.test.*

fun main() {
//    println(System.getProperty("user.dir"))
//
//    val grammar = GrammarBuilder.build("./src/main/resources/Test.txt")
//    println(grammar)
//
//    grammar.buildFirst()
//    grammar.buildFollow()
//
//    println(grammar.group)
//
//    val generatePath = "./src/main/kotlin"
//    val generator = GenerateEnum(grammar, generatePath)
//    generator.generate()
//    val generator2 = GenerateLexer(grammar, generatePath)
//    generator2.generate()
//    val generator3 = GenerateParser(grammar, generatePath)
//    generator3.generate()
    val lexer = TestLexer("adis  adis          joker joker")
    val parser = TestParser(lexer)
    val ast = parser.parse()
    println(ast)
}