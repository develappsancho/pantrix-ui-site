package com.pantrixui.site.core.designsystem.content

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.button.ButtonColors
import com.pantrixui.components.elements.button.ButtonSize
import com.pantrixui.components.elements.button.ButtonSizeStyle
import com.pantrixui.components.elements.button.ButtonStyle
import com.pantrixui.components.elements.button.ButtonVariant
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.components.elements.button.colors
import com.pantrixui.components.elements.icon.PantrixIcon
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.icons.PantrixIcons
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.inCapitals
import com.pantrixui.site.core.designsystem.sized

/**
 * A band of one of the site's own pages, its landing page, the adaptive page and the one for coding assistants:
 * across the window, its [ground] and a hairline over and under it where it has one, and its content no wider than
 * the design's page, in the middle of a wider window.
 *
 * @param padding the room inside it, around its content.
 * @param modifier the band's modifier.
 * @param ground its fill, or none.
 * @param gap the room between what it holds.
 * @param lineUnder whether a band with a ground has its hairline under it too: not over the page's foot, which has
 *   its own, nor at the page's end.
 * @param content what it holds, one under another.
 */
@Composable
fun LandingBand(
    padding: PaddingValues,
    modifier: Modifier = Modifier,
    ground: Color? = null,
    gap: Dp = 0.dp,
    lineUnder: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val line = SiteTheme.colors.hairline
    Box(
        modifier
            .fillMaxWidth()
            .then(
                if (ground == null) {
                    Modifier
                } else {
                    Modifier.background(ground).drawBehind {
                        val width = 1.dp.toPx()
                        drawLine(line, Offset(0f, width / 2), Offset(size.width, width / 2), width)
                        if (lineUnder) {
                            drawLine(
                                line,
                                Offset(0f, size.height - width / 2),
                                Offset(size.width, size.height - width / 2),
                                width
                            )
                        }
                    }
                }
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            Modifier.padding(padding).widthIn(max = LANDING_WIDTH).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(gap),
            content = content
        )
    }
}

/** The room at a band's sides in the layout on show. */
val landingGutter: Dp
    @Composable get() = if (SiteTheme.layout == SiteLayout.Phone) 16.dp else 24.dp

/** A band's room above and below: 72 on a desktop, 56 on a tablet, 40 on a phone, and its sides' [landingGutter]. */
@Composable
fun landingBandPadding(): PaddingValues = PaddingValues(
    horizontal = landingGutter,
    vertical = when (SiteTheme.layout) {
        SiteLayout.Desktop -> 72.dp
        SiteLayout.Tablet -> 56.dp
        SiteLayout.Phone -> 40.dp
    }
)

/**
 * The site's extra bold heading type at [size], its line [line] times its size and its letters [spacing] ems apart:
 * a heading the site's own styles have no size for.
 */
@Composable
fun landingHeading(
    size: TextUnit,
    line: Float,
    spacing: Float
): TextStyle = SiteTheme.type.pageTitle.copy(
    fontSize = size,
    lineHeight = (size.value * line).sp,
    letterSpacing = (size.value * spacing).sp,
    fontWeight = FontWeight.ExtraBold
)

/** The small capitals over a band's heading: what it is about, in the brand's colour. */
@Composable
fun LandingKicker(text: String) {
    val phone = SiteTheme.layout == SiteLayout.Phone
    PantrixText(
        text = text.inCapitals(),
        style = landingHeading(if (phone) 12.sp else 13.sp, line = KICKER_LINE, spacing = KICKER_SPACING),
        color = PantrixTheme.colors.primary.onContainer
    )
}

/** A band's heading: 36 on a desktop, 32 on a tablet and 26 on a phone, or [desktop] where the design has another. */
@Composable
fun LandingHeading(
    text: String,
    modifier: Modifier = Modifier,
    desktop: TextUnit = 36.sp,
    color: Color = PantrixTheme.colors.textPrimary
) {
    val size = when (SiteTheme.layout) {
        SiteLayout.Desktop -> desktop
        SiteLayout.Tablet -> 32.sp
        SiteLayout.Phone -> 26.sp
    }
    PantrixText(
        text = text,
        style = landingHeading(size, line = HEADING_LINE, spacing = HEADING_SPACING),
        color = color,
        modifier = modifier.semantics { heading() }
    )
}

