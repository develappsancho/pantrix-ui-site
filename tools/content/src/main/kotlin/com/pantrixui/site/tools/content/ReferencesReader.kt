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
 *
 * A text names how many components the library has as `{{components}}` ([SitePages.COMPONENTS]). An app that draws
 * with every one of them, the library's demo, says so with [ALL] in place of their list, and is given the library's
 * list. Every component an app names is one of the library's, or the build stops saying which is not.
 */
internal object ReferencesReader {
    /** The file, in the folder of the site's pages. */
    const val FILE = "references.json"

    /** What an app's `components` is where it draws with every one of the library's. */
    const val ALL = "all"

    /** The apps in [language], from [folder], with the components of the [library]; none where it has no such file. */
    fun read(
        folder: File,
        language: Language,
        library: List<String>
    ): References {
        val file = File(folder, FILE)
        if (!file.isFile) return References()
        return read(file.readText(), language, library)
    }

    /** The apps in [language], from the file's [text], with the components of the [library], by their composables. */
    fun read(
        text: String,
        language: Language,
        library: List<String>
    ): References {
        val said = Json.parseToJsonElement(SitePages.withCount(text)).inLanguage(language).withAll(library)
        val references = JSON.decodeFromJsonElement(References.serializer(), said)
        references.projects.forEach { project ->
            val unknown = project.components - library.toSet()
            check(unknown.isEmpty()) { "${project.key} in $FILE names components the library does not have: $unknown" }
            check(project.components.size == project.components.toSet().size) {
                "${project.key} in $FILE names a component twice"
            }
        }
        return references
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

    /** The file's apps with each one whose `components` is [ALL] given the [library]'s, in the alphabet's order. */
    private fun JsonElement.withAll(library: List<String>): JsonElement {
        val projects = (this as? JsonObject)?.get("projects") as? JsonArray ?: return this
        val every = JsonArray(library.sorted().map { JsonPrimitive(it) })
        val given = projects.map { project ->
            val components = (project as? JsonObject)?.get("components")
            if (project is JsonObject && components is JsonPrimitive && components.content == ALL) {
                JsonObject(project + ("components" to every))
            } else {
                project
            }
        }
        return JsonObject(this + ("projects" to JsonArray(given)))
    }

    /** Whether this object is one text in each language: its keys are the languages' codes, and each is a string. */
    private fun JsonObject.isSaid(): Boolean =
        keys == LANGUAGES && values.all { it is JsonPrimitive && it.isString }

    private val LANGUAGES = Language.entries.map { it.code }.toSet()

    /** A field the model does not know stops the build too: it would be a text the page never shows. */
    private val JSON = Json { ignoreUnknownKeys = false }
}
