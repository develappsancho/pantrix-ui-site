package com.pantrixui.site.tools.content

import com.pantrixui.site.core.model.Span
import com.pantrixui.site.core.model.Tone

// How a piece of a page is cut into what it holds: its list's items, its parts at a heading, its text.

/** A piece's list of `name: what` as the rows of a table of two columns. */
internal fun labelled(piece: Segment.Piece): List<List<List<Span>>> = items(piece).map { item ->
    listOf(MarkdownBlocks.spans(item.substringBefore(": ")), MarkdownBlocks.spans(item.substringAfter(": ")))
}

/** What a piece holds, as one text. */
internal fun text(piece: Segment.Piece): String =
    piece.children.filterIsInstance<Segment.Text>().joinToString("\n") { it.text }.trim()

/** The items of a list that is all a piece holds. */
internal fun items(piece: Segment.Piece): List<String> = piece.children.filterIsInstance<Segment.Text>()
    .flatMap { it.text.lines() }
    .filter { it.trimStart().startsWith("- ") }
    .map { it.trimStart().removePrefix("- ").trim() }

/** A piece cut into parts at each line that starts with [marker]: the line's rest, and what follows it. */
internal fun parts(
    piece: Segment.Piece,
    marker: String
): List<Pair<String, List<Segment>>> {
    val parts = mutableListOf<Pair<String, MutableList<Segment>>>()
    piece.children.forEach { segment ->
        if (segment is Segment.Text) {
            cutText(segment.text, marker, parts)
        } else if (parts.isNotEmpty()) {
            parts.last().second += segment
        }
    }
    return parts
}

/** Cuts [text] at each line that starts with [marker] into [parts]: a new part at each, the lines into the last. */
internal fun cutText(
    text: String,
    marker: String,
    parts: MutableList<Pair<String, MutableList<Segment>>>
) {
    val lines = mutableListOf<String>()

    fun flush() {
        if (lines.isNotEmpty() && parts.isNotEmpty()) parts.last().second += Segment.Text(lines.joinToString("\n"))
        lines.clear()
    }
    text.lines().forEach { line ->
        if (line.startsWith(marker)) {
            flush()
            parts += line.removePrefix(marker) to mutableListOf()
        } else {
            lines += line
        }
    }
    flush()
}

internal fun tone(name: String): Tone = when (name.substringBefore(' ').lowercase()) {
    "success" -> Tone.Success
    "warning" -> Tone.Warning
    "danger" -> Tone.Danger
    "why" -> Tone.Why
    else -> Tone.Info
}
