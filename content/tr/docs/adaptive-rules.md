---
title: Uyarlanabilir arayüzün kuralları
description: Pantrix ile yazılan bir arayüzün pencereye nasıl uyacağının kuralları; bir ekran ya da bileşen yazarken neye bakılır, ne değişir, ne aynı kalır, ve kütüphanenin bugün ne yaptığı.
section: Rehberler
---

Pantrix ile yazılan bir arayüz Android, iOS, masaüstü ve web'de aynı koddur. Bu belge, o arayüzün pencereye nasıl uyacağının **kurallarını** söyler: bir ekran ya da bileşen yazarken neye bakılır, ne değişir, ne aynı kalır. Her kuralın yanında kütüphanenin bugün ne yaptığı yazar.

Üç belge birbirini tamamlar:

:::table
| Belge | Ne söyler |
| :--- | :--- |
| Bu belge | Kurallar: ne yapılmalı |
| [Uyarlanabilir arayüz rehberi](/docs/guides/adaptive) | Kütüphanenin verdikleri: API nasıl çalışır |
| [Uyarlanabilir yardımcıların kullanımı](/docs/guides/adaptive-usage) | Örnek vakalar: giriş ekranı, tablo, göstermek ve gizlemek |
:::

## 1. İlkeler {#principles}

1. **Cihaz değil pencere.** Düzeni cihazın türü değil, pencerenin boyutu seçer. iPad Split View, masaüstü penceresi ve tarayıcı sekmesi serbestçe boyut değiştirir; açılan bir katlanabilir bir anda tablet olur. `if (platform == Android)` bir düzen kararı değildir.
2. **Düzeni genişlik seçer.** Yükseklik, üst üste dizmeye yer olup olmadığını söyler. Duruş, pencerenin menteşede ikiye ayrılıp ayrılmadığını söyler.
3. **Ekran pencereye, bileşen kendi kutusuna bakar.** Ekran `PantrixTheme.window`'a sorar. Bileşen nereye konacağını bilmez: kendisine verilen yere sorar (`BoxWithPantrixWindow`).
4. **Parmak mı, fare mi: platforma değil girdiye sorulur.** Telefondaki tarayıcı da bir web'dir, dokunmatik bir kiosk da bir masaüstü.
5. **İçerik tek yerden çağrılır.** Pencere değişince çevresindeki düzen değişir; içerik aynı yerde durur ve durumu (seçim, kaydırma, yazılan metin) korunur.

## 2. Pencere sınıfları {#window-classes}

### Genişlik

:::table
| Sınıf | Genişlik (dp) | Nerede görülür | Kenar boşluğu | Sütun arası |
| :--- | :--- | :--- | :--- | :--- |
| **Compact** | < 600 | Telefon, katlanabilirin kapak ekranı, dar tarayıcı penceresi | 16 | 16 |
| **Medium** | 600–839 | Dikey tablet, açık katlanabilir, iPad Split View | 24 | 16 |
| **Expanded** | 840–1199 | Yatay tablet, yatay telefon | 24 | 24 |
| **Large** | 1200–1599 | Masaüstü penceresi, tarayıcı | 32 | 24 |
| **ExtraLarge** | ≥ 1600 | Geniş masaüstü penceresi, harici ekran | 32 | 24 |
:::

Sınırlar Android'in pencere boyutu sınıflarıdır (600 / 840 / 1200 / 1600). Ürün kendi sınırlarını temaya verebilir; o zaman bu belgedeki sayılar onun sayılarıyla okunur.

*   **Kural:** düzen sınıfa göre seçilir, boyutun kendisine göre değil. Sınıfı okuyan yalnızca sınıf değişince yeniden kurulur.
*   **Kural:** bir sınıf için ayrı tasarım yoksa bir alttakinin düzeni geçerlidir. ExtraLarge, Large'ı izler.
*   **Bugün:** `PantrixTheme.window.widthClass`, `byWidth(...)`, `responsive(...)`, `isNarrow`, `rememberWindowIsAtLeast(dp)`.

### Yükseklik

:::table
| Sınıf | Yükseklik (dp) | Nerede görülür |
| :--- | :--- | :--- |
| **Compact** | < 480 | Yatay tutulan telefon, alçak bir masaüstü ya da tarayıcı penceresi |
| **Medium** | 480–899 | Dikey telefon, yatay tablet |
| **Expanded** | ≥ 900 | Dikey tablet, masaüstü penceresi |
:::

Yatay tutulan telefon genişliğiyle Expanded çıkar (iPhone 17: 874 × 402 dp), ama dikeyde yeri yoktur: enine uzanan her çubuk ekranın yedide birini alır. **Kısa pencere kuralı** (yükseklik Compact iken):

