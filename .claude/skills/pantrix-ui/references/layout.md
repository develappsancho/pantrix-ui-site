# Screens, navigation and adaptive layout

How a screen is laid out: the scaffold and its bars, a screen's page inside it, a screen outside it, the navigation that
moves with the width, the window a layout chooses by, the scaffolds that lay out panes, and a device that folds. The
pages `scaffold`, `page-scaffold`, `showcase-scaffold`, `top-bar`, `navigation-bar`, `navigation-rail`, `sidebar`,
`adaptive-window`, `adaptive-visibility`, `adaptive-login`, `adaptive-table`, `adaptive-two-pane`,
`adaptive-list-detail`, `adaptive-supporting-pane`, `adaptive-tabletop` and `adaptive-fold-overlays` have every example.

Contents: The scaffold · A screen inside the shell · A screen outside the shell · Navigation that moves with the
width · The window · A screen asks the window, a component asks its room · Showing, hiding and keeping state · Panes ·
A device that folds

## The scaffold

`PantrixScaffold` lays out a screen: a top bar, a bottom bar and a side bar around the content, and the snackbars over
it.

- **It fills a bounded space,** the window: a scrolling parent has no height to give it.
- **The content's padding.** The content reaches under the bars, and is given as `padding` the room each bar, or the
  window's inset, takes on its edge. A list is given it as `contentPadding`, so that it scrolls under the bars.
- **Insets.** Each bar is given the insets of the window edges it touches: its fill runs behind the system bar and its
  content stays clear of it. The keyboard is not among them: a screen with text fields adds
  `Modifier.consumeWindowInsets(padding).imePadding()` to its content.
- **Focus under the bars.** Content that scrolls under a bar says what it keeps free at its ends with
  `Modifier.scrollPadding`: what takes the focus is brought into view between the bars.
- **Snackbars.** Given `snackbars`, the scaffold shows them and provides them to everything inside it as
  `LocalPantrixSnackbars` ([overlays.md](overlays.md)).

## A screen inside the shell

An app lays its shell out once, a `PantrixScaffold` with its navigation, and each screen inside it with
`PantrixPageScaffold`. The page draws its own top bar, so that the title goes with its page as one slides over another;
the shell's scaffold is given no `topBar`.

```kotlin
import androidx.compose.runtime.Composable
import com.pantrixui.components.container.page.PageAction
import com.pantrixui.components.container.page.PageTitlePlacement
import com.pantrixui.components.container.page.PantrixPageScaffold
import com.pantrixui.foundation.icons.PantrixIcons
import org.jetbrains.compose.resources.stringResource

@Composable
fun OrdersScreen(onNewOrder: () -> Unit, onExport: () -> Unit) {
    PantrixPageScaffold(
        title = stringResource(Res.string.orders),
        actions = listOf(
            PageAction(
                key = "new",
                label = stringResource(Res.string.new_order),
                icon = PantrixIcons.Plus,
                primary = true
            ),
            PageAction(key = "export", label = stringResource(Res.string.export), icon = PantrixIcons.Download)
        ),
        onAction = { key -> if (key == "new") onNewOrder() else onExport() },
        titlePlacement = PageTitlePlacement.Adaptive,
        tagKey = "orders"
    ) {
        OrdersSummary()
    }
}
```

- **No padding to hand down.** The page reads the enclosing scaffold's padding itself (`LocalPantrixScaffoldPadding`),
  keeps the theme's page margin at its sides, ends above the keyboard and keeps a focused field clear of a bottom bar.
- **Actions are data:** `PageAction(key, label, icon, primary, enabled, destructive, tagKey)` and one `onAction(key)`.
  A wide page shows them as buttons with their labels, a Compact one as icons, and what has no room in a "…" menu;
  `trailing` holds the app's own buttons, always shown.
- **The title's place.** `PageTitlePlacement.Adaptive` writes the title, the subtitle and the actions as a heading in
  the content from Expanded on, by the page's own width; `largeTitle` gives the bar a title that collapses.
- **Headers.** `header` scrolls with the content: a breadcrumb, a notice. `stickyHeader` stays under the bar: tabs,
  filters.
