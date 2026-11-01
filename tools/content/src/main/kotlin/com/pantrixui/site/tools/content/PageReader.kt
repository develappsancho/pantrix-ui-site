package com.pantrixui.site.tools.content

import com.pantrixui.site.core.model.Block
import com.pantrixui.site.core.model.Box
import com.pantrixui.site.core.model.Card
import com.pantrixui.site.core.model.CheckGroup
import com.pantrixui.site.core.model.CheckKind
import com.pantrixui.site.core.model.ContentsEntry
import com.pantrixui.site.core.model.Fact
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Page
import com.pantrixui.site.core.model.Point
import com.pantrixui.site.core.model.Row
import com.pantrixui.site.core.model.Span
import com.pantrixui.site.core.model.Step
import com.pantrixui.site.core.model.Tab
import com.pantrixui.site.core.model.TableStyle
import com.pantrixui.site.core.model.Tone

/**
 * A page the site writes itself, from Markdown with a head of its own and the site's pieces in it.
 *
 * ```
 * ---
 * title: Installation
 * description: Add Pantrix UI to a Compose Multiplatform project…
 * section: Getting started
 * ---
 * :::facts
 * - Kotlin: 2.4.21 or newer
 * :::
 * ```
 *
 * A piece starts with `:::` and its name and ends with `:::` alone on a line; pieces may hold pieces. The pieces
 * are `facts`, `steps` (each step a `##` heading), `tabs` and what its views are of (each tab a `###` heading),
 * `callout` and its tone (`why` for a cause), `table` and its caption (a list of `path: what`, or a Markdown table
 * whose rows marked `{highlight}` are set apart), `labels` (a list of `name: what`), `rows` and `cards`, each a list
 * in Markdown; and for a guide, `aside` and the key of the sketch beside what it holds, `sketch` and the key of a
 * sketch as wide as the page, `note`, `points` (a list of
 * `**Title.** sentence`), `boxes` (each box a `###` heading) and `checks` (each column a `###` heading ending in
 * `{seen}`, `{read}` or `{unchecked}`, with a list). Everything outside them is Markdown.
 */
