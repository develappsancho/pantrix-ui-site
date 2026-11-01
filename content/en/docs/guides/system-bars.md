---
title: System bars that follow the theme
description: Why, in a Compose Multiplatform app with a dark theme switch of its own, the parts the system draws, Android's status and navigation bars and iOS's status bar, keyboard and controls, stay in the system's dark mode, what the library gives for it, and how it is tested. The problem is not Pantrix's own: it shows the same way in every Compose Multiplatform app that lets its reader choose its theme.
---

What is said here of Compose and the platforms was read in androidx.activity's `EdgeToEdge.kt`, androidx.core's `WindowInsetsControllerCompat` and UIKit's documentation of `UIViewController.overrideUserInterfaceStyle`. What could not be verified is counted in section 7.

## 1. The problem {#problem}

:::aside system-bars
### Symptom

*   The app's own switch turns the theme dark; the system stays in its light mode. On Android the status bar's clock and icons stay dark and cannot be read on the dark ground, and so do the navigation bar's buttons.
*   The other way round happens too: the system is dark, the app light. The icons stay light and are lost on a white ground.
*   On iOS the status bar's text follows the system's mode, and stays dark on the app's dark ground. The keyboard that opens for a field and the system's own controls (a date wheel, the share sheet) are in the system's mode as well.
:::

### Cause

How the bars and the system's parts look is not in the app's theme but in an object of the platform:

:::table
| Platform | What sets it | By default |
| :--- | :--- | :--- |
| Android | `WindowInsetsControllerCompat.isAppearanceLightStatusBars` and `isAppearanceLightNavigationBars` (a bar with dark icons is called a light bar) | `enableEdgeToEdge()` sets them by the system's night mode (`SystemBarStyle.auto`) |
| iOS | The view controller's `traitCollection.userInterfaceStyle`; the status bar, the keyboard and UIKit's controls follow it | The system's mode |
| Desktop | The window's frame is the operating system's | The system's theme |
| Web | The browser's own bars | The browser's theme |
:::

No API in Compose's common code reaches any of them: `isSystemInDarkTheme()` only reads the system's mode, and tells the system nothing of the app's choice.

## 2. The solution {#fix}

The outermost theme is the one place that knows whether the screen is dark or light. After every composition it tells the platform: on Android it sets the window's bar icons, on iOS the interface style of the app's view controller. A theme inside it (a dark card on a light screen) leaves the bars alone: the window's bars belong to the screen as a whole.

## 3. With Pantrix {#with}

`PantrixTheme` does it by itself; the app writes nothing for it. The dark its reader chose is given to the theme, and the bars follow:

```kotlin
val dark = settings.dark ?: isSystemInDarkTheme()
PantrixTheme(preset = Brand, colors = rememberPantrixColors(Brand, darkTheme = dark)) {
    App()
}
```

*   **Android:** the app still calls `enableEdgeToEdge()`; the bars are see-through and the app draws behind them. The theme sets the icons `enableEdgeToEdge` chose by the system's mode again by its own ground, at every change.
*   **iOS:** the theme's dark is written to the `ComposeUIViewController`'s `overrideUserInterfaceStyle`. The status bar, the keyboard and the system's controls open in the app's theme.
*   **Desktop and web:** it does nothing.
*   **Dark** is read from the theme's ground: the theme is dark where its `background` is nearer black than white.

### 3.1. An app that sets its bars itself

An app that paints its bars in colours of its own (and does not draw edge to edge), or chooses their icons itself, tells the theme to leave them:

```kotlin
PantrixTheme(preset = Brand, followSystemBars = false) {
    App()
}
```

Pantrix's own layers (a sheet, a dialog) still read their window's icons from the app's window: the app's choice reaches them too.

## 4. Without Pantrix {#without}

### 4.1. Common code (`commonMain`)

```kotlin
/** Makes the parts the system draws follow the [dark] theme; called once, inside the outermost theme. */
@Composable
expect fun FollowSystemBars(dark: Boolean)
```

At the app's root, right inside its theme:

```kotlin
MyTheme(dark = dark) {
    FollowSystemBars(dark)
    App()
}
```

