---
title: Keyboard shortcuts
description: Why, in Compose Multiplatform, a keyboard shortcut that belongs to the whole window (such as Cmd+K or Ctrl+K) does not work with a single onPreviewKeyEvent, what the library gives for it, how a shortcut is written on each system, how it is kept from the browser on the web, and how it is tested. The problem is not Pantrix's own: it shows the same way in every Compose Multiplatform project with an app shortcut in its common code.
---

Everything said here of Compose was read in the sources of Compose Multiplatform 1.12.1, or seen in a test that runs in this repository, on a device or in a browser; which was seen where is written in sections 7 and 8.

## 1. The problem {#problem}

### Symptom

*   A shortcut put on the app's root with `Modifier.onPreviewKeyEvent { … }` hears only while something has the focus. While the app is just opened, or once the focus has left a field, Cmd+K does nothing.
*   On Apple's systems a shortcut starts with Command, elsewhere with Control: whether to look at `isMetaPressed` or at `isCtrlPressed` changes with the system. On the desktop and in a browser, which system it is cannot be told from `PlatformType`.
*   A listener that runs the shortcut on `KeyDown` and returns `true` leaves the key's lifting (`KeyUp`) to the control that has the focus. A button that presses Enter on `KeyUp` sees the end of a press it did not start.
*   How the shortcut is written changes with the system too: `⇧⌘K` on a Mac, `Ctrl+Shift+K` on Windows and Linux.
*   In a browser, Cmd+F that the app runs while a text field has the focus opens the browser's find bar as well; Ctrl+H opens the browser's history on Windows, and F3 its next match.

:::sketch keyboard-shortcuts
:::

### Cause

A key event is given to the node that has the focus and to its ancestors. While nothing has the focus, Compose gives the event to the listeners **above** the root focus node: no listener in the content is one of them. `FocusOwnerImpl.dispatchKeyEvent` (Compose Multiplatform 1.12.1, `commonMain/androidx/compose/ui/focus/FocusOwnerImpl.kt`):

```kotlin
val activeFocusTarget = findFocusTargetNode()
val focusedKeyInputNode =
    activeFocusTarget?.lastLocalKeyInputNode()
        ?: activeFocusTarget?.nearestAncestorIncludingSelf(Nodes.KeyInput)?.node
        ?: rootFocusNode.nearestAncestor(Nodes.KeyInput)?.node
```

`rootFocusNode.nearestAncestor(…)` is the platform's own nodes; an `onPreviewKeyEvent` on the app's root is below them, and is skipped. For a shortcut to be heard at all times, the root itself has to have the focus while nothing else does.

A node that does this has to watch two things too:

*   **Tab.** An ancestor that has the focus is a stop in Tab's one-dimensional search. When Tab is pressed while the root has the focus, Compose searches the siblings of what has the focus; the root has no siblings, and the focus stays on the root.
*   **Going round.** When Tab goes from the last control round to the first, Compose first lets go of the focus, then looks for the first control (`FocusOwnerImpl.moveFocus`: `clearFocus(…)`, then `takeFocus(focusDirection, …)`). A root that makes itself focusable the moment the focus is let go becomes the "first" of that search.

In a browser a key comes to the page first. While no text field has the focus, Compose answers the key at once in the canvas's `keydown`, and keeps what it answered from the browser (`preventDefault`). While a text field has the focus, the key comes to the hidden input Compose puts beside the field; Compose handles it in the next frame, and by then the browser has done what it does with the key. There Compose keeps only Tab. `DomInputStrategy` (Compose Multiplatform 1.12.2, `webMain/androidx/compose/ui/platform/DomInputStrategy.kt`):

```kotlin
htmlInput.addEventListener("keydown", { evt ->
    nativeInputEventsProcessor.registerEvent(evt as KeyboardEvent)
    if (evt.keyCode == tabKeyCode) {
        evt.preventDefault()
    }
    …
})
```

A key that writes a character without Cmd or Ctrl (`isTypedEvent`) Compose never gives to the scene: the browser types it, and Compose takes what was typed.

## 2. The solution: four parts {#fix}

:::table
| Part | What it does |
| :--- | :--- |
| `PantrixShortcut(key, command, shift, alt)` | A key and its modifiers. `command` is Command on Apple's systems and Control on the others. `isPressedBy(event, system)`: whether exactly these modifiers are held as the key goes down, and no other. `keysOn(system)`, `writtenOn(system)`: as the system writes it. |
| `Modifier.onShortcut(shortcut) { … }` | While the focus is on this node or inside it, hears the shortcut before the control that has the focus does (`onPreKeyEvent`). It swallows the lifting of the key it ran too. |
| `Modifier.shortcutRoot()` | Holds the focus while nothing inside it does; it is no stop for Tab. |
| `Modifier.keepFromBrowser(shortcuts)` | On the web, keeps the shortcuts from the browser while the focus is on this node or inside it: as the key goes down it tells the browser to leave it, and Compose still hears it. `onShortcut` keeps its own shortcut this way. It does nothing on the other platforms. |
:::

