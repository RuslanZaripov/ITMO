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
sealed class Rule {
    abstract val name: RuleName
}

object EPSILON : Rule() {
    override val name: RuleName = "EPS"
}

data class Terminal(override val name: RuleName, val regex: String) : Rule() {
    override fun toString(): String {
        return "$name -> $regex"
    }
}

data class NonTerminal(override val name: RuleName, val productions: List<RuleName>) : Rule() {
    override fun toString(): String {
        return "$name -> ${productions.joinToString(" | ")}"
    }
}


sealed class State {
    object Epsilon : State()
    data class Names(val prods: HashSet<RuleName>) : State()
}

data class Grammar(val rules: List<Rule>) {
    private val first = mutableMapOf<RuleName, HashSet<RuleName>>()
    private val follow = mutableMapOf<RuleName, HashSet<RuleName>>()

    private val nonTerminals = rules.filterIsInstance<NonTerminal>()

    private val rulesMap = rules.associateBy { it.name }
    private fun getRuleByName(name: RuleName): Rule {
        return rulesMap[name] ?: throw IllegalArgumentException("Rule with name $name not found")
    }

    private fun computeFirstSet(productions: List<RuleName>): HashSet<RuleName> {
        if (productions.isEmpty()) {
            return hashSetOf(EPSILON.name)
        }

        val firstRule = getRuleByName(productions[0])
        if (firstRule is Terminal) {
            return hashSetOf(firstRule.name)
        }

        val firstTmp = first[firstRule.name]!!
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

        follow[nonTerminals[0].name]!!.add("$")

        var changed = true
        while (changed) {
            changed = false
            nonTerminals.forEach { nonTerminal ->
                val prods = nonTerminal.productions
                prods.forEachIndexed { index, prod ->
                    if (getRuleByName(prod) is NonTerminal) {
                        val followTmp = computeFirstSet(prods.subList(index + 1, prods.size))
                        if (followTmp.remove(EPSILON.name)) {
                            followTmp.addAll(computeFirstSet(prods.subList(1, prods.size)))
                        }

                        val followSet = follow[prod]!!
                        if (followSet.addAll(followTmp)) {
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
        return Grammar(listener.rules)
    }

    private class Listener : GrammarBaseListener() {
        val rules = mutableListOf<Rule>()

        override fun exitTerminalRule(ctx: GrammarParser.TerminalRuleContext) {
            rules.add(Terminal(ctx.TOKEN_NAME().text, ctx.REGEX().text))
        }

        override fun exitNonTerminalRule(ctx: GrammarParser.NonTerminalRuleContext) {
            ctx.alternatives().alternative().forEach { alter ->
                val productions = alter.production().map { it.text }
                rules.add(NonTerminal(ctx.RULE_NAME().text, productions))
            }
        }
    }
}
