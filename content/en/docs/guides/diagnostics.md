---
title: Runtime diagnostics
description: Why some flaws show only on a running screen, what the library gives to catch them, how an app turns diagnostics on only in its own debug build, and how this is tested. The problem is not Pantrix's own: it is seen the same way in every Compose Multiplatform project that uses automation tags, colours chosen at run time or texts that come from outside.
---

Everything said here about Compose was read in the sources of Compose Multiplatform 1.12.2, or seen in a test that runs in this repository. What is said about Kotlin and the Gradle plugins was read in the sources of Kotlin 2.4.21 and of the Compose Gradle plugin 1.12.2. Which was seen where is written in sections 7 and 8; what could not be verified is counted apart in section 8.

## 1. The problem {#problem}

:::aside diagnostics
### Symptom

*   A test looks for a button with `onNodeWithTag("Button_save")` and says "Expected exactly '1' node but found '2' nodes". The same key was given in two places, but the two clash only while both are on the screen at the same time: a list's row and the detail it opens, two examples on a catalogue page, the two pages of a transition.
*   A brand's colour comes from a server, a theme's colour from the user's choice. The screenshot test draws with its own colours; no test sees whether the text on the run-time colour can be read.
*   A checkbox silently shrinks when the row that holds it gives it less room. In the screenshot the box is the same; the area a finger can press has narrowed.
*   An icon button's `contentDescription` comes empty from a resource. When the screen reader reaches the button, it says nothing.
*   The app opens in German, but the library has no German words: the components' own words ("Close", "Clear") stay in English.
*   A screen's root and a field inside it listen to the same shortcut. When the key is pressed the outer one runs; the inner one never runs, and no one notices.
:::

### Cause

*   **Lint looks at the source.** It cannot know a colour's value at run time, a name that comes from a resource, or how many nodes carry the same tag at the same time.
*   **A screenshot sees only what is drawn.** It does not see the semantic name, the automation tag or the area that can be pressed.
*   **Compose does not look for repeats.** `Modifier.testTag` only adds a property to the semantics (`TestTagElement`, `SemanticsModifierNode`); two nodes can carry the same tag. The test finders, for their part, count every node that is attached to the tree and not deactivated, even one not placed, in all roots (`SemanticsOwner.getAllSemanticsNodes`). The modifiers of a deactivated node are detached from the tree (`onDetach`), and attached again when it is reused (`onAttach`): the rows a scrolled list recycles are not counted.
*   **Some repeats are passing.** A page transition shows two pages together for a few frames; a row that opens squeezes what is inside it for a moment. A check that looks at every frame counts these as flaws too.

## 2. The solution: four pieces {#fix}

:::table
| # | Piece | What it gives |
| :--- | :--- | :--- |
| 1 | **The check inside the component.** Every component says what it draws, its name, its tag and its shortcut where it knows them | What is measured is the value at run time: the colour given, the name that came, the nodes on the screen |
| 2 | **Free while off.** The checks return in their first lines | The release build pays almost nothing; the screen is not set up again to turn them on and off |
| 3 | **The receiver.** What is found is given to one place: to the log, to a collector, to a test | The same check works in a test and in the debug build |
| 4 | **The timing.** What layout finds is reported if it lasts half a second; each finding once, for as long as it lasts | Transitions and opening rows give no false alarms; the log does not fill up with the same finding |
:::

Without the first piece the second has no meaning: a check that looks from outside the component does not know a button's colour or name. Without the third, the first two are of use only in a test; without the fourth, a transition produces a finding every time.

## 3. With Pantrix {#with}

The library gives all four pieces (`com.pantrixui.foundation.diagnostics`; the inspector in `com.pantrixui.components.overlay.inspector`):

