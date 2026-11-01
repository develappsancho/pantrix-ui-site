@file:Suppress("MagicNumber") // A sketch's sample values, its times, amounts and sizes, are what it shows.

package com.pantrixui.site.feature.docs.sketch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pantrixui.components.elements.button.ButtonColors
import com.pantrixui.components.elements.button.ButtonVariant
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.components.elements.iconbutton.PantrixIconButton
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.diagnostics.PantrixDiagnostics
import com.pantrixui.foundation.diagnostics.PantrixFindings
import com.pantrixui.foundation.icons.PantrixIcons
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.content.PhoneFrame
import com.pantrixui.site.core.designsystem.content.SketchControls
import com.pantrixui.site.core.designsystem.content.SketchFigure
import com.pantrixui.site.core.designsystem.content.SketchNote
import com.pantrixui.site.core.designsystem.content.SketchSwitch
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.Words

/**
 * The diagnostics guide's sketch: a screen with three flaws no unit test sees, a button whose colours do not read,
 * two buttons given one tag and an icon button with no name, drawn under a theme whose diagnostics are on or off.
 * What Pantrix's own checks find while it runs is listed under the phone.
 */
@Composable
internal fun DiagnosticsSketch() {
    val language = LocalLanguage.current
    var on by rememberSaveable { mutableStateOf(true) }
    val findings = remember { PantrixFindings() }
    SketchFigure(caption = DiagnosticsSay.Caption.of(language)) {
        PhoneFrame {
            // A theme of its own per state: the checks run again as the screen is drawn again under it.
            key(on) {
                PantrixTheme(diagnostics = if (on) findings else PantrixDiagnostics.Off) {
                    SketchPhoneScreen(
                        app = DiagnosticsSay.App.of(language),
                        title = DiagnosticsSay.Title.of(language)
                    ) {
                        FlawedScreen(language)
                    }
                }
            }
        }
        SketchControls {
            SketchSwitch(
                code = "PantrixDiagnostics",
                checked = on,
                onToggle = {
                    findings.clear()
                    on = !on
                },
                tag = "sketch_diagnostics"
            )
            // The screen's own flaws: what the theme's colours say of themselves is the site's, not the sketch's.
            val found = if (on) findings.all.filter { it.component != null } else emptyList()
            if (found.isEmpty()) {
                SketchNote((if (on) DiagnosticsSay.Looking else DiagnosticsSay.Off).of(language))
            } else {
                Column(Modifier.widthIn(max = 304.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    found.forEach { finding ->
                        PantrixText(
                            text = "${finding.check.name}: ${finding.message}",
                            style = SiteTheme.type.bodySmall,
                            color = PantrixTheme.colors.textSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FlawedScreen(language: com.pantrixui.site.core.model.Language) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            PantrixText(
                text = DiagnosticsSay.Plan.of(language),
                style = PantrixTheme.typography.body,
                modifier = Modifier.weight(1f)
            )
            // No name: a screen reader says nothing of what it does.
            PantrixIconButton(
                imageVector = PantrixIcons.Info,
                contentDescription = "",
                onClick = {},
                tagKey = "sketch_info"
            )
        }
        // Colours that do not read: a pale grey on white.
        PantrixButton(
            text = DiagnosticsSay.Upgrade.of(language),
            onClick = {},
            fullWidth = true,
            colors = ButtonColors(container = Color(0xFFE7E9ED), content = Color(0xFFB7BBC4)),
            tagKey = "sketch_upgrade"
        )
        // One tag on two buttons: a test that looks for it cannot tell them apart.
        PantrixButton(
            text = DiagnosticsSay.Keep.of(language),
            onClick = {},
            variant = ButtonVariant.Outline,
            fullWidth = true,
            tagKey = "sketch_plan"
        )
        PantrixButton(
            text = DiagnosticsSay.Cancel.of(language),
            onClick = {},
            variant = ButtonVariant.Text,
            fullWidth = true,
            tagKey = "sketch_plan"
        )
    }
}

/** The sketch's words, in each language. */
private object DiagnosticsSay {
    val App = Words("Settings", "Ayarlar")
    val Title = Words("Your plan", "Planınız")
    val Plan = Words("Basic, monthly", "Temel, aylık")
    val Upgrade = Words("Upgrade", "Yükselt")
    val Keep = Words("Keep the plan", "Planı koru")
    val Cancel = Words("Cancel the plan", "Planı iptal et")
    val Looking = Words("The checks run as the screen is drawn.", "Denetimler ekran çizilirken çalışır.")
    val Off = Words(
        "Diagnostics are off, as in a release build: the flaws are there, and nothing says so.",
        "Tanılama kapalı, bir yayın derlemesindeki gibi: kusurlar orada, söyleyen yok."
    )
    val Caption = Words(
        "Pantrix’s own diagnostics, running on this page.",
        "Pantrix'in kendi tanılaması, bu sayfada çalışıyor."
    )
}
