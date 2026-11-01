package com.pantrixui.site.core.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * The site's text styles, as its design sets its pages: larger and heavier than Pantrix's type scale, which the
 * components keep. Each layout has its own sizes: a page's title is 40 on the desktop, 38 on a tablet, 32 on a phone.
 *
 * A line is as tall as its line height, the first and the last too, as a web page sets it: Compose otherwise trims the
 * room over a text's first line and under its last, and every row of the design would come out a few points short.
 *
 * @property pageTitle a page's heading.
 * @property lead the paragraph under it.
 * @property sectionTitle a section's heading.
 * @property stepTitle a step's heading, and a section's part.
 * @property body running text.
 * @property bodySmall the text of a card, a row, a note.
 * @property nav the header's links.
 * @property sidebarItem a sidebar's rows.
 * @property overline a group's label, set in capitals.
 * @property breadcrumb a page's trail.
 * @property code a block of code.
 * @property inlineCode a piece of code inside a sentence.
 * @property button a small button's label.
 * @property factLabel a fact's label.
 * @property factValue a fact's value.
 * @property cardTitle a card's first line.
 * @property guideHeading a guide's section heading: "1. The problem".
 * @property guideSubheading a heading inside a guide's section: "What is seen".
 * @property guideTitle a guide's heading, a little smaller than a page's on a tablet and a phone.
 * @property tag a page's tag: "6 min read".
 */
@Immutable
data class SiteType(
    val pageTitle: TextStyle,
    val lead: TextStyle,
    val sectionTitle: TextStyle,
    val stepTitle: TextStyle,
    val body: TextStyle,
    val bodySmall: TextStyle,
    val nav: TextStyle,
    val sidebarItem: TextStyle,
    val overline: TextStyle,
    val breadcrumb: TextStyle,
    val code: TextStyle,
    val inlineCode: TextStyle,
    val button: TextStyle,
    val factLabel: TextStyle,
    val factValue: TextStyle,
    val cardTitle: TextStyle,
    val guideHeading: TextStyle,
    val guideSubheading: TextStyle,
    val guideTitle: TextStyle,
    val tag: TextStyle
) {
    companion object {
        /** The styles of [layout], in the site's [sans] and [mono] families. */
        fun of(
            layout: SiteLayout,
            sans: FontFamily,
            mono: FontFamily
        ): SiteType {
            val phone = layout == SiteLayout.Phone
            // The design sets running text at 1.65 of its size, and its headings take the same line.
            val lineOfText = if (phone) 1.6f else 1.65f

            fun sans(
                size: Float,
                weight: FontWeight,
                spacing: Float = 0f,
                line: Float = lineOfText
            ) = TextStyle(
                fontFamily = sans,
                fontWeight = weight,
                fontSize = size.sp,
                lineHeight = (size * line).sp,
                letterSpacing = spacing.em(size),
                lineHeightStyle = AsOnTheWeb
            )

            fun mono(
                size: Float,
                line: Float
            ) = TextStyle(
                fontFamily = mono,
                fontWeight = FontWeight.Medium,
                fontSize = size.sp,
                lineHeight = (size * line).sp,
                lineHeightStyle = AsOnTheWeb
            )

            val titleSize = when (layout) {
                SiteLayout.Desktop -> 40f
                SiteLayout.Tablet -> 38f
                SiteLayout.Phone -> 32f
            }
            val leadSize = when (layout) {
                SiteLayout.Desktop -> 18f
                SiteLayout.Tablet -> 17f
                SiteLayout.Phone -> 16f
            }
            return SiteType(
                pageTitle = sans(titleSize, FontWeight.ExtraBold, spacing = -0.03f, line = 1.1f),
                lead = sans(leadSize, FontWeight.Medium),
                sectionTitle = sans(if (phone) 20f else 22f, FontWeight.ExtraBold, spacing = -0.02f),
                stepTitle = sans(if (phone) 18f else 20f, FontWeight.ExtraBold, spacing = -0.01f),
                body = sans(15f, FontWeight.Medium),
                bodySmall = sans(13.5f, FontWeight.Medium),
                nav = sans(14f, FontWeight.SemiBold),
                sidebarItem = sans(if (phone) 15f else 14f, FontWeight.SemiBold),
                overline = sans(12f, FontWeight.ExtraBold, spacing = 0.06f),
                breadcrumb = sans(13f, FontWeight.SemiBold),
                code = mono(if (phone) 12f else 13f, line = if (phone) 1.7f else 1.75f),
                inlineCode = mono(if (phone) 13f else 13.5f, line = lineOfText),
                button = sans(if (layout == SiteLayout.Desktop) 13f else 14f, FontWeight.Bold),
                factLabel = sans(12.5f, FontWeight.SemiBold),
                factValue = sans(if (phone) 14f else 15f, FontWeight.ExtraBold),
                cardTitle = sans(15f, FontWeight.ExtraBold),
                guideHeading = sans(layout.guideHeadingSize, FontWeight.ExtraBold, spacing = -0.02f, line = 1.25f),
                guideSubheading = sans(16f, FontWeight.ExtraBold),
                guideTitle = sans(
                    layout.guideTitleSize,
                    FontWeight.ExtraBold,
                    spacing = -0.03f,
                    line = layout.guideTitleLine
                ),
                tag = sans(12.5f, FontWeight.Bold)
            )
        }

        /** A guide's section heading's size in [this] layout. */
        private val SiteLayout.guideHeadingSize: Float
            get() = when (this) {
                SiteLayout.Desktop -> 24f
                SiteLayout.Tablet -> 22f
                SiteLayout.Phone -> 20f
            }

        /** A guide's title's size in [this] layout: a page's on a desktop, smaller on a tablet and a phone. */
        private val SiteLayout.guideTitleSize: Float
            get() = when (this) {
                SiteLayout.Desktop -> 40f
                SiteLayout.Tablet -> 36f
                SiteLayout.Phone -> 30f
            }

        /** A guide's title's line, as many times its size: a little more on a phone, where it wraps. */
        private val SiteLayout.guideTitleLine: Float
            get() = if (this == SiteLayout.Phone) 1.12f else 1.1f

        /** A line's room shared above and below its letters, and none trimmed: as a browser sets a line. */
        private val AsOnTheWeb = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)

        /** [this] many ems of a [size] sp text, as the design writes its letter spacing. */
        private fun Float.em(size: Float): TextUnit = (this * size).sp
    }
}

/**
 * The style at [size], its line as many times its size as before: the design sets a smaller text in the same
 * proportion of line, not in the larger text's.
 */
fun TextStyle.sized(size: TextUnit): TextStyle =
    copy(fontSize = size, lineHeight = (size.value * lineHeight.value / fontSize.value).sp)

/** The site's text styles, for everything under the site's theme. */
val LocalSiteType: ProvidableCompositionLocal<SiteType> = staticCompositionLocalOf {
    SiteType.of(SiteLayout.Desktop, FontFamily.Default, FontFamily.Monospace)
}
