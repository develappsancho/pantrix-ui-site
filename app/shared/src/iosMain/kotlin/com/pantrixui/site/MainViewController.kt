package com.pantrixui.site

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/** The site as the iOS app shows it: what its Swift entry point puts on screen. */
@Suppress("ktlint:standard:function-naming", "FunctionNaming") // Swift calls it by this name, as a type's would be.
fun MainViewController(): UIViewController {
    startSiteKoin()
    return ComposeUIViewController { SiteApp() }
}
