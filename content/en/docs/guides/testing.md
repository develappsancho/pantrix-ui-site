---
title: Testing a screen
description: Why the test of a screen written in Compose Multiplatform changes from machine to machine, how a component is found by its automation tag, what is put in place of the platform's services, and how the same tags reach device automation (Maestro). The problem is not Pantrix's own: it is seen the same way in every Compose Multiplatform project that uses automation tags, a theme, the clipboard, a file picker or the clock.
---

Everything said about Compose was read in the sources of Compose Multiplatform 1.12.2 and of compose ui-test 1.12.2, or seen in a test that runs in this repository; what is said about Maestro was seen with Maestro 2.10.0 on Android emulators (an API 34 tablet, a Pixel 7 Pro on API 33, a Pixel 9 Pro Fold on API 35), in the iPhone 18 Pro Max simulator (iOS 27.0) and in Maestro's Chromium. Which was seen where is written in sections 7 and 8.

## 1. The problem {#problem}

:::aside testing
### Symptom

*   A test looks for a button as `onNodeWithTag("Button_save draft")` and does not find it: the key goes into the tag in lower case, with `_` in place of what is not a letter (`Button_save_draft`). When the button is moved inside a `TestScope`, the scope's name comes at the head of the tag and the test breaks again.
*   The same screen test passes on the developer's Mac and fails on CI: the machine's language, dark mode, font scale, operating system (does a shortcut start with ⌘ or with Ctrl) and time zone differ.
*   A test that presses "Copy" writes over the text on the clipboard of the person running the test. On the desktop, a test that presses "Choose a file" opens the system's file window and waits there.
*   A date picker's test fails on the first day of the month, or at midnight. A chart's time axis tells other hours on CI (UTC) than on the developer's machine (UTC+3).
*   A test that checks accessibility passes, but there is a button with no name on the screen: the diagnostics report it on the next frame, and by then the test has long ended.
*   On Android `runComposeUiTest` says "Unable to resolve activity for Intent … ComponentActivity": Robolectric does not start an activity a manifest does not declare.
*   A Maestro flow never finds the tag on Android; on iOS it does. On the web it finds neither the tag nor the text.
:::

### Cause

*   **A tag is a text built by a rule.** In Pantrix it is `Scope.Component_key`; the key is lowered and shortened with `toTagSlug`, and a control's part stands inside the control's tag (`SegmentButton_sort.Segment_newest`). A test that does not know the rule builds the tag by hand, and breaks when the rule changes.
*   **The theme reads its defaults from the device.** That is right for an app: the system's language, dark mode, font scale, reduce-motion setting and operating system. For a test, each is an input bound to the machine.
*   **Compose's test clipboard is the system's.** `LocalClipboard` gives the machine's clipboard in a test too; `rememberFilePicker()` opens the platform's picker.
*   **The clock is the device's.** Today is read from `Clock.System` and from the device's UTC offset (`systemUtcOffset`).
*   **Some findings come late.** The diagnostics report a squeezed touch target once it has lasted half a second, the same tag on two nodes once it has lasted a second and a half: a page transition shows two pages together for as long as it lasts (700 ms on Android).
*   **Robolectric starts only an activity a manifest declares.** androidx's `ui-test-manifest` declares this activity, but it comes with androidx Compose's version, not with Compose Multiplatform's.
*   **Device automation reads the platform's accessibility tree.** On Android a tag becomes a `resource-id` only while `testTagsAsResourceId` is on; on iOS Compose makes `testTag` an `accessibilityIdentifier` itself. On the web Compose draws on a canvas; the semantics tree has a hidden DOM mirror, but it is inside a shadow root (section 8).

## 2. The solution: five pieces {#fix}

:::table
| # | Piece | What it gives |
| :--- | :--- | :--- |
| 1 | **Build the tag by the component's rule.** The test writes the component's name and key; the function the component uses builds the tag | The rule is in one place; the key is written the same as in the screen's code |
| 2 | **A fixed theme.** The platform, the language, dark mode, the density, the font scale, motion and the operating system are the test's choice | The test sees the same screen on every machine |
| 3 | **Test doubles.** The clipboard, the file picker, analytics, haptic feedback and the clock are the test's own | The machine is not touched; what the screen says is recorded |
| 4 | **Assertions.** The diagnostics' findings, the name, the touch target, the shortcut, the text | Accessibility and the platform's rules are tested in one line |
| 5 | **Device automation.** The same tags reach Android and iOS | A Maestro flow too looks for the key in the screen's code; it does not break when the language changes |
:::

Without the first piece the others are brittle: every tag written by hand is a copy of the rule. Without the second and the third, a test is bound to the machine.

## 3. With Pantrix {#with}

The test module `ui/testing` is published as `com.pantrixui:testing`; its package is `com.pantrixui.testing`. It depends only on `:ui:foundation` and on compose ui-test: components are given by their names, as text. Three additions in foundation come with it: `pantrixTestTagOf`, `LocalPantrixClock` and `LocalFilePicker`.

### 3.1. Setting up

```kotlin
kotlin {
    sourceSets {
        commonTest.dependencies {
            implementation("com.pantrixui:testing:0.1.1")
            implementation(kotlin("test"))
        }
        // runComposeUiTest really draws on the desktop: the test JVM needs this machine's Skiko.
        jvmTest.dependencies { implementation(compose.desktop.currentOs) }
    }
}
```

