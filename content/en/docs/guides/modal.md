---
title: A modal layer of the app’s own
description: Why, in Compose Multiplatform, an app's building a modal layer of its own (a scrim over the whole window, and over it content the app draws) is another job on each platform, what the library gives for it, and how it is tested. The problem is not Pantrix's own: it shows the same way in every Compose Multiplatform project that wants a layer a dialog or a sheet does not draw (a search with filters of its own, a picture shown large, an editor that fills a phone).
---

Everything said here of Compose was read in the sources of Compose Multiplatform 1.12.2, or seen in a test that runs in this repository; which was seen where is written in sections 7 and 8.

## 1. The problem {#problem}

:::aside modal
### Symptom

*   Compose's `Dialog` is something else on each platform. On Android it is a window of its own: it dims what is behind it itself, opens with its own animation, fits a default width and chooses the icons of the system bars itself. On iOS, the desktop and the web it is a layer of the scene: it draws its own scrim and plays its own transition.
*   A `Dialog` that is closed goes at once: the content has no exit motion.
*   A `Popup(focusable = true)` takes Escape and, on Android, back, but does not give the rest of a modal: the scrim, closing on a press on it, a "Close" for a screen reader, the focus kept in the content and given back once it closes, keeping clear of a fold.
*   Every app that builds a layer of its own writes these again, and tests them on each platform apart.
:::

### Why

There are things a `Dialog` decides itself on each platform (Compose Multiplatform 1.12.2, the `ui` module, `DialogProperties`):

:::table
| Platform | What `Dialog` decides itself | To turn it off |
| :--- | :--- | :--- |
| Android | The window's dimming (`FLAG_DIM_BEHIND`), the opening animation, a default width, keeping inside the system bars, the bars' icons | `usePlatformDefaultWidth = false`, `decorFitsSystemWindows = false`; on the window `clearFlags(FLAG_DIM_BEHIND)`, `setWindowAnimations(0)` and the bars' appearance (`WindowCompat.getInsetsController`) |
| iOS, desktop, web | A default width, the window's insets and the software keyboard's inset, the scrim's colour, the opening and closing transition | `usePlatformDefaultWidth = false`, `usePlatformInsets = false`, `useSoftwareKeyboardInset = false`, `scrimColor = Color.Transparent`, `animateTransition = false` (the experimental constructor) |
:::

With these turned off, the layer is an empty surface: the app builds the scrim, the content's place, the motion and the focus itself, the same way on every platform.

## 2. The solution {#fix}

In the components (`com.pantrixui.components.overlay.modal`):

1.  **`PantrixModal(visible, onDismissRequest, …) { content }`**: a scrim over the whole window, and over it the app's content. It is the layer `PantrixDialog`, `PantrixSheet` and `PantrixCommandPalette` stand on.
2.  **`ModalPosition`**: where the content sits, and how it enters and leaves. `Center` is the window's middle, `Top` near its top: a search whose field stays where it is.

The modal gives the content no fill, no shape and no width: the content draws itself. The rest is the modal's:

*   Back, Escape and a press on the scrim call `onDismissRequest`; the modal does not close itself, the caller turns `visible` off. The content enters and leaves with the theme's motion; once it has left, nothing is composed.
*   The focus follows the WAI-ARIA dialog pattern: opened from the keyboard, it moves to the content's first control and Tab goes round inside; opened by touch, it stays where it was. Once it closes, it is back where it was. The app under it draws no focus ring.
*   A screen reader hears `paneTitle` as it opens, then the content, and last the scrim, as one "Close" action where a press on it closes.
*   Where a fold divides the window, the content keeps to the half it was opened from; a modal opened inside a `PantrixWindowFrame` takes that frame for its window.

## 3. With Pantrix {#with}

### 3.1. A search at the top

```kotlin
var searching by rememberSaveable { mutableStateOf(false) }
PantrixModal(
    visible = searching,
    onDismissRequest = { searching = false },
    position = ModalPosition.Top,
    paneTitle = stringResource(Res.string.search),
    tagKey = "search"
) {
    PantrixSurface(Modifier.widthIn(max = 640.dp), shape = PantrixTheme.shapes.lg, elevation = 8.dp) {
        SearchPanel(onOpen = { searching = false })
    }
}
```

*   `margin` is the room left between the content and the window's safe area (the theme's `xl` step, 24dp); it is part of the scrim: a press right beside the content closes it too.
*   A close is reported once: the `Modal` event, `dismiss`, with `via` (`scrim` or `back`).

### 3.2. A page on a phone

```kotlin
PantrixModal(
    visible = editing,
    onDismissRequest = { editing = false },
    margin = PaddingValues(),
    paneTitle = stringResource(Res.string.edit_note),
    scrimColor = PantrixTheme.colors.background,
    tagKey = "editor"
) {
    NoteEditor(Modifier.fillMaxSize(), onDone = { editing = false })
}
```

A `margin` of none, content that fills itself and a scrim that is the theme's background make the modal a page: the content fills the safe area, and behind the system bars is the background's colour too.

### 3.3. One that asks for a decision

`dismissOnScrimClick = false` and `dismissOnBack = false` make the modal close only by a button in it; the scrim is then a fill a screen reader passes over.

## 4. Without Pantrix {#without}

Below is the shortest form that can be copied into a project without the library; it gives the scrim, Escape and the motion, not the focus trap and the fold. Pantrix's code (`ui/components/src/*Main/kotlin/com/pantrixui/components/overlay/modal/`) does the same with its tests and edge cases.

