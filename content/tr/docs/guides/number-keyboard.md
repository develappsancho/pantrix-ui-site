---
title: Sayı alanı ve eylem tuşu
description: Compose Multiplatform'da sayı yazılan ve yazılınca bir şey yapan bir alanın iPhone'da neden gönderilemediği, bunun için kütüphanenin ne verdiği ve nasıl sınandığı. Sorun Pantrix'e özgü değildir: ortak kodunda KeyboardType.Number ile bir ImeAction isteyen her Compose Multiplatform projesinde aynı şekilde görülür.
---

Burada söylenenlerin hangisinin simülatörde görüldüğü, hangisinin Compose Multiplatform 1.12.1'in kaynağında okunduğu ve hangisinin doğrulanmadığı 7. bölümde ayrı ayrı yazılıdır.

## 1. Sorun {#problem}

:::aside number-keyboard
### Belirti

*   Ortak kodda bir alan sayı klavyesini ve bir eylemi birlikte ister: `KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Go)`. Bir sayfa numarası, bir adet, bir doğrulama kodu.
*   Android'de sayı klavyesi açılır ve sağ altında eylem tuşu vardır; tuşa basınca `onKeyboardAction` çağrılır. Masaüstünde ve web'de Enter aynı işi görür.
*   iPhone'da sayı tuş takımı açılır: on rakam ve bir silme tuşu. **Eylem tuşu yoktur.** Sayı yazılır ama gönderilemez; alanın yaptığı iş o platformda hiç yapılamaz.
*   Hiçbir test bunu göstermez. Klavye sistemindir, testin sahnesinde çizilmez; testte "klavyenin eylemi" doğrudan çağrılır (`performImeAction()`) ve her platformda geçer.
:::

### Neden

iPhone'un sayı tuş takımı (`UIKeyboardType.numberPad`) bir PIN girmek için tasarlanmıştır ve bir return tuşu taşımaz. Compose, `KeyboardType.Number` isteyen alana iOS'ta bu tuş takımını verir. `ImeAction`, UIKit'te return tuşunun türüdür (`returnKeyType`); return tuşu olmayan bir klavyede gösterilecek yeri yoktur.

Yerli iOS uygulamaları bunu iki yoldan biriyle çözer: klavyenin üstüne "Bitti" düğmeli bir çubuk koyar (`inputAccessoryView`) ya da return tuşu olan bir klavye ister. Compose kendiliğinden ikisini de yapmaz.

## 2. Çözüm {#fix}

iOS'ta, eylemi olan bir sayı için **sayılar ve noktalama klavyesi** istenir (`UIKeyboardType.numbersAndPunctuation`): rakamlarıyla açılır ve eylemi gösteren bir return tuşu vardır.

Compose bunun için bir yol verir: `KeyboardOptions.platformImeOptions`. iOS'taki `PlatformImeOptions { keyboardType(…) }`, klavyenin türünü UIKit'in kendi türüyle söyler ve `KeyboardType`'tan türetileni ezer.

API `@ExperimentalComposeUiApi` taşır ve kararlı bir karşılığı yoktur: Compose'un `KeyboardType`'larından hiçbiri iOS'ta bu klavyeye çevrilmez (7. bölüm), `PlatformImeOptions`'ın kurucusundaki her ayar da aynı işareti taşır. Kütüphane ona tek bir dosyada, `iosMain`'de dayanır; herkese açık hiçbir imzada deneysel bir tür yoktur. Bir Compose yükseltmesinde imzası değişirse o dosya derlenmez, davranışı değişirse `NumberKeyboardOnIosTest` düşer. Klavyenin kendisi ancak simülatörde görülür: yükseltmeden sonra alan bir kez açılıp bakılır.

Üç ayrıntı:

:::table
| # | Ayrıntı | Ne yapılır |
| :--- | :--- | :--- |
| 1 | O klavye yazı yazmak içindir: tuşlarının üstünde sözcük öneren bir çubuk gösterir | `autoCorrectEnabled = false`: çubuk kalkar |
| 2 | Aynı klavyede işaretler ve, bir basış ötede, harfler de vardır | Sayı olmayanı alan kendisi geri çevirir: `InputTransformation` |
| 3 | Eylemi olmayan bir sayının (`ImeAction.Default`, `ImeAction.None`) sayı tuş takımında bir eksiği yoktur | Ona dokunulmaz: iOS'ta da sayı tuş takımını alır |
:::

## 3. Pantrix ile {#with}

Kütüphane tek bir fonksiyon verir (`com.pantrixui.foundation.platform`):

```kotlin
public expect fun numberKeyboardOptions(
    imeAction: ImeAction = ImeAction.Default,
    decimal: Boolean = false
): KeyboardOptions
```