:::table
| API | What it does |
| :--- | :--- |
| `PantrixTheme(diagnostics = …)` | The theme's diagnostics: the checks of the components inside give what they find here. Its default is `PantrixDiagnostics.Off`. A theme inside that is not given one uses the outer one's receiver |
| `PantrixDiagnostics.Off` | Turns the checks off: nothing is looked for, nothing is reported |
| `PantrixDiagnostics.Log` | Writes every finding to the platform's log: on Android a warning to Logcat with the tag `Pantrix` (`Log.w`), on iOS to the unified log (`NSLog`, Xcode's console), in the browser `console.warn`, on the JVM to standard error |
| `PantrixFindings(then = …)` | Keeps the findings (`all`, `clear()`, `boundsOf(finding)`), and gives each of them to `then` too. If `all` is read in a composition, it is followed as it grows |
| `PantrixFinding` | A finding: `check`, `component`, `testTag`, `screen` (the `TestScope` chain), `message` (in English) and the measured `values` |
| `PantrixCheck` | The six checks (3.1) |
| `LocalPantrixDiagnostics` | The receiver at that point |
| `PantrixDiagnosticsInspector(findings) { … }` | A button over the app that shows the number of findings; when pressed, the list of findings; the node of the finding chosen from the list is framed on the screen until the next press. While there are no findings it shows nothing |
| `Modifier.checkName(component, name)` | `MissingName` for the app's own component |
| `Modifier.checkContrast(component) { pairs }`, `ContrastPair`, `ContrastNeed` | `LowContrast` for the app's own component |
| `contrastRatio(foreground, background)` | The WCAG 2.2 ratio of two colours |
:::

### 3.1. the six checks

:::table
| Check | When it is reported | Where it comes from | How it is put right |
| :--- | :--- | :--- | :--- |
| `DuplicateTestTag` | If two nodes in the same window carry the same tag for half a second | `Modifier.pantrixTestTag`: all Pantrix components and the app code that uses it | Its own `tagKey` for each, or each in its own `TestScope` |
| `LowContrast` | The theme's 34 role pairs, once each time its colours change; the components' text and mark pairs, once for each component and its colours, however many of them show | `PantrixTheme`; the 39 components with a `…Colors` class of their own; `checkContrast` | Text 4.5:1; large text and a control's mark 3:1 |
| `SmallTouchTarget` | If a control is laid out for half a second in less room than the platform's touch target | `Modifier.minTouchTarget`: Pantrix's pressable controls | Have the one above give it room |
| `MissingName` | If the name the screen reader would say is empty. `null` is not reported: a picture that is decoration, or a control named by its text, gives no name | The components that take a `contentDescription`, a `label` or a `title`; `checkName` | A name in the app's language that says what it is or what it does |
| `NoStringsForLanguage` | If the theme's words are English while its language is not | `PantrixTheme` | That language's words, with `PantrixTheme(strings = …)` |
| `ShadowedShortcut` | If a node around it also listens to a shortcut: the inner one never runs | `Modifier.onShortcut` | Remove the inner one, or move it to another key |
:::

The theme's 34 pairs: for each of the six roles (`primary`, `success`, `info`, `warning`, `danger`, `neutral`), `onColor` on `color`, `onContainer` on `container`, and `color` on `background` and on `surface`; the page's texts `onSurface`, `textPrimary` and `textSecondary`, and the marks `borderStrong` and `focusRing`, on `background` and on `surface`.

**A ground with no fill.** A ground that shows what is under it, like the ground of a button with no fill, is measured on the theme's `background` and `surface` if the component stands on one of the theme's own grounds, and the lower ratio is counted. If the place the component stands on is a fill of another colour that gives its own content colour (`ProvidePantrixContentColor`, as a dark card does), such a pair is not measured: what is under it is not known.

### 3.2. only in the debug build

Pantrix does not know what kind of build it runs in; the app tells it. The plainest way is for each platform's entry point to give common code a `debug`:

```kotlin
// commonMain
@Composable
fun App(debug: Boolean) {
    val findings = remember { PantrixFindings(then = PantrixDiagnostics.Log) }
    PantrixTheme(diagnostics = if (debug) findings else PantrixDiagnostics.Off) {
        if (debug) PantrixDiagnosticsInspector(findings) { Screens() } else Screens()
    }
}
```

`debug` does not change during a build's life, so the `if` is safe here. For a setting that is turned on and off while the app runs, see the first pitfall in section 7.

Each platform knows the debug build its own way:

:::table
| Platform | Where it is known from | At the entry point |
| :--- | :--- | :--- |
| Android | From the app's `debuggable` flag: the debug build type turns it on | `val debug = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0`, in the `Activity`'s `onCreate`. Since Android Gradle plugin 8, `BuildConfig` is not generated by itself (`buildFeatures { buildConfig = true }`); this flag does not depend on it. If the shared code is in a library module, the app module reads the flag and gives it to it |
| iOS | From the build type of the Kotlin/Native binary: Xcode's `Debug` configuration has the debug framework built | `@OptIn(ExperimentalNativeApi::class) val debug = Platform.isDebugBinary` (`kotlin.native.Platform`), in `MainViewController`. `embedAndSignAppleFrameworkForXcode` reads the type from Xcode's `CONFIGURATION`; in a configuration other than `Debug` and `Release`, from `KOTLIN_FRAMEWORK_BUILD_TYPE` |
| Desktop | The JVM has no build type: the `run` task that runs while developing gives a system property | `build.gradle.kts`: `tasks.named<JavaExec>("run") { systemProperty("app.debug", "true") }`; in `main`, `System.getProperty("app.debug") == "true"`. The packaged app (`packageDmg`, `packageMsi`) does not start with `run` and does not get the property |
| Web | There is no build type the code in the browser can read | The page's address: `window.location.search.contains("debug")` (`kotlinx.browser`). Anyone who knows the address can turn it on; since the diagnostics only write to the console, this is often enough. If it is not, the build writes a constant |
:::

### 3.3. in a test

```kotlin
@Test
fun the_checkout_screen_has_no_findings() = runComposeUiTest {
    val findings = PantrixFindings()
    setContent { PantrixTheme(diagnostics = findings) { CheckoutScreen() } }
    // What layout finds is reported once it has lasted half a second.
    mainClock.advanceTimeBy(1_000)
    assertEquals(emptyList(), findings.all)
}
```

A finding's `message` is enough in the test's failure message: it says in English what is wrong and how it is put right. `values` carries what was measured (in `LowContrast`, `ratio`, `needed`, `foreground`, `background` and, if it was measured, `ground`; in `SmallTouchTarget`, `width`, `height` and `needed`).

### 3.4. your own component

The app's own component takes part in the checks too. The component itself knows its name and its colours:

```kotlin
@Composable
fun Swatch(name: String, fill: Color, label: Color) {
    Column(
        modifier = Modifier
            .checkName("Swatch", name)
            .checkContrast("Swatch") { listOf(ContrastPair("label on fill", label, fill, ContrastNeed.Text)) }
            .background(fill)
            .padding(12.dp)
    ) {
        PantrixText(text = name, color = label)
    }
}
```

The pairs of `checkContrast` are asked for only while diagnostics are on, and again when the diagnostics or the theme's colours change. A disabled component gives no pairs, since no one is expected to read it.

### 3.5. a part with a receiver of its own

A theme inside can take its own receiver: `PantrixTheme(diagnostics = own) { … }`. That part's findings go only to `own`, not to the outer one. The demo in this repository shows the examples of the "Diagnostics" page this way: each example's flaw is gathered in its own receiver, and does not fall into the app's own inspector.

## 4. Without Pantrix {#without}

The checks made from inside the component (colour, name, touch target) need a library or the app's own component layer: code that looks from outside does not know which colour a button draws on which. The two pieces below, though, can be copied into any project as they are.

### 4.1. the contrast ratio (`commonMain`)

