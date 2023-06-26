import java.io.File
import java.util.*

class GenerateParser(val grammar: Grammar, pathToDir: String) {
    private val prefixPackageName = "gen.${grammar.name?.lowercase(Locale.getDefault())}"
    private val prefixPath = prefixPackageName.replace(".", "/")

    private val enumName = "${grammar.name}Token"
    private val parserName = "${grammar.name}Parser"
    private val lexerName = "${grammar.name}Lexer"

    private val path = "$pathToDir/$prefixPath/$parserName.kt"

    fun generate() {
        val sourceCode = """
            |package $prefixPackageName
            |
            |import kotlin.properties.Delegates
            |import kotlin.math.pow
            |
            |class Visualizer {
            |    private var index = 0
            |
            |    fun visualize(tree: Tree): String {
            |        val sb = StringBuilder("digraph G {\n")
            |        traverse(tree, -1, 0, sb)
            |        sb.append("}")
            |        return sb.toString()
            |    }
            |
            |    private fun traverse(tree: Tree, parentId: Int, currentId: Int, sb: StringBuilder) {
            |        sb.append("\t${'$'}currentId [${'$'}{formatNode(tree)}]\n")
            |        if (parentId != -1) {
            |            sb.append("\t${'$'}parentId -> ${'$'}currentId\n")
            |        }
            |        if (tree.children.isNotEmpty()) {
            |            tree.children.forEach { child ->
            |                index += 1
            |                traverse(child, currentId, index, sb)
            |            }
            |        } else {
            |            index += 1
            |        }
            |    }
            |    
            |    private fun formatNode(tree: Tree): String {
            |        return when(tree) {
            |            is Leaf -> "label=\"${'$'}{tree.value}\", style=filled, color=green"
            |            else -> "label=\"${'$'}{tree.name}\", color=brown"
            |        }
            |    }
            |}
            |
            |open class Tree(open val name: String, val children: MutableList<Tree> = mutableListOf()) {
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
            |                next.print(builder, "${"$"}childrenPrefix|-- ", "${"$"}childrenPrefix|   ")
            |            } else {
            |                next.print(builder, "${"$"}childrenPrefix|__ ", "${"$"}childrenPrefix    ")
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
            |${addCode(grammar.typeAliasMap)}
            |
        """.trimMargin("|")
        File(path).parentFile.mkdirs()
        File(path).writeText(sourceCode)
    }

    private fun addCode(typeAliasMap: MutableMap<String, AliasContext>): String {
        return typeAliasMap.toList().joinToString(separator = "\n") { ctx -> ctx.second.code.drop(1).dropLast(1) }
            .trimMargin("|")
    }

    private fun generateMethods() = grammar.allNonTerminalNames.joinToString(separator = "\n") { generateMethod(it) }

    private fun generateContexts() = grammar.allNonTerminalNames.joinToString(
        separator = "\n\n"
    ) {
        """
            |class ${capitalize(it.name)}Context(name: String${setAttrs(it, requireTypes = true)}) : Tree(name) {
            |${generateBody(it)}
            |}
        """.trimMargin("|").prependIndent("\t")
    }

    private fun generateBody(rule: NonTerminal): String {
        return """
            |${generateContext(rule)}
            |${generateReturnAttrs(rule)}
        """.trimMargin("|").prependIndent("\t")
    }

    private fun generateReturnAttrs(rule: NonTerminal) =
        rule.ruleCtx.returnAttrs
            ?.let { it.joinToString(separator = "\n") { attr -> formatAlias("var ${attr.name} by Delegates.notNull<${attr.type}>()") } }
            ?: "/* no return attrs */"


