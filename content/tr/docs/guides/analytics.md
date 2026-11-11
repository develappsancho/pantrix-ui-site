---
title: Olayları analitiğe bağlamak
description: Pantrix'in bileşenlerinin hangi olayları bildirdiği, bir olayın neyi taşıyıp neyi asla taşımadığı ve bir uygulamanın bu olayları kendi analitik ürününe (Firebase, Amplitude, PostHog…) nasıl bağladığı. Sorun Pantrix'e özgü değildir: analitiği olan her Compose Multiplatform uygulaması, arayüzün olaylarını ürüne giden olaylara çeviren bir sınıf yazar ve asıl kararlar o sınıftadır.
---

Bileşenler ve örnek hakkında söylenen her şey bu depoda koşan testlerde ya da cihazlarda görülmüştür. Ürünlerin çağrıları kendi belgelerinden okunmuştur ve burada çalıştırılmamıştır. Hangisinin nerede görüldüğü 7. bölümde yazılıdır.

## 1. Sorun {#problem}

Bir bileşen kütüphanesi "şu düğmeye basıldı" der. Ürüne ne gideceği ise uygulamanın kararıdır:

*   **Hangi adla:** ürün `Button click` mi görür, `checkout_pay_tapped` mı? Adlar ürünün raporlarında yıllarca yaşar ve bir bileşenin adı değişince değişmemelidir.
*   **Neyi asla taşımadan:** kullanıcının yazdığı bir metni, kişi adıyla anahtarlanmış bir tablo satırını. Bir kez gönderilen kişisel veri geri alınamaz.
*   **Ne zaman:** her basışta bir ağ isteği pil ve veri harcar. Uygulama arka plana geçince bekleyen olaylar kaybolabilir.
*   **Ağ yokken:** olaylar atılır mı, sırası korunur mu, kuyruk ne kadar büyür?
*   **İzinle:** kullanıcı evet demeden hiçbir şey toplanmamalıdır. Hayır deyince bekleyenler de gitmemelidir.
*   **Sürümde:** otomasyon etiketleri sürüm derlemesinde semantik ağaçtan çıkarılır, ama olaylar bileşenleri birbirinden ayırmaya devam etmelidir.

Bu kararlar tek bir yerde verilmezse her ekran kendi olayını kendi adıyla gönderir, kişisel anahtarlar ürüne sızar ve bir ağ kesintisi raporlarda boşluk bırakır.

:::sketch analytics
:::

## 2. Pantrix'in olayları {#pantrix-s-events}

### Olay

Her bileşen kullanıcının yaptığını bir `PantrixUiEvent` olarak temanın analitiğine verir:

:::table
| Alan | Ne | Örnek |
| :--- | :--- | :--- |
| `component` | Bileşenin bildirildiği ad | `Button`, `SegmentButton`, `Chart` |
| `action` | Ne oldu | `click`, `select`, `toggle`, `open` |
| `testTag` | Bileşenin otomasyon etiketi | `Checkout.Button_pay` |
| `screen` | `TestScope` zinciri, yani ekranın adı | `Checkout` |
| `props` | Kapalı bir kümeden değerler | `variant=Solid`, `segment=express` |
:::

```kotlin
PantrixTheme(analytics = PantrixAnalytics { event -> /* bağlayıcıya ver */ }) {
    App()
}
```

*   Olay olduğu anda `track` UI iş parçacığında çağrılır. Olayda zaman alanı yoktur: zamanı bağlayıcı damgalar. `track` olayı alır ve döner, gönderim UI iş parçacığının dışında yapılır.
*   Temaya analitik verilmezse olaylar atılır (`PantrixAnalytics.NoOp`).
*   Uygulamanın kendi olayı da aynı yere gider: `LocalPantrixAnalytics.current.track(PantrixUiEvent(…))`.

### Kimlik: etiket ve ekran