internal class PageReader(
    private val language: Language,
    private val path: String
) {
    /** The page [source] says. */
    fun read(source: String): Page {
        val (head, body) = splitHead(source)
        val blocks = blocks(Segment.parse(body.lines()))
        val title = head["title"] ?: error("$path in ${language.code}: a page has a title")
        val description = head["description"].orEmpty()
        return Page(
            path = path,
            language = language,
            title = title,
            description = description,
            section = head["section"],
            blocks = blocks,
            contents = contents(blocks),
            markdown = listOf("# $title", description, readableMarkdown(body)).filter {
                it.isNotBlank()
            }.joinToString("\n\n")
        )
    }

    /** The body as a reader copies it: the site's piece markers taken out, and a heading's attributes too. */
    private fun readableMarkdown(body: String): String = body.lines()
        .filterNot { it.trim().startsWith(":::") }
        .joinToString("\n") { line ->
            if (line.startsWith("#")) line.replace(Regex("""\s*\{[^}]*\}\s*$"""), "") else line
        }
        .replace(Regex("\n{3,}"), "\n\n")
        .trim()

    private fun splitHead(source: String): Pair<Map<String, String>, String> {
        val lines = source.lines()
        if (lines.firstOrNull()?.trim() != "---") return emptyMap<String, String>() to source
        val end = lines.drop(1).indexOfFirst { it.trim() == "---" } + 1
        val head = lines.subList(1, end).filter { ':' in it }.associate {
            it.substringBefore(':').trim() to it.substringAfter(':').trim()
        }
        return head to lines.drop(end + 1).joinToString("\n")
    }

    private fun blocks(segments: List<Segment>): List<Block> = segments.flatMap { segment ->
        when (segment) {
            is Segment.Text -> MarkdownBlocks.blocks(segment.text)
            is Segment.Piece -> listOf(piece(segment))
        }
    }

    private fun piece(piece: Segment.Piece): Block = when (piece.name) {
        "facts" -> Block.Facts(items(piece).map { Fact(it.substringBefore(':').trim(), it.substringAfter(':').trim()) })

        "steps" -> Block.Steps(
            parts(piece, "## ").map { (heading, segments) ->
                val parsed = Heading.of(heading)
                Step(
                    anchor = parsed.anchor,
                    title = parsed.title,
                    optional = parsed.optional,
                    blocks = blocks(segments),
                    contentsTitle = parsed.contentsTitle
                )
            }
        )

        "tabs" -> Block.Tabs(
            tabs = parts(piece, "### ").map { (heading, segments) -> Tab(heading.trim(), blocks(segments)) },
            label = piece.argument.ifEmpty { null }
        )

        "callout" -> Block.Callout(tone = tone(piece.argument), blocks = blocks(piece.children))

        "table" -> markdownTable(piece) ?: Block.Table(
            caption = piece.argument.ifEmpty { null },
            rows = labelled(piece)
        )

        "labels" -> Block.Table(
            caption = piece.argument.ifEmpty {
                null
            },
            rows = labelled(piece),
            style = TableStyle.Labels
        )

        "aside", "sketch", "note", "points", "boxes", "checks" -> guidePiece(piece)

        "rows" -> Block.Rows(
            items(piece).map { item ->
                Row(
                    label = item.substringBefore(": ").trim('*', ' '),
                    text = MarkdownBlocks.spans(item.substringAfter(": "))
                )
            }
        )

        "cards" -> Block.Cards(
            items(piece).map { item ->
                val link = MarkdownBlocks.spans(
                    item.substringBefore("): ") + ")"
                ).filterIsInstance<Span.Link>().single()
                Card(title = link.text.plainText(), text = item.substringAfter("): ").trim(), href = link.href)
            }
        )

        else -> error("$path in ${language.code}: no piece is called \"${piece.name}\"")
    }

    /** One of a guide's own pieces: a sketch beside its text, a note, points, boxes and columns of checks. */
    private fun guidePiece(piece: Segment.Piece): Block = when (piece.name) {
        "aside" -> Block.Aside(sketch = piece.argument, blocks = blocks(piece.children))

        "sketch" -> Block.Sketch(piece.argument)

        "note" -> Block.Note(MarkdownBlocks.spans(text(piece)))

        "points" -> Block.Points(
            items(piece).map { item ->
                val spans = MarkdownBlocks.spans(item)
                val title = spans.takeWhile { it is Span.Text && it.strong }
                Point(title = title, text = spans.drop(title.size).trimmedStart())
            }
        )

        "boxes" -> Block.Boxes(
            parts(piece, "### ").map { (heading, segments) ->
                Box(MarkdownBlocks.spans(heading.trim()), blocks(segments))
            }
        )

        "checks" -> Block.Checks(
            parts(piece, "### ").map { (heading, segments) ->
                val kind = CHECK_KINDS.entries.firstOrNull { (mark, _) -> heading.trimEnd().endsWith(mark) }
                    ?: error(
                        "$path in ${language.code}: a column of checks ends in {seen}, {read} or {unchecked}: $heading"
                    )
                CheckGroup(
                    kind = kind.value,
                    title = heading.trimEnd().removeSuffix(kind.key).trim(),
                    items = segments.filterIsInstance<Segment.Text>().flatMap { it.text.lines() }
                        .filter { it.trimStart().startsWith("- ") }
                        .map { MarkdownBlocks.spans(it.trimStart().removePrefix("- ").trim()) }
                )
            }
        )

        else -> error("$path in ${language.code}: no piece of a guide is called \"${piece.name}\"")
    }

    /**
     * The Markdown table a `table` piece holds, with its caption, its first column in bold and the rows marked
     * `{highlight}` set apart; null where the piece holds a list instead.
     */
    private fun markdownTable(piece: Segment.Piece): Block.Table? {
        val lines = text(piece).lines()
        if (lines.none { it.trimStart().startsWith("|") }) return null
        // The rows after the header and the line under it, counted from nought, that are marked.
        val body = lines.filter { it.trimStart().startsWith("|") }.drop(2)
        val highlighted = body.indices.filter { HIGHLIGHT in body[it] }
        val table = MarkdownBlocks.blocks(lines.joinToString("\n") { it.replace(HIGHLIGHT, "") })
            .filterIsInstance<Block.Table>().single()
        return table.copy(
            caption = piece.argument.ifEmpty { null },
            highlighted = highlighted,
            style = TableStyle.Labels
        )
    }

    /** The page's sections and steps, as its "On this page" list names them. */
    private fun contents(blocks: List<Block>): List<ContentsEntry> = blocks.flatMap { block ->
        when (block) {
            is Block.Heading -> if (block.level == 2) {
                listOf(ContentsEntry(block.anchor, block.contentsTitle ?: block.text.plainText()))
            } else {
                emptyList()
            }

            is Block.Steps -> block.steps.mapIndexed { index, step ->
                ContentsEntry(step.anchor, "${index + 1}. ${step.contentsTitle ?: step.title}")
            }

            else -> emptyList()
        }
    }
}

