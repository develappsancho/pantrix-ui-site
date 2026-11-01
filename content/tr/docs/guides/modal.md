---
title: Uygulamanın kendi modal katmanı
description: Compose Multiplatform'da bir uygulamanın kendi modal katmanını (bütün pencerenin üstünde bir perde ve onun üstünde uygulamanın çizdiği içerik) kurmasının neden her platformda başka bir iş olduğu, kütüphanenin bunun için ne verdiği ve nasıl sınandığı. Sorun Pantrix'e özgü değildir: bir dialog'un ya da bir sheet'in çizmediği bir katman (kendi süzgeçleri olan bir arama, büyük gösterilen bir resim, telefonu dolduran bir düzenleyici) isteyen her Compose Multiplatform projesinde aynı şekilde görülür.
---

Compose hakkında burada söylenen her şey Compose Multiplatform 1.12.2'nin kaynaklarında okunmuş ya da bu depoda koşan bir testte görülmüştür; hangisinin nerede görüldüğü 7. ve 8. bölümlerde yazılıdır.

## 1. Sorun {#problem}

:::aside modal
### Belirti

*   Compose'un `Dialog`'u her platformda başka bir şeydir. Android'de kendi penceresidir: arkasını kendisi karartır, kendi animasyonuyla açılır, varsayılan bir genişliğe sığar ve sistem çubuklarının simgelerini kendisi seçer. iOS'ta, masaüstünde ve web'de sahnenin bir katmanıdır: kendi perdesini çizer ve kendi geçişini oynar.
*   `Dialog` kapatılınca hemen gider: içeriğin bir çıkış hareketi yoktur.
*   Bir `Popup(focusable = true)` Escape'i ve Android'de geriyi alır, ama bir modalın geri kalanını vermez: perde, perdeye basınca kapanmak, ekran okuyucu için bir "Kapat", odağın içerikte kalması ve kapanınca geri dönmesi, katlanmadan kaçınmak.
*   Kendi katmanını kuran her uygulama bunları yeniden yazar, ve her platformda ayrı ayrı sınar.
:::

### Neden

`Dialog`'un her platformda kendisi karar verdiği şeyler vardır (Compose Multiplatform 1.12.2, `ui` modülü, `DialogProperties`):

:::table
| Platform | `Dialog` neye kendisi karar verir | Kapatmak için |
| :--- | :--- | :--- |
| Android | Pencerenin karartması (`FLAG_DIM_BEHIND`), açılış animasyonu, varsayılan genişlik, sistem çubuklarının içinde kalmak, çubukların simgeleri | `usePlatformDefaultWidth = false`, `decorFitsSystemWindows = false`; pencerede `clearFlags(FLAG_DIM_BEHIND)`, `setWindowAnimations(0)` ve çubukların görünüşü (`WindowCompat.getInsetsController`) |
| iOS, masaüstü, web | Varsayılan genişlik, pencerenin payları ve yazılım klavyesinin payı, perdenin rengi, açılış ve kapanış geçişi | `usePlatformDefaultWidth = false`, `usePlatformInsets = false`, `useSoftwareKeyboardInset = false`, `scrimColor = Color.Transparent`, `animateTransition = false` (deneysel kurucu) |
:::

Bunlar kapatılınca katman boş bir yüzeydir: perdeyi, içeriğin yerini, hareketi ve odağı uygulama kendisi kurar, her platformda aynı biçimde.

## 2. Çözüm {#fix}

Bileşenlerde (`com.pantrixui.components.overlay.modal`):

1.  **`PantrixModal(visible, onDismissRequest, …) { content }`**: bütün pencerenin üstünde bir perde ve onun üstünde uygulamanın içeriği. `PantrixDialog`'un, `PantrixSheet`'in ve `PantrixCommandPalette`'in üstünde durduğu katmandır.
2.  **`ModalPosition`**: içeriğin yeri ve nasıl girip çıktığı. `Center` pencerenin ortası, `Top` üstüne yakın: alanı yerinde kalan bir arama.

Modal içeriğe dolgu, şekil ya da genişlik vermez: içerik kendini çizer. Gerisi modalındır:

*   Geri, Escape ve perdeye basmak `onDismissRequest`'i çağırır; modal kendini kapatmaz, `visible`'ı çağıran kapatır. İçerik temanın hareketiyle girer ve çıkar; çıktıktan sonra hiçbir şey kurulmaz.
*   Odak WAI-ARIA'nın dialog kalıbına uyar: klavyeyle açılınca içeriğin ilk denetimine geçer ve Tab içeride döner; dokunarak açılınca olduğu yerde kalır. Kapanınca eski yerine döner. Altındaki uygulama odak halkası çizmez.
*   Ekran okuyucu açılırken `paneTitle`'ı duyar, sonra içeriği, en son da perdeye basmak kapatıyorsa tek bir "Kapat" eylemi olarak perdeyi.
*   Bir katlanma pencereyi böldüğünde içerik açıldığı yarıda kalır; bir `PantrixWindowFrame`'in içinde açılan modal o çerçeveyi pencere sayar.