*   `testTag` bileşenin `tagKey`'inden kurulur: `TestScope("Checkout")` içindeki `PantrixButton(tagKey = "pay")` `Checkout.Button_pay` olur. Bir test ya da bir plan aynı etiketi ekran kurulmadan `pantrixTestTagOf("Button", "pay", scope = "Checkout")` ile bulur.
*   `tagKey`'i olmayan bir bileşenin etiketi çağrının yerinden kurulur (`Button#1k3z9`) ve kod değişince değişir. Ürünün saydığı bir bileşene `tagKey` verilir.
*   **Sürüm derlemesi:** `TestTagPolicy.Off` etiketleri semantik ağaca koymaz. Bileşen etiketini yine bilir ve olayları o etiketle gider. Uygulamanın kendi koyduğu bir `Modifier.testTag` olayı değiştirmez.
*   `screen` `TestScope` zincirinden gelir ve etiket politikasından bağımsızdır.
*   **Bir sayfa bir ekrandır.** `tagKey`'i verilen bir `PantrixPageScaffold` tuttuklarını `TestScope(tagKey)` içine koyar: üstündeki bileşenlerin olayları `orders` ekranındadır, etiketleri `orders.` ile başlar.

### Olaya asla girmeyenler

*   **Yazılan ve okunan metin.** Bir alana yazılan, bir alanda duran ya da bir bileşenin gösterdiği hiçbir metin olaya girmez. Bu söz, metin alan her bileşende bir regresyon testiyle tutulur (`TypedTextIsNeverReportedTest`). Kapsadığı bileşenler: `PantrixTextField`, `PantrixFilledTextField`, `PantrixAutoResizeTextField`, `PantrixFilledAutoResizeTextField`, `PantrixSecureTextField`, `PantrixFilledSecureTextField`, `PantrixDateTextField`, `PantrixDateFilledTextField`, `PantrixTimeTextField`, `PantrixTimeFilledTextField`, `PantrixSelect`'in ve `PantrixMultiSelect`'in arama alanı, `PantrixCommandPalette`, `PantrixElasticDatePicker`'ın alanları, `PantrixJsonView`'in belgesi ve araması. Test her alana bir sözcük yazar, sonra yerine bir sayı yazar, alanı klavye eylemiyle ve odağı başka yere vererek bırakır. Sonunda hiçbir olayın hiçbir alanında ikisinin de olmadığını doğrular.
*   **Serbest bir değer.** Olayın `props`'u yalnızca aşağıdaki üç türden değer taşır. Kullanıcının kendisinin yazdığı tek değer `PantrixPageJump`'ın sayfa numarasıdır, o da sayfalamanın aralığındaki bir sayıdır.

### Özelliklerin üç türü

:::table
| Tür | Anahtarlar | Kişisel mi |
| :--- | :--- | :--- |
| Pantrix'in sözcükleri ve doğru/yanlış değerleri (20) | `action`, `checked`, `collapsed`, `copied`, `destructive`, `direction`, `duration`, `external`, `folded`, `format`, `part`, `reselected`, `revealed`, `selected`, `shown`, `state`, `to`, `tone`, `variant`, `via` | Hayır: değerleri kütüphanenin kendi sözcükleridir (`Solid`, `back`, `true`) |
| Sayılar (8) | `block`, `cell`, `count`, `index`, `level`, `rejected`, `rows`, `size` | Hayır |
| Uygulamanın anahtarları (14) | `row`, `column`, `option`, `node`, `series`, `slice`, `stage`, `task`, `tile`, `destination`, `section`, `page`, `command`, `segment` | **Olabilir:** uygulamanın bir şeye verdiği anahtardır |
:::

`page` iki türdendir. `PantrixCarousel`'de, `PantrixPagination`'da ve `PantrixPageJump`'ta bir sayıdır, `PantrixBreadcrumb`'ta ise uygulamanın sayfaya verdiği anahtardır. Örnek bu yüzden onu Pantrix'in değerlerine katmaz.

Uygulamanın anahtarlarını gönderen bileşenler:

