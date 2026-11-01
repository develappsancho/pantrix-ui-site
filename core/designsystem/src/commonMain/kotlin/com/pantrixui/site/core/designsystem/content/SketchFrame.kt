package com.pantrixui.site.core.designsystem.content

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.segmentbutton.PantrixSegmentButton
import com.pantrixui.components.elements.segmentbutton.SegmentOption
import com.pantrixui.components.elements.switchbutton.PantrixSwitchButton
import com.pantrixui.components.elements.switchbutton.SwitchStyle
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.siteClickable
import com.pantrixui.site.core.designsystem.sized

/**
 * A guide's live sketch as its design lays it out: what runs, the controls under it, and the caption that says what
 * it is.
 *
 * @param caption the line under it.
 * @param content what runs, then its controls, one under the other.
 */
@Composable
fun SketchFigure(
    caption: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        content()
        PantrixText(
            text = caption,
            style = SiteTheme.type.body.sized(12.sp),
            color = SiteTheme.colors.muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = CONTROLS_WIDTH)
        )
    }
}

/**
 * A phone, drawn: a dark edge around a screen in the page's own surface, 264 by 452, and a little smaller on a phone,
 * where it stands under the text.
 *
 * @param modifier the phone's modifier.
 * @param screen what its screen shows.
 */
@Composable
fun PhoneFrame(
    modifier: Modifier = Modifier,
    screen: @Composable BoxScope.() -> Unit
) {
    val phone = SiteTheme.layout == SiteLayout.Phone
    val shape = RoundedCornerShape(36.dp)
    Box(
        modifier
            .size(if (phone) 250.dp else 264.dp, if (phone) 430.dp else 452.dp)
            .shadow(16.dp, shape, clip = false)
            .clip(shape)
            .background(PantrixTheme.colors.surface)
            .border(8.dp, SiteTheme.colors.sketch.frame, shape)
            .padding(8.dp),
        content = screen
    )
}

/**
 * A desktop's window, drawn: a bar with its three buttons and its [title], over a body in the page's surface. A
 * sketch of something seen on a desktop or in a browser stands in one.
 *
 * @param title what the window's bar says.
 * @param modifier the window's modifier: its width, where a sketch gives it one.
 * @param height how tall its body is, or null for as tall as what it shows.
 * @param body what the window shows.
 */
@Composable
fun WindowFrame(
    title: String,
    modifier: Modifier = Modifier,
    height: Dp? = 300.dp,
    body: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier
            .shadow(12.dp, shape, clip = false)
            .clip(shape)
            .background(PantrixTheme.colors.surface)
            .border(1.dp, PantrixTheme.colors.border, shape)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(36.dp)
                .background(SiteTheme.colors.surfaceAlt)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                Modifier.align(Alignment.CenterStart).clearAndSetSemantics { },
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                WINDOW_BUTTONS.forEach { color -> Box(Modifier.size(10.dp).background(color, CircleShape)) }
            }
            PantrixText(
                text = title,
                style = SiteTheme.type.bodySmall.sized(12.5.sp).copy(fontWeight = FontWeight.SemiBold),
                color = SiteTheme.colors.muted,
                maxLines = 1
            )
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(PantrixTheme.colors.border))
        Box(Modifier.fillMaxWidth().then(if (height != null) Modifier.height(height) else Modifier), content = body)
    }
}

/**
 * The choice under a sketch between a few of its states: a row of segments.
 *
 * @param options each state's value and what its segment says.
 * @param selected the state shown.
 * @param onSelect what choosing one does.
 * @param tag its automation tag.
 */
@Composable
fun <T> SketchChoice(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    tag: String
) {
    PantrixSegmentButton(
        options = options.map { (value, label) -> SegmentOption(value, label) },
        selected = selected,
        onSelect = onSelect,
        fullWidth = false,
        tagKey = tag
    )
}

/**
 * The switch under a sketch that turns what it shows on and off: a rounded button with a switch and the code it
 * stands for.
 *
 * @param code what it turns on, as code: `clearFocusOnTap()`.
 * @param checked whether it is on.
 * @param onToggle what pressing it does.
 * @param tag its automation tag.
 */
@Composable
fun SketchSwitch(
    code: String,
    checked: Boolean,
    onToggle: () -> Unit,
    tag: String
) {
    val shape = CircleShape
    Row(
        Modifier
            .heightIn(min = 40.dp)
            .border(1.dp, PantrixTheme.colors.border, shape)
            .siteClickable(tag, Role.Switch, shape, onClick = onToggle)
            .semantics {
                contentDescription = code
                toggleableState = ToggleableState(checked)
            }
            .padding(start = 8.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Drawn, not pressed: the button around it is what a press and a screen reader reach.
        PantrixSwitchButton(
            checked = checked,
            onCheckedChange = null,
            style = SwitchStyle(
                trackWidth = 36.dp,
                trackHeight = 22.dp,
                thumbSizeOff = 16.dp,
                thumbSizeOn = 16.dp,
                thumbSizePressed = 16.dp
            ),
            modifier = Modifier.clearAndSetSemantics { },
            tagKey = "${tag}_track"
        )
        PantrixText(
            // A long name that does not fit breaks after a dot, between two of its parts, never inside one.
            text = code.replace(".", ".\u200B"),
            style = SiteTheme.type.inlineCode.sized(12.5.sp),
            color = PantrixTheme.colors.textPrimary,
            modifier = Modifier.clearAndSetSemantics { }
        )
    }
}

/**
 * What a sketch says has just happened, under its controls: read out by a screen reader as it changes.
 *
 * @param text what it says.
 */
@Composable
fun SketchNote(text: String) {
    PantrixText(
        text = text,
        style = SiteTheme.type.body.sized(13.sp).copy(lineHeight = 19.5.sp),
        color = PantrixTheme.colors.textSecondary,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .widthIn(max = CONTROLS_WIDTH)
            .heightIn(min = 40.dp)
            .semantics { liveRegion = LiveRegionMode.Polite }
    )
}

/** A window's three buttons on its bar: close, shrink, fill. */
private val WINDOW_BUTTONS = listOf(Color(0xFFFF5F57), Color(0xFFFEBC2E), Color(0xFF28C840))

/** What a sketch's controls and caption may span: a little wider than its phone. */
private val CONTROLS_WIDTH = 304.dp

/** A sketch's controls, one under another. */
@Composable
fun SketchControls(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.widthIn(max = CONTROLS_WIDTH),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}
