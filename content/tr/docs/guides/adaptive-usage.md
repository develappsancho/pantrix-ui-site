---
title: Hangi pencere yardımcısı, ne zaman
description: Bir ekran ya da bileşen yazarken pencere yardımcılarından hangisinin nasıl kullanılacağı üç örnek vaka üzerinden.
---

Buradaki her örnek, örnek uygulamada çalışır hâlde durur ve `AdaptiveSamplesTest` ile sınanır:

:::table
| Sayfa | Ne gösterir | Dosya | Web adresi |
| :--- | :--- | :--- | :--- |
| Giriş ekranı | Vaka 1: tanıtım ve giriş ekranı | `AdaptiveLoginSamples.kt` | `#adaptive-login` |
| Tablo | Vaka 2: tablo bileşeni | `AdaptiveTableSamples.kt` | `#adaptive-table` |
| Göster ve gizle | Vaka 3: göstermek, gizlemek, taşımak | `AdaptiveVisibilitySamples.kt` | `#adaptive-visibility` |
| Pencere sınıfları | Pencerenin ve her yardımcının o anki cevabı | `AdaptiveWindowSamples.kt` | `#adaptive-window` |
:::

Dosyalar `app/shared/src/commonMain/kotlin/com/pantrixui/cmp/demo/` altındadır. Sayfalar örnek uygulamada "Uyarlanabilir" bölümündedir; her sayfa başlığı ve açıklaması olan örneklerden oluşur ve her örneğin kodu yanındaki "Kod" görünümünde durur. Aşağıda tırnak içinde yazan adlar o örneklerin başlıklarıdır.

## 1. Yazmadan önce iki soru {#two-questions-before-writing}

### Ekran mı yazıyorum, bileşen mi?

*   **Ekran** (bir sayfa, bir akış): pencerenin tamamını kullanır, pencereye sorar. `PantrixTheme.window` ve `responsive(...)`.
*   **Bileşen** (tablo, kart, form, panel): nereye konacağı bilinmez. Geniş bir pencerede dar bir panelin içinde de durabilir. Pencereye değil **kendisine verilen yere** bakar: `BoxWithPantrixWindow` o yeri bir pencere olarak verir. Yardımcıların hepsi o kutunun üstünde de çalışır (4. bölüm).

Pencerenin tamamını kaplamayan bir ekran (yanında geniş bir sidebar varsa, bir panelin içinde açılıyorsa) için pencerenin sınıfı, ona kalan yeri olduğundan geniş gösterir. Öyle bir ekran da bileşen gibi davranır: kendi yerini ölçer.

### Pencereye mi bakıyorum, platforma mı, girdiye mi?

*   **"Yer var mı?"** sorusu pencerenindir: `isNarrow`, `isShort`, `widthClass`. Telefonda da, daraltılmış bir masaüstü penceresinde de aynı cevabı verir. "Mobil görünüm" denince kastedilen çoğu zaman budur.
*   **"Bu platformda anlamlı mı?"** sorusu platformundur: `PantrixTheme.platform.type`. "Uygulamayı indir" afişi yalnızca web'de, "mağazada puanla" bağlantısı yalnızca Android'de ve iOS'ta anlamlıdır; pencerenin boyutuna bağlı değildir.
*   **"Parmakla mı kullanılıyor, fareyle mi?"** sorusu girdinindir: `PantrixTheme.platform.isTouch`. Çekerek yenileme ipucu parmağa, klavye kısayolu ipucu fareye göredir. Bunu platformun türü söylemez: telefonun tarayıcısı da web'dir.

:::sketch adaptive-usage
:::

## 2. Hangi yardımcı, ne zaman {#which-helper-when}

:::table
| İhtiyaç | Yaz | Örnek |
| :--- | :--- | :--- |
| Genişliğe göre bir **değer**: sütun sayısı, boşluk, pay, yazı stili | `window.byWidth(...)` | `window.byWidth(compact = 2, medium = 4)` |
| Aynısı, pencere elde değilken, composable içinde | `responsive(...)` | `responsive(compact = 1, medium = 2, large = 3)` |
| Sayfanın kenar boşluğu | `PantrixTheme.layout.margin` | `Modifier.padding(horizontal = PantrixTheme.layout.margin)` |
| Sütunların ve panellerin arası | `PantrixTheme.layout.gutter` | `Arrangement.spacedBy(PantrixTheme.layout.gutter)` |
| Yüksekliğe göre bir değer | `window.byHeight(...)` | `window.byHeight(compact = 44.dp, medium = 56.dp)` |
| Katlanabilir duruşuna göre bir değer | `window.byPosture(...)` | `window.byPosture(flat = 1, book = 2, tabletop = 2)` |
| "Telefon düzeni mi?" | `window.isNarrow` | `if (window.isNarrow) BottomBar()` |
| "Alta çubuk sığar mı?" (yatay telefon) | `window.isShort` | `if (window.isShort) HeaderAction() else BottomBar()` |
| "Şu sınıftan itibaren" | `widthClass >= …` | `if (window.widthClass >= WindowWidthClass.Expanded) DetailPane()` |
| "Şu iki sınıf arasında" | `widthClass in a..b` | `window.widthClass in WindowWidthClass.Medium..WindowWidthClass.Expanded` |
| Sınıflara uymayan bir genişlik eşiği | `rememberWindowIsAtLeast(dp)` | `val threeColumns = rememberWindowIsAtLeast(1000.dp)` |
| Bir kez verilip saklanan bir karar | `window.isSizeKnown` | `if (window.isSizeKnown) App()` |
| Bileşenin kendi yeri | `BoxWithPantrixWindow { box -> … }` | `BoxWithPantrixWindow(modifier) { box -> if (box.isNarrow) List() else Table() }` |
| Elle sınıflanan bir boyut | `PantrixWindow(size, breakpoints = …)` | `PantrixWindow(paneSize, breakpoints = window.breakpoints)` |
| Ekranın bir parçasına başka bir pencere | `LocalPantrixWindow provides …` | 5. bölüm |
| Platforma özgü bir parça | `PantrixTheme.platform.type` | `if (platform == PlatformType.Web) DownloadBanner()` |
| Parmağa ya da fareye özgü bir parça | `PantrixTheme.platform.isTouch` | `if (platform.isTouch) PullToRefreshHint()` |
| Koşula bağlı bir modifier | `Modifier.thenIf(...)` | `Modifier.thenIf(window.isNarrow) { fillMaxWidth() }` |
:::

