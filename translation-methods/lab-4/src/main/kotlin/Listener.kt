import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker

typealias RuleName = String
typealias Alias = String
typealias RuleNameOrAlias = String
typealias Type = String

sealed class Rule {
    abstract val name: RuleName
}

data class Terminal(override val name: RuleName, val regex: String, val shouldBeSkipped: Boolean) : Rule() {
    override fun toString(): String {
        return "$name: $regex ${if (shouldBeSkipped) "-> skip" else ""}"
    }
}

data class NonTerminal(
    override val name: RuleName,
    val productions: List<State>,
    val ruleCtx: RuleContext,
) : Rule() {
    override fun toString(): String {
        return "$name: ${productions.joinToString(" ")}"
    }
}

data class RuleContext(
    var attrs: List<Attribute>? = null,
    var returnAttrs: List<Attribute>? = null,
    var ctorArgs: MutableMap<RuleNameOrAlias, String> = mutableMapOf(),
    var action: MutableMap<RuleNameOrAlias, String> = mutableMapOf(),
)

data class Attribute(val name: String, val type: Type)

data class Grammar(val name: String?, val rules: List<Rule>, val typeAliasMap: MutableMap<String, AliasContext>) {
    private val first = mutableMapOf<RuleName, HashSet<State>>()
    private val follow = mutableMapOf<RuleName, HashSet<State>>()

    val terminals = rules.filterIsInstance<Terminal>()
    private val nonTerminals = rules.filterIsInstance<NonTerminal>()
    val allNonTerminalNames = nonTerminals.distinctBy { it.name }

    val startNonTerminal = nonTerminals.first()
    val groupRulesByName = rules.groupBy { it.name }

    fun getFirstSet(nonTerminalName: String, productions: List<State>): HashSet<State> {
        val firstSet = computeFirstSet(productions)
        if (firstSet.remove(State.EPSILON)) {
            firstSet.addAll(follow[nonTerminalName]!!)
        }
        return firstSet
    }

    private fun computeFirstSet(productions: List<State>): HashSet<State> {
        if (productions.isEmpty()) {
            return hashSetOf(State.EPSILON)
        }

        val firstProduction = productions.first()
        if (firstProduction is State.EPSILON) {
            return hashSetOf(State.EPSILON)
        }

        if (firstProduction is State.Terminal) {
            return hashSetOf(firstProduction)
        }

        val firstTmp = HashSet(first[(firstProduction as State.NonTerminal).name]!!)
        if (firstTmp.remove(State.EPSILON)) {
            firstTmp.addAll(computeFirstSet(productions.subList(1, productions.size)))
        }
        return firstTmp
    }

    fun buildFirst() {
        nonTerminals.forEach { first.putIfAbsent(it.name, hashSetOf()) }

        var changed = true
        while (changed) {
            changed = false
            nonTerminals.forEach { nonTerminal ->
                val firstSet = first[nonTerminal.name]!!
                val productions = nonTerminal.productions
                if (firstSet.addAll(computeFirstSet(productions))) {
                    changed = true
                }
            }
        }

//        println("\nFIRST:")
//        first.forEach { (ruleName, firstSet) ->
//            print("$ruleName -> $firstSet\n")
//        }
    }

    fun buildFollow() {
        nonTerminals.forEach { follow.putIfAbsent(it.name, hashSetOf()) }

        follow[startNonTerminal.name]!!.add(State.EOF)

        var changed = true
        while (changed) {
            changed = false
            nonTerminals.forEach { nonTerminal ->
                val productions = nonTerminal.productions

                productions.forEachIndexed { index, production ->
                    if (production is State.NonTerminal) {
                        val followTmp = computeFirstSet(productions.subList(index + 1, productions.size))
                        if (followTmp.remove(State.EPSILON)) {
                            followTmp.addAll(follow[nonTerminal.name]!!)
                        }
                        if (follow[production.name]!!.addAll(followTmp)) {
                            changed = true
                        }
                    }
                }
            }
        }

//        println("\nFOLLOW:")
//        follow.forEach { (ruleName, followSet) ->
//            print("$ruleName -> $followSet\n")
//        }
    }

    override fun toString(): String {
        return rules.joinToString(separator = "\n") { it.toString() }
    }
}

sealed class State {
    data class Terminal(val name: RuleName, val alias: Alias?) : State()
    data class NonTerminal(val name: RuleName, val alias: Alias?) : State()
    object EPSILON : State()
    object EOF : State()
}

data class AliasContext(val type: String, val code: String)

object GrammarBuilder {
    fun build(grammarPath: String): Grammar {
        val input = CharStreams.fromFileName(grammarPath)
        val parser = GrammarParser(CommonTokenStream(GrammarLexer(input)))
        val parsedTree = parser.inputGrammar()
        val listener = Listener()
        val walker = ParseTreeWalker()
        walker.walk(listener, parsedTree)
        return Grammar(
            listener.grammarName,
            listener.rules,
            listener.typeAliasMap
        )
    }

    private class Listener : GrammarBaseListener() {
        var grammarName: String? = null
        val rules = mutableListOf<Rule>()
        val typeAliasMap = mutableMapOf<String, AliasContext>()

        override fun exitTerminalRule(ctx: GrammarParser.TerminalRuleContext) {
            val shouldBeSkipped = ctx.SKIP_MODIFIER() != null
            rules.add(Terminal(ctx.TOKEN_NAME().text, ctx.REGEX().text, shouldBeSkipped))
        }

        override fun exitNonTerminalRule(ctx: GrammarParser.NonTerminalRuleContext) {
            val ruleCtx = RuleContext()
            ruleCtx.returnAttrs = ctx.returnList()?.attributeList()?.attribute()?.map {
                Attribute(it.RULE_NAME().text, it.TOKEN_NAME().text)
            }
            ruleCtx.attrs = ctx.attributeList()?.attribute()?.map {
                Attribute(it.RULE_NAME().text, it.TOKEN_NAME().text)
            }
            ctx.alternatives().alternative().forEach { alter ->
                val productions = mutableListOf<State>()
                for (productionCtx in alter.production()) {
                    val ruleName = productionCtx.getChild(0).text
                    val ruleAlias = productionCtx.ALIAS()?.text?.drop(1)

                    if (ruleName == "EPSILON") {
                        productions.add(State.EPSILON)
                    } else if (ruleName.isUpper()) {
                        productions.add(State.Terminal(ruleName, ruleAlias))
                    } else {
                        productions.add(State.NonTerminal(ruleName, ruleAlias))
                    }

                    val associateName = ruleAlias ?: ruleName

                    productionCtx.ARGS()?.let { args ->
                        ruleCtx.ctorArgs[associateName] = args.text
                    }
                    productionCtx.CODE()?.let { code ->
                        ruleCtx.action[associateName] = code.text
                    }
                }

//                println(ctx.RULE_NAME().text + " " + productions + " " + ruleCtx)

                rules.add(NonTerminal(ctx.RULE_NAME().text, productions, ruleCtx))
            }
        }

        override fun exitGrammarName(ctx: GrammarParser.GrammarNameContext) {
            grammarName = ctx.TOKEN_NAME().text
        }

        override fun exitTypealias(ctx: GrammarParser.TypealiasContext) {
            typeAliasMap[ctx.name.text] = AliasContext(ctx.type.text, ctx.code.text)
        }

        private fun String.isUpper(): Boolean {
            return this.all { it.isUpperCase() }
        }
    }
}
