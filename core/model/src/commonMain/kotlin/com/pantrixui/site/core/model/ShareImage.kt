package com.pantrixui.site.core.model

import kotlinx.serialization.Serializable

/**
 * The image a shared link of a page shows, as the static page's writer lists it and the share image's drawer draws it.
 *
 * @property file where the image goes, beside the web app: `og/en/docs/installation.png`.
 * @property title the page's title, large.
 * @property kicker what part of the site the page is in, over the title: `Components · Data`.
 * @property language the language the image is written in.
 */
@Serializable
data class ShareImage(
    val file: String,
    val title: String,
    val kicker: String,
    val language: Language
) {
    companion object {
        /** The share image's width and height: what a shared link's card shows, at twice its least. */
        const val WIDTH: Int = 1200
        const val HEIGHT: Int = 630

        /** The file of the share image of the page at [path] in [language]: `/` is `og/en/index.png`. */
        fun fileOf(
            path: String,
            language: Language
        ): String = "og/${language.code}/${path.trim('/').ifEmpty { "index" }}.png"
    }
}
