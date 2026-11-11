---
title: Bir ekranın hâllerini önizlemek
description: Compose Multiplatform'da yazılmış bir ekranın önizlemesinin neden makineden makineye ve telefondaki uygulamadan farklı çizildiği, bir ekranın hâllerinin pencerelerinde, uygulamanın temasında, koyu ve açık olarak nasıl önizlendiği ve bir ekran görüntüsü testinin aynı hâlleri nasıl çizdiği. Sorun Pantrix'e özgü değildir: ekranlarını önizleyen her Compose Multiplatform projesinde aynı biçimde görülür.
---

Compose hakkında söylenenler Compose Multiplatform 1.12.2'nin kaynaklarında okundu (`ui-tooling-preview`'daki `Preview.kt`, `PreviewParameter.kt`) ya da bu depoda koşan bir testte görüldü; denenmeyenler 6. bölümde yazılıdır.

## 1. Sorun {#problem}

:::aside previews
### Belirti

*   Bir telefon ekranının önizlemesi farenin dokunma hedefleriyle ve kısayollarında Ctrl ile çizilir: IDE onu JVM'de çizer, tema da platformu makineden okur.
*   Bir ekranın koyu önizlemesi açık, açık önizlemesi koyu çıkar: tema makinenin kendi koyu kipini izler.
*   Bugünün tarihini gösteren bir ekran her gün değişir; aynı hâlden çizilen ekran görüntüsü testi ay dönümünde düşer.
*   Katlanan bir telefonun iki yarısının önizlemesi tek panel gösterir: ekrana pencerenin katlı olduğunu hiçbir şey söylemez.
*   Bir ekranın hâlleri iki kez sayılır, bir kez önizlemeler için, bir kez ekran görüntüsü testleri için; iki liste ayrılır.
:::

### Neden

*   **Tema varsayılanlarını cihazdan okur.** Bir uygulama için doğrusu budur; önizlemede cihaz, IDE'nin çalıştığı makinedir: platformu, koyu kipi, işletim sistemi ve saati.
*   **`@Preview` ortak kodda boyuttan pek fazlasını söylemez.** `widthDp`, `heightDp`, `locale` ve `fontScale` her platforma ulaşır; koyuluğu söyleyen `uiMode` Android'indir.
*   **Test teması bir testindir.** `PantrixTestTheme` bunların hepsini sabitler, ama yalnızca test kaynak kümelerine giren `com.pantrixui:testing`'tedir: `commonMain`'deki bir önizleme onu kullanamaz.

## 2. Çözüm {#fix}

:::table
| # | Parça | Ne verir |
| :--- | :--- | :--- |
| 1 | **Uygulamanın temasının çevresinde bir önizleme teması.** Platform, koyu ya da açık, pencere ve saat önizlemenin seçimidir | Önizleme her makinede aynıdır ve söylediği platformdaki ekrandır |
| 2 | **Hâller bir liste olarak.** Bir `PreviewParameterProvider` onları sayar | Her hâl bir önizlemedir, liste bir kez yazılır |
| 3 | **Pencereler boyut olarak.** `@Preview(widthDp, heightDp)`, bir boyutun söylemediği için de pencere | Ekran dizildiği yerde görülür: bir telefon, bir tablet, bir kat |
| 4 | **Aynı hâller bir ekran görüntüsü testinde.** Test aynı listeyi `PantrixTestTheme`'de dolaşır | Önizlenen, denetlenendir |
:::

## 3. Pantrix ile {#with}

### 3.1. Önizlemenin teması

`PantrixPreviewTheme`, `com.pantrixui:foundation`'dadır (`com.pantrixui.foundation.theme`). Uygulamanın kendi teması onun içine konur: bir başkasının içindeki tema, kendisi vermedikçe dıştakinin verdiğini korur; kendi renkleri de dıştaki ne ise öyle koyu ya da açıktır.

:::table
| Parametre | Varsayılan | Neyi sabitler |
| :--- | :--- | :--- |
| `platform` | `PlatformType.Android` | Dokunma hedeflerini, odak halkalarını ve platformun yollarını; kısayollar, masaüstünde ve web'de Windows'unki, sisteminin yazdığı gibi yazılır |
| `dark` | `false` | Makinenin kipi ne olursa olsun koyu ya da açık |
| `window` | `null`: önizlemenin kendisi | Ekrana içinde olduğu söylenen pencere, boyu ve katlarıyla; bütün önizlemenin üstünde durur |
| `clock` | `null`: cihazınki | Ekranın okuduğu an: `rememberToday()`, bir saat seçicinin saati, bir grafiğin anları |
| hareket | Azaltılmış | Hiçbir şey yarı yolda çizilmez |
:::

```kotlin
@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun CheckoutOnAnIphoneDarkPreview() {
    PantrixPreviewTheme(platform = PlatformType.Ios, dark = true, clock = PreviewClock) {
        AppTheme { CheckoutScreen(state = CheckoutState.Filled) }
    }
}

/** 24 Ekim 2026 öğlesi, UTC: her önizlemenin ve her ekran görüntüsünün gösterdiği an. */
val PreviewInstant: Instant = Instant.parse("2026-10-24T12:00:00Z")

val PreviewClock = PantrixClock(
    object : Clock {
        override fun now(): Instant = PreviewInstant
    }
) { Duration.ZERO }
```

*   **Dil sabitlenmez.** İçteki uygulama teması bir dil vermedikçe cihazınkidir; kendi dilini tutan bir uygulama verir: `PantrixTheme(locale = …)`. `@Preview(locale = "tr")` onu yalnızca Android'in önizlemesine söyler.
*   **`AppTheme` uygulamanındır.** Bir preset üstüne kurulan tema (`PantrixTheme(preset = Brand)`) preset'in koyu ya da açık renklerini `dark`'a göre alır; renkleri doğrudan verilen bir tema onları korur.

