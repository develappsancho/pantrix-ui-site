---
title: Web'de geç gelen yazı tipi
description: Compose Multiplatform'un web hedefinde bir uygulamanın kendi yazı tipinin neden bir an geç geldiği, bunu nasıl ölçtüğümüz ve nasıl giderdiğimiz. Sorun Pantrix'e özgü değildir: web hedefi olan ve kendi font dosyasını kullanan her Compose Multiplatform projesinde aynı şekilde görülür.
---

## 1. Sorun {#problem}

### Belirti

*   Sayfa tarayıcıda ilk açıldığında ya da yenilendiğinde metin önce başka bir yazı tipiyle çizilir. Bir saniyeden kısa bir süre sonra bütün harfler değişir, metinler yeniden yerleşir ve satırlar kayar.
*   Uygulama çalışırken yazı tipi değiştirildiğinde (bizde "Ayarlar"daki "Yazı tipi") aynısı bir kez daha olur.
*   Android'de, iOS'ta ve masaüstünde hiç görülmez.

Web sayfalarında bunun adı FOUT'tur (*flash of unstyled text*).

:::sketch web-fonts
:::

### Neden

Compose Multiplatform'da bir font dosyası `Font(Res.font.…)` ile okunur. Bu fonksiyon platforma göre iki ayrı şey yapar (Compose Multiplatform 1.12.1, `components-resources`):

:::table
| Platform | Okuma | İlk karede |
| :--- | :--- | :--- |
| Android, iOS, masaüstü | Eşzamanlı: dosya, istendiği anda okunur (`ResourceState.blocking.kt`) | Yazı tipi hazırdır |
| Web (`js`, `wasmJs`) | Eşzamansız: dosya `fetch` ile indirilir (`ResourceState.web.kt`) | Yazı tipi yoktur |
:::

Web'de `Font(…)`, dosya gelene dek bir **yedek** döndürür: Skiko'nun içinde taşıdığı Roboto (`FontResources.skiko.kt`, `defaultFont`). Dosya gelince döndürdüğü değer değişir, onu okuyan her şey yeniden kurulur ve her metin yeniden yerleşir. Üç ayrıntı sorunu büyütür:

1.  **Okuma geç başlar.** `Font(…)` bir composable'dır: indirme ancak uygulama ilk kez kurulurken, yani Wasm indirilip derlendikten sonra başlar.
2.  **Her ağırlık ayrı bir fonttur.** Önbelleğin anahtarı dosyanın yolu, ağırlığı, stili ve eksen ayarıdır. Yedi ağırlıkta kaydedilen tek bir değişken font yedi kez okunur ve yedi ayrı anda gelebilir: tema birkaç kez baştan kurulur.
3.  **Her yüklemede yeniden istenir.** Kaynakların tarayıcıdaki önbelleği (Cache API) her sayfa yüklemesinde sıfırlanır (`ResourceWebCache.web.kt`); dosyayı tarayıcının kendi HTTP önbelleği verir.

### Ölçüm

Üretim paketi (`wasmJsBrowserDistribution`) yerel bir sunucudan açıldı; zamanlar sayfaya eklenen bir ölçüm betiğiyle okundu (bkz. 6. bölüm). Sayılar sayfa açıldıktan sonraki milisaniyelerdir.

:::table
| Olay | Düzeltmeden önce | Düzeltmeden sonra |
| :--- | :--- | :--- |
| Yazı tipi dosyaları istendi | 408 (uygulama kurulduktan sonra; yalnızca temanın dosyası) | 71 (kod inerken, `<link rel="preload">`; on beş dosyanın hepsi) |
| Bütün fontlar okundu | 440 | 274 |
| Uygulamanın ilk karesi | 401, **yedek yazı tipiyle** | 456, **kendi yazı tipiyle** |
| Harfler değişti | 500 dolayında | Hiç |
| "Ayarlar"da başka bir yazı tipi seçildi | Yedi dosya o anda istenir; gelene dek yedek yazı tipi | Yeni istek yok; bir karede değişir |
:::

