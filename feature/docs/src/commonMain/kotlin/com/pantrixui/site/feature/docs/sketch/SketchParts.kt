@file:Suppress("MagicNumber") // A sketch's sample values, its times, amounts and sizes, are what it shows.

package com.pantrixui.site.feature.docs.sketch

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.InterceptPlatformTextInput
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.a11y.PantrixPlatform
import com.pantrixui.foundation.platform.PlatformType
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.sized
import kotlinx.coroutines.awaitCancellation

/**
 * A sketch's phone screen, in iOS's conventions whatever the site runs on, since the sketch is of an iPhone: its
 * status line with the time and [app], its [title], and under them what the screen shows.
 *
 * @param app the app's name in the status line.
 * @param title the screen's heading.
 * @param modifier the screen's modifier, the whole of it: what hears a tap anywhere on it goes here.
 * @param overlay what stands over the screen from edge to edge: a keyboard at its foot.
 * @param screen what the screen shows under its heading.
 */
@Composable
internal fun SketchPhoneScreen(
    app: String,
    title: String?,
    modifier: Modifier = Modifier,
    overlay: @Composable BoxScope.() -> Unit = {},
    screen: @Composable BoxScope.() -> Unit
) {
    SketchPlatform(PlatformType.Ios) {
        Box(modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().padding(horizontal = 14.dp)) {
                Row(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatusText("9:41")
                    StatusText(app)
                }
                if (title != null) {
                    PantrixText(
                        text = title,
                        style = SiteTheme.type.body.sized(16.sp).copy(fontWeight = FontWeight.ExtraBold),
                        color = PantrixTheme.colors.textPrimary,
                        modifier = Modifier.padding(top = 12.dp, bottom = 10.dp)
                    )
                }
                Box(Modifier.fillMaxWidth().weight(1f), content = screen)
            }
            overlay()
        }
    }
}

/**
 * [content] in [type]'s conventions, the platform a sketch is of, whatever the site runs on; the system's bars stay
 * the site's.
 */
@Composable
internal fun SketchPlatform(
    type: PlatformType,
    content: @Composable () -> Unit
) {
    val host = PantrixTheme.platform
    PantrixTheme(platform = PantrixPlatform.forType(type, isTouch = host.isTouch), followSystemBars = false) {
        content()
    }
}

@Composable
private fun StatusText(text: String) {
    PantrixText(
        text = text,
        style = SiteTheme.type.body.sized(11.sp).copy(lineHeight = 14.sp, fontWeight = FontWeight.ExtraBold),
        color = PantrixTheme.colors.textPrimary
    )
}

/**
 * [content] with the system's keyboard held shut: a sketch draws its own keyboard on its own screen, where the
 * system's would open over the page.
 */
@Composable
internal fun NoSystemKeyboard(content: @Composable () -> Unit) {
    InterceptPlatformTextInput(interceptor = { _, _ -> awaitCancellation() }, content = content)
}

/**
 * A key of a keyboard a sketch draws.
 *
 * @property label what is written on it.
 * @property width how wide it is beside the others in its row.
 * @property letter whether it types, which is drawn lighter than a key that does something.
 */
internal data class KeyCap(
    val label: String,
    val width: Float = 1f,
    val letter: Boolean = true
)

/** The keys of [text], one a character, each a key that types. */
internal fun keys(text: String): List<KeyCap> = text.map { KeyCap("$it") }

/**
 * A keyboard drawn over the bottom of a sketch's screen: its [rows] of keys, each pressed by [onKey]. A press on it
 * is the keyboard's, not the screen's: it is no tap on empty room.
 */
@Composable
internal fun DrawnKeyboard(
    rows: List<List<KeyCap>>,
    onKey: (KeyCap) -> Unit,
    modifier: Modifier = Modifier,
    keyHeight: Int = 30
) {
    val colors = SiteTheme.colors.sketch
    Column(
        modifier
            .fillMaxWidth()
            .background(colors.keyboard)
            .clearAndSetSemantics { }
            .padding(start = 4.dp, end = 4.dp, top = 8.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        rows.forEach { row ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
            ) {
                row.forEach { key -> Key(key, keyHeight, onKey) }
            }
        }
    }
}

@Composable
private fun RowScope.Key(
    key: KeyCap,
    height: Int,
    onKey: (KeyCap) -> Unit
) {
    val colors = SiteTheme.colors.sketch
    val shape = RoundedCornerShape(5.dp)
    Box(
        Modifier
            .weight(key.width)
            .height(height.dp)
            .shadow(1.dp, shape)
            .background(if (key.letter) colors.key else colors.otherKey, shape)
            .pointerInput(key) { detectTapGestures { onKey(key) } },
        contentAlignment = Alignment.Center
    ) {
        PantrixText(
            text = key.label,
            style = SiteTheme.type.body.sized(if (height > 34) 15.sp else 10.sp)
                .copy(lineHeight = 14.sp, fontWeight = FontWeight.Bold),
            color = colors.onKey,
            maxLines = 1
        )
    }
}

/** Whether a field has the focus, as its last focus event said: read when a press lands, before a frame passes. */
internal class FocusFlag {
    var focused: Boolean = false
}
