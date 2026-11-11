---
title: Previewing a screen's states
description: Why the preview of a screen written in Compose Multiplatform is drawn differently from machine to machine and from the app on a phone, how a screen's states are previewed in its windows, in the app's theme, dark and light, and how a screenshot test draws the same states. The problem is not Pantrix's own: it is seen the same way in every Compose Multiplatform project that previews its screens.
---

What is said of Compose was read in the sources of Compose Multiplatform 1.12.2 (`Preview.kt`, `PreviewParameter.kt` in `ui-tooling-preview`) or seen in a test that runs in this repository; what was not tried is written in section 6.

## 1. The problem {#problem}

:::aside previews
### Symptom

*   A phone screen's preview is drawn with a mouse's touch targets and with Ctrl in its shortcuts: the IDE draws it on the JVM, and the theme reads the platform from the machine.
*   The dark preview of a screen comes out light, or the light one dark: the theme follows the machine's own dark mode.
*   A screen that shows today's date changes every day; the screenshot test drawn from the same state fails at the turn of the month.
*   The preview of the two halves of a foldable shows one pane: nothing tells the screen that the window is folded.
*   A screen's states are listed twice, once for the previews and once for the screenshot tests, and the two lists part.
:::

### Cause

*   **The theme reads its defaults from the device.** For an app that is right; in a preview the device is the machine the IDE runs on: its platform, its dark mode, its operating system and its clock.
*   **`@Preview` says the size and little else in common code.** `widthDp`, `heightDp`, `locale` and `fontScale` reach every platform; `uiMode`, which tells dark, is Android's.
*   **The test theme is a test's.** `PantrixTestTheme` fixes all of this, but stands in `com.pantrixui:testing`, which goes into test source sets alone: a preview in `commonMain` cannot use it.

## 2. The solution {#fix}

:::table
| # | Piece | What it gives |
| :--- | :--- | :--- |
| 1 | **A preview theme around the app's theme.** The platform, dark or light, the window and the clock are the preview's choice | The preview is the same on every machine, and is the screen on the platform it says |
| 2 | **The states as a list.** A `PreviewParameterProvider` counts them | Each state is a preview, and the list is written once |
| 3 | **The windows as sizes.** `@Preview(widthDp, heightDp)`, and the window for what a size does not say | The screen is seen where it is laid out: a phone, a tablet, a fold |
| 4 | **The same states in a screenshot test.** The test walks the same list in `PantrixTestTheme` | What is previewed is what is checked |
:::

## 3. With Pantrix {#with}

### 3.1. The preview's theme

`PantrixPreviewTheme` is in `com.pantrixui:foundation` (`com.pantrixui.foundation.theme`). The app's own theme goes inside it: a theme inside another keeps what the outer one sets unless it sets it itself, and its own colours are dark or light as the outer one is.

:::table
| Parameter | Default | What it fixes |
| :--- | :--- | :--- |
| `platform` | `PlatformType.Android` | The touch targets, the focus rings and the platform's ways; the shortcuts are written as its system writes them, Windows's on the desktop and the web |
| `dark` | `false` | Dark or light, whatever the machine's mode |
| `window` | `null`: the preview's own | The window the screen is told it is in, its size and its folds; it stands over the whole preview |
| `clock` | `null`: the device's | The moment the screen reads: `rememberToday()`, a time picker's hour, a chart's moments |
| motion | Reduced | Nothing is drawn halfway |
:::

```kotlin
@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun CheckoutOnAnIphoneDarkPreview() {
    PantrixPreviewTheme(platform = PlatformType.Ios, dark = true, clock = PreviewClock) {
        AppTheme { CheckoutScreen(state = CheckoutState.Filled) }
    }
}

/** Noon on 24 October 2026, UTC: the moment every preview and every screenshot shows. */
val PreviewInstant: Instant = Instant.parse("2026-10-24T12:00:00Z")

val PreviewClock = PantrixClock(
    object : Clock {
        override fun now(): Instant = PreviewInstant
    }
) { Duration.ZERO }
```

*   **The language is not fixed.** It is the device's unless the app's theme inside gives one, as an app that keeps its own language does: `PantrixTheme(locale = …)`. `@Preview(locale = "tr")` tells it to Android's preview only.
*   **`AppTheme` is the app's.** A theme built on a preset (`PantrixTheme(preset = Brand)`) takes the preset's dark or light colours by `dark`; a theme given its colours outright keeps them.

### 3.2. A screen's states

