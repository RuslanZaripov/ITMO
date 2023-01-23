import java.io.File
import java.util.*

class GenerateParser(val grammar: Grammar, private val path: String) {
    private val prefixPackageName = "gen.${grammar.name?.lowercase(Locale.getDefault())}"
    private val prefixPath = prefixPackageName.replace(".", "/")

    private val enumName = "${grammar.name}Token"
    private val parserName = "${grammar.name}Parser"
    private val lexerName = "${grammar.name}Lexer"

    fun generate() {
        val path = "$path/$prefixPath/$parserName.kt"

        val sourceCode = """
            |package $prefixPackageName
            |
            |open class Tree(open val name: String, private val children: MutableList<Tree> = mutableListOf()) {
            |    fun add(tree: Tree) {
            |        children.add(tree)
            |    }
            |    
            |    override fun toString(): String {
            |        val builder = StringBuilder()
            |        print(builder, "", "")
            |        return builder.toString()
            |    }  
            |
            |    private fun print(builder: StringBuilder, prefix: String, childrenPrefix: String) {
            |        builder.append(prefix)
            |        builder.append(name)
            |        builder.append("\n")
            |        val it = children.iterator()
            |        while (it.hasNext()) {
            |            val next = it.next()
            |            if (it.hasNext()) {
            |                next.print(builder, "${"$"}childrenPrefix├── ", "${"$"}childrenPrefix│   ")
            |            } else {
            |                next.print(builder, "${"$"}childrenPrefix└── ", "${"$"}childrenPrefix    ")
            |            }
            |        }
            |    }
            |}
            |
            |open class Leaf(val token: $enumName, val value: String) : Tree(token.name)
            |
            |class $parserName(private val lexer: $lexerName) {
            |    private var curToken: $enumName = lexer.nextToken()
            |   
            |    private fun next() {
            |        curToken = lexer.nextToken()
            |    }
            |   
            |    fun parse(): Tree {
            |        val ast = ${grammar.startNonTerminal.name}()
            |        return when (curToken) {
            |            $enumName.END -> ast
            |            else -> throw Exception("Unexpected token: ${'$'}curToken")
            |        }
            |    }
            |    
            |    private fun check(token: $enumName): $enumName {
            |        val last = curToken
            |        if (last != token) {
            |            throw Exception("Unexpected token: ${'$'}curToken")
            |        }
            |        next()
            |        return last
            |    }
            |   
            |${generateContexts()}
            |    
            |${generateMethods()}
            |}
            |
        """.trimMargin("|")

        // create path if not exists and write source code to file
        File(path).parentFile.mkdirs()
        File(path).writeText(sourceCode)
    }

    private fun generateMethods() =
        grammar.nonTerminals.distinctBy { it.name }.joinToString(separator = "\n") { generateMethod(it) }

    private fun generateContexts() =
        grammar.nonTerminals.distinctBy { it.name }.joinToString(
            prefix = "\t",
            separator = "\n\t"
        ) { "class ${capitalize(it.name)}Context(name: String) : Tree(name)" }

    private fun generateMethod(rule: NonTerminal): String {
        val ctxName = "${rule.name}LocalContext"
        return """
            |private fun ${rule.name}(): Tree {
            |    val $ctxName = ${capitalize(rule.name)}Context("${rule.name}")
            |    var lastToken: $enumName
            |    
            |    when (curToken) {
            |${generateBranches(rule.name, ctxName)}
            |        else -> throw Exception("Unexpected token: ${'$'}curToken")
            |    }
            |    return $ctxName
            |}
            |
        """.trimMargin("|").prependIndent("\t")
    }

    private fun generateBranches(ruleName: String, ctxName: String): String {
        return grammar.group[ruleName]!!.filterIsInstance<NonTerminal>().joinToString("\n") { alter ->
            val firstSet = grammar.getFirstSet(ruleName, alter.productions)
            val cond = firstSet.joinToString(", ") { "$enumName.$it" }
            val body = alter.productions.joinToString("\n") { prod ->
                val treeName: String
                if (prod.isUpper()) {
                    treeName = "Leaf(lastToken, lastToken.value!!)"
                    "lastToken = check($enumName.$prod)"
                } else {
                    treeName = "${prod}Context"
                    "val $treeName = $prod()"
                }.plus("\n${ctxName}.add($treeName)")
            }.prependIndent("\t")
            """
            |$cond -> {
            |$body
            |}
            """.trimMargin("|").prependIndent("\t\t")
        }
    }

    private fun capitalize(name: String): String {
        return name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    private fun String.isUpper(): Boolean {
        return this.all { it.isUpperCase() }
    }
}