Yerelde ağ gecikmesi yoktur ve fark yüz milisaniyedir; gerçek bir ağda yazı tipi geç geldiği kadar uzun sürer. İlk karenin kendisi iki ölçümde de 175 ile 240 ms arasında sürdü: yazı tipini beklemek açılışı geciktirmedi, çünkü dosyalar o kare kurulmadan çok önce inmişti.

## 2. Çözüm: dört parça {#fix}

:::table
| # | Parça | Ne sağlar |
| :--- | :--- | :--- |
| 1 | **Erken indir.** `index.html`'de `<link rel="preload">` | Dosya, uygulamanın kodu inerken iner; uygulama istediğinde tarayıcının önbelleğindedir |
| 2 | **Önce oku, sonra çiz.** Uygulamayı bir kapının arkasına al | Uygulama ilk karesini kendi yazı tipiyle çizer; harfler değişmez |
| 3 | **Yükleme işaretini tut.** Uygulamayı gövdeye değil, kendi kutusuna kur | Beklerken boş sayfa değil, sayfanın yükleme işareti görünür |
| 4 | **Değişen yazı tipinde eskisinde kal.** Yenisi gelince bir kez değiş | Çalışırken yazı tipi değiştirmek de yedek yazı tipini göstermez |
:::

İkinci parça işin özüdür ve Compose'un bir özelliğine dayanır: **okunan font bellekte kalır** ve onu sonradan isteyen ilk kurulumda hemen bulur. Yani yazı tipini önce bir yerde sorup (`preloadFont`) uygulamayı ondan sonra kurmak yeter; uygulamanın kendisinde hiçbir şey değişmez.

## 3. Pantrix ile {#with}

Kütüphane dört parçanın Kotlin tarafını verir (`com.pantrixui.foundation.token.typography`):

:::table
| API | Ne yapar |
| :--- | :--- |
| `PantrixFont(resource, weight, style, variationSettings)` | Bir font: kaynaklardaki dosya ve ailenin onu kaydettiği ağırlık ile stil. `variationSettings` değişken bir fontun eksenlerindeki yeridir; verilmezse ağırlık ile stilin söylediğidir. Önden okuma da aynı ayarla sorar |
| `PantrixTypeface(vararg fonts)` | Bir yazı tipi, font dosyalarıyla. `PantrixTypeface.Default` kütüphanenin kendi yazı tipidir |
| `rememberPantrixTypography(typeface)` | Pantrix'in ölçeği, o yazı tipiyle. Yazı tipi değişirken eskisinde kalır, yenisi gelince bir kez değişir |
| `rememberPantrixFontFamily(typeface)` | Yalnızca aile (`FontFamily`) |
| `areTypefacesLoaded(vararg typefaces)` | Verilen yazı tiplerinin bütün fontları okundu mu. Sormak okumayı başlatır |
| `isPantrixTypefaceLoaded()` | Aynısı, kütüphanenin kendi yazı tipi için |
| `PantrixTypefaceGate(loaded, timeoutMillis, onShown, content)` | İçeriği yazı tipi okunduktan sonra çizer; en çok `timeoutMillis` kadar bekler |
:::

Android'de, iOS'ta ve masaüstünde hepsi hemen cevap verir: aynı kod her platformda yazılır, yalnızca web'de bekler.

### 3.1. Yazı tipini bir kez söyle

```kotlin
// commonMain
val Brand = PantrixTypeface(
    PantrixFont(Res.font.brand_regular),
    PantrixFont(Res.font.brand_medium, FontWeight.Medium),
    PantrixFont(Res.font.brand_bold, FontWeight.Bold)
)

@Composable
fun App() {
    PantrixTheme(typography = rememberPantrixTypography(Brand)) {
        // …
    }
}
```

Fontlar tek bir yerde sayılır; aile de, tarayıcıda okunup okunmadıkları da o listeden sorulur. İkisi ayrı yerlerde sayılırsa bir gün birbirini tutmaz (bkz. 5. bölüm, "Aynı anahtar").

Yalnızca kütüphanenin kendi yazı tipini kullanan bir uygulamanın bir şey söylemesi gerekmez: `PantrixTheme { … }` yeter.