- **Scroll and width.** The page scrolls. `scroll = PageScroll.None` leaves a table or a map to scroll itself, given
  `contentPadding`; a list's page is `PantrixLazyPageScaffold`, whose content is a `LazyListScope`. The page takes its
  whole width by default (`PageWidth.Full`), in a wide window too; `PageWidth.Content` keeps a page that is read to the
  theme's content width.
- **A scroll bar.** A column of the screen's own that scrolls takes `Modifier.pantrixVerticalScroll(state)` in place of
  `verticalScroll`, and a `LazyColumn` takes `Modifier.pantrixScrollBar(listState)`: Pantrix's bar, drawn over the
  content, which a mouse can drag and which shows under a finger while the content moves.
- **The window's title.** The page names the browser's tab and the desktop window after itself; the app adds its own
  name once, at its root, with `LocalPantrixWindowTitleFormat`.
- **Its key** tags the page and its parts (`PageScaffold_orders.Action_new`), puts what the app gives it inside
  `TestScope("orders")` (`orders.Button_save`) and reports a `show` event each time the page opens.

## A screen outside the shell

A screen with no navigation around it, signing in or up, a password reset, a page not found, is a
`PantrixShowcaseScaffold`: a showcase on one side, a quote, a picture or slides, and the form on the other. In a narrow
room only the form is left.

```kotlin
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pantrixui.components.container.showcase.PantrixShowcaseScaffold
import org.jetbrains.compose.resources.stringResource

@Composable
fun SignInScreen(onSignIn: () -> Unit, onSignUp: () -> Unit) {
    PantrixShowcaseScaffold(
        showcase = { Testimonial() },
        title = stringResource(Res.string.sign_in),
        topBar = {
            if (!showcaseShown) AppLogo()
            Spacer(Modifier.weight(1f))
            LanguageMenu()
        },
        footer = { SignUpInstead(onSignUp) },
        bottomBar = { if (!showcaseShown) Copyright() },
        tagKey = "sign_in"
    ) {
        SignInForm(onSignIn)
    }
}
```

- **The showcase** stands beside the form from `showcaseFrom` on, Expanded by default, by the scaffold's own width, at
  the `showcaseSide` given; narrower it is not composed at all. `showcaseBackground` fills it under the system's bars,
  `scrim` shades a picture under what is written on it, and `ShowcaseScaffoldColors` gives it a fill of its own.
- **The form's side:** `header`, the form and `footer`, together in the middle or apart (`ShowcaseFormLayout`), between
  `topBar` and `bottomBar`, rows across the side that stay as the form scrolls. Where the showcase is not shown, a line
  parts each bar from the form, and an empty bar takes no room. Each slot is told `showcaseShown`, so that the logo
  the showcase carries in a wide window goes to the top bar's start in a narrow one.
- **Frame.** From Large on it is a card on `backdrop` (`ShowcaseFrame.Adaptive`); `ShowcaseFrame.Edge` reaches the
  edges at every width. With no showcase the card is the form's width.
- **Room and state.** It stands outside the shell and keeps clear of the system's bars itself (`windowInsets`). Only
  the form's side scrolls, above the keyboard. What the form remembers stays as the layout changes.
- **The window's title, the event and the tags** are the page scaffold's: `title`, not drawn, names the window, and
  `tagKey` reports `show` and scopes what it holds.

## Navigation that moves with the width

The `PantrixScaffold` that takes `navigation` places it by its own width: along the bottom while it is narrower than
600dp, along the start edge from there on. It tells the navigation where it stands; the app draws a bar or a rail
there, from one list of destinations.

