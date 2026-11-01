# Veri Tablosu

`PantrixDataTable`'ın sitenin kendi örnekleri: sayfasında demonunkilerin yerine gösterilir.

## Örnekler

### Siparişler

Bir uygulamanın gösterdiği gibi bir sipariş tablosu: müşteri avatarı ve e-postasıyla, siparişin durumu bir etiketle, tutar da kolonunun sonunda, basamakları alt alta gelsin diye. Her kolon genişliğin ne kadarını aldığını söyler: kendi genişliği ya da ötekilerin bıraktığından bir pay (`weight`), `minWidth`'ten dar olmadan.

Tablo her genişlikte, bir telefonda da tablo kalır; kolonları sığmadığında yana kayar (`layout = DataTableLayout.Table`). Seçimi kendi genişliğine bırakılan tablo 600dp'den darsa kayıtların listesi olur: son örnek bunu gösterir.

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
    DataTableColumn.text(key = "id", header = "Sipariş", width = 96.dp) { "#${it.id}" },
    DataTableColumn(key = "customer", header = "Müşteri", weight = 2f, minWidth = 240.dp) { order ->
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
    DataTableColumn.text(key = "date", header = "Tarih", width = 128.dp) { it.date },
    DataTableColumn(key = "status", header = "Durum", width = 128.dp) { order ->
        PantrixTag(text = order.status.label, tone = order.status.tone, dot = true)
    },
    DataTableColumn.text(key = "total", header = "Tutar", width = 112.dp, alignment = Alignment.End) {
        money(it.total)
    }
)

private enum class Status(val label: String, val tone: TagTone) {
    Paid("Ödendi", TagTone.Success),
    Pending("Bekliyor", TagTone.Warning),
    Refunded("İade edildi", TagTone.Neutral),
    Failed("Başarısız", TagTone.Danger)
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
    Order("3210", "Deniz Arslan", "deniz.arslan@posta.com", AvatarTone.Primary, "28 Eki 2026", Status.Paid, 1999),
    Order("3209", "Elif Kaya", "elif.kaya@posta.com", AvatarTone.Info, "28 Eki 2026", Status.Pending, 390),
    Order("3208", "Mert Yıldız", "mert@posta.com", AvatarTone.Success, "27 Eki 2026", Status.Paid, 2990),
    Order("3207", "Zeynep Demir", "zeynep.demir@posta.com", AvatarTone.Warning, "27 Eki 2026", Status.Refunded, 990),
    Order("3206", "Can Öztürk", "can.ozturk@posta.com", AvatarTone.Danger, "26 Eki 2026", Status.Failed, 4500),
    Order("3205", "Ayşe Şahin", "ayse.sahin@posta.com", AvatarTone.Neutral, "25 Eki 2026", Status.Paid, 23500)
)

