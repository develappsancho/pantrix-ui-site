package com.pantrixui.site.tools.prerender

import com.pantrixui.site.core.model.ComponentPage
import com.pantrixui.site.core.model.ComponentParameter
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Span

/**
 * A component's page in plain HTML, as its static page writes it: what the site's component page shows, without its
 * live examples, which are their code here.
 *
 * @param language the page's language.
 */
internal class ComponentHtml(private val language: Language) {
    private val html = Html(language)

    /** The page of a component: its lead, its import, its features, examples, API, styles and accessibility. */
    fun body(page: ComponentPage): String = buildString {
        val words = StaticWords.of(language)
        append(html.heading(Html.TITLE, Html.text(page.card.name)))
        append("\n<p>${html.spans(page.lead)}</p>")
        append("\n<pre><code>import ${Html.text(page.packageName)}.${Html.text(page.card.function)}</code></pre>")
        if (page.features.isNotEmpty()) append("\n" + features(page.features.map { it.label to it.text }))
        if (page.examples.isNotEmpty()) {
            append("\n" + html.heading(Html.SECTION, Html.text(words.examples), "examples"))
            page.examples.forEach { example ->
                append("\n" + html.heading(Html.PART, Html.text(example.title)))
                example.description.forEach { append("\n<p>${html.spans(it)}</p>") }
                append("\n" + html.block(example.code))
            }
        }
        if (page.forms.isNotEmpty()) {
            append("\n" + html.heading(Html.SECTION, "API", "api"))
            page.forms.forEach { form ->
                append("\n" + html.heading(Html.PART, Html.text(form.label)))
                if (form.description.isNotEmpty()) append("\n<p>${html.spans(form.description)}</p>")
                append("\n" + parameters(form.parameters, words))
            }
        }
        listOfNotNull(page.style, page.colors).forEach { type ->
            append("\n" + html.heading(Html.SECTION, Html.text(type.name)))
            if (type.description.isNotEmpty()) append("\n<p>${html.spans(type.description)}</p>")
            append("\n" + parameters(type.fields, words))
        }
        if (page.accessibility.isNotEmpty()) {
            append("\n" + html.heading(Html.SECTION, Html.text(words.accessibility), "accessibility"))
            append("\n" + features(page.accessibility.map { it.label to it.text }))
        }
    }

    private fun features(features: List<Pair<String?, List<Span>>>): String = html.list(
        ordered = false,
        features.map { (label, text) ->
            label?.let { "<strong>${Html.text(it)}.</strong> " }.orEmpty() + html.spans(text)
        }
    )

    private fun parameters(
        parameters: List<ComponentParameter>,
        words: StaticWords
    ): String {
        val rows = parameters.joinToString("") { parameter ->
            "<tr><td><code>${Html.text(parameter.name)}</code></td><td><code>${Html.text(parameter.type)}</code></td>" +
                "<td>${parameter.default?.let { "<code>${Html.text(it)}</code>" }.orEmpty()}</td>" +
                "<td>${html.spans(parameter.description)}</td></tr>"
        }
        val head = listOf(words.name, words.type, words.default, words.what)
            .joinToString("") { "<th>${Html.text(it)}</th>" }
        return "<table><thead><tr>$head</tr></thead><tbody>$rows</tbody></table>"
    }
}
