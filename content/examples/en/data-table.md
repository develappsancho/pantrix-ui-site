# Data Table

The site's own examples of `PantrixDataTable`, shown on its page in place of the demo's.

## Examples

### Orders

A table of orders as an app shows one: the customer with an avatar and an email, the order's status as a tag, and the total at the end of its column, so that the digits line up. Each column says how much of the width it takes: a width of its own, or a share of what the others leave (`weight`), no narrower than `minWidth`.

The table stays a table at every width, a phone's too, and scrolls sideways where its columns do not fit (`layout = DataTableLayout.Table`). Left to choose by its width, a table narrower than 600dp becomes a list of records: the last example shows that.

```kotlin
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.pantrixui.components.data.datatable.DataTableColumn
import com.pantrixui.components.data.datatable.DataTableLayout
import com.pantrixui.components.data.datatable.DataTableVariant
import com.pantrixui.components.data.datatable.PantrixDataTable
import com.pantrixui.components.data.tag.PantrixTag
import com.pantrixui.components.data.tag.TagTone
import com.pantrixui.components.elements.avatar.AvatarTone
import com.pantrixui.components.elements.avatar.PantrixAvatar
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.theme.PantrixTheme

PantrixDataTable(
    rows = ORDERS,
    columns = remember { orderColumns() },
    rowKey = { it.id },
    variant = DataTableVariant.Bordered,
    layout = DataTableLayout.Table,
    tagKey = "orders"
)

private fun orderColumns(): List<DataTableColumn<Order>> = listOf(
    DataTableColumn.text(key = "id", header = "Order", width = 96.dp) { "#${it.id}" },
    DataTableColumn(key = "customer", header = "Customer", weight = 2f, minWidth = 240.dp) { order ->
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            PantrixAvatar(contentDescription = null, initials = order.initials, tone = order.tone, size = 32.dp)
            Column {
                PantrixText(text = order.customer, style = PantrixTheme.typography.label)
                PantrixText(
                    text = order.email,
                    style = PantrixTheme.typography.footnote,
                    color = PantrixTheme.colors.textSecondary
                )
            }
        }
    },
    DataTableColumn.text(key = "date", header = "Date", width = 128.dp) { it.date },
    DataTableColumn(key = "status", header = "Status", width = 128.dp) { order ->
        PantrixTag(text = order.status.label, tone = order.status.tone, dot = true)
    },
    DataTableColumn.text(key = "total", header = "Total", width = 112.dp, alignment = Alignment.End) {
        money(it.total)
    }
)

private enum class Status(val label: String, val tone: TagTone) {
    Paid("Paid", TagTone.Success),
    Pending("Pending", TagTone.Warning),
    Refunded("Refunded", TagTone.Neutral),
    Failed("Failed", TagTone.Danger)
}

private class Order(
    val id: String,
    val customer: String,
    val email: String,
    val tone: AvatarTone,
    val date: String,
    val status: Status,
    val total: Int
) {
    val initials: String = customer.split(" ").joinToString("") { it.take(1) }
}

private val ORDERS = listOf(
    Order("3210", "Olivia Martin", "olivia.martin@mail.com", AvatarTone.Primary, "Oct 28, 2026", Status.Paid, 1999),
    Order("3209", "Jackson Lee", "jackson.lee@mail.com", AvatarTone.Info, "Oct 28, 2026", Status.Pending, 39),
    Order("3208", "Isabella Nguyen", "isabella@mail.com", AvatarTone.Success, "Oct 27, 2026", Status.Paid, 299),
    Order("3207", "William Kim", "will@mail.com", AvatarTone.Warning, "Oct 27, 2026", Status.Refunded, 99),
    Order("3206", "Sofia Davis", "sofia.davis@mail.com", AvatarTone.Danger, "Oct 26, 2026", Status.Failed, 450),
    Order("3205", "Liam Johnson", "liam.johnson@mail.com", AvatarTone.Neutral, "Oct 25, 2026", Status.Paid, 2350)
)

private fun money(amount: Int): String = "$" + amount.toString().reversed().chunked(3).joinToString(",").reversed()
```

### Sorting

A column with `sortable = true` has a header that is a button. Press Price: the products go from the cheapest up, and an arrow beside the header says so; press it again and they go from the dearest down; a third press takes the order away. The order is the screen's (`sort`): the table says which order is asked for (`onSortChange`), and `sort.comparator(columns)` puts the rows in it.

The names are ordered as the reader's language orders its letters (`rememberTextOrder`), and the stock and the price as numbers, each by a comparator of its own. With "Sort by several columns" on, a press adds its column to the order: press Category, then Price, and the products of a category go from the cheapest up.

