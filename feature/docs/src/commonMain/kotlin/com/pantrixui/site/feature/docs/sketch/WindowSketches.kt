@file:Suppress("MagicNumber") // A sketch's sample values, its times, amounts and sizes, are what it shows.

package com.pantrixui.site.feature.docs.sketch

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.pantrixui.components.elements.divider.PantrixVerticalDivider
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.components.inputs.textfield.PantrixTextField
import com.pantrixui.foundation.keyboard.PantrixShortcut
import com.pantrixui.foundation.keyboard.onShortcut
import com.pantrixui.foundation.platform.PantrixPointerIcons
import com.pantrixui.foundation.platform.PlatformType
import com.pantrixui.foundation.platform.currentOperatingSystem
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.content.SketchChoice
import com.pantrixui.site.core.designsystem.content.SketchControls
import com.pantrixui.site.core.designsystem.content.SketchFigure
import com.pantrixui.site.core.designsystem.content.SketchNote
import com.pantrixui.site.core.designsystem.content.SketchSwitch
import com.pantrixui.site.core.designsystem.content.WindowFrame
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.Words

/** How wide a window a sketch draws may be. */
private val WINDOW_WIDTH = 560.dp

/**
 * The window title guide's sketch: a mail app of one window and two screens. With `PantrixWindowTitle` the window's
 * bar names the screen shown; without it, it keeps the app's name whatever is shown. The site's own tab is left as it
 * is: the bar is drawn.
 */
@Composable
internal fun WindowTitleSketch() {
    val language = LocalLanguage.current
    var fix by rememberSaveable { mutableStateOf(true) }
    var screen by rememberSaveable { mutableStateOf(0) }
    val screens = listOf(WindowSay.Inbox, WindowSay.Settings)
    val app = "Mail"
    SketchFigure(caption = WindowSay.TitleCaption.of(language)) {
        WindowFrame(
            title = if (fix) "${screens[screen].of(language)} · $app" else app,
            modifier = Modifier.widthIn(max = WINDOW_WIDTH).fillMaxWidth(),
            height = 200.dp
        ) {
            SketchPlatform(PlatformType.Desktop) {
                Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SketchChoice(
                        options = screens.mapIndexed { index, words -> index to words.of(language) },
                        selected = screen,
                        onSelect = { screen = it },
                        tag = "sketch_screen"
                    )
                    PantrixText(text = screens[screen].of(language), style = PantrixTheme.typography.title)
                }
            }
        }
        SketchControls {
            SketchSwitch(code = "PantrixWindowTitle", checked = fix, onToggle = {
                fix = !fix
            }, tag = "sketch_windowTitle")
            SketchNote((if (fix) WindowSay.Named else WindowSay.Same).of(language))
        }
    }
}

/**
 * The pointer icons guide's sketch: two panes and the handle between them, dragged to share the room. With
 * `PantrixPointerIcons.ResizeHorizontal` the pointer over the handle says it can be dragged; without it, it stays an
 * arrow. iOS shows no pointer at all.
 */
@Composable
internal fun PointerIconsSketch() {
    val language = LocalLanguage.current
    var fix by rememberSaveable { mutableStateOf(true) }
    var share by rememberSaveable { mutableFloatStateOf(0.4f) }
    SketchFigure(caption = WindowSay.PointerCaption.of(language)) {
        WindowFrame(
            title = WindowSay.Files.of(language),
            modifier = Modifier.widthIn(max = WINDOW_WIDTH).fillMaxWidth(),
            height = 200.dp
        ) {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val width = with(LocalDensity.current) { maxWidth.toPx() }
                Row(Modifier.fillMaxSize()) {
                    Pane(WindowSay.Folders.of(language), Modifier.weight(share))
                    Box(
                        Modifier
                            .width(HANDLE)
                            .fillMaxHeight()
                            .pointerHoverIcon(if (fix) PantrixPointerIcons.ResizeHorizontal else PointerIcon.Default)
                            .pointerInput(width) {
                                detectHorizontalDragGestures { _, dragAmount ->
                                    share = (share + dragAmount / width).coerceIn(MIN_SHARE, 1f - MIN_SHARE)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        PantrixVerticalDivider()
                    }
                    Pane(WindowSay.Mail.of(language), Modifier.weight(1f - share))
                }
            }
        }
        SketchControls {
            SketchSwitch(
                code = "PantrixPointerIcons.ResizeHorizontal",
                checked = fix,
                onToggle = { fix = !fix },
                tag = "sketch_pointerIcon"
            )
            SketchNote((if (fix) WindowSay.Resize else WindowSay.Arrow).of(language))
        }
    }
}

@Composable
private fun Pane(
    title: String,
    modifier: Modifier
) {
    Column(modifier.fillMaxHeight().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        PantrixText(text = title, style = PantrixTheme.typography.label, color = PantrixTheme.colors.textSecondary)
        repeat(3) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .background(SiteTheme.colors.hover, RoundedCornerShape(6.dp))
                    .padding(8.dp)
            )
        }
    }
}

private val HANDLE = 12.dp
private const val MIN_SHARE = 0.2f

/**
 * The keyboard shortcuts guide's sketch: a window that opens its search with the shortcut written for the reader's
 * own system, `⌘K` or `Ctrl+K`, heard by `Modifier.onShortcut`. A key goes only to what has the focus and around it:
 * with the window's root holding the focus while nothing does, as `shortcutRoot()` does, the shortcut is heard after a
 * click on empty room; without it, only while the field has the focus.
 */