private fun money(amount: Int): String = amount.toString().reversed().chunked(3).joinToString(".").reversed() + " ₺"
```

### Sıralama

`sortable = true` olan kolonun başlığı bir düğmedir. Fiyat'a bas: ürünler en ucuzdan yukarı dizilir ve başlığın yanındaki ok bunu söyler; yeniden bas, en pahalıdan aşağı dizilir; üçüncü basış sırayı kaldırır. Sıra ekranındır (`sort`): tablo hangi sıranın istendiğini söyler (`onSortChange`), `sort.comparator(columns)` de satırları o sıraya dizer.

Adlar okuyucunun dilinin alfabesine göre (`rememberTextOrder`), stok ve fiyat da sayı olarak, her biri kendi karşılaştırıcısıyla sıralanır. Ürün'e bas: "Çınar kitaplık" "Aurora"dan sonra, "Echo"dan önce gelir; alfabeye bakmayan bir sıra onu en sona, "Orbit"ten sonraya atardı. "Birden çok kolona göre sırala" açıkken basılan kolon sıraya eklenir: önce Kategori'ye, sonra Fiyat'a bas, bir kategorinin ürünleri en ucuzdan yukarı dizilir.

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
        label = "Birden çok kolona göre sırala",
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
    DataTableColumn.text(key = "name", header = "Ürün", weight = 2f, sortable = true, textOrder = textOrder) {
        it.name
    },
    DataTableColumn.text(key = "category", header = "Kategori", sortable = true, textOrder = textOrder) {
        it.category
    },
    DataTableColumn(
        key = "stock",
        header = "Stok",
        width = 136.dp,
        alignment = Alignment.End,
        sortable = true,
        comparator = compareBy<Product> { it.stock }
    ) { product ->
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (product.stock < LOW_STOCK) PantrixTag(text = "Az", tone = TagTone.Warning)
            PantrixText(text = "${product.stock}")
        }
    },
    DataTableColumn.text(
        key = "price",
        header = "Fiyat",
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
    Product("LMP-01", "Aurora masa lambası", "Aydınlatma", 42, 890),
    Product("CHR-07", "Nimbus ofis koltuğu", "Mobilya", 7, 3490),
    Product("MSE-12", "Orbit kablosuz fare", "Aksesuar", 128, 390),
    Product("MUG-03", "Ember seramik kupa", "Mutfak", 3, 180),
    Product("DSK-02", "Atlas ayarlı masa", "Mobilya", 15, 6290),
    Product("SPK-05", "Echo hoparlör", "Ses", 64, 1290),
    Product("SHF-08", "Çınar kitaplık", "Mobilya", 11, 2450),
    Product("LMP-04", "Lumen lambader", "Aydınlatma", 21, 1490)
)

private const val LOW_STOCK = 10

private fun money(amount: Int): String = amount.toString().reversed().chunked(3).joinToString(".").reversed() + " ₺"
```

### Satır seçme

`selected` ve `onSelectionChange` verilen tablo her satırın başına bir kutu, başlığa da bütün satırları birden seçen bir kutu koyar. Hangi satırların seçili olduğu ekranındır: burada başlığın üstündeki araç çubuğu (`toolbar`) onları sayar ve ekipten çıkarır.

