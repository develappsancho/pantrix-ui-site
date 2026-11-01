package com.pantrixui.site.tools.content

import com.pantrixui.site.core.model.Language
import java.io.File

/**
 * The examples the site writes itself, in place of the demo's, for a component its page shows better with examples of
 * its own: `content/examples/<language>/<key>.md`, the key that of the component's page. A file is written as a page
 * of the library's documents writes its examples: a title, then under `## Examples` each example's `###` title, what
 * it says and its code. Each file is there in both languages, with as many examples in each, or the build stops
 * saying which is not.
 */
internal object SiteExamples {
    /** The folder of the examples in the site's `content`. */
    const val FOLDER = "examples"

    /** The examples under [folder], each language's by the key of the component's page; none where it is not. */
    fun read(folder: File): Map<Language, Map<String, LibraryPage>> {
        val read = Language.entries.associateWith { language ->
            File(folder, language.code).listFiles { file -> file.extension == "md" }.orEmpty()
                .associate { file -> file.nameWithoutExtension to LibraryPage.read(file.readText()) }
        }
        val english = read.getValue(Language.English)
        read.forEach { (language, pages) ->
            check(pages.keys == english.keys) {
                "$FOLDER/${language.code} has examples of ${pages.keys.sorted()}, and the English of " +
                    "${english.keys.sorted()}"
            }
            pages.forEach { (key, page) ->
                val count = english.getValue(key).examples.size
                check(page.examples.size == count) {
                    "$FOLDER/${language.code}/$key.md has ${page.examples.size} examples, and the English $count"
                }
            }
        }
        return read
    }
}
