// The entry point keeps the name the template gave it, main.kt: the desktop plugin runs MainKt.
@file:Suppress("ktlint:standard:filename")

package com.pantrixui.site

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() {
    startSiteKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            // The page on show names the window once it is drawn: `Installation · Pantrix UI`.
            title = "Pantrix UI",
            state = rememberWindowState(width = 1440.dp, height = 900.dp)
        ) {
            SiteApp()
        }
    }
}
