---
title: Bir dilin alfabesine göre sıralama
description: Compose Multiplatform'un ortak kodunda bir ad listesinin neden dilin alfabesine göre sıralanmadığı, bunun için kütüphanenin ne verdiği ve nasıl sınandığı. Sorun Pantrix'e özgü değildir: ortak kodunda sorted() ya da sortedBy { it.name } ile ad, şehir, başlık sıralayan her Kotlin Multiplatform projesinde aynı şekilde görülür.
---

Burada söylenenlerin hangisinin bu depoda koşan bir testte görüldüğü ve hangisinin doğrulanmadığı 7. bölümde ayrı ayrı yazılıdır.

## 1. Sorun {#problem}

:::aside text-order
### Belirti

*   Türkçe bir müşteri listesi `sortedBy { it.name }` ile sıralanır. "Çelik", "Öztürk", "Şahin" ve "İpek" listenin en sonuna, "Zeynep"ten sonraya düşer.
*   "ılık", "iğne"den **sonra** gelir; Türk alfabesinde `ı`, `i`'den öncedir.
*   Büyük harfle başlayan her ad, küçük harfle başlayan her addan önce gelir: "Zeynep", "ada"dan önce.
*   Aynı ekran her platformda aynı (yanlış) sırayı verir; bu yüzden bir platformda denenip fark edilmez.

```kotlin
listOf("zeytin", "şeker", "öğle", "iğne", "ılık", "çilek", "Çay", "cam").sorted()
// [cam, iğne, zeytin, Çay, çilek, öğle, ılık, şeker]
```
:::

### Neden

Kotlin'de iki yazının karşılaştırılması (`String.compareTo`) harflerin Unicode'daki sayılarına bakar. Bu, her platformda aynı sonucu veren, hızlı ve hiçbir dilin alfabesi olmayan bir sıradır: büyük harflerin sayıları küçük harflerinkinden küçüktür, `ç` (231), `ö` (246), `ş` (351) ve `ı` (305) de `z`'den (122) büyüktür.

Bir dilin alfabesine göre sıralamak "collation" denen ayrı bir iştir ve Kotlin'in ortak kütüphanesinde karşılığı yoktur. Her platformun kendi aracı vardır:

:::table
| Platform | Araç | Ortak koddan erişilir mi |
| :--- | :--- | :--- |
| Android | `java.text.Collator` (arkasında ICU) | Hayır, `androidMain` |
| Masaüstü (JVM) | `java.text.Collator` | Hayır, `jvmMain` |
| iOS | `NSString.compare(_:options:range:locale:)` | Hayır, `iosMain` |
| Web (`js`, `wasmJs`) | `Intl.Collator` | Hayır, `webMain` |
:::

Kısa yoldan gidilen iki çözüm de yetmez:

1.  **`lowercase()` ile karşılaştırmak.** Büyük küçük harf sorununu çözer, alfabeyi çözmez: `ç` yine `z`'den sonradır.
2.  **`String.CASE_INSENSITIVE_ORDER`.** JVM'de `İ` ile `i`'yi eşit sayar, JS'te, Wasm'da ve iOS'ta saymaz: aynı liste masaüstünde ve tarayıcıda başka sıralanır.

## 2. Çözüm {#fix}

Her platformun kendi aracını tek bir `Comparator<String>` arkasına almak: ortak kodda bir `expect`, dört kaynak kümesinde karşılığı.

Üç karar her platformda aynı tutulur ki liste platformdan platforma değişmesin:

*   **Büyük küçük harf eşittir.** "ada" ile "Ada" yan yana durur. Kotlin'in `sortedWith`'i eşitleri geldikleri sırada bıraktığı için sıralama kararlıdır.
*   **Aksan ayırt edilir.** "resume", "résumé"den önce gelir.
*   **Rakamlar harf gibidir.** "10", "9"dan önce gelir. Sayı sıralanacaksa sayıyla sıralanır; `java.text.Collator`'da sayısal sıralama seçeneği olmadığı için öteki platformlarda da açılmadı.

## 3. Pantrix ile {#with}

`PantrixLocale.textOrder()` o dilin karşılaştırıcısını verir; `rememberTextOrder()` bir bileşimde temanın dilininkini tutar.

```kotlin
// Ortak kodda, bileşimin dışında
val order = PantrixLocale.Turkish.textOrder()
val sorted = customers.sortedWith(compareBy(order) { it.name })

// Bir bileşimde: temanın diliyle, dil değişmedikçe aynı nesne
val order = rememberTextOrder()
val sorted = remember(customers, order) { customers.sortedWith(compareBy(order) { it.name }) }
```

