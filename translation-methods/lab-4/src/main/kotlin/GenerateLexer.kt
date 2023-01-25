import java.io.File
import java.util.*

class GenerateLexer(private val grammar: Grammar, private val pathToDir: String) {
    private val prefixPackageName = "gen.${grammar.name?.lowercase(Locale.getDefault())}"
    private val prefixPath = prefixPackageName.replace(".", "/")

    private val lexerName = "${grammar.name}Lexer"
    private val enumName = "${grammar.name}Token"

    private val path = "$pathToDir/$prefixPath/$lexerName.kt"

    fun generate() {
        val sourceCode = """
            |package $prefixPackageName
            |
            |import java.util.regex.Matcher
            |
            |class $lexerName(private var input: String) {
            |    private var curPos = 0
            |    private var map: MutableMap<$enumName, Matcher> = mutableMapOf()
            |    
            |    fun nextToken(): $enumName {
            |        for ((token, matcher) in map) {
            |            if (matcher.region(curPos, input.length).lookingAt()) {
            |                return if (!token.shouldBeSkipped) {
            |                    token.value = matcher.group()
            |                    curPos = matcher.end()
            |                    token
            |                } else {
            |                    curPos = matcher.end()
            |                    nextToken()
            |                }
            |            }
            |        }
            |        throw Exception("No token found")
            |    }
            |    
            |    init {
            |        input += '#'
            |        for (token in $enumName.values()) {
            |            map[token] = token.pattern.matcher(input)
            |        }
            |    }
            |}
        """.trimMargin("|")
        File(path).parentFile.mkdirs()
        File(path).writeText(sourceCode)
    }
}