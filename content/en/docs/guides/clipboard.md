---
title: Copying text to the clipboard
description: Why copying a text to the clipboard is not a single call in Compose Multiplatform, what the library gives for it, what a copy button should say and when, how the text on the clipboard is read, how a paste is heard, and how these are tested. The problem is not Pantrix's own: it is seen the same way in every Compose Multiplatform project that has a "copy" button in its common code.
---

Everything said here about Compose was read in the sources of Compose Multiplatform 1.12.1 or seen in a test that runs in this repository; which was seen where is written in sections 7 and 8. What could not be verified is counted apart in section 8.

## 1. The problem {#problem}

:::aside clipboard
### Symptom

*   In common code it used to be one line: `LocalClipboardManager.current.setText(AnnotatedString(text))`. This interface was deprecated; the compiler says "Use LocalClipboard instead which supports suspend functions".
*   `LocalClipboard`, which came in its place, has no call for "copy this text". The one write call is `setClipEntry(clipEntry: ClipEntry?)`, and a `ClipEntry` cannot be built in common code.
*   The call suspends (`suspend`): it cannot be called straight from a button's `onClick`; it needs a coroutine.
*   How a copy ends changes from platform to platform. The browser can refuse the write; on a page not in a secure context nothing is copied, and no one says so; on Android 13 and above the system shows what was copied itself. A button that says "copied" the moment it is pressed says something wrong in all three.
:::

### Cause

`ClipEntry` is only a name in common code: `expect class ClipEntry { val clipMetadata: ClipMetadata }` (`ClipboardManager.kt`). Its constructor and everything in it are in the platform's own declaration (Compose Multiplatform 1.12.1; on Android, `androidx.compose.ui` 1.12.1):

:::table
| Platform | What `ClipEntry` is there | How plain text is built | Mark |
| :--- | :--- | :--- | :--- |
| Android | A class that wraps a `ClipData`: `ClipEntry(val clipData: ClipData)` | `ClipEntry(ClipData.newPlainText(label, text))` | None |
| Desktop (JVM) | A class that wraps an object: `ClipEntry(nativeClipEntry: Any)`. The clipboard reads it as a `java.awt.datatransfer.Transferable` | `ClipEntry(StringSelection(text))` | The constructor is `@ExperimentalComposeUiApi` |
| iOS | Compose's own class; its constructor is `internal` | `ClipEntry.withPlainText(text)` | `@ExperimentalComposeUiApi` |
| Web, `js` | `ClipEntry(clipboardItems: Array<ClipboardItem>)` | `ClipEntry.withPlainText(text)` | The constructor is experimental, `withPlainText` is not |
| Web, `wasmJs` | `ClipEntry(clipboardItems: JsArray<ClipboardItem>)` | `ClipEntry.withPlainText(text)` | The constructor is experimental, `withPlainText` is not |
:::

Source files: `AndroidClipboardManager.android.kt`, `PlatformClipboard.desktop.kt`, `PlatformClipboard.ios.kt`, `PlatformClipboard.js.kt`, `PlatformClipboard.wasm.kt`.

Three details make the work bigger:

1.  **The two web targets are two separate declarations.** `ClipEntry` is declared separately for `js` and for `wasmJs`; the source set the two share (`webMain`) sees only the empty declaration in common code. The counterpart of an `expect` function is therefore written five times: Android, iOS, desktop, JS, Wasm.
2.  **Experimental marks.** The desktop's constructor and iOS's `withPlainText` carry `@ExperimentalComposeUiApi`; every place that uses them needs `@OptIn`.
3.  **Writing suspends, because it does in the browser.** The browser's clipboard returns a `Promise`; Compose waits for it, and if it is rejected the error is thrown from that call. The old interface started the write in the browser and did not wait for its result (`JsPlatformClipboardManager.setText`); its read, too, always returned empty there (`getText(): AnnotatedString? = null`). It was an interface that could not tell the result.

### How the platforms finish the write

:::table
| Platform | What Compose does | What can go wrong |
| :--- | :--- | :--- |
| Android | `ClipboardManager.setPrimaryClip(clipData)` | The system service's error is thrown from the call as a `RuntimeException` (`rethrowFromSystemServer`, the Android SDK's source) |
| Desktop | `setContents(transferable, owner)` on the system's clipboard | `IllegalStateException` if the clipboard is not available at that moment (the JDK's documentation). In a headless JVM (`java.awt.headless=true`) there is no clipboard: the write is silently skipped |
| iOS | `UIPasteboard.generalPasteboard.string = text` | No error path in the source |
| Web | If the full clipboard interface is there, `navigator.clipboard.write([ClipboardItem])`, and if not, `writeText(text)`; both are waited for | If the browser refuses, the error is thrown from the call. In an insecure context there is no clipboard: the write is silently skipped, and a warning falls into the console |
:::

In the two places where it says "silently skipped", `setClipEntry` returns without throwing: the code that wraps it thinks the text was copied.

## 2. The solution: three pieces {#fix}