Android'de, masaüstünde ve web'de istenen tür ve eylemdir, fazlası değil. iOS'ta eylemi olan bir sayı için sayılar ve noktalama klavyesini ister, düzeltme kapalı. `decimal = true`, `KeyboardType.Number` yerine `KeyboardType.Decimal` ister.

```kotlin
val quantity = rememberTextFieldState()
PantrixTextField(
    state = quantity,
    label = "Adet",
    keyboardOptions = numberKeyboardOptions(ImeAction.Done),
    onKeyboardAction = { onQuantity(quantity.text.toString().toIntOrNull()) },
    inputTransformation = DigitsOnly
)

/** Rakam olmayan bir şey içeren değişikliği bütünüyle geri çevirir. */
private val DigitsOnly = InputTransformation {
    if (!asCharSequence().all { it.isDigit() }) revertAllChanges()
}
```

`PantrixPageJump` alanını bununla kurar. `PantrixTextField`'in `keyboardOptions` parametresi de buraya işaret eder.

## 4. Pantrix olmadan {#without}

Bir `expect` fonksiyonu ve platform başına bir karşılık.

### 4.1. ortak kod (`commonMain`)

```kotlin
expect fun numberKeyboardOptions(
    imeAction: ImeAction = ImeAction.Default,
    decimal: Boolean = false
): KeyboardOptions

internal fun plainNumberKeyboardOptions(
    imeAction: ImeAction,
    decimal: Boolean
): KeyboardOptions = KeyboardOptions(
    keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number,
    imeAction = imeAction
)
```

### 4.2. iOS (`iosMain`)

```kotlin
@OptIn(ExperimentalComposeUiApi::class)
actual fun numberKeyboardOptions(
    imeAction: ImeAction,
    decimal: Boolean
): KeyboardOptions {
    val plain = plainNumberKeyboardOptions(imeAction, decimal)
    val needsAKey = imeAction != ImeAction.Default && imeAction != ImeAction.None
    return if (!needsAKey) {
        plain
    } else {
        plain.copy(
            autoCorrectEnabled = false,
            platformImeOptions = PlatformImeOptions { keyboardType(UIKeyboardTypeNumbersAndPunctuation) }
        )
    }
}
```

`PlatformImeOptions` ve `keyboardType`: `androidx.compose.ui.text.input`; `UIKeyboardTypeNumbersAndPunctuation`: `platform.UIKit`.

### 4.3. öteki platformlar (`androidMain`, `jvmMain`, web)

```kotlin
actual fun numberKeyboardOptions(
    imeAction: ImeAction,
    decimal: Boolean
): KeyboardOptions = plainNumberKeyboardOptions(imeAction, decimal)
```

## 5. Yardımcının yapmadıkları {#limits toc="5. Yapmadıkları"}

*   **Klavyenin kapattığı yeri açmaz.** iOS'ta pencere klavyeyle küçülmez: eylemin sonucunu ekranın altında gösteren şeyin klavyenin payını kendisi hesaba katması gerekir. `PantrixScaffold`'un snackbar'ları katar: iOS'ta üstte çıkarlar, altta gösterilmeleri istenirse klavyenin üstünde dururlar (ikisi de simülatörde görüldü). Bu rehberin ilk hâli snackbar'ın klavyenin arkasında kaldığını söylüyordu; o, geciken bir ekran görüntüsünden çıkarılmış yanlış bir gözlemdi.
*   **Sayı olmayanı dışarıda tutmaz.** iOS'taki klavyede işaretler ve harfler yazılabilir; Android'in sayı klavyesinde de nokta, virgül ve tire vardır. Alanın ne aldığını alan söyler: `InputTransformation`.
*   **Klavyenin üstüne "Bitti" çubuğu koymaz.** Yerli iOS'un öteki yolu budur (`inputAccessoryView`). Compose'un `PlatformImeOptions`'ı onu da alır; sayı tuş takımını koruyup bir çubuk eklemek isteyen proje oradan kurar. Burada denenmedi.
*   **Ondalık ayırıcıyı seçmez.** `decimal = true` iOS'ta da sayılar ve noktalama klavyesini verir: nokta da virgül de oradadır. Hangisinin geçerli olduğu dile bağlıdır ve alanın işidir.
*   **Telefon numarası için değildir.** `KeyboardType.Phone` ayrı bir tuş takımıdır (`phonePad`); onun da return tuşu yoktur. Eylemi olan bir telefon alanı aynı duvara çarpar; burada çözülmedi.

## 6. Test etmek {#testing}

Bir test klavyeyi göremez; görebildiği, alanın klavyeden ne istediğidir.

