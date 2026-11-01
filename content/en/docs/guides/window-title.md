---
title: The window’s title
description: Why, in Compose Multiplatform, the window of an app of one window and many screens keeps one name whatever is shown, what the library gives for it, and how it is tested. The problem is not Pantrix's own: it shows the same way in every Compose Multiplatform app that runs on the web and on the desktop.
---

What is said here of Compose was read in the sources of Compose Multiplatform 1.12.2 (`Window.desktop.kt`, `ComponentUpdater.kt`, `LocalAwtWindow.kt`, `ComposeWindowPanel.desktop.kt`). What could not be verified is counted in section 7.

## 1. The problem {#problem}

### Symptom

*   On the web every page of the app has the same name on the browser's tab: the `<title>` of `index.html`. A user with ten tabs open cannot see which one is "Orders"; every entry of the browser's history has the same name too.
*   On the desktop the window's title bar, the taskbar and the window switcher show the name given to `Window(title = …)`, whatever screen the app is on.
*   The screen's name is known already, and written in its bar; but there is no common API that carries it to the window.

:::sketch window-title
:::

### Cause

A window's title is not in common code. On each platform it is a field of another object:

:::table
| Platform | The window's title | From common code |
| :--- | :--- | :--- |
| Web (`js`, `wasmJs`) | `document.title`: the name of the tab and of the history | None; `kotlinx.browser.document` is in the web source set alone |
| Desktop (JVM) | `java.awt.Frame.title`: the title bar, the taskbar | None; the window is reached through `LocalAwtWindow`, which is `@ExperimentalComposeUiApi` |
| Android | No window's title a user sees: the recent apps say the app's name | No need |
| iOS | No window's title a user sees: the app switcher says the app's name | No need |
:::

Two details make it harder:

1.  **More than one screen is there at once.** While one page slides over another both are in the composition; in a wide window a list and its detail stand side by side. "Write the title as a screen opens and put the old one back as it closes" is not enough: the second screen opens, the first one puts the very first title back as it closes, and the open screen's name is lost.
2.  **On the desktop `Window` writes a title too.** `Window(title = …)` writes its title again only when its own value changes (`ComponentUpdater` compares it with the one before); as long as it does not, the title a screen gave stays, and when it does, it is written over it.

## 2. The solution {#fix}

A stack of titles is kept for each window. A screen that comes into the composition puts its title at the end of the stack, and one that leaves takes its own title out of it. The window is given the name at the end of the stack; once the stack is empty, the window's own title, read before the first screen came, is put back. Where a screen's title changes, the window changes with it while that screen is at the end of the stack. The app's name is added by a format: a function from the screen's name to the window's, given once at the app's root.

## 3. With Pantrix {#with}

```kotlin
import com.pantrixui.foundation.platform.LocalPantrixWindowTitleFormat
import com.pantrixui.foundation.platform.PantrixWindowTitle
import com.pantrixui.foundation.platform.PantrixWindowTitleFormat

// At the app's root, once: the app's name after each screen's.
CompositionLocalProvider(LocalPantrixWindowTitleFormat provides PantrixWindowTitleFormat { "$it · Admin" }) {
    App()
}

// In a screen: while it is shown, the window is called "Orders · Admin".
@Composable
fun OrdersScreen() {
    PantrixWindowTitle("Orders")
    OrdersList()
}
```

*   `PantrixWindowTitle(title)`: gives the window the screen's name for as long as the screen is in the composition. It writes `document.title` on the web and the window's title on the desktop; on Android and iOS it does nothing.
*   `LocalPantrixWindowTitleFormat`: how the screen's name is written as the window's. Unless given, `PantrixWindowTitleFormat.Plain`: the name as it is.
*   `PantrixPageScaffold` does it itself: each page gives the window its own `title`. An app that builds its screens with it does not call `PantrixWindowTitle` besides.

### 3.1. pages inside pages

A page inside another page's content, as a preview of a page or an example of one is, gives the window no name: it leaves the title to the page it stands in. Two pages side by side, a list and its detail, are not one inside the other; both give a name, and the one that came last wins.

## 4. Without Pantrix {#without}

### 4.1. common code (`commonMain`)