A `PreviewParameterProvider` counts the states once; a preview takes them as a parameter, and each is drawn as a preview of its own:

```kotlin
class CheckoutStates : PreviewParameterProvider<CheckoutState> {
    override val values = sequenceOf(CheckoutState.Empty, CheckoutState.Filled, CheckoutState.Failed)
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun CheckoutPreview(@PreviewParameter(CheckoutStates::class) state: CheckoutState) {
    PantrixPreviewTheme(clock = PreviewClock) { AppTheme { CheckoutScreen(state = state) } }
}
```

### 3.3. Windows

*   **A size is a `@Preview`.** `widthDp` and `heightDp` are the window the theme reads: 390×844 a phone, 834×1194 a tablet standing, 1440×900 a desktop. Several `@Preview`s stand on one function.
*   **What a size does not say is the window.** A fold, a posture, a pane: `PantrixPreviewTheme(window = PantrixWindow(DpSize(951.dp, 669.dp), posture = WindowPosture(listOf(fold))))`. The window stands over the whole preview, and an overlay opened in it covers it.

### 3.4. The same states in a screenshot test

The test walks the same provider, in `PantrixTestTheme` with the same platform, dark and clock:

```kotlin
@OptIn(ExperimentalTestApi::class)
class CheckoutScreenshotTest {
    @Test
    fun every_state() = CheckoutStates().values.forEachIndexed { index, state ->
        runDesktopComposeUiTest(width = 390, height = 844) {
            setContent {
                PantrixTestTheme(platform = PlatformType.Ios, dark = true, clock = TestClock(PreviewInstant)) {
                    AppTheme { CheckoutScreen(state = state) }
                }
            }
            onRoot().captureRoboImage("Checkout_$index.png")
        }
    }
}
```

`PantrixTestTheme` takes a `TestClock`, the preview a `PantrixClock`: the two are given the same moment. The screenshot setup is in [the testing guide](/docs/guides/testing) (3.6).

## 4. Without Pantrix {#without}

The shortest form that can be copied into a project without the library: the values a theme would read from the device, given outright around the app's theme.

```kotlin
@Composable
fun FixedPreview(dark: Boolean = false, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalAppDark provides dark,               // the app's own local, which its theme reads in place of isSystemInDarkTheme()
        LocalAppClock provides PreviewClock,      // the app's own clock, which its screens read in place of Clock.System
        LocalLayoutDirection provides LayoutDirection.Ltr,
        content = content
    )
}
```

Each value the app's theme reads from the device needs a local of the app's own: a theme that calls `isSystemInDarkTheme()` or `Clock.System` itself cannot be fixed from outside.

## 5. Pitfalls {#pitfalls}

*   **`PantrixTestTheme` in a preview.** It is in `com.pantrixui:testing`, which goes into test source sets alone: a preview in `commonMain` does not see it, and an app that adds it to `commonMain` ships its test activity. A preview takes `PantrixPreviewTheme`.
*   **`uiMode` is Android's.** `@Preview(uiMode = UI_MODE_NIGHT_YES)` turns the system dark in Android's preview alone; `PantrixPreviewTheme(dark = true)` does it on every platform.
*   **A theme given its colours outright.** `PantrixTheme(colors = BrandLight)` inside the preview's theme stays light in a dark preview: the app chooses its set by `dark` itself, or builds its theme on a preset.

## 6. Verification {#verification}

### Tests

:::table
| What | Where |
| :--- | :--- |
| The platform, dark and light, the system the shortcuts are written for, reduced motion; the app's theme inside keeping the platform and taking its preset's dark colours; the clock and the window, which stands over the whole preview | `ui/foundation/src/commonTest/.../theme/PantrixPreviewThemeTest.kt` (JVM, iOS, JS, Wasm) |
:::

### Looked at by hand

*   **The demo's "A preview's theme" example** on the nested theme page: a part drawn on iOS, dark, on 24 October 2026, in the Harbor preset's dark colours, inside the demo's own theme.

### Not verified

*   A preview drawn in Android Studio's or IntelliJ's preview pane: `PantrixPreviewTheme` was drawn in tests and in the demo, not in an IDE.
*   The screenshot test of 3.4 as written: it puts together what the testing guide says, and was not run.

## 7. Where it is in this repository {#repository toc="7. In the repository"}

*   `ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/theme/PantrixPreviewTheme.kt`, and its test beside it in `commonTest`.
*   The demo: `app/shared/src/commonMain/kotlin/com/pantrixui/cmp/demo/samples/NestedThemeSamples.kt` ("A preview's theme").
