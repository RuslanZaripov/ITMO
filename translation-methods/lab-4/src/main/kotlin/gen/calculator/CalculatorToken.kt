package gen.calculator

import java.util.regex.Pattern

enum class CalculatorToken(val regex: String, val shouldBeSkipped: Boolean = false) { 
    PLUS("\\+"),
	MINUS("-"),
	TIMES("\\*"),
	DIVIDE("/"),
	LPAREN("\\("),
	RPAREN("\\)"),
	EPSILON("EPSILON"),
	NUM("[0-9]+"),
    END("#");
    
    lateinit var value: String
    val pattern: Pattern = Pattern.compile(regex)
}
