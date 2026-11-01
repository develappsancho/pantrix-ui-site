package com.pantrixui.site.feature.foundations

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.segmentbutton.PantrixSegmentButton
import com.pantrixui.components.elements.segmentbutton.SegmentOption
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.l10n.PantrixStrings
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.foundation.token.color.PantrixColor
import com.pantrixui.foundation.token.color.PantrixColors
import com.pantrixui.foundation.token.color.contrastRatio
import com.pantrixui.site.core.designsystem.PantrixPreset
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.FoundationSection
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Words

/**
 * The colours: the preset's six roles with the contrast of what is written on them, its grounds, text and lines with
 * their contrast on its surface, and the brand's palette. A choice of light and dark shows either of the preset's
 * sets, whichever theme the site is in.
 */
@Composable
internal fun ColourSection(
    state: FoundationsUiState,
    onAction: (FoundationsAction) -> Unit,
    modifier: Modifier
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val colors = PantrixPreset.colors(state.dark)
    Section(
        title = FoundationSection.Colour.title.of(language),
        modifier = modifier,
        aside = {
            PantrixSegmentButton(
                options = listOf(
                    SegmentOption(false, FoundationsSay.Light.of(language), tagKey = "light"),
                    SegmentOption(true, FoundationsSay.Dark.of(language), tagKey = "dark")
                ),
                selected = state.dark,
                onSelect = { onAction(FoundationsAction.ShowDark(it)) },
                fullWidth = false,
                tagKey = "foundationsColours"
            )
        }
    ) {
        if (layout == SiteLayout.Desktop) {
            Lead(FoundationsSay.ColourLeadStart.of(language), "PantrixColor", FoundationsSay.ColourLeadEnd.of(language))
        }
        Roles(colors)
        Grounds(colors, state.dark)
        Palette()
    }
}

/** The six roles: three to a row, or one to a row on a phone. */
@Composable
private fun Roles(colors: PantrixColors) {
    val roles = roleColours(colors)
    if (SiteTheme.layout == SiteLayout.Phone) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            roles.forEach { (name, role) -> RoleRow(name, role) }
        }
    } else {
        val gap = if (SiteTheme.layout == SiteLayout.Desktop) 14.dp else 12.dp
        Column(verticalArrangement = Arrangement.spacedBy(gap)) {
            roles.chunked(ROLES_A_ROW).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                    row.forEach { (name, role) -> RoleCard(name, role, Modifier.weight(1f)) }
                }
            }
        }
    }
}

