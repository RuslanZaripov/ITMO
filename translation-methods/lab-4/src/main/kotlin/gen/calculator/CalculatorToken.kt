package gen.calculator

import java.util.regex.Pattern

enum class CalculatorToken(val regex: String, val shouldBeSkipped: Boolean = false) { 
    PLUS("\\+"),
	MINUS("-"),
	TIMES("\\*"),
	DIVIDE("/"),
	LPAREN("\\("),
	RPAREN("\\)"),
	NUM("[0-9]+"),
	WS("[ \\n\\t\\r]+", true),
    END("#");
    
    lateinit var value: String
    val pattern: Pattern = Pattern.compile(regex)
}