*   **Only to the test source sets.** The module's Android side declares `androidx.activity.ComponentActivity` in its own manifest; if it goes into an app's own build, it carries this activity too.
*   **Android's host tests.** If Android's host tests are in a separate source tree (`androidHostTest`), the module is added there too. Once it is depended on, the activity merges into the test's manifest, and the test's own `AndroidManifest.xml` is not needed (`TestingOnAndroidTest` is therefore without a manifest).
*   **An experimental API.** `ComposeUiTest` and `runComposeUiTest` are `@ExperimentalTestApi` in compose ui-test. The helpers that take it (`onEachPlatform`, `assertNoFindings`) are too; the finding helpers are not.

### 3.2. Finding a component

:::table
| API | What it does |
| :--- | :--- |
| `hasPantrixTag(component, tagKey?, scope?)` | A `SemanticsMatcher`: the tag the component carries with this key and in this scope |
| `onPantrixNode(component, tagKey?, scope?)` | That single node; if there is none, or more than one, what is done to it fails |
| `onAllPantrixNodes(component, tagKey?, scope?)` | All of them, in the tree's order |
| `pantrixTags()` | The Pantrix tags the screen carries, in the tree's order |
| `pantrixTestTagOf(component, tagKey, scope)` (foundation) | The tag's text, without the screen being set up |
:::

```kotlin
onPantrixNode("Button", "Save draft").performClick()            // Button_save_draft
onPantrixNode("Button", "pay", scope = "Checkout.Payment")      // Checkout.Payment.Button_pay
onAllPantrixNodes("Button", scope = "Checkout").assertCountEquals(3)
onPantrixNode("Segment", "newest", scope = "SegmentButton_sort") // a control's part
```

*   **The key is written the same as in the screen's code.** `pantrixTestTagOf` builds the tag; `rememberPantrixTestTag` builds a keyed tag with it too. `"Save draft"` and `"save_draft"` are the same tag.
*   **If no scope is given, the tag has no scope.** A component in a `TestScope` is found by writing its scope; the scope is the whole chain (`Checkout.Payment`, not `Checkout`).
*   **A page is a scope.** A `PantrixPageScaffold` given a `tagKey` puts what the app gives it inside `TestScope(tagKey)`: a button tagged `save` on the page `orders` is found as `orders.Button_save`. The page's own parts are not inside it: its bar is `TopBar_orders`, an action `PageScaffold_orders.Action_export`. While one page gives way to another, and while a back gesture is held on iOS, the two pages stand together: the same button on every page's bar is found twice, and when that lasts over a second and a half, as a back gesture held still can, the diagnostics report it as `DuplicateTestTag`. Give each page a `tagKey`, or put the buttons every page carries in a `TestScope` named after the page they stand on: the demo says `overview.Button_language`.
*   **If no key is given**, all the component's nodes in that scope are found, the one built without a key too (`Button#1k3z9`). A component with a longer name (`ButtonGroup`) and a part inside a control are not counted.
*   **A part is inside the control's tag.** The part of a control with a key is `Control_key.Part_key`: `scope` is the control's tag. The part of a control with no key carries only its own tag.
*   **`pantrixTags()`** shows what is looked for while a new screen's test or a Maestro flow is being written: `println(pantrixTags().joinToString("\n"))`. A tag of another form, like the app's own `Modifier.testTag("login")`, is not in the list. By default it looks at every node's own tree (`useUnmergedTree = true`): the contents a button merges show too.

### 3.3. A fixed theme

`PantrixTestTheme` shows a screen the same on every machine. The defaults:

:::table
| Parameter | Default | Why |
| :--- | :--- | :--- |
| `platform` | `PlatformType.Android` | The touch targets, the focus rings and the platform's ways are fixed |
| `system` | The platform's; `Windows` on the desktop and the web | What shortcuts start with (`⌘` or `Ctrl`) does not depend on the machine |
| `dark` | `false` | |
| `locale` | `PantrixLocale.English` | The text's direction comes with it |
| `density` | `PantrixDensity.Comfortable` | |
| `fontScale` | `1f` | Only the text grows; the screen's pixels stay the same |
| `window` | `null`: the window the test draws | If it is given, inside `PantrixWindowFrame`: folds and layers follow it |
| motion | Reduced | Nothing is caught halfway; a snackbar comes in and goes out in one frame |
| `findings` | `null`: diagnostics on, findings to the log | If it is given, the findings are gathered there (3.5) |
| `analytics`, `haptics` | `NoOp` | Given a recorder, what the screen says is kept (3.4) |
| `clipboard` | The theme's own `TestClipboard` | The machine's clipboard is never written |
| `filePicker` | The theme's own picker, which never answers | No window of the system opens |
| `clock` | `null`: the device's clock | Every test that shows a date or a time should give its own `TestClock` |
:::

```kotlin
@OptIn(ExperimentalTestApi::class)
@Test
fun pay_is_told_and_the_screen_has_no_findings() = runComposeUiTest {
    val findings = PantrixFindings()
    val analytics = RecordingAnalytics()
    setContent { PantrixTestTheme(findings = findings, analytics = analytics) { CheckoutScreen() } }
    onPantrixNode("Button", "pay").performClick()
    assertEquals(listOf("click"), analytics.eventsOf("Button").map { it.action })
    assertNoFindings(findings)
}
```