### 4.2. The platforms' counterparts

```kotlin
// androidMain
@Composable
actual fun FollowSystemBars(dark: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return
    SideEffect {
        val window = view.context.activity()?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).apply {
            // Android calls a bar with dark icons a light bar.
            isAppearanceLightStatusBars = !dark
            isAppearanceLightNavigationBars = !dark
        }
    }
}

private tailrec fun Context.activity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.activity()
    else -> null
}
```

```kotlin
// iosMain
@Composable
actual fun FollowSystemBars(dark: Boolean) {
    val controller = LocalUIViewController.current
    SideEffect {
        controller.overrideUserInterfaceStyle =
            if (dark) UIUserInterfaceStyle.UIUserInterfaceStyleDark else UIUserInterfaceStyle.UIUserInterfaceStyleLight
    }
}
```

```kotlin
// jvmMain and webMain
@Composable
actual fun FollowSystemBars(dark: Boolean) = Unit
```

## 5. Testing {#testing}

On Android the bars' state is read under Robolectric: a host test sets the theme dark and light and asks the activity's window for its icons.

```kotlin
@Test
fun the_bars_icons_follow_the_theme_as_it_changes() = runComposeUiTest {
    var dark by mutableStateOf(true)
    var activity: Activity? = null
    setContent {
        activity = LocalActivity.current
        PantrixTheme(darkTheme = dark) { Box(Modifier.size(8.dp)) }
    }
    val window = checkNotNull(activity).window
    val bars = WindowCompat.getInsetsController(window, window.decorView)
    assertFalse(bars.isAppearanceLightStatusBars)
    dark = false
    waitForIdle()
    assertTrue(bars.isAppearanceLightStatusBars)
}
```

iOS's tests run without an app, in a plain Compose scene: there is no view controller there, and reading `LocalUIViewController` fails the test. Pantrix therefore first asks whether there is an app; the behaviour on iOS is tried on the simulator.

## 6. Pitfalls {#pitfalls}

*   **Themes inside themes.** A dark card inside a light screen must not change the bars. Only the outermost theme sets them.
*   **The order of `enableEdgeToEdge`.** `enableEdgeToEdge()` sets the icons by the system's mode. Called after the composition, it overwrites the theme's: it is called in `onCreate`, before `setContent`.
*   **A `ContextWrapper`.** `LocalView.current.context` is not always an `Activity`; the activity is reached through the wrappers.
*   **An iOS setting without a view controller.** Tests and previews have no `LocalUIViewController`.
*   **Coloured bars.** In an app that does not draw edge to edge and paints its bars in a colour of its own, the icons are chosen by that colour, not by the theme's ground: `followSystemBars = false`.

## 7. Verification {#verification}

### Tests

*   `ui/foundation`'s Android host tests (`SystemBarsOnAndroidTest`): the icons follow the theme's dark and change as it changes; `followSystemBars = false` leaves the app's setting alone; a theme inside another changes nothing of the bars.

### Seen on the simulator

*   On the iPhone 18 Pro Max simulator (iOS 27.0), in the demo hosted in SwiftUI: with the system dark, choosing "Light" turned both the app and the status bar light; going back to "System" turned both dark again. They stayed right after the screen was turned and the app sent to the background, and when the system's mode changed while the app followed it: overriding the view controller's style did not change the system that Compose's `isSystemInDarkTheme()` reads.

### Behaviours broken by hand

*   With the theme's setting taken out, two of the three tests fail: the icons stay in the system's mode.

### Not verified

*   The behaviour on iOS has no automated test (the tests run without an app); it was tried on the simulator as above, not on a real iPhone. The keyboard's and UIKit's controls' colours were not looked at separately.
*   The colour of a mobile browser's address bar (`<meta name="theme-color">`) is outside this scope.

## 8. Where it is in this repository {#repository toc="8. In the repository"}

*   `ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/platform/SystemBars.kt` and its platforms' counterparts.
*   `ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/theme/PantrixTheme.kt`: the outermost theme and `followSystemBars`.
*   `ui/foundation/src/androidHostTest/kotlin/com/pantrixui/foundation/platform/SystemBarsOnAndroidTest.kt`.
