---
title: Bir ekranı test etmek
description: Compose Multiplatform'da yazılmış bir ekranın testinin neden makineden makineye değiştiği, bir bileşenin otomasyon etiketiyle nasıl bulunduğu, platform hizmetlerinin yerine neyin konduğu ve aynı etiketlerin cihaz otomasyonuna (Maestro) nasıl ulaştığı. Sorun Pantrix'e özgü değildir: otomasyon etiketi, tema, pano, dosya seçici ya da saat kullanan her Compose Multiplatform projesinde aynı şekilde görülür.
---

Compose hakkında söylenen her şey Compose Multiplatform 1.12.2'nin ve compose ui-test 1.12.2'nin kaynaklarında okunmuş ya da bu depoda koşan bir testte görülmüştür; Maestro hakkında söylenenler Maestro 2.10.0 ile Android emülatörlerinde (API 34 tablet, Pixel 7 Pro API 33, Pixel 9 Pro Fold API 35), iPhone 18 Pro Max simülatöründe (iOS 27.0) ve Maestro'nun Chromium'unda görülmüştür. Hangisinin nerede görüldüğü 7. ve 8. bölümlerde yazılıdır.

## 1. Sorun {#problem}

:::aside testing
### Belirti

*   Bir test bir düğmeyi `onNodeWithTag("Button_save draft")` diye arar ve bulamaz: anahtar etikete küçük harfle ve harf olmayanların yerine `_` ile girer (`Button_save_draft`). Düğme bir `TestScope`'un içine taşınınca etiketin başına bölümün adı gelir ve test yine kırılır.
*   Aynı ekran testi geliştiricinin Mac'inde geçer, CI'da düşer: makinenin dili, karanlık modu, yazı ölçeği, işletim sistemi (kısayol ⌘ ile mi Ctrl ile mi başlar) ve saat dilimi farklıdır.
*   "Kopyala"ya basan bir test, testi koşan kişinin panosundaki metnin üstüne yazar. Masaüstünde "Dosya seç"e basan bir test sistemin dosya penceresini açar ve orada bekler.
*   Bir tarih seçicinin testi ayın ilk günü ya da gece yarısı düşer. Bir grafiğin zaman ekseni CI'da (UTC) başka saatler söyler, geliştiricinin makinesinde (UTC+3) başka.
*   Erişilebilirliği sınayan bir test geçer, ama ekranda adı olmayan bir düğme vardır: tanılama onu yarım saniye sonra bildirir, test o an çoktan bitmiştir.
*   Android'de `runComposeUiTest` "Unable to resolve activity for Intent … ComponentActivity" der: Robolectric bir manifest'in bildirmediği etkinliği başlatmaz.
*   Maestro'nun bir akışı Android'de etiketi hiç bulamaz; iOS'ta bulur. Web'de ne etiketi ne yazıyı bulur.
:::

### Neden

*   **Etiket bir kuralla kurulan bir metindir.** Pantrix'te `Bölüm.Bileşen_anahtar`; anahtar `toTagSlug` ile küçültülüp kısaltılır, bir kontrolün parçası kontrolün etiketinin içinde durur (`SegmentButton_sort.Segment_newest`). Kuralı bilmeyen test, etiketi elle kurar ve kural değişince kırılır.
*   **Tema varsayılanlarını cihazdan okur.** Bir uygulama için doğru olan budur: sistemin dili, karanlık modu, yazı ölçeği, hareketi azaltma ayarı, işletim sistemi. Bir test için her biri makineye bağlı bir girdidir.
*   **Compose'un test panosu sistemindir.** `LocalClipboard` testte de makinenin panosunu verir; `rememberFilePicker()` platformun seçicisini açar.
*   **Saat cihazındır.** Bugün `Clock.System`'dan ve cihazın UTC farkından (`systemUtcOffset`) okunur.
*   **Bazı bulgular geç gelir.** Tanılama, bir yerleşimin bulduğunu (iki düğümde aynı etiket, sıkışmış dokunma hedefi) yarım saniye sürünce bildirir: bir geçiş birkaç kare boyunca iki sayfayı birlikte gösterir.
*   **Robolectric yalnızca bir manifest'in bildirdiği etkinliği başlatır.** androidx'in `ui-test-manifest`'i bu etkinliği bildirir ama androidx Compose'un sürümüyle gelir, Compose Multiplatform'unkiyle değil.
*   **Cihaz otomasyonu platformun erişilebilirlik ağacını okur.** Android'de etiket ancak `testTagsAsResourceId` açıkken `resource-id` olur; iOS'ta Compose `testTag`'i kendisi `accessibilityIdentifier` yapar. Web'de Compose bir tuvale çizer; anlam ağacının gizli bir DOM aynası vardır, ama bir shadow root'un içindedir (8. bölüm).

## 2. Çözüm: beş parça {#fix}

:::table
| # | Parça | Ne sağlar |
| :--- | :--- | :--- |
| 1 | **Etiketi bileşenin kuralıyla kurmak.** Test bileşenin adını ve anahtarını yazar; etiketi bileşenin kullandığı fonksiyon kurar | Kural tek yerdedir; anahtar ekrandaki kodla aynı yazılır |
| 2 | **Sabit bir tema.** Platform, dil, karanlık mod, yoğunluk, yazı ölçeği, hareket ve işletim sistemi testin seçimidir | Test her makinede aynı ekranı görür |
| 3 | **Test çiftleri.** Pano, dosya seçici, analitik, dokunsal geri bildirim ve saat testin kendisinindir | Makineye dokunulmaz; ekranın söylediği kaydedilir |
| 4 | **Doğrulamalar.** Tanılamanın bulguları, ad, dokunma hedefi, kısayol, yazı | Erişilebilirlik ve platform kuralları bir satırla sınanır |
| 5 | **Cihaz otomasyonu.** Aynı etiketler Android'e ve iOS'a ulaşır | Bir Maestro akışı da ekrandaki kodun anahtarını arar; dil değişince kırılmaz |
:::

Birinci parça olmadan ötekiler kırılgandır: elle yazılmış her etiket bir kural kopyasıdır. İkinci ve üçüncü olmadan bir test makineye bağlıdır.

## 3. Pantrix ile {#with}

Test modülü `ui/testing`, `com.pantrixui:testing` olarak yayınlanır; paketi `com.pantrixui.testing`. Yalnızca `:ui:foundation`'a ve compose ui-test'e bağlıdır: bileşenler adlarıyla, metin olarak verilir. Foundation'da üç açılım onunla birlikte gelir: `pantrixTestTagOf`, `LocalPantrixClock` ve `LocalFilePicker`.

### 3.1. Kurulum

