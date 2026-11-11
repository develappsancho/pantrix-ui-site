---
title: iOS için küçük bir semantik ağaç
description: Compose Multiplatform'da çok satırlı bir ekranın iOS'ta, erişilebilirlik ağacı okunurken, kaydırılırken ve bir satır açılıp kapanırken neden takıldığı, ağacın nasıl küçük tutulduğu ve nasıl ölçüldüğü. Sorun Pantrix'e özgü değildir: bir ekranda yüzlerce semantik düğümü birden kuran her Compose uygulamasında iOS'ta aynı şekilde görülür.
---

Burada söylenenlerin hangisinin simülatörde görüldüğü, hangisinin Compose Multiplatform 1.12.2'nin kaynağında okunduğu ve hangisinin doğrulanmadığı 6. bölümde ayrı ayrı yazılıdır.

## 1. Sorun {#problem}

:::aside semantics-tree
### Belirti

*   iOS'ta, ekranın erişilebilirlik ağacını bir şey okurken, uzun bir ekran kaydırılırken ya da bir satır açılıp kapanırken arayüz donar gibi olur: kısa aralıklarla, her biri yüzlerce milisaniye süren takılmalar.
*   Ağacı okuyanlar: VoiceOver, XCTest'in üstünde çalışan test sürücüleri (Maestro'nunki de), Accessibility Inspector ve ekranı erişilebilirlik ağacından okuyan araçlar. Hiçbiri yokken aynı ekran akıcıdır; bu yüzden geliştirici çoğu zaman görmez, VoiceOver kullanan kullanıcı her zaman görür.
*   Android'de aynı ekran, ağacı okuyan bir araç çalışırken de akıcıdır (6. bölüm).
:::

### Neden

Compose iOS'ta semantik ağacı UIKit'in erişilebilirlik öğelerine çevirir ve onları güncel tutar (`AccessibilityMediator`):

*   **Ağaç okununca eşitleme açılır.** İlk okumayla eşitleme etkin olur (`isAccessibilityActive`). Son okumadan iki saniye sonra, odakta bir öğe yoksa, kapanır. VoiceOver'ın imleci bir öğedeyken açık kalır.
*   **Her değişiklikte bütün ağaç.** Eşitleme açıkken her semantik değişiklikte, kaydırmanın ve bir animasyonun her adımında, ağacın tamamı kökten yeniden dolaşılır, yalnızca değişen kısmı değil. Öğe olabilecek her düğüm için bir UIKit öğesi kurulur ya da güncellenir, çerçevesi pencerede hesaplanır ve kardeşleriyle sıralanır. Bu ana iş parçacığında olur; iki geçiş arasında 100 ms beklenir.
*   **Maliyet kurulmuş düğüm sayısıyla büyür.** Ekranda görünen değil, kompozisyonda olan her düğüm sayılır: bir `Column` içindeki yüzlerce satır, ekranın dışında da olsa ağaçtadır.

Pantrix'in demo'sunda Data Table sayfası, on altı tablo bir sütunda, 1585 düğüm tutuyordu; öteki sayfalar 107 ile 205 arası. iPhone simülatöründe, Debug yapısında, ağaç okunurken o sayfa yarım saniyede bir yaklaşık 400 ms takılıyordu.

## 2. Çözüm {#fix}

Ağaç küçük tutulur. Üç yol vardır, etkisi büyükten küçüğe:

:::table
| Yol | Ne yapılır | Ne kazandırır |
| :--- | :--- | :--- |
| Yalnızca görüneni kurmak | Uzun içerik bir `Column` değil, bir `LazyColumn` ya da `LazyVerticalGrid` olur | Ağaçta yalnızca ekrandaki satırlar durur; ekranın boyu ne olursa olsun düğüm sayısı ekranın aldığı kadardır |
| Bir olanı birleştirmek | Tek bir şey söyleyen satır, bir ad ve değeri, bir hücre, `Modifier.semantics(mergeDescendants = true) {}` alır | Birleşen düğümün içindeki yazılar için iOS ayrı öğe kurmaz; ekran okuyucu da onu tek parça okur |
| Okunmayacak olanı çıkarmak | Yalnızca süs olan bir şekil, bir yazının yanındaki aynı anlamdaki ikon, `Modifier.clearAndSetSemantics {}` alır | O düğüm ve altı ağaçtan çıkar |
:::

Dört ayrıntı:

:::table
| # | Ayrıntı | Ne yapılır |
| :--- | :--- | :--- |
| 1 | Birleştirmek dolaşmayı durdurmaz: iOS birleşen düğümün altındaki düğümlerden de geçer, yalnızca onlar için öğe kurmaz | En büyük kazanç tembel listedendir; birleştirmek onun üstüne gelir |
| 2 | iOS birleşik bir düğümün etiketini en çok beş yazıdan kurar; altıncısı etikette yoktur | Kısa olan birleştirilir: bir satır, bir hücre. Çok yazılı bir kartın tamamı değil |
| 3 | Birleşen düğümün içindeki bir test etiketi birleşik ağaçta yoktur | Test onu `useUnmergedTree = true` ile bulur |
| 4 | Tembel listede ekrandan çıkan satır kompozisyondan da çıkar | Satırın `rememberSaveable` ile tuttuğu durum geri döner; düz `remember` ile tuttuğu baştan başlar. Odağı alan bir satır bar'ın arkasında kalmasın diye satırlar listenin payını söyler ([scroll-padding](/docs/guides/scroll-padding) rehberi) |
:::

