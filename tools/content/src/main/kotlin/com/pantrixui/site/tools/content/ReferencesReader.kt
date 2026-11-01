package com.pantrixui.site.tools.content

import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.References
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.io.File

/**
 * Reads the apps built with Pantrix UI from the repository's `content/references.json`, where both languages are in
 * one file: a text that is said in each language is an object of the two, `{ "en": …, "tr": … }`, and one that is the
 * same in both, a name or an address, is the text alone. Each language is read as [References], its texts the ones in
 * that language.
 */
internal object ReferencesReader {
    /** The file, in the folder of the site's pages. */
    const val FILE = "references.json"

    /** The apps in [language], from [folder]; none where the folder has no such file. */
    fun read(
        folder: File,
        language: Language
    ): References {
        val file = File(folder, FILE)
        if (!file.isFile) return References()
        return read(file.readText(), language)
    }

    /** The apps in [language], from the file's [text]. */
    fun read(
        text: String,
        language: Language
    ): References {
        val said = Json.parseToJsonElement(text).inLanguage(language)
        return JSON.decodeFromJsonElement(References.serializer(), said)
    }

    /**
     * This element with each text that is said in each language in [language] alone. A text missing in a language is
     * no such text, and the model's reading of it stops the build: a page in one language would show nothing where
     * the other shows the text.
     */
    private fun JsonElement.inLanguage(language: Language): JsonElement = when (this) {
        is JsonArray -> JsonArray(map { it.inLanguage(language) })

        is JsonObject -> if (isSaid()) {
            getValue(language.code)
        } else {
            JsonObject(
                mapValues {
                    it.value.inLanguage(language)
                }
            )
        }

        is JsonPrimitive -> this
    }

    /** Whether this object is one text in each language: its keys are the languages' codes, and each is a string. */
    private fun JsonObject.isSaid(): Boolean =
        keys == LANGUAGES && values.all { it is JsonPrimitive && it.isString }

    private val LANGUAGES = Language.entries.map { it.code }.toSet()

    /** A field the model does not know stops the build too: it would be a text the page never shows. */
    private val JSON = Json { ignoreUnknownKeys = false }
}