`rowLabel` her satırı ekran okuyucuya adıyla söyler: kutusu hangi üyeyi seçtiğini söyler. Birkaçını çıkar, sonra "Hepsini geri al" ile geri getir.

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
                    "${members.size} üye"
                } else {
                    PantrixTheme.strings.selectedCount(chosen.size)
                },
                style = PantrixTheme.typography.label
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PantrixButton(
                    text = "Hepsini geri al",
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
                    text = "Çıkar",
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
    DataTableColumn(key = "member", header = "Üye", weight = 2f, minWidth = 240.dp) { member ->
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
    DataTableColumn(key = "role", header = "Rol", width = 120.dp) { member ->
        PantrixTag(text = member.role, tone = if (member.role == "Sahip") TagTone.Primary else TagTone.Neutral)
    },
    DataTableColumn.text(key = "team", header = "Ekip", width = 128.dp) { it.team },
    DataTableColumn.text(key = "active", header = "Son görülme", width = 128.dp) { it.lastActive }
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
    Member("Selin Aydın", "selin@acme.com.tr", "Sahip", "Yönetim", "Şimdi"),
    Member("Burak Çelik", "burak@acme.com.tr", "Yönetici", "Mühendislik", "5 dk önce"),
    Member("Naz Polat", "naz@acme.com.tr", "Editör", "Tasarım", "1 saat önce"),
    Member("Emre Koç", "emre@acme.com.tr", "Editör", "Mühendislik", "Dün"),
    Member("İpek Güneş", "ipek@acme.com.tr", "İzleyici", "Pazarlama", "3 gün önce"),
    Member("Kerem Aksoy", "kerem@acme.com.tr", "İzleyici", "Satış", "2 hafta önce")
)
```

### Açılan satırlar

`rowDetail` ve `onExpandedChange` verilen tablonun her satırı bir okla başlar: oka bas, satırın tuttuğu şey altında, tablonun genişliğinde açılır. Burada bir fatura kalemlerine, toplamına ve onu indiren bir düğmeye açılır. Hangi satırların açık olduğu ekranındır (`expanded`): ekran ilkini kendisi açar.

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

var open by remember { mutableStateOf(setOf("FTR-2041")) }
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
    DataTableColumn.text(key = "number", header = "Fatura", width = 112.dp) { it.number },
    DataTableColumn.text(key = "client", header = "Müşteri", weight = 2f, minWidth = 160.dp) { it.client },
    DataTableColumn.text(key = "due", header = "Son ödeme", width = 120.dp) { it.due },
    DataTableColumn(key = "status", header = "Durum", width = 112.dp) { invoice ->
        PantrixTag(text = invoice.status, tone = if (invoice.status == "Gecikti") TagTone.Danger else TagTone.Success)
    },
    DataTableColumn.text(key = "amount", header = "Tutar", width = 120.dp, alignment = Alignment.End) {
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
            PantrixText(text = "Toplam", style = PantrixTheme.typography.label.copy(fontWeight = FontWeight.SemiBold))
            PantrixText(
                text = money(invoice.lines.sumOf { it.amount }),
                style = PantrixTheme.typography.label.copy(fontWeight = FontWeight.SemiBold)
            )
        }
        PantrixButton(
            text = "PDF olarak indir",
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
        "FTR-2041",
        "Kuzey Ticaret",
        "12 Kas 2026",
        "Ödendi",
        listOf(
            Line("Tasarım sistemi incelemesi", 42000),
            Line("Bileşen atölyesi, 2 gün", 36000),
            Line("Yol", 4800)
        )
    ),
    Invoice("FTR-2040", "Anadolu Yazılım", "5 Kas 2026", "Ödendi", listOf(Line("Yıllık lisans", 75_000))),
    Invoice(
        "FTR-2039",
        "Ege Lojistik",
        "20 Eki 2026",
        "Gecikti",
        listOf(Line("Erişilebilirlik incelemesi", 28000), Line("14 ekranda düzeltme", 31500))
    ),
    Invoice("FTR-2038", "Marmara Sağlık", "14 Eki 2026", "Ödendi", listOf(Line("Destek paketi, Ekim", 12000)))
)

private fun money(amount: Int): String = amount.toString().reversed().chunked(3).joinToString(".").reversed() + " ₺"
```

### Geniş bir rapor

On dört kolon hiçbir sayfaya sığmaz: tablo yana kayar; sürükleyerek, tekerlekle, satırların altındaki çubukla ya da klavyeyle. Bölge başa, toplam sona sabitlendi (`pin`): her ay onlarla birlikte okunur, ötekiler altlarından geçer. `Grid` tablosunda kolonların arasına da çizgi çekilir: gözün satır boyunca olduğu kadar kolon boyunca da okuduğu bir rapor için.

Tablonun durumunu ekran tutar (`rememberDataTableState`): "Aralık'a git" ayları, kolonu görünene kadar kaydırır (`animateScrollToColumn`).

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
        text = "Aralık'a git",
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
        header = "Bölge",
        width = 152.dp,
        pin = DataTablePin.Start
    ) { it.name }
    val months = MONTHS.mapIndexed { month, name ->
        DataTableColumn.text<Region>(
            key = "m${month + 1}",
            header = name,
            width = 104.dp,
            alignment = Alignment.End
        ) { money(it.months[month]) }
    }
    val total = DataTableColumn.text<Region>(
        key = "total",
        header = "Toplam",
        width = 128.dp,
        alignment = Alignment.End,
        pin = DataTablePin.End
    ) { money(it.months.sum()) }
    return listOf(region) + months + total
}

private class Region(val name: String, val months: List<Int>)

private val MONTHS = listOf("Oca", "Şub", "Mar", "Nis", "May", "Haz", "Tem", "Ağu", "Eyl", "Eki", "Kas", "Ara")

private val REGIONS = listOf(
    Region("Marmara", year(start = 420_000, growth = 18_000)),
    Region("İç Anadolu", year(start = 365_000, growth = 12_000)),
    Region("Ege", year(start = 280_000, growth = 24_000)),
    Region("Akdeniz", year(start = 125_000, growth = 6_000)),
    Region("Karadeniz", year(start = 98_000, growth = 4_500))
)

private fun year(start: Int, growth: Int): List<Int> = List(12) { month ->
    start + month * growth + month % 4 * 15_000
}