:::table
| # | Piece | What it gives |
| :--- | :--- | :--- |
| 1 | **Build the type on the platform.** One `expect` function, five counterparts | Common code says "this text"; it does not know the platform's type |
| 2 | **Make the result an answer.** A suspending function that wraps the write: was it copied | The button says what happened, not that it was pressed. Everything the platform throws is one answer: it could not be copied. The one exception is cancellation, which is thrown again |
| 3 | **Know the clipboard's traits beforehand.** Is there a clipboard to write to; does the system show what was copied itself | The two cases that throw no error are told right too: the page with no clipboard, and Android 13 |
:::

Without the third piece the first two go wrong in two places. On a page with no clipboard no error is thrown, and the answer is "copied"; on Android 13 the answer is right, but the app says once more what the system has just shown.

## 3. With Pantrix {#with}

The library gives all three pieces (`com.pantrixui.foundation.platform`):

:::table
| API | What it does |
| :--- | :--- |
| `rememberPantrixClipboard()` | A clipboard for plain text: over the clipboard `LocalClipboard` gives, with the traits of `LocalClipboardTraits`. It is the same object across recompositions; when the clipboard or the traits change, a new one is given |
| `PantrixClipboard.copyText(text, label)` | Puts the text on the clipboard and says whether it was put (`suspend`, `Boolean`). If there is no clipboard to write to, it does not go to the clipboard at all and says `false`. `label` is carried only on Android |
| `PantrixClipboard.readText()` | Reads the plain text on the clipboard (`suspend`, `String?`). It says `null` where the clipboard holds no text, where there is no clipboard to read from, or where the platform refused the read; with no clipboard to read from it does not go to the clipboard at all |
| `PantrixClipboard.traits` | This clipboard's traits |
| `ClipboardTraits(writable, announcedBySystem, readable)` | Three traits: is there a clipboard to write to; does the system show what was copied itself; is there a clipboard to read from |
| `currentClipboardTraits()` | The platform's traits, as they are at the moment asked. On Android from API 33, `announcedBySystem`; in the browser if the page is not in a secure context, and on the desktop if the JVM runs headless, `writable` and `readable` are false; in the browser without `navigator.clipboard.read` (Firefox before 127) `readable` is false |
| `LocalClipboardTraits` | The traits `rememberPantrixClipboard()` looks at. If none are given, the platform's; a test gives another platform's from here |
| `Modifier.onPaste(enabled, onPaste)` | Hears the reader's paste, ⌘V or Ctrl+V, while the focus is inside the node, and calls `onPaste` with its text; unless a text field inside takes the paste itself |
:::

### 3.1. The copy button

```kotlin
// commonMain
@Composable
fun CopyLinkButton(link: String) {
    val clipboard = rememberPantrixClipboard()
    val snackbars = LocalPantrixSnackbars.current
    val scope = rememberCoroutineScope()
    PantrixButton(
        text = "Copy the link",
        onClick = {
            scope.launch {
                // Suspends: in the browser it returns when the write is done.
                // The answer says what happened, not that it was pressed.
                val copied = clipboard.copyText(link, label = "Link")
                val message = when {
                    !copied -> "The link could not be copied"
                    // Android 13 and above: the system shows what was copied itself.
                    clipboard.traits.announcedBySystem -> null
                    else -> "Link copied"
                }
                message?.let { snackbars?.show(message = it) }
            }
        }
    )
}
```

Not one line looks at the platform: the same button compiles on all five targets. `rememberCoroutineScope()` is the button's own scope; if the screen is left, a copy left halfway is cancelled too, and no answer comes.

### 3.2. What the button says, and when it keeps quiet

:::table
| Case | `copyText` | Trait | The button |
| :--- | :--- | :--- | :--- |
| Copied; the system shows nothing (desktop, iOS, browser, Android 12L and below) | `true` | `announcedBySystem = false` | Says "Copied" |
| Copied; the system shows it itself (Android 13 and above) | `true` | `announcedBySystem = true` | **Keeps quiet** |
| The platform refused (the browser did not allow it, the clipboard is not available at that moment) | `false` | — | Says "Could not be copied" |
| There is no clipboard to write to (a page not in a secure context) | `false`; the clipboard is not gone to | `writable = false` | Says "Could not be copied" |
:::

*   **On every platform a failed copy is said.** The system does not show it; on Android 13 too, the app says it.
*   **`writable` is known before the press.** Drawing the button disabled from the start, or not drawing it at all, is a choice too: `enabled = clipboard.traits.writable`. The sample app in this repository draws the button, and says "could not be copied" when it is pressed.
*   **The rule is the app's.** The library says what happened; the app decides what to say. In the sample app this decision is one function, and is tested apart (`copyMessage`, `CopyMessageTest`).

### 3.3. The label

`label` says what was put on the clipboard: "Link", "Code". Only Android carries it; the documentation of `ClipData.newPlainText` calls it "User-visible label for the clip data", so it is given in the app's language. On the other platforms it is ignored. If it is not given, it is empty.

