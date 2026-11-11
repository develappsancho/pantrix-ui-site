---
title: A small semantics tree for iOS
description: Why, in Compose Multiplatform, a screen of many rows stalls on iOS while its accessibility tree is read, as it scrolls and as a row opens and closes, how the tree is kept small, and how it is measured. The problem is not Pantrix's own: it shows the same way on iOS in every Compose app that composes hundreds of semantics nodes on one screen at once.
---

Which of what is said here was seen on the simulator, which was read in the source of Compose Multiplatform 1.12.2, and which was not verified is written apart in section 6.

## 1. The problem {#problem}

### The symptom

*   On iOS, while something reads the screen's accessibility tree, a long screen seems to freeze as it scrolls or as a row opens and closes: stalls at short intervals, each of hundreds of milliseconds.
*   What reads the tree: VoiceOver, test drivers built on XCTest (Maestro's too), Accessibility Inspector, and tools that read the screen from its accessibility tree. Without any of them the same screen is smooth; so the developer often does not see it, and a user of VoiceOver always does.
*   On Android the same screen is smooth while a tool reads its tree too (section 6).

### The cause

On iOS Compose turns the semantics tree into UIKit's accessibility elements and keeps them up to date (`AccessibilityMediator`):

*   **Reading the tree turns syncing on.** The first read makes the sync active (`isAccessibilityActive`). Two seconds after the last read, when no element has the focus, it turns off. While VoiceOver's cursor is on an element it stays on.
*   **The whole tree at every change.** While the sync is on, at every semantics change, at every step of a scroll and of an animation, the whole tree is walked again from its root, not only the part that changed. For every node that can be an element a UIKit element is made or updated, its frame is reckoned in the window, and it is sorted among its siblings. This happens on the main thread; between two passes it waits 100 ms.
*   **The cost grows with the nodes composed.** Every node in the composition counts, not only what is on the screen: hundreds of rows in a `Column` are in the tree even off the screen.

In Pantrix's demo the Data Table page, sixteen tables in one column, held 1585 nodes; the other pages between 107 and 205. On an iPhone simulator, in a Debug build, while its tree was read, that page stalled for about 400 ms every half second.

:::sketch semantics-tree
:::

## 2. The solution {#fix}

The tree is kept small. There are three ways, from the largest effect down:

:::table
| Way | What is done | What it gains |
| :--- | :--- | :--- |
| Composing only what is seen | Long content is a `LazyColumn` or a `LazyVerticalGrid`, not a `Column` | Only the rows on the screen are in the tree; whatever the screen's length, the count of nodes is what the screen holds |
| Merging what is one thing | A row that says one thing, a name and its value, a cell, takes `Modifier.semantics(mergeDescendants = true) {}` | iOS makes no element of its own for the texts inside a merging node; a screen reader reads it as one piece too |
| Leaving out what is not to be read | A shape that is only decoration, an icon beside a text that means the same, takes `Modifier.clearAndSetSemantics {}` | That node and what is under it leave the tree |
:::

Four details:

:::table
| # | Detail | What is done |
| :--- | :--- | :--- |
| 1 | Merging does not stop the walk: iOS goes through the nodes under a merging node too, it only makes no elements of them | The largest gain is from the lazy list; merging comes on top of it |
| 2 | iOS builds a merged node's label from five texts at the most; a sixth is not in the label | What is short is merged: a line, a cell. Not the whole of a card of many texts |
| 3 | A test tag inside a merging node is not in the merged tree | A test finds it with `useUnmergedTree = true` |
| 4 | In a lazy list a row that leaves the screen leaves the composition too | What the row keeps with `rememberSaveable` comes back; what it keeps with plain `remember` starts again. So that a row that takes the focus does not stay behind a bar, the rows say the list's room (the [scroll-padding](/docs/guides/scroll-padding) guide) |
:::

## 3. With Pantrix {#with}

### 3.1. A list's page

A long page is a `PantrixLazyPageScaffold`: its heading and its header are the list's first rows, and its rows are composed as they come into view. The content takes the list's room as `contentPadding` (`LazyPageScope`); each row says it, so that a row that takes the focus by the keyboard does not stay behind a bar at the bottom:

```kotlin
PantrixLazyPageScaffold(title = "Orders") {
    val shown = Modifier.scrollPadding(contentPadding)
    items(orders, key = { it.id }) { order -> OrderRow(order, shown) }
}
```

Where a page builds its own list, `PantrixPageScaffold(scroll = PageScroll.None)` gives the same room as `contentPadding`; the rows say it the same way.

### 3.2. Data table

`PantrixDataTable` has a cell read as one node; in the list a table becomes on a narrow screen, each line of a record too, the column's name with its value. What a cell holds to press, a button, is read on its own. A table whose row is pressed (`onRowClick`) already reads the row as one button. The merged tree of the sixteen tables in Pantrix's demo:

:::table
| Window | Before | After |
| :--- | :--- | :--- |
| Phone (430 dp) | 1427 nodes | 905 nodes |
| Wide (1200 dp) | 1676 nodes | 1136 nodes |
:::

On iOS, while the tree is read, a pass of the sync on the demo's lazy page went from 159 ms down to 118 ms with it (section 6). The test tag of something put inside a cell is found with `useUnmergedTree = true`.

## 4. Without Pantrix {#without}

The same two steps in any Compose project:

```kotlin
@Composable
fun OrderLine(name: String, value: String) {
    // A name and its value: one thing for a screen reader, one element for iOS.
    Row(Modifier.semantics(mergeDescendants = true) {}) {
        Text(name)
        Text(value)
    }
}

@Composable
fun OrdersScreen(orders: List<Order>) {
    // Only the orders on the screen are composed.
    LazyColumn {
        items(orders, key = { it.id }) { order -> OrderCard(order) }
    }
}
```

## 5. Measuring {#measuring}

### 5.1. Counting the nodes in a test

A screen's count of nodes is counted in a test and held to a limit; it then does not grow by itself as the screen does:

```kotlin
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.semanticsNodeCount(useUnmergedTree: Boolean = false): Int {
    fun count(node: SemanticsNode): Int = 1 + node.children.sumOf { count(it) }
    return count(onRoot(useUnmergedTree).fetchSemanticsNode())
}

@Test
fun the_orders_screen_keeps_its_tree_small() = runComposeUiTest {
    setContent { AppTheme { OrdersScreen(orders = sampleOrders(500)) } }
    val count = semanticsNodeCount()
    assertTrue(count < 300, "the orders screen holds $count semantics nodes")
}
```

The merged tree's count is close to the elements iOS makes; the unmerged tree's (`useUnmergedTree = true`) is the nodes iOS walks.

### 5.2. With time profiler on iOS

The screen is opened and, while something reads the tree (VoiceOver on a device, a test driver or Accessibility Inspector on the simulator), a recording is taken as it scrolls:

```bash
xcrun xctrace record --template 'Time Profiler' --device <udid> --attach <pid> --time-limit 20s --output screen.trace
xcrun xctrace export --input screen.trace --xpath '/trace-toc/run[@number="1"]/data/table[@schema="potential-hangs"]' > hangs.xml
```

`potential-hangs` lists where the main thread was held long, with their starts and durations; the `time-profile` table tells which function held it how long (in Compose, `AccessibilityMediator.sync`). Kotlin/Native's Debug build is slower than its Release: a duration is told with the build it was measured in.

## 6. Verification {#verification}

### Seen

On the iPhone 18 Pro Max simulator (iOS 27.0), in the demo's Debug build, on the Data Table page, while Maestro's driver read the tree, with Time Profiler (a pass of `AccessibilityMediator.sync`, on the main thread):

:::table
| Page | A pass, median | Longest | Hangs |
| :--- | :--- | :--- | :--- |
| Before: sixteen examples in one column | 403 ms | 417 ms | 21 in 60 s |
| A lazy page | 159 ms | 216 ms | 1 in 100 s |
| A lazy page and a cell of one node | 118 ms | 172 ms | 0 in 100 s |
:::

*   The last two rows were measured with the same drive: the page was scrolled down 24 times and up 24 times, and the tree was read after each scroll. The first row was measured before the change, while the page was scrolled by hand and rows were opened and closed; the length of a pass depends on the tree that is composed, not on the drive.
*   While the tree is read, three quarters of the main thread's samples are still in `sync`: the shorter the passes, the more often they run. The cost left is Compose's (`docs/OPEN_ITEMS.md`).
*   With nothing reading the tree `sync` did not run at all: a recording driven by screenshots has not one sample of `sync` in 45 seconds.
*   On the Android emulator the same page was scrolled while `uiautomator dump` read its tree in a loop: the median frame took 16 ms, the 99th percentile 25–26 ms, the share of slow frames about 4 per cent; the same as with nothing reading it.
*   Data Table's counts of nodes (the table in 3.2) were counted on the JVM, drawing the demo's sixteen examples one by one with iOS's measures.

### Tests

*   `DataTableTest.a_cell_and_a_record_s_line_are_each_read_as_one_thing`: a table's cell holds its text, a record's line in the list holds the column's name and the value, a record whose row is pressed holds them all.
*   `PageScaffoldTest.what_takes_the_focus_in_a_list_s_page_is_shown_above_the_bar_at_its_foot`: a list's page gives its rows its room, and a row that takes the focus stands above the bottom bar.
*   `DemoPagesTest`: every page of the demo is drawn in the lazy list, each example brought onto the screen in turn, in two languages and with the measures of three platforms.

### Read in the source

Compose Multiplatform 1.12.2, the source of `ui-iosarm64`, `androidx/compose/ui/platform/Accessibility.ios.kt`:

*   `AccessibilityMediator`: the sync loop, `isAccessibilityActive`, the two seconds after the last read (`scheduleAccessibilityDisablingAndCleanup`), the 100 ms between two passes.
*   `sync()`: the walk that starts from `unmergedRootSemanticsNode`; `canBeAccessibilityElement`: a text under a merging node is no element, and to find that out the node walks up through its parents; sorting with `BeyondBoundsComparator`.
*   `MAX_TEXT_COLLECT_NODES = 5`: the count of texts that go into a merged node's label.

### Not verified

*   The Release build was not measured; the durations are its Debug build's.
*   Not tried on a real iPhone and with VoiceOver: the simulator has no VoiceOver, and a test driver read the tree.
*   The measurement of the page before and those after were not taken with the same drive (the first item of Seen).
*   On Android the page after the change was not measured again: there was no stall before either.
*   That a cell is read as one piece was not listened to with a screen reader. In the merged tree on the JVM the cell holds its texts; in Android's `uiautomator` tree the texts still show as the cell's children, and TalkBack is expected to read them with the cell, which was not tried.

## 7. Where in this repository {#repository toc="7. In the repository"}

:::table
| What | Where |
| :--- | :--- |
| The demo's lazy page | `app/shared/src/commonMain/kotlin/com/pantrixui/cmp/demo/page/ComponentPage.kt` (`ComponentPage`) |
| Data Table's cell and a record's line | `ui/components/src/commonMain/kotlin/com/pantrixui/components/data/datatable/DataTableBody.kt` (`BodyCell`), `DataTableList.kt` (`RecordLine`) |
| A list's page and its room | `ui/components/src/commonMain/kotlin/com/pantrixui/components/container/page/PageScaffold.kt` (`ListBody`), `LazyPageScope` (`PageAction.kt`) |
| Tests | `DataTableTest`, `PageScaffoldTest` (`ui/components/src/commonTest`), `DemoPagesTest` (`app/shared/src/jvmTest`) |
| The open item about the cost in Compose | `docs/OPEN_ITEMS.md`, section 3 |
:::
