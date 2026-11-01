---
title: Dosya seçmek ve bırakmak
description: Compose Multiplatform'da kullanıcının uygulamaya bir dosya vermesinin (platformun seçicisinden seçerek ya da bir bölgenin üstüne sürükleyip bırakarak) neden ortak kodda doğrudan yapılamadığı, kütüphanenin bunun için ne verdiği, dosyanın nasıl okunduğu ve bunun nasıl sınandığı. Sorun Pantrix'e özgü değildir: kullanıcıdan dosya alan her Compose Multiplatform projesinde aynı şekilde görülür.
---

Compose hakkında burada söylenen her şey Compose Multiplatform 1.12.1'in kaynaklarında okunmuş ya da bu depoda koşan bir testte, bir cihazda veya tarayıcıda görülmüştür; hangisinin nerede görüldüğü 7. ve 8. bölümlerde yazılıdır.

## 1. Sorun {#problem}

:::aside files
### Belirti

*   Ortak Compose'da bir dosya seçici yoktur. "Dosya seç" düğmesinin arkasında her platformda başka bir şey vardır: Android'de bir etkinlik sonucu (`ACTION_OPEN_DOCUMENT` ya da fotoğraf seçicisi), iOS'ta sunulan bir `UIDocumentPickerViewController` ya da `PHPickerViewController`, masaüstünde işletim sisteminin penceresi, web'de bir `<input type="file">`.
*   Seçilen dosya da her platformda başka bir türdür: Android'de bir içerik `Uri`'si, iOS'ta bir `NSURL`, masaüstünde bir `java.io.File`, web'de tarayıcının `File`'ı. Ortak kod bunlardan hiçbirini tanımaz.
*   Bırakma için Compose'un ortak bir `Modifier.dragAndDropTarget`'ı vardır, ama verdiği `DragAndDropEvent` her platformda başka şeyler taşır ve dosyaları ortak koddan okunamaz.
*   Bir dosyayı sürükleyip getirmek işaretçinin işidir: telefonda çoğu zaman sürüklenecek bir şey yoktur. Yalnızca bırakmayla dosya alan bir bölge, telefonda ve klavyeyle kullanılamaz.
:::

### Neden

Compose'un bırakma olayı her platformda kendi türünü sarar (Compose Multiplatform 1.12.1, `ui` modülü):

:::table
| Platform | `DragAndDropEvent` neyi taşır | Dosyalar nasıl okunur |
| :--- | :--- | :--- |
| Android | `toAndroidDragEvent()`: bir `DragEvent` | `clipData`'nın öğelerindeki içerik `Uri`'leri. Başka bir uygulamanınkiler ancak bırakma sürerken `Activity.requestDragAndDropPermissions(event)` istenirse okunabilir. |
| iOS | `items`: `UIDragItem`'lar (`@ExperimentalComposeUiApi`) | Her öğenin `itemProvider`'ı dosyasını `loadFileRepresentation…` ile yükler. Verdiği dosya yükleme işleyicisi dönünce silinir: içinde kopyalanmalıdır. |
| Masaüstü | `dragData()`: bir `DragData` (`@ExperimentalComposeUiApi`) | `DragData.FilesList.readFiles()`: diskteki dosyaların adresleri (`file:/…`). |
| Web | `transferData?.domDataTransferOrNull`: tarayıcının `DataTransfer`'ı | `files`. Tarayıcı bunları yalnızca `drop` olayının içinde verir: sonradan okunamaz. |
:::

Seçiciler için ortak bir şey hiç yoktur; her biri platformun kendi koduyla açılır ve sonucu kendi türünde verir.

## 2. Çözüm: dört parça {#fix}

Foundation'da (`com.pantrixui.foundation.files`):

