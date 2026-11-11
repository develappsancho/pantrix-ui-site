---
title: The app's own words
description: Why, in a Compose Multiplatform app whose reader chooses its language, the components switch to the language chosen while the app's own words stay in the device's, and two ways of taking the app's words along. The problem is not Pantrix's own: it is seen the same way in every Compose Multiplatform app that lets its reader choose a language other than the device's.
---

What is said of Compose's resources was read in the sources of Compose Multiplatform 1.12.2 (`components-resources`) or seen in an app built on the library; what was seen where is written in section 6.

## 1. The problem {#problem}

:::aside language
### Symptom

*   The reader chooses Türkçe in the settings. The components say "Kapat" and "Yükleniyor" at once; the app's own buttons and titles stay in English, the device's language.
*   On the web the screen turns Turkish, and a text the app reads outside the screen, a comment written into a file, stays English.
*   A test draws the screen in Turkish (`PantrixTestTheme(locale = PantrixLocale.Turkish)`), and the app's words in it are still English.
:::

### Cause

*   **The theme's language is the components'.** `PantrixTheme(locale = …)` gives the language to what Pantrix itself says (`PantrixStrings`) and the direction of the writing. It does not reach what the app reads from elsewhere.
*   **Compose's resources read the platform's language.** `stringResource` and `getString` take the language from the device: on Android and the JVM from the default `Locale`, on iOS from the preferred languages, on the web from the browser. They do not see `PantrixTheme.locale`.
*   **The web reads two values.** Inside the composition the resources take `navigator.languages`; outside it, `getString`'s environment takes `navigator.language` (seen in an app). An app that changes one of them changes half of its words.

## 2. The solution {#fix}

:::table
| # | Way | What it gives | What it costs |
| :--- | :--- | :--- | :--- |
| A | **The app's words in Kotlin, chosen by the theme's language.** One object of words for each language, as `PantrixStrings` is | The language changes at once and the same on every platform; a test chooses it with the theme; nothing of the platform is changed | The words leave Compose's resources |
| B | **Compose's resources, with the platform's language changed.** Each platform's own value of the language is set to the reader's choice, and the composition is built again | The words stay in the resources | A piece for each platform, two values on the web, and the platform's language changed for the whole app |
:::

## 3. With Pantrix: the words in Kotlin {#with-pantrix-the-words-in-kotlin}

### 3.1. The words

An interface holds what the app says; an object for each language says it. The theme's language chooses the object:

```kotlin
interface AppWords {
    val save: String
    val ordersTitle: String
    fun ordersCount(count: Int): String
}

object EnglishWords : AppWords {
    override val save = "Save"
    override val ordersTitle = "Orders"
    override fun ordersCount(count: Int) = if (count == 1) "1 order" else "$count orders"
}

object TurkishWords : AppWords {
    override val save = "Kaydet"
    override val ordersTitle = "Siparişler"
    override fun ordersCount(count: Int) = "$count sipariş"
}

/** The words of the nearest theme's language. */
val appWords: AppWords
    @Composable @ReadOnlyComposable
    get() = if (PantrixTheme.locale.language == "tr") TurkishWords else EnglishWords
```

*   **A screen reads `appWords`**, as a component reads `PantrixTheme.strings`: `PantrixButton(text = appWords.save, onClick = onSave, tagKey = "save")`.
*   **The automation tag is never the words.** A control's `tagKey` stays the same in every language: a test and a Maestro flow find it whatever the language is.

### 3.2. The chosen language

The reader's choice is kept by the app, and given to the outermost theme:

```kotlin
@Composable
fun App(settings: Settings) {
    val chosen by settings.language.collectAsState() // null: the device's
    PantrixTheme(locale = chosen ?: systemPantrixLocale()) { Home() }
}
```

Pantrix's components and the app's words then speak the same language, and the writing's direction follows it.

### 3.3. Testing

`PantrixTestTheme(locale = PantrixLocale.Turkish)` draws the app's words in Turkish too, since they are chosen by the theme's language. A test of a word looks for it in one language and finds the control by its tag in any.

## 4. Without Pantrix's theme: compose's resources {#without}

Compose's resources follow the platform's language, so the app sets that language to the reader's choice, on each platform in its own way, and builds the composition again with `key(language) { … }`:

:::table
| Platform | Where the language is set |
| :--- | :--- |
| Android | `Locale.setDefault(locale)`, and a configuration with that locale for the resources |
| The JVM | `Locale.setDefault(locale)` |
| iOS | `NSUserDefaults.standardUserDefaults.setObject(listOf(tag), forKey = "AppleLanguages")` |
| The web | A script in the page that gives `navigator.languages` and `navigator.language` the chosen tag: both, or what is read outside the composition stays in the browser's language |
:::

This is the way JetBrains documents for Compose Multiplatform's resources (`LocalAppLocale`). Pantrix's own words follow `PantrixTheme.locale` whichever way the app's take: the app gives the same language to the theme.

## 5. Pitfalls {#pitfalls}

*   **Half of the words on the web.** Changing only `navigator.languages` leaves `getString` outside the composition in `navigator.language`'s language (section 1).
*   **The platform's language is the whole app's.** Way B changes it for every screen and every library in the app; on iOS `AppleLanguages` lasts past the app's restart.
*   **A word made from a number.** "1 order", "2 orders": a language counts its own way, and a function in the words' object says it, not a template the screen fills.

## 6. Verification {#verification}

### Looked at by hand

*   **Way A in this repository:** Pantrix's demo keeps its texts in Kotlin, in Turkish and in English (`Bilingual`, chosen by `LocalDemoLanguage`, whose `locale` it gives to its theme). Its settings change the language at once: the shell's texts, the pages, the examples and the components together. `DemoPagesTest` draws every page in both languages; the shell's Maestro flows switch the language on Android and on iOS.
*   **Both ways in an app built on the library (Formatico, 2026-11-06):** way B on Android, in the iOS simulator and on the web, the language switching at once and kept at the next opening, the two values of the web found then; and way A, which the app moved to afterwards.

### Not verified

*   Way B in this repository: its table restates what was seen in an app and what JetBrains documents.

## 7. Where it is in this repository {#repository toc="7. In the repository"}

*   Pantrix's words: `ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/l10n/` (`PantrixStrings`, `PantrixLocale`).
*   The demo's way A: `app/shared/src/commonMain/kotlin/com/pantrixui/cmp/demo/catalog/DemoLanguage.kt`; the demo's "Language and words" page, `app/shared/src/commonMain/kotlin/com/pantrixui/cmp/demo/samples/`.
