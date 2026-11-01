---
title: Resize pointers
description: Why, in Compose Multiplatform, the pointer does not change by itself over a control that makes something larger or smaller when it is dragged, what the library gives for it, and how it is tested. The problem is not Pantrix's own: it shows the same way in every Compose Multiplatform project that has a handle between two panels, a table column whose width is dragged, or a panel made larger from its edge.
---

What is said here of Compose was read in the sources of Compose Multiplatform 1.12.2 (`PointerIcon.kt`, `PointerIcon.desktop.kt`, `PointerIcon.web.kt`, `PointerIcon.ios.kt`, `RootNodeOwner.skiko.kt`), and on Android in `androidx.compose.ui`'s `PointerIcon.android.kt`. What could not be verified is counted separately in section 7.

## 1. The problem {#problem}

### Symptom

*   The handle between two panels is dragged with a mouse and the panels' widths change; but over the handle the pointer is always the arrow. The user finds the handle only by chance.
*   Writing `PointerIcon.` in common code offers four choices: `Default`, `Crosshair`, `Text`, `Hand`. There is no double-headed arrow.
*   Each platform has a resize pointer of its own, but each asks for it in that platform's own type: none can be built from common code.

:::sketch pointer-icons
:::

### Cause

In common code `PointerIcon` is only an interface; its four ready values are the platforms' `actual`s. The way to build any other pointer is different on each platform:

:::table
| Platform | Compose's ready four | How another pointer is built | The resize pointer |
| :--- | :--- | :--- | :--- |
| Android | `AndroidPointerIconType(TYPE_ARROW)`, `TYPE_CROSSHAIR`, `TYPE_TEXT`, `TYPE_HAND` | `PointerIcon(pointerIconType: Int)` or `PointerIcon(android.view.PointerIcon)` | `TYPE_HORIZONTAL_DOUBLE_ARROW`, `TYPE_VERTICAL_DOUBLE_ARROW` |
| Desktop (JVM) | `AwtCursor(Cursor(Cursor.DEFAULT_CURSOR))` and the others | `PointerIcon(cursor: java.awt.Cursor)` | `Cursor.E_RESIZE_CURSOR`, `Cursor.N_RESIZE_CURSOR` |
| Web (`js`, `wasmJs`) | `BrowserCursor("default")`, `"crosshair"`, `"text"`, `"pointer"` | `PointerIcon.fromKeyword(keyword: String)`, `@ExperimentalComposeUiApi` | CSS's `col-resize` and `row-resize` |
| iOS | `UIKitCursor("default")` and the others | None: the class is `private` | None |
:::

Two details make the job larger:

1.  **On iOS Compose shapes no pointer at all.** The common stage hands the pointer to `platformContext.setPointerIcon(…)` (`RootNodeOwner.skiko.kt`); that call's default is empty (`PlatformContext.skiko.kt`), and iOS does not fill it in. Compose's own four do not change an iPad's pointer either. iOS's counterpart is written only as a name.
2.  **The web's builder is experimental.** `fromKeyword` carries `@ExperimentalComposeUiApi`; the place that uses it asks for `@OptIn`. The word given is written as it is into the canvas's `style.cursor` (`ComposeWindowInternal.web.kt`): a word written wrong gives no error, and the pointer stays the arrow.

## 2. The solution {#fix}

The two resize pointers are declared in common code as an `expect` value, and each platform answers it in its own type. The place that uses them sees only the common name and gives it to Compose's modifier `pointerHoverIcon`. The modifier shows the pointer when the pointer enters the node, and lets it go when it leaves.

## 3. With Pantrix {#with}

```kotlin
import com.pantrixui.foundation.platform.PantrixPointerIcons

Box(
    Modifier
        .width(12.dp)
        .fillMaxHeight()
        .pointerHoverIcon(PantrixPointerIcons.ResizeHorizontal)
        .draggable(rememberDraggableState { by -> width += with(density) { by.toDp() } }, Orientation.Horizontal)
)
```

*   `PantrixPointerIcons.ResizeHorizontal`: over a control that is dragged sideways to make something wider or narrower. The handle between two panels, the end of a table's column.
*   `PantrixPointerIcons.ResizeVertical`: over a control that is dragged up and down to make something taller or shorter. A panel's bottom edge.

Where the library uses them:

:::table
| Component | Where |
| :--- | :--- |
| `PantrixListDetailScaffold(resizable = true)` | The handle between the list and the detail |
| `PantrixDataTable` | The end of the header of a column whose width changes; only on a platform used with a mouse, since a finger does not drag that strip |
| The charts' navigator (`ChartZoom(navigator = true)`) | The two ends of the part in view, and while an end is dragged. `ResizeVertical` in a horizontal bar chart's upright navigator |
:::

### 3.1. A pointer that changes with the position

The navigator is a single node: the pointer is not the same everywhere in it. Over an end of the part in view it is the resize pointer, in its middle and outside it the arrow. For that, the pointer's position is listened to and the pointer is read from a state:

```kotlin
var hovered by remember { mutableStateOf<Grip?>(null) }
Box(
    Modifier
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    val x = event.changes.first().position.x
                    hovered = if (event.type == PointerEventType.Exit) null else gripUnder(x)
                }
            }
        }
        .pointerHoverIcon(if (held ?: hovered != null) PantrixPointerIcons.ResizeHorizontal else PointerIcon.Default)
)
```

When the pointer changes while the pointer is over the node, Compose shows the new one at once (the setter of `HoverIconModifierNode.icon`). The end being dragged (`held`) is kept until the drag ends: even when the end stops at a limit and the pointer moves away from it, the pointer stays the resize pointer.

### 3.2. Keyboard and screen reader

The pointer tells only whoever holds the mouse. A real handle is open to a keyboard and a screen reader too: it takes the focus, goes step by step with the arrow keys and to its ends with Home and End, and is told to a screen reader as a value that can be set (`progressBarRangeInfo` and `setProgress`). `PantrixListDetailScaffold`'s handle and `PantrixDataTable`'s column end are so; the example on the demo's "Platform and system" page, which shows the pointer alone, does not write them.

## 4. Without Pantrix {#without}

### 4.1. Common code (`commonMain`)

```kotlin
package com.example.pointer

import androidx.compose.ui.input.pointer.PointerIcon

object ResizePointers {
    val Horizontal: PointerIcon get() = resizeHorizontal
    val Vertical: PointerIcon get() = resizeVertical
}

internal expect val resizeHorizontal: PointerIcon
internal expect val resizeVertical: PointerIcon
```

### 4.2. The platforms' counterparts

`androidMain`:

```kotlin
internal actual val resizeHorizontal: PointerIcon = PointerIcon(android.view.PointerIcon.TYPE_HORIZONTAL_DOUBLE_ARROW)
internal actual val resizeVertical: PointerIcon = PointerIcon(android.view.PointerIcon.TYPE_VERTICAL_DOUBLE_ARROW)
```

`jvmMain`:

```kotlin
import java.awt.Cursor

internal actual val resizeHorizontal: PointerIcon = PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR))
internal actual val resizeVertical: PointerIcon = PointerIcon(Cursor(Cursor.N_RESIZE_CURSOR))
```

`webMain` (the source set `js` and `wasmJs` share; `fromKeyword` is the same declaration in both):

```kotlin
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.pointer.fromKeyword

@OptIn(ExperimentalComposeUiApi::class)
internal actual val resizeHorizontal: PointerIcon = PointerIcon.fromKeyword("col-resize")

@OptIn(ExperimentalComposeUiApi::class)
internal actual val resizeVertical: PointerIcon = PointerIcon.fromKeyword("row-resize")
```

`iosMain`:

```kotlin
internal actual val resizeHorizontal: PointerIcon = PointerIcon.Default
internal actual val resizeVertical: PointerIcon = PointerIcon.Default
```

On the web `ew-resize` and `ns-resize` would do too. CSS defines `col-resize` and `row-resize` as the pointers that say a column's or a row's size will change; the handle between two panels and a column's end are exactly that job.

## 5. Testing {#testing}

The pointer is not in the semantics: a test cannot read it with `SemanticsProperties`. But the element `pointerHoverIcon` makes is a `data class` (`PointerHoverIconModifierElement`), whose equality is the pointer's; it can be looked for among the node's modifiers:

```kotlin
fun SemanticsNodeInteraction.showsPointer(icon: PointerIcon): Boolean =
    fetchSemanticsNode().layoutInfo.getModifierInfo().any { it.modifier == Modifier.pointerHoverIcon(icon) }

onNodeWithTag("ListDetailHandle_mail").showsPointer(PantrixPointerIcons.ResizeHorizontal)
```

