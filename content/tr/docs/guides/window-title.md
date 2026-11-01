---
title: Pencerenin başlığı
description: Compose Multiplatform'da tek pencereli, çok ekranlı bir uygulamanın penceresinin neden hep aynı adı taşıdığı, kütüphanenin bunun için ne verdiği ve bunun nasıl sınandığı. Sorun Pantrix'e özgü değildir: web'de ve masaüstünde çalışan her Compose Multiplatform uygulamasında aynı şekilde görülür.
---

Compose hakkında burada söylenenler Compose Multiplatform 1.12.2'nin kaynaklarında (`Window.desktop.kt`, `ComponentUpdater.kt`, `LocalAwtWindow.kt`, `ComposeWindowPanel.desktop.kt`) okunmuştur. Doğrulanamayanlar 7. bölümde ayrıca sayılır.

## 1. Sorun {#problem}

### Belirti

*   Web'de uygulamanın her sayfası tarayıcı sekmesinde aynı adı taşır: `index.html`'in `<title>`'ı. Kullanıcı on sekme açtığında hangisinin "Siparişler" olduğunu göremez; tarayıcının geçmişinde de her girdi aynı addır.
*   Masaüstünde pencerenin başlık çubuğu, görev çubuğu ve pencere değiştirici, uygulama hangi ekranda olursa olsun `Window(title = …)`'a verilen adı gösterir.
*   Ekranın adı zaten bilinir, bar'ında yazar; ama onu pencereye taşıyan ortak bir API yoktur.

:::sketch window-title
:::

### Neden

Pencerenin başlığı ortak kodda yoktur. Her platformda başka bir nesnenin alanıdır:

:::table
| Platform | Pencerenin başlığı | Ortak koddan |
| :--- | :--- | :--- |
| Web (`js`, `wasmJs`) | `document.title`: sekmenin ve geçmişin adı | Yok; `kotlinx.browser.document` yalnızca web kaynak kümesinde |
| Masaüstü (JVM) | `java.awt.Frame.title`: başlık çubuğu, görev çubuğu | Yok; pencereye `LocalAwtWindow` ile ulaşılır, o da `@ExperimentalComposeUiApi` |
| Android | Kullanıcının gördüğü bir pencere başlığı yok: son uygulamalar uygulamanın adını söyler | Gerek yok |
| iOS | Kullanıcının gördüğü bir pencere başlığı yok: uygulama değiştirici uygulamanın adını söyler | Gerek yok |
:::

İki ayrıntı işi büyütür:

1.  **Aynı anda birden çok ekran vardır.** Bir sayfa ötekinin üstüne kayarken ikisi de kompozisyondadır; geniş bir pencerede bir liste ve ayrıntısı yan yana durur. "Ekran açılınca başlığı yaz, kapanınca eskisine dön" yetmez: ikinci ekran açılır, birincisi kapanırken başlığı en baştakine geri koyar ve açık ekranın adı kaybolur.
2.  **Masaüstünde `Window` da başlık yazar.** `Window(title = …)` başlığı yalnızca kendi değeri değişince yeniden yazar (`ComponentUpdater` öncekiyle karşılaştırır); değişmedikçe bir ekranın verdiği başlık kalır, değişirse onun üstüne yazılır.

## 2. Çözüm {#fix}

Her pencere için bir başlık yığını tutulur. Bir ekran kompozisyona girince başlığını yığının sonuna koyar, çıkınca kendi başlığını yığından çıkarır. Pencereye yığının sonundaki ad verilir; yığın boşalınca pencerenin kendi başlığı, ilk ekran gelmeden önce okunan, geri konur. Bir ekranın başlığı değişirse, o ekran yığının sonundaysa pencere de değişir. Uygulama adı bir biçimle eklenir: ekranın adını alıp pencereninkini veren bir fonksiyon, uygulamanın kökünde bir kez verilir.

## 3. Pantrix ile {#with}

```kotlin
import com.pantrixui.foundation.platform.LocalPantrixWindowTitleFormat
import com.pantrixui.foundation.platform.PantrixWindowTitle
import com.pantrixui.foundation.platform.PantrixWindowTitleFormat

// Uygulamanın kökünde, bir kez: her ekranın adının ardına uygulamanın adı.
CompositionLocalProvider(LocalPantrixWindowTitleFormat provides PantrixWindowTitleFormat { "$it · Yönetim" }) {
    App()
}

// Bir ekranda: pencere bu ekran gösterildikçe "Siparişler · Yönetim" adını taşır.
@Composable
fun OrdersScreen() {
    PantrixWindowTitle("Siparişler")
    OrdersList()
}
```

*   `PantrixWindowTitle(title)`: ekran kompozisyonda kaldıkça pencereye adını verir. Web'de `document.title`'ı, masaüstünde pencerenin başlığını yazar; Android'de ve iOS'ta bir şey yapmaz.
*   `LocalPantrixWindowTitleFormat`: ekranın adının pencereye nasıl yazılacağı. Verilmezse `PantrixWindowTitleFormat.Plain`: ad olduğu gibi.
*   `PantrixPageScaffold` bunu kendisi yapar: her sayfa pencereye kendi `title`'ını verir. Bir uygulama ekranlarını onunla kuruyorsa `PantrixWindowTitle`'ı ayrıca çağırmaz.

### 3.1. iç içe sayfalar

Bir sayfanın içeriğindeki başka bir sayfa, bir sayfanın önizlemesi ya da bir örneği gibi, pencereye ad vermez: başlığı içinde durduğu sayfaya bırakır. Yan yana duran iki sayfa, bir liste ve ayrıntısı, iç içe değildir; ikisi de ad verir ve en son gelen kazanır.

## 4. Pantrix olmadan {#without}