```kotlin
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.luminance

/** WCAG 2.2: text reads at 4.5 and above, large text and a control's mark at 3 and above. */
fun contrastRatio(foreground: Color, background: Color): Float {
    require(foreground.isSpecified && background.isSpecified) { "An unspecified colour has no contrast." }
    val drawn = if (foreground.alpha < 1f) foreground.compositeOver(background) else foreground
    val one = drawn.luminance() + 0.05f
    val other = background.luminance() + 0.05f
    return maxOf(one, other) / minOf(one, other)
}
```

This is the body of the library's `contrastRatio`. A foreground colour that shows what is behind it is measured as it is drawn, put over the ground; the ground is measured as it is given.

### 4.2. repeated tags in a test (`commonTest`)

```kotlin
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider

/** The tags two or more nodes carry, and how many nodes carry each. */
fun SemanticsNodeInteractionsProvider.repeatedTestTags(): Map<String, Int> =
    onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.TestTag), useUnmergedTree = true)
        .fetchSemanticsNodes()
        .groupingBy { it.config[SemanticsProperties.TestTag] }
        .eachCount()
        .filterValues { it > 1 }
```

At the end of a screen test, `assertEquals(emptyMap(), repeatedTestTags())`. It counts what the test finders count: unplaced nodes too, recycled rows not. The moment the test looks is a single moment; if it looks in the middle of a transition, it finds a passing repeat too.

## 5. What the checks do not do {#limits toc="5. What it does not do"}

*   **Compose's own `Modifier.testTag` is not counted.** Only the tags `pantrixTestTag` puts go into the register.
*   **A pressable area that does not use `minTouchTarget` is not measured.** The app's own `clickable` finds no `SmallTouchTarget`.
*   **A chart's own series colours are not measured.** What is measured in charts are the text and mark pairs of the style and of the colours class.
*   **iOS's switch thumb is not measured.** In the iOS style (`thumbAlwaysLight`) the thumb is always light and stands apart from its track by its shadow; a colour ratio says nothing there.
*   **Pictures, gradients, and translucent colours on a filled ground are not measured.** What is under them is not known.
*   **There is no check for an overlay that falls on the fold.** Pantrix's overlays stay on one side of the fold (`ModalRoom`); this check would never fire on Pantrix's own overlays.
*   **There are no levels.** Every finding has a kind; the app decides which one stops a release.
*   **The messages are in English only.** A developer reads them, not the app's user. The inspector's own words (`diagnostics`, `findingCount`, `notOnScreen`) are in `PantrixStrings` in Turkish, English and Arabic.

## 6. Testing {#testing}