```kotlin
@Composable
fun AppModal(visible: Boolean, onDismiss: () -> Unit, content: @Composable () -> Unit) {
    val shown = remember { MutableTransitionState(false) }
    shown.targetState = visible
    if (!visible && shown.isIdle && !shown.currentState) return
    Dialog(
        onDismissRequest = onDismiss,
        // On Android turn the window's dimming and animation off too (the table in section 1).
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)
    ) {
        AnimatedVisibility(shown, enter = fadeIn(), exit = fadeOut()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .pointerInput(onDismiss) { detectTapGestures { onDismiss() } },
                contentAlignment = Alignment.Center
            ) {
                Box(Modifier.pointerInput(Unit) {}) { content() }
            }
        }
    }
}
```

*   For an exit motion the layer has to stay composed until the content has left: it does not return as soon as `visible` is false, but once the transition has ended.
*   The `pointerInput(Unit) {}` on the content keeps a press on the content from reaching the scrim.

## 5. What the helper does not do {#limits toc="5. What it does not do"}

*   **The content's surface.** The modal gives no fill, shape, shadow or width; a card or a page is the content's job.
*   **Closing by a drag.** Pulling down to close is `PantrixSheet`'s; the modal itself is not dragged.
*   **Sliding in from an edge.** `ModalPosition` gives only the middle and the top; a layer that comes in from an edge is a sheet.

## 6. Testing {#testing}

*   While the modal is open, the content is inside a dialog's node (`isDialog()`); the content is found by its tag, `Modal_<tagKey>`.
*   A press on the scrim is a press on the window's corner: `onNode(isDialog()).performTouchInput { click(Offset(1f, 1f)) }`.
*   Escape is sent from a focused control: `onNodeWithTag(…).requestFocus()` and `performKeyInput { pressKey(Key.Escape) }`.
*   How a close came is read in the analytics event: `RecordingAnalytics().eventsOf("Modal")`, `props["via"]`.

## 7. Pitfalls {#pitfalls}

*   **A Popup is not a modal.** `Popup(focusable = true)` takes Escape and, on Android, back, but it is not Pantrix's modal layer (`PantrixModalLayer`): the Pantrix controls under it go on drawing their focus rings.
*   **The first focus of a modal opened from the keyboard.** The first control is known after the layout: the focus is asked for once a frame has passed. Focus asked for while the component is composed can go to the wrong control, since the content is not laid out yet.
*   **The window's first frame.** A platform window, or a layer on iOS, spends its first frames opening: an enter motion that starts with the composition is over before the modal is on the screen. Pantrix starts the enter a frame after the window's first draw.
*   **The bars' icons on Android.** The modal's window lies over the system bars and chooses their icons itself; told nothing, it shows light icons: over a light app the clock stays white on the grey scrim.

## 8. Verification {#verification}

### Tests

:::table
| What | Where |
| :--- | :--- |
| Nothing composed while closed, the window's title, sitting in the middle, the margin, the scrim's colour and a press on it, the "Close" for a screen reader, Escape, the first focus, Tab going round inside, the focus coming back, the ring of the app under it, the exit motion, reduced motion | `ModalTest` (JVM, iOS, JS, Wasm) |
| The public modal's tag, its events, sitting at the top and in the middle, the default margin and a page with none, the scrim's colour in `TOLD_APART` | `PantrixModalTest` (JVM, iOS, JS, Wasm) |
| Android's window: no dimming and animation of its own, waiting for the first frame, as large as the screen, back, the scrim, the bars' icons | `DialogOnAndroidTest` (Robolectric; the same shell) |
| The look: content at the top and in the middle, the light and the dark theme | `ComponentScreenshotTest.modal` (JVM, Android, iOS) |
:::

### Looked at by hand

*   **On the Android emulator (API 33) and the iOS simulator (iPhone 18 Pro Max, iOS 27.0), 2026-10-28:** the demo's "A search at the top" example opened at the window's top, over the scrim; the field stayed where it was, and the results were listed under it; on Android the back key closed it.
*   **On the web (Wasm, the app's browser, 1440×900):** the same example opened at the top, the scrim faded the page, and Escape closed it. While the pane was hidden the browser throttled its frames, and the enter came a few seconds late; a second press, on the scrim, closed it, as it should.
*   **Maestro, on both platforms:** `flows/overlays/modal.yaml`: the search opens and a result closes it; the modal that asks for a decision stays until a button in it is chosen. The examples' flows (`dialog`, `sheet`, `command-palette`) still passed once the inner shell was named `ModalHost`.
*   **Diagnostics:** at the first look the demo's search field had no name (`MissingName`, `TextField_search_query`); the example was given a `contentDescription`.

### Not verified

*   A screen reader's voice, opening and closing by hand on an iPad and in the desktop app.

## 9. Where in this repository {#repository toc="9. In the repository"}

*   Components: `ui/components/src/commonMain/kotlin/com/pantrixui/components/overlay/modal/` (`PantrixModal.kt`, the common shell `Modal.kt`, the places `ModalPlacement.kt`, the frame `ModalRoom.kt`, the window `ModalWindow.kt`) and each platform's `ModalWindow` in its source set.
*   The demo: `app/shared/src/commonMain/kotlin/com/pantrixui/cmp/demo/samples/ModalSamples.kt`.

## 10. What is left open {#open}

None.
