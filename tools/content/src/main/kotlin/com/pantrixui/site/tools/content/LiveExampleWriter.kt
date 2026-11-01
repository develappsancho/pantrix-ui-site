package com.pantrixui.site.tools.content

import com.pantrixui.site.core.model.Language
import java.io.File

/**
 * Writes the demo's examples the site runs as Kotlin: each in a file and a package of its own, its body the body of a
 * composable `Example`, and what of its file it calls beside it, as the library's documents carry the demo's code.
 * An index finds each by its language, its component and its name. The page draws an example from it, and shows the
 * same code under it.
 *
 * @param out the folder of the sources: the module's generated Kotlin.
 */
internal class LiveExampleWriter(private val out: File) {
    /** Writes [examples], each language's, in place of what was written before; says how many. */
    fun write(examples: Map<Language, List<RunnableExample>>): Int {
        out.deleteRecursively()
        examples.forEach { (language, written) -> written.forEach { write(language, it) } }
        File(out, "${ROOT.replace('.', '/')}/LiveExampleIndex.kt").apply { parentFile.mkdirs() }
            .writeText(index(examples))
        return examples.values.sumOf { it.size }
    }

    private fun write(
        language: Language,
        example: RunnableExample
    ) {
        val pkg = packageOf(language, example)
        val (body, declarations) = split(example.code)
        val imports = (example.imports + ALWAYS).distinct().sorted()
        val text = buildString {
            appendLine("// Written at build time by the content tool, from the library's documents: the demo's example")
            appendLine(
                "// \"${example.title.replace("\n", " ")}\" of ${example.component}. The page shows this code under it."
            )
            appendLine("@file:Suppress(\"all\")")
            appendLine("@file:OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)")
            appendLine()
            appendLine("package $pkg")
            appendLine()
            imports.forEach { appendLine("import $it") }
            appendLine()
            appendLine("@Composable")
            appendLine("internal fun Example() {")
            body.lines().forEach { line -> appendLine(if (line.isBlank()) "" else "    $line") }
            appendLine("}")
            if (declarations.isNotBlank()) {
                appendLine()
                appendLine(ordered(declarations))
            }
        }
        File(out, "${pkg.replace('.', '/')}/Example.kt").apply { parentFile.mkdirs() }.writeText(text)
    }

