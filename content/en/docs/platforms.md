---
title: Platforms
description: Each platform's entry point draws the same App(), and the theme fits it to the platform by itself. What differs from one platform to another, and what a project adds for each.
section: Getting started
---

## One app, four entry points {#entry}

:::tabs
### Android

```kotlin MainActivity.kt
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}
```

### iOS

```kotlin MainViewController.kt
fun MainViewController(): UIViewController = ComposeUIViewController { App() }
```

### Desktop

```kotlin Main.kt
fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "My app") {
        App()
    }
}
```

### Web

```kotlin Main.kt
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(viewportContainerId = "app") {
        PantrixTypefaceGate(onShown = { document.getElementById("loading")?.remove() }) {
            App()
        }
    }
}
```
:::

## What the theme fits {#conventions}

:::table By platform
| | Android | iOS | Desktop | Web |
| --- | --- | --- | --- | --- |
| Touch target | 48dp | 44pt | 32dp under a pointer | 32dp, 48dp to a finger |
| Focus ring | For the keyboard | For the keyboard | For the keyboard | For the keyboard |
| Component styles | Android's | iOS's | The desktop's | The web's |
| Haptics | Played | Played | None | None |
| Pointer shapes | With a mouse | None | Yes | Yes |
:::

`PantrixTheme.platform` says which conventions hold: its `type`, whether it is used by touch, its smallest touch target. A browser on a phone is the web used by touch, and the theme knows it by itself.

## On each platform {#each}

:::rows
- **Android**: API 30 and newer. `enableEdgeToEdge()` lets the app draw behind the system bars, and `PantrixScaffold` keeps its content clear of them. The outermost theme makes tags the resource ids Maestro and UI Automator find.
- **iOS**: Devices, and the simulator on Apple silicon. The library is built against the iOS 27.1 SDK and brings the fold's bridge: no cinterop of the project's own. The status bar, the keyboard and the controls follow the theme: [System bars](/docs/guides/system-bars).
- **Desktop**: The JVM. The window's title follows the screen with `PantrixWindowTitle`; a shortcut for the whole window is written once for every system: [Keyboard shortcuts](/docs/guides/keyboard-shortcuts).
- **Web**: Wasm and JS. `PantrixTypefaceGate` draws the app once its typeface has come, so the type never changes before the reader's eyes: [Web fonts](/docs/guides/web-fonts).
:::

:::callout
**A layout reads the window, not the device.** A tablet in split screen is a phone's width, and a desktop window can be narrow. `PantrixTheme.window` gives its size class and its posture: [Fitting the window](/docs/guides/adaptive).
:::

## Where next {#next}

:::cards
- [Forms](/docs/forms): Fields, rules and a form's submission.
- [Testing](/docs/testing): The same screen on every machine.
- [Fitting the window](/docs/guides/adaptive): Size classes, panes and folds.
- [Web fonts](/docs/guides/web-fonts): A typeface that arrives late, cured.
:::