`LocalOperatingSystem` (`foundation/platform`) says which system the shortcut is pressed on: its default is `currentOperatingSystem()`; a test gives another system.

## 3. With Pantrix {#with}

### 3.1. The whole window's shortcut

```kotlin
var palette by rememberSaveable { mutableStateOf(false) }
Box(Modifier.shortcutRoot().onShortcut(PantrixShortcut(Key.K, command = true)) { palette = true }) {
    App()
    PantrixCommandPalette(visible = palette, onDismissRequest = { palette = false }, …)
}
```

`shortcutRoot` stands once, at the window's root. The shortcuts have to be on the same node as it or above it: while the root has the focus, only the root's and its ancestors' listeners hear. Where two roots stand side by side for a moment, as two screens do while one gives way to the other, the one that has the focus keeps it.

### 3.2. The commands' own shortcuts

`Modifier.commandShortcuts(commands, onRun)` binds the shortcut of every command in the list that can be run. What the palette shows and what the keyboard runs is one list:

```kotlin
Box(
    Modifier
        .shortcutRoot()
        .onShortcut(PantrixShortcut(Key.K, command = true)) { palette = true }
        .commandShortcuts(commands, onRun = { run(it.key) })
) { … }
```

A command with `enabled = false` lets its key go by; of two commands that carry the same shortcut, the first runs. While a Pantrix modal is open the keys are its: the shortcuts below it are not heard.

### 3.3. How a shortcut is written

```kotlin
val shortcut = PantrixShortcut(Key.R, command = true, shift = true)
shortcut.writtenOn(OperatingSystem.MacOs)   // "⇧⌘R"
shortcut.writtenOn(OperatingSystem.Windows) // "Ctrl+Shift+R"
shortcut.keysOn(LocalOperatingSystem.current) // for keycaps, one by one
```

`PantrixKeycaps(shortcut)` draws those keys each on a cap, as a menu or a list of shortcuts does, and a screen reader hears the shortcut once, as `writtenOn` writes it.

Apple's order is Option, Shift, Command and the key; the others' is Ctrl, Alt, Shift and the key. Letters, digits, the arrows, F1–F12, Enter, Escape, Tab, space, Backspace, Delete, Home and End, the page keys and the common signs have names; any other key is written `#its number`.

### 3.4. Keeping it from the browser

`onShortcut` keeps its own shortcut by itself. The keys a node answers itself, for example the ones answered with `onPreviewKeyEvent`, are given to `keepFromBrowser`:

```kotlin
val find = PantrixShortcut(Key.F, command = true)
val system = LocalOperatingSystem.current
PantrixTextArea(
    state = text,
    modifier = Modifier
        .keepFromBrowser(listOf(find))
        .onPreviewKeyEvent { event -> find.isPressedBy(event, system).also { if (it) finding = true } },
    label = "Text"
)
```

Only the keys the app answers are given: a key kept from the browser does nothing in the browser, even when the app does nothing with it. `PantrixCodeEditor` keeps its own keys this way: find, the next and the previous match, replace, deleting lines, comments and the matching bracket.

## 4. Without Pantrix {#without}

The root's node, `ShortcutRootNode` (`ui/foundation/.../keyboard/Shortcuts.kt`), is built without the library so:

```kotlin
fun Modifier.shortcutRoot(): Modifier = this then ShortcutRootElement

private object ShortcutRootElement : ModifierNodeElement<ShortcutRootNode>() {
    override fun create() = ShortcutRootNode()
    override fun update(node: ShortcutRootNode) = Unit
    override fun hashCode() = "shortcutRoot".hashCode()
    override fun equals(other: Any?) = other === this
}

private class ShortcutRootNode : DelegatingNode(), KeyInputModifierNode, CompositionLocalConsumerModifierNode {
    private val target = delegate(
        FocusTargetModifierNode(focusability = Focusability.Always, onFocusChange = { _, state -> changed(state) })
    )

    override fun onAttach() = takeIfNothingHasIt()

    override fun onPreKeyEvent(event: KeyEvent) = false

    // While the root has the focus, it takes Tab itself and carries it to what is inside.
    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.type != KeyEventType.KeyDown || event.key != Key.Tab || !target.focusState.isFocused) return false
        val focus = currentValueOf(LocalFocusManager)
        focus.moveFocus(FocusDirection.Enter)
        if (event.isShiftPressed) focus.moveFocus(FocusDirection.Previous)
        return true
    }

    private fun changed(state: FocusState) {
        // While something inside has the focus the root is no stop for Tab; and since going round lets go of the
        // focus and looks for the first, the root takes back its focusability only once everything has settled.
        if (state.hasFocus && !state.isFocused) target.focusability = Focusability.Never
        if (!state.hasFocus) takeIfNothingHasIt()
    }

    private fun takeIfNothingHasIt() {
        coroutineScope.launch {
            if (!target.focusState.hasFocus) {
                target.focusability = Focusability.Always
                target.requestFocus()
            }
        }
    }
}
```