*   **Her platformda:** dönen seçenekler sayı klavyesi ve adı verilen eylemdir (`NumberKeyboardTest`, ortak test).
*   **iOS'ta:** eylemi olan bir sayı sayılar ve noktalama klavyesini ister ve düzeltmesi kapalıdır; eylemi olmayan sayı tuş takımını korur (`NumberKeyboardOnIosTest`, `iosTest`). Okunan, `platformImeOptions?.keyboardType`'tır (`androidx.compose.ui.text.input.keyboardType` uzantısı).
*   **Alanın davranışı:** `performImeAction()` eylemi çağırır; alanın o eylemle ne yaptığı her platformda sınanır (`PageJumpTest`).

Klavyenin gerçekten o tuşla açıldığı ve tuşun işi yaptığı yalnızca bir cihazda ya da simülatörde görülür. **Sayı yazılan ve eylemi olan her yeni alan simülatörde açılmalıdır.**

## 7. Doğrulama {#verification}

### Görülenler

*   iPhone 18 Pro Max simülatörü, iOS 27.0, Compose Multiplatform 1.12.1 (2026-10-11): `KeyboardType.Number` + `ImeAction.Go` isteyen alan sayı tuş takımını açtı; return tuşu yoktu ve yazılan sayı gönderilemedi.
*   Aynı simülatörde, `numbersAndPunctuation` ile: klavye rakam satırıyla ve mavi bir eylem tuşuyla açıldı; tuş `onKeyboardAction`'ı çağırdı. `ImeAction.Go` ile (sayfaya git alanı) ve `ImeAction.Done` ile (demo'nun adet alanı) denendi. `autoCorrectEnabled = false` öncesinde tuşların üstünde sözcük çubuğu vardı, sonrasında yoktu.
*   Android emülatörü, API 34 (2026-10-11): sayı klavyesi eylem tuşuyla açıldı ve tuş işi yaptı, iki eylemle de.
*   Web (masaüstü tarayıcısı): Enter işi yaptı.

### Kaynakta okunanlar

*   `PlatformImeOptions`'ın iOS'taki bildirimi ve `keyboardType(UIKeyboardType?)` ayarı: `ui-text` 1.12.1, `iosMain/androidx/compose/ui/text/input/PlatformImeOptions.ios.kt`. Belgesi, burada açıkça verilen değerlerin `ImeOptions`'tan türetilenleri ezdiğini söyler. Aynı dosyada `returnKeyType`, `inputAccessoryView` ve ötekiler de vardır; `PlatformImeOptionsConfiguration` ve her ayarı `@ExperimentalComposeUiApi` taşır.
*   `KeyboardType`'ın iOS'ta neye çevrildiği: `ui` 1.12.1, `iosMain/androidx/compose/ui/platform/SkikoUITextInputTraits.ios.kt`. `Number` sayı tuş takımıdır (`UIKeyboardTypeNumberPad`), `Decimal` ondalık tuş takımı (`UIKeyboardTypeDecimalPad`), `Phone` telefon tuş takımı (`UIKeyboardTypePhonePad`); ötekiler (`Text`, `Ascii`, `Uri`, `Email`, `Password`) harfle açılan klavyelerdir. Sayılar ve noktalama klavyesine çevrilen yoktur.

### Doğrulanmayanlar

*   **iPad.** Sayı tuş takımı iPad'de tam bir klavye olarak açılır; orada return tuşu olabilir. Denenmedi. Yardımcı iPad'de de sayılar ve noktalama klavyesini ister.
*   **Donanım klavyesi bağlı bir iPhone.** Simülatörde donanım tuşları uygulamaya ulaşmadı.
*   **Gerçek cihaz** ve iOS'un eski sürümleri. Eylem tuşunun yazısı ("git", "bitti") ya da ikonu sürümle değişebilir.
*   **`KeyboardType.Decimal` ve `KeyboardType.Phone`'un iOS'taki tuş takımları.** Hangi tuş takımına çevrildikleri kaynakta okundu; return tuşlarının olmadığı UIKit'in bilinen davranışıdır, burada açılıp bakılmadı.
*   **Telefonun tarayıcısında web.** Sanal klavyenin sayı alanına ne verdiğine bakılmadı.

## 8. Bu depoda nerede {#repository toc="8. Depoda"}

:::table
| Ne | Nerede |
| :--- | :--- |
| Fonksiyon ve ortak yardımcısı | `ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/platform/NumberKeyboard.kt` |
| iOS karşılığı | `ui/foundation/src/iosMain/kotlin/com/pantrixui/foundation/platform/NumberKeyboard.ios.kt` |
| Öteki karşılıklar | aynı pakette `NumberKeyboard.android.kt`, `NumberKeyboard.jvm.kt`, `NumberKeyboard.web.kt` |
| Testler | `NumberKeyboardTest` (`commonTest`), `NumberKeyboardOnIosTest` (`iosTest`) |
| Kullanan bileşen | `PantrixPageJump` (`ui/components/.../data/pagination/PageJump.kt`) |
| Demo | `TextField` sayfasında "Sayı alanı ve eylem tuşu"; `Pagination` sayfasında tablo ayağı |
:::
