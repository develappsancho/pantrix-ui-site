---
title: Fitting the window
description: The window information PantrixTheme gives its content: what it is, where it comes from, how it is read and how it is fixed in a test.
---

Which helper to use while writing a screen or a component, and how, is in the [adaptive usage guide](/docs/guides/adaptive-usage), with three working sample cases. The rules an interface keeps while fitting the window are in [its rules](https://github.com/developersancho/pantrix-ui-cmp/blob/main/docs/guide/adaptive/adaptive-rules.md) (in Turkish).

## 1. What does it give? {#what-does-it-give}

Every screen that uses `PantrixTheme` finds the window it is drawn in ready. There is no separate wrapper or setup:

```kotlin
PantrixTheme {
    val window = PantrixTheme.window

    window.widthClass                 // Compact, Medium, Expanded, Large, ExtraLarge
    window.heightClass                // Compact, Medium, Expanded
    window.size                       // DpSize
    window.posture.isBook             // half open, the hinge vertical
    window.posture.isTabletop         // half open, the hinge horizontal
    window.posture.separatingFold     // the hinge the content must not cross, or else null
    window.paneCount()                // how many panes there is room for side by side: 1, 2 or 3 (section 10)
    window.breakpoints                // the boundaries where the classes start

    PantrixTheme.layout.margin        // the page's margin, by the window's class
    PantrixTheme.layout.gutter        // the room between columns and panes
}
```

The types are in the `com.pantrixui.foundation.adaptive` package. What is looked at is not the device or the screen but **the window**: an app in a split screen, in a resized desktop window or in a browser tab is as wide as that window.

:::sketch adaptive
:::

## 2. Classes and breakpoints {#classes-and-breakpoints}

:::table
| Width class | Width (dp) | Where it is seen |
| :--- | :--- | :--- |
| `Compact` | < 600 | A portrait phone, a foldable's cover screen, a narrow browser window |
| `Medium` | 600–839 | An open foldable, a portrait tablet, a split screen |
| `Expanded` | 840–1199 | A landscape tablet, most landscape phones |
| `Large` | 1200–1599 | A desktop window, a browser, a large tablet |
| `ExtraLarge` | ≥ 1600 | A wide desktop window, an external display |
:::

:::table
| Height class | Height (dp) | Where it is seen |
| :--- | :--- | :--- |
| `Compact` | < 480 | A phone held landscape |
| `Medium` | 480–899 | A portrait phone, a landscape tablet |
| `Expanded` | ≥ 900 | A portrait tablet, a desktop window |
:::

The breakpoints are Android's window size classes, and stand in `PantrixBreakpoints`. The classes are ordered; "from this width on" is a comparison:

```kotlin
if (window.widthClass >= WindowWidthClass.Expanded) ListBesideDetail() else ListAlone()
```

## 3. Helpers: choosing a value and asking {#helpers-choosing-a-value-and-asking}

There are helpers for the choices and the questions a screen writes most often. All are in the `com.pantrixui.foundation.adaptive` package, and each gives a name to something the window already says.

```kotlin
val window = PantrixTheme.window

// The ones that choose a value: a class not given takes the value of the one below it, a posture not given that of `flat`
val columns = window.byWidth(compact = 2, medium = 4)                    // 2, 4, 4, 4, 4
val barHeight = window.byHeight(compact = 44.dp, medium = 56.dp)
val panes = window.byPosture(flat = 1, book = 2, tabletop = 2)

// Questions
window.isNarrow       // the width is Compact: a portrait phone
window.isShort        // the height is Compact: a landscape phone
window.isSizeKnown    // has the platform told the size

// The short form inside a composable
val cardPadding = responsive(compact = 12.dp, medium = 16.dp, large = 20.dp)  // the same as PantrixTheme.window.byWidth(...)
val threeColumns = rememberWindowIsAtLeast(1000.dp)                      // a width at which no class starts

// The component's own room: the box tells the room given to it as if it were a window
BoxWithPantrixWindow(modifier) { box -> if (box.isNarrow) OrderList() else OrderTable() }
```

:::table
| Helper | What it tells | When it sets up again |
| :--- | :--- | :--- |
| `byWidth(compact, medium, expanded, large, extraLarge)` | The width class's value | When the class changes |
| `byHeight(compact, medium, expanded)` | The height class's value | When the class changes |
| `byPosture(flat, book, tabletop)` | The posture's value: `book` in a book, `tabletop` on a table, `flat` in every other case | When the posture changes |
| `isNarrow` | Whether the width class is `Compact` | When the class changes |
| `isShort` | Whether the height class is `Compact` | When the class changes |
| `isSizeKnown` | Whether the platform has told the window's width and height | When the answer changes |
| `responsive(...)` | The `byWidth` of the theme's window | When the class changes |
| `rememberWindowIsAtLeast(width)` | Whether the window is at least `width` wide | When the window crosses that width |
| `BoxWithPantrixWindow { box -> … }` | The room given to the component, as a window (section 10) | When the class the content reads changes |
:::

Notes:

*   **There is no helper for "from this class on", because none is needed:** the classes are ordered. `window.widthClass >= WindowWidthClass.Expanded`, `<=` and a range (`in WindowWidthClass.Medium..WindowWidthClass.Expanded`) do the same job.
*   **`isNarrow` and `isShort` are `true` before the size is known too:** until then the window reads `Compact` in both directions (section 5).
*   **`responsive` looks only at the width**, and is called only inside a composable. For the height, or in a place that is not a composable (a measurement, a callback), ask the window itself: `byHeight`, `byWidth`.
*   **`rememberWindowIsAtLeast` is for a single screen's own threshold.** If all the layouts change at the same widths, change the breakpoints (section 9).
*   **A plain `if` is enough to show a block only in some classes.** Do not call the same content from two separate places by class: when the class changes, what the content remembers (the selection, the scroll, the text typed) resets. Call it from one place, and choose the values by class.

## 4. Height: a landscape phone {#height-a-landscape-phone}

The width chooses the layout. The height says whether there is room to stack things.

A phone held landscape comes out `Medium` or `Expanded` by its width (iPhone 17: 874 × 402 dp), but its height is `Compact`: every bar that runs across it takes a seventh of the screen. The rule for a window whose height is `Compact`:

*   The width still chooses the layout: the navigation stands at the side, and if the window is wider than 840 dp, the list and the detail come side by side.
*   The navigation at the side carries as many items as fit a short window.
*   There is no fixed bar at the bottom: the main action is in the header.
*   There is no sheet that opens from the bottom: menus are popovers, forms side sheets.
*   The tabletop posture is outside this rule: there the lower half is the controls' place.

Today the only item of this rule with a counterpart in the components is the first: `PantrixScaffold` takes the navigation to the side from 600 dp on, on a landscape phone too. For the other items the library gives only the value to ask (`heightClass`); the components will apply the rule themselves as they come.

## 5. Where does the size come from? {#where-does-the-size-come-from}

The theme does no measuring; it reads the size from Compose's own window information (`LocalWindowInfo`). On 3 October 2026 it was looked at on every platform with the sample app:

:::table
| Platform | The size read | The first composition |
| :--- | :--- | :--- |
| Android | The Activity's window, the system bars included. On a foldable emulator: 852 × 883 when open, 443 × 970 on the cover screen, 970 × 443 with the cover landscape | Starts with the right size |
| iOS | The bounds of the `UIWindow`. iPhone 17: 402 × 874, iPad: 820 × 1180 | Starts with the right size |
| Desktop | The window's content area (800 × 572 in an 800 × 600 window). It follows the window as it is dragged | Starts with **a zero size** |
| Web | The box Compose draws in, not the browser window. All five classes were tried, from 375 to 1700 | Starts with **a zero size** |
:::

**On the desktop and the web the first composition sees the window as 0 × 0**, so the class is `Compact` for a moment; the size comes right after. This is Compose's order on those two platforms: on the desktop the content is set up before the window is shown, and on the web the box's size is read after the content's first composition. Nothing is on the screen at that moment, but take care of this:

*   Do not keep the class with `remember` or `rememberSaveable`; read it from `PantrixTheme.window` every time.
*   Do not tie a decision that runs once in the first composition (like the start page) to the class.

If there is a decision given once and kept, holding the content back until the size comes is in the app's hands. `isSizeKnown` is for this; on Android and iOS it is `true` from the start, so nothing waits there:

```kotlin
PantrixTheme {
    // The app is first set up in a window whose class is real.
    if (PantrixTheme.window.isSizeKnown) App()
}
```

The theme does not do this by itself: in a setting that never reports its size, the screen would stay empty.

## 6. Where does the posture come from? {#where-does-the-posture-come-from}

*   **Android:** from Jetpack WindowManager's `FoldingFeature`. No new library enters the app: on Android, Compose UI already depends on WindowManager. On a device with window extensions 9 and later the hinge is ready in the first composition; on an older one the first composition is flat, and the hinge comes right after.
*   **The web:** from the browser, with two APIs. When a hinge splits the viewport, the browser gives the viewport in segments (Viewport Segments, `window.viewport.segments`): two segments side by side are a vertical hinge, two one above the other a horizontal one. It also says that the device is held half open (Device Posture, `device-posture: folded`). Both are in Chromium today; a browser that does not know them, and a device that does not fold, is flat. The answer is followed with a media query: when the device folds, the posture changes by itself. To an app running in a frame inside another page (`iframe`), the browser gives no segments; it is flat there too.
*   **iOS:** from UIKit from iOS 27.1 on, with two sources. The hinge's place is in the view's reserved regions (`reservedRegions(kind: .division)`): every region where the content is split in two is a hinge. The region's frame also takes in the margin that Apple wants things to be touched to keep away from; the hinge itself is the line inside that margin (`bounds`), and the strip together with the margin is `clearance`. If the region is active, the hinge separates the window (`isSeparating`). How far the hinge is open is told by `UIHingeInteraction`: if it is partly open, `isHalfOpened`. If regions that something covers (`.occlusion`) fall on the hinge, it is `isOccluding`; covered regions elsewhere, like the corner of the camera and the status bar, are not hinges, and the safe area already keeps away from them. The library reads the regions in an invisible view behind the content every time UIKit lays it out: when the phone folds, opens and turns, the posture changes by itself. To read them, it is enough for the library to be built with the Xcode 27.1 SDK; the app runs on older iOS versions too, where the posture is flat. Since today's version of Kotlin does not know these APIs, UIKit is reached through a small Objective-C bridge written with cinterop: how it works, why it is not Swift and what happens on older iOS versions is in the [guide to the iOS fold's cinterop](https://github.com/developersancho/pantrix-ui-cmp/blob/main/docs/guide/ios-fold-cinterop/IOS_FOLD_CINTEROP_GUIDE.md) (in Turkish).
*   **The desktop:** flat.

An app that reads the hinge itself gives its posture:

```kotlin
PantrixTheme(window = rememberPantrixWindow(posture = myPosture)) { … }
```

:::table
| Posture | How it is known | `posture` |
| :--- | :--- | :--- |
| Flat and open | There is a hinge, and the screen is one piece | `folds` filled, `separatingFold` null |
| Book | Half open, the hinge vertical | `isBook` |
| Tabletop | Half open, the hinge horizontal | `isTabletop` |
| Closed | The cover screen | `WindowPosture.Flat`, the window `Compact` |
:::

The hinge's place is in `separatingFold.bounds`: in pixels, in the window's coordinates. The strip around it that touches leave free is `clearance`: on iOS it is 20 pt wide on either side of the hinge; on Android and the web it is `bounds` itself.

Measured in Xcode 27.1's **iPhone Duo** simulator: the cover screen is 466 × 678 pt and has no hinge; at its right edge stands the system's vertical bar (section 10, "The system's vertical bar"). The inner screen is 951 × 669 pt; the hinge is in its middle (x = 475.5), and its region is 40 pt wide with the 20 pt margins on either side. When the phone is turned, the inner screen becomes 669 × 951 and the hinge stands horizontal. In the simulator the hinge's angle is changed from Xcode's device window; there is no command for it in `simctl`.

## 7. Recomposition {#recomposition}

`PantrixTheme.window` is the same object for as long as the theme stands; its values are read when they are asked for.

*   What reads `widthClass` or `heightClass` is set up again **only when the class changes**. The helpers are so too (the table in section 3).
*   What reads `size` is set up again **at every change**: on iOS at every frame while rotating, on the desktop and the web at every pixel while the window is dragged.

That is why layouts read the class. If you are to ask about a width that fits none of the classes, do not write `window.size.width >= 1000.dp`; use `rememberWindowIsAtLeast(1000.dp)`: it sets up again only when the window crosses that width. If the size itself is needed, read it in a small composable of its own; the sample app's `WindowSamples` does so.

## 8. Fixing the window {#fixing-the-window}

Places where the real window is not the right answer:

```kotlin
// Tests and previews: seeing the layout at a given size
PantrixTheme(window = PantrixWindow(DpSize(700.dp, 900.dp))) { … }

// A part of the screen: let its content fit its own pane, not the window
CompositionLocalProvider(LocalPantrixWindow provides PantrixWindow(DpSize(360.dp, 900.dp))) { … }
```

*   **Embedded Compose:** in a Compose view placed in a part of a native screen, the window is wider than that part. Give the part's size with `PantrixWindow(...)`.
*   **A nested theme:** a theme not given a window uses the outer one's window. The window a test fixed is not lost in a dark theme inside. Not only the window: the inner theme takes every setting not given to it from the outer one (below).
*   **With no theme:** `PantrixTheme.window` still answers: Compose's window, with the default breakpoints, flat.

**In tests the window changes by platform:** the window of the Skiko tests (JVM, iOS, web) is 1024 × 768, that is, `Expanded`. The window of the Android host tests is Robolectric's `qualifiers`. A test that tries a behaviour bound to the class should fix its window as above; otherwise the same test gives another result by platform.

### A nested theme: if a part of the screen differs

A dark panel on a light screen, or a tight table on a roomy page, is a second theme inside the theme. The inner theme **takes every setting not given to it from the outer one**; what it gives is its own:

```kotlin
PantrixTheme(
    locale = PantrixLocale.Turkish,
    analytics = myAnalytics,
    platform = PantrixPlatform.forType(PlatformType.Desktop, isTouch = true)
) {
    Page()
    PantrixTheme(darkTheme = true) {        // a dark panel: only the colours change
        PantrixSurface {                    // the surface inside draws the dark ground, not the theme
            PantrixButton(text = "Save", onClick = save)
            // the touch target 48 dp, "Yükleniyor" in Turkish, the click goes to myAnalytics
        }
    }
    PantrixTheme(density = PantrixDensity.Compact) {   // a tight table: only the density changes
        OrdersTable()
    }
}
```

The sample app has a page for this: **Theme: Inside Another Theme** (on the web, `#theme-inside-another`). It shows the dark part, the tight part and a part in another language; each writes the theme's values, and the line that differs from the page's is marked "(changed)".

*   **The theme draws no ground.** The inner theme only changes values; the `PantrixSurface` or the card put inside it draws the dark ground. Without a ground, the dark theme's light text stays on the light page.
*   **Nor is the theme a layout.** The inner theme's content is placed by the layout the theme stands in (`Column`, `Row`); the inner theme puts no box between. The box that opens the test tags to automation is the outermost theme's.
*   **What is inherited:** the platform, the density, the spacing and the layout measures, the shapes, the typography, the motion, the elevation, the language and the texts, the styles, the component overrides, the test tag policy, analytics, haptics and the window.
*   **The colours:** the inner theme is as dark as the outer one and keeps its colours (a brand's too). If the opposite is said with `darkTheme`, it takes the set of that kind: if the outer look was set up with a preset, the preset's dark (or light) set; if it was not, Pantrix's own set. A brand with no preset gives its dark set with `colors`.
*   **What scales with the density** (the spacing, the layout measures, the touch target) is scaled once, by the inner theme's own density; not a second time on top of the outer one's.
*   **If the language changes**, the texts are that language's; texts given to the outer theme by hand are kept only while the language is the same.
*   **A theme with a preset** (`PantrixTheme(preset = …)`) takes the four things the preset says (the colours, the typography, the shapes, the component overrides) from the preset, and the rest from the outer one. It too is as dark as the outer theme: inside a dark theme it is drawn with the preset's dark colours even when the system is light. At the root, that is, while it is inside no theme, it follows the system.
*   **If there is a `ProvidePantrixStyles` between them**, the inner theme takes the styles in force at that point: the outer theme's and those of every provider between.

## 9. Your own breakpoints {#your-own-breakpoints}

```kotlin
PantrixTheme(window = rememberPantrixWindow(breakpoints = PantrixBreakpoints(medium = 640.dp))) { … }
```

The widths and the heights must be ordered from small to large; if they are not, `PantrixBreakpoints` throws.

The window tells the breakpoints it is classed by: `PantrixTheme.window.breakpoints`. Components are classed by them too: `BoxWithPantrixWindow` takes the breakpoints from the window in force. Code that classes a size by hand does the same:

```kotlin
val pane = PantrixWindow(paneSize, breakpoints = PantrixTheme.window.breakpoints)
```

## 10. A component that is a pane does not ask the window {#a-component-that-is-a-pane-does-not-ask-the-window}

The window is for decisions at the level of the page. A component that is itself a pane (the scaffold, the two panes, the sheets to come) does not ask the window, but measures the room given to it: for a component inside a narrow pane, the window is still wide. `PantrixScaffold` is so: it takes the navigation to the side when its own width reaches 600 dp.

A component learns the room given to it with `BoxWithPantrixWindow`. The box is built on top of `BoxWithConstraints`, and gives the most room it can take as a `PantrixWindow`: everything a screen asks the window (`isNarrow`, `byWidth`, `widthClass >= …`) is asked of the box.

```kotlin
@Composable
fun OrdersTable(orders: List<Order>, modifier: Modifier = Modifier) {
    BoxWithPantrixWindow(modifier) { box ->
        if (box.isNarrow) OrderList(orders) else OrderGrid(orders, showDate = box.widthClass >= WindowWidthClass.Expanded)
    }
}
```

*   **The breakpoints are the window's.** The box is classed by whatever breakpoints the window in force is classed by; if the component has thresholds of its own, they are given with the `breakpoints` parameter.
*   **The content is not set up again at every size.** The content of `BoxWithConstraints` is set up again every time the box changes its size. The content here is set up when the class it reads changes; what reads `box.size` follows every change. The box is the same object while its room changes.
*   **On a scrolling page the height is unbounded.** The box's height class reads `Expanded`, and `isShort` is never `true`: ask the box for the width and the window for the height.
*   **The box is flat.** The hinge is the window's: ask the window for the posture. Where it crosses the box is told by `rememberFoldsInNode()` (below).
*   **`PantrixTheme.window` and `responsive` ask the window inside the box too.** If everything inside is wanted to ask the box (a pane whose content is a screen of its own), the box is handed down: `CompositionLocalProvider(LocalPantrixWindow provides box) { … }`.
*   **Its intrinsic size cannot be asked.** Like `BoxWithConstraints`, it sets up its content while it is being measured; it is not put inside a row or a column sized with `IntrinsicSize`.

The posture is outside this, because it is a property of the window. **In the tabletop posture** (half open, the hinge horizontal) the scaffold keeps the navigation at the bottom whatever the width: the lower half is where the hands are, and a column at the side would cross the hinge.

*   The rule holds if the hinge crosses the scaffold. A scaffold taller than both halves of the window cannot fit in one half, so the hinge crosses it; a scaffold that fits in one half counts as a pane there, and follows its width.
*   A `sideBar` the app puts itself stays in its place; only `navigation`, which changes its place, is moved.
*   While the navigation and the content are moved, they stay a single composition: what they remember stays with them.

### Two panes and the hinge

How many panes to show is a decision at the level of the page, and is asked of the window. Where the panes part, though, depends on where the panes stand, and is asked there.

```kotlin
val panes = PantrixTheme.window.paneCount()      // Compact and Medium 1, Expanded 2, from Large on 3 (limited by maxPanes)

if (panes >= 2) {
    PantrixTwoPane(
        first = { Inbox(onOpen = { opened = it }) },
        second = { Message(opened) },
        modifier = Modifier.fillMaxSize(),
        firstSize = PaneSize.Fixed(360.dp)       // or PaneSize.Fraction(0.4f); by default half and half
    )
} else {
    if (opened == null) Inbox(onOpen = { opened = it }) else Message(opened)
}
```

*   **`paneCount(maxPanes = 2)`:** 1, 2 or 3 by the width; in the book posture (or with a vertical hinge that separates two screens), at least 2 at every width. In the tabletop posture the hinge is horizontal and does not separate panes side by side: the number goes by the width.
*   **`PantrixTwoPane` always draws two panes.** Side by side (`TwoPaneOrientation.SideBySide`) or one under the other (`Stacked`). The first one's share is `firstSize`, and the room between is `gap`; its default is `PantrixTheme.layout.gutter`.
*   **They meet at the hinge.** If a hinge that separates the room in the direction the panes stand (a vertical one for panes side by side, a horizontal one for panes one under the other) crosses the room, the panes part exactly there, and the room between is the hinge's `clearance`. Then neither the share nor the gap holds.
*   **The reading direction.** In a right-to-left language, the first of the panes side by side is on the right. The hinge stays where it is on the screen.
*   **The room.** The room must be bounded in the direction of the split (there are no two panes one under the other inside a scrolling column). If it is unbounded in the other direction, it is as long as the longer pane.
*   **The state.** When the number of panes changes, a pane's content is called from another place and set up again. What must stay (the scroll position, the selection) is remembered above the pane and given to it: `val scroll = rememberScrollState()`.

Code that writes its own layout by the hinge reads the hinge in its node's own coordinates:

```kotlin
val folds = rememberFoldsInNode()
Layout(content, modifier.then(folds.modifier)) { measurables, constraints ->
    val fold = folds.separatingFold          // the hinge that cuts and separates this node, or else null
    // fold.bounds and fold.clearance are in pixels, and are measured from the node's top-left corner
    …
}
```

`folds` is read again as the node changes its place (while the page scrolls too), as the window changes and as the device folds, and the layout that measures is measured again. A hinge that does not cut the node is not in the list; a line that runs along the node's edge does not cut it.

**A folding window in a preview and a test:** `PantrixWindowFrame(window, modifier) { … }` makes a box a window of its own. What is inside reads the window given with `PantrixTheme.window`; its hinge is measured from the box's top-left corner, wherever the box is on the screen. Even if the box is drawn small in a scaled layer, the hinge scales together with the content. `window = null` passes on the window the box stands in: a picker that offers the real window together with made-up windows calls the content from the same place at every choice, and the state is not lost. The "Book" (951 × 669, a vertical hinge) and "Table" (669 × 951, a horizontal hinge) windows on the sample app's cards are drawn this way; they are the measures of the iPhone Duo's inner screen.

```kotlin
val book = with(LocalDensity.current) {
    val x = 475.5.dp.toPx(); val h = 669.dp.toPx(); val pad = 20.dp.toPx()
    PantrixWindow(
        DpSize(951.dp, 669.dp),
        posture = WindowPosture(listOf(WindowFold(
            bounds = Rect(x, 0f, x, h), orientation = FoldOrientation.Vertical,
            isHalfOpened = true, isSeparating = true, isOccluding = false,
            clearance = Rect(x - pad, 0f, x + pad, h)
        )))
    )
}
PantrixWindowFrame(book, Modifier.size(951.dp, 669.dp)) { InboxScreen() }
```

### List and detail: `PantrixListDetailScaffold`

A list, and the detail of the item opened in the list, is the adapting layout seen most often: an inbox, settings, contacts, files. So that every screen does not write the `if (panes >= 2) … else …` code above again, `PantrixListDetailScaffold` manages the number of panes, the move and the back gesture in one pane, the focus, the hinge and the list's width itself.

```kotlin
val inbox = rememberListDetailState()                    // the open item, the extra pane, the list's width; it is saved

PantrixListDetailScaffold(
    state = inbox,
    list = { Messages(onOpen = { id -> inbox.select(id) }) },
    detail = { id ->
        // In one pane the detail is in the list's place: only then does a back button stand.
        Message(id, onBack = if (isSinglePane) ({ inbox.back() }) else null)
    },
    modifier = Modifier.fillMaxSize(),
    emptyDetail = { PantrixText("Choose a message") },        // in two panes, while there is no open item
    listTitle = "Inbox",                                      // the panes' names for the screen reader
    detailTitle = { id -> subjectOf(id) },
    extra = { id -> History(id, showHeader = !isExtraInSheet) },  // an optional third pane
    extraTitle = "History",
    resizable = true                                          // the list's width is dragged
)
```

*   **The number of panes by its own room.** The scaffold asks not the window but its own box (the rule above): `Compact` and `Medium` one pane, from `Expanded` on two, and from `Large` on three if `extra` is given. A hinge that cuts the box vertically and separates it makes two panes at every width.
*   **One pane.** While there is no open item, the list shows. `select(key)` opens the detail in the list's place, sliding in from the end. The back key, Escape on the desktop and in the browser, and the edge swipe on iOS call `back()`. These come from `androidx.navigationevent`: Compose gives the back gesture from there on every platform, Android's predictive back gesture and iOS's edge swipe with their progress. The detail slides following the finger, and goes back to its place if it is let go halfway.
*   **Several scaffolds, one back.** If several scaffolds on a screen show a detail in one pane (a catalogue page, a screen's several cards), back closes the detail opened last: the scaffold's back listener is registered again at every item opened, and the back event goes to the listener registered last. The open detail of a scaffold scrolled off the screen takes back too when its turn comes.
*   **In two panes, back is the app's.** The scaffold's back listener works only in one pane and while an item is open; in the other cases back goes to the page's own navigation (Navigation 3's back stack).
*   **The focus goes with the pane shown.** On an item opened from the list with the keyboard, the focus moves to the detail; on coming back it returns to the item that was opened (`focusRestorer`). When it is opened by touch the focus is not touched: a focus that moved to a text field would open the keyboard.
*   **A pane not shown is not placed.** In one pane, the hidden list stays in the composition but is not placed: both the keyboard and the screen reader skip it, and the scroll position and everything it remembers stay. On going from one pane to two, the list and the detail are called from the same place, and their states are not lost.
*   **Keys.** Items are named by `String` keys that can be saved and are the same in every language. The detail is set up again for each key: keep what the detail edits (a setting's value, a draft) in the app's state, not inside the detail.
*   **The extra pane.** `showExtra()` opens it beside the detail, at the end (`extraWidth`), if there is room for three panes. If there is not, it slides in from the end over the detail, in a side sheet drawn in the scaffold's own room: its title is `extraTitle`, with a close button beside it and, under it, a scrim that covers only the scaffold. Pressing the scrim, the back key and Escape close it (back closes the sheet first, then the detail). The keyboard's focus goes into the sheet, does not move to the panes behind while it is open, and the screen reader does not read them; on closing, the focus goes back to where it came from in the detail. The sheet is not a separate window: the extra pane is the same composition in both places, and what it remembers (`remember` too) stays when its place changes. Its width is `extraWidth`, and it narrows enough to leave a touch target's room of scrim at the start; its look (the corners, the shadow, the colours, the scrim) is `SheetStyle` and the sheet's colours. `isExtraInSheet` lets it choose not to draw its own header in the sheet; `back()` closes the extra pane too.
*   **Every pane is a window.** The scaffold gives each pane's content the pane's room as a window (the rule "hand the box down" above): in a pane, `PantrixTheme.window`, `isNarrow`, `byWidth`, `responsive` and `PantrixTheme.layout` answer by the pane's width. In a 900 dp room, both the 360 dp list and the 524 dp detail are `Compact`. The posture stays the window's: a `PantrixTwoPane` in a pane still finds the fold.
*   **The screen reader.** Each pane is read as a single group. `listTitle`, `detailTitle` and `extraTitle` give the panes names (`paneTitle`); on Android, TalkBack says the name of the pane that becomes visible. On iOS, Compose does not report a pane's name to VoiceOver.
*   **The handle.** `resizable = true` puts a handle between the panes: it is dragged by finger and by pointer; while it has the focus, the arrow keys move it in steps of 16 dp, and Home and End take it to its narrowest and widest; the screen reader reads it as "List width" and the room's percentage, and can change its value. The list stays between `minListWidth` and the width that leaves `minDetailWidth` to the detail; the width is in `ListDetailState.listWidth` (`Dp.Unspecified` brings it back to the style's). Since the hinge places the panes itself, there is no handle there.
*   **They meet at the hinge.** The list is on the start side of the hinge, the detail on the other side; the room between is the hinge's `clearance`. A pixel the strip covers in part is left empty too, and a hinge in the middle leaves the two sides equal (`PantrixTwoPane` splits so too).
*   **The reading direction and motion.** In a right-to-left language the list is on the right, and the detail slides in from the left. If the system asks for less motion, the detail opens and closes without sliding. While it slides, the theme's background is painted under the detail, so that it covers the list.
*   **The room.** The scaffold takes the room given to it, and the room must be bounded in both directions: on a screen, `Modifier.fillMaxSize()`.
*   **The style.** `ListDetailScaffoldStyle`: `listWidth` 360 dp, `minListWidth` 240 dp, `minDetailWidth` 320 dp, `extraWidth` 360 dp, `gap` (if it is not given, the layout's gutter), the handle's bar and the strip it is caught by (48/44 by finger, 16 dp by pointer). A brand gives its own values with the `listDetailScaffold.*` keys.
*   **Automation.** `ListDetailScaffold_<tagKey>`, its panes `ListDetailList_…`, `ListDetailDetail_…`, `ListDetailExtra_…`, the handle `ListDetailHandle_…`, the side sheet's scrim `ListDetailScrim_…`, the close button `ListDetailClose_…`.

On the sample app's "List and detail" page, five cases stand with their cards: an inbox, settings (with the first section open in two panes), contacts with an extra pane, files whose width is dragged, and meeting at the fold on a folding device.

### Main content and a supporting pane: `PantrixSupportingPaneScaffold`

A screen's main content and a pane that supports it: a document and its comments, a chart and its filters, a map and its results. The difference from list and detail is that the main content is always there: the supporting pane either stands beside it or opens over it.

```kotlin
val comments = rememberSupportingPaneState()             // whether it is shown beside, whether it is open over, its width

PantrixSupportingPaneScaffold(
    state = comments,
    main = { Document(onComments = { if (isSupportingShown) comments.hide() else comments.show() }) },
    supporting = {
        // Beside, the pane draws its own title and hide button; in a sheet, the title and the close button are the scaffold's.
        Comments(onHide = if (supportingPlace == SupportingPanePlace.Beside) ({ comments.hide() }) else null)
    },
    modifier = Modifier.fillMaxSize(),
    mainTitle = "Report",                                    // the panes' names for the screen reader
    supportingTitle = "Comments",                            // the sheet's title too
    presentation = SupportingPanePresentation.Sheet,         // in a narrow room a sheet (the default), or Pane: in the main content's place
    resizable = true                                         // the supporting pane's width is dragged
)
```

*   **Beside.** The scaffold asks its own box: from `Expanded` on, and if the main content will not go below `minMainWidth`, the supporting pane stands at the end, `supportingWidth` (360 dp) wide; the main content takes the rest, and back is the app's. The supporting pane is open from the start; `hide()` takes it away, and the main content takes all the room.
*   **A sheet in a narrow room.** If there is no room beside, the supporting pane is closed until `show()`. In the `Sheet` presentation it opens in a sheet over the main content, in the scaffold's own room: on a phone (`Compact`) it comes from the bottom, with `SheetStyle.margin` of scrim left above it, and closes when dragged down by its handle; in a wider room (`Medium`) it comes from the end, `supportingWidth` wide. In both, the title is `supportingTitle`, with a close button beside it; pressing the scrim, the back key, Escape and iOS's edge swipe close it. The keyboard's focus goes into the sheet at its title, stays inside while it is open, and on closing goes back to where it came from in the main content.
*   **In the main content's place in a narrow room.** In the `Pane` presentation the supporting pane slides in from the end in the main content's place, like list and detail's single pane: as the back gesture goes on it goes with the finger, and goes back to its place if it is let go halfway. The back button in the supporting pane's bar calls `hide()`; the button stands only while `supportingPlace == InPlaceOfMain`.
*   **Two halves, kept apart.** `isShownBeside` is what is shown in a wide room, `isOpenOver` what is shown in a narrow one; `isShown`, `show()` and `hide()` look at the scaffold's room at that moment. When the window narrows, the sheet does not open by itself, and does not cover the main content. A supporting pane open over the content stays beside it when the window widens. A `show()` called before the scaffold has measured its room (at the start of a screen) shows the pane wherever the room puts it.
*   **What the panes are told.** `SupportingPaneScope`: `supportingPlace` (`Beside`, `Sheet`, `InPlaceOfMain`) and `isSupportingShown`. A show and hide button's text is read from `isSupportingShown`.
*   **The fold.** A foldable held like a book makes two panes at every width, and they meet at the fold. On one set on a table, the main content is above the fold and the supporting pane below it (the controls below). The fold places the panes itself; there is no handle.
*   **The handle.** The same handle as list and detail's: it is dragged, set with the arrow keys (the arrow that points to the main content widens the supporting pane) and with Home and End, the screen reader reads it as "Supporting pane width", and the resize cursor stands over it. The width is in `SupportingPaneState.supportingWidth`.
*   **The state is kept.** The supporting pane is the same composition while it is hidden too, and while it moves between beside and a sheet: a comment typed or a filter chosen stays. A pane not shown is not placed; the keyboard and the screen reader skip it.
*   **Every pane a window, the screen reader, the reading direction, motion.** As in list and detail: each pane reads its own room as a window; `mainTitle` and `supportingTitle` are the panes' names; in a right-to-left language the supporting pane is on the left; "reduce motion" takes away the sliding and the opening.
*   **The style.** `SupportingPaneScaffoldStyle`: `supportingWidth` 360 dp, `minSupportingWidth` 240 dp, `minMainWidth` 360 dp, `gap`, the handle's bar and its strip. A brand gives them with the `supportingPaneScaffold.*` keys. The sheet's look is `SheetStyle`.
*   **Automation.** `SupportingPaneScaffold_<tagKey>`, its panes `SupportingPaneMain_…` and `SupportingPaneSupporting_…`, the handle `SupportingPaneHandle_…`, the sheet's scrim `SupportingPaneScrim_…`, the close button `SupportingPaneClose_…`.

On the sample app's "Supporting pane" page five cases stand: a document and its comments, a chart and its filters with a width that is dragged, results that open in the map's place in a narrow room, hiding and showing beside, and parting at the fold on a folding device.

### The tabletop posture: `PantrixTabletopLayout`

Content that is looked at, and controls that are touched: a video and the player's buttons, a video call and its buttons, the camera's picture and its shutter. When a foldable is set half open on a table (the hinge horizontal), what is looked at should be above and the hands below.

```kotlin
PantrixTabletopLayout(
    content = { Video(player) },
    controls = {
        // On a table the whole lower half is the controls': they can be drawn large.
        PlayerControls(player, large = arrangement == TabletopArrangement.Tabletop)
    },
    modifier = Modifier.fillMaxSize(),
    flatControls = TabletopControls.Below                    // while flat: Below (the default) or Overlay
)
```

*   **On a table.** The content fills the half above the fold, the controls the half below; the fold's strip (`clearance`) stays empty. The controls take the whole lower half, and decide themselves how to line themselves up.
*   **Like a book.** The content at the start of the fold, the controls on the other side; in a right-to-left language the content is on the right.
*   **While flat.** `Below`: the controls at the bottom at their own height, and the content takes the room left. `Overlay`: the content takes all the room, and the controls ride over its bottom (as in a video player). While flat, the controls are told the layout's whole room as a window: they choose themselves how tall to be.
*   **It looks at its own room.** It looks for the fold not in the window but where it cuts the layout's room (`rememberFoldsInNode`): a layout that stays wholly in one half is laid out flat. A hinge that separates two screens (as on a Surface Duo) makes the tabletop or the book layout while flat too: every separating hinge counts.
*   **The scope.** `TabletopScope.arrangement`: `Flat`, `Tabletop`, `Book`. The controls grow on a table, and the controls over the video draw a ground of their own.
*   **The state is kept.** While the posture changes, the two parts stay in their places in the composition, at once and with no motion: the video goes on from where it was.
*   **Every part a window, the screen reader.** Each part reads its own room as a window; each of the two is read as a group, the content first. The parts are given no names: they are two parts of a single screen.
*   **Navigation.** In the tabletop posture `PantrixScaffold` already takes the navigation to the bottom; the layout is put in the scaffold's content.
*   **Automation.** `TabletopLayout_<tagKey>`, its parts `TabletopContent_…` and `TabletopControls_…`.

On the sample app's "Tabletop" page four cases stand: a video player, a player whose controls are over the video, a video call, and parts that write their measures.

### Overlays on a fold

When a foldable is held half open, or when there is a hinge between two screens, a dialog or a popover that falls on the fold is split in two. Pantrix's overlays stay in one half of the fold; the rule is in the overlays' shared code, and the components need no parameter:

:::table
| Overlay | Like a book (the hinge upright) | On a table (the hinge horizontal) |
| :--- | :--- | :--- |
| Dialog | the middle of the half it was opened from; if that is not known, the start half | the middle of the half it was opened from; if that is not known, the lower half |
| Bottom sheet | the half it was opened from, at that half's width | the lower half; a full-screen sheet the whole lower half |
| Side sheet | the whole end half: from the fold's strip to the window's end | the lower half |
| Command palette | like a dialog | the upper half, its height limited by that half |
| Popover, menu, tooltip, a select's list | the half of the control it is tied to | the half of the control it is tied to |
| Snackbar | one half: the end half at the end corner, the start half for the others | in its place |
:::

*   **Where it was opened from.** "The half it was opened from" is the half of the place pressed last; if a key was pressed after that, the half of the control in focus. The theme's root and the overlays' roots listen for this (`pantrixTransientsRoot`); an overlay asks once as it opens, and stays in that half until it closes.
*   **Tied overlays.** One stays in the half where most of the control it is tied to stands: if it does not fit in that half, it first moves to the other side, then slides, and last shrinks and scrolls inside. It never crosses the fold.
*   **The system bars.** The content leaves only the room (the insets) of the edges it reaches. On a table, the full-screen sheet in the lower half does not go under the status bar; its corners stay round.
*   **Overlays opened in a frame.** An overlay opened inside a `PantrixWindowFrame` counts the frame as its window: the scrim covers the frame, a sheet comes from the frame's edge, the fold is the frame's fold, and pressing outside the frame is pressing the scrim. In a scaled-down frame the overlay too is drawn that much smaller. What is inside the overlay is told the frame's window: a dialog opened from a list and detail scaffold's pane reads the window's width, not the pane's.
*   **It is decided on opening.** An overlay chooses its place as it opens; if the frame under it slides or shrinks, the next opening places it by the new place. If the window itself or the posture changes, the overlay follows it.

An app that shows its own `Dialog` or `Popup` gets the same information with `rememberOverlayWindow()`:

```kotlin
val overlay = rememberOverlayWindow()
// Once, on opening: where it was opened from, and the fold that splits the window in two.
val openedFrom = remember { overlay.openedFrom() }
val fold = overlay.folds.firstOrNull { it.isSeparating }
val onTheLeft = fold != null && openedFrom != null && openedFrom.x < fold.bounds.center.x
```

The places of `OverlayWindow` are in the app window's coordinates and in pixels: a `Dialog` that fills the window places what is inside it there. `window` is the overlay's window (the frame's or the outermost theme's), `frame` the place where the frame is drawn in the window, `scale` the scale the frame is drawn at.

### A covering hinge: `Modifier.keepClearOfFolds()`

On a device with two screens, the hinge covers a strip of the screen: what is drawn under it cannot be seen. A node that runs a little over the hinge (a list a little wider than one screen, a bar along a whole edge) should keep what is inside it on the screen where most of it stands:

```kotlin
Box(Modifier.fillMaxSize()) {
    Inbox()
    // A bar along the whole bottom edge stays on one of the two screens.
    Toolbar(Modifier.align(Alignment.BottomCenter).fillMaxWidth().keepClearOfFolds())
}
```

*   Only a hinge that really covers the screen counts (`WindowFold.isOccluding`). A folding phone's fold is a single screen; what is drawn on it can be seen, and it is not kept away from.
*   The side of the node with less room, and the hinge's strip, stay empty; the node keeps its own size. If the hinge runs through the very middle of the node, the node stays on the start side: spreading the content to both sides there is the job of `PantrixTwoPane`.
*   The node is measured by the place it was last laid out in: when it is laid out elsewhere, it is measured again.

On the sample app's "Overlays on a fold" page all of these stand: a dialog opened from two corners, a popover and a menu beside the fold, sheets, the command palette and the snackbar, and `keepClearOfFolds` on a device with two screens. The examples open in the "Book" and "Table" windows, inside the card, counting the frame as the window.

### The system's vertical bar: the iPhone duo's outer screen

iOS 27.1 puts the bars upright on the iPhone Duo's outer screen: it sets aside a column along the screen's end edge, and places the status bar upright at its top; UIKit also brings a tab bar and a navigation bar's buttons down into that column. The app's content keeps away from the column through the safe area. Measured in the simulator (466 × 678 pt): `traitCollection.verticalBarEdge` tells the end edge, the column is as wide as the right safe area inset (84 pt), the status bar covers the column's top 174 pt, and the room UIKit gives a bar (`layoutRegionForBarOnEdge`) is between 174 and 644 pt. UIKit's tabs stand at the bottom of this room, in a 48 pt capsule; a button like "…" stands right under the status bar.

Pantrix gives this as a part of the window, and puts its own components there:

*   **The window:** `PantrixTheme.window.verticalBar` is a `WindowVerticalBar`: the column (`bounds`) and the bars' room (`barBounds`), in pixels and in the window's coordinates. It is null everywhere the system has no vertical bar: Android, the desktop, the web, the inner screen, before iOS 27.1, and a library built with an Xcode older than 27.1. On iOS it is read like the hinge: an invisible view behind the content reads it again when the safe area, or the traits the edge depends on, change (the bridge: the [guide to the iOS fold's cinterop](https://github.com/developersancho/pantrix-ui-cmp/blob/main/docs/guide/ios-fold-cinterop/IOS_FOLD_CINTEROP_GUIDE.md), in Turkish).
*   **In a node:** `rememberVerticalBarInNode()` gives the column in a node's own coordinates; if the node does not reach the column, it is null. The window is that of the frame the node is drawn in, or of the outermost theme: not a pane's own window.
*   **`PantrixScaffold(navigation = …)`:** a scaffold that reaches the column puts the navigation at the column's bottom, whatever its width, and calls it `ScaffoldNavigationPlacement.VerticalBar`; the app draws an upright navigation bar there. It does this only in a window that stands upright and that no hinge crosses: on the Duo's outer screen held upright. The system sets aside a column on the inner screen and on both screens turned sideways too (the status bar is upright there as well); there the scaffold leaves the column to the status bar, and the navigation stays where the width says, the buttons in the top bar. The content keeps away from the column with its inset in every case, in a preview window that does not tell the inset too. Of two nested scaffolds, the outer one holds the column. The navigation stays in the same composition while it changes its place.
*   **`PantrixTopBar`:** a top bar inside a scaffold that holds the column gives its navigation icon and its actions to the column's top, and keeps its title. In a scaffold that does not hold the column, on the inner screen and in landscape, the buttons are in the top bar. If there are two bars at the same time, as while one page slides over another, the column shows the one that came last.
*   **`PantrixNavigationBar(orientation = NavigationBarOrientation.Vertical)`:** the destinations one under the other, in a capsule of the bar's fill. Its default is icons only; `showLabels = true` writes the labels too. The name of a destination with no label is told to the screen reader and shows in a tooltip. A destination whose badge reaches out widens the capsule by as much as it reaches; the badge stays inside.

```kotlin
PantrixScaffold(
    navigation = { placement, insets ->
        when (placement) {
            ScaffoldNavigationPlacement.Bottom -> PantrixNavigationBar(tabs, where, go, insets = insets)
            ScaffoldNavigationPlacement.VerticalBar ->
                PantrixNavigationBar(tabs, where, go, orientation = NavigationBarOrientation.Vertical)
            ScaffoldNavigationPlacement.Start -> PantrixNavigationRail(tabs, where, go, insets = insets)
        }
    }
) { padding -> Screen(padding) }
```

A preview or a test sets up a screen with a column with `PantrixWindow(size, verticalBar = WindowVerticalBar(...))`. The "Cover" window on the sample app's cards is set up this way (466 × 678, with an 84 column); the app's own shell stands so on the Duo's outer screen too.

## 11. Input: a finger or a mouse? {#input-a-finger-or-a-mouse}

The window's size does not say what the app is used with: a 400 dp window can be a phone's browser, or a narrowed desktop window. The platform says it:

```kotlin
val platform = PantrixTheme.platform

platform.isTouch          // is the primary pointer a finger
platform.minTouchTarget   // the touch target; components take it from the theme themselves
```

:::table
| Where | `isTouch` | Touch target |
| :--- | :--- | :--- |
| Android | yes | 48 dp (Material) |
| iOS | yes | 44 dp (Apple HIG) |
| Desktop | no | 32 dp |
| Web, a browser on a desk | no | 32 dp |
| Web, a phone's and a tablet's browser | yes | 48 dp |
:::

The web runs on both kinds of device, and no guideline covers it; so it looks at its pointer. A finger is given the measure given on Android.

*   **The signal is the browser's.** The answer is the `pointer: coarse` media query: true if the browser's **primary** pointer is a finger. On a laptop with a touch screen, the primary pointer is the trackpad, and the target stays at 32 dp.
*   **It is live.** When a trackpad is plugged into a tablet, or the browser's device emulation is turned on and off, the theme gets the new answer; the page need not be reloaded. The answer is read in the first composition, so there is no frame drawn first with the wrong measure.
*   **A touch target is two things, and both change:** the room the component sets aside in the layout, and how far around the drawn part a finger counts as a hit.
*   **The look does not change.** In a phone's browser too the web's look is drawn; what grows is the room around it.
*   **Android and iOS keep their own measure.** It is 48 dp on a tablet with a mouse plugged in too.
*   **The desktop says it itself.** The JVM does not report that the screen is a touch screen; a touch desktop app such as a kiosk gives the platform itself.

```kotlin
// A touch desktop app
PantrixTheme(platform = PantrixPlatform.forType(PlatformType.Desktop, isTouch = true)) { … }

// A phone's browser in a test and a preview
PantrixTheme(platform = PantrixPlatform.forType(PlatformType.Web, isTouch = true)) { … }
```

For the question "is it touch", do not look at the platform's type: a phone's browser is `PlatformType.Web`, but is used by finger. Ask `isTouch`:

```kotlin
if (PantrixTheme.platform.isTouch) PullToRefreshHint() else KeyboardShortcutsHint()
```

On the web the "increase contrast" and "reduce motion" settings are read the same way, and are followed if they change while the page is open.

## 12. Layout measures: the margin, the gutter, the columns and the content's width {#layout-measures-the-margin-the-gutter-the-columns-and-the-content-s-width}

The page's four measures change with the window's class, and are read from the theme:

```kotlin
val layout = PantrixTheme.layout

Column(Modifier.padding(horizontal = layout.margin)) { … }             // the page's room at the sides
Row(horizontalArrangement = Arrangement.spacedBy(layout.gutter)) { … }  // the room between columns and panes
layout.columns                                                         // the number of the grid's columns
Column(Modifier.maxContentWidth()) { … }                               // the content at most layout.maxContentWidth
```

:::table
| Class | Width (dp) | `margin` | `gutter` | `columns` | `maxContentWidth` |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `Compact` | < 600 | 16 | 16 | 4 | unbounded |
| `Medium` | 600–839 | 24 | 16 | 8 | unbounded |
| `Expanded` | 840–1199 | 24 | 24 | 12 | unbounded |
| `Large` | 1200–1599 | 32 | 24 | 12 | 1040 |
| `ExtraLarge` | ≥ 1600 | 32 | 24 | 12 | 1040 |
:::

A phone has little width to give, a desktop window a lot: the margin and the gutter grow with the window. The values of both are steps of the spacing scale (`lg`, `xl`, `xxl`). The number of columns is Material's responsive grid; 1040 is the content's width on a wide screen.

*   **`margin`** is the room between the window's or the pane's edge and the content: the page takes its content in from the sides by it.
*   **`gutter`** is the room between a grid's columns, and between panes that stand side by side.
*   **`columns`** is how many columns the page's grid has. The page chooses how many columns a part spans; its width is given by `layout.spanWidth(available, span)`: the part's columns and the gutters between them. `available` is the room the columns share, inside the page's margins:

    ```kotlin
    BoxWithConstraints {
        val layout = PantrixTheme.layout
        val (article, links) = PantrixTheme.window.byWidth(compact = 4 to 4, medium = 5 to 3, expanded = 8 to 4)
        Row(horizontalArrangement = Arrangement.spacedBy(layout.gutter)) {
            Article(Modifier.width(layout.spanWidth(maxWidth, article)))
            Links(Modifier.width(layout.spanWidth(maxWidth, links)))
        }
    }
    ```

    Two parts side by side, with the gutter between them, fill the row exactly. There is no grid component in the library; the page builds it with `Row` and `spanWidth`.
*   **`maxContentWidth`** is the content's greatest width: a line that runs the length of a desktop window is not read. `Modifier.maxContentWidth(alignment)` keeps the content at that width, and leaves the rest of the room empty beside it; the content is aligned to the start (the default) or to the middle. In a class with no bound, the content takes all the room given. The sample app's page frame (`DemoPage`) bounds its content so.
*   **Recomposition:** what reads them is set up again when the window's class changes; it is not set up while the window changes its size within the class.
*   **The density:** the margin and the gutter are spacing, and the theme's density (`Compact`, `Spacious`) scales them together with the spacing scale. The number of columns is a number, and the greatest width a measure of the content: the density changes neither.
*   **A component's own box:** a component that behaves by its room asks for the measures of its box: `PantrixTheme.layout(box)`. A component in a narrow pane takes a phone's margin even in a wide window. If the box was handed down (`LocalPantrixWindow provides box`), `PantrixTheme.layout` already goes by the box.
*   **The library does not apply them by itself:** no component adds a margin to its content. The page takes its own content in itself; the sample app's page frame (`DemoPage`) does so.

The product gives the theme the measures of the classes it designed; those it does not give stay as they are:

```kotlin
PantrixTheme(layouts = PantrixLayouts(compact = PantrixLayout(margin = 20.dp, gutter = 12.dp))) { … }
```

The number of columns and the greatest width of a class given its own measures are the ones given too; if they are not given, they are a phone's, 4 columns and unbounded. A product that gives a wider class gives them too:

```kotlin
PantrixLayouts(large = PantrixLayout(margin = 40.dp, gutter = 24.dp, columns = 12, maxContentWidth = 1200.dp))
```

If `ExtraLarge` is not given, it takes the measures of `Large`.

## 13. Tests {#testing}

:::table
| File | What it holds |
| :--- | :--- |
| `commonTest/.../adaptive/WindowSizeClassTest.kt` | The boundaries (599.9 / 600 …), the order of the classes, an unknown size, your own breakpoints |
| `commonTest/.../adaptive/WindowPostureTest.kt` | Book, tabletop, flat and open, a two-screen hinge |
| `commonTest/.../adaptive/PantrixWindowTest.kt` | The window the theme gives, a change of size, recomposition, fixing, a nested theme |
| `commonTest/.../adaptive/WindowHelpersTest.kt` | Six helpers: the answers they give, and when they set up again |
| `commonTest/.../adaptive/PanesTest.kt` | The number of panes (width, book, tabletop, a two-screen hinge, flat and open), the frame's window, the hinge's place in a node: in a frame, in a nested frame, in a frame drawn small, in the app's window when the node changes its place |
| `iosTest/.../adaptive/UIKitFoldTest.kt` | The posture from what UIKit says: the cover screen, book, tabletop, flat and open, a region that covers the hinge, an iPhone with no hinge (with the iPhone Duo simulator's numbers) |
| `commonTest/.../adaptive/BoxWithPantrixWindowTest.kt` | The component's box: the room it tells, the breakpoints it takes from the window, when it sets up again; the breakpoints the window tells |
| `androidHostTest/.../adaptive/WindowOnAndroidTest.kt` | The window Android reports, and the hinge that comes from WindowManager |
| `commonTest/.../adaptive/LayoutColumnsTest.kt` | Each class's number of columns and greatest content width, the density not scaling them, `spanWidth` (the columns and the gutters between, a narrow room, the bounds), `Modifier.maxContentWidth` (the bound, following when the window is resized, at the start, in the middle, at the end, right to left, the product's bound, the component's box, an unbounded width) |
| `commonTest/.../adaptive/PantrixLayoutTest.kt` | Each class's margin and gutter, following when the class changes, scaling with the density, the product's own measures, the component's box |
| `commonTest/.../a11y/PantrixPlatformTest.kt` | Each platform's touch target, the touch browser, `isTouch`, following when the pointer changes, the layout and the hit |
| `commonTest/.../theme/NestedThemeTest.kt` | A nested theme: what is inherited, scaling once, a dark part and the preset's dark set, what is given being the inner one's, a theme with a preset, following when the outer one changes, a value from outside the theme not being inherited, the surrounding layout placing the content |
| `androidHostTest/.../theme/ThemeOnAndroidTest.kt` | The theme in the system's night mode; in a nested theme, the outer theme's darkness, not the system's; the test tags opened to automation once, at the outermost theme |
| `webTest/.../adaptive/WindowPostureWebTest.kt` | The hinge the browser tells: book, tabletop, a two-screen hinge, flat and open; reading again when the window and the density change; the segments read from the browser; the browser knowing the three queries; flat in a real browser |
| `webTest/.../platform/MediaQueryTest.kt` | In a real browser: the media query's answer, its changing while the page is open, the listening being let go |
| `ui/components` · `commonTest/.../TouchBrowserTest.kt` | The components in a touch browser: the look the same, the room set aside the finger's |
| `ui/components` · `commonTest/.../NestedThemeTest.kt` | The controls in a dark part: the app's platform, language and analytics; the styles in force, in a theme with a preset too |
| `ui/components` · `commonTest/.../TwoPaneTest.kt` | The two panes' share, their own size, one under the other, right to left, meeting at the hinge (vertical and horizontal, right to left), a hinge that does not separate and one outside the room, an unbounded height, an unbounded split direction, the default gap |
| `ui/components` · `commonTest/.../ListDetailScaffoldTest.kt` | The list's width bounds; one pane, back (from the dispatcher), a back gesture that goes on and one that is given up, the back left to the app in two panes; two and three panes, right to left, meeting at the hinge and the handle's absence; the extra pane standing beside, and in a sheet while it scrolls itself; the state kept from one pane to two; dragging the handle (mouse, finger), its arrows, Home and End, the screen reader's setting, right to left; the focus going with the pane shown; the state being saved |
| `ui/components` · `commonTest/.../ScaffoldTest.kt` | The navigation's place in the tabletop posture, a scaffold that fits in one half, the state kept while folding |
| `commonTest/.../adaptive/WindowVerticalBarTest.kt` | The window's vertical bar: a fixed window, the system's window following it, its absence on the tests' platforms, the column in the node's coordinates, a node that does not reach the column, the window of the frame and of the theme, before the node is laid out |
| `iosTest/.../adaptive/UIKitVerticalBarTest.kt` | The vertical bar from what UIKit says (with the numbers of the iPhone Duo's outer screen), and no bar if there are no numbers |
| `ui/components` · `commonTest/.../ScaffoldVerticalBarTest.kt` | The navigation and the buttons in the column, a scaffold with no column and one that does not reach it, the vertical bar against Start, the content's inset, a column on the left, right to left, a short scaffold, the buttons of the bar that came last, nested scaffolds, the composition kept while entering the column |
| `ui/components` · `commonTest/.../NavigationBarUprightTest.kt` | The upright bar: destinations one under the other, the name of a destination with no label, the tooltip on a long press, the labels and the capsule's padding, a badge that stays inside the capsule (left to right and right to left), icons only in the bottom bar |
| `app/shared` · `jvmTest/.../demo/AdaptiveSamplesTest.kt` | The sample app's three cases: the sign-in screen, the table component, showing and hiding ([the usage guide](/docs/guides/adaptive-usage)); on the window page, the posture's name and the panes parting at the hinge; on the list and detail page, the number of panes by the window chosen, two equal pages in a book, the message opened in one pane and the back button |
| `app/shared` · `jvmTest/.../demo/NestedThemeSamplesTest.kt` | The sample app's nested theme page: what the page takes from the app, the dark part, the tight part, a part in another language, the events going to the page, a theme inside a `Row` |
:::

```bash
./gradlew :ui:foundation:jvmTest --tests 'com.pantrixui.foundation.adaptive.*'
```

```bash
./gradlew :ui:foundation:testAndroidHostTest
```

## 14. What is not there yet {#what-is-not-there-yet}

*   The components that behave by the hinge are `PantrixScaffold` (the tabletop posture), `PantrixTwoPane`, `PantrixListDetailScaffold`, `PantrixSupportingPaneScaffold`, `PantrixTabletopLayout` (splitting at the hinge) and all the overlays (in one half of the hinge). There are overlays that choose their presentation by the window's width too: the menu that is a sheet in a narrow window (`OverlayPresentation.Adaptive`), full screen in a narrow window and a side sheet in a wide one (`SheetEdge.Adaptive`), the date controls' calendar (`DatePickerPresentation.Adaptive`). There is no list and detail scene that works together with Navigation 3.
*   Getting out of the system's vertical bar (`preferredVerticalBarBehavior = .disabled`) is not in the library: that setting belongs to a `UIViewController`, and Compose's view controller does not let it be changed. An app that wants the status bar horizontal sets it in its own view controller that wraps the Compose view. What the inner screen is told by the system (whether it has a vertical bar) was not measured yet.
*   On the desktop a touch screen is not detected, and will not be: the JVM does not report it. A touch desktop app tells the theme itself (section 11).
