---
title: Which window helper, when
description: Which window helper to use, and how, while writing a screen or a component, worked through three sample cases.
---

Every example here stands working in the sample app, and is tested with `AdaptiveSamplesTest`:

:::table
| Page | What it shows | File | Web address |
| :--- | :--- | :--- | :--- |
| Sign-in screen | Case 1: a marketing and sign-in screen | `AdaptiveLoginSamples.kt` | `#adaptive-login` |
| Table | Case 2: a table component | `AdaptiveTableSamples.kt` | `#adaptive-table` |
| Show and hide | Case 3: showing, hiding, moving | `AdaptiveVisibilitySamples.kt` | `#adaptive-visibility` |
| Window classes | The window's answer, and each helper's, at the moment | `AdaptiveWindowSamples.kt` | `#adaptive-window` |
:::

The files are under `app/shared/src/commonMain/kotlin/com/pantrixui/cmp/demo/`. The pages are in the "Adaptive" section of the sample app; each page is made of examples with a title and a description, and each example's code stands in the "Code" view beside it. The names written in quotes below are those examples' titles.

## 1. Two questions before writing {#two-questions-before-writing}

### Am I writing a screen or a component?

*   **A screen** (a page, a flow): it uses the whole window, and asks the window. `PantrixTheme.window` and `responsive(...)`.
*   **A component** (a table, a card, a form, a panel): where it will be put is not known. In a wide window it can stand inside a narrow panel too. It looks not at the window but at **the room given to it**: `BoxWithPantrixWindow` gives that room as a window. All the helpers work on that box too (section 4).

For a screen that does not cover the whole window (if there is a wide sidebar beside it, or it opens inside a panel), the window's class shows the room left to it wider than it is. Such a screen too behaves like a component: it measures its own room.

### Am I looking at the window, the platform or the input?

*   **"Is there room?"** is the window's question: `isNarrow`, `isShort`, `widthClass`. It gives the same answer on a phone and in a narrowed desktop window. What is meant by "the mobile view" is most often this.
*   **"Does it make sense on this platform?"** is the platform's question: `PantrixTheme.platform.type`. A "download the app" banner makes sense only on the web, a "rate in the store" link only on Android and iOS; it does not depend on the window's size.
*   **"Is it used with a finger or with a mouse?"** is the input's question: `PantrixTheme.platform.isTouch`. A pull-to-refresh hint is for the finger, a keyboard shortcut hint for the mouse. The platform's type does not say this: the phone's browser is the web too.

:::sketch adaptive-usage
:::

## 2. Which helper, when {#which-helper-when}

:::table
| Need | Write | Example |
| :--- | :--- | :--- |
| A **value** by width: the number of columns, a spacing, a share, a text style | `window.byWidth(...)` | `window.byWidth(compact = 2, medium = 4)` |
| The same, inside a composable, with no window at hand | `responsive(...)` | `responsive(compact = 1, medium = 2, large = 3)` |
| The page's margin | `PantrixTheme.layout.margin` | `Modifier.padding(horizontal = PantrixTheme.layout.margin)` |
| The room between columns and panes | `PantrixTheme.layout.gutter` | `Arrangement.spacedBy(PantrixTheme.layout.gutter)` |
| A value by height | `window.byHeight(...)` | `window.byHeight(compact = 44.dp, medium = 56.dp)` |
| A value by a foldable's posture | `window.byPosture(...)` | `window.byPosture(flat = 1, book = 2, tabletop = 2)` |
| "Is it the phone layout?" | `window.isNarrow` | `if (window.isNarrow) BottomBar()` |
| "Does a bar fit at the bottom?" (a landscape phone) | `window.isShort` | `if (window.isShort) HeaderAction() else BottomBar()` |
| "From this class on" | `widthClass >= …` | `if (window.widthClass >= WindowWidthClass.Expanded) DetailPane()` |
| "Between these two classes" | `widthClass in a..b` | `window.widthClass in WindowWidthClass.Medium..WindowWidthClass.Expanded` |
| A width threshold that fits none of the classes | `rememberWindowIsAtLeast(dp)` | `val threeColumns = rememberWindowIsAtLeast(1000.dp)` |
| A decision given once and kept | `window.isSizeKnown` | `if (window.isSizeKnown) App()` |
| The component's own room | `BoxWithPantrixWindow { box -> … }` | `BoxWithPantrixWindow(modifier) { box -> if (box.isNarrow) List() else Table() }` |
| A size classed by hand | `PantrixWindow(size, breakpoints = …)` | `PantrixWindow(paneSize, breakpoints = window.breakpoints)` |
| Another window for a part of the screen | `LocalPantrixWindow provides …` | Section 5 |
| A part of a platform's own | `PantrixTheme.platform.type` | `if (platform == PlatformType.Web) DownloadBanner()` |
| A part for the finger or for the mouse | `PantrixTheme.platform.isTouch` | `if (platform.isTouch) PullToRefreshHint()` |
| A modifier under a condition | `Modifier.thenIf(...)` | `Modifier.thenIf(window.isNarrow) { fillMaxWidth() }` |
:::

