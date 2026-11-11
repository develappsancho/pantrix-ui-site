---
title: Çalışma anı tanılaması
description: Bazı kusurların neden yalnızca çalışan bir ekranda göründüğü, bunları yakalamak için kütüphanenin ne verdiği, bir uygulamanın tanılamayı yalnızca kendi debug derlemesinde nasıl açacağı ve bunun nasıl sınandığı. Sorun Pantrix'e özgü değildir: otomasyon etiketi, çalışma anında seçilen renkler ya da dışarıdan gelen metinler kullanan her Compose Multiplatform projesinde aynı şekilde görülür.
---

Compose hakkında burada söylenen her şey Compose Multiplatform 1.12.2'nin kaynaklarında okunmuş ya da bu depoda koşan bir testte görülmüştür. Kotlin ve Gradle eklentileri hakkında söylenenler Kotlin 2.4.21'in ve Compose Gradle eklentisi 1.12.2'nin kaynaklarında okunmuştur. Hangisinin nerede görüldüğü 7. ve 8. bölümlerde yazılıdır; doğrulanamayanlar 8. bölümde ayrıca sayılır.

## 1. Sorun {#problem}

:::aside diagnostics
### Belirti

*   Bir test `onNodeWithTag("Button_save")` ile bir düğme arar ve "Expected exactly '1' node but found '2' nodes" der. Aynı anahtar iki yerde verilmiştir, ama ikisi ancak aynı anda ekrandayken çakışır: bir listenin satırı ile açılan ayrıntısı, bir katalog sayfasındaki iki örnek, bir geçişin iki sayfası.
*   Bir markanın rengi sunucudan, bir temanın rengi kullanıcının seçiminden gelir. Ekran görüntüsü testi kendi renkleriyle çizer; çalışma anındaki rengin üstündeki yazının okunup okunmadığını hiçbir test görmez.
*   Bir onay kutusu, onu tutan satır daha az yer verince sessizce küçülür. Ekran görüntüsünde kutu aynıdır; parmağın basabileceği alan daralmıştır.
*   Bir ikon düğmesinin `contentDescription`'ı bir kaynaktan boş gelir. Ekran okuyucu düğmeye gelince hiçbir şey söylemez.
*   Uygulama Almanca açılır, ama kütüphanenin Almanca sözleri yoktur: bileşenlerin kendi sözleri ("Kapat", "Temizle") İngilizce kalır.
*   Bir ekranın kökü ve içindeki bir alan aynı kısayolu dinler. Tuşa basılınca dıştaki çalışır; içteki hiçbir zaman çalışmaz ve kimse fark etmez.
:::

### Neden

*   **Lint kaynağa bakar.** Bir rengin çalışma anındaki değerini, bir kaynaktan gelen adı, aynı anda kaç düğümün aynı etiketi taşıdığını bilemez.
*   **Ekran görüntüsü yalnızca çizileni görür.** Semantik adı, otomasyon etiketini ve basılabilen alanı görmez.
*   **Compose tekrara bakmaz.** `Modifier.testTag` yalnızca semantiğe bir özellik ekler (`TestTagElement`, `SemanticsModifierNode`); iki düğüm aynı etiketi taşıyabilir. Test bulucuları ise ağaca bağlı ve devre dışı olmayan her düğümü sayar, yerleşmemiş olsa da, bütün köklerde (`SemanticsOwner.getAllSemanticsNodes`). Devre dışı bırakılan bir düğümün değiştiricileri ağaçtan ayrılır (`onDetach`), yeniden kullanılınca yeniden bağlanır (`onAttach`): kaydırılan bir listenin geri dönüştürdüğü satırlar sayılmaz.
*   **Bazı tekrarlar geçicidir.** Bir sayfa geçişi birkaç kare boyunca iki sayfayı birlikte gösterir; açılan bir satır içindekini bir an sıkıştırır. Her kareye bakan bir denetim bunları da kusur sayar.

## 2. Çözüm: dört parça {#fix}

:::table
| # | Parça | Ne sağlar |
| :--- | :--- | :--- |
| 1 | **Denetim bileşenin içinde.** Her bileşen ne çizdiğini, adını, etiketini ve kısayolunu bildiği yerde söyler | Ölçülen, çalışma anındaki değerdir: verilen renk, gelen ad, ekrandaki düğümler |
| 2 | **Kapalıyken bedava.** Denetimler ilk satırlarında döner | Yayın derlemesi neredeyse hiçbir şey ödemez; açıp kapamak için ekran yeniden kurulmaz |
| 3 | **Alıcı.** Bulunan bir yere verilir: günlüğe, bir toplayıcıya, bir teste | Aynı denetim testte de, debug derlemesinde de çalışır |
| 4 | **Zamanlama.** Yerleşimin bulduğu yarım saniye, yinelenen bir etiket bir buçuk saniye sürerse bildirilir; her bulgu sürdüğü sürece bir kez | Geçişler ve açılan satırlar yanlış alarm vermez; günlük aynı bulguyla dolmaz |
:::

