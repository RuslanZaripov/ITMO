package gen.labsecondgrammar

import java.util.regex.Pattern

enum class LabSecondGrammarToken(val regex: String, val shouldBeSkipped: Boolean = false) { 
    ASSIGN("="),
	COLON(":"),
	SEMICOLON(";"),
	VAR("var"),
	VAL("val"),
	TYPE("Int"),
	INT("[0-9]+"),
	ID("[a-zA-Z][a-zA-Z0-9]*"),
	WS("[ \\n\\t\\r]+", true),
    END("#");
    
    lateinit var value: String
    val pattern: Pattern = Pattern.compile(regex)
}
