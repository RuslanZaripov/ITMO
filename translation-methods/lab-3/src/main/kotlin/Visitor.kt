import CPPParser.*
import org.antlr.runtime.Token
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.TerminalNode

class Visitor : CPPBaseVisitor<Unit>() {
    private val builder = StringBuilder()
    private var indent = 0

    companion object {
        private const val FOR = "for"
        private const val WHILE = "while"
        private const val SPACE = " "
        private const val TAB = "    "
        private const val ELSE = "else"
        private const val IF = "if"
        private const val RETURN = "return"
        private const val SEMICOLON = ";"
        private const val LEFT_PAREN = "("
        private const val RIGHT_PAREN = ")"
        private const val LEFT_BRACE = "{"
        private const val RIGHT_BRACE = "}"
        private val operations = setOf(
            AndAnd,
            OrOr,
            Plus,
            Minus,
            Mul,
            Div,
            Mod,
            And,
            Or,
            Caret,
        )
        private val assignmentOperations = setOf(
            Assign,
            PlusAssign,
            MinusAssign,
            MulAssign,
            DivAssign,
            ModAssign,
            AndAssign,
            OrAssign,
            XorAssign,
        )
        private val comparisonOperators = setOf(
            Equal,
            NotEqual,
            Less,
            Greater,
            LessEqual,
            GreaterEqual,
        )
    }

    fun getCode(): String {
        return builder.toString()
    }

    private var shiftOperatorPartFound = false;

    override fun visitTerminal(node: TerminalNode) {
        if (node.symbol.type != Token.EOF) {
            when (node.parent) {
                is UnaryOperatorContext -> {
                    builder.append(node.text)
                    return
                }

                is ShiftOperatorContext -> {
                    shiftOperatorPartFound = if (shiftOperatorPartFound) {
                        builder.append(node.text)
                        builder.append(SPACE)
                        false
                    } else {
                        builder.append(SPACE)
                        builder.append(node.text)
                        true
                    }
                    return
                }

                is PtrOperatorContext -> {
                    builder.append(node.text)
                    builder.append(SPACE)
                    return
                }

                else -> {
                    when {
                        isOperator(node) -> builder.append(SPACE)
                    }
                    builder.append(node.text)
                    when {
                        isOperator(node) -> builder.append(SPACE)
                        isComma(node) -> builder.append(SPACE)
                    }
                }
            }
        }
    }

    private fun isOperator(node: TerminalNode) =
        (operations.contains(node.symbol.type)
                || assignmentOperations.contains(node.symbol.type)
                || comparisonOperators.contains(node.symbol.type))

    private fun isComma(node: TerminalNode) = node.symbol.type == Comma

    private fun isDoubleColon(node: TerminalNode) = node.symbol.type == DoubleColon

    override fun visitFunctionDefinition(ctx: FunctionDefinitionContext) {
        visit(ctx.declSpecifierSeq())
        builder.append(SPACE)
        visit(ctx.declarator())
        builder.append(SPACE)
        visit(ctx.compoundStatement())
    }

    override fun visitCompoundStatement(ctx: CompoundStatementContext) {
        builder.append(LEFT_BRACE).appendLine()
        indent++
        visit(ctx.statementseq())
        indent--
        builder.append(TAB.repeat(indent)).append(RIGHT_BRACE).appendLine()
    }

    override fun visitSimpleDeclaration(ctx: SimpleDeclarationContext) {
        if (ctx.declSpecifierSeq() != null) {
            visit(ctx.declSpecifierSeq())
            builder.append(SPACE)
        }
        visit(ctx.initDeclaratorList())
        builder.append(SEMICOLON).appendLine()
    }

    override fun visitStatement(ctx: StatementContext) {
        if ((ctx.parent !is SelectionStatementContext
                    && ctx.parent !is IterationStatementContext)
            || ctx.getChild(0) !is CompoundStatementContext
        ) {
            builder.append(TAB.repeat(indent))
        }
        visitChildren(ctx)
    }

    override fun visitReturnExpression(ctx: ReturnExpressionContext) {
        builder.append(RETURN).append(SPACE)
        visit(ctx.expression())
    }

    override fun visitJumpStatement(ctx: JumpStatementContext) {
        visit(ctx.getChild(0))
        builder.append(SEMICOLON).appendLine()
    }

    override fun visitExpressionStatement(ctx: ExpressionStatementContext) {
        appendIfExist(ctx.expression())
        builder.append(SEMICOLON).appendLine()
    }

    override fun visitIterationStatement(ctx: IterationStatementContext) {
        when (ctx.getStart().type) {
            For -> {
                builder.append(FOR).append(SPACE).append(LEFT_PAREN)
                visit(ctx.forInitStmt())
                builder.deleteCharAt(builder.length - 1)
                appendIfExist(ctx.condition(), before = SPACE)
                builder.append(SEMICOLON)
                appendIfExist(ctx.expression(), before = SPACE)
                builder.append(RIGHT_PAREN)
                formatBranch(ctx.statement())
            }

            While -> {
                builder.append(WHILE).append(SPACE).append(LEFT_PAREN)
                visit(ctx.condition())
                builder.append(RIGHT_PAREN)
                formatBranch(ctx.statement())
            }
        }
    }

    private fun appendIfExist(
        ctx: ParserRuleContext?,
        before: String? = null,
        after: String? = null
    ) {
        if (ctx != null) {
            before?.let { builder.append(it) }
            visit(ctx)
            after?.let { builder.append(it) }
        }
    }

    override fun visitSelectionStatement(ctx: SelectionStatementContext) {
        builder.append(IF).append(SPACE).append(LEFT_PAREN)
        visit(ctx.condition())
        builder.append(RIGHT_PAREN)
        formatBranch(ctx.statement(0))
        if (ctx.statement().size > 1) {
            formatElseBranch(ctx.statement(1))
        }
    }

    private fun formatElseBranch(ctx: StatementContext) {
        if (!isCompoundStatement(ctx)) {
            builder
                .append(TAB.repeat(indent))
                .append(ELSE)
                .append(SPACE)
                .appendLine()
            indent++
        } else {
            builder
                .deleteCharAt(builder.length - 1)
                .append(SPACE)
                .append(ELSE)
                .append(SPACE)
        }
        visit(ctx)
        if (!isCompoundStatement(ctx)) {
            indent--
        }
    }

    private fun formatBranch(ctx: StatementContext) {
        if (!isCompoundStatement(ctx)) {
            builder.appendLine()
            indent++
        } else {
            builder.append(SPACE)
        }
        visit(ctx)
        if (!isCompoundStatement(ctx)) {
            indent--
        }
    }

    private fun isCompoundStatement(ctx: StatementContext) = ctx.compoundStatement() != null

    override fun visitDeclSpecifierSeq(ctx: DeclSpecifierSeqContext) {
        val amount = ctx.childCount
        for (index in 0 until amount) {
            visit(ctx.getChild(index))
            if (index != amount - 1) {
                builder.append(SPACE)
            }
        }
    }
}