private fun money(amount: Int): String = amount.toString().reversed().chunked(3).joinToString(".").reversed() + " ₺"
```

### Sunucudan sayfa sayfa

Satırları sayfa sayfa gösteren tablo: yalnızca ekrandaki sayfayı bilir; ayağı (`footer`) bunların hangi satırlar olduğunu söyler ve sayfalar arasında gezdirir (`PantrixPagination`). Sonraki sayfa yoldayken tablo yüklenir (`loading`): satırların yerinde, tablonun kendi kolonlarında bloklar durur; satırlar gelince hiçbir şey yerinden oynamaz.

Burada sunucu 600 milisaniyelik bir beklemedir. Bir iade tehlike renginde yazılır.

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
    DataTableColumn.text(key = "id", header = "İşlem", width = 128.dp) { it.id },
    DataTableColumn.text(key = "date", header = "Tarih", width = 120.dp) { it.date },
    DataTableColumn.text(key = "description", header = "Açıklama", weight = 2f, minWidth = 180.dp) {
        it.description
    },
    DataTableColumn.text(key = "method", header = "Yöntem", width = 176.dp) { it.method },
    DataTableColumn(key = "amount", header = "Tutar", width = 120.dp, alignment = Alignment.End) { transaction ->
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

private val DESCRIPTIONS = listOf("Pro paket", "Ekip koltukları", "Ek: analitik", "Danışmanlık saati", "Donanım seti")

private val METHODS = listOf("Visa •• 4242", "Havale", "Mastercard •• 1881", "Troy •• 6060")

private val TRANSACTIONS = List(46) { index ->
    Transaction(
        id = "TX-${8_410 + index}",
        date = "${28 - index / 2} Eki 2026",
        description = DESCRIPTIONS[index % DESCRIPTIONS.size],
        method = METHODS[index % METHODS.size],
        amount = (if (index % 7 == 3) -1 else 1) * (400 + index * 370 % 9_000)
    )
}

private const val PAGE_SIZE = 8

private fun money(amount: Int): String {
    val digits = kotlin.math.abs(amount).toString().reversed().chunked(3).joinToString(".").reversed()
    return if (amount < 0) "−$digits ₺" else "$digits ₺"
}
```

### On bin satır

Yüksekliği verilen tablo (`Modifier.height`) başlığını yerinde tutar, satırlarını altında kaydırır ve yalnızca görünen satırları kurar: on bin satır ondan pahalı değildir. Parmakla, tekerlekle ya da bir satır odaktayken Page Down ile kaydır.

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
        text = "Son üç saatin 10.000 olayı",
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
    DataTableColumn.text(key = "time", header = "Saat", width = 104.dp) { it.time },
    DataTableColumn(key = "level", header = "Düzey", width = 112.dp) { event ->
        PantrixTag(text = event.level.label, tone = event.level.tone)
    },
    DataTableColumn.text(key = "service", header = "Servis", width = 120.dp) { it.service },
    DataTableColumn.text(key = "message", header = "İleti", weight = 3f, minWidth = 240.dp) { it.message }
)