## 3. Pantrix ile {#with}

### 3.1. Listenin sayfası

Uzun bir sayfa `PantrixLazyPageScaffold`'dur: başlığı ve üst kısmı listenin ilk satırlarıdır, satırları göründükçe kurulur. İçerik listenin payını `contentPadding` olarak alır (`LazyPageScope`); her satır onu söyler ki klavyeyle odağı alan bir satır alttaki bar'ın arkasında kalmasın:

```kotlin
PantrixLazyPageScaffold(title = "Orders") {
    val shown = Modifier.scrollPadding(contentPadding)
    items(orders, key = { it.id }) { order -> OrderRow(order, shown) }
}
```

Bir sayfa kendi listesini kendisi kuruyorsa `PantrixPageScaffold(scroll = PageScroll.None)` aynı payı `contentPadding` olarak verir; satırları aynı şekilde söyler.

### 3.2. Data table

`PantrixDataTable` bir hücreyi tek bir düğüm olarak okutur; dar ekranda tablonun dönüştüğü listede bir kaydın her satırını da, kolonun adıyla değeri birlikte. Bir hücrenin tuttuğu basılacak şey, bir düğme, ayrıca okunur. Satırı basılan (`onRowClick`) bir tablo zaten satırı tek bir düğme olarak okutur. Pantrix'in demo'sundaki on altı tablonun birleşik ağacı:

:::table
| Pencere | Önce | Sonra |
| :--- | :--- | :--- |
| Telefon (430 dp) | 1427 düğüm | 905 düğüm |
| Geniş (1200 dp) | 1676 düğüm | 1136 düğüm |
:::

iOS'ta, ağaç okunurken, demo'nun tembel sayfasında bir eşitleme geçişi bununla 159 ms'den 118 ms'ye indi (6. bölüm). Bir hücrenin içine konan bir şeyin test etiketi `useUnmergedTree = true` ile bulunur.

## 4. Pantrix olmadan {#without}

Aynı iki adım her Compose projesinde:

```kotlin
@Composable
fun OrderLine(name: String, value: String) {
    // Bir ad ve değeri: ekran okuyucu için tek bir şey, iOS için tek bir öğe.
    Row(Modifier.semantics(mergeDescendants = true) {}) {
        Text(name)
        Text(value)
    }
}

@Composable
fun OrdersScreen(orders: List<Order>) {
    // Yalnızca ekrandaki siparişler kurulur.
    LazyColumn {
        items(orders, key = { it.id }) { order -> OrderCard(order) }
    }
}
```

## 5. Ölçmek {#measuring}

### 5.1. Testte düğümleri saymak

Bir ekranın düğüm sayısı testte sayılır ve bir sınırla tutulur; ekran büyüdükçe sayı kendiliğinden artmaz:

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

Birleşik ağacın sayısı iOS'un kurduğu öğelere yakındır; birleştirilmemiş ağacınki (`useUnmergedTree = true`) iOS'un dolaştığı düğümlerdir.

### 5.2. iOS'ta time profiler ile

Ekran açılır ve, ağacı bir şey okurken (cihazda VoiceOver, simülatörde bir test sürücüsü ya da Accessibility Inspector), kaydırılırken kayıt alınır:

```bash
xcrun xctrace record --template 'Time Profiler' --device <udid> --attach <pid> --time-limit 20s --output screen.trace
xcrun xctrace export --input screen.trace --xpath '/trace-toc/run[@number="1"]/data/table[@schema="potential-hangs"]' > hangs.xml
```

