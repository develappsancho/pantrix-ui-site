package com.pantrixui.site.tools.content

import com.pantrixui.site.core.model.Block
import com.pantrixui.site.core.model.ChangeEntry
import com.pantrixui.site.core.model.ChangeGroup
import com.pantrixui.site.core.model.ChangeKind
import com.pantrixui.site.core.model.ChangeSection
import com.pantrixui.site.core.model.Changelog
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Release
import com.pantrixui.site.core.model.Span

/**
 * The library's changelog as the site's [Changelog], from its document in Keep a Changelog's form: a title, a line
 * to the other language, a first paragraph, then a `## [version]` heading for each version, `###` for each kind of
 * change, `####` for each module and a list of lines under it, each with a bold lead.
 *
 * The modules are named in the changelog's language; a module's key and its artifacts come from the English
 * changelog, which [english] gives the Turkish one: the two have the same headings in the same order.
 *
 * @param language the language the changelog is written in.
 * @param english the English changelog, read first, for the Turkish one; null for the English one itself.
 */
internal class ChangelogReader(
    private val language: Language,
    private val english: Changelog? = null
) {
    /** The changelog [source] says. */
    fun read(source: String): Changelog {
        val lines = source.trimEnd().lines()
        val title = lines.first { it.startsWith("# ") }.removePrefix("# ").trim()
        val head = lines.drop(lines.indexOfFirst { it.startsWith("# ") } + 1)
            .dropWhile { it.isBlank() || it.startsWith(OTHER_LANGUAGE.getValue(language)) }
        val description = MarkdownBlocks.spans(head.takeWhile { it.isNotBlank() }.joinToString(" ") { it.trim() })
        val releases = cut(head, "## ").mapIndexed { index, (heading, body) ->
            release(heading, body, english?.releases?.getOrNull(index))
        }
        return Changelog(language, title, description, releases, source.trim())
    }

    private fun release(
        heading: String,
        body: List<String>,
        twin: Release?
    ): Release {
        val name = heading.substringAfter('[').substringBefore(']')
        val date = heading.substringAfter("] - ", missingDelimiterValue = "").trim().ifEmpty { null }
        val introLines = body.takeWhile { !it.startsWith("### ") }
        val intro = MarkdownBlocks.blocks(introLines.joinToString("\n"))
        val targets = intro.filterIsInstance<Block.ItemList>().firstOrNull()
        val afterTargets = intro.indexOfFirst { it === targets }.let { if (it < 0) intro.size else it + 1 }
        val sections = cut(body.drop(introLines.size), "### ").mapIndexed { index, (kindTitle, kindBody) ->
            val kind = kindOf(kindTitle)
            val twinGroups = twin?.sections?.getOrNull(index)?.groups
            ChangeSection(
                kind = kind,
                title = kindTitle.trim(),
                groups = cut(kindBody, "#### ").mapIndexed { groupIndex, (module, groupBody) ->
                    group(module.trim(), groupBody, twinGroups?.getOrNull(groupIndex))
                }
            )
        }
        return Release(
            version = name.takeUnless { it in UNRELEASED },
            date = date,
            intro = intro.take(afterTargets).filterNot { it === targets },
            targets = targets?.items.orEmpty().map { item ->
                item.filterIsInstance<Block.Paragraph>().flatMap { it.text }.trimmedEnd()
            },
            sections = sections,
            lead = intro.drop(afterTargets)
        )
    }

    private fun group(
        module: String,
        body: List<String>,
        twin: ChangeGroup?
    ): ChangeGroup {
        val key = twin?.key ?: module.lowercase().replace(' ', '-')
        val list = MarkdownBlocks.blocks(body.joinToString("\n")).filterIsInstance<Block.ItemList>().firstOrNull()
        return ChangeGroup(
            module = module,
            key = key,
            artifacts = ARTIFACTS[key].orEmpty(),
            entries = list?.items.orEmpty().map(::entry)
        )
    }

    /** A line of the changelog: its bold lead taken out of its first paragraph, and **Breaking:** before it. */
    private fun entry(blocks: List<Block>): ChangeEntry {
        val first = blocks.firstOrNull() as? Block.Paragraph ?: return ChangeEntry(blocks = blocks)
        var spans = first.text
        val breaking = spans.leadOrNull() in BREAKING
        if (breaking) spans = spans.drop(1).trimmedStart()
        val label = spans.leadOrNull()
        if (label != null) spans = spans.drop(1).trimmedStart()
        return ChangeEntry(
            label = label,
            blocks = listOf(Block.Paragraph(spans)) + blocks.drop(1),
            breaking = breaking
        )
    }

    private fun kindOf(title: String): ChangeKind {
        val name = title.trim().lowercase()
        return KINDS.entries.firstOrNull { (_, names) -> name in names }?.key
            ?: error("The changelog in ${language.code} has a kind of change it does not know: $title")
    }

    companion object {
        /** What the line that leads to the other language starts with, by the changelog's language. */
        private val OTHER_LANGUAGE = mapOf(Language.English to "Türkçesi:", Language.Turkish to "English:")

        /** What the version still to come is called in each language. */
        private val UNRELEASED = setOf("Unreleased", "Yayınlanmamış")

        /** What a breaking change's line starts with, in each language, without its colon. */
        private val BREAKING = setOf("Breaking", "Kırıcı")

        /** The kinds of change, by what each language calls them. */
        private val KINDS = mapOf(
            ChangeKind.Added to setOf("added", "eklenenler"),
            ChangeKind.Changed to setOf("changed", "değişenler"),
            ChangeKind.Deprecated to setOf("deprecated", "kullanımdan kalkacaklar"),
            ChangeKind.Removed to setOf("removed", "kaldırılanlar"),
            ChangeKind.Fixed to setOf("fixed", "düzeltilenler"),
            ChangeKind.Security to setOf("security", "güvenlik")
        )

        /** What each module of the changelog is published as, by its key. */
        private val ARTIFACTS = mapOf(
            "foundation" to listOf("com.pantrixui:foundation"),
            "components" to listOf("com.pantrixui:components"),
            "annotations" to listOf("com.pantrixui:annotations"),
            "material-3" to listOf("com.pantrixui:material3"),
            "testing" to listOf("com.pantrixui:testing"),
            "tooling" to listOf("com.pantrixui:detekt-rules", "com.pantrixui:mcp")
        )

        /** [lines] cut at each line that starts with [marker]: each such line's rest, and the lines up to the next. */
        private fun cut(
            lines: List<String>,
            marker: String
        ): List<Pair<String, List<String>>> {
            val parts = mutableListOf<Pair<String, MutableList<String>>>()
            lines.forEach { line ->
                if (line.startsWith(marker)) {
                    parts += line.removePrefix(marker) to mutableListOf()
                } else {
                    parts.lastOrNull()?.second?.add(line)
                }
            }
            return parts
        }

        /** The bold lead of a line, without its colon: "Theme" of "**Theme:** …". */
        private fun List<Span>.leadOrNull(): String? =
            (firstOrNull() as? Span.Text)?.takeIf { it.strong && it.text.trimEnd().endsWith(':') }
                ?.text?.trimEnd()?.removeSuffix(":")?.trim()

        private fun List<Span>.trimmedEnd(): List<Span> {
            val last = lastOrNull() as? Span.Text ?: return this
            val trimmed = last.text.trimEnd().removeSuffix(";").removeSuffix(".").trimEnd()
            return dropLast(1) + last.copy(text = trimmed)
        }
    }
}
