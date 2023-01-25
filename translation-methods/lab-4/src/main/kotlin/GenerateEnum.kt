import java.io.File
import java.util.*

class GenerateEnum(private val grammar: Grammar, private val pathToDir: String) {
    private val prefixPackageName = "gen.${grammar.name?.lowercase(Locale.getDefault())}"
    private val prefixPath = prefixPackageName.replace(".", "/")

    private val enumName = "${grammar.name}Token"
    private val path = "$pathToDir/$prefixPath/$enumName.kt"

    fun generate() {
        val sourceCode = """
            #package $prefixPackageName
            #
            #import java.util.regex.Pattern
            #
            #enum class $enumName(val regex: String, val shouldBeSkipped: Boolean = false) { 
            #    ${generateTokensInit()}
            #    END("#");
            #    
            #    lateinit var value: String
            #    val pattern: Pattern = Pattern.compile(regex)
            #}
            #
        """.trimMargin("#")
        File(path).parentFile.mkdirs()
        File(path).writeText(sourceCode)
    }

    private fun generateTokensInit() =
        grammar.terminals.joinToString(
            ",\n\t",
            postfix = ","
        ) { "${it.name}(${it.regex}${setSkipOption(it.shouldBeSkipped)})" }

    private fun setSkipOption(shouldBeSkipped: Boolean): String = if (shouldBeSkipped) ", true" else ""
}