**On every platform:** `onEachPlatform(content, check)` sets up the screen on the four platforms in turn, in a single run of the test. On the web a run is a promise the test gives back, so no one waits for runs started from a loop; nothing that fails in them fails the test.

```kotlin
onEachPlatform({ platform -> PantrixTestTheme(platform = platform) { SaveButton() } }) { platform ->
    onPantrixNode("Button", "save").assertTouchTargetOf(platform)
}
```

### 3.4. Test doubles

:::table
| Double | What it keeps, how it answers |
| :--- | :--- |
| `TestClipboard(traits)` | What the screen copied, in order. `text()` and `texts()` read as the platform reads: on Android the first item of the `ClipData`, on the desktop the `Transferable`'s text, on iOS plain text, on the web `text/plain`. On the web, reading waits for the browser (`suspend`). `traits` is told to the screen as the platform's traits: `ClipboardTraits(announcedBySystem = true)` is Android 13, `ClipboardTraits(writable = false)` a page served over plain http. The platform clipboard that Compose's text field asks (`nativeClipboard`) is the test's too: on the desktop its own AWT clipboard that keeps the last copy, on iOS a pasteboard with its own name, apart from the general pasteboard |
| `RecordingAnalytics()` | The events the components report, in order: `events`, `eventsOf("Button")` |
| `RecordingHaptics()` | The haptic feedback played, in order: `played` |
| `TestFilePicker(files)` | Answers every pick with `files`: only with the types the picker shows, and with the first if a single file was asked for; if none fits, it does not answer, like a user who closes the picker. `requests` keeps what each pick asked for |
| `TestClock(now, utcOffset)` | Shows the moment the test set up; it moves only when the test moves it (`advanceBy`, `now = …`). If `utcOffset` is not given, it is UTC's time |
| `TestFileSaver(result)` | Keeps the files the screen saved, in order (`files`), and answers every save with `result`: saved, if it is not given. The theme always gives its own, and no window for saving opens; a test that reads what was saved gives its own inside the theme, through `LocalFileSaver` |
:::

The doubles listed, but `TestFileSaver`, are `PantrixTestTheme`'s parameters. They can be given without the theme too, from foundation's own places:

```kotlin
// The whole screen's picker: rememberFilePicker() and PantrixFileDrop's default take this.
CompositionLocalProvider(LocalFilePicker provides picker) { OfferScreen() }

// The screen's saver: rememberFileSaver() gives this. It is given inside PantrixTestTheme.
CompositionLocalProvider(LocalFileSaver provides saver) { ThemeBuilderScreen() }

// The screen's clock: rememberToday(), the time field's picker, the elastic date picker and the charts' moments read this.
CompositionLocalProvider(LocalPantrixClock provides PantrixClock(clock) { 3.hours }) { BookingScreen() }
```

*   **`TestClock` is the time the screen reads, not the test's frames.** A screen that waits for midnight waits for the test's own clock too: `clock.advanceBy(1.minutes)` together with `mainClock.advanceTimeBy(60_000)`.
*   **The screen's clock is not the device's.** `PantrixDate.today()` and `PantrixTime.now()` read the device's clock, not `LocalPantrixClock`; on a screen, `rememberToday()` or `LocalPantrixClock.current.today()` ([the date guide](/docs/guides/date-time)).

### 3.5. Assertions

:::table
| API | What it asserts |
| :--- | :--- |
| `assertNoFindings(findings)` | The diagnostics found nothing: a control with no name, an unreadable colour, a squeezed touch target, one tag on two nodes. It first moves the test's clock past a second and a half, then looks; if it fails, it names each finding |
| `assertHasName(name)` | The name the screen reader gives the node: its content description, or else its text |
| `assertTouchTargetOf(platform)` | The area a finger can press is as large as the platform asks: 48dp on Android, 44pt on iOS, 32dp with a mouse on the desktop and the web. What is measured is the region that takes the press, not what is drawn |
| `performShortcut(shortcut, system)` | Presses the shortcut as that system's keyboard does: with Command on Apple's systems, with Control on the others. That the screen too is written for that system is said by `PantrixTestTheme(system = …)` |
| `readText()` | The text the node shows: what a text field holds, or else its texts and the texts of what it merges, line by line |
:::

Pixel and colour helpers are not in the module: looking for a colour in pixels depends on the component's drawing, and stays only in Pantrix's own tests.

### 3.6. Screenshots

There is no helper in the module for screenshot tests, and the module is not bound to Roborazzi: on which platform, with which tool and where to record is the app's choice. Pantrix's own setup is in the Roborazzi guide (in Turkish). Its summary for an app:

*   **Three platforms.** The desktop (`jvmTest`, `io.github.takahirom.roborazzi.captureRoboImage`), Android (`androidHostTest`, Robolectric, `@GraphicsMode(GraphicsMode.Mode.NATIVE)`, `com.github.takahirom.roborazzi.captureRoboImage`) and iOS (`iosTest`, `captureRoboImage(this, filePath = …)`). There is no Roborazzi on the web.
*   **`PantrixTestTheme` sets up the scene.** If the theme, the platform, the language and the font scale are not fixed, the picture changes from machine to machine. A scene that shows a date should take a `TestClock`: if it does not, the picture changes every month.
*   **Each platform's own reference.** The desktop and iOS draw with Skiko, Android with its own renderer; the same component comes out a few pixels different.
*   **The references depend on the versions of Compose and of the simulator.** The move from Compose Multiplatform 1.12.1 to 1.12.2 changed the shadows, together with Skiko; iOS references also depend on the simulator and the iOS version they were recorded on.

### 3.7. Device automation: Maestro

Pantrix's tags reach the device's accessibility tree: on Android as `resource-id` (the outermost theme, and the root of every modal or popup layer, turns `testTagsAsResourceId` on), on iOS as `accessibilityIdentifier` (Compose converts `testTag` itself). A [Maestro](https://maestro.dev) flow finds a component with `id:`, by the key in the screen's code; the flow does not break when the app opens in another language.

A window the app opens itself, a `Popup` or a `Dialog` of its own, is outside these roots on Android: the root of what it shows carries `Modifier.pantrixAutomationRoot()` itself. On iOS, the desktop and the web this changes nothing.

```kotlin
Popup(onDismissRequest = onClose) {
    PantrixModalLayer {
        PantrixSurface(modifier = Modifier.pantrixTransientsRoot().pantrixAutomationRoot()) {
            PantrixButton(text = stringResource(Res.string.close), onClick = onClose, tagKey = "close_layer")
        }
    }
}
```

```yaml
appId: ${APP_ID}
---
- launchApp:
    stopApp: true
- tapOn:
    id: "Button_browse"
- tapOn:
    id: "TextField_search"
- inputText: "PantrixSheet"
- tapOn:
    id: "Card_sheet"
```

**The demo's flows** are under `app/maestro/`; every page of the catalogue has a flow:

*   **`flows/shell/`:** the app's shell: the overview, the catalogue (the category filter and the search), a component page (the tabs) and the settings (the language and the diagnostics). Tagged `shell`.
*   **`flows/<section>/<page>.yaml`:** the catalogue's 118 pages, in their sections' folders (`flows/form/checkbox.yaml`). A flow opens its page by its link, `pantrixui://page/<key>`; brings each example's card into view in the page's order, switches it to its code and back to the example; then tries what the page's component is for: pressing, typing, choosing, opening and closing. Tagged `page` and with the section's name (`form`); a few `smoke`.
*   **`flows/fold/`:** the pages looked at on a foldable's inner screen while it is held like a book: two panes that meet at the hinge, the side rail. Tagged `fold`. A flow checks the posture first; on the Pixel_9_Pro_Fold emulator the hinge is half opened with `adb emu sensor set hinge-angle0 90`.
*   **`subflows/`:** the shared steps and the places where the two platforms part: opening a page by its link, an example's card, bringing an element to the middle of the screen, an option that is chosen or not, opening a section.

The demo's `MaestroFlowsTest` (JVM) checks that the flows keep up with the catalogue: every page has a flow in its section's folder, the flow opens the page by its link and visits each of the page's examples in order. A page or an example added without its flow fails the test, with no device.

:::table
| How | Command |
| :--- | :--- |
| By hand | On Android's debug build `maestro test -e APP_ID=com.pantrixui.cmp.demo.debug -e LINK_SCHEME=pantrixui-debug --exclude-tags fold app/maestro`; on iOS the bundle identifier, which carries the team identifier (`com.pantrixui.cmp.demo.PantrixUI<TEAM_ID>`), and no `LINK_SCHEME` |
| Android, Gradle | `./gradlew :app:androidApp:maestroTest`: installs the debug build on that device alone and runs the flows |
| iOS, Gradle | `./gradlew :app:shared:maestroIosTest`: builds the app with Xcode for the open simulator, installs it and runs the flows |
| One section | `./gradlew :app:androidApp:maestroTest -Pmaestro.flows=flows/form` |
| Smoke test | `./gradlew :app:shared:maestroIosTest -Pmaestro.include=smoke` |
:::

*   **Both are optional**, and do not go into `check`: a flow needs a device, and Maestro is a program installed apart from the build.
*   `-Pmaestro.flows=<path>,…` runs only the flows it names: files or folders inside `app/maestro` (`flows/form`, `flows/shell/overview.yaml`); a folder brings every flow under it.
*   `-Pmaestro.include=<tag>,…` runs only the flows that carry one of those tags, and `-Pmaestro.exclude=<tag>,…` those that carry none of them.
*   **The flows tagged `fold`** run only when they are asked for: `-Pmaestro.include=fold` or `-Pmaestro.flows=flows/fold`. A phone does not have their room.
*   **The report:** the module's `build/maestro/report/report.xml` (JUnit), emptied before each run. The screenshot of the screen where a flow failed is in `debug/<flow>/screenshots/`, that screen's view hierarchy in `debug/<flow>/screen-hierarchy/`, with the steps and the logs beside them. A failing task says where the report is.
*   `-Pmaestro.device=<id>` chooses one when there are several devices: on Android the serial number (`emulator-5554`), on iOS the simulator's UDID. If it is not given, the iOS task uses the first open simulator; the Android task installs the build on the one device connected, and with two connected it stops with adb's "more than one device", pointing at this property.
*   `-Pmaestro.executable=<path>` points to the program; if it is not given, `~/.maestro/bin/maestro`, where Maestro's installer puts it, and if that is not there either, `maestro` on the path.
*   In the iOS task, Xcode's build phase calls the shared module's own Gradle task (`embedAndSignAppleFrameworkForXcode`): the inner Gradle opens a second daemon, and does not wait for the outer one.
*   **How long:** all the flows take about three hours on one platform; a page about a minute and a half, an example eight or nine seconds. Most of the time is Maestro waiting for the screen to settle after each press. A section or a tag ends sooner: `-Pmaestro.flows`, `-Pmaestro.include=smoke`.

**When to use which:** a screen's behaviour, accessibility and platform rules are tested in common tests (`runComposeUiTest`), on five targets. Maestro shows, on a real device, that a real app was installed and that the tags reach the system; it takes in the keyboard, the system bars and the navigation too.

## 4. Without Pantrix {#without}

What follows are the shortest forms that can be copied into a project without the library; Pantrix's code (`ui/testing/src/`) does the same with tests and edge cases.

### 4.1. Building the tag in one place (`commonMain` and `commonTest`)

```kotlin
// commonMain: the component builds its tag with this.
fun testTagOf(component: String, key: String, scope: String? = null): String {
    val slug = key.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_').take(32).trimEnd('_')
    return listOfNotNull(scope, "${component}_$slug").joinToString(".")
}

// commonTest: the test calls the same.
fun SemanticsNodeInteractionsProvider.onComponent(component: String, key: String, scope: String? = null) =
    onNode(hasTestTag(testTagOf(component, key, scope)))
```

### 4.2. A fixed theme and the test's clipboard (`commonTest`)

```kotlin
class KeptClipboard : Clipboard {
    val entries = mutableListOf<ClipEntry?>()
    override suspend fun getClipEntry(): ClipEntry? = entries.lastOrNull()
    override suspend fun setClipEntry(clipEntry: ClipEntry?) { entries += clipEntry }
}

@Composable
fun FixedScreen(clipboard: Clipboard = remember { KeptClipboard() }, content: @Composable () -> Unit) {
    val drawn = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(drawn.density, fontScale = 1f),
        LocalLayoutDirection provides LayoutDirection.Ltr,
        LocalClipboard provides clipboard,
        content = content
    )
}
```

The theme's own defaults too (the language, dark mode, motion, the operating system) must be given openly in the same way; which they are is read in the theme's parameters.

### 4.3. Opening the tags to android's automation (`androidMain`)

```kotlin
// Once at the root; since a Dialog or a Popup is a separate window, at its root too.
Modifier.semantics { testTagsAsResourceId = true }
```

It is not needed on iOS: Compose gives `testTag` as `accessibilityIdentifier`.

## 5. What the helper does not do {#limits toc="5. What it does not do"}

*   **A screenshot helper.** The module is not bound to Roborazzi (3.6).
*   **Pixel and colour helpers.** They stay in Pantrix's own tests.
*   **Fixing the window and the clock by itself.** If `window` is not given, the screen is in the window the test draws (1024×768 on the desktop, 320dp wide in Robolectric); if `clock` is not given, it is the device's clock.
*   **Device automation on the web.** Maestro 2.10's Chromium cannot see Compose's semantics tree (section 8); on the web the common tests run.
*   **A text field's menu on Android.** On Android, when the field is right-clicked with a mouse or while the selection toolbar opens, Compose asks only its own clipboard (`AndroidClipboard`) whether there is something to paste. A test that right-clicks a text field with the test's clipboard fails with `IllegalArgumentException` (seen in Robolectric); focusing, typing and copying do not ask. The toolbar could not be opened in Robolectric: selection by touch fails in the magnifier, whatever the clipboard (an open item).
*   **Knowing a component's inside.** The module knows components by their names; the names of a component's parts (`Segment`, `Day_2026_10_24`) are in that component's KDoc.

## 6. Testing {#testing}

The module's own tests are mostly in common code and run on five targets (the JVM, the iOS simulator, JS, Wasm; Android's in a separate tree): finding, the theme, the doubles and the assertions. The platform's clipboard on the desktop and on iOS is tried by their own tests. Android's host test has no manifest: it shows that the module's manifest declares the activity; when the activity is taken out of the manifest, it fails with "Unable to resolve activity".

