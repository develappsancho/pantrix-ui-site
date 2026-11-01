---
title: Material 3 köprüsü
description: Material 3 kullanan bir uygulamanın Pantrix'e nasıl geçebileceği ya da ikisini bir arada nasıl kullanabileceği: iki temanın aynı renkleri, yazıları ve şekilleri konuşması. Sorun Pantrix'e özgü değildir.
---

Kütüphanenin kendisi Material'sız kalır: köprü Material'ı kullanan tek modüldür ve hiçbir kütüphane modülü ona bağlanmaz. Burada Material 3 hakkında söylenen her şey Material 3 1.9.0'ın ve 1.11.0-alpha07'nin kaynaklarında okunmuş ya da bu depoda koşan bir testte görülmüştür (11. bölüm).

## 1. Sorun {#problem}

*   **Ekran ekran geçiş:** Material bir uygulamaya Pantrix'le yazılmış bir ekran eklenince iki tema yan yana yaşar. Pantrix'in mavisi Material'ın morunun yanında durur; kenarlar, köşeler ve yazı boyları ekrandan ekrana değişir.
*   **Pantrix'te olmayan bir bileşen:** Pantrix bir uygulamaya bir Material bileşeni (bir kaydırıcı, bir tarih aralığı seçicisi) konunca, bileşen Material'ın varsayılan renkleriyle gelir ve uygulamaya yabancı durur.
*   **Karışık bir ekranın yazısı:** iki sistem, yazının ve ikonun rengini ve yazı stilini ayrı yerlerden okur. Material `LocalContentColor` ve `LocalTextStyle` okur, Pantrix `LocalPantrixContentColor` ve `LocalPantrixTextStyle`. Koyu bir Pantrix temasının içindeki Material `Text`'i, onu bir Material yüzeyi sarmadıkça, Material'ın varsayılanı olan siyahla çizilir.

:::sketch material3
:::

## 2. Kurulum {#setting-up}

```kotlin
commonMain.dependencies {
    implementation("com.pantrixui:material3:0.1.0-alpha04")
}
```

Modül Material 3'ün son kararlı sürümüne, `1.9.0`'a göre derlenir. Bu bir alt sınırdır: uygulamada daha yeni bir Material 3 varsa Gradle onu seçer ve köprü onunla çalışır. Material 3'ün CMP'deki sürümleri 1.9.0'dan sonra yalnızca alfa ve beta olarak çıktı; köprü kullanıcısına bir alfa dayatmamak için kararlı olana bağlanır. Tema API'si 1.9.0 ile 1.11.0-alpha07'de aynıdır: 48 renk, 15 yazı stili, 5 şekil.

## 3. İki yön {#two-directions}

### Material bir uygulamada Pantrix

```kotlin
MaterialTheme(colorScheme = appScheme) {
    Scaffold { padding ->
        PantrixThemeFromMaterial {
            // Pantrix'in bileşenleri uygulamanın Material temasının renkleri, yazıları ve şekilleriyle
            PantrixButton(text = "Kaydet", onClick = {}, modifier = Modifier.padding(padding))
        }
    }
}
```

`PantrixThemeFromMaterial` çevresindeki `MaterialTheme`'i okur ve bir `PantrixTheme` kurar. Renkler şemanın `toPantrixColors()`'ıdır ve tema şema kadar koyudur. Yazılar Material'ın ölçeğinden, Pantrix'in kendi ölçeği üstüne kurulur; şekiller yarıçaplarına göre alınır. Geri kalan her şey (boşluklar, hareket, dil, analitik) Pantrix'in kendisinindir ya da içerideki bir `PantrixTheme`'in verdiği.

Ekranın en dıştaki Pantrix teması bu olduğu için Android'in çubuklarının simgelerini ve iOS'un durum çubuğunu, klavyesini ve kontrollerini şemanın koyuluğuna uydurur ([sistem çubukları](/docs/guides/system-bars) rehberi). Çubuklarını kendisi ayarlayan ya da renkli çubuklu bir Material uygulaması `PantrixThemeFromMaterial(followSystemBars = false) { … }` yazar.

### Pantrix bir uygulamada Material

```kotlin
PantrixTheme {
    MaterialThemeFromPantrix {
        // Pantrix'te olmayan bir Material bileşeni, Pantrix'in temasıyla
        Slider(value = volume, onValueChange = { volume = it })
    }
}
```

`MaterialThemeFromPantrix` çevresindeki Pantrix temasını okur ve bir `MaterialTheme` kurar: şema `toColorScheme()`, yazı ölçeği `toMaterialTypography()`, şekiller `toMaterialShapes()`.

