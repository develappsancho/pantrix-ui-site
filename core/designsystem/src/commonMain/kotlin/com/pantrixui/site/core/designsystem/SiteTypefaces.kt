package com.pantrixui.site.core.designsystem

import androidx.compose.ui.text.font.FontWeight
import com.pantrixui.foundation.token.typography.PantrixFont
import com.pantrixui.foundation.token.typography.PantrixTypeface
import com.pantrixui.site.core.designsystem.generated.resources.Res
import com.pantrixui.site.core.designsystem.generated.resources.jetbrains_mono_medium
import com.pantrixui.site.core.designsystem.generated.resources.noto_sans_arabic
import com.pantrixui.site.core.designsystem.generated.resources.site_symbols

/**
 * The site's two typefaces: Plus Jakarta Sans, Pantrix's own, for what is read, and JetBrains Mono for code.
 *
 * Code is set in one weight, Medium, so JetBrains Mono is the static font of that weight, made from its variable font
 * (version 2.305) with fontTools' instancer: the web fetches 192 KB instead of the 304 KB that carry every weight.
 *
 * On the web a typeface is fetched and arrives a moment after the app starts. The web's entry point holds the first
 * frame back until both have been read (`areTypefacesLoaded`, `PantrixTypefaceGate`), and its page fetches their files
 * while the app's code is fetched.
 *
 * A character the site's fonts lack is drawn in their fallbacks: Pantrix's own for the marks of a Mac's keys, Noto
 * Sans Arabic for the Arabic of the home page and of the guides on dates and numbers (the Arabic block at weight 400
 * with its shaping, 51 KB), and Pantrix Site Symbols for the site's own marks (`↵ ∿ ▦ ◎ ✓ ✗ ⟳`, 3 KB).
 * `tools/fonts/site_fonts.py` cuts both from Noto's fonts. Without them a browser would fetch a Noto font from Google
 * for each.
 */
object SiteTypefaces {
    /** What the site's text is set in: Pantrix's own typeface, with Arabic and the site's marks among its fallbacks. */
    val Sans: PantrixTypeface = PantrixTypeface(
        fonts = PantrixTypeface.Default.fonts,
        fallbacks = PantrixTypeface.Default.fallbacks + PantrixFont(Res.font.noto_sans_arabic) +
            PantrixFont(Res.font.site_symbols)
    )

    /** What its code is set in. */
    val Mono: PantrixTypeface = PantrixTypeface(PantrixFont(Res.font.jetbrains_mono_medium, FontWeight.Medium))
}
