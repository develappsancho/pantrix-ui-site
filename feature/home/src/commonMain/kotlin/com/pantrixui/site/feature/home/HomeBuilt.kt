package com.pantrixui.site.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.SiteIcons
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.content.LandingBand
import com.pantrixui.site.core.designsystem.content.LandingButton
import com.pantrixui.site.core.designsystem.content.LandingButtonKind
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.Route
import com.pantrixui.site.core.model.SiteDownloads
import com.pantrixui.site.core.model.Words

/**
 * The band that leads to the apps built with Pantrix UI: this site is the first of them, it says, with where it runs,
 * and two buttons, the references and its source. The words beside the platforms on a desktop; over them elsewhere.
 *
 * @param open opens one of the site's pages.
 * @param onLink opens an address outside the site.
 */
@Composable
internal fun BuiltWithPantrix(
    open: (Route) -> Unit,
    onLink: (String) -> Unit
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val colors = PantrixTheme.colors
    val phone = layout == SiteLayout.Phone
    val shape = RoundedCornerShape(if (layout == SiteLayout.Desktop) 24.dp else 20.dp)
    val bottom = when (layout) {
        SiteLayout.Desktop -> 64.dp
        SiteLayout.Tablet, SiteLayout.Phone -> 48.dp
    }
    val words = @Composable {
        Kicker(BuiltSay.Kicker.of(language))
        PantrixText(
            text = BuiltSay.Title.of(language),
            style = display(
                when (layout) {
                    SiteLayout.Desktop -> 32.sp
                    SiteLayout.Tablet -> 28.sp
                    SiteLayout.Phone -> 24.sp
                },
                line = 1.15f,
                spacing = -0.03f
            ),
            color = colors.textPrimary
        )
        SectionLead(start = BuiltSay.Lead.of(language), size = if (layout == SiteLayout.Desktop) 16.sp else 15.sp)
    }
    val buttons = @Composable {
        val references = @Composable {
            LandingButton(
                text = BuiltSay.SeeReferences.of(language),
                onClick = { open(Route.References) },
                tag = "builtReferences",
                height = if (layout == SiteLayout.Desktop) 46.dp else 48.dp,
                fill = phone,
                arrow = true
            )
        }
        val source = @Composable {
            LandingButton(
                text = BuiltSay.Source.of(language),
                onClick = { onLink(SiteDownloads.REPOSITORY) },
                tag = "builtSource",
                kind = LandingButtonKind.Outline,
                height = if (layout == SiteLayout.Desktop) 46.dp else 48.dp,
                fill = phone,
                trailing = SiteIcons.ArrowUpRight
            )
        }
        if (phone) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                references()
                source()
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                references()
                source()
            }
        }
    }
    LandingBand(PaddingValues(start = layout.gutter, end = layout.gutter, bottom = bottom)) {
        val card = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surface)
            .border(1.dp, colors.border, shape)
        if (layout == SiteLayout.Desktop) {
            Row(
                card.padding(40.dp),
                horizontalArrangement = Arrangement.spacedBy(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f).widthIn(max = 560.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    words()
                    Box(Modifier.padding(top = 8.dp)) { buttons() }
                }
                Tiles(Modifier.weight(TILES_SHARE), columns = 2)
            }
        } else {
            Column(card.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                words()
                Tiles(Modifier.fillMaxWidth(), columns = if (phone) 1 else 2)
                Box(Modifier.padding(top = 4.dp)) { buttons() }
            }
        }
    }
}

/** Where the site runs, a tile a platform, in [columns]. */
@Composable
private fun Tiles(
    modifier: Modifier,
    columns: Int
) {
    val language = LocalLanguage.current
    val colors = PantrixTheme.colors
    val site = SiteTheme.colors
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        BuiltSay.Platforms.chunked(columns).forEach { row ->
            Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { (name, detail) ->
                    Column(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(14.dp))
                            .background(site.surfaceAlt)
                            .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        PantrixText(
                            text = name,
                            style = display(15.sp, line = 1.3f, spacing = 0f),
                            color = colors.textPrimary
                        )
                        PantrixText(
                            text = detail.of(language),
                            style = SiteTheme.type.bodySmall.copy(fontSize = 13.sp, lineHeight = 18.sp),
                            color = site.muted
                        )
                    }
                }
                repeat(columns - row.size) { Box(Modifier.weight(1f)) }
            }
        }
    }
}

/** The band's words. */
private object BuiltSay {
    val Kicker = Words("Built with Pantrix UI", "Pantrix UI ile yapıldı")
    val Title = Words("This site is the first reference.", "Bu site ilk referans.")
    val Lead = Words(
        "pantrixui.com and its apps for Android, iOS and macOS share one Kotlin codebase and draw their own screens " +
            "with the library's components. Its source is open.",
        "pantrixui.com ve Android, iOS ve macOS uygulamaları tek bir Kotlin kod tabanını paylaşır ve kendi " +
            "ekranlarını kütüphanenin bileşenleriyle çizer. Kaynağı açık."
    )
    val SeeReferences = Words("See the references", "Referanslara bak")
    val Source = Words("Its source on GitHub", "Kaynağı GitHub'da")

    /** Where the site runs, each with what it is there. */
    val Platforms = listOf(
        "Web" to
            Words(
                "Wasm, and a JavaScript build for older browsers",
                "Wasm, eski tarayıcılar için de bir JavaScript sürümü"
            ),
        "Android" to Words("Phones, tablets and foldables", "Telefonlar, tabletler ve katlanabilir cihazlar"),
        "iOS" to Words("iPhone and iPad", "iPhone ve iPad"),
        "macOS" to Words("A signed and notarized app", "İmzalı ve noterden geçmiş bir uygulama")
    )
}

/** How much of a desktop's band the platforms take beside its words. */
private const val TILES_SHARE = 0.8f
