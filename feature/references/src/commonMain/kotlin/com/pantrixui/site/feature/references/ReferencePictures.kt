package com.pantrixui.site.feature.references

import com.pantrixui.site.feature.references.generated.resources.Res
import com.pantrixui.site.feature.references.generated.resources.allDrawableResources
import org.jetbrains.compose.resources.DrawableResource

/**
 * The pictures of the apps' cards: each one's own screen, on a desktop, 16:10 as the design's card has it. They are
 * this module's drawables, named after the app's key in `content/references.json`, its dashes as underscores:
 * `reference_formatico.webp`, and `reference_formatico_dark.webp` for the dark theme. An app with no picture shows its
 * mark.
 */
internal object ReferencePictures {
    /** What the drawables' names start with. */
    const val PREFIX = "reference_"

    /** What a picture for the dark theme's name ends with. */
    const val DARK = "_dark"

    /** The picture of the app with [key] in the dark theme where [dark], or in the light one where it has no other. */
    fun of(
        key: String,
        dark: Boolean
    ): DrawableResource? {
        val name = PREFIX + key.replace('-', '_')
        val light = Res.allDrawableResources[name]
        return if (dark) Res.allDrawableResources[name + DARK] ?: light else light
    }

    /** The names of every app's pictures: `reference_formatico`, `reference_formatico_dark`. */
    val names: Set<String> get() = Res.allDrawableResources.keys.filter { it.startsWith(PREFIX) }.toSet()
}
