abstract class StateCodeGenerator() {

    abstract val ctxName: String
    abstract val actualProductionName: String
    abstract val pseudoName: String
    abstract val rule: NonTerminal

    fun generateCode(): String {
        return """
            |${initProductionTree()}
            |${fillContextClass()}
            |${formatAction()}
            |$ctxName.add(${getAstAddition()})
        """.trimMargin("|")
    }

    fun formatContextName(action: String) = action.replace("$", "$ctxName.")

    private fun formatAction(): String =
        rule.ruleCtx.action[pseudoName]
            ?.let { formatContextName(it) }
            ?.drop(1)?.dropLast(1)
            ?.trimMargin("|")
            ?: "/* no action performed on attributes for $ctxName */"

    open fun initProductionTree(): String = "/* no initialization performed for $pseudoName */"

    open fun fillContextClass(): String = "/* no context filling for $pseudoName */"

    abstract fun getAstAddition(): String
}

class NonTerminalCodeGenerator(
    state: State.NonTerminal,

    override val rule: NonTerminal,
    override val ctxName: String,
) : StateCodeGenerator() {

    override val actualProductionName = state.name
    override val pseudoName = state.alias ?: actualProductionName

    private val treeHoldingVarName = pseudoName

    override fun initProductionTree() =
        "val $treeHoldingVarName = $actualProductionName${formatCtorArgs()}"

    private fun formatCtorArgs() = rule.ruleCtx.ctorArgs[pseudoName]?.let { formatContextName(it) } ?: "()"

    override fun fillContextClass() =
        "$ctxName.$pseudoName = $treeHoldingVarName"

    override fun getAstAddition() = treeHoldingVarName
}

class TerminalCodeGenerator(
    state: State.Terminal,
    private val tokenClassName: String,

    override val rule: NonTerminal,
    override val ctxName: String,
) : StateCodeGenerator() {

    override val actualProductionName = state.name
    override val pseudoName = state.alias ?: actualProductionName

    private val treeHoldingVarName: String = "lastToken"

    override fun initProductionTree() =
        "$treeHoldingVarName = check($tokenClassName.$actualProductionName)"

    override fun getAstAddition() =
        "Leaf($treeHoldingVarName, $treeHoldingVarName.value)"

    override fun fillContextClass() =
        "$ctxName.$pseudoName = $treeHoldingVarName"
}

class EpsilonCodeGenerator(
    // TODO: remove token className
    private val tokenClassName: String,

    // TODO: is sending a rule here is a good idea?
    override val rule: NonTerminal,
    // TODO: ctx same
    override val ctxName: String,
) : StateCodeGenerator() {

    override val actualProductionName = "EPSILON"
    override val pseudoName = actualProductionName

    override fun getAstAddition() =
        "Leaf($tokenClassName.$actualProductionName, \"ε\")"
}