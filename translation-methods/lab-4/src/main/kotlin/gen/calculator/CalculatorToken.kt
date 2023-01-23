package gen.calculator

import java.util.regex.Pattern

enum class CalculatorToken(val regex: String, val shouldBeSkipped: Boolean = false) { 
    PLUS("+"),
	MINUS("-"),
	TIMES("*"),
	DIVIDE("/"),
	LPAREN("("),
	RPAREN(")"),
	ID("[a-zA-Z]+"),
    END("#");
    
    var value: String? = null
    val pattern: Pattern = Pattern.compile(regex)
}
