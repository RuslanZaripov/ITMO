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
            |    fun parse(): ${capitalize(grammar.startNonTerminal.name)}Context {
            |        val ast = ${grammar.startNonTerminal.name}()
            |        return when (curToken) {
            |            $enumName.END -> ast
            |            else -> {
            |                println("Tree: \n${"$"}ast")
            |                throw Exception("Unexpected token: ${'$'}curToken")
            |            }
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
            separator = "\n"
        ) { """
            |class ${capitalize(it.name)}Context(name: String${setAttrs(it, requireTypes = true)}) : Tree(name) {
            |${generateBody(it)}
            |}
        """.trimMargin("|").prependIndent("\t") }

    private fun generateBody(it: NonTerminal): String {
//        println(generateContext(it))
        return """
            |${generateContext(it)}
            |${it.ruleCtx.returnAttrs?.let { it.joinToString(separator = "\n") { attr -> "var ${attr.name}: ${attr.type}? = null" } } ?: return ""}
        """.trimMargin("|").prependIndent("\t")
//        it.ruleCtx.returnAttrs?.let { return it.joinToString(prefix = "{\n\t", separator = "\n\t", postfix = "\n}") { attr -> "var ${attr.name}: ${attr.type}? = null" } } ?: return ""
    }

    private fun generateContext(it: NonTerminal) = grammar.group[it.name]?.joinToString(separator = "\n") { alter ->
        (alter as NonTerminal).productions.joinToString(separator = "\n") { production ->
            if (production.isUpper()) {
                "lateinit var $production: $enumName"
            } else {
                "lateinit var $production: ${capitalize(production)}Context"
            }
        }
    } ?: ""

    private fun generateMethod(rule: NonTerminal): String {
        val ctxName = "${rule.name}LocalContext"
        return """
            |private fun ${rule.name}${generateArgs(rule)}: ${capitalize(rule.name)}Context {
            |    val $ctxName = ${capitalize(rule.name)}Context("${rule.name}"${setAttrs(rule, requireTypes = false)})
            |    var lastToken: $enumName
            |    
            |    when (curToken) {
            |${generateBranches(rule.name, ctxName)}
            |        else -> {
            |            println("Tree: \n${"$"}$ctxName")
            |            throw Exception("Unexpected token: ${'$'}curToken")
            |        }
            |    }
            |    return $ctxName
            |}
            |
        """.trimMargin("|").prependIndent("\t")
    }

    private fun setAttrs(rule: NonTerminal, requireTypes: Boolean): String {
        return rule.ruleCtx.attrs?.joinToString(prefix = ", ", separator = ", ") { if (requireTypes) { "val ${it.name}: ${it.type}" } else { it.name } } ?: ""
    }

    private fun generateArgs(rule: NonTerminal): String {
        return rule.ruleCtx.attrs?.joinToString(prefix = "(", separator = ", ", postfix = ")") { arg -> "${arg.name}: ${arg.type}" } ?: "()"
    }

    private fun generateBranches(ruleName: String, ctxName: String): String {
        return grammar.group[ruleName]!!.filterIsInstance<NonTerminal>().joinToString("\n") { alter ->
            val firstSet = grammar.getFirstSet(ruleName, alter.productions)
            val cond = firstSet.joinToString(", ") { "$enumName.$it" }
            val body = alter.productions.joinToString("\n") { prod ->
                val treeName: String
                if (prod == "EPSILON") {
                    val action = alter.ruleCtx.code[prod]
                    if (action != null) return@joinToString formatAction(action, ctxName)
                }
                if (prod.isUpper()) {
                    treeName = "Leaf(lastToken, lastToken.value)"
                    var code = "lastToken = check($enumName.$prod)"
                    code += "\n$ctxName.$prod = lastToken"
                    val action = alter.ruleCtx.code[prod]
                    if (action != null) code += "\n${formatAction(action, ctxName)}"
                    code
                } else {
                    treeName = prod
                    var code = "val $treeName = $prod${getArgs(alter, prod, ctxName)}"
                    code += "\n$ctxName.$prod = $treeName"
                    val action = alter.ruleCtx.code[prod]
                    if (action != null) code += "\n${formatAction(action, ctxName)}"

                    code
                }
                    .plus("\n${ctxName}.add($treeName)")
                    .plus("\n")

            }.prependIndent("\t")
            """
            |$cond -> {
            |$body
            |}
            """.trimMargin("|").prependIndent("\t\t")
        }
    }

    private fun getArgs(alter: NonTerminal, prod: RuleName, ctxName: String) =
        alter.ruleCtx.initCode[prod]?.replace("$", "$ctxName.") ?: "()"

    private fun formatAction(action: String, ctxName: String): String {
        return action.replace("$", "$ctxName.").drop(1).dropLast(1)
    }

    private fun capitalize(name: String): String {
        return name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    private fun String.isUpper(): Boolean {
        return this.all { it.isUpperCase() }
    }
}