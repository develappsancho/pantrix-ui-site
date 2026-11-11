---
title: Pencereye uyum
description: PantrixTheme'in içeriğine verdiği pencere bilgisi: ne olduğu, nereden geldiği, nasıl okunduğu ve testte nasıl sabitlendiği.
---

Bir ekran ya da bileşen yazarken hangi yardımcının nasıl kullanılacağı, çalışan üç örnek vakayla [ADAPTIVE_USAGE_GUIDE.md](/docs/guides/adaptive-usage) belgesindedir. Bir arayüzün pencereye uyarken tutacağı kurallar [adaptive/adaptive-rules.md](/docs/adaptive-rules) belgesindedir.

## 1. Ne veriyor? {#what-does-it-give}

`PantrixTheme` kullanan her ekran, çizildiği pencereyi hazır bulur. Ayrı bir sarmalayıcı ya da kurulum yoktur:

```kotlin
PantrixTheme {
    val window = PantrixTheme.window

    window.widthClass                 // Compact, Medium, Expanded, Large, ExtraLarge
    window.heightClass                // Compact, Medium, Expanded
    window.size                       // DpSize
    window.posture.isBook             // yarı açık, menteşe dikey
    window.posture.isTabletop         // yarı açık, menteşe yatay
    window.posture.separatingFold     // içeriğin üstünden geçmemesi gereken menteşe, yoksa null
    window.paneCount()                // yan yana kaç panele yer var: 1, 2 ya da 3 (10. bölüm)
    window.breakpoints                // sınıfların başladığı sınırlar

    PantrixTheme.layout.margin        // sayfanın kenar boşluğu, pencerenin sınıfına göre
    PantrixTheme.layout.gutter        // sütunların ve panellerin arası
}
```

Türler `com.pantrixui.foundation.adaptive` paketindedir. Bakılan şey cihaz ya da ekran değil, **penceredir**: bölünmüş ekrandaki, boyutu değiştirilen bir masaüstü penceresindeki ya da tarayıcı sekmesindeki uygulama o pencere kadar geniştir.

:::sketch adaptive
:::

## 2. Sınıflar ve sınırlar {#classes-and-breakpoints}

:::table
| Genişlik sınıfı | Genişlik (dp) | Nerede görülür |
| :--- | :--- | :--- |
| `Compact` | < 600 | Dikey telefon, katlanabilirin kapak ekranı, dar tarayıcı penceresi |
| `Medium` | 600–839 | Açık katlanabilir, dikey tablet, bölünmüş ekran |
| `Expanded` | 840–1199 | Yatay tablet, yatay telefonların çoğu |
| `Large` | 1200–1599 | Masaüstü penceresi, tarayıcı, büyük tablet |
| `ExtraLarge` | ≥ 1600 | Geniş masaüstü penceresi, harici ekran |
:::

:::table
| Yükseklik sınıfı | Yükseklik (dp) | Nerede görülür |
| :--- | :--- | :--- |
| `Compact` | < 480 | Yatay tutulan telefon |
| `Medium` | 480–899 | Dikey telefon, yatay tablet |
| `Expanded` | ≥ 900 | Dikey tablet, masaüstü penceresi |
:::

Sınırlar Android'in pencere boyutu sınıflarıdır ve `PantrixBreakpoints`'te durur. Sınıflar sıralıdır; "şu genişlikten itibaren" bir karşılaştırmadır:

```kotlin
if (window.widthClass >= WindowWidthClass.Expanded) ListBesideDetail() else ListAlone()
```

## 3. Yardımcılar: değer seçmek ve sormak {#helpers-choosing-a-value-and-asking}

Bir ekranın en sık yazdığı seçimler ve sorular için yardımcılar vardır. Hepsi `com.pantrixui.foundation.adaptive` paketindedir ve pencerenin zaten söylediği bir şeye ad verir.

```kotlin
val window = PantrixTheme.window

// Değer seçenler: verilmeyen sınıf bir küçüğünün, verilmeyen duruş `flat`'in değerini alır
val columns = window.byWidth(compact = 2, medium = 4)                    // 2, 4, 4, 4, 4
val barHeight = window.byHeight(compact = 44.dp, medium = 56.dp)
val panes = window.byPosture(flat = 1, book = 2, tabletop = 2)

// Sorular
window.isNarrow       // genişlik Compact: dikey telefon
window.isShort        // yükseklik Compact: yatay telefon
window.isSizeKnown    // platform boyutu söyledi mi

// Composable içinde kısa yazım
val cardPadding = responsive(compact = 12.dp, medium = 16.dp, large = 20.dp)  // PantrixTheme.window.byWidth(...) ile aynı
val threeColumns = rememberWindowIsAtLeast(1000.dp)                      // sınıfların başlamadığı bir genişlik

// Bileşenin kendi yeri: kutu, verilen yeri bir pencere gibi söyler
BoxWithPantrixWindow(modifier) { box -> if (box.isNarrow) OrderList() else OrderTable() }
```

:::table
| Yardımcı | Ne söyler | Ne zaman yeniden kurar |
| :--- | :--- | :--- |
| `byWidth(compact, medium, expanded, large, extraLarge)` | Genişlik sınıfının değeri | Sınıf değişince |
| `byHeight(compact, medium, expanded)` | Yükseklik sınıfının değeri | Sınıf değişince |
| `byPosture(flat, book, tabletop)` | Duruşun değeri: kitapta `book`, masada `tabletop`, öteki her durumda `flat` | Duruş değişince |
| `isNarrow` | Genişlik sınıfı `Compact` mı | Sınıf değişince |
| `isShort` | Yükseklik sınıfı `Compact` mı | Sınıf değişince |
| `isSizeKnown` | Platform pencerenin genişliğini ve yüksekliğini söyledi mi | Cevap değişince |
| `responsive(...)` | Temanın penceresinin `byWidth`'i | Sınıf değişince |
| `rememberWindowIsAtLeast(width)` | Pencere en az `width` genişliğinde mi | Pencere o genişliği geçerken |
| `BoxWithPantrixWindow { box -> … }` | Bileşene verilen yer, bir pencere olarak (10. bölüm) | İçeriğin okuduğu sınıf değişince |
:::

Notlar:

*   **"Şu sınıftan itibaren" için yardımcı yok, çünkü gerekmiyor:** sınıflar sıralıdır. `window.widthClass >= WindowWidthClass.Expanded`, `<=` ve aralık (`in WindowWidthClass.Medium..WindowWidthClass.Expanded`) aynı işi görür.
*   **`isNarrow` ve `isShort`, boyut bilinmeden de `true`'dur:** pencere o ana kadar iki yönde de `Compact` okunur (5. bölüm).
*   **`responsive` yalnızca genişliğe bakar** ve yalnızca composable içinde çağrılır. Yükseklik için, ya da composable olmayan bir yerde (ölçüm, bir geri çağrı), pencerenin kendisine sor: `byHeight`, `byWidth`.
*   **`rememberWindowIsAtLeast` tek bir ekranın özel eşiği içindir.** Bütün düzenler aynı genişliklerde değişiyorsa sınırları değiştir (9. bölüm).
*   **Bir bloğu yalnızca bazı sınıflarda göstermek için düz `if` yeter.** Aynı içeriği sınıfa göre iki ayrı yerden çağırma: sınıf değişince içeriğin hatırladıkları (seçim, kaydırma, yazılan metin) sıfırlanır. Tek yerden çağır, değerleri sınıfa göre seç.

## 4. Yükseklik: yatay telefon {#height-a-landscape-phone}

Düzeni genişlik seçer. Yükseklik, üst üste dizmeye yer olup olmadığını söyler.

Yatay tutulan telefon genişliğiyle `Medium` ya da `Expanded` çıkar (iPhone 17: 874 × 402 dp), ama yüksekliği `Compact`'tır: enine uzanan her çubuk ekranın yedide birini alır. Yüksekliği `Compact` olan pencerenin kuralı:

*   Düzeni yine genişlik seçer: navigasyon yanda durur, 840 dp'den genişse liste ve detay yan yana gelir.
*   Yandaki navigasyon kısa pencereye sığan kadar öğe taşır.
*   Altta sabit çubuk olmaz: ana aksiyon başlıktadır.
*   Alttan açılan sheet olmaz: menüler popover, formlar yan sheet'tir.
*   Masa duruşu bu kuralın dışındadır: orada alt yarı kontrollerin yeridir.

Bugün bu kuralın bileşenlerde karşılığı olan tek maddesi ilki: `PantrixScaffold` navigasyonu 600 dp'den itibaren yana alır, yatay telefonda da. Öteki maddeler için kütüphane yalnızca sorulacak değeri (`heightClass`) veriyor; bileşenler geldikçe kuralı kendileri uygulayacak.

## 5. Boyut nereden geliyor? {#where-does-the-size-come-from}

Tema ölçüm yapmaz; boyutu Compose'un kendi pencere bilgisinden (`LocalWindowInfo`) okur. 3 Ekim 2026'da her platformda örnek uygulamayla bakıldı:

