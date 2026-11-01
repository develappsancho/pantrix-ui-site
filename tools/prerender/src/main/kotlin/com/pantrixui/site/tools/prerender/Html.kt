package com.pantrixui.site.tools.prerender

import com.pantrixui.site.core.model.Block
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Span
import com.pantrixui.site.core.routing.SiteRoutes

/**
 * A page's blocks and spans as plain HTML, for the static page a browser without Wasm and a search engine read: the
 * same words in the same order, with headings, lists, tables, code and links, and none of the site's look.
 *
 * @param language the page's language: a link to another page of the site leads to its address in this language.
 */
internal class Html(private val language: Language) {
    /** [blocks] one after another. */
    fun blocks(blocks: List<Block>): String = blocks.joinToString("\n") { block(it) }

    /** [block] as HTML. */
    fun block(block: Block): String = when (block) {
        is Block.Heading -> heading(block.level, spans(block.text), block.anchor)

        is Block.Paragraph -> "<p>${spans(block.text)}</p>"

        is Block.Code -> code(block)

        is Block.ItemList -> list(block.ordered, block.items.map { blocks(it) })

        is Block.Table -> table(block)

        is Block.Callout -> "<aside>${blocks(block.blocks)}</aside>"

        is Block.Tabs -> block.tabs.joinToString("\n") { tab ->
            "<p><strong>${text(tab.label)}</strong></p>\n${blocks(tab.blocks)}"
        }

        is Block.Steps -> list(
            ordered = true,
            block.steps.map { step -> heading(PART, text(step.title), step.anchor) + blocks(step.blocks) }
        )

        is Block.Facts -> definitions(block.facts.map { text(it.label) to text(it.value) })

        is Block.Cards -> list(
            ordered = false,
            block.cards.map { card -> "${link(card.href, text(card.title))}: ${text(card.text)}" }
        )

        is Block.Rows -> definitions(block.rows.map { text(it.label) to spans(it.text) })

        is Block.Aside -> blocks(block.blocks)

        is Block.Sketch -> ""

        is Block.Note -> "<p><small>${spans(block.text)}</small></p>"

        is Block.Points -> definitions(block.points.map { spans(it.title) to spans(it.text) })

        is Block.Boxes -> block.boxes.joinToString("\n") { box ->
            "<section>${heading(DETAIL, spans(box.title))}${blocks(box.blocks)}</section>"
        }

        is Block.Checks -> block.groups.joinToString("\n") { group ->
            heading(DETAIL, text(group.title)) + list(ordered = false, group.items.map { spans(it) })
        }
    }

    /** [spans], a run of text, as HTML: bold, italics, code and links. */
    fun spans(spans: List<Span>): String = spans.joinToString("") { span ->
        when (span) {
            is Span.Text -> text(span.text)
                .let { if (span.emphasis) "<em>$it</em>" else it }
                .let { if (span.strong) "<strong>$it</strong>" else it }

            is Span.Code -> "<code>${text(span.text)}</code>"

            is Span.Link -> link(span.href, spans(span.text))
        }
    }

    /** A heading of [level], its words already HTML, with [anchor] for its id where it has one. */
    fun heading(
        level: Int,
        words: String,
        anchor: String? = null
    ): String = "<h$level${anchor?.let { " id=\"${text(it)}\"" }.orEmpty()}>$words</h$level>"

    /** A list of [items], each already HTML. */
    fun list(
        ordered: Boolean,
        items: List<String>
    ): String {
        val tag = if (ordered) "ol" else "ul"
        return "<$tag>" + items.joinToString("") { "<li>$it</li>" } + "</$tag>"
    }

    /** A link to [href], its words already HTML: a page of the site leads to its address in the page's language. */
    fun link(
        href: String,
        words: String
    ): String = "<a href=\"${text(addressOf(href))}\">$words</a>"

    /** [href] as this page links to it: a path of the site in the page's language, anything else as it is. */
    fun addressOf(href: String): String {
        if (!href.startsWith("/")) return href
        val anchor = href.substringAfter('#', "").let { if (it.isEmpty()) "" else "#$it" }
        return SiteRoutes.addressOf(SiteRoutes.routeOf(href.substringBefore('#')), language) + anchor
    }

    private fun code(block: Block.Code): String {
        val name = block.file?.let { "<p><code>${text(it)}</code></p>\n" }.orEmpty()
        val lines = block.lines.joinToString("\n") { line -> line.joinToString("") { text(it.text) } }
        return "$name<pre><code class=\"language-${text(block.language)}\">$lines</code></pre>"
    }

    private fun table(table: Block.Table): String {
        val caption = table.caption?.let { "<caption>${text(it)}</caption>" }.orEmpty()
        val head = table.header?.let { header ->
            "<thead><tr>" + header.joinToString("") { "<th>${spans(it)}</th>" } + "</tr></thead>"
        }.orEmpty()
        val rows = table.rows.joinToString("") { row ->
            "<tr>" + row.joinToString("") { "<td>${spans(it)}</td>" } + "</tr>"
        }
        return "<table>$caption$head<tbody>$rows</tbody></table>"
    }

    private fun definitions(pairs: List<Pair<String, String>>): String =
        "<dl>" + pairs.joinToString("") { (term, said) -> "<dt>$term</dt><dd>$said</dd>" } + "</dl>"

    companion object {
        /** The heading levels the static pages write: the page's title, a section, a part of one, a detail of that. */
        const val TITLE = 1
        const val SECTION = 2
        const val PART = 3
        const val DETAIL = 4

        /** [text] with what HTML reads as markup written as text. */
        fun text(text: String): String = buildString(text.length) {
            text.forEach { char ->
                when (char) {
                    '&' -> append("&amp;")
                    '<' -> append("&lt;")
                    '>' -> append("&gt;")
                    '"' -> append("&quot;")
                    '\'' -> append("&#39;")
                    else -> append(char)
                }
            }
        }
    }
}
