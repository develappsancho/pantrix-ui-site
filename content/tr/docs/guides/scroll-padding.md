---
title: Bir çubuğun altında kalan odak
description: Compose Multiplatform'da bir bar'ın altından kayan içerikte klavye odağının neden bar'ın arkasında kaybolduğu, bunun için kütüphanenin ne verdiği ve nasıl sınandığı. Sorun Pantrix'e özgü değildir: içeriğini bir alt bar'ın, bir üst bar'ın ya da sistem çubuğunun altından kaydıran her Compose uygulamasında aynı şekilde görülür.
---

Burada söylenenlerin hangisinin cihazda görüldüğü, hangisinin Compose Multiplatform 1.12.1'in kaynağında okunduğu ve hangisinin doğrulanmadığı 7. bölümde ayrı ayrı yazılıdır.

## 1. Sorun {#problem}

:::aside scroll-padding
### Belirti

*   Bir ekranın altında bir bar vardır ve içerik onun altından kayar: liste `contentPadding` olarak bar'ın payını alır, son satırı bar'ın üstünde durur, kayarken satırlar bar'ın arkasından geçer.
*   Ekran bir klavyeyle gezilir: Tab, ok tuşları, bir TV kumandası, bir anahtar erişimi cihazı. Odak aşağı indikçe liste kayar ve odağı alan öğe **bar'ın arkasına** gelir: halkası görünmez, neyin odakta olduğu bilinmez. Yukarı çıkarken aynısı üst bar'ın arkasında olur.
*   Dokunmayla sorun yoktur. Bir test de göstermez: `requestFocus()` odağı verir, `assertIsFocused()` geçer; öğenin nerede durduğuna bakan olmaz.
*   WCAG 2.2'nin 2.4.11 ölçütü tam bunu ister (Focus Not Obscured, AA): odağı alan bileşen, yazarın koyduğu bir içerikle bütünüyle örtülmemelidir.
:::

### Neden

Compose odağı alan öğeyi görünür alana getirir: öğe bir istek gönderir, isteği yolundaki her kaydırıcı sırayla karşılar ve öğeyi **kendi kenarına** kadar kaydırır, fazlasını değil. Kaydırıcının "görünür" saydığı, kendi boyunun tamamıdır. İçerik boşluğu (bir lazy listenin `contentPadding`'i ya da kaydırmanın içindeki bir `padding`) o alanın parçasıdır; kaydırıcı onu bilmez. Bar'ın altına uzanan bir listenin alt kenarı bar'ın arkasıdır.

Küçük kardeşi: kütüphanenin odak halkası öğenin dışına çizilir. Kaydırıcı öğeyi kenarına getirince halkanın bir kenarı kaydırma alanının dışında kalır ve kesilir.

## 2. Çözüm {#fix}

Önce soru: içerik bar'ın altından kaymak **zorunda mı**?

:::table
| Durum | Yol |
| :--- | :--- |
| Bar opak ve arkasında görünecek bir şey yok | Boşluk içeriğe değil kaydırıcının kendisine verilir: `LazyColumn(Modifier.padding(padding))`. Kaydırma alanı bar'da biter, kenarı bar'ın üstüdür; sorun kalmaz. Hiçbir API gerekmez. |
| İçerik bar'ın altından geçmeli (yarı saydam bar, bar'ı olmayan kenarda sistem çubuğunun arkası) | İçerik, sonlarında ne kadar boşluk bıraktığını söyler; görünür alana getirme isteği o kadar büyütülür. Aşağıdaki budur. |
:::

İkinci yolun parçası tektir: içeriğin üstünde duran ve **geçen her isteği boşluk kadar büyüten** bir modifier. Öğe "beni göster" der; modifier bunu "beni ve altımdaki 64dp'yi göster" yapar; kaydırıcı öğeyi kenarından 64dp içeride, bar'ın üstünde bırakır. Compose bunun için herkese açık ve kararlı bir arayüz verir: `BringIntoViewModifierNode` (`androidx.compose.ui.relocation`).

