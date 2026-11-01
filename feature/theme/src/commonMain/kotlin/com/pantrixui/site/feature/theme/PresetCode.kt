@file:Suppress("TooManyFunctions") // A token of each kind is made by a small function of its own, as the code reads it.

package com.pantrixui.site.feature.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.pantrixui.foundation.token.color.PantrixColor
import com.pantrixui.foundation.token.color.PantrixColors
import com.pantrixui.site.core.model.Token
import com.pantrixui.site.core.model.TokenKind

/**
 * The Kotlin of [state]'s preset, a whole file of it that compiles as it is copied: the preset's object, then its
 * light and its dark colours with every role written out. A line at a time, each a run of tokens in their kinds.
 */
internal fun presetCode(state: ThemeUiState): List<List<Token>> {
    val lines = KotlinLines()
    lines.preset(state)
    listOf(false, true).forEach { dark ->
        lines.blank()
        lines.colors(state.objectName + if (dark) "Dark" else "Light", presetColors(Color(state.primary), dark))
    }
    return lines.all
}

/** The Kotlin of [state]'s preset as text: what is copied. */
internal fun presetText(state: ThemeUiState): String =
    presetCode(state).joinToString("\n") { line -> line.joinToString("") { it.text } }

/** Lines of Kotlin, built a run of tokens at a time. */
private class KotlinLines {
    val all = mutableListOf<List<Token>>()

    fun line(vararg tokens: Token) {
        all += tokens.toList()
    }

    fun line(tokens: List<Token>) {
        all += tokens
    }

    fun blank() {
        all += emptyList<Token>()
    }

    /** `object <Name>Preset : DesignPreset { … }`: its name, its colours, its type and its shapes. */
    fun preset(state: ThemeUiState) {
        val name = state.objectName
        line(kw("object"), plain(" "), type(name), plain(" : DesignPreset {"))
        line(plain("    "), kw("override val"), plain(" name = "), str("\"${state.name.replace("\"", "\\\"")}\""))
        line(
            plain("    "),
            kw("override fun"),
            plain(" colors(dark: Boolean) = "),
            kw("if"),
            plain(" (dark) ${name}Dark "),
            kw("else"),
            plain(" ${name}Light")
        )
        blank()
        typography(state.face)
        blank()
        shapes(state.corners.radii)
        line(plain("}"))
    }

    private fun typography(face: Face) {
        val head = listOf(plain("    "), ann("@Composable"), plain(" "), kw("override fun"))
        when (face) {
            Face.Bundled -> {
                line(head + plain(" typography() = rememberPantrixTypography()"))
            }

            Face.Own -> {
                line(head + plain(" typography() = rememberPantrixTypography("))
                line(
                    plain(
                        "        PantrixTypeface(PantrixFont(Res.font.brand_regular), " +
                            "PantrixFont(Res.font.brand_bold, FontWeight.Bold))"
                    )
                )
                line(plain("    )"))
            }
        }
    }

    private fun shapes(radii: Radii) {
        line(plain("    "), ann("@Composable"), plain(" "), kw("override fun"), plain(" shapes() = PantrixShapes("))
        line(corner("xs", radii.xs, ", ") + corner("sm", radii.sm, ","), indent = true)
        line(corner("md", radii.md, ", ") + corner("lg", radii.lg, ","), indent = true)
        line(corner("xl", radii.xl, ""), indent = true)
        line(plain("    )"))
    }

    private fun line(
        tokens: List<Token>,
        indent: Boolean
    ) = line((if (indent) listOf(plain("        ")) else emptyList()) + tokens)

    private fun corner(
        name: String,
        radius: Int,
        after: String
    ) = listOf(plain("$name = RoundedCornerShape("), num("$radius"), plain(".dp)$after"))

    /** `private val <name> = PantrixColors(…)`, every role of [colors] written out. */
    fun colors(
        name: String,
        colors: PantrixColors
    ) {
        line(kw("private val"), plain(" $name = PantrixColors("))
        roles(colors).forEach { (role, value) -> role(role, value) }
        grounds(colors).forEach { (role, value) -> line(plain("    $role = "), colour(value), plain(",")) }
        val chart = colors.chart.flatMapIndexed { index, value ->
            listOfNotNull(plain(", ").takeIf { index > 0 }, colour(value))
        }
        line(listOf(plain("    chart = listOf(")) + chart + plain(")"))
        line(plain(")"))
    }

    private fun role(
        name: String,
        value: PantrixColor
    ) {
        line(plain("    $name = PantrixColor("))
        line(plain("        color = "), colour(value.color), plain(", onColor = "), colour(value.onColor), plain(","))
        line(
            plain("        container = "),
            colour(value.container),
            plain(", onContainer = "),
            colour(value.onContainer)
        )
        line(plain("    ),"))
    }
}

/** The roles of [colors] with four colours each, by name. */
private fun roles(colors: PantrixColors): List<Pair<String, PantrixColor>> = listOf(
    "primary" to colors.primary,
    "success" to colors.success,
    "info" to colors.info,
    "warning" to colors.warning,
    "danger" to colors.danger,
    "neutral" to colors.neutral
)

/** The grounds, the text and the lines of [colors], by name, in the order the constructor takes them. */
private fun grounds(colors: PantrixColors): List<Pair<String, Color>> = listOf(
    "background" to colors.background,
    "surface" to colors.surface,
    "onSurface" to colors.onSurface,
    "disabledContainer" to colors.disabledContainer,
    "disabledContent" to colors.disabledContent,
    "textPrimary" to colors.textPrimary,
    "textSecondary" to colors.textSecondary,
    "textDisabled" to colors.textDisabled,
    "textInverse" to colors.textInverse,
    "border" to colors.border,
    "borderHover" to colors.borderHover,
    "borderStrong" to colors.borderStrong,
    "divider" to colors.divider,
    "focusRing" to colors.focusRing,
    "scrim" to colors.scrim
)

/** `Color(0xFF2264FF)`, as a number of the code. */
private fun colour(color: Color): Token {
    val code = (color.toArgb().toLong() and ARGB).toString(HEX_RADIX).padStart(ARGB_DIGITS, '0').uppercase()
    return Token("Color(0x$code)", TokenKind.Number)
}

private fun plain(text: String) = Token(text)

private fun kw(text: String) = Token(text, TokenKind.Keyword)

private fun type(text: String) = Token(text, TokenKind.Type)

private fun str(text: String) = Token(text, TokenKind.StringLiteral)

private fun num(text: String) = Token(text, TokenKind.Number)

private fun ann(text: String) = Token(text, TokenKind.Annotation)

private const val ARGB = 0xFFFFFFFFL
private const val ARGB_DIGITS = 8
private const val HEX_RADIX = 16
