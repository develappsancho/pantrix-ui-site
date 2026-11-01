---
title: Tipografi
description: Pantrix'in yazı ölçeği, stili verilmeyen yazının neyle yazıldığı ve metin alanlarına yazılan yazının boyu.
---

## 1. Ölçek {#the-scale}

`PantrixTypography` dokuz rol tutar. Boyutlar ve satır yükseklikleri sp iledir, sistemin yazı boyutuyla büyür.

:::table
| Rol | Boyut / satır | Ağırlık | İşi |
| :--- | :--- | :--- | :--- |
| `display` | 36 / 42 | Bold | Sayfanın başlığı |
| `headline` | 28 / 34 | Bold | Bir bölümün başlığı |
| `title` | 22 / 28 | SemiBold | Bir kartın, panelin, dialog'un başlığı |
| `callout` | 18 / 26 | Medium | Öne çıkan metin |
| `body` | 16 / 24 | Medium | Paragraf; Android'de ve iOS'ta metin alanına yazılan |
| `label` | 14 / 20 | Medium | Kontrol etiketi; masaüstünde ve web'de metin alanına yazılan; **temel yazı** |
| `footnote` | 12 / 16 | Medium | Yardım metni, hata açıklaması |
| `caption` | 11 / 14 | Medium | Zaman damgası, sayaç |
| `code` | 14 / 20 | Medium, eş aralıklı | Kimlik, kod parçası |
:::

:::sketch typography
:::

## 2. Rolüyle anmak: `TypeRole` {#naming-it-by-its-role-typerole}

Bir bileşenin stili yazı tipi tutmaz, yalnız hangi rolde yazdığını söyler. Yazı tipi temanındır:

```kotlin
val style: TextStyle = PantrixTheme.typography[TypeRole.Body]
```

`TypeRole` her rol için bir değer taşır (`Display` … `Code`). Bir stilin `TypeRole?` alanı boş kalırsa preset'inki
geçerlidir.

## 3. Temel yazı: `LocalPantrixTextStyle` {#the-base-text-localpantrixtextstyle}

Stili verilmeyen yazının stili. `PantrixText`, `PantrixLink` ve `PantrixAutoText` `style` verilmediğinde bunu alır.

*   `PantrixTheme` onu ölçeğin `label`'ıyla başlatır. İç içe bir tema kendi ölçeğinin `label`'ıyla yeniden başlatır.
*   Bir bölüm kendi temel yazısını verir. Verdiği alanlar kazanır, vermedikleri çevresindekinde kalır:

```kotlin
ProvidePantrixTextStyle(PantrixTheme.typography.body) {
    PantrixText("Bu bölümdeki paragraflar body ile yazılır.")
    ProvidePantrixTextStyle(TextStyle(fontWeight = FontWeight.SemiBold)) {
        PantrixText("Yalnız kalınlık değişir, boyut body'nin kalır.")
    }
}
```

*   Açıkça verilen bir `style` temel yazının üstündedir.
*   Bir `PantrixTheme`'in dışında değer `TextStyle.Default`'tur.

### Compose foundation'ın `BasicText`'i bu değeri okumaz

`BasicText`'e stil verilmezse metin `TextStyle.Default` ile yazılır: sistemin yazı tipi, 14sp. Material'in
`LocalTextStyle`'ını da yalnız Material'in kendi bileşenleri okur. Uygulamanın kendi `BasicText`'i temel yazıyı kendisi
almalıdır:

```kotlin
BasicText(text = words, style = LocalPantrixTextStyle.current)
```

