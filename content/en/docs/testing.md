---
title: Testing
description: How an app's UI test finds Pantrix's components by their tags, draws the screen the same on every machine, keeps the machine's services out of it, and reaches a device with the same tags.
section: Getting started
---

## Tags, not texts {#tags}

Every component carries a tag built from its name and the `tagKey` it is given, the same in every language. A test or a Maestro flow finds it by that, and does not break when a word is translated.

:::table How a tag is made
- `PantrixButton(tagKey = "pay")`: `Button_pay`
- `TestScope("Checkout") { … }`: `Checkout.Button_pay`, and sections nest: `Checkout.Payment.Button_pay`
- `form.field("email", …)`: `TextField_email`: a bound component is tagged by the field's `id`
- `PantrixSelect(tagKey = "country")`: `Select_country.SelectOption_tr`: a control's parts are named inside its tag
:::

## A test {#test}

The helpers are `com.pantrixui:testing`, on every target.

```kotlin CheckoutScreenTest.kt
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

## The same screen on every machine {#theme}

`PantrixTestTheme` fixes what a screen would take from the machine: the platform, light or dark, the language, the density, the font's scale, motion. It takes the doubles of the platform's services as parameters.

:::table The doubles
- `RecordingAnalytics()`: The components' events, in order
- `RecordingHaptics()`: The haptics played
- `TestClipboard()`: What the screen copied, and another platform's clipboard
- `TestFilePicker(files)`: Answers every pick with the files given
- `TestClock(now, utcOffset)`: The screen's clock, which moves only when the test moves it
- `PantrixFindings()`: What the diagnostics found, for `assertNoFindings`
:::

:::callout
**A date is read at two offsets.** A test of a screen that shows a date gives it a `TestClock` and reads it at two offsets from UTC: one alone may be the machine's own.
:::

## Each platform in one test {#platforms}

`onEachPlatform` draws the screen once for each platform's conventions. `assertTouchTargetOf(platform)` checks 48dp on Android, 44pt on iOS and 32dp under a mouse.

```kotlin SaveButtonTest.kt
@OptIn(ExperimentalTestApi::class)
@Test
fun the_button_is_a_finger_s_size_on_every_platform() = runComposeUiTest {
    onEachPlatform({ platform -> PantrixTestTheme(platform = platform) { SaveButton() } }) { platform ->
        onPantrixNode("Button", "save").assertTouchTargetOf(platform)
    }
}
```

## On a device {#device}

The tags reach the device's accessibility tree: on Android as the `resource-id`, on iOS as the `accessibilityIdentifier`. A `Popup` or `Dialog` of the app's own puts `Modifier.pantrixAutomationRoot()` on its root.

```yaml checkout.yaml
appId: ${APP_ID}
---
- launchApp
- tapOn:
    id: "Button_browse"
- tapOn:
    id: "TextField_search"
- inputText: "invoice"
```

## Where next {#next}

:::cards
- [Testing a screen](/docs/guides/testing): Every helper, and why a test changes from machine to machine.
- [Runtime diagnostics](/docs/guides/diagnostics): Flaws seen only on a running screen.
- [detekt rules in your app](/docs/guides/detekt): The rules a machine can keep.
- [Forms](/docs/forms): A form's fields and their tags.
:::