### 3.4. Reading the clipboard and hearing a paste

A screen takes what is on the clipboard in two ways: by a button (`readText()`) or by the reader's own paste, ⌘V or Ctrl+V (`Modifier.onPaste`).

```kotlin
// commonMain
@Composable
fun StartScreen(open: (String) -> Unit) {
    val clipboard = rememberPantrixClipboard()
    val scope = rememberCoroutineScope()
    // Anywhere in the window: the root holds the focus while nothing else does.
    Column(Modifier.shortcutRoot().onPaste { text -> open(text) }) {
        PantrixButton(
            text = "Paste from the clipboard",
            // Suspends: a browser and iOS may ask first.
            onClick = { scope.launch { clipboard.readText()?.let(open) } },
            enabled = clipboard.traits.readable,
            tagKey = "paste"
        )
    }
}
```

:::table
| Platform | `readText()` | ⌘V or Ctrl+V (`onPaste`) |
| :--- | :--- | :--- |
| Android | Reads; from Android 12 on the system says with a notice that the clipboard was read | The hardware keyboard's key: it reads the clipboard as the key goes down |
| Desktop | Reads: another app's copy off the main thread, a text held in memory where it is; on a headless JVM `readable` is false | It reads the clipboard as the key goes down |
| iOS | iOS asks before it reads what another app copied | The hardware keyboard's key: it reads the clipboard as the key goes down. With a text field in the focus UIKit does the paste; the node leaves the key to it |
| Web | The browser asks: Chrome once for the page, Safari at each read with a "Paste" button of its own; both answer only just after a press | The browser's own paste: the text comes without a question |
:::

*   **A text field pastes into itself.** If the focus is in a text field inside, the paste is the field's; `onPaste` hears nothing.
*   **The inner one takes it.** Of two `onPaste`s around the focus, the inner one takes the paste; one with `enabled = false` leaves it to the one around it.
*   **⌘V is not a shortcut.** On the web `onShortcut(PantrixShortcut(Key.V, command = true))` keeps the key from the browser, and the browser does not paste at all. The paste is heard with `onPaste`.
*   **An empty clipboard.** If the clipboard holds no text, `readText()` returns `null` and `onPaste` is not called.

## 4. Without Pantrix {#without}

The same solution, with Compose Multiplatform's own API only. The code below can be copied into any project; it is a simplified form of its counterparts in the library.

### 4.1. Common code (`commonMain`)

```kotlin
/** [text], in the type this platform's clipboard takes. Only Android carries the label. */
expect fun plainTextClipEntry(label: String, text: String): ClipEntry

/** The clipboard's two traits on this platform. */
data class ClipboardTraits(
    val writable: Boolean = true,
    val announcedBySystem: Boolean = false
)

/** This platform's clipboard traits, as they are at the moment asked. */
expect fun currentClipboardTraits(): ClipboardTraits

/** The traits the buttons look at. A test gives another platform's traits from here. */
val LocalClipboardTraits = staticCompositionLocalOf { currentClipboardTraits() }

/** Puts [text] on the clipboard and says whether it was put. */
suspend fun Clipboard.copyText(
    text: String,
    label: String = "",
    traits: ClipboardTraits = currentClipboardTraits()
): Boolean = traits.writable &&
    try {
        setClipEntry(plainTextClipEntry(label, text))
        true
    } catch (cancelled: CancellationException) {
        // Cancellation is not an answer: if it is swallowed, the cancelled coroutine goes on its way.
        throw cancelled
    } catch (_: Throwable) {
        // The browser's refusal is not a Kotlin `Exception`.
        false
    }
```

`CancellationException` here is `kotlin.coroutines.cancellation.CancellationException`.

### 4.2. Platform counterparts

```kotlin
// androidMain
actual fun plainTextClipEntry(label: String, text: String): ClipEntry =
    ClipEntry(ClipData.newPlainText(label, text))

// From Android 13 (API 33) on, the system shows what was put on the clipboard with its own preview.
actual fun currentClipboardTraits(): ClipboardTraits =
    ClipboardTraits(announcedBySystem = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
```

```kotlin
// jvmMain
@OptIn(ExperimentalComposeUiApi::class)
actual fun plainTextClipEntry(label: String, text: String): ClipEntry = ClipEntry(StringSelection(text))

// A JVM with no screen (headless) has no system clipboard; Compose skips writing there without throwing.
actual fun currentClipboardTraits(): ClipboardTraits =
    ClipboardTraits(writable = !GraphicsEnvironment.isHeadless())
```

```kotlin
// iosMain
@OptIn(ExperimentalComposeUiApi::class)
actual fun plainTextClipEntry(label: String, text: String): ClipEntry = ClipEntry.withPlainText(text)

actual fun currentClipboardTraits(): ClipboardTraits = ClipboardTraits()
```

```kotlin
// jsMain and wasmJsMain: the same line, in two separate files
actual fun plainTextClipEntry(label: String, text: String): ClipEntry = ClipEntry.withPlainText(text)
```