*   Düzeni yine genişlik seçer: navigasyon yanda durur.
*   Altta sabit çubuk olmaz; ana aksiyon başlıktadır.
*   Alttan açılan sheet olmaz: menüler ve onaylar popover'dır, formlar yan sheet'tir.
*   Masa duruşu bu kuralın dışındadır (4. bölüm).

**Bugün:** `heightClass`, `byHeight(...)`, `isShort`. `PantrixScaffold` navigasyonu genişliğe göre yana alır, yani yatay telefonda yandadır. Menü, tarih seçici ve destek paneli de genişliğe bakar: yatay telefon genişliğiyle Expanded olduğundan orada menü popover, destek paneli yanda bir paneldir (10. bölüm). Yüksekliğe ayrıca bakmazlar: hem kısa hem Compact genişlikte bir pencere menüyü yine alttan sheet olarak açar.

## 3. Düzen ölçüleri {#layout}

*   **Kural:** sayfa içeriğini yanlardan `PantrixTheme.layout.margin` ile içeri alır. Değer elle seçilmez.
*   **Kural:** sütunların ve yan yana panellerin arası `PantrixTheme.layout.gutter`'dır.
*   **Kural:** yoğunluk (`Compact`, `Spacious`) yalnızca boşluğu ölçekler: kenar boşluğu, sütun arası, dolgu, yükseklik. Köşe, çizgi kalınlığı ve sınıf sınırları ölçeklenmez.
*   **Kural:** bir panelin içindeki bileşen pencerenin değil kendi kutusunun ölçüsünü alır: `PantrixTheme.layout(box)`.
*   **Kural:** sayfanın ızgarası `PantrixTheme.layout.columns` sütundur: 4, Medium'dan itibaren 8, Expanded'dan itibaren 12. Bir parçanın genişliği `layout.spanWidth(available, span)`'dir.
*   **Kural:** içerik `PantrixTheme.layout.maxContentWidth`'ten geniş olmaz: Large'dan itibaren 1040 dp, daha dar sınıflarda sınırsız. `Modifier.maxContentWidth()` içeriği o genişlikte tutar.
*   **Bugün:** dört ölçü de var: kenar boşluğu, sütun arası, sütun sayısı, en fazla içerik genişliği.

## 4. Katlanabilir duruşlar {#postures}

:::table
| Duruş | Nasıl anlaşılır | Kural |
| :--- | :--- | :--- |
| **Kapalı** (kapak ekranı) | Pencere Compact, menteşe yok | Telefon düzeninin aynısı. Açınca aynı ekran, aynı seçim ve aynı kaydırma konumuyla devam eder. |
| **Açık, düz** | Menteşe var, ekran tek parça | Sınıfının düzeni. Menteşeden kaçınmaya gerek yok. |
| **Kitap** | Yarı açık, menteşe dikey | İki panel tam menteşede ayrılır; menteşenin üstüne hiçbir şey gelmez. |
| **Masa** | Yarı açık, menteşe yatay | Üst yarı bakılacak şeyler içindir; kontroller, formlar ve navigasyon alt yarıdadır. |
:::

*   **Kural:** pencereyi ayıran menteşenin (`posture.separatingFold`) üstüne içerik konmaz ve dokunulacak kontroller ona yakın durmaz.
*   **Kural:** iki ekranlı cihazın menteşesi her açıda ayırır; tek ekranlı katlanabilirin menteşesi yalnızca yarı açıkken ayırır.
*   **Bugün:** `PantrixTheme.window.posture`, `isBook`, `isTabletop`, `byPosture(...)`. `PantrixScaffold` masa duruşunda navigasyonu her genişlikte altta tutar. `PantrixTwoPane` iki paneli tam menteşede böler ve aralarında menteşenin boş istediği şeridi (`clearance`) bırakır; `PantrixTheme.window.paneCount()` kaç panel gösterileceğini söyler, kitap duruşunda her genişlikte en az iki. Kendi düzenini yazan menteşeyi `rememberFoldsInNode()` ile düğümünde okur.

Menteşeyi kim söyler:

:::table
| Platform | Kaynak | Durum |
| :--- | :--- | :--- |
| Android | Jetpack WindowManager (`FoldingFeature`) | Okunuyor |
| Web | Tarayıcının Device Posture ve Viewport Segments API'leri (Chromium) | Okunuyor |
| iOS | UIKit, iOS 27.1'den itibaren: ayrılmış bölgeler (`.division`, `.occlusion`) ve `UIHingeInteraction` | Okunuyor. Daha eski iOS'ta düz. |
| Masaüstü | Yok | Düz |
:::

Menteşeyi kendi okuyan bir uygulama duruşu temaya verir: `rememberPantrixWindow(posture = …)`.

