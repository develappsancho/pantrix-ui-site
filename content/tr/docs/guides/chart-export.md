---
title: Bir grafiği dosya olarak dışa aktarma
description: Bir Pantrix grafiğinin PNG, SVG ve PDF olarak nasıl dışa aktarıldığı, kütüphanenin neyi yapıp neyi uygulamaya bıraktığı, üretilen dosyanın kullanıcıya her platformda nasıl ulaştırılacağı ve üç dosya türünün sınırları.
---

Kütüphane dosyayı **üretir** ve uygulamaya **verir**; kaydetmez, paylaşmaz, indirmez. Bunun nedeni 3. bölümdedir: bir dosyayı kullanıcıya ulaştırmak dört platformda dört ayrı iştir ve Android'de bir dosya sağlayıcısı ister.

Burada kütüphane hakkında söylenen her şey bu depoda koşan testlerde ya da üretilen dosyalara bakılarak görülmüştür; hangisinin nerede görüldüğü 6. bölümde yazılıdır. 3. bölümdeki platform kodu demonun kendi kodudur (`app/shared`'deki `com.pantrixui.cmp.demo.platform` paketi) ve dört platformda derlenir; nerede denendiği 6. bölümdedir. Oradaki tek taslak, Android'in kaydetme penceresi, derlenmedi.

## 1. Kullanım {#use}

Dışa aktarma parametriktir: `export` verilmeyen grafikte hiçbir şey görünmez.

```kotlin
PantrixLineChart(
    data = months,
    x = { it.name },
    values = revenue,
    export = ChartExport(fileName = "gelir") { file ->
        // file.name = "gelir.svg", file.mimeType = "image/svg+xml", file.bytes = içerik
        share(file)
    }
)
```

`export` verilince grafiğin kendi düğmelerinin arasında (yakınlaştırma düğmeleri varsa onların yanında) bir dışa aktarma düğmesi çıkar. Okuyucu düğmeye basar, türü seçer; grafik dosyayı üretir ve `onExport`'a verir.

:::table
| Alan | Ne söyler | Varsayılan |
| :--- | :--- | :--- |
| `formats` | Okuyucunun seçtiği türler, sırasıyla. Tek tür verilirse seçim açılmaz, düğme o dosyayı üretir | Üçü de: PNG, SVG, PDF |
| `fileName` | Dosyanın adı, uzantısız | `"chart"` |
| `scale` | PNG'nin dp başına piksel sayısı. Ekranın yoğunluğundan bağımsızdır | `2f` |
| `background` | Resmin zemini. `Color.Unspecified` temanın `background`'udur; `Color.Transparent` zemin çizmez | Temanın zemini |
| `padding` | Resmin grafiğin çevresinde, her yanında bıraktığı pay. Zemin payın altında da vardır | `12.dp` |
| `onExport` | Üretilen `ChartExportFile` buna verilir | Zorunlu |
:::

`export`, `PantrixChart` ile onun üstündeki altı kısa grafikte (çizgi, alan, çubuk, aralık, şelale, mum) ve öteki on dört türün hepsinde vardır: pasta, gauge, ilerleme halkası, meter group, status tracker, sparkline, scatter, radar, polar, heatmap, treemap, huni, sankey ve Gantt. Her birinde imzanın sonundadır; yalnızca `PantrixProgressCircle`'da `content`'ten öncedir, sondaki lambda yine halkanın içindeki içerik kalsın diye.

Düğme grafiğin üstünde, satırların bittiği yanda bir satırda durur. Eksenli grafikte yakınlaştırma düğmeleriyle aynı satırdadır. Yüksekliği verilmeyen grafik bu satır kadar uzar; yüksekliği verilen grafik yüksekliğini korur, çizimi o kadar kısalır.

:::sketch chart-export
:::

## 2. Dosyada ne var {#what-is-in-the-file}

Dosya, grafiği **ekrandaki hâliyle** gösterir:

*   Lejanttan çıkarılan seri dosyada da çıkmıştır (lejantta soluk durur).
*   Yakından bakılan grafiğin (`zoom`) görünen kısmı dosyaya çıkar.
*   Gösterilen yer, tooltip, yakınlaştırma düğmeleri ve gezgin şeridi dosyada **yoktur**.
*   Lejant, ekranda nerede durursa dursun, dosyada çizim alanının altındadır. Heatmap'in ve ölçekli treemap'in renk ölçeği de öyle: en küçük değer, renk geçişi ve en büyük değer, satırların başladığı yandan.
*   Çağıranın bir composable olarak verdiği içerik dosyada yoktur: pastanın deliğindeki `center`, gauge'un ortasındaki `center`, ilerleme halkasının içindeki `content`. Gauge'un ortaya kendi yazdığı değer dosyadadır. Status tracker'ın altında gösterilen bloğun adını yazan satır da dosyada yoktur.
*   Grafiğin çevresinde `padding` kadar pay vardır. Grafiğin ekrandaki kutusu yazılarının bittiği yerde biter (çevresindeki boşluğu içinde durduğu kart verir); payı olmayan bir resimde eksenin sayıları resmin tam kenarında durur.
*   Giriş animasyonu sürerken alınan dosya da grafiğin son hâlini gösterir.

Üç tür aynı çizimden üretilir; aralarındaki farklar şunlardır:

:::table
| Tür | Ne | Yazılar | Boyut (on iki aylık iki serili bir grafik) |
| :--- | :--- | :--- | :--- |
| PNG | Piksellerden resim; `scale` kadar keskin | Uygulamanın yazı tipiyle, ekrandaki gibi | Onlarca kilobayt |
| SVG | Çizgi ve şekil; her boyutta keskin | Gerçek metin (`<text>`), dosyayı açanın sans-serif yazı tipiyle | Birkaç kilobayt |
| PDF | Tek sayfa, çizgi ve şekil; her boyutta keskin | Helvetica ile gerçek metin; Helvetica'nın yazamadığı satır resim olarak | Birkaç kilobayt |
:::

### Yazılar neden ekrandakinden farklı

SVG ve PDF uygulamanın yazı tipini taşımaz: kütüphanenin elinde yazı tipinin dosyası yoktur. SVG'de yazılar dosyayı açan programın sans-serif yazı tipiyle, PDF'te her PDF okuyucusunda bulunan Helvetica ile yazılır. Bu yazı tiplerinin harfleri uygulamanınkinden dar ya da geniş olabilir; her satır, ekranda kapladığı yerin **ortasına** yazılır. Bir eksenin yanındaki sayı bu yüzden eksene bir iki piksel yakın ya da uzak durabilir.

PDF'te Helvetica, Batı Avrupa dillerinin ve Türkçenin harflerini yazar (Türkçenin altı harfi için dosya kendi kodlamasını taşır). İçinde Helvetica'nın yazamadığı bir harf olan satır (Arapça bir ad, `₺` işareti) sayfaya **kendi resmi** olarak konur: ekran yoğunluğunun dört katı keskinlikte, ama öteki her şey gibi sonsuz keskin değil, ve metin olarak seçilemez.

## 3. Dosyayı kullanıcıya ulaştırmak {#getting-the-file-to-the-user}

`onExport` bir `ChartExportFile` alır: `name`, `mimeType`, `format`, `bytes`. Gerisi platformun işidir.

:::table
| Platform | Alışılmış yol | Ne ister |
| :--- | :--- | :--- |
| Android | Paylaşım sayfası (`Intent.ACTION_SEND`) ya da "farklı kaydet" (`CreateDocument`) | Paylaşmak için manifestte bir `FileProvider`; kaydetmek için bir `Activity` sonucu |
| iOS | Paylaşım sayfası (`UIActivityViewController`) | Geçici bir dosya ve sayfayı sunacak bir görünüm denetleyicisi |
| Masaüstü | Kaydetme penceresi | Yok |
| Web | Tarayıcının indirmesi | Yok |
:::

Kütüphane bunu şimdilik kendisi yapmaz. Android'de paylaşmak bir `FileProvider` ve bir yol dosyası ister; bir kütüphane onları kendi manifestinde tanımlayabilir (uygulamanın manifestine birleşir), ama o zaman kütüphaneyi kullanan her uygulamaya bir sağlayıcı eklenir. Bunun kütüphaneye alınması açık bir karardır (`docs/OPEN_ITEMS.md`, 2. bölüm).

Demo bu yolları `app/shared`'deki `com.pantrixui.cmp.demo.platform` paketinde yazar: ortak kodda tek bir bildirim, her platformun kaynak kümesinde onun karşılığı (`FileSaver.android.kt`, `FileSaver.ios.kt`, `FileSaver.jvm.kt`, `FileSaver.web.kt`). Grafik örneklerindeki dışa aktarma örneği (`ChartExported`) üretilen dosyayı önce gösterir, "Dosyayı kaydet" düğmesiyle de verir.

```kotlin
// commonMain
internal fun interface FileSaver {
    fun save(name: String, mimeType: String, bytes: ByteArray)
}

@Composable
internal expect fun rememberFileSaver(): FileSaver

// Grafikte:
val saver = rememberFileSaver()
PantrixLineChart(
    // ...
    export = ChartExport { file -> saver.save(file.name, file.mimeType, file.bytes) }
)
```

Bildirim bir örnek dosyasında değil ayrı bir pakettedir: demonun her örnek dosyası İngilizce ikiziyle başka bir pakete kopyalanır, ve `expect` bir bildirimin kopyasının karşılığı olmaz. Aşağıdaki parçalar o dosyalardandır.

### Android: paylaşım sayfası

```kotlin
val context = LocalContext.current
// save:
val folder = File(context.cacheDir, "exports").apply { mkdirs() }
val file = File(folder, name).apply { writeBytes(bytes) }
val uri = FileProvider.getUriForFile(context, context.packageName + ".files", file)
val send = Intent(Intent.ACTION_SEND)
    .setType(mimeType)
    .putExtra(Intent.EXTRA_STREAM, uri)
    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
context.startActivity(Intent.createChooser(send, name))
```

Sayfada seçilen uygulama dosyayı `FileProvider`'ın verdiği adresle okur. Sağlayıcı uygulamanın manifestinde tanımlanır ve önbellekte yalnızca bir klasörü açar:

```xml
<!-- AndroidManifest.xml, <application> içinde -->
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.files"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

```xml
<!-- res/xml/file_paths.xml -->
<paths>
    <cache-path name="exports" path="exports/" />
</paths>
```

`FileProvider`, `androidx.core` kitaplığındadır (demo `androidMain`'e `androidx.core.ktx`'i ekler). Manifestteki `authorities` koddaki adresle aynı olmalıdır; değilse `getUriForFile` hata fırlatır. Dosya klasörün dışındaysa da öyle.

### Android: kaydetme penceresi

Demoda yoktur; **bu parça derlenmedi.** Okuyucu dosyanın nereye yazılacağını seçer, sağlayıcı gerekmez:

```kotlin
val pending = remember { mutableStateOf<ChartExportFile?>(null) }
val saver = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri ->
    val file = pending.value
    if (uri != null && file != null) {
        context.contentResolver.openOutputStream(uri)?.use { it.write(file.bytes) }
    }
    pending.value = null
}
// onExport:
{ file -> pending.value = file; saver.launch(file.name) }
```

### iOS: paylaşım sayfası

```kotlin
val path = NSTemporaryDirectory() + name
val data = if (bytes.isEmpty()) {
    NSData()
} else {
    bytes.usePinned { NSData.create(bytes = it.addressOf(0), length = bytes.size.toULong()) }
}
data.writeToFile(path, atomically = true)
var top = UIApplication.sharedApplication.keyWindow?.rootViewController
while (top?.presentedViewController != null) top = top.presentedViewController
val sheet = UIActivityViewController(listOf(NSURL.fileURLWithPath(path)), null)
sheet.popoverPresentationController?.sourceView = top?.view
top?.presentViewController(sheet, animated = true, completion = null)
```

"Dosyalara Kaydet" de sayfanın içindedir. Sayfa en üstteki görünüm denetleyicisinden sunulur: başka bir denetleyiciyi sunmakta olan bir denetleyici ikinci bir sunumu reddeder. iPad'de sayfa bir açılır pencere olarak sunulur ve neye bağlanacağını (`popoverPresentationController.sourceView`) ister. `NSData.create`'e boş bir dizinin adresi verilemez: boş dosya ayrı yazılır.

### Masaüstü: kaydetme penceresi

```kotlin
val dialog = FileDialog(null as Frame?, name, FileDialog.SAVE).apply {
    file = name
    isVisible = true
}
val chosen = dialog.file
if (chosen != null) File(dialog.directory, chosen).writeBytes(bytes)
```

Pencere kapatılırsa `dialog.file` boştur ve hiçbir şey yazılmaz.

### Web: indirme

```kotlin
val scope = rememberCoroutineScope()
// save:
val url = URL.createObjectURL(blobOf(bytes, mimeType))
val link = document.createElement("a") as HTMLAnchorElement
link.href = url
link.download = name
link.click()
scope.launch {
    delay(40.seconds)
    URL.revokeObjectURL(url)
}
```

Adres basıştan hemen sonra geri alınmaz: indirme basışla başlar ama dosyayı sonra okur, ve adresi hemen geri alınan indirmeyi bazı tarayıcılar yarıda keser.

`blobOf`, `js` ve `wasmJs` hedeflerinde ayrı yazılır; ikisinin ortak kaynak kümesi (`webMain`) bir `ByteArray`'i tarayıcının bayt dizisine çeviremez:

```kotlin
// jsMain: Kotlin/JS'te bir ByteArray zaten bir Int8Array'dir
internal actual fun blobOf(bytes: ByteArray, type: String): Blob =
    Blob(arrayOf(bytes.unsafeCast<Int8Array>()), BlobPropertyBag(type = type))

