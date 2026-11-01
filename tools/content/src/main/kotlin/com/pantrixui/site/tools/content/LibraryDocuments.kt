package com.pantrixui.site.tools.content

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.util.zip.ZipFile

/**
 * The library's documents for coding assistants, as the `com.pantrixui:mcp` jar carries them under `pantrix-ui/`.
 * The site reads the changelog from them, in English and in Turkish, so that what it shows is what the library wrote
 * at its last build.
 *
 * @param jar the jar.
 */
internal class LibraryDocuments(private val jar: File) {
    /** The version the documents were written for, without `-SNAPSHOT`: the one the changelog's "Unreleased" is. */
    fun version(): String {
        val library = Json.parseToJsonElement(text(LIBRARY)).jsonObject.getValue("library").jsonObject
        return library.getValue("version").jsonPrimitive.content.removeSuffix("-SNAPSHOT")
    }

    /** The text of the document at [path], `CHANGELOG.md`, which the jar has. */
    fun text(path: String): String = ZipFile(jar).use { zip ->
        val entry = zip.getEntry("$ROOT/$path") ?: error("${jar.name} has no $ROOT/$path")
        zip.getInputStream(entry).readBytes().decodeToString()
    }

    companion object {
        /** The folder of the documents in the jar. */
        const val ROOT = "pantrix-ui"

        /** The changelog in English and in Turkish. */
        const val ENGLISH_CHANGELOG = "CHANGELOG.md"
        const val TURKISH_CHANGELOG = "tr/CHANGELOG.md"

        /** What the library is, its version among it. */
        const val LIBRARY = "pantrix-ui.json"
    }
}