`PantrixDataTable`'ın yazı kolonu onu doğrudan alır:

```kotlin
val order = rememberTextOrder()
val columns = remember(order) {
    listOf(
        DataTableColumn.text<Customer>(key = "name", header = name, sortable = true, textOrder = order) { it.name }
    )
}
PantrixDataTable(rows = remember(customers, sort) { customers.sortedWith(sort.comparator(columns)) }, columns = columns)
```

`textOrder` verilmeyen yazı kolonu eskisi gibi, harflerin sayılarıyla ve büyük küçük harf ayırmadan sıralar.

**Karşılaştırıcıyı sakla.** Bir karşılaştırıcı kurmak bedava değildir ve bir sıralama ona binlerce kez sorar. Bileşimin dışında bir alanda, içinde `rememberTextOrder()` ile tutulur; her karşılaştırmada yeniden kurulmaz.

## 4. Pantrix olmadan {#without}

Dört dosya. Ortak kodda:

```kotlin
// commonMain
expect fun textOrder(languageTag: String): Comparator<String>
```

Android'de ve masaüstünde (iki kaynak kümesinde aynı dosya):

```kotlin
// androidMain, jvmMain
actual fun textOrder(languageTag: String): Comparator<String> {
    val collator = Collator.getInstance(Locale.forLanguageTag(languageTag))
    collator.strength = Collator.SECONDARY // aksanı ayırt eder, büyük küçük harfi etmez
    return Comparator { first, second -> collator.compare(first, second) }
}
```

iOS'ta:

```kotlin
// iosMain
@OptIn(ExperimentalForeignApi::class)
actual fun textOrder(languageTag: String): Comparator<String> {
    val locale = NSLocale(localeIdentifier = languageTag)
    return Comparator { first, second ->
        val whole = NSMakeRange(0u, first.length.toULong())
        @Suppress("CAST_NEVER_SUCCEEDS")
        (first as NSString).compare(second, options = NSCaseInsensitiveSearch, range = whole, locale = locale).toInt()
    }
}
```

Web'de, `js` ile `wasmJs`'in ortak kaynak kümesinde:

```kotlin
// webMain
actual fun textOrder(languageTag: String): Comparator<String> =
    Comparator { first, second -> compareInLanguage(first, second, languageTag) }

@OptIn(ExperimentalWasmJsInterop::class)
private fun compareInLanguage(first: String, second: String, languageTag: String): Int = js(
    """(function () {
        try {
            var kept = globalThis.__textOrders || (globalThis.__textOrders = {});
            var order = kept[languageTag] ||
                (kept[languageTag] = new Intl.Collator(languageTag, { sensitivity: 'accent' }));
            return order.compare(first, second);
        } catch (refused) {
            return first < second ? -1 : (first > second ? 1 : 0);
        }
    })()"""
)
```

Web'deki iki ayrıntı:

*   **Karşılaştırıcı sayfada tutulur.** JS ile Wasm'ın birbirine aynı şekilde verdiği tek şey yazıdır; bir `Intl.Collator` nesnesi iki hedefte ayrı türlerle taşınır. Bu yüzden nesne Kotlin tarafına alınmaz, dil başına bir kez kurulup sayfada (`globalThis`) saklanır: bir sıralamanın binlerce sorusunun her birinde yeniden kurulmasın diye. Ne kadar kazandırdığı bu depoda ölçülmedi.
*   **Tanınmayan etiket hata fırlatır.** `new Intl.Collator("geçersiz etiket")` bir `RangeError` fırlatır; yukarıdaki kod onu yakalar ve harflerin sayılarına döner. Öteki platformlar tanımadıkları etikette sessizce genel kurala döner.

## 5. Yardımcının yapmadıkları {#limits toc="5. Yapmadıkları"}

*   **Sunucunun sırasını vermez.** Sıra platformundur ve platformlar her dilin her harfinde anlaşmaz. Tam da bir sunucunun sırasında olması gereken liste (sayfa sayfa gelen bir tablo) sunucuda sıralanır.
*   **Sayıları sayı gibi sıralamaz.** "Sipariş 10", "Sipariş 9"dan önce gelir.
*   **Aramaz.** Bir yazının içinde başka bir yazıyı dile göre aramak (Türkçede "I" yazınca "ı" bulmak) ayrı bir iştir; bu yalnızca karşılaştırır.
*   **Büyük harfe çevirmez.** `uppercase()` de dile bakmaz: Türkçede "i" "İ" olmalıdır. Pantrix bu yüzden bir tablo başlığını kendiliğinden büyük harfe çevirmez; başlık yazıldığı gibi yazılır.

