package com.pantrixui.site.feature.references

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.content.dotted
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.LibraryFacts
import com.pantrixui.site.core.model.Words

/** The ground a picture of an app stands on: the canvas's colour and its dots, what it holds in its middle. */
@Composable
internal fun SketchGround(
    modifier: Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier.background(SiteTheme.colors.canvas).dotted(PantrixTheme.colors.border, step = 18.dp).clipToBounds(),
        contentAlignment = Alignment.Center,
        content = content
    )
}

/**
 * The site at [address] on a desktop and on a phone, drawn small and [scale] times its size: a picture, whose words
 * are the site's own home page's, and which a screen reader hears as one.
 */
@Composable
internal fun SiteSketch(
    address: String,
    scale: Float
) {
    val language = LocalLanguage.current
    val picture = ReferencesSay.picture(address).of(language)
    Box(
        Modifier
            .requiredSize(SKETCH_WIDTH, SKETCH_HEIGHT)
            .scale(scale)
            .clearAndSetSemantics {
                contentDescription = picture
                role = Role.Image
            }
    ) {
        DesktopWindow(Modifier.align(Alignment.TopStart))
        PhoneWindow(Modifier.align(Alignment.BottomEnd))
    }
}

/** The site's home page in a browser's window: its bar, its header, its title and a card on the stage beside it. */
@Composable
private fun DesktopWindow(modifier: Modifier) {
    val colors = PantrixTheme.colors
    val site = SiteTheme.colors
    val language = LocalLanguage.current
    Column(
        modifier
            .size(DESKTOP_WIDTH, DESKTOP_HEIGHT)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.background)
            .border(1.dp, colors.borderHover, RoundedCornerShape(12.dp))
    ) {
        Row(
            Modifier.fillMaxWidth().height(24.dp).background(site.surfaceAlt).padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WINDOW_LIGHTS.forEach { light -> Box(Modifier.size(8.dp).background(light, CircleShape)) }
        }
        Line(site.hairline)
        Row(
            Modifier.fillMaxWidth().height(34.dp).padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BrandMark(18.dp)
            SketchText(BRAND, 11.sp, colors.textPrimary)
            NAV.forEach { SketchText(it.of(language), 9.sp, site.muted, FontWeight.SemiBold) }
            Spacer(Modifier.weight(1f))
            Box(
                Modifier.size(90.dp, 18.dp).background(site.surfaceAlt, RoundedCornerShape(6.dp))
                    .border(1.dp, colors.border, RoundedCornerShape(6.dp))
            )
        }
        Line(site.hairline)
        Row(
            Modifier.padding(horizontal = 18.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Headline(22.sp)
                SketchText(LEAD.of(language), 9.5.sp, colors.textSecondary, FontWeight.Medium, line = LEAD_LINE)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Pill(START.of(language), filled = true, size = 9.sp)
                    Pill(BROWSE.of(language), filled = false, size = 9.sp)
                }
            }
            Box(
                Modifier
                    .width(170.dp)
                    .height(206.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(site.canvas)
                    .dotted(colors.border, step = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    Modifier.width(124.dp).background(colors.surface, RoundedCornerShape(10.dp)).padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SketchText(REVIEW.of(language), 9.sp, colors.textPrimary)
                    Box(
                        Modifier.fillMaxWidth().height(34.dp)
                            .border(1.dp, colors.borderHover, RoundedCornerShape(6.dp))
                    )
                    Box(Modifier.fillMaxWidth().height(16.dp).background(colors.primary.color, CircleShape))
                }
            }
        }
    }
}