`potential-hangs` ana iş parçacığının uzun süre tutulduğu yerleri, başlangıçları ve süreleriyle listeler; `time-profile` tablosu da hangi fonksiyonun ne kadar tuttuğunu söyler (Compose'ta `AccessibilityMediator.sync`). Kotlin/Native'in Debug yapısı Release'ten yavaştır: bir süre, hangi yapıda ölçüldüğüyle birlikte söylenir.

## 6. Doğrulama {#verification}

### Görülenler

iPhone 18 Pro Max simülatöründe (iOS 27.0), demo'nun Debug yapısında, Data Table sayfasında, ağacı Maestro'nun sürücüsü okurken Time Profiler ile (`AccessibilityMediator.sync`'in bir geçişi, ana iş parçacığında):

:::table
| Sayfa | Bir geçiş, ortanca | En uzun | Takılma |
| :--- | :--- | :--- | :--- |
| Önce: on altı örnek bir sütunda | 403 ms | 417 ms | 60 sn'de 21 |
| Tembel sayfa | 159 ms | 216 ms | 100 sn'de 1 |
| Tembel sayfa ve tek düğümlü hücre | 118 ms | 172 ms | 100 sn'de 0 |
:::

*   Son iki satır aynı sürüşle ölçüldü: sayfa 24 kez aşağı, 24 kez yukarı kaydırıldı, her kaydırmadan sonra ağaç okundu. İlk satır, değişiklikten önce, sayfa elle kaydırılıp satırlar açılıp kapanırken ölçüldü; bir geçişin süresi sürüşe değil, kurulmuş ağaca bağlıdır.
*   Ağaç okunurken ana iş parçacığının örneklerinin yine dörtte üçü `sync`'tedir: geçişler kısaldıkça daha sık koşar. Kalan maliyet Compose'tadır (`docs/OPEN_ITEMS.md`).
*   Ağacı okuyan yokken `sync` hiç koşmadı: ekran görüntüsüyle sürülen bir kayıtta 45 saniyede `sync`'in tek örneği yok.
*   Android emülatöründe aynı sayfa, ağacı `uiautomator dump` döngüyle okurken kaydırıldı: kare süresinin ortancası 16 ms, yüzde 99'u 25–26 ms, yavaş karelerin payı yaklaşık yüzde 4; okuyan yokkenkiyle aynı.
*   Data Table'ın düğüm sayıları (3.2'deki tablo) JVM'de, demo'nun on altı örneği iOS ölçüleriyle tek tek çizilerek sayıldı.

### Testler

*   `DataTableTest.a_cell_and_a_record_s_line_are_each_read_as_one_thing`: tablonun hücresi yazısını tutar, listede kaydın satırı kolonun adını ve değeri tutar, satırı basılan kayıt hepsini.
*   `PageScaffoldTest.what_takes_the_focus_in_a_list_s_page_is_shown_above_the_bar_at_its_foot`: listenin sayfası satırlara payını verir, odağı alan satır alttaki bar'ın üstünde durur.
*   `DemoPagesTest`: demo'nun her sayfası tembel listede, her örneği sırayla ekrana getirilerek, iki dilde ve üç platformun ölçüleriyle çizilir.

### Kaynakta okunanlar

Compose Multiplatform 1.12.2, `ui-iosarm64`'ün kaynağı, `androidx/compose/ui/platform/Accessibility.ios.kt`:

*   `AccessibilityMediator`: eşitleme döngüsü, `isAccessibilityActive`, son okumadan sonraki iki saniye (`scheduleAccessibilityDisablingAndCleanup`), iki geçiş arasındaki 100 ms.
*   `sync()`: `unmergedRootSemanticsNode`'dan başlayan dolaşma; `canBeAccessibilityElement`: birleşen bir düğümün altındaki yazı öğe olmaz, bunu bulmak için düğüm üstlerine doğru yürür; `BeyondBoundsComparator` ile sıralama.
*   `MAX_TEXT_COLLECT_NODES = 5`: birleşik bir düğümün etiketine giren yazı sayısı.

### Doğrulanmayanlar

*   Release yapısı ölçülmedi; süreler Debug'ındır.
*   Gerçek bir iPhone'da ve VoiceOver'la denenmedi: simülatörde VoiceOver yoktur, ağacı bir test sürücüsü okudu.
*   Önceki sayfa ölçümü ile sonrakiler aynı sürüşle alınmadı (Görülenler'in ilk maddesi).
*   Android'de değişiklikten sonraki sayfa yeniden ölçülmedi: önce de takılma yoktu.
*   Hücrenin tek parça okunuşu ekran okuyucuyla dinlenmedi. JVM'deki birleşik ağaçta hücre yazılarını tutar; Android'in `uiautomator` ağacında yazılar hücrenin çocukları olarak yine görünür ve onları TalkBack'in hücreyle birlikte okuması beklenir, denenmedi.

## 7. Bu depoda nerede {#repository toc="7. Depoda"}

:::table
| Ne | Nerede |
| :--- | :--- |
| Demo'nun tembel sayfası | `app/shared/src/commonMain/kotlin/com/pantrixui/cmp/demo/page/ComponentPage.kt` (`ComponentPage`) |
| Data Table'ın hücresi ve kaydın satırı | `ui/components/src/commonMain/kotlin/com/pantrixui/components/data/datatable/DataTableBody.kt` (`BodyCell`), `DataTableList.kt` (`RecordLine`) |
| Listenin sayfası ve payı | `ui/components/src/commonMain/kotlin/com/pantrixui/components/container/page/PageScaffold.kt` (`ListBody`), `LazyPageScope` (`PageAction.kt`) |
| Testler | `DataTableTest`, `PageScaffoldTest` (`ui/components/src/commonTest`), `DemoPagesTest` (`app/shared/src/jvmTest`) |
| Compose'taki maliyetin açık maddesi | `docs/OPEN_ITEMS.md`, 3. bölüm |
:::
