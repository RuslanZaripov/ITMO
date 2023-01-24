import java.io.File
import java.util.*

class GenerateEnum(private val grammar: Grammar, private val path: String) {
    private val prefixPackageName = "gen.${grammar.name?.lowercase(Locale.getDefault())}"
    private val prefixPath = prefixPackageName.replace(".", "/")

    fun generate() {
        val enumName = "${grammar.name}Token"
        val path = "$path/$prefixPath/$enumName.kt"

        val nonTerminals = grammar.rules.filterIsInstance<Terminal>()
        // TODO: add WS: [ \t\n ]+ -> skip
        val sourceCode = """
            #package $prefixPackageName
            #
            #import java.util.regex.Pattern
            #
            #enum class $enumName(val regex: String, val shouldBeSkipped: Boolean = false) { 
            #    ${nonTerminals.joinToString(",\n\t", postfix = ",") { "${it.name}(${it.regex}${setSkipOption(it.shouldBeSkipped)})" }}
            #    END("#");
            #    
            #    lateinit var value: String
            #    val pattern: Pattern = Pattern.compile(regex)
            #}
            #
        """.trimMargin("#")

        // create path if not exists and write source code to file
        File(path).parentFile.mkdirs()
        File(path).writeText(sourceCode)
    }

    private fun setSkipOption(shouldBeSkipped: Boolean): String = if (shouldBeSkipped) ", true" else ""
}