```kotlin
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.pantrixui.components.container.scaffold.PantrixScaffold
import com.pantrixui.components.container.scaffold.ScaffoldNavigationPlacement
import com.pantrixui.components.container.topbar.PantrixTopBar
import com.pantrixui.components.navigation.common.NavigationItem
import com.pantrixui.components.navigation.navigationbar.NavigationBarOrientation
import com.pantrixui.components.navigation.navigationbar.PantrixNavigationBar
import com.pantrixui.components.navigation.navigationrail.PantrixNavigationRail
import com.pantrixui.components.overlay.snackbar.rememberPantrixSnackbarState
import com.pantrixui.foundation.icons.PantrixIcons
import org.jetbrains.compose.resources.stringResource

@Composable
fun MailShell() {
    var destination by rememberSaveable { mutableStateOf("inbox") }
    val snackbars = rememberPantrixSnackbarState()
    val destinations = listOf(
        NavigationItem(key = "inbox", label = stringResource(Res.string.inbox), icon = PantrixIcons.Inbox),
        NavigationItem(key = "starred", label = stringResource(Res.string.starred), icon = PantrixIcons.Star),
        NavigationItem(key = "people", label = stringResource(Res.string.people), icon = PantrixIcons.Person)
    )
    PantrixScaffold(
        navigation = { placement, insets ->
            when (placement) {
                ScaffoldNavigationPlacement.Bottom -> PantrixNavigationBar(
                    items = destinations,
                    selected = destination,
                    onSelect = { destination = it },
                    insets = insets,
                    tagKey = "main"
                )
                ScaffoldNavigationPlacement.Start -> PantrixNavigationRail(
                    items = destinations,
                    selected = destination,
                    onSelect = { destination = it },
                    insets = insets,
                    tagKey = "main"
                )
                ScaffoldNavigationPlacement.VerticalBar -> PantrixNavigationBar(
                    items = destinations,
                    selected = destination,
                    onSelect = { destination = it },
                    orientation = NavigationBarOrientation.Vertical,
                    tagKey = "main"
                )
            }
        },
        topBar = { insets -> PantrixTopBar(title = stringResource(Res.string.mail), insets = insets, tagKey = "mail") },
        snackbars = snackbars
    ) { padding ->
        MailScreen(destination = destination, modifier = Modifier.fillMaxSize().padding(padding))
    }
}
```

- **The destinations.** `NavigationItem(key, label, icon, selectedIcon, badge, enabled, tagKey)`: the key is the same
  in every language. A bar takes three to five, a rail three to seven; `selected` is the screen's state, and a press on
  the current destination is reported too.
- **`VerticalBar`** is the column the iPhone Duo's outer screen keeps for upright bars; the bar stands there upright.
- **A sidebar.** In a wide window with groups and sections of destinations, `PantrixSidebar` is the side bar: it takes
  a rail of icons by itself where room runs short. `SidebarGroup(items, title = "Library")` heads a group with its name
  in the column, and parts it with a line in the rail.
- **The top bar.** `PantrixTopBar(title, navigationIcon, actions, …)`: its title stands at the start, and in the
  middle on iOS. `PantrixBackButton` draws each platform's back icon. A collapsing title takes a
  `rememberPantrixTopBarState()` and the content's `Modifier.topBarScroll(state)`.
- **Tabs** inside a screen are `PantrixTabs`: the row only, with `selectedIndex` and `onSelect`; the screen draws the
  selected view under it.

## The window

The theme tells every composable the window it is drawn in: `PantrixTheme.window`. It is the window, not the device:
half a split screen, a narrowed desktop window and a browser tab are as wide as they are.

- **Width classes.** `widthClass`: `Compact` under 600dp, `Medium` from 600, `Expanded` from 840, `Large` from 1200,
  `ExtraLarge` from 1600. They are in order: `PantrixTheme.window.widthClass >= WindowWidthClass.Expanded`.
- **Height classes.** `heightClass`: `Compact` under 480dp, `Medium` from 480, `Expanded` from 900. A phone on its
  side is short.
- **Yes or no.** `isNarrow` (the width is `Compact`) and `isShort` (the height is).
- **A value by class.** `window.byWidth(compact = 1, medium = 2, expanded = 3)`, `byHeight` and `byPosture` pick a
  value; a class left out takes the one below it. `responsive(compact = 16.dp, large = 40.dp)` is the same for the
  theme's window.
- **The page's measures.** `PantrixTheme.layout` is the page's `margin` (16dp on a phone, 32dp on a desktop), its
  `gutter`, its `columns` (4, 8 or 12) and its `maxContentWidth`. `Modifier.maxContentWidth()` keeps content to that
  width; `layout.spanWidth(available, span)` is how wide a part some columns wide is.