```kotlin
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.pantrixui.components.data.datatable.DataTableColumn
import com.pantrixui.components.data.datatable.DataTableLayout
import com.pantrixui.components.data.datatable.DataTableSort
import com.pantrixui.components.data.datatable.DataTableVariant
import com.pantrixui.components.data.datatable.PantrixDataTable
import com.pantrixui.components.data.datatable.comparator
import com.pantrixui.components.data.tag.PantrixTag
import com.pantrixui.components.data.tag.TagTone
import com.pantrixui.components.elements.switchbutton.PantrixSwitchButton
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.l10n.rememberTextOrder

var sort by remember { mutableStateOf(listOf(DataTableSort("price", descending = true))) }
var several by remember { mutableStateOf(false) }
val textOrder = rememberTextOrder()
val columns = remember(textOrder) { productColumns(textOrder) }
Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    PantrixSwitchButton(
        checked = several,
        onCheckedChange = { several = it },
        label = "Sort by several columns",
        tagKey = "several"
    )
    PantrixDataTable(
        rows = remember(sort, columns) { PRODUCTS.sortedWith(sort.comparator(columns)) },
        columns = columns,
        rowKey = { it.sku },
        sort = sort,
        onSortChange = { sort = it },
        multiSort = several,
        variant = DataTableVariant.Bordered,
        layout = DataTableLayout.Table,
        tagKey = "products"
    )
}

private fun productColumns(textOrder: Comparator<String>): List<DataTableColumn<Product>> = listOf(
    DataTableColumn.text(key = "name", header = "Product", weight = 2f, sortable = true, textOrder = textOrder) {
        it.name
    },
    DataTableColumn.text(key = "category", header = "Category", sortable = true, textOrder = textOrder) {
        it.category
    },
    DataTableColumn(
        key = "stock",
        header = "Stock",
        width = 136.dp,
        alignment = Alignment.End,
        sortable = true,
        comparator = compareBy<Product> { it.stock }
    ) { product ->
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (product.stock < LOW_STOCK) PantrixTag(text = "Low", tone = TagTone.Warning)
            PantrixText(text = "${product.stock}")
        }
    },
    DataTableColumn.text(
        key = "price",
        header = "Price",
        width = 112.dp,
        alignment = Alignment.End,
        sortable = true,
        comparator = compareBy<Product> { it.price }
    ) { money(it.price) }
)

private class Product(
    val sku: String,
    val name: String,
    val category: String,
    val stock: Int,
    val price: Int
)

private val PRODUCTS = listOf(
    Product("LMP-01", "Aurora desk lamp", "Lighting", 42, 89),
    Product("CHR-07", "Nimbus office chair", "Furniture", 7, 349),
    Product("MSE-12", "Orbit wireless mouse", "Accessories", 128, 39),
    Product("MUG-03", "Ember ceramic mug", "Kitchen", 3, 18),
    Product("DSK-02", "Atlas standing desk", "Furniture", 15, 629),
    Product("SPK-05", "Echo speaker", "Audio", 64, 129),
    Product("HUB-09", "Ion USB-C hub", "Accessories", 9, 59),
    Product("LMP-04", "Lumen floor lamp", "Lighting", 21, 149)
)

private const val LOW_STOCK = 10

private fun money(amount: Int): String = "$" + amount.toString().reversed().chunked(3).joinToString(",").reversed()
```

### Choosing rows

Given `selected` and `onSelectionChange`, the table puts a box at the start of each row, and one in the header that chooses every row at once. Which rows are chosen is the screen's: here the toolbar over the header (`toolbar`) counts them, and removes them from the team.

`rowLabel` names each row to a screen reader: its box says which member it chooses. Remove a few, then bring them back with "Restore all".