1.  **`PantrixFile`**: ad, boyut (bilinmiyorsa `null`), MIME türü ve `suspend fun readBytes()`. Hiçbir şey `readBytes` çağrılana kadar okunmaz. Platformun kendi türü de oradadır: Android'de `uri`, iOS'ta `url`, masaüstünde `file`, web'de `domFile`. Testler ve uygulamanın kendi ürettiği dosyalar için bellekten bir kurucusu vardır: `PantrixFile(name, bytes, mimeType)`.
2.  **`PantrixFileType`**: bir MIME türü (alt türü `*` olabilir) ve uzantılar. Hazırları: `Any`, `Images`, `Videos`, `Audio`, `Pdf`, `Text`. Bir dosya MIME türü uyduğunda **ya da** uzantısı listede olduğunda türe uyar; platformun tür söylemediği bir dosya da adıyla tanınır.
3.  **`FilePicker` ve `rememberFilePicker()`**: `pick(FilePickRequest(accept, multiple)) { files -> … }`. Platformun belge seçicisini açar; yalnızca görsel istenince (`FilePickRequest.imagesOnly`) Android ve iOS fotoğraf seçicisini açar. Hiçbiri izin istemez. `FilePicker` tek yöntemli bir arayüzdür: test kendi seçicisini verir. **`LocalFilePicker`** bir seçici verdiğinde `rememberFilePicker()` platformunkinin yerine onu döndürür: bir test ya da önizleme bir ekranın bütün bölgelerine aynı seçiciyi bir kerede verir.
4.  **`Modifier.fileDropTarget(enabled, onOver) { files -> … }`**: bırakılanı `PantrixFile` listesine çevirir. Android'de okuma iznini ister, iOS'ta dosyaları uygulamanın geçici klasörüne kopyalar, web'de dosyaları bırakma sürerken alır. `onOver`, dosya tutabilecek bir şey üstüne gelince `true`, ayrılınca `false` söyler.

Bileşenlerde (`com.pantrixui.components.elements.filedrop`) bunların üstünde **`PantrixFileDrop`** (basınca seçiciyi açan, üstüne bırakılanı alan, gelenleri tür, boyut ve sayıya göre eleyen bölge) ve **`PantrixFileItem`** (alınan bir dosyanın satırı) durur.

## 3. Pantrix ile {#with}

### 3.1. bir formun ekleri

```kotlin
var attachments by remember { mutableStateOf(emptyList<PantrixFile>()) }
var refused by remember { mutableStateOf(emptyList<RejectedFile>()) }

PantrixFileDrop(
    onFiles = { files -> attachments += files },
    accept = listOf(PantrixFileType.Pdf, PantrixFileType.Images),
    multiple = true,
    maxSize = 10_000_000,
    onRejected = { refused = it },
    supportingText = if (refused.isEmpty()) "PDF ya da görsel, en çok 10 MB" else "Alınmadı: ${refused.first().file.name}",
    error = refused.isNotEmpty(),
    tagKey = "attachments"
)
attachments.forEach { file ->
    PantrixFileItem(name = file.name, size = file.size, onRemove = { attachments -= file })
}
```

*   Bölge durum tutmaz: alınanları uygulama tutar ve satır olarak dizer.
*   Uymayanlar `onRejected`'a nedenleriyle (`FileRejection.Type`, `Size`, `Count`) gider; ne söyleneceği uygulamanındır.
*   Ekran okuyucuya kaç dosyanın alındığı ve kaçının alınmadığı kibarca söylenir.

### 3.2. dosyayı okumak

```kotlin
scope.launch {
    val bytes = file.readBytes()          // Android, masaüstü ve iOS'ta ana iş parçacığının dışında
    upload(file.name, file.mimeType, bytes)
}
```

Büyük bir dosyayı akış olarak okumak isteyen platformun kendi türünü kullanır: Android'de `contentResolver.openInputStream(file.uri!!)`, masaüstünde `file.file!!.inputStream()`.

### 3.3. bölge olmadan

Kendi tasarımı olan bir uygulama yalnızca foundation'ın parçalarını kullanabilir:

