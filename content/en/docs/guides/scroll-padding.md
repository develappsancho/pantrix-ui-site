---
title: Focus beneath a bar
description: Why, in Compose Multiplatform, keyboard focus is lost behind a bar in content that scrolls beneath the bar, what the library gives for it and how it is tested. The problem is not Pantrix's own: it is seen the same way in every Compose app that scrolls its content beneath a bottom bar, a top bar or the system bar.
---

Which of what is said here was seen on a device, which was read in the source of Compose Multiplatform 1.12.1 and which was not verified is written separately in section 7.

## 1. The problem {#problem}

:::aside scroll-padding
### Symptom

*   There is a bar at the bottom of a screen and the content scrolls beneath it: the list takes the bar's share as `contentPadding`, its last row stands above the bar, and while it scrolls the rows pass behind the bar.
*   The screen is moved through with a keyboard: Tab, the arrow keys, a TV remote, a switch access device. As the focus goes down the list scrolls, and the item that takes the focus comes **behind the bar**: its ring cannot be seen, and what is in focus is not known. Going up, the same happens behind the top bar.
*   With touch there is no problem. Nor does a test show it: `requestFocus()` gives the focus, `assertIsFocused()` passes; no one looks at where the item stands.
*   WCAG 2.2's criterion 2.4.11 asks for exactly this (Focus Not Obscured, AA): the component that takes the focus must not be wholly covered by content the author put there.
:::

### Cause