private enum class Level(val label: String, val tone: TagTone) {
    Info("Bilgi", TagTone.Info),
    Warning("Uyarı", TagTone.Warning),
    Error("Hata", TagTone.Danger)
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
    "İstek 42 ms'de yanıtlandı",
    "Önbellek 1.204 anahtarla ısıtıldı",
    "Ödeme sağlayıcısı yeniden deneniyor",
    "Bir oturumun belirteci yenilendi",
    "Dizin 3,1 sn'de yeniden kuruldu",
    "Kuyruk %80 dolu"
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

### Kampanyalar

Bir hücre, kolonunun çizdiği her şeyi tutar: burada kampanyanın kanalı adının altında, durumu bir etiketle, harcadığı bütçesinin yanında ve son on iki haftası bir çizgiyle (`PantrixSparkline`).

Bir satır bütünüyle de ayırt edilir. Bütçesini aşan kampanya tehlike tonundadır (`rowTone`): satırı rolün yumuşak dolgusunu, yazısı da temanın o dolguyla eşlediği rengi alır. Biten kampanya kapalıdır (`rowEnabled`): soluk çizilir, içindeki hiçbir şeye basılmaz, satır seçilmez.

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
    DataTableColumn(key = "campaign", header = "Kampanya", weight = 2f, minWidth = 200.dp) { campaign ->
        Column {
            PantrixText(text = campaign.name, style = PantrixTheme.typography.label)
            PantrixText(
                text = campaign.channel,
                style = PantrixTheme.typography.footnote,
                color = PantrixTheme.colors.textSecondary
            )
        }
    },
    DataTableColumn(key = "status", header = "Durum", width = 112.dp) { campaign ->
        PantrixTag(text = campaign.status.label, tone = campaign.status.tone, dot = true)
    },
    DataTableColumn.text(key = "spent", header = "Harcanan", width = 184.dp, alignment = Alignment.End) {
        "${money(it.spent)} / ${money(it.budget)}"
    },
    DataTableColumn.text(key = "conversions", header = "Dönüşüm", width = 112.dp, alignment = Alignment.End) {
        "${it.trend.sum()}"
    },
    DataTableColumn(key = "trend", header = "Son 12 hafta", width = 144.dp) { campaign ->
        PantrixSparkline(
            values = campaign.trend,
            modifier = Modifier.size(width = 112.dp, height = 28.dp),
            contentDescription = "${campaign.name} kampanyasının son on iki haftadaki dönüşümleri",
            markLast = true
        )
    }
)

private enum class CampaignStatus(val label: String, val tone: TagTone) {
    Active("Yayında", TagTone.Success),
    Paused("Durdu", TagTone.Warning),
    Ended("Bitti", TagTone.Neutral)
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
        "Sonbahar indirimi",
        "Arama reklamları",
        CampaignStatus.Active,
        32_400,
        50_000,
        listOf(12, 18, 15, 22, 28, 26, 31, 35, 33, 41, 46, 52)
    ),
    Campaign(
        "Yeni ürün tanıtımı",
        "Sosyal medya",
        CampaignStatus.Active,
        64_800,
        60_000,
        listOf(5, 9, 14, 30, 48, 44, 39, 36, 31, 29, 27, 24)
    ),
    Campaign(
        "Bülteni canlandırma",
        "E-posta",
        CampaignStatus.Paused,
        8_200,
        20_000,
        listOf(20, 19, 21, 18, 17, 16, 15, 15, 14, 12, 12, 11)
    ),
    Campaign(
        "Yaz webinarı",
        "Etkinlik",
        CampaignStatus.Ended,
        41_000,
        45_000,
        listOf(3, 8, 16, 25, 31, 18, 6, 2, 1, 0, 0, 0)
    )
)

private fun money(amount: Int): String = amount.toString().reversed().chunked(3).joinToString(".").reversed() + " ₺"
```

### Görünümler

Aynı gönderiler, tablonun her görünümünde (`variant`): `Plain` satırların arasında bir çizgiyle sayfada durur, `Bordered` bütün tablonun çevresine bir kenar çizer, `Grid` kolonların arasına da çizgi çeker. Şeritler (`striped`) satırları birer atlayarak doldurur: uzun bir satırı gözle izlemeyi kolaylaştırır.

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
            label = "Şeritler",
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
    DataTableColumn.text(key = "tracking", header = "Takip no", width = 136.dp) { it.tracking },
    DataTableColumn.text(key = "destination", header = "Varış yeri", weight = 2f, minWidth = 160.dp) {
        it.destination
    },
    DataTableColumn.text(key = "carrier", header = "Taşıyıcı", width = 112.dp) { it.carrier },
    DataTableColumn.text(key = "eta", header = "Varış", width = 112.dp) { it.eta },
    DataTableColumn.text(key = "weight", header = "Ağırlık", width = 96.dp, alignment = Alignment.End) {
        "${it.weight.toString().replace('.', ',')} kg"
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
    Shipment("1Z 884 201", "Rotterdam, Hollanda", "UPS", "2 Kas", 12.4),
    Shipment("JD 014 772", "Lyon, Fransa", "DHL", "3 Kas", 3.1),
    Shipment("9400 1109", "Austin, ABD", "USPS", "4 Kas", 0.8),
    Shipment("CX 552 918", "Osaka, Japonya", "FedEx", "6 Kas", 21.0),
    Shipment("RR 330 845", "Krakov, Polonya", "DPD", "6 Kas", 6.5)
)
```