Pantrix's own tests use the module too. The `TestTheme` of the components' tests is a thin wrapper over `PantrixTestTheme`: the colours, the motion and the diagnostics come from a theme inside it. Events and haptic feedback are kept with `RecordingAnalytics` and `RecordingHaptics`, and the desktop's clipboard tests use `TestClipboard`. Foundation's and the components' Android host tests have no manifest of their own: the module's manifest declares the activity.

Each behaviour was broken once, and the test was seen to turn red (section 8). The clock is read with two offsets: this machine is at UTC+3, and a test written with one offset would pass on a component that reads the device's clock too.

## 7. Pitfalls {#pitfalls}

*   **`waitForIdle` advances the test's clock.** While the clock advances by itself (`mainClock.autoAdvance`, the default), a finding that waits comes in `waitForIdle`. In a test that drives the clock by hand it does not come: `assertNoFindings` advances the clock itself, a look written by hand does not.
*   **A clock test that uses the same offset as the device's shows nothing.** On a machine at UTC+3, a test written with `{ 3.hours }` passes on a chart that does not read the screen's clock too. Reading with two offsets (Istanbul and New York) prevents this.
*   **A local given from outside the theme loses to the theme's.** `PantrixTestTheme` gives the clipboard and its traits, the operating system, the font scale and the file picker itself; a test that gives these from outside the theme does not get its value to the screen (`CompositionLocalProvider(LocalClipboard provides …) { PantrixTestTheme { … } }`). They are given by parameter, or from inside the theme. A screen density and a clock given from outside do get through: the theme changes only the font scale, and the clock only when `clock` is given.
*   **Compose's text field looks at the platform's clipboard.** It asks the clipboard's `nativeClipboard` whether there is something to paste: on the desktop while the right-click menu opens, on iOS at every focus. A clipboard that does not give it fails with `NotImplementedError`, by the interface's default; on iOS every text field that takes the focus fails. `TestClipboard` gives its own AWT clipboard on the desktop and its own named pasteboard on iOS; a clipboard a test writes itself must give one too.
*   **`setContent` is called once in a test.** A second call gives an error; a changing parameter is given with `mutableStateOf`.
*   **On Android the common tests do not run in the host tree.** A behaviour of Android's own is tested apart, in `androidHostTest`.
*   **On the web, reading the clipboard waits.** `TestClipboard.text()` waits for the browser's promise on the web; on a page not in a secure context, Compose writes an empty text.
*   **The file picker in an iOS test.** In Compose's iOS tests there is no app and no `LocalUIViewController`; `rememberFilePicker()` returns a picker there that never answers.
*   **In Maestro a chosen option is two separate properties on two platforms.** iOS gives every chosen or switched-on control as `selected`: a radio button, a segment, a checkbox, a switch, a chip, a tree's row and a page number. Android gives only a node in the tab role (a tab, a row of the bottom bar and of the side menu) as `selected`, and the others as `checked`. The demo's `subflows/assert_chosen.yaml` and `assert_not_chosen.yaml` look by platform; a tab is looked at with `selected: true` on both.
*   **On iOS, `hideKeyboard` cannot put away a Compose field's keyboard.** Maestro says "Couldn't hide the keyboard" and the flow fails; on Android the back key puts it away. A keyboard left up covers the bottom of the screen: Maestro presses an item's middle, and that can be a key of the keyboard. The demo's flows put typing last, on the page opened again at its head; when the flow ends the app is closed, and the keyboard goes with it. The search in the catalogue is done with an API's name: the single result stays above the keyboard.
*   **The iOS simulator asks as well when it opens a link of the app's own scheme.** `openLink` (`xcrun simctl openurl`) hands the link to the app at once, and opens the system's "Open in “PantrixUI”?" dialog over it too; while the dialog is up, the app's tree is not seen. The dialog takes no press while it is coming in. The demo's `subflows/open_page.yaml` presses "Open" on iOS until the dialog is gone, waiting for its animation to end. The dialog's words are in the simulator's language: the flow knows its English and its Turkish.
*   **A press on a button at the foot of the screen can reach the bottom bar.** Maestro counts an item as visible once an edge of it is on the screen; the middle of a button near the foot can fall on the app's own bottom bar (seen on iOS: a press on "Add digit" reached the bar's "Overview", and the page changed). In the demo's flows each block that comes back to an example's controls first brings the item it presses first to the middle of the screen; what it looks at stands beside it.
*   **An item that cannot be centred.** `centerElement: true` cannot bring an item at the end of the page to the middle, scrolls until its timeout, and then says "No visible element found" (seen on iOS: the button was at the bottom of the screen, and the page scrolled no further). The demo's `subflows/show.yaml` first centres it as an optional step with a short timeout, then settles for the item being on the screen. The end of a page ends above the bars.
*   **A card's code can stay under its description, off the screen.** In an example with a long description, switching to the code puts the code block below the screen: `assertVisible` fails. The demo's `subflows/example.yaml` scrolls down to the code block (`visibilityPercentage: 10`: a long code is never seen whole), then goes back to the switch.
*   **The Android plugin's `installDebug` installs on every connected device.** It does not look at `-Pmaestro.device`: with two devices connected, the app on the other one is installed again, and closed, in the middle of a flow running there (seen). So `maestroTest` does not depend on it: it installs the debug build's APK itself, with `adb -s <id> install`, on the chosen device alone.
*   **On iOS, pressing a page that is sliding only stops the slide.** A press while the scroll's momentum lasts reaches no button; Maestro counts it as done. The demo's example step presses the card's switch inside a `retry`, checking that the switch has turned, and presses again when it must.
*   **An example's own scroller can take the scroll.** Maestro scrolls from the middle of the screen; when a long document in a fixed height (the JSON view's long example) stands there, it takes the scroll and the page stays where it is. The flows of such pages (`# gutter`) take the steps that scroll the page from the gutter beside the cards, at its end (`subflows/*_gutter.yaml`); the gutter at the start falls on the side rail of a foldable.
*   **A condition waits seven seconds for an item that is there.** `when: notVisible` or `repeat: while: notVisible` waits until its timeout while the item is on the screen, and is then skipped. In a step that runs for every example this is over an hour on one platform: the fallbacks stand only in the pages that need them.
*   **An item without a description is not in the tree.** An avatar given no description, a badge or an icon with none, is hidden from a screen reader (`hideFromAccessibility`); Maestro does not see it either. A badge's count is in the name of the icon it stands on: the flow looks at that.
*   **The system bars and the taskbar.** An item scrolled to the bottom of the screen can stay under Android's taskbar (seen on the tablet emulator: the button at y 1674–1800, the taskbar starting at 1682). `centerElement: true` is given to `scrollUntilVisible`.
*   **On iOS, Maestro's driver gets up late at the first launch.** "iOS driver not ready in time": it passed with `MAESTRO_DRIVER_STARTUP_TIMEOUT=240000`. `maestroIosTest` gives it itself.
*   **`id:` is a regular expression.** The `.` in Pantrix's tags matches any character; it is harmless. Keys taken as they are written, like the rows of a JSON view, can have `$`, `[` or `(`: those must be escaped.
*   **The tag of a component with no key depends on its place in the code.** The demo's number cards and page chips have no key (`Card#-1uvwalkaw10zb`): they are only shown, not pressed. Every component a flow presses should take a `tagKey`.