```kotlin
// webMain
actual fun currentClipboardTraits(): ClipboardTraits = ClipboardTraits(writable = browserClipboardWrites())

// The browser gives the clipboard only in a secure context: https, or the machine itself.
@OptIn(ExperimentalWasmJsInterop::class)
private fun browserClipboardWrites(): Boolean =
    js("window.isSecureContext === true && Boolean(window.navigator.clipboard && window.navigator.clipboard.writeText)")
```

The question asked of the browser is written once, in `webMain`; `ClipEntry.withPlainText` cannot be, and stands apart on the two targets (see section 7, "A counterpart written in `webMain`"). In a project with only one web target, both are written in that target's source set.

### 4.3. Use

```kotlin
// commonMain
@Composable
fun CopyLinkButton(link: String, snackbars: SnackbarHostState) {
    val clipboard = LocalClipboard.current
    val traits = LocalClipboardTraits.current
    val scope = rememberCoroutineScope()
    Button(
        onClick = {
            scope.launch {
                val copied = clipboard.copyText(link, label = "Link", traits = traits)
                val message = when {
                    !copied -> "The link could not be copied"
                    // Android 13 and above: the system shows what was copied itself.
                    traits.announcedBySystem -> null
                    else -> "Link copied"
                }
                message?.let { snackbars.showSnackbar(it) }
            }
        }
    ) {
        Text("Copy the link")
    }
}
```

## 5. What the helper does not do {#limits toc="5. What it does not do"}

The helper puts a plain text on the clipboard and says whether it was put, reads the plain text on the clipboard, and hears the reader's paste. It does not do these:

*   **It draws no paste menu.** The "Paste" in a text field's menu is the field's; `onPaste` hears only the key and the browser's paste.
*   **It puts nothing but plain text.** It carries no styled text, HTML, address (URI), image or file.
*   **It does not empty the clipboard.** In Compose that is `setClipEntry(null)`.
*   **It does not mark the content as sensitive.** On Android, the mark that asks for content such as a password to be hidden while it is shown is `ClipDescription.EXTRA_IS_SENSITIVE` (the Android SDK's source); `ClipData.newPlainText` does not put it.
*   **It does not tell.** The app shows the snackbar; the helper only says what happened.

If a project is to carry more than plain text itself, or to read without Pantrix, the way again parts from platform to platform. What Compose's sources show is below; the reading column is taken from Compose Foundation's own reader, which its text fields use when pasting (`ClipboardUtils.*.kt`, `readText()`). That reader is `internal`; an app cannot call it.

:::table
| Platform | Reading the text back | Something other than plain text |
| :--- | :--- | :--- |
| Android | `getClipEntry()?.clipData?.getItemAt(0)?.text?.toString()`. At the first access to the clipboard's content the system shows a warning (by its name in Compose's documentation, "App pasted from Clipboard"); only to ask what is on the clipboard, `clipboard.nativeClipboardManager.primaryClipDescription` | Any `ClipData`: `ClipEntry(clipData)` or `clipData.toClipEntry()`. For the first address in it, `firstUriOrNull()` (experimental) |
| Desktop | `getClipEntry()?.asAwtTransferable` (experimental); if `isDataFlavorSupported(DataFlavor.stringFlavor)`, `getTransferData(DataFlavor.stringFlavor) as? String`. Foundation does this on `Dispatchers.IO` for another app's copy, where it is for a `StringSelection` held in memory, and counts an `IOException` as "no text" | Any `Transferable`: `ClipEntry(transferable)`. If it is also a `ClipboardOwner`, it is given as the clipboard's owner |
| iOS | `getClipEntry()?.getPlainText()` (experimental). If the clipboard is empty, `getClipEntry()` returns null | Compose's `ClipEntry` carries only text ("Currently, it operates only with string(s)"); for the rest, `UIPasteboard` is used directly |
| Web | `getClipEntry()?.clipboardItems` (experimental); the first item's `getType("text/plain")` gives a `Blob`, and the text comes with `blob.text()`, as a `Promise`. The array is `Array` in `js` and `JsArray` in `wasmJs`: it is written apart on the two targets. If the browser refuses the read, Compose throws no error and returns an empty entry; if there is no `read` at all, it returns null | `ClipEntry(clipboardItems)` with your own `ClipboardItem`s (the constructor is experimental). In a browser without the full clipboard interface, Compose writes only plain text, with `writeText` |
:::

## 6. Testing {#testing}

### 6.1. A fake clipboard

Compose gives a test the real clipboard (see section 7, "A clipboard given no fake"). Every test that copies gives its own clipboard with `LocalClipboard`:

```kotlin
/** A clipboard that keeps everything put on it and does not touch the machine's clipboard. */
class RecordingClipboard(private val refuses: Boolean = false) : Clipboard {
    val entries = mutableListOf<ClipEntry?>()

    override suspend fun getClipEntry(): ClipEntry? = entries.lastOrNull()

    override suspend fun setClipEntry(clipEntry: ClipEntry?) {
        check(!refuses) { "NotAllowedError: Write permission denied." }
        entries += clipEntry
    }
}
```