/** The site's home page on a phone: its header, its title and its two buttons, in the phone's dark frame. */
@Composable
private fun PhoneWindow(modifier: Modifier) {
    val colors = PantrixTheme.colors
    val language = LocalLanguage.current
    Column(
        modifier
            .size(PHONE_WIDTH, PHONE_HEIGHT)
            .clip(RoundedCornerShape(26.dp))
            .background(PHONE_FRAME)
            .padding(6.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(colors.background)
    ) {
        Row(
            Modifier.padding(top = 14.dp).fillMaxWidth().height(30.dp).padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BrandMark(14.dp, letter = false)
            SketchText(BRAND, 9.sp, colors.textPrimary)
        }
        Line(SiteTheme.colors.hairline)
        Column(
            Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Headline(15.sp)
            Pill(START.of(language), filled = true, size = 8.5.sp, fill = true)
            Pill(BROWSE.of(language), filled = false, size = 8.5.sp, fill = true)
        }
    }
}

/** The home page's title, its last sentence in the brand's colour. */
@Composable
private fun Headline(size: TextUnit) {
    val language = LocalLanguage.current
    val accent = PantrixTheme.colors.primary.color
    PantrixText(
        text = buildAnnotatedString {
            append(TITLE.dropLast(1).joinToString(" ") { it.of(language) } + " ")
            withStyle(SpanStyle(color = accent)) { append(TITLE.last().of(language)) }
        },
        style = strong(size).copy(lineHeight = (size.value * HEADLINE_LINE).sp),
        color = PantrixTheme.colors.textPrimary
    )
}

/**
 * A button of the sketch: a pill of words, the brand's fill where [filled], an edge where not. One that [fill]s its
 * width, as on the phone, has its words in its middle and no room at its sides, as the design draws it: the phone's is
 * too narrow for "Browse … components" with room around it.
 */
@Composable
private fun Pill(
    text: String,
    filled: Boolean,
    size: TextUnit,
    fill: Boolean = false
) {
    val colors = PantrixTheme.colors
    val shape = RoundedCornerShape(6.dp)
    val ground = if (filled) {
        Modifier.background(
            colors.primary.color,
            shape
        )
    } else {
        Modifier.border(1.dp, colors.borderHover, shape)
    }
    val width = if (fill) Modifier.fillMaxWidth() else Modifier
    val room = if (fill) Modifier else Modifier.padding(horizontal = 10.dp)
    Box(width.height(22.dp).then(ground).then(room), contentAlignment = Alignment.Center) {
        SketchText(text, size, if (filled) colors.primary.onColor else colors.textPrimary, FontWeight.Bold)
    }
}

/** The brand's mark, [size] large: its "P" where [letter], a plain square where the sketch is too small for one. */
@Composable
private fun BrandMark(
    size: Dp,
    letter: Boolean = true
) {
    Box(
        Modifier.size(size).background(BRAND_BLUE, RoundedCornerShape(size * BRAND_CORNER)),
        contentAlignment = Alignment.Center
    ) {
        if (letter) SketchText("P", (size.value * BRAND_LETTER).sp, Color.White)
    }
}

/** A line of [text] at [size] in [color], in the sketch's [weight], bold unless told, its lines [line] apart. */
@Composable
private fun SketchText(
    text: String,
    size: TextUnit,
    color: Color,
    weight: FontWeight = FontWeight.ExtraBold,
    line: Float = WORDS_LINE
) {
    PantrixText(text = text, style = strong(size, weight).copy(lineHeight = (size.value * line).sp), color = color)
}

/** A hairline across the window. */
@Composable
private fun Line(color: Color) {
    Box(Modifier.fillMaxWidth().height(1.dp).background(color))
}

/** The home page's title, as its own words say it. */
private val TITLE = listOf(
    Words("One codebase.", "Tek kod tabanı."),
    Words("Four platforms.", "Dört platform."),
    Words("Each one at home.", "Her biri kendi evinde.")
)
private val LEAD = Words(
    "A design system for Compose Multiplatform: ${LibraryFacts.COMPONENTS} components on Compose Foundation, " +
        "no Material.",
    "Compose Multiplatform için bir tasarım sistemi: Compose Foundation üstünde ${LibraryFacts.COMPONENTS} bileşen, " +
        "Material yok."
)
private val START = Words("Get started", "Başla")
private val BROWSE =
    Words("Browse ${LibraryFacts.COMPONENTS} components", "${LibraryFacts.COMPONENTS} bileşene göz at")
private val REVIEW = Words("Write a review", "Bir yorum yaz")
private val NAV = listOf(
    Words("Docs", "Belgeler"),
    Words("Components", "Bileşenler"),
    Words("Foundations", "Temeller"),
    Words("Adaptive", "Uyarlanabilir")
)
private const val BRAND = "Pantrix UI"

/** The brand's own blue, which the site's mark is drawn in whatever the theme. */
private val BRAND_BLUE = Color(0xFF2264FF)

/** The frame of the phone, a near black in both themes. */
private val PHONE_FRAME = Color(0xFF181D2B)

/** The three lights of a Mac's window. */
private val WINDOW_LIGHTS = listOf(Color(0xFFFF5F57), Color(0xFFFEBC2E), Color(0xFF28C840))

private val SKETCH_WIDTH = 560.dp
private val SKETCH_HEIGHT = 380.dp
private val DESKTOP_WIDTH = 500.dp
private val DESKTOP_HEIGHT = 330.dp
private val PHONE_WIDTH = 150.dp
private val PHONE_HEIGHT = 300.dp
private const val BRAND_CORNER = 0.28f
private const val BRAND_LETTER = 0.55f
private const val HEADLINE_LINE = 1.1f
private const val WORDS_LINE = 1.4f

/** How far apart the lead's lines are, as the design's: half their size again. */
private const val LEAD_LINE = 1.5f