### 4.1. ortak kod (`commonMain`)

```kotlin
/** Bir pencerenin başlıkları: en son verilen önce. */
class WindowTitles(private val read: () -> String, private val write: (String) -> Unit) {
    private val given = mutableListOf<Entry>()
    private var own: String? = null

    inner class Entry(title: String) {
        var title: String = title
            set(value) {
                if (field == value) return
                field = value
                if (given.lastOrNull() === this) write(value)
            }

        fun add() {
            if (given.isEmpty()) own = read()
            given.remove(this)
            given += this
            write(title)
        }

        fun remove() {
            val wasLast = given.lastOrNull() === this
            if (!given.remove(this) || !wasLast) return
            val before = given.lastOrNull()
            if (before != null) write(before.title) else own?.let(write).also { own = null }
        }
    }
}

@Composable
expect fun windowTitles(): WindowTitles?

@Composable
fun WindowTitle(title: String) {
    val titles = windowTitles() ?: return
    val entry = remember(titles) { titles.Entry(title) }
    DisposableEffect(entry) {
        entry.add()
        onDispose { entry.remove() }
    }
    SideEffect { entry.title = title }
}
```

### 4.2. platform karşılıkları

```kotlin
// webMain: sayfanın tek bir başlığı vardır.
private val documentTitles = WindowTitles(read = { document.title }, write = { document.title = it })

@Composable
actual fun windowTitles(): WindowTitles? = documentTitles
```

```kotlin
// jvmMain: her pencerenin kendi başlıkları, pencere bırakılınca onunla gider.
private val frameTitles = WeakHashMap<Frame, WindowTitles>()

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun windowTitles(): WindowTitles? {
    val frame = LocalAwtWindow.current as? Frame ?: return null
    return remember(frame) {
        synchronized(frameTitles) {
            frameTitles.getOrPut(frame) { WindowTitles(read = { frame.title.orEmpty() }, write = { frame.title = it }) }
        }
    }
}
```

```kotlin
// androidMain ve iosMain: kullanıcıya görünen bir pencere başlığı yok.
@Composable
actual fun windowTitles(): WindowTitles? = null
```

## 5. Test etmek {#testing}

Yığının kuralları platformsuzdur ve her hedefte sınanır: en son verilen ad kazanır, çıkan ad öncekine yer bırakır, son ad gidince pencerenin kendi başlığı döner, sonda olmayan bir adın değişmesi pencereye dokunmaz.

```kotlin
val titles = WindowTitles(read = { window }, write = { window = it })
val orders = titles.Entry("Siparişler")
val order = titles.Entry("Sipariş 42")
orders.add()
order.add()
assertEquals("Sipariş 42", window)
order.remove()
assertEquals("Siparişler", window)
```

Web'de gerçek bir tarayıcıda `document.title` okunur: bir ekran onu biçimiyle yazar, başlığı değişince değiştirir, kompozisyondan çıkınca sayfanın kendi `<title>`'ını geri koyar. Masaüstünün testleri bir pencere açmaz (`LocalAwtWindow` testte `null`'dır): orada `PantrixWindowTitle` bir şey yapmaz; sayfanın JVM testleri onu böyle çağırır.

## 6. Tuzaklar {#pitfalls}

*   **İlk kare.** Web'de uygulama ilk karesini çizene dek sekme `index.html`'in `<title>`'ını gösterir; ekranın adı ilk kompozisyondan sonra gelir. Sayfanın `<title>`'ı uygulamanın adı olarak kalmalıdır.
*   **`Window(title = …)` değişirse.** Masaüstünde `Window`'a verilen başlık değişince Compose onu yazar ve ekranın adı silinir; ekran yeniden kompozisyona girene dek öyle kalır. Uygulamanın kendi adı sabit bir başlık olarak verilmelidir.
*   **Diyaloglar.** Bir `DialogWindow`'un içindeki ekran ana pencereye ad vermez: `LocalAwtWindow` orada diyaloğun kendisidir ve bir `Frame` değildir.

## 7. Doğrulama {#verification}

### Testler

:::table
| Ne | Nerede | Hedef |
| :--- | :--- | :--- |
| Yığının kuralları | `ui/foundation/src/commonTest/…/platform/WindowTitlesTest.kt` | JVM, iOS, JS, Wasm |
| Tarayıcının sekmesi | `ui/foundation/src/webTest/…/platform/WindowTitleOnTheWebTest.kt` | JS, Wasm |
| Sayfanın sekmeye ad vermesi, iç içe sayfanın vermemesi | `ui/components/src/webTest/…/PageScaffoldOnTheWebTest.kt` | JS, Wasm |
:::

### Elle bozulan davranışlar

Web'in karşılığı `null` döndürünce iki tarayıcı testi de kırmızı oldu; sayfa iç içe olduğuna bakmadan pencereye ad verince sayfanın tarayıcı testi kırmızı oldu.

### Doğrulanmayanlar

*   iPadOS'un çoklu pencere görünümünde bir sahnenin adı: bu kütüphane iOS'ta bir şey yazmaz.
*   Tarayıcı geçmişinin girdilerinin adı.

## 8. Bu depoda nerede {#repository toc="8. Depoda"}

*   API: `ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/platform/PantrixWindowTitle.kt`, platform karşılıkları aynı paketin `webMain`, `jvmMain`, `androidMain` ve `iosMain` dosyaları.
*   Kullanan: `ui/components/src/commonMain/kotlin/com/pantrixui/components/container/page/PageFrame.kt`.
*   Demo: uygulamanın teması biçimi verir (`app/shared/…/theme/Theme.kt`); "Page Scaffold" sayfasının "Pencerenin başlığı" örneği (`app/shared/…/samples/PageScaffoldSamples.kt`).
