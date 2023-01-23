//import java.util.regex.Pattern
//
//abstract class AbstractLexer(private val input: CharSequence) {
//    private var curPos = 0
//    private var curChar: Char? = null
//    private var length = input.length
//
//    private val rules = setOf<Terminal>()
//
//    private fun nextToken() : Token {
//        for (rule in rules) {
//            val matcher = Pattern.compile(rule.regex).matcher(input)
//
//            if (matcher.region(curPos, length).lookingAt()) {
//                val token = Token.valueOf(rule.name)
//                curPos = matcher.end()
//                return token
//            }
//        }
//    }
//}