```kotlin
val picker = rememberFilePicker()
var over by remember { mutableStateOf(false) }
Box(
    Modifier
        .fileDropTarget(onOver = { over = it }) { files -> attach(files) }
        .clickable { picker.pick(FilePickRequest(listOf(PantrixFileType.Images))) { attach(it) } }
)
```

Web'de seçici yalnızca bir kullanıcı basışının içinde açılır: `pick` bir tıklamadan ya da bir tuştan çağrılmalıdır.

## 4. Pantrix olmadan {#without}

Aşağıdakiler kütüphanesiz bir projeye kopyalanabilecek en kısa hâllerdir; Pantrix'in kodu (`ui/foundation/src/*Main/kotlin/com/pantrixui/foundation/files/`) aynı şeyi testleri ve kenar durumlarıyla yapar.

**Android, seçici** (`androidx.activity` gerekir; Compose UI onu zaten getirir, ama yalnızca çalışma zamanında: derlemek için açıkça yazılmalıdır):

```kotlin
val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris -> /* … */ }
launcher.launch(arrayOf("application/pdf", "image/*"))
// Yalnızca görsel: ActivityResultContracts.PickVisualMedia(), PickVisualMediaRequest(PickVisualMedia.ImageOnly)
```

**Android, bırakma:**

```kotlin
Modifier.dragAndDropTarget(
    shouldStartDragAndDrop = { event -> event.toAndroidDragEvent().clipDescription?.hasMimeType("*/*") == true },
    target = object : DragAndDropTarget {
        override fun onDrop(event: DragAndDropEvent): Boolean {
            val drag = event.toAndroidDragEvent()
            activity.requestDragAndDropPermissions(drag)   // bırakma sürerken
            val uris = (0 until drag.clipData.itemCount).mapNotNull { drag.clipData.getItemAt(it).uri }
            return uris.isNotEmpty()
        }
    }
)
```

**iOS, seçici** (`asCopy = true`: dosya uygulamanın klasörüne kopyalanır, güvenlik kapsamı gerekmez):

```kotlin
val picker = UIDocumentPickerViewController(forOpeningContentTypes = listOf(UTTypePDF), asCopy = true)
picker.delegate = delegate              // UIKit temsilciyi zayıf tutar: bir yerde saklanmalıdır
LocalUIViewController.current.presentViewController(picker, animated = true, completion = null)
```

**Masaüstü, seçici:**

```kotlin
val dialog = FileDialog(null as Frame?, "Dosya seçin", FileDialog.LOAD).apply {
    isMultipleMode = true
    filenameFilter = FilenameFilter { _, name -> name.endsWith(".pdf", ignoreCase = true) }
    file = "*.pdf"                       // Windows süzgeci çağırmaz, adı okur
    isVisible = true                     // kullanıcı kapatana kadar bekler
}
val files = dialog.files
```

**Web, seçici ve bırakma:**

```kotlin
val input = document.createElement("input") as HTMLInputElement
input.type = "file"; input.accept = "application/pdf,.pdf"; input.multiple = true
input.addEventListener("change", { /* input.files */ })
input.click()                            // bir kullanıcı basışının içinde

// Bırakma: event.transferData?.domDataTransferOrNull?.files, drop olayının içinde okunur.
```

## 5. Yardımcının yapmadıkları {#limits toc="5. Yapmadıkları"}

*   **Akış olarak okuma.** `readBytes()` dosyanın tamamını belleğe alır. Akış için platformun kendi türü kullanılır (3.2).
*   **Kamera.** Fotoğraf seçicisi kullanıcının resimlerini gösterir; o an fotoğraf çektirmez.
*   **Dosya kaydetmek.** Bu belge yalnızca almayı anlatır. Demo'nun kaydetmesi `app/shared/.../platform/FileSaver.kt`'tadır (`docs/guide/chart-export/CHART_EXPORT_GUIDE.md`, 3. bölüm).
*   **Kalıcı izin.** Android'de seçilen bir dosyanın okuma izni uygulama çalıştıkça sürer; `takePersistableUriPermission` ile kalıcı yapılmaz.
*   **Etkinlik yeniden kurulunca gelen sonuç.** Seçici açıkken Android etkinliği öldürüp yeniden kurarsa sonuç yeni kayda gelir, ama bekleyen bir `onPicked` olmadığı için bırakılır.
*   **Sürüklerken türü denetlemek.** Bölge sürükleme sırasında türü uymayanı ayırt etmez; bırakılınca reddeder (sürüklenenin içeriğini her platform söylemiyor).

