package com.pantrixui.site.tools.content

import com.pantrixui.site.core.model.Token
import com.pantrixui.site.core.model.TokenKind

/**
 * Splits code into coloured runs, line by line, as the site's design colours it: keywords, strings, numbers,
 * comments and annotations each in their colour, and the names a reader looks for, a composable or a type that is
 * called or declared, in theirs. Everything else is plain. A language it does not read is all plain.
 */
internal object Highlighter {
    /** The lines of [code] in [language], each a run of tokens. */
    fun lines(
        code: String,
        language: String
    ): List<List<Token>> = when (language.lowercase()) {
        "kotlin", "kt", "kts" -> KotlinLexer(code).tokens().splitIntoLines()
        "yaml", "yml" -> code.lines().map { yamlLine(it).merged() }
        else -> code.lines().map { listOf(Token(it)) }
    }

    /**
     * A line of YAML, as the design colours a Maestro flow: the command a list's item starts with as a keyword, a
     * quoted string as a string and what follows a `#` outside one as a comment. A key under a command is plain.
     */
    private fun yamlLine(line: String): List<Token> {
        val item = YAML_ITEM.find(line) ?: return yamlRest(line)
        return listOf(Token(item.groupValues[1]), Token(item.groupValues[2], TokenKind.Keyword)) +
            yamlRest(line.substring(item.value.length))
    }

    /** What follows a line's command: its strings, its comment, and the plain text between them. */
    private fun yamlRest(rest: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var start = 0
        var position = 0
        while (position < rest.length) {
            val char = rest[position]
            val end = when {
                char == '"' || char == '\'' -> closingQuote(rest, position)
                char == '#' && (position == 0 || rest[position - 1].isWhitespace()) -> rest.length
                else -> null
            }
            if (end == null) {
                position++
                continue
            }
            if (position > start) tokens += Token(rest.substring(start, position))
            val kind = if (char == '#') TokenKind.Comment else TokenKind.StringLiteral
            tokens += Token(rest.substring(position, end), kind)
            position = end
            start = end
        }
        if (start < rest.length) tokens += Token(rest.substring(start))
        return tokens
    }

    /** Where the string that opens at [start] in [text] ends: after its closing quote, or at the text's end. */
    private fun closingQuote(
        text: String,
        start: Int
    ): Int {
        val close = text.indexOf(text[start], start + 1)
        return if (close < 0) text.length else close + 1
    }

    /** The tokens cut at each line's end, a token that spans lines cut in two. */
    private fun List<Token>.splitIntoLines(): List<List<Token>> {
        val lines = mutableListOf(mutableListOf<Token>())
        forEach { token ->
            token.text.split('\n').forEachIndexed { index, part ->
                if (index > 0) lines += mutableListOf<Token>()
                if (part.isNotEmpty()) lines.last() += Token(part, token.kind)
            }
        }
        return lines.map { it.merged() }
    }

    /** Neighbours of one kind as one token. */
    private fun List<Token>.merged(): List<Token> = fold(mutableListOf()) { merged, token ->
        val last = merged.lastOrNull()
        if (last != null && last.kind == token.kind) {
            merged[merged.lastIndex] = Token(last.text + token.text, token.kind)
        } else {
            merged += token
        }
        merged
    }
}

/** The start of a list's item and the key it opens with: `- tapOn` in `- tapOn: "Send"`. */
private val YAML_ITEM = Regex("""^(\s*-\s+)([A-Za-z][\w-]*)(?=:)""")

/** Reads Kotlin, and the Kotlin of Gradle's scripts, well enough to colour it. */
private class KotlinLexer(private val code: String) {
    private var position = 0
    private val tokens = mutableListOf<Token>()
    private var lastWord: String? = null

    fun tokens(): List<Token> {
        while (position < code.length) {
            if (!takeCommentOrString()) takeCode()
        }
        return tokens
    }

    /** Takes the comment or the string at the current position whole, and says whether there was one. */
    private fun takeCommentOrString(): Boolean {
        val char = code[position]
        when {
            code.startsWith(LINE_COMMENT, position) -> take(TokenKind.Comment) { code.indexOf('\n', position).orEnd() }
            code.startsWith(BLOCK_OPEN, position) -> take(TokenKind.Comment) { closing(BLOCK_OPEN, BLOCK_CLOSE) }
            code.startsWith(RAW_QUOTES, position) -> take(TokenKind.StringLiteral) { closing(RAW_QUOTES, RAW_QUOTES) }
            char == '"' || char == '\'' -> take(TokenKind.StringLiteral) { quoted(char) }
            else -> return false
        }
        return true
    }

