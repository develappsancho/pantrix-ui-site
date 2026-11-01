---
title: Sorting by a language’s alphabet
description: Why a list of names is not sorted by the language's alphabet in the common code of Compose Multiplatform, what the library gives for it, and how it is tested. The problem is not Pantrix's own: it shows the same way in every Kotlin Multiplatform project that sorts names, cities or titles in its common code with sorted() or sortedBy { it.name }.
---

Which of what is said here was seen in a test that runs in this repository, and which was not verified, is written out in section 7.

## 1. The problem {#problem}

:::aside text-order
### Symptom

*   A Turkish list of customers is sorted with `sortedBy { it.name }`. "Çelik", "Öztürk", "Şahin" and "İpek" fall to the end of the list, after "Zeynep".
*   "ılık" comes **after** "iğne"; in the Turkish alphabet `ı` comes before `i`.
*   Every name that starts with a capital comes before every name that starts with a small letter: "Zeynep" before "ada".
*   The same screen gives the same (wrong) order on every platform; so it is not noticed by trying it on another platform.

```kotlin
listOf("zeytin", "şeker", "öğle", "iğne", "ılık", "çilek", "Çay", "cam").sorted()
// [cam, iğne, zeytin, Çay, çilek, öğle, ılık, şeker]
```
:::

### Cause

In Kotlin, comparing two texts (`String.compareTo`) looks at the letters' numbers in Unicode. This is an order that gives the same result on every platform, is fast, and is the alphabet of no language: the capitals' numbers are smaller than the small letters', and `ç` (231), `ö` (246), `ş` (351) and `ı` (305) are greater than `z` (122).

Sorting by a language's alphabet is a separate job, called "collation", and Kotlin's common library has nothing for it. Each platform has a tool of its own:

:::table
| Platform | Tool | Reached from common code |
| :--- | :--- | :--- |
| Android | `java.text.Collator` (with ICU behind it) | No, `androidMain` |
| Desktop (JVM) | `java.text.Collator` | No, `jvmMain` |
| iOS | `NSString.compare(_:options:range:locale:)` | No, `iosMain` |
| Web (`js`, `wasmJs`) | `Intl.Collator` | No, `webMain` |
:::

Neither of the two short cuts is enough:

1.  **Comparing with `lowercase()`.** It solves the capitals, not the alphabet: `ç` still comes after `z`.
2.  **`String.CASE_INSENSITIVE_ORDER`.** On the JVM it counts `İ` and `i` as equal; in JS, in Wasm and on iOS it does not: the same list is sorted differently on the desktop and in a browser.

## 2. The solution {#fix}

Putting each platform's own tool behind one `Comparator<String>`: an `expect` in common code, and its counterpart in four source sets.

Three decisions are kept the same on every platform, so that a list does not change from platform to platform:

*   **Capitals and small letters are equal.** "ada" and "Ada" stand side by side. Since Kotlin's `sortedWith` leaves equals in the order they came in, the sort is stable.
*   **Accents are told apart.** "resume" comes before "résumé".
*   **Digits are like letters.** "10" comes before "9". What is to be sorted as a number is sorted by the number; `java.text.Collator` has no option to sort numerically, so it was not turned on on the other platforms either.

## 3. With Pantrix {#with}

`PantrixLocale.textOrder()` gives the comparator of that language; `rememberTextOrder()` keeps the theme's language's in a composition.

```kotlin
// In common code, outside a composition
val order = PantrixLocale.Turkish.textOrder()
val sorted = customers.sortedWith(compareBy(order) { it.name })

// In a composition: by the theme's language, the same object while the language does not change
val order = rememberTextOrder()
val sorted = remember(customers, order) { customers.sortedWith(compareBy(order) { it.name }) }
```

`PantrixDataTable`'s text column takes it directly:

```kotlin
val order = rememberTextOrder()
val columns = remember(order) {
    listOf(
        DataTableColumn.text<Customer>(key = "name", header = name, sortable = true, textOrder = order) { it.name }
    )
}
PantrixDataTable(rows = remember(customers, sort) { customers.sortedWith(sort.comparator(columns)) }, columns = columns)
```

A text column given no `textOrder` sorts as before, by the letters' numbers and without telling capitals apart.

**Keep the comparator.** Setting up a comparator is not free, and a sort asks it thousands of times. Outside a composition it is kept in a field, inside one with `rememberTextOrder()`; it is not set up again for each comparison.

## 4. Without Pantrix {#without}

Four files. In common code:

```kotlin
// commonMain
expect fun textOrder(languageTag: String): Comparator<String>
```

On Android and on the desktop (the same file in two source sets):