Dört ayrıntı:

:::table
| # | Ayrıntı | Ne yapılır |
| :--- | :--- | :--- |
| 1 | Kendisinde olandan fazlası istenen kaydırıcı kaydırmaz ve artanı **kullanıcının sürüklemesi olarak** üstündekine geçirir: içinde durduğu bir sheet oynar, çekip yenilenen bir liste işaretini gösterir | İçeriğin uzandığından fazlası istenmez. Kaydırılan içerik boyundan tanınır (yerinden büyüktür) ve istek onun sınırında kesilir |
| 2 | Lazy liste öğelerini tek tek dizer: içeriğin nereye kadar uzandığı bir öğeden bilinemez | Modifier her öğeye konur ve boşluğun tamamı istenir; listenin `contentPadding`'i en az o kadar olmalıdır |
| 3 | Klavye açıkken kaydırma alanı klavyenin üstünde biter (`imePadding`) ve alt bar klavyenin arkasındadır: alanı örtmez | Boşluktan klavyenin payı düşülür (`padding - WindowInsets.ime.asPaddingValues()`); düşülmezse ilk tuşta sayfa bar'ın boyu kadar yukarı sıçrar |
| 4 | Odak halkası öğenin dışındadır | Bir sütunda ya da satırda halka yerini kendisi ister. Lazy listede isteyemez: liste halkanın yerini `contentPadding`'ine katar ve öğelerine aynı değeri söyler |
:::

### Neden `BringIntoViewSpec` değil

Compose'un bu iş için görünen aracı `LocalBringIntoViewSpec`'tir: kaydırıcıya "alanının şu kadarı görünür sayılmaz" denir. Demo'nun ilk çözümü oydu; üç sebeple bırakıldı:

*   **İçindeki her kaydırıcıya ulaşır.** Bir composition local'dır: sayfanın kaydırıcısına verilen kural, içindeki yana kayan satıra ve metin alanının kendi kaydırmasına da gider. Kural alanın yönünü bilmez: onlar da kendi kenarlarının o kadarını görünür saymaz, alt bar'ın boyu bir satırın sağ ucundan düşülür. İçeridekilere her zamankini geri vermek gerekir; lazy listede bu her öğede ayrı yapılır.
*   **Deneysel.** `LocalBringIntoViewSpec` Android'de ve Skiko hedeflerinde `@ExperimentalFoundationApi` taşır.
*   **İçeriğin nereye kadar uzandığını bilmez.** İçerikte o boşluk yoksa kaydırıcıdan olmayan istenir ve artan üstündekine geçer (1. ayrıntı).

Bir üstünlüğü vardır: kaydırma alanı küçülürken (klavye açılırken) odaktaki öğeyi görünür tutan Compose'un kendi izlemesi de o kuralı kullanır. Modifier yalnızca istekleri büyütür; 5. bölümde yazılı.

## 3. Pantrix ile {#with}

Kütüphane tek bir modifier verir (`com.pantrixui.foundation.a11y`):

```kotlin
public fun Modifier.scrollPadding(padding: PaddingValues): Modifier
```

Boşluk **eklemez**, içeriğin bıraktığı boşluğu **söyler**. Kaydırılanın üstüne, kaydırıcının içine konur.

### 3.1. kayan bir sütun ya da satır

Kaydırmadan sonra, sözünü ettiği `padding`'den önce:

```kotlin
PantrixScaffold(bottomBar = { insets -> AppTabs(Modifier.padding(insets)) }) { padding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .scrollPadding(padding)
            .padding(padding)
    ) {
        // …
    }
}
```

