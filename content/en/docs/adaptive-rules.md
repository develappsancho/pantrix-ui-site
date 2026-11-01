---
title: The rules of an adaptive interface
description: The rules by which an interface written with Pantrix fits its window; what to look at while writing a screen or a component, what changes and what stays, and what the library does today.
section: Guides
---

An interface written with Pantrix is the same code on Android, iOS, the desktop and the web. This page gives the **rules** by which that interface fits its window: what to look at while writing a screen or a component, what changes and what stays the same. Beside each rule is what the library does today.

Three documents complete each other:

:::table
| Document | What it says |
| :--- | :--- |
| This page | The rules: what should be done |
| [The adaptive interface guide](/docs/guides/adaptive) | What the library gives: how the API works |
| [Using the adaptive helpers](/docs/guides/adaptive-usage) | Worked cases: a sign-in screen, a table, showing and hiding |
:::

## 1. Principles {#principles}

1. **The window, not the device.** The layout is chosen by the window's size, not by the kind of device. iPad Split View, a desktop window and a browser tab change their size freely; a foldable that opens becomes a tablet at once. `if (platform == Android)` is not a layout decision.
2. **The width chooses the layout.** The height says whether there is room to stack things. The posture says whether the window is split in two at the hinge.
3. **A screen asks the window, a component asks its own box.** A screen asks `PantrixTheme.window`. A component does not know where it will be put: it asks the place it is given (`BoxWithPantrixWindow`).
4. **Finger or mouse: ask the input, not the platform.** A browser on a phone is a web too, and a touch kiosk is a desktop.
5. **The content is called from one place.** When the window changes, the layout around the content changes; the content stays where it is and keeps its state (the choice, the scroll, the text typed).

## 2. Window classes {#window-classes}

### Width

:::table
| Class | Width (dp) | Where it is seen | Margin | Gutter |
| :--- | :--- | :--- | :--- | :--- |
| **Compact** | < 600 | A phone, a foldable's cover screen, a narrow browser window | 16 | 16 |
| **Medium** | 600–839 | A tablet held upright, an open foldable, iPad Split View | 24 | 16 |
| **Expanded** | 840–1199 | A tablet held sideways, a phone held sideways | 24 | 24 |
| **Large** | 1200–1599 | A desktop window, a browser | 32 | 24 |
| **ExtraLarge** | ≥ 1600 | A wide desktop window, an external display | 32 | 24 |
:::

The bounds are Android's window size classes (600 / 840 / 1200 / 1600). A product can give the theme bounds of its own; the numbers of this page are then read as its numbers.

*   **Rule:** the layout is chosen by the class, not by the size itself. Whatever reads the class is built again only when the class changes.
*   **Rule:** where a class has no design of its own, the layout of the one below holds. ExtraLarge follows Large.
*   **Today:** `PantrixTheme.window.widthClass`, `byWidth(...)`, `responsive(...)`, `isNarrow`, `rememberWindowIsAtLeast(dp)`.

### Height

:::table
| Class | Height (dp) | Where it is seen |
| :--- | :--- | :--- |
| **Compact** | < 480 | A phone held sideways, a low desktop or browser window |
| **Medium** | 480–899 | A phone held upright, a tablet held sideways |
| **Expanded** | ≥ 900 | A tablet held upright, a desktop window |
:::

A phone held sideways comes out Expanded by its width (iPhone 17: 874 × 402 dp), but it has no room upward: every bar laid across takes a seventh of the screen. **The rule of a short window** (while the height is Compact):

*   The width still chooses the layout: the navigation stands at the side.
*   There is no bar fixed at the bottom; the main action is in the header.
*   There is no sheet that opens from the bottom: menus and confirmations are popovers, forms are side sheets.
*   The tabletop posture stands outside this rule (section 4).

**Today:** `heightClass`, `byHeight(...)`, `isShort`. `PantrixScaffold` puts the navigation at the side by the width, so it is at the side on a phone held sideways. The part of the rule about sheets and menus will be those components' behaviour once they are written.

## 3. Layout measures {#layout}

