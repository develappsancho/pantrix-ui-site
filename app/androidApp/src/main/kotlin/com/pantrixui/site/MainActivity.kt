package com.pantrixui.site

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

/**
 * The site as the Android app shows it: the whole of it, drawn edge to edge under the system's bars. The bars' icons
 * follow the site's theme button by themselves: the site's outermost Pantrix theme sets them.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        startSiteKoin()
        setContent { SiteApp() }
    }
}