### 3.2. Web girişi

```kotlin
// webMain
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // Kutusu olmayan bir sayfada (tarayıcının sakladığı eski bir index.html) gövdeye kurulur.
    val box = "app".takeIf { document.getElementById(it) != null }
    ComposeViewport(viewportContainerId = box) {
        PantrixTypefaceGate(
            loaded = areTypefacesLoaded(Brand),
            onShown = { document.getElementById("loading")?.remove() }
        ) {
            App()
        }
    }
}
```

Kütüphanenin kendi yazı tipi için `loaded` verilmez: `PantrixTypefaceGate(onShown = …) { App() }`.

### 3.3. Sayfa

```html
<head>
    <!-- Yazı tipi, uygulamanın kodu inerken indirilir. `crossorigin` olmadan tarayıcı bu indirmeyi
         uygulamanın kendi isteğine saymaz ve dosya iki kez iner. -->
    <link rel="preload" as="fetch" type="font/ttf" crossorigin
          href="composeResources/<paket>.generated.resources/font/brand_regular.ttf">
</head>
<body>
    <!-- Yükleme işareti: uygulamanın ilk karesi çizilene dek üstünde durur. -->
    <div id="loading">…</div>
    <!-- Uygulamanın kurulduğu kutu. -->
    <div id="app"></div>
    <script type="application/javascript" src="webApp.js"></script>
</body>
```

```css
html, body { width: 100%; height: 100%; margin: 0; padding: 0; overflow: hidden; }

#app { width: 100%; height: 100%; }

#loading {
    position: fixed;
    inset: 0;
    z-index: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    pointer-events: none;
}
```

Adresteki `<paket>`, kaynakları taşıyan modülün üretilen `Res` sınıfının paketidir. Kütüphanenin kendi yazı tipinin adresi `composeResources/com.pantrixui.foundation.generated.resources/font/plus_jakarta_sans.ttf`'dir. Emin olmak için paketi bir kez derleyip `build/dist/…/composeResources` klasörüne bakın.

### 3.4. Çalışırken değişen yazı tipi

`rememberPantrixTypography(typeface)` bunu kendisi çözer: `typeface` değiştiğinde, yenisi okunana dek eski tipografiyi vermeyi sürdürür ve yenisini bir kez, tamamı geldiğinde verir.

```kotlin
val typeface = if (settings.brand) Brand else PantrixTypeface.Default
PantrixTheme(typography = rememberPantrixTypography(typeface)) { /* … */ }
```

Seçim ile değişim arasında dosyaların inmesi kadar bir süre geçer. O da istenmiyorsa yazı tipleri önden okunur: kapıya hepsi birlikte verilir ve sayfa hepsini önden indirir. Bu depodaki örnek uygulama öyle yapar, çünkü üç yazı tipini yan yana gösteren bir sayfası vardır:

```kotlin
PantrixTypefaceGate(loaded = areTypefacesLoaded(PantrixTypeface.Default, PlusJakartaSans, Poppins)) { App() }
```

Bedeli indirilen bayttır: örnek uygulamada on beş dosya, 2,0 MB. Bir ürün için doğru olan çoğunlukla yalnızca ilk ekranın yazı tipini önden okumak, ötekileri değişime bırakmaktır.

### 3.5. Fontlarda olmayan karakterler

