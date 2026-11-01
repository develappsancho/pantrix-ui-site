package com.pantrixui.site.core.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The site's colours beyond Pantrix's roles: the grounds its pages are laid on and the colours of its code. Pantrix's
 * roles, `PantrixTheme.colors`, are what a component reads; these are what the site's own frame and pages read.
 *
 * @property canvas the ground of a band of a page set apart from the rest: a landing's section, the changelog's head.
 * @property surfaceAlt the ground of a sidebar beside the page.
 * @property hover what a row or a link is filled with under a pointer.
 * @property hairline the line that splits a page's frame: under the header, beside the sidebar.
 * @property muted the quietest text: a section's label, a breadcrumb, a count.
 * @property accentLine the edge of what is tinted in the primary colour.
 * @property warningLine the edge of a warning's note.
 * @property violet the fill of a mark of what is yet to come.
 * @property onViolet what is written on it.
 * @property violetMark the dot of what is yet to come, as strong in both themes.
 * @property code the colours of a block of code.
 * @property sketch the colours of a guide's sketch of a phone.
 */
@Immutable
data class SiteColors(
    val canvas: Color,
    val surfaceAlt: Color,
    val hover: Color,
    val hairline: Color,
    val muted: Color,
    val accentLine: Color,
    val warningLine: Color,
    val violet: Color,
    val onViolet: Color,
    val violetMark: Color,
    val code: CodeColors,
    val sketch: SketchColors
) {
    companion object {
        /** The light set. */
        val Light = SiteColors(
            canvas = Color(0xFFF5F7F9),
            surfaceAlt = Color(0xFFFAFBFC),
            hover = Color(0xFFEEF0F4),
            hairline = Color(0xFFECEEF3),
            muted = Color(0xFF63647B),
            accentLine = Color(0xFFCFDFFF),
            warningLine = Color(0xFFFBD3AD),
            violet = Color(0xFFEFEAFF),
            onViolet = Color(0xFF4C2FB8),
            violetMark = VIOLET_MARK,
            code = CodeColors.Light,
            sketch = SketchColors.Light
        )

        /** The dark set. */
        val Dark = SiteColors(
            canvas = Color(0xFF10141E),
            surfaceAlt = Color(0xFF1B2130),
            hover = Color(0xFF212839),
            hairline = Color(0xFF1F2533),
            muted = Color(0xFF9DA2B2),
            accentLine = Color(0xFF264795),
            warningLine = Color(0xFF5C3613),
            violet = Color(0xFF241A4A),
            onViolet = Color(0xFFC9BBFF),
            violetMark = VIOLET_MARK,
            code = CodeColors.Dark,
            sketch = SketchColors.Dark
        )

        /** The set for the theme: the dark one when [dark]. */
        fun of(dark: Boolean): SiteColors = if (dark) Dark else Light
    }
}

/**
 * The colours of a block of code. A block is dark in both themes, so that code reads the same in each; only its
 * ground and its lines change.
 *
 * @property background its ground.
 * @property line the line under its header and between its parts.
 * @property text code that is nothing in particular.
 * @property comment a comment, and the header's quieter words.
 * @property keyword a keyword: `fun`, `val`, `import`.
 * @property literal a string.
 * @property number a number.
 * @property function a call's name or a declared function's.
 * @property annotation an annotation: `@Composable`.
 * @property type a type's name.
 * @property selectedTab the fill of the chosen tab in its header.
 * @property onSelectedTab the chosen tab's label.
 */
@Immutable
data class CodeColors(
    val background: Color,
    val line: Color,
    val text: Color,
    val comment: Color,
    val keyword: Color,
    val literal: Color,
    val number: Color,
    val function: Color,
    val annotation: Color,
    val type: Color,
    val selectedTab: Color,
    val onSelectedTab: Color
) {
    companion object {
        private val Base = CodeColors(
            background = Color(0xFF181D2B),
            line = Color(0xFF262D3D),
            text = Color(0xFFE7E9ED),
            comment = Color(0xFFA0A5B3),
            keyword = Color(0xFF8BADFF),
            literal = Color(0xFF7FD8A8),
            number = Color(0xFFFA8A37),
            function = Color(0xFFFDF3DC),
            annotation = Color(0xFFEDB42E),
            type = Color(0xFFFDF3DC),
            selectedTab = Color(0xFF262D3D),
            onSelectedTab = Color(0xFFF4F6FA)
        )

        /** In the light theme. */
        val Light = Base

        /** In the dark theme: a deeper ground under the same colours. */
        val Dark = Base.copy(background = Color(0xFF0F131C), line = Color(0xFF222A3A))
    }
}

/**
 * The colours of a guide's sketch of a phone: its frame, dark in both themes as a phone's is, and the keyboard drawn
 * on its screen.
 *
 * @property frame the phone's edge.
 * @property keyboard the keyboard's ground.
 * @property key a letter's key.
 * @property otherKey a key that is not a letter: shift, delete, return.
 * @property onKey what is written on a key.
 */
@Immutable
data class SketchColors(
    val frame: Color,
    val keyboard: Color,
    val key: Color,
    val otherKey: Color,
    val onKey: Color
) {
    companion object {
        /** In the light theme. */
        val Light = SketchColors(
            frame = Color(0xFF181D2B),
            keyboard = Color(0xFFD1D4DB),
            key = Color(0xFFFFFFFF),
            otherKey = Color(0xFFAEB3BE),
            onKey = Color(0xFF05070A)
        )

        /** In the dark theme. */
        val Dark = SketchColors(
            frame = Color(0xFF181D2B),
            keyboard = Color(0xFF2B2F38),
            key = Color(0xFF4A4F5A),
            otherKey = Color(0xFF3A3F49),
            onKey = Color(0xFFF4F6FA)
        )
    }
}

/** The site's colours, for everything under the site's theme. */
val LocalSiteColors: ProvidableCompositionLocal<SiteColors> = staticCompositionLocalOf { SiteColors.Light }

/** The violet of a dot of what is yet to come, the same in both themes. */
private val VIOLET_MARK = Color(0xFF6D4AFF)