Burada fazlası istenemez: içerik boyundan tanınır ve istek onun sınırında kesilir. İçerikte o boşluk yoksa en sondaki öğe kenara kadar gelir, üstündekine hiçbir şey geçmez. Odak halkası yerini kendisi ister; içeriğin sonunda bar'ın payından sonra bir halkalık yer daha varsa (sayfaların çoğunda kendi kenar boşluğu vardır) son öğenin halkası da bütündür.

Yana kayan bir satırda `padding`'in başı ve sonu yazı yönüne göre okunur. Demo'nun yana kayan çerçevesi iki ucunda bir halkanın yerini bırakır ve bunu böyle söyler:

```kotlin
val ring = PaddingValues(horizontal = PantrixTheme.platform.focusRingReach)
Box(Modifier.horizontalScroll(state).scrollPadding(ring).padding(ring)) { content() }
```

### 3.2. lazy liste

Her öğeye; değer, listenin `contentPadding`'idir:

```kotlin
PantrixScaffold(bottomBar = { insets -> AppTabs(Modifier.padding(insets)) }) { padding ->
    // Bar'ların payı ve, üstünde ve altında, bir halkanın yeri.
    val room = padding + PaddingValues(vertical = PantrixTheme.platform.focusRingReach)
    LazyColumn(contentPadding = room) {
        items(orders, key = { it.id }) { order ->
            OrderRow(order, Modifier.scrollPadding(room))
        }
    }
}
```

Öğesine konmayan bir satır eskisi gibi davranır: kenara gelir. Listede `contentPadding` söylenenden azsa listeden olmayan istenir (2. bölüm, 1. ayrıntı): **iki değer aynı değişkenden gelmelidir.** Izgaralarda (`LazyVerticalGrid`) kullanım aynıdır.

### 3.3. klavye açıkken

Metin alanı olan ekran kaydırma alanını klavyenin üstünde bitirir. O sırada alt bar klavyenin arkasındadır; boşluktan klavyenin payı düşülür. Compose'un `PaddingValues` çıkarması bunu yapar: sıfırın altına inmez ve değeri sorulduğu anda okur, klavye oynarken ekran yeniden kurulmaz.

```kotlin
Column(
    Modifier
        .imePadding()
        .verticalScroll(scroll)
        .scrollPadding(padding - WindowInsets.ime.asPaddingValues())
        .padding(padding)
)
```

Demo'nun sayfası böyledir (`App.kt`, `DemoPage`).

### 3.4. içindeki kaydırıcılar

Onlara dokunulmaz. Sütunun içindeki yana kayan bir satır öğesini kendi kenarına getirir, her zamanki gibi; ondan çıkan istek sütunun modifier'ından geçer ve sütun satırı bar'ın üstünde gösterir. Bir metin alanının imleci kendi kutusunda olduğu gibi kalır.

## 4. Pantrix olmadan {#without}

Bir modifier ve onun node'u. Kararlı API'lerle yazılır; `expect`/`actual` gerekmez.

```kotlin
fun Modifier.scrollPadding(padding: PaddingValues): Modifier = this then ScrollPaddingElement(padding)

private data class ScrollPaddingElement(val padding: PaddingValues) : ModifierNodeElement<ScrollPaddingNode>() {
    override fun create() = ScrollPaddingNode(padding)

    override fun update(node: ScrollPaddingNode) {
        node.padding = padding
    }
}

private class ScrollPaddingNode(var padding: PaddingValues) :
    Modifier.Node(),
    BringIntoViewModifierNode {
    override suspend fun bringIntoView(
        childCoordinates: LayoutCoordinates,
        boundsProvider: () -> Rect?
    ) {
        // İstek üstteki kaydırıcıya bu node'un kendi koordinatlarında ve büyütülmüş olarak gider.
        bringIntoView {
            val asked = if (isAttached && childCoordinates.isAttached) boundsProvider() else null
            asked?.let { rect ->
                val own = requireLayoutCoordinates()
                val here = rect.translate(own.localBoundingBoxOf(childCoordinates, clipBounds = false).topLeft)
                val direction = requireLayoutDirection()
                val grown = with(requireDensity()) {
                    Rect(
                        left = here.left - padding.calculateLeftPadding(direction).roundToPx(),
                        top = here.top - padding.calculateTopPadding().roundToPx(),
                        right = here.right + padding.calculateRightPadding(direction).roundToPx(),
                        bottom = here.bottom + padding.calculateBottomPadding().roundToPx()
                    )
                }
                // Kayan bir sütunun içeriğindeyken: içeriğin uzandığından fazlası istenmez. Lazy bir listenin
                // öğesinde bu satır çıkarılır; orada sınırı listenin contentPadding'i tutar.
                grown.intersect(Rect(0f, 0f, own.size.width.toFloat(), own.size.height.toFloat()))
            }
        }
    }
}
```