Tarayıcıda Compose, uygulamanın hiçbir fontunda olmayan bir karakteri (bir okun, `⌘`'nin, Arapça bir harfin) Google'ın sunucularından indirdiği bir Noto fontuyla çizer (`NotoFontDownloader`, `https://fonts.gstatic.com/s/`). Bunun üç bedeli vardır:

*   Her ziyaretçinin tarayıcısı Google'a istek atar.
*   Çevrimdışı bir sayfada karakter boş kalır.
*   Bir içerik güvenliği politikası (CSP) `connect-src`'üne `https://fonts.gstatic.com`'u koymak zorunda kalır.

Compose'un bunu kapatan genel bir ayarı yoktur. Ama indirdiği fontu herkese açık bir yolla kurar, `FontFamily.Resolver.preload`; karakteri taşıyan bir font önceden aynı yolla kurulmuşsa Compose eksik karakter görmez ve hiçbir şey indirmez.

`PantrixTypeface`'in `fallbacks`'i bu fontlardır. Yazı tipinin fontlarıyla birlikte okunur, çözücüye kurulur ve `areTypefacesLoaded` (kapı da) onları da bekler. Kütüphanenin kendi yazı tipi, `PantrixTypeface.Default`, bir kısayolun Apple'ın sistemlerinde yazıldığı ve Plus Jakarta Sans'ta olmayan işaretler için Pantrix Symbols'ı taşır: `⌘ ⇧ ⌥ ⌫ ⌦ ⇥ ⇞ ⇟ ⎋`, Noto'nun üç fontundan kesilmiş 3 KB. Kendi yazı tipini veren bir uygulama onu kendisi ekler; Arapça yazan bir uygulama kendi Arapça fontunu da verir:

```kotlin
val Brand = PantrixTypeface(
    fonts = listOf(PantrixFont(Res.font.brand_regular), PantrixFont(Res.font.brand_bold, FontWeight.Bold)),
    fallbacks = PantrixTypeface.Default.fallbacks + PantrixFont(Res.font.noto_sans_arabic)
)
```

Android'de, iOS'ta ve masaüstünde böyle bir karakteri sistemin fontları çizer; yedekler orada okunmaz. Hangi karakterlerin indirildiği, sayfa açıkken `performance.getEntriesByType('resource')`'da `fonts.gstatic.com` adresleriyle görülür.

## 4. Pantrix olmadan {#without}

Aynı çözüm, yalnızca Compose Multiplatform'un kendi API'siyle. Aşağıdaki kod herhangi bir projeye kopyalanabilir; kütüphanedeki karşılıklarının sadeleştirilmiş hâlidir.

### 4.1. Ortak kod (`commonMain`)

```kotlin
/** Bir font: kaynaklardaki dosya ve ailenin onu kaydettiği ağırlık ile stil. */
data class AppFont(
    val resource: FontResource,
    val weight: FontWeight = FontWeight.Normal,
    val style: FontStyle = FontStyle.Normal
)

/** Fontların ailesi. Tarayıcıda henüz gelmemiş bir font, ailede yedek yazı tipidir. */
@Composable
fun rememberFontFamily(fonts: List<AppFont>): FontFamily {
    val read = key(fonts) { fonts.map { Font(it.resource, it.weight, it.style) } }
    return remember(read) { FontFamily(read) }
}

/** Fontların hepsi okundu mu. Sormak okumayı başlatır; web dışında her zaman doğrudur. */
@Composable
expect fun areFontsLoaded(fonts: List<AppFont>): Boolean

/** [value] okunmuşsa kendisi; okunmamışsa en son okunmuş olan. Hiçbiri okunmamışsa yine [value]. */
@Composable
fun <T> lastLoaded(value: T, loaded: Boolean): T {
    var last by remember { mutableStateOf<T?>(null) }
    if (!loaded) return last ?: value
    SideEffect { last = value }
    return value
}

/** [content]'i yazı tipi okunduktan sonra çizer; en çok [timeoutMillis] kadar bekler. */
@Composable
fun FontGate(
    loaded: Boolean,
    timeoutMillis: Long = 3000,
    onShown: () -> Unit = {},
    content: @Composable () -> Unit
) {
    var waiting by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(timeoutMillis)
        waiting = false
    }
    // Bir kez açılan kapı açık kalır: çizilmiş bir uygulama yeniden kurulmaz.
    var opened by remember { mutableStateOf(false) }
    if (opened || loaded || !waiting) {
        SideEffect { opened = true }
        content()
        val shown by rememberUpdatedState(onShown)
        LaunchedEffect(Unit) {
            // İçeriği taşıyan kareden sonraki ilk kare: o kare çizilmiştir.
            withFrameNanos { }
            shown()
        }
    }
}
```

### 4.2. Platform karşılıkları

```kotlin
// webMain
@OptIn(ExperimentalResourceApi::class)
@Composable
actual fun areFontsLoaded(fonts: List<AppFont>): Boolean {
    // Önce hepsi istenir, sonra bakılır: fontlar art arda değil, yan yana okunur.
    val read = key(fonts) { fonts.map { preloadFont(it.resource, it.weight, it.style).value } }
    return read.none { it == null }
}
```

```kotlin
// androidMain, iosMain, jvmMain
@Composable
actual fun areFontsLoaded(fonts: List<AppFont>): Boolean = true
```

`preloadFont` yalnızca web kaynak kümelerinde vardır ve `@ExperimentalResourceApi` taşır; ortak koddan çağrılamaz. `expect`/`actual` bu yüzden gerekir. `webMain`, `js` ve `wasmJs` hedeflerinin ikisi de tanımlıysa Kotlin'in kendiliğinden kurduğu kaynak kümesidir.

### 4.3. Kullanım

```kotlin
// commonMain
val Brand = listOf(
    AppFont(Res.font.brand_regular),
    AppFont(Res.font.brand_bold, FontWeight.Bold)
)

@Composable
fun App() {
    val family = rememberFontFamily(Brand)
    // Çalışırken değişen bir yazı tipi için: yenisi okunana dek eskisinde kalır.
    val shown = lastLoaded(family, loaded = areFontsLoaded(Brand))
    // typographyIn: tipografinizi verilen aileyle kuran kendi fonksiyonunuz.
    MaterialTheme(typography = typographyIn(shown)) { /* … */ }
}
```

```kotlin
// webMain
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val box = "app".takeIf { document.getElementById(it) != null }
    ComposeViewport(viewportContainerId = box) {
        FontGate(
            loaded = areFontsLoaded(Brand),
            onShown = { document.getElementById("loading")?.remove() }
        ) {
            App()
        }
    }
}
```

Sayfa ve stil 3.3'tekilerle aynıdır.

### 4.4. Fontlarda olmayan karakterler

Yedek bir font, okununca pencerenin yazı tipi çözücüsüne kurulur; Compose'un kendi indirdiklerini kurduğu yol budur:

```kotlin
// webMain
@OptIn(ExperimentalResourceApi::class)
@Composable
fun installFallbacks(fallbacks: List<FontResource>): Boolean {
    val read = fallbacks.map { preloadFont(it).value }
    val resolver = LocalFontFamilyResolver.current
    var installed by remember(fallbacks, resolver) { mutableStateOf(false) }
    LaunchedEffect(read, resolver) {
        val fonts = read.filterNotNull()
        if (fonts.size == fallbacks.size) {
            fonts.forEach { resolver.preload(FontFamily(it)) }
            installed = true
        }
    }
    return fallbacks.isEmpty() || installed
}
```

Kapıya fontlarla birlikte verilir: `FontGate(loaded = areFontsLoaded(Brand) && installFallbacks(Symbols))`. Buradaki `&&` ikinci çağrıyı geciktirmez, çünkü ikisi de her kurulumda çağrılır ve okuma ilk çağrıda başlar; bir değişkene alıp sonra birleştirmek daha açıktır.

## 5. Tuzaklar {#pitfalls}

:::table
| Tuzak | Ne olur | Ne yapılır |
| :--- | :--- | :--- |
| **Aynı anahtar** | Önden okunan font ile temanın istediği font aynı dosya, **aynı ağırlık ve aynı stil** değilse önbellekte bulunmaz; uygulama yine yedekle çizilir | Fontları tek bir listede say; aileyi de, soruyu da o listeden kur |
| **`&&` ile sormak** | `areLoaded(a) && areLoaded(b)`: ikincisi, birincisi gelene dek hiç sorulmaz; dosyalar art arda iner | Hepsini tek çağrıda ver |
| **`crossorigin`'siz ön yükleme** | Tarayıcı indirdiğini uygulamanın `fetch` isteğine saymaz; dosya iki kez iner ve konsola uyarı düşer | `as="fetch"` ve `crossorigin` birlikte |
| **Gövdeye kurulan uygulama** | `ComposeViewport`, kurulduğu kutunun içini siler: yükleme işareti uygulamanın ilk karesinden önce gider, arada boş sayfa kalır | Uygulamayı kendi kutusuna kur; işareti ilk kareden sonra kaldır |
| **Zaman aşımı olmayan kapı** | Dosya gelmezse (yanlış adres, kesik ağ) sayfa sonsuza dek boş kalır | Bir süre bekle, sonra yedekle çiz |
| **Kapanabilen kapı** | Cevap sonradan "okunmadı"ya dönerse uygulama kaldırılır ve bütün durumunu yitirir | Bir kez açılan kapı açık kalsın |
| **Eski `index.html`** | Tarayıcı eski sayfayı saklamışsa kutu yoktur; kutuyu arayan uygulama hiç açılmaz | Kutu yoksa gövdeye kur |
| **Elle yazılan adresler** | Modülün ya da dosyanın adı değişince ön yükleme boşa iner; hiçbir şey bozulmaz, yalnızca geç iner | Adresleri font klasörleriyle karşılaştıran bir test (bizde `WebFontPreloadTest`) |
| **Gizli sekme** | Tarayıcı gizli sekmeye kare vermez; uygulama görünür olana dek kurulmaz | Bir hata değildir; ölçerken sekmenin görünür olduğuna bak |
| **Tarayıcı testleri** | Karma kaynakları sunmaz: testlerde font hiç gelmez ve soru hep "okunmadı" der | Teste kendi okuyucunu ver (`LocalResourceReader`) |
| **Her şeyi önden okumak** | Hiç kullanılmayacak yazı tipleri de her ziyaretçiye iner | Yalnızca ilk ekranın yazı tipini önden oku; ötekileri değişime bırak |
| **`webMain`'de dizi** | `js` ile `wasmJs`'in ortak kaynak kümesinde editör bir dizinin uzantısını çözemez: `vararg` bir parametrede `flatMap`, "Cannot access 'kotlin.Cloneable' which is a supertype of 'Array'" uyarısı verir. Derleme geçer, uyarı yalnızca editördedir | `vararg`'ı ortak kodda listeye çevir; platform karşılığı liste alsın (4.1'deki `areFontsLoaded` gibi) |
| **Fontta olmayan bir karakter** | Compose onun için Google'dan bir Noto fontu indirir; sayfa çevrimdışıysa karakter boş kalır, CSP Google'a izin vermek zorunda kalır | Karakteri taşıyan fontu yedek olarak ver (3.5): `PantrixTypeface.Default.fallbacks` Mac tuşlarının işaretlerini taşır |
:::

