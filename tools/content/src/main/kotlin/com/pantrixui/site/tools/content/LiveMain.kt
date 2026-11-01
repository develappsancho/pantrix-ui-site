package com.pantrixui.site.tools.content

import com.pantrixui.site.core.model.Language
import java.io.File

/**
 * Writes the demo's examples the site runs, as Kotlin under `--out`, from the library's documents in `--docs`, the
 * `com.pantrixui:mcp` jar: each component's examples whose code calls nothing of the demo app itself, in both
 * languages, and their index ([LiveExampleWriter]).
 */
fun main(args: Array<String>) {
    val options = args.toList().chunked(2).associate { it.first() to it.last() }
    val documents = LibraryDocuments(File(options.getValue("--docs")))
    val out = File(options.getValue("--out"))
    val reader = ComponentReader(
        json = documents.text(LibraryDocuments.LIBRARY),
        pages = Language.entries.associateWith(documents::pages)
    )
    val written = LiveExampleWriter(out).write(Language.entries.associateWith(reader::runnable))
    println("Wrote $written of the demo's examples, in both languages, to $out")
}