/** A band's sentence under its heading, with a name of the code in it where [code] is given. */
@Composable
fun LandingLead(
    start: String,
    code: String? = null,
    end: String = "",
    modifier: Modifier = Modifier
) {
    val phone = SiteTheme.layout == SiteLayout.Phone
    PantrixText(
        text = withCode(start, code, end, if (phone) 13.sp else 14.sp),
        style = SiteTheme.type.body.copy(fontSize = if (phone) 15.sp else 16.sp),
        color = PantrixTheme.colors.textSecondary,
        modifier = modifier
    )
}

/** [start], [code] in the site's code type in the text's own colour, and [end]: a sentence that names the code. */
@Composable
fun withCode(
    start: String,
    code: String?,
    end: String,
    codeSize: TextUnit
): AnnotatedString {
    val mono = SiteTheme.type.inlineCode.fontFamily
    val color = PantrixTheme.colors.textPrimary
    return buildAnnotatedString {
        append(start)
        if (code != null) {
            withStyle(SpanStyle(fontFamily = mono, fontSize = codeSize, color = color)) { append(code) }
        }
        append(end)
    }
}

/** How a button of one of the site's own pages looks: filled in the brand's colour, or with a line around it. */
enum class LandingButtonKind { Main, Outline }

/**
 * A button of one of the site's own pages, most often a link to another part of the site: [text], an arrow after it
 * where [arrow], [height] tall and rounded as the design's buttons are, whatever the platform.
 *
 * @param text what it says, and what a screen reader calls it.
 * @param onClick what pressing it does.
 * @param tag its automation key.
 * @param modifier the button's modifier.
 * @param kind how it looks.
 * @param height how tall it is.
 * @param padding the room at its sides.
 * @param arrow whether an arrow follows its words.
 * @param fill whether it is as wide as where it stands, as a phone's are.
 * @param textSize the size of its words.
 * @param colors its colours where the band it stands on asks for others than its [kind]'s, as a dark one does.
 * @param weight the weight of its words.
 * @param leading a mark before its words, as "+" before a suggestion.
 */
@Composable
fun LandingButton(
    text: String,
    onClick: () -> Unit,
    tag: String,
    modifier: Modifier = Modifier,
    kind: LandingButtonKind = LandingButtonKind.Main,
    height: Dp = 48.dp,
    padding: Dp = 22.dp,
    arrow: Boolean = false,
    fill: Boolean = false,
    textSize: TextUnit = 15.sp,
    colors: ButtonColors? = null,
    weight: FontWeight = FontWeight.Bold,
    leading: ImageVector? = null
) {
    val theme = PantrixTheme.colors
    val buttonColors = colors ?: when (kind) {
        LandingButtonKind.Main -> ButtonVariant.Solid.colors()

        LandingButtonKind.Outline -> ButtonColors(
            container = theme.surface,
            content = theme.textPrimary,
            border = theme.borderHover
        )
    }
    PantrixButton(
        onClick = onClick,
        contentDescription = text,
        modifier = if (fill) modifier.fillMaxWidth() else modifier,
        variant = if (kind == LandingButtonKind.Main) ButtonVariant.Solid else ButtonVariant.Outline,
        size = ButtonSize.Medium,
        style = ButtonStyle(medium = ButtonSizeStyle(height = height, horizontalPadding = padding)),
        shape = RoundedCornerShape(12.dp),
        colors = buttonColors,
        tagKey = tag
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (leading != null) PantrixIcon(imageVector = leading, contentDescription = null, size = 16.dp)
            PantrixText(text = text, style = SiteTheme.type.button.sized(textSize).copy(fontWeight = weight))
            if (arrow) PantrixIcon(imageVector = PantrixIcons.ArrowForward, contentDescription = null, size = 16.dp)
        }
    }
}

/** A card of a landing page: a surface with a line around it and the room's corners. */
fun Modifier.landingCard(
    surface: Color,
    border: Color,
    corner: Dp = 14.dp
): Modifier {
    val shape = RoundedCornerShape(corner)
    return clip(shape).background(surface).border(1.dp, border, shape)
}

/** A hairline across what it stands in, in [color]: the site's hairline unless a ground asks for another. */
@Composable
fun Hairline(
    modifier: Modifier = Modifier,
    color: Color = SiteTheme.colors.hairline
) {
    Box(modifier.fillMaxWidth().height(1.dp).background(color))
}

/**
 * The widest the content of the site's own pages grows, its sides' room left out, as the design's pages lay it out:
 * their bands, their header and their foot.
 */
val LANDING_WIDTH = 1240.dp

private const val KICKER_LINE = 1.4f
private const val KICKER_SPACING = 0.08f
private const val HEADING_LINE = 1.12f
private const val HEADING_SPACING = -0.03f