:::table
| Anahtar | Bileşen | Değeri |
| :--- | :--- | :--- |
| `row`, `column` | `PantrixDataTable` | Satırın ve sütunun anahtarı |
| `option` | `PantrixSelect`, `PantrixMultiSelect` | Seçeneğin anahtarı |
| `node` | `PantrixTreeView`, `PantrixLazyTreeView`, `PantrixSankeyChart` | Düğümün anahtarı; Sankey'de bir akış `kaynak>hedef` |
| `series` | Eksenli grafikler, `PantrixScatterChart`, `PantrixRadarChart`, `PantrixPolarChart` | Serinin anahtarı |
| `slice` | `PantrixPieChart` | Dilimin anahtarı |
| `stage` | `PantrixFunnelChart` | Aşamanın anahtarı |
| `task` | `PantrixGanttChart` | Görevin anahtarı |
| `tile` | `PantrixTreemap` | Karonun anahtarı |
| `destination` | `PantrixNavigationBar`, `PantrixNavigationRail`, `PantrixSidebar` | Hedefin anahtarı |
| `section` | `PantrixAccordion`, `PantrixSidebar` | Bölümün anahtarı |
| `page` | `PantrixBreadcrumb` | Sayfanın anahtarı |
| `command` | `PantrixCommandPalette` | Komutun anahtarı |
| `segment` | `PantrixSegmentButton` | Seçeneğin `tagKey`'i, yoksa sırası |
:::

Kütüphane bu anahtarları temanın analitiğine her zaman verir; ürüne gidip gitmeyecekleri bağlayıcının kararıdır. Bileşenlerin testlerinde `row`'un değerleri kişi adlarıydı (`ada`, `grace`): kullanıcılarını adıyla anahtarlayan bir tablonun her olayı o adı taşır.

### Kullanıcı bir şey yapmadan gelen olaylar

:::table
| Bileşen | Olay | Ne zaman |
| :--- | :--- | :--- |
| `PantrixCarousel` | `change`, `via=auto` | Kendi kendine döndüğünde |
| `PantrixSnackbarHost` | `dismiss`, `via=timeout` | Süresi dolduğunda |
| `PantrixElasticDatePicker` | `autoRefresh` | Kendiliğinden yenilediğinde |
| `PantrixDialog`, `PantrixSheet`, `PantrixMenu`, `PantrixPopover`, `PantrixCommandPalette` | `open` | Programla açıldığında da |
| `PantrixPageScaffold`, `PantrixLazyPageScaffold` | `show` | `tagKey`'i olan bir sayfa her kompozisyona girdiğinde: bir ekranın görüntülenmesi |
:::

Bunları kullanıcının etkileşimi saymak istemeyen bir bağlayıcı `via`'ya bakar ya da onlara planında ayrı bir ad verir.

### Bileşenlerin olayları

Tablo, bileşenlerin testlerinde görülen olaylardan ve kaynaklarından yazıldı. "Ad" sütunu olayın `component`'idir. Tabloda olmayan bileşenler kendileri olay bildirmez, içlerindeki bileşenler kendi olaylarını bildirir: `PantrixTopBar`'ın geri düğmesi (`PantrixBackButton`) bir `PantrixIconButton`'dır, onun olayını bildirir.