## 6. Doğrulama {#verification}

### Ölçüm betiği

Tarayıcıda bir karenin tam olarak ne zaman çizildiği dışarıdan görülmez; olayların sırası görülür. Üretim paketinin bir kopyasına, `index.html`'de uygulamanın betiğinden önce yüklenen küçük bir betik eklenir:

```js
(function () {
  const log = (m) => console.log('[font-probe] ' + Math.round(performance.now()) + 'ms ' + m);
  const isFont = (s) => s.indexOf('/font/') >= 0;
  const origFetch = window.fetch;
  window.fetch = function (u) {
    const s = String((u && u.url) || u);
    if (isFont(s)) log('fetch ' + s.split('/').pop());
    return origFetch.apply(this, arguments);
  };
  // Bir kareden uzun süren kareler: baştan kurulan bir sayfa burada görünür.
  let last = performance.now();
  const tick = (now) => {
    if (now - last > 24) log('long frame ' + Math.round(now - last) + 'ms');
    last = now;
    if (now < 6000) requestAnimationFrame(tick);
  };
  requestAnimationFrame(tick);
})();
```

Beklenen sıra: fontlar uygulamanın ilk uzun karesinden **önce** okunmuştur, ve o kareden sonra font için yeni bir istek görülmez. `performance.getEntriesByType('resource')` dosyayı kimin istediğini söyler: ön yükleme çalışıyorsa `initiatorType` `"link"`tir ve dosya bir kez iner.