*   **Rule:** a page keeps its content in from the sides by `PantrixTheme.layout.margin`. The value is not chosen by hand.
*   **Rule:** the gap between columns and between panels side by side is `PantrixTheme.layout.gutter`.
*   **Rule:** density (`Compact`, `Spacious`) scales the space alone: the margin, the gutter, the padding, the height. Corners, line widths and the classes' bounds are not scaled.
*   **Rule:** a component inside a panel takes the measures of its own box, not of the window: `PantrixTheme.layout(box)`.
*   **Rule:** the page's grid is `PantrixTheme.layout.columns` columns: 4, 8 from Medium on, 12 from Expanded on. A part's width is `layout.spanWidth(available, span)`.
*   **Rule:** the content is no wider than `PantrixTheme.layout.maxContentWidth`: 1040 dp from Large on, with no limit in the narrower classes. `Modifier.maxContentWidth()` keeps the content at that width.
*   **Today:** all four measures are there: the margin, the gutter, the count of columns, the widest the content goes.

## 4. Foldable postures {#postures}

:::table
| Posture | How it is told | Rule |
| :--- | :--- | :--- |
| **Closed** (the cover screen) | The window is Compact, with no hinge | The same as a phone's layout. On opening, the same screen goes on, with the same choice and the same scroll. |
| **Open, flat** | There is a hinge, the screen is one piece | Its class's layout. There is no need to keep clear of the hinge. |
| **Book** | Half open, the hinge upright | Two panels part exactly at the hinge; nothing comes over the hinge. |
| **Tabletop** | Half open, the hinge across | The upper half is for what is looked at; controls, forms and navigation are in the lower half. |
:::

*   **Rule:** no content is put over the hinge that splits the window (`posture.separatingFold`), and no control that is touched stands close to it.
*   **Rule:** a dual-screen device's hinge splits at every angle; a single-screen foldable's splits only while it is half open.
*   **Today:** `PantrixTheme.window.posture`, `isBook`, `isTabletop`, `byPosture(...)`. `PantrixScaffold` keeps the navigation at the bottom at every width in the tabletop posture. `PantrixTwoPane` splits two panels exactly at the hinge and leaves between them the strip the hinge needs clear (`clearance`); `PantrixTheme.window.paneCount()` says how many panels to show, at least two at every width in the book posture. A layout written by hand reads the hinge in its node with `rememberFoldsInNode()`.

What tells of the hinge:

:::table
| Platform | Source | State |
| :--- | :--- | :--- |
| Android | Jetpack WindowManager (`FoldingFeature`) | Read |
| Web | The browser's Device Posture and Viewport Segments APIs (Chromium) | Read |
| iOS | UIKit, from iOS 27.1 on: reserved regions (`.division`, `.occlusion`) and `UIHingeInteraction` | Read. Flat on an older iOS. |
| Desktop | None | Flat |
:::

An app that reads the hinge itself gives the posture to the theme: `rememberPantrixWindow(posture = …)`.

## 5. Input {#input}

:::table
| Platform | Smallest touch target |
| :--- | :--- |
| Android | 48 dp |
| iOS | 44 pt |
| Web, with a mouse | 32 dp |
| Web, by touch | 48 dp |
| Desktop | 32 dp; 48 dp where the app says it is touched |
:::

*   **Rule:** a component's visible size is its platform's; the room it keeps around it is a finger's. The two are different things.
*   **Rule:** whether input is by touch is not asked of the platform: `PantrixTheme.platform.isTouch`.
*   **Rule:** hover is a help alone. Everything that shows on hover is reached by the keyboard and by touch too.
*   **Today:** on the web the pointer is followed live (`pointer: coarse`): a trackpad attached to a tablet makes the targets smaller. On the desktop a touch screen is not detected, since the JVM does not report one; a touch desktop app, such as a kiosk, tells the theme itself: `PantrixPlatform.forType(PlatformType.Desktop, isTouch = true)`.

## 6. Components {#components}

*   **Rule:** a component does not ask the window. The same table can be put in a narrow panel of a wide window; had it asked the window, it would draw a table there too and not fit.
*   **Rule:** a component learns its own place with `BoxWithPantrixWindow`, and asks its box the questions a screen asks the window (`isNarrow`, `byWidth`, `widthClass`).
*   **Rule:** the choice of a component with two views (such as a table and a list) is kept outside the component; it is not lost when the view changes.

