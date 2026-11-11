---
title: Panoya metin kopyalama
description: Compose Multiplatform'da bir metni panoya kopyalamanın neden tek bir çağrı olmadığı, bunun için kütüphanenin ne verdiği, bir kopyalama düğmesinin ne zaman ne söylemesi gerektiği, panodaki metnin nasıl okunduğu, bir yapıştırmanın nasıl duyulduğu ve bunların nasıl sınandığı. Sorun Pantrix'e özgü değildir: ortak kodunda bir "kopyala" düğmesi olan her Compose Multiplatform projesinde aynı şekilde görülür.
---

Compose hakkında burada söylenen her şey Compose Multiplatform 1.12.1'in kaynaklarında okunmuş ya da bu depoda koşan bir testte görülmüştür; hangisinin nerede görüldüğü 7. ve 8. bölümlerde yazılıdır. Doğrulanamayanlar 8. bölümde ayrıca sayılır.

## 1. Sorun {#problem}

:::aside clipboard
### Belirti

*   Ortak kodda eskiden tek satırdı: `LocalClipboardManager.current.setText(AnnotatedString(metin))`. Bu arayüz kullanımdan kalktı; derleyici "Use LocalClipboard instead which supports suspend functions" der.
*   Yerine gelen `LocalClipboard`'ta "şu metni kopyala" diye bir çağrı yoktur. Tek yazma çağrısı `setClipEntry(clipEntry: ClipEntry?)`'dir ve bir `ClipEntry` ortak kodda kurulamaz.
*   Çağrı askıdadır (`suspend`): bir düğmenin `onClick`'inden doğrudan çağrılamaz, bir coroutine ister.
*   Kopyalamanın sonu platformdan platforma değişir. Tarayıcı yazmayı reddedebilir; güvenli bir bağlamda olmayan sayfada hiçbir şey kopyalanmaz ve kimse haber vermez; Android 13 ve üstünde sistem kopyalananı kendisi gösterir. Basıldığı anda "kopyalandı" diyen bir düğme üçünde de yanlış bir şey söyler.
:::

### Neden

