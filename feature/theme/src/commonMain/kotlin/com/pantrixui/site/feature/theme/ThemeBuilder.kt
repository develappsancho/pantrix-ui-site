package com.pantrixui.site.feature.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.pantrixui.foundation.theme.preset.DesignPreset
import com.pantrixui.foundation.token.color.PantrixColor
import com.pantrixui.foundation.token.color.PantrixColors
import com.pantrixui.foundation.token.color.contrastRatio
import com.pantrixui.foundation.token.shape.PantrixShapes
import com.pantrixui.foundation.token.typography.PantrixTypography
import com.pantrixui.site.core.designsystem.ShopeersPreset
import kotlin.math.roundToInt

/**
 * The preset the builder makes of [state], as a theme takes it: Shopeers' colours with the brand's in its primary
 * role and its focus ring, the corners chosen, and the type the site wears. A typeface of the brand's own is the
 * brand's app's to load: the preview keeps Pantrix's.
 */
internal class BuiltPreset(private val state: ThemeUiState) : DesignPreset {
    override val name: String = state.name

    override fun colors(dark: Boolean): PantrixColors = presetColors(Color(state.primary), dark)

    @Composable
    override fun typography(): PantrixTypography = ShopeersPreset.typography()

    @Composable
    override fun shapes(): PantrixShapes = shapesOf(state.corners)
}

/** Shopeers' colours with [primary]'s role and focus ring, in light or [dark]. */
internal fun presetColors(
    primary: Color,
    dark: Boolean
): PantrixColors = ShopeersPreset.colors(
    dark
).copy(primary = primaryRole(primary, dark), focusRing = textShade(primary, dark))

/**
 * The primary role the brand's colour makes: the colour itself in light, a lighter one in dark; written on in white
 * or near black, whichever reads better; its container a pale wash of it, and what is written there a deep shade.
 */
internal fun primaryRole(
    primary: Color,
    dark: Boolean
): PantrixColor {
    val color = if (dark) mix(primary, Color.White, DARK_LIFT) else primary
    return PantrixColor(
        color = color,
        onColor = onColorOf(color),
        container = if (dark) {
            mix(
                primary,
                DARK_SURFACE,
                DARK_CONTAINER
            )
        } else {
            mix(primary, Color.White, LIGHT_CONTAINER)
        },
        onContainer = if (dark) {
            mix(
                primary,
                Color.White,
                DARK_ON_CONTAINER
            )
        } else {
            mix(primary, NEAR_BLACK, LIGHT_ON_CONTAINER)
        }
    )
}

/** White or near black on [color]: whichever reads better. */
internal fun onColorOf(color: Color): Color =
    if (contrastRatio(Color.White, color) >= contrastRatio(NEAR_BLACK, color)) Color.White else NEAR_BLACK

/**
 * The shade of [primary] its links and its focus ring are drawn in: in light the colour darkened a step at a time
 * until text in it reads on white, in dark a paler one.
 */
internal fun textShade(
    primary: Color,
    dark: Boolean
): Color {
    if (dark) return mix(primary, Color.White, DARK_TEXT)
    var shade = primary
    repeat(DARKENING_STEPS) {
        if (contrastRatio(shade, Color.White) < TEXT_CONTRAST) shade = mix(shade, NEAR_BLACK, DARKENING)
    }
    return shade
}

/** [a] [t] of the way to [b], each channel of the sRGB code on its own, as the design mixes colours. */
internal fun mix(
    a: Color,
    b: Color,
    t: Float
): Color {
    val x = a.toArgb()
    val y = b.toArgb()

    fun channel(shift: Int): Int {
        val from = (x shr shift) and CHANNEL
        val to = (y shr shift) and CHANNEL
        return (from + (to - from) * t).roundToInt().coerceIn(0, CHANNEL)
    }
    return Color(red = channel(RED), green = channel(GREEN), blue = channel(BLUE), alpha = CHANNEL)
}

/** The shapes [corners] rounds to. */
internal fun shapesOf(corners: Corners): PantrixShapes {
    val radii = corners.radii
    return PantrixShapes(
        xs = RoundedCornerShape(radii.xs.dp),
        sm = RoundedCornerShape(radii.sm.dp),
        md = RoundedCornerShape(radii.md.dp),
        lg = RoundedCornerShape(radii.lg.dp),
        xl = RoundedCornerShape(radii.xl.dp)
    )
}

/** [argb]'s code, `#2264FF`. */
internal fun hexOf(argb: Long): String = "#" + (argb and RGB).toString(HEX).padStart(HEX_DIGITS, '0').uppercase()

/** The colour [text] codes, `#2264FF` or `2264ff`, opaque; null for anything else. */
internal fun parseHex(text: String): Long? {
    val digits = text.trim().removePrefix("#")
    if (digits.length != HEX_DIGITS || digits.any { it.digitToIntOrNull(HEX) == null }) return null
    return OPAQUE or digits.toLong(HEX)
}

/** The near black the brand writes in, and its dark surface. */
internal val NEAR_BLACK = Color(0xFF05070A)
private val DARK_SURFACE = Color(0xFF151A26)

/** How the design works one colour into a role: the shares of white, black and the surface it mixes in. */
private const val DARK_LIFT = 0.25f
private const val DARK_CONTAINER = 0.78f
private const val DARK_ON_CONTAINER = 0.7f
private const val DARK_TEXT = 0.45f
private const val LIGHT_CONTAINER = 0.9f
private const val LIGHT_ON_CONTAINER = 0.45f
private const val DARKENING = 0.12f
private const val DARKENING_STEPS = 8

/** What text is read at. */
internal const val TEXT_CONTRAST = 4.5f

private const val CHANNEL = 0xFF
private const val RED = 16
private const val GREEN = 8
private const val BLUE = 0
private const val RGB = 0xFFFFFFL
private const val OPAQUE = 0xFF000000L
private const val HEX = 16
private const val HEX_DIGITS = 6