### Düz dönüşümler

İki sarmalayıcının altında altı fonksiyon var; bir uygulama temasını kendisi kuracaksa onları kullanır:

:::table
| Material → Pantrix | Pantrix → Material |
| :--- | :--- |
| `ColorScheme.toPantrixColors(base)` | `PantrixColors.toColorScheme()` |
| `Typography.toPantrixTypography(base)` | `PantrixTypography.toMaterialTypography()` |
| `Shapes.toPantrixShapes(base)` | `PantrixShapes.toMaterialShapes(fallback)` |
:::

### Örnek uygulama

`samples/pantrix-material3` iki yönü tek ekranda gösterir. Aynı form bir kez Material'ın bileşenleriyle, bir kez Pantrix'inkilerle çizilir. Uygulama bir Material uygulaması olarak başlar (markasının şemasıyla, `PantrixThemeFromMaterial`). Bir düğmeyle Pantrix'in kendi temasıyla yazılmış bir uygulamaya döner (`MaterialThemeFromPantrix`). Ekran öteki temaya değerleriyle taşınır: yalnızca görünüş değişir. README'si nasıl çalıştırılacağını söyler.

## 4. Eşleme tabloları {#mapping-tables}

### Renkler: Material → Pantrix

:::table
| Pantrix | Material |
| :--- | :--- |
| `primary` rolü (`color`, `onColor`, `container`, `onContainer`) | `primary`, `onPrimary`, `primaryContainer`, `onPrimaryContainer` |
| `danger` rolü | `error`, `onError`, `errorContainer`, `onErrorContainer` |
| `neutral` rolü | `secondary`, `onSecondary`, `secondaryContainer`, `onSecondaryContainer` |
| `background` | `background` |
| `surface` | `surfaceContainerLow` |
| `onSurface` | `onSurface` |
| `textPrimary` | `onBackground` |
| `textSecondary` | `onSurfaceVariant` |
| `textInverse` | `inverseOnSurface` |
| `border`, `borderStrong` | `outline` |
| `divider` | `outlineVariant` |
| `scrim` | `scrim`'in rengi, `base`'in saydamlığıyla |
| `success`, `info`, `warning`, `borderHover`, `focusRing`, `disabledContainer`, `disabledContent`, `textDisabled`, `chart` | Material'da karşılığı yok: `base`'den (5. bölüm) |
:::

İki eşleme bilerek seçildi:

*   **Yüzey:** Material'ın temel şemasında `surface` ile `background` aynı renktir; kartı ve sayfayı `surfaceContainer…` basamakları ayırır. Pantrix'in `surface`'i bu yüzden `surfaceContainerLow`'dur: kart, Material'da olduğu gibi zeminden ayrılır.
*   **Kutunun kenarı:** Material bir alanın kenarını ve bir kontrolün işaretini aynı renkte, `outline`'da çizer. Pantrix'in iki ayrı rengi (`border` yumuşak kenar, `borderStrong` işaret) burada ikisi de `outline` olur: Pantrix'in alanları uygulamanın Material alanları gibi çizilir.

### Renkler: Pantrix → Material