The matching rule is one line; for the system, `navigator.platform` (a browser) or `os.name` (the JVM) is read:

```kotlin
val lead = if (apple) event.isMetaPressed else event.isCtrlPressed
val stray = if (apple) event.isCtrlPressed else event.isMetaPressed
val pressed = event.type == KeyEventType.KeyDown && event.key == key && lead == command && !stray &&
    event.isShiftPressed == shift && event.isAltPressed == alt
```

On the web, what keeps a key from the browser is a listener put on the window when the focus comes to the node and taken off when it goes (`ui/foundation/src/webMain/.../keyboard/KeepFromBrowser.web.kt`). It runs in the capturing phase, as the key comes down to the field's input, and takes nothing from the page:

```kotlin
val listener: (Event) -> Unit = { event ->
    val key = event as? KeyboardEvent
    val types = key != null && !key.metaKey && !key.ctrlKey && key.key.length == 1
    if (key != null && !key.isComposing && !types && pressesAShortcut(key)) event.preventDefault()
}
window.addEventListener("keydown", listener, true)
// when the focus leaves the node: window.removeEventListener("keydown", listener, true)
```

The key is matched by where it is on the keyboard, as Compose reads it (`KeyboardEvent.code`: `KeyF`, `Slash`, `F3`).

## 5. What the helper does not do {#limits toc="5. What it does not do"}

*   **While the app has no focus.** A shortcut is heard only while the app's window has the focus; there is no system-wide shortcut (while the app is behind).
*   **Shortcuts in sequence.** Shortcuts of two presses, such as "G, then I", do not exist.
*   **Holding down.** When a key is held down and repeats, the shortcut runs again at every repeat; there is no common mark that tells a repeat apart.
*   **Clashes.** That two shortcuts take the same key is not reported: the outer one runs, and in `commandShortcuts` the first in the list.
*   **The browser's own shortcuts.** Shortcuts the browser or the system keeps, such as Ctrl+W, Ctrl+T and Cmd+Q, never come to the page; `keepFromBrowser` cannot keep them either.
*   **The iPad's list of shortcuts.** The list that comes up on an iPad while ⌘ is held (`UIKeyCommand`) is not filled.

## 6. Testing {#testing}

```kotlin
setContent {
    CompositionLocalProvider(LocalOperatingSystem provides OperatingSystem.MacOs) {
        Box(Modifier.testTag("field").onShortcut(PantrixShortcut(Key.K, command = true)) { runs++ }.focusable())
    }
}
onNodeWithTag("field").requestFocus()
onNodeWithTag("field").performKeyInput { withKeyDown(Key.MetaLeft) { pressKey(Key.K) } }
```

*   Give the system in a test with `LocalOperatingSystem`: a test left to the machine it runs on expects another key on a Mac and on Linux.
*   Whether the root has the focus is read with a `Modifier.onFocusChanged { … }` put **before** `shortcutRoot`: the root has no semantics of its own, and `assertIsFocused` cannot see it.
*   Try letting go of the focus with `LocalFocusManager.current.clearFocus()`, inside `runOnIdle { … }`.

## 7. Pitfalls {#pitfalls}