Birinci parça olmadan ikincisi anlamsızdır: bileşenin dışından bakan bir denetim, bir düğmenin rengini ya da adını bilmez. Üçüncü parça olmadan ilk ikisi yalnızca bir testte işe yarar; dördüncüsü olmadan bir geçiş her seferinde bulgu üretir.

## 3. Pantrix ile {#with}

Kütüphane dört parçayı da verir (`com.pantrixui.foundation.diagnostics`; inspector'ı `com.pantrixui.components.overlay.inspector`):

:::table
| API | Ne yapar |
| :--- | :--- |
| `PantrixTheme(diagnostics = …)` | Temanın tanılaması: içindeki bileşenlerin denetimleri bulduklarını buraya verir. Varsayılanı `PantrixDiagnostics.Off`'tur. İçteki bir tema verilmezse dıştakinin alıcısını kullanır |
| `PantrixDiagnostics.Off` | Denetimleri kapatır: hiçbir şey aranmaz, hiçbir şey bildirilmez |
| `PantrixDiagnostics.Log` | Her bulguyu platformun günlüğüne yazar: Android'de Logcat'e `Pantrix` etiketiyle bir uyarı (`Log.w`), iOS'ta birleşik günlüğe (`NSLog`, Xcode'un konsolu), tarayıcıda `console.warn`, JVM'de standart hata |
| `PantrixFindings(then = …)` | Bulguları saklar (`all`, `clear()`, `boundsOf(finding)`), her birini `then`'e de verir. `all` bir kompozisyonda okunursa büyüdükçe izlenir |
| `PantrixFinding` | Bir bulgu: `check`, `component`, `testTag`, `screen` (`TestScope` zinciri), `message` (İngilizce) ve ölçülen `values` |
| `PantrixCheck` | Yedi denetim (3.1) |
| `LocalPantrixDiagnostics` | O noktadaki alıcı |
| `PantrixDiagnosticsInspector(findings) { … }` | Uygulamanın üstünde bulguların sayısını gösteren bir düğme; basınca bulguların listesi; listeden seçilen bulgunun düğümü ekrana bir sonraki basışa kadar çerçevelenir. Bulgu yokken bir şey göstermez |
| `Modifier.checkName(component, name)` | Uygulamanın kendi bileşeni için `MissingName` |
| `Modifier.checkContrast(component) { pairs }`, `ContrastPair`, `ContrastNeed` | Uygulamanın kendi bileşeni için `LowContrast` |
| `Modifier.checkGraphCycles(component, cycles)` | Uygulamanın kendi grafiği için `GraphCycle` |
| `contrastRatio(foreground, background)` | İki rengin WCAG 2.2 oranı |
:::

### 3.1. Yedi denetim

:::table
| Denetim | Ne zaman bildirilir | Nereden gelir | Nasıl düzelir |
| :--- | :--- | :--- | :--- |
| `DuplicateTestTag` | Aynı penceredeki iki düğüm aynı etiketi bir buçuk saniye taşırsa: bir sayfa geçişinden uzun | `Modifier.pantrixTestTag`: bütün Pantrix bileşenleri ve onu kullanan uygulama kodu | Her birine kendi `tagKey`'i, ya da her birini kendi `TestScope`'una koymak |
| `LowContrast` | Temanın 34 rol çifti, renkleri her değiştiğinde bir kez; bileşenlerin yazı ve işaret çiftleri, bileşen ve renkleri için bir kez, kaç tanesi gösterirse göstersin | `PantrixTheme`; kendi `…Colors` sınıfı olan 39 bileşen; `checkContrast` | Yazı 4,5:1; büyük yazı ve bir kontrolün işareti 3:1 |
| `SmallTouchTarget` | Bir kontrol platformun dokunma hedefinden az yere yarım saniye yerleşirse | `Modifier.minTouchTarget`: Pantrix'in basılan kontrolleri | Üstündekine yer verdirmek |
| `MissingName` | Ekran okuyucunun söyleyeceği ad bir buçuk saniye boş kalırsa: bir an geç gelen bir ad, uygulamanın kaynaklarından okunan sözcüklerin tarayıcıda geldiği gibi, bildirilmez. `null` bildirilmez: süs olan bir resim ya da yazısıyla adlanan bir kontrol adını vermez | `contentDescription`, `label` ya da `title` alan bileşenler; `checkName` | Uygulamanın dilinde, ne olduğunu ya da ne yaptığını söyleyen bir ad |
| `NoStringsForLanguage` | Temanın dili İngilizce değilken sözleri İngilizceyse | `PantrixTheme` | `PantrixTheme(strings = …)` ile o dilin sözleri |
| `ShadowedShortcut` | Bir kısayolu çevresindeki bir düğüm de dinliyorsa: içteki hiç çalışmaz | `Modifier.onShortcut` | İçtekini kaldırmak ya da başka bir tuşa almak |
| `GraphCycle` | Bir grafiğin bağları bir döngüye giriyorsa: döngüyü kapatan bağ geriye çizilir ve hiçbir şeyi yerleştirmez | `PantrixGraph`; `checkGraphCycles` | Bağı bırakmak ya da ters çevirmek; döngü amaçlanmışsa bulgu yerinde kalır |
:::