`Clipboard` is in common code: this fake can be written in a common test too. What can be looked at there is how many times it was written to and what `copyText` says; the text inside the `ClipEntry` that comes cannot be read in common code.

### 6.2. Another platform's traits

The behaviour of Android 13, or of a page with no clipboard, is tested in a test that does not run on that platform too: the traits are given with `LocalClipboardTraits`.

```kotlin
// jvmTest
@OptIn(ExperimentalTestApi::class, ExperimentalComposeUiApi::class)
class CopyLinkButtonTest {
    @Test
    fun a_copy_the_system_shows_itself_is_not_told_twice() = runComposeUiTest {
        val clipboard = RecordingClipboard()
        val snackbars = PantrixSnackbarState()
        setContent {
            PantrixTheme {
                CompositionLocalProvider(
                    LocalClipboard provides clipboard,
                    // Android 13's traits, in a test that runs on the desktop.
                    LocalClipboardTraits provides ClipboardTraits(announcedBySystem = true),
                    LocalPantrixSnackbars provides snackbars
                ) {
                    CopyLinkButton(link = "https://example.com")
                }
            }
        }
        onNodeWithText("Copy the link").performClick()
        waitForIdle()
        // The text is read as the desktop reads it: with the `Transferable`'s text flavour.
        val copied = clipboard.entries.single()?.asAwtTransferable?.getTransferData(DataFlavor.stringFlavor)
        assertEquals("https://example.com", copied)
        assertEquals(null, snackbars.current)
    }
}
```

With `ClipboardTraits(writable = false)` the same test tries the page with no clipboard: nothing comes to the fake clipboard, and the button says "could not be copied". `RecordingClipboard(refuses = true)` is a browser that refuses.

### 6.3. Reading the text that came

Each platform's test looks at the text itself its own way:

:::table
| Platform | Where the test runs | How the text is read |
| :--- | :--- | :--- |
| Desktop | `jvmTest`, with the fake clipboard | `entry.asAwtTransferable?.getTransferData(DataFlavor.stringFlavor)` |
| Android | Robolectric, with the real clipboard: Robolectric's clipboard is in the test's memory | `LocalClipboard.current.nativeClipboardManager.primaryClip`: the first item's `text`, `description.label` |
| iOS | The simulator, with the fake clipboard | `entry.getPlainText()` |
| Web | The browser, with Compose's own clipboard | The test's functions are put in place of `navigator.clipboard.write` and `writeText`; the `text/plain` part of the item that comes to `write` is read |
:::

On the web the way is different. `webTest`, like `webMain`, is the source set the two targets share, and the inside of `ClipEntry` cannot be written there in one form: its items are an `Array` on one target and a `JsArray` on the other. What the test stands in for is not Compose's clipboard but the browser's: so what Compose really gives the browser is seen, and a refused write goes its real way too (`Promise.reject(new DOMException(…, 'NotAllowedError'))`). Both functions are stood in for; Compose decides which to call by looking at the browser.

### 6.4. Testing a paste

An app's test puts a text on the clipboard as another app would have copied it: `TestClipboard.putText(text)` (`com.pantrixui:testing`). The screen's `readText()` reads it:

```kotlin
val clipboard = TestClipboard()
val opened = mutableListOf<String>()
setContent { PantrixTestTheme(clipboard = clipboard) { StartScreen(open = { opened += it }) } }
clipboard.putText("{\"a\": 1}")
onPantrixNode("Button", "paste").performClick()
waitForIdle()
assertEquals(listOf("{\"a\": 1}"), opened)
```

A paste by the key is tried with `performPaste(text, clipboard, system)`: it puts the text on the clipboard and presses the paste key as that system's keyboard does. On the web ⌘V is the browser's own paste and comes from the machine's clipboard; a test's key does not bring it. There the helper sends the page the browser's paste while the key is down: a `ClipboardEvent` carrying the text, rising from the element that has the focus. So the same test passes on all five targets:

```kotlin
onPantrixNode("Screen", "start").performPaste("{\"a\": 1}", clipboard, OperatingSystem.Android)
assertEquals(listOf("{\"a\": 1}"), opened)
```

`system` is the system the screen is typed on, `PantrixTestTheme(system = …)`; the theme's default is Android, whose paste is Ctrl+V. A text field is typed into with `performTextInput`.

## 7. Pitfalls {#pitfalls}

