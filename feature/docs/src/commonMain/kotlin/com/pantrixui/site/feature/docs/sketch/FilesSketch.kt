package com.pantrixui.site.feature.docs.sketch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pantrixui.components.container.card.CardVariant
import com.pantrixui.components.container.card.PantrixCard
import com.pantrixui.components.elements.filedrop.PantrixFileDrop
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.files.PantrixFileType
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.content.PhoneFrame
import com.pantrixui.site.core.designsystem.content.SketchChoice
import com.pantrixui.site.core.designsystem.content.SketchControls
import com.pantrixui.site.core.designsystem.content.SketchFigure
import com.pantrixui.site.core.designsystem.content.SketchNote
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.Words

/**
 * The files guide's sketch: Pantrix's own file drop, the platform's real picker behind its button and, on a desktop
 * and in a browser, a real drop. What was picked is read in common code: its name, its size and its type. Nothing is
 * sent anywhere: the file stays on the reader's machine.
 */
@Composable
internal fun FilesSketch() {
    val language = LocalLanguage.current
    var pictures by rememberSaveable { mutableStateOf(false) }
    var picked by remember { mutableStateOf<List<Picked>>(emptyList()) }
    SketchFigure(caption = FilesSay.Caption.of(language)) {
        PhoneFrame {
            SketchPhoneScreen(app = FilesSay.App.of(language), title = FilesSay.Title.of(language)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PantrixFileDrop(
                        onFiles = { files -> picked = files.map { Picked(it.name, it.size, it.mimeType) } },
                        accept = listOf(if (pictures) PantrixFileType.Images else PantrixFileType.Any),
                        label = FilesSay.Label.of(language),
                        modifier = Modifier.fillMaxWidth(),
                        tagKey = "sketch_files"
                    )
                    picked.forEach { file ->
                        PantrixCard(variant = CardVariant.Outlined, modifier = Modifier.fillMaxWidth()) {
                            PantrixText(text = file.name, style = PantrixTheme.typography.label, maxLines = 1)
                            PantrixText(
                                text = listOfNotNull(file.size?.let { PantrixTheme.strings.fileSize(it) }, file.type)
                                    .joinToString(" · "),
                                style = PantrixTheme.typography.footnote,
                                color = PantrixTheme.colors.textSecondary
                            )
                        }
                    }
                }
            }
        }
        SketchControls {
            SketchChoice(
                options = listOf(false to FilesSay.AnyFile.of(language), true to FilesSay.Pictures.of(language)),
                selected = pictures,
                onSelect = { pictures = it },
                tag = "sketch_accept"
            )
            SketchNote((if (picked.isEmpty()) FilesSay.Start else FilesSay.Read).of(language))
        }
    }
}

/** What the sketch shows of a file that was picked or dropped. */
private data class Picked(
    val name: String,
    val size: Long?,
    val type: String
)

/** The sketch's words, in each language. */
private object FilesSay {
    val App = Words("Mail", "Posta")
    val Title = Words("Attach to the reply", "Yanıta ekle")
    val Label = Words("Attach a file", "Bir dosya ekle")
    val AnyFile = Words("Any file", "Her dosya")
    val Pictures = Words("Pictures", "Resimler")
    val Start = Words(
        "Choose a file with the platform’s own picker, or drop one on a desktop and in a browser.",
        "Platformun kendi seçicisiyle bir dosya seç, ya da masaüstünde ve tarayıcıda bir tane bırak."
    )
    val Read = Words(
        "Read in common code: its name, size and type. It stays on this machine.",
        "Ortak kodda okundu: adı, boyutu ve türü. Bu makinede kalıyor."
    )
    val Caption = Words(
        "Pantrix’s own file drop and the platform’s picker, running on this page.",
        "Pantrix'in kendi dosya bırakma alanı ve platformun seçicisi, bu sayfada çalışıyor."
    )
}
