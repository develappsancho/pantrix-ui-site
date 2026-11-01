---
title: Sayıları biçimlendirme
description: Compose Multiplatform'da bir miktarın (1234567.891) ekrana neden her platformda aynı çıkmadığı, bunun için kütüphanenin ne verdiği ve nasıl sınandığı. Sorun Pantrix'e özgü değildir: ortak kodunda bir tutar, bir ölçü ya da bir yüzde gösteren her Compose Multiplatform projesinde aynı şekilde görülür.
---

Burada söylenenlerin hangisinin testle tutulduğu, hangisinin platformların belgelerinden bilindiği ve hangisinin doğrulanmadığı 8. bölümde ayrı ayrı yazılıdır.

## 1. Sorun {#problem}

:::aside number-format
### Belirti

*   Ortak kodda bir sayıyı yazıya çevirmenin tek yolu `toString()`'dir: `1234567.891.toString()` binlikleri ayırmaz, ondalığı yuvarlamaz, dilin işaretini kullanmaz. Türkçe bir ekranda `1234567.891` yazar; beklenen `1.234.567,89`'dur.
*   `toString()` platformdan platforma da değişir: `100.0`, JVM'de ve iOS'ta `100.0`, tarayıcıda (JS) `100` yazılır. Küçük ve büyük sayıların üslü yazımı da ayrıdır.
*   `String.format("%,.2f", x)` yalnızca JVM'de vardır; ortak kodda derlenmez.
*   `0.1 + 0.2` ekrana `0.30000000000000004` çıkar. `1.005` iki basamağa yuvarlanınca `1.01` beklenir, çoğu yol `1.00` verir.
:::

### Neden

Kotlin'in ortak kitaplığında sayı biçimleyici yoktur. Her platformun kendi biçimleyicisi vardır ve `expect`/`actual` ile bağlanabilir: JVM ve Android'de `java.text.NumberFormat`, iOS'ta `NSNumberFormatter`, tarayıcıda `Intl.NumberFormat`. Ama bunlar aynı sayı için aynı yazıyı vermez:

:::table
| Fark | Ne olur |
| :--- | :--- |
| Yarımın yuvarlanması | `DecimalFormat` ve `NSNumberFormatter` varsayılan olarak yarımı çift olana yuvarlar (`2.5` → `2`), `Intl.NumberFormat` sıfırdan uzağa (`2.5` → `3`) |
| Kısa yazım (`1,2 B`, `2,5 Mn`) | Java'da 12'den beri, Android'de `android.icu` ile API 24'ten beri, tarayıcıda `notation: "compact"` ile vardır; `NSNumberFormatter`'da yoktur |
| Dilin verisi | İşaretler ve kısaltmalar işletim sisteminin ya da tarayıcının taşıdığı dil verisinden gelir: aynı uygulama, eski bir Android'de ve yeni bir tarayıcıda aynı dil için farklı yazabilir; verisi olmayan bir ortam İngilizceye düşer |
| Test | Ekran görüntüsü testleri ve metin doğrulamaları platforma göre ayrı beklenti ister |
:::

Bir tasarım sistemi için sonuncusu belirleyicidir: bir grafiğin ekseni Android'de `1,2 B`, iOS'ta `1.234` yazıyorsa aynı ekran iki ayrı ekrandır.

`1.005` sorunu ise platformdan değil, sayının tutuluşundandır: bir `Double`, `1.005`'i ondan biraz küçük bir sayı olarak tutar (`1.00499999999999989…`). Yüzle çarpılınca `100.49999999999999` çıkar ve yarımın altında kalır.

## 2. Çözüm {#fix}

Sayı **ortak kodda** yazılır, platforma sorulmaz. Üç parça:

1.  **Dilin işaretleri dil paketinde durur.** Ondalık işareti, binlik işareti, rakamlar, yüzde kalıbı ve kısa yazımın dört kalıbı: bir dil için sekiz değer. Uygulamanın kendi paketi bunları kendi dili için verir.
2.  **Yuvarlama tek kuraldır: yarım sıfırdan uzağa.** `2.5` → `3`, `-2.5` → `-3`. Okulda öğretilen ve `Intl.NumberFormat`'ın varsayılanı olan kural.
3.  **Yuvarlanan, sayının yazıldığı hâlidir.** Ölçeklenmiş sayı, bir `Double`'ın kendi belirsizliği kadar (son basamağının iki adımı) yukarı itilir, sonra yarıma bakılır. Böylece `1.005` → `1.01`, `0.615` → `0.62` olur; yarımın gerçekten altında kalan (`1.0049`) altında kalır. İtme, yuvarlanan basamağın yüzde birini geçmez: ondan büyüğü `Double`'ın kayması değil, sayının kendi basamağıdır.