- **Recomposition.** A reader of a class is composed again only when the class changes; a reader of `size`, at every
  change.

## A screen asks the window, a component asks its room

A screen chooses its layout by the window. A component that may stand in a narrow pane of a wide window chooses by its
own room: `BoxWithPantrixWindow(modifier) { box -> … }` tells its content that room as a window, with the same
helpers (`box.isNarrow`, `box.byWidth(…)`), and composes it again only when the box's class changes.
`PantrixTheme.layout(box)` gives the margin and the gutter of the box's class.

## Showing, hiding and keeping state

- A part that shows only in some windows is a plain `if`: `if (!PantrixTheme.window.isNarrow) MarketingPanel()`.
- Where the same content stands in both layouts, it is called from one place and given what changes: called from two
  places, Compose counts it twice, and what it remembers starts from nothing when the class changes. Where it has to
  move from one parent to another, `movableContentOf` moves it with its state.
- Which platform the code runs on is `PantrixTheme.platform.type`, and whether it is used by touch
  `PantrixTheme.platform.isTouch`: neither is the window's size.
- State that has to outlive an Android activity is kept with `rememberSaveable`.

## Panes

The adaptive scaffolds choose how many panes to show by their own room and the fold, keep each pane's state as the
room changes, and handle back.

| Need | Use |
| :--- | :--- |
| A list and the detail of the item opened in it: an inbox, settings, files | `PantrixListDetailScaffold` with `rememberListDetailState()` |
| Main content and a pane that supports it: a document and its comments, a map and its results | `PantrixSupportingPaneScaffold` with `rememberSupportingPaneState()` |
| Two panes side by side or one above the other, split at a fold | `PantrixTwoPane` |
| What is looked at and what is touched, by how a foldable is held: a video and its controls | `PantrixTabletopLayout` |

```kotlin
import androidx.compose.runtime.Composable
import com.pantrixui.components.container.listdetail.PantrixListDetailScaffold
import com.pantrixui.components.container.listdetail.rememberListDetailState
import org.jetbrains.compose.resources.stringResource

@Composable
fun Inbox(mails: List<Mail>) {
    val inbox = rememberListDetailState()
    PantrixListDetailScaffold(
        state = inbox,
        list = { MailList(mails = mails, opened = inbox.selectedKey, onOpen = { id -> inbox.select(id) }) },
        detail = { id ->
            // In one pane the detail stands in the list's place: only then does it show a back button.
            MailDetail(mail = mails.first { it.id == id }, onBack = if (isSinglePane) ({ inbox.back() }) else null)
        },
        emptyDetail = { NothingOpen() },
        listTitle = stringResource(Res.string.inbox),
        detailTitle = { id -> mails.first { it.id == id }.subject },
        tagKey = "inbox"
    )
}
```

- **List and detail.** One pane in a `Compact` or `Medium` room, two from `Expanded` on, three from `Large` on where
  an `extra` pane is given. In one pane, back, Escape and the edge swipe of iOS call `back()`; in two, back is the
  app's.
- **Supporting pane.** Beside the main content from `Expanded` on; in a narrower room `show()` opens it in a sheet, or
  in the main content's place with `SupportingPanePresentation.Pane`.
- **Each pane is a window.** Inside a pane, `PantrixTheme.window` and `PantrixTheme.layout` answer for the pane.
- **`resizable = true`** puts a handle between the panes, which a keyboard and a screen reader can move too.

## A device that folds

The window's `posture` says how a foldable is held: like a book (`isBook`), on a table (`isTabletop`), or flat. The
scaffolds and `PantrixTwoPane` meet at the fold by themselves: nothing falls on it. The overlays keep to a half of the
fold with no parameter of their own. `PantrixTheme.window.paneCount()` says how many panes the window has room for;
`Modifier.keepClearOfFolds()` keeps content off the hinge of a device of two screens; `rememberFoldsInNode()` gives the
folds in a node's own coordinates, for a layout of the app's own. A preview or a test draws a folded window with
`PantrixWindowFrame(window = PantrixWindow(size, posture)) { … }`.