`BringIntoViewModifierNode` ve `bringIntoView`: `androidx.compose.ui.relocation`; `requireLayoutCoordinates`, `requireLayoutDirection`, `requireDensity`: `androidx.compose.ui.node`.

Bu yalın hâlde iki kullanım iki ayrı modifier'dır (sınırında kesen ve kesmeyen). Pantrix'inki tektir: kaydırılan içeriği boyundan bulur, bulduğu yerde keser, bulamadığı yerde (lazy liste, kaydırıcısına sığan içerik) boşluğun tamamını ister (`RoomInView.kt`, `withRoom`).

İçteki lambda'da `isAttached`'e bakılır: istek, kaydırıcı onun için kaydırdığı sürece her karede yeniden sorulur ve o sırada modifier kompozisyondan çıkmış olabilir; `requireLayoutCoordinates()` bağlı olmayan bir node'da hata fırlatır.

## 5. Modifier'ın yapmadıkları {#limits toc="5. Yapmadıkları"}

*   **Boşluk eklemez.** İçerikte o boşluk yoksa son öğeyi bar'ın üstüne çıkaracak yer de yoktur.
*   **Bar'ın nerede olduğunu bilmez.** Verilen, içeriğin sonlarındaki boşluktur. Kaydırıcı o kenara kadar uzanmıyorsa (klavye açıkken, bar'a değmeyen bir bölmede) örtülen kısım daha azdır ve bunu çağıran söyler (3.3).
*   **Kaydırma alanı küçülürken bir şey yapmaz.** Klavye açılınca odaktaki öğeyi görünür tutan Compose'un kendi izlemesidir; o bir istek değildir ve modifier'dan geçmez. Compose bir metin alanında **imleci** tutar, kutuyu değil: iOS simülatöründe klavye açılınca alanın alt kenarı klavyenin arkasında kaldı. Bu, modifier'dan bağımsız olarak her Compose metin alanında böyledir; `docs/OPEN_ITEMS.md`'de yazılı.
*   **Dokunma yerini bilmez.** Bir düğme, parmağa ayrılan yerin ortasında ve ondan küçük çizilir; görünür alana getirme isteği çizilen kutudan ve halkasından hesaplanır, yerden değil. Çizilenle yeri arasındaki fark bir halkadan büyükse, içeriğin başındaki düğmeye dönülünce kaydırıcı o fark kadar kaymış kalır: dokunmatik bir tarayıcıda düğme 48dp'lik yerde 32dp çizilir ve demo'nun çerçevesi 4dp'de kaldı. Android'de (48 ve 40dp) ve iOS'ta (44 ve 36pt) fark bir halka kadardır ve çerçeve en başa döner.
*   **Ekran okuyucunun odağı klavye odağı değildir.** TalkBack ve VoiceOver bir öğeye gelince kaydırmayı kendi eylemleriyle yapar. Dinlenmedi.
*   **Yeri pikseline söz vermez.** Compose, uzun bir yoldan getirdiğini hedefinden birkaç piksel öteye taşır ve orada bırakır: öğe bar'dan boşluk kadar **ya da biraz daha** uzakta durur.
*   **Lazy listede fazlasını isteyebilir.** Listenin `contentPadding`'i söylenenden azsa (3.2).

