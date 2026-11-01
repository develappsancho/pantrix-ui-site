---
title: Temayı izleyen sistem çubukları
description: Compose Multiplatform'da kendi koyu tema düğmesi olan bir uygulamada sistemin çizdiği parçaların, Android'in durum ve gezinme çubuklarının, iOS'un durum çubuğunun, klavyesinin ve kendi kontrollerinin neden sistemin koyu modunda kaldığı, kütüphanenin bunun için ne verdiği ve nasıl test edildiği. Sorun Pantrix'e özgü değildir: kendi temasını seçtiren her Compose Multiplatform uygulamasında aynı biçimde görünür.
---

Burada Compose ve platformlar hakkında söylenenler androidx.activity'nin `EdgeToEdge.kt`'sinde, androidx.core'un `WindowInsetsControllerCompat`'ında ve UIKit'in `UIViewController.overrideUserInterfaceStyle` belgesinde okundu. Doğrulanamayanlar 7. bölümde sayılır.

## 1. Sorun {#problem}

:::aside system-bars
### Belirti

*   Uygulamanın kendi düğmesiyle koyu temaya geçilir; sistem açık modda kalır. Android'de durum çubuğunun saati ve simgeleri koyu kalır ve koyu zeminde okunmaz; gezinme çubuğunun düğmeleri de öyle.
*   Tersi de olur: sistem koyu modda, uygulama açık temada. Simgeler açık renkte kalır ve beyaz zeminde kaybolur.
*   iOS'ta durum çubuğunun yazısı sistemin modunu izler; uygulamanın koyu zemininde koyu yazı kalır. Bir alana dokununca açılan klavye ve sistemin kendi kontrolleri (tarih tekerleği, paylaşma sayfası) da sistemin modundadır.
:::

### Neden

Çubukların ve sistem parçalarının görünüşü uygulamanın temasında değil, platformun bir nesnesindedir:

:::table
| Platform | Ne belirler | Varsayılan |
| :--- | :--- | :--- |
| Android | `WindowInsetsControllerCompat.isAppearanceLightStatusBars` ve `isAppearanceLightNavigationBars` (koyu simgeli çubuğa "açık çubuk" denir) | `enableEdgeToEdge()` bunları sistemin gece moduna göre ayarlar (`SystemBarStyle.auto`) |
| iOS | Görünüm denetleyicisinin `traitCollection.userInterfaceStyle`'ı; durum çubuğu, klavye ve UIKit kontrolleri onu izler | Sistemin modu |
| Masaüstü | Pencerenin çerçevesi işletim sisteminindir | Sistemin teması |
| Web | Tarayıcının kendi çubukları | Tarayıcının teması |
:::

Compose'un ortak kodunda bunlardan hiçbirine ulaşan bir API yoktur: `isSystemInDarkTheme()` yalnızca sistemin modunu okur, uygulamanın seçimini sisteme söylemez.

## 2. Çözüm {#fix}

En dıştaki tema, ekranın koyu mu açık mı olduğunu bilen tek yerdir. Her kompozisyondan sonra o bilgiyi platforma söyler: Android'de pencerenin çubuk simgelerini, iOS'ta uygulamanın görünüm denetleyicisinin arayüz stilini ayarlar. İçteki bir tema (açık bir ekrandaki koyu bir kart) çubuklara dokunmaz: pencerenin çubukları ekranın bütününündür.

## 3. Pantrix ile {#with}

`PantrixTheme` bunu kendisi yapar; uygulamanın bir şey yazması gerekmez. Okurun seçtiği koyuluk temaya verilir, çubuklar onu izler:

```kotlin
val dark = settings.dark ?: isSystemInDarkTheme()
PantrixTheme(preset = Brand, colors = rememberPantrixColors(Brand, darkTheme = dark)) {
    App()
}
```

*   **Android:** uygulama yine `enableEdgeToEdge()` çağırır; çubukların arkası şeffaftır ve uygulama arkalarına çizer. Tema, `enableEdgeToEdge`'in sistemin moduna göre koyduğu simgeleri her değişiklikte kendi zeminine göre yeniden ayarlar.
*   **iOS:** temanın koyuluğu `ComposeUIViewController`'ın `overrideUserInterfaceStyle`'ına yazılır. Durum çubuğu, klavye ve sistemin kontrolleri uygulamanın temasında açılır.
*   **Masaüstü ve web:** bir şey yapmaz.
*   **Koyuluk** temanın zemininden okunur: `background` siyaha beyazdan yakınsa tema koyudur.

### 3.1. çubukları kendisi ayarlayan uygulama

Çubuklarını kendi renkleriyle boyayan (kenardan kenara çizmeyen) ya da simgelerini kendisi seçen bir uygulama, temaya bunu bırakmasını söyler:

```kotlin
PantrixTheme(preset = Brand, followSystemBars = false) {
    App()
}
```

Pantrix'in kendi katmanları (sheet, dialog) kendi pencerelerinin simgelerini yine uygulamanın penceresinden okur; uygulamanın seçimi onlara da geçer.

## 4. Pantrix olmadan {#without}

### 4.1. ortak kod (`commonMain`)

```kotlin
/** Sistemin çizdiği parçaları [dark] temayı izletir; en dıştaki temanın içinde bir kez çağrılır. */
@Composable
expect fun FollowSystemBars(dark: Boolean)
```

