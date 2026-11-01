package com.pantrixui.site.core.designsystem.device

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.icon.PantrixIcon
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.adaptive.PantrixWindow
import com.pantrixui.foundation.adaptive.PantrixWindowFrame
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.SiteIcons
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.content.live
import kotlin.math.roundToInt

/**
 * What a device's screen is drawn in: a device's dark frame, a Mac's window with its three lights, or a browser's
 * window with its address.
 */
enum class DeviceChrome { Device, MacWindow, Browser }

/**
 * How large [device] is drawn in [this] chrome, its frame or its window's bars around its screen, in dp: what a
 * picture of it is scaled from to fit.
 */
fun DeviceChrome.outerSize(device: SimDevice): DpSize = when (this) {
    DeviceChrome.Device -> DpSize((device.width + FRAME * 2).dp, (device.height + FRAME * 2).dp)
    DeviceChrome.MacWindow -> DpSize(device.width.dp, (device.height + MAC_BAR).dp)
    DeviceChrome.Browser -> DpSize(device.width.dp, (device.height + BROWSER_BARS).dp)
}

/**
 * [device] at its own size in dp, in [chrome], drawn [scale] times as large: Pantrix lays [content] out for the
 * window's own width and height, [window], and the picture of it is made smaller, a foldable's hinge with it. A browser
 * shows [address] in its bar.
 *
 * What it draws is a live app: the static page of the page it stands on leaves its words out.
 *
 * @param device the device, its size and how it is held.
 * @param window the window Pantrix lays the app out for: [windowOf] the device.
 * @param scale how large the picture is drawn, a share of the device's size.
 * @param modifier the picture's modifier.
 * @param chrome what the screen is drawn in.
 * @param address what a browser's bar says.
 * @param content the app.
 */
@Composable
fun DeviceFrame(
    device: SimDevice,
    window: PantrixWindow,
    scale: Float,
    modifier: Modifier = Modifier,
    chrome: DeviceChrome = DeviceChrome.Device,
    address: String = "",
    content: @Composable () -> Unit
) {
    val outer = chrome.outerSize(device)
    val screen = @Composable { screenModifier: Modifier ->
        PantrixWindowFrame(
            window,
            screenModifier
                .live()
                .size(device.width.dp, device.height.dp)
                .background(PantrixTheme.colors.background)
                .then(if (device.fold == SimFold.Flat) Modifier else Modifier.hinge(device.fold == SimFold.Book))
        ) { content() }
    }
    Box(
        modifier.layout { measurable, _ ->
            val width = outer.width.roundToPx()
            val height = outer.height.roundToPx()
            val placeable = measurable.measure(Constraints.fixed(width, height))
            layout((width * scale).roundToInt(), (height * scale).roundToInt()) {
                placeable.placeWithLayer(0, 0) {
                    scaleX = scale
                    scaleY = scale
                    transformOrigin = TransformOrigin(0f, 0f)
                }
            }
        }
    ) {
        when (chrome) {
            DeviceChrome.Device -> Box(
                Modifier
                    .clip(RoundedCornerShape((device.corner + FRAME).dp))
                    .background(DEVICE_FRAME)
                    .padding(FRAME.dp)
            ) { screen(Modifier.clip(RoundedCornerShape(device.corner.dp))) }

            DeviceChrome.MacWindow -> Window {
                Bar(MAC_BAR) { Lights() }
                screen(Modifier)
            }

            DeviceChrome.Browser -> Window {
                Bar(BROWSER_BARS) {
                    Lights()
                    AddressBar(address, Modifier.weight(1f))
                }
                screen(Modifier)
            }
        }
    }
}

/** A desktop's window: rounded, its edge and a shadow's line around its bars and its screen. */
@Composable
private fun Window(content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(WINDOW_CORNER)
    Column(
        Modifier.clip(shape).background(PantrixTheme.colors.background).border(1.dp, PantrixTheme.colors.border, shape)
    ) {
        content()
    }
}

/** A window's bar, [height] tall, what it holds in a row from its start. */
@Composable
private fun Bar(
    height: Int,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit
) {
    Row(
        Modifier.fillMaxWidth().height(height.dp).background(SiteTheme.colors.surfaceAlt).padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

/** A Mac's three lights. */
@Composable
private fun Lights() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        LIGHTS.forEach { light -> Box(Modifier.size(12.dp).background(light, CircleShape)) }
    }
}

/** A browser's address bar: a lock and the address. */
@Composable
private fun AddressBar(
    address: String,
    modifier: Modifier
) {
    val colors = PantrixTheme.colors
    Row(
        modifier
            .widthIn(max = 640.dp)
            .height(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(colors.background)
            .border(1.dp, colors.border, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PantrixIcon(SiteIcons.Lock, contentDescription = null, tint = SiteTheme.colors.muted, size = 14.dp)
        PantrixText(
            text = address,
            style = SiteTheme.type.bodySmall.copy(fontSize = 15.sp),
            color = colors.textSecondary,
            maxLines = 1
        )
    }
}

/** The hinge's strip drawn over a half-open foldable's screen: a shade across its middle, dashed at its edges. */
private fun Modifier.hinge(vertical: Boolean): Modifier = drawWithContent {
    drawContent()
    val half = HINGE.toPx() / 2
    val shade = listOf(Color.Transparent, HINGE_SHADE, Color.Transparent)
    if (vertical) {
        val middle = size.width / 2
        drawRect(
            Brush.horizontalGradient(shade, middle - half, middle + half),
            Offset(middle - half, 0f),
            Size(half * 2, size.height)
        )
    } else {
        val middle = size.height / 2
        drawRect(
            Brush.verticalGradient(shade, middle - half, middle + half),
            Offset(0f, middle - half),
            Size(
                size.width,
                half * 2
            )
        )
    }
}

/** How wide a device's dark frame is around its screen. */
private const val FRAME = 10

/** How tall a Mac window's bar is, and a browser's bars. */
private const val MAC_BAR = 36
private const val BROWSER_BARS = 64

private val WINDOW_CORNER = 12.dp
private val DEVICE_FRAME = Color(0xFF181D2B)
private val HINGE_SHADE = Color(0x4705070A)
private val LIGHTS = listOf(Color(0xFFFF5F57), Color(0xFFFEBC2E), Color(0xFF28C840))