## 6. Test etmek {#testing}

*   **Seçiciyi değiştirmek.** `PantrixFileDrop(picker = …)` bir test seçicisi alır: `FilePicker { request, onPicked -> onPicked(listOf(PantrixFile("a.pdf", bytes))) }`. İstenen türler ve sayı `request`'te okunur. Uygulamalar kendi formlarını da böyle sınar.
*   **iOS testinde seçici.** Compose'un iOS testlerinde uygulama ve `LocalUIViewController` yoktur; orada `rememberFilePicker()` hiç cevap vermeyen bir seçici döndürür, kullanıcının kapattığı bir seçici gibi. Dosya seçtiren bir testin kendi seçicisini vermesi gerekir.
*   **Bütün ekranın seçicisi.** Bölgeleri bir ekranın derinlerinde duran bir test, seçiciyi tek tek vermek yerine `CompositionLocalProvider(LocalFilePicker provides picker) { Screen() }` yazar: `rememberFilePicker()` çağıran her yer, `PantrixFileDrop`'un varsayılanı dahil, o seçiciyi alır ve platformun seçicisi açılmaz.
*   **Bırakmak.** Compose'un testleri bir bırakma olayı üretemez. Bırakılanı çeviren kod platform testlerinde sahte verilerle sınanır: masaüstünde bir `DragData.FilesList` uygulaması, Android'de `ClipData` ve `ClipDescription`, web'de tarayıcıda kurulan gerçek bir `DataTransfer`.
*   **Platformların seçicileri.** Android'de Robolectric etkinliğin başlattığı intent'i tutar ve sistemin cevabını verir (`shadowOf(activity).nextStartedActivityForResult`, `receiveResult`): seçicinin açtığı eylem, MIME türleri ve gelen dosyalar böyle sınanır. Web'de seçicinin sayfaya koyduğu `<input>` okunur ve `change`'i elle gönderilir. iOS'ta tekdüze türlerin seçimi ve geçici klasöre kopyalama sınanır; sunulan pencerenin kendisi sınanmaz.

## 7. Tuzaklar {#pitfalls}

*   **Windows'un penceresi süzgeci çağırmaz.** AWT `FileDialog`'un `filenameFilter`'ı macOS'ta ve Linux'ta çalışır, Windows'ta çağrılmaz. Pantrix orada türleri ad alanına kalıp olarak yazar (`*.pdf;*.png`); kullanıcı yine de başka bir dosya seçebilir ve bölge onu reddeder.
*   **Android'in fotoğraf seçicisi asıl adı vermez.** Fotoğraf seçicisinden gelen dosyanın adı gizlilik için bir medya numarasıdır (`1000000648.png`). iOS'un fotoğraf seçicisi önerilen adı verir. Emülatörde görüldü.
*   **iOS'ta ödünç dosya.** Bir yükleyicinin verdiği dosya, yükleme işleyicisi döner dönmez silinir. Pantrix onu işleyicinin içinde uygulamanın geçici klasörüne kopyalar.
*   **Web'de bırakılan dosyalar yalnızca olay sürerken okunur.** `DataTransfer.files` `drop` olayının içinde okunmalıdır; sonradan boştur.
*   **Wasm'da baytlar kopyalanır.** Kotlin/JS'te bir `ByteArray` tarayıcının `Int8Array`'idir ve olduğu gibi alınır; Kotlin/Wasm'da baytlar tek tek kopyalanır, büyük bir dosya bu yüzden yavaştır.
*   **Kod ile kurulan sürükleme olaylarında konum.** Tarayıcıda JavaScript ile kurulan bir `DragEvent`'in `offsetX`'i, piksel yoğunluğu 2 olan bir ekranda `clientX`'in yarısı gelir; Compose bırakmayı yanlış yerde sanır. Gerçek bir sürüklemede bu olmaz; bir denemede `offsetX` ve `offsetY` olayın üstüne doğru değerle yazılmalıdır.
*   **Ortadan kısaltma.** `TextOverflow.MiddleEllipsis` yalnızca Android'de ortadan kısaltır; iOS'ta, masaüstünde ve web'de sondan kısaltır (CMP-6716). Dosya satırı adı kendisi ölçüp ortadan kısaltır, ekran okuyucuya tam adı söyler.