    private fun generateContext(rule: NonTerminal) =
        grammar.groupRulesByName[rule.name]?.joinToString(separator = "\n") { alter ->
            (alter as NonTerminal).productions
                .joinToString(separator = "\n") { state ->
                    when (state) {
                        is State.Terminal -> state.alias?.let {
                            "lateinit var ${state.alias}: $enumName"
                        } ?: "var ${state.name}: $enumName? = null"

                        is State.NonTerminal ->
                            "lateinit var ${state.alias ?: state.name}: ${capitalize(state.name)}Context"

                        State.EPSILON -> "/* context ${rule.name} can be epsilon */"

                        else -> throw Exception("Unspecified state $state found while generating context class for ${rule.name}")
                    }
                }
        } ?: "/* no need in context class for ${rule.name} */"

    private fun generateMethod(rule: NonTerminal): String {
        val ruleName = rule.name
        val localContextName = "${ruleName}LocalContext"

        return """
            |private fun $ruleName${generateArgs(rule)}: ${capitalize(ruleName)}Context {
            |    val $localContextName = ${capitalize(ruleName)}Context("$ruleName"${setAttrs(rule, requireTypes = false)})
            |    var lastToken: $enumName
            |    
            |    when (curToken) {
            |${generateBranches(ruleName, localContextName).plus("\n")}
            |        else -> {
            |            println("Tree: \n${"$"}$localContextName")
            |            throw Exception("Unexpected token: ${'$'}curToken")
            |        }
            |    }
            |    return $localContextName
            |}
            |
        """.trimMargin("|").prependIndent("\t")
    }

    private fun setAttrs(rule: NonTerminal, requireTypes: Boolean): String {
        return rule.ruleCtx.attrs?.joinToString(prefix = ", ", separator = ", ") {
            if (requireTypes) {
                formatAlias("val ${it.name}: ${it.type}")
            } else {
                it.name
            }
        } ?: " /* no input attrs */"
    }

    private fun formatAlias(s: String): String {
        var result = s
        grammar.typeAliasMap.forEach { ctx -> result = result.replace(ctx.key, ctx.value.type) }
        return result
    }

    private fun generateArgs(rule: NonTerminal): String {
        return rule.ruleCtx.attrs?.joinToString(
            prefix = "(",
            separator = ", ",
            postfix = ")"
        ) { arg -> formatAlias("${arg.name}: ${arg.type}") }
            ?: "()"
    }

    private fun generateBranches(ruleName: String, ctxName: String): String {
        return grammar.groupRulesByName[ruleName]!!.filterIsInstance<NonTerminal>().joinToString("\n\n") { alter ->

            val firstSet = grammar.getFirstSet(ruleName, alter.productions)

            val cond = firstSet.joinToString(", ") { state -> "$enumName.${tokenName(state, ruleName)}" }

            val body = alter.productions.joinToString("\n\n") { state ->
                when (state) {
                    is State.Terminal -> TerminalCodeGenerator(state, enumName, alter, ctxName).generateCode()
                    is State.NonTerminal -> NonTerminalCodeGenerator(state, alter, ctxName).generateCode()
                    State.EPSILON -> EpsilonCodeGenerator(enumName, alter, ctxName).generateCode()
                    else -> throw Exception("Unspecified state $state found while generating branch for $ruleName")
                }
            }.prependIndent("\t")

            """
            |$cond -> {
            |$body
            |}
            """.trimMargin("|").prependIndent("\t\t")
        }
    }

    private fun tokenName(it: State, ruleName: String) = when (it) {
        is State.EOF -> "END"
        is State.NonTerminal -> it.name
        is State.Terminal -> it.name
        else -> throw Exception("first set of $ruleName contains unspecified state")
    }

    private fun getArgs(initializationCode: String?, ctxName: String) =
        initializationCode?.let { addContextName(it, ctxName) } ?: "()"

    private fun formatAction(action: String?, ctxName: String): String =
        action?.let { addContextName(action, ctxName) }?.drop(1)?.dropLast(1)?.trimMargin("|")
            ?: "/* no action performed on attributes for $ctxName */"

    private fun addContextName(action: String, ctxName: String) = action.replace("$", "$ctxName.")

    private fun capitalize(name: String): String {
        return name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    private fun String.isUpper(): Boolean {
        return this.all { it.isUpperCase() }
    }
}