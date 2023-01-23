package gen.test

import java.util.regex.Pattern

enum class TestToken(val regex: String, val shouldBeSkipped: Boolean = false) { 
    ID("joker"),
	PIG("adis"),
	WS("[ \\n\\t\\r]+", true),
    END("#");
    
    var value: String? = null
    val pattern: Pattern = Pattern.compile(regex)
}