## 5. Girdi {#input}

:::table
| Platform | En küçük dokunma hedefi |
| :--- | :--- |
| Android | 48 dp |
| iOS | 44 pt |
| Web, fare ile | 32 dp |
| Web, dokunarak | 48 dp |
| Masaüstü | 32 dp; uygulama dokunmatik olduğunu söylerse 48 dp |
:::

*   **Kural:** bileşenin görünen boyutu platformunkidir; çevresinde ayırdığı yer parmağa göredir. İkisi ayrı şeylerdir.
*   **Kural:** "dokunmatik mi" diye platforma sorulmaz: `PantrixTheme.platform.isTouch`.
*   **Kural:** hover yalnızca bir yardımcıdır. Hover'da görünen her şeye klavyeyle ve dokunarak da ulaşılır.
*   **Bugün:** web'de işaretçi canlı izlenir (`pointer: coarse`): tablete trackpad takılınca hedef küçülür. Masaüstünde dokunmatik ekran algılanmaz, çünkü JVM bunu bildirmez; kiosk gibi dokunmatik bir masaüstü uygulaması bunu temaya kendisi söyler: `PantrixPlatform.forType(PlatformType.Desktop, isTouch = true)`.

## 6. Bileşenler {#components}

*   **Kural:** bileşen pencereye sormaz. Aynı tablo geniş bir pencerede dar bir panele konabilir; pencereye sorsaydı orada da tablo çizer ve sığmazdı.
*   **Kural:** bileşen kendi yerini `BoxWithPantrixWindow` ile öğrenir ve ekranın pencereye sorduğu soruları kutusuna sorar (`isNarrow`, `byWidth`, `widthClass`).
*   **Kural:** iki görünümü olan bileşenin (tablo ve liste gibi) seçimi bileşenin dışında tutulur; görünüm değişince kaybolmaz.

## 7. Göstermek ve gizlemek {#showing-and-hiding}

:::table
| Neye göre | Ne zaman | Nasıl |
| :--- | :--- | :--- |
| Pencere | Yer yoksa gösterme | `window.isNarrow`, `byWidth(...)` |
| Girdi | Yalnızca parmakla ya da yalnızca fareyle anlamlıysa | `platform.isTouch` |
| Platform | Yalnızca o platformda var olan bir şeyse (mağaza bağlantısı gibi) | `platform.type` |
:::

*   **Kural:** yer yüzünden gizlenen şey kaybolmaz, başka bir yere taşınır: menüye, ikinci satıra, ayrı bir sayfaya.
*   **Kural:** gizlemek yerine değiştirmek çoğu zaman daha iyidir: etiketli düğme dar yerde ikon olur.

## 8. Süreklilik ve durum {#continuity}

*   **Kural:** pencere büyüyüp küçülünce, cihaz katlanıp açılınca kullanıcı aynı ekranda kalır. Seçili öğe, kaydırma konumu ve formdaki kaydedilmemiş metin korunur.
*   **Kural:** içerik düzenin her kolundan ayrı ayrı çağrılmaz. Ayrı çağrılan içerik Compose için ayrı içeriktir ve durumu sıfırlanır.
*   **Kural:** yeri değişmek zorunda olan içerik `movableContentOf` ile taşınır.
*   **Kural:** korunması gereken durum `rememberSaveable`'da ya da ViewModel'dedir.
*   **Bugün:** `PantrixScaffold` içeriği tek yerden çağırır. Android'de pencere değişiminde Activity'nin yeniden yaratılmaması için manifest'e `configChanges` yazılır (örnek uygulamada var).

## 9. Tema {#theme}

*   **Kural:** uygulamanın tek bir kök teması vardır. Platform, dil, analytics ve stiller orada söylenir.
*   **Kural:** ekranın bir bölümü farklı görünecekse (koyu bir panel, sıkışık bir tablo) içine ikinci bir tema açılır ve yalnızca değişen şey verilir: `PantrixTheme(darkTheme = true) { … }`. İçteki tema, verilmeyen her ayarı dıştakinden alır; koyu bölümün renkleri, görünüm bir preset ile kurulduysa o preset'in koyu setidir.
*   **Kural:** test ve önizleme pencereyi sabitler: `PantrixTheme(window = PantrixWindow(DpSize(…)))`. Sınıfa bağlı bir davranışı sınayan test penceresini kendisi verir; test ortamının penceresi platforma göre değişir.

## 10. Boyuta göre ne değişir {#by-size}

Tablo, kütüphanenin bileşenlerinin bugünkü varsayılanıdır. İskeletler pencereye değil kendi yerlerine bakar; bir menü ve bir tarih seçici, açıldıkları pencereye.