```kotlin
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pantrixui.components.data.datatable.DataTableColumn
import com.pantrixui.components.data.datatable.DataTableLayout
import com.pantrixui.components.data.datatable.DataTableVariant
import com.pantrixui.components.data.datatable.PantrixDataTable
import com.pantrixui.components.data.tag.PantrixTag
import com.pantrixui.components.data.tag.TagTone
import com.pantrixui.components.elements.avatar.PantrixAvatar
import com.pantrixui.components.elements.button.ButtonSize
import com.pantrixui.components.elements.button.ButtonVariant
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.theme.PantrixTheme

var members by remember { mutableStateOf(MEMBERS) }
var chosen by remember { mutableStateOf(emptySet<String>()) }
PantrixDataTable(
    rows = members,
    columns = remember { memberColumns() },
    rowKey = { it.email },
    rowLabel = { it.name },
    selected = chosen,
    onSelectionChange = { chosen = it },
    toolbar = {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PantrixText(
                text = if (chosen.isEmpty()) {
                    "${members.size} members"
                } else {
                    PantrixTheme.strings.selectedCount(chosen.size)
                },
                style = PantrixTheme.typography.label
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PantrixButton(
                    text = "Restore all",
                    onClick = {
                        members = MEMBERS
                        chosen = emptySet()
                    },
                    variant = ButtonVariant.Text,
                    size = ButtonSize.Small,
                    enabled = members.size < MEMBERS.size,
                    tagKey = "restore"
                )
                PantrixButton(
                    text = "Remove",
                    onClick = {
                        members = members.filterNot { it.email in chosen }
                        chosen = emptySet()
                    },
                    variant = ButtonVariant.Destructive,
                    size = ButtonSize.Small,
                    enabled = chosen.isNotEmpty(),
                    tagKey = "remove"
                )
            }
        }
    },
    variant = DataTableVariant.Bordered,
    layout = DataTableLayout.Table,
    tagKey = "members"
)

private fun memberColumns(): List<DataTableColumn<Member>> = listOf(
    DataTableColumn(key = "member", header = "Member", weight = 2f, minWidth = 240.dp) { member ->
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            PantrixAvatar(contentDescription = null, initials = member.initials, size = 32.dp)
            Column {
                PantrixText(text = member.name, style = PantrixTheme.typography.label)
                PantrixText(
                    text = member.email,
                    style = PantrixTheme.typography.footnote,
                    color = PantrixTheme.colors.textSecondary
                )
            }
        }
    },
    DataTableColumn(key = "role", header = "Role", width = 120.dp) { member ->
        PantrixTag(text = member.role, tone = if (member.role == "Owner") TagTone.Primary else TagTone.Neutral)
    },
    DataTableColumn.text(key = "team", header = "Team", width = 128.dp) { it.team },
    DataTableColumn.text(key = "active", header = "Last active", width = 128.dp) { it.lastActive }
)

private class Member(
    val name: String,
    val email: String,
    val role: String,
    val team: String,
    val lastActive: String
) {
    val initials: String = name.split(" ").joinToString("") { it.take(1) }
}

private val MEMBERS = listOf(
    Member("Ava Thompson", "ava@acme.io", "Owner", "Leadership", "Now"),
    Member("Noah Garcia", "noah@acme.io", "Admin", "Engineering", "5 min ago"),
    Member("Mia Robinson", "mia@acme.io", "Editor", "Design", "1 hour ago"),
    Member("Ethan Clark", "ethan@acme.io", "Editor", "Engineering", "Yesterday"),
    Member("Harper Lewis", "harper@acme.io", "Viewer", "Marketing", "3 days ago"),
    Member("Lucas Walker", "lucas@acme.io", "Viewer", "Sales", "2 weeks ago")
)
```

### Rows that open

Given `rowDetail` and `onExpandedChange`, each row starts with a chevron: press it, and what the row holds opens under it, as wide as the table. Here an invoice opens to its lines, its total and a button that downloads it. Which rows are open is the screen's (`expanded`), so the screen opens the first one itself.

```kotlin
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pantrixui.components.data.datatable.DataTableColumn
import com.pantrixui.components.data.datatable.DataTableLayout
import com.pantrixui.components.data.datatable.DataTableVariant
import com.pantrixui.components.data.datatable.PantrixDataTable
import com.pantrixui.components.data.tag.PantrixTag
import com.pantrixui.components.data.tag.TagTone
import com.pantrixui.components.elements.button.ButtonSize
import com.pantrixui.components.elements.button.ButtonVariant
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.components.elements.divider.PantrixHorizontalDivider
import com.pantrixui.components.elements.icon.PantrixIcon
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.icons.PantrixIcons
import com.pantrixui.foundation.theme.PantrixTheme

var open by remember { mutableStateOf(setOf("INV-2041")) }
PantrixDataTable(
    rows = INVOICES,
    columns = remember { invoiceColumns() },
    rowKey = { it.number },
    rowLabel = { it.number },
    expanded = open,
    onExpandedChange = { open = it },
    rowDetail = { invoice -> InvoiceLines(invoice) },
    variant = DataTableVariant.Bordered,
    layout = DataTableLayout.Table,
    tagKey = "invoices"
)

private fun invoiceColumns(): List<DataTableColumn<Invoice>> = listOf(
    DataTableColumn.text(key = "number", header = "Invoice", width = 112.dp) { it.number },
    DataTableColumn.text(key = "client", header = "Client", weight = 2f, minWidth = 160.dp) { it.client },
    DataTableColumn.text(key = "due", header = "Due", width = 120.dp) { it.due },
    DataTableColumn(key = "status", header = "Status", width = 112.dp) { invoice ->
        PantrixTag(text = invoice.status, tone = if (invoice.status == "Overdue") TagTone.Danger else TagTone.Success)
    },
    DataTableColumn.text(key = "amount", header = "Amount", width = 112.dp, alignment = Alignment.End) {
        money(it.lines.sumOf { line -> line.amount })
    }
)

@Composable
private fun InvoiceLines(invoice: Invoice) {
    Column(Modifier.widthIn(max = 480.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        invoice.lines.forEach { line ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                PantrixText(text = line.item, color = PantrixTheme.colors.textSecondary)
                PantrixText(text = money(line.amount))
            }
        }
        PantrixHorizontalDivider()
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            PantrixText(text = "Total", style = PantrixTheme.typography.label.copy(fontWeight = FontWeight.SemiBold))
            PantrixText(
                text = money(invoice.lines.sumOf { it.amount }),
                style = PantrixTheme.typography.label.copy(fontWeight = FontWeight.SemiBold)
            )
        }
        PantrixButton(
            text = "Download PDF",
            onClick = {},
            variant = ButtonVariant.Outline,
            size = ButtonSize.Small,
            leadingIcon = { PantrixIcon(imageVector = PantrixIcons.Download, contentDescription = null) },
            tagKey = "download_${invoice.number}"
        )
    }
}

private class Line(val item: String, val amount: Int)

private class Invoice(
    val number: String,
    val client: String,
    val due: String,
    val status: String,
    val lines: List<Line>
)

private val INVOICES = listOf(
    Invoice(
        "INV-2041",
        "Northwind Traders",
        "Nov 12, 2026",
        "Paid",
        listOf(Line("Design system audit", 4200), Line("Component workshop, 2 days", 3600), Line("Travel", 480))
    ),
    Invoice("INV-2040", "Globex", "Nov 5, 2026", "Paid", listOf(Line("Annual licence, 25 seats", 7500))),
    Invoice(
        "INV-2039",
        "Initech",
        "Oct 20, 2026",
        "Overdue",
        listOf(Line("Accessibility review", 2800), Line("Fixes to 14 screens", 3150))
    ),
    Invoice("INV-2038", "Umbrella Health", "Oct 14, 2026", "Paid", listOf(Line("Support plan, October", 1200)))
)

private fun money(amount: Int): String = "$" + amount.toString().reversed().chunked(3).joinToString(",").reversed()
```

