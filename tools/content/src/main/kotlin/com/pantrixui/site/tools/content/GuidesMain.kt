package com.pantrixui.site.tools.content

import com.pantrixui.site.core.model.GuideCatalogue
import com.pantrixui.site.core.model.Language
import java.io.File

/**
 * Writes the site's guide pages, `<language>/docs/guides/<key>.md` under `--out`, from the library's guides in
 * `--docs`, the `com.pantrixui:mcp` jar ([GuidePages]): every guide of the site's catalogue in both languages, but
 * those the site writes by hand ([HAND_WRITTEN]). The pages are the site's sources and are committed with it: this
 * runs by hand, when the library's guides change (`./gradlew :core:content:writeGuidePages`), not at every build.
 */
fun main(args: Array<String>) {
    val options = args.toList().chunked(2).associate { it.first() to it.last() }
    val documents = LibraryDocuments(File(options.getValue("--docs")))
    val out = File(options.getValue("--out"))
    val texts = Language.entries.associateWith(documents::guides)
    val english = texts.getValue(Language.English)
    val pages = GuidePages(GuideCatalogue.entries.map { it.key }.toSet())
    val written = GuideCatalogue.entries.filter { it.key !in HAND_WRITTEN }.onEach { entry ->
        Language.entries.forEach { language ->
            val text = texts.getValue(language)[entry.key]
                ?: error("The library has no guide ${entry.key} in ${language.code}")
            val page = pages.page(
                key = entry.key,
                language = language,
                text = text,
                english = english.getValue(entry.key),
                title = entry.title.of(language)
            )
            File(out, "${language.code}/docs/guides/${entry.key}.md").apply { parentFile.mkdirs() }.writeText(page)
        }
    }
    println("Wrote ${written.size} guides, in both languages, to $out")
}

/** The guides whose pages the site writes by hand, and that the library's text does not overwrite. */
internal val HAND_WRITTEN = setOf("keyboard-dismissal")