:::table
| Öğe | Compact | Medium | Expanded | Large ve üstü |
| :--- | :--- | :--- | :--- | :--- |
| Navigasyon (`PantrixScaffold`) | Altta gezinme barı | Yanda ray | Yanda ray; 1088 dp'den sidebar | Yanda sidebar |
| Sayfanın başlığı (`PantrixPageScaffold`, `PageTitlePlacement.Adaptive`) | Üst bar'da | Üst bar'da | İçeriğin başında, büyük | İçeriğin başında, büyük |
| Liste ve detay (`PantrixListDetailScaffold`) | Tek panel; detay listenin yerinde açılır | Tek panel; detay listenin yerinde açılır | Liste ve detay yan yana | Yan yana; ek panel de yanlarında |
| Destek paneli (`PantrixSupportingPaneScaffold`) | Alttan sheet | Yandan sheet | Yanda bir panel | Yanda bir panel |
| Satır menüsü, tarih seçici (`OverlayPresentation.Adaptive`, `DatePickerPresentation.Adaptive`) | Alttan sheet | Popover | Popover | Popover |
| Onay (`PantrixDialog`) | Ortada dialog | Ortada dialog | Ortada dialog | Ortada dialog |
| Sayfanın aksiyonları (`PantrixPageScaffold`'ın `actions`'ı) | Başlıkta, ikonlarıyla; sığmayanlar "…" menüsünde | Başlıkta, etiketleriyle | Başlıkta, etiketleriyle | Başlıkta, etiketleriyle |
:::

*   **Navigasyon:** iskelet navigasyonu 600 dp'de (`ScaffoldStyle.sideNavigationMinWidth`) alttan yana alır; yanda ne duracağını uygulama seçer: `PantrixNavigationRail` ya da `PantrixSidebar`. Uyarlanabilir `PantrixSidebar` (varsayılanı) sütunu yanındakine 840 dp'den az yer bırakacaksa kendiliğinden rayına döner: 248 dp'lik sütunla 1088 dp'den dar bir yerde. Masa duruşunda navigasyon her genişlikte alttadır (4. bölüm).
*   **Liste ve detay, destek paneli:** iskelet kendi alanını sayar; kitap duruşu her genişlikte iki panel yapar ve paneller menteşede buluşur. Destek paneli `SupportingPanePresentation.Pane` ile dar yerde ana içeriğin yerinde açılır.
*   **Popover ve seçim kutuları:** `PantrixPopover`, `PantrixSelect` ve `PantrixMultiSelect` her genişlikte popover'dır; `presentation = OverlayPresentation.Adaptive` onları da menü gibi yapar.
*   **Pencereyle değişmeyenler:** onay her genişlikte ortada bir dialog'dur, ana aksiyon dar pencerede de başlıktadır. Dar pencerede alttan açılan bir onay ya da altta sabit bir aksiyon isteyen ekran onu kendisi kurar: `PantrixSheet` ve `PantrixScaffold`'ın `bottomBar`'ı.

## 11. Bugün ne var {#today}

:::table
| Konu | Durum |
| :--- | :--- |
| Pencere, sınıflar, yardımcılar | Var |
| Düzen ölçüleri (kenar boşluğu, sütun arası, sütun sayısı, en fazla içerik genişliği) | Var |
| Bileşenin kendi kutusu (`BoxWithPantrixWindow`) | Var |
| Dokunma hedefi, dokunmatik tarayıcı | Var |
| Duruş: Android, web ve iOS (27.1'den itibaren) | Var |
| İki panel, panel sayısı, menteşenin bir düğümdeki yeri | Var |
| Scaffold: navigasyonun yeri, masa duruşu | Var |
| İç içe temada devralma | Var |
| Navigasyon bileşenleri | Var |
| Liste ve detay iskeleti, destek paneli, masa duruşu düzeni | Var |
| Uyarlanan sheet ve menüler, menteşede katmanlar | Var |
| Sistemin dikey bar'ı (iPhone Duo'nun dış ekranı): gezinme ve üst bar'ın düğmeleri sütunda | Var |
:::

## Kaynaklar {#sources}

*   [Adaptive layouts | Kotlin Multiplatform Documentation](https://kotlinlang.org/docs/multiplatform/compose-adaptive-layouts.html)
*   [Use window size classes | Android Developers](https://developer.android.com/develop/adaptive-apps/guides/use-window-size-classes)
*   [Make your app fold aware | Android Developers](https://developer.android.com/develop/ui/compose/layouts/adaptive/foldables/make-your-app-fold-aware)
*   [Device Posture API | MDN](https://developer.mozilla.org/en-US/docs/Web/API/Device_Posture_API)
*   [Viewport Segments API | MDN](https://developer.mozilla.org/en-US/docs/Web/API/Viewport_segments_API)
