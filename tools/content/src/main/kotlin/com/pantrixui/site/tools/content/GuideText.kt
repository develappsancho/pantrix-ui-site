package com.pantrixui.site.tools.content

import com.pantrixui.site.core.model.Language

/** The pieces of text [GuidePages] works with: links, spans of code, sentences and Turkish words. */
internal object GuideText {
    /** A link, `[text](target)`. */
    val LINK = Regex("""\[([^\]]+)]\(([^)]+)\)""")

    /** A span of code, `` `code` ``. */
    val CODE_SPAN = Regex("`[^`]*`")

    /** A Turkish clause that names what a document explains, as a name: "olmadığını" becomes "olmadığı". */
    fun nominal(text: String): String = NOMINAL.replace(text, "$1$2")

    /** [text] without its links' targets and its marks of bold, italics and code. */
    fun plain(text: String): String =
        LINK.replace(text, "$1").replace("**", "").replace("`", "").replace("*", "")

    /** [word] in small letters, a Turkish one by Turkish's rules: I is ı, İ is i. */
    fun lower(
        word: String,
        language: Language
    ): String = (if (language == Language.Turkish) word.replace('I', 'ı').replace('İ', 'i') else word).lowercase()

    /** This text with its first letter a capital. */
    fun String.capitalised(): String = replaceFirstChar { it.uppercaseChar() }

    /** The heading of a guide's symptom in [language], where its sketch stands. */
    fun symptomOf(language: Language): String =
        if (language == Language.English) "### Symptom" else "### Belirti"

    /** [text] with no more than one blank line in a row. */
    fun collapsed(text: String): String = BLANK_LINES.replace(text, "\n\n")

    /** [text] with the parts that are not code given to [change], and its spans of code left as they are. */
    fun outsideCode(
        text: String,
        change: (String) -> String
    ): String = pieces(text, CODE_SPAN).joinToString("") { (part, code) -> if (code) part else change(part) }

    /** [text] cut at each match of [by], in order: each piece with whether it is a match. */
    fun pieces(
        text: String,
        by: Regex
    ): List<Pair<String, Boolean>> {
        val out = mutableListOf<Pair<String, Boolean>>()
        var at = 0
        by.findAll(text).forEach { match ->
            out += text.substring(at, match.range.first) to false
            out += match.value to true
            at = match.range.last + 1
        }
        out += text.substring(at) to false
        return out
    }

    private val NOMINAL = Regex("""(?U)(ğ|c|k)(ı|i|u|ü)n(ı|i|u|ü)\b""")
    private val BLANK_LINES = Regex("\n{3,}")
}