### A wide report

Fourteen columns are wider than any page: the table scrolls sideways, by a drag, a wheel, the bar under the rows, or the keyboard. The region is pinned to the start and the total to the end (`pin`), so that each month is read against them while the others pass under. In a `Grid` table a line runs between the columns as well, for a report the eye reads down as much as along.

The screen holds the table's state (`rememberDataTableState`): "Go to December" scrolls the months until its column is in view (`animateScrollToColumn`).

```kotlin
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.pantrixui.components.data.datatable.DataTableColumn
import com.pantrixui.components.data.datatable.DataTableLayout
import com.pantrixui.components.data.datatable.DataTablePin
import com.pantrixui.components.data.datatable.DataTableVariant
import com.pantrixui.components.data.datatable.PantrixDataTable
import com.pantrixui.components.data.datatable.rememberDataTableState
import com.pantrixui.components.elements.button.ButtonSize
import com.pantrixui.components.elements.button.ButtonVariant
import com.pantrixui.components.elements.button.PantrixButton
import kotlinx.coroutines.launch

val state = rememberDataTableState()
val scope = rememberCoroutineScope()
Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    PantrixButton(
        text = "Go to December",
        onClick = { scope.launch { state.animateScrollToColumn("m12") } },
        variant = ButtonVariant.Outline,
        size = ButtonSize.Small,
        tagKey = "to_december"
    )
    PantrixDataTable(
        rows = REGIONS,
        columns = remember { revenueColumns() },
        rowKey = { it.name },
        variant = DataTableVariant.Grid,
        layout = DataTableLayout.Table,
        state = state,
        tagKey = "revenue"
    )
}

private fun revenueColumns(): List<DataTableColumn<Region>> {
    val region = DataTableColumn.text<Region>(
        key = "region",
        header = "Region",
        width = 152.dp,
        pin = DataTablePin.Start
    ) { it.name }
    val months = MONTHS.mapIndexed { month, name ->
        DataTableColumn.text<Region>(key = "m${month + 1}", header = name, width = 96.dp, alignment = Alignment.End) {
            money(it.months[month])
        }
    }
    val total = DataTableColumn.text<Region>(
        key = "total",
        header = "Total",
        width = 120.dp,
        alignment = Alignment.End,
        pin = DataTablePin.End
    ) { money(it.months.sum()) }
    return listOf(region) + months + total
}

private class Region(val name: String, val months: List<Int>)

private val MONTHS = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

private val REGIONS = listOf(
    Region("North America", year(start = 42_000, growth = 1_800)),
    Region("Europe", year(start = 36_500, growth = 1_200)),
    Region("Asia Pacific", year(start = 28_000, growth = 2_400)),
    Region("Latin America", year(start = 12_500, growth = 600)),
    Region("Middle East", year(start = 9_800, growth = 450))
)

private fun year(start: Int, growth: Int): List<Int> = List(12) { month ->
    start + month * growth + month % 4 * 1_500
}

private fun money(amount: Int): String = "$" + amount.toString().reversed().chunked(3).joinToString(",").reversed()
```

### Pages from a server

A table that shows one page of rows at a time: it knows only the page on the screen, and the footer (`footer`) says which rows those are and moves between pages (`PantrixPagination`). While the next page is on its way the table is loading (`loading`): blocks stand in the rows' place, in the table's own columns, so that nothing jumps when the rows come.

Here the server is a pause of 600 milliseconds. A refund is written in the danger colour.