### Yüklenirken ve boşken

Tablo iki durumda da başlığını tutar. Yüklenirken (`loading`) satırlarının yerinde bloklar durur; satırı yokken `empty` ile verileni gösterir, burada neyin eksik olduğunu söyleyen bir söz ve başlamanın bir yolu; bir şey verilmezse temanın "Satır yok" sözünü. Birkaç satır yüksekliğinde kalır: onu boşaltan bir süzgeç sayfayı katlamaz.

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
        options = remember { Shown.entries.map { SegmentOption(it, it.label, tagKey = it.name.lowercase()) } },
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
        PantrixText(text = "Henüz dosya yok", style = PantrixTheme.typography.label)
        PantrixText(
            text = "Yüklediğin dosyalar burada listelenir.",
            style = PantrixTheme.typography.footnote,
            color = PantrixTheme.colors.textSecondary
        )
        PantrixButton(
            text = "Dosya yükle",
            onClick = {},
            variant = ButtonVariant.Outline,
            size = ButtonSize.Small,
            leadingIcon = { PantrixIcon(imageVector = PantrixIcons.Upload, contentDescription = null) },
            tagKey = "upload"
        )
    }
}

private fun fileColumns(): List<DataTableColumn<SharedFile>> = listOf(
    DataTableColumn.text(key = "name", header = "Ad", weight = 2f, minWidth = 200.dp) { it.name },
    DataTableColumn.text(key = "owner", header = "Sahibi", width = 140.dp) { it.owner },
    DataTableColumn.text(key = "modified", header = "Değişti", width = 128.dp) { it.modified },
    DataTableColumn.text(key = "size", header = "Boyut", width = 96.dp, alignment = Alignment.End) { it.size }
)

private enum class Shown(val label: String) {
    Rows("Satırlar"),
    Loading("Yükleniyor"),
    Empty("Boş")
}

private class SharedFile(
    val name: String,
    val owner: String,
    val modified: String,
    val size: String
)

private val FILES = listOf(
    SharedFile("Marka kılavuzu.pdf", "Naz Polat", "Bugün", "4,2 MB"),
    SharedFile("4. çeyrek yol haritası.key", "Selin Aydın", "Dün", "18,7 MB"),
    SharedFile("Fiyat çalışması.xlsx", "Burak Çelik", "24 Eki", "860 KB"),
    SharedFile("Karşılama akışı.fig", "Emre Koç", "21 Eki", "12,3 MB")
)
```

### Kayıtların listesi

Aynı siparişler, bir tablo ya da kayıtların listesi olarak: her sipariş kendi bloğu, her değer kolonunun adının altında. Tablo, liste olması söylendiğinde listedir (`DataTableLayout.List`). `Auto` seçimi tablonun kendi genişliğine bırakır: stilin `compactWidth`'inden, 600dp'den darsa liste, değilse tablo; geniş bir ekranın dar bir panelindeki tablo da listedir.

Liste tablonun yapabildiklerini korur: satır seçen kutuları ve kayıtların üstündeki bir şeritte, sıralanan kolonların adlarıyla sırayı.

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
    DataTableColumn.text(key = "id", header = "Sipariş", width = 96.dp) { "#${it.id}" },
    DataTableColumn.text(key = "customer", header = "Müşteri", weight = 2f, sortable = true) { it.customer },
    DataTableColumn(key = "status", header = "Durum", width = 128.dp) { order ->
        PantrixTag(text = order.status, tone = if (order.status == "Ödendi") TagTone.Success else TagTone.Warning)
    },
    DataTableColumn.text(
        key = "total",
        header = "Tutar",
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
    Order("3210", "Deniz Arslan", "Ödendi", 1999),
    Order("3209", "Elif Kaya", "Bekliyor", 390),
    Order("3208", "Mert Yıldız", "Ödendi", 2990),
    Order("3207", "Zeynep Demir", "Bekliyor", 990)
)

private fun money(amount: Int): String = amount.toString().reversed().chunked(3).joinToString(".").reversed() + " ₺"
```
