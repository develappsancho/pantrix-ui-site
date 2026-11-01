package com.pantrixui.site.core.model

import kotlinx.serialization.Serializable

/**
 * A language the site is written in. English is the site's first language and has no prefix in an address; every
 * other language's pages sit under its [code]: `/tr/docs/installation`.
 *
 * @property code the language's ISO 639-1 code, as an address and the page's `lang` attribute write it.
 */
@Serializable
enum class Language(val code: String) {
    English("en"),
    Turkish("tr");

    companion object {
        /** The language an address or a system names by [code], or English for a code the site has no pages in. */
        fun of(code: String?): Language = entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: English
    }
}
