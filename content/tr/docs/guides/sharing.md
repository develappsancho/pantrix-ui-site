---
title: Bir metni başka bir uygulamaya göndermek
description: Compose Multiplatform'da bir metni ya da bir bağlantıyı başka bir uygulamaya, bir mesaja, bir e-postaya, bir nota göndermenin neden her platformda başka bir kod olduğu, bunun için kütüphanenin ne verdiği ve nasıl sınandığı. Sorun Pantrix'e özgü değildir: "Başka bir uygulamayla gönder" diyen her Compose Multiplatform uygulamasında aynı biçimde görülür.
---

Platformlar hakkında söylenenler Android'in `Intent.createChooser`'ının, UIKit'in `UIActivityViewController`'ının ve tarayıcıların Web Share API'sinin (`navigator.share`) belgelerine dayanır ya da bu depoda koşan bir testte görüldü; neyin nerede görüldüğü 6. bölümde yazılıdır.

## 1. Sorun {#problem}

:::aside sharing
### Belirti

*   Ortak kodda bir "Gönder" düğmesi yazılamaz: Compose'un ortak API'sinde bir paylaşım sayfası yoktur.
*   Uygulama her platform için kendi `expect`/`actual`'ını yazar: Android'de bir `Intent`, iOS'ta bir görünüm denetleyicisi, web'de `navigator.share`; masaüstünde bir karşılığı yoktur.
*   iPad'de `UIActivityViewController` bir popover olarak açılır; nereden açıldığı söylenmezse uygulama çöker.
*   Firefox'un masaüstündeki sürümünde `navigator.share` yoktur, ve bir basışın dışında çağrılan paylaşımı her tarayıcı reddeder.
*   Bir test düğmeye bastığında ya sistemin sayfası açılır ya da hiçbir şey sınanamaz.
:::

### Neden

*   **Paylaşım sayfası platformundur.** Her biri kendi türünü alır ve kendi yerinden açılır: Android bir `Context`'ten, iOS en üstteki görünüm denetleyicisinden, tarayıcı bir basışın içinden.
*   **Ne döndüğü de ayrıdır.** Android'in seçicisi okurun neyi seçtiğini uygulamaya söylemez; iOS sayfa kapanınca tamamlandı ya da kapatıldı der; tarayıcı bir söz (promise) çözer, ya da kapatılınca `AbortError`, reddedince `NotAllowedError` ile geri çevirir.

## 2. Çözüm {#fix}

:::table
| # | Parça | Ne verir |
| :--- | :--- | :--- |
| 1 | **Ortak bir arayüz ve platform başına uygulaması** | Düğme ortak kodda bir kez yazılır |
| 2 | **Olmayan yerde hiç** | Masaüstü ve `navigator.share`'i olmayan tarayıcı, gönderme yerine kopyalamayı önerir |
| 3 | **Tek bir sonuç türü** | Gönderildi, kapatıldı, gösterildi ya da gösterilemedi: ekran bunu her platformda aynı biçimde okur |
| 4 | **Testin kendi sayfası** | Hiçbir sistem sayfası açılmaz, gönderilen okunur |
:::

## 3. Pantrix ile {#with}

### 3.1. Sayfa

`rememberShareSheet()`, `com.pantrixui:foundation`'dadır (`com.pantrixui.foundation.platform`). Bir `ShareSheet` ya da sayfası olmayan bir platformda `null` verir:

```kotlin
val sheet = rememberShareSheet()
val clipboard = rememberPantrixClipboard()
val scope = rememberCoroutineScope()
if (sheet != null) {
    PantrixButton(
        text = appWords.sendWithAnotherApp,
        onClick = { sheet.share(link, title = documentName) { result -> onShared(result) } },
        tagKey = "send"
    )
} else {
    // Masaüstünde ve navigator.share'i olmayan bir tarayıcıda: bağlantıyı kopyalamak.
    PantrixButton(text = appWords.copyLink, onClick = { scope.launch { clipboard.copyText(link) } }, tagKey = "copy")
}
```

:::table
| Platform | Sayfa | `ShareResult` |
| :--- | :--- | :--- |
| Android | Sistemin seçicisi, `Intent.ACTION_SEND` ile `text/plain`; başlık konu (`EXTRA_SUBJECT`) ve seçicinin başlığı | `Started`: seçici okurun neyi seçtiğini söylemez |
| iOS | `UIActivityViewController`; iPad'de pencerenin ortasında oksuz bir popover; başlık bir e-postanın konusu | `Shared` ya da `Cancelled` |
| Web | `navigator.share`, başlık sayfanın başlığı | `Shared`; kapatılınca `Cancelled`; reddedilince `Failed` |
| Masaüstü | Yok: `rememberShareSheet()` `null` verir | — |
:::

*   **Bir bağlantı metin olarak gönderilir.** Sayfanın listelediği her uygulama onu böyle alır.
*   **Web'de bir basış.** `share` bir düğmenin `onClick`'inden, araya bir bekleme girmeden çağrılır: tarayıcı bir basışın istemediği sayfayı reddeder.
*   **Sonuç `Started` olabilir.** Android'de "gönderildi" denmez: okur sayfayı kapatmış da olabilir.

### 3.2. Test etmek

`PantrixTestTheme` ekrana kendi `TestShareSheet`'ini verir: hiçbir sistem sayfası açılmaz. Gönderileni okuyan bir test kendisininkini temanın içinde verir, sayfası olmayan bir platformu sınayan bir test hiçbirini:

```kotlin
val sheet = TestShareSheet()
setContent {
    PantrixTestTheme { CompositionLocalProvider(LocalShareSheet provides sheet) { ShareScreen(link) } }
}
onPantrixNode("Button", "send").performClick()
assertEquals(listOf(SharedText(link, title = "Orders")), sheet.shared)

// Masaüstünün ekranı, bağlantıyı kopyalayan:
setContent { PantrixTestTheme { CompositionLocalProvider(LocalShareSheet provides null) { ShareScreen(link) } } }
onPantrixNode("Button", "copy").assertExists()
```

`TestShareSheet(result = ShareResult.Cancelled)` sayfayı kapatan bir okur gibi yanıt verir.

## 4. Pantrix olmadan {#without}

Kütüphanesiz bir projede her platform kendi çağrısını yapar; ortak kod bir `expect fun` ile ona ulaşır:

:::table
| Platform | Çağrı | Dikkat |
| :--- | :--- | :--- |
| Android | `context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text), title))` | Bir aktivitenin dışından `FLAG_ACTIVITY_NEW_TASK` ister |
| iOS | `top.presentViewController(UIActivityViewController(listOf(text), null), true, null)` | `top` en üstte sunulan denetleyicidir; iPad'de `popoverPresentationController`'a bir `sourceView` ve `sourceRect` verilir |
| Web | `navigator.share({ text: text, title: title })` | Bir basışın içinden; olmayan tarayıcıda `typeof navigator.share` `"undefined"`'dır |
| Masaüstü | Yok | Panoya kopyalamak |
:::

## 5. Tuzaklar {#pitfalls}

*   **iPad'de kaynaksız popover.** `sourceView` verilmeyen bir `UIActivityViewController` iPad'de uygulamayı çökertir. Pantrix onu pencerenin ortasına koyar.
*   **Basışsız paylaşım.** Bir bekleme ya da bir ağ isteğinden sonra çağrılan `navigator.share` `NotAllowedError` ile reddedilir: `Failed`. Paylaşılacak metin basıştan önce hazırdır.
*   **Android'de "gönderildi" denmez.** Seçici okurun seçimini söylemez; ekran `Started`'ı bir başarı olarak göstermez.
*   **Bir dosya başka bir iştir.** Android'de bir dosyayı paylaşmak manifestte bir `FileProvider` ister; bunun parçaları [grafik dışa aktarma rehberinin](/docs/guides/chart-export) 3. bölümündedir.

## 6. Doğrulama {#verification}

### Testler

:::table
| Ne | Nerede |
| :--- | :--- |
| Verilen sayfa ekranın sayfasıdır, hiçbiri verilmezse hiçbiri | `ui/foundation/src/commonTest/.../platform/ShareSheetTest.kt` (JVM, Android, iOS, JS, Wasm) |
| Android: seçicinin `ACTION_SEND` niyeti, `text/plain`, metin, konu ve seçicinin başlığı; başlıksız metnin konusu yok; `Started` | `ui/foundation/src/androidHostTest/.../platform/ShareSheetOnAndroidTest.kt` (Robolectric) |
| iOS: uygulaması olmayan bir test ikilisinde `Cancelled` | `ui/foundation/src/iosTest/.../platform/ShareSheetOnIosTest.kt` |
| Web: `navigator.share`'e verilen metin ve başlık, başlıksız metin; `Shared`, `Cancelled`, `Failed`; `navigator.share` yoksa sayfa yok (testin kendi `navigator.share`'iyle) | `ui/foundation/src/webTest/.../platform/ShareSheetOnTheWebTest.kt` (JS, Wasm) |
| Masaüstü: sayfa yok | `ui/foundation/src/jvmTest/.../platform/ShareSheetOnTheDesktopTest.kt` |
| `TestShareSheet` ve test temasının kendi sayfası | `ui/testing/src/commonTest/.../TestDoublesTest.kt`, `PantrixTestThemeTest.kt` |
:::

### Elle bakılanlar

*   **Android emülatöründe (2026-11-11):** demo'nun Pano sayfasında "Başka bir uygulamaya göndermek": seçici bağlantıyla açıldı, sistemin başlığıyla ("Sharing link"); geri tuşuyla kapanınca kart "Paylaşım sayfası açıldı" dedi, seçimi bilmeden.
*   **iOS simülatöründe (iPhone 18 Pro Max, 2026-11-11):** aynı örnekte `UIActivityViewController` bağlantıyla açıldı; "Kopyala" seçilince kart "Gönderildi", sayfanın dışına basılınca "Sayfa kapatıldı" dedi.
*   **Web'de (yerleşik tarayıcı, 2026-11-11):** `navigator.share` yoktu ve düğme bağlantıyı kopyalamayı önerdi. Sayfaya bir yedek `navigator.share` verilince düğme paylaşım düğmesi oldu, yedek metni ve başlığı aldı ve kart "Gönderildi" dedi; anahtar açılınca kopyalama düğmesi geri geldi.

### Doğrulanmayanlar

*   Bir iPad'de popover.
*   Firefox'ta `navigator.share`'in yokluğu: testte gizlenerek sınandı.

## 7. Bu depoda nerede {#repository toc="7. Depoda"}

*   `ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/platform/ShareSheet.kt` ve platformların `ShareSheet.*.kt`'leri.
*   `ui/testing/src/commonMain/kotlin/com/pantrixui/testing/TestShareSheet.kt`.
*   Demo: `app/shared/src/commonMain/kotlin/com/pantrixui/cmp/demo/samples/ClipboardSamples.kt` ("Başka bir uygulamaya göndermek").