/** A role's card: its colour with its name, its code and the contrast of what is written on it, over its container. */
@Composable
private fun RoleCard(
    name: String,
    role: PantrixColor,
    modifier: Modifier
) {
    val language = LocalLanguage.current
    val desktop = SiteTheme.layout == SiteLayout.Desktop
    val shape = RoundedCornerShape(16.dp)
    Column(modifier.clip(shape).border(1.dp, PantrixTheme.colors.border, shape)) {
        Column(
            Modifier
                .fillMaxWidth()
                .height(if (desktop) 96.dp else 88.dp)
                .background(role.color)
                .padding(horizontal = if (desktop) 16.dp else 14.dp, vertical = if (desktop) 14.dp else 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                PantrixText(
                    text = name,
                    style = strong(if (desktop) 16.sp else 15.sp),
                    color = role.onColor,
                    modifier = Modifier.weight(1f)
                )
                PantrixText(
                    text = role.color.hex(),
                    style = mono(if (desktop) 12.sp else 11.5.sp),
                    color = role.onColor
                )
            }
            Row {
                if (desktop) {
                    PantrixText(
                        text = "onColor ${role.onColor.hex()}",
                        style = strong(12.5.sp, FontWeight.Bold),
                        color = role.onColor,
                        modifier = Modifier.weight(1f)
                    )
                }
                PantrixText(
                    text = grade(contrastRatio(role.onColor, role.color), PantrixTheme.strings, language),
                    style = strong(if (desktop) 12.5.sp else 12.sp, FontWeight.Bold),
                    color = role.onColor
                )
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .background(role.container)
                .padding(horizontal = if (desktop) 16.dp else 14.dp, vertical = if (desktop) 10.dp else 9.dp)
        ) {
            val container = FoundationsSay.Container.of(language)
            PantrixText(
                text = if (desktop) "$container ${role.container.hex()}" else container,
                style = strong(if (desktop) 12.5.sp else 12.sp, FontWeight.Bold),
                color = role.onContainer,
                modifier = Modifier.weight(1f)
            )
            PantrixText(
                text = grade(contrastRatio(role.onContainer, role.container), PantrixTheme.strings, language),
                style = strong(if (desktop) 12.5.sp else 12.sp, FontWeight.Bold),
                color = role.onContainer
            )
        }
    }
}

/** A phone's role: its colour beside its container, each with the contrast of what is written on it. */
@Composable
private fun RoleRow(
    name: String,
    role: PantrixColor
) {
    val language = LocalLanguage.current
    val shape = RoundedCornerShape(14.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(shape)
            .border(1.dp, PantrixTheme.colors.border, shape)
    ) {
        Column(
            Modifier.weight(PHONE_COLOUR_SHARE).fillMaxHeight().background(role.color).padding(12.dp, 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PantrixText(text = name, style = strong(15.sp), color = role.onColor)
            Row {
                PantrixText(
                    text = role.color.hex(),
                    style = mono(11.5.sp),
                    color = role.onColor,
                    modifier = Modifier.weight(1f)
                )
                PantrixText(
                    text = grade(contrastRatio(role.onColor, role.color), PantrixTheme.strings, language),
                    style = strong(12.sp, FontWeight.Bold),
                    color = role.onColor
                )
            }
        }
        Column(
            Modifier.weight(1f).fillMaxHeight().background(role.container).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PantrixText(
                text = FoundationsSay.Container.of(language),
                style = strong(12.5.sp, FontWeight.Bold),
                color = role.onContainer
            )
            PantrixText(
                text = grade(contrastRatio(role.onContainer, role.container), PantrixTheme.strings, language),
                style = strong(12.sp, FontWeight.Bold),
                color = role.onContainer
            )
        }
    }
}

/** The grounds, the text and the lines: each colour, its contrast on the surface where it matters, and its job. */
@Composable
private fun Grounds(
    colors: PantrixColors,
    dark: Boolean
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val shape = RoundedCornerShape(14.dp)
    val mode = (if (dark) FoundationsSay.Dark else FoundationsSay.Light).of(language).lowercase()
    Column(Modifier.fillMaxWidth().clip(shape).border(1.dp, PantrixTheme.colors.border, shape)) {
        PantrixText(
            text = FoundationsSay.Grounds.of(language) + if (layout == SiteLayout.Phone) "" else ", $mode",
            style = strong(15.sp),
            color = PantrixTheme.colors.textPrimary,
            modifier = Modifier.fillMaxWidth().background(SiteTheme.colors.surfaceAlt).padding(16.dp, 12.dp)
        )
        Line()
        if (layout == SiteLayout.Desktop) {
            GroundRow(
                cells = listOf(
                    FoundationsSay.Role,
                    FoundationsSay.Colour,
                    FoundationsSay.OnSurface,
                    FoundationsSay.For
                ).map { words ->
                    {
                        PantrixText(
                            text = words.of(language),
                            style = strong(12.sp),
                            color = SiteTheme.colors.muted
                        )
                    }
                }
            )
            Line()
        }
        groundColours(colors).forEachIndexed { index, (name, color) ->
            if (index > 0) Line(SiteTheme.colors.hairline)
            Ground(name, color, colors.surface)
        }
    }
}

/** A ground's, a text's or a line's row, in the layout's columns. */
@Composable
private fun Ground(
    name: String,
    color: Color,
    surface: Color
) {
    val language = LocalLanguage.current
    val badge: @Composable () -> Unit = { Badge(name, color, surface) }
    when (SiteTheme.layout) {
        SiteLayout.Desktop -> GroundRow(
            cells = listOf(
                {
                    PantrixText(
                        text = name,
                        style = mono(12.5.sp, FontWeight.SemiBold),
                        color = PantrixTheme.colors.textPrimary
                    )
                },
                {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Swatch(color, 22.dp)
                        PantrixText(text = color.hex(), style = mono(12.sp), color = PantrixTheme.colors.textSecondary)
                    }
                },
                badge,
                {
                    PantrixText(
                        text = FoundationsSay.uses[name]?.of(language).orEmpty(),
                        style = SiteTheme.type.bodySmall,
                        color = PantrixTheme.colors.textSecondary
                    )
                }
            )
        )

        SiteLayout.Tablet -> GroundRow(
            cells = listOf(
                {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Swatch(color, 24.dp)
                        PantrixText(
                            text = name,
                            style = mono(12.5.sp, FontWeight.SemiBold),
                            color = PantrixTheme.colors.textPrimary
                        )
                    }
                },
                { PantrixText(text = color.hex(), style = mono(12.sp), color = PantrixTheme.colors.textSecondary) },
                badge
            )
        )

        SiteLayout.Phone -> Row(
            Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Swatch(color, 28.dp)
            Column(Modifier.weight(1f)) {
                PantrixText(
                    text = name,
                    style = mono(12.5.sp, FontWeight.SemiBold),
                    color = PantrixTheme.colors.textPrimary
                )
                PantrixText(text = color.hex(), style = mono(11.5.sp), color = PantrixTheme.colors.textSecondary)
            }
            badge()
        }
    }
}