:::table
| Pitfall | What happens | What is done | Where it was seen |
| :--- | :--- | :--- | :--- |
| **A clipboard given no fake** | In a test, `LocalClipboard` is the platform's real clipboard: on the desktop, the system's clipboard. A test that copies writes over whatever is copied on the machine at that moment | Give your own `Clipboard` in every test | Source: `RootNodeOwner.skiko.kt` (`createPlatformClipboard()`) |
| **Reading the text in common code** | The one member, in common code, of the `ClipEntry` that comes to the fake clipboard is `clipMetadata`; the text cannot be reached | In the common test, count the writes; read the text in the platform's own test (6.3) | Source; while these tests were written |
| **`clipMetadata`** | On the desktop, iOS and the web it is `TODO()`: when called it throws `NotImplementedError` | Do not call it | Source: the four `PlatformClipboard.*.kt` (CMP-1260) |
| **An insecure context** | A page served over plain http has no clipboard. Compose skips the write, writes a warning to the console and throws no error: a button that tells the result from an error says "copied" | Ask before writing: `traits.writable` | Compose's skipping in the source: `PlatformClipboard.js.kt`, `.wasm.kt`. `writable` saying "none" in a test, with `navigator.clipboard` hidden |
| **A headless JVM** | With `java.awt.headless=true` there is no system clipboard. Compose skips the write and throws no error: a button that tells the result from an error says "copied" | Ask before writing: on the desktop `writable` is `!GraphicsEnvironment.isHeadless()` | Source: `PlatformClipboard.desktop.kt`. Before the trait was added, in a temporary test: the answer `true`, what was read back `null`. The trait's rule in a test: `desktopClipboardTraits(headless = true)` |
| **`catch (e: Exception)`** | The browser's refusal is not a Kotlin `Exception` (in JS it is the browser's own error, in Wasm a `JsException`). Code that catches only `Exception` misses it; the error is thrown from the button's coroutine | Catch `Throwable` | Test: with the catch narrowed to `Exception`, `a_write_the_browser_refuses_is_answered_false` is red on both JS and Wasm |
| **A swallowed cancellation** | `catch (e: Throwable)` catches cancellation too: the coroutine of a screen that was left goes on its way with the answer "could not be copied" | Catch `CancellationException` first and throw it again | Test: `a_copy_whose_coroutine_is_cancelled_is_cancelled_and_answers_nothing` |
| **A copy told twice** | On Android 13 and above the system shows what was copied with its own preview; if the app says so too, the same thing is said twice | Keep quiet while `announcedBySystem`; still say a failed copy | Android's documentation; see section 8, "Not Verified" |
| **A counterpart written in `webMain`** | If `ClipEntry.withPlainText` is called in `webMain`, the two targets' builds pass; the shared source set's build (`compileWebMainKotlinMetadata`) and the editor say "Unresolved reference 'withPlainText'" | Write the counterpart in `jsMain` and in `wasmJsMain` separately | By compiling with a temporary file |
| **Experimental marks** | The desktop's constructor, iOS's `withPlainText` and, on the reading side, `asAwtTransferable`, `getPlainText()` and `clipboardItems` carry `@ExperimentalComposeUiApi`; they can change in a later version of Compose | Let the `@OptIn` stand in one place, inside the helper | Source |
| **A web test that stands in for one function** | Compose calls `write` if the full clipboard interface is there, and `writeText` if not. If the test stood in for only one, the other writes to the real clipboard | Stand in for both; before copying, see that they are in place | Source: `PlatformClipboard.js.kt`, `.wasm.kt` |
| **`nativeClipboard` on a fake clipboard** | The interface's `nativeClipboard` is deprecated, and throws `NotImplementedError` if it is not given. On the desktop a text field's right-click menu still asks the clipboard with it | Not needed for a copy button. If the same test also has a text field's menu, give it on the fake clipboard too | Source: `Clipboard.kt`, `ClipboardUtils.desktop.kt`. In this repository, `TextFieldDesktopTest` |
| **Reading on Android** | `getClipEntry()` reaches the clipboard's content; at the first access the system shows the warning "App pasted from Clipboard" | Only to ask what is there, `primaryClipDescription` | Source: the documentation of `Clipboard.kt` |
| **Making ⌘V a shortcut** | On the web `onShortcut` keeps the key from the browser; the browser does not paste and no `paste` event comes | Hear the paste with `Modifier.onPaste` | Source: `ComposeWindowInternal.web.kt` (`processClipKeyDown`); the browser sends a paste only if the key was not kept |
| **Pasting twice on iOS** | UIKit pastes ⌘V into the focused field and hands the key to Compose as well; Compose's field does not take the key, and a key listener around it takes the same text once more | Leave the key alone while a text input of UIKit's has the focus: `onPaste` does so | Source: `InputViews.ios.kt` (`pressesBegan`), `NativeTextInputView.ios.kt` (`paste`). In a test iOS's field did not take ⌘V (`OnPasteTest`) |
| **Reading without a press** | A browser answers a read only just after a press; reading at start or from a timer is refused | Tie the read to a button; `onPaste` for ⌘V | The browsers' documentation; not tried |
| **A label that carries no language** | The label is a text the user sees; a label buried in the code stays in the old language when the app's language changes | Let the caller give the label, in the app's language | The Android SDK's source (`ClipData.newPlainText`); test: the label comes on Android and not elsewhere |
:::

## 8. Verification {#verification}

### Tests