### Testler

:::table
| Test | Neye bakar |
| :--- | :--- |
| `ui/foundation`, `PantrixTypefaceWebTest` (tarayıcıda) | Bir yazı tipi bütün fontları okununca okunmuştur; birlikte sorulanlar birlikte okunur; okunan, sonraki ilk kurulumda hazırdır; ölçek, yeni yazı tipi gelene dek eskisinde kalır |
| `ui/foundation`, `PantrixTypefaceTest` | Web dışında yazı tipi baştan okunmuştur; yolda olan verilmez, en son okunan verilir |
| `ui/foundation`, `PantrixTypefaceGateTest` | Kapı yazı tipi gelince açılır, bir kez açılınca açık kalır, sayfaya içerik çizildikten sonra haber verir, bir süre sonra beklemekten vazgeçer |
| `app/shared`, `WebFontPreloadTest` | Sayfanın önden indirdiği dosyalar, font klasörlerindeki dosyalarla aynıdır; her satır `as="fetch"` ve `crossorigin` taşır |
| `ui/foundation`, `PantrixTypefaceWebTest` (tarayıcıda), yedekler | Yedek fontlarla birlikte istenir; yazı tipi yedek okunup kurulunca okunmuştur; okunamayan yedek yazı tipini bekletir; font olmayan bir yedek hiçbir şeyi kırmaz |
| `ui/foundation`, `PantrixSymbolsOnTheDesktopTest` | Bir kısayolun Apple'ın sistemlerinde yazıldığı her karakter Plus Jakarta Sans'ta ya da Pantrix Symbols'ta vardır |
:::