### 3.2. Bir ekranın hâlleri

Bir `PreviewParameterProvider` hâlleri bir kez sayar; bir önizleme onları parametre olarak alır ve her biri kendi önizlemesi olarak çizilir:

```kotlin
class CheckoutStates : PreviewParameterProvider<CheckoutState> {
    override val values = sequenceOf(CheckoutState.Empty, CheckoutState.Filled, CheckoutState.Failed)
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun CheckoutPreview(@PreviewParameter(CheckoutStates::class) state: CheckoutState) {
    PantrixPreviewTheme(clock = PreviewClock) { AppTheme { CheckoutScreen(state = state) } }
}
```

### 3.3. Pencereler

*   **Bir boyut bir `@Preview`'dir.** `widthDp` ve `heightDp` temanın okuduğu penceredir: 390×844 bir telefon, 834×1194 dik duran bir tablet, 1440×900 bir masaüstü. Bir fonksiyonun üstünde birkaç `@Preview` durur.
*   **Bir boyutun söylemediği pencerededir.** Bir kat, bir duruş, bir panel: `PantrixPreviewTheme(window = PantrixWindow(DpSize(951.dp, 669.dp), posture = WindowPosture(listOf(fold))))`. Pencere bütün önizlemenin üstünde durur, içinde açılan bir katman da onu örter.

### 3.4. Aynı hâller bir ekran görüntüsü testinde

Test aynı sağlayıcıyı, aynı platform, koyuluk ve saatle `PantrixTestTheme`'de dolaşır:

```kotlin
@OptIn(ExperimentalTestApi::class)
class CheckoutScreenshotTest {
    @Test
    fun every_state() = CheckoutStates().values.forEachIndexed { index, state ->
        runDesktopComposeUiTest(width = 390, height = 844) {
            setContent {
                PantrixTestTheme(platform = PlatformType.Ios, dark = true, clock = TestClock(PreviewInstant)) {
                    AppTheme { CheckoutScreen(state = state) }
                }
            }
            onRoot().captureRoboImage("Checkout_$index.png")
        }
    }
}
```

`PantrixTestTheme` bir `TestClock`, önizleme bir `PantrixClock` alır: ikisine aynı an verilir. Ekran görüntüsünün kurulumu [test rehberindedir](/docs/guides/testing) (3.6).

## 4. Pantrix olmadan {#without}

Kütüphanesiz bir projeye kopyalanabilecek en kısa biçim: bir temanın cihazdan okuyacağı değerler, uygulamanın temasının çevresinde doğrudan verilir.

```kotlin
@Composable
fun FixedPreview(dark: Boolean = false, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalAppDark provides dark,               // uygulamanın kendi yereli; teması isSystemInDarkTheme() yerine onu okur
        LocalAppClock provides PreviewClock,      // uygulamanın kendi saati; ekranları Clock.System yerine onu okur
        LocalLayoutDirection provides LayoutDirection.Ltr,
        content = content
    )
}
```

Uygulamanın temasının cihazdan okuduğu her değer uygulamanın kendi bir yerelini ister: `isSystemInDarkTheme()`'i ya da `Clock.System`'i kendisi çağıran bir tema dışarıdan sabitlenemez.

## 5. Tuzaklar {#pitfalls}

*   **Önizlemede `PantrixTestTheme`.** Yalnızca test kaynak kümelerine giren `com.pantrixui:testing`'tedir: `commonMain`'deki bir önizleme onu görmez, onu `commonMain`'e ekleyen bir uygulama da test etkinliğini yayına taşır. Bir önizleme `PantrixPreviewTheme`'i alır.
*   **`uiMode` Android'indir.** `@Preview(uiMode = UI_MODE_NIGHT_YES)` sistemi yalnızca Android'in önizlemesinde koyu yapar; `PantrixPreviewTheme(dark = true)` bunu her platformda yapar.
*   **Renkleri doğrudan verilen bir tema.** Önizlemenin temasının içindeki `PantrixTheme(colors = BrandLight)` koyu bir önizlemede açık kalır: uygulama setini `dark`'a göre kendisi seçer ya da temasını bir preset üstüne kurar.

## 6. Doğrulama {#verification}

### Testler

:::table
| Ne | Nerede |
| :--- | :--- |
| Platform, koyu ve açık, kısayolların yazıldığı sistem, azaltılmış hareket; içteki uygulama temasının platformu koruması ve preset'inin koyu renklerini alması; saat ve bütün önizlemenin üstünde duran pencere | `ui/foundation/src/commonTest/.../theme/PantrixPreviewThemeTest.kt` (JVM, iOS, JS, Wasm) |
:::

### Elle bakılanlar

*   **Demo'nun "Bir önizlemenin teması" örneği**, iç içe tema sayfasında: demo'nun kendi temasının içinde iOS'ta, koyu, 24 Ekim 2026'da, Harbor preset'inin koyu renkleriyle çizilen bir bölüm.

### Doğrulanmayanlar

*   Android Studio'nun ya da IntelliJ'in önizleme panelinde çizilen bir önizleme: `PantrixPreviewTheme` testlerde ve demo'da çizildi, bir IDE'de çizilmedi.
*   3.4'teki ekran görüntüsü testi, yazıldığı hâliyle: test rehberinin söylediklerini bir araya getirir, koşturulmadı.

## 7. Bu depoda nerede {#repository toc="7. Depoda"}

*   `ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/theme/PantrixPreviewTheme.kt` ve `commonTest`'te yanındaki testi.
*   Demo: `app/shared/src/commonMain/kotlin/com/pantrixui/cmp/demo/samples/NestedThemeSamples.kt` ("Bir önizlemenin teması").