## 8. Verification {#verification}

### Tests

:::table
| What | Where |
| :--- | :--- |
| Finding: the key and its cutting, the scope chain, finding with no key, the parts, the screen's tags and the merged tree | `ui/testing/src/commonTest/.../PantrixTagsTest.kt` (JVM, iOS, JS, Wasm) |
| The theme: the defaults, what is given, Windows on the desktop and the web, the window and keeping the state, the clipboard and a text field that takes the focus over it, the picker, the clock, the diagnostics, the finding that waits, every platform | `PantrixTestThemeTest` |
| The doubles: analytics, haptic feedback, the picker's answers, the clock, the clipboard (hard texts included) and an unwritable clipboard | `TestDoublesTest` |
| The assertions: the name, the text, the touch target, the shortcut | `PantrixAssertionsTest` |
| The platform's clipboard on the desktop: it keeps the last copy, empties when cleared, and every test's clipboard is its own | `ui/testing/src/jvmTest/.../TestClipboardOnTheDesktopTest.kt` |
| The platform's clipboard on iOS: the same, and that the general pasteboard is never written | `ui/testing/src/iosTest/.../TestClipboardOnIosTest.kt` |
| Android: a test with no manifest, the `ClipData` and its label, the picker | `ui/testing/src/androidHostTest/.../TestingOnAndroidTest.kt` |
| The tag's text | `ui/foundation/src/commonTest/.../testing/TestTagTest.kt` |
| The screen's clock and its readers | `PantrixClockTest`, `TimeTextFieldTest`, `ElasticDatePickerTest`, `ChartScreenClockTest` |
| The screen's picker | `LocalFilePickerTest`, `FilesOnAndroidTest` |
:::