## 7. Showing and hiding {#showing-and-hiding}

:::table
| By what | When | How |
| :--- | :--- | :--- |
| The window | Do not show it where there is no room | `window.isNarrow`, `byWidth(...)` |
| The input | Where it makes sense only by finger, or only with a mouse | `platform.isTouch` |
| The platform | Where it exists on that platform alone (such as a store link) | `platform.type` |
:::

*   **Rule:** what is hidden for want of room is not lost, but moved somewhere else: to a menu, to a second line, to a page of its own.
*   **Rule:** changing is often better than hiding: a button with a label becomes an icon in a narrow place.

## 8. Continuity and state {#continuity}

*   **Rule:** when the window grows and shrinks, when the device folds and opens, the reader stays on the same screen. The chosen item, the scroll and the unsaved text of a form are kept.
*   **Rule:** the content is not called from each branch of the layout apart. Content called apart is different content to Compose, and its state starts again.
*   **Rule:** content that has to change its place is moved with `movableContentOf`.
*   **Rule:** state that has to be kept is in `rememberSaveable` or in the ViewModel.
*   **Today:** `PantrixScaffold` calls the content from one place. On Android, `configChanges` is written in the manifest so that the Activity is not made again when the window changes (the sample app has it).

## 9. Theme {#theme}

*   **Rule:** an app has one root theme. The platform, the language, analytics and the styles are given there.
*   **Rule:** where a part of the screen is to look different (a dark panel, a dense table), a second theme is opened inside it and given only what changes: `PantrixTheme(darkTheme = true) { … }`. The inner theme takes every setting it is not given from the outer one; the dark part's colours are the preset's dark set where the look was built with a preset.
*   **Rule:** a test and a preview fix the window: `PantrixTheme(window = PantrixWindow(DpSize(…)))`. A test of a behaviour bound to a class gives its window itself; the test environment's window changes with the platform.

## 10. What changes with the size {#by-size}

The navigation components and two panels (`PantrixTwoPane`) are there; which one to use the screen chooses itself today. Once the scaffold that manages a list and its detail itself and the sheets that adapt are written, their default behaviour will be this table.

:::table
| Element | Compact | Medium | Expanded | Large and up |
| :--- | :--- | :--- | :--- | :--- |
| Navigation | A tab bar at the bottom | A rail at the side | A rail at the side | A sidebar and a top bar |
| List and detail | One panel; the detail opens over it | One wide panel | The list and the detail side by side | The list and the detail side by side |
| A form's and a detail's sheet | Full screen | A side sheet | A side sheet | A side sheet |
| A row's menu, a confirmation | A sheet from the bottom | A popover | A popover | A popover |
| The main action | Fixed at the bottom | In the header | In the header | In the header |
:::

## 11. What there is today {#today}

:::table
| Subject | State |
| :--- | :--- |
| The window, the classes, the helpers | There |
| The layout measures (margin, gutter, count of columns, widest content) | There |
| A component's own box (`BoxWithPantrixWindow`) | There |
| Touch targets, a touch browser | There |
| Posture: Android, the web and iOS (from 27.1 on) | There |
| Two panels, the count of panels, the hinge's place in a node | There |
| Scaffold: the navigation's place, the tabletop posture | There |
| Inheriting in a nested theme | There |
| Navigation components | There |
| The list and detail scaffold, the supporting pane, the tabletop layout | There |
| Sheets and menus that adapt, layers at the hinge | There |
| The system's vertical bar (the iPhone Duo's cover screen): the navigation's and the top bar's buttons in the column | There |
:::

## Sources {#sources}

*   [Adaptive layouts | Kotlin Multiplatform Documentation](https://kotlinlang.org/docs/multiplatform/compose-adaptive-layouts.html)
*   [Use window size classes | Android Developers](https://developer.android.com/develop/adaptive-apps/guides/use-window-size-classes)
*   [Make your app fold aware | Android Developers](https://developer.android.com/develop/ui/compose/layouts/adaptive/foldables/make-your-app-fold-aware)
*   [Device Posture API | MDN](https://developer.mozilla.org/en-US/docs/Web/API/Device_Posture_API)
*   [Viewport Segments API | MDN](https://developer.mozilla.org/en-US/docs/Web/API/Viewport_segments_API)