```kotlin
/** A window's titles: the last one given first. */
class WindowTitles(private val read: () -> String, private val write: (String) -> Unit) {
    private val given = mutableListOf<Entry>()
    private var own: String? = null

    inner class Entry(title: String) {
        var title: String = title
            set(value) {
                if (field == value) return
                field = value
                if (given.lastOrNull() === this) write(value)
            }

        fun add() {
            if (given.isEmpty()) own = read()
            given.remove(this)
            given += this
            write(title)
        }

        fun remove() {
            val wasLast = given.lastOrNull() === this
            if (!given.remove(this) || !wasLast) return
            val before = given.lastOrNull()
            if (before != null) write(before.title) else own?.let(write).also { own = null }
        }
    }
}

@Composable
expect fun windowTitles(): WindowTitles?

@Composable
fun WindowTitle(title: String) {
    val titles = windowTitles() ?: return
    val entry = remember(titles) { titles.Entry(title) }
    DisposableEffect(entry) {
        entry.add()
        onDispose { entry.remove() }
    }
    SideEffect { entry.title = title }
}
```

### 4.2. the platforms' counterparts

```kotlin
// webMain: the page has one title.
private val documentTitles = WindowTitles(read = { document.title }, write = { document.title = it })

@Composable
actual fun windowTitles(): WindowTitles? = documentTitles
```

```kotlin
// jvmMain: each window's own titles, which go with the window once it is let go.
private val frameTitles = WeakHashMap<Frame, WindowTitles>()

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun windowTitles(): WindowTitles? {
    val frame = LocalAwtWindow.current as? Frame ?: return null
    return remember(frame) {
        synchronized(frameTitles) {
            frameTitles.getOrPut(frame) { WindowTitles(read = { frame.title.orEmpty() }, write = { frame.title = it }) }
        }
    }
}
```

```kotlin
// androidMain and iosMain: no window's title a user sees.
@Composable
actual fun windowTitles(): WindowTitles? = null
```

## 5. Testing {#testing}

The stack's rules have no platform and are tested on every target: the last name given wins, a name that leaves makes way for the one before it, the window's own title comes back once the last name is gone, and a name that is not at the end changes nothing of the window.

```kotlin
val titles = WindowTitles(read = { window }, write = { window = it })
val orders = titles.Entry("Orders")
val order = titles.Entry("Order 42")
orders.add()
order.add()
assertEquals("Order 42", window)
order.remove()
assertEquals("Orders", window)
```

On the web `document.title` is read in a real browser: a screen writes it in its format, changes it as its title changes, and puts the page's own `<title>` back as it leaves the composition. The desktop's tests open no window (`LocalAwtWindow` is `null` in a test): there `PantrixWindowTitle` does nothing; the page's JVM tests call it so.

## 6. Pitfalls {#pitfalls}

*   **The first frame.** On the web the tab shows the `<title>` of `index.html` until the app has drawn its first frame; the screen's name comes after the first composition. The page's `<title>` is to stay the app's name.
*   **If `Window(title = …)` changes.** On the desktop Compose writes the title given to `Window` when it changes, and the screen's name is gone; it stays so until a screen comes into the composition again. The app's own name is to be given as a title that does not change.
*   **Dialogs.** A screen inside a `DialogWindow` gives the main window no name: `LocalAwtWindow` is the dialog itself there, which is no `Frame`.

## 7. Verification {#verification}

### Tests

:::table
| What | Where | Target |
| :--- | :--- | :--- |
| The stack's rules | `ui/foundation/src/commonTest/…/platform/WindowTitlesTest.kt` | JVM, iOS, JS, Wasm |
| The browser's tab | `ui/foundation/src/webTest/…/platform/WindowTitleOnTheWebTest.kt` | JS, Wasm |
| A page naming the tab, and a page inside it not | `ui/components/src/webTest/…/PageScaffoldOnTheWebTest.kt` | JS, Wasm |
:::

### Behaviours broken by hand

With the web's counterpart giving `null`, both browser tests turned red; with a page naming the window whether or not it stands inside another, the page's browser test turned red.

### Not verified

*   The name of a scene in iPadOS's multitasking views: this library writes nothing on iOS.
*   The names of the entries of the browser's history.

## 8. Where it is in this repository {#repository toc="8. In the repository"}

*   The API: `ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/platform/PantrixWindowTitle.kt`, with the platforms' counterparts in the same package's `webMain`, `jvmMain`, `androidMain` and `iosMain` files.
*   What uses it: `ui/components/src/commonMain/kotlin/com/pantrixui/components/container/page/PageFrame.kt`.
*   The demo: the app's theme gives the format (`app/shared/…/theme/Theme.kt`); the "The window's title" example of the "Page Scaffold" page (`app/shared/…/samples/PageScaffoldSamples.kt`).