    /** The index: for a language, a component and an example's name, the composable that draws it. */
    private fun index(examples: Map<Language, List<RunnableExample>>): String = buildString {
        appendLine("// Written at build time by the content tool: the demo's examples the site runs, by where each is.")
        appendLine("@file:Suppress(\"all\")")
        appendLine()
        appendLine("package $ROOT")
        appendLine()
        appendLine("import androidx.compose.runtime.Composable")
        appendLine("import com.pantrixui.site.core.model.Language")
        appendLine()
        appendLine(
            "/** The example [name] of the component [component] in [language], or null where the site runs none. */"
        )
        appendLine("internal fun liveExampleOf(")
        appendLine("    language: Language,")
        appendLine("    component: String,")
        appendLine("    name: String")
        appendLine("): (@Composable () -> Unit)? = when (language) {")
        Language.entries.forEach { language ->
            appendLine("    Language.${language.name} -> ${functionOf(language)}(component, name)")
        }
        appendLine("}")
        Language.entries.forEach { language ->
            val byComponent = examples[language].orEmpty().groupBy { it.component }
            appendLine()
            appendLine("private fun ${functionOf(language)}(")
            appendLine("    component: String,")
            appendLine("    name: String")
            appendLine("): (@Composable () -> Unit)? = when (component) {")
            byComponent.keys.forEach { component ->
                appendLine("    \"$component\" -> ${functionOf(language)}${segment(component)}(name)")
            }
            appendLine("    else -> null")
            appendLine("}")
            byComponent.forEach { (component, written) ->
                appendLine()
                appendLine(
                    "private fun ${functionOf(
                        language
                    )}${segment(component)}(name: String): (@Composable () -> Unit)? ="
                )
                appendLine("    when (name) {")
                written.forEach { example ->
                    appendLine("        \"${example.name}\" -> { { ${packageOf(language, example)}.Example() } }")
                }
                appendLine("        else -> null")
                appendLine("    }")
            }
        }
    }

    private fun functionOf(language: Language): String = "examples" + language.name

    companion object {
        /** The package the examples and their index are written under. */
        const val ROOT = "com.pantrixui.site.feature.components.live"

        /** What every example's file imports, whatever its code does. */
        private val ALWAYS = listOf(
            "androidx.compose.foundation.ExperimentalFoundationApi",
            "androidx.compose.foundation.layout.ExperimentalLayoutApi",
            "androidx.compose.runtime.Composable"
        )

        /** What a line of the file's own that an example calls starts with, where the example's body has ended. */
        private val DECLARATION = Regex(
            """^(private |internal |public |@Composable|@OptIn|@Suppress|@Immutable|@Stable|/\*\*|enum class|""" +
                """data class|class |object |sealed |fun |val |const val)"""
        )

        private val KEYWORDS = setOf("as", "in", "is", "do", "if", "for", "fun", "val", "var", "object", "when", "try")

        /** The package of [example] in [language]: one of its own, so that what its file declares is its alone. */
        fun packageOf(
            language: Language,
            example: RunnableExample
        ): String = "$ROOT.${language.code}.${segment(example.component)}.${segment(example.name)}"

        /** [key] as a part of a package's name: its letters and digits, never a keyword, never a digit first. */
        fun segment(key: String): String {
            val letters = key.filter { it.isLetterOrDigit() }.lowercase()
            val safe = if (letters.firstOrNull()?.isDigit() != false) "x$letters" else letters
            return if (safe in KEYWORDS) "${safe}x" else safe
        }

        /**
         * [declarations], what of its file an example calls, in an order that compiles: a property after those its
         * value reads. The documents write them in the order the code names them, and a property of a file is read in
         * the order it is declared.
         */
        fun ordered(declarations: String): String {
            val lines = declarations.lines()
            val starts = lines.indices.filter { index ->
                (index == 0 || lines[index - 1].isBlank()) && DECLARATION.containsMatchIn(lines[index])
            }
            val pieces = starts.mapIndexed { at, start ->
                lines.subList(start, starts.getOrElse(at + 1) { lines.size }).joinToString("\n").trim()
            }
            val names = pieces.map { piece -> NAME.find(piece)?.groupValues?.get(1) }
            val placed = mutableListOf<Int>()

            fun place(index: Int, seen: Set<Int>) {
                if (index in placed || index in seen) return
                val piece = pieces[index]
                if (PROPERTY.containsMatchIn(piece.lineSequence().first())) {
                    names.forEachIndexed { other, name ->
                        val read = name != null && other != index && Regex("""\b${Regex.escape(name)}\b""")
                            .containsMatchIn(piece.substringAfter('='))
                        if (read && PROPERTY.containsMatchIn(pieces[other].lineSequence().first())) {
                            place(other, seen + index)
                        }
                    }
                }
                placed += index
            }
            pieces.indices.forEach { place(it, emptySet()) }
            return placed.joinToString("\n\n") { pieces[it] }
        }

        /** What a declaration declares: its name after its kind. */
        private val NAME = Regex("""(?:val|var|fun|class|object|interface)\s+(?:<[^>]*>\s*)?(?:[\w.]+\.)?(\w+)""")

        /** A property of a file, whose value is read when the file is first used. */
        private val PROPERTY = Regex("""^(private |internal |public )?val """)

        /**
         * [code], an example's, as its body and what of its file it calls: the body is up to the first blank line
         * followed by a declaration, which the documents write after the body.
         */
        fun split(code: String): Pair<String, String> {
            val lines = code.lines()
            val start = lines.indices.firstOrNull { index ->
                index > 0 && lines[index - 1].isBlank() && DECLARATION.containsMatchIn(lines[index]) &&
                    !lines[index].startsWith("val ")
            }
            return if (start == null) {
                Pair(code, "")
            } else {
                Pair(lines.take(start).joinToString("\n").trimEnd(), lines.drop(start).joinToString("\n").trim())
            }
        }
    }
}