Temanın 34 çifti: altı rolün (`primary`, `success`, `info`, `warning`, `danger`, `neutral`) her biri için `onColor` `color` üstünde, `onContainer` `container` üstünde, `color` `background` ve `surface` üstünde; sayfanın `onSurface`, `textPrimary` ve `textSecondary` yazıları ile `borderStrong` ve `focusRing` işaretleri, `background` ve `surface` üstünde.

**Dolgusuz zemin.** Dolgusu olmayan bir düğmenin zemini gibi, altındakini gösteren bir zemin, bileşen temanın kendi zeminlerinden birinde duruyorsa temanın `background`'u ve `surface`'ı üstünde ölçülür ve düşük olanı sayılır. Bileşenin durduğu yer kendi içerik rengini veren başka renkte bir dolguysa (`ProvidePantrixContentColor`, koyu bir kartın yaptığı gibi) böyle bir çift ölçülmez: altında ne olduğu bilinmez.

### 3.2. Yalnızca debug derlemesinde

Pantrix hangi tür bir derlemede çalıştığını bilmez; bunu uygulama söyler. En sade yol, her platformun giriş noktasının ortak koda bir `debug` vermesidir:

```kotlin
// commonMain
@Composable
fun App(debug: Boolean) {
    val findings = remember { PantrixFindings(then = PantrixDiagnostics.Log) }
    PantrixTheme(diagnostics = if (debug) findings else PantrixDiagnostics.Off) {
        if (debug) PantrixDiagnosticsInspector(findings) { Screens() } else Screens()
    }
}
```

`debug` bir derlemenin ömrü boyunca değişmez; bu yüzden `if` burada güvenlidir. Çalışırken açılıp kapanan bir ayar için 7. bölümdeki ilk tuzağa bakın.

Her platform debug derlemesini kendi yolundan bilir:

:::table
| Platform | Nereden bilinir | Giriş noktasında |
| :--- | :--- | :--- |
| Android | Uygulamanın `debuggable` bayrağından: debug derleme türü onu açar | `val debug = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0`, `Activity`'nin `onCreate`'inde. Android Gradle eklentisi 8'den beri `BuildConfig` kendiliğinden üretilmez (`buildFeatures { buildConfig = true }`); bu bayrak ona bağlı değildir. Paylaşılan kod bir kütüphane modülündeyse bayrağı uygulama modülü okur ve ona verir |
| iOS | Kotlin/Native ikilisinin derleme türünden: Xcode'un `Debug` yapılandırması debug framework'ünü derletir | `@OptIn(ExperimentalNativeApi::class) val debug = Platform.isDebugBinary` (`kotlin.native.Platform`), `MainViewController`'da. `embedAndSignAppleFrameworkForXcode` türü Xcode'un `CONFIGURATION`'ından okur; `Debug` ve `Release` dışındaki bir yapılandırmada `KOTLIN_FRAMEWORK_BUILD_TYPE`'tan |
| Masaüstü | JVM'nin bir derleme türü yoktur: geliştirirken koşan `run` görevi bir sistem özelliği verir | `build.gradle.kts`: `tasks.named<JavaExec>("run") { systemProperty("app.debug", "true") }`; `main`'de `System.getProperty("app.debug") == "true"`. Paketlenen uygulama (`packageDmg`, `packageMsi`) `run` ile başlamaz ve özelliği almaz |
| Web | Tarayıcıdaki kodun okuyabileceği bir derleme türü yoktur | Sayfanın adresi: `window.location.search.contains("debug")` (`kotlinx.browser`). Adresi bilen herkes açabilir; tanılama yalnızca konsola yazdığı için bu çoğu zaman yeter. Yetmezse derleme bir sabit yazar |
:::