:::table
| Bileşen | Ad | `action` | `props` |
| :--- | :--- | :--- | :--- |
| `PantrixAccordion` | `Accordion` | `expand`, `collapse` | `section` |
| `PantrixAlert` | `Alert` | `dismiss` | `tone`, `variant` |
| `PantrixBreadcrumb` | `Breadcrumb` | `select` | `page` (anahtar), `folded` |
| `PantrixButton` | `Button` | `click` | `variant` |
| `PantrixCard` | `Card` | `click` | `variant` |
| `PantrixCarousel` | `Carousel` | `change`, `pause`, `play` | `page` (sayı), `via` |
| `PantrixChart`, `PantrixLineChart`, `PantrixBarChart`, `PantrixAreaChart`, `PantrixRangeChart`, `PantrixWaterfallChart`, `PantrixCandlestickChart` | `Chart` | `select`, `toggle`, `zoom`, `export` | `series`, `shown`, `to`, `format` |
| `PantrixCheckbox`, `PantrixTriStateCheckbox` | `Checkbox` | `toggle` | `state` |
| `PantrixChip` | `Chip` | `click`, `toggle`, `remove` | `selected` |
| `PantrixCodeBlock` | `CodeBlock` | `copy` | `copied` |
| `PantrixCommandPalette` | `CommandPalette` | `open`, `run`, `dismiss` | `command`, `via` |
| `PantrixDataTable` | `DataTable` | `select`, `selectAll`, `sort`, `expand`, `collapse`, `open` | `row`, `column`, `selected`, `rows`, `direction` |
| `PantrixDatePicker` | `DatePicker` | `select`, `years` | `part` |
| `PantrixDateTextField`, `PantrixDateFilledTextField`, `PantrixTimeTextField`, `PantrixTimeFilledTextField` | Bileşenin adı (`DateTextField`…) | `focus`, `open`, `select` | — |
| `PantrixDialog` | `Dialog` | `open`, `dismiss` | `via` |
| `PantrixElasticDatePicker` | `ElasticDatePicker` | `open`, `select`, `refresh`, `autoRefresh` | — |
| `PantrixFileDrop` | `FileDrop` | `browse`, `drop`, `files` | `count`, `rejected`, `via` |
| `PantrixFunnelChart` | `Funnel` | `select`, `export` | `stage`, `format` |
| `PantrixGanttChart` | `Gantt` | `select`, `zoom`, `export` | `task`, `to`, `format` |
| `PantrixGauge`, `PantrixMeterGroup`, `PantrixProgressCircle`, `PantrixSparkline` | Bileşenin adı (`Gauge`…) | `export` | `format` |
| `PantrixHeatmap` | `Heatmap` | `select`, `export` | `cell`, `format` |
| `PantrixIconButton` | `IconButton` | `click` | `variant` |
| `PantrixJsonView` | `JsonView` | `expand`, `collapse`, `copy` | `level`, `copied` |
| `PantrixLazyTreeView` | `LazyTreeView` | `select`, `expand`, `collapse`, `check`, `uncheck`, `action` | `node` |
| `PantrixLink` | `Link` | `click` | `external` |
| `PantrixMenu` | `Menu`; öğesi `MenuItem` | `open`, `dismiss`; öğe: `click` | öğe: `destructive` |
| `PantrixMultiSelect` | `MultiSelect`; arama alanı `MultiSelectSearch` | `open`, `toggle`; arama: `focus`, `clear` | `option`, `selected` |
| `PantrixNavigationBar`, `PantrixNavigationRail` | Bileşenin adı | `select` | `destination`, `reselected` |
| `PantrixPageJump` | `PageJump` | `focus`, `select` | `page` (sayı) |
| `PantrixPageScaffold`, `PantrixLazyPageScaffold` | `PageScaffold` | `show` | — |
| `PantrixPageSize` | `PageSize` | `open`, `select` | `size` |
| `PantrixPagination` | `Pagination` | `select` | `page` (sayı), `via` |
| `PantrixPieChart` | `PieChart` | `select`, `toggle`, `export` | `slice`, `shown`, `format` |
| `PantrixPolarChart`, `PantrixRadarChart` | Bileşenin adı | `select`, `toggle`, `export` | `series`, `shown`, `format` |
| `PantrixPopover` | `Popover` | `open`, `dismiss` | — |
| `PantrixRadioButton` | `RadioButton` | `select` | — |
| `PantrixRadioGroup` | `RadioGroup` | `select` | `index` |
| `PantrixSankeyChart` | `Sankey` | `select`, `export` | `node`, `format` |
| `PantrixScatterChart` | `ScatterChart` | `select`, `toggle`, `zoom`, `export` | `series`, `shown`, `to`, `format` |
| `PantrixSecureTextField`, `PantrixFilledSecureTextField` | Bileşenin adı | `focus`, `clear`, `toggle` | `revealed` |
| `PantrixSegmentButton` | `SegmentButton` | `select` | `segment` |
| `PantrixSelect` | `Select`; arama alanı `SelectSearch` | `open`, `select`; arama: `focus`, `clear` | `option` |
| `PantrixSheet` | `Sheet` | `open`, `dismiss` | `via` |
| `PantrixSidebar` | `Sidebar` | `select`, `expand`, `collapse` | `destination`, `section`, `collapsed` |
| `PantrixSnackbarHost` | `Snackbar` | `open`, `action`, `dismiss` | `tone`, `duration`, `action`, `via` |
| `PantrixStatusTracker` | `StatusTracker` | `select`, `export` | `block`, `format` |
| `PantrixSwitchButton` | `SwitchButton` | `toggle` | `checked` |
| `PantrixTabs` | `Tabs` | `select` | `index`, `via` |
| `PantrixTextField`, `PantrixFilledTextField`, `PantrixAutoResizeTextField`, `PantrixFilledAutoResizeTextField` | Bileşenin adı | `focus`, `clear` | — |
| `PantrixTimePicker` | `TimePicker` | `select` | `part` |
| `PantrixTreemap` | `Treemap` | `select`, `export` | `tile`, `format` |
| `PantrixTreeView` | `TreeView` | `select`, `expand`, `collapse`, `check`, `uncheck`, `action` | `node` |
:::