## 7. Bu depoda nerede {#repository toc="7. Depoda"}

:::table
| Dosya | Ne |
| :--- | :--- |
| `ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/token/typography/PantrixTypeface.kt` | `PantrixFont`, `PantrixTypeface`, `rememberPantrixFontFamily`, `areTypefacesLoaded`, `isPantrixTypefaceLoaded` |
| `ui/foundation/src/webMain/…/PantrixTypeface.web.kt` | `areTypefacesLoaded`'ın tarayıcıdaki parçası: `areFontsLoaded` (`preloadFont`). Yazı tiplerinin fontları ortak kodda tek listeye toplanır, burası yalnızca listeyi alır |
| `ui/foundation/src/commonMain/…/PantrixTypography.kt` | `rememberPantrixTypography(typeface)`: yeni yazı tipi gelene dek eskisinde kalır |
| `ui/foundation/src/commonMain/…/PantrixTypefaceGate.kt` | Kapı |
| `app/shared/src/commonMain/kotlin/com/pantrixui/cmp/demo/theme/Typefaces.kt` | Örnek uygulamanın kendi iki yazı tipi |
| `app/webApp/src/webMain/kotlin/com/pantrixui/cmp/demo/main.kt` | Web girişi: kutu, kapı, yükleme işaretinin kaldırılması |
| `app/webApp/src/webMain/resources/index.html`, `styles.css` | Ön yükleme satırları, `#app` ve `#loading` |
| `ui/foundation/src/commonMain/composeResources/font/pantrix_symbols.ttf` | Pantrix Symbols, kütüphanenin yedeği; lisansı yanında: `files/licenses/pantrix_symbols_ofl.txt` |
| `tooling/fonts/pantrix_symbols.py` | Pantrix Symbols'ı Noto'nun üç fontundan keser (fontTools) |
:::

## 8. Açık kalanlar {#open}

Bu işte görülen ve dokunulmayan maddeler `docs/OPEN_ITEMS.md`'dedir. Başlıcaları:

*   Kütüphanenin yazı tipi tek dosyadır ama tarayıcıda yedi kez okunur; yazı tipini beklemeyen bir uygulamada ağırlıklar ayrı ayrı gelir.
*   `preloadFont` deneyseldir (`@ExperimentalResourceApi`); Compose Multiplatform'un sonraki bir sürümünde değişebilir.
*   Yalnızca yerleşik tarayıcıda ve Wasm hedefinde görüldü; gerçek tarayıcılarda ve yavaş bir ağda bakılmadı.
*   Yükleme işareti Wasm derlenirken takılır ve koyu temalı bir cihazda da beyaz zeminde açılır.
