package com.pantrixui.site.feature.docs.sketch

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.content.SketchChoice
import com.pantrixui.site.core.designsystem.content.SketchControls
import com.pantrixui.site.core.designsystem.content.SketchFigure
import com.pantrixui.site.core.designsystem.content.SketchNote
import com.pantrixui.site.core.designsystem.content.SketchSwitch
import com.pantrixui.site.core.designsystem.content.WindowFrame
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.SiteAddress
import com.pantrixui.site.core.model.Words

/**
 * The web compatibility guide's sketch: a page opened in three browsers, with the loader `site.js` and without it.
 * Where the browser runs Wasm with its garbage collection, the Wasm build draws the page. Where it does not, or where
 * the Wasm build fails as it starts, as on a phone short of memory, the loader starts the JavaScript build, which draws
 * the same page. Without the loader, the old browser shows the page's static words and the phone a blank page.
 */
@Composable
internal fun WebCompatibilitySketch() {
    val language = LocalLanguage.current
    var browser by remember { mutableStateOf(Browser.Old) }
    var loader by rememberSaveable { mutableStateOf(true) }
    val shown = browser.shows(loader)
    SketchFigure(caption = WebSay.Caption.of(language)) {
        WindowFrame(
            title = SiteAddress.host,
            modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(),
            height = 180.dp
        ) {
            when (shown) {
                Shown.Wasm -> DrawnPage(script = "site-wasm.js")
                Shown.JavaScript -> DrawnPage(script = "site-js.js")
                Shown.StaticWords -> StaticWords()
                Shown.Blank -> Box(Modifier.fillMaxSize())
            }
        }
        SketchControls {
            SketchChoice(
                options = Browser.entries.map { it to it.label.of(language) },
                selected = browser,
                onSelect = { browser = it },
                tag = "sketch_browser"
            )
            SketchSwitch(code = "site.js", checked = loader, onToggle = { loader = !loader }, tag = "sketch_loader")
            SketchNote(shown.note(browser).of(language))
        }
    }
}

/** The browsers the page is opened in: Chrome 113, a phone short of memory, and Chrome 120, as the notes say. */
private enum class Browser(val label: Words) {
    Old(Words("Old", "Eski")),
    Phone(Words("Phone", "Telefon")),
    New(Words("New", "Yeni"));

    /** What the page shows in this browser, with the loader or without it. */
    fun shows(loader: Boolean): Shown = when (this) {
        New -> Shown.Wasm
        Old -> if (loader) Shown.JavaScript else Shown.StaticWords
        Phone -> if (loader) Shown.JavaScript else Shown.Blank
    }
}

/** What the page shows. */
private enum class Shown {
    Wasm,
    JavaScript,
    StaticWords,
    Blank;

    /** What the note under the sketch says of it, in [browser]. */
    fun note(browser: Browser): Words = when (this) {
        Wasm -> WebSay.Wasm
        JavaScript -> if (browser == Browser.Old) WebSay.OldJavaScript else WebSay.PhoneJavaScript
        StaticWords -> WebSay.Static
        Blank -> WebSay.Blank
    }
}

/** The page as the app draws it, with the name of the build's script in its corner. */
@Composable
private fun DrawnPage(script: String) {
    val language = LocalLanguage.current
    val colors = PantrixTheme.colors
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PantrixText(
                text = WebSay.Heading.of(language),
                style = PantrixTheme.typography.headline,
                modifier = Modifier.weight(1f)
            )
            PantrixText(
                text = script,
                style = SiteTheme.type.code.copy(fontSize = 12.sp),
                color = colors.textSecondary,
                modifier = Modifier
                    .border(1.dp, colors.border, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
        PantrixText(text = WebSay.Body.of(language), style = PantrixTheme.typography.body, color = colors.textSecondary)
    }
}

/** The page's static words as a browser shows HTML with no styles: a serif face and underlined links. */
@Composable
private fun StaticWords() {
    val language = LocalLanguage.current
    val colors = PantrixTheme.colors
    val plain = PantrixTheme.typography.body.copy(fontFamily = FontFamily.Serif)
    val link = plain.copy(textDecoration = TextDecoration.Underline)
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        PantrixText(text = "Pantrix UI", style = link, color = colors.primary.color)
        PantrixText(text = WebSay.Links.of(language), style = link, color = colors.primary.color)
        PantrixText(
            text = WebSay.Heading.of(language),
            style = plain.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
            color = colors.textPrimary
        )
        PantrixText(text = WebSay.Body.of(language), style = plain, color = colors.textPrimary)
    }
}

/** The sketch's words. */
private object WebSay {
    val Heading = Words("One codebase. Four platforms.", "Tek kod. Dört platform.")
    val Body = Words(
        "The same page, whichever build draws it.",
        "Aynı sayfa, onu hangi sürüm çizerse çizsin."
    )
    val Links = Words("Docs   Components   Foundations", "Belgeler   Bileşenler   Temeller")
    val Wasm = Words(
        "Chrome 120 runs Wasm with its garbage collection: the Wasm build draws the page.",
        "Chrome 120, Wasm'ı çöp toplamasıyla çalıştırır: sayfayı Wasm sürümü çizer."
    )
    val OldJavaScript = Words(
        "Chrome 113 has no garbage collection for Wasm: the loader starts the JavaScript build, which draws the " +
            "same page.",
        "Chrome 113'te Wasm için çöp toplama yok: yükleyici JavaScript sürümünü başlatır, o da aynı sayfayı çizer."
    )
    val PhoneJavaScript = Words(
        "On a phone short of memory the Wasm build fails before its first frame: the loader starts the " +
            "JavaScript build instead.",
        "Belleği yetmeyen bir telefonda Wasm sürümü ilk karesinden önce çöker: yükleyici onun yerine JavaScript " +
            "sürümünü başlatır."
    )
    val Static = Words(
        "Wasm alone: Chrome 113 cannot compile the app, and the page's static words stay, with no styles.",
        "Yalnızca Wasm: Chrome 113 uygulamayı derleyemez, sayfanın statik metni biçimsiz kalır."
    )
    val Blank = Words(
        "Wasm alone: on a phone short of memory the build fails as it starts, and the page stays blank.",
        "Yalnızca Wasm: belleği yetmeyen bir telefonda sürüm açılırken çöker, sayfa boş kalır."
    )
    val Caption = Words(
        "A page in three browsers, with the loader site.js and without it.",
        "Bir sayfa üç tarayıcıda, site.js yükleyicisiyle ve onsuz."
    )
}