### Behaviours broken by hand

*   In the test module 24 behaviours were broken one by one; in each, at least one test turned red: finding with no key telling apart the parts and the longer names, the filtering of the tags, the key built by hand, the theme's operating system, the font scale, the clipboard's traits, the machine's clipboard, the platform's picker, the device's clock, the diagnostics turned off and the findings not kept, the window's frame, every platform set up again, the half-second wait, the order of the name, the touch target's two directions, the shortcut's Command, the text field's text, the picker's types and single file, the copy of the events, the clipboard's last copy, and the clock's advance.
*   The platform clipboard's stand-in was broken. On the desktop in four ways: the stand-in keeps nothing, the copy does not reach the stand-in, the stand-in does not empty when cleared, one stand-in for all clipboards. On iOS in four ways: the stand-in keeps nothing, it does not empty when cleared, one stand-in for all clipboards, no stand-in at all. In all of them a test turned red. The last is the very lack that failed 299 of Pantrix's iOS tests; the test of a text field that takes the focus over the theme's clipboard caught it too. On iOS, a break that writes to the system's general pasteboard was not tried: Simulator can share it with the Mac's.
*   In the three additions in foundation 18 behaviours were broken; in 17 a test turned red. The remaining one is the time field's `remember` key: it makes no difference, since the picker's inside is set up again at every opening.

### Looked at by hand