The short rule: if a **value** is needed, the `by…` family; if a **yes or no** is needed, `is…` or a comparison of classes.

### One by one

*   **`byWidth(compact, medium, expanded, large, extraLarge)`**: the one used most. A class not given takes the value of the one below it; only the classes where the layout changes are written. The value can be of any type: a number, a `Dp`, a `TextStyle`, a share (`Float`), an enum.
*   **`byHeight(compact, medium, expanded)`**: where height is scarce. Its most frequent use is for a landscape phone: the spacing of rows, a bar's height, a frame's height.
*   **`byPosture(flat, book, tabletop)`**: a different value only on foldables. A posture not given takes the value of `flat`, so on devices that do not fold nothing changes.
*   **`isNarrow`**: the width class is `Compact`. The question of the "one pane, the navigation at the bottom" layout.
*   **`isShort`**: the height class is `Compact`. The window is most often wide (`Medium` or `Expanded`), but there is no room to line bars up at the top and the bottom.
*   **`responsive(...)`**: the same thing as `PantrixTheme.window.byWidth(...)`, in one word. Use it in a composable that needs a single value, where you do not want to write `val window = PantrixTheme.window`. It looks only at the width, and is called only inside a composable.
*   **`rememberWindowIsAtLeast(width)`**: a screen's own threshold that fits none of 600 / 840 / 1200 / 1600. Do not write `window.size.width >= 1000.dp`: that sets up again at every size, this only when the threshold is crossed.
*   **`PantrixTheme.layout`**: the page's two measures come ready, and are not chosen by hand. `margin` is the page's room at the sides (16, 24 from 600 dp, 32 dp from 1200 dp); `gutter` is the room between columns and between panes side by side (16, 24 dp from 840 dp). A component asks for its own box with `PantrixTheme.layout(box)`.
*   **`BoxWithPantrixWindow { box -> … }`**: the component's own room. The box tells the room given to its content as a window; everything above works on `box` too. The breakpoints are the window's, and the content is set up again when the class it reads changes (section 4).
*   **`isSizeKnown`**: on the desktop and the web the content is first set up before the window's size has come, and at that moment the window reads `Compact`. Nothing is on the screen, but a decision given and kept in that first composition (the start page, a layout chosen inside `remember`) has been given by the wrong class. If you have such a decision, tie the content to `isSizeKnown`.

### Where in the sample app

