package com.pantrixui.site.tools.prerender

import com.pantrixui.site.core.model.Language

/**
 * The few words the static pages write that are neither the content's nor the frame's: the names of a component
 * page's parts and of a parameter table's columns.
 *
 * @property examples the heading of a component's examples.
 * @property accessibility the heading of what a component does for a screen reader and a keyboard.
 * @property name the column of a parameter's name.
 * @property type the column of its type.
 * @property default the column of what it is when it is not given.
 * @property what the column of what it is for.
 * @property unreleased the name of the changelog's version that is not out yet.
 * @property otherLanguage the link to the page in the other language, in that language.
 */
internal data class StaticWords(
    val examples: String,
    val accessibility: String,
    val name: String,
    val type: String,
    val default: String,
    val what: String,
    val unreleased: String,
    val otherLanguage: String
) {
    companion object {
        private val English = StaticWords(
            examples = "Examples",
            accessibility = "Accessibility",
            name = "Parameter",
            type = "Type",
            default = "Default",
            what = "What it is",
            unreleased = "Unreleased",
            otherLanguage = "Türkçe"
        )

        private val Turkish = StaticWords(
            examples = "Örnekler",
            accessibility = "Erişilebilirlik",
            name = "Parametre",
            type = "Tür",
            default = "Varsayılan",
            what = "Ne olduğu",
            unreleased = "Yayımlanmadı",
            otherLanguage = "English"
        )

        /** The words in [language]. */
        fun of(language: Language): StaticWords = when (language) {
            Language.English -> English
            Language.Turkish -> Turkish
        }
    }
}
