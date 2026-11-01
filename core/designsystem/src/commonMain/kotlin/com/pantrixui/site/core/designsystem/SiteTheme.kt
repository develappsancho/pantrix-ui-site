package com.pantrixui.site.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.toUpperCase
import com.pantrixui.foundation.adaptive.WindowWidthClass
import com.pantrixui.foundation.adaptive.rememberPantrixWindow
import com.pantrixui.foundation.l10n.PantrixLocale
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.foundation.token.color.rememberPantrixColors
import com.pantrixui.site.core.i18n.ProvideLanguage
import com.pantrixui.site.core.model.Language

/**
 * The three ways the site lays a page out, by the width of its window, as its designs are drawn: a phone's, 390 wide,
 * is Pantrix's compact window; a tablet's, 834, its medium one; and the desktop's, 1440, everything wider, a tablet on
 * its side among them.
 */
enum class SiteLayout {
    Phone,
    Tablet,
    Desktop
}

/**
 * The site's look around [content]: Pantrix's theme in [PantrixPreset], the site's own colours and styles, and the
 * words of [language].
 *
 * @param dark whether the theme is dark; the site's own button chooses it, not only the system.
 * @param language the language the site and Pantrix's components speak.
 * @param content what is drawn in it.
 */
@Composable
fun SiteTheme(
    dark: Boolean,
    language: Language,
    content: @Composable () -> Unit
) {
    // The window is classed before the theme is built: the theme's code type is the layout's.
    val window = rememberPantrixWindow()
    val layout = siteLayoutOf(window.widthClass)
    val base = PantrixPreset.typography()
    val sans = base.body.fontFamily ?: FontFamily.Default
    val mono = base.code.fontFamily ?: FontFamily.Monospace
    val type = remember(layout, sans, mono) { SiteType.of(layout, sans, mono) }
    PantrixTheme(
        preset = PantrixPreset,
        colors = rememberPantrixColors(PantrixPreset, darkTheme = dark),
        typography = base,
        window = window,
        locale = language.pantrixLocale
    ) {
        CompositionLocalProvider(
            LocalSiteColors provides SiteColors.of(dark),
            LocalSiteType provides type,
            LocalSiteLayout provides layout
        ) {
            ProvideLanguage(language, content)
        }
    }
}

/** What the site's theme gives what is drawn in it. */
object SiteTheme {
    /** The site's colours beyond Pantrix's roles. */
    val colors: SiteColors
        @Composable
        @ReadOnlyComposable
        get() = LocalSiteColors.current

    /** The site's text styles for the layout on show. */
    val type: SiteType
        @Composable
        @ReadOnlyComposable
        get() = LocalSiteType.current

    /** The layout the window is wide enough for. */
    val layout: SiteLayout
        @Composable
        @ReadOnlyComposable
        get() = LocalSiteLayout.current
}

/** The layout of a window [widthClass] wide. */
fun siteLayoutOf(widthClass: WindowWidthClass): SiteLayout = when (widthClass) {
    WindowWidthClass.Compact -> SiteLayout.Phone
    WindowWidthClass.Medium -> SiteLayout.Tablet
    else -> SiteLayout.Desktop
}

/**
 * Lays [content] out as the site lays it out in a window of [layout]'s width, whatever the window's own: a part of a
 * page that keeps to one column in a window too narrow for the columns its layout draws takes that column's design,
 * as the home page's hero takes the tablet's in a desktop window too narrow for its two.
 */
@Composable
fun WithSiteLayout(
    layout: SiteLayout,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalSiteLayout provides layout, content = content)
}

/** What Pantrix's components say their own words in: "Close", "Loading". */
val Language.pantrixLocale: PantrixLocale
    get() = when (this) {
        Language.English -> PantrixLocale.English
        Language.Turkish -> PantrixLocale.Turkish
    }

/**
 * [this] in capitals by the rules of the language the theme speaks, as an overline writes its words: in Turkish an `i`
 * is an `İ`, which a capital made with no language loses.
 */
@Composable
@ReadOnlyComposable
fun String.inCapitals(): String = toUpperCase(LocaleList(Locale(PantrixTheme.locale.languageTag)))

private val LocalSiteLayout = staticCompositionLocalOf { SiteLayout.Desktop }