:::table
| Test | Where it runs | What it looks at |
| :--- | :--- | :--- |
| `ui/foundation`, `PantrixClipboardTest` | The JVM, the iOS simulator, JS, Wasm (9 tests) | A copy puts an entry on the clipboard and says `true`; while there is no clipboard to write to it says `false` and does not touch the clipboard at all; whatever the clipboard throws, the answer is `false`; a cancelled copy is cancelled and gives no answer; the remembered clipboard is the same across recompositions, is a new one when the clipboard or the traits change, and looks at the new one |
| `ui/foundation`, `PantrixClipboardOnTheDesktopTest` | The JVM (4 tests) | What comes is a `Transferable` whose text flavour is the text given; line ends, non-ASCII letters and emoji come whole; a text held in memory is read on the thread that asks, another app's copy away from it; the desktop's traits: written and read on a JVM with a screen, neither on a headless one |
| `ui/foundation`, `PantrixClipboardOnAndroidTest` | Robolectric (7 tests) | The first item on the system's clipboard is the text given, its label the label given, its type plain text; if no label is given it is empty; `announcedBySystem` is true on API 33 and 34, and false on 32 and 30 |
| `ui/foundation`, `PantrixClipboardOnIosTest` | The iOS simulator (3 tests) | The `getPlainText()` of the entry that comes is the text given; a hard text comes whole; iOS's traits |
| `ui/foundation`, `PantrixClipboardOnTheWebTest` | JS and Wasm in ChromeHeadless (5 tests) | On the test runner's page `writable` is true; with `navigator.clipboard` hidden it is false; the `text/plain` part of the item given to the browser is the text given; a hard text comes whole; the answer to a write the browser refuses is `false` |
| `ui/foundation`, `PantrixClipboardReadTest` | The JVM, the iOS simulator, JS, Wasm (6 tests) | A copied text is read back as it went, a hard text whole; the last copy is read; an empty clipboard gives no text; with no clipboard to read from the clipboard is never asked; a clipboard that refuses the read gives no text |
| `ui/foundation`, `OnPasteTest` | The JVM, the iOS simulator, JS, Wasm (7 tests) | The paste key gives the clipboard's text to the node around the focus and to one after the focused node in its chain, not on the web: there the key is the browser's; a node away from the focus hears nothing; a text field inside pastes into itself; the inner node takes it; one with `enabled = false` leaves it to the one around it; an empty clipboard pastes nothing |
| `ui/foundation`, `OnPasteOnTheWebTest` | JS and Wasm in ChromeHeadless (8 tests) | The browser's paste is handed on with its text and marked as taken; one not taken is left to the browser; one a field took is not handed on; once closed nothing is heard; the node hears the paste that follows the key going down inside it, not one with no key before it; on a window's root after `shortcutRoot` too; the inner one takes it; one with `enabled = false` leaves it to the one around it |
| `ui/testing`, `TestDoublesTest` | The JVM, the iOS simulator, JS, Wasm | The screen reads the text put with `putText`; cleared, it reads none |
| `ui/testing`, `PantrixAssertionsTest` | The JVM, the iOS simulator, JS, Wasm | `onPaste` hears `performPaste`'s text on every target, on the web by the browser's paste; each system pastes with its own lead key, and the other's key pastes nothing; the text is on the clipboard either way; it is heard on a window's root after `shortcutRoot` too |
| `ui/testing`, `TestingOnAndroidTest` | Robolectric | On Android `performPaste` puts a `ClipData` on the test's clipboard and Ctrl+V reads it |
| `app/shared`, `CodeBlockTest` | The JVM (5 tests) | The sample app's copy button puts the code on the clipboard and says so; it says "could not be copied" for a refusing clipboard and for a page with no clipboard; it does not say a copy the system shows |
| `app/shared`, `CopyMessageTest` | The JVM, the iOS simulator, JS, Wasm (3 tests) | The rule of what is said after a copy |
:::

For 32 on Android no separate runtime was downloaded: Robolectric keeps the version number in a writable field, and the test writes 32 into that field on 33's runtime and puts it back. 30, 33 and 34 run on their own runtimes.

### Behaviours broken by hand

Each behaviour was broken once by hand, and which test turned red was looked at; then it was put back.

