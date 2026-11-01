package com.pantrixui.site.feature.adaptive

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.segmentbutton.PantrixSegmentButton
import com.pantrixui.components.elements.segmentbutton.SegmentOption
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.adaptive.LocalPantrixLayouts
import com.pantrixui.foundation.adaptive.PantrixWindow
import com.pantrixui.foundation.adaptive.PantrixWindowFrame
import com.pantrixui.foundation.adaptive.isNarrow
import com.pantrixui.foundation.adaptive.isShort
import com.pantrixui.foundation.adaptive.paneCount
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.content.dotted
import com.pantrixui.site.core.designsystem.content.live
import com.pantrixui.site.core.designsystem.device.DeviceChrome
import com.pantrixui.site.core.designsystem.device.DeviceFrame
import com.pantrixui.site.core.designsystem.device.SimDevice
import com.pantrixui.site.core.designsystem.device.SimFold
import com.pantrixui.site.core.designsystem.device.outerSize
import com.pantrixui.site.core.designsystem.device.windowOf
import com.pantrixui.site.core.designsystem.siteClickable
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.Words
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * The window simulator: the windows to choose from, an app drawn by Pantrix's own scaffolds in the window chosen, at
 * its own size and drawn smaller to fit, and what the window says of itself and why the app looks as it does.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun Simulator(
    state: AdaptiveUiState,
    onAction: (AdaptiveAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val phone = layout == SiteLayout.Phone
    val shape = RoundedCornerShape(if (phone) 0.dp else 24.dp)
    val density = LocalDensity.current
    val window = remember(state.device, density) { windowOf(state.device, density) }
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, SiteTheme.colors.hairline, shape)
            .background(SiteTheme.colors.canvas)
    ) {
        Column(
            Modifier.fillMaxWidth().background(
                PantrixTheme.colors.surface
            ).padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val chips: @Composable () -> Unit = {
                SimDevice.entries.forEach { device ->
                    DeviceChip(device, device == state.device) { onAction(AdaptiveAction.PickDevice(device)) }
                }
            }
            // A phone's windows are a row that scrolls sideways; a wider page's wrap.
            if (phone) {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) { chips() }
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) { chips() }
            }
            PantrixSegmentButton(
                options = SimApp.entries.map { app ->
                    SegmentOption(
                        app,
                        (
                            if (app ==
                                SimApp.Inbox
                            ) {
                                AdaptiveSay.Inbox
                            } else {
                                AdaptiveSay.VideoCall
                            }
                        ).of(language),
                        tagKey = app.name.lowercase()
                    )
                },
                selected = state.app,
                onSelect = { onAction(AdaptiveAction.PickApp(it)) },
                fullWidth = phone,
                tagKey = "simApp"
            )
        }
        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .height(
                    when (layout) {
                        SiteLayout.Desktop -> 600.dp
                        SiteLayout.Tablet -> 520.dp
                        SiteLayout.Phone -> 440.dp
                    }
                )
                .dotted(PantrixTheme.colors.borderHover, step = 18.dp)
                .padding(if (phone) 12.dp else 20.dp),
            contentAlignment = Alignment.Center
        ) {
            val outer = DeviceChrome.Device.outerSize(state.device)
            val outerWidth = outer.width.value
            val outerHeight = outer.height.value
            val scale = min(min(maxWidth.value / outerWidth, maxHeight.value / outerHeight), 1f)
            // A new window is a new app: the open message of one window is not carried to the next.
            key(state.device, state.app) {
                Scaled(window, state, scale)
            }
        }
        Readout(window, state, Modifier.fillMaxWidth().background(PantrixTheme.colors.surface).padding(16.dp))
    }
}

/** A window's choice: what it is, and its size. */
@Composable
private fun DeviceChip(
    device: SimDevice,
    chosen: Boolean,
    onClick: () -> Unit
) {
    val language = LocalLanguage.current
    val colors = PantrixTheme.colors
    val shape = RoundedCornerShape(10.dp)
    Row(
        Modifier
            .height(if (SiteTheme.layout == SiteLayout.Desktop) 36.dp else 40.dp)
            .background(if (chosen) colors.primary.container else colors.surface, shape)
            .border(1.dp, if (chosen) SiteTheme.colors.accentLine else colors.border, shape)
            .semantics { selected = chosen }
            .siteClickable(
                tag = "device_${device.name.lowercase()}",
                role = Role.RadioButton,
                shape = shape,
                onClick = onClick
            )
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val ink = if (chosen) colors.primary.onContainer else colors.textSecondary
        PantrixText(text = AdaptiveSay.device(device).of(language), style = SiteTheme.type.button, color = ink)
        PantrixText(
            text = "${device.width}×${device.height}",
            style = SiteTheme.type.bodySmall.copy(fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold),
            color = ink.copy(alpha = SIZE_ALPHA)
        )
    }
}

