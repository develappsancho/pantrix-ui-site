package com.pantrixui.site.core.designsystem

import androidx.compose.ui.text.font.FontWeight
import com.pantrixui.foundation.token.typography.PantrixFont
import com.pantrixui.foundation.token.typography.PantrixTypeface
import com.pantrixui.site.core.designsystem.generated.resources.Res
import com.pantrixui.site.core.designsystem.generated.resources.jetbrains_mono_medium

/**
 * The site's two typefaces: Plus Jakarta Sans, Pantrix's own, for what is read, and JetBrains Mono for code.
 *
 * Code is set in one weight, Medium, so JetBrains Mono is the static font of that weight, made from its variable font
 * (version 2.305) with fontTools' instancer: the web fetches 192 KB instead of the 304 KB that carry every weight.
 *
 * On the web a typeface is fetched and arrives a moment after the app starts. The web's entry point holds the first
 * frame back until both have been read (`areTypefacesLoaded`, `PantrixTypefaceGate`), and its page fetches their files
 * while the app's code is fetched.
 */
object SiteTypefaces {
    /** What the site's text is set in. */
    val Sans: PantrixTypeface = PantrixTypeface.Default

    /** What its code is set in. */
    val Mono: PantrixTypeface = PantrixTypeface(PantrixFont(Res.font.jetbrains_mono_medium, FontWeight.Medium))
}