## 8. Doğrulama {#verification}

### Testler

:::table
| Ne | Nerede |
| :--- | :--- |
| Tür eşleşmesi, MIME tablosu, bellekteki dosya, seçici isteği, `LocalFilePicker`'ın verdiği seçici | `ui/foundation/src/commonTest/.../files/` (JVM, iOS, JS, Wasm) |
| Masaüstünün süzgeci, Windows kalıbı, bırakılan dosya listesi | `FilesOnTheDesktopTest` |
| Android'in belge ve fotoğraf seçicisi (Robolectric), `LocalFilePicker`'ın verdiği seçicinin yerine geçmesi, bırakmanın türleri ve adresleri | `FilesOnAndroidTest` |
| Tarayıcıda `<input>`, `DataTransfer` ile bırakma, baytların okunması | `FilesOnTheWebTest` (JS ve Wasm) |
| Tekdüze türler, geçici klasöre kopyalama | `FilesOnIosTest` |
| Bölge: basış ve klavye, eleme, sözler, duyuru, analitik; satır; renkler; stiller | `ui/components/src/commonTest/.../File*Test.kt`, `MiddleCutTest` |
:::

### Elle bakılanlar

*   **Web:** gerçek bir `DataTransfer` ile bırakma; sürüklerken bölgenin ışıklanması; ret ve nedeni; satır ve ilerleme; basışın açtığı `<input>`'un `accept`'i; devre dışı ve kendi renkleri; Türkçe sözler.
*   **Android emülatörü (API 34):** belge seçicisi (süzgeç: metin dosyası görünmedi), çoklu seçim, fotoğraf seçicisi ve okunan resmin gösterilmesi, erişilebilirlik ağacı.
*   **iOS simülatörü:** Dosyalar (süzgeç: metin dosyası seçilemedi), fotoğraf seçicisi ve okunan resim, erişilebilirlik ağacı.

### Doğrulanmayanlar

*   Android'de ve iPad'de başka bir uygulamadan bırakma, masaüstü uygulamasında elle seçme ve bırakma, Windows'un ve Linux'un pencereleri, bir ekran okuyucunun sesi.

## 9. Bu depoda nerede {#repository toc="9. Depoda"}

*   Foundation: `ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/files/` (`PantrixFile.kt`, `PantrixFileType.kt`, `FilePicker.kt`, `FileDropTarget.kt`) ve her platformun kaynak kümesindeki karşılıkları.
*   Bileşenler: `ui/components/src/commonMain/kotlin/com/pantrixui/components/elements/filedrop/`, ortadan kısaltma `components/common/MiddleCut.kt`.
*   Metinler: `PantrixStrings`'te `dropFilesHere`, `chooseFiles`, `orChooseFiles`, `orDropHere`, `dropToAdd`, `filesAdded`, `filesRejected`, `fileSize`.
*   Demo: `app/shared/src/commonMain/kotlin/com/pantrixui/cmp/demo/samples/FileDropSamples.kt`.

## 10. Açık kalanlar {#open}

`docs/OPEN_ITEMS.md`'de: akış olarak okuma, kamera, sürüklerken tür denetimi, `PantrixText`'in ortadan kısaltmasının her platformda çalışması, cihazlarda bırakma.