```kotlin
// androidMain, jvmMain
actual fun textOrder(languageTag: String): Comparator<String> {
    val collator = Collator.getInstance(Locale.forLanguageTag(languageTag))
    collator.strength = Collator.SECONDARY // tells accents apart, not capitals
    return Comparator { first, second -> collator.compare(first, second) }
}
```

On iOS:

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

On the web, in the source set `js` and `wasmJs` share:

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

Two details on the web:

*   **The comparator is kept in the page.** Text is the one thing JS and Wasm hand each other the same way; an `Intl.Collator` object is carried by different types on the two targets. So the object is not taken to the Kotlin side: it is set up once for each language and kept in the page (`globalThis`), so that it is not set up again for each of a sort's thousands of questions. How much this saves was not measured in this repository.
*   **A tag it does not know throws.** `new Intl.Collator("not a tag")` throws a `RangeError`; the code above catches it and falls back to the letters' numbers. The other platforms fall back to the general rule silently on a tag they do not know.

## 5. What the helper does not do {#limits toc="5. What it does not do"}

*   **It does not give the server's order.** The order is the platform's, and platforms do not agree on every letter of every language. A list that must be in exactly a server's order (a table that comes page by page) is sorted on the server.
*   **It does not sort numbers as numbers.** "Order 10" comes before "Order 9".
*   **It does not search.** Searching a text for another text by a language (finding "ı" when "I" is typed in Turkish) is a separate job; this only compares.
*   **It does not make capitals.** `uppercase()` does not look at the language either: in Turkish "i" has to become "İ". That is why Pantrix does not turn a table's header to capitals by itself; a header is written as it is written.

## 6. Testing {#testing}

What is to be tested is that the platform's tool really knows the rules of the language; that is why the test has to run on every target. Turkish is a good test: all six of `ç`, `ğ`, `ı`, `ö`, `ş`, `ü` fall in the wrong place by the letters' numbers.

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

Do not test the equality of capitals and small letters with the Turkish "I" and "ı": the platforms' mapping of capitals can part on that pair. "Çay" and "çay" are equal on every platform.

## 7. Verification {#verification}

What was seen in the tests that run in this repository (2026-10-11, `TextOrderTest`, `TextOrderOnAndroidTest`):

:::table
| What | JVM | iOS simulator | JS (headless Chrome) | Wasm (headless Chrome) | Android (host) |
| :--- | :---: | :---: | :---: | :---: | :---: |
| Eight Turkish words in the order of the alphabet | ✓ | ✓ | ✓ | ✓ | ✓ |
| The Turkish alphabet's 29 letters in order | ✓ | ✓ | ✓ | ✓ | |
| "ada" and "Ada" equal, the list stable | ✓ | ✓ | ✓ | ✓ | ✓ |
| "resume" before "résumé" | ✓ | ✓ | ✓ | ✓ | ✓ |
| A tag it does not know gives no error | ✓ | ✓ | ✓ | ✓ | ✓ |
:::

Not verified:

*   **An Android device.** The Android test runs on the host, and there `java.text.Collator` is the JDK's. On a device the same class has ICU behind it; it is expected to know the Turkish rules, and was not tested on a device or an emulator in this repository.
*   **A packaged desktop app.** The JDK carries the rules of the languages other than English in the module `jdk.localedata`. If the runtime packaged with the app does not hold this module, `Collator` falls back to the general rule without an error, and a Turkish list is sorted wrong again. In Compose Desktop the module is added with `nativeDistributions { modules("jdk.localedata") }`. This was not tried in a packaged app in this repository; the tests run with a full JDK.
*   **Safari and Firefox.** The browser tests run in Chrome alone.
*   **Languages other than Turkish.** Languages with rules of their own, such as German, Swedish or Spanish, were not tested; the tool is the same.

## 8. Where it is in this repository {#repository toc="8. In the repository"}

:::table
| File | What |
| :--- | :--- |
| `ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/l10n/TextOrder.kt` | `PantrixLocale.textOrder()`, `rememberTextOrder()` |
| `ui/foundation/src/{androidMain,jvmMain,iosMain,webMain}/kotlin/com/pantrixui/foundation/l10n/TextOrder.*.kt` | The platforms' counterparts |
| `ui/foundation/src/commonTest/kotlin/com/pantrixui/foundation/l10n/TextOrderTest.kt` | The tests that run on four targets |
| `ui/foundation/src/androidHostTest/kotlin/com/pantrixui/foundation/l10n/TextOrderOnAndroidTest.kt` | Android's source set |
| `ui/components/src/commonMain/kotlin/com/pantrixui/components/data/datatable/DataTableColumn.kt` | `DataTableColumn.text(textOrder = …)` |
:::
