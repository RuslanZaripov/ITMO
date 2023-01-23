package gen.test

import java.util.regex.Matcher

class TestLexer(private var input: String) {
    private var curPos = 0
    private var map: MutableMap<TestToken, Matcher> = mutableMapOf()
    
    fun nextToken(): TestToken {
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
       for (token in TestToken.values()) {
           map[token] = token.pattern.matcher(input)
       }
    }
}