@Composable
internal fun KeyboardShortcutsSketch() {
    val language = LocalLanguage.current
    var fix by rememberSaveable { mutableStateOf(true) }
    var opened by remember { mutableStateOf(0) }
    val system = currentOperatingSystem()
    val shortcut = remember { PantrixShortcut(Key.K, command = true) }
    val root = remember { FocusRequester() }
    val focus = LocalFocusManager.current
    val rootInteractions = remember { MutableInteractionSource() }
    val rootFocused by rootInteractions.collectIsFocusedAsState()
    val field = rememberTextFieldState()
    SketchFigure(caption = WindowSay.ShortcutCaption.of(language)) {
        WindowFrame(
            title = WindowSay.Notes.of(language),
            modifier = Modifier.widthIn(max = WINDOW_WIDTH).fillMaxWidth(),
            height = 200.dp
        ) {
            SketchPlatform(PlatformType.Desktop) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .onShortcut(shortcut) { opened++ }
                        .focusRequester(root)
                        .then(if (fix) Modifier.focusable(interactionSource = rootInteractions) else Modifier)
                        .pointerInput(fix) {
                            detectTapGestures { if (fix) root.requestFocus() else focus.clearFocus() }
                        }
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PantrixTextField(
                        state = field,
                        placeholder = WindowSay.Find.of(language),
                        modifier = Modifier.fillMaxWidth(),
                        tagKey = "sketch_find"
                    )
                    PantrixText(
                        text = if (opened >
                            0
                        ) {
                            WindowSay.Opened.of(language) + " ($opened)"
                        } else {
                            WindowSay.Press.of(language)
                                .replace("%s", shortcut.writtenOn(system))
                        },
                        style = PantrixTheme.typography.body,
                        color = if (opened > 0) PantrixTheme.colors.success.color else PantrixTheme.colors.textSecondary
                    )
                }
            }
        }
        SketchControls {
            SketchSwitch(code = "shortcutRoot()", checked = fix, onToggle = { fix = !fix }, tag = "sketch_shortcutRoot")
            SketchNote(
                when {
                    fix && rootFocused -> WindowSay.RootHolds
                    fix -> WindowSay.ClickRoom
                    else -> WindowSay.FieldOnly
                }.of(language).replace("%s", shortcut.writtenOn(system))
            )
        }
    }
}

/** The window sketches' words, in each language. */
private object WindowSay {
    val Inbox = Words("Inbox", "Gelen kutusu")
    val Settings = Words("Settings", "Ayarlar")
    val Named = Words(
        "The window’s bar names the screen shown, as a desktop’s window list and a browser’s tab do.",
        "Pencerenin çubuğu gösterilen ekranı anıyor, bir masaüstünün pencere listesi ve bir tarayıcı sekmesi gibi."
    )
    val Same = Words(
        "The window keeps the app’s name whatever is shown: two windows of it cannot be told apart.",
        "Pencere, ne gösterilirse gösterilsin uygulamanın adını taşıyor: iki penceresi ayırt edilemiyor."
    )
    val TitleCaption = Words(
        "Pantrix’s own segment button on this page; the window’s bar is drawn, and the page’s tab is left as it is.",
        "Pantrix'in kendi segment düğmesi bu sayfada; pencerenin çubuğu çizildi, sayfanın sekmesine dokunulmadı."
    )
    val Files = Words("Mail", "Posta")
    val Folders = Words("Folders", "Klasörler")
    val Mail = Words("Messages", "İletiler")
    val Resize = Words(
        "Over the handle the pointer turns to a resize arrow: it says the handle can be dragged. Drag it.",
        "Tutamacın üstünde imleç boyutlandırma okuna dönüyor: sürüklenebileceğini söylüyor. Sürükle."
    )
    val Arrow = Words(
        "The pointer stays an arrow over the handle: nothing says it can be dragged.",
        "İmleç tutamacın üstünde ok olarak kalıyor: sürüklenebileceğini söyleyen yok."
    )
    val PointerCaption = Words(
        "Pantrix’s own pointer icons, running on this page; iOS shows no pointer.",
        "Pantrix'in kendi imleçleri bu sayfada çalışıyor; iOS'ta imleç yok."
    )
    val Notes = Words("Notes", "Notlar")
    val Find = Words("Search your notes", "Notlarında ara")
    val Press = Words("Press %s to search.", "Aramak için %s'e bas.")
    val Opened = Words("The search opened", "Arama açıldı")
    val RootHolds = Words(
        "Nothing else has the focus, and the window’s root holds it: %s is heard.",
        "Odak başka hiçbir şeyde değil, pencerenin kökü onu tutuyor: %s duyuluyor."
    )
    val ClickRoom = Words(
        "Click the window’s empty room, then press %s.",
        "Pencerenin boş yerine tıkla, sonra %s'e bas."
    )
    val FieldOnly = Words(
        "Without a root that holds the focus, %s is heard only while the field has it.",
        "Odağı tutan bir kök olmadan %s yalnızca alan odaktayken duyuluyor."
    )
    val ShortcutCaption = Words(
        "Pantrix’s own shortcut and text field, running on this page.",
        "Pantrix'in kendi kısayolu ve metin alanı, bu sayfada çalışıyor."
    )
}
