package com.pantrixui.site.feature.components

/**
 * A style's fields as a reader reads them, from what the style says of itself: `TextFieldStyle(shape=Medium,
 * height=48.0.dp, …)`, each field by its name, its value as the page writes it. Every style of the library writes
 * itself so, which lets the page read any of them without knowing its fields.
 */
internal object StyleValues {
    /** The fields [written], a style's own text, says, a style inside it by its own fields after its name. */
    fun of(written: String): Map<String, String> {
        val open = written.indexOf('(')
        if (open < 0 || !written.endsWith(")")) return emptyMap()
        val fields = linkedMapOf<String, String>()
        piecesOf(written.substring(open + 1, written.length - 1)).forEach { piece ->
            val equals = piece.indexOf('=')
            if (equals < 0) return@forEach
            val name = piece.substring(0, equals).trim()
            val value = piece.substring(equals + 1).trim()
            val nested = NESTED_STYLE.matchEntire(value)
            if (nested != null) {
                of(value).forEach { (inner, said) -> fields["$name.$inner"] = said }
            } else {
                fields[name] = shown(value)
            }
        }
        return fields
    }

    /** [value] as the page writes it: `48dp` for `48.0.dp`, a dash for what is not set. */
    fun shown(value: String): String = when {
        value == "null" || value.endsWith("Unspecified") || value == "NaN.dp" -> NONE
        DP.matches(value) -> number(value.removeSuffix(".dp")) + "dp"
        SP.matches(value) -> number(value.removeSuffix(".sp")) + "sp"
        value.startsWith("ShapeSpec.Fixed") -> FIXED
        value.startsWith("ShapeSpec.") -> value.removePrefix("ShapeSpec.")
        value.startsWith("Color(") -> value
        else -> value.substringAfterLast('.').takeIf { value.matches(ENUM) } ?: value
    }

    private fun number(written: String): String = written.removeSuffix(".0")

    /** The pieces of [text] that commas part outside every bracket. */
    private fun piecesOf(text: String): List<String> {
        val pieces = mutableListOf<String>()
        var depth = 0
        val piece = StringBuilder()
        text.forEach { char ->
            when (char) {
                '(', '[', '{' -> depth++
                ')', ']', '}' -> depth--
            }
            if (char == ',' && depth == 0) {
                pieces += piece.toString()
                piece.clear()
            } else {
                piece.append(char)
            }
        }
        if (piece.isNotBlank()) pieces += piece.toString()
        return pieces
    }

    /** What the page writes for a field that is not set. */
    const val NONE = "–"
    private const val FIXED = "Fixed"

    private val DP = Regex("""-?\d+(\.\d+)?\.dp""")
    private val SP = Regex("""-?\d+(\.\d+)?\.sp""")
    private val ENUM = Regex("""[A-Z]\w*(\.[A-Z]\w*)*""")
    private val NESTED_STYLE = Regex("""[A-Z]\w*Style\(.*\)""")
}