Uygulamanın kökünde, temanın hemen içinde:

```kotlin
MyTheme(dark = dark) {
    FollowSystemBars(dark)
    App()
}
```

### 4.2. platform karşılıkları

```kotlin
// androidMain
@Composable
actual fun FollowSystemBars(dark: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return
    SideEffect {
        val window = view.context.activity()?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).apply {
            // Android koyu simgeli çubuğa açık çubuk der.
            isAppearanceLightStatusBars = !dark
            isAppearanceLightNavigationBars = !dark
        }
    }
}

private tailrec fun Context.activity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.activity()
    else -> null
}
```

```kotlin
// iosMain
@Composable
actual fun FollowSystemBars(dark: Boolean) {
    val controller = LocalUIViewController.current
    SideEffect {
        controller.overrideUserInterfaceStyle =
            if (dark) UIUserInterfaceStyle.UIUserInterfaceStyleDark else UIUserInterfaceStyle.UIUserInterfaceStyleLight
    }
}
```

```kotlin
// jvmMain ve webMain
@Composable
actual fun FollowSystemBars(dark: Boolean) = Unit
```

## 5. Test etmek {#testing}

Android'de çubukların durumu Robolectric'te okunur: bir host testi temayı koyu ve açık kurar, etkinliğin penceresinden simgeleri sorar.

```kotlin
@Test
fun the_bars_icons_follow_the_theme_as_it_changes() = runComposeUiTest {
    var dark by mutableStateOf(true)
    var activity: Activity? = null
    setContent {
        activity = LocalActivity.current
        PantrixTheme(darkTheme = dark) { Box(Modifier.size(8.dp)) }
    }
    val window = checkNotNull(activity).window
    val bars = WindowCompat.getInsetsController(window, window.decorView)
    assertFalse(bars.isAppearanceLightStatusBars)
    dark = false
    waitForIdle()
    assertTrue(bars.isAppearanceLightStatusBars)
}
```

iOS'un testleri bir uygulama olmadan, düz bir Compose sahnesinde koşar: orada görünüm denetleyicisi yoktur ve `LocalUIViewController` okununca test düşer. Pantrix bu yüzden önce uygulamanın olup olmadığına bakar; iOS'taki davranış simülatörde denenir.

## 6. Tuzaklar {#pitfalls}

*   **İç içe temalar.** Açık bir ekranın içindeki koyu bir kart çubukları değiştirmemelidir. Çubukları yalnızca en dıştaki tema ayarlar.
*   **`enableEdgeToEdge`'in sırası.** `enableEdgeToEdge()` simgeleri sistemin moduna göre koyar. Kompozisyondan sonra çağrılırsa temanın ayarını ezer: `onCreate`'te, `setContent`'ten önce çağrılır.
*   **Bir `ContextWrapper`.** `LocalView.current.context` her zaman `Activity` değildir; etkinliğe sarmalayıcılardan geçilerek ulaşılır.
*   **iOS'ta görünüm denetleyicisi olmayan bir ortam.** Testlerde ve önizlemelerde `LocalUIViewController` yoktur.
*   **Renkli çubuklar.** Kenardan kenara çizmeyen, çubuklarını kendi rengiyle boyayan bir uygulamada simgeler o renge göre seçilmelidir, temanın zeminine göre değil: `followSystemBars = false`.

## 7. Doğrulama {#verification}

### Testler

*   `ui/foundation`'ın Android host testleri (`SystemBarsOnAndroidTest`): simgeler temanın koyuluğunu izler ve tema değişince değişir; `followSystemBars = false` uygulamanın ayarına dokunmaz; içteki bir tema çubukları değiştirmez.

### Simülatörde görülenler

*   iPhone 18 Pro Max simülatöründe (iOS 27.0), SwiftUI'nin içinde barındırılan demo'da: sistem koyuyken "Açık" seçilince uygulama da durum çubuğu da açık oldu; "Sistem"e dönünce ikisi yeniden koyu oldu. Ekran döndürülüp uygulama arka plana alındıktan sonra da, uygulama sistemi izlerken sistemin modu değiştiğinde de doğru kaldı: görünüm denetleyicisinin stilini ezmek, Compose'un `isSystemInDarkTheme()`'inin okuduğu sistemi değiştirmedi.

### Elle bozulan davranışlar

*   Temanın ayarı kaldırıldığında üç testten ikisi düşer: simgeler sistemin modunda kalır.

### Doğrulanmayanlar

*   iOS'taki davranışın otomatik bir testi yoktur (testler bir uygulama olmadan koşar); yukarıdaki gibi simülatörde denendi, gerçek bir iPhone'da denenmedi. Klavyenin ve UIKit kontrollerinin rengine ayrıca bakılmadı.
*   Mobil tarayıcıların adres çubuğunun rengi (`<meta name="theme-color">`) bu kapsamın dışındadır.

## 8. Bu depoda nerede {#repository toc="8. Depoda"}

*   `ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/platform/SystemBars.kt` ve platform karşılıkları.
*   `ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/theme/PantrixTheme.kt`: en dıştaki tema ve `followSystemBars`.
*   `ui/foundation/src/androidHostTest/kotlin/com/pantrixui/foundation/platform/SystemBarsOnAndroidTest.kt`.