Kütüphanenin kendi içinde stilsiz bir `BasicText` yoktur (2026-10-18'de tarandı): yazıların hepsi `PantrixText`'ten
geçer.

## 4. Metin alanına yazılan yazının boyu {#the-size-of-what-is-typed-into-a-text-field}

Metin alanının stili hangi rolde yazdığını `textRole` ile söyler: `TextFieldStyle.textRole` ve
`FilledTextFieldStyle.textRole`.

:::table
| Platform | Rol | Boyut |
| :--- | :--- | :--- |
| Android | `Body` | 16sp |
| iOS | `Body` | 16sp |
| Masaüstü | `Label` | 14sp |
| Web | `Label` | 14sp |
:::

Bu rolle yazılanlar:
*   Yazılan yazı ve yer tutucu, bütün alan türlerinde: çerçeveli, dolu, gizli yazılı ve kendini boyutlayan alanlar.
*   Dolu alanın içerideki etiketi dinlenirken. Yukarı kalkınca `footnote` olur.
*   Select'in kutusu ve listesindeki arama alanı. Select'in "seçenekleri kadar geniş" ölçüsü de bununla yapılır.
*   Sayfalamanın sayfaya atlama alanı.

Kutunun üstündeki etiket (`label` rolü) ve yardım metni (`footnote`) değişmez.

### Material 3 ile karşılaştırma

:::table
| Parça | Material 3 | Pantrix |
| :--- | :--- | :--- |
| Yazılan yazı | `bodyLarge` (16/24, Regular) | Android ve iOS: `body` (16/24, Medium); masaüstü ve web: `label` (14/20, Medium) |
| Yer tutucu | `bodyLarge` | yazılan yazıyla aynı |
| İçerideki etiket | dinlenirken `bodyLarge`, kalkınca `bodySmall` (12/16) | yazılan yazının rolünden `footnote`'a (12/16) |
| Yardım metni | `bodySmall` | `footnote` |
:::

Material 3'te bunlar `OutlinedTextFieldTokens.InputFont = BodyLarge` ile gelir; `MaterialTheme` de `LocalTextStyle`'a
`bodyLarge` verir. Pantrix'in `label`'ı Material 3'te `labelLarge`'a denk gelir (14/20, Medium), ama M3 onu düğme ve
sekme yazısında kullanır.

### Bütün uygulama için değiştirmek

```kotlin
PantrixTheme(
    styles = pantrixStyles(
        TextFieldStyle.override(all = TextFieldStyle(textRole = TypeRole.Body)),
        FilledTextFieldStyle.override(all = FilledTextFieldStyle(textRole = TypeRole.Body))
    )
) { App() }
```

Tek bir alan için `PantrixTextField(style = TextFieldStyle(textRole = TypeRole.Callout))` yeter.

## 5. Tuzaklar {#pitfalls}

*   **Masaüstünde etiket ve yazı aynı boydadır.** Kutunun üstündeki etiket de yazılan yazı da 14sp'dir: ayrım renk ve
    kalınlıkla kalır.
*   **Alanın boyu değişmez.** Kutunun yüksekliği en az yüksekliktir. Tek satırlı alan aynı boyda kalır; çok satırlı
    alanın satırları masaüstünde 20sp'ye iner.
*   **iPhone'daki Safari 16px'ten küçük yazılı bir alana odaklanınca sayfayı yakınlaştırır.** Web hedefi iPhone'da
    `label` (14sp) yazar. Compose web'in arkadaki gizli giriş alanının buna takılıp takılmadığı denenmedi.
*   **Stil görünüştür, rol değil.** Başlık stiliyle yazılan metin ekran okuyucu için başlık olmaz: `PantrixText`'in
    `role` parametresi söyler.

## 6. Dosyalar {#files}

:::table
| Dosya | İçinde |
| :--- | :--- |
| `ui/foundation/.../token/typography/PantrixTypography.kt` | Ölçek, `buildPantrixTypography`, `rememberPantrixTypography` |
| `ui/foundation/.../token/typography/TypeRole.kt` | `TypeRole`, `PantrixTypography[role]` |
| `ui/foundation/.../token/typography/PantrixTextStyle.kt` | `LocalPantrixTextStyle`, `ProvidePantrixTextStyle` |
| `ui/components/.../inputs/textfield/TextFieldStyle.kt` | `textRole` ve preset'leri |
| `ui/components/.../inputs/filledtextfield/FilledTextFieldStyle.kt` | `textRole` ve preset'leri |
| `app/shared/.../samples/TypographySamples.kt` | Demo: "Bir bölümün temel yazısı" |
:::