## 3. Pantrix ile {#with}

### 3.1. Üstte bir arama

```kotlin
var searching by rememberSaveable { mutableStateOf(false) }
PantrixModal(
    visible = searching,
    onDismissRequest = { searching = false },
    position = ModalPosition.Top,
    paneTitle = stringResource(Res.string.search),
    tagKey = "search"
) {
    PantrixSurface(Modifier.widthIn(max = 640.dp), shape = PantrixTheme.shapes.lg, elevation = 8.dp) {
        SearchPanel(onOpen = { searching = false })
    }
}
```

*   `margin` içerikle pencerenin güvenli alanı arasında kalan boşluktur (temanın `xl` adımı, 24dp); perdenin parçasıdır: içeriğin hemen yanına basmak da kapatır.
*   Kapatmak bir kez bildirilir: `Modal` olayı, `dismiss`, `via` ile (`scrim` ya da `back`).

### 3.2. Telefonda bir sayfa

```kotlin
PantrixModal(
    visible = editing,
    onDismissRequest = { editing = false },
    margin = PaddingValues(),
    paneTitle = stringResource(Res.string.edit_note),
    scrimColor = PantrixTheme.colors.background,
    tagKey = "editor"
) {
    NoteEditor(Modifier.fillMaxSize(), onDone = { editing = false })
}
```

Boşluksuz bir `margin`, kendini dolduran içerik ve temanın zemini olan bir perde modalı bir sayfa yapar: içerik güvenli alanı doldurur, sistem çubuklarının arkası da zeminin rengindedir.

### 3.3. Bir karar isteyen

`dismissOnScrimClick = false` ve `dismissOnBack = false` modalı yalnızca içindeki bir düğmeyle kapanır yapar; perde o zaman ekran okuyucunun atladığı bir dolgudur.

## 4. Pantrix olmadan {#without}

Aşağıdaki, kütüphanesiz bir projeye kopyalanabilecek en kısa hâldir; perdeyi, Escape'i ve hareketi verir, odak tuzağını ve katlanmayı vermez. Pantrix'in kodu (`ui/components/src/*Main/kotlin/com/pantrixui/components/overlay/modal/`) aynı şeyi testleri ve kenar durumlarıyla yapar.

```kotlin
@Composable
fun AppModal(visible: Boolean, onDismiss: () -> Unit, content: @Composable () -> Unit) {
    val shown = remember { MutableTransitionState(false) }
    shown.targetState = visible
    if (!visible && shown.isIdle && !shown.currentState) return
    Dialog(
        onDismissRequest = onDismiss,
        // Android'de ayrıca pencerenin karartmasını ve animasyonunu kapatın (1. bölümdeki tablo).
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)
    ) {
        AnimatedVisibility(shown, enter = fadeIn(), exit = fadeOut()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .pointerInput(onDismiss) { detectTapGestures { onDismiss() } },
                contentAlignment = Alignment.Center
            ) {
                Box(Modifier.pointerInput(Unit) {}) { content() }
            }
        }
    }
}
```

*   Çıkış hareketi için katman, içerik çıkana kadar kurulu kalmalıdır: `visible` false olunca hemen dönülmez, geçiş bitince dönülür.
*   İçerikteki `pointerInput(Unit) {}` içeriğe basmanın perdeye ulaşmasını önler.

## 5. Yardımcının yapmadıkları {#limits toc="5. Yapmadıkları"}

*   **İçeriğin yüzeyi.** Modal dolgu, şekil, gölge ve genişlik vermez; bir kart ya da bir sayfa içeriğin işidir.
*   **Sürükleyerek kapatmak.** Aşağı çekip kapatmak `PantrixSheet`'tedir; modalın kendisi sürüklenmez.
*   **Kenardan kayarak girmek.** `ModalPosition` yalnızca ortayı ve üstü verir; kenarlardan giren katman sheet'tir.

## 6. Test etmek {#testing}

*   Modal açıkken içerik bir dialog düğümünün içindedir (`isDialog()`); içerik `Modal_<tagKey>` etiketiyle bulunur.
*   Perdeye basmak pencerenin köşesine basmaktır: `onNode(isDialog()).performTouchInput { click(Offset(1f, 1f)) }`.
*   Escape odaklı bir denetimden gönderilir: `onNodeWithTag(…).requestFocus()` ve `performKeyInput { pressKey(Key.Escape) }`.
*   Kapatmanın nasıl geldiği analitik olayında okunur: `RecordingAnalytics().eventsOf("Modal")`, `props["via"]`.