## 6. Test etmek {#testing}

*   **Nerede durduğuna bakılır, odakta olduğuna değil.** `requestFocus()`, `waitForIdle()`, sonra öğenin `boundsInRoot`'u bar'ınkiyle karşılaştırılır (`ScrollPaddingTest`, `DemoShellTest`).
*   **Üstündekine bir şey geçmediği ayrıca tutulur.** Kaydırıcı, `onPostScroll`'u yazan bir `NestedScrollConnection`'ın içine konur; liste boş kalmalıdır. İçeriğin iki ucunda ve içerik kaydırıcısına sığarken.
*   **Lazy listede yalnızca görünen öğe vardır.** Odağı alacak öğe önce görünür alana getirilir (`performScrollToIndex`), yarısı dışarıda kalacak şekilde: bütünüyle görünen bir öğe hiçbir şeyi kaydırmaz.
*   **Kesin bir konum küçük bir yolda sınanır.** Uzun yolda Compose'un taşması (5. bölüm) hedefi gizler: sona 180 piksel uzaktan gelen bir çerçeve, istenen 4 piksel eksik olsa da sona yapışır. Demo'nun çerçeve testi çerçeveyi ucundan 6 piksel kaydırıp başlar (`HorizontalScrollFrameTest`).
*   **Tarayıcıda bir piksel pay bırakılır.** JavaScript'te kaydırmanın son adımı pikselin bir payı kadar önce bitebilir.

## 7. Doğrulama {#verification}

### Görülenler

*   Android emülatörü, API 34 (2026-10-11): demo'nun Pagination sayfasında Tab ile inildi; odağı alan her düğme, halkasıyla birlikte, alt bar'ın üstünde durdu. Yana kayan çerçevede satırın sonuna gidilip Shift ve Tab ile başa dönüldü: satır, ilk hâliyle piksel piksel aynı yerdeydi (öncesinde bir halka kadar kaymış kalıyordu).
*   Aynı emülatörde Scaffold sayfasının "Bar'ın altından kayan listede odak" örneği (lazy liste): aşağı inerken odaktaki düğme halkasıyla birlikte örneğin alt bar'ının üstünde, Shift ve Tab ile çıkarken üst bar'ının altında durdu.
*   iPhone 18 Pro Max simülatörü, iOS 27.0: klavye açıkken bar'ın payı düşülmeden yazılan ilk rakamda sayfa bar'ın boyu kadar yukarı sıçradı; düşülünce sıçramadı. Alanın alt kenarı iki hâlde de klavyenin arkasındaydı (5. bölüm).
*   Yerleşik tarayıcı, telefon genişliğinde bir pencere (375 × 812, dokunma taklidiyle): Scaffold sayfasının aynı örneğinde Tab ile inerken ve Shift ve Tab ile çıkarken odaktaki düğme halkasıyla birlikte iki bar'ın arasındaydı; Pagination sayfasında odağı alan düğme alt bar'ın üstündeydi. Yana kayan çerçeve satırın sonuna kadar gitti; başa dönülünce 4dp kaymış kaldı (5. bölüm, dokunma yeri).

### Testler

*   `ScrollPaddingTest` (on iki test, beş hedefte): sütunun sonu ve başı, satırın iki ucu ve sağdan sola düzen, içeriğin uzandığından fazlasının istenmemesi, değişen boşluk, içteki kaydırıcı, üstündekinin kırptığı öğe, kaydırma sürerken kaldırılan boşluk, lazy sütun ve lazy satır, odak halkasıyla birlikte.
*   `DemoShellTest`: sayfanın aşağısındaki bir düğme odağı alınca alt bar'ın üstünde durur.
*   `HorizontalScrollFrameTest`: çerçeve, ucundaki düğme odağı alınca en ucuna kadar gider.
*   Yazılan satırların mutasyonu: 80 mutantın 63'ü öldü, 6'sı derlenmedi; sağ kalan 11'in beşi gerçek boşluktu ve testleri yazıldı (satırı sütunun içinde kayan bir halka, kırpılan öğe, kaldırılan boşluk, halkanın taştığı kadar, kartın kopyalama düğmesi).