## 6. Test etmek {#testing}

Sınanacak olan, platformun aracının gerçekten o dilin kurallarını bildiğidir; bu yüzden test her hedefte koşmalıdır. Türkçe iyi bir sınavdır: `ç`, `ğ`, `ı`, `ö`, `ş`, `ü` harflerinin altısı da harflerin sayılarıyla yanlış yere düşer.

```kotlin
@Test
fun turkish_texts_are_in_the_order_of_the_turkish_alphabet() {
    val words = listOf("zeytin", "şeker", "öğle", "iğne", "ılık", "çilek", "Çay", "cam")
    assertEquals(
        listOf("cam", "Çay", "çilek", "ılık", "iğne", "öğle", "şeker", "zeytin"),
        words.sortedWith(PantrixLocale.Turkish.textOrder())
    )
}
```

Büyük küçük harf eşitliğini Türkçe "I" ve "ı" ile sınamayın: platformların büyük küçük harf eşlemesi o çiftte ayrılabilir. "Çay" ile "çay" her platformda eşittir.

## 7. Doğrulama {#verification}

Bu depoda koşan testlerde görülenler (2026-10-11, `TextOrderTest`, `TextOrderOnAndroidTest`):

:::table
| Ne | JVM | iOS simülatörü | JS (başsız Chrome) | Wasm (başsız Chrome) | Android (ana makine) |
| :--- | :---: | :---: | :---: | :---: | :---: |
| Sekiz Türkçe sözcük alfabe sırasında | ✅ | ✅ | ✅ | ✅ | ✅ |
| Türk alfabesinin 29 harfi sırayla | ✅ | ✅ | ✅ | ✅ | |
| "ada" ile "Ada" eşit, liste kararlı | ✅ | ✅ | ✅ | ✅ | ✅ |
| "resume", "résumé"den önce | ✅ | ✅ | ✅ | ✅ | ✅ |
| Tanınmayan etiket hata vermiyor | ✅ | ✅ | ✅ | ✅ | ✅ |
:::

Doğrulanmayanlar:

*   **Android cihazı.** Android testi ana makinede koşar ve orada `java.text.Collator` JDK'nınkidir. Cihazda aynı sınıfın arkasında ICU vardır; Türkçe kurallarını bildiği beklenir, bu depoda bir cihazda ya da emülatörde sınanmadı.
*   **Paketlenmiş masaüstü uygulaması.** JDK, İngilizce dışındaki dillerin kurallarını `jdk.localedata` modülünde taşır. Uygulamayla birlikte paketlenen çalışma zamanı bu modülü içermiyorsa `Collator` hata vermeden genel kurala döner ve Türkçe liste yine yanlış sıralanır. Compose Desktop'ta modül `nativeDistributions { modules("jdk.localedata") }` ile eklenir. Bu depoda paketlenmiş bir uygulamada denenmedi; testler tam bir JDK ile koşar.
*   **Safari ve Firefox.** Tarayıcı testleri yalnızca Chrome'da koşar.
*   **Türkçe dışındaki diller.** Almanca, İsveççe, İspanyolca gibi kendi kuralı olan diller sınanmadı; araç aynıdır.

## 8. Bu depoda nerede {#repository toc="8. Depoda"}

:::table
| Dosya | Ne |
| :--- | :--- |
| `ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/l10n/TextOrder.kt` | `PantrixLocale.textOrder()`, `rememberTextOrder()` |
| `ui/foundation/src/{androidMain,jvmMain,iosMain,webMain}/kotlin/com/pantrixui/foundation/l10n/TextOrder.*.kt` | Platformların karşılıkları |
| `ui/foundation/src/commonTest/kotlin/com/pantrixui/foundation/l10n/TextOrderTest.kt` | Dört hedefte koşan testler |
| `ui/foundation/src/androidHostTest/kotlin/com/pantrixui/foundation/l10n/TextOrderOnAndroidTest.kt` | Android'in kaynak kümesi |
| `ui/components/src/commonMain/kotlin/com/pantrixui/components/data/datatable/DataTableColumn.kt` | `DataTableColumn.text(textOrder = …)` |
:::
