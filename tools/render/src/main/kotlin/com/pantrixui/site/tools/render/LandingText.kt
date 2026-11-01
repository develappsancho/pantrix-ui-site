package com.pantrixui.site.tools.render

import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import com.pantrixui.site.core.designsystem.content.CodeExample
import com.pantrixui.site.core.designsystem.content.LiveExample

/**
 * A page's words as the app shows them, read from its semantics, in HTML: its headings and its text in their order,
 * the first heading the page's own. What a reader acts on (a button, a tab, a switch, a field) and the parts where live
 * components run ([LiveExample]) are left out: their words are the controls', not the page's. So is what the page
 * hides from a screen reader, such as a picture drawn with words in it: the tree read here keeps it, a screen reader
 * does not.
 */
internal object LandingText {
    /** The words under [root], the app's root, in HTML. */
    fun of(root: SemanticsNode): String {
        val out = mutableListOf<String>()
        var titled = false

        fun walk(node: SemanticsNode) {
            val config = node.config
            val text = config.getOrNull(SemanticsProperties.Text).orEmpty()
            val words = text.joinToString(" ") { html(it) }.trim()
            val heading = config.contains(SemanticsProperties.Heading)
            when {
                config.contains(LiveExample) || config.contains(SemanticsProperties.Role) -> Unit

                config.isClearingSemantics -> Unit

                config.contains(SemanticsProperties.EditableText) -> Unit

                config.contains(CodeExample) -> codeOf(node).takeIf { it.isNotEmpty() }?.let { lines ->
                    out += "<pre><code>${lines.joinToString("\n") { escapedCode(it) }}</code></pre>"
                }

                text.isNotEmpty() && words.isEmpty() -> Unit

                text.isNotEmpty() -> out += when {
                    heading && !titled -> "<h1>$words</h1>".also { titled = true }
                    heading -> "<h2>$words</h2>"
                    else -> "<p>$words</p>"
                }

                else -> node.children.forEach(::walk)
            }
        }
        walk(root)
        return out.zipWithNext().filterNot { (before, after) -> before == after }.map { it.second }
            .let { rest -> (out.take(1) + rest).joinToString("\n") }
    }

    /** The lines of a block of code under [node], in their order: the tabs over it are not its lines. */
    private fun codeOf(node: SemanticsNode): List<String> {
        val text = node.config.getOrNull(SemanticsProperties.Text).orEmpty()
        return when {
            node.config.contains(SemanticsProperties.Role) -> emptyList()
            text.isNotEmpty() -> text.flatMap { it.text.lines() }
            else -> node.children.flatMap(::codeOf)
        }
    }

    /** [text] as HTML, a link of it as a link where it leads to an address. */
    private fun html(text: AnnotatedString): String {
        val links = text.getLinkAnnotations(0, text.length)
        if (links.isEmpty()) return escaped(text.text)
        val out = StringBuilder()
        var at = 0
        links.sortedBy { it.start }.forEach { range ->
            out.append(escaped(text.text.substring(at, range.start)))
            val words = escaped(text.text.substring(range.start, range.end))
            val url = (range.item as? LinkAnnotation.Url)?.url
            out.append(if (url != null) "<a href=\"${escaped(url)}\">$words</a>" else words)
            at = range.end
        }
        out.append(escaped(text.text.substring(at)))
        return out.toString()
    }

    /** [text] with what HTML reads as markup written as text, and its runs of spaces and lines as one space. */
    fun escaped(text: String): String = escapedCode(text.replace(Regex("""\s+"""), " "))

    /** [text] with what HTML reads as markup written as text, its spaces as they are. */
    private fun escapedCode(text: String): String = buildString {
        text.forEach { char ->
            when (char) {
                '&' -> append("&amp;")
                '<' -> append("&lt;")
                '>' -> append("&gt;")
                '"' -> append("&quot;")
                else -> append(char)
            }
        }
    }
}
