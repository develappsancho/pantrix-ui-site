package com.pantrixui.site.tools.content

import com.pantrixui.site.core.model.Block
import com.pantrixui.site.core.model.Span
import com.pantrixui.site.core.model.Tone
import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.flavours.gfm.GFMElementTypes
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMTokenTypes
import org.intellij.markdown.parser.CancellationToken
import org.intellij.markdown.parser.MarkdownParser

/**
 * GitHub-flavoured Markdown as the site's blocks: headings, paragraphs, code, lists, tables and quotes. A heading
 * may end in attributes, `## Add the repository {#repository toc="The repository"}`: its anchor and what the page's
 * contents call it.
 */
internal object MarkdownBlocks {
    private val parser = MarkdownParser(GFMFlavourDescriptor(), cancellationToken = CancellationToken.NonCancellable)

    /** A heading that starts with its number: `1. The Problem`. */
    private val NUMBERED = Regex("""^(\d+)\.(\s)""")

    /** The blocks of [markdown]. */
    fun blocks(markdown: String): List<Block> {
        val tree = parser.buildMarkdownTreeFromString(markdown as CharSequence)
        return tree.children.mapNotNull { block(it, markdown) }
    }

    /** The runs of text of one line of [markdown]: a list item's text, a cell's, a card's. */
    fun spans(markdown: String): List<Span> {
        val tree = parser.buildMarkdownTreeFromString(markdown as CharSequence)
        val paragraph = tree.children.firstOrNull { it.type == MarkdownElementTypes.PARAGRAPH } ?: return emptyList()
        return Inline(markdown).spans(paragraph.children)
    }

    private fun block(
        node: ASTNode,
        source: String
    ): Block? = when (node.type) {
        MarkdownElementTypes.ATX_1, MarkdownElementTypes.ATX_2, MarkdownElementTypes.ATX_3,
        MarkdownElementTypes.ATX_4, MarkdownElementTypes.ATX_5, MarkdownElementTypes.ATX_6 -> heading(node, source)

        MarkdownElementTypes.PARAGRAPH -> Block.Paragraph(Inline(source).spans(node.children))

        MarkdownElementTypes.CODE_FENCE -> code(node, source)

        MarkdownElementTypes.UNORDERED_LIST -> list(node, source, ordered = false)

        MarkdownElementTypes.ORDERED_LIST -> list(node, source, ordered = true)

        GFMElementTypes.TABLE -> table(node, source)

        MarkdownElementTypes.BLOCK_QUOTE -> Block.Callout(
            tone = Tone.Info,
            blocks = blocks(node.text(source).lines().joinToString("\n") { it.removePrefix(">").removePrefix(" ") })
        )

        else -> null
    }

    private fun heading(
        node: ASTNode,
        source: String
    ): Block.Heading {
        val level = node.type.toString().last().digitToInt()
        val content = node.children.firstOrNull { it.type == MarkdownTokenTypes.ATX_CONTENT }
        val heading = Heading.of(content?.text(source)?.trim().orEmpty())
        return Block.Heading(
            level = level,
            anchor = heading.anchor,
            text = headingSpans(heading.title),
            contentsTitle = heading.contentsTitle
        )
    }

    /**
     * The runs of a heading's [title]. A numbered one, "1. The Problem", read alone as a line would be a list: its
     * number is kept as text before the rest.
     */
    private fun headingSpans(title: String): List<Span> {
        val number = NUMBERED.find(title) ?: return spans(title)
        return (listOf(Span.Text(number.value)) + spans(title.substring(number.range.last + 1))).mergedText()
    }

    private fun code(
        node: ASTNode,
        source: String
    ): Block.Code {
        val info = node.children.firstOrNull {
            it.type == MarkdownTokenTypes.FENCE_LANG
        }?.text(source)?.trim().orEmpty()
        val language = info.substringBefore(' ').ifEmpty { "text" }
        val file = info.substringAfter(' ', missingDelimiterValue = "").trim().ifEmpty { null }
        val start = node.children.indexOfFirst { it.type == MarkdownTokenTypes.EOL }
        val end = node.children.indexOfLast { it.type == MarkdownTokenTypes.CODE_FENCE_END }
        val inside = node.children.subList(start + 1, if (end < 0) node.children.size else end)
        val code = inside.joinToString("") { it.text(source) }.removeSuffix("\n")
        return Block.Code(language = language, file = file, lines = Highlighter.lines(code, language))
    }

    private fun list(
        node: ASTNode,
        source: String,
        ordered: Boolean
    ): Block.ItemList = Block.ItemList(
        ordered = ordered,
        items = node.children.filter { it.type == MarkdownElementTypes.LIST_ITEM }.map { item ->
            item.children.mapNotNull { child -> block(child, source) }
        }
    )

    private fun table(
        node: ASTNode,
        source: String
    ): Block.Table {
        fun cells(row: ASTNode) = row.children.filter { it.type == GFMTokenTypes.CELL }.map { cell ->
            spans(cell.text(source).trim())
        }
        val header = node.children.firstOrNull { it.type == GFMElementTypes.HEADER }?.let(::cells)
        val rows = node.children.filter { it.type == GFMElementTypes.ROW }.map(::cells)
        return Block.Table(header = header, rows = rows)
    }

    /** The runs of text of a run of inline nodes. */
    private class Inline(private val source: String) {
        private val spans = mutableListOf<Span>()