Kısa kural: **değer** gerekiyorsa `by…` ailesi, **evet/hayır** gerekiyorsa `is…` ya da sınıf karşılaştırması.

### Tek tek

*   **`byWidth(compact, medium, expanded, large, extraLarge)`**: en çok kullanılacak olan. Verilmeyen sınıf bir küçüğünün değerini alır; yalnızca düzenin değiştiği sınıflar yazılır. Değer her türden olabilir: sayı, `Dp`, `TextStyle`, pay (`Float`), bir enum.
*   **`byHeight(compact, medium, expanded)`**: yüksekliğin kıt olduğu yerde. En sık kullanımı yatay telefon içindir: satır aralığı, bar yüksekliği, bir çerçevenin boyu.
*   **`byPosture(flat, book, tabletop)`**: yalnızca katlanabilirlerde farklı bir değer. Verilmeyen duruş `flat`'in değerini alır, yani katlanmayan cihazlarda hiçbir şey değişmez.
*   **`isNarrow`**: genişlik sınıfı `Compact`. "Tek panel, navigasyon altta" düzeninin sorusu.
*   **`isShort`**: yükseklik sınıfı `Compact`. Pencere çoğu zaman geniştir (`Medium` ya da `Expanded`) ama alta ve üste çubuk dizecek yeri yoktur.
*   **`responsive(...)`**: `PantrixTheme.window.byWidth(...)` ile aynı şeydir, tek sözcükle. `val window = PantrixTheme.window` yazmak istemediğin, tek değer gereken composable'da kullan. Yalnızca genişliğe bakar ve yalnızca composable içinde çağrılır.
*   **`rememberWindowIsAtLeast(width)`**: bir ekranın, 600 / 840 / 1200 / 1600'e uymayan kendi eşiği. `window.size.width >= 1000.dp` yazma: o her boyutta yeniden kurar, bu yalnızca eşik aşılırken.
*   **`PantrixTheme.layout`**: sayfanın iki ölçüsü hazır gelir, elle seçilmez. `margin` sayfanın yanlardaki boşluğudur (16, 600 dp'den itibaren 24, 1200 dp'den itibaren 32 dp); `gutter` sütunların ve yan yana panellerin arasıdır (16, 840 dp'den itibaren 24 dp). Bileşen kendi kutusu için `PantrixTheme.layout(box)` diye sorar.
*   **`BoxWithPantrixWindow { box -> … }`**: bileşenin kendi yeri. Kutu, içeriğine verilen yeri bir pencere olarak söyler; yukarıdaki her şey `box` üstünde de çalışır. Sınırlar pencereninkilerdir ve içerik, okuduğu sınıf değişince yeniden kurulur (4. bölüm).
*   **`isSizeKnown`**: masaüstünde ve web'de içerik ilk kez, pencerenin boyutu gelmeden kurulur ve o an pencere `Compact` okunur. Ekranda bir şey yoktur, ama o ilk kompozisyonda verilip saklanan bir karar (başlangıç sayfası, `remember` içinde seçilen bir düzen) yanlış sınıfa göre verilmiş olur. Öyle bir kararın varsa içeriği `isSizeKnown`'a bağla.

### Örnek uygulamada nerede

:::table
| Yardımcı | Kullanıldığı yer |
| :--- | :--- |
| `byWidth` | Giriş ekranı: tanıtımın payı ve başlığın yazı stili. Pencere sınıfları: "Sınıfa göre sütun sayısı", "Bu pencerede bir düzenin seçeceği" |
| `byHeight` | Giriş ekranı: çerçevenin boyu, formun satır aralığı. Pencere sınıfları: "Yüksekliğe göre değer" |
| `byPosture` | Pencere sınıfları: "Katlanma duruşu" ve "Bu pencerede bir düzenin seçeceği" |
| `isNarrow` | Giriş ekranı: tanıtım. Tablo: liste ya da tablo (kutuya sorulur). Göster ve gizle: pencereye göre gösterilenler, düğme, sayaçlar. Pencere sınıfları: "Dar mı, kısa mı" |
| `isShort` | Giriş ekranı: tanıtımın maddeleri. Göster ve gizle: "Pencereye göre". Tablo: "Genişlik kutudan, yükseklik pencereden" |
| `isSizeKnown` | Pencere sınıfları: "Pencerenin boyutu" |
| `responsive` | Giriş ekranı: tanıtımın ve formun iç boşluğu. Pencere sınıfları: "Kısa yazım: responsive" |
| `PantrixTheme.layout.margin` | Her sayfanın yanlardaki boşluğu ve başlığın hizası (`DemoPage`, `DemoTopBar`). Pencere sınıfları: "Düzen ölçüleri" |
| `PantrixTheme.layout.gutter` | Pencere sınıfları: "Sınıfa göre sütun sayısı"nda kutuların arası, ve "Düzen ölçüleri". Tablo: "Kutunun ölçüleri" |
| `rememberWindowIsAtLeast` | Pencere sınıfları: "Sınıflara uymayan bir eşik" |
| Sınıf karşılaştırması ve aralık | Tablo: tarih sütunu. Göster ve gizle: "Pencereye göre" |
| `BoxWithPantrixWindow` | Tablo: bileşenin kendi kutusu, ve sayfanın öteki örnekleri |
| `PantrixWindow(size, breakpoints)` | Örnek kartlarındaki pencere seçicisi (gerçek pencerenin sınırlarıyla). Göster ve gizle: "Bir parçaya kendi penceresi". Tablo: "Kutunun sınırları" |
| `LocalPantrixWindow` | Örnek kartlarındaki pencere seçicisi. Göster ve gizle: "Bir parçaya kendi penceresi". Tablo: "Kutuyu içindekilere pencere olarak vermek" |
| `PantrixTheme.platform.type` | Göster ve gizle: "Platforma göre" |
| `PantrixTheme.platform.isTouch` | Göster ve gizle: "Girdiye göre" |
| `Modifier.thenIf` | Göster ve gizle: "Aynı bileşen, değişen değer" |
| `movableContentOf` | Göster ve gizle: "Aynı içerik, başka dizilim" |
:::

## 3. Örnek vaka 1: tanıtım ve giriş ekranı {#sample-case-1-a-marketing-and-sign-in-screen}

Solda tanıtım, sağda giriş formu. Dar pencerede tanıtım gider, form kalır.

```
Dar (dikey telefon)     Geniş                             Kısa (yatay telefon)
┌──────────────┐        ┌───────────────┬──────────────┐  ┌──────────────┬─────────────┐
│  Giriş yap   │        │ Ekibinle aynı │  Giriş yap   │  │ Ekibinle     │ Giriş yap   │
│  [e-posta ]  │        │ sayfada ol    │  [e-posta ]  │  │ aynı sayfada │ [e-posta ]  │
│  [şifre   ]  │        │ ✓ stok        │  [şifre   ]  │  │ ol           │ [şifre   ]  │
│  [Giriş yap] │        │ ✓ fatura      │  [Giriş yap] │  └──────────────┴─────────────┘
└──────────────┘        └───────────────┴──────────────┘   maddeler yok, form kayar
```

Bu bir ekrandır: pencereye sorar.

```kotlin
@Composable
fun SignInScreen(modifier: Modifier = Modifier) {
    val window = PantrixTheme.window
    Row(modifier = modifier.fillMaxSize()) {
        // Gelip giden, hatırlayacak bir şeyi olmayan bir ek: düz `if` yeter.
        if (!window.isNarrow) {
            MarketingPanel(
                modifier = Modifier
                    // Sınıfa göre bir değer: Large'dan itibaren tanıtımın payı büyür.
                    .weight(window.byWidth(compact = 1f, large = 1.25f))
                    .fillMaxHeight()
            )
        }
        // Form her düzende aynı çağrıdır.
        SignInForm(modifier = Modifier.weight(1f).fillMaxHeight())
    }
}
```

Tanıtımın içi:

```kotlin
Column(
    modifier = Modifier
        .fillMaxSize()
        // Pencere elde olmadan tek bir değer.
        .padding(responsive(compact = 16.dp, medium = 24.dp, large = 40.dp))
) {
    PantrixText(
        text = "Ekibinle aynı sayfada ol",
        // Yazı stili de sınıfa göre seçilen bir değerdir.
        style = window.byWidth(compact = typography.title, large = typography.headline)
    )
    // Yatay telefon: genişlik yeter, yükseklik yetmez. Maddeler gider.
    if (!window.isShort) {
        Benefits()
    }
}
```

Formun satır aralığı ve ekranın yüksekliği yüksekliğe göre seçilir:

```kotlin
verticalArrangement = Arrangement.spacedBy(window.byHeight(compact = spacing.sm, medium = spacing.md))
```

:::table
| Kullanılan | Ne için |
| :--- | :--- |
| `isNarrow` | Tanıtım gösterilsin mi |
| `byWidth` | Tanıtımın payı, başlığın yazı stili |
| `responsive` | İç boşluklar |
| `isShort` | Yatay telefonda maddeleri kaldırmak |
| `byHeight` | Satır aralığı, ekranın boyu |
:::

Form genişlemez: `widthIn(max = 360.dp)` ile en çok 360 dp'dir ve kendi yarısının ortasında durur. Her şey sınıfla çözülmez; okunacak ve doldurulacak içerik için bir üst genişlik çoğu zaman yeter.

### Bu örneğin asıl dersi: form tek yerden çağrılır

Compose bir composable'ın hatırladıklarını **çağrıldığı yere** bağlar. Yukarıda `SignInForm()` her düzende aynı satırdır; tanıtım gelip giderken e-posta alanına yazılan metin yerinde kalır. Şöyle yazılsaydı kaybolurdu:

```kotlin
// YANLIŞ: form iki ayrı yerden çağrılıyor.
if (window.isNarrow) {
    SignInForm()
} else {
    Row {
        MarketingPanel()
        SignInForm()      // başka bir çağrı yeri: pencere genişleyince form sıfırdan başlar
    }
}
```

Kullanıcı telefonu çevirdiğinde ya da pencereyi büyüttüğünde yazdığı e-posta silinir. `what_is_typed_stays_as_the_window_changes_its_class` testi doğru yazımı tutar; formu iki yerden çağırınca kırmızıya döner.

Formun alanları `rememberSaveable` ile hatırlar: Android Activity'yi yeniden yarattığında da (koyu temaya geçiş, dil değişimi) yazılan, çağrı yeri aynı kaldığı için geri gelir (6. bölüm).

## 4. Örnek vaka 2: tablo bileşeni {#sample-case-2-a-table-component}

Yer genişken düz tablo, dar yerde liste.

```
Dar yer (< 600 dp)            Geniş yer (≥ 600 dp); ≥ 840 dp'de tarih sütunu da gelir
┌────────────────────────┐    ┌────────┬──────────────┬──────────┬────────┐
│ Ayşe Yılmaz   1.240 TL │    │ Sipariş│ Müşteri      │ Durum    │  Tutar │
│ #1042 · 02.10  [Hazır] │    ├────────┼──────────────┼──────────┼────────┤
├────────────────────────┤    │ #1042  │ Ayşe Yılmaz  │ Hazır    │  1.240 │
│ Mehmet Demir    380 TL │    │ #1041  │ Mehmet Demir │ Kargoda  │    380 │
│ #1041 · 02.10 [Kargo]  │    └────────┴──────────────┴──────────┴────────┘
└────────────────────────┘
```

Bu bir bileşendir: **pencereye değil kendi yerine bakar.** Aynı tablo geniş bir pencerede dar bir panelin içine konabilir; pencereye sorsaydı orada da tablo çizer ve sığmazdı. Örnek sayfada aynı bileşen iki kez durur, bir kez sayfanın genişliğinde ve bir kez 320 dp'lik bir kutuda: geniş pencerede üstteki tablo, alttaki listedir.

```kotlin
@Composable
fun OrdersTable(
    orders: List<Order>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Bileşenin kendi kutusu, pencere gibi: yardımcıların hepsi onun üstünde çalışır.
    BoxWithPantrixWindow(modifier = modifier) { box ->
        if (box.isNarrow) {
            OrderList(orders, selectedId, onSelect)
        } else {
            // Tarih sütunu yalnızca 840 dp'den geniş yerde.
            OrderGrid(orders, selectedId, onSelect, showDate = box.widthClass >= WindowWidthClass.Expanded)
        }
    }
}
```

`BoxWithPantrixWindow`, `BoxWithConstraints`'in üstüne kurulu bir kutudur: içeriğine alabileceği en fazla yeri bir `PantrixWindow` olarak verir.

*   `box.isNarrow`, `box.byWidth(...)`, `box.widthClass >= …`: ekranın pencereye sorduğu her şey kutuya sorulur.
*   **Sınırlar pencereninkilerdir.** Ürün pencereyi kendi sınırlarıyla sınıflıyorsa (`rememberPantrixWindow(breakpoints = …)`) kutu da onlarla sınıflanır; bir şey yazmak gerekmez. Bileşenin kendi eşikleri varsa `breakpoints` parametresiyle verilir.
*   **İçerik her boyutta yeniden kurulmaz.** `BoxWithConstraints`'in içeriği kutu her piksel değiştiğinde yeniden kurulur; buradaki içerik, okuduğu sınıf değişince kurulur. Yukarıdaki tablo, kutunun genişlik sınıfı değiştiğinde yeniden kurulur: dört sınırda, her pikselde değil.
*   Farkı "Tablo" sayfasının ilk örneği gösterir ("Aynı bileşen, iki ayrı yerde"): aynı tablo sayfanın genişliğinde bir kez, 320 dp'lik bir kutuda bir kez daha durur. Pencere ikisi için de aynıdır; kutudaki, pencere ne kadar geniş olursa olsun listedir. Pencereye sorsaydı 320 dp'ye dört sütun dizerdi.
*   Kaydırılan bir sayfanın içinde kutunun yüksekliği sınırsızdır (`Dp.Infinity`): yükseklik sınıfı `Expanded` okunur ve `box.isShort` hiçbir zaman `true` olmaz. Bileşen genişlik için kutuya, yükseklik için pencereye sorar.
*   Kutu düzdür: menteşe pencerenindir. Duruşa göre karar verecek bileşen `PantrixTheme.window.posture`'a sorar.
*   `PantrixTheme.window` ve `responsive(...)` kutunun içinde de pencereye sorar. İçteki her şey kutuya sorsun istersen kutuyu onlara ver: `CompositionLocalProvider(LocalPantrixWindow provides box) { … }`.
*   `BoxWithConstraints` gibi içeriğini ölçülürken kurar: `IntrinsicSize` ile boyutlanan bir satırın ya da sütunun içine konmaz.

Aynı şey elle de yazılır; kutunun yaptığı budur, üstüne sınırları pencereden alır ve içeriği gereksiz yere yeniden kurmaz:

```kotlin
BoxWithConstraints(modifier) {
    val box = PantrixWindow(DpSize(maxWidth, maxHeight), breakpoints = PantrixTheme.window.breakpoints)
    …
}
```

### Seçim bileşenin dışında durur

Tablo ile liste iki ayrı görünümdür ve farklı çağrı yerleridir. Seçili satır onların içinde hatırlansaydı görünüm değişince kaybolurdu. Seçim dışarıdan gelir ve dışarıya bildirilir:

```kotlin
var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
OrdersTable(orders = orders, selectedId = selectedId, onSelect = { selectedId = it })
```

Kural: **iki görünüm arasında yaşaması gereken durum, iki görünümün üstünde tutulur.** Örnek sayfada listedeki bir karta basınca aynı sipariş üstteki tabloda da seçili görünür.

### Tablo hep pencereyi dolduruyorsa

Bileşen yalnızca pencerenin tam genişliğinde kullanılacaksa pencereye sormak da çalışır ve daha kısadır:

```kotlin
if (PantrixTheme.window.isNarrow) OrderList(…) else OrderGrid(…)
```

Bedeli, bileşenin bir panelin içine ya da bir yan menünün yanına konduğu gün yanlış görünümü çizmesidir. Yeniden kullanılacak bir bileşende kutuyu ölç.

## 5. Örnek vaka 3: göstermek ve gizlemek {#sample-case-3-showing-and-hiding}

Bir parçanın yalnızca mobilde, ya da yalnızca masaüstünde ve web'de görünmesi.

### Pencereye göre

Düz `if` yeter:

```kotlin
val window = PantrixTheme.window

if (window.isNarrow) StickyAddToCartBar()                                  // yalnızca dar pencerede
if (!window.isNarrow) HeaderAction()                                       // dar olmayan her pencerede
if (window.widthClass >= WindowWidthClass.Expanded) DetailPane()           // Expanded'dan itibaren
if (window.widthClass in WindowWidthClass.Medium..WindowWidthClass.Expanded) LabelledRail()
if (window.isShort) CompactTopBar()                                        // yatay telefonda
```

:::table
| Pencere | Görünenler |
| :--- | :--- |
| Dikey telefon (402 × 874) | yalnızca dar |
| Dikey tablet (820 × 1180) | dar olmayan, Medium–Expanded |
| Yatay telefon (874 × 402) | dar olmayan, Expanded'dan itibaren, Medium–Expanded, yalnızca kısa |
| Masaüstü (1280 × 800) | dar olmayan, Expanded'dan itibaren |
:::

### Platforma göre

```kotlin
val platform = PantrixTheme.platform.type

if (platform == PlatformType.Web) DownloadAppBanner()
if (platform == PlatformType.Android || platform == PlatformType.Ios) RateInStoreLink()
```

Geniş bir Android tableti de Android'dir; daraltılmış bir tarayıcı penceresi de web'dir. "Mobilde görünsün" derken hangisini kastettiğine karar ver: çoğu zaman pencere, bazen platform.

### Girdiye göre

Parmakla mı kullanılıyor, fareyle mi: bunu platformun türü söylemez. Android ve iOS hep parmakladır, masaüstü hep fareyledir; web ise ikisi de olur.

```kotlin
val platform = PantrixTheme.platform

if (platform.isTouch) PullToRefreshHint()          // Android, iOS, telefon ve tablet tarayıcısı
if (!platform.isTouch) KeyboardShortcutsHint()     // masaüstü, masadaki tarayıcı
```

:::table
| Nerede | `platform.type` | `platform.isTouch` | Dokunma hedefi |
| :--- | :--- | :--- | :--- |
| Android telefon ve tablet | `Android` | evet | 48 dp |
| iPhone ve iPad | `Ios` | evet | 44 dp |
| Masaüstü uygulaması | `Desktop` | hayır | 32 dp |
| Masadaki tarayıcı | `Web` | hayır | 32 dp |
| Telefondaki tarayıcı | `Web` | evet | 48 dp |
:::

Dokunma hedefini bileşenler temadan kendileri alır; ekran yazarken bir şey yapmak gerekmez. Tarayıcıda cevap canlıdır: tablete bir trackpad takılınca değişir. Ayrıntısı [ADAPTIVE_GUIDE.md](/docs/guides/adaptive)'nin 11. bölümündedir.

### Gizlemek yerine değiştirmek

Aynı bileşen iki düzende de varsa onu gizleyip başka bir yerde yeniden çağırma; ona verilen değeri değiştir:

```kotlin
PantrixButton(
    text = "Devam et",
    onClick = onContinue,
    // Dar pencerede satırı doldurur, genişte kendi genişliğindedir.
    modifier = Modifier.thenIf(window.isNarrow) { fillMaxWidth() }
)
```

### Gizlenen parçanın durumu

`if`'in içindeki bir parça gizlenince kompozisyondan çıkar: yer tutmaz, okunmaz, **hatırladıklarını da bırakır**. Bir ek için (ipucu, afiş, ikinci bir düğme) bu sorun değildir. Aynı içerik için sorundur:

```kotlin
// YANLIŞ: iki ayrı sayaç. Sınıf değişince sayı sıfırlanır.
if (window.isNarrow) Counter(label = "dar") else Counter(label = "geniş")

// DOĞRU: tek sayaç, değişen etiket. Sayı korunur.
Counter(label = if (window.isNarrow) "dar" else "geniş")
```

### İçeriğin yeri değişmek zorundaysa: taşı

Dar pencerede alt alta, genişte yan yana duran içerik için tek bir çağrı yeri yetmez: `Column` ile `Row` iki ayrı yerdir. `movableContentOf` içeriği **taşır**; aynı kompozisyon yeni yerine gider ve hatırladıkları yerinde kalır:

```kotlin
// Bir kez kurulur ve hatırlanır; çağrıldığı yer değişince içerik oraya taşınır.
val counters = remember {
    movableContentOf {
        Counter(label = "Taşınan A")
        Counter(label = "Taşınan B")
    }
}
if (window.isNarrow) {
    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) { counters() }
} else {
    Row(horizontalArrangement = Arrangement.spacedBy(spacing.md)) { counters() }
}
```

`movableContentOf` `androidx.compose.runtime` paketindedir, Compose'un kendi aracıdır. Aynı satırı düz bir lambda ile yazmak sayıları sıfırlar; `content_moved_from_a_column_into_a_row_keeps_its_count` testi bunu tutar.

Taşınan içerik de kompozisyon yaşadıkça hatırlar. Android Activity'yi yeniden yarattığında ondan geriye yalnızca `rememberSaveable` ile kaydettiği kalır (6. bölüm).

### Örnek kartındaki pencere seçicisi

Dört sayfada da pencereyi okuyan her örneğin kartında bir pencere seçicisi vardır (Gerçek / Dar / Orta / Kısa / Geniş) ve o örneğe kendi penceresini verir; öteki örnekler kendi pencerelerinde kalır. Pencere sınıfları › "Katlanma duruşu"nda seçici yoktur: sabit pencereler düzdür, duruş yalnızca cihazdan okunur. "Dar" 360 × 800 dp'lik bir telefon, "Orta" 768 × 1024 dp'lik dik bir tablet, "Kısa" 880 × 400 dp'lik yatay bir telefon, "Geniş" 1280 × 800 dp'lik bir masaüstü penceresidir; dördü dört ayrı genişlik sınıfına düşer (`Compact`, `Medium`, `Expanded`, `Large`). "Gerçek" uygulamanınkidir; seçicinin altındaki "Şu an" satırı örneğe verilen pencerenin sınıflarını yazar. Örnek seçilen pencerenin genişliğinde kurulur. Karta sığmayan pencere sıkıştırılmaz: küçültülerek bütünüyle çizilir ve satır ölçeği söyler; %60'tan küçük çizilmesi gerekiyorsa kendi boyutunda kalır, çerçevesi yana kayar ve altında bir kaydırma çubuğu çıkar. Telefonda geniş pencerenin, masaüstünde dar pencerenin hâli pencereye dokunmadan görülür. "Gizlenince durum ne olur" örneğinde sayaçları artırıp seçicide "Dar" ile "Geniş" arasında geçince "iki yerden" çağrılan sıfırlanır, "tek yerden" çağrılan kalır; "Aynı içerik, başka dizilim"de taşınanlar da kalır.

```kotlin
// Sabit bir pencere, gerçeğinin sınırlarıyla sınıflanır.
val wide = PantrixWindow(DpSize(1280.dp, 800.dp), breakpoints = PantrixTheme.window.breakpoints)
CompositionLocalProvider(LocalPantrixWindow provides wide) {
    Examples()   // içindeki her `PantrixTheme.window` ve `responsive(...)` bu pencereye sorar
}
```

Aynı teknik, bir önizlemeyi ya da bir testi belli bir boyutta kurmak için de kullanılır. Platform ve girdi bundan etkilenmez: "Platforma göre" ve "Girdiye göre" örneklerinin bu yüzden seçicisi yoktur.

## 6. Durumu korumak {#keeping-the-state}

Pencere sınıf değiştirdiğinde düzen değişir. İçeriğin hatırladıkları (yazılan metin, seçim, sayaç) şu üç yoldan biriyle korunur:

:::table
| Yol | Ne zaman | Örnek |
| :--- | :--- | :--- |
| **Tek çağrı yeri**: içerik hep aynı satırdan çağrılır, değişen ona verilen değerdir | Yapı aynı kalıyorsa: yanına bir şey gelip gidiyor, boyutu ya da etiketi değişiyor | Giriş formu, "tek yerden" sayaç |
| **Durumu yukarıda tutmak**: durum iki görünümün üstünde durur, onlara parametreyle iner | İki ayrı görünüm aynı veriyi gösteriyorsa | Tablonun ve listenin seçili siparişi |
| **Taşımak**: `movableContentOf` | Aynı içerik başka bir ebeveynin içine geçmek zorundaysa | Alt alta ve yan yana duran sayaçlar |
:::

Önce ilkini dene; en ucuzu odur ve hiçbir şey taşınmaz. İkincisinde görünümlerin kendi içinde hatırladıkları (kaydırma konumu gibi) yine sıfırlanır: yalnızca yukarıda tutulan durum korunur.

### Android: activity yeniden yaratılınca ne kalır

Masaüstünde, web'de ve iOS'ta pencerenin boyutu değişince kompozisyon yaşamaya devam eder; yukarıdaki üç yol yeter. Android'de döndürme, katlama ve çoklu pencerede boyut değişimi birer yapılandırma değişimidir: uygulama bunları üstlenmediyse Activity, kompozisyonla birlikte yeniden yaratılır ve geriye yalnızca **kaydedilen** durum kalır.

**Örnek uygulama pencere değişimlerini üstlenir.** Manifestindeki satır şudur:

```xml
android:configChanges="orientation|screenSize|smallestScreenSize|screenLayout|density"
```

Bu satırla Android de öteki platformlar gibi davranır: cihaz katlanınca ya da dönünce Activity yaşar, `PantrixTheme.window` yeni sınıfı söyler ve içerik yalnızca yeniden kurulur. Listede olmayan değişimlerde (koyu tema, dil, yazı boyutu, klavye) ve sistem süreci öldürüp geri getirdiğinde Activity yine yeniden yaratılır; koyu tema bilerek dışarıda bırakıldı, çünkü `enableEdgeToEdge` sistem çubuklarının ikon rengini `onCreate`'te seçer.

:::table
| Durum nerede | Kompozisyon yaşarken sınıf değişir | Activity yeniden yaratılır |
| :--- | :--- | :--- |
| `remember`, tek çağrı yeri | Korunur | **Gider** |
| `rememberSaveable`, tek çağrı yeri | Korunur | Korunur |
| `rememberSaveable`, iki çağrı yeri | Sıfırlanır | Sıfırlanır |
| `movableContentOf` içinde `remember` | Korunur | **Gider** |
| `movableContentOf` içinde `rememberSaveable` | Korunur | Korunur |
:::

*   Kalması gereken durumu `rememberSaveable` ile tut (ya da ekranın üstünde, bir ViewModel'de): pencere değişimleri üstlenilmiş olsa da tema ve dil değişimi ile süreç ölümü Activity'yi yeniden yaratır. Çağrı yeri kuralı onun için de geçerlidir: kaydedilen değer çağrıldığı yere göre geri gelir.
*   İki çağrı yerinin her birinin kendi kaydı vardır. Pencere değişimlerini üstlenmeyen bir uygulamada cihazı katlayıp açınca "iki yerden" sayaç 2 → 0 → 2 gösterir: dar düzenin sayacı ile geniş düzeninki iki ayrı sayaçtır. Kompozisyon yaşarken aynı sayaç 2 → 0 → 0 gösterir.
*   Örnek sayfalardaki form, tablonun seçimi, pencere seçicisi ve sayaçlar bu yüzden `rememberSaveable` kullanır.
*   Tablonun iki sütununa da 3 Ekim 2026'da katlanabilir emülatörde bakıldı: sağdaki sütuna manifest satırı eklenmeden önce cihaz katlanıp açılarak, sonra da koyu temaya geçilerek; soldakine satır eklendikten sonra katlayıp açarak ve döndürerek.

## 7. Tuzaklar {#pitfalls}

:::table
| Tuzak | Ne olur | Doğrusu |
| :--- | :--- | :--- |
| Aynı içeriği sınıfa göre iki yerden çağırmak | Sınıf değişince durum (yazılan metin, seçim, kaydırma) sıfırlanır | Tek yerden çağır, değişeni parametreyle ver; yeri değişecekse taşı |
| İki görünümün (tablo / liste) ortak durumunu içlerinde tutmak | Görünüm değişince kaybolur | Durumu ikisinin üstünde tut |
| Kalması gereken durumu `remember` ile tutmak | Android Activity'yi yeniden yaratınca gider: tema ve dil değişimi, süreç ölümü; pencere değişimlerini üstlenmeyen uygulamada döndürme ve katlama da | `rememberSaveable` ya da bir ViewModel |
| Bileşende pencereye sormak | Dar bir panelde geniş düzeni çizer | `BoxWithPantrixWindow` ile kendi kutusuna sor |
| Kutuyu elle kurarken sınırları unutmak: `PantrixWindow(DpSize(maxWidth, maxHeight))` | Ürünün kendi sınırları bileşene ulaşmaz | `BoxWithPantrixWindow`, ya da `breakpoints = window.breakpoints` |
| Sayfa kenar boşluğunu elle seçmek: `responsive(16.dp, 24.dp, 32.dp)` | Her ekran kendi değerini seçer, ürünün ölçüsü tek yerden değişmez | `PantrixTheme.layout.margin` |
| `window.size`'ı düzen kararı için okumak | Her pikselde yeniden kurulur | Sınıfı oku; özel eşik için `rememberWindowIsAtLeast` |
| Sınıfı `remember` ile saklamak | Masaüstünde ve web'de ilk kompozisyonun `Compact`'ı kalır | Her seferinde pencereden oku; saklanacaksa `isSizeKnown`'ı bekle |
| "Mobil" için platforma bakmak | Daraltılmış masaüstü penceresi masaüstü düzeninde kalır | Yer için pencereye, platforma özgü şey için platforma bak |
| "Dokunmatik" için platformun türüne bakmak | Telefonun tarayıcısı web'dir: parmağa göre olan şey orada görünmez | `PantrixTheme.platform.isTouch` |
| `isNarrow`'u "telefon" sanmak | Yatay telefon `isNarrow` değildir (874 dp, `Expanded`) | Yatay telefon için `isShort` |
| Kaydırılan sayfadaki kutuya `isShort` sormak | Hep `false`: kutunun yüksekliği sınırsızdır | Yükseklik için pencereye sor |
:::

## 8. Test etmek {#testing}

Testin kendi penceresi hep aynı boyuttadır (Skiko'da 1024 × 768, yani `Expanded`). Bir düzeni belli bir pencerede sınamak için pencereyi temaya ver; pencere bir durumsa test içinde değiştirilebilir:

```kotlin
var window by mutableStateOf(PantrixWindow(DpSize(402.dp, 874.dp)))
setContent {
    PantrixTheme(darkTheme = false, window = window) { SignInScreen(Modifier.fillMaxSize()) }
}
onNodeWithTag("TextField_sign_in_email").performTextInput("ada@pantrix.io")

window = PantrixWindow(DpSize(1280.dp, 800.dp))        // pencere genişledi
waitForIdle()
onNodeWithTag("TextField_sign_in_email").assertTextContains("ada@pantrix.io")
```

Kendi yerine bakan bir bileşene pencere değil kutu verilir:

```kotlin
var width by mutableStateOf(320.dp)
setContent {
    PantrixTheme(darkTheme = false) {
        Box(Modifier.width(width)) { OrdersTable(ORDERS, selectedId = null, onSelect = {}) }
    }
}
// 320 dp: liste. Sonra `width = 700.dp`: tablo, tarih sütunu yok. `width = 900.dp`: tarih sütunu da var.
```

Ürünün kendi sınırlarının bileşene ulaştığı da böyle sınanır: pencere o sınırlarla verilir, kutu aynı kalır.

```kotlin
// Medium 300 dp'de başlıyorsa 320 dp'lik kutu liste değil tablodur.
val window = PantrixWindow(DpSize(1280.dp, 800.dp), breakpoints = PantrixBreakpoints(medium = 300.dp))
PantrixTheme(darkTheme = false, window = window) {
    Box(Modifier.width(320.dp)) { OrdersTable(ORDERS, selectedId = null, onSelect = {}) }
}
```

Platforma ve girdiye göre gösterilenler için platform da temaya verilir:

```kotlin
PantrixTheme(darkTheme = false, platform = PantrixPlatform.forType(PlatformType.Android)) { … }

// Telefondaki tarayıcı: platform web, kullanım parmakla
PantrixTheme(darkTheme = false, platform = PantrixPlatform.forType(PlatformType.Web, isTouch = true)) { … }
```

Android'in Activity'yi yeniden yaratması JVM testinde de görülür: içerik, testin kendi `SaveableStateRegistry`'siyle kaydedilir, indirilir ve kaydedilenden yeniden kurulur. `AdaptiveSamplesTest`'teki `SavedScreen` bunu yapar (Compose'un `StateRestorationTester`'ı Android dışında çalışmaz):

```kotlin
val screen = SavedScreen()
var window by mutableStateOf(DESKTOP)
setContent {
    screen.Content {
        PantrixTheme(darkTheme = false, window = window) { ComponentExamples(DemoComponent.AdaptiveVisibility) }
    }
}
// … sayaçlar artırılır …
screen.buildAgain(this) { window = PHONE }             // yeniden yaratılan Activity, bu kez dar pencerede
onNodeWithText("Tek yerden (dar): 2").assertExists()   // kaydedilen, çağrı yeri aynı: geri geldi
onNodeWithText("İki yerden (dar): 0").assertExists()   // başka bir çağrı yeri: sıfırdan
```

Örneklerin testleri `app/shared/src/jvmTest/kotlin/com/pantrixui/cmp/demo/AdaptiveSamplesTest.kt` dosyasındadır:

```bash
./gradlew :app:shared:jvmTest --tests 'com.pantrixui.cmp.demo.AdaptiveSamplesTest'
```