Compose brings the item that takes the focus into the visible area: the item sends a request, every scroller on its way meets the request in turn and scrolls the item **to its own edge**, no farther. What the scroller counts as "visible" is its whole size. The content padding (a lazy list's `contentPadding`, or a `padding` inside a scroll) is part of that area; the scroller does not know it. The bottom edge of a list that reaches under the bar is behind the bar.

Its little sibling: the library's focus ring is drawn outside the item. When the scroller brings the item to its edge, one side of the ring is outside the scrolling area and is cut off.

## 2. The solution {#fix}

First the question: **must** the content scroll beneath the bar?

:::table
| Case | Way |
| :--- | :--- |
| The bar is opaque and there is nothing to be seen behind it | The padding is given to the scroller itself, not to the content: `LazyColumn(Modifier.padding(padding))`. The scrolling area ends at the bar and its edge is the bar's top; no problem is left. No API is needed. |
| The content must pass beneath the bar (a translucent bar, behind the system bar on an edge with no bar) | The content says how much room it leaves at its ends; the request to bring into view is grown by that much. This is what follows. |
:::

The second way has one piece: a modifier that stands over the content and **grows every request that passes by the padding**. The item says "show me"; the modifier makes it "show me and the 64dp under me"; the scroller leaves the item 64dp inside its edge, above the bar. Compose gives a public and stable interface for this: `BringIntoViewModifierNode` (`androidx.compose.ui.relocation`).

Four details:

:::table
| # | Detail | What is done |
| :--- | :--- | :--- |
| 1 | A scroller asked for more than it holds does not scroll the excess, and passes it to the one above it **as the user's drag**: a sheet it stands in moves, a pull-to-refresh list shows its mark | No more is asked than the content reaches. The scrolled content is recognised by its size (it is larger than its place) and the request is cut at its bounds |
| 2 | A lazy list lays out its items one by one: how far the content reaches cannot be known from an item | The modifier is put on each item and the whole padding is asked for; the list's `contentPadding` must be at least that much |
| 3 | While the keyboard is open the scrolling area ends above the keyboard (`imePadding`) and the bottom bar is behind the keyboard: it does not cover the area | The keyboard's share is taken off the padding (`padding - WindowInsets.ime.asPaddingValues()`); if it is not, at the first key the page jumps up by the bar's height |
| 4 | The focus ring is outside the item | In a column or a row the ring asks for its room itself. In a lazy list it cannot: the list adds the ring's room to its `contentPadding` and tells its items the same value |
:::

### Why not `BringIntoViewSpec`

The tool Compose shows for this job is `LocalBringIntoViewSpec`: the scroller is told "this much of your area does not count as visible". It was the demo's first solution; it was left for three reasons:

*   **It reaches every scroller inside.** It is a composition local: the rule given to the page's scroller also goes to a row that scrolls sideways inside it and to a text field's own scroll. The rule does not know the area's direction: they too do not count that much of their edges as visible, and the bottom bar's height is taken off a row's right end. The ones inside have to be given the usual rule back; in a lazy list this is done in each item apart.
*   **It is experimental.** `LocalBringIntoViewSpec` carries `@ExperimentalFoundationApi` on Android and on the Skiko targets.
*   **It does not know how far the content reaches.** If the content has no such padding, the scroller is asked for what it does not have and the excess passes to the one above (detail 1).

It has one advantage: while the scrolling area shrinks (while the keyboard opens), Compose's own tracking that keeps the focused item visible uses that rule too. The modifier only grows requests; this is written in section 5.

## 3. With Pantrix {#with}

The library gives one modifier (`com.pantrixui.foundation.a11y`):

```kotlin
public fun Modifier.scrollPadding(padding: PaddingValues): Modifier
```

It does **not add** padding; it **tells** the padding the content leaves. It is put over what is scrolled, inside the scroller.

### 3.1. A scrolling column or row

After the scroll, before the `padding` it speaks of:

```kotlin
PantrixScaffold(bottomBar = { insets -> AppTabs(Modifier.padding(insets)) }) { padding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .scrollPadding(padding)
            .padding(padding)
    ) {
        // …
    }
}
```

Here no more can be asked: the content is recognised by its size and the request is cut at its bounds. If the content has no such padding the last item comes to the edge, and nothing passes to the one above. The focus ring asks for its room itself; if at the end of the content there is room for a ring after the bar's share (most pages have a margin of their own), the last item's ring is whole too.

In a row that scrolls sideways, the start and the end of `padding` are read by the reading direction. The demo's sideways-scrolling frame leaves a ring's room at both its ends and says so like this:

```kotlin
val ring = PaddingValues(horizontal = PantrixTheme.platform.focusRingReach)
Box(Modifier.horizontalScroll(state).scrollPadding(ring).padding(ring)) { content() }
```

### 3.2. A lazy list

On each item; the value is the list's `contentPadding`:

```kotlin
PantrixScaffold(bottomBar = { insets -> AppTabs(Modifier.padding(insets)) }) { padding ->
    // The bars' share and, above and below, a ring's room.
    val room = padding + PaddingValues(vertical = PantrixTheme.platform.focusRingReach)
    LazyColumn(contentPadding = room) {
        items(orders, key = { it.id }) { order ->
            OrderRow(order, Modifier.scrollPadding(room))
        }
    }
}
```

A row it is not put on behaves as before: it comes to the edge. If the list's `contentPadding` is less than what is said, the list is asked for what it does not have (section 2, detail 1): **the two values must come from the same variable.** In grids (`LazyVerticalGrid`) the use is the same.

### 3.3. While the keyboard is open

A screen with a text field ends the scrolling area above the keyboard. Meanwhile the bottom bar is behind the keyboard; the keyboard's share is taken off the padding. Compose's subtraction of `PaddingValues` does this: it does not go below zero and reads its value at the moment it is asked, so the screen is not composed again while the keyboard moves.

```kotlin
Column(
    Modifier
        .imePadding()
        .verticalScroll(scroll)
        .scrollPadding(padding - WindowInsets.ime.asPaddingValues())
        .padding(padding)
)
```

The demo's page is like this (`App.kt`, `DemoPage`).

### 3.4. Scrollers inside

They are not touched. A row that scrolls sideways inside the column brings its item to its own edge, as always; the request that comes out of it passes through the column's modifier, and the column shows the row above the bar. A text field's cursor stays as it is in its own box.

## 4. Without Pantrix {#without}

A modifier and its node. It is written with stable APIs; no `expect`/`actual` is needed.

```kotlin
fun Modifier.scrollPadding(padding: PaddingValues): Modifier = this then ScrollPaddingElement(padding)

private data class ScrollPaddingElement(val padding: PaddingValues) : ModifierNodeElement<ScrollPaddingNode>() {
    override fun create() = ScrollPaddingNode(padding)

    override fun update(node: ScrollPaddingNode) {
        node.padding = padding
    }
}

private class ScrollPaddingNode(var padding: PaddingValues) :
    Modifier.Node(),
    BringIntoViewModifierNode {
    override suspend fun bringIntoView(
        childCoordinates: LayoutCoordinates,
        boundsProvider: () -> Rect?
    ) {
        // The request goes to the scroller above in this node's own coordinates, grown.
        bringIntoView {
            val asked = if (isAttached && childCoordinates.isAttached) boundsProvider() else null
            asked?.let { rect ->
                val own = requireLayoutCoordinates()
                val here = rect.translate(own.localBoundingBoxOf(childCoordinates, clipBounds = false).topLeft)
                val direction = requireLayoutDirection()
                val grown = with(requireDensity()) {
                    Rect(
                        left = here.left - padding.calculateLeftPadding(direction).roundToPx(),
                        top = here.top - padding.calculateTopPadding().roundToPx(),
                        right = here.right + padding.calculateRightPadding(direction).roundToPx(),
                        bottom = here.bottom + padding.calculateBottomPadding().roundToPx()
                    )
                }
                // In the content of a scrolling column: no more is asked than the content reaches. On a lazy
                // list's item this line is left out; there the list's contentPadding holds the bound.
                grown.intersect(Rect(0f, 0f, own.size.width.toFloat(), own.size.height.toFloat()))
            }
        }
    }
}
```

`BringIntoViewModifierNode` and `bringIntoView`: `androidx.compose.ui.relocation`; `requireLayoutCoordinates`, `requireLayoutDirection`, `requireDensity`: `androidx.compose.ui.node`.

In this bare form the two uses are two separate modifiers (one that cuts at the bounds and one that does not). Pantrix's is one: it finds the scrolled content by its size, cuts where it finds it, and where it does not (a lazy list, content that fits its scroller) asks for the whole padding (`RoomInView.kt`, `withRoom`).

In the inner lambda `isAttached` is checked: the request is asked again on every frame for as long as the scroller scrolls for it, and by then the modifier may have left the composition; `requireLayoutCoordinates()` throws on a node that is not attached.

## 5. What the modifier does not do {#limits toc="5. What it does not do"}

*   **It adds no padding.** If the content has no such padding, there is no room either to raise the last item above the bar.
*   **It does not know where the bar is.** What is given is the padding at the content's ends. If the scroller does not reach that edge (while the keyboard is open, in a pane that does not touch the bar), less is covered, and the caller says so (3.3).
*   **It does nothing while the scrolling area shrinks.** When the keyboard opens, what keeps the focused item visible is Compose's own tracking; that is not a request and does not pass through the modifier. In a text field Compose keeps the **cursor** in view, not the box: in the iOS simulator, when the keyboard opened, the field's bottom edge stayed behind the keyboard. This is so in every Compose text field, with the modifier or without it; it is written in `docs/OPEN_ITEMS.md`.
*   **It does not know the touch target.** A button is drawn in the middle of the room set aside for a finger, and smaller than it; the request to bring into view is reckoned from the box drawn and its ring, not from the room. If the difference between what is drawn and its room is larger than a ring, then on going back to the button at the start of the content the scroller stays scrolled by that difference: in a touch browser a button is drawn at 32dp in a 48dp room, and the demo's frame stopped at 4dp. On Android (48 and 40dp) and on iOS (44 and 36pt) the difference is as large as a ring, and the frame goes back to the very start.
*   **A screen reader's focus is not the keyboard focus.** TalkBack and VoiceOver do the scrolling with their own actions when they reach an item. Not listened to.
*   **It does not promise the place to the pixel.** Compose carries what it brings a long way a few pixels past its target and leaves it there: the item stands away from the bar by the padding **or a little more**.
*   **In a lazy list it can ask for more.** If the list's `contentPadding` is less than what is said (3.2).

## 6. Testing {#testing}

*   **Where it stands is looked at, not that it is in focus.** `requestFocus()`, `waitForIdle()`, then the item's `boundsInRoot` is compared with the bar's (`ScrollPaddingTest`, `DemoShellTest`).
*   **That nothing passes to the one above is held apart.** The scroller is put inside a `NestedScrollConnection` that writes down `onPostScroll`; the list must stay empty. At both ends of the content, and while the content fits its scroller.
*   **In a lazy list only the items in view exist.** The item to take the focus is first brought into view (`performScrollToIndex`), so that half of it stays outside: an item wholly in view scrolls nothing.
*   **An exact place is tested over a short way.** Over a long way, Compose's overshoot (section 5) hides the target: a frame that comes from 180 pixels away sticks to the end even if the 4 pixels asked for are missing. The demo's frame test starts with the frame scrolled 6 pixels from its end (`HorizontalScrollFrameTest`).
*   **In the browser a pixel's leeway is left.** In JavaScript the last step of a scroll can end a fraction of a pixel early.

## 7. Verification {#verification}

### Seen

*   Android emulator, API 34 (2026-10-11): on the demo's Pagination page, Tab was used to go down; every button that took the focus stood, with its ring, above the bottom bar. In the sideways-scrolling frame the end of the row was reached, and Shift and Tab went back to the start: the row was in the same place as at first, pixel for pixel (before, it stayed scrolled by a ring).
*   On the same emulator, the Scaffold page's example "Focus in a list that scrolls beneath the bars" (a lazy list): going down, the focused button stood with its ring above the example's bottom bar, and going up with Shift and Tab, below its top bar.
*   iPhone 18 Pro Max simulator, iOS 27.0: while the keyboard was open, with the bar's share not taken off, the page jumped up by the bar's height at the first digit typed; with it taken off, it did not jump. The field's bottom edge was behind the keyboard in both cases (section 5).
*   The built-in browser, a window of a phone's width (375 × 812, with touch emulation): in the Scaffold page's same example, going down with Tab and up with Shift and Tab, the focused button stood with its ring between the two bars; on the Pagination page the button that took the focus was above the bottom bar. The sideways-scrolling frame went to the end of the row; on going back to the start it stayed scrolled by 4dp (section 5, the touch target).

### Tests

*   `ScrollPaddingTest` (twelve tests, on five targets): the end and the start of a column, both ends of a row and a right-to-left layout, no more asked than the content reaches, a changing padding, a scroller inside, an item clipped by the one above, padding removed while a scroll runs, a lazy column and a lazy row, with the focus ring.
*   `DemoShellTest`: when a button low on the page takes the focus, it stands above the bottom bar.
*   `HorizontalScrollFrameTest`: the frame goes all the way to its end when the button at its end takes the focus.
*   Mutation of the lines written: of 80 mutants 63 were killed and 6 did not compile; five of the 11 that survived were real gaps, and their tests were written (a ring that scrolls in a row inside a column, a clipped item, removed padding, as much as the ring reaches out, the card's copy button).

### Read in the sources

Compose Multiplatform 1.12.1, `foundation` and `ui`:

*   `ContentInViewNode`: what the scroller counts as visible is its own size; while it meets the request it passes the scroll on as `NestedScrollSource.UserInput`; when the area shrinks it keeps the focused item in view not by a request but by its own tracking.
*   `LazyList` and `LazyGrid`: read `LocalBringIntoViewSpec` in the composition and wrap it for sticky headers (`StickyHeaderBringIntoViewSpec`); the content padding is not in the reckoning. It was seen in a test too: a `LazyColumn` with a `contentPadding` brings the item to its own edge.
*   `LocalBringIntoViewSpec`: its declaration on Android and on the Skiko targets carries `@ExperimentalFoundationApi`; every scroller not given the rule by hand reads it from its own node (`ContentInViewNode.requireBringIntoViewSpec`).
*   `BringIntoViewModifierNode` and `DelegatableNode.bringIntoView`: they carry no experimental mark.
*   `FocusTargetModifierNode.getFocusedRect` and `TextFieldDecoratorModifier`: what is kept in view while the area shrinks is the rectangle the focus target itself tells; a text field tells its cursor's (`focusProperties.focusRect`).

### Not verified

*   **A TV remote, switch access, screen readers.** Tried only with Tab.
*   **An iPad and an iPhone with a hardware keyboard.** In the simulator the hardware keys did not reach the app; what was seen on iOS is only the state while the keyboard is open.
*   **The desktop app's window and a wide browser window.** Their tests ran; no one moved through them by hand. In a wide window the demo has no bottom bar; its navigation is at the side.
*   **Grids, `reverseLayout` and the pager.** Only `LazyColumn`, `LazyRow`, and the scrolling column and row were tested.
*   **A translucent bar.** The demo's bars are opaque: that the focus stands above the bar was seen, but what shows from behind the bar was not looked at.

## 8. Where it is in this repository {#repository toc="8. In the repository"}

:::table
| What | Where |
| :--- | :--- |
| The modifier | `ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/a11y/ScrollPadding.kt` |
| The reckoning that finds the scrolled content and cuts the request at its bounds | `RoomInView.kt` in the same package (`withRoom`); the focus ring uses it too (`FocusRing.kt`) |
| As much as a ring reaches out | `PantrixPlatform.focusRingReach` (`Platform.kt`) |
| Tests | `ScrollPaddingTest`, `FocusRingInViewTest` (`ui/foundation/src/commonTest/.../a11y`) |
| The demo's page | `app/shared/src/commonMain/kotlin/com/pantrixui/cmp/demo/App.kt` (`DemoPage`) |
| The demo's sideways-scrolling frame | `app/shared/.../demo/page/HorizontalScrollFrame.kt` |
| The example in the demo | "Focus in a list that scrolls beneath the bars" on the `Scaffold` page |
:::