`ClipEntry` ortak kodda yalnızca bir addır: `expect class ClipEntry { val clipMetadata: ClipMetadata }` (`ClipboardManager.kt`). Kurucusu ve içindeki her şey platformun kendi bildirimindedir (Compose Multiplatform 1.12.1; Android'de `androidx.compose.ui` 1.12.1):

:::table
| Platform | `ClipEntry` orada nedir | Düz metin nasıl kurulur | İşaret |
| :--- | :--- | :--- | :--- |
| Android | Bir `ClipData`'yı saran sınıf: `ClipEntry(val clipData: ClipData)` | `ClipEntry(ClipData.newPlainText(etiket, metin))` | Yok |
| Masaüstü (JVM) | Bir nesneyi saran sınıf: `ClipEntry(nativeClipEntry: Any)`. Pano onu bir `java.awt.datatransfer.Transferable` olarak okur | `ClipEntry(StringSelection(metin))` | Kurucu `@ExperimentalComposeUiApi` |
| iOS | Compose'un kendi sınıfı; kurucusu `internal` | `ClipEntry.withPlainText(metin)` | `@ExperimentalComposeUiApi` |
| Web, `js` | `ClipEntry(clipboardItems: Array<ClipboardItem>)` | `ClipEntry.withPlainText(metin)` | Kurucu deneysel, `withPlainText` değil |
| Web, `wasmJs` | `ClipEntry(clipboardItems: JsArray<ClipboardItem>)` | `ClipEntry.withPlainText(metin)` | Kurucu deneysel, `withPlainText` değil |
:::

Kaynak dosyaları: `AndroidClipboardManager.android.kt`, `PlatformClipboard.desktop.kt`, `PlatformClipboard.ios.kt`, `PlatformClipboard.js.kt`, `PlatformClipboard.wasm.kt`.

Üç ayrıntı işi büyütür:

1.  **İki web hedefi iki ayrı bildirimdir.** `ClipEntry`, `js` ve `wasmJs` için ayrı ayrı bildirilmiştir; ikisinin ortak kaynak kümesi (`webMain`) yalnızca ortak koddaki boş bildirimi görür. Bir `expect` fonksiyonunun karşılığı bu yüzden beş kez yazılır: Android, iOS, masaüstü, JS, Wasm.
2.  **Deneysel işaretler.** Masaüstünün kurucusu ve iOS'un `withPlainText`'i `@ExperimentalComposeUiApi` taşır; kullanan her yer `@OptIn` ister.
3.  **Yazma askıdadır, çünkü tarayıcıda öyledir.** Tarayıcının panosu bir `Promise` döner; Compose onu bekler ve reddedilirse hata o çağrıdan fırlar. Eski arayüz tarayıcıda yazmayı başlatır, sonucunu beklemezdi (`JsPlatformClipboardManager.setText`); okuması da orada hep boş dönerdi (`getText(): AnnotatedString? = null`). Sonucu söyleyemeyen bir arayüzdü.

### Platformlar yazmayı nasıl bitirir

:::table
| Platform | Compose ne yapar | Ne ters gidebilir |
| :--- | :--- | :--- |
| Android | `ClipboardManager.setPrimaryClip(clipData)` | Sistem servisinin hatası çağrıdan bir `RuntimeException` olarak fırlar (`rethrowFromSystemServer`, Android SDK kaynağı) |
| Masaüstü | Sistemin panosuna `setContents(transferable, owner)` | Pano o an kullanılamıyorsa `IllegalStateException` (JDK'nın belgesi). Başsız bir JVM'de (`java.awt.headless=true`) pano yoktur: yazma sessizce atlanır |
| iOS | `UIPasteboard.generalPasteboard.string = metin` | Kaynakta bir hata yolu yok |
| Web | Tam pano arayüzü varsa `navigator.clipboard.write([ClipboardItem])`, yoksa `writeText(metin)`; ikisi de beklenir | Tarayıcı reddederse hata çağrıdan fırlar. Güvenli olmayan bağlamda pano yoktur: yazma sessizce atlanır, konsola bir uyarı düşer |
:::

"Sessizce atlanır" denen iki yerde `setClipEntry` hata fırlatmadan döner: onu saran kod kopyalandığını sanır.

## 2. Çözüm: üç parça {#fix}

:::table
| # | Parça | Ne sağlar |
| :--- | :--- | :--- |
| 1 | **Türü platformda kur.** Tek bir `expect` fonksiyonu, beş karşılık | Ortak kod "bu metin" der; platformun türünü bilmez |
| 2 | **Sonucu cevap yap.** Yazmayı saran askıda bir fonksiyon: kopyalandı mı | Düğme olanı söyler, basıldığını değil. Platformun fırlattığı her şey tek bir cevaptır: kopyalanamadı. Tek istisna iptaldir, o yeniden fırlatılır |
| 3 | **Panonun huylarını önceden bil.** Yazılacak bir pano var mı; sistem kopyalananı kendisi gösteriyor mu | Hata fırlatmayan iki durum da doğru söylenir: panosu olmayan sayfa ve Android 13 |
:::

Üçüncü parça olmadan ilk ikisi iki yerde yanılır. Panosu olmayan bir sayfada hata fırlamaz ve cevap "kopyalandı" olur; Android 13'te cevap doğrudur ama uygulama, sistemin az önce gösterdiğini bir kez daha söyler.

## 3. Pantrix ile {#with}

Kütüphane üç parçayı da verir (`com.pantrixui.foundation.platform`):

:::table
| API | Ne yapar |
| :--- | :--- |
| `rememberPantrixClipboard()` | Düz metin için pano: `LocalClipboard`'ın verdiği panonun üstünde, `LocalClipboardTraits`'in huylarıyla. Yeniden kurulumlarda aynı nesnedir; pano ya da huylar değişince yenisi verilir |
| `PantrixClipboard.copyText(text, label)` | Metni panoya koyar ve konup konmadığını söyler (`suspend`, `Boolean`). Yazılacak pano yoksa panoya hiç gitmez ve `false` der. `label` yalnızca Android'de taşınır |
| `PantrixClipboard.readText()` | Panodaki düz metni okur (`suspend`, `String?`). Panoda metin yoksa, okunacak pano yoksa ya da platform okumayı reddettiyse `null` der; okunacak pano yoksa panoya hiç gitmez |
| `PantrixClipboard.traits` | Bu panonun huyları |
| `ClipboardTraits(writable, announcedBySystem, readable)` | Üç huy: yazılacak bir pano var mı; sistem kopyalananı kendisi gösteriyor mu; okunacak bir pano var mı |
| `currentClipboardTraits()` | Platformun huyları, sorulduğu andaki hâliyle. Android'de API 33'ten başlayarak `announcedBySystem`; tarayıcıda sayfa güvenli bir bağlamda değilse ve masaüstünde JVM başsız çalışıyorsa `writable` ve `readable` yanlış; tarayıcıda `navigator.clipboard.read` yoksa (127'den eski Firefox) `readable` yanlış |
| `LocalClipboardTraits` | `rememberPantrixClipboard()`'un baktığı huylar. Verilmezse platformunkidir; bir test başka bir platformunkini buradan verir |
| `Modifier.onPaste(enabled, onPaste)` | Odak düğümün içindeyken okuyucunun yapıştırmasını, ⌘V'yi ya da Ctrl+V'yi duyar ve metniyle `onPaste`'i çağırır; içteki bir metin alanı yapıştırmayı kendisi almadıkça |
:::

### 3.1. Kopyalama düğmesi

```kotlin
// commonMain
@Composable
fun CopyLinkButton(link: String) {
    val clipboard = rememberPantrixClipboard()
    val snackbars = LocalPantrixSnackbars.current
    val scope = rememberCoroutineScope()
    PantrixButton(
        text = "Bağlantıyı kopyala",
        onClick = {
            scope.launch {
                // Askıda: tarayıcıda yazma bitince döner. Cevap olanı söyler, basıldığını değil.
                val copied = clipboard.copyText(link, label = "Bağlantı")
                val message = when {
                    !copied -> "Bağlantı kopyalanamadı"
                    // Android 13 ve üstü: sistem kopyalananı kendisi gösterir.
                    clipboard.traits.announcedBySystem -> null
                    else -> "Bağlantı kopyalandı"
                }
                message?.let { snackbars?.show(message = it) }
            }
        }
    )
}
```

Platforma bakan tek bir satır yoktur: aynı düğme beş hedefte de derlenir. `rememberCoroutineScope()` düğmenin kendi kapsamıdır; ekran terk edilirse yarım kalan kopyalama da iptal olur ve cevap gelmez.

### 3.2. Düğme ne der, ne zaman susar

:::table
| Durum | `copyText` | Huy | Düğme |
| :--- | :--- | :--- | :--- |
| Kopyalandı; sistem bir şey göstermiyor (masaüstü, iOS, tarayıcı, Android 12L ve altı) | `true` | `announcedBySystem = false` | "Kopyalandı" der |
| Kopyalandı; sistem kendisi gösteriyor (Android 13 ve üstü) | `true` | `announcedBySystem = true` | **Susar** |
| Platform reddetti (tarayıcı izin vermedi, pano o an kullanılamıyor) | `false` | — | "Kopyalanamadı" der |
| Yazılacak pano yok (güvenli bağlamda olmayan sayfa) | `false`; panoya gidilmez | `writable = false` | "Kopyalanamadı" der |
:::

*   **Kopyalanamayan her platformda söylenir.** Onu sistem göstermez; Android 13'te de uygulama söyler.
*   **`writable` basılmadan önce bilinir.** Düğmeyi baştan kapalı çizmek ya da hiç çizmemek de bir seçimdir: `enabled = clipboard.traits.writable`. Bu depodaki örnek uygulama düğmeyi çizer ve basılınca "kopyalanamadı" der.
*   **Kural uygulamanındır.** Kütüphane ne olduğunu söyler; ne deneceğine uygulama karar verir. Örnek uygulamada bu karar tek bir fonksiyondur ve ayrıca sınanır (`copyMessage`, `CopyMessageTest`).

### 3.3. Etiket

`label` panoya konanın ne olduğunu söyler: "Bağlantı", "Kod". Yalnızca Android taşır; `ClipData.newPlainText`'in belgesi ona "User-visible label for the clip data" der, bu yüzden uygulamanın dilinde verilir. Diğer platformlarda yok sayılır. Verilmezse boştur.

### 3.4. Panodan okumak ve yapıştırmayı duymak

Bir ekran panodakini iki yoldan alır: bir düğmeyle (`readText()`) ya da okuyucunun kendi yapıştırmasıyla, ⌘V ya da Ctrl+V (`Modifier.onPaste`).

```kotlin
// commonMain
@Composable
fun StartScreen(open: (String) -> Unit) {
    val clipboard = rememberPantrixClipboard()
    val scope = rememberCoroutineScope()
    // Pencerenin her yerinde: kök, başka hiçbir şey odakta değilken odağı tutar.
    Column(Modifier.shortcutRoot().onPaste { text -> open(text) }) {
        PantrixButton(
            text = "Panodan yapıştır",
            // Askıda: tarayıcı ve iOS önce sorabilir.
            onClick = { scope.launch { clipboard.readText()?.let(open) } },
            enabled = clipboard.traits.readable,
            tagKey = "paste"
        )
    }
}
```

:::table
| Platform | `readText()` | ⌘V ya da Ctrl+V (`onPaste`) |
| :--- | :--- | :--- |
| Android | Okur; sistem Android 12'den başlayarak panodan okunduğunu bir uyarıyla söyler | Donanım klavyesinin tuşu: tuş inerken panoyu okur |
| Masaüstü | Okur: başka bir uygulamanın kopyasını ana iş parçacığının dışında, bellekte tutulan bir metni yerinde; ekransız JVM'de `readable` yanlıştır | Tuş inerken panoyu okur |
| iOS | Başka bir uygulamanın kopyaladığını okumadan önce iOS izin sorar | Donanım klavyesinin tuşu: tuş inerken panoyu okur. Odakta bir metin alanı varken yapıştırmayı UIKit yapar; düğüm tuşu ona bırakır |
| Web | Tarayıcı sorar: Chrome sayfa için bir kez, Safari her okumada kendi "Yapıştır" düğmesiyle; ikisi de yalnızca bir basıştan hemen sonra yanıt verir | Tarayıcının kendi yapıştırması: metin soru sorulmadan gelir |
:::

*   **Metin alanı kendisine yapıştırır.** Odak içteki bir metin alanındaysa yapıştırma onundur; `onPaste` bir şey duymaz.
*   **İçteki alır.** Odağın çevresinde iki `onPaste` varsa yapıştırmayı içteki alır; `enabled = false` olan onu çevresindekine bırakır.
*   **⌘V bir kısayol değildir.** Web'de `onShortcut(PantrixShortcut(Key.V, command = true))` tuşu tarayıcıdan saklar ve tarayıcı hiç yapıştırmaz. Yapıştırma `onPaste` ile duyulur.
*   **Boş pano.** Panoda metin yoksa `readText()` `null` döner ve `onPaste` çağrılmaz.

## 4. Pantrix olmadan {#without}

Aynı çözüm, yalnızca Compose Multiplatform'un kendi API'siyle. Aşağıdaki kod herhangi bir projeye kopyalanabilir; kütüphanedeki karşılıklarının sadeleştirilmiş hâlidir.

### 4.1. Ortak kod (`commonMain`)

```kotlin
/** [text], bu platformun panosunun aldığı türde. Etiketi yalnızca Android taşır. */
expect fun plainTextClipEntry(label: String, text: String): ClipEntry

/** Panonun bu platformdaki iki huyu. */
data class ClipboardTraits(
    val writable: Boolean = true,
    val announcedBySystem: Boolean = false
)

/** Bu platformun panosunun huyları, sorulduğu andaki hâliyle. */
expect fun currentClipboardTraits(): ClipboardTraits

/** Düğmelerin baktığı huylar. Bir test, başka bir platformun huyunu buradan verir. */
val LocalClipboardTraits = staticCompositionLocalOf { currentClipboardTraits() }

/** [text]'i panoya koyar ve konup konmadığını söyler. */
suspend fun Clipboard.copyText(
    text: String,
    label: String = "",
    traits: ClipboardTraits = currentClipboardTraits()
): Boolean = traits.writable &&
    try {
        setClipEntry(plainTextClipEntry(label, text))
        true
    } catch (cancelled: CancellationException) {
        // İptal bir cevap değildir: yutulursa iptal edilen coroutine yoluna devam eder.
        throw cancelled
    } catch (_: Throwable) {
        // Tarayıcının reddi bir Kotlin `Exception`'ı değildir.
        false
    }
```

`CancellationException` burada `kotlin.coroutines.cancellation.CancellationException`'dır.

### 4.2. Platform karşılıkları

```kotlin
// androidMain
actual fun plainTextClipEntry(label: String, text: String): ClipEntry =
    ClipEntry(ClipData.newPlainText(label, text))

// Android 13'ten (API 33) başlayarak sistem, panoya konanı kendi önizlemesiyle gösterir.
actual fun currentClipboardTraits(): ClipboardTraits =
    ClipboardTraits(announcedBySystem = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
```

```kotlin
// jvmMain
@OptIn(ExperimentalComposeUiApi::class)
actual fun plainTextClipEntry(label: String, text: String): ClipEntry = ClipEntry(StringSelection(text))

// Ekranı olmayan (başsız) bir JVM'de sistemin panosu yoktur; Compose oraya yazmayı hata fırlatmadan atlar.
actual fun currentClipboardTraits(): ClipboardTraits =
    ClipboardTraits(writable = !GraphicsEnvironment.isHeadless())
```

```kotlin
// iosMain
@OptIn(ExperimentalComposeUiApi::class)
actual fun plainTextClipEntry(label: String, text: String): ClipEntry = ClipEntry.withPlainText(text)

actual fun currentClipboardTraits(): ClipboardTraits = ClipboardTraits()
```

```kotlin
// jsMain ve wasmJsMain: aynı satır, iki ayrı dosyada
actual fun plainTextClipEntry(label: String, text: String): ClipEntry = ClipEntry.withPlainText(text)
```

```kotlin
// webMain
actual fun currentClipboardTraits(): ClipboardTraits = ClipboardTraits(writable = browserClipboardWrites())

// Tarayıcı panoyu yalnızca güvenli bir bağlamda verir: https, ya da makinenin kendisi.
@OptIn(ExperimentalWasmJsInterop::class)
private fun browserClipboardWrites(): Boolean =
    js("window.isSecureContext === true && Boolean(window.navigator.clipboard && window.navigator.clipboard.writeText)")
```

Tarayıcıya sorulan soru `webMain`'de tek kez yazılır; `ClipEntry.withPlainText` ise yazılamaz, iki hedefte ayrı ayrı durur (bkz. 7. bölüm, "`webMain`'e yazılan karşılık"). Yalnızca bir web hedefi olan projede ikisi de o hedefin kaynak kümesine yazılır.

### 4.3. Kullanım

```kotlin
// commonMain
@Composable
fun CopyLinkButton(link: String, snackbars: SnackbarHostState) {
    val clipboard = LocalClipboard.current
    val traits = LocalClipboardTraits.current
    val scope = rememberCoroutineScope()
    Button(
        onClick = {
            scope.launch {
                val copied = clipboard.copyText(link, label = "Bağlantı", traits = traits)
                val message = when {
                    !copied -> "Bağlantı kopyalanamadı"
                    // Android 13 ve üstü: sistem kopyalananı kendisi gösterir.
                    traits.announcedBySystem -> null
                    else -> "Bağlantı kopyalandı"
                }
                message?.let { snackbars.showSnackbar(it) }
            }
        }
    ) {
        Text("Bağlantıyı kopyala")
    }
}
```

## 5. Yardımcının yapmadıkları {#limits toc="5. Yapmadıkları"}

Yardımcı düz bir metni panoya koyar ve konup konmadığını söyler, panodaki düz metni okur ve okuyucunun yapıştırmasını duyar. Şunları yapmaz:

*   **Bir yapıştırma menüsü çizmez.** Bir metin alanının menüsündeki "Yapıştır" alanındır; `onPaste` yalnızca tuşu ve tarayıcının yapıştırmasını duyar.
*   **Düz metinden başkasını koymaz.** Biçimli metin, HTML, bir adres (URI), bir görüntü ya da bir dosya taşımaz.
*   **Panoyu boşaltmaz.** Compose'da bu `setClipEntry(null)`'dır.
*   **İçeriği hassas diye işaretlemez.** Android'de bir parola gibi içeriğin gösterilirken gizlenmesini isteyen işaret `ClipDescription.EXTRA_IS_SENSITIVE`'dir (Android SDK kaynağı); `ClipData.newPlainText` onu koymaz.
*   **Haber vermez.** Snackbar'ı uygulama gösterir; yardımcı yalnızca ne olduğunu söyler.

Düz metinden başkasını taşımayı ya da Pantrix olmadan okumayı bir proje kendisi yazacaksa yolu yine platformdan platforma ayrılır. Compose'un kaynaklarının gösterdiği kadarı aşağıdadır; okuma sütunu, Compose Foundation'ın metin alanlarında yapıştırırken kullandığı kendi okuyucusundan (`ClipboardUtils.*.kt`, `readText()`) alınmıştır. O okuyucu `internal`'dır, bir uygulama onu çağıramaz.

:::table
| Platform | Metni geri okumak | Düz metinden başkası |
| :--- | :--- | :--- |
| Android | `getClipEntry()?.clipData?.getItemAt(0)?.text?.toString()`. Panonun içeriğine ilk erişimde sistem bir uyarı gösterir (Compose'un belgesindeki adıyla "App pasted from Clipboard"); yalnızca panoda ne olduğunu sormak için `clipboard.nativeClipboardManager.primaryClipDescription` | Herhangi bir `ClipData`: `ClipEntry(clipData)` ya da `clipData.toClipEntry()`. İçindeki ilk adres için `firstUriOrNull()` (deneysel) |
| Masaüstü | `getClipEntry()?.asAwtTransferable` (deneysel); `isDataFlavorSupported(DataFlavor.stringFlavor)` ise `getTransferData(DataFlavor.stringFlavor) as? String`. Foundation bunu başka bir uygulamanın kopyası için `Dispatchers.IO`'da yapar, bellekte tutulan bir `StringSelection` için yerinde, ve `IOException`'ı "metin yok" sayar | Herhangi bir `Transferable`: `ClipEntry(transferable)`. `ClipboardOwner` da ise panonun sahibi olarak verilir |
| iOS | `getClipEntry()?.getPlainText()` (deneysel). Pano boşsa `getClipEntry()` null döner | Compose'un `ClipEntry`'si yalnızca metin taşır ("Currently, it operates only with string(s)"); gerisi için `UIPasteboard` doğrudan kullanılır |
| Web | `getClipEntry()?.clipboardItems` (deneysel); ilk öğenin `getType("text/plain")`'i bir `Blob` verir, metni `blob.text()` ile, bir `Promise` olarak gelir. Dizi `js`'te `Array`, `wasmJs`'te `JsArray`'dir: iki hedefte ayrı yazılır. Tarayıcı okumayı reddederse Compose hata fırlatmaz, içi boş bir giriş döner; `read` hiç yoksa null döner | Kendi `ClipboardItem`'larınla `ClipEntry(clipboardItems)` (kurucu deneysel). Tam pano arayüzü olmayan tarayıcıda Compose yalnızca `writeText` ile düz metni yazar |
:::

## 6. Test etmek {#testing}

### 6.1. Sahte pano

Compose bir teste gerçek panoyu verir (bkz. 7. bölüm, "Sahtesi verilmeyen pano"). Kopyalayan her test kendi panosunu `LocalClipboard` ile verir:

```kotlin
/** Konan her şeyi tutan, makinenin panosuna dokunmayan bir pano. */
class RecordingClipboard(private val refuses: Boolean = false) : Clipboard {
    val entries = mutableListOf<ClipEntry?>()

    override suspend fun getClipEntry(): ClipEntry? = entries.lastOrNull()

    override suspend fun setClipEntry(clipEntry: ClipEntry?) {
        check(!refuses) { "NotAllowedError: Write permission denied." }
        entries += clipEntry
    }
}
```

`Clipboard` ortak koddadır: bu sahte, ortak bir testte de yazılır. Orada bakılabilenler kaç kez yazıldığı ve `copyText`'in ne dediğidir; gelen `ClipEntry`'nin içindeki metin ortak kodda okunamaz.

### 6.2. Başka bir platformun huyu

Android 13'ün ya da panosu olmayan bir sayfanın davranışı, o platformda koşmayan bir testte de sınanır: huylar `LocalClipboardTraits` ile verilir.

```kotlin
// jvmTest
@OptIn(ExperimentalTestApi::class, ExperimentalComposeUiApi::class)
class CopyLinkButtonTest {
    @Test
    fun a_copy_the_system_shows_itself_is_not_told_twice() = runComposeUiTest {
        val clipboard = RecordingClipboard()
        val snackbars = PantrixSnackbarState()
        setContent {
            PantrixTheme {
                CompositionLocalProvider(
                    LocalClipboard provides clipboard,
                    // Android 13'ün huyu, masaüstünde koşan bir testte.
                    LocalClipboardTraits provides ClipboardTraits(announcedBySystem = true),
                    LocalPantrixSnackbars provides snackbars
                ) {
                    CopyLinkButton(link = "https://example.com")
                }
            }
        }
        onNodeWithText("Bağlantıyı kopyala").performClick()
        waitForIdle()
        // Metin masaüstünün okuduğu gibi okunur: `Transferable`'ın metin biçimiyle.
        val copied = clipboard.entries.single()?.asAwtTransferable?.getTransferData(DataFlavor.stringFlavor)
        assertEquals("https://example.com", copied)
        assertEquals(null, snackbars.current)
    }
}
```

`ClipboardTraits(writable = false)` ile aynı test panosu olmayan sayfayı sınar: sahte panoya hiçbir şey gelmez ve düğme "kopyalanamadı" der. `RecordingClipboard(refuses = true)` reddeden bir tarayıcıdır.

### 6.3. Gelen metni okumak

Metnin kendisine her platformun testi kendi yoluyla bakar:

:::table
| Platform | Test nerede koşar | Metin nasıl okunur |
| :--- | :--- | :--- |
| Masaüstü | `jvmTest`, sahte panoyla | `entry.asAwtTransferable?.getTransferData(DataFlavor.stringFlavor)` |
| Android | Robolectric, gerçek panoyla: Robolectric'in panosu testin belleğindedir | `LocalClipboard.current.nativeClipboardManager.primaryClip`: ilk öğenin `text`'i, `description.label` |
| iOS | Simülatör, sahte panoyla | `entry.getPlainText()` |
| Web | Tarayıcı, Compose'un kendi panosuyla | `navigator.clipboard.write` ve `writeText` yerine testin fonksiyonları konur; `write`'a gelen öğenin `text/plain` parçası okunur |
:::

Web'de yol başkadır. `webTest`, `webMain` gibi iki hedefin ortak kaynak kümesidir ve `ClipEntry`'nin içi orada tek bir biçimde yazılamaz: öğeleri bir hedefte `Array`, ötekinde `JsArray`'dir. Testin yerine geçtiği şey Compose'un panosu değil, tarayıcınınkidir: böylece Compose'un tarayıcıya gerçekten ne verdiği görülür, reddedilen bir yazma da gerçek yolundan geçer (`Promise.reject(new DOMException(…, 'NotAllowedError'))`). İki fonksiyonun da yerine geçilir; hangisinin çağrılacağına Compose tarayıcıya bakarak karar verir.

### 6.4. Yapıştırmayı sınamak

Bir uygulamanın testi panoya başka bir uygulamanın kopyaladığı gibi bir metin koyar: `TestClipboard.putText(text)` (`com.pantrixui:testing`). Ekranın `readText()`'i onu okur:

```kotlin
val clipboard = TestClipboard()
val opened = mutableListOf<String>()
setContent { PantrixTestTheme(clipboard = clipboard) { StartScreen(open = { opened += it }) } }
clipboard.putText("{\"a\": 1}")
onPantrixNode("Button", "paste").performClick()
waitForIdle()
assertEquals(listOf("{\"a\": 1}"), opened)
```

Tuşla yapıştırmayı `performPaste(text, clipboard, system)` dener: metni panoya koyar ve yapıştırma tuşuna o sistemin klavyesi gibi basar. Web'de ⌘V tarayıcının kendi yapıştırmasıdır ve makinenin panosundan gelir; bir testin tuşu onu getirmez. Orada yardımcı, tuş inikken sayfaya tarayıcının yapıştırmasını gönderir: odaktaki öğeden yükselen, metni taşıyan bir `ClipboardEvent`. Böylece aynı test beş hedefte de geçer:

```kotlin
onPantrixNode("Screen", "start").performPaste("{\"a\": 1}", clipboard, OperatingSystem.Android)
assertEquals(listOf("{\"a\": 1}"), opened)
```

`system`, ekranın yazıldığı sistemdir, `PantrixTestTheme(system = …)`; temanın varsayılanı Android'dir, yapıştırması Ctrl+V'dir. Bir metin alanına `performTextInput` ile yazılır.

## 7. Tuzaklar {#pitfalls}

:::table
| Tuzak | Ne olur | Ne yapılır | Nerede görüldü |
| :--- | :--- | :--- | :--- |
| **Sahtesi verilmeyen pano** | Testte `LocalClipboard` platformun gerçek panosudur: masaüstünde sistemin panosu. Kopyalayan bir test, makinede o an kopyalanmış ne varsa üstüne yazar | Her testte kendi `Clipboard`'unu ver | Kaynak: `RootNodeOwner.skiko.kt` (`createPlatformClipboard()`) |
| **Ortak kodda metni okumak** | Sahte panoya gelen `ClipEntry`'nin ortak koddaki tek üyesi `clipMetadata`'dır; metne ulaşılamaz | Ortak testte yazmaları say; metni platformun kendi testinde oku (6.3) | Kaynak; bu testler yazılırken |
| **`clipMetadata`** | Masaüstünde, iOS'ta ve web'de `TODO()`'dur: çağrılınca `NotImplementedError` fırlar | Çağırma | Kaynak: dört `PlatformClipboard.*.kt` (CMP-1260) |
| **Güvenli olmayan bağlam** | Düz http ile sunulan sayfada pano yoktur. Compose yazmayı atlar, konsola bir uyarı yazar, hata fırlatmaz: sonucu hatadan anlayan düğme "kopyalandı" der | Yazmadan önce sor: `traits.writable` | Compose'un atlaması kaynakta: `PlatformClipboard.js.kt`, `.wasm.kt`. `writable`'ın "yok" demesi testte, `navigator.clipboard` gizlenerek |
| **Başsız JVM** | `java.awt.headless=true` ile sistemin panosu yoktur. Compose yazmayı atlar, hata fırlatmaz: sonucu hatadan anlayan düğme "kopyalandı" der | Yazmadan önce sor: masaüstünde `writable`, `!GraphicsEnvironment.isHeadless()`'tir | Kaynak: `PlatformClipboard.desktop.kt`. Huy eklenmeden önce geçici bir testte: cevap `true`, geri okunan `null`. Huyun kuralı testte: `desktopClipboardTraits(headless = true)` |
| **`catch (e: Exception)`** | Tarayıcının reddi bir Kotlin `Exception`'ı değildir (JS'te tarayıcının kendi hatası, Wasm'da `JsException`). Yalnızca `Exception` yakalayan kod onu kaçırır; hata düğmenin coroutine'inden fırlar | `Throwable` yakala | Test: yakalama `Exception`'a daraltılınca `a_write_the_browser_refuses_is_answered_false` JS'te de Wasm'da da kırmızı |
| **Yutulan iptal** | `catch (e: Throwable)` iptali de yakalar: terk edilen bir ekranın coroutine'i "kopyalanamadı" cevabıyla yoluna devam eder | Önce `CancellationException`'ı yakala ve yeniden fırlat | Test: `a_copy_whose_coroutine_is_cancelled_is_cancelled_and_answers_nothing` |
| **İki kez söylenen kopya** | Android 13 ve üstünde sistem kopyalananı kendi önizlemesiyle gösterir; uygulama da söylerse aynı şey iki kez söylenir | `announcedBySystem` iken sus; kopyalanamayanı yine söyle | Android'in belgeleri; bkz. 8. bölüm, "Doğrulanmayanlar" |
| **`webMain`'e yazılan karşılık** | `ClipEntry.withPlainText` `webMain`'de çağrılırsa iki hedefin derlemesi geçer; ortak kaynak kümesinin derlemesi (`compileWebMainKotlinMetadata`) ve editör "Unresolved reference 'withPlainText'" der | Karşılığı `jsMain` ve `wasmJsMain`'e ayrı ayrı yaz | Geçici bir dosyayla derlenerek |
| **Deneysel işaretler** | Masaüstünün kurucusu, iOS'un `withPlainText`'i ve okuma tarafındaki `asAwtTransferable`, `getPlainText()`, `clipboardItems` `@ExperimentalComposeUiApi` taşır; Compose'un sonraki bir sürümünde değişebilir | `@OptIn` tek bir yerde, yardımcının içinde dursun | Kaynak |
| **Tek fonksiyonun yerine geçen web testi** | Compose tam pano arayüzü varsa `write`'ı, yoksa `writeText`'i çağırır. Test yalnızca birinin yerine geçmişse öteki gerçek panoya yazar | İkisinin de yerine geç; kopyalamadan önce yerlerinde olduklarına bak | Kaynak: `PlatformClipboard.js.kt`, `.wasm.kt` |
| **Sahte panoda `nativeClipboard`** | Arayüzdeki `nativeClipboard` kullanımdan kalkmıştır ve verilmezse `NotImplementedError` fırlatır. Masaüstünde metin alanının sağ tık menüsü panoyu hâlâ onunla sorar | Kopyalama düğmesi için gerekmez. Aynı testte bir metin alanının menüsü de varsa sahte panoda onu da ver | Kaynak: `Clipboard.kt`, `ClipboardUtils.desktop.kt`. Bu depoda `TextFieldDesktopTest` |
| **Android'de okumak** | `getClipEntry()` panonun içeriğine erişir; ilk erişimde sistem "App pasted from Clipboard" uyarısını gösterir | Yalnızca ne olduğunu sormak için `primaryClipDescription` | Kaynak: `Clipboard.kt`'nin belgesi |
| **⌘V'yi kısayol yapmak** | Web'de `onShortcut` tuşu tarayıcıdan saklar; tarayıcı yapıştırmaz ve `paste` olayı hiç gelmez | Yapıştırmayı `Modifier.onPaste` ile duy | Kaynak: `ComposeWindowInternal.web.kt` (`processClipKeyDown`), tarayıcı yapıştırmayı ancak tuş saklanmadıysa gönderir |
| **iOS'ta iki kez yapıştırmak** | UIKit ⌘V'yi odaktaki alana yapıştırır ve tuşu Compose'a da verir; Compose'un alanı tuşu almaz, çevresindeki bir tuş dinleyicisi aynı metni bir kez daha alır | Odakta UIKit'in bir metin girişi varken tuşa dokunma: `onPaste` böyle yapar | Kaynak: `InputViews.ios.kt` (`pressesBegan`), `NativeTextInputView.ios.kt` (`paste`). Testte iOS'un alanı ⌘V'yi almadı (`OnPasteTest`) |
| **Basışsız okumak** | Tarayıcı okumayı yalnızca bir basıştan hemen sonra yanıtlar; açılışta ya da bir zamanlayıcıdan okumak reddedilir | Okumayı bir düğmeye bağla; ⌘V için `onPaste` | Tarayıcıların belgeleri; denenmedi |
| **Dil taşımayan etiket** | Etiket kullanıcıya görünür bir metindir; koda gömülen bir etiket uygulamanın dili değişince eskisinde kalır | Etiketi çağıran versin, uygulamanın dilinde | Android SDK kaynağı (`ClipData.newPlainText`); test: etiket Android'de gelir, başka yerde gelmez |
:::

## 8. Doğrulama {#verification}

### Testler

:::table
| Test | Nerede koşar | Neye bakar |
| :--- | :--- | :--- |
| `ui/foundation`, `PantrixClipboardTest` | JVM, iOS simülatörü, JS, Wasm (9 test) | Kopyalama panoya bir giriş koyar ve `true` der; yazılacak pano yokken `false` der ve panoya hiç dokunmaz; pano ne fırlatırsa fırlatsın cevap `false`'tur; iptal edilen kopyalama iptal olur, cevap vermez; hatırlanan pano yeniden kurulumda aynıdır, pano ya da huylar değişince yenisidir ve yenisine bakar |
| `ui/foundation`, `PantrixClipboardOnTheDesktopTest` | JVM (4 test) | Gelen şey, metin biçimi verilen metin olan bir `Transferable`'dır; satır sonları, ASCII dışı harfler ve emoji eksiksiz gelir; bellekte tutulan metin soran iş parçacığında, başka bir uygulamanın kopyası onun dışında okunur; masaüstünün huyları: ekranı olan JVM'de yazılır ve okunur, başsız olanda ikisi de olmaz |
| `ui/foundation`, `PantrixClipboardOnAndroidTest` | Robolectric (7 test) | Sistemin panosundaki ilk öğe verilen metindir, etiketi verilen etikettir, türü düz metindir; etiket verilmezse boştur; `announcedBySystem` API 33 ve 34'te doğru, 32 ve 30'da yanlıştır |
| `ui/foundation`, `PantrixClipboardOnIosTest` | iOS simülatörü (3 test) | Gelen girişin `getPlainText()`'i verilen metindir; zor metin eksiksiz gelir; iOS'un huyları |
| `ui/foundation`, `PantrixClipboardOnTheWebTest` | ChromeHeadless'ta JS ve Wasm (5 test) | Test koşucusunun sayfasında `writable` doğrudur; `navigator.clipboard` gizlenince yanlıştır; tarayıcıya verilen öğenin `text/plain` parçası verilen metindir; zor metin eksiksiz gelir; tarayıcının reddettiği yazmanın cevabı `false`'tur |
| `ui/foundation`, `PantrixClipboardReadTest` | JVM, iOS simülatörü, JS, Wasm (6 test) | Kopyalanan metin olduğu gibi, zor metin de eksiksiz geri okunur; son kopya okunur; boş pano metin vermez; okunacak pano yokken panoya hiç sorulmaz; okumayı reddeden pano metin vermez |
| `ui/foundation`, `OnPasteTest` | JVM, iOS simülatörü, JS, Wasm (7 test) | Yapıştırma tuşu panonun metnini odağın çevresindeki düğüme ve zincirde odağı alan düğümden sonra duran düğüme verir, web'de vermez: orada tuş tarayıcınındır; odak dışındaki düğüm duymaz; içteki metin alanı kendisine yapıştırır; içteki düğüm alır; `enabled = false` olan çevresindekine bırakır; boş pano bir şey yapıştırmaz |
| `ui/foundation`, `OnPasteOnTheWebTest` | ChromeHeadless'ta JS ve Wasm (8 test) | Tarayıcının yapıştırması metniyle verilir ve alınmış diye işaretlenir; alınmayan tarayıcıya kalır; bir alanın aldığı verilmez; kapandıktan sonra duyulmaz; düğüm içinde inen tuşun ardından gelen yapıştırmayı duyar, tuşsuz geleni duymaz; pencerenin kökünde `shortcutRoot`'tan sonra da duyar; içteki alır; `enabled = false` olan çevresindekine bırakır |
| `ui/testing`, `TestDoublesTest` | JVM, iOS simülatörü, JS, Wasm | `putText` ile konan metni ekran okur; temizlenince okumaz |
| `ui/testing`, `PantrixAssertionsTest` | JVM, iOS simülatörü, JS, Wasm | `performPaste`'in metnini `onPaste` her hedefte duyar, web'de tarayıcının yapıştırmasıyla; her sistem kendi öncü tuşuyla yapıştırır, ötekinin tuşu bir şey yapıştırmaz; metin her durumda panodadır; pencerenin kökünde `shortcutRoot`'tan sonra da duyulur |
| `ui/testing`, `TestingOnAndroidTest` | Robolectric | Android'de `performPaste` testin panosuna bir `ClipData` koyar ve Ctrl+V onu okur |
| `app/shared`, `CodeBlockTest` | JVM (5 test) | Örnek uygulamanın kopyalama düğmesi kodu panoya koyar ve söyler; reddeden panoyu, panosu olmayan sayfayı "kopyalanamadı" diye söyler; sistemin gösterdiği kopyayı söylemez |
| `app/shared`, `CopyMessageTest` | JVM, iOS simülatörü, JS, Wasm (3 test) | Kopyalamadan sonra ne dendiğinin kuralı |
:::

Android'deki 32 için ayrı bir çalışma zamanı indirilmedi: Robolectric sürüm numarasını yazılabilir bir alanda tutar, test 33'ün çalışma zamanında o alana 32 yazar ve geri koyar. 30, 33 ve 34 kendi çalışma zamanlarında koşar.

### Elle bozulan davranışlar

Her davranış bir kez elle bozuldu ve hangi testin kırmızı olduğuna bakıldı; sonra geri alındı.

*   **Ortak kod:** yazmanın hiç yapılmaması; yazdıktan sonra `false` denmesi; `writable`'a bakılmaması; fırlatan panoya `true` denmesi; yalnızca `Exception` yakalanması; iptalin yutulması; `remember`'ın kaldırılması; anahtarlarından panonun ya da huyların çıkarılması; huyların `LocalClipboardTraits`'ten okunmaması; etiket ile metnin yer değiştirmesi. On birinde de bir test kırmızı oldu.
*   **Platformlar:** her platformda metin yerine etiketin konması; Android'de sınırın 34'e, 32'ye ve 30'a çekilmesi; masaüstünde ve iOS'ta huyların değiştirilmesi; tarayıcıda sorunun hep "var" ve hep "yok" demesi. Hepsinde o platformun bir testi kırmızı oldu.
*   **Yalnızca bir yerde görülen:** `LocalClipboardTraits`'in varsayılanı platformunki yerine boş huylar olunca JVM'de hiçbir test kırmızı olmaz, çünkü orada ikisi aynıdır. Android'de `on_android_14_a_remembered_clipboard_knows_the_system_shows_a_copy_itself` kırmızı olur.
*   **Tuşla yapıştırmak:** masaüstünde bellekte tutulan metnin de ana iş parçacığının dışında okunması (`a_text_held_in_memory_is_read_in_place_and_another_application_s_away_from_the_thread` kırmızı oldu; `ui/testing`'in yapıştırma testleri geçti, okuma o kadar kısa sürdüğü için: düzeltilen bir yarıştır); `performPaste`'in hep Control'le basması (JVM'de `a_paste_is_pressed_as_each_system_presses_it`); web'de sayfaya yapıştırma gönderilmemesi (JS'te yapıştırmanın iki testi de).

### Doğrulanmayanlar

*   **Android 13'ün önizlemesi.** Sistemin kopyalananı kendisi gösterdiği ve uygulamadan ikinci kez söylememesini istediği Android'in belgelerindendir. Bu depoda Android 14 emülatöründe görüldüğü `docs/OPEN_ITEMS.md`'de yazılıdır; bu rehber yazılırken yeniden bakılmadı. Robolectric önizlemeyi çizmez: testler yalnızca huyun hangi sürümde ne dediğine bakar.
*   **Gerçek bir http sayfası.** Güvenli olmayan bağlam, düz http ile sunulan bir sayfada denenmedi. Testte `navigator.clipboard` sayfadan gizlendi; Compose'un o durumda yazmayı atladığı kaynakta okundu, bir testte görülmedi.
*   **Tarayıcının gerçek reddi.** Bir tarayıcının yazmayı hangi koşullarda reddettiğine bakılmadı. Testteki ret, testin kendi koyduğu bir rettir; hiçbir test gerçek panoya yazmadı.
*   **Tam pano arayüzü olmayan tarayıcı.** Compose'un `writeText`'e düştüğü yol (kaynaktaki nota göre 127'den eski Firefox) hiçbir tarayıcıda koşmadı. Testler yalnızca ChromeHeadless'ta koştu (154 ve 156).
*   **Gerçek panolar.** Masaüstünde, iOS'ta ve tarayıcıda hiçbir test sistemin panosuna yazmadı. Kopyalananın başka bir uygulamaya yapıştırılabildiğine bu rehber için bakılmadı.
*   **Android'de çok büyük metin.** Sistemin taşıyamayacağı kadar büyük bir metnin reddedildiği denenmedi. Kaynakta görülen, servisin hatasının bir `RuntimeException` olarak fırladığıdır.
*   **Masaüstünde kullanılamayan pano.** `IllegalStateException` JDK'nın belgesinde yazılıdır; görülmedi.
*   **Başsız bir JVM'nin kendisi.** AWT, ekranı olup olmadığına bir JVM için bir kez karar verir ve sonradan değiştirilemez. Test kuralı dener (`desktopClipboardTraits`) ve koştuğu JVM'nin cevabının o kuraldan geldiğine bakar; başsız bir JVM'de koşmadı.
*   **iOS simülatörünün panosu.** Simülatör panosunu üstünde koştuğu makineyle paylaşabilir; iOS testleri bu yüzden sahte panoyla koşar. Paylaşımın kendisi denenmedi.
*   **Susan sistemler.** Masaüstünde, iOS'ta, tarayıcıda ve Android 12L ile altında sistemin bir kopyayı kendisi göstermediği kütüphanenin kabulüdür (`announcedBySystem = false`); gerçek cihazlarda bakılmadı.
*   **Ekran okuyucu.** Kopyalamanın sesli olarak nasıl duyurulduğuna bakılmadı.
*   **iOS'ta donanım klavyesiyle ⌘V.** UIKit'in odaktaki alana yapıştırdığı ve tuşu Compose'a da verdiği kaynakta okundu. Buradan simülatöre ⌘V basılamadı: odakta UIKit'in metin girişi varken düğümün tuşu bıraktığı görülmedi.
*   **Tarayıcının okuma izni.** `readText()`'in Chrome'da ve Safari'de sorduğu soru görülmedi; testlerde okunan testin kendi panosudur.
*   **Gerçek bir yapıştırma.** Tarayıcının gerçek bir ⌘V'de `paste` olayını Compose'un girişine gönderdiği kaynakta okundu; testte olay sayfaya testin kendisi gönderdi.

## 9. Bu depoda nerede {#repository toc="9. Depoda"}

:::table
| Dosya | Ne |
| :--- | :--- |
| `ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/platform/PantrixClipboard.kt` | `ClipboardTraits`, `currentClipboardTraits`, `LocalClipboardTraits`, `PantrixClipboard`, `rememberPantrixClipboard` |
| `ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/platform/OnPaste.kt` | `Modifier.onPaste` |
| `ui/foundation/src/androidMain/…/PantrixClipboard.android.kt`, `iosMain/…/PantrixClipboard.ios.kt`, `jvmMain/…/PantrixClipboard.jvm.kt` | Üç platformun huyları, panoya konan türü ve ondan okunan metin; iOS'ta UIKit'in metin girişine bakış |
| `ui/foundation/src/webMain/…/PantrixClipboard.web.kt` | Tarayıcının huyları, tarayıcıya sorulan soru ve tarayıcının yapıştırmasının dinlenmesi |
| `ui/foundation/src/jsMain/…/PantrixClipboard.js.kt`, `wasmJsMain/…/PantrixClipboard.wasmJs.kt` | İki web hedefinde panoya konan tür ve ondan okunan metin |
| `ui/foundation/src/commonTest/…/platform/RecordingClipboard.kt` | Testlerin sahte panosu |
| `ui/foundation/src/commonTest`, `jvmTest`, `androidHostTest`, `iosTest`, `webTest` altında `…/platform/PantrixClipboard*Test.kt` ve `…/platform/OnPaste*Test.kt` | 8. bölümdeki testler |
| `app/shared/src/commonMain/kotlin/com/pantrixui/cmp/demo/page/CodeBlock.kt` | Örnek uygulamanın kopyalama düğmesi ve ne diyeceğinin kuralı (`copyMessage`) |
:::

## 10. Açık kalanlar {#open}

*   Yardımcı düz metinden başkasını taşımaz ve bir yapıştırma menüsü çizmez (bkz. 5. bölüm).
*   iOS'ta donanım klavyesiyle ⌘V'nin, odakta bir alan varken alana bırakıldığı cihazda görülmedi (bkz. 8. bölüm).
*   Masaüstünde ve iOS'ta dayandığı Compose API'si deneyseldir (`@ExperimentalComposeUiApi`); Compose Multiplatform'un sonraki bir sürümünde değişebilir.
*   Yalnızca testlerde görüldü: gerçek cihazlarda, gerçek tarayıcılarda ve düz http ile sunulan bir sayfada bakılmadı.
