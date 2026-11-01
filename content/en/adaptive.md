---
title: Adaptive UI
description: How a Pantrix screen fits every window, a foldable's too.
---

## The adaptive page's code {#code}

The page's code, as its sections show it.

### The window in code {#window}

```kotlin
val window = PantrixTheme.window

window.widthClass              // Compact … ExtraLarge
window.heightClass             // Compact, Medium, Expanded
window.posture.isBook          // half open, the hinge upright
window.posture.isTabletop      // half open, the hinge across
window.paneCount()             // 1, 2 or 3 side by side

PantrixTheme.layout.margin     // 16, 24 or 32dp
PantrixTheme.layout.gutter     // 16 or 24dp

val columns = window.byWidth(compact = 2, medium = 4)
val panes = window.byPosture(flat = 1, book = 2)
if (window.widthClass >= WindowWidthClass.Expanded) DetailPane()
```

### The scaffold {#scaffold}

```kotlin
PantrixScaffold(navigation = { placement, insets ->
    when (placement) {
        ScaffoldNavigationPlacement.Bottom -> PantrixNavigationBar(items, selected, onSelect, insets = insets)
        else -> PantrixNavigationRail(items, selected, onSelect, insets = insets)
    }
}) { padding -> Content(Modifier.padding(padding)) }
```

### List and detail {#list-detail}

```kotlin
val inbox = rememberListDetailState()
PantrixListDetailScaffold(
    state = inbox,
    list = { Messages(onOpen = { id -> inbox.select(id) }) },
    detail = { id -> Message(id) },
    extra = { id -> History(id) },
    resizable = true
)
```

### A supporting pane {#supporting}

```kotlin
val comments = rememberSupportingPaneState()
PantrixSupportingPaneScaffold(
    state = comments,
    main = { Document(onComments = { comments.show() }) },
    supporting = { Comments() },
    supportingTitle = stringResource(Res.string.comments),
    presentation = SupportingPanePresentation.Sheet
)
```

### The tabletop {#tabletop}

```kotlin
PantrixTabletopLayout(
    content = { Video(player) },
    controls = { PlayerControls(player, large = arrangement == TabletopArrangement.Tabletop) },
    flatControls = TabletopControls.Overlay
)
```

### Columns {#columns}

```kotlin
val layout = PantrixTheme.layout
Row(Modifier.maxContentWidth(), horizontalArrangement = Arrangement.spacedBy(layout.gutter)) {
    Article(Modifier.width(layout.spanWidth(maxWidth, span = 8)))
    Related(Modifier.width(layout.spanWidth(maxWidth, span = 4)))
}
```

### A component's own room {#own-box}

```kotlin
BoxWithPantrixWindow(modifier) { box ->
    if (box.isNarrow) OrderList(orders)
    else OrderGrid(orders, showDate = box.widthClass >= WindowWidthClass.Expanded)
}
```

### A window of its own {#frame}

```kotlin
val book = PantrixWindow(
    DpSize(951.dp, 669.dp),
    posture = WindowPosture(listOf(WindowFold(
        bounds = hinge, orientation = FoldOrientation.Vertical,
        isHalfOpened = true, isSeparating = true,
        isOccluding = false, clearance = strip
    )))
)
PantrixWindowFrame(book, Modifier.size(951.dp, 669.dp)) { InboxScreen() }
```