*   **The first click in a browser.** When the page opens, the DOM's focus is not on Compose's element; until the first click the keys never come to Compose, even while the root has the focus. This is the browser's limit; `shortcutRoot` solves what comes after it (seen in a browser in this repository: the root had the focus, and the key event came to the shortcut's node after a click).
*   **Made-up events whose modifier is not pressed on its own.** If an automation tool sends Cmd+K as a K that only carries `metaKey: true`, Compose for the web does not count it as pressed with Meta: it reads the modifiers from the key downs it has seen itself. When Meta's down is sent first and then K's, the shortcut runs. On a real keyboard there is no problem.
*   **A key that types in a field is not kept from the browser.** If a shortcut that writes a character without Cmd or Ctrl (`/`, or a letter with Option) were kept from the browser in a field, the field could not type it; Compose does not give it to a shortcut either. `keepFromBrowser` leaves such a key alone.
*   **AltGr.** On Windows, AltGr comes as Ctrl and Alt pressed together, and in many languages it types a letter or a sign (AltGr+Z is "ż" in Polish, AltGr+F is "[" in Hungarian). Since `PantrixShortcut` matches the modifiers exactly, a shortcut with Ctrl is not pressed by AltGr; a shortcut with Ctrl and Alt takes that letter's place on Windows, and should not be given.
*   **The order of `focusProperties`.** `Modifier.focusProperties { canFocus = false }` affects only the focus target that comes **after** it. `.clickable(…).focusProperties { … }` does not turn off the clickable's focus target; `focusProperties` has to be written first and `clickable` after it (found in the command palette's rows: the focus went to the first row, not to the field).
*   **Asking for the focus inside a modal.** A focus asked for at once inside a `LaunchedEffect` in a modal's content fails silently: a field takes no focus before the window has laid out its content. A frame is waited for (`withFrameNanos {}`); Pantrix's modal does so too.

## 8. Verification {#verification}

### Tests

:::table
| Test | What it holds | Targets |
| :--- | :--- | :--- |
| `foundation/.../keyboard/ShortcutsTest` | How it is written (Apple's and the others', every named key), equality, being pressed with exactly its modifiers, the down and the lifting not given to what has the focus, the outer one winning, the root taking the focus at the start and when the focus is let go, Tab skipping the root | JVM, iOS simulator, JS, Wasm |
| `components/.../CommandShortcutsTest` | Every command's shortcut, the one that cannot be run letting its key go by, the first winning | JVM, iOS, JS, Wasm |
| `components/.../CommandPaletteTest` | The shortcut in the palette's rows read as a state, as the system writes it | JVM, iOS, JS, Wasm |
| `foundation/.../keyboard/KeepFromBrowserOnTheWebTest` | A shortcut kept only with its own modifiers; other keys, a key that types in a field and AltGr left alone; the node keeping while the focus is inside and letting go when it leaves; `onShortcut` keeping its own key while it is enabled | JS, Wasm |
| `components/.../CodeEditorOnTheWebTest` | Ctrl+F and Ctrl+H kept while the editor has the focus; AltGr+F, and Ctrl+H in a read-only editor, left alone | JS, Wasm |
:::

Android's host tests do not see `commonTest` (see ROBORAZZI_GUIDE.md).

### Looked at by hand

*   **Android emulator:** as soon as the app opened, touching nothing, `adb shell input keycombination KEYCODE_CTRL_LEFT KEYCODE_K` opened the demo's palette.
*   **A browser (Chrome, macOS):** after a click, Cmd+K (with Meta's down first) opened the palette.
*   **The built-in browser (Chromium, macOS):** in the demo's code editor, Cmd+F's key event went to the browser kept (`defaultPrevented`) and the editor's find bar opened; Cmd+K's key event was not kept.

### Not verified

*   Cmd+K with a hardware keyboard on iOS: in the simulator Cmd+K is the simulator's own shortcut that shows and hides the software keyboard, and does not come to the app.
*   The desktop app by hand.
*   A screen reader reading the shortcuts out.
*   That the browser's find bar, history and console do not open in Chrome, Safari and Firefox with a real keyboard: the built-in browser has no find bar of its own; it was read from the kept key event's `defaultPrevented`.

## 9. Where it is in this repository {#repository toc="9. In the repository"}

:::table
| File | What is in it |
| :--- | :--- |
| `ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/keyboard/PantrixShortcut.kt` | `PantrixShortcut`, the keys' names |
| `ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/keyboard/Shortcuts.kt` | `onShortcut`, `shortcutRoot` |
| `ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/keyboard/KeepFromBrowser.kt` | `keepFromBrowser` |
| `ui/foundation/src/webMain/kotlin/com/pantrixui/foundation/keyboard/KeepFromBrowser.web.kt` | The listener that keeps a key from the browser on the web, the names of where the keys are on the keyboard |
| `ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/platform/OperatingSystem.kt` | `OperatingSystem`, `LocalOperatingSystem` |
| `ui/components/src/commonMain/kotlin/com/pantrixui/components/overlay/commandpalette/CommandShortcuts.kt` | `commandShortcuts` |
| `app/shared/src/commonMain/kotlin/com/pantrixui/cmp/demo/shell/DemoCommands.kt` | The demo's root and its own palette |
:::

## 10. What is left open {#open}

In `docs/OPEN_ITEMS.md`: shortcuts in sequence, the repeats of holding down, the iPad's list of shortcuts, and verifying with a hardware keyboard on iOS.
