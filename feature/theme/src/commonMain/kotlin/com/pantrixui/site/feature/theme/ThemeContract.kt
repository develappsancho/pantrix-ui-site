package com.pantrixui.site.feature.theme

import com.pantrixui.foundation.platform.PlatformType
import com.pantrixui.foundation.token.density.PantrixDensity
import com.pantrixui.site.core.base.UiAction
import com.pantrixui.site.core.base.UiEffect
import com.pantrixui.site.core.base.UiState

/**
 * The theme builder, as its screen draws it: the preset being made, and how its preview is shown.
 *
 * @property name what the preset is called: its object is the name's letters and digits and `Preset`.
 * @property primary the brand's colour, as `0xFFRRGGBB`.
 * @property hexInput what the colour's field holds, which may be a code half typed.
 * @property corners how round the preset's shapes are.
 * @property face the typeface the preset sets its type in.
 * @property density the density the preset is shown in, which is the theme's to choose and not the preset's.
 * @property platform the platform whose conventions the preview is drawn in.
 * @property dark whether the preview shows the preset's dark colours.
 * @property copied whether the Kotlin was just copied: its button says so for a moment.
 * @property codeOpen whether a phone's sheet of the Kotlin is open.
 */
data class ThemeUiState(
    val name: String = DEFAULT_NAME,
    val primary: Long = DEFAULT_PRIMARY,
    val hexInput: String = hexOf(DEFAULT_PRIMARY),
    val corners: Corners = Corners.Rounded,
    val face: Face = Face.Bundled,
    val density: PantrixDensity = PantrixDensity.Comfortable,
    val platform: PlatformType = PlatformType.Android,
    val dark: Boolean = false,
    val copied: Boolean = false,
    val codeOpen: Boolean = false
) : UiState {
    /** The preset's object, as the Kotlin names it: `ShopeersPreset`, `BrandPreset` for a name of no letters. */
    val objectName: String
        get() = name.filter { it.isLetterOrDigit() && it.code < ASCII_END }.ifEmpty { "Brand" } + "Preset"

    /** The file the Kotlin is: `ShopeersPreset.kt`. */
    val fileName: String
        get() = "$objectName.kt"

    private companion object {
        /** The first code past ASCII: a Kotlin name keeps to letters and digits a file system and a reader share. */
        const val ASCII_END = 128
    }
}

/**
 * How round a preset's shapes are, from its smallest to its largest.
 *
 * @property radii the radius of each of its roundings.
 */
enum class Corners(val radii: Radii) {
    /** Pantrix's own scale. */
    Sharp(SHARP),

    /** Shopeers', the site's own. */
    Rounded(ROUNDED),

    /** Rounder than both. */
    Soft(SOFT)
}

/**
 * The radii of a preset's roundings, in dp.
 *
 * @property xs the smallest.
 * @property sm a small control's.
 * @property md a button's and a field's.
 * @property lg a card's.
 * @property xl a sheet's and a dialog's.
 */
class Radii(
    val xs: Int,
    val sm: Int,
    val md: Int,
    val lg: Int,
    val xl: Int
) {
    /** The five, from the smallest. */
    val all: List<Int>
        get() = listOf(xs, sm, md, lg, xl)
}

private val SHARP = Radii(xs = 2, sm = 4, md = 8, lg = 12, xl = 16)
private val ROUNDED = Radii(xs = 6, sm = 8, md = 12, lg = 16, xl = 20)
private val SOFT = Radii(xs = 8, sm = 12, md = 16, lg = 20, xl = 28)

/** The typeface a preset sets its type in. */
enum class Face {
    /** Plus Jakarta Sans, which Pantrix carries. */
    Bundled,

    /** The brand's own, from its font files. */
    Own
}

/** What the user does on the theme builder. */
sealed interface ThemeAction : UiAction {
    /** Calls the preset [name]. */
    data class Name(val name: String) : ThemeAction

    /** Types [text] in the colour's field: a whole code, `#RRGGBB`, makes it the brand's colour. */
    data class Hex(val text: String) : ThemeAction

    /** Makes [argb] the brand's colour. */
    data class PickColour(val argb: Long) : ThemeAction

    /** Rounds the preset's shapes as [corners] does. */
    data class PickCorners(val corners: Corners) : ThemeAction

    /** Sets its type in [face]. */
    data class PickFace(val face: Face) : ThemeAction

    /** Shows the preview in [density]. */
    data class PickDensity(val density: PantrixDensity) : ThemeAction

    /** Draws the preview in [type]'s conventions. */
    data class PickPlatform(val type: PlatformType) : ThemeAction

    /** Shows the preset's dark colours, or its light ones. */
    data class ShowDark(val dark: Boolean) : ThemeAction

    /** Starts again from Shopeers. */
    data object Reset : ThemeAction

    /** Copies the Kotlin. */
    data object CopyKotlin : ThemeAction

    /** Tells the model whether the platform took the Kotlin: [done] when it did. */
    data class Copied(val done: Boolean) : ThemeAction

    /** Hands the reader the Kotlin as a file, to keep where they choose. */
    data object Download : ThemeAction

    /** Opens a phone's sheet of the Kotlin, or closes it. */
    data class ShowCode(val open: Boolean) : ThemeAction
}

/** What the theme builder asks of the platform. */
sealed interface ThemeEffect : UiEffect {
    /** Puts [text] on the clipboard. */
    data class Copy(val text: String) : ThemeEffect

    /** Gives the reader [text] as a file named [name], in the platform's window for saving a file. */
    data class Save(
        val name: String,
        val text: String
    ) : ThemeEffect
}

/** The preset the builder starts from: Shopeers, the site's own. */
internal const val DEFAULT_NAME = "Shopeers"
internal const val DEFAULT_PRIMARY = 0xFF2264FF