```kotlin
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pantrixui.components.data.datatable.DataTableColumn
import com.pantrixui.components.data.datatable.DataTableLayout
import com.pantrixui.components.data.datatable.DataTableVariant
import com.pantrixui.components.data.datatable.PantrixDataTable
import com.pantrixui.components.data.pagination.PantrixPagination
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.theme.PantrixTheme
import kotlinx.coroutines.delay

var page by remember { mutableIntStateOf(1) }
var shown by remember { mutableIntStateOf(1) }
var loading by remember { mutableStateOf(false) }
LaunchedEffect(page) {
    if (page != shown) {
        loading = true
        delay(600)
        shown = page
        loading = false
    }
}
val first = (shown - 1) * PAGE_SIZE
val rows = TRANSACTIONS.drop(first).take(PAGE_SIZE)
PantrixDataTable(
    rows = rows,
    columns = remember { transactionColumns() },
    rowKey = { it.id },
    loading = loading,
    loadingRows = PAGE_SIZE,
    footer = {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            itemVerticalAlignment = Alignment.CenterVertically
        ) {
            PantrixText(
                text = PantrixTheme.strings.rowRange(first + 1, first + rows.size, TRANSACTIONS.size),
                color = PantrixTheme.colors.textSecondary
            )
            PantrixPagination(
                page = page,
                pageCount = (TRANSACTIONS.size + PAGE_SIZE - 1) / PAGE_SIZE,
                onPageChange = { page = it },
                tagKey = "pages"
            )
        }
    },
    variant = DataTableVariant.Bordered,
    layout = DataTableLayout.Table,
    tagKey = "transactions"
)

private fun transactionColumns(): List<DataTableColumn<Transaction>> = listOf(
    DataTableColumn.text(key = "id", header = "Transaction", width = 128.dp) { it.id },
    DataTableColumn.text(key = "date", header = "Date", width = 120.dp) { it.date },
    DataTableColumn.text(key = "description", header = "Description", weight = 2f, minWidth = 180.dp) {
        it.description
    },
    DataTableColumn.text(key = "method", header = "Method", width = 176.dp) { it.method },
    DataTableColumn(key = "amount", header = "Amount", width = 112.dp, alignment = Alignment.End) { transaction ->
        PantrixText(
            text = money(transaction.amount),
            color = if (transaction.amount < 0) PantrixTheme.colors.danger.color else Color.Unspecified
        )
    }
)

private class Transaction(
    val id: String,
    val date: String,
    val description: String,
    val method: String,
    val amount: Int
)

private val DESCRIPTIONS = listOf("Pro plan", "Team seats", "Add-on: analytics", "Consulting hours", "Hardware kit")

private val METHODS = listOf("Visa •• 4242", "Bank transfer", "Mastercard •• 1881", "PayPal")

private val TRANSACTIONS = List(46) { index ->
    Transaction(
        id = "TX-${8_410 + index}",
        date = "Oct ${28 - index / 2}, 2026",
        description = DESCRIPTIONS[index % DESCRIPTIONS.size],
        method = METHODS[index % METHODS.size],
        amount = (if (index % 7 == 3) -1 else 1) * (40 + index * 37 % 900)
    )
}

private const val PAGE_SIZE = 8

private fun money(amount: Int): String {
    val digits = kotlin.math.abs(amount).toString().reversed().chunked(3).joinToString(",").reversed()
    return if (amount < 0) "−$$digits" else "$$digits"
}
```

### Ten thousand rows

A table given a height (`Modifier.height`) keeps its header in place and scrolls its rows under it, and builds only the rows in view: ten thousand cost no more than ten. Scroll it with a finger, a wheel, or Page Down once a row has the focus.

```kotlin
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pantrixui.components.data.datatable.DataTableColumn
import com.pantrixui.components.data.datatable.DataTableLayout
import com.pantrixui.components.data.datatable.DataTableVariant
import com.pantrixui.components.data.datatable.PantrixDataTable
import com.pantrixui.components.data.tag.PantrixTag
import com.pantrixui.components.data.tag.TagTone
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.theme.PantrixTheme

Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    PantrixText(
        text = "10,000 events from the last three hours",
        style = PantrixTheme.typography.footnote,
        color = PantrixTheme.colors.textSecondary
    )
    PantrixDataTable(
        rows = EVENTS,
        columns = remember { eventColumns() },
        modifier = Modifier.height(400.dp),
        rowKey = { it.id },
        variant = DataTableVariant.Bordered,
        layout = DataTableLayout.Table,
        tagKey = "events"
    )
}

private fun eventColumns(): List<DataTableColumn<Event>> = listOf(
    DataTableColumn.text(key = "time", header = "Time", width = 104.dp) { it.time },
    DataTableColumn(key = "level", header = "Level", width = 112.dp) { event ->
        PantrixTag(text = event.level.name, tone = event.level.tone)
    },
    DataTableColumn.text(key = "service", header = "Service", width = 120.dp) { it.service },
    DataTableColumn.text(key = "message", header = "Message", weight = 3f, minWidth = 240.dp) { it.message }
)

private enum class Level(val tone: TagTone) {
    Info(TagTone.Info),
    Warning(TagTone.Warning),
    Error(TagTone.Danger)
}

private class Event(
    val id: String,
    val time: String,
    val level: Level,
    val service: String,
    val message: String
)

private val SERVICES = listOf("api", "billing", "auth", "search", "mailer")

private val MESSAGES = listOf(
    "Request served in 42 ms",
    "Cache warmed for 1,204 keys",
    "Retrying the payment provider",
    "Token refreshed for a session",
    "Index rebuilt in 3.1 s",
    "Queue is 80% full"
)

private val EVENTS = List(10_000) { index ->
    val second = 10_800 - index
    Event(
        id = "$index",
        time = "${10 + second / 3_600}:${(second / 60 % 60).toString().padStart(2, '0')}:" +
            (second % 60).toString().padStart(2, '0'),
        level = when {
            index % 41 == 0 -> Level.Error
            index % 9 == 0 -> Level.Warning
            else -> Level.Info
        },
        service = SERVICES[index % SERVICES.size],
        message = MESSAGES[index % MESSAGES.size]
    )
}
```