/** What marks a row of a table as the one set apart. */
private const val HIGHLIGHT = "{highlight}"

/** The end of a column of checks' heading, by how what it lists was checked. */
private val CHECK_KINDS = mapOf(
    "{seen}" to CheckKind.Seen,
    "{read}" to CheckKind.Read,
    "{unchecked}" to CheckKind.Unchecked
)

/** These runs without the spaces at their start. */
internal fun List<Span>.trimmedStart(): List<Span> {
    val first = firstOrNull() as? Span.Text ?: return this
    val trimmed = first.text.trimStart()
    return if (trimmed.isEmpty()) drop(1).trimmedStart() else listOf(first.copy(text = trimmed)) + drop(1)
}

/** A run of text as it reads, without its formatting. */
internal fun List<Span>.plainText(): String = joinToString("") { span ->
    when (span) {
        is Span.Text -> span.text
        is Span.Code -> span.text
        is Span.Link -> span.text.plainText()
    }
}

/** A part of a page's body: Markdown, or one of the site's pieces with what it holds. */
internal sealed interface Segment {
    /** Markdown. */
    data class Text(val text: String) : Segment

    /** A piece: its [name], what follows the name on its first line, and what it holds. */
    data class Piece(
        val name: String,
        val argument: String,
        val children: List<Segment>
    ) : Segment

    companion object {
        /** The segments of [lines]. A `:::` inside a block of code is code, not a piece. */
        fun parse(lines: List<String>): List<Segment> {
            val root = mutableListOf<Segment>()
            val open = ArrayDeque<Triple<String, String, MutableList<Segment>>>()
            val text = mutableListOf<String>()
            var inCode = false

            fun target() = open.lastOrNull()?.third ?: root

            fun flush() {
                if (text.any { it.isNotBlank() }) target() += Text(text.joinToString("\n"))
                text.clear()
            }
            lines.forEach { line ->
                val trimmed = line.trim()
                when {
                    trimmed.startsWith("```") -> {
                        inCode = !inCode
                        text += line
                    }

                    inCode -> {
                        text += line
                    }

                    trimmed == ":::" -> {
                        flush()
                        val (name, argument, children) = open.removeLast()
                        target() += Piece(name, argument, children)
                    }

                    trimmed.startsWith(":::") -> {
                        flush()
                        val header = trimmed.removePrefix(":::").trim()
                        open.addLast(
                            Triple(header.substringBefore(' '), header.substringAfter(' ', "").trim(), mutableListOf())
                        )
                    }

                    else -> {
                        text += line
                    }
                }
            }
            flush()
            require(open.isEmpty()) { "a piece is not closed: ${open.last().first}" }
            return root
        }
    }
}