:::table
| Material | Pantrix |
| :--- | :--- |
| `primary`, `onPrimary`, `primaryContainer`, `onPrimaryContainer` | `primary` rolü |
| `secondary` ve eşleri | `neutral` rolü |
| `tertiary` ve eşleri | `info` rolü |
| `error` ve eşleri | `danger` rolü |
| `background`, `surface` | `background` (Material'ın kendi şemalarındaki gibi ikisi aynı) |
| `onBackground` | `textPrimary` |
| `onSurface` | `onSurface` |
| `onSurfaceVariant` | `textSecondary` |
| `surfaceContainerLow` | `surface` |
| `surfaceContainer`, `surfaceContainerHigh`, `surfaceContainerHighest` | `surface`'in üstüne `textPrimary`'den %4, %8, %12 |
| `surfaceContainerLowest` | Yazıdan uzak olan zemin: açık temada `surface`, koyu temada `background` |
| `surfaceDim`, `surfaceBright` | İki uç: yazıya doğru olanı %16, öteki en uzak zemin |
| `surfaceVariant` | `surfaceContainerHighest` |
| `surfaceTint` | `primary.color` |
| `inverseSurface`, `inverseOnSurface` | `neutral.color`, `textInverse` |
| `inversePrimary` | `primary.container` |
| `outline`, `outlineVariant` | `borderStrong`, `border` |
| `scrim` | `scrim`, tam saydamsızlıkta (Material bileşeni kullanırken kendisi %32'ye indirir) |
| `primaryFixed`, `primaryFixedDim`, `onPrimaryFixed`, `onPrimaryFixedVariant` ve `secondary`, `tertiary` için eşleri | Rolün `container`'ı ve `onContainer`'ı |
:::

`outlineVariant` Pantrix'in bölücüsünden değil kutunun kenarından gelir. Material 3 1.9.0 çerçeveli düğmeyi, seçilmemiş çipi ve çerçeveli kartı bu renkle çizer. Pantrix'in bölücüsüyle bu kenar yüzeye karşı açıkta da koyuda da 1,24:1'de kalıyor, zor seçiliyordu. Pantrix'in kendi alan ve çip kenarıyla (`border`) açıkta 1,47:1, koyuda 1,62:1'dir; Material'ın temel şemasında aynı kenar 1,57:1 ve 1,82:1'dir. Bedeli: Material'ın bölücüleri (`HorizontalDivider` da bu rengi kullanır) Pantrix'inkilerden bir ton koyu çizilir.

### Yazı

Her stil boyca karşılığı olanı alır. Pantrix'in ölçeği: display 36, headline 28, title 22, callout 18, body 16, label 14, footnote 12, caption 11 ve eş aralıklı code 14. Material'ın temel ölçeği: display 57/45/36, headline 32/28/24, title 22/16/14, body 16/14/12, label 14/12/11.

:::table
| Pantrix | Material → Pantrix | Pantrix → Material |
| :--- | :--- | :--- |
| `display` | `displaySmall` | `displayLarge`, `displayMedium`, `displaySmall` |
| `headline` | `headlineMedium` | `headlineLarge`, `headlineMedium` |
| `title` | `titleLarge` | `headlineSmall`, `titleLarge` |
| `callout` | `bodyLarge`'ın yazı tipi ve ağırlığı, `base`'in callout boyunda | — |
| `body` | `bodyLarge` | `titleMedium`, `bodyLarge` |
| `label` | `labelLarge` | `titleSmall`, `bodyMedium`, `labelLarge` |
| `footnote` | `bodySmall` | `bodySmall`, `labelMedium` |
| `caption` | `labelSmall` | `labelSmall` |
| `code` | `base`'in code'u | — |
:::

Material'ın vurgulu stilleri (`…Emphasized`) düz stilleriyle aynıdır. 1.9.0'da vurgulu stilleri ayrıca veren kurucu `internal`'dır; açık kurucu bir uygulamanın ölçeğini böyle kurar.

### Şekiller

Basamaklar adlarına göre değil yarıçaplarına göre eşlenir. Material'ın ölçeği Pantrix'inkinden bir basamak daha yuvarlaktır: Material'ın küçük basamağı 8 dp, Pantrix'inki 4 dp. Adlara göre eşlenseydi Material temasındaki her Pantrix bileşeni iki kat yuvarlak olurdu; 18 dp'lik bir onay kutusu 8 dp köşeyle daireye döner, radyo düğmesine benzerdi. Yarıçapa göre eşlenince Material'ın kendi ölçeği Pantrix'in kendi ölçeğini verir; bir bileşen öteki sistemin temasında da kendi temasındaki kadar yuvarlaktır.

:::table
| Pantrix | Material → Pantrix | Pantrix → Material |
| :--- | :--- | :--- |
| `xs` (2 dp) | `base`'in `xs`'i | — |
| `sm` (4 dp) | `extraSmall` (4 dp) | `extraSmall` |
| `md` (8 dp) | `small` (8 dp) | `small` |
| `lg` (12 dp) | `medium` (12 dp) | `medium` |
| `xl` (16 dp) | `large` (16 dp) | `large` ve `extraLarge` |
:::

Pantrix'in `xs`'i Material'ın her basamağından daha az yuvarlaktır; `base`'den gelir. Material'ın `extraLarge`'ı (28 dp) Pantrix'in her basamağından daha yuvarlaktır: Material → Pantrix yönünde karşılığı yoktur, öteki yönde en yuvarlak basamağı, `xl`'yi alır. Böylece Material'ın diyalogları ve alt sayfaları Pantrix'in köşeli bir markasında da köşeli olur.

`none` (köşeli) ile `full` (tam yuvarlak) iki sistemde de aynıdır. Material yalnızca köşelerden yapılmış şekil (`CornerBasedShape`) alır: köşelerden yapılmamış bir Pantrix şekli o basamakta `fallback`'in şeklini alır (varsayılanı Material'ın kendi ölçeği). Material'ın deneysel üç şekli (`largeIncreased`, `extraLargeIncreased`, `extraExtraLarge`) eşlenmez.

## 5. Karşılığı olmayanlar ve `base` {#what-has-no-counterpart-and-base}

Material'ın şemasında başarı, bilgi ve uyarı rolü, üstüne gelinen kenar, odak halkası, devre dışı renkler ve grafik renkleri yoktur. `toPantrixColors(base)` bunları `base`'den alır. Varsayılan `base`, Pantrix'in kendi renkleridir, şema kadar koyu: koyu bir şema için `PantrixColors.Dark`. Bir uygulama kendi başarı yeşilini vermek isterse kendi `base`'ini verir:

```kotlin
val colors = appScheme.toPantrixColors(base = PantrixColors.Light.copy(success = brandGreen))
```

Yazıda da callout'un boyu ve code `base`'den gelir; `toPantrixTypography(base)`'e verilen ölçek genellikle `rememberPantrixTypography()`'dir. Şekillerde `xs` `base`'den gelir; `toPantrixShapes(base)`'in varsayılanı Pantrix'in kendi ölçeğidir.

## 6. Gidip gelme {#round-trips}

*   **Pantrix → Material → Pantrix,** ilk tema `base` olarak verilince, aynı Pantrix temasını geri verir: bütün renkler, dokuz yazı rolü ve beş şekil. İki istisna çizgilerdedir. Kutunun kenarının Material'da kendi yeri yoktur: `border` geri gelince `borderStrong` olur. Material kutunun kenarını ve bölücüyü tek renkle çizer: `divider` geri gelince `border` olur.
*   **Material → Pantrix → Material,** Pantrix'te yeri olan her rengi korur: rollerin dört rengi, zemin, yazı renkleri, `surfaceContainerLow` ve `outline`. `outlineVariant` geri gelince `outline` olur: Pantrix onu bölücüsünde tutar, Material'a ise kutunun kenarını verir, o da Material'ın `outline`'ından gelir. İç içe bir köprüde (Material bir uygulamada Pantrix, onun içinde yine Material) içteki Material'ın çerçeveli bileşenleri ve bölücüleri dıştakinden koyu çizilir. Material'ın Pantrix'te karşılığı olmayan renkleri (`tertiary`, yüzey basamakları) yeniden türetilir. Şekillerde de `extraLarge`'ın Pantrix'te karşılığı yoktur: geri gelince `large` ile aynı olur.

## 7. Karışık ekran: içerik rengi ve yazı stili {#a-mixed-screen-content-colour-and-text-style}

İki sarmalayıcı kökte öteki sistemin değerlerini de doldurur:

*   **`MaterialThemeFromPantrix`:** Material'ın içerik rengini ve yazı stilini, orada bir Pantrix metninin alacağı renk ve stil yapar. Koyu bir Pantrix temasındaki Material `Text`'i temanın yazı renginde çizilir, siyah değil.
*   **`PantrixThemeFromMaterial`:** Pantrix'in yazı stilini, orada bir Material metninin alacağı stil yapar: şemanın büyük gövde yazısı (`bodyLarge`). İçerik rengini değiştirmez. Bir `MaterialTheme`'in kökünde Material'ın içerik rengi kendi varsayılanı olan siyahtır, onu yalnızca bir Material yüzeyi değiştirir. Pantrix'in kendi rengi ise temanın zeminlerinde zaten şemanın yazı rengidir (`textPrimary` ← `onBackground`).

Ekranın derinindeki bir sınır için iki küçük sarmalayıcı daha var:

```kotlin
Surface(color = MaterialTheme.colorScheme.primaryContainer) {
    // Material'ın yüzeyindeki Pantrix içeriği, yüzeyin içerik rengini alır
    PantrixContentFromMaterial { PantrixText("Taslak kaydedildi") }
}

PantrixSurface(color = PantrixTheme.colors.primary.color, contentColor = PantrixTheme.colors.primary.onColor) {
    // Pantrix'in dolgusundaki Material içeriği, dolgunun içerik rengini alır
    MaterialContentFromPantrix { Text("Yeni") }
}
```

## 8. Material'ın temel şeması Pantrix'in kurallarıyla {#material-s-baseline-scheme-by-pantrix-s-rules}

Material'ın temel şemaları (`lightColorScheme()`, `darkColorScheme()`) Pantrix renklerine çevrilip Pantrix'in kendi renklerine uyguladığı kurallarla ölçüldü (WCAG 2.2: yazı 4.5:1, bir kontrolün işareti ve odak halkası 3:1):

*   Rollerin, yazıların, kontrol işaretinin ve odak halkasının hepsi kurala uyuyor; açık ve koyu temada, zeminde ve yüzeyde.
*   Uymayan tek kural çizgilerin sırasıdır. Pantrix'te çizgiler bölücüden kontrolün işaretine doğru güçlenir (`divider` < `border` < `borderHover` < `borderStrong`). Material'da kutunun kenarı ile işaret aynı renk olduğu için, kenar işaret kadar güçlüdür.

`MaterialContrastTest` bu sonucu tutar: eşleme ya da Material'ın şeması değişirse test düşer.

## 9. Köprüde olmayanlar {#what-is-not-in-the-bridge}

*   **Hareket:** Material'ın `MotionScheme`'i deneysel; Pantrix'in hareketi (ve azaltılmış hareket ayarı) Material'a geçmez.
*   **Dinamik renk:** Android'in duvar kâğıdından gelen renkleri (`dynamicLightColorScheme`) ilk sürümde yok. Bir uygulama onu kendisi çağırıp `toPantrixColors()`'a verebilir.
*   **Yükselti:** Material'ın tonla yükseltisi (`surfaceTint`) ile Pantrix'in gölgeleri birbirine çevrilmez.
*   **Boşluk ve yoğunluk:** Material'da karşılıkları yok.

## 10. Test etmek {#testing}

Köprünün testleri `ui/material3/src/commonTest`'te, beş hedefte koşar: eşlemeler alan alan, gidip gelme, iki sarmalayıcı ve karışık ekranın içerik rengi, Material'ın temel şemasının kontrast ölçümü. Android'in ana makine ağacında ortak testler koşmadığından, iki sarmalayıcıyı Robolectric'te ayrıca bir test çizer (`MaterialThemesOnAndroidTest`).

Bir uygulama kendi temasını şöyle sınar:

```kotlin
@Test
fun pantrix_s_screen_takes_the_app_s_colours() = runComposeUiTest {
    lateinit var colors: PantrixColors
    setContent { MaterialTheme(colorScheme = appScheme) { PantrixThemeFromMaterial { colors = PantrixTheme.colors } } }
    waitForIdle()
    assertEquals(appScheme.primary, colors.primary.color)
}
```

## 11. Doğrulananlar ve doğrulanmayanlar {#verification}

### Doğrulananlar

*   Material 3'ün CMP'deki sürümleri Maven Central'ın listesinden okundu: son kararlı sürüm 1.9.0.
*   1.9.0'ın ve 1.11.0-alpha07'nin `ColorScheme`'i, `Typography`'si, `Shapes`'i ve `MaterialTheme`'i Gradle önbelleğindeki kaynaklardan okundu. Vurgulu yazı stillerini ayrıca veren kurucunun 1.9.0'da `internal` olduğu oradan görüldü.
*   Köprü Compose Multiplatform 1.12.2 ile derleniyor: Material 3 1.9.0, Compose'un 1.12.2'sine yükseltilerek çözülüyor. Testleri JVM'de, Android'de (Robolectric), iOS simülatöründe, JS'te ve Wasm'da geçti.
*   **Daha yeni bir Material 3:** köprü 1.9.0'a göre derlenmişken JVM testleri çalışma zamanında 1.11.0-alpha07 ile koşturuldu ve 24 testin hepsi geçti. Yayımlanan köprü, uygulamanın daha yeni Material 3'üyle ikili olarak uyumlu (en azından bu sürümle).
*   **Örnek uygulama:** iOS simülatöründe, Android emülatöründe, web'de (Wasm) ve masaüstünde (ekran dışında çizilerek) çalıştırıldı, iki tema açıkta ve koyuda resimlerle karşılaştırıldı. Testleri JVM'de, iOS'ta, JS'te, Wasm'da ve Android emülatöründe geçti. Resimler şekil eşlemesindeki kusuru gösterdi (4. bölümdeki "Şekiller").
*   detekt'in `NoMaterialImport` kuralı köprüde kapalı, `ui/components`'ta bir Material import'unu yine yakalıyor.

### Doğrulanmayanlar

*   Köprü 1.11.0-alpha07'den yeni bir Material 3 ile (1.12.0-alpha03, 1.13.0-beta01) çalıştırılmadı; 1.11.0-alpha07 ile de yalnızca JVM'de çalıştırıldı.
*   Örnek uygulamanın iOS'u Android Studio'nun çalıştırma yapılandırmasından denenmedi: Xcode projesi `xcodebuild` ile derlenip simülatörde açıldı.
*   Material'ın deneysel API'leri (vurgulu stiller, genişletilmiş şekiller, `MotionScheme`) köprüde yok; onlarla birlikte kullanım denenmedi.