### 3.3. Bir testte

```kotlin
@Test
fun the_checkout_screen_has_no_findings() = runComposeUiTest {
    val findings = PantrixFindings()
    setContent { PantrixTheme(diagnostics = findings) { CheckoutScreen() } }
    // Yerleşimin bulduğu yarım saniye, yinelenen bir etiket bir buçuk saniye sürünce bildirilir.
    mainClock.advanceTimeBy(2_000)
    assertEquals(emptyList(), findings.all)
}
```

Bir bulgunun `message`'ı testin hata iletisinde yeter: neyin yanlış olduğunu ve nasıl düzeleceğini İngilizce söyler. `values` ölçüleni taşır (`LowContrast`'ta `ratio`, `needed`, `foreground`, `background` ve ölçüldüyse `ground`; `SmallTouchTarget`'ta `width`, `height`, `needed`).

### 3.4. Kendi bileşenin

Uygulamanın kendi bileşeni de denetimlere katılır. Adı ve renkleri bileşenin kendisi bilir:

```kotlin
@Composable
fun Swatch(name: String, fill: Color, label: Color) {
    Column(
        modifier = Modifier
            .checkName("Swatch", name)
            .checkContrast("Swatch") { listOf(ContrastPair("label on fill", label, fill, ContrastNeed.Text)) }
            .background(fill)
            .padding(12.dp)
    ) {
        PantrixText(text = name, color = label)
    }
}
```

`checkContrast`'ın çiftleri yalnızca tanılama açıkken istenir; tanılama ya da temanın renkleri değişince yeniden. Devre dışı bir bileşen, kimsenin okuması beklenmediği için çift vermez.

### 3.5. Ayrı bir alıcısı olan bölüm

İçteki bir tema kendi alıcısını alabilir: `PantrixTheme(diagnostics = own) { … }`. O bölümün bulguları yalnızca `own`'a gider, dıştakine gitmez. Bu depodaki demo, "Tanılama" sayfasının örneklerini böyle gösterir: her örneğin kusuru kendi alıcısında toplanır ve uygulamanın kendi inspector'ına düşmez.

## 4. Pantrix olmadan {#without}

Bileşenin içinden yapılan denetimler (renk, ad, dokunma hedefi) bir kütüphane ya da uygulamanın kendi bileşen katmanı ister: dışarıdan bakan bir kod, bir düğmenin hangi rengi hangisinin üstüne çizdiğini bilmez. Aşağıdaki iki parça ise her projeye olduğu gibi kopyalanabilir.

### 4.1. Kontrast oranı (`commonMain`)

```kotlin
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.luminance

/** WCAG 2.2: yazı 4.5 ve üstünde, büyük yazı ve bir kontrolün işareti 3 ve üstünde okunur. */
fun contrastRatio(foreground: Color, background: Color): Float {
    require(foreground.isSpecified && background.isSpecified) { "An unspecified colour has no contrast." }
    val drawn = if (foreground.alpha < 1f) foreground.compositeOver(background) else foreground
    val one = drawn.luminance() + 0.05f
    val other = background.luminance() + 0.05f
    return maxOf(one, other) / minOf(one, other)
}
```

Bu, kütüphanenin `contrastRatio`'sunun gövdesidir. Arkasını gösteren bir ön renk çizildiği gibi, zeminin üstüne konarak ölçülür; zemin verildiği gibi ölçülür.

### 4.2. Bir testte tekrar eden etiketler (`commonTest`)

```kotlin
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider

/** İki ya da daha çok düğümün taşıdığı etiketler ve her birini kaç düğümün taşıdığı. */
fun SemanticsNodeInteractionsProvider.repeatedTestTags(): Map<String, Int> =
    onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.TestTag), useUnmergedTree = true)
        .fetchSemanticsNodes()
        .groupingBy { it.config[SemanticsProperties.TestTag] }
        .eachCount()
        .filterValues { it > 1 }
```

Bir ekran testinin sonunda `assertEquals(emptyMap(), repeatedTestTags())`. Test bulucularının saydığını sayar: yerleşmemiş düğümleri de, geri dönüştürülen satırları değil. Testin baktığı an tek bir andır; bir geçişin ortasında bakarsa geçici bir tekrarı da bulur.

## 5. Denetimlerin yapmadıkları {#limits toc="5. Yapmadıkları"}