    /** Takes the annotation, number, word or mark at the current position. */
    private fun takeCode() {
        val char = code[position]
        when {
            char == '@' && code.getOrNull(position + 1)?.isLetter() == true -> {
                take(TokenKind.Annotation) { wordEnd(position + 1) }
            }

            char.isDigit() -> {
                take(TokenKind.Number) { numberEnd() }
            }

            char.isLetter() || char == '_' -> {
                word()
            }

            else -> {
                take(TokenKind.Plain) { position + 1 }
            }
        }
    }

    /** Where what [open] opened at the current position ends: after its [close], or at the end of the code. */
    private fun closing(
        open: String,
        close: String
    ): Int = code.indexOf(close, position + open.length).let { if (it < 0) code.length else it + close.length }

    private fun word() {
        val end = wordEnd(position)
        val word = code.substring(position, end)
        val next = code.drop(end).trimStart(' ').firstOrNull()
        // A name after a single colon is a type in a declaration, `object Brand : DesignPreset {`: not a call.
        val before = tokens.lastOrNull { it.text.isNotBlank() }?.text?.trimEnd().orEmpty()
        val isType = before.endsWith(":") && !before.endsWith("::")
        val kind = when {
            word in KEYWORDS -> TokenKind.Keyword
            lastWord in DECLARATIONS -> TokenKind.Function
            word.first().isUpperCase() && (next == '(' || next == '{') && !isType -> TokenKind.Function
            else -> TokenKind.Plain
        }
        tokens += Token(word, kind)
        lastWord = word
        position = end
    }

    private inline fun take(
        kind: TokenKind,
        end: () -> Int
    ) {
        val until = end()
        tokens += Token(code.substring(position, until), kind)
        if (kind != TokenKind.Plain || code.substring(position, until).isNotBlank()) lastWord = null
        position = until
    }

    /** The end of a string or a character opened with [quote] at the current position. */
    private fun quoted(quote: Char): Int {
        var at = position + 1
        while (at < code.length && code[at] != quote && code[at] != '\n') {
            if (code[at] == '\\') at++
            at++
        }
        return (at + 1).coerceAtMost(code.length)
    }

    private fun wordEnd(from: Int): Int {
        var at = from
        while (at < code.length && (code[at].isLetterOrDigit() || code[at] == '_')) at++
        return at
    }

    private fun numberEnd(): Int {
        var at = position
        while (at < code.length && code[at].isInNumber()) at++
        return at
    }

    private fun Int.orEnd(): Int = if (this < 0) code.length else this

    private companion object {
        const val LINE_COMMENT = "//"
        const val BLOCK_OPEN = "/*"
        const val BLOCK_CLOSE = "*/"
        const val RAW_QUOTES = "\"\"\""

        val KEYWORDS = setOf(
            "abstract",
            "actual",
            "annotation",
            "as",
            "break",
            "by",
            "catch",
            "class",
            "companion",
            "const",
            "constructor",
            "continue",
            "crossinline",
            "data",
            "do",
            "else",
            "enum",
            "expect",
            "external",
            "false",
            "final",
            "finally",
            "for",
            "fun",
            "if",
            "import",
            "in",
            "infix",
            "init",
            "inline",
            "inner",
            "interface",
            "internal",
            "is",
            "lateinit",
            "noinline",
            "null",
            "object",
            "open",
            "operator",
            "out",
            "override",
            "package",
            "private",
            "protected",
            "public",
            "reified",
            "return",
            "sealed",
            "super",
            "suspend",
            "tailrec",
            "this",
            "throw",
            "true",
            "try",
            "typealias",
            "val",
            "value",
            "var",
            "vararg",
            "when",
            "where",
            "while"
        )

        /** The keywords a declared name follows: what is declared is coloured as a name to look for. */
        val DECLARATIONS = setOf("fun", "object", "class", "interface")
    }
}

/** Whether a number goes on through [this]: its digits, its point, its separators and its suffix. */
private fun Char.isInNumber(): Boolean = isLetterOrDigit() || this == '.' || this == '_'
