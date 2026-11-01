package com.pantrixui.site.feature.components

import com.pantrixui.site.core.model.ComponentPage
import com.pantrixui.site.core.model.Span

/** A component's page as Markdown, for "Copy page": what it is, how it is imported, its examples and its API. */
internal object ComponentMarkdown {
    fun of(page: ComponentPage): String = buildString {
        appendLine("# ${page.card.name}")
        appendLine()
        appendLine(page.lead.markdown())
        appendLine()
        appendLine("```kotlin")
        appendLine("import ${page.packageName}.${page.card.function}")
        appendLine("```")
        if (page.features.isNotEmpty() || page.accessibility.isNotEmpty()) {
            appendLine()
            (page.features + page.accessibility).forEach { feature ->
                val label = feature.label?.let { "**$it.** " }.orEmpty()
                appendLine("- $label${feature.text.markdown()}")
            }
        }
        page.examples.forEach { example ->
            appendLine()
            appendLine("## ${example.title}")
            example.description.forEach { paragraph ->
                appendLine()
                appendLine(paragraph.markdown())
            }
            appendLine()
            appendLine("```kotlin")
            appendLine(example.code.lines.joinToString("\n") { line -> line.joinToString("") { it.text } })
            appendLine("```")
        }
        page.forms.forEach { form ->
            appendLine()
            appendLine("## ${page.card.function}(${form.label} …)")
            appendLine()
            form.parameters.forEach { parameter ->
                val default = parameter.default?.let { " = $it" }.orEmpty()
                appendLine("- `${parameter.name}: ${parameter.type}$default`: ${parameter.description.markdown()}")
            }
        }
    }.trimEnd() + "\n"

    /** [this] as Markdown: code between backticks, bold and italics marked, links as links. */
    private fun List<Span>.markdown(): String = joinToString("") { span ->
        when (span) {
            is Span.Code -> "`${span.text}`"

            is Span.Text -> when {
                span.strong -> "**${span.text}**"
                span.emphasis -> "*${span.text}*"
                else -> span.text
            }

            is Span.Link -> "[${span.text.markdown()}](${span.href})"
        }
    }
}
