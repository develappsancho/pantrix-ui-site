---
title: Formatting numbers
description: Why, in Compose Multiplatform, an amount (1234567.891) does not come out the same on the screen on every platform, what the library gives for it, and how it is tested. The problem is not Pantrix's own: it shows the same way in every Compose Multiplatform project that shows a sum, a measure or a percentage in its common code.
---

Which of what is said here is held by a test, which is known from the platforms' documentation, and which was not verified, is written out in section 8.

## 1. The problem {#problem}

:::aside number-format
### Symptom

*   In common code the one way to turn a number into text is `toString()`: `1234567.891.toString()` does not part the thousands, does not round the fraction, does not use the language's signs. On a Turkish screen it writes `1234567.891`; what is expected is `1.234.567,89`.
*   `toString()` changes from platform to platform too: `100.0` is written `100.0` on the JVM and iOS, and `100` in a browser (JS). Small and large numbers' exponent forms differ as well.
*   `String.format("%,.2f", x)` exists on the JVM alone; it does not compile in common code.
*   `0.1 + 0.2` comes out on the screen as `0.30000000000000004`. When `1.005` is rounded to two places, `1.01` is expected; most ways give `1.00`.
:::

### Cause

Kotlin's common library has no number formatter. Each platform has a formatter of its own, and they can be wired with `expect`/`actual`: `java.text.NumberFormat` on the JVM and Android, `NSNumberFormatter` on iOS, `Intl.NumberFormat` in a browser. But they do not give the same text for the same number:

:::table
| Difference | What happens |
| :--- | :--- |
| Rounding a half | `DecimalFormat` and `NSNumberFormatter` round a half to the even one by default (`2.5` → `2`), `Intl.NumberFormat` away from nought (`2.5` → `3`) |
| The short form (`1.2K`, `2.5M`) | It exists in Java since 12, on Android with `android.icu` since API 24, in a browser with `notation: "compact"`; `NSNumberFormatter` has none |
| The language's data | The signs and the abbreviations come from the language data the operating system or the browser carries: the same app may write differently for the same language on an old Android and in a new browser; a place without the data falls back to English |
| Tests | Screenshot tests and text checks need an expectation for each platform |
:::

For a design system the last one decides: if a chart's axis writes `1,2 B` on Android and `1.234` on iOS, the same screen is two different screens.

The `1.005` problem, though, comes not from the platform but from how the number is held: a `Double` holds `1.005` as a number a little smaller than it (`1.00499999999999989…`). Multiplied by a hundred it gives `100.49999999999999`, and stays below the half.

## 2. The solution {#fix}

The number is written **in common code**, and the platform is not asked. Three parts:

1.  **The language's signs stand in the language's pack.** The decimal sign, the group sign, the digits, the percentage's pattern and the short form's four patterns: eight values for a language. The app's own pack gives them for its own language.
2.  **Rounding is one rule: a half away from nought.** `2.5` → `3`, `-2.5` → `-3`. The rule taught at school, and `Intl.NumberFormat`'s default.
3.  **What is rounded is the number as it is written.** The scaled number is pushed up by as much as a `Double`'s own uncertainty (two steps of its last digit), and then the half is looked at. So `1.005` → `1.01` and `0.615` → `0.62`; what really stays below the half (`1.0049`) stays below it. The push never passes a hundredth of the digit being rounded: anything larger than that is no slip of the `Double` but the number's own digit.

A `Double` holds fifteen reliable digits. Decimal places asked for past that are not written (`1.0 / 3` is `0.333333333333333`, even where twenty places are asked for); of a number longer than fifteen digits, fifteen digits are written and the rest are noughts (`1.2345e30` → `1,234,500,000,000,000,000,000,000,000,000`).

## 3. With Pantrix {#with}

Three functions, in the theme's language pack (`com.pantrixui.foundation.l10n.PantrixStrings`):

```kotlin
public fun decimal(value: Number, fractionDigits: Int = 2, minFractionDigits: Int = 0, grouped: Boolean = true): String
public fun compact(value: Number, fractionDigits: Int = 1): String
public fun percent(share: Number, fractionDigits: Int = 0): String
```

```kotlin
val strings = PantrixTheme.strings
PantrixText(text = strings.decimal(order.total))          // 1,234,567.89
PantrixText(text = strings.compact(channel.followers))    // 1.2M
PantrixText(text = strings.percent(task.done))            // 26%
```

:::table
| Call | English | Turkish | Arabic |
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