## 3. Bağlayıcı {#the-connector}

Bağlayıcı, temaya verilen `PantrixAnalytics`'tir: Pantrix'in olaylarını alır ve ürüne giden olayları kendisi kurar. Örneğin bağlayıcısı `ProductAnalytics`'tir ve bütün kararlar onun kurucusunda toplanır:

```kotlin
@Composable
fun App() {
    val scope = rememberCoroutineScope()
    val analytics = remember(scope) {
        ProductAnalytics(
            plan = CHECKOUT_PLAN,            // hangi olay hangi adla, hangi değerlerle
            transport = productTransport,    // ürüne veren tek çağrı (4. bölüm)
            clock = Clock.System,            // olayın olduğu an
            scope = scope,                   // zamanlayıcı ve gönderim ekranla birlikte durur
            policy = SendPolicy()            // 20'lik paketler, 30 saniye, 500 olaylık kuyruk
        )
    }
    // Arka plana geçerken bekleyenler gider: başka bir fırsat olmayabilir.
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { scope.launch { analytics.flush() } }
    PantrixTheme(analytics = analytics, testTagPolicy = TestTagPolicy.Off) {
        CheckoutScreen()
    }
}
```

### İzleme planı

```kotlin
val CHECKOUT_PLAN = TrackingPlan(
    listOf(
        PlannedEvent(pantrixTestTagOf("IconButton", "more", "Checkout"), "click", "quantity_increased"),
        PlannedEvent(
            tag = pantrixTestTagOf("SegmentButton", "shipping", "Checkout"),
            action = "select",
            name = "shipping_chosen",
            notPersonal = setOf("segment")
        ),
        PlannedEvent(
            tag = pantrixTestTagOf("Button", "pay", "Checkout"),
            action = "click",
            name = "checkout_pay_tapped",
            keep = setOf("variant")
        )
    )
)
```

*   Plan bir olayı bileşeninin etiketinden ve eyleminden tanır. Etiketi `pantrixTestTagOf` kurar ve bu, ekrandaki bileşenin taşıdığı etiketin aynısıdır.
*   `keep`, Pantrix'in değerlerinden hangilerinin olayla gideceğini söyler. `notPersonal`, uygulamanın anahtarlarından hangilerinin kişisel olmadığını söyler. Planlı bir olay yalnızca bu ikisinin saydıklarını taşır.
*   Plan kurulurken üç şeyi denetler: aynı etiket ve eyleme iki olay verilmemiştir; çağrının yerinden kurulmuş bir etiket (`#` taşıyan) planda yoktur; `keep` yalnızca Pantrix'in değerlerini sayar. Uygulamanın bir anahtarını `keep`'e yazmak kurucuda bir hatadır: anahtar ancak `notPersonal` ile, yani kişisel olmadığı söylenerek geçer.

### Genel olay

Planın adlandırmadığı her olay tek bir `ui_interaction` olarak gider ve şunları taşır: `ui_component`, `ui_action`, `ui_screen`, `ui_tag` (yalnızca anahtardan kurulmuşsa) ve Pantrix'in sözcükleri ve sayıları. Uygulamanın anahtarları ve çağrı yerinden kurulan etiketler girmez.