Bir `Double` on beş güvenilir basamak tutar. İstenen ondalık basamaklar bunu aşıyorsa yazılmaz (`1.0 / 3`, yirmi basamak istense de `0,333333333333333`'tür); on beş basamaktan uzun bir sayının on beş basamağı yazılır, gerisi sıfırdır (`1.2345e30` → `1.234.500.000.000.000.000.000.000.000.000`).

## 3. Pantrix ile {#with}

Üç fonksiyon, temanın dil paketinde (`com.pantrixui.foundation.l10n.PantrixStrings`):

```kotlin
public fun decimal(value: Number, fractionDigits: Int = 2, minFractionDigits: Int = 0, grouped: Boolean = true): String
public fun compact(value: Number, fractionDigits: Int = 1): String
public fun percent(share: Number, fractionDigits: Int = 0): String
```

```kotlin
val strings = PantrixTheme.strings
PantrixText(text = strings.decimal(order.total))          // 1.234.567,89
PantrixText(text = strings.compact(channel.followers))    // 1,2 Mn
PantrixText(text = strings.percent(task.done))            // %26
```

:::table
| Çağrı | İngilizce | Türkçe | Arapça |
| :--- | :--- | :--- | :--- |
| `decimal(1234567.891)` | `1,234,567.89` | `1.234.567,89` | `١٬٢٣٤٬٥٦٧٫٨٩` |
| `decimal(2.5, minFractionDigits = 2)` | `2.50` | `2,50` | `٢٫٥٠` |
| `decimal(1234567.5, grouped = false)` | `1234567.5` | `1234567,5` | `١٢٣٤٥٦٧٫٥` |
| `compact(1234)` | `1.2K` | `1,2 B` | `١٫٢ ألف` |
| `compact(2_500_000)` | `2.5M` | `2,5 Mn` | `٢٫٥ مليون` |
| `compact(999_950)` | `1M` | `1 Mn` | `١ مليون` |
| `percent(0.256)` | `26%` | `%26` | `٢٦٪` |
| `percent(-0.05)` | `-5%` | `-%5` | `-٥٪` |
:::

*   **`decimal`** bir miktarı yazar: bir tutar, bir ölçü, bir eksenin sayısı. Kesrin sonundaki sıfırlar atılır (`2.50` → `2,5`); `minFractionDigits` en az kaç basamağın kalacağını söyler.
*   **`compact`** bin, milyon, milyar ve trilyon olarak kısaltır; binin altındaki sayı `decimal` gibi yazılır. Yuvarlanınca bine ulaşan sayı bir üst basamaktan sayılır: `999 950`, `1000 B` değil `1 Mn`'dir.
*   **`percent`** bir payı yüzde olarak yazar: `0.256` yüzde yirmi altıdır. Elindeki sayı zaten yüzdeyse (`25.6`) önce yüze böl.
*   **`number(Int)`** yerinde durur ve başka bir iş görür: yalnızca sayan ya da adlandıran bir sayıyı (bir sayfanın numarası, bir yıl) dilin rakamlarıyla, araya işaret koymadan yazar. `2026` bir yıl olarak `2026`'dır, `2.026` değil.

Sayı olmayan `NaN`, sonu olmayan `∞` ve `-∞` olarak yazılır. Sıfıra yuvarlanan negatif bir sayının eksisi yoktur (`-0.004` → `0`).

### Kendi dilin

Paketi olmayan bir dil için işaretleri uygulama verir; verilmeyen alan İngilizce kalır:

```kotlin
val german = PantrixStrings(
    decimalSeparator = ",",
    groupSeparator = ".",
    percentTemplate = "{number} %",
    thousandsTemplate = "{number} Tsd.",
    millionsTemplate = "{number} Mio.",
    billionsTemplate = "{number} Mrd.",
    trillionsTemplate = "{number} Bio."
)
PantrixTheme(strings = german) { … }
```

Okunamayacak bir paket kurulurken geri çevrilir: ondalık işareti boş ya da binlik işaretiyle aynıysa, bir kalıpta `{number}` yoksa. Basamaklarını tek parça yazan bir dil `groupSeparator = ""` verir.

### Grafikte

`PantrixChart`, ekseni `format` vermeyen bir grafiğin değerlerini bu fonksiyonlarla yazar:

*   **Eksenin yanında** işaretlenen değer, eksenin en büyük değeri on bine ulaşmışsa kısa yazılır (`compact`: `12 B`, `1,5 Mn`). Ulaşmamışsa tam yazılır (`decimal`), bir işareti sonrakinden ayıran basamak kadar kesirle: adımı `0,5` olan eksen `1,5` yazar, adımı `50` olan `150`.
*   **Tooltip'te ve ekran okuyucuda** değer tam yazılır (`decimal`).

Kendi yazımını isteyen eksen aynı fonksiyonları çağırır; `format` eksenin yanında, tooltip'te ve ekran okuyucuda aynı yazımı verir:

```kotlin
val strings = PantrixTheme.strings
PantrixChart(
    series = series,
    yAxis = ChartAxis(format = { strings.percent(it) })    // 0.256 → %26
)
```

Tooltip'in kendi yazımı da olabilir (`ChartTooltip.format`): eksenin yanında kısa, tooltip'te birimiyle yazmak isteyen ya da ekseni hiç çizmeyen (`yAxis = null`) grafik bunu verir. Verildiğinde tooltip'te ve ekran okuyucuda o geçerlidir; eksenin yanındaki yazım eksenin kalır:

```kotlin
PantrixChart(
    series = series,
    yAxis = ChartAxis(format = { strings.compact(it) }),                    // eksende 12 B
    tooltip = ChartTooltip(format = { "${strings.decimal(it)} ziyaret" })   // tooltip'te 12.340 ziyaret
)
```

İki değer ekseni olan grafikte her değer kendi ekseninin `format`'ıyla yazılır: sayı olan seri sayı, oran olan seri yüzde olarak. Değerleri işaretlerinin yanına yazan seri de (`ChartDataLabels`) okunduğu eksenin yazımını kullanır; eksen bir şey söylemiyorsa değer on binden itibaren kısa, altında tam yazılır. Etiketin kendi yazımı `ChartDataLabels(format = …)` ile verilir.

```kotlin
PantrixChart(
    series = listOf(
        BarSeries("orders", "Sipariş", months, x = { it.name }, y = { it.orders }, labels = ChartDataLabels()),
        LineSeries("rate", "İade oranı", months, x = { it.name }, y = { it.rate }, axis = ChartYAxis.Second)
    ),
    secondYAxis = ChartAxis(format = { strings.percent(it, fractionDigits = 1) })   // 0.042 → %4,2
)
```

Yerlerin adlarını uygulama verir (`ChartAxis.label`); adı bir tam sayı olan yer (`2026`) temanın rakamlarıyla, araya işaret konmadan yazılır.

## 4. Pantrix olmadan {#without}

Aşağıdaki, bir projenin `commonMain`'ine olduğu gibi kopyalanabilir. Platform kodu yoktur.

```kotlin
import kotlin.math.abs
import kotlin.math.floor

/** Bir dilin sayı yazarken kullandığı işaretler. */
data class NumberMarks(
    val decimal: String = ".",
    val group: String = ",",
    val digits: String = "0123456789"
)

/** [value]: en çok [maxFraction] ondalık basamağa yuvarlanmış, binlikleri ayrılmış. */
fun formatDecimal(
    value: Double,
    marks: NumberMarks = NumberMarks(),
    maxFraction: Int = 2,
    minFraction: Int = 0
): String {
    if (value.isNaN()) return "NaN"
    if (value.isInfinite()) return if (value < 0) "-∞" else "∞"
    val magnitude = abs(value)
    // Bir Double'ın güvenilir on beş basamağı var: ötesi istenmiş olsa da yazılmaz.
    var places = minOf(maxFraction, 15)
    while (places > 0 && magnitude * POWERS[places] >= 1e15) places--
    var head = magnitude * POWERS[places]
    var noughts = 0
    while (head >= 1e15) {
        head /= 10.0
        noughts++
    }
    // Yarım, sayının yazıldığı hâline göre yukarı gider: 1.005, Double'da 1.00499… olarak durur.
    val seen = head + minOf(head * 4.440892098500626e-16, 0.01)
    val below = floor(seen)
    val rounded = if (seen - below >= 0.5) below + 1.0 else below
    val all = (rounded.toLong().toString() + "0".repeat(noughts)).padStart(places + 1, '0')
    val whole = all.dropLast(places)
    val fraction = all.takeLast(places).trimEnd('0').padEnd(minFraction, '0')
    val first = whole.length % 3
    val groups = listOfNotNull(whole.take(first).ifEmpty { null }) + whole.drop(first).chunked(3)
    val text = groups.joinToString(marks.group) + if (fraction.isEmpty()) "" else marks.decimal + fraction
    val written = text.map { it.digitToIntOrNull()?.let(marks.digits::get) ?: it }.joinToString("")
    val nought = whole.all { it == '0' } && fraction.all { it == '0' }
    return if (value < 0 && !nought) "-$written" else written
}

private val POWERS = listOf(1e0, 1e1, 1e2, 1e3, 1e4, 1e5, 1e6, 1e7, 1e8, 1e9, 1e10, 1e11, 1e12, 1e13, 1e14, 1e15)
```

```kotlin
val turkish = NumberMarks(decimal = ",", group = ".")
formatDecimal(1234567.891, turkish)   // 1.234.567,89
formatDecimal(1.005)                  // 1.01
```

Yüzde, payı yüzle çarpıp dilin kalıbına koymaktır (`"%" + formatDecimal(share * 100, marks, 0)`). Kısa yazım, sayıyı bine bölerek yuvarlanmış hâli binin altında kalan ilk basamağı bulmaktır; yuvarlamadan **sonra** bakılmalıdır, yoksa `999 950` için `1000 B` çıkar.

`4.440892098500626e-16`, ikinin eksi elli birinci kuvvetidir: bir `Double`'ın son basamağının iki adımı, sayının kendisine oranla.

## 5. Yardımcının yapmadıkları {#limits toc="5. Yapmadıkları"}

*   **Para birimi.** Simgenin yeri, boşluğu ve kuruş basamakları para birimine ve dile göre değişir. `decimal` sayıyı yazar; simgeyi ekran koyar.
*   **Çoğul.** `1 dosya`, `2 dosya`, Arapçadaki altı çoğul biçimi: sayıya göre sözcük seçmez.
*   **Hint gruplaması** (`12,34,567`) ve dört basamaklıyı gruplamayan diller (İspanyolca `1234`, ama `12.345`): her üç basamak ayrılır.
*   **Uzun kısa yazım** (`1,2 bin`, `2,5 milyon`): kalıplar verilerek yapılır, hazır paketi yoktur.
*   **Bilimsel yazım** (`1,2 × 10⁹`) ve birimler (`12 km`, `3,4 MB`).
*   **Okuma.** Yazıdan sayıya geri dönmez: bir alana yazılan `1.234,5` ayrıştırılmaz.
*   **`Long`'un tamamı.** Değer `Double`'a çevrilir; `9 007 199 254 740 993` gibi on beş basamağı aşan bir tam sayı son basamaklarını yitirir. Böyle bir sayı çoğu zaman bir kimliktir ve `toString()` ile yazılır.

## 6. Test etmek {#testing}

Biçimleyici saf bir fonksiyondur: tema, Compose ya da platform gerekmez.

```kotlin
@Test
fun a_total_is_written_as_turkish_writes_it() {
    assertEquals("1.234.567,89", PantrixStrings.Turkish.decimal(1234567.891))
    assertEquals("1,2\u00A0Mn", PantrixStrings.Turkish.compact(1_234_567))
}
```

Bir ekranın testi, hangi dilin geleceğini kendisi söyler: `PantrixTheme(strings = PantrixStrings.Turkish)`. Aynı test beş hedefte de aynı beklentiyle geçer; platforma göre ayrı beklenti gerekmez.

## 7. Tuzaklar {#pitfalls}

*   **Görünmeyen boşluk.** Türkçe ve Arapça kısa yazımda sayı ile kısaltma arasındaki boşluk satır bölmeyen boşluktur (U+00A0): `1,2 B` satır sonunda ikiye ayrılmaz. Bir testte düz boşlukla karşılaştırmak düşer; beklentiyi `\u00A0` ile yaz.
*   **Arapça işaretler.** Ondalık işareti `٫` (U+066B), binlik işareti `٬` (U+066C), yüzde işareti `٪` (U+066A) ve ardından görünmeyen bir yön işaretidir (U+061C): çevresindeki yazı ters yöne aksa da işareti sayının yanında tutar. Arapça paket rakamlarını Arapçanın rakamlarıyla yazar; İngilizce rakam isteyen ekran pakete `digits = PantrixStrings.English.digits` verir.
*   **Eksi işareti.** Türkçede eksi, yüzde işaretinin önündedir: `-%5`. Kalıba sayıyla birlikte eksiyi koyan bir kod `%-5` yazar.
*   **`Float`.** `0.1f`, `Double`'a çevrilince `0.10000000149011612` olur. `decimal` onu `0,1` yazar, çünkü istenen basamağa yuvarlar; on beş basamak istenirse `Float`'ın kayması görünür.
*   **Çok basamak istemek.** `fractionDigits = 20` hata değildir, ama on beş güvenilir basamağın ötesi yazılmaz.

## 8. Doğrulama {#verification}

### Testler

`PantrixNumbersTest` (`commonTest`), beş hedefte aynı beklentilerle koşar: JVM, Android (host), iOS simülatörü, tarayıcıda JS ve Wasm. Tuttukları:

*   Üç dilin işaretleri, rakamları ve kısa yazımları; gruplama bir basamaktan dokuz basamağa.
*   Yarımın sıfırdan uzağa yuvarlanması, negatifler dahil.
*   Yazıldığı hâliyle yuvarlama: `1.005`, `0.615`, `1.255`, `8.345`, `4.35`; ve yirmi bin tam sayıya kadar her yedinci yüzdelik için üç şey: kendisi geri yazılır, ardına `5` eklenince bir üste çıkar, `49` eklenince yerinde kalır.
*   On beş basamağın ötesi, `NaN` ve sonsuz, sıfıra yuvarlanan negatif.
*   Bine yuvarlanan kısa yazımın bir üst basamağa geçmesi, her basamakta.
*   Geri çevrilen basamak sayıları, işaretler ve kalıplar; her biri neyin verildiğini söyleyen bir iletiyle.

Yuvarlamanın hesabı ayrıca, koda geçmeden önce, on milyon yüzdelik ve iki milyon yarım üzerinde denendi (Python'da, aynı `Double`'larla): yanlış çıkan olmadı.

### Belgelerden bilinenler

Platformların 1. bölümdeki farkları (varsayılan yuvarlama kuralları, kısa yazımın hangi platformda bulunduğu) o platformların kendi belgelerinde yazılı olanlardır. **Bu depoda yan yana çalıştırılıp karşılaştırılmadılar.**

### Doğrulanmayanlar

*   **Arapça yazımın bir Arapça okurca okunması.** İşaretler ve kısaltmalar Unicode'un dil verisindeki (CLDR) biçimlerdir; bir ana dil konuşuru bakmadı.
*   **Ekrandaki çizim.** Arapça yüzde işaretinin ve eksinin, sağdan sola akan bir satırda sayının hangi yanında durduğu testte değil, yalnızca karakter dizisi olarak tutulur.
*   **Arapçanın Latin rakamlı bölgeleri** (Mağrip, Körfez'in bir kısmı): hazır paket Arap rakamlarıyla yazar.

## 9. Bu depoda nerede {#repository toc="9. Depoda"}

:::table
| Ne | Nerede |
| :--- | :--- |
| Üç fonksiyon ve dilin işaretleri | `ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/l10n/PantrixStrings.kt` |
| Dilin işaretleriyle yazım | aynı pakette `NumberWriting.kt` |
| Yuvarlama ve basamaklar | aynı pakette `WrittenDigits.kt` |
| Testler | `PantrixNumbersTest` (`ui/foundation/src/commonTest/.../l10n`) |
| Kullanan bileşen | `PantrixChart`: `ui/components/src/commonMain/kotlin/com/pantrixui/components/chart/cartesian/` (`ValueScale.kt`, `ChartReading.kt`) |
:::