*   **Compose'un kendi `Modifier.testTag`'i sayılmaz.** Yalnızca `pantrixTestTag`'in koyduğu etiketler kayda girer.
*   **`minTouchTarget` kullanmayan bir basılabilir alan ölçülmez.** Uygulamanın kendi `clickable`'ı `SmallTouchTarget` bulmaz.
*   **Bir grafiğin kendi seri renkleri ölçülmez.** Grafiklerde ölçülenler stilin ve renk sınıfının yazı ve işaret çiftleridir.
*   **iOS'un anahtar başparmağı ölçülmez.** iOS stilinde (`thumbAlwaysLight`) başparmak hep açık renklidir ve izinden gölgesiyle ayrılır; renk oranı orada bir şey söylemez.
*   **Resimler, gradyanlar ve dolgulu bir zemindeki saydam renkler ölçülmez.** Altlarında ne olduğu bilinmez.
*   **Kat yerine düşen bir overlay için denetim yoktur.** Pantrix'in overlay'leri kat yerinin bir yanında kalır (`ModalRoom`); bu denetim Pantrix'in kendi overlay'lerinde hiç tetiklenmezdi.
*   **Seviye yoktur.** Her bulgunun bir türü vardır; hangisinin yayını durduracağına uygulama karar verir.
*   **İletiler yalnızca İngilizcedir.** Onları uygulamanın kullanıcısı değil bir geliştirici okur. Inspector'ın kendi sözleri (`diagnostics`, `findingCount`, `notOnScreen`) `PantrixStrings`'te Türkçe, İngilizce ve Arapçadır.

## 6. Test etmek {#testing}

*   **Alıcı:** her test kendi `PantrixFindings`'ini temaya verir ve `all`'a bakar. `PantrixDiagnostics.Log` bir testte gereksizdir.
*   **Saat:** yerleşimin bulguları yarım saniye, yinelenen bir etiket bir buçuk saniye sürünce bildirilir. Saati kendiliğinden ilerleyen bir testte `waitForIdle()` yeter; `mainClock.autoAdvance = false` olan bir testte saat elle ilerletilir: yerleşimin bulguları için en az 500 ms, yinelenen etiket için en az 1500 ms. Kısa süren bir tekrarın bildirilmediği de böyle sınanır: tekrar süresi dolmadan kaldırılır.
*   **Bütün ekranlar:** bu depodaki demo, `DemoPagesTest`'te her sayfayı iki dilde ve uygulamanın kendi kısayolunun altında çizer, bir örneğin koduna ve API sekmesine geçer ve sayfa başına bulguları toplar; hiçbir sayfada bulgu yoktur. `DiagnosticsSamplesTest`, iki tanılama sayfasının her örneğinin yalnızca kendi bulgularını gösterdiğine bakar: örneklerin kendi alıcıları vardır ve bulguları uygulamanınkine düşmez.
*   **Yer:** `boundsOf(finding)` bulgunun düğümünün penceredeki yeridir; inspector'ın çerçevesi ondan çizilir. Düğümü olmayan bir bulgu (temanın renkleri, dil) `null` verir.

## 7. Tuzaklar {#pitfalls}