        fun spans(nodes: List<ASTNode>): List<Span> {
            nodes.forEach { add(it, strong = false, emphasis = false) }
            // Markdown reads a line break and the indent after it as one space, as a browser does.
            return spans.mergedText().map { span ->
                if (span is Span.Text) span.copy(text = span.text.replace(SPACES, " ")) else span
            }.trimmed()
        }

        private fun add(
            node: ASTNode,
            strong: Boolean,
            emphasis: Boolean
        ) {
            when (node.type) {
                MarkdownElementTypes.STRONG -> {
                    node.inner(MARKERS).forEach { add(it, true, emphasis) }
                }

                MarkdownElementTypes.EMPH -> {
                    node.inner(MARKERS).forEach { add(it, strong, true) }
                }

                MarkdownElementTypes.CODE_SPAN -> {
                    spans += Span.Code(
                        node.children.filter {
                            it.type != MarkdownTokenTypes.BACKTICK
                        }.joinToString("") { it.text(source) }
                    )
                }

                MarkdownElementTypes.INLINE_LINK -> {
                    link(node)
                }

                MarkdownElementTypes.AUTOLINK, GFMTokenTypes.GFM_AUTOLINK -> {
                    val url = node.text(source).trim('<', '>')
                    spans += Span.Link(listOf(Span.Text(url)), url)
                }

                MarkdownTokenTypes.EOL, MarkdownTokenTypes.HARD_LINE_BREAK -> {
                    text(" ", strong, emphasis)
                }

                else -> {
                    if (node.children.isEmpty()) {
                        text(node.text(source), strong, emphasis)
                    } else {
                        node.children.forEach { add(it, strong, emphasis) }
                    }
                }
            }
        }

        private fun link(node: ASTNode) {
            val label = node.children.firstOrNull { it.type == MarkdownElementTypes.LINK_TEXT }
            val destination = node.children.firstOrNull { it.type == MarkdownElementTypes.LINK_DESTINATION }
            val text = Inline(
                source
            ).spans(label?.inner(setOf(MarkdownTokenTypes.LBRACKET, MarkdownTokenTypes.RBRACKET)) ?: emptyList())
            spans += Span.Link(text, destination?.text(source).orEmpty())
        }

        private fun text(
            text: String,
            strong: Boolean,
            emphasis: Boolean
        ) {
            spans += Span.Text(text, strong = strong, emphasis = emphasis)
        }

        private fun ASTNode.inner(markers: Set<IElementType>): List<ASTNode> {
            val first = children.indexOfFirst { it.type !in markers }
            val last = children.indexOfLast { it.type !in markers }
            return if (first < 0) emptyList() else children.subList(first, last + 1)
        }

        private fun ASTNode.text(source: String): String = getTextInNode(source).toString()

        companion object {
            val MARKERS = setOf(MarkdownTokenTypes.EMPH)
            val SPACES = Regex("""\s{2,}""")
        }
    }

    private fun ASTNode.text(source: String): String = getTextInNode(source).toString()
}

/** Plain runs of one kind as one, so that a sentence is not cut where the parser cut it. */
internal fun List<Span>.mergedText(): List<Span> = fold(mutableListOf()) { merged, span ->
    val last = merged.lastOrNull()
    if (span is Span.Text && last is Span.Text && last.sameKindAs(span)) {
        merged[merged.lastIndex] = last.copy(text = last.text + span.text)
    } else {
        merged += span
    }
    merged
}

/** Whether [other] is set as [this] is: as strong and as emphasised. */
private fun Span.Text.sameKindAs(other: Span.Text): Boolean = strong == other.strong && emphasis == other.emphasis

/** Runs without the white space at either end of the whole. */
private fun List<Span>.trimmed(): List<Span> {
    if (isEmpty()) return this
    val first = (first() as? Span.Text)?.let { it.copy(text = it.text.trimStart()) } ?: first()
    val list = listOf(first) + drop(1)
    val last = (list.last() as? Span.Text)?.let { it.copy(text = it.text.trimEnd()) } ?: list.last()
    return (list.dropLast(1) + last).filterNot { it is Span.Text && it.text.isEmpty() }
}

/**
 * A heading's title and the attributes it may end in: `Title {#anchor toc="Short" optional}`.
 *
 * @property title what the heading says.
 * @property anchor its id: given, or made from the title.
 * @property contentsTitle what the page's contents call it, if not its title.
 * @property optional whether it is marked as a step that may be left out.
 */
internal data class Heading(
    val title: String,
    val anchor: String,
    val contentsTitle: String?,
    val optional: Boolean
) {
    companion object {
        private val ATTRIBUTES = Regex("""\s*\{([^}]*)\}\s*$""")
        private val TOC = Regex("""toc="([^"]*)"""")

        fun of(text: String): Heading {
            val match = ATTRIBUTES.find(text)
            val title = if (match == null) text else text.substring(0, match.range.first)
            val attributes = match?.groupValues?.get(1).orEmpty()
            val anchor = attributes.split(' ').firstOrNull { it.startsWith("#") }?.drop(1) ?: slug(title)
            return Heading(
                title = title.trim(),
                anchor = anchor,
                contentsTitle = TOC.find(attributes)?.groupValues?.get(1),
                optional = attributes.split(' ').any { it == "optional" }
            )
        }

        /** An id made from [text]: small letters, digits and hyphens. */
        fun slug(text: String): String = text.lowercase()
            .replace(Regex("[`*_]"), "")
            .replace(Regex("[^a-z0-9ğüşıöç]+"), "-")
            .trim('-')
    }
}