### Campaigns

A cell holds whatever its column draws: here a campaign's channel under its name, its status as a tag, what it has spent against its budget, and a line of its last twelve weeks (`PantrixSparkline`).

A row can be told apart as a whole. A campaign over its budget is in the danger tone (`rowTone`): its row takes the role's soft fill, and its text the colour the theme pairs with it. An ended campaign is closed (`rowEnabled`): it is drawn faded, and nothing in it can be pressed or chosen.

```kotlin
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pantrixui.components.chart.sparkline.PantrixSparkline
import com.pantrixui.components.data.datatable.DataTableColumn
import com.pantrixui.components.data.datatable.DataTableLayout
import com.pantrixui.components.data.datatable.DataTableTone
import com.pantrixui.components.data.datatable.DataTableVariant
import com.pantrixui.components.data.datatable.PantrixDataTable
import com.pantrixui.components.data.tag.PantrixTag
import com.pantrixui.components.data.tag.TagTone
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.theme.PantrixTheme

PantrixDataTable(
    rows = CAMPAIGNS,
    columns = remember { campaignColumns() },
    rowKey = { it.name },
    rowEnabled = { it.status != CampaignStatus.Ended },
    rowTone = { if (it.spent > it.budget) DataTableTone.Danger else null },
    variant = DataTableVariant.Bordered,
    layout = DataTableLayout.Table,
    tagKey = "campaigns"
)

private fun campaignColumns(): List<DataTableColumn<Campaign>> = listOf(
    DataTableColumn(key = "campaign", header = "Campaign", weight = 2f, minWidth = 200.dp) { campaign ->
        Column {
            PantrixText(text = campaign.name, style = PantrixTheme.typography.label)
            PantrixText(
                text = campaign.channel,
                style = PantrixTheme.typography.footnote,
                color = PantrixTheme.colors.textSecondary
            )
        }
    },
    DataTableColumn(key = "status", header = "Status", width = 112.dp) { campaign ->
        PantrixTag(text = campaign.status.name, tone = campaign.status.tone, dot = true)
    },
    DataTableColumn.text(key = "spent", header = "Spent", width = 168.dp, alignment = Alignment.End) {
        "${money(it.spent)} of ${money(it.budget)}"
    },
    DataTableColumn.text(key = "conversions", header = "Conversions", width = 120.dp, alignment = Alignment.End) {
        "${it.trend.sum()}"
    },
    DataTableColumn(key = "trend", header = "Last 12 weeks", width = 144.dp) { campaign ->
        PantrixSparkline(
            values = campaign.trend,
            modifier = Modifier.size(width = 112.dp, height = 28.dp),
            contentDescription = "Conversions of ${campaign.name} over the last twelve weeks",
            markLast = true
        )
    }
)

private enum class CampaignStatus(val tone: TagTone) {
    Active(TagTone.Success),
    Paused(TagTone.Warning),
    Ended(TagTone.Neutral)
}

private class Campaign(
    val name: String,
    val channel: String,
    val status: CampaignStatus,
    val spent: Int,
    val budget: Int,
    val trend: List<Int>
)

private val CAMPAIGNS = listOf(
    Campaign(
        "Autumn sale",
        "Search ads",
        CampaignStatus.Active,
        3_240,
        5_000,
        listOf(12, 18, 15, 22, 28, 26, 31, 35, 33, 41, 46, 52)
    ),
    Campaign(
        "Product launch",
        "Social",
        CampaignStatus.Active,
        6_480,
        6_000,
        listOf(5, 9, 14, 30, 48, 44, 39, 36, 31, 29, 27, 24)
    ),
    Campaign(
        "Newsletter revival",
        "Email",
        CampaignStatus.Paused,
        820,
        2_000,
        listOf(20, 19, 21, 18, 17, 16, 15, 15, 14, 12, 12, 11)
    ),
    Campaign(
        "Summer webinar",
        "Events",
        CampaignStatus.Ended,
        4_100,
        4_500,
        listOf(3, 8, 16, 25, 31, 18, 6, 2, 1, 0, 0, 0)
    )
)

private fun money(amount: Int): String = "$" + amount.toString().reversed().chunked(3).joinToString(",").reversed()
```

