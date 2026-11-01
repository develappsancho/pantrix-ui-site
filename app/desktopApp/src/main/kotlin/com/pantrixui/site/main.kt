// The entry point keeps the name the template gave it, main.kt: the desktop plugin runs MainKt.
@file:Suppress("ktlint:standard:filename")

package com.pantrixui.site

import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.jetbrains.skia.Image

fun main() {
    startSiteKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            // The page on show names the window once it is drawn: `Installation · Pantrix UI`.
            title = "Pantrix UI",
            state = rememberWindowState(width = 1440.dp, height = 900.dp),
            icon = remember { appIcon() }
        ) {
            SiteApp()
        }
    }
}

/** The window's icon, seen in the title bar and the task bar on Windows and Linux; macOS's Dock takes the `.icns`. */
private fun appIcon(): Painter {
    val bytes = checkNotNull(Thread.currentThread().contextClassLoader.getResourceAsStream("PantrixUI.png")) {
        "PantrixUI.png is not on the classpath"
    }.use { it.readBytes() }
    return BitmapPainter(Image.makeFromEncoded(bytes).toComposeImageBitmap())
}