This needs the modifier to be on the same node as the tag: `pantrixTestTag` and `pointerHoverIcon` in the same `Box`. For a pointer that changes with the position, the mouse is moved there first:

```kotlin
onNodeWithTag(NAVIGATOR).performMouseInput { moveTo(Offset(x, centerY)) }
waitForIdle()
assertTrue(onNodeWithTag(NAVIGATOR).showsPointer(PantrixPointerIcons.ResizeHorizontal))
```

The platform's real pointer is tested too: each platform's test looks that `PantrixPointerIcons` is that platform's expected pointer. On the desktop it equals `PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR))` (`AwtCursor` builds its equality from the cursor's type), on Android `PointerIcon(TYPE_HORIZONTAL_DOUBLE_ARROW)`, on the web `PointerIcon.fromKeyword("col-resize")`, on iOS `PointerIcon.Default`.

## 6. Pitfalls {#pitfalls}

*   **A finger.** A touch screen has no pointer; the modifier does nothing. Where a narrow strip is small for a finger (like `PantrixDataTable`'s column end), no drag is set up for touch at all, and no pointer is given.
*   **A mouse on Android.** Android draws the pointer only while there is a mouse or a touchpad: ChromeOS, Samsung DeX, a tablet with a mouse. In an emulator the computer's mouse most often comes as touch, and the pointer does not show.
*   **An ancestor comes first.** An ancestor with `pointerHoverIcon(…, overrideDescendants = true)` holds down every pointer below it.
*   **Leaving the node while dragging.** The pointer is shown and let go by Compose's `Enter` and `Exit` events. When `Exit` comes once a pressed pointer leaves the node was not verified (section 7); the navigator, which keeps the end being dragged itself, is not affected by it.
*   **On the desktop each system draws its own.** `E_RESIZE_CURSOR` is a type; which of the system's pictures it is drawn with is AWT's decision.

## 7. Verification {#verification}

### Tests

:::table
| What | Where | Targets |
| :--- | :--- | :--- |
| Each platform's pointer | `ui/foundation/src/{jvmTest,androidHostTest,webTest,iosTest}/…/platform/PointerIconsOn…Test.kt` | JVM, Android, JS, Wasm, iOS |
| The handle's pointer | `ListDetailScaffoldTest.the_handle_makes_the_list_wider_and_narrower` | Common tests: JVM, iOS, JS, Wasm |
| The column end's pointer, no pointer for a finger | `DataTableTest.the_end_of_a_resizable_header_is_dragged_to_a_width`, `a_finger_does_not_drag_a_header_s_end_and_a_keyboard_still_moves_it` | Common tests |
| The navigator's pointer that changes with the end, kept while dragging | `ChartZoomToolsTest.a_pointer_over_an_end_of_the_part_in_view_is_one_that_resizes`, `ChartTurnedZoomTest.the_part_in_view_is_dragged_down_the_navigator_of_a_horizontal_chart` | Common tests |
:::

### Behaviours broken by hand

Seven mutants were tried by hand, and the tests killed all seven: the modifier taken off the handle and off the column end, the navigator not listening to the position, the upright navigator giving the horizontal pointer, the nearness to an end set to nought, the end being dragged not counted in the pointer, and not let go when the drag ends.

### Not verified

*   How the pointer looks on the screen: on the desktop on macOS, Windows and Linux, in a browser, and on Android with a mouse. The tests check which pointer is given to Compose, not how the system draws it.
*   Whether Compose sends `Exit` at once when a pressed pointer leaves the node, or when it is let go.

## 8. Where it is in this repository {#repository toc="8. In the repository"}

*   The API: `ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/platform/PantrixPointerIcons.kt`, and the platforms' counterparts in the same package's `androidMain`, `jvmMain`, `webMain` and `iosMain` files.
*   Those that use it: `container/listdetail/ListDetailScaffold.kt` (`PaneHandle`), `data/datatable/DataTableHeader.kt` (`Grip`), `chart/cartesian/PlotNavigator.kt` and `NavigatorHand.kt` (`gripUnder`, `held`).
*   The test's helper: `ui/components/src/commonTest/kotlin/com/pantrixui/components/TestSupport.kt`, `showsPointer`.
*   The demo: the example "Resize pointer" of the "Platform and system" page (`app/shared/…/samples/PlatformSamples.kt`).
