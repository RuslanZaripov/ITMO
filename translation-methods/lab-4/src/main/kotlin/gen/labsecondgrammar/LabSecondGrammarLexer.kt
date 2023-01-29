package gen.labsecondgrammar

import java.util.regex.Matcher

class LabSecondGrammarLexer(private var input: String) {
    private var curPos = 0
    private var map: MutableMap<LabSecondGrammarToken, Matcher> = mutableMapOf()
    
    fun nextToken(): LabSecondGrammarToken {
        for ((token, matcher) in map) {
            if (matcher.region(curPos, input.length).lookingAt()) {
                return if (!token.shouldBeSkipped) {
                    token.value = matcher.group()
                    curPos = matcher.end()
                    token
                } else {
                    curPos = matcher.end()
                    nextToken()
                }
            }
        }
        throw Exception("No token found")
    }
    
    init {
        input += '#'
        for (token in LabSecondGrammarToken.values()) {
            map[token] = token.pattern.matcher(input)
        }
    }
}