:::table
| Platform | Okunan boyut | İlk kompozisyon |
| :--- | :--- | :--- |
| Android | Activity'nin penceresi, sistem çubukları dahil. Katlanabilir emülatörde: açıkken 852 × 883, kapak ekranında 443 × 970, kapak yatayken 970 × 443 | Doğru boyutla başlar |
| iOS | `UIWindow`'un sınırları. iPhone 17: 402 × 874, iPad: 820 × 1180 | Doğru boyutla başlar |
| Masaüstü | Pencerenin içerik alanı (800 × 600'lük pencerede 800 × 572). Pencere sürüklendikçe izler | **Sıfır boyutla** başlar |
| Web | Compose'un çizdiği kutu; tarayıcı penceresi değil. 375'ten 1700'e beş sınıf da denendi | **Sıfır boyutla** başlar |
:::

**Masaüstünde ve web'de ilk kompozisyon pencereyi 0 × 0 görür**, yani sınıf bir an `Compact`'tır; boyut hemen ardından gelir. Bu, Compose'un o iki platformdaki sırasıdır: masaüstünde içerik pencere gösterilmeden önce kurulur, web'de kutunun boyutu içeriğin ilk kompozisyonundan sonra okunur. O an ekranda bir şey yoktur, ama şuna dikkat:

*   Sınıfı `remember` ya da `rememberSaveable` ile saklama; her seferinde `PantrixTheme.window`'dan oku.
*   İlk kompozisyonda bir kez çalışan bir kararı (başlangıç sayfası gibi) sınıfa bağlama.

Bir kez verilip saklanan bir karar varsa, içeriği boyut gelene kadar bekletmek uygulamanın elindedir. `isSizeKnown` bunun içindir; Android ve iOS'ta baştan `true`'dur, yani orada hiçbir şey beklemez:

```kotlin
PantrixTheme {
    // Uygulama ilk kez, sınıfı gerçek olan bir pencerede kurulur.
    if (PantrixTheme.window.isSizeKnown) App()
}
```

Tema bunu kendiliğinden yapmaz: boyutunu hiç bildirmeyen bir ortamda ekran boş kalırdı.

## 6. Duruş nereden geliyor? {#where-does-the-posture-come-from}

*   **Android:** Jetpack WindowManager'ın `FoldingFeature`'ından. Uygulamaya yeni kütüphane girmez: Compose UI, Android'de WindowManager'a zaten bağlıdır. Window extensions 9 ve sonrası olan cihazda menteşe ilk kompozisyonda hazırdır; daha eskisinde ilk kompozisyon düzdür ve menteşe hemen ardından gelir.
*   **Web:** Tarayıcıdan, iki API ile. Menteşe görünümü böldüğünde tarayıcı görünümü parçalar hâlinde verir (Viewport Segments, `window.viewport.segments`): yan yana iki parça dikey, üst üste iki parça yatay menteşedir. Cihazın yarı açık tutulduğunu da söyler (Device Posture, `device-posture: folded`). İkisi de bugün Chromium'dadır; bunları bilmeyen tarayıcı ve katlanmayan cihaz düzdür. Cevap bir medya sorgusuyla izlenir: cihaz katlanınca duruş kendiliğinden değişir. Başka bir sayfanın içindeki bir çerçevede (`iframe`) çalışan uygulamaya tarayıcı parçaları vermez; orada da düzdür.
*   **iOS:** iOS 27.1'den itibaren UIKit'ten, iki kaynakla. Menteşenin yeri görünümün ayrılmış bölgelerindedir (`reservedRegions(kind: .division)`): içeriğin ikiye bölündüğü her bölge bir menteşedir. Bölgenin çerçevesi, Apple'ın dokunulacak şeylerin uzak durmasını istediği payı da içerir; menteşenin kendisi o payın içindeki çizgidir (`bounds`), pay ile birlikte şerit `clearance`'tır. Bölge etkinse menteşe pencereyi ayırır (`isSeparating`). Menteşenin ne kadar açık olduğunu `UIHingeInteraction` söyler: kısmen açıksa `isHalfOpened`. Bir şeyin örttüğü bölgeler (`.occlusion`) menteşenin üstüne düşüyorsa `isOccluding`'dir; kamera ve durum çubuğunun köşesi gibi başka yerdeki örtülü bölgeler menteşe değildir, güvenli alan onlardan zaten uzak tutar. Kütüphane bölgeleri içeriğin arkasındaki görünmez bir görünümde, UIKit her yerleştirdiğinde okur: telefon katlanınca, açılınca ve döndürülünce duruş kendiliğinden değişir. Okumak için kütüphanenin Xcode 27.1 SDK'sı ile derlenmesi yeter; uygulama daha eski iOS'larda da çalışır, orada duruş düzdür. Kotlin'in bugünkü sürümü bu API'leri tanımadığı için UIKit'e cinterop ile yazılmış küçük bir Objective-C köprüsünden ulaşılır: nasıl çalıştığı, neden Swift olmadığı ve eski iOS'larda ne olduğu IOS_FOLD_CINTEROP_GUIDE.md'dedir.
*   **Masaüstü:** Düz.

Menteşeyi kendi okuyan bir uygulama duruşunu verir:

```kotlin
PantrixTheme(window = rememberPantrixWindow(posture = myPosture)) { … }
```

:::table
| Duruş | Nasıl anlaşılır | `posture` |
| :--- | :--- | :--- |
| Düz açık | Menteşe var, ekran tek parça | `folds` dolu, `separatingFold` null |
| Kitap | Yarı açık, menteşe dikey | `isBook` |
| Masa | Yarı açık, menteşe yatay | `isTabletop` |
| Kapalı | Kapak ekranı | `WindowPosture.Flat`, pencere `Compact` |
:::

Menteşenin yeri `separatingFold.bounds`'tadır: piksel cinsinden, pencerenin koordinatlarında. Çevresinde dokunulanların boş bıraktığı şerit `clearance`'tır: iOS'ta menteşenin iki yanında 20 pt geniştir, Android ve web'de `bounds`'un kendisidir.

Xcode 27.1'in **iPhone Duo** simülatöründe ölçülenler: kapak ekranı 466 × 678 pt'dir ve menteşesi yoktur; sağ kenarında sistemin dikey bar'ı durur (10. bölüm, "Sistemin dikey bar'ı"). İç ekran 951 × 669 pt'dir; menteşe ortasındadır (x = 475,5) ve bölgesi iki yanındaki 20'şer pt'lik payla 40 pt geniştir. Telefon çevrilince iç ekran 669 × 951 olur ve menteşe yatay durur. Menteşenin açısı simülatörde Xcode'un cihaz penceresinden değiştirilir; `simctl`'de bir komutu yoktur.

## 7. Yeniden kurulma (recomposition) {#recomposition}

`PantrixTheme.window` tema durdukça aynı nesnedir; değerleri sorulduğunda okunur.

*   `widthClass` ya da `heightClass` okuyan, **yalnızca sınıf değişince** yeniden kurulur. Yardımcılar da böyledir (3. bölümdeki tablo).
*   `size` okuyan **her değişimde** kurulur: iOS döndürürken her karede, masaüstünde ve web'de pencere sürüklenirken her pikselde.

Bu yüzden düzenler sınıfı okur. Sınıflara uymayan bir genişlik soracaksan `window.size.width >= 1000.dp` yazma, `rememberWindowIsAtLeast(1000.dp)` kullan: yalnızca pencere o genişliği geçerken yeniden kurar. Boyutun kendisi gerekiyorsa onu küçük, kendi composable'ında oku; örnek uygulamadaki `WindowSamples` böyle yapar.

## 8. Pencereyi sabitlemek {#fixing-the-window}

Gerçek pencerenin doğru cevap olmadığı yerler:

```kotlin
// Test ve önizleme: düzeni belli bir boyutta görmek
PantrixTheme(window = PantrixWindow(DpSize(700.dp, 900.dp))) { … }

// Ekranın bir parçası: içeriği pencereye değil kendi paneline uysun
CompositionLocalProvider(LocalPantrixWindow provides PantrixWindow(DpSize(360.dp, 900.dp))) { … }
```

*   **Gömülü Compose:** yerel bir ekranın bir bölümüne yerleştirilen Compose görünümünde pencere, o bölümden geniştir. Bölümün boyutunu `PantrixWindow(...)` ile ver.
*   **İç içe tema:** kendisine pencere verilmeyen bir tema, dıştakinin penceresini kullanır. Testin sabitlediği pencere, içerideki koyu temada kaybolmaz. Yalnızca pencere değil: içteki tema, verilmeyen her ayarı dıştakinden alır (aşağıda).
*   **Tema yokken:** `PantrixTheme.window` yine cevap verir: Compose'un penceresi, varsayılan sınırlarla, düz.

**Testlerde pencere platforma göre değişir:** Skiko testlerinin (JVM, iOS, web) penceresi 1024 × 768'dir, yani `Expanded`. Android host testlerinin penceresi Robolectric'in `qualifiers`'ıdır. Sınıfa bağlı bir davranışı sınayan test penceresini yukarıdaki gibi sabitlemeli; yoksa aynı test platforma göre başka sonuç verir.

### İç içe tema: ekranın bir bölümü farklıysa

Açık bir ekranın koyu bir paneli ya da ferah bir sayfadaki sıkışık bir tablo, temanın içinde ikinci bir temadır. İçteki tema, **kendisine verilmeyen her ayarı dıştakinden alır**; verdiği onun olur:

```kotlin
PantrixTheme(
    locale = PantrixLocale.Turkish,
    analytics = myAnalytics,
    platform = PantrixPlatform.forType(PlatformType.Desktop, isTouch = true)
) {
    Page()
    PantrixTheme(darkTheme = true) {        // koyu panel: yalnızca renkler değişir
        PantrixSurface {                    // koyu zemini tema değil, içindeki surface çizer
            PantrixButton(text = "Kaydet", onClick = save)
            // dokunma hedefi 48 dp, "Yükleniyor" Türkçe, tıklama myAnalytics'e gider
        }
    }
    PantrixTheme(density = PantrixDensity.Compact) {   // sıkışık tablo: yalnızca yoğunluk değişir
        OrdersTable()
    }
}
```

Örnek uygulamada bunun sayfası var: **Theme: Inside Another Theme** (web'de `#theme-inside-another`). Koyu bölümü, sıkışık bölümü ve başka dilde bölümü gösterir; her birinde temanın değerleri yazar ve sayfanınkinden farklı olan satır "(değişti)" diye işaretlenir.

*   **Tema zemin çizmez.** İçteki tema yalnızca değerleri değiştirir; koyu zemini içine konan `PantrixSurface` ya da kart çizer. Zemin olmadan koyu temanın açık renkli metni açık sayfanın üstünde kalır.
*   **Tema düzen de değildir.** İçteki temanın içeriğini, temanın içinde durduğu düzen (`Column`, `Row`) yerleştirir; içteki tema araya kutu koymaz. Test etiketlerini otomasyona açan kutu en dıştaki temanındır.
*   **Devralınanlar:** platform, yoğunluk, boşluk ve düzen ölçüleri, şekiller, tipografi, hareket, yükseklik (elevation), dil ve metinler, stiller, bileşen override'ları, test etiketi politikası, analytics, haptics ve pencere.
*   **Renkler:** içteki tema dıştaki kadar koyudur ve onun renklerini korur (bir markanınkileri de). `darkTheme` ile tersi söylenirse o türdeki seti alır: dıştaki görünüm bir preset ile kurulduysa preset'in koyu (ya da açık) setini, kurulmadıysa Pantrix'in kendi setini. Preset'siz bir markanın koyu seti `colors` ile verilir.
*   **Yoğunlukla ölçeklenenler** (boşluk, düzen ölçüleri, dokunma hedefi) içteki temanın kendi yoğunluğuyla bir kez ölçeklenir; dıştakinin üstüne ikinci kez değil.
*   **Dil değişirse** metinler o dilinkilerdir; dıştaki temaya elle verilmiş metinler yalnızca dil aynıyken korunur.
*   **Preset'li tema** (`PantrixTheme(preset = …)`) preset'in söylediği dört şeyi (renk, tipografi, şekil, bileşen override'ları) preset'ten, gerisini dıştakinden alır. O da dıştaki tema kadar koyudur: koyu bir temanın içinde, sistem açık olsa bile preset'in koyu renkleriyle çizilir. Kökte, yani hiçbir temanın içinde değilken sistemi izler.
*   **Arada `ProvidePantrixStyles` varsa** içteki tema o noktada geçerli olan stilleri alır: dıştaki temanınkileri ve aradaki her sağlayıcınınkileri.

## 9. Kendi sınırların {#your-own-breakpoints}

```kotlin
PantrixTheme(window = rememberPantrixWindow(breakpoints = PantrixBreakpoints(medium = 640.dp))) { … }
```

Genişlikler ve yükseklikler küçükten büyüğe sıralı olmalıdır; değilse `PantrixBreakpoints` hata fırlatır.

Pencere, sınıflandığı sınırları söyler: `PantrixTheme.window.breakpoints`. Bileşenler de onlarla sınıflanır: `BoxWithPantrixWindow` sınırları etkin pencereden alır. Bir boyutu elle sınıflayan kod aynısını yapar:

```kotlin
val pane = PantrixWindow(paneSize, breakpoints = PantrixTheme.window.breakpoints)
```

## 10. Panel olan bileşen pencereye sormaz {#a-component-that-is-a-pane-does-not-ask-the-window}

Pencere, sayfa düzeyindeki kararlar içindir. Kendisi bir panel olan bileşen (scaffold, iki panel, ileride sheet'ler) pencereye sormaz, kendisine verilen yeri ölçer: dar bir panelin içindeki bileşen için pencere hâlâ geniştir. `PantrixScaffold` böyledir: navigasyonu kendi genişliği 600 dp'ye varınca yana alır.

Bir bileşen kendisine verilen yeri `BoxWithPantrixWindow` ile öğrenir. Kutu `BoxWithConstraints`'in üstüne kuruludur ve alabileceği en fazla yeri bir `PantrixWindow` olarak verir: ekranın pencereye sorduğu her şey (`isNarrow`, `byWidth`, `widthClass >= …`) kutuya sorulur.

```kotlin
@Composable
fun OrdersTable(orders: List<Order>, modifier: Modifier = Modifier) {
    BoxWithPantrixWindow(modifier) { box ->
        if (box.isNarrow) OrderList(orders) else OrderGrid(orders, showDate = box.widthClass >= WindowWidthClass.Expanded)
    }
}
```

*   **Sınırlar pencereninkilerdir.** Kutu, etkin pencere hangi sınırlarla sınıflanıyorsa onlarla sınıflanır; bileşenin kendi eşikleri varsa `breakpoints` parametresiyle verilir.
*   **İçerik her boyutta yeniden kurulmaz.** `BoxWithConstraints`'in içeriği kutu her boyut değiştirdiğinde yeniden kurulur. Buradaki içerik, okuduğu sınıf değişince kurulur; `box.size` okuyan her değişimi izler. Kutu, yeri değişirken aynı nesnedir.
*   **Kaydırılan sayfada yükseklik sınırsızdır.** Kutunun yükseklik sınıfı `Expanded` okunur, `isShort` hiç `true` olmaz: genişliği kutuya, yüksekliği pencereye sor.
*   **Kutu düzdür.** Menteşe pencerenindir: duruşu pencereye sor. Kutunun neresinden geçtiğini `rememberFoldsInNode()` söyler (aşağıda).
*   **`PantrixTheme.window` ve `responsive` kutunun içinde de pencereye sorar.** İçindeki her şey kutuya sorsun isteniyorsa (içeriği kendi başına bir ekran olan bir panel) kutu elden verilir: `CompositionLocalProvider(LocalPantrixWindow provides box) { … }`.
*   **İç ölçüsü sorulamaz.** `BoxWithConstraints` gibi içeriğini ölçülürken kurar; `IntrinsicSize` ile boyutlanan bir satırın ya da sütunun içine konmaz.

Duruş bunun dışındadır, çünkü pencerenin özelliğidir. **Masa duruşunda** (yarı açık, menteşe yatay) scaffold navigasyonu genişlik ne olursa olsun altta tutar: alt yarı ellerin olduğu yerdir ve yandaki bir sütun menteşenin üstünden geçerdi.

*   Kural, menteşe scaffold'un içinden geçiyorsa geçerlidir. Pencerenin iki yarısından da uzun olan bir scaffold bir yarıya sığamaz, yani menteşe içinden geçer; bir yarıya sığan scaffold oradaki bir panel sayılır ve genişliğine uyar.
*   Uygulamanın kendi koyduğu `sideBar` yerinde kalır; yalnızca yer değiştiren `navigation` taşınır.
*   Navigasyon ve içerik taşınırken tek bir kompozisyon olarak kalır: hatırladıkları yerindedir.

### İki panel ve menteşe

Kaç panel gösterileceği sayfa düzeyinde bir karardır ve pencereye sorulur. Panellerin nerede ayrılacağı ise panellerin durduğu yere bağlıdır ve oraya sorulur.

```kotlin
val panes = PantrixTheme.window.paneCount()      // Compact ve Medium 1, Expanded 2, Large'dan itibaren 3 (maxPanes ile sınırlı)

if (panes >= 2) {
    PantrixTwoPane(
        first = { Inbox(onOpen = { opened = it }) },
        second = { Message(opened) },
        modifier = Modifier.fillMaxSize(),
        firstSize = PaneSize.Fixed(360.dp)       // ya da PaneSize.Fraction(0.4f); varsayılanı yarı yarıya
    )
} else {
    if (opened == null) Inbox(onOpen = { opened = it }) else Message(opened)
}
```

*   **`paneCount(maxPanes = 2)`:** genişliğe göre 1, 2 ya da 3; kitap duruşunda (ya da iki ekranı ayıran dikey menteşede) her genişlikte en az 2. Masa duruşunda menteşe yataydır ve yan yana panelleri ayırmaz: sayı genişliğe göredir.
*   **`PantrixTwoPane` hep iki paneli çizer.** Yan yana (`TwoPaneOrientation.SideBySide`) ya da alt alta (`Stacked`). Birincinin payı `firstSize`, aradaki boşluk `gap`'tir; varsayılanı `PantrixTheme.layout.gutter`.
*   **Menteşede buluşur.** Panellerin durduğu yönde yeri ayıran bir menteşe (yan yana paneller için dikey, alt alta paneller için yatay) yerin içinden geçiyorsa paneller tam orada ayrılır ve aradaki boşluk menteşenin `clearance`'ıdır. Pay da boşluk da o zaman geçmez.
*   **Okuma yönü.** Sağdan sola bir dilde yan yana panellerin birincisi sağdadır. Menteşe ekranda olduğu yerde kalır.
*   **Tutamaç.** `onFirstSizeChange` verilince boşlukta bir tutamaç durur: parmak, işaretçi, ok tuşları ve ekran okuyucu birinci paneli onunla büyütür ve küçültür, boyut `firstSize`'ın türünde bildirilir ve uygulama onu geri verir. Çubuğu `handleColors`'la (`PaneHandleColors`) kendi renklerini alır. Boşluktan geniş şeridin bölmelerin kenarına taşmasını aşağıdaki liste-ayrıntı tutamacı anlatır; ikisi aynı parçadır. Yeri ayıran bir menteşe varsa tutamaç yoktur.
*   **Yer.** Bölünme yönünde yer sınırlı olmalıdır (kaydırılan bir sütunun içinde alt alta iki panel olmaz). Öteki yönde sınırsızsa çift uzun olan panel kadardır.
*   **Durum.** Panel sayısı değişince bir panelin içeriği başka bir yerden çağrılır ve yeniden kurulur. Kalması gereken (kaydırma yeri, seçim) panelin üstünde hatırlanır ve ona verilir: `val scroll = rememberScrollState()`.

Kendi düzenini menteşeye göre yazan, menteşeyi düğümünün kendi koordinatlarında okur:

```kotlin
val folds = rememberFoldsInNode()
Layout(content, modifier.then(folds.modifier)) { measurables, constraints ->
    val fold = folds.separatingFold          // bu düğümü kesen ve ayıran menteşe, yoksa null
    // fold.bounds ve fold.clearance pikseldir ve düğümün sol üst köşesinden ölçülür
    …
}
```

`folds` düğüm yer değiştirdikçe (sayfa kaydırılırken de), pencere değiştikçe ve cihaz katlandıkça yeniden okunur ve ölçen düzen yeniden ölçülür. Düğümü kesmeyen menteşe listede yoktur; düğümün kenarından geçen bir çizgi onu kesmez.

**Önizleme ve testte katlanan bir pencere:** `PantrixWindowFrame(window, modifier) { … }` bir kutuyu kendi başına pencere yapar. İçindekiler `PantrixTheme.window` ile verilen pencereyi okur; menteşesi kutunun sol üst köşesinden ölçülür, kutu ekranda nerede olursa olsun. Kutu ölçeklenmiş bir katmanda küçük çizilse de menteşe içerikle birlikte ölçeklenir. `window = null` kutunun içinde durduğu pencereyi geçirir: gerçek pencereyi uydurulmuş pencerelerle birlikte sunan bir seçici, içeriği her seçimde aynı yerden çağırır ve durumu kaybolmaz. Örnek uygulamanın kartlarındaki "Kitap" (951 × 669, dikey menteşe) ve "Masa" (669 × 951, yatay menteşe) pencereleri böyle çizilir; iPhone Duo'nun iç ekranının ölçüleridir.

```kotlin
val book = with(LocalDensity.current) {
    val x = 475.5.dp.toPx(); val h = 669.dp.toPx(); val pad = 20.dp.toPx()
    PantrixWindow(
        DpSize(951.dp, 669.dp),
        posture = WindowPosture(listOf(WindowFold(
            bounds = Rect(x, 0f, x, h), orientation = FoldOrientation.Vertical,
            isHalfOpened = true, isSeparating = true, isOccluding = false,
            clearance = Rect(x - pad, 0f, x + pad, h)
        )))
    )
}
PantrixWindowFrame(book, Modifier.size(951.dp, 669.dp)) { InboxScreen() }
```

### Liste ve ayrıntı: `PantrixListDetailScaffold`

Bir liste ve listede açılan öğenin ayrıntısı en sık görülen uyarlanan düzendir: gelen kutusu, ayarlar, kişiler, dosyalar. Yukarıdaki `if (panes >= 2) … else …` kodunu her ekran yeniden yazmasın diye `PantrixListDetailScaffold` panel sayısını, tek paneldeki geçişi ve geri hareketini, odağı, menteşeyi ve listenin genişliğini kendisi yönetir.

```kotlin
val inbox = rememberListDetailState()                    // açık öğe, ekstra panel, listenin genişliği; kaydedilir

PantrixListDetailScaffold(
    state = inbox,
    list = { Messages(onOpen = { id -> inbox.select(id) }) },
    detail = { id ->
        // Tek panelde ayrıntı listenin yerindedir: geri düğmesi yalnızca o zaman durur.
        Message(id, onBack = if (isSinglePane) ({ inbox.back() }) else null)
    },
    modifier = Modifier.fillMaxSize(),
    emptyDetail = { PantrixText("Bir ileti seç") },           // iki panelde, açık öğe yokken
    listTitle = "Gelen kutusu",                               // ekran okuyucuya panellerin adı
    detailTitle = { id -> subjectOf(id) },
    extra = { id -> History(id, showHeader = !isExtraInSheet) },  // isteğe bağlı üçüncü panel
    extraTitle = "Geçmiş",
    resizable = true                                          // listenin genişliği sürüklenir
)
```

*   **Panel sayısı kendi yerine göre.** Scaffold pencereye değil kendi kutusuna sorar (yukarıdaki kural): `Compact` ve `Medium` bir panel, `Expanded`'dan itibaren iki, `Large`'dan itibaren `extra` verilmişse üç. Kutuyu dikey kesen ve ayıran bir menteşe her genişlikte iki panel yapar.
*   **Tek panel.** Açık öğe yokken liste görünür. `select(key)` ayrıntıyı listenin yerinde açar, sondan kayarak gelir. Geri tuşu, masaüstünde ve tarayıcıda Escape, iOS'ta kenardan kaydırma `back()`'i çağırır. Bunlar `androidx.navigationevent`'ten gelir: Compose her platformda geri hareketini oradan verir, Android'in öngörülü geri hareketi ve iOS'un kenar kaydırması ilerleyişiyle birlikte. Ayrıntı parmağı izleyerek kayar, yarıda bırakılırsa yerine döner.
*   **Birkaç scaffold, bir geri.** Bir ekranda birden çok scaffold tek panelde ayrıntı gösteriyorsa (bir katalog sayfası, bir ekranın birkaç kartı) geri en son açılan ayrıntıyı kapatır: scaffold'un geri dinleyicisi her açılan öğede yeniden kaydedilir ve geri olayını en son kaydedilen dinleyici alır. Ekrandan kaydırılmış bir scaffold'un açık ayrıntısı da sırası gelince geriyi alır.
*   **İki panelde geri uygulamanındır.** Scaffold'un geri dinleyicisi yalnızca tek panelde ve bir öğe açıkken çalışır; öteki durumlarda geri, sayfanın kendi gezinmesine (Navigation 3'ün geri yığınına) gider.
*   **Odak gösterilen panelle gider.** Klavyeyle listeden açılan bir öğede odak ayrıntıya geçer; geri dönülünce açılan öğeye döner (`focusRestorer`). Dokunmayla açılınca odağa dokunulmaz: yazı alanına geçen odak klavyeyi açardı.
*   **Gösterilmeyen panel yerleştirilmez.** Tek panelde gizli olan liste kompozisyonda kalır ama yerleştirilmez: klavye de ekran okuyucu da onu atlar, kaydırma yeri ve hatırladığı her şey yerindedir. Bir panelden ikiye geçince liste ve ayrıntı aynı yerden çağrılır, durumları kaybolmaz.
*   **Anahtarlar.** Öğeler kaydedilebilen, her dilde aynı `String` anahtarlarla anılır. Ayrıntı her anahtar için yeniden kurulur: ayrıntının düzenlediğini (bir ayarın değeri, bir taslak) uygulamanın durumunda tut, ayrıntının içinde değil.
*   **Ekstra panel.** `showExtra()` onu üç panele yer varsa ayrıntının yanında, sonda (`extraWidth`) açar. Yoksa scaffold'un kendi yerinde çizilen bir yan sheet'te ayrıntının üstüne sondan kayarak gelir: başlığı `extraTitle`, yanında bir kapatma düğmesi, altında yalnızca scaffold'u örten bir scrim. Scrim'e basmak, geri tuşu ve Escape onu kapatır (geri önce sheet'i, sonra ayrıntıyı kapatır). Klavyenin odağı sheet'e girer, o açıkken arkadaki panellere geçmez ve ekran okuyucu onları okumaz; kapanınca odak ayrıntıda geldiği yere döner. Sheet ayrı bir pencere değildir: ekstra panel iki yerde de aynı kompozisyondur ve yer değişince hatırladığı (`remember` da) kalır. Genişliği `extraWidth`'tir, scrim'den başta bir dokunma hedefi kadar yer kalacak kadar daralır; görünüşü (köşeler, gölge, renkler, scrim) `SheetStyle` ve sheet'in renkleridir. `isExtraInSheet` sheet'te kendi başlığını çizmemeyi seçtirir; `back()` ekstra paneli de kapatır.
*   **Her panel bir pencere.** Scaffold her panelin içeriğine panelin yerini pencere olarak verir (yukarıdaki "kutuyu elden ver" kuralı): paneldeki `PantrixTheme.window`, `isNarrow`, `byWidth`, `responsive` ve `PantrixTheme.layout` panelin genişliğine göre cevap verir. 900 dp'lik bir yerde 360 dp'lik liste de 524 dp'lik ayrıntı da `Compact`'tır. Duruş pencerenin kalır: paneldeki bir `PantrixTwoPane` kat yerini yine bulur.
*   **Ekran okuyucu.** Her panel tek bir grup olarak okunur. `listTitle`, `detailTitle` ve `extraTitle` panellere ad verir (`paneTitle`); Android'de TalkBack görünür olan panelin adını söyler. Compose iOS'ta bir panelin adını VoiceOver'a bildirmiyor.
*   **Tutamaç.** `resizable = true` panellerin arasına bir tutamaç koyar: parmakla ve işaretçiyle sürüklenir, odaktayken ok tuşları 16 dp'lik adımlarla, Home ve End en dar ve en geniş hâline götürür; ekran okuyucu onu "Liste genişliği" ve yerin yüzdesi olarak okur ve değerini değiştirebilir. Liste `minListWidth` ile ayrıntıya `minDetailWidth` bırakan genişlik arasında kalır; genişlik `ListDetailState.listWidth`'tedir (`Dp.Unspecified` stilinkine döndürür). Menteşe panelleri kendisi yerleştirdiği için orada tutamaç yoktur.
*   **Tutamacın şeridi ve renkleri.** Çubuk `handleColors`'la (`PaneHandleColors`) kendi renklerini alır: dururken (varsayılanı `textSecondary`'nin %40'ı), işaretçinin altında ve sürüklenirken (`textSecondary`), odakta (`focusRing`); verilmeyen renk temanınkidir. Parmağın tuttuğu şerit boşluktan genişse (telefonda 48/44 dp, varsayılan sütun arası 16 dp) bölmelerin kenarına taşar. Oradaki bir dokunuş alttaki bölmeye de gider: kenardaki bir düğmeye dokunulur, tutamacı yalnızca bölünmenin yönünde giden bir sürükleme taşır, aynı yönde kayan bir bölmenin üstünde bile. Fare şeritteyken yalnızca tutamacındır; alttaki bölme kendi imlecini tutamacın üstünde bırakmasın diye.
*   **Menteşede buluşur.** Liste menteşenin baş tarafında, ayrıntı öbür tarafındadır; aradaki boşluk menteşenin `clearance`'ıdır. Şeridin kısmen kapladığı piksel de boş kalır, ortadaki bir menteşe iki yanı eşit bırakır (`PantrixTwoPane` da böyle böler).
*   **Okuma yönü ve hareket.** Sağdan sola bir dilde liste sağdadır, ayrıntı soldan kayar. Sistem daha az hareket istiyorsa ayrıntı kaymadan açılır ve kapanır. Kayarken ayrıntının altına temanın arka zemini boyanır, listeyi örtsün diye.
*   **Yer.** Scaffold kendisine verilen yeri alır ve yer iki yönde de sınırlı olmalıdır: bir ekranda `Modifier.fillMaxSize()`.
*   **Stil.** `ListDetailScaffoldStyle`: `listWidth` 360 dp, `minListWidth` 240 dp, `minDetailWidth` 320 dp, `extraWidth` 360 dp, `gap` (verilmezse düzenin sütun arası), tutamacın çubuğu ve yakalandığı şerit (parmakla 48/44, işaretçiyle 16 dp). Marka kendi değerlerini `listDetailScaffold.*` anahtarlarıyla verir.
*   **Otomasyon.** `ListDetailScaffold_<tagKey>`, panelleri `ListDetailList_…`, `ListDetailDetail_…`, `ListDetailExtra_…`, tutamaç `ListDetailHandle_…`, yan sheet'in scrim'i `ListDetailScrim_…`, kapatma düğmesi `ListDetailClose_…`.

Örnek uygulamanın "Liste ve ayrıntı" sayfasında beş vaka kartlarıyla durur: gelen kutusu, ayarlar (iki panelde ilk bölüm açık), ekstra panelli kişiler, genişliği sürüklenen dosyalar, katlanan cihazda kat yerinde buluşma.

### Ana içerik ve destek paneli: `PantrixSupportingPaneScaffold`

Bir ekranın asıl içeriği ve onu destekleyen bir panel: belge ve yorumları, grafik ve filtreleri, harita ve sonuçları. Liste-ayrıntıdan farkı, ana içeriğin hep orada olmasıdır: destek paneli ya yanında durur ya da üstünde açılır.

```kotlin
val comments = rememberSupportingPaneState()             // yanda gösterilip gösterilmediği, üstte açık olup olmadığı, genişliği

PantrixSupportingPaneScaffold(
    state = comments,
    main = { Document(onComments = { if (isSupportingShown) comments.hide() else comments.show() }) },
    supporting = {
        // Yanda panel kendi başlığını ve gizle düğmesini çizer; sheet'te başlık ve kapatma düğmesi iskelenindir.
        Comments(onHide = if (supportingPlace == SupportingPanePlace.Beside) ({ comments.hide() }) else null)
    },
    modifier = Modifier.fillMaxSize(),
    mainTitle = "Rapor",                                     // ekran okuyucuya panellerin adı
    supportingTitle = "Yorumlar",                            // sheet'in başlığı da
    presentation = SupportingPanePresentation.Sheet,         // dar yerde sheet (varsayılan) ya da Pane: ana içeriğin yerinde
    resizable = true                                         // destek panelinin genişliği sürüklenir
)
```

*   **Yanda.** Scaffold kendi kutusuna sorar: `Expanded`'dan itibaren ve ana içerik `minMainWidth`'in altına inmeyecekse destek paneli sonda, `supportingWidth` (360 dp) genişliğinde durur; ana içerik kalanı alır, geri uygulamanındır. Destek paneli baştan açıktır, `hide()` kaldırır ve ana içerik bütün yeri alır.
*   **Dar yerde sheet.** Yanda yer yoksa destek paneli `show()`'a kadar kapalıdır. `Sheet` sunumunda scaffold'un kendi yerinde, ana içeriğin üstünde bir sheet'te açılır: telefonda (`Compact`) alttan gelir, üstünde `SheetStyle.margin` kadar scrim kalır ve tutamacından aşağı sürüklenince kapanır; daha geniş bir yerde (`Medium`) sondan gelir, `supportingWidth` genişliğinde. İkisinde de başlık `supportingTitle`, yanında bir kapatma düğmesi; scrim'e basmak, geri tuşu, Escape ve iOS'un kenar kaydırması kapatır. Klavyenin odağı sheet'e başlığından girer, o açıkken içinde kalır, kapanınca ana içerikte geldiği yere döner.
*   **Dar yerde ana içeriğin yerinde.** `Pane` sunumunda destek paneli ana içeriğin yerine sondan kayarak gelir, liste-ayrıntının tek paneli gibi: geri hareketi ilerledikçe parmakla gider, yarıda bırakılırsa yerine döner. Destek panelinin barındaki geri düğmesi `hide()`'ı çağırır; düğme yalnızca `supportingPlace == InPlaceOfMain` iken durur.
*   **İki yarı, ayrı tutulur.** `isShownBeside` geniş bir yerde, `isOpenOver` dar bir yerde ne gösterildiğidir; `isShown`, `show()` ve `hide()` scaffold'un o anki yerine bakar. Pencere daralınca sheet kendiliğinden açılmaz, ana içeriği örtmez. Üstte açık olan destek paneli pencere genişleyince yanda kalır. Scaffold yerini ölçmeden çağrılan `show()` (bir ekranın başında) paneli yer onu nereye koyarsa orada gösterir.
*   **Panellere söylenen.** `SupportingPaneScope`: `supportingPlace` (`Beside`, `Sheet`, `InPlaceOfMain`) ve `isSupportingShown`. Bir göster/gizle düğmesinin yazısı `isSupportingShown`'dan okunur.
*   **Kat yeri.** Kitap gibi tutulan bir katlanabilir her genişlikte iki panel yapar, kat yerinde buluşurlar. Masaya konmuş olanda ana içerik kat yerinin üstünde, destek paneli altındadır (denetimler aşağıda). Kat yeri panelleri kendisi yerleştirir; tutamaç yoktur.
*   **Tutamaç.** Liste-ayrıntınınkiyle aynı tutamaç: sürüklenir, ok tuşlarıyla (ana içeriği gösteren ok destek panelini genişletir), Home ve End ile ayarlanır, ekran okuyucu onu "Destek paneli genişliği" olarak okur, üstünde boyutlandırma imleci durur. Genişlik `SupportingPaneState.supportingWidth`'tedir.
*   **Durum korunur.** Destek paneli gizliyken de, yanda ile sheet arasında geçerken de aynı kompozisyondur: yazılan bir yorum, seçilen bir filtre kalır. Gösterilmeyen panel yerleştirilmez; klavye ve ekran okuyucu onu atlar.
*   **Her panel bir pencere, ekran okuyucu, okuma yönü, hareket.** Liste-ayrıntıdaki gibi: her panel kendi yerini pencere olarak okur; `mainTitle` ve `supportingTitle` panellerin adıdır; sağdan sola bir dilde destek paneli soldadır; "hareketi azalt" kayma ve açılmayı kaldırır.
*   **Stil.** `SupportingPaneScaffoldStyle`: `supportingWidth` 360 dp, `minSupportingWidth` 240 dp, `minMainWidth` 360 dp, `gap`, tutamacın çubuğu ve şeridi. Marka `supportingPaneScaffold.*` anahtarlarıyla verir. Sheet'in görünüşü `SheetStyle`'dır.
*   **Otomasyon.** `SupportingPaneScaffold_<tagKey>`, panelleri `SupportingPaneMain_…` ve `SupportingPaneSupporting_…`, tutamaç `SupportingPaneHandle_…`, sheet'in scrim'i `SupportingPaneScrim_…`, kapatma düğmesi `SupportingPaneClose_…`.

Örnek uygulamanın "Destek paneli" sayfasında beş vaka durur: belge ve yorumlar, genişliği sürüklenen grafik ve filtreler, dar yerde haritanın yerinde açılan sonuçlar, yanda gizleyip göstermek, katlanan cihazda kat yerinde ayrılma.

### Masa duruşu: `PantrixTabletopLayout`

Bakılan bir içerik ve dokunulan denetimler: video ve oynatıcının düğmeleri, görüntülü görüşme ve düğmeleri, kameranın görüntüsü ve deklanşörü. Katlanabilir masaya yarı açık konunca (menteşe yatay) bakılan yukarıda, eller aşağıda olmalıdır.

```kotlin
PantrixTabletopLayout(
    content = { Video(player) },
    controls = {
        // Masada alt yarının tamamı denetimlerindir: büyük çizilebilirler.
        PlayerControls(player, large = arrangement == TabletopArrangement.Tabletop)
    },
    modifier = Modifier.fillMaxSize(),
    flatControls = TabletopControls.Below                    // düzken: Below (varsayılan) ya da Overlay
)
```

*   **Masada.** İçerik kat yerinin üstündeki yarıyı, denetimler alttaki yarıyı doldurur; kat yerinin şeridi (`clearance`) boş kalır. Denetimler alt yarının tamamını alır, kendilerini nasıl dizeceklerine kendileri karar verir.
*   **Kitap gibi.** İçerik kat yerinin başında, denetimler öbür yanında; sağdan sola bir dilde içerik sağdadır.
*   **Düzken.** `Below`: denetimler kendi yüksekliklerinde altta, içerik kalan yeri alır. `Overlay`: içerik bütün yeri alır, denetimler onun altına, üstüne biner (bir video oynatıcısındaki gibi). Düzken denetimlere düzenin bütün yeri pencere olarak söylenir: ne kadar uzun olacaklarını kendileri seçer.
*   **Kendi yerine bakar.** Kat yerini pencerede değil, düzenin yerini kestiği yerde arar (`rememberFoldsInNode`): tamamen bir yarıda kalan düzen düz yerleşir. İki ekranı ayıran bir menteşe (Surface Duo gibi) düzken de masa ya da kitap düzeni yapar: ayıran her menteşe sayılır.
*   **Kapsam.** `TabletopScope.arrangement`: `Flat`, `Tabletop`, `Book`. Denetimler masada büyür, videonun üstündeki denetimler kendi zeminini çizer.
*   **Durum korunur.** Duruş değişirken iki parça kompozisyonda yerinde kalır, bir anda ve hareketsiz: video kaldığı yerden sürer.
*   **Her parça bir pencere, ekran okuyucu.** Her parça kendi yerini pencere olarak okur; ikisi birer grup olarak okunur, önce içerik. Parçalara ad verilmez: tek bir ekranın iki parçasıdır.
*   **Navigasyon.** `PantrixScaffold` masa duruşunda navigasyonu zaten alta alır; düzen iskelenin içeriğine konur.
*   **Otomasyon.** `TabletopLayout_<tagKey>`, parçaları `TabletopContent_…` ve `TabletopControls_…`.

Örnek uygulamanın "Masa duruşu" sayfasında dört vaka durur: video oynatıcı, denetimleri videonun üstünde olan oynatıcı, görüntülü görüşme, ölçülerini yazan parçalar.

### Menteşede katmanlar

Bir katlanabilir yarı açık tutulduğunda ya da iki ekranın arasında bir menteşe olduğunda, kat yerinin üstüne düşen bir dialog ya da popover ikiye bölünür. Pantrix'in katmanları kat yerinin bir yarısında kalır; kural katmanların ortak kodundadır, bileşenlere bir parametre gerekmez:

:::table
| Katman | Kitap gibi (menteşe dik) | Masada (menteşe yatay) |
| :--- | :--- | :--- |
| Dialog | açıldığı yarının ortası; bilinmiyorsa başlangıç yarısı | açıldığı yarının ortası; bilinmiyorsa alt yarı |
| Alttan sheet | açıldığı yarı, o yarının genişliğinde | alt yarı; tam ekran sheet alt yarının tamamı |
| Yan sheet | tam son yarı: kat yerinin şeridinden pencerenin sonuna | alt yarı |
| Komut paleti | dialog gibi | üst yarı, yüksekliği o yarıyla sınırlı |
| Popover, menü, tooltip, select'in listesi | bağlandığı denetimin yarısı | bağlandığı denetimin yarısı |
| Snackbar | bir yarı: son köşedeki son yarı, ötekiler başlangıç yarısı | yerinde |
:::

*   **Nereden açıldı.** "Açıldığı yarı", son basılan yerin yarısıdır; ondan sonra bir tuşa basıldıysa odaktaki denetimin yarısı. Bunu tema kökü ve katmanların kökleri dinler (`pantrixTransientsRoot`); katman açılırken bir kez sorar ve kapanana kadar o yarıda kalır.
*   **Bağlı katmanlar.** Bağlandığı denetimin çoğunun durduğu yarıda kalır: o yarıda sığmazsa önce öbür yana geçer, sonra kayar, en son küçülür ve içi kayar. Kat yerini hiçbir zaman aşmaz.
*   **Sistem çubukları.** İçerik yalnızca eriştiği kenarların boşluğunu (inset) bırakır. Masada alt yarıdaki tam ekran sheet durum çubuğunun altına girmez; köşeleri yuvarlak kalır.
*   **Çerçevede açılan katmanlar.** `PantrixWindowFrame` içinde açılan bir katman çerçeveyi kendi penceresi sayar: scrim çerçeveyi örter, sheet çerçevenin kenarından gelir, kat yeri çerçevenin kat yeridir; çerçevenin dışına basmak scrim'e basmaktır. Küçültülmüş bir çerçevede katman da o kadar küçük çizilir. Katmanın içindekilere çerçevenin penceresi söylenir: liste-ayrıntı iskelesinin panelinden açılan bir dialog panelin değil pencerenin genişliğini okur.
*   **Açılırken karar verilir.** Katman yerini açılırken seçer; altında çerçeve kayar ya da küçülürse bir sonraki açılış yeni yere göre yerleşir. Pencerenin kendisi ya da duruş değişirse katman ona uyar.

Kendi `Dialog`'unu ya da `Popup`'ını gösteren bir uygulama aynı bilgiyi `rememberOverlayWindow()` ile alır:

```kotlin
val overlay = rememberOverlayWindow()
// Açılırken bir kez: nereden açıldığı, ve pencereyi ikiye bölen kat yeri.
val openedFrom = remember { overlay.openedFrom() }
val fold = overlay.folds.firstOrNull { it.isSeparating }
val onTheLeft = fold != null && openedFrom != null && openedFrom.x < fold.bounds.center.x
```

`OverlayWindow`'un yerleri uygulamanın penceresinin koordinatlarında ve pikseldir: pencereyi dolduran bir `Dialog` içindekileri oraya yerleştirir. `window` katmanın penceresidir (çerçevenin ya da en dıştaki temanın), `frame` çerçevenin pencerede çizildiği yer, `scale` çerçevenin çizildiği ölçek.

### Örten menteşe: `Modifier.keepClearOfFolds()`

İki ekranlı bir cihazda menteşe ekranın bir şeridini örter: altına çizilen görünmez. Menteşenin üstünden biraz taşan bir düğüm (bir ekrandan biraz geniş bir liste, bütün kenar boyunca bir çubuk) içindekileri çoğunun durduğu ekranda tutmalıdır:

```kotlin
Box(Modifier.fillMaxSize()) {
    Inbox()
    // Bütün alt kenar boyunca bir çubuk iki ekrandan birinde kalır.
    Toolbar(Modifier.align(Alignment.BottomCenter).fillMaxWidth().keepClearOfFolds())
}
```

*   Yalnızca ekranı gerçekten örten bir menteşe sayılır (`WindowFold.isOccluding`). Katlanan bir telefonun kat yeri tek bir ekrandır; üstüne çizilen görünür, ondan uzak durulmaz.
*   Düğümün daha az yeri olan yanı ve menteşenin şeridi boş kalır; düğüm kendi boyutunu korur. Menteşe düğümün tam ortasından geçiyorsa düğüm başlangıç yanında kalır: orada içeriği iki yana yaymak `PantrixTwoPane`'in işidir.
*   Düğüm son yerleştiği yere göre ölçülür: başka bir yere yerleşince yeniden ölçülür.

Örnek uygulamanın "Menteşede katmanlar" sayfasında bunların hepsi durur: iki köşeden açılan dialog, kat yerinin yanında popover ve menü, sheet'ler, komut paleti ve snackbar, iki ekranlı bir cihazda `keepClearOfFolds`. Örnekler "Kitap" ve "Masa" pencerelerinde, kartın içinde çerçeveyi pencere sayarak açılır.

### Sistemin dikey bar'ı: iPhone duo'nun dış ekranı

iOS 27.1, iPhone Duo'nun dış ekranında bar'ları dik koyar: ekranın bitiş kenarı boyunca bir sütun ayırır, tepesine durum çubuğunu dikey yerleştirir; UIKit bir sekme bar'ını ve bir gezinme bar'ının düğmelerini de o sütuna indirir. Uygulamanın içeriği güvenli alanla sütundan uzak durur. Simülatörde ölçülenler (466 × 678 pt): `traitCollection.verticalBarEdge` bitiş kenarını söyler, sütun sağ güvenli alan payı kadardır (84 pt), durum çubuğu sütunun üst 174 pt'sini kaplar, UIKit'in bir bar'a verdiği yer (`layoutRegionForBarOnEdge`) 174 ile 644 pt arasıdır. UIKit'in sekmeleri bu yerin dibinde, 48 pt'lik bir kapsülde durur; "…" gibi bir düğme durum çubuğunun hemen altında.

Pantrix bunu pencerenin bir parçası olarak verir ve kendi bileşenlerini oraya koyar:

*   **Pencere:** `PantrixTheme.window.verticalBar` bir `WindowVerticalBar`'dır: sütun (`bounds`) ve bar'ların yeri (`barBounds`), pikselle ve pencerenin koordinatlarında. Sistemin dikey bar'ı olmayan her yerde null'dır: Android, masaüstü, web, iç ekran, iOS 27.1'den önce ve 27.1'den eski bir Xcode ile derlenmiş kütüphane. iOS'ta menteşe gibi okunur: içeriğin arkasındaki görünmez bir görünüm, güvenli alan ya da kenarın bağlı olduğu özellikler değişince yeniden okur (köprü: IOS_FOLD_CINTEROP_GUIDE.md).
*   **Düğümde:** `rememberVerticalBarInNode()` sütunu bir düğümün kendi koordinatlarında verir; düğüm sütuna uzanmıyorsa null'dır. Pencere, düğümün çizildiği çerçevenin ya da en dıştaki temanınkidir: bir panelin kendi penceresi değil.
*   **`PantrixScaffold(navigation = …)`:** sütuna uzanan scaffold gezinmeyi, genişliği ne olursa olsun, sütunun dibine koyar ve ona `ScaffoldNavigationPlacement.VerticalBar` der; uygulama orada dik duran bir navigation bar çizer. Bunu yalnızca dik duran ve içinden menteşe geçmeyen bir pencerede yapar: Duo'nun dik tutulan dış ekranında. Sistem iç ekranda ve yana çevrilen iki ekranda da sütun ayırır (durum çubuğu orada da diktir); scaffold orada sütunu durum çubuğuna bırakır, gezinme genişliğin dediği yerde, düğmeler üst bar'da kalır. İçerik her durumda sütundan payıyla uzak durur, payı söylemeyen bir önizleme penceresinde de. İç içe iki scaffold'dan sütunu dıştaki tutar. Gezinme yer değiştirirken aynı kompozisyonda kalır.
*   **`PantrixTopBar`:** sütunu tutan bir scaffold'un içindeki üst bar, gezinme ikonunu ve eylemlerini sütunun tepesine verir, başlığını tutar. Sütunu tutmayan scaffold'da, iç ekranda ve yatayda düğmeler üst bar'dadır. Aynı anda iki bar varsa, bir sayfa ötekinin üstüne kayarken olduğu gibi, sütun en son geleninkini gösterir.
*   **`PantrixNavigationBar(orientation = NavigationBarOrientation.Vertical)`:** hedefler alt alta, bar'ın dolgusundan bir kapsülde. Varsayılanı yalnız ikonlardır, `showLabels = true` etiketleri de yazar. Etiketsiz bir hedefin adı ekran okuyucuya söylenir ve bir tooltip'te görünür. Rozeti uzanan bir hedef kapsülü uzandığı kadar genişletir; rozet içeride kalır.

```kotlin
PantrixScaffold(
    navigation = { placement, insets ->
        when (placement) {
            ScaffoldNavigationPlacement.Bottom -> PantrixNavigationBar(tabs, where, go, insets = insets)
            ScaffoldNavigationPlacement.VerticalBar ->
                PantrixNavigationBar(tabs, where, go, orientation = NavigationBarOrientation.Vertical)
            ScaffoldNavigationPlacement.Start -> PantrixNavigationRail(tabs, where, go, insets = insets)
        }
    }
) { padding -> Screen(padding) }
```

Bir önizleme ya da test, sütunu olan bir ekranı `PantrixWindow(size, verticalBar = WindowVerticalBar(...))` ile kurar. Örnek uygulamanın kartlarındaki "Kapak" penceresi böyle kurulmuştur (466 × 678, 84'lük sütun); uygulamanın kendi kabuğu da Duo'nun dış ekranında böyle durur.

## 11. Girdi: parmak mı, fare mi? {#input-a-finger-or-a-mouse}

Pencerenin boyutu, uygulamanın neyle kullanıldığını söylemez: 400 dp'lik bir pencere telefonun tarayıcısı da olabilir, daraltılmış bir masaüstü penceresi de. Bunu platform söyler:

```kotlin
val platform = PantrixTheme.platform

platform.isTouch          // birincil işaretçi parmak mı
platform.minTouchTarget   // dokunma hedefi; bileşenler bunu temadan kendileri alır
```

:::table
| Nerede | `isTouch` | Dokunma hedefi |
| :--- | :--- | :--- |
| Android | evet | 48 dp (Material) |
| iOS | evet | 44 dp (Apple HIG) |
| Masaüstü | hayır | 32 dp |
| Web, masadaki tarayıcı | hayır | 32 dp |
| Web, telefon ve tablet tarayıcısı | evet | 48 dp |
:::

Web iki tür cihazda da çalışır ve hiçbir kılavuz onu kapsamaz; o yüzden işaretçisine bakar. Parmağa Android'de verilen ölçü verilir.

*   **Sinyal tarayıcınındır.** Cevap `pointer: coarse` medya sorgusudur: tarayıcının **birincil** işaretçisi parmaksa doğru. Dokunmatik ekranlı bir dizüstünde birincil işaretçi trackpad'dir, hedef 32 dp kalır.
*   **Canlıdır.** Tablete trackpad takılınca ya da tarayıcının cihaz öykünmesi açılıp kapanınca tema yeni cevabı alır; sayfayı yenilemek gerekmez. Cevap ilk kompozisyonda okunur, yani önce yanlış ölçüyle çizilen bir kare olmaz.
*   **Dokunma hedefi iki şeydir, ikisi de değişir:** bileşenin yerleşimde ayırdığı yer ve parmağın, çizilen kısmın ne kadar çevresinde isabet sayıldığı.
*   **Görünüş değişmez.** Telefon tarayıcısında da web'in görünüşü çizilir; büyüyen, çevresindeki yerdir.
*   **Android ve iOS kendi ölçüsünü korur.** Fare takılı bir tablette de 48 dp'dir.
*   **Masaüstü kendisi söyler.** JVM ekranın dokunmatik olduğunu bildirmez; kiosk gibi dokunmatik bir masaüstü uygulaması platformu kendisi verir.

```kotlin
// Dokunmatik bir masaüstü uygulaması
PantrixTheme(platform = PantrixPlatform.forType(PlatformType.Desktop, isTouch = true)) { … }

// Testte ve önizlemede bir telefon tarayıcısı
PantrixTheme(platform = PantrixPlatform.forType(PlatformType.Web, isTouch = true)) { … }
```

"Dokunmatik mi" sorusu için platformun türüne bakma: telefonun tarayıcısı `PlatformType.Web`'dir ama parmakla kullanılır. `isTouch`'a sor:

```kotlin
if (PantrixTheme.platform.isTouch) PullToRefreshHint() else KeyboardShortcutsHint()
```

Web'de "kontrastı artır" ve "hareketi azalt" ayarları da aynı yolla okunur ve sayfa açıkken değişirlerse izlenir.

## 12. Düzen ölçüleri: kenar boşluğu, sütun arası, sütunlar ve içeriğin genişliği {#layout-measures-the-margin-the-gutter-the-columns-and-the-content-s-width}

Sayfanın dört ölçüsü pencerenin sınıfıyla değişir ve temadan okunur:

```kotlin
val layout = PantrixTheme.layout

Column(Modifier.padding(horizontal = layout.margin)) { … }             // sayfanın yanlardaki boşluğu
Row(horizontalArrangement = Arrangement.spacedBy(layout.gutter)) { … }  // sütunların ve panellerin arası
layout.columns                                                         // ızgaranın sütun sayısı
Column(Modifier.maxContentWidth()) { … }                               // içerik en fazla layout.maxContentWidth
```

:::table
| Sınıf | Genişlik (dp) | `margin` | `gutter` | `columns` | `maxContentWidth` |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `Compact` | < 600 | 16 | 16 | 4 | sınırsız |
| `Medium` | 600–839 | 24 | 16 | 8 | sınırsız |
| `Expanded` | 840–1199 | 24 | 24 | 12 | sınırsız |
| `Large` | 1200–1599 | 32 | 24 | 12 | 1040 |
| `ExtraLarge` | ≥ 1600 | 32 | 24 | 12 | 1040 |
:::

Telefonun verecek genişliği azdır, masaüstü penceresinin fazladır: kenar boşluğu ve sütun arası pencereyle büyür. İkisinin değerleri boşluk ölçeğinin adımlarıdır (`lg`, `xl`, `xxl`). Sütun sayısı Material'ın duyarlı ızgarasıdır; 1040, geniş ekranda içeriğin genişliğidir.

*   **`margin`** pencerenin ya da panelin kenarı ile içerik arasındaki boşluktur: sayfa içeriğini yanlardan onunla içeri alır.
*   **`gutter`** bir ızgaranın sütunları ve yan yana duran paneller arasındaki boşluktur.
*   **`columns`** sayfanın ızgarasının kaç sütun olduğudur. Bir parçanın kaç sütuna yayıldığını sayfa seçer; genişliğini `layout.spanWidth(available, span)` verir: parçanın sütunları ve aralarındaki sütun araları. `available` sütunların paylaştığı yerdir, sayfanın kenar boşluklarının içi:

    ```kotlin
    BoxWithConstraints {
        val layout = PantrixTheme.layout
        val (article, links) = PantrixTheme.window.byWidth(compact = 4 to 4, medium = 5 to 3, expanded = 8 to 4)
        Row(horizontalArrangement = Arrangement.spacedBy(layout.gutter)) {
            Article(Modifier.width(layout.spanWidth(maxWidth, article)))
            Links(Modifier.width(layout.spanWidth(maxWidth, links)))
        }
    }
    ```

    Yan yana iki parça ve aralarındaki sütun arası satırı tam doldurur. Kütüphanede bir ızgara bileşeni yoktur; sayfa onu `Row` ve `spanWidth` ile kurar.
*   **`maxContentWidth`** içeriğin en fazla genişliğidir: masaüstü penceresi boyunca uzanan bir satır okunmaz. `Modifier.maxContentWidth(alignment)` içeriği o genişlikte tutar ve yerin geri kalanını yanında boş bırakır; içerik başa (varsayılan) ya da ortaya hizalanır. Sınırın olmadığı sınıfta içerik verilen bütün yeri alır. `PantrixPageScaffold` sayfanın bütün genişliğini varsayılan olarak kullanır; `width = PageWidth.Content` okunan bir sayfayı bu sınırda tutar. Örnek uygulamanın sayfaları geniş pencerede bütün yeri alır, paragrafları bu sınırda kalır.
*   **Yeniden kurulma:** okuyan, pencerenin sınıfı değişince yeniden kurulur; pencere sınıfın içinde boyut değiştirirken kurulmaz.
*   **Yoğunluk:** kenar boşluğu ve sütun arası boşluktur, temanın yoğunluğu (`Compact`, `Spacious`) onları boşluk ölçeğiyle birlikte ölçekler. Sütun sayısı bir sayı, en fazla genişlik içeriğin ölçüsüdür: yoğunluk ikisini de değiştirmez.
*   **Bileşenin kendi kutusu:** yerine göre davranan bileşen, kutusunun ölçülerini sorar: `PantrixTheme.layout(box)`. Dar bir paneldeki bileşen, geniş pencerede de telefonun kenar boşluğunu alır. Kutu elden verildiyse (`LocalPantrixWindow provides box`) `PantrixTheme.layout` zaten kutuya göredir.
*   **Kütüphane bunları kendiliğinden uygulamaz:** hiçbir bileşen içeriğine kenar boşluğu eklemez. Sayfa kendi içeriğini kendisi içeri alır; örnek uygulamanın sayfa çerçevesi (`DemoPage`) böyle yapar.

Ürün, tasarladığı sınıfların ölçülerini temaya verir; vermedikleri olduğu gibi kalır:

```kotlin
PantrixTheme(layouts = PantrixLayouts(compact = PantrixLayout(margin = 20.dp, gutter = 12.dp))) { … }
```

Kendi ölçüleri verilen bir sınıfın sütun sayısı ve en fazla genişliği de verilendir; verilmezse bir telefonunkilerdir, 4 sütun ve sınırsız. Daha geniş bir sınıfı veren ürün onları da verir:

```kotlin
PantrixLayouts(large = PantrixLayout(margin = 40.dp, gutter = 24.dp, columns = 12, maxContentWidth = 1200.dp))
```

`ExtraLarge` verilmezse `Large`'ın ölçülerini alır.

## 13. Testler {#testing}

:::table
| Dosya | Ne tutuyor |
| :--- | :--- |
| `commonTest/.../adaptive/WindowSizeClassTest.kt` | Sınırlar (599,9 / 600 …), sınıfların sırası, bilinmeyen boyut, kendi sınırların |
| `commonTest/.../adaptive/WindowPostureTest.kt` | Kitap, masa, düz açık, iki ekranlı menteşe |
| `commonTest/.../adaptive/PantrixWindowTest.kt` | Temanın verdiği pencere, boyut değişimi, yeniden kurulma, sabitleme, iç içe tema |
| `commonTest/.../adaptive/WindowHelpersTest.kt` | Altı yardımcı: verdikleri cevaplar ve ne zaman yeniden kurdukları |
| `commonTest/.../adaptive/PanesTest.kt` | Panel sayısı (genişlik, kitap, masa, iki ekranlı menteşe, düz açık), çerçevenin penceresi, menteşenin bir düğümdeki yeri: çerçevede, iç içe çerçevede, küçük çizilen çerçevede, uygulamanın penceresinde düğüm yer değiştirince |
| `iosTest/.../adaptive/UIKitFoldTest.kt` | UIKit'in söylediğinden duruş: kapak ekranı, kitap, masa, düz açık, menteşeyi örten bölge, menteşesi olmayan iPhone (iPhone Duo simülatörünün sayılarıyla) |
| `commonTest/.../adaptive/BoxWithPantrixWindowTest.kt` | Bileşenin kutusu: söylediği yer, pencereden aldığı sınırlar, ne zaman yeniden kurduğu; pencerenin söylediği sınırlar |
| `androidHostTest/.../adaptive/WindowOnAndroidTest.kt` | Android'in bildirdiği pencere ve WindowManager'dan gelen menteşe |
| `commonTest/.../adaptive/LayoutColumnsTest.kt` | Her sınıfın sütun sayısı ve en fazla içerik genişliği, yoğunluğun onları ölçeklememesi, `spanWidth` (sütunlar ve aradaki boşluklar, dar yer, sınırlar), `Modifier.maxContentWidth` (sınır, pencere yeniden boyutlanınca izlenmesi, başta, ortada, sonda, sağdan sola, ürünün sınırı, bileşenin kutusu, sınırsız genişlik) |
| `commonTest/.../adaptive/PantrixLayoutTest.kt` | Her sınıfın kenar boşluğu ve sütun arası, sınıf değişince izlenmesi, yoğunlukla ölçeklenmesi, ürünün kendi ölçüleri, bileşenin kutusu |
| `commonTest/.../a11y/PantrixPlatformTest.kt` | Her platformun dokunma hedefi, dokunmatik tarayıcı, `isTouch`, işaretçi değişince izlenmesi, yerleşim ve isabet |
| `commonTest/.../theme/NestedThemeTest.kt` | İç içe tema: devralınanlar, bir kez ölçekleme, koyu bölüm ve preset'in koyu seti, verilenin içtekinin olması, preset'li tema, dıştaki değişince izlemek, tema dışından gelen değerin devralınmaması, içeriği çevredeki düzenin yerleştirmesi |
| `androidHostTest/.../theme/ThemeOnAndroidTest.kt` | Sistemin gece modunda tema; iç içe temada sistemin değil dıştaki temanın koyuluğu; test etiketlerinin otomasyona bir kez, en dıştaki temada açılması |
| `webTest/.../adaptive/WindowPostureWebTest.kt` | Tarayıcının söylediği menteşe: kitap, masa, iki ekranlı menteşe, düz açık; pencere ve yoğunluk değişince yeniden okuma; parçaların tarayıcıdan okunması; tarayıcının üç sorguyu tanıması; gerçek tarayıcıda düz |
| `webTest/.../platform/MediaQueryTest.kt` | Gerçek tarayıcıda: medya sorgusunun cevabı, sayfa açıkken değişmesi, dinlemenin bırakılması |
| `ui/components` · `commonTest/.../TouchBrowserTest.kt` | Dokunmatik tarayıcıda bileşenler: görünüş aynı, ayrılan yer parmağın |
| `ui/components` · `commonTest/.../NestedThemeTest.kt` | Koyu bölümdeki kontroller: uygulamanın platformu, dili ve analytics'i; geçerli stiller, preset'li temada da |
| `ui/components` · `commonTest/.../TwoPaneTest.kt` | İki panelin payı, kendi boyutu, alt alta, sağdan sola, menteşede buluşması (dikey ve yatay, sağdan sola), ayırmayan ve yerin dışındaki menteşe, sınırsız yükseklik, sınırsız bölünme yönü, varsayılan boşluk |
| `ui/components` · `commonTest/.../TwoPaneHandleTest.kt` | İki panelin tutamacı: yalnızca `onFirstSizeChange` ile, pay ve kendi boyutu olarak bildirilen boyut, oklar, Home ve End, ekran okuyucu, alt alta, sağdan sola, boyutu tutan çağıran |
| `ui/components` · `commonTest/.../PaneHandleTouchTest.kt` | Boşluktan geniş şerit: bölmenin kenarına dokunuş bölmenin, aynı yönde kayan bölmenin üstünden sürükleme tutamacın, iki iskelette de; fare şeritteyken bölme üstte sayılmaz |
| `ui/components` · `commonTest/.../PaneHandleColorsTest.kt` | Tutamacın renkleri `TOLD_APART`'ta: verilmeyen temanın, dururken, sürüklenirken ve odakta kendi rengi, üç bileşende |
| `ui/components` · `commonTest/.../ListDetailScaffoldTest.kt` | Listenin genişlik sınırları; tek panel, geri (dağıtıcıdan), ilerleyen ve vazgeçilen geri hareketi, iki panelde uygulamaya kalan geri; iki ve üç panel, sağdan sola, menteşede buluşma ve tutamacın olmaması; ekstra panelin yanda ve kendini kaydırırken sheet'te durması; bir panelden ikiye korunan durum; tutamacın sürüklenmesi (fare, parmak), okları, Home ve End, ekran okuyucunun ayarlaması, sağdan sola; odağın gösterilen panelle gitmesi; durumun kaydedilmesi |
| `ui/components` · `commonTest/.../ScaffoldTest.kt` | Masa duruşunda navigasyonun yeri, bir yarıya sığan scaffold, katlanırken korunan durum |
| `commonTest/.../adaptive/WindowVerticalBarTest.kt` | Pencerenin dikey bar'ı: sabit pencere, sistemin penceresinin onu izlemesi, testlerin platformlarında olmaması, düğümün koordinatlarında sütun, sütuna uzanmayan düğüm, çerçevenin ve temanın penceresi, düğüm yerleşmeden önce |
| `iosTest/.../adaptive/UIKitVerticalBarTest.kt` | UIKit'in söylediğinden dikey bar (iPhone Duo'nun dış ekranının sayılarıyla), sayı yoksa bar yok |
| `ui/components` · `commonTest/.../ScaffoldVerticalBarTest.kt` | Sütunda gezinme ve düğmeler, sütunsuz ve sütuna uzanmayan scaffold, Start'a karşı dikey bar, içeriğin payı, soldaki sütun, sağdan sola, kısa scaffold, son gelen bar'ın düğmeleri, iç içe scaffold, sütuna girerken korunan kompozisyon |
| `ui/components` · `commonTest/.../NavigationBarUprightTest.kt` | Dik bar: alt alta hedefler, etiketsiz hedefin adı, uzun basışta tooltip, etiketler ve kapsülün boşlukları, kapsülün içinde kalan rozet (soldan sağa ve sağdan sola), alttaki bar'da yalnız ikonlar |
| `app/shared` · `jvmTest/.../demo/AdaptiveSamplesTest.kt` | Örnek uygulamadaki üç vaka: giriş ekranı, tablo bileşeni, göstermek ve gizlemek ([kullanım rehberi](/docs/guides/adaptive-usage)); pencere sayfasında duruşun adı ve panellerin menteşede ayrılması; liste ve ayrıntı sayfasında seçilen pencereye göre panel sayısı, kitapta eşit iki sayfa, tek panelde açılan ileti ve geri düğmesi |
| `app/shared` · `jvmTest/.../demo/NestedThemeSamplesTest.kt` | Örnek uygulamanın iç içe tema sayfası: sayfanın uygulamadan aldıkları, koyu bölüm, sıkışık bölüm, başka dilde bölüm, olayların sayfaya gitmesi, bir `Row` içindeki tema |
:::

```bash
./gradlew :ui:foundation:jvmTest --tests 'com.pantrixui.foundation.adaptive.*'
```

```bash
./gradlew :ui:foundation:testAndroidHostTest
```

## 14. Henüz olmayanlar {#what-is-not-there-yet}

*   Menteşeye göre davranan bileşenler `PantrixScaffold` (masa duruşu), `PantrixTwoPane`, `PantrixListDetailScaffold`, `PantrixSupportingPaneScaffold`, `PantrixTabletopLayout` (menteşede bölünme) ve bütün katmanlar (menteşenin bir yarısında). Sunumunu pencerenin genişliğine göre seçen katmanlar da var: dar pencerede sheet olan menü (`OverlayPresentation.Adaptive`), dar pencerede tam ekran, genişte yan sheet (`SheetEdge.Adaptive`), tarih denetimlerinin takvimi (`DatePickerPresentation.Adaptive`). Navigation 3 ile birlikte çalışan bir liste-ayrıntı sahnesi yok.
*   Sistemin dikey bar'ından çıkmak (`preferredVerticalBarBehavior = .disabled`) kütüphanede yok: o ayar bir `UIViewController`'ındır ve Compose'un view controller'ı onu değiştirmeye izin vermez. Durum çubuğunu yatay isteyen bir uygulama, Compose görünümünü saran kendi view controller'ında ayarlar. İç ekranın sistemden ne söylediği (dikey bar var mı) henüz ölçülmedi.
*   Masaüstünde dokunmatik ekran algılanmaz ve algılanmayacak: JVM bunu bildirmiyor. Dokunmatik bir masaüstü uygulaması bunu temaya kendisi söyler (11. bölüm).
