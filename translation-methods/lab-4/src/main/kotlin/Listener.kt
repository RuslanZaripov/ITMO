import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker

typealias RuleName = String

//abstract class Rule(open val name: RuleName)
//
//data class Terminal(override val name: RuleName, val regex: String) : Rule(name)
//
//data class NonTerminal(override val name: RuleName, val productions: List<RuleName>) : Rule(name)

// sealed class Rule with subclassed Terminal and NonTerminal
// EPSILON, EOF are terminal rules

// 1.
// sealed class State()
// class RuleName(val name: String) : State()
// object EPSILON : State()
// object EOF : State()

// 2.
// EPSILON : Terminal("EPSILON", "")

sealed class Rule {
    abstract val name: RuleName
}

object EPSILON : Rule() {
    override val name: RuleName = "EPSILON"
}

data class Terminal(override val name: RuleName, val regex: String, val shouldBeSkipped: Boolean) : Rule() {
    override fun toString(): String {
        return "$name: $regex ${if (shouldBeSkipped) "-> skip" else ""}"
    }
}

data class NonTerminal(
    override val name: RuleName,
    val productions: List<RuleName>,
    val ruleCtx: RuleContext
) : Rule() {
    override fun toString(): String {
        return "$name: ${productions.joinToString(" ")}"
    }
}

data class RuleContext(
    var attrs: List<Attribute>? = null,
    var returnAttrs: List<Attribute>? = null,
    var initCode: MutableMap<RuleName, String> = mutableMapOf(),
    var code: MutableMap<RuleName, String> = mutableMapOf(),
)

data class Attribute(val name: String, val type: String)

// TODO: make sealed class State with subclassed RuleName and Epsilon
data class Grammar(val name: String?, val rules: List<Rule>) {
    val first = mutableMapOf<RuleName, HashSet<RuleName>>()
    val follow = mutableMapOf<RuleName, HashSet<RuleName>>()

    val terminals = rules.filterIsInstance<Terminal>()
    val nonTerminals = rules.filterIsInstance<NonTerminal>()
    val startNonTerminal = nonTerminals.first()

    val group = rules.groupBy { it.name }

    private val rulesMap = rules.associateBy { it.name }
    fun getRuleByName(name: RuleName): Rule {
        return rulesMap[name] ?: throw IllegalArgumentException("Rule with name $name not found")
    }

    fun getFirstSet(nonTerminalName: String, productions: List<RuleName>): HashSet<RuleName> {
        val firstSet = computeFirstSet(productions)
        if (firstSet.remove(EPSILON.name)) {
            firstSet.addAll(follow[nonTerminalName]!!)
        }
        return firstSet
    }

    private fun computeFirstSet(productions: List<RuleName>): HashSet<RuleName> {
        if (productions.isEmpty()) {
            return hashSetOf(EPSILON.name)
        }

        val firstRule = getRuleByName(productions[0])
        if (firstRule is Terminal) {
            return hashSetOf(firstRule.name)
        }

        val firstTmp = HashSet(first[firstRule.name]!!)
        if (firstTmp.remove(EPSILON.name)) {
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
                if (firstSet.addAll(computeFirstSet(nonTerminal.productions))) {
                    changed = true
                }
            }
        }

        println("\nFIRST:")
        first.forEach { (ruleName, firstSet) ->
            print("$ruleName -> $firstSet\n")
        }
    }

    fun buildFollow() {
        nonTerminals.forEach { follow.putIfAbsent(it.name, hashSetOf()) }

        follow[startNonTerminal.name]!!.add("END")

        var changed = true
        while (changed) {
            changed = false
            nonTerminals.forEach { nonTerminal ->
                val prods = nonTerminal.productions

                prods.forEachIndexed { index, prod ->
                    if (getRuleByName(prod) is NonTerminal) {
                        val followTmp = computeFirstSet(prods.subList(index + 1, prods.size))
                        if (followTmp.remove(EPSILON.name)) {
                            followTmp.addAll(follow[nonTerminal.name]!!)
                        }
                        if (follow[prod]!!.addAll(followTmp)) {
                            changed = true
                        }
                    }
                }
            }
        }

        println("\nFOLLOW:")
        follow.forEach { (ruleName, followSet) ->
            print("$ruleName -> $followSet\n")
        }
    }

    override fun toString(): String {
        return rules.joinToString(separator = "\n") { it.toString() }
    }
}

object GrammarBuilder {
    fun build(grammarPath: String): Grammar {
        val input = CharStreams.fromFileName(grammarPath)
        val parser = GrammarParser(CommonTokenStream(GrammarLexer(input)))
        val parsedTree = parser.inputGrammar()
        val listener = Listener()
        val walker = ParseTreeWalker()
        walker.walk(listener, parsedTree)
        return Grammar(listener.grammarName, listener.rules)
    }

    private class Listener : GrammarBaseListener() {
        var grammarName: String? = null
        val rules = mutableListOf<Rule>()

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
                val productions = alter.production().map { it.getChild(0).text }
                for (productionContext in alter.production()) {
                    if (productionContext.ARGS() != null) {
                        ruleCtx.initCode[productionContext.getChild(0).text] = productionContext.ARGS().text
                    }
                    if (productionContext.CODE() != null) {
                        ruleCtx.code[productionContext.getChild(0).text] = productionContext.CODE().text
                    }
                }
                println(ctx.RULE_NAME().text + " " + productions + " " + ruleCtx)
                rules.add(NonTerminal(ctx.RULE_NAME().text, productions, ruleCtx))
            }
        }

        override fun exitGrammarName(ctx: GrammarParser.GrammarNameContext) {
            grammarName = ctx.TOKEN_NAME().text
        }
    }
}
