# Testing a screen

How an app's UI test finds Pantrix's components, draws the screen the same on every machine, keeps the machine out
of it, and checks what a screen reader and a finger meet. The helpers are `com.pantrixui:testing`, in the package
`com.pantrixui.testing`, on every target; [setup.md](setup.md) adds it to `commonTest`.

Contents: Automation tags · A test · The same screen on every machine · Each platform · Running · On a device

## Automation tags

Every component carries a tag built from its name and the `tagKey` it is given, the same in every language:

- `PantrixButton(tagKey = "pay")` is `Button_pay`. A component bound to a form's field is tagged by the field's `id`.
- `TestScope("Checkout") { … }` puts the section's name before the tags inside it, and sections nest:
  `Checkout.Payment.Button_pay`.
- A control's parts are named inside its tag: `Select_country.SelectOption_tr`, `NavigationBar_main.Item_inbox`. Each
  component's KDoc and page say its parts.
- A component given no `tagKey` is tagged by the place of its call, which moves when the code does: every component a
  test or a flow presses is given one.
- A key is normalised as a tag: `"Save draft"` and `"save_draft"` are one tag. It is never built from visible text.
- `PantrixTheme(testTagPolicy = TestTagPolicy.Off)` puts no tags in a release build's semantics; the events still
  carry them.

## A test

```kotlin
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.runComposeUiTest
import com.pantrixui.foundation.diagnostics.PantrixFindings
import com.pantrixui.testing.PantrixTestTheme
import com.pantrixui.testing.RecordingAnalytics
import com.pantrixui.testing.assertNoFindings
import com.pantrixui.testing.onPantrixNode
import kotlin.test.Test
import kotlin.test.assertEquals

class CheckoutScreenTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun paying_reports_the_press_and_the_screen_has_no_findings() = runComposeUiTest {
        val findings = PantrixFindings()
        val analytics = RecordingAnalytics()
        setContent {
            PantrixTestTheme(findings = findings, analytics = analytics) { CheckoutScreen() }
        }
        onPantrixNode("TextField", "card_number").requestFocus().performTextInput("4242424242424242")
        onPantrixNode("Button", "pay").performClick()
        assertEquals(listOf("click"), analytics.eventsOf("Button").map { it.action })
        assertNoFindings(findings)
    }
}
```

- **Finding.** `onPantrixNode(component, tagKey, scope)` is the one node, `onAllPantrixNodes(…)` every one in the
  tree's order, `hasPantrixTag(…)` a `SemanticsMatcher`. A component in a `TestScope` is found with its whole chain:
  `onPantrixNode("Button", "pay", scope = "Checkout.Payment")`. `pantrixTags()` lists what the screen carries, for
  writing a new test or a flow.
- **The component's name** is the first part of its tag, which the documents give with each component: mostly the
  composable's name without `Pantrix` (`Button`, `TextField`, `Select`), `IconButton` for `PantrixBackButton`, `Chart`
  for the charts drawn on `PantrixChart`. [components.md](components.md) lists each one's tags.
- **Experimental API.** `runComposeUiTest` is `@ExperimentalTestApi` in Compose's ui-test, and so are the helpers that
  take a `ComposeUiTest`: `onEachPlatform` and `assertNoFindings`.
- **`setContent` is called once** in a test; a value that changes is given as a `mutableStateOf`.

## The same screen on every machine

`PantrixTestTheme` fixes what a screen would otherwise take from the machine: `platform` (`PlatformType.Android`),
`dark` (false), `locale` (`PantrixLocale.English`), `density`, `fontScale`, a reduced motion, a clipboard and a file
picker of its own. It takes the doubles as parameters:

| Double | What it keeps |
| :--- | :--- |
| `RecordingAnalytics()` | The components' events, in order: `events`, `eventsOf("Button")` |
| `RecordingHaptics()` | The haptics played: `played` |
| `TestClipboard()` | What the screen copied: `text()`, `texts()`; its `traits` play another platform's clipboard |
| `TestFilePicker(files)` | Answers every pick with `files` |
| `TestFileSaver(result)` | The files the screen saved, `files`; given inside the theme through `LocalFileSaver` |
| `TestClock(now, utcOffset)` | The screen's clock, which moves only when the test moves it: `advanceBy(…)` |
| `PantrixFindings()` | What the diagnostics found, for `assertNoFindings` |

- A test of a screen that shows a date or a time gives it a `TestClock`, and reads it at two offsets from UTC: one
  offset alone may be the machine's own.
- The doubles go in by the theme's parameters: a `CompositionLocalProvider` outside `PantrixTestTheme` is overridden by
  the theme's own values.
- `window = PantrixWindow(DpSize(…))` draws the screen in a window of that size, and with a `posture`, folded.

## Each platform

```kotlin
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import com.pantrixui.testing.PantrixTestTheme
import com.pantrixui.testing.assertTouchTargetOf
import com.pantrixui.testing.onEachPlatform
import com.pantrixui.testing.onPantrixNode
import kotlin.test.Test

class SaveButtonTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun the_button_is_a_finger_s_size_on_every_platform() = runComposeUiTest {
        onEachPlatform({ platform -> PantrixTestTheme(platform = platform) { SaveButton() } }) { platform ->
            onPantrixNode("Button", "save").assertTouchTargetOf(platform)
        }
    }
}
```

`onEachPlatform` draws the screen once for each platform's conventions in one test. The other checks:
`assertHasName(name)` (what a screen reader calls the node), `assertTouchTargetOf(platform)` (48dp on Android, 44pt on
iOS, 32dp with a mouse), `performShortcut(shortcut, system)` and `readText()`.

## Running

The common tests run on every target: `jvmTest`, `iosSimulatorArm64Test`, `jsBrowserTest`, `wasmJsBrowserTest`.
Android's run in its host tree (`androidHostTest`) with Robolectric, where the module is added again. Screenshots are
the app's choice of tool: the module has no helper for them.

## On a device

The tags reach the device's accessibility tree: on Android as the `resource-id` (the theme turns
`testTagsAsResourceId` on, and Pantrix's own overlays do in their windows), on iOS as the `accessibilityIdentifier`.
A window the app opens itself, a `Popup` or a `Dialog` of its own, puts `Modifier.pantrixAutomationRoot()` on the root
of what it shows: on Android it is a window of its own, which the theme's mark does not reach. A Maestro flow finds a
component by the same tag, and does not break in another language:

```yaml
appId: ${APP_ID}
---
- launchApp
- tapOn:
    id: "Button_browse"
- tapOn:
    id: "TextField_search"
- inputText: "invoice"
```

`id:` is a regular expression: a key that holds `$`, `[` or `(` is escaped.