Tek bir genel olayın iki nedeni var. Ürünler ayrı olay adlarını sınırlar (Firebase'de 500), bileşen başına bir ad bu sınırı planın adlarıyla paylaşır. Bir de ürünün raporlarında yalnızca planın adları ürünün diliyle görünür, geri kalanı tek bir yerde süzülür.

### İzin

`Consent` üç değerlidir: `Unknown`, `Granted`, `Denied`. `Granted` olana kadar hiçbir olay kuyruğa girmez ve günlükte "toplanmadı" diye geçer. İzin geri alınınca bekleyenler atılır ve zamanlayıcılar durur. O anda yolda olan bir paket başaramazsa yeniden denenmez, atılır. Ürünün kendi SDK'sının da izni vardır ve ikisi birlikte değişmelidir (4. bölüm).

### Gönderim ve zaman

*   `track` olayı zamanıyla damgalar, kuyruğa koyar ve döner. UI iş parçacığında ağ yoktur.
*   Bir paket üç durumda gider: `SendPolicy.batch` olay biriktiğinde, ilk olay `SendPolicy.every` kadar beklediğinde, uygulama `flush()` dediğinde. Hiçbir şey beklemiyorken hiçbir şey uyanmaz.
*   Gönderim `sendOn`'da yapılır (`Dispatchers.Default`) ve aynı anda yalnızca bir paket yoldadır. `scope`'un dispatcher'ı UI'ınkidir: olaylar `track` ile gönderim arasında o tek iş parçacığında el değiştirir.

### Ağ yokken

*   Başarısız bir paket kuyruğun başına, sırasıyla geri konur ve olaylar ürüne oldukları sırayla varır.
*   Yeniden deneme `firstRetry` (1 saniye) sonra yapılır ve her başarısızlıkta iki katına çıkar, en çok `longestRetry` (5 dakika) olur. Başaran bir gönderim beklemeleri baştan başlatır.
*   Kuyruk `capacity` (500) olay tutar. Dolunca en eskisi atılır ve günlükte sayılır.
*   Örneğin kuyruğu bellektedir: süreç ölürse bekleyenler gider. Olayları diske yazan bir SDK varsa kuyruğu ona bırakmak daha sağlamdır (4. bölüm).

## 4. Ürüne bağlamak {#connecting-to-the-product}

Bağlayıcının ürünle tek temas noktası `Transport`'tur:

```kotlin
fun interface Transport {
    /** [batch]'i ürüne verir: vardıysa true. */
    suspend fun send(batch: List<ProductEvent>): Boolean
}
```

Bir `ProductEvent`'in adı (`name`), özellikleri (`properties`, hepsi `String`) ve zamanı (`at`) vardır. Gönderici false dönerse ya da bir hata fırlatırsa paket bekler ve yeniden denenir.

### Kendi kuyruğu olan sdk'lar

Aşağıdaki üç SDK da olayları kendisi biriktirir ve paket paket gönderir. Bağlayıcının kuyruğu o zaman ikinci bir kuyruktur. Önerilen kurulum: planı ve izni bağlayıcıda tutun, her olayı SDK'ya hemen verin (`SendPolicy(batch = 1)`), biriktirmeyi, yeniden denemeyi ve arka planda göndermeyi SDK'ya bırakın. Gönderici, SDK olayı aldığında true döner.

### Firebase (Android)

```kotlin
val transport = Transport { batch ->
    batch.forEach { event ->
        Firebase.analytics.logEvent(event.name) {
            event.properties.forEach { (key, value) -> param(key, value) }
        }
    }
    true
}
```

*   Kurulum: `implementation(platform("com.google.firebase:firebase-bom:…"))` ve `implementation("com.google.firebase:firebase-analytics")`.
*   Sınırlar: bir olay en çok 25 parametre taşır. Parametre adları en çok 40 karakterdir, bir harfle başlar ve yalnızca harf, rakam ve alt çizgi içerir. Metin değerleri en çok 100 karakterdir (Google Analytics 360'ta 500). En çok 500 ayrı olay adı kullanılabilir. `firebase_`, `google_` ve `ga_` önekleri ayrılmıştır. Genel olayın `ui_` adları bu kurallara uyar. İç içe birçok `TestScope`'un altındaki bir bileşenin `ui_tag`'i 100 karaktere yaklaşabilir: ürünün saydığı böyle bir bileşen plana kendi adıyla yazılır.
*   İzin: `setAnalyticsCollectionEnabled(false)` toplamayı kapatır. `setConsent` ile `ANALYTICS_STORAGE` reddedilir. Bağlayıcının `consent`'iyle birlikte çağrılır.

### Amplitude (Android, Kotlin SDK)

```kotlin
val amplitude = Amplitude(AMPLITUDE_API_KEY, applicationContext) {
    flushQueueSize = 20          // varsayılanı 30
    flushIntervalMillis = 30000  // varsayılanı 30000
}
val transport = Transport { batch ->
    batch.forEach { event ->
        amplitude.track(event.name, mutableMapOf<String, Any?>().apply { putAll(event.properties) })
    }
    true
}
```

*   İzin: `amplitude.optOut = true` iken SDK olay saklamaz ve göndermez.
*   `amplitude.flush()` bekleyenleri hemen gönderir: bağlayıcının `flush()`'ı onu da çağırabilir.

### PostHog (Android)

```kotlin
PostHogAndroid.setup(context, PostHogAndroidConfig(apiKey = POSTHOG_API_KEY, host = POSTHOG_HOST))
val transport = Transport { batch ->
    batch.forEach { event -> PostHog.capture(event = event.name, properties = event.properties) }
    true
}
```

*   Biriktirme: `flushAt` (varsayılanı 20 olay) ve `flushIntervalSeconds` (varsayılanı 30). `PostHog.flush()` bekleyenleri hemen gönderir, ama belgesine göre gönderimi garanti etmez.
*   İzin: `PostHog.optOut()`, ya da kurulumdan önce `config.optOut = true`.

### Öteki platformlar

Ürünlerin iOS ve web SDK'ları Swift'te ve JavaScript'te yazılıdır. Ortak kod onları iki yoldan çağırır:

*   **`expect`/`actual`:** ortak kodda `expect fun productTransport(): Transport`, her platformda o platformun SDK'sını çağıran bir `actual`. Web'de SDK'nın JavaScript arayüzü Kotlin'den `external` bildirimlerle çağrılır.
*   **Kabuktan bir fonksiyon:** platformun kabuğu ortak koda düz bir fonksiyon verir, gönderici onu çağırır. iOS'ta `MainViewController(onEvent: { name, properties in Analytics.logEvent(name, parameters: properties) })` gibi. Kotlin'in `(String, Map<String, String>) -> Unit` türü Swift'te bir kapanıştır. `suspend` bir arayüzü Swift'te uygulamaya gerek kalmaz.

## 5. Test etmek {#testing}

*   **Bağlayıcının kararları:** `runTest` içinde, `scope = backgroundScope` ve `sendOn = StandardTestDispatcher(testScheduler)` ile. Zamanlayıcı ve yeniden deneme testin sanal zamanında koşar: `advanceTimeBy(policy.every - 1.milliseconds)` ile "zamanından önce gitmedi", 2 milisaniye daha ile "gitti" denir. Saat `TestClock`'tur (`com.pantrixui.testing`). Örneğin `ProductAnalyticsTest`'i (15 test) her kararı ayrı bir testte tutar.
*   **Plan:** bir Pantrix olayından ürünün olayına giden yol saf bir fonksiyondur (`TrackingPlan.productEventOf`) ve ekransız sınanır (`TrackingPlanTest`).
*   **Bir ekranın olayları:** `PantrixTestTheme(analytics = RecordingAnalytics())` bileşenlerin bildirdiklerini sırayla tutar: `eventsOf("Button")` ([test rehberi](/docs/guides/testing)).
*   **Ekran ve bağlayıcı birlikte:** UI testinde bağlayıcıya `sendOn = Dispatchers.Unconfined` verilir. Tarayıcıda tek bir iş parçacığı vardır ve `waitUntil` beklerken `Dispatchers.Default`'a sıra gelmez: gönderim hiç olmaz, test zaman aşımına düşer. Örneğin `SampleScreenTest`'i promosyon koduna yazılanın hiçbir pakette olmadığını da doğrular.
*   **Sürüm derlemesi gibi:** `TestTagPolicy.Off` ile kurulan bir ekranda semantik ağaçta Pantrix etiketi olmadığı ve planın olayının yine geldiği sınanır.
*   Örneğin testleri: `./gradlew :samples:pantrix-analytics:shared:jvmTest`, `iosSimulatorArm64Test`, `jsBrowserTest`, `wasmJsBrowserTest`, bir cihaz ya da emülatör bağlıyken `connectedAndroidDeviceTest`.

## 6. Kütüphanesiz {#without}

Bağlayıcının Pantrix'e bağlı tek parçası, olayı alan `track(event: PantrixUiEvent)` ve planın o olaydan okuduğu dört alandır (`component`, `action`, `testTag`, `screen`) ile `props`. Başka bir kaynaktan olay alan bir uygulama `productEventOf`'u kendi olay türüyle yazar. Kuyruk, izin, gönderim ve `Transport` aynen kalır: örneğin `ProductAnalytics.kt`, `SendPolicy.kt`, `Transport.kt` ve `Consent.kt` dosyaları Compose'a da bağlı değildir.

## 7. Doğrulananlar ve doğrulanmayanlar {#verification}

### Doğrulananlar

*   **Olay envanteri:** bileşenlerin JVM testleri (4896 test, `c808f18b`) bir deneme kulvarında, test temasından geçen her olay bir dosyaya yazılarak koşuldu: 2537 olay, 65 addan 134 (ad, eylem) çifti. 2. bölümün tablosu bu olaylardan ve kaynaklardan yazıldı. Tarih ve saat alanlarının `select`'i `date/common/PickerField.kt`'den, arama alanlarının `clear`'ı `SelectList.kt`'nin ortak arama alanından okundu. `autoRefresh` sonradan `auto_refresh`'in yerine geçti.
*   **Metin girmez:** `TypedTextIsNeverReportedTest` (15 test) metni klavye eyleminde ve alandan çıkarken sızdıran iki sahte alanla sınandı ve ikisini de yakaladı.
*   **`TestTagPolicy.Off`'ta etiket:** `TestTagTest` ve `OwnTestTagTest`. Olayın etiketini sınayan test eski kodda boş etiketlerle düştü.
*   **Bağlayıcı:** örneğin 22 testi beş hedefte geçti: JVM, Android (emülatör, API 33), iOS simülatörü, JS, Wasm. Bağlayıcı 12 ayrı yerden bilerek bozuldu ve her bozulmayı en az bir test yakaladı: izin, katlanan bekleme, kuyruk sınırı, zamanlayıcı, `flush`, sıra, aynı anda tek paket, beklemelerin sıfırlanması, genel olayın değerleri ve çağrı yeri etiketi.
*   **Uygulama:** iPhone 18 Pro Max simülatöründe, Pixel 7 Pro emülatöründe (API 33), tarayıcıda (Wasm) ve masaüstünde açıldı. İzin, adet, kargo, promosyon kodu, ödeme ve "Send now" akışı denendi. Gönderilen JSON'da yazılan promosyon kodu yoktu.

### Okunan, çalıştırılmayan

*   **Firebase:** `logEvent` yapıcısı ve kurulum Android belgelerinden ("Log events", "Get started"); sınırlar Firebase'in Swift başvurusundan (`Analytics` sınıfı); izin çağrıları Kotlin başvurusundan (`FirebaseAnalytics.ConsentType`). Bir olay adının uzunluk sınırı Firebase'in kendi sayfalarında bulunamadı; adlar planda kısa ve `snake_case` tutulur.
*   **Amplitude:** Android Kotlin SDK sayfası (kurulum, `track`, `optOut`, `flushQueueSize`, `flushIntervalMillis`, `flush`). `track`'in özellik parametresinin türü belgedeki örnekten (`MutableMap<String, Any?>`) çıkarıldı.
*   **PostHog:** Android sayfası (kurulum, `capture`, `optOut`, `flush`, `flushAt`, `flushIntervalSeconds`).
*   Üç ürün de bu depoda bağımlılık değildir: çağrılar derlenmedi ve çalıştırılmadı. iOS'ta kabuktan fonksiyon verme ve web'de `external` bildirimler de denenmedi.
*   Belgeler 2026-10-24'te okundu.