/** A row of the table, its cells in equal columns but the last, which takes what is left. */
@Composable
private fun GroundRow(cells: List<@Composable () -> Unit>) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        cells.forEachIndexed { index, cell ->
            Box(Modifier.weight(if (index == cells.lastIndex) LAST_COLUMN else 1f)) { cell() }
        }
    }
}

/** A colour's square. */
@Composable
private fun Swatch(
    color: Color,
    size: Dp
) {
    val shape = RoundedCornerShape(if (size > 24.dp) 8.dp else 6.dp)
    Box(Modifier.size(size).background(color, shape).border(1.dp, PantrixTheme.colors.borderHover, shape))
}

/**
 * The contrast of [color] on [surface], with whether it is enough where the role needs it: text 4.5:1, the focus
 * ring 3:1; a ground, a line and the disabled text need none.
 */
@Composable
private fun Badge(
    name: String,
    color: Color,
    surface: Color
) {
    val language = LocalLanguage.current
    val ratio = contrastRatio(color, surface)
    val needed = NEEDED[name]
    val passes = needed == null || ratio >= needed
    val (container, content) = when {
        needed == null -> SiteTheme.colors.hover to PantrixTheme.colors.textSecondary
        passes -> PantrixTheme.colors.success.container to PantrixTheme.colors.success.onContainer
        else -> PantrixTheme.colors.danger.container to PantrixTheme.colors.danger.onContainer
    }
    val verdict = when {
        needed == null -> ""
        passes -> " " + FoundationsSay.Passes.of(language)
        else -> " " + FoundationsSay.Fails.of(language)
    }
    PantrixText(
        text = "${PantrixTheme.strings.decimal(ratio, fractionDigits = 2, minFractionDigits = 2)}:1$verdict",
        style = strong(if (SiteTheme.layout == SiteLayout.Phone) 11.5.sp else 12.sp, FontWeight.Bold),
        color = content,
        maxLines = 1,
        modifier = Modifier
            .background(container, RoundedCornerShape(percent = 50))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

/** The preset's blues and greys, each step in its colour with its number. */
@Composable
private fun Palette() {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val phone = layout == SiteLayout.Phone
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (layout != SiteLayout.Tablet) {
            PantrixText(
                text = FoundationsSay.Palette.of(language),
                style = strong(if (phone) 16.sp else 17.sp),
                color = PantrixTheme.colors.textPrimary,
                modifier = Modifier.padding(top = if (phone) 6.dp else 0.dp)
            )
        }
        Scale(
            BLUES,
            columns = if (phone) PHONE_STEPS else BLUES.size,
            tall = if (layout ==
                SiteLayout.Desktop
            ) {
                76.dp
            } else if (phone) {
                64.dp
            } else {
                72.dp
            },
            codes = true
        )
        Scale(
            GREYS,
            columns = if (phone) PHONE_STEPS else GREYS.size,
            tall = if (layout ==
                SiteLayout.Desktop
            ) {
                76.dp
            } else if (phone) {
                56.dp
            } else {
                64.dp
            },
            codes = layout != SiteLayout.Tablet
        )
    }
}

/** A scale of colours, [columns] to a row, each [tall], its code under its number where [codes]. */
@Composable
private fun Scale(
    steps: List<PaletteStep>,
    columns: Int,
    tall: Dp,
    codes: Boolean
) {
    val shape = RoundedCornerShape(12.dp)
    Column(Modifier.fillMaxWidth().clip(shape).border(1.dp, SiteTheme.colors.hairline, shape)) {
        steps.chunked(columns).forEach { row ->
            Row {
                row.forEach { step ->
                    val color = Color(step.argb)
                    // A light step writes in the brand's near-black, a dark one in white, as the design does.
                    val ink = if (contrastRatio(Color.White, color) >= LIGHT_INK) Color.White else NEAR_BLACK
                    Column(
                        Modifier.weight(1f).height(tall).background(color).padding(8.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        PantrixText(text = "${step.step}", style = strong(11.sp, FontWeight.Bold), color = ink)
                        if (codes) {
                            PantrixText(
                                text = color.hex(),
                                style = mono(if (columns > BLUES.size) 10.sp else 11.sp),
                                color = ink,
                                maxLines = 1
                            )
                        }
                    }
                }
                // A short last row keeps its steps as wide as the rows above.
                repeat(columns - row.size) { Box(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun Line(color: Color = PantrixTheme.colors.border) {
    Box(Modifier.fillMaxWidth().height(1.dp).background(color))
}

/** The six roles of [colors], by name. */
private fun roleColours(colors: PantrixColors): List<Pair<String, PantrixColor>> = listOf(
    "primary" to colors.primary,
    "success" to colors.success,
    "info" to colors.info,
    "warning" to colors.warning,
    "danger" to colors.danger,
    "neutral" to colors.neutral
)

/** The grounds, the text and the lines of [colors], by name. */
private fun groundColours(colors: PantrixColors): List<Pair<String, Color>> = listOf(
    "background" to colors.background,
    "surface" to colors.surface,
    "textPrimary" to colors.textPrimary,
    "textSecondary" to colors.textSecondary,
    "textDisabled" to colors.textDisabled,
    "border" to colors.border,
    "borderHover" to colors.borderHover,
    "borderStrong" to colors.borderStrong,
    "divider" to colors.divider,
    "focusRing" to colors.focusRing
)

/** The contrast a role needs on the surface, by its name: text is read at 4.5:1, the focus ring seen at 3:1. */
private val NEEDED: Map<String, Float> = mapOf("textPrimary" to 4.5f, "textSecondary" to 4.5f, "focusRing" to 3f)

/** The colour's code as the design writes it: `#2264FF`. */
internal fun Color.hex(): String = "#" + (toArgb() and RGB).toString(HEX).padStart(HEX_DIGITS, '0').uppercase()

/**
 * [ratio] as the design grades text by it, in [language] and its way of writing a number: `4.6:1 AA`,
 * `3.4:1 AA large`, or that it fails; `4,6:1 AA` in Turkish.
 */
internal fun grade(
    ratio: Float,
    strings: PantrixStrings,
    language: Language
): String {
    val words = when {
        ratio >= AA -> Words("AA", "AA")
        ratio >= AA_LARGE -> Words("AA large", "AA büyük")
        else -> Words("fails", "kalır")
    }
    return "${strings.decimal(ratio, fractionDigits = 1, minFractionDigits = 1)}:1 ${words.of(language)}"
}

/** The site's sans type at [size] in [weight]. */
@Composable
internal fun strong(
    size: TextUnit,
    weight: FontWeight = FontWeight.ExtraBold
) = SiteTheme.type.body.copy(fontSize = size, lineHeight = (size.value * LINE).sp, fontWeight = weight)

/** The site's code type at [size] in [weight]. */
@Composable
internal fun mono(
    size: TextUnit,
    weight: FontWeight = FontWeight.Medium
) = SiteTheme.type.inlineCode.copy(fontSize = size, lineHeight = (size.value * LINE).sp, fontWeight = weight)

private const val ROLES_A_ROW = 3
private const val PHONE_STEPS = 4
private const val PHONE_COLOUR_SHARE = 1.5f
private const val LAST_COLUMN = 1.6f
private const val AA = 4.5f
private const val AA_LARGE = 3f
private const val LIGHT_INK = 4f
private const val LINE = 1.4f
private const val RGB = 0xFFFFFF
private const val HEX = 16
private const val HEX_DIGITS = 6
private val NEAR_BLACK = Color(0xFF05070A)