*   **Common code:** the write never made; `false` said after writing; `writable` not looked at; `true` said to a clipboard that throws; only `Exception` caught; the cancellation swallowed; `remember` removed; the clipboard or the traits taken out of its keys; the traits not read from `LocalClipboardTraits`; the label and the text swapped. In all eleven a test turned red.
*   **The platforms:** on each platform, the label put in place of the text; on Android, the bound moved to 34, to 32 and to 30; on the desktop and iOS, the traits changed; in the browser, the question always saying "there is" and always "there is none". In all of them a test of that platform turned red.
*   **Seen in one place only:** when the default of `LocalClipboardTraits` is empty traits instead of the platform's, no test turns red on the JVM, because the two are the same there. On Android `on_android_14_a_remembered_clipboard_knows_the_system_shows_a_copy_itself` turns red.
*   **The paste by the key:** on the desktop a text held in memory read off the main thread as well (`a_text_held_in_memory_is_read_in_place_and_another_application_s_away_from_the_thread` turned red; `ui/testing`'s paste tests passed, as the read is that short: it is a race that was closed); `performPaste` always pressing with Control (on the JVM `a_paste_is_pressed_as_each_system_presses_it`); on the web no paste sent to the page (on JS both of the paste tests).

### Not verified

*   **Android 13's preview.** That the system shows what was copied itself, and asks the app not to say it a second time, is from Android's documentation. That it was seen in this repository on an Android 14 emulator is written in `docs/OPEN_ITEMS.md`; it was not looked at again while this guide was written. Robolectric does not draw the preview: the tests look only at what the trait says on which version.
*   **A real http page.** The insecure context was not tried on a page served over plain http. In the test, `navigator.clipboard` was hidden from the page; that Compose skips the write in that case was read in the source, not seen in a test.
*   **The browser's real refusal.** Under which conditions a browser refuses the write was not looked at. The refusal in the test is one the test put there itself; no test wrote to a real clipboard.
*   **A browser without the full clipboard interface.** The way where Compose falls back to `writeText` (by the note in the source, Firefox older than 127) did not run in any browser. The tests ran only in ChromeHeadless (154 and 156).
*   **The real clipboards.** On the desktop, iOS and the browser no test wrote to the system's clipboard. That what was copied can be pasted into another app was not looked at for this guide.
*   **A very large text on Android.** That a text too large for the system to carry is refused was not tried. What is seen in the source is that the service's error is thrown as a `RuntimeException`.
*   **A clipboard not available on the desktop.** `IllegalStateException` is written in the JDK's documentation; it was not seen.
*   **A headless JVM itself.** AWT decides once for a JVM whether it has a screen, and it cannot be changed later. The test tries the rule (`desktopClipboardTraits`) and looks at whether the answer of the JVM it runs on comes from that rule; it did not run on a headless JVM.
*   **The iOS simulator's clipboard.** The simulator can share its clipboard with the machine it runs on; the iOS tests therefore run with the fake clipboard. The sharing itself was not tried.
*   **The quiet systems.** That the system does not show a copy itself on the desktop, iOS, the browser and Android 12L and below is the library's assumption (`announcedBySystem = false`); it was not looked at on real devices.
*   **A screen reader.** How a copy is announced aloud was not looked at.
*   **⌘V on iOS with a hardware keyboard.** That UIKit pastes into the focused field and hands the key to Compose as well was read in the source. ⌘V could not be pressed on the simulator from here: that the node leaves the key while a text input of UIKit's has the focus was not seen.
*   **The browser's read permission.** The question `readText()` asks in Chrome and Safari was not seen; what the tests read is the test's own clipboard.
*   **A real paste.** That the browser sends the `paste` event to Compose's input on a real ⌘V was read in the source; in the test the event was sent to the page by the test itself.

## 9. Where it is in this repository {#repository toc="9. In the repository"}

:::table
| File | What |
| :--- | :--- |
| `ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/platform/PantrixClipboard.kt` | `ClipboardTraits`, `currentClipboardTraits`, `LocalClipboardTraits`, `PantrixClipboard`, `rememberPantrixClipboard` |
| `ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/platform/OnPaste.kt` | `Modifier.onPaste` |
| `ui/foundation/src/androidMain/…/PantrixClipboard.android.kt`, `iosMain/…/PantrixClipboard.ios.kt`, `jvmMain/…/PantrixClipboard.jvm.kt` | The three platforms' traits, the type put on the clipboard and the text read from it; on iOS, the look at UIKit's text input |
| `ui/foundation/src/webMain/…/PantrixClipboard.web.kt` | The browser's traits, the question asked of the browser and the listening for the browser's paste |
| `ui/foundation/src/jsMain/…/PantrixClipboard.js.kt`, `wasmJsMain/…/PantrixClipboard.wasmJs.kt` | The type put on the clipboard on the two web targets and the text read from it |
| `ui/foundation/src/commonTest/…/platform/RecordingClipboard.kt` | The tests' fake clipboard |
| `…/platform/PantrixClipboard*Test.kt` and `…/platform/OnPaste*Test.kt` under `ui/foundation/src/commonTest`, `jvmTest`, `androidHostTest`, `iosTest`, `webTest` | The tests in section 8 |
| `app/shared/src/commonMain/kotlin/com/pantrixui/cmp/demo/page/CodeBlock.kt` | The sample app's copy button and the rule of what it says (`copyMessage`) |
:::

## 10. What is left open {#open}

*   The helper carries nothing but plain text and draws no paste menu (see section 5).
*   That ⌘V on iOS with a hardware keyboard is left to the field while one has the focus was not seen on a device (see section 8).
*   On the desktop and iOS, the Compose API it rests on is experimental (`@ExperimentalComposeUiApi`); it can change in a later version of Compose Multiplatform.
*   It was seen only in tests: it was not looked at on real devices, in real browsers or on a page served over plain http.