*   **`decimal`** writes an amount: a sum, a measure, a number on an axis. The noughts at the end of the fraction are dropped (`2.50` → `2.5`); `minFractionDigits` says how many places at least are kept.
*   **`compact`** shortens to thousands, millions, billions and trillions; a number below a thousand is written as `decimal` writes it. A number that reaches a thousand once rounded is counted in the next step up: `999 950` is `1M`, not `1000K`.
*   **`percent`** writes a share as a percentage: `0.256` is twenty-six per cent. If the number you have is a percentage already (`25.6`), divide it by a hundred first.
*   **`number(Int)`** stays where it is and does another job: it writes a number that only counts or names (a page's number, a year) in the language's digits, with no sign in between. `2026` as a year is `2026`, not `2,026`.

What is not a number is written `NaN`, and what has no end `∞` and `-∞`. A negative number rounded to nought has no minus (`-0.004` → `0`).

### Your own language

For a language with no pack, the app gives the signs; a field not given stays English:

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

A pack that could not be read is turned down as it is built: where the decimal sign is empty or the same as the group sign, or where a pattern has no `{number}`. A language that writes its digits in one piece gives `groupSeparator = ""`.

### In a chart

`PantrixChart` writes the values of a chart whose axis gives no `format` with these functions:

*   **Beside the axis**, a value marked is written short if the axis's greatest value has reached ten thousand (`compact`: `12K`, `1.5M`). If it has not, it is written in full (`decimal`), with as much fraction as the digit that parts one mark from the next: an axis whose step is `0.5` writes `1.5`, one whose step is `50` writes `150`.
*   **In the tooltip and to a screen reader** the value is written in full (`decimal`).

An axis that wants a writing of its own calls the same functions; `format` gives the same writing beside the axis, in the tooltip and to a screen reader:

```kotlin
val strings = PantrixTheme.strings
PantrixChart(
    series = series,
    yAxis = ChartAxis(format = { strings.percent(it) })    // 0.256 → 26%
)
```

The tooltip can have a writing of its own as well (`ChartTooltip.format`): a chart that wants to write short beside the axis and with its unit in the tooltip, or one that draws no axis at all (`yAxis = null`), gives it. Where it is given, it holds in the tooltip and to a screen reader; the writing beside the axis stays the axis's:

```kotlin
PantrixChart(
    series = series,
    yAxis = ChartAxis(format = { strings.compact(it) }),                    // on the axis 12K
    tooltip = ChartTooltip(format = { "${strings.decimal(it)} visits" })    // in the tooltip 12,340 visits
)
```

In a chart with two axes of values, each value is written by its own axis's `format`: a series that is a number as a number, one that is a rate as a percentage. A series that writes its values beside their marks (`ChartDataLabels`) uses the writing of the axis it is read by too; where the axis says nothing, a value is written short from ten thousand on and in full below. A label's own writing is given with `ChartDataLabels(format = …)`.

```kotlin
PantrixChart(
    series = listOf(
        BarSeries("orders", "Orders", months, x = { it.name }, y = { it.orders }, labels = ChartDataLabels()),
        LineSeries("rate", "Return rate", months, x = { it.name }, y = { it.rate }, axis = ChartYAxis.Second)
    ),
    secondYAxis = ChartAxis(format = { strings.percent(it, fractionDigits = 1) })   // 0.042 → 4.2%
)
```

The places' names are the app's to give (`ChartAxis.label`); a place whose name is a whole number (`2026`) is written in the theme's digits, with no sign in between.

## 4. Without Pantrix {#without}

What follows can be copied as it is into a project's `commonMain`. There is no platform code.

```kotlin
import kotlin.math.abs
import kotlin.math.floor

/** The signs a language writes numbers with. */
data class NumberMarks(
    val decimal: String = ".",
    val group: String = ",",
    val digits: String = "0123456789"
)

/** [value]: rounded to [maxFraction] decimal places at the most, with its thousands parted. */
fun formatDecimal(
    value: Double,
    marks: NumberMarks = NumberMarks(),
    maxFraction: Int = 2,
    minFraction: Int = 0
): String {
    if (value.isNaN()) return "NaN"
    if (value.isInfinite()) return if (value < 0) "-∞" else "∞"
    val magnitude = abs(value)
    // A Double has fifteen reliable digits: what lies past them is not written, even where it is asked for.
    var places = minOf(maxFraction, 15)
    while (places > 0 && magnitude * POWERS[places] >= 1e15) places--
    var head = magnitude * POWERS[places]
    var noughts = 0
    while (head >= 1e15) {
        head /= 10.0
        noughts++
    }
    // A half goes up by the number as it is written: 1.005 stands in a Double as 1.00499….
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

A percentage is the share multiplied by a hundred and put into the language's pattern (`"%" + formatDecimal(share * 100, marks, 0)`). The short form is dividing the number by a thousand until its rounded form stays below a thousand; this is looked at **after** rounding, or `999 950` gives `1000K`.

`4.440892098500626e-16` is two to the power of minus fifty-one: two steps of a `Double`'s last digit, as a share of the number itself.

## 5. What the helper does not do {#limits toc="5. What it does not do"}

*   **Currency.** The symbol's place, its space and the digits of the cents change with the currency and the language. `decimal` writes the number; the screen puts the symbol.
*   **Plurals.** `1 file`, `2 files`, Arabic's six plural forms: it does not choose a word by the number.
*   **Indian grouping** (`12,34,567`) and languages that do not group a four-digit number (Spanish `1234`, but `12.345`): every three digits are parted.
*   **The long short form** (`1.2 thousand`, `2.5 million`): it is made by giving the patterns; there is no ready pack.
*   **Scientific notation** (`1.2 × 10⁹`) and units (`12 km`, `3.4 MB`).
*   **Reading.** It does not go back from text to a number: a `1.234,5` typed into a field is not parsed.
*   **The whole of a `Long`.** The value is turned into a `Double`; a whole number past fifteen digits, such as `9 007 199 254 740 993`, loses its last digits. Such a number is most often an identifier, and is written with `toString()`.

## 6. Testing {#testing}

The formatter is a pure function: it needs no theme, no Compose and no platform.

```kotlin
@Test
fun a_total_is_written_as_turkish_writes_it() {
    assertEquals("1.234.567,89", PantrixStrings.Turkish.decimal(1234567.891))
    assertEquals("1,2 Mn", PantrixStrings.Turkish.compact(1_234_567))
}
```

A screen's test says itself which language comes: `PantrixTheme(strings = PantrixStrings.Turkish)`. The same test passes on all five targets with the same expectation; no expectation for each platform is needed.

## 7. Pitfalls {#pitfalls}

*   **An invisible space.** In the Turkish and Arabic short forms, the space between the number and the abbreviation is a no-break space (U+00A0): `1,2 B` is not split in two at a line's end. Comparing with a plain space in a test fails; write the expectation with ` `.
*   **Arabic signs.** The decimal sign is `٫` (U+066B), the group sign `٬` (U+066C), the percent sign `٪` (U+066A) followed by an invisible mark of direction (U+061C): it keeps the sign beside the number even where the text around it flows the other way. The Arabic pack writes its digits in Arabic's digits; a screen that wants English digits gives the pack `digits = PantrixStrings.English.digits`.
*   **The minus sign.** In Turkish the minus stands before the percent sign: `-%5`. Code that puts the minus into the pattern with the number writes `%-5`.
*   **`Float`.** `0.1f` turned into a `Double` is `0.10000000149011612`. `decimal` writes it `0.1`, since it rounds to the places asked for; where fifteen places are asked for, the `Float`'s slip shows.
*   **Asking for many places.** `fractionDigits = 20` is no error, but what lies past the fifteen reliable digits is not written.

## 8. Verification {#verification}

### Tests

`PantrixNumbersTest` (`commonTest`) runs with the same expectations on five targets: the JVM, Android (host), the iOS simulator, JS and Wasm in a browser. What they hold:

*   The signs, digits and short forms of three languages; grouping from one digit to nine.
*   Rounding a half away from nought, negatives among them.
*   Rounding by the number as it is written: `1.005`, `0.615`, `1.255`, `8.345`, `4.35`; and, for every seventh hundredth up to twenty thousand whole numbers, three things: it is written back as itself, with a `5` added after it it goes up one, with `49` added it stays where it is.
*   What lies past fifteen digits, `NaN` and infinity, a negative rounded to nought.
*   The short form that rounds to a thousand moving to the next step up, at every step.
*   Numbers of places, signs and patterns that are turned down, each with a message that says what was given.

The arithmetic of the rounding was also tried, before it went into the code, on ten million hundredths and two million halves (in Python, with the same `Double`s): none came out wrong.

### Known from the documentation

The platforms' differences in section 1 (the default rounding rules, which platform has the short form) are what those platforms' own documentation says. **They were not run side by side and compared in this repository.**

### Not verified

*   **Arabic writing being read by an Arabic reader.** The signs and abbreviations are the forms of Unicode's language data (CLDR); no native speaker looked at them.
*   **The drawing on the screen.** Which side of the number the Arabic percent sign and the minus stand on, in a line that flows from right to left, is held in the test only as a string of characters.
*   **Arabic's regions of Latin digits** (the Maghreb, part of the Gulf): the ready pack writes in Arabic digits.

## 9. Where it is in this repository {#repository toc="9. In the repository"}

:::table
| What | Where |
| :--- | :--- |
| The three functions and the language's signs | `ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/l10n/PantrixStrings.kt` |
| Writing with the language's signs | `NumberWriting.kt` in the same package |
| Rounding and digits | `WrittenDigits.kt` in the same package |
| Tests | `PantrixNumbersTest` (`ui/foundation/src/commonTest/.../l10n`) |
| The component that uses it | `PantrixChart`: `ui/components/src/commonMain/kotlin/com/pantrixui/components/chart/cartesian/` (`ValueScale.kt`, `ChartReading.kt`) |
:::