### Looks

The same shipments in each of the table's looks (`variant`): `Plain` stands on the page with a line between the rows, `Bordered` puts one edge around the whole table, and `Grid` draws a line between the columns as well. Stripes (`striped`) fill every other row, which helps the eye along a long row.

```kotlin
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.pantrixui.components.data.datatable.DataTableColumn
import com.pantrixui.components.data.datatable.DataTableLayout
import com.pantrixui.components.data.datatable.DataTableVariant
import com.pantrixui.components.data.datatable.PantrixDataTable
import com.pantrixui.components.elements.segmentbutton.PantrixSegmentButton
import com.pantrixui.components.elements.segmentbutton.SegmentOption
import com.pantrixui.components.elements.switchbutton.PantrixSwitchButton

var variant by remember { mutableStateOf(DataTableVariant.Bordered) }
var striped by remember { mutableStateOf(true) }
Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        itemVerticalAlignment = Alignment.CenterVertically
    ) {
        PantrixSegmentButton(
            options = remember {
                DataTableVariant.entries.map { SegmentOption(it, it.name, tagKey = it.name.lowercase()) }
            },
            selected = variant,
            onSelect = { variant = it },
            fullWidth = false,
            tagKey = "variant"
        )
        PantrixSwitchButton(
            checked = striped,
            onCheckedChange = { striped = it },
            label = "Stripes",
            tagKey = "striped"
        )
    }
    PantrixDataTable(
        rows = SHIPMENTS,
        columns = remember { shipmentColumns() },
        variant = variant,
        striped = striped,
        layout = DataTableLayout.Table,
        tagKey = "shipments"
    )
}

private fun shipmentColumns(): List<DataTableColumn<Shipment>> = listOf(
    DataTableColumn.text(key = "tracking", header = "Tracking", width = 136.dp) { it.tracking },
    DataTableColumn.text(key = "destination", header = "Destination", weight = 2f, minWidth = 160.dp) {
        it.destination
    },
    DataTableColumn.text(key = "carrier", header = "Carrier", width = 112.dp) { it.carrier },
    DataTableColumn.text(key = "eta", header = "Arrives", width = 112.dp) { it.eta },
    DataTableColumn.text(key = "weight", header = "Weight", width = 96.dp, alignment = Alignment.End) {
        "${it.weight} kg"
    }
)

private class Shipment(
    val tracking: String,
    val destination: String,
    val carrier: String,
    val eta: String,
    val weight: Double
)

private val SHIPMENTS = listOf(
    Shipment("1Z 884 201", "Rotterdam, Netherlands", "UPS", "Nov 2", 12.4),
    Shipment("JD 014 772", "Lyon, France", "DHL", "Nov 3", 3.1),
    Shipment("9400 1109", "Austin, United States", "USPS", "Nov 4", 0.8),
    Shipment("CX 552 918", "Osaka, Japan", "FedEx", "Nov 6", 21.0),
    Shipment("RR 330 845", "Kraków, Poland", "DPD", "Nov 6", 6.5)
)
```

### Loading and empty

The table keeps its header in both states. While it is loading (`loading`), blocks stand in its rows' place; with no rows, it shows what `empty` gives it, here a word of what is missing and a way to start, or the theme's "No rows" where nothing is given. It stays a few rows tall, so that a filter that empties it does not fold the page up.

