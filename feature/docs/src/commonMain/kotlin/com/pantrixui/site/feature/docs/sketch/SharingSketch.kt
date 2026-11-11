package com.pantrixui.site.feature.docs.sketch

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pantrixui.components.container.card.CardVariant
import com.pantrixui.components.container.card.PantrixCard
import com.pantrixui.components.elements.button.ButtonVariant
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.platform.ShareResult
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.content.PhoneFrame
import com.pantrixui.site.core.designsystem.content.SketchChoice
import com.pantrixui.site.core.designsystem.content.SketchControls
import com.pantrixui.site.core.designsystem.content.SketchFigure
import com.pantrixui.site.core.designsystem.content.SketchNote
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.Words

/**
 * The sharing guide's sketch: a link and its share button, on the platform chosen. Where the platform has a sheet,
 * the button opens it and the app is told what became of the text: Android hands it to a chooser that says no more
 * than that it started, iOS and a browser say whether it was sent. Where it has none, as on the desktop,
 * `rememberShareSheet()` is null and the screen offers to copy the link in its place.
 */
@Composable
internal fun SharingSketch() {
    val language = LocalLanguage.current
    var platform by rememberSaveable { mutableStateOf(ShareWhere.Android) }
    var open by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<ShareResult?>(null) }
    SketchFigure(caption = ShareSay.Caption.of(language)) {
        PhoneFrame {
            SketchPhoneScreen(
                app = ShareSay.App.of(language),
                title = ShareSay.Title.of(language),
                overlay = {
                    if (open) {
                        Sheet(platform = platform) { sent ->
                            open = false
                            // iOS and a browser say whether it was sent; Android has said all it will.
                            if (platform != ShareWhere.Android) {
                                result = if (sent) ShareResult.Shared else ShareResult.Cancelled
                            }
                        }
                    }
                }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PantrixCard(variant = CardVariant.Outlined, modifier = Modifier.fillMaxWidth()) {
                        PantrixText(text = ShareSay.Recipe.of(language), style = PantrixTheme.typography.title)
                        PantrixText(text = "shop.example/r/42", color = PantrixTheme.colors.textSecondary)
                    }
                    if (platform.hasSheet) {
                        PantrixButton(
                            text = ShareSay.Share.of(language),
                            onClick = {
                                open = true
                                // Android's chooser says it started, and no more.
                                result = if (platform == ShareWhere.Android) ShareResult.Started else null
                            },
                            fullWidth = true,
                            tagKey = "sketch_share"
                        )
                    } else {
                        PantrixButton(
                            text = ShareSay.CopyLink.of(language),
                            onClick = {},
                            variant = ButtonVariant.Outline,
                            fullWidth = true,
                            tagKey = "sketch_copy_link"
                        )
                    }
                }
            }
        }
        SketchControls {
            SketchChoice(
                options = ShareWhere.entries.map { it to it.label.of(language) },
                selected = platform,
                onSelect = {
                    platform = it
                    open = false
                    result = null
                },
                tag = "sketch_platform"
            )
            SketchNote(
                when {
                    !platform.hasSheet -> ShareSay.NoSheet
                    result == null -> ShareSay.Start
                    else -> ShareSay.told(result.toString())
                }.of(language)
            )
        }
    }
}

/** The platform's sheet over the screen: where the text can go, and a way out of it. */
@Composable
private fun Sheet(
    platform: ShareWhere,
    onChoose: (sent: Boolean) -> Unit
) {
    val language = LocalLanguage.current
    Box(Modifier.fillMaxSize().background(PantrixTheme.colors.scrim)) {
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(PantrixTheme.colors.surface, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            PantrixText(text = platform.sheetTitle.of(language), style = PantrixTheme.typography.footnote)
            listOf(ShareSay.Messages, ShareSay.Mail).forEach { app ->
                PantrixText(
                    text = app.of(language),
                    modifier = Modifier.fillMaxWidth().clickable { onChoose(true) }.padding(vertical = 8.dp)
                )
            }
            PantrixText(
                text = ShareSay.Close.of(language),
                color = PantrixTheme.colors.primary.color,
                modifier = Modifier.fillMaxWidth().clickable { onChoose(false) }.padding(vertical = 8.dp)
            )
        }
    }
}

/** Where the sketch's share happens, and whether that platform has a sheet. */
private enum class ShareWhere(
    val label: Words,
    val hasSheet: Boolean,
    val sheetTitle: Words
) {
    Android(Words("Android", "Android"), hasSheet = true, Words("Share with", "Şununla paylaş")),
    Ios(Words("iOS", "iOS"), hasSheet = true, Words("Share", "Paylaş")),
    Web(Words("Web", "Web"), hasSheet = true, Words("Share", "Paylaş")),
    Desktop(Words("Desktop", "Masaüstü"), hasSheet = false, Words("", ""))
}

private object ShareSay {
    val App = Words("Recipes", "Tarifler")
    val Title = Words("Lemon tart", "Limonlu tart")
    val Recipe = Words("Lemon tart, for six", "Limonlu tart, altı kişilik")
    val Share = Words("Share the recipe", "Tarifi paylaş")
    val CopyLink = Words("Copy the link", "Bağlantıyı kopyala")
    val Messages = Words("Messages", "Mesajlar")
    val Mail = Words("Mail", "Posta")
    val Close = Words("Cancel", "Vazgeç")
    val Caption = Words(
        "One call in common code; each platform answers with what it knows.",
        "Ortak kodda tek bir çağrı; her platform bildiği kadarını söyler."
    )
    val Start = Words("Press the button to share the link.", "Bağlantıyı paylaşmak için düğmeye bas.")
    val NoSheet = Words(
        "rememberShareSheet() is null here: the screen offers to copy the link instead.",
        "rememberShareSheet() burada null: ekran onun yerine bağlantıyı kopyalamayı sunar."
    )

    fun told(result: String) = Words("The app is told: ShareResult.$result", "Uygulamaya söylenen: ShareResult.$result")
}
