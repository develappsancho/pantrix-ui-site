---
title: Uyarlanabilir arayüz
description: Bir Pantrix ekranının her pencereye, katlanan bir cihazınkine de nasıl uyduğu.
---

## Uyarlanabilir sayfanın kodu {#code}

Sayfanın kodu, bölümlerinin gösterdiği gibi.

### Koddaki pencere {#window}

```kotlin
val window = PantrixTheme.window

window.widthClass              // Compact … ExtraLarge
window.heightClass             // Compact, Medium, Expanded
window.posture.isBook          // yarı açık, menteşe dik
window.posture.isTabletop      // yarı açık, menteşe yatay
window.paneCount()             // yan yana 1, 2 ya da 3

PantrixTheme.layout.margin     // 16, 24 ya da 32dp
PantrixTheme.layout.gutter     // 16 ya da 24dp

val columns = window.byWidth(compact = 2, medium = 4)
val panes = window.byPosture(flat = 1, book = 2)
if (window.widthClass >= WindowWidthClass.Expanded) DetailPane()
```

### İskelet {#scaffold}

```kotlin
PantrixScaffold(navigation = { placement, insets ->
    when (placement) {
        ScaffoldNavigationPlacement.Bottom -> PantrixNavigationBar(items, selected, onSelect, insets = insets)
        else -> PantrixNavigationRail(items, selected, onSelect, insets = insets)
    }
}) { padding -> Content(Modifier.padding(padding)) }
```

### Liste ve ayrıntı {#list-detail}

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

### Destek bölmesi {#supporting}

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

### Masa duruşu {#tabletop}

```kotlin
PantrixTabletopLayout(
    content = { Video(player) },
    controls = { PlayerControls(player, large = arrangement == TabletopArrangement.Tabletop) },
    flatControls = TabletopControls.Overlay
)
```

### Sütunlar {#columns}

```kotlin
val layout = PantrixTheme.layout
Row(Modifier.maxContentWidth(), horizontalArrangement = Arrangement.spacedBy(layout.gutter)) {
    Article(Modifier.width(layout.spanWidth(maxWidth, span = 8)))
    Related(Modifier.width(layout.spanWidth(maxWidth, span = 4)))
}
```

### Bileşenin kendi yeri {#own-box}

```kotlin
BoxWithPantrixWindow(modifier) { box ->
    if (box.isNarrow) OrderList(orders)
    else OrderGrid(orders, showDate = box.widthClass >= WindowWidthClass.Expanded)
}
```

### Kendi başına bir pencere {#frame}

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