:::table
| Tuzak | Ne olur | Ne yapılır | Nerede görüldü |
| :--- | :--- | :--- | :--- |
| **Bir koşulla sarılan inspector** | `if (on) PantrixDiagnosticsInspector(findings) { content() } else content()`, `on` çalışırken değişirse içeriği kompozisyonda başka bir yere taşır. İçerikteki her `remember` ve `rememberSaveable` baştan kurulur: demo'da "Ayarlar"da tanılamayı kapatmak uygulamayı genel bakışa döndürüyordu | Inspector'ı hep yerinde bırak: tanılama kapalıyken bulgu yoktur ve inspector bir şey göstermez | Demo'yu yazarken; test: `DemoShellTest.the_diagnostics_switch_leaves_the_app_where_it_is` (koşul geri konunca kırmızı) |
| **Kapanınca kalan bulgular** | Alıcı `Off`'a dönünce yeni bulgu gelmez, ama `PantrixFindings` eskilerini saklar: inspector eski sayıyı gösterir | Tanılama kapanınca `clear()` | Test: `SettingsPageTest.turned_off_the_diagnostics_forget_what_they_found` |
| **Bir sayfanın örnekleri** | Bir katalog sayfasındaki örnekler aynı anda ekrandadır: iki örnekte aynı anahtar, gerçek bir tekrardır | Örneklerin anahtarlarını ayır; bir yardımcıyı iki örnek kullanıyorsa ona bir anahtar ver | Demo: beş sayfada on dört tekrar bulundu ve düzeltildi |
| **Bir geçişin iki sayfası** | Geçiş, sürdüğü boyunca iki sayfayı birlikte bağlı tutar: Navigation 3'ün varsayılanlarıyla Android'de 700 ms, iOS'ta 500 ms. İkisinde aynı etiket varsa her karede bakan bir denetim bunu tekrar sayar; yarım saniye bekleyen de sayıyordu (0.1.0-alpha01'in sürüm APK'sında ve iOS uygulamasında görüldü) | Bir buçuk saniye bekleyen denetim bunu bildirmez | Kaynak: `SemanticsOwner`, Navigation 3'ün `NavDisplay`'i; test: `DuplicateTestTagTest.a_tag_two_pages_carry_while_one_gives_way_to_the_other_is_not_reported` |
| **Geri dönüştürülen satırlar** | Kaydırılan bir listenin devre dışı bıraktığı satırların değiştiricileri ağaçtan ayrılır; yeniden kullanılınca bağlanır | Bir şey yapılmaz: sayılmazlar | Kaynak; test: `DuplicateTestTagTest.the_rows_of_a_list_that_scrolls_and_reuses_them_are_no_duplicates` |
| **Dolgulu zeminde dolgusuz bileşen** | Beyaz yazılı, dolgusuz bir düğme koyu bir kartın üstünde okunur; sayfanın açık zemininde ölçülseydi okunmaz sayılırdı | Dolgunun içerik rengini ver (`ProvidePantrixContentColor`). Pantrix'in yüzeyi ve kartı bunu kendileri yapar: başka renkte bir dolguda onun içerik rengini verirler, temanın zemininde hiçbirini. İçerik rengi verilen yerde çift ölçülmez | Test: `ComponentDiagnosticsTest.a_button_with_no_fill_on_a_fill_of_another_colour_is_not_measured_on_the_page` |
| **İçteki temanın alıcısı** | İçteki bir tema `diagnostics` alırsa bulguları dıştakine gitmez | Bütün uygulamanın bulguları için içteki temalara alıcı verme | Test: `PantrixDiagnosticsTest.a_theme_inside_another_reports_where_that_one_does_unless_it_is_told_otherwise` |
| **Kendi alıcısı olan bölümlerin tekrarları** | Kendi alıcısı olan iki bölüm aynı etiketi taşırsa tekrar o bölümlerin alıcılarına bildirilir, dıştakine değil: uygulamanın kendi bulgularına bakan bir tarama onu görmez. Demo'nun "Diagnostics Inspector" sayfasında iki örnek aynı ekranı aynı anahtarlarla gösteriyordu ve her inspector iki yerine dört bulgu sayıyordu | Kendi alıcısı olan bölümlerin bulgularına da bak; aynı yardımcıyı kullanan iki bölümün anahtarlarını ayır | Web'de demo'ya bakarken; test: `DiagnosticsSamplesTest.each_inspector_counts_the_two_findings_of_its_own_screen` |
| **Durum listesinin eşitliği** | Compose'un `SnapshotStateList`'i yapısal eşitlik tanımaz. `assertEquals(emptyList(), list)` JVM'de geçer, çünkü JUnit `expected.equals(actual)` sorar ve boş liste her boş listeye eşittir; iOS'ta ve web'de `kotlin.test` `actual == expected` sorar ve iki boş liste eşit çıkmaz | Dışarıya bir durum listesi verilecekse okunduğu andaki kopyası verilir: `PantrixFindings.all` öyledir | iOS ve JS test koşusunda: `all` listenin kendisini verirken 21 test kırmızıydı |
| **NSLog'un değişken argümanları** | Kotlin/Native bir Kotlin metnini NSLog'un `...` argümanına C dizgisi olarak verir; `NSLog("%@", line)` onu bir nesne sanar ve uygulama `EXC_BAD_ACCESS` ile çöker | Satırı biçimin kendisi yap, yüzde işaretlerini ikile: `NSLog(line.replace("%", "%%"))` | iOS simülatöründe demo'nun "Tanılama" sayfası açılınca; test: `PantrixDiagnosticsOnIosTest` (eski hâlinde test süreci signal 11 ile düşer) |
| **Elle ilerleyen saat** | `mainClock.autoAdvance = false` bir testte yerleşimin bulguları hiç gelmez | Saati en az 500 ms, yinelenen etiket için en az 1500 ms ilerlet | Testler bu depoda böyle yazıldı |
| **Uygulamanın kendi kısayolu** | Uygulamanın kökü bir kısayol dinliyorsa (demo'da ⌘K komut paletini açar), aynı kısayolu dinleyen her ekran onun gölgesinde kalır. Demo'nun "Gölgede kalan kısayol" örneği ⌘K dinlediği için ikinci bir bulgu gösteriyordu: ekranın kökü de uygulamanınkinin gölgesindeydi | Ekranları testte uygulamanın kökünün altında çiz; örnekte uygulamanın kullanmadığı bir tuş (⌘F) | Web'de demo'ya bakarken; test: `DiagnosticsSamplesTest` |
| **Masaüstünün hedefi** | Fareyle kullanılan masaüstünde ve web'de dokunma hedefi 32dp'dir: 32dp'lik bir kutu orada sıkıştırmaz, telefonda sıkıştırır. Demo'nun örneği masaüstünde ve web'de bulgu göstermiyordu | Bir sıkışmayı her platformda göstermek için hedeflerin en küçüğünden alçak bir kutu (24dp) | Web'de demo'ya bakarken; test: `DiagnosticsSamplesTest` |
| **Günlüğün yeri** | Android'de bulgular `Pantrix` etiketiyle Logcat'tedir; iOS'ta Xcode'un konsolunda; tarayıcıda konsolda bir uyarı olarak; masaüstünde standart hatada | Android'de `adb logcat -s Pantrix` | Kaynak: dört `PantrixDiagnostics.*.kt` |
:::

## 8. Doğrulama {#verification}

### Testler

:::table
| Test | Nerede koşar | Neye bakar |
| :--- | :--- | :--- |
| `ui/foundation`, `PantrixDiagnosticsTest` | JVM, iOS simülatörü, JS, Wasm | Tema alıcı verilmedikçe hiçbir şey aramaz; içteki tema dıştakinin alıcısını kullanır; bulgular bir kez ve geldikleri sırayla saklanır; şüphe bir sonraki karede, yerleşiminki yarım saniye, yinelenen etiket bir buçuk saniye sonra bildirilir; süresi dolmadan kalkan bildirilmez; bir kez bildirilir, geri gelirse yeniden; aynı şüphenin sahipleri tek şüphedir; masa yalnızca bekleyen bir şey varken kare ister; günlük satırı |
| `ui/foundation`, `PantrixDiagnosticsOnIosTest` | iOS simülatörü | Yüzde işaretleri ve `%@` taşıyan bir bulgu birleşik günlüğe yazılır ve uygulama çökmez |
| `ui/foundation`, `ContrastRatioTest` | Aynı dört hedef | Beyaz üstünde siyah 21, kendi üstünde bir renk 1; hangisinin üstte çizildiği fark etmez; arkasını gösteren ön renk çizildiği gibi ölçülür; belirtilmemiş bir rengin oranı yoktur |
| `ui/foundation`, `DuplicateTestTagTest`, `SmallTouchTargetTest`, `LowContrastTest`, `MissingNameTest`, `NoStringsForLanguageTest`, `ShadowedShortcutTest`, `GraphCycleTest` | Aynı dört hedef | Her denetim: bulduğu, bulmadığı, kapalıyken hiçbir şey ölçmediği, sonradan açılınca bulduğu |
| `ui/components`, `ComponentDiagnosticsTest`, `DiagnosticsInspectorTest` | JVM, iOS simülatörü, JS, Wasm | Pantrix'in kendi renkleri iki modda da bulgu değildir; bir bileşenin bulgusu etiketiyle gelir; dolgulu zeminde dolgusuz bileşen; adlar, tekrarlar, sıkışan hedef; inspector'ın düğmesi, listesi, çerçevesi ve temizlemesi |
| `ui/components`, `ComponentScreenshotTest.diagnostics_inspector` | JVM, Android, iOS simülatörü | Inspector'ın açık ve koyu temadaki görüntüsü |
| `app/shared`, `DemoPagesTest`, `DiagnosticsSamplesTest`, `DemoShellTest`, `SettingsPageTest`, `DemoSettingsTest` | JVM | Her sayfa iki dilde sıfır bulguyla; iki tanılama sayfasının her örneği yalnızca kendi bulgularını gösterir; ayar açılıp kapanınca uygulama yerinde kalır; kapanınca bulgular silinir; ayar saklanır ve eski kayıtta açıktır |
:::

### Elle bozulan davranışlar

Her davranış bir kez elle bozuldu ve hangi testin kırmızı olduğuna bakıldı; sonra geri alındı.

*   **Demo:** inspector'ın bir koşulla sarılması (`DemoShellTest`'in testi kırmızı); kapanınca bulguların silinmemesi (`SettingsPageTest`'in testi kırmızı); "Tanılama" sayfasının iki örneğinin eski hâli, ⌘K ve 32dp'lik kutu (`DiagnosticsSamplesTest` kırmızı: sıkışma sıfır, gölgelenen kısayol iki kez); "Diagnostics Inspector" sayfasının iki örneğinin aynı anahtarları paylaşması (`DiagnosticsSamplesTest` kırmızı: "Bulgular: 4").
*   **Zamanlama:** yarım saniyenin sıfıra çekilmesi. Altı test kırmızı oldu: kısa süren tekrar, sıkışma ve şüphe, yarım saniye sonra gelen bildirim, etiketini değiştiren düğüm ve masanın kare istemesi.
*   **Kapalıyken bedava:** `Off`'un da açık sayılması. Dört test kırmızı oldu: tema, etiket kaydı, sıkışan hedef ve kontrast.
*   **iOS'un günlüğü:** `NSLog("%@", line)`'a dönülmesi. `PantrixDiagnosticsOnIosTest`'in süreci signal 11 ile çöktü.
*   **Dolgusuz zemin:** altındakini gösteren zeminin her dolguda temanın zeminlerinde ölçülmesi. İlk hâliyle hiçbir test kırmızı olmadı: test, sayfada da okunan bir renk kullanıyordu. Test, sayfada okunmayan bir renkle yeniden yazıldı; bozma yeniden denenince kırmızı oldu.

### Doğrulanmayanlar

*   **Platformların debug yolları.** 3.2'deki tablonun kodu bu depoda derlenmedi ve çalıştırılmadı. `Platform.isDebugBinary` Kotlin/Native 2.4.21'in `Platform.kt`'sinde, Xcode yapılandırmasının okunuşu Kotlin Gradle eklentisi 2.4.21'in `XcodeEnvironment.kt`'sinde, `run` görevinin bir `JavaExec` olduğu Compose Gradle eklentisi 1.12.2'nin `configureJvmApplication.kt`'sinde okundu. Android'in `FLAG_DEBUGGABLE`'ı ve `BuildConfig`'in Android Gradle eklentisi 8'deki varsayılanı Android'in belgelerindendir.
*   **Gerçek günlükler.** iOS simülatöründe birleşik günlükte (`xcrun simctl spawn <udid> log stream`) ve tarayıcının konsolunda bir uyarı olarak satırın kendisi görüldü. Logcat'e ve masaüstünün standart hatasına bakılmadı; testler orada günlük satırının metnine bakar.
*   **Ekran okuyucu.** Inspector'ın ve listesinin sesli olarak nasıl okunduğu dinlenmedi.

## 9. Bu depoda nerede {#repository toc="9. Depoda"}

:::table
| Dosya | Ne |
| :--- | :--- |
| `ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/diagnostics/PantrixDiagnostics.kt` | `PantrixCheck`, `PantrixFinding`, `PantrixDiagnostics`, `LocalPantrixDiagnostics` |
| `…/diagnostics/PantrixFindings.kt` | `PantrixFindings` |
| `…/diagnostics/DiagnosticsDesk.kt` | Pencere başına masa: şüpheler, yarım saniye, bir kez bildirme |
| `…/diagnostics/DuplicateTestTag.kt`, `LowContrast.kt`, `MissingName.kt`, `NoStringsForLanguage.kt`, `GraphCycle.kt` | Denetimler; `checkContrast`, `ContrastPair`, `ContrastNeed`, `checkName`, `checkGraphCycles` |
| `…/a11y/TouchTarget.kt`, `…/keyboard/Shortcuts.kt`, `…/testing/TestTag.kt` | `SmallTouchTarget`, `ShadowedShortcut` ve etiketlerin kaydı |
| `…/token/color/ContrastRatio.kt` | `contrastRatio` |
| `ui/foundation/src/{android,ios,jvm,web}Main/…/diagnostics/PantrixDiagnostics.*.kt` | Platformların günlükleri |
| `ui/components/src/commonMain/kotlin/com/pantrixui/components/overlay/inspector/DiagnosticsInspector.kt` | `PantrixDiagnosticsInspector` |
| `ui/components/src/commonMain/kotlin/com/pantrixui/components/chart/common/ChartContrast.kt` | Grafiklerin çiftleri |
| `app/shared/src/commonMain/kotlin/com/pantrixui/cmp/demo/samples/DiagnosticsSamples.kt`, `DiagnosticsInspectorSamples.kt` | Demo'nun iki sayfası |
| `app/shared/src/commonMain/kotlin/com/pantrixui/cmp/demo/App.kt`, `theme/Theme.kt`, `shell/SettingsPage.kt` | Demo'nun kendi tanılaması, inspector'ı ve ayarı |
:::

## 10. Açık kalanlar {#open}

*   Bir grafiğin kendi seri renkleri ölçülmez (bkz. 5. bölüm).
*   Kat yerine düşen bir overlay için bir denetim yoktur; Pantrix'in kendi overlay'lerinde tetiklenmezdi.
*   Gerçek cihazlarda ve tarayıcıda yalnızca demo'nun kendisine bakıldı (bkz. 8. bölüm).
