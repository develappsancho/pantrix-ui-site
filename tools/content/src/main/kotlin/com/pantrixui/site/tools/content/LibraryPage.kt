package com.pantrixui.site.tools.content

/**
 * A page of the library's documents, `pages/<key>.md` or `tr/pages/<key>.md`, taken apart by its headings, which are
 * the same in both languages but for their words: its title and summary, its features, its examples and the API it
 * documents.
 *
 * @property title what the page is called.
 * @property summary what it says before its parts, as Markdown.
 * @property features its features, a list item each, as Markdown.
 * @property examples its examples, in order.
 * @property declarations the declarations of its API, a form of an overloaded function each, and a type inside
 *   another by its name after the other's, `ButtonStyle.Size`.
 */
internal class LibraryPage(
    val title: String,
    val summary: String,
    val features: List<String>,
    val examples: List<LibraryExample>,
    val declarations: List<LibraryDeclaration>
) {
    companion object {
        /** [markdown], a page of the documents, taken apart. */
        fun read(markdown: String): LibraryPage {
            val lines = markdown.lines()
            val title = lines.first { it.startsWith("# ") }.removePrefix("# ").trim()
            val sections = sectionsOf(lines.drop(lines.indexOfFirst { it.startsWith("# ") } + 1))
            val head = sections[null].orEmpty()
            // The summary ends where the list of the page's components starts: a line with a function in it.
            val summary = head.takeWhile { !it.startsWith("- `") }.joinToString("\n").trim()
            return LibraryPage(
                title = title,
                summary = summary,
                features = itemsOf(sections.entries.firstOrNull { it.key in FEATURES }?.value.orEmpty()),
                examples = examplesOf(sections.entries.firstOrNull { it.key in EXAMPLES }?.value.orEmpty()),
                declarations = declarationsOf(sections[API].orEmpty())
            )
        }

        /** The lines under each `##` heading, by its words; those before the first, under null. */
        private fun sectionsOf(lines: List<String>): Map<String?, List<String>> {
            val sections = linkedMapOf<String?, MutableList<String>>(null to mutableListOf())
            var current: String? = null
            var fenced = false
            lines.forEach { line ->
                if (line.trimStart().startsWith(FENCE)) fenced = !fenced
                if (!fenced && line.startsWith("## ")) {
                    current = line.removePrefix("## ").trim()
                    sections[current] = mutableListOf()
                } else {
                    sections.getValue(current).add(line)
                }
            }
            return sections
        }

        /** The items of a list, each with the lines under it joined to it: what a feature says. */
        private fun itemsOf(lines: List<String>): List<String> {
            val items = mutableListOf<StringBuilder>()
            lines.forEach { line ->
                when {
                    line.startsWith("- ") -> items += StringBuilder(line.removePrefix("- "))
                    line.startsWith("  ") && items.isNotEmpty() -> items.last().append('\n').append(line.drop(2))
                    line.isBlank() && items.isNotEmpty() -> items.last().append('\n')
                }
            }
            return items.map { it.toString().trim() }
        }

        private fun examplesOf(lines: List<String>): List<LibraryExample> = cut(lines, "### ").map { (title, body) ->
            val open = body.indexOfFirst { it.trim().startsWith(FENCE) }
            val close = body.drop(open + 1).indexOfFirst { it.trim() == FENCE } + open + 1
            val description = body.take(maxOf(open, 0)).joinToString("\n").trim()
            val code = if (open < 0) emptyList() else body.subList(open + 1, close)
            val imports = code.takeWhile { it.startsWith("import ") || it.isBlank() }
            val after = if (open < 0) emptyList() else body.drop(close + 1)
            LibraryExample(
                title = title,
                description = description,
                imports = imports.filter { it.startsWith("import ") }.map { it.removePrefix("import ").trim() },
                code = code.drop(imports.size).joinToString("\n").trimEnd(),
                callsTheApp = after.any { line -> APP_NAMES.any { it in line } }
            )
        }

        private fun declarationsOf(lines: List<String>): List<LibraryDeclaration> {
            val declarations = mutableListOf<LibraryDeclaration>()
            var fenced = false
            var start = -1
            var name = ""

            fun close(end: Int) {
                if (start >= 0) declarations += declarationOf(name, lines.subList(start, end))
            }
            lines.forEachIndexed { index, line ->
                if (line.trimStart().startsWith(FENCE)) fenced = !fenced
                val heading = HEADING.matchEntire(line)
                if (!fenced && heading != null) {
                    close(index)
                    name = heading.groupValues[1].trim()
                    start = index + 1
                }
            }
            close(lines.size)
            return declarations
        }

        private fun declarationOf(
            name: String,
            lines: List<String>
        ): LibraryDeclaration {
            val open = lines.indexOfFirst { it.trim().startsWith(FENCE) }
            val close = lines.drop(open + 1).indexOfFirst { it.trim() == FENCE } + open + 1
            val signature = if (open < 0) "" else lines.subList(open + 1, close).joinToString("\n")
            val after = if (open < 0) lines else lines.drop(close + 1)
            val firstList = after.indexOfFirst { it.trim().removeSuffix(":") in LISTS && it.trim().endsWith(":") }
            val description = (if (firstList < 0) after else after.take(firstList)).joinToString("\n").trim()
            val parameters = after.indexOfFirst { it.trim() in PARAMETERS }.takeIf { it >= 0 }?.let { at ->
                val items = after.drop(at + 1).dropWhile { it.isBlank() }
                    .takeWhile { it.isBlank() || it.startsWith("- ") || it.startsWith("  ") }
                itemsOf(items).mapNotNull(::parameterOf)
            }.orEmpty()
            val defaults = defaultsOf(signature)
            return LibraryDeclaration(
                name = name,
                signature = signature,
                description = description,
                parameters = parameters.map { it.copy(default = defaults[it.name]) }
            )
        }

        private fun parameterOf(item: String): LibraryParameter? {
            val found = PARAMETER.find(item) ?: return null
            val written = found.groupValues[1].removePrefix("vararg ")
            return LibraryParameter(
                name = written.substringBefore(':').trim(),
                type = written.substringAfter(':').trim(),
                description = item.substring(found.range.last + 1).removePrefix(":").trim()
            )
        }

        /**
         * The defaults [signature] gives its parameters, by their names: what stands after ` = ` in each piece of
         * its first brackets that commas part outside every bracket.
         */
        fun defaultsOf(signature: String): Map<String, String> {
            val open = signature.indexOf('(')
            if (open < 0) return emptyMap()
            val pieces = mutableListOf<String>()
            var depth = 0
            var piece = StringBuilder()
            var previous = ' '
            for (char in signature.substring(open + 1)) {
                when (char) {
                    '(', '[', '{', '<' -> depth++

                    ')', ']', '}' -> depth--

                    // The arrow of a function's type closes nothing.
                    '>' -> if (previous != '-') depth--
                }
                previous = char
                if (depth < 0) break
                if (char == ',' && depth == 0) {
                    pieces += piece.toString()
                    piece = StringBuilder()
                } else {
                    piece.append(char)
                }
            }
            pieces += piece.toString()
            return pieces.mapNotNull { written ->
                val text = written.lines().joinToString(" ") { it.trim() }.trim()
                val equals = topLevelEquals(text)
                if (equals < 0) return@mapNotNull null
                val name = text.substring(0, equals).substringBefore(':').trim().split(' ').last()
                name to text.substring(equals + 1).trim()
            }.toMap()
        }

        /** Where the ` = ` that starts a default is in [text]: the first `=` outside every bracket and arrow. */
        private fun topLevelEquals(text: String): Int {
            var depth = 0
            text.forEachIndexed { index, char ->
                when (char) {
                    '(', '[', '{', '<' -> depth++
                    ')', ']', '}' -> depth--
                    '>' -> if (index > 0 && text[index - 1] != '-') depth--
                    '=' -> if (depth == 0 && text.getOrNull(index + 1) != '>') return index
                }
            }
            return -1
        }

        /** [lines] cut at each line that starts with [marker]: each such line's rest, and the lines up to the next. */
        private fun cut(
            lines: List<String>,
            marker: String
        ): List<Pair<String, List<String>>> {
            val parts = mutableListOf<Pair<String, MutableList<String>>>()
            var fenced = false
            lines.forEach { line ->
                if (line.trimStart().startsWith(FENCE)) fenced = !fenced
                if (!fenced && line.startsWith(marker)) {
                    parts += line.removePrefix(marker).trim() to mutableListOf()
                } else {
                    parts.lastOrNull()?.second?.add(line)
                }
            }
            return parts
        }

        private const val FENCE = "```"
        private const val API = "API"
        private val FEATURES = setOf("Features", "Özellikler")
        private val EXAMPLES = setOf("Examples", "Örnekler")
        private val PARAMETERS = setOf("Parameters:", "Parametreler:")

        /** The words before the list of what an example's code calls of the demo app itself. */
        private val APP_NAMES =
            listOf("What the code calls of the demo app itself", "Kodun demo uygulamasının kendisinden")

        /** What a declaration says after its description, each a list with these words before it. */
        private val LISTS = setOf(
            "Parameters",
            "Parametreler",
            "Another constructor",
            "Başka bir kurucu",
            "Entries",
            "Değerleri",
            "Members",
            "Üyeleri",
            "On the type",
            "Tipin kendisinde"
        )

        /** A declaration's heading, `### PantrixTextArea`, or a type inside another, `#### ButtonStyle.Size`. */
        private val HEADING = Regex("""#{3,6} (.+)""")

        /** A parameter's item: its name and type between backticks, `text: String`. */
        private val PARAMETER = Regex("""^`([^`]+)`""")
    }
}

/**
 * An example of a page of the library's documents.
 *
 * @property title what it shows.
 * @property description what it says of itself, as Markdown.
 * @property imports what its code imports.
 * @property code its code, without its imports.
 * @property callsTheApp whether its code calls something of the demo app itself, which an app writes for itself.
 */
internal class LibraryExample(
    val title: String,
    val description: String,
    val imports: List<String>,
    val code: String,
    val callsTheApp: Boolean = false
)

/**
 * A declaration of the API a page of the library's documents documents.
 *
 * @property name its name, a type inside another after the other's: `ButtonStyle.Size`.
 * @property signature how it is declared.
 * @property description what its documentation says of it, as Markdown.
 * @property parameters its parameters, or a type's fields.
 */
internal class LibraryDeclaration(
    val name: String,
    val signature: String,
    val description: String,
    val parameters: List<LibraryParameter>
)

/**
 * A parameter of a declaration.
 *
 * @property name its name.
 * @property type its type, as written.
 * @property description what it is for, as Markdown.
 * @property default its default, as written, or null where it has to be given.
 */
internal data class LibraryParameter(
    val name: String,
    val type: String,
    val description: String,
    val default: String? = null
)