/**
 * The device at its own size in dp, its frame around its screen, drawn [scale] times as large: Pantrix lays the app
 * out for the window's own width and height, and the picture of it is made smaller, the fold with it.
 */
@Composable
private fun Scaled(
    window: PantrixWindow,
    state: AdaptiveUiState,
    scale: Float
) {
    val language = LocalLanguage.current
    val device = state.device
    DeviceFrame(device, window, scale) {
        when (state.app) {
            SimApp.Inbox -> InboxApp(language, sidebar = navigationOf(device) == SimNavigation.Sidebar)
            SimApp.VideoCall -> VideoApp(language)
        }
    }
}

/** What the window says of itself, read from Pantrix, and why the app is laid out as it is. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Readout(
    window: PantrixWindow,
    state: AdaptiveUiState,
    modifier: Modifier
) {
    val language = LocalLanguage.current
    val device = state.device
    val measures = LocalPantrixLayouts.current.forWidth(window.widthClass)
    val navigation = navigationOf(device)
    val posture = when {
        window.posture.isBook -> "Book"
        window.posture.isTabletop -> "Tabletop"
        else -> "Flat"
    }
    val navigationName = when {
        state.app == SimApp.VideoCall -> "none"
        navigation == SimNavigation.Bottom -> "Bottom · bar"
        navigation == SimNavigation.Sidebar -> "Start · sidebar"
        else -> "Start · rail"
    }
    val values = listOf(
        Triple("widthClass", window.widthClass.name, true),
        Triple("heightClass", window.heightClass.name, true),
        Triple("posture", posture, true),
        Triple("isNarrow", "${window.isNarrow}", false),
        Triple("isShort", "${window.isShort}", false),
        Triple("paneCount()", "${window.paneCount()}", false),
        Triple("layout.margin", "${measures.margin.value.roundToInt()}dp", false),
        Triple("layout.gutter", "${measures.gutter.value.roundToInt()}dp", false),
        Triple("columns", "${measures.columns}", false),
        Triple("navigation", navigationName, false)
    )
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            values.forEach { (name, value, accent) -> Value(name, value, accent) }
        }
        PantrixText(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = PantrixTheme.colors.textPrimary, fontWeight = FontWeight.Bold)) {
                    append(AdaptiveSay.WhyItLooks.of(language))
                }
                append(why(state).of(language))
            },
            style = SiteTheme.type.bodySmall.copy(fontSize = 14.sp),
            color = PantrixTheme.colors.textSecondary
        )
    }
}

@Composable
private fun Value(
    name: String,
    value: String,
    accent: Boolean
) {
    val shape = RoundedCornerShape(9.dp)
    val mono = SiteTheme.type.inlineCode.copy(fontSize = 12.5.sp)
    Row(
        Modifier
            .background(SiteTheme.colors.surfaceAlt, shape)
            .border(1.dp, PantrixTheme.colors.border, shape)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PantrixText(text = name, style = mono, color = SiteTheme.colors.muted)
        PantrixText(
            text = value,
            style = mono.copy(fontWeight = FontWeight.Bold),
            color = if (accent) PantrixTheme.colors.primary.onContainer else PantrixTheme.colors.textPrimary
        )
    }
}

/** Why the simulator's app looks as it does in the window chosen. */
internal fun why(state: AdaptiveUiState): Words {
    val device = state.device
    return when {
        state.app == SimApp.VideoCall -> {
            when (device.fold) {
                SimFold.Table -> AdaptiveSay.WhyVideoTable
                SimFold.Book -> AdaptiveSay.WhyVideoBook
                SimFold.Flat -> AdaptiveSay.WhyVideoFlat
            }
        }

        device.fold == SimFold.Table -> {
            AdaptiveSay.WhyInboxTable
        }

        device.fold == SimFold.Book -> {
            AdaptiveSay.WhyInboxBook
        }

        else -> {
            val navigation = navigationOf(device)
            val room = device.width - navigationWidth(navigation)
            AdaptiveSay.whyInbox(
                room = room,
                navigation = when (navigation) {
                    SimNavigation.Bottom -> AdaptiveSay.BottomBar
                    SimNavigation.Sidebar -> AdaptiveSay.Sidebar
                    SimNavigation.Rail -> AdaptiveSay.Rail
                },
                boxClass = widthClassName(room),
                panes = panesIn(room, device.fold),
                short = device.height < SHORT_BELOW
            )
        }
    }
}

/** How faint a window's size is beside its name. */
private const val SIZE_ALPHA = 0.7f

/** Where the Compact height class ends. */
private const val SHORT_BELOW = 480