*   **The receiver:** every test gives its own `PantrixFindings` to the theme and looks at `all`. `PantrixDiagnostics.Log` is needless in a test.
*   **The clock:** layout's findings are reported once they have lasted half a second. In a test whose clock advances by itself, `waitForIdle()` is enough; in a test with `mainClock.autoAdvance = false`, the clock is advanced by hand by at least 500 ms. That a short repeat is not reported is tested this way too: the repeat is removed before 500 ms are up.
*   **All the screens:** the demo in this repository draws every page in `DemoPagesTest` in two languages and under the app's own shortcut, moves to an example's code and to the API tab, and gathers the findings for each page; no page has findings. `DiagnosticsSamplesTest` looks at whether each example of the two diagnostics pages shows only its own findings: the examples have their own receivers, and their findings do not fall into the app's.
*   **The place:** `boundsOf(finding)` is the place of the finding's node in the window; the inspector's frame is drawn from it. A finding with no node (the theme's colours, the language) gives `null`.

## 7. Pitfalls {#pitfalls}

:::table
| Pitfall | What happens | What is done | Where it was seen |
| :--- | :--- | :--- | :--- |
| **An inspector wrapped in a condition** | `if (on) PantrixDiagnosticsInspector(findings) { content() } else content()`, if `on` changes while the app runs, moves the content to another place in the composition. Every `remember` and `rememberSaveable` in the content is set up from the start: in the demo, turning diagnostics off in "Settings" took the app back to the overview | Leave the inspector always in its place: while diagnostics are off there are no findings, and the inspector shows nothing | While the demo was being written; test: `DemoShellTest.the_diagnostics_switch_leaves_the_app_where_it_is` (red when the condition is put back) |
| **Findings left after turning off** | When the receiver turns back to `Off` no new finding comes, but `PantrixFindings` keeps the old ones: the inspector shows the old count | `clear()` when diagnostics turn off | Test: `SettingsPageTest.turned_off_the_diagnostics_forget_what_they_found` |
| **The examples of a page** | The examples on a catalogue page are on the screen at the same time: the same key in two examples is a real repeat | Keep the examples' keys apart; if two examples use one helper, give it a key | Demo: fourteen repeats were found on five pages and fixed |
| **The two pages of a transition** | A transition keeps two pages attached together for a few frames; if both have the same tag, a check that looks at every frame counts it as a repeat | The check that waits half a second does not report it | Source: `SemanticsOwner`; test: `DuplicateTestTagTest.a_tag_carried_twice_for_a_moment_is_not_reported` |
| **Recycled rows** | The modifiers of the rows a scrolled list deactivates are detached from the tree; they are attached again when reused | Nothing is done: they are not counted | Source; test: `DuplicateTestTagTest.the_rows_of_a_list_that_scrolls_and_reuses_them_are_no_duplicates` |
| **A component with no fill on a filled ground** | A button with no fill and white text reads on a dark card; measured on the page's light ground it would have been counted unreadable | Give the fill's content colour (`ProvidePantrixContentColor`). Pantrix's surface and card do this themselves: on a fill of another colour they give its content colour, on the theme's ground none. Where a content colour is given, the pair is not measured | Test: `ComponentDiagnosticsTest.a_button_with_no_fill_on_a_fill_of_another_colour_is_not_measured_on_the_page` |
| **The receiver of a theme inside** | If a theme inside takes `diagnostics`, its findings do not go to the outer one | For the whole app's findings, give no receiver to the themes inside | Test: `PantrixDiagnosticsTest.a_theme_inside_another_reports_where_that_one_does_unless_it_is_told_otherwise` |
| **Repeats in parts with their own receivers** | If two parts with their own receivers carry the same tag, the repeat is reported to those parts' receivers, not to the outer one: a sweep that looks at the app's own findings does not see it. On the demo's "Diagnostics Inspector" page two examples showed the same screen with the same keys, and each inspector counted four findings instead of two | Look at the findings of the parts with their own receivers too; keep apart the keys of two parts that use the same helper | While looking at the demo on the web; test: `DiagnosticsSamplesTest.each_inspector_counts_the_two_findings_of_its_own_screen` |
| **A state list's equality** | Compose's `SnapshotStateList` knows no structural equality. `assertEquals(emptyList(), list)` passes on the JVM, because JUnit asks `expected.equals(actual)` and an empty list equals every empty list; on iOS and the web `kotlin.test` asks `actual == expected`, and the two empty lists do not come out equal | If a state list is to be given out, its copy at the moment it is read is given: `PantrixFindings.all` is so | In the iOS and JS test runs: while `all` gave the list itself, 21 tests were red |
| **NSLog's variadic arguments** | Kotlin/Native gives a Kotlin string to NSLog's `...` argument as a C string; `NSLog("%@", line)` takes it for an object, and the app crashes with `EXC_BAD_ACCESS` | Make the line the format itself, doubling the percent signs: `NSLog(line.replace("%", "%%"))` | In the iOS simulator, when the demo's "Diagnostics" page was opened; test: `PantrixDiagnosticsOnIosTest` (in the old form the test process falls with signal 11) |
| **A clock advanced by hand** | In a test with `mainClock.autoAdvance = false`, layout's findings never come | Advance the clock by at least 500 ms | The tests in this repository were written so |
| **The app's own shortcut** | If the app's root listens to a shortcut (in the demo, ⌘K opens the command palette), every screen that listens to the same shortcut stays in its shadow. The demo's example "A shadowed shortcut" showed a second finding because it listened to ⌘K: the screen's root was in the shadow of the app's too | In tests, draw the screens under the app's root; in the example, a key the app does not use (⌘F) | While looking at the demo on the web; test: `DiagnosticsSamplesTest` |
| **The desktop's target** | On the desktop and the web, used with a mouse, the touch target is 32dp: a 32dp box does not squeeze there, while it does on a phone. The demo's example showed no finding on the desktop and the web | To show a squeeze on every platform, a box lower than the smallest of the targets (24dp) | While looking at the demo on the web; test: `DiagnosticsSamplesTest` |
| **Where the log is** | On Android the findings are in Logcat with the tag `Pantrix`; on iOS in Xcode's console; in the browser in the console as a warning; on the desktop in standard error | On Android, `adb logcat -s Pantrix` | Source: the four `PantrixDiagnostics.*.kt` |
:::

## 8. Verification {#verification}

### Tests

:::table
| Test | Where it runs | What it looks at |
| :--- | :--- | :--- |
| `ui/foundation`, `PantrixDiagnosticsTest` | The JVM, the iOS simulator, JS, Wasm | The theme looks for nothing unless given a receiver; a theme inside uses the outer one's receiver; findings are kept once and in the order they come; a suspicion is reported on the next frame, layout's after half a second; one that goes before half a second is up is not reported; it is reported once, and again if it comes back; the owners of the same suspicion are one suspicion; the desk asks for frames only while something waits; the log line |
| `ui/foundation`, `PantrixDiagnosticsOnIosTest` | The iOS simulator | A finding carrying percent signs and `%@` is written to the unified log, and the app does not crash |
| `ui/foundation`, `ContrastRatioTest` | The same four targets | Black on white 21, a colour on itself 1; which is drawn on top makes no difference; a foreground colour that shows what is behind it is measured as drawn; an unspecified colour has no ratio |
| `ui/foundation`, `DuplicateTestTagTest`, `SmallTouchTargetTest`, `LowContrastTest`, `MissingNameTest`, `NoStringsForLanguageTest`, `ShadowedShortcutTest` | The same four targets | Each check: what it finds, what it does not find, that it measures nothing while off, that it finds when turned on later |
| `ui/components`, `ComponentDiagnosticsTest`, `DiagnosticsInspectorTest` | The JVM, the iOS simulator, JS, Wasm | Pantrix's own colours are no finding in either mode; a component's finding comes with its tag; a component with no fill on a filled ground; names, repeats, a squeezed target; the inspector's button, list, frame and clearing |
| `ui/components`, `ComponentScreenshotTest.diagnostics_inspector` | The JVM, Android, the iOS simulator | The inspector's picture in the light and the dark theme |
| `app/shared`, `DemoPagesTest`, `DiagnosticsSamplesTest`, `DemoShellTest`, `SettingsPageTest`, `DemoSettingsTest` | The JVM | Every page in two languages with zero findings; each example of the two diagnostics pages shows only its own findings; the app stays where it is when the setting is turned on and off; the findings are cleared when it turns off; the setting is kept, and is on in an old record |
:::

### Behaviours broken by hand

Each behaviour was broken once by hand, and which test turned red was looked at; then it was put back.

*   **The demo:** the inspector wrapped in a condition (`DemoShellTest`'s test red); findings not cleared on turning off (`SettingsPageTest`'s test red); the old form of the two examples of the "Diagnostics" page, ⌘K and the 32dp box (`DiagnosticsSamplesTest` red: zero squeezes, the shadowed shortcut twice); the two examples of the "Diagnostics Inspector" page sharing the same keys (`DiagnosticsSamplesTest` red: "Findings: 4").
*   **The timing:** the half second brought down to zero. Six tests turned red: the short repeat, the squeeze and the suspicion, the report that comes after half a second, the node that changes its tag, and the desk asking for frames.
*   **Free while off:** `Off` counted as on too. Four tests turned red: the theme, the tag register, the squeezed target and the contrast.
*   **iOS's log:** going back to `NSLog("%@", line)`. `PantrixDiagnosticsOnIosTest`'s process crashed with signal 11.
*   **A ground with no fill:** a ground that shows what is under it measured on the theme's grounds on every fill. In the first form no test turned red: the test used a colour that reads on the page too. The test was written again with a colour that does not read on the page; when the break was tried again, it turned red.

### Not verified

*   **The platforms' debug ways.** The code of the table in 3.2 was not compiled or run in this repository. `Platform.isDebugBinary` was read in Kotlin/Native 2.4.21's `Platform.kt`, the reading of Xcode's configuration in the Kotlin Gradle plugin 2.4.21's `XcodeEnvironment.kt`, and that the `run` task is a `JavaExec` in the Compose Gradle plugin 1.12.2's `configureJvmApplication.kt`. Android's `FLAG_DEBUGGABLE` and `BuildConfig`'s default in Android Gradle plugin 8 are from Android's documentation.
*   **The real logs.** In the iOS simulator the line itself was seen in the unified log (`xcrun simctl spawn <udid> log stream`), and in the browser's console as a warning. Logcat and the desktop's standard error were not looked at; the tests there look at the text of the log line.
*   **A screen reader.** How the inspector and its list are read aloud was not listened to.

## 9. Where it is in this repository {#repository toc="9. In the repository"}

:::table
| File | What |
| :--- | :--- |
| `ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/diagnostics/PantrixDiagnostics.kt` | `PantrixCheck`, `PantrixFinding`, `PantrixDiagnostics`, `LocalPantrixDiagnostics` |
| `…/diagnostics/PantrixFindings.kt` | `PantrixFindings` |
| `…/diagnostics/DiagnosticsDesk.kt` | The desk for each window: the suspicions, the half second, reporting once |
| `…/diagnostics/DuplicateTestTag.kt`, `LowContrast.kt`, `MissingName.kt`, `NoStringsForLanguage.kt` | The checks; `checkContrast`, `ContrastPair`, `ContrastNeed`, `checkName` |
| `…/a11y/TouchTarget.kt`, `…/keyboard/Shortcuts.kt`, `…/testing/TestTag.kt` | `SmallTouchTarget`, `ShadowedShortcut` and the tags' register |
| `…/token/color/ContrastRatio.kt` | `contrastRatio` |
| `ui/foundation/src/{android,ios,jvm,web}Main/…/diagnostics/PantrixDiagnostics.*.kt` | The platforms' logs |
| `ui/components/src/commonMain/kotlin/com/pantrixui/components/overlay/inspector/DiagnosticsInspector.kt` | `PantrixDiagnosticsInspector` |
| `ui/components/src/commonMain/kotlin/com/pantrixui/components/chart/common/ChartContrast.kt` | The charts' pairs |
| `app/shared/src/commonMain/kotlin/com/pantrixui/cmp/demo/samples/DiagnosticsSamples.kt`, `DiagnosticsInspectorSamples.kt` | The demo's two pages |
| `app/shared/src/commonMain/kotlin/com/pantrixui/cmp/demo/App.kt`, `theme/Theme.kt`, `shell/SettingsPage.kt` | The demo's own diagnostics, its inspector and its setting |
:::

## 10. What is left open {#open}

*   A chart's own series colours are not measured (see section 5).
*   There is no check for an overlay that falls on the fold; it would not fire on Pantrix's own overlays.
*   On real devices and in the browser only the demo itself was looked at (see section 8).