:::table
| Helper | Where it is used |
| :--- | :--- |
| `byWidth` | Sign-in screen: the marketing panel's share and the title's text style. Window classes: "Number of columns by class", "What a layout would pick in this window" |
| `byHeight` | Sign-in screen: the frame's height, the form's row spacing. Window classes: "A value by height" |
| `byPosture` | Window classes: "Fold posture" and "What a layout would pick in this window" |
| `isNarrow` | Sign-in screen: the marketing panel. Table: the list or the table (the box is asked). Show and hide: what is shown by the window, the button, the counters. Window classes: "Is it narrow, is it short?" |
| `isShort` | Sign-in screen: the marketing panel's benefits. Show and hide: "By window". Table: "Width from the box, height from the window" |
| `isSizeKnown` | Window classes: "The window's size" |
| `responsive` | Sign-in screen: the inner padding of the marketing panel and of the form. Window classes: "The short form: responsive" |
| `PantrixTheme.layout.margin` | Every page's room at the sides and the title's alignment (`DemoPage`, `DemoTopBar`). Window classes: "Layout measures" |
| `PantrixTheme.layout.gutter` | Window classes: the room between the boxes in "Number of columns by class", and "Layout measures". Table: "The box's measures" |
| `rememberWindowIsAtLeast` | Window classes: "A threshold that fits none of the classes" |
| A comparison of classes and a range | Table: the date column. Show and hide: "By window" |
| `BoxWithPantrixWindow` | Table: the component's own box, and the page's other examples |
| `PantrixWindow(size, breakpoints)` | The window picker on the example cards (with the real window's breakpoints). Show and hide: "A window of its own for a part". Table: "The box's breakpoints" |
| `LocalPantrixWindow` | The window picker on the example cards. Show and hide: "A window of its own for a part". Table: "Handing the box to its content as a window" |
| `PantrixTheme.platform.type` | Show and hide: "By platform" |
| `PantrixTheme.platform.isTouch` | Show and hide: "By input" |
| `Modifier.thenIf` | Show and hide: "The same component, a value that changes" |
| `movableContentOf` | Show and hide: "The same content, another arrangement" |
:::

## 3. Sample case 1: a marketing and sign-in screen {#sample-case-1-a-marketing-and-sign-in-screen}

Marketing on the left, the sign-in form on the right. In a narrow window the marketing goes, and the form stays.

```
Narrow (portrait phone) Wide                              Short (landscape phone)
┌──────────────┐        ┌───────────────┬──────────────┐  ┌──────────────┬─────────────┐
│  Sign in     │        │ Get on the    │  Sign in     │  │ Get on the   │ Sign in     │
│  [Email    ] │        │ same page as  │  [Email    ] │  │ same page as │ [Email    ] │
│  [Password ] │        │ your team     │  [Password ] │  │ your team    │ [Password ] │
│  [Sign in  ] │        │ ✓ stock       │  [Sign in  ] │  └──────────────┴─────────────┘
└──────────────┘        │ ✓ invoices    │              │    no benefits, the form scrolls
                        └───────────────┴──────────────┘
```

This is a screen: it asks the window.

```kotlin
@Composable
fun SignInScreen(modifier: Modifier = Modifier) {
    val window = PantrixTheme.window
    Row(modifier = modifier.fillMaxSize()) {
        // An extra that comes and goes, with nothing to remember: a plain `if` is enough.
        if (!window.isNarrow) {
            MarketingPanel(
                modifier = Modifier
                    // A value by class: from Large on, the marketing panel's share grows.
                    .weight(window.byWidth(compact = 1f, large = 1.25f))
                    .fillMaxHeight()
            )
        }
        // The form is the same call in every layout.
        SignInForm(modifier = Modifier.weight(1f).fillMaxHeight())
    }
}
```

The inside of the marketing panel:

```kotlin
Column(
    modifier = Modifier
        .fillMaxSize()
        // A single value, with no window at hand.
        .padding(responsive(compact = 16.dp, medium = 24.dp, large = 40.dp))
) {
    PantrixText(
        text = "Get on the same page as your team",
        // The text style too is a value chosen by class.
        style = window.byWidth(compact = typography.title, large = typography.headline)
    )
    // A landscape phone: the width is enough, the height is not. The benefits go.
    if (!window.isShort) {
        Benefits()
    }
}
```

The form's row spacing and the screen's height are chosen by height:

```kotlin
verticalArrangement = Arrangement.spacedBy(window.byHeight(compact = spacing.sm, medium = spacing.md))
```

:::table
| Used | For what |
| :--- | :--- |
| `isNarrow` | Whether the marketing panel is shown |
| `byWidth` | The marketing panel's share, the title's text style |
| `responsive` | The inner padding |
| `isShort` | Taking the benefits away on a landscape phone |
| `byHeight` | The row spacing, the screen's height |
:::

The form does not widen: with `widthIn(max = 360.dp)` it is at most 360 dp, and stands in the middle of its own half. Not everything is solved by class; for content that is read and filled in, an upper width is often enough.

### This example's real lesson: the form is called from one place

Compose ties what a composable remembers to **the place it is called from**. Above, `SignInForm()` is the same line in every layout; while the marketing panel comes and goes, the text typed into the email field stays where it is. Written like this, it would be lost:

```kotlin
// WRONG: the form is called from two separate places.
if (window.isNarrow) {
    SignInForm()
} else {
    Row {
        MarketingPanel()
        SignInForm()      // another call site: when the window widens, the form starts from zero
    }
}
```

When the user turns the phone or makes the window bigger, the email they typed is deleted. The test `what_is_typed_stays_as_the_window_changes_its_class` holds the right way of writing; when the form is called from two places, it turns red.

The form's fields remember with `rememberSaveable`: when Android re-creates the Activity too (a switch to the dark theme, a change of language), what was typed comes back, because the call site stayed the same (section 6).

## 4. Sample case 2: a table component {#sample-case-2-a-table-component}

A plain table while there is room, a list in a narrow room.

```
Narrow room (< 600 dp)           Wide room (≥ 600 dp); from 840 dp on, the date column comes too
┌───────────────────────────┐    ┌────────┬──────────────┬──────────┬────────┐
│ Ayşe Yılmaz      1.240 TL │    │ Order  │ Customer     │ Status   │  Total │
│ #1042 · 02.10 [Preparing] │    ├────────┼──────────────┼──────────┼────────┤
├───────────────────────────┤    │ #1042  │ Ayşe Yılmaz  │ Preparing│  1.240 │
│ Mehmet Demir       380 TL │    │ #1041  │ Mehmet Demir │ Shipped  │    380 │
│ #1041 · 02.10   [Shipped] │    └────────┴──────────────┴──────────┴────────┘
└───────────────────────────┘
```

This is a component: **it looks not at the window but at its own room.** The same table can be put inside a narrow panel in a wide window; had it asked the window, it would draw a table there too, and would not fit. On the sample page the same component stands twice, once at the page's width and once in a 320 dp box: in a wide window the upper one is a table and the lower one a list.

```kotlin
@Composable
fun OrdersTable(
    orders: List<Order>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // The component's own box, like a window: all the helpers work on it.
    BoxWithPantrixWindow(modifier = modifier) { box ->
        if (box.isNarrow) {
            OrderList(orders, selectedId, onSelect)
        } else {
            // The date column only in a room wider than 840 dp.
            OrderGrid(orders, selectedId, onSelect, showDate = box.widthClass >= WindowWidthClass.Expanded)
        }
    }
}
```

`BoxWithPantrixWindow` is a box built on top of `BoxWithConstraints`: it gives its content the most room it can take, as a `PantrixWindow`.

*   `box.isNarrow`, `box.byWidth(...)`, `box.widthClass >= …`: everything a screen asks the window is asked of the box.
*   **The breakpoints are the window's.** If the product classes the window by its own breakpoints (`rememberPantrixWindow(breakpoints = …)`), the box is classed by them too; nothing needs to be written. If the component has thresholds of its own, they are given with the `breakpoints` parameter.
*   **The content is not set up again at every size.** The content of `BoxWithConstraints` is set up again every time the box changes by a pixel; the content here is set up when the class it reads changes. The table above is set up again when the box's width class changes: at four boundaries, not at every pixel.
*   The first example of the "Table" page shows the difference ("The same component, in two places"): the same table stands once at the page's width and once more in a 320 dp box. The window is the same for both; the one in the box is a list however wide the window is. Had it asked the window, it would line up four columns in 320 dp.
*   Inside a scrolling page the box's height is unbounded (`Dp.Infinity`): the height class reads `Expanded`, and `box.isShort` is never `true`. The component asks the box for the width and the window for the height.
*   The box is flat: the hinge is the window's. A component that is to decide by the posture asks `PantrixTheme.window.posture`.
*   `PantrixTheme.window` and `responsive(...)` ask the window inside the box too. If you want everything inside to ask the box, give them the box: `CompositionLocalProvider(LocalPantrixWindow provides box) { … }`.
*   Like `BoxWithConstraints`, it sets up its content while it is being measured: it is not put inside a row or a column sized with `IntrinsicSize`.

The same thing is written by hand too; this is what the box does, and on top of that it takes the breakpoints from the window and does not set up its content again needlessly:

```kotlin
BoxWithConstraints(modifier) {
    val box = PantrixWindow(DpSize(maxWidth, maxHeight), breakpoints = PantrixTheme.window.breakpoints)
    …
}
```

### The selection stays outside the component

The table and the list are two separate views, and different call sites. Had the selected row been remembered inside them, it would be lost when the view changes. The selection comes from outside, and is reported outside:

```kotlin
var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
OrdersTable(orders = orders, selectedId = selectedId, onSelect = { selectedId = it })
```

The rule: **state that must live across two views is kept above the two views.** On the sample page, pressing a card in the list shows the same order selected in the table above too.

### If the table always fills the window

If the component is to be used only at the window's full width, asking the window works too, and is shorter:

```kotlin
if (PantrixTheme.window.isNarrow) OrderList(…) else OrderGrid(…)
```

The price is that, on the day the component is put inside a panel or beside a side menu, it draws the wrong view. In a component that will be reused, measure the box.

## 5. Sample case 3: showing and hiding {#sample-case-3-showing-and-hiding}

A part that shows only on mobile, or only on the desktop and the web.

### By window

A plain `if` is enough:

```kotlin
val window = PantrixTheme.window

if (window.isNarrow) StickyAddToCartBar()                                  // only in a narrow window
if (!window.isNarrow) HeaderAction()                                       // in every window that is not narrow
if (window.widthClass >= WindowWidthClass.Expanded) DetailPane()           // from Expanded on
if (window.widthClass in WindowWidthClass.Medium..WindowWidthClass.Expanded) LabelledRail()
if (window.isShort) CompactTopBar()                                        // on a landscape phone
```

:::table
| Window | What shows |
| :--- | :--- |
| A portrait phone (402 × 874) | only narrow |
| A portrait tablet (820 × 1180) | not narrow, Medium–Expanded |
| A landscape phone (874 × 402) | not narrow, from Expanded on, Medium–Expanded, only short |
| The desktop (1280 × 800) | not narrow, from Expanded on |
:::

### By platform

```kotlin
val platform = PantrixTheme.platform.type

if (platform == PlatformType.Web) DownloadAppBanner()
if (platform == PlatformType.Android || platform == PlatformType.Ios) RateInStoreLink()
```

A wide Android tablet is Android too; a narrowed browser window is the web too. When you say "let it show on mobile", decide which you mean: most often the window, sometimes the platform.

### By input

Whether it is used with a finger or with a mouse, the platform's type does not say. Android and iOS are always used by finger, the desktop always by mouse; the web can be either.

```kotlin
val platform = PantrixTheme.platform

if (platform.isTouch) PullToRefreshHint()          // Android, iOS, the phone's and the tablet's browser
if (!platform.isTouch) KeyboardShortcutsHint()     // the desktop, a browser on a desk
```

:::table
| Where | `platform.type` | `platform.isTouch` | Touch target |
| :--- | :--- | :--- | :--- |
| An Android phone and tablet | `Android` | yes | 48 dp |
| An iPhone and an iPad | `Ios` | yes | 44 dp |
| The desktop app | `Desktop` | no | 32 dp |
| A browser on a desk | `Web` | no | 32 dp |
| A browser on a phone | `Web` | yes | 48 dp |
:::

The components take the touch target from the theme themselves; nothing needs to be done while a screen is being written. In the browser the answer is live: it changes when a trackpad is plugged into a tablet. Its details are in section 11 of the [adaptive guide](/docs/guides/adaptive).

### Changing instead of hiding

If the same component is in both layouts, do not hide it and call it again elsewhere; change the value given to it:

```kotlin
PantrixButton(
    text = "Continue",
    onClick = onContinue,
    // In a narrow window it fills the row; in a wide one it is its own width.
    modifier = Modifier.thenIf(window.isNarrow) { fillMaxWidth() }
)
```

### The state of a hidden part

A part inside an `if` leaves the composition when it is hidden: it takes no room, is not read, and **lets go of what it remembers too**. For an extra (a hint, a banner, a second button) this is no problem. For the same content it is:

```kotlin
// WRONG: two separate counters. When the class changes, the count resets.
if (window.isNarrow) Counter(label = "narrow") else Counter(label = "wide")

// RIGHT: one counter, a label that changes. The count is kept.
Counter(label = if (window.isNarrow) "narrow" else "wide")
```

### If the content's place must change: move it

For content that stands one under the other in a narrow window and side by side in a wide one, a single call site is not enough: `Column` and `Row` are two separate places. `movableContentOf` **moves** the content; the same composition goes to its new place, and what it remembers stays with it:

```kotlin
// Set up once and remembered; when the place it is called from changes, the content is moved there.
val counters = remember {
    movableContentOf {
        Counter(label = "Moved A")
        Counter(label = "Moved B")
    }
}
if (window.isNarrow) {
    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) { counters() }
} else {
    Row(horizontalArrangement = Arrangement.spacedBy(spacing.md)) { counters() }
}
```

`movableContentOf` is in the `androidx.compose.runtime` package, Compose's own tool. Writing the same lines with a plain lambda resets the counts; the test `content_moved_from_a_column_into_a_row_keeps_its_count` holds this.

Moved content too remembers for as long as the composition lives. When Android re-creates the Activity, only what it saved with `rememberSaveable` is left of it (section 6).

### The window picker on the example card

On all four pages, the card of every example that reads the window has a window picker (Real / Narrow / Medium / Short / Wide), which gives that example its own window; the other examples stay in their own windows. Window classes › "Fold posture" has no picker: the fixed windows are flat, and the posture is read only from the device. "Narrow" is a 360 × 800 dp phone, "Medium" a 768 × 1024 dp upright tablet, "Short" an 880 × 400 dp landscape phone, "Wide" a 1280 × 800 dp desktop window; the four fall into four separate width classes (`Compact`, `Medium`, `Expanded`, `Large`). "Real" is the app's own; the "Now" line under the picker writes the classes of the window given to the example. The example is set up at the width of the window chosen. A window that does not fit the card is not squeezed: it is drawn whole, scaled down, and the line says the scale; if it would have to be drawn smaller than 60%, it stays at its own size, its frame scrolls sideways and a scroll bar comes up under it. The look of a wide window on a phone, and of a narrow window on the desktop, is seen without touching the window. In the example "What happens to state when a part is hidden", after the counters are raised and the picker is switched between "Narrow" and "Wide", the one called "from two places" resets and the one called "from one place" stays; in "The same content, another arrangement", the moved ones stay too.

```kotlin
// A fixed window is classed by the real one's breakpoints.
val wide = PantrixWindow(DpSize(1280.dp, 800.dp), breakpoints = PantrixTheme.window.breakpoints)
CompositionLocalProvider(LocalPantrixWindow provides wide) {
    Examples()   // every `PantrixTheme.window` and `responsive(...)` inside asks this window
}
```

The same technique is used to set up a preview or a test at a given size. The platform and the input are not affected by it: the examples "By platform" and "By input" have no picker for this reason.

## 6. Keeping the state {#keeping-the-state}

When the window changes its class, the layout changes. What the content remembers (the text typed, the selection, a counter) is kept in one of three ways:

:::table
| Way | When | Example |
| :--- | :--- | :--- |
| **One call site**: the content is always called from the same line; what changes is the value given to it | If the structure stays the same: something comes and goes beside it, its size or its label changes | The sign-in form, the counter "from one place" |
| **Keeping the state above**: the state stands above the two views and comes down to them by parameter | If two separate views show the same data | The selected order of the table and of the list |
| **Moving**: `movableContentOf` | If the same content must pass into another parent | The counters that stand one under the other and side by side |
:::

Try the first one first; it is the cheapest, and nothing is moved. In the second, what the views remember inside themselves (like the scroll position) still resets: only the state kept above is kept.

### Android: what is left when the activity is re-created

On the desktop, the web and iOS, the composition goes on living when the window's size changes; the three ways above are enough. On Android, rotation, folding and a change of size in multi-window are each a configuration change: unless the app takes them over, the Activity is re-created together with the composition, and only the **saved** state is left.

**The sample app takes the window changes over.** The line in its manifest is this:

```xml
android:configChanges="orientation|screenSize|smallestScreenSize|screenLayout|density"
```

With this line Android behaves like the other platforms: when the device folds or turns, the Activity lives, `PantrixTheme.window` tells the new class, and the content is only set up again. On the changes not in the list (the dark theme, the language, the font size, the keyboard), and when the system kills the process and brings it back, the Activity is still re-created; the dark theme was left out on purpose, because `enableEdgeToEdge` chooses the colour of the system bars' icons in `onCreate`.

:::table
| Where the state is | The class changes while the composition lives | The Activity is re-created |
| :--- | :--- | :--- |
| `remember`, one call site | Kept | **Lost** |
| `rememberSaveable`, one call site | Kept | Kept |
| `rememberSaveable`, two call sites | Reset | Reset |
| `remember` inside `movableContentOf` | Kept | **Lost** |
| `rememberSaveable` inside `movableContentOf` | Kept | Kept |
:::

*   Keep the state that must stay with `rememberSaveable` (or above the screen, in a ViewModel): even with the window changes taken over, a change of theme or of language and the death of the process re-create the Activity. The call site rule holds for it too: the saved value comes back by the place it is called from.
*   Each of the two call sites has its own record. In an app that does not take the window changes over, when the device is folded and opened, the counter "from two places" shows 2 → 0 → 2: the narrow layout's counter and the wide layout's are two separate counters. While the composition lives, the same counter shows 2 → 0 → 0.
*   The form, the table's selection, the window picker and the counters on the sample pages therefore use `rememberSaveable`.
*   Both columns of the table were looked at on a foldable emulator on 3 October 2026: the right-hand column by folding and opening the device before the manifest line was added, and then by switching to the dark theme; the left-hand one by folding and opening, and by rotating, after the line was added.

## 7. Pitfalls {#pitfalls}

:::table
| Pitfall | What happens | The right way |
| :--- | :--- | :--- |
| Calling the same content from two places by class | When the class changes, the state (the text typed, the selection, the scroll) resets | Call it from one place, and give what changes by parameter; if its place is to change, move it |
| Keeping the shared state of two views (table / list) inside them | It is lost when the view changes | Keep the state above the two |
| Keeping state that must stay with `remember` | It is lost when Android re-creates the Activity: a change of theme or of language, the death of the process; in an app that does not take the window changes over, rotation and folding too | `rememberSaveable` or a ViewModel |
| Asking the window in a component | It draws the wide layout in a narrow panel | Ask its own box, with `BoxWithPantrixWindow` |
| Forgetting the breakpoints while building the box by hand: `PantrixWindow(DpSize(maxWidth, maxHeight))` | The product's own breakpoints do not reach the component | `BoxWithPantrixWindow`, or `breakpoints = window.breakpoints` |
| Choosing the page margin by hand: `responsive(16.dp, 24.dp, 32.dp)` | Each screen chooses its own value; the product's measure does not change from one place | `PantrixTheme.layout.margin` |
| Reading `window.size` for a layout decision | It is set up again at every pixel | Read the class; for a threshold of its own, `rememberWindowIsAtLeast` |
| Keeping the class with `remember` | On the desktop and the web, the first composition's `Compact` stays | Read it from the window every time; if it is to be kept, wait for `isSizeKnown` |
| Looking at the platform for "mobile" | A narrowed desktop window stays in the desktop layout | For room look at the window, and for a thing of a platform's own at the platform |
| Looking at the platform's type for "touch" | The phone's browser is the web: what is for the finger does not show there | `PantrixTheme.platform.isTouch` |
| Taking `isNarrow` for "a phone" | A landscape phone is not `isNarrow` (874 dp, `Expanded`) | `isShort` for a landscape phone |
| Asking `isShort` of the box on a scrolling page | Always `false`: the box's height is unbounded | Ask the window for the height |
:::

## 8. Testing {#testing}

The test's own window is always the same size (1024 × 768 in Skiko, that is, `Expanded`). To test a layout in a given window, give the window to the theme; if the window is a state, it can be changed within the test:

```kotlin
var window by mutableStateOf(PantrixWindow(DpSize(402.dp, 874.dp)))
setContent {
    PantrixTheme(darkTheme = false, window = window) { SignInScreen(Modifier.fillMaxSize()) }
}
onNodeWithTag("TextField_sign_in_email").performTextInput("ada@pantrix.io")

window = PantrixWindow(DpSize(1280.dp, 800.dp))        // the window widened
waitForIdle()
onNodeWithTag("TextField_sign_in_email").assertTextContains("ada@pantrix.io")
```

A component that looks at its own room is given a box, not a window:

```kotlin
var width by mutableStateOf(320.dp)
setContent {
    PantrixTheme(darkTheme = false) {
        Box(Modifier.width(width)) { OrdersTable(ORDERS, selectedId = null, onSelect = {}) }
    }
}
// 320 dp: the list. Then `width = 700.dp`: the table, with no date column. `width = 900.dp`: the date column too.
```

That the product's own breakpoints reach the component is tested this way too: the window is given with those breakpoints, and the box stays the same.

```kotlin
// If Medium starts at 300 dp, the 320 dp box is a table, not a list.
val window = PantrixWindow(DpSize(1280.dp, 800.dp), breakpoints = PantrixBreakpoints(medium = 300.dp))
PantrixTheme(darkTheme = false, window = window) {
    Box(Modifier.width(320.dp)) { OrdersTable(ORDERS, selectedId = null, onSelect = {}) }
}
```

For what is shown by platform and by input, the platform too is given to the theme:

```kotlin
PantrixTheme(darkTheme = false, platform = PantrixPlatform.forType(PlatformType.Android)) { … }

// A browser on a phone: the platform is the web, the use is by finger
PantrixTheme(darkTheme = false, platform = PantrixPlatform.forType(PlatformType.Web, isTouch = true)) { … }
```

Android's re-creating the Activity is seen in a JVM test too: the content is saved with the test's own `SaveableStateRegistry`, taken down, and set up again from what was saved. `SavedScreen` in `AdaptiveSamplesTest` does this (Compose's `StateRestorationTester` does not work outside Android):

```kotlin
val screen = SavedScreen()
var window by mutableStateOf(DESKTOP)
setContent {
    screen.Content {
        PantrixTheme(darkTheme = false, window = window) { ComponentExamples(DemoComponent.AdaptiveVisibility) }
    }
}
// … the counters are raised …
screen.buildAgain(this) { window = PHONE }             // the re-created Activity, this time in a narrow window
onNodeWithText("From one place (narrow): 2").assertExists()   // saved, and the call site the same: it came back
onNodeWithText("From two places (narrow): 0").assertExists()  // another call site: from zero
```

The examples' tests are in the file `app/shared/src/jvmTest/kotlin/com/pantrixui/cmp/demo/AdaptiveSamplesTest.kt`:

```bash
./gradlew :app:shared:jvmTest --tests 'com.pantrixui.cmp.demo.AdaptiveSamplesTest'
```