*   **Android emulator (API 34, a 2208×1840 tablet):** the demo's five flows passed with the Maestro CLI and with `./gradlew :app:androidApp:maestroTest`. The tags show as `resource-id`, the one in the sheet's separate window too.
*   **iPhone 18 Pro Max simulator (iOS 27.0):** the five flows passed with the Maestro CLI and with `./gradlew :app:shared:maestroIosTest` (3 minutes 30 seconds with the Xcode build). The tags show as `accessibilityIdentifier`; on a phone the sections are in the bottom bar.
*   **All the flows, Pixel 7 Pro emulator (Android 13, API 33, 1440×3120):** `./gradlew :app:androidApp:maestroTest` with no properties, on the whole workspace: all 118 flows (4 of the shell, 114 of the pages) passed, in 2 hours 40 minutes. The JUnit report wrote 118 test cases, 0 failures; their tags are right by section. The flows tagged `fold` did not run.
*   **All the flows, iPhone 18 Pro Max simulator (iOS 27.0):** `./gradlew :app:shared:maestroIosTest` with no properties: 116 of the 118 flows passed, in 3 hours 2 minutes. Of the two that failed, one was a flow fixed after the run had started; in the other a press on a button at the foot of the screen had reached the bottom bar (section 7). These two, the 25 flows that fix changed, the shell and the Elements section ran again in their last form and passed; the same passed on Android too. The flows that last fix did not touch passed in their form before the step that waits for the screen to settle on iOS (`subflows/show_still.yaml`).
*   **Foldable, Pixel 9 Pro Fold emulator (Android 15, API 35, inner screen 852×883 dp):** with the hinge half opened by `adb emu sensor set hinge-angle0 90`, `-Pmaestro.include=fold` ran the three flows, and all three passed. With the hinge flat (`180`) the list-and-detail flow failed at the step that checks the posture. On the same device `-Pmaestro.include=shell` ran only the shell's four flows, and all four passed with the side rail.
*   **The install goes on the chosen device alone:** with two emulators connected (a Pixel 7 Pro and a Pixel 9 Pro Fold), `-Pmaestro.device=emulator-5554` installed the debug build on that one alone; the other, whose app had been removed first, still had none. With no device named, the task stopped with adb's "more than one device", pointing at `-Pmaestro.device`.
*   **A window of the app's own:** with `pantrixAutomationRoot()` on its root, the demo's own `Popup` on the "Building blocks" page had its "Close" button found as `Button_close_layer` on Android too; unmarked, it was in the tree by its text alone. After Pantrix's overlays moved to the same modifier, the eight flows of the overlays and the two of the selects passed on both platforms.
*   **The task's choices:** `-Pmaestro.flows` ran a file, a folder and several paths apart by commas; `-Pmaestro.include` filtered by tag; a run with no properties left `fold` out. A failing flow's screenshot and view hierarchy were written under `build/maestro/report/debug/<flow>/`; the task failed saying where the report is.
*   **The flows' checks are real:** a flow that checks an unchosen option as "chosen" failed on both devices; on iOS the platform's branch ran, on Android Android's.
*   **The web (Maestro's Chromium):** the page opens and is drawn, but Maestro's tree has only the `#app` container and two empty elements. Compose 1.12.2 mirrors the semantics tree into a hidden DOM (`#cmp_a11y_root`; `opacity: 0`, `pointer-events: none`): on the overview the `id` of 57 elements is a Pantrix tag (`Button_browse`, its role `button`, its name "Browse the components"). The elements on the screen stand at their node's place and size (`position: fixed`), those off the screen are zero-sized. This tree is inside the shadow root of the element that carries the canvas; Maestro 2.10 does not go in there.
*   **The manifest merge:** the own manifests of foundation's and the components' Android host tests were deleted; their tests (40 and 193) pass with the module's manifest.
*   **The demo's three new examples on the web:** "The screen's clock" shows 24 October in Kiritimati and 23 October in Pago Pago; "A screen with sample files" writes the two sample files; "The tag a test looks for" writes the tag looked for and the tag carried the same.

### Not verified

*   Real devices, Maestro Cloud, and a CI machine on Linux or Windows. Two languages on an Android phone: the full run ran in English, the device's language; the flows are written to pass in Turkish too (tags, and the words of both languages), and no full run was made on a Turkish device.
*   A separate project that takes the module from Maven: its POM, its module metadata and the Android AAR's manifest were generated and read, but not published to the local repository.
*   Finding the tags on the web with a tool that goes into the shadow root (for example Playwright's selectors).
*   On Android, a selection toolbar opened with the test's clipboard (section 5): read in Compose's source; it could not be opened in Robolectric, since selection by touch fails in the magnifier, and it was not run on a device.

## 9. Where it is in this repository {#repository toc="9. In the repository"}

*   The test module: `ui/testing/src/commonMain/kotlin/com/pantrixui/testing/` (`PantrixTags.kt`, `PantrixTestTheme.kt`, `TestClipboard.kt` and each platform's reading and clipboard, `RecordingAnalytics.kt`, `RecordingHaptics.kt`, `TestFilePicker.kt`, `TestFileSaver.kt`, `TestClock.kt`, `PantrixAssertions.kt`); the manifest is `ui/testing/src/androidMain/AndroidManifest.xml`.
*   Foundation's additions: `testing/TestTag.kt` (`pantrixTestTagOf`), `date/PantrixClock.kt`, `files/FilePicker.kt` (`LocalFilePicker`); the automation bridge is `testing/AutomationBridge.kt` (`pantrixAutomationRoot`).
*   Maestro: the flows in `app/maestro/`; the tasks in `build-logic/src/main/kotlin/com/pantrixui/plugins/maestro/` (`pantrix.demo.maestro`, applied in `app/androidApp` and `app/shared`).
*   The demo: `app/shared/src/commonMain/kotlin/com/pantrixui/cmp/demo/samples/TestTagsSamples.kt` ("The tag a test looks for"), `FilesSamples.kt` ("A screen with sample files"), `DatePickerSamples.kt` ("The screen's clock").

## 10. What is left open {#open}

In `docs/OPEN_ITEMS.md`: device automation on the web, and the text field menu opened with the test's clipboard on Android.