## 7. Tuzaklar {#pitfalls}

*   **Bir Popup modal değildir.** `Popup(focusable = true)` Escape'i ve Android'de geriyi alır, ama Pantrix'in modal katmanı (`PantrixModalLayer`) değildir: altındaki Pantrix denetimleri odak halkalarını çizmeye devam eder.
*   **Klavyeyle açılan modalın ilk odağı.** İlk denetim yerleşimden sonra belli olur: odak bir kare bekleyip öyle istenir. Bileşen kurulurken istenen odak, içerik henüz yerleşmediği için yanlış denetime gidebilir.
*   **Pencerenin ilk karesi.** Bir platform penceresi ya da iOS'ta bir katman ilk karelerini açılırken harcar: kurulumla başlayan bir giriş hareketi modal ekrana gelmeden biter. Pantrix girişi pencerenin ilk çiziminden bir kare sonra başlatır.
*   **Android'de çubukların simgeleri.** Modalın penceresi sistem çubuklarının üstünde durur ve simgelerini kendisi seçer; söylenmezse açık simgeler gösterir: açık bir uygulamanın üstündeki gri perdede saat beyaz kalır.

## 8. Doğrulama {#verification}

### Testler

:::table
| Ne | Nerede |
| :--- | :--- |
| Kapalıyken hiçbir şey kurulmaması, pencerenin başlığı, ortada durmak, boşluk, perdenin rengi ve ona basmak, ekran okuyucu için "Kapat", Escape, ilk odak, Tab'ın içeride dönmesi, odağın geri dönmesi, altındaki uygulamanın halkası, çıkış hareketi, azaltılmış hareket | `ModalTest` (JVM, iOS, JS, Wasm) |
| Herkese açık modalın etiketi, olayları, üstte ve ortada durmak, varsayılan boşluk ve boşluksuz sayfa, perdenin rengi `TOLD_APART`'ta | `PantrixModalTest` (JVM, iOS, JS, Wasm) |
| Android'in penceresi: kendi karartmasının ve animasyonunun olmaması, ilk kareyi beklemek, ekran kadar büyüklük, geri, perde, çubukların simgeleri | `DialogOnAndroidTest` (Robolectric; aynı kabuk) |
| Görünüş: üstte ve ortada içerik, açık ve koyu tema | `ComponentScreenshotTest.modal` (JVM, Android, iOS) |
:::

### Elle bakılanlar

*   **Android emülatöründe (API 33) ve iOS simülatöründe (iPhone 18 Pro Max, iOS 27.0), 2026-10-28:** demonun "Üstte bir arama" örneği pencerenin üstünde, perdenin üstünde açıldı; alan yerinde kaldı, sonuçlar altında listelendi; Android'de geri tuşu kapattı.
*   **Web'de (Wasm, uygulamanın tarayıcısı, 1440×900):** aynı örnek üstte açıldı, perde sayfayı soldurdu, Escape kapattı. Pano gizliyken tarayıcı kareleri kıstığı için giriş birkaç saniye gecikti; perdeye ikinci bir basış onu kapattı, olması gerektiği gibi.
*   **Maestro, iki platformda:** `flows/overlays/modal.yaml`: arama açılır ve bir sonuç kapatır; karar isteyen modal, içindeki bir düğme seçilene kadar kalır. Örneklerin akışları (`dialog`, `sheet`, `command-palette`) iç kabuğun adı `ModalHost` olduktan sonra da geçti.
*   **Tanılama:** ilk bakışta demonun arama alanı adsızdı (`MissingName`, `TextField_search_query`); örneğe `contentDescription` verildi.

### Doğrulanmayanlar

*   Bir ekran okuyucunun sesi, iPad'de ve masaüstü uygulamasında elle açıp kapatmak.

## 9. Bu depoda nerede {#repository toc="9. Depoda"}

*   Bileşenler: `ui/components/src/commonMain/kotlin/com/pantrixui/components/overlay/modal/` (`PantrixModal.kt`, ortak kabuk `Modal.kt`, yerleri `ModalPlacement.kt`, çerçeve `ModalRoom.kt`, pencere `ModalWindow.kt`) ve her platformun kaynak kümesindeki `ModalWindow`'u.
*   Demo: `app/shared/src/commonMain/kotlin/com/pantrixui/cmp/demo/samples/ModalSamples.kt`.

## 10. Açık kalanlar {#open}

Yok.