### Kaynakta okunanlar

Compose Multiplatform 1.12.1, `foundation` ve `ui`:

*   `ContentInViewNode`: kaydırıcının görünür saydığı kendi boyudur; isteği karşılarken kaydırmayı `NestedScrollSource.UserInput` olarak iletir; alan küçülünce odaktaki öğeyi istekle değil kendi izlemesiyle tutar.
*   `LazyList` ve `LazyGrid`: `LocalBringIntoViewSpec`'i kompozisyonda okur ve yapışan başlıklar için sarar (`StickyHeaderBringIntoViewSpec`); içerik boşluğu hesapta yoktur. Testte de görüldü: `contentPadding`'i olan bir `LazyColumn` öğeyi kendi kenarına getirir.
*   `LocalBringIntoViewSpec`: Android'deki ve Skiko hedeflerindeki bildirimi `@ExperimentalFoundationApi` taşır; kuralı elle verilmeyen her kaydırıcı onu kendi düğümünden okur (`ContentInViewNode.requireBringIntoViewSpec`).
*   `BringIntoViewModifierNode` ve `DelegatableNode.bringIntoView`: deneysel işaret taşımaz.
*   `FocusTargetModifierNode.getFocusedRect` ve `TextFieldDecoratorModifier`: alan küçülünce tutulan, odak hedefinin kendi söylediği dikdörtgendir; metin alanı imlecinkini söyler (`focusProperties.focusRect`).

### Doğrulanmayanlar

*   **TV kumandası, anahtar erişimi, ekran okuyucular.** Yalnızca Tab ile denendi.
*   **iPad ve donanım klavyeli iPhone.** Simülatörde donanım tuşları uygulamaya ulaşmadı; iOS'ta görülen yalnızca klavye açıkkenki durumdur.
*   **Masaüstü uygulamasının penceresi ve geniş bir tarayıcı penceresi.** Testleri koştu; pencerede elle gezilmedi. Geniş pencerede demo'nun alt bar'ı yoktur, gezinmesi yandadır.
*   **Izgaralar, `reverseLayout` ve pager.** Yalnızca `LazyColumn`, `LazyRow`, kayan sütun ve satır sınandı.
*   **Yarı saydam bir bar.** Demo'nun bar'ları opaktır: odağın bar'ın üstünde durduğu görüldü, bar'ın arkasından ne göründüğüne bakılmadı.

## 8. Bu depoda nerede {#repository toc="8. Depoda"}

:::table
| Ne | Nerede |
| :--- | :--- |
| Modifier | `ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/a11y/ScrollPadding.kt` |
| Kaydırılan içeriği bulan ve isteği sınırında kesen hesap | aynı pakette `RoomInView.kt` (`withRoom`); odak halkası da onu kullanır (`FocusRing.kt`) |
| Bir halkanın taştığı kadar | `PantrixPlatform.focusRingReach` (`Platform.kt`) |
| Testler | `ScrollPaddingTest`, `FocusRingInViewTest` (`ui/foundation/src/commonTest/.../a11y`) |
| Demo'nun sayfası | `app/shared/src/commonMain/kotlin/com/pantrixui/cmp/demo/App.kt` (`DemoPage`) |
| Demo'nun yana kayan çerçevesi | `app/shared/.../demo/page/HorizontalScrollFrame.kt` |
| Demo'da örnek | `Scaffold` sayfasında "Bar'ın altından kayan listede odak" |
:::