```kotlin
kotlin {
    sourceSets {
        commonTest.dependencies {
            implementation("com.pantrixui:testing:0.1.0-SNAPSHOT")
            implementation(kotlin("test"))
        }
        // runComposeUiTest masaüstünde gerçekten çizer: test JVM'i bu makinenin Skiko'sunu ister.
        jvmTest.dependencies { implementation(compose.desktop.currentOs) }
    }
}
```

*   **Yalnızca test kaynak kümelerine.** Modülün Android tarafı `androidx.activity.ComponentActivity`'yi kendi manifest'inde bildirir; bir uygulamanın kendi derlemesine girerse bu etkinliği de taşır.
*   **Android'in host testleri.** Android'in host testleri ayrı bir kaynak ağacındaysa (`androidHostTest`), modül oraya ayrıca eklenir. Bağımlı olunca etkinlik testin manifest'ine birleşir; testin kendi `AndroidManifest.xml`'ine gerek kalmaz (`TestingOnAndroidTest` bu yüzden manifest'sizdir).
*   **Deneysel API.** `ComposeUiTest` ve `runComposeUiTest` compose ui-test'te `@ExperimentalTestApi`'dir. Onu alan yardımcılar (`onEachPlatform`, `assertNoFindings`) da öyledir; bulma yardımcıları değildir.

### 3.2. Bir bileşeni bulmak

:::table
| API | Ne yapar |
| :--- | :--- |
| `hasPantrixTag(component, tagKey?, scope?)` | Bir `SemanticsMatcher`: bileşenin bu anahtarla ve bu bölümde taşıdığı etiket |
| `onPantrixNode(component, tagKey?, scope?)` | O tek düğüm; yoksa ya da birden çoksa ona yapılan düşer |
| `onAllPantrixNodes(component, tagKey?, scope?)` | Hepsi, ağacın sırasıyla |
| `pantrixTags()` | Ekranın taşıdığı Pantrix etiketleri, ağacın sırasıyla |
| `pantrixTestTagOf(component, tagKey, scope)` (foundation) | Etiketin metni, ekran kurulmadan |
:::

```kotlin
onPantrixNode("Button", "Save draft").performClick()            // Button_save_draft
onPantrixNode("Button", "pay", scope = "Checkout.Payment")      // Checkout.Payment.Button_pay
onAllPantrixNodes("Button", scope = "Checkout").assertCountEquals(3)
onPantrixNode("Segment", "newest", scope = "SegmentButton_sort") // bir kontrolün parçası
```

*   **Anahtar ekrandaki kodla aynı yazılır.** Etiketi `pantrixTestTagOf` kurar; `rememberPantrixTestTag` da anahtarlı bir etiketi onunla kurar. `"Save draft"` ile `"save_draft"` aynı etikettir.
*   **Bölüm verilmezse etiketin bölümü yoktur.** Bir `TestScope`'taki bileşen, bölümü yazılarak bulunur; bölüm zincirin tamamıdır (`Checkout.Payment`, `Checkout` değil).
*   **Bir sayfa bir bölümdür.** `tagKey`'i verilen bir `PantrixPageScaffold` uygulamanın ona verdiklerini `TestScope(tagKey)` içine koyar: `orders` sayfasındaki `save` etiketli bir düğme `orders.Button_save` diye bulunur. Sayfanın kendi parçaları onun içinde değildir: bar'ı `TopBar_orders`, bir eylemi `PageScaffold_orders.Action_export`'tur. Bir sayfa yerini ötekine bırakırken, iOS'ta geri hareketi parmakla tutulurken de, iki sayfa birlikte durur: her sayfanın bar'ında duran aynı düğme iki kez bulunur, ve bu yarım saniyeden uzun sürerse tanılama onu `DuplicateTestTag` diye söyler. Sayfalara birer `tagKey` verilir, ya da her sayfada duran düğmeler durdukları sayfanın adıyla bir `TestScope`'a konur: demo `overview.Button_language` der.
*   **Anahtar verilmezse** bileşenin o bölümdeki bütün düğümleri bulunur; anahtarsız kurulmuş olanı da (`Button#1k3z9`). Adı daha uzun bir bileşen (`ButtonGroup`) ve bir kontrolün içindeki parça sayılmaz.
*   **Parça kontrolün etiketinin içindedir.** Anahtarı olan bir kontrolün parçası `Kontrol_anahtar.Parça_anahtar`'dır: `scope` kontrolün etiketidir. Anahtarı olmayan bir kontrolün parçası yalnızca kendi etiketini taşır.
*   **`pantrixTags()`** yeni bir ekranın testini ya da bir Maestro akışını yazarken neyin arandığını gösterir: `println(pantrixTags().joinToString("\n"))`. Uygulamanın kendi `Modifier.testTag("login")` gibi başka biçimde bir etiketi listede yoktur. Varsayılan olarak her düğümün ağacına bakar (`useUnmergedTree = true`): bir düğmenin birleştirdiği içindekiler de görünür.

### 3.3. Sabit bir tema

`PantrixTestTheme` bir ekranı her makinede aynı gösterir. Varsayılanlar:

:::table
| Parametre | Varsayılan | Neden |
| :--- | :--- | :--- |
| `platform` | `PlatformType.Android` | Dokunma hedefleri, odak halkaları ve platformun yolları sabit |
| `system` | Platformunki; masaüstünde ve web'de `Windows` | Kısayolların neyle başladığı (`⌘` mı `Ctrl` mi) makineye bağlı kalmaz |
| `dark` | `false` | |
| `locale` | `PantrixLocale.English` | Yazının yönü de onunla gelir |
| `density` | `PantrixDensity.Comfortable` | |
| `fontScale` | `1f` | Yalnızca yazı büyür; ekranın pikselleri aynı kalır |
| `window` | `null`: testin çizdiği pencere | Verilirse `PantrixWindowFrame` içinde: katlar ve katmanlar ona göre |
| hareket | Azaltılmış | Hiçbir şey yarı yolda yakalanmaz; bir snackbar bir karede girer ve çıkar |
| `findings` | `null`: tanılama açık, bulgular günlüğe | Verilirse bulgular orada toplanır (3.5) |
| `analytics`, `haptics` | `NoOp` | Kaydedici verilince ekranın söyledikleri tutulur (3.4) |
| `clipboard` | Temanın kendi `TestClipboard`'u | Makinenin panosu hiç yazılmaz |
| `filePicker` | Temanın kendi, hiç cevap vermeyen seçicisi | Sistemin hiçbir penceresi açılmaz |
| `clock` | `null`: cihazın saati | Tarih ya da saat gösteren her test kendi `TestClock`'unu vermeli |
:::

```kotlin
@OptIn(ExperimentalTestApi::class)
@Test
fun pay_is_told_and_the_screen_has_no_findings() = runComposeUiTest {
    val findings = PantrixFindings()
    val analytics = RecordingAnalytics()
    setContent { PantrixTestTheme(findings = findings, analytics = analytics) { CheckoutScreen() } }
    onPantrixNode("Button", "pay").performClick()
    assertEquals(listOf("click"), analytics.eventsOf("Button").map { it.action })
    assertNoFindings(findings)
}
```

**Her platformda:** `onEachPlatform(content, check)` ekranı dört platformda sırayla, testin tek koşusunda kurar. Web'de bir koşu, testin geri verdiği bir söz (promise) olduğundan, bir döngüden başlatılan koşuları kimse beklemez; onlarda düşen hiçbir testi düşürmez.

```kotlin
onEachPlatform({ platform -> PantrixTestTheme(platform = platform) { SaveButton() } }) { platform ->
    onPantrixNode("Button", "save").assertTouchTargetOf(platform)
}
```

### 3.4. Test çiftleri

:::table
| Çift | Ne tutar, nasıl cevap verir |
| :--- | :--- |
| `TestClipboard(traits)` | Ekranın kopyaladığını, sırayla. `text()` ve `texts()` platformun okuduğu gibi okur: Android'de `ClipData`'nın ilk öğesi, masaüstünde `Transferable`'ın metni, iOS'ta düz metin, web'de `text/plain`. Web'de okumak tarayıcıyı bekler (`suspend`). `traits` ekrana platformun huyu olarak söylenir: `ClipboardTraits(announcedBySystem = true)` Android 13, `ClipboardTraits(writable = false)` düz http'den sunulan bir sayfa. Compose'un metin alanının sorduğu platform panosu da (`nativeClipboard`) testindir: masaüstünde son kopyayı tutan kendi AWT panosu, iOS'ta genel pasteboard'dan ayrı, kendi adlı bir pasteboard |
| `RecordingAnalytics()` | Bileşenlerin bildirdiği olayları sırayla: `events`, `eventsOf("Button")` |
| `RecordingHaptics()` | Çalınan dokunsal geri bildirimleri sırayla: `played` |
| `TestFilePicker(files)` | Her seçime `files` ile cevap verir: yalnızca seçicinin gösterdiği türlerden, tek dosya istenmişse ilkiyle; hiçbiri uymuyorsa cevap vermez, seçiciyi kapatan bir kullanıcı gibi. `requests` her seçimin ne istediğini tutar |
| `TestClock(now, utcOffset)` | Testin kurduğu anı gösterir; yalnızca test ilerletince ilerler (`advanceBy`, `now = …`). `utcOffset` verilmezse UTC'nin saatidir |
| `TestFileSaver(result)` | Ekranın kaydettiği dosyaları sırayla tutar (`files`) ve her kaydetmeye `result` ile cevap verir: verilmezse kaydedildi. Tema her zaman kendisininkini verir, hiçbir kaydetme penceresi açılmaz; kaydedileni okuyan test kendisininkini temanın içinde, `LocalFileSaver` ile verir |
:::

Listelenen çiftler, `TestFileSaver` dışında, `PantrixTestTheme`'in parametreleridir. Tema kullanılmadan da verilebilirler, foundation'ın kendi yerlerinden:

```kotlin
// Bütün ekranın seçicisi: rememberFilePicker() ve PantrixFileDrop'un varsayılanı bunu alır.
CompositionLocalProvider(LocalFilePicker provides picker) { OfferScreen() }

// Ekranın kaydedicisi: rememberFileSaver() bunu verir. PantrixTestTheme'in içinde verilir.
CompositionLocalProvider(LocalFileSaver provides saver) { ThemeBuilderScreen() }

// Ekranın saati: rememberToday(), saat alanının seçicisi, elastic date picker ve grafiklerin anları bunu okur.
CompositionLocalProvider(LocalPantrixClock provides PantrixClock(clock) { 3.hours }) { BookingScreen() }
```

*   **`TestClock` ekranın okuduğu zamandır, testin kareleri değil.** Gece yarısını bekleyen bir ekran testin kendi saatini de bekler: `clock.advanceBy(1.minutes)` ile birlikte `mainClock.advanceTimeBy(60_000)`.
*   **Ekranın saati cihazınki değildir.** `PantrixDate.today()` ve `PantrixTime.now()` cihazın saatini okur, `LocalPantrixClock`'u okumaz; bir ekranda `rememberToday()` ya da `LocalPantrixClock.current.today()` ([tarih rehberi](/docs/guides/date-time)).

### 3.5. Doğrulamalar

:::table
| API | Ne doğrular |
| :--- | :--- |
| `assertNoFindings(findings)` | Tanılama hiçbir şey bulmadı: adsız kontrol, okunmayan renk, sıkışmış dokunma hedefi, iki düğümde bir etiket. Önce testin saatini yarım saniyenin ötesine ilerletir, sonra bakar; düşerse her bulguyu adıyla söyler |
| `assertHasName(name)` | Ekran okuyucunun düğüme verdiği ad: içerik açıklaması, yoksa yazısı |
| `assertTouchTargetOf(platform)` | Parmağın basabileceği alan platformun istediği kadar: Android'de 48dp, iOS'ta 44pt, masaüstünde ve web'de fareyle 32dp. Ölçülen, basışı alan bölgedir, çizilen değil |
| `performShortcut(shortcut, system)` | Kısayola o sistemin klavyesi gibi basar: Apple'ın sistemlerinde Command, ötekilerde Control ile. Ekranın da o sistemde yazıldığını `PantrixTestTheme(system = …)` söyler |
| `readText()` | Düğümün gösterdiği yazı: bir metin alanının tuttuğu, yoksa yazıları ve birleştirdiklerinin yazıları, satır satır |
:::

Piksel ve renk yardımcıları modülde yoktur: bir rengi pikselde aramak bileşenin çizimine bağlıdır ve yalnızca Pantrix'in kendi testlerinde durur.

### 3.6. Ekran görüntüleri

Ekran görüntüsü testleri için modülde bir yardımcı yoktur ve modül Roborazzi'ye bağlanmaz: hangi platformda, hangi araçla ve nereye kaydedileceği uygulamanın seçimidir. Pantrix'in kendi kurulumu [Roborazzi rehberindedir](https://github.com/developersancho/pantrix-ui-cmp/blob/main/docs/guide/roborazzi/ROBORAZZI_GUIDE.md). Bir uygulama için özeti:

*   **Üç platform.** Masaüstü (`jvmTest`, `io.github.takahirom.roborazzi.captureRoboImage`), Android (`androidHostTest`, Robolectric, `@GraphicsMode(GraphicsMode.Mode.NATIVE)`, `com.github.takahirom.roborazzi.captureRoboImage`) ve iOS (`iosTest`, `captureRoboImage(this, filePath = …)`). Web'de Roborazzi yoktur.
*   **Sahneyi `PantrixTestTheme` kurar.** Tema, platform, dil ve yazı ölçeği sabitlenmezse görüntü makineden makineye değişir. Tarih gösteren bir sahne bir `TestClock` almalıdır: yoksa görüntü her ay değişir.
*   **Her platformun kendi referansı.** Masaüstü ve iOS Skiko ile çizer, Android kendi çizicisiyle; aynı bileşen birkaç piksel farklı çıkar.
*   **Referanslar Compose'un ve simülatörün sürümüne bağlıdır.** Compose Multiplatform 1.12.1'den 1.12.2'ye geçiş, Skiko ile birlikte gölgeleri değiştirdi; iOS referansları da kaydedildikleri simülatöre ve iOS sürümüne bağlıdır.

### 3.7. Cihaz otomasyonu: Maestro

Pantrix'in etiketleri cihazın erişilebilirlik ağacına ulaşır: Android'de `resource-id` olarak (en dıştaki tema ve her modal ya da açılır katmanın kökü `testTagsAsResourceId`'yi açar), iOS'ta `accessibilityIdentifier` olarak (Compose `testTag`'i kendisi çevirir). Bir [Maestro](https://maestro.dev) akışı bir bileşeni `id:` ile, ekrandaki kodun anahtarıyla bulur; uygulama başka bir dilde açılınca akış kırılmaz.

Uygulamanın kendi açtığı bir pencere, kendi `Popup`'ı ya da `Dialog`'u, Android'de bu köklerin dışındadır: gösterdiğinin kökü `Modifier.pantrixAutomationRoot()`'u kendisi taşır. iOS'ta, masaüstünde ve web'de bu bir şey değiştirmez.

```kotlin
Popup(onDismissRequest = onClose) {
    PantrixModalLayer {
        PantrixSurface(modifier = Modifier.pantrixTransientsRoot().pantrixAutomationRoot()) {
            PantrixButton(text = stringResource(Res.string.close), onClick = onClose, tagKey = "close_layer")
        }
    }
}
```

```yaml
appId: ${APP_ID}
---
- launchApp:
    stopApp: true
- tapOn:
    id: "Button_browse"
- tapOn:
    id: "TextField_search"
- inputText: "PantrixSheet"
- tapOn:
    id: "Card_sheet"
```

**Demo'nun akışları** `app/maestro/` altındadır; kataloğun her sayfasının bir akışı vardır:

*   **`flows/shell/`:** uygulamanın kabuğu: genel bakış, katalog (bölüm süzgeci ve arama), bir bileşen sayfası (sekmeler) ve ayarlar (dil ve tanılama). Etiketi `shell`.
*   **`flows/<bölüm>/<sayfa>.yaml`:** kataloğun 118 sayfası, bölümlerinin klasöründe (`flows/form/checkbox.yaml`). Akış sayfayı bağlantısıyla açar, `pantrixui://page/<anahtar>`; her örneğin kartını sayfadaki sırayla ekrana getirir, koduna geçer ve örneğe döner; sonra sayfanın bileşeninin asıl işini dener: basmak, yazmak, seçmek, açıp kapamak. Etiketleri `page` ve bölümün adı (`form`); birkaçı `smoke`.
*   **`flows/fold/`:** katlanan bir telefonun iç ekranında, telefon kitap gibi tutulurken bakılan sayfalar: menteşede buluşan iki panel, yan ray. Etiketi `fold`. Akış önce duruşa bakar; Pixel_9_Pro_Fold emülatöründe menteşe `adb emu sensor set hinge-angle0 90` ile yarı açılır.
*   **`subflows/`:** ortak adımlar ve iki platformun ayrıldığı yerler: bir sayfayı bağlantısıyla açmak, bir örneğin kartı, bir öğeyi ekranın ortasına getirmek, bir seçeneğin seçili olduğu ya da olmadığı, bir bölümü açmak.

Demo'nun `MaestroFlowsTest`'i (JVM) akışların kataloğun gerisinde kalmadığına bakar: her sayfanın bölümünün klasöründe bir akışı vardır, akış sayfayı bağlantısıyla açar ve sayfanın her örneğine sırasıyla uğrar. Bir sayfa ya da bir örnek eklenip akışı yazılmazsa test cihazsız düşer.

:::table
| Nasıl | Komut |
| :--- | :--- |
| Elle | `maestro test -e APP_ID=com.pantrixui.cmp.demo --exclude-tags fold app/maestro` (iOS'ta bundle kimliği takım kimliğiyle: `com.pantrixui.cmp.demo.PantrixUI<TEAM_ID>`) |
| Android, Gradle | `./gradlew :app:androidApp:maestroTest`: debug derlemesini yalnızca o cihaza kurar, akışları koşar |
| iOS, Gradle | `./gradlew :app:shared:maestroIosTest`: uygulamayı Xcode ile açık simülatöre derler, kurar, akışları koşar |
| Bir bölüm | `./gradlew :app:androidApp:maestroTest -Pmaestro.flows=flows/form` |
| Duman testi | `./gradlew :app:shared:maestroIosTest -Pmaestro.include=smoke` |
:::

*   **İkisi de isteğe bağlıdır** ve `check`'e girmez: bir akış bir cihaz ister, Maestro da derlemeden ayrı kurulan bir programdır.
*   `-Pmaestro.flows=<yol>,…` yalnızca adı verilen akışları koşar: `app/maestro`'nun içindeki dosyalar ya da klasörler (`flows/form`, `flows/shell/overview.yaml`); bir klasör altındaki bütün akışları getirir.
*   `-Pmaestro.include=<etiket>,…` yalnızca o etiketlerden birini taşıyan akışları, `-Pmaestro.exclude=<etiket>,…` o etiketleri taşımayanları koşar.
*   **`fold` etiketli akışlar** yalnızca istenince koşar: `-Pmaestro.include=fold` ya da `-Pmaestro.flows=flows/fold`. Telefonda onların yeri yoktur.
*   **Rapor:** modülün `build/maestro/report/report.xml`'i (JUnit), her koşudan önce boşaltılır. Düşen bir akışın düştüğü ekranın görüntüsü `debug/<akış>/screenshots/`'ta, o ekranın görünüm ağacı `debug/<akış>/screen-hierarchy/`'de, adımları ve günlükleri yanlarındadır. Görev düşerken raporun yerini söyler.
*   `-Pmaestro.device=<kimlik>` birden çok cihaz varken birini seçer: Android'de seri numarası (`emulator-5554`), iOS'ta simülatörün UDID'si. Verilmezse iOS görevi açık ilk simülatörü kullanır; Android görevi derlemeyi bağlı tek cihaza kurar, iki cihaz bağlıysa adb'nin "more than one device" sözüyle ve bu özelliği göstererek durur.
*   `-Pmaestro.executable=<yol>` programı gösterir; verilmezse Maestro'nun kurucusunun koyduğu `~/.maestro/bin/maestro`, o da yoksa yoldaki `maestro`.
*   iOS görevinde Xcode'un derleme aşaması ortak modülün kendi Gradle görevini çağırır (`embedAndSignAppleFrameworkForXcode`): içteki Gradle ikinci bir daemon açar, dıştakini beklemez.
*   **Süre:** bütün akışlar bir platformda üç saat kadar sürer; bir sayfa bir buçuk dakika kadar, bir örnek sekiz dokuz saniye. Zamanın çoğu Maestro'nun her basıştan sonra ekranın durulmasını beklemesidir. Bir bölüm ya da bir etiket daha çabuk biter: `-Pmaestro.flows`, `-Pmaestro.include=smoke`.

**Ne zaman hangisi:** Ekranın davranışı, erişilebilirliği ve platform kuralları ortak testlerde (`runComposeUiTest`), beş hedefte sınanır. Maestro gerçek bir cihazda, gerçek bir uygulamanın kurulduğunu ve etiketlerin sisteme ulaştığını gösterir; klavyeyi, sistem çubuklarını ve gezinmeyi de içerir.

## 4. Pantrix olmadan {#without}

Aşağıdakiler kütüphanesiz bir projeye kopyalanabilecek en kısa hâllerdir; Pantrix'in kodu (`ui/testing/src/`) aynı şeyi testleri ve kenar durumlarıyla yapar.

### 4.1. Etiketi tek yerden kurmak (`commonMain` ve `commonTest`)

```kotlin
// commonMain: bileşen etiketini bununla kurar.
fun testTagOf(component: String, key: String, scope: String? = null): String {
    val slug = key.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_').take(32).trimEnd('_')
    return listOfNotNull(scope, "${component}_$slug").joinToString(".")
}

// commonTest: test de aynısını çağırır.
fun SemanticsNodeInteractionsProvider.onComponent(component: String, key: String, scope: String? = null) =
    onNode(hasTestTag(testTagOf(component, key, scope)))
```

### 4.2. Sabit bir tema ve testin panosu (`commonTest`)

```kotlin
class KeptClipboard : Clipboard {
    val entries = mutableListOf<ClipEntry?>()
    override suspend fun getClipEntry(): ClipEntry? = entries.lastOrNull()
    override suspend fun setClipEntry(clipEntry: ClipEntry?) { entries += clipEntry }
}

@Composable
fun FixedScreen(clipboard: Clipboard = remember { KeptClipboard() }, content: @Composable () -> Unit) {
    val drawn = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(drawn.density, fontScale = 1f),
        LocalLayoutDirection provides LayoutDirection.Ltr,
        LocalClipboard provides clipboard,
        content = content
    )
}
```

Temanın kendi varsayılanları da (dil, karanlık mod, hareket, işletim sistemi) aynı biçimde açıkça verilmelidir; hangileri olduğu temanın parametrelerinde okunur.

### 4.3. Etiketleri android'in otomasyonuna açmak (`androidMain`)

```kotlin
// Kökte bir kez; bir Dialog ya da Popup ayrı bir pencere olduğundan onun kökünde de.
Modifier.semantics { testTagsAsResourceId = true }
```

iOS'ta gerek yoktur: Compose `testTag`'i `accessibilityIdentifier` olarak verir.

## 5. Yardımcının yapmadıkları {#limits toc="5. Yapmadıkları"}

*   **Ekran görüntüsü yardımcısı.** Modül Roborazzi'ye bağlanmaz (3.6).
*   **Piksel ve renk yardımcıları.** Pantrix'in kendi testlerinde kalır.
*   **Pencereyi ve saati kendiliğinden sabitlemek.** `window` verilmezse ekran testin çizdiği pencerededir (masaüstünde 1024×768, Robolectric'te 320dp genişlik); `clock` verilmezse cihazın saatidir.
*   **Web'de cihaz otomasyonu.** Maestro 2.10'un Chromium'u Compose'un anlam ağacını göremiyor (8. bölüm); web'de ortak testler koşar.
*   **Android'de metin alanının menüsü.** Compose Android'de, fareyle sağ tıklanınca ya da seçim çubuğu açılırken, yapıştırılacak bir şey olup olmadığını yalnızca kendi panosuna (`AndroidClipboard`) sorar. Testin panosuyla bir metin alanına sağ tıklayan bir test `IllegalArgumentException` ile düşer (Robolectric'te görüldü); odaklanmak, yazmak ve kopyalamak sormaz. Çubuk Robolectric'te açılamadı: dokunarak seçim, pano ne olursa olsun, büyüteçte düşüyor ([açık madde](https://github.com/developersancho/pantrix-ui-cmp/blob/main/docs/OPEN_ITEMS.md)).
*   **Bir bileşenin içini bilmek.** Modül bileşenleri adlarıyla tanır; bir bileşenin parçalarının adı (`Segment`, `Day_2026_10_24`) o bileşenin KDoc'undadır.

## 6. Test etmek {#testing}

Modülün kendi testleri çoğunlukla ortak koddadır ve beş hedefte koşar (JVM, iOS simülatörü, JS, Wasm; Android'inkiler ayrı ağaçta): bulmak, tema, çiftler ve doğrulamalar. Platformun panosunu masaüstünde ve iOS'ta kendi testleri sınar. Android'in host testi manifest'sizdir: modülün manifest'inin etkinliği bildirdiğini gösterir; etkinlik manifest'ten çıkarılınca "Unable to resolve activity" ile düşer.

Pantrix'in kendi testleri de modülü kullanır. Bileşen testlerinin `TestTheme`'i `PantrixTestTheme`'in üstünde ince bir sarmalayıcıdır: renkler, hareket ve tanılama onun içindeki bir temadan gelir. Olaylar ve dokunsal geri bildirim `RecordingAnalytics` ve `RecordingHaptics`'le tutulur, masaüstünün pano testleri `TestClipboard`'u kullanır. Foundation'ın ve bileşenlerin Android host testlerinin kendi manifest'i yoktur: etkinliği modülün manifest'i bildirir.

Her davranış bir kez bozulup testin kırmızıya döndüğü görüldü (8. bölüm). Saat, iki farkla okunur: bu makine UTC+3'te, tek farkla yazılmış bir test cihazın saatini okuyan bir bileşende de geçerdi.

## 7. Tuzaklar {#pitfalls}

*   **`waitForIdle` testin saatini ilerletir.** Saat kendiliğinden ilerlerken (`mainClock.autoAdvance`, varsayılan) yarım saniyelik bir bulgu `waitForIdle`'da gelir. Saati elle süren bir testte gelmez: `assertNoFindings` saati kendisi ilerletir, elle yazılan bir bakış ilerletmez.
*   **Cihazınkiyle aynı farkı kullanan bir saat testi bir şey göstermez.** UTC+3'teki bir makinede `{ 3.hours }` ile yazılmış bir test, ekranın saatini okumayan bir grafikte de geçer. İki farkla okumak (İstanbul ve New York) bunu önler.
*   **Temanın dışından verilen bir yerel temanınkine yenilir.** `PantrixTestTheme` panoyu ve huylarını, işletim sistemini, yazı ölçeğini ve dosya seçiciyi kendisi verir; bunları temanın dışından veren bir testin değeri ekrana ulaşmaz (`CompositionLocalProvider(LocalClipboard provides …) { PantrixTestTheme { … } }`). Parametreyle ya da temanın içinden verilir. Dışarıdan verilen ekran yoğunluğu ve saat geçer: tema yalnızca yazı ölçeğini değiştirir, saati de yalnızca `clock` verildiğinde.
*   **Compose'un metin alanı platformun panosuna bakar.** Yapıştırılacak bir şey olup olmadığını panonun `nativeClipboard`'ına sorar: masaüstünde sağ tık menüsü açılırken, iOS'ta her odaklanışta. Onu vermeyen bir pano, arayüzün varsayılanıyla, `NotImplementedError` ile düşer; iOS'ta odaklanan her metin alanı düşer. `TestClipboard` masaüstünde kendi AWT panosunu, iOS'ta kendi adlı pasteboard'unu verir; bir testin kendi yazdığı pano da vermelidir.
*   **`setContent` bir testte bir kez çağrılır.** İkinci çağrı hata verir; değişen bir parametre `mutableStateOf` ile verilir.
*   **Android'de ortak testler host ağacında koşmaz.** Android'e özgü bir davranış `androidHostTest`'te ayrıca sınanır.
*   **Web'de panonun okunması bekler.** `TestClipboard.text()` web'de tarayıcının sözünü bekler; güvenli bağlamda olmayan bir sayfada Compose boş bir metin yazar.
*   **iOS testinde dosya seçici.** Compose'un iOS testlerinde uygulama ve `LocalUIViewController` yoktur; `rememberFilePicker()` orada hiç cevap vermeyen bir seçici döndürür.
*   **Maestro'da seçili bir seçenek iki platformda iki ayrı özelliktir.** iOS seçili ya da açık her denetimi `selected` diye verir: radyo düğmesi, segment, onay kutusu, anahtar, çip, ağacın ve sayfa numaralarının satırı. Android yalnızca sekme rolündeki bir düğümü (sekme, alt bar'ın ve yan menünün satırı) `selected`, ötekileri `checked` diye verir. Demo'nun `subflows/assert_chosen.yaml`'ı ve `assert_not_chosen.yaml`'ı platforma göre bakar; bir sekmeye iki platformda da `selected: true` ile bakılır.
*   **iOS'ta `hideKeyboard` bir Compose alanının klavyesini kapatamaz.** Maestro "Couldn't hide the keyboard" der ve akış düşer; Android'de geri tuşuyla kapanır. Açık kalan klavye ekranın altını örter: Maestro bir öğenin ortasına basar, o da klavyenin bir tuşu olabilir. Demo'nun akışları yazmayı en sona koyar, sayfayı baştan açarak; akış bitince uygulama kapanır, klavye onunla gider. Katalogdaki arama ise bir API adıyla yapılır: tek sonuç klavyenin üstünde kalır.
*   **iOS simülatörü uygulamanın kendi düzenindeki bir bağlantıyı açarken bir de sorar.** `openLink` (`xcrun simctl openurl`) bağlantıyı uygulamaya hemen verir, üstüne de sistemin "Open in “PantrixUI”?" diyaloğunu açar; diyalog açıkken uygulamanın ağacı görünmez. Diyalog gelirken bir basışı almaz. Demo'nun `subflows/open_page.yaml`'ı iOS'ta diyalog gidene dek, animasyonun bitmesini bekleyip "Open"a basar. Diyaloğun sözleri simülatörün dilindedir: akış İngilizcesini ve Türkçesini tanır.
*   **Ekranın dibindeki bir düğmeye basış alt bar'a gidebilir.** Maestro bir öğeyi kenarı ekranda kalınca görünür sayar; dibe yakın bir düğmenin ortası uygulamanın kendi alt bar'ının üstüne düşebilir (iOS'ta görüldü: "Hane ekle"ye basış bar'ın "Genel bakış"ına gitti, sayfa değişti). Demo'nun akışlarında bir örneğin denetimlerine dönen her blok, önce ilk basacağı öğeyi ekranın ortasına getirir; ona baktıkları yanındadır.
*   **Ortalanamayan bir öğe.** `centerElement: true` sayfanın sonundaki bir öğeyi ortaya getiremez ve zaman aşımına kadar kaydırır, sonra "No visible element found" der (iOS'ta görüldü: düğme ekranın altındaydı, sayfa daha kaymıyordu). Demo'nun `subflows/show.yaml`'ı önce kısa bir zaman aşımıyla isteğe bağlı olarak ortalar, sonra öğenin ekranda olmasıyla yetinir. Sayfanın sonu bar'ların üstünde biter.
*   **Kartın kodu açıklamasının altında, ekranın dışında kalabilir.** Açıklaması uzun bir örnekte koda geçilince kod bloğu ekranın altına düşer: `assertVisible` düşer. Demo'nun `subflows/example.yaml`'ı kod bloğuna kadar kaydırır (`visibilityPercentage: 10`: uzun bir kod hiç bütün görünmez), sonra düğmeye geri döner.
*   **Android eklentisinin `installDebug`'ı bağlı her cihaza kurar.** `-Pmaestro.device`'a bakmaz: iki cihaz bağlıyken öteki cihazdaki uygulama, orada koşan bir akışın ortasında yeniden kurulur ve kapanır (görüldü). `maestroTest` bu yüzden ona dayanmaz: debug derlemesinin APK'sını `adb -s <kimlik> install` ile yalnızca seçilen cihaza kendisi kurar.
*   **iOS'ta kayan sayfaya basmak yalnızca kaymayı durdurur.** Kaydırmanın ataleti sürerken gelen basış bir düğmeye ulaşmaz; Maestro basışı yapılmış sayar. Demo'nun örnek adımı karttaki geçişe `retry` içinde basar ve geçişin döndüğünü doğrulayarak gerekirse yeniden basar.
*   **Bir örneğin kendi kaydırıcısı kaydırmayı alabilir.** Maestro ekranın ortasından kaydırır; orada sabit yükseklikte uzun bir belge (JSON görünümünün uzun örneği) varsa kaydırmayı o alır, sayfa yerinde kalır. Böyle sayfaların akışları (`# gutter`) sayfayı kartların yanındaki boşluktan, sonundan kaydıran adımları kullanır (`subflows/*_gutter.yaml`); baştaki boşluk katlanan telefonda yan raya düşer.
*   **Bir koşul, görünen bir öğe için yedi saniye bekler.** `when: notVisible` ya da `repeat: while: notVisible`, öğe ekrandayken zaman aşımına kadar bekleyip sonra atlanır. Her örnekte çalışan bir adımda bu, bir platformda bir saati aşar: yedek yollar yalnızca onlara ihtiyacı olan sayfalarda durur.
*   **Açıklaması olmayan bir öğe ağaçta yoktur.** Açıklaması verilmeyen bir avatar, açıklamasız bir rozet ya da simge ekran okuyucudan gizlenir (`hideFromAccessibility`); Maestro onu da görmez. Bir rozetin sayısı, üstünde durduğu simgenin adındadır: akış ona bakar.
*   **Sistem çubukları ve görev çubuğu.** Ekranın altına kaydırılan bir öğe Android'in görev çubuğunun altında kalabilir (tablet emülatöründe görüldü: düğme y 1674–1800, görev çubuğu 1682'den başlıyor). `scrollUntilVisible`'a `centerElement: true` verilir.
*   **iOS'ta Maestro'nun sürücüsü ilk açılışta geç kalkar.** "iOS driver not ready in time": `MAESTRO_DRIVER_STARTUP_TIMEOUT=240000` ile geçti. `maestroIosTest` bunu kendisi verir.
*   **`id:` bir düzenli ifadedir.** Pantrix etiketlerindeki `.` her karakterle eşleşir; zararsızdır. Bir JSON görünümünün satırları gibi yazıldığı gibi alınan anahtarlarda `$`, `[` ya da `(` olabilir: onlar kaçırılmalı.
*   **Anahtarsız bir bileşenin etiketi kodun yerine bağlıdır.** Demo'nun sayı kartları ve sayfa çipleri anahtarsızdır (`Card#-1uvwalkaw10zb`): yalnızca gösterilir, basılmaz. Bir akışın bastığı her bileşen bir `tagKey` almalı.

## 8. Doğrulama {#verification}

### Testler

:::table
| Ne | Nerede |
| :--- | :--- |
| Bulmak: anahtar ve kesilmesi, bölüm zinciri, anahtarsız bulmak, parçalar, ekranın etiketleri ve birleştirilmiş ağaç | `ui/testing/src/commonTest/.../PantrixTagsTest.kt` (JVM, iOS, JS, Wasm) |
| Tema: varsayılanlar, verilenler, masaüstünde ve web'de Windows, pencere ve durumun korunması, pano ve onun üstünde odaklanan bir metin alanı, seçici, saat, tanılama, yarım saniyelik bulgu, her platform | `PantrixTestThemeTest` |
| Çiftler: analitik, dokunsal geri bildirim, seçicinin cevapları, saat, pano (zor metinler dahil) ve yazılamayan pano | `TestDoublesTest` |
| Doğrulamalar: ad, yazı, dokunma hedefi, kısayol | `PantrixAssertionsTest` |
| Masaüstünde platformun panosu: son kopyayı tutar, temizlenince boşalır, her testin panosu kendinin | `ui/testing/src/jvmTest/.../TestClipboardOnTheDesktopTest.kt` |
| iOS'ta platformun panosu: aynıları, genel pasteboard'a hiç yazılmaması da | `ui/testing/src/iosTest/.../TestClipboardOnIosTest.kt` |
| Android: manifest'siz test, `ClipData` ve etiketi, seçici | `ui/testing/src/androidHostTest/.../TestingOnAndroidTest.kt` |
| Etiketin metni | `ui/foundation/src/commonTest/.../testing/TestTagTest.kt` |
| Ekranın saati ve okuyanları | `PantrixClockTest`, `TimeTextFieldTest`, `ElasticDatePickerTest`, `ChartScreenClockTest` |
| Ekranın seçicisi | `LocalFilePickerTest`, `FilesOnAndroidTest` |
:::

### Elle bozulan davranışlar

*   Test modülünde 24 davranış tek tek bozuldu; her birinde en az bir test kırmızıya döndü: anahtarsız bulmanın parçaları ve uzun adları ayırması, etiketlerin süzülmesi, anahtarın elle kurulması, temanın işletim sistemi, yazı ölçeği, panonun huyları, makinenin panosu, platformun seçicisi, cihazın saati, tanılamanın kapanması ve bulguların tutulmaması, pencerenin çerçevesi, her platformun yeniden kurulması, yarım saniyelik bekleyiş, adın sırası, dokunma hedefinin iki yönü, kısayolun Command'ı, metin alanının yazısı, seçicinin türleri ve tek dosyası, olayların kopyası, panonun son kopyası, saatin ilerlemesi.
*   Platform panosunun yedeği bozuldu. Masaüstünde dört yoldan: yedek hiçbir şey tutmuyor, kopya yedeğe ulaşmıyor, temizlenince yedek boşalmıyor, bütün panolara tek bir yedek. iOS'ta dört yoldan: yedek hiçbir şey tutmuyor, temizlenince boşalmıyor, bütün panolara tek bir yedek, hiç yedek yok. Hepsinde bir test kırmızıya döndü. Sonuncusu, Pantrix'in iOS testlerinden 299'unu düşüren eksiğin kendisidir; onu temanın panosunun üstünde odaklanan metin alanı testi de yakaladı. iOS'ta sistemin genel pasteboard'una yazan bir bozma denenmedi: Simulator onu Mac'inkiyle paylaşabilir.
*   Foundation'daki üç açılımda 18 davranış bozuldu; 17'sinde bir test kırmızıya döndü. Kalanı saat alanının `remember` anahtarıdır: seçicinin içi her açılışta yeniden kurulduğu için fark etmez.

### Elle bakılanlar

*   **Android emülatörü (API 34, 2208×1840 tablet):** demo'nun beş akışı Maestro CLI ile ve `./gradlew :app:androidApp:maestroTest` ile geçti. Etiketler `resource-id` olarak görünüyor; sheet'in ayrı penceresindeki de.
*   **iPhone 18 Pro Max simülatörü (iOS 27.0):** beş akış Maestro CLI ile ve `./gradlew :app:shared:maestroIosTest` ile geçti (Xcode derlemesiyle 3 dakika 30 saniye). Etiketler `accessibilityIdentifier` olarak görünüyor; telefonda bölümler alt bardadır.
*   **Bütün akışlar, Pixel 7 Pro emülatörü (Android 13, API 33, 1440×3120):** `./gradlew :app:androidApp:maestroTest` özelliksiz, bütün çalışma alanıyla: 118 akışın (kabuğun 4'ü, sayfaların 114'ü) hepsi geçti, 2 saat 40 dakikada. JUnit raporu 118 testcase, 0 hata yazdı; etiketleri bölümlere göre doğru. `fold` etiketli akışlar koşmadı.
*   **Bütün akışlar, iPhone 18 Pro Max simülatörü (iOS 27.0):** `./gradlew :app:shared:maestroIosTest` özelliksiz: 118 akışın 116'sı geçti, 3 saat 2 dakikada. Düşen ikisinden biri koşu başladıktan sonra düzeltilen bir akıştı; ötekinde ekranın dibindeki bir düğmeye basış alt bar'a gitmişti (7. bölüm). Bu ikisi ve o düzeltmenin değiştirdiği 25 akış, kabuk ve Elements bölümü son hâlleriyle yeniden koştu ve geçti; aynısı Android'de de geçti. Son düzeltmenin dokunmadığı akışlar, iOS'ta ekranın durulmasını bekleyen adımdan (`subflows/show_still.yaml`) önceki hâlleriyle geçti.
*   **Katlanan telefon, Pixel 9 Pro Fold emülatörü (Android 15, API 35, iç ekran 852×883 dp):** menteşe `adb emu sensor set hinge-angle0 90` ile yarı açıkken `-Pmaestro.include=fold` üç akışı koştu, üçü de geçti. Menteşe düzken (`180`) liste-ayrıntı akışı duruşa bakan adımda düştü. Aynı cihazda `-Pmaestro.include=shell` yalnızca kabuğun dört akışını koştu ve dördü de yan rayla geçti.
*   **Kurulum yalnızca seçilen cihaza:** iki emülatör bağlıyken (Pixel 7 Pro ve Pixel 9 Pro Fold) `-Pmaestro.device=emulator-5554` debug derlemesini yalnızca ona kurdu; uygulaması önceden kaldırılan öteki cihazda uygulama yine yoktu. Cihaz adı verilmeyince görev adb'nin "more than one device" sözüyle ve `-Pmaestro.device`'ı göstererek durdu.
*   **Uygulamanın kendi penceresi:** demo'nun "Yapı taşları" sayfasındaki kendi `Popup`'ı köküne `pantrixAutomationRoot()` koyunca "Kapat" düğmesi Android'de de `Button_close_layer` diye bulundu; işaretsizken ağaçta yalnızca yazısıyla vardı. Pantrix'in katmanları aynı modifier'a geçtikten sonra katmanların sekiz akışı ve select'lerin ikisi iki platformda geçti.
*   **Görevin seçimleri:** `-Pmaestro.flows` bir dosyayı, bir klasörü ve virgülle ayrılmış birkaç yolu koştu; `-Pmaestro.include` etiketle süzdü; özelliksiz koşu `fold`'u dışarıda bıraktı. Düşen bir akışın ekran görüntüsü ve görünüm ağacı `build/maestro/report/debug/<akış>/` altına yazıldı; görev raporun yerini söyleyerek düştü.
*   **Akışların denetimleri gerçek:** seçili olmayan bir seçeneği "seçili" diye denetleyen bir akış iki cihazda da düştü; iOS'ta platformun kolu koştu, Android'de Android'inki.
*   **Web (Maestro'nun Chromium'u):** sayfa açılıyor ve çiziliyor, ama Maestro'nun ağacında yalnızca `#app` kabı ve iki boş öğe var. Compose 1.12.2 anlam ağacını gizli bir DOM'a yansıtıyor (`#cmp_a11y_root`; `opacity: 0`, `pointer-events: none`): genel bakışta 57 öğenin `id`'si Pantrix etiketi (`Button_browse`, rolü `button`, adı "Browse the components"). Ekrandaki öğeler düğümün yerinde ve boyunda duruyor (`position: fixed`), ekran dışındakiler sıfır boyutlu. Bu ağaç tuvali taşıyan öğenin shadow root'unun içinde; Maestro 2.10 oraya girmiyor.
*   **Manifest'in birleşmesi:** foundation'ın ve bileşenlerin Android host testlerinin kendi manifest'i silindi; testleri (40 ve 193) modülün manifest'iyle geçiyor.
*   **Web'de demo'nun üç yeni örneği:** "Ekranın saati" Kiritimati'de 24 Ekim'i, Pago Pago'da 23 Ekim'i gösteriyor; "Örnek dosyalarla bir ekran" iki örnek dosyayı yazıyor; "Testin aradığı etiket" aranan ve taşınan etiketi aynı yazıyor.

### Doğrulanmayanlar

*   Gerçek cihazlar, Maestro Cloud ve Linux ya da Windows'ta bir CI makinesi. Bir Android telefonda iki dil: tam koşu cihazın dili olan İngilizceyle koştu; akışlar Türkçede de geçmek için yazıldı (etiketler ve iki dilin sözleri), Türkçe bir cihazda tam koşu yapılmadı.
*   Modülün Maven'den alındığı ayrı bir proje: POM'u, modül meta verisi ve Android AAR'ının manifest'i üretildi ve okundu, yerel depoya yayınlanmadı.
*   Web'de shadow root'a giren bir araç (örneğin Playwright'ın seçicileri) ile etiketlerin bulunması.
*   Android'de testin panosuyla açılan bir seçim çubuğu (5. bölüm): Compose'un kaynağında okundu; Robolectric'te dokunarak seçim büyüteçte düştüğü için açılamadı, cihazda koşulmadı.

## 9. Bu depoda nerede {#repository toc="9. Depoda"}

*   Test modülü: `ui/testing/src/commonMain/kotlin/com/pantrixui/testing/` (`PantrixTags.kt`, `PantrixTestTheme.kt`, `TestClipboard.kt` ve her platformun okuması ve panosu, `RecordingAnalytics.kt`, `RecordingHaptics.kt`, `TestFilePicker.kt`, `TestFileSaver.kt`, `TestClock.kt`, `PantrixAssertions.kt`); manifest `ui/testing/src/androidMain/AndroidManifest.xml`.
*   Foundation'ın açılımları: `testing/TestTag.kt` (`pantrixTestTagOf`), `date/PantrixClock.kt`, `files/FilePicker.kt` (`LocalFilePicker`); otomasyon köprüsü `testing/AutomationBridge.kt` (`pantrixAutomationRoot`).
*   Maestro: akışlar `app/maestro/`; görevler `build-logic/src/main/kotlin/com/pantrixui/plugins/maestro/` (`pantrix.demo.maestro`, `app/androidApp` ve `app/shared`'de uygulanır).
*   Demo: `app/shared/src/commonMain/kotlin/com/pantrixui/cmp/demo/samples/TestTagsSamples.kt` ("Testin aradığı etiket"), `FilesSamples.kt` ("Örnek dosyalarla bir ekran"), `DatePickerSamples.kt` ("Ekranın saati").

## 10. Açık kalanlar {#open}

`docs/OPEN_ITEMS.md`'de: web'de cihaz otomasyonu ve Android'de testin panosuyla açılan metin alanı menüsü.