// wasmJsMain: baytlar tarayıcının belleğine bir bir kopyalanır
internal actual fun blobOf(bytes: ByteArray, type: String): Blob {
    val array = Uint8Array(bytes.size)
    bytes.forEachIndexed { index, byte -> array[index] = byte }
    val parts = JsArray<JsAny?>()
    parts[0] = array
    return Blob(parts, BlobPropertyBag(type = type))
}
```

## 4. Bilinmesi gerekenler {#what-to-know}

*   **Dosya basışla birlikte, ana iş parçacığında üretilir.** On iki yerli bir grafikte gözle fark edilmez. On binlerce noktalı bir grafiğin vektör dosyası, seyreltilmiş çizgisiyle birlikte yüzlerce kilobayt olabilir ve üretimi bir kareyi geçebilir; süresi ölçülmedi.
*   **Renk geçişli alan dolgusu** SVG'de gerçek bir geçiştir, PDF'te renklerin yanında bir gri maskeyle yazılır: altındaki çubuklar ekrandaki gibi görünür.
*   **PDF'in her sayısı sayfanın kendi biriminde yazılır**, sayfa bir bütün olarak ölçeklenip çevrilmez. macOS'un PDF çizimi (Önizleme, Quick Look), ölçeklenmiş bir sayfanın altında kurulan maskeyi yanlış boyutta çiziyor ve geçişli dolgu kayboluyordu; bu, ekran yoğunluğu 1'den farklı olan her cihazda (her telefonda) üretilen dosyada görülürdü.
*   **Değeri olmayan grafiğin dosyası "Veri yok" yazar.** Grafiğe `empty` ile kendi içeriği verilmişse o içerik yalnızca ekrandadır: bir composable bir dosyaya çizilemez, dosya temanın sözünü yazar.
*   **Koyu temada** dosyanın zemini de koyudur ve yazıları açıktır. Kâğıda basılacak bir dosya için `background = Color.White` yetmez: yazıların rengi temadan gelir. Böyle bir dosya açık temada üretilmelidir.
*   **Hareket ve görünüm.** Dosya, grafiğin ekranda son yerleştiği boyuttadır: dar bir telefonda üretilen dosya dar, geniş bir pencerede üretilen geniştir.
*   **Android'in ana makine testlerinde (Robolectric) SVG ve PDF üretilemez.** Bir çizimin yolu adım adım Compose'un yol yineleyicisiyle okunur; o da Android'de `androidx.graphics.path`'in yerel kitaplığını yükler. Kitaplık uygulamayla birlikte gelir (cihazda ve emülatörde vardır), testlerin koştuğu makinede yoktur: düğmeye basıp SVG ya da PDF seçen bir Robolectric testi `UnsatisfiedLinkError` ile düşer. PNG orada da üretilir.

## 5. Otomasyon ve bildirim {#automation-and-reporting}

Dışa aktarma düğmesinin etiketi grafiğin kendi etiketinin ardından `.Export`'tur: `Chart_<tagKey>.Export`, `PieChart_<tagKey>.Export`, `Gantt_<tagKey>.Export` ve ötekiler. Tür seçimi açılıyorsa satırlarının etiketleri `.Export_png`, `.Export_svg` ve `.Export_pdf`'tir. Bir dosya üretilince grafiğin kendi bileşen adıyla (`Chart`, `PieChart`, `Gauge`, `Gantt` …) `export` olayı bildirilir; `format` özelliği dosyanın uzantısıdır (`png`, `svg`, `pdf`). Dosyanın adı ve içeriği bildirilmez.

## 6. Nasıl doğrulandı {#verification}

:::table
| Ne | Nerede |
| :--- | :--- |
| Çizimin çizgi, şekil ve söz olarak saklanması; SVG ve PDF'in yazımı; PDF'in parça tablosu ve akış uzunluğu | `ChartExportFilesTest` (masaüstü, iOS simülatörü ve iki tarayıcı hedefi) |
| Düğme, tür seçimi ve kapanması, dosyanın adı ve türü, PNG'nin piksel boyutu, zemin, pay, ekran yoğunluğu, görünen kısım, çıkarılan seri, kesikli çizgiler, işaretler, değer etiketleri, eksen başlığı, Arapça adın PDF'teki resmi | `ChartExportTest` (aynı dört hedef) |
| Resmin lejantı: satırlar, ortalama, örnek ve ad, sağdan sola, en yüksek ada göre satır | `ChartExportLegendTest` (aynı dört hedef) |
| Öteki on dört tür: düğmenin yeri, her türün dosyasının adı, olayı ve yazıları, gösterilen yerin dosyada olmaması, pasta ve gauge ortası, heatmap'in ölçeği ve sağdan sola, düğme satırının yükseklikle ilişkisi | `ChartExportKindsTest` (aynı dört hedef) |
| Öteki türler web'de | 2026-10-15'te web demosunda pasta, heatmap, gauge, ilerleme halkası, sparkline ve Gantt'ın PNG'si üretilip ekrandakiyle karşılaştırıldı (fare altındaki hücrenin tooltip'i dosyada yoktu); SVG ve PDF'leri bir tarayıcıda açılmadı |
| PNG'nin pikselleri: çevrede zemin, içeride grafik | `ChartExportDesktopTest` (masaüstü, Skia ile geri okunur), `ChartExportOnAndroidTest` (Android, Robolectric: Android'in kendi bitmap'iyle) |
| Üç türün görünüşü | Masaüstünde üretilen dosyalara macOS'un çizimiyle (Quick Look, `sips`) bakıldı: ekran yoğunluğu 1 ve 2,75 |
| Android'de üretim | Emülatörde (ekran yoğunluğu 3,5) üç tür üretildi; PNG demoda geri gösterildi, üç dosya cihazdan alınıp macOS'ta çizildi |
| iOS'ta üretim | Simülatörde (ekran yoğunluğu 3) üç tür üretildi; PNG demoda geri gösterildi, üç dosya simülatörden alınıp macOS'ta çizildi |
| 3. bölümdeki demo kodu | Dört platformda derlendi. iOS simülatöründe PDF seçilip "Dosyayı kaydet"e basıldı: paylaşım sayfası dosyanın adıyla, türüyle ve boyutuyla açıldı ("visits", PDF, 5 KB), "Dosyalara Kaydet" sayfadaydı; bir yere kaydedilmedi |
:::

Doğrulanamayanlar: eksenli grafiğin dosyalarına web'de bakılamadı (tarayıcı paneli ekranda değildi; testler tarayıcıda koşuyor ama üretilen dosya bir tarayıcıda açılmadı); öteki türlerin web'de yalnızca PNG'sine bakıldı, Android ve iOS'ta dosyalarına bakılmadı; PDF'e Adobe Acrobat, Chrome ve Firefox'un çizimiyle bakılmadı (yalnızca macOS'unkiyle); SVG'ye bir tarayıcıda ve bir çizim programında bakılmadı (yalnızca macOS'un Quick Look'uyla); 3. bölümdeki demo kodu Android'de, masaüstünde ve web'de yalnızca derlendi, denenmedi (Android'in emülatörü o sırada başka bir iş için kullanılıyordu; masaüstünün kaydetme penceresi açılmadı; web'e bakılamadı); Android'in kaydetme penceresi taslağı derlenmedi; basılı çıktıya bakılmadı; gerçek bir telefonda denenmedi.
