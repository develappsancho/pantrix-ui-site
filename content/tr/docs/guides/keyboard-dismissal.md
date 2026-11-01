---
title: Klavyeyi kapatma
description: Compose Multiplatform'da bir alanın yazılım klavyesinin iPhone'da neden kapatılamadığı, kütüphanenin bunun için ne verdiği ve nasıl sınandığı. Sorun Pantrix'e özgü değildir: birkaç satırlık ya da sayı yazılan bir alanı olan her ekranda görülür.
---
## 1. Sorun {#problem}

:::aside keyboard-dismissal
### Ne görülür

- iPhone'da klavyenin sağ altındaki tuş yeni satır açar. Klavyeyi kapatan bir tuş yoktur, alanın dışına dokunmak da kapatmaz: altındaki “Gönder” düğmesine ulaşılamaz.
- Sayı tuş takımında ne eylem tuşu ne kapatma tuşu vardır.
- Android'de geri hareketi klavyeyi kapatır; dışarıya dokunmak yine kapatmaz. Masaüstünde ve web'de boş bir yere tıklamak odağı alanda bırakır.
- Hiçbir birim testi bunu göstermez: klavye sistemindir. Maestro'nun `hideKeyboard` komutu da iOS'ta Compose'un alanından klavyeyi kapatamaz.
:::

:::callout why
**Neden.** Yerli bir iOS uygulamasında klavyeyi alan değil ekran kapatır: bir dokunuş görünümün düzenlemesini bitirir (`endEditing`) ya da kaydırmak klavyeyi indirir. Compose'da klavye odaktaki alanla kalır ve Compose dışarıya bir dokunuşta odağı bırakmaz: hiçbir şeyin almadığı bir dokunuş hiçbir yere gitmez.
:::

## 2. Çözüm {#fix}

Ekranın köküne, gösterdiği her şeyin altına bir dokunuş dinleyicisi konur. **Altındaki hiçbir şeyin almadığı** bir dokunuşta odağı bırakır (`FocusManager.clearFocus()`): alan odağını bırakır, klavye de onunla gider.

:::table Basışı kim alır
| Kullanıcı | Compose | Ardından |
| --- | --- | --- |
| Bir düğmeye dokunuş | Düğme alır | İşini yapar; odak olduğu yerde kalır |
| Alana dokunuş | Alan alır | İmleç kayar; klavye kalır |
| Kaydıran bir sürükleme | Kaydıran alır | Odak kalır |
| Boş bir yere dokunuş {highlight} | Hiçbir şey almaz | `clearFocus()`: klavye gider |
:::

## 3. Pantrix ile {#with}

Tek bir değiştirici, `com.pantrixui.foundation.keyboard` paketinde, ekranın ya da uygulamanın kökünde:

```kotlin ClearFocusOnTap.kt
public fun Modifier.clearFocusOnTap(): Modifier
```

```kotlin CommentScreen.kt
Column(Modifier.fillMaxSize().clearFocusOnTap()) {
    PantrixTextArea(state = comment, label = stringResource(Res.string.comment))
    PantrixButton(text = stringResource(Res.string.send), onClick = ::send)
}
```

:::rows
- iOS: Klavyeyi kapatmanın yolu.
- Android: Sistemin geri hareketinin yanına bir yol.
- Masaüstü ve web: Boş bir yere tıklamak, tarayıcıdaki gibi odağı bırakır.
:::

## 4. Pantrix olmadan {#without}

`pointerInput` ve `LocalFocusManager` ile, ortak kodda bir değiştirici düğümü. Düğüm yazmak istemeyen bir proje aynısını bir composable'ın içinde yapar.

:::tabs commonMain
### Bir değiştirici düğümü

```kotlin
fun Modifier.clearFocusOnTap(): Modifier = this then ClearFocusOnTapElement

private data object ClearFocusOnTapElement : ModifierNodeElement<ClearFocusOnTapNode>() {
    override fun create() = ClearFocusOnTapNode()
    override fun update(node: ClearFocusOnTapNode) = Unit
}

private class ClearFocusOnTapNode : DelegatingNode(), CompositionLocalConsumerModifierNode {
    init {
        delegate(SuspendingPointerInputModifierNode {
            detectTapGestures(onTap = { currentValueOf(LocalFocusManager).clearFocus() })
        })
    }
}
```