```kotlin
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pantrixui.components.data.datatable.DataTableColumn
import com.pantrixui.components.data.datatable.DataTableLayout
import com.pantrixui.components.data.datatable.DataTableVariant
import com.pantrixui.components.data.datatable.PantrixDataTable
import com.pantrixui.components.elements.button.ButtonSize
import com.pantrixui.components.elements.button.ButtonVariant
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.components.elements.icon.PantrixIcon
import com.pantrixui.components.elements.segmentbutton.PantrixSegmentButton
import com.pantrixui.components.elements.segmentbutton.SegmentOption
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.icons.PantrixIcons
import com.pantrixui.foundation.theme.PantrixTheme

var shown by remember { mutableStateOf(Shown.Rows) }
Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    PantrixSegmentButton(
        options = remember { Shown.entries.map { SegmentOption(it, it.name, tagKey = it.name.lowercase()) } },
        selected = shown,
        onSelect = { shown = it },
        fullWidth = false,
        tagKey = "shown"
    )
    PantrixDataTable(
        rows = if (shown == Shown.Rows) FILES else emptyList(),
        columns = remember { fileColumns() },
        loading = shown == Shown.Loading,
        loadingRows = FILES.size,
        empty = { NoFiles() },
        variant = DataTableVariant.Bordered,
        layout = DataTableLayout.Table,
        tagKey = "files"
    )
}

@Composable
private fun NoFiles() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PantrixIcon(
            imageVector = PantrixIcons.Folder,
            contentDescription = null,
            size = 32.dp,
            tint = PantrixTheme.colors.textSecondary
        )
        PantrixText(text = "No files yet", style = PantrixTheme.typography.label)
        PantrixText(
            text = "Files you upload are listed here.",
            style = PantrixTheme.typography.footnote,
            color = PantrixTheme.colors.textSecondary
        )
        PantrixButton(
            text = "Upload a file",
            onClick = {},
            variant = ButtonVariant.Outline,
            size = ButtonSize.Small,
            leadingIcon = { PantrixIcon(imageVector = PantrixIcons.Upload, contentDescription = null) },
            tagKey = "upload"
        )
    }
}

private fun fileColumns(): List<DataTableColumn<SharedFile>> = listOf(
    DataTableColumn.text(key = "name", header = "Name", weight = 2f, minWidth = 200.dp) { it.name },
    DataTableColumn.text(key = "owner", header = "Owner", width = 140.dp) { it.owner },
    DataTableColumn.text(key = "modified", header = "Modified", width = 128.dp) { it.modified },
    DataTableColumn.text(key = "size", header = "Size", width = 96.dp, alignment = Alignment.End) { it.size }
)

private enum class Shown { Rows, Loading, Empty }

private class SharedFile(
    val name: String,
    val owner: String,
    val modified: String,
    val size: String
)

private val FILES = listOf(
    SharedFile("Brand guidelines.pdf", "Mia Robinson", "Today", "4.2 MB"),
    SharedFile("Q4 roadmap.key", "Ava Thompson", "Yesterday", "18.7 MB"),
    SharedFile("Pricing study.xlsx", "Noah Garcia", "Oct 24", "860 KB"),
    SharedFile("Onboarding flow.fig", "Ethan Clark", "Oct 21", "12.3 MB")
)
```

### A list of records

The same orders as a table, or as a list of records: each order a block of its own, each value under its column's name. The table is a list where it is told to be one (`DataTableLayout.List`). `Auto` lets its own width choose: narrower than the style's `compactWidth`, 600dp, it is a list, and a table otherwise; the table in a narrow pane of a wide screen is a list too.

The list keeps what the table could do: the boxes that choose a row, and the order, by the names of the columns that sort in a strip over the records.

```kotlin
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.pantrixui.components.data.datatable.DataTableColumn
import com.pantrixui.components.data.datatable.DataTableLayout
import com.pantrixui.components.data.datatable.DataTableSort
import com.pantrixui.components.data.datatable.DataTableVariant
import com.pantrixui.components.data.datatable.PantrixDataTable
import com.pantrixui.components.data.datatable.comparator
import com.pantrixui.components.data.tag.PantrixTag
import com.pantrixui.components.data.tag.TagTone
import com.pantrixui.components.elements.segmentbutton.PantrixSegmentButton
import com.pantrixui.components.elements.segmentbutton.SegmentOption

var layout by remember { mutableStateOf(DataTableLayout.List) }
var sort by remember { mutableStateOf(emptyList<DataTableSort>()) }
var chosen by remember { mutableStateOf(emptySet<String>()) }
val columns = remember { orderColumns() }
Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    PantrixSegmentButton(
        options = remember {
            DataTableLayout.entries.map { SegmentOption(it, it.name, tagKey = it.name.lowercase()) }
        },
        selected = layout,
        onSelect = { layout = it },
        fullWidth = false,
        tagKey = "layout"
    )
    PantrixDataTable(
        rows = remember(sort) { ORDERS.sortedWith(sort.comparator(columns)) },
        columns = columns,
        rowKey = { it.id },
        rowLabel = { it.customer },
        sort = sort,
        onSortChange = { sort = it },
        selected = chosen,
        onSelectionChange = { chosen = it },
        variant = DataTableVariant.Bordered,
        layout = layout,
        tagKey = "records"
    )
}

private fun orderColumns(): List<DataTableColumn<Order>> = listOf(
    DataTableColumn.text(key = "id", header = "Order", width = 96.dp) { "#${it.id}" },
    DataTableColumn.text(key = "customer", header = "Customer", weight = 2f, sortable = true) { it.customer },
    DataTableColumn(key = "status", header = "Status", width = 128.dp) { order ->
        PantrixTag(text = order.status, tone = if (order.status == "Paid") TagTone.Success else TagTone.Warning)
    },
    DataTableColumn.text(
        key = "total",
        header = "Total",
        width = 112.dp,
        alignment = Alignment.End,
        sortable = true,
        comparator = compareBy<Order> { it.total }
    ) { money(it.total) }
)

private class Order(
    val id: String,
    val customer: String,
    val status: String,
    val total: Int
)

private val ORDERS = listOf(
    Order("3210", "Olivia Martin", "Paid", 1999),
    Order("3209", "Jackson Lee", "Pending", 39),
    Order("3208", "Isabella Nguyen", "Paid", 299),
    Order("3207", "William Kim", "Pending", 99)
)

private fun money(amount: Int): String = "$" + amount.toString().reversed().chunked(3).joinToString(",").reversed()
```
