package com.pantrixui.site.tools.prerender

import com.pantrixui.site.core.model.ContentFiles
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.ShareImage
import com.pantrixui.site.core.routing.SiteRoutes
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import java.io.File

/**
 * Writes the site's static pages into `--out`, the web app's folder: each page of `--content` (the content tool's
 * output) in both languages, from the app's `index.html` at `--template`, with `--site` as the site's address. The
 * landing pages take their words from `--landing`, as `:tools:render` read them from the app, where it is given; the
 * list of the pages' share images goes to `--share`, for `:tools:render` to draw. The library's documents for coding
 * assistants in `--docs`, the `com.pantrixui:mcp` jar, are written at the site's root ([LlmsFiles]).
 */
fun main(args: Array<String>) {
    val options = args.toList().chunked(2).associate { it.first() to it.last() }
    val content = File(options.getValue("--content"))
    val out = File(options.getValue("--out"))
    val site = options.getValue("--site").trimEnd('/')
    val landing = options["--landing"]?.let { File(it) }?.takeIf { it.isFile }?.let(::landingWords).orEmpty()
    val document = StaticDocument(File(options.getValue("--template")).readText(), site)
    val pages = Language.entries.associateWith { language ->
        val words = landing[language.code].orEmpty().mapKeys { (path, _) -> SiteRoutes.routeOf(path) }
        StaticPages(content, language, words).all()
    }
    val written = StaticSite(pages, document, site).write(out)
    options["--docs"]?.let { LlmsFiles(File(it), site).write(out) }
    options["--share"]?.let { File(it) }?.apply { parentFile.mkdirs() }?.writeText(
        ContentFiles.json.encodeToString(
            ListSerializer(ShareImage.serializer()),
            pages.values.flatten().map { it.shareImage }
        )
    )
    println("Wrote $written static pages, the 404 page, sitemap.xml, robots.txt and llms.txt to $out")
}

/** The landing pages' words: each language's code to each page's path to its words in HTML. */
private fun landingWords(file: File): Map<String, Map<String, String>> = ContentFiles.json.decodeFromString(
    MapSerializer(String.serializer(), MapSerializer(String.serializer(), String.serializer())),
    file.readText()
)