### Bir composable'ın içinde

```kotlin
val focusManager = LocalFocusManager.current
Column(
    Modifier
        .fillMaxSize()
        .pointerInput(focusManager) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
) {
    // ekranın içeriği
}
```
:::

:::note
`detectTapGestures` `androidx.compose.foundation.gestures` paketindedir; düğümün tipleri `androidx.compose.ui.node` ve `…ui.input.pointer` paketlerinde.
:::

## 5. Yardımcının yapmadıkları {#limits toc="5. Yapmadıkları"}

:::points
- **Kaydırınca klavyeyi indirmez.** iOS'un `keyboardDismissMode`'unun burada karşılığı yoktur: kaydırmak odağı olduğu yerde bırakır.
- **Klavyenin üstüne bir “Bitti” çubuğu koymaz.** O, yerli iOS'un öteki yoludur (`inputAccessoryView`). Burada denenmedi.
- **Kendi penceresi olan bir katmanın dokunuşlarını duymaz.** Bir alan taşıyan `Popup` ya da `Dialog` değiştiriciyi kendi içeriğine de alır.
- **Odağı alan bir düğmeden odağı geri almaz.** Masaüstünde ve web'de basılan bir düğme odağı kendisi alabilir: bu düğmenin işidir.
:::

## 6. Test etmek {#testing}

:::boxes
### Beş hedefte bir birim testi

`ClearFocusOnTapTest`: boş bir yere dokunuş odağı alır; alana parmakla ya da fareyle dokunuş odağı tutar; odak almayan bir düğme işini yapar; kaydıran bir sürükleme odağı tutar.

### Maestro'da `hideKeyboard`'un yerine

Kökünde değiştirici olan bir ekranda boş bir yere dokunan bir adım aynısını yapar.

```yaml
- tapOn: "A note for the courier"  # boş yer: klavye gider
- tapOn:
    id: "Send"
```
:::

:::note
Klavyenin gerçekten gittiği yalnızca bir cihazda ya da bir simülatörde görülür.
:::

## 7. Doğrulama {#verification}

:::checks
### Simülatörde, emülatörde ve web'de görüldü {seen}

- iPhone 18 Pro Max simülatörü, iOS 27.0: klavyenin sağ alt tuşu yeni satır açtı; açıklamaya dokunuş klavyeyi kapattı, alana dokunuş yeniden açtı.
- Android emülatörü, Pixel 7 Pro, API 33: açıklamaya dokunuş klavyeyi kapattı (`mInputShown=false`); alana ikinci dokunuş onu açık tuttu ve imleci kaydırdı.
- Web, Chrome, Wasm: boş bir yere tıklamak alanın kenarını dinlenmedeki hâline getirdi; alana tıklamak odağı tuttu.

### Kaynaklarda okundu {read}

- `detectTapGestures` ilk basışı bekler ve alınmış bir basışı saymaz (foundation 1.12.2, TapGestureDetector.kt).
- Bir metin alanı basışını her platformda `detectTapAndPress` içinde alır (TextFieldSelectionState).

### Denetlenmedi {unchecked}

- iPad ve donanım klavyeli bir iPhone.
- Gerçek bir cihaz ve iOS'un eski sürümleri.
- Bir telefonun tarayıcısında web.
:::

## 8. Depoda nerede {#repository toc="8. Depoda"}

:::labels
- Değiştirici: [`foundation/keyboard/ClearFocusOnTap.kt`](https://github.com/developersancho/pantrix-ui-cmp/blob/main/ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/keyboard/ClearFocusOnTap.kt)
- Testler: `ClearFocusOnTapTest`, `commonTest` içinde
- Kim kullanır: Demo uygulamasının kökü; `PantrixTextArea` ve `PantrixFilledTextArea` onu anar
- Demo: Text Area sayfasındaki [**“Dışarıya dokunmak klavyeyi kapatır”**](/components/text-area) örneği
:::
