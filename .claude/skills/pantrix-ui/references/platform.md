# Languages and platform services

What the theme gives a screen beyond its look: the language and the words the components say, numbers, dates and
alphabetical order by language, the platform and the system's settings, and the services a screen uses the same way on
every platform. The pages `language`, `platform`, `haptics`, `clipboard`, `files`, `shortcuts`, `analytics` and
`diagnostics` have every example.

## Language and reading direction

- **The language.** `PantrixLocale` is a language and the direction of its script: `PantrixLocale.English`,
  `PantrixLocale.Turkish`, `PantrixLocale.Arabic`, or `PantrixLocale.forLanguageTag("de-DE")`. The theme takes the
  system's unless it is given one: `PantrixTheme(locale = …) { … }`. `PantrixTheme.locale` is the nearest theme's.
- **The direction** follows the language: a component reads it from Compose's `LocalLayoutDirection`, which the theme
  sets. A layout written with start and end turns round by itself (rule 6 of [SKILL.md](../SKILL.md)).
- **The components' words.** `PantrixStrings` holds what the components say by themselves: "Close", "Loading", what a
  screen reader is told. Turkish, English and Arabic come with it (`PantrixStrings.forLocale(locale)`); another
  language is a pack's `copy(…)`, given as `PantrixTheme(strings = …)`. The theme never translates the app's own texts:
  those come from the app's resources.

## Numbers, dates and order

Each is written in the theme's language, its digits included:

```kotlin
import androidx.compose.runtime.Composable
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.date.PantrixDate
import com.pantrixui.foundation.theme.PantrixTheme

@Composable
fun OrderLine(total: Double, share: Double, due: PantrixDate) {
    val strings = PantrixTheme.strings
    PantrixText(text = strings.decimal(total))                      // 1,234.56 · 1.234,56 · ١٬٢٣٤٫٥٦
    PantrixText(text = strings.percent(share))                      // 26% · %26 · ٢٦٪
    PantrixText(text = strings.date(due.day, due.month, due.year))  // Oct 17, 2026 · 17 Eki 2026
}
```

- **Numbers.** `strings.decimal(value, fractionDigits, minFractionDigits, grouped)`, `strings.compact(value)` (`1.2K`)
  and `strings.percent(share)`. `Int.toString()` and `String.format` write neither the language's separators nor its
  digits.
- **Dates and times.** `PantrixDate` and `PantrixTime` need no date library. `strings.date`, `strings.longDate`,
  `strings.longMonthYear` and `strings.weekday` write them; `PantrixDate.parse("2026-10-17")` and `toString()` are ISO
  8601. The first day of the week is the language's: `PantrixLocale.firstDayOfWeek`.
- **Today.** `rememberToday()` in a screen: it reads the screen's clock, `LocalPantrixClock`, which a test or a preview
  gives, and is read again at midnight. `PantrixDate.today()` reads the device's clock once.
- **Alphabetical order.** `rememberTextOrder()` is a `Comparator<String>` in the theme's language: in Turkish `ı`
  comes before `i`, and `Ç` after `C`. `sortedWith(rememberTextOrder())` sorts a list as a reader expects.

## The platform and the system

- **The platform's conventions.** `PantrixTheme.platform` is `PantrixPlatform`: its `type` (`PlatformType.Android`,
  `Ios`, `Desktop`, `Web`), whether it is used by touch (`isTouch`), its smallest touch target (`minTouchTarget`) and
  its focus ring. The components draw each platform's own geometry from it.
- **Where the code runs.** `currentPlatformType()` and `currentOperatingSystem()`; in a browser the system is the one
  the browser runs on.
- **The system's settings.** `systemReduceMotion()`, `systemIsInHighContrast()`, `systemDifferentiatesWithoutColor()`,
  `systemPointerIsTouch()`, `systemPantrixLocale()`, `systemUtcOffset()`. The theme reads the ones its components need
  by itself: reduced motion sets every duration of `PantrixTheme.motion` to nought.

## Services

Each works the same way on every platform, and says where a platform cannot.

- **Haptics.** `PantrixTheme.haptics.perform(HapticType.Success)`: a meaning (`Selection`, `ToggleOn`, `ToggleOff`,
  `Success`, `Warning`, `Error`, `Impact`, `LongPress`), never a vibration pattern. The components play their own; the
  desktop and the web play nothing. `PantrixTheme(haptics = PantrixHaptics.NoOp)` turns them off.
- **Clipboard.** `rememberPantrixClipboard()` and its suspending `copyText(text, label)`, which returns whether the
  text got there. A web page that is not secure and a JVM with no display have no clipboard; on Android 13 and later
  the system shows what was copied itself, and `currentClipboardTraits().announcedBySystem` tells the app not to say
  it again.
- **Files.** `rememberFilePicker()` and `pick(FilePickRequest(accept, multiple)) { files -> … }`; where only pictures
  are asked for, Android and iOS open their photo picker. `Modifier.fileDropTarget(onOver, onFiles)` takes files dropped
  on the desktop and the web. A `PantrixFile` reads its content when it is asked for. `PantrixFileDrop` is the
  component built on both. `rememberFileSaver()` and `save(PantrixFile(name, bytes, mimeType)) { result -> … }` give
  the user a file the app made, in each platform's window for saving: `FileSaveResult.Saved`, `Cancelled`, `Failed`,
  and `Started` on the web, whose download says no more.
- **Keyboard shortcuts.** `PantrixShortcut(Key.S, command = true)` is Command+S on Apple's systems and Ctrl+S
  elsewhere; `Modifier.onShortcut(shortcut) { … }` listens for it, and `Modifier.shortcutRoot()` on the window's root
  hears it while nothing has the focus.
- **Pointer shapes.** `Modifier.pointerHoverIcon(PantrixPointerIcons.ResizeHorizontal)` (and `ResizeVertical`) over a
  handle that is dragged; iOS shows no pointer.
- **Analytics.** `PantrixTheme(analytics = PantrixAnalytics { event -> … })` receives every component's events: a
  `PantrixUiEvent` of `component`, `action`, `testTag`, `screen` and `props`, never what was typed.
  `LocalPantrixAnalytics` takes the app's own events to the same place. A key that goes into an event, a row's or an
  option's, is never personal data.
- **Diagnostics.** In a debug build, `PantrixTheme(diagnostics = PantrixDiagnostics.Log)` writes what a running screen
  shows wrong to the platform's log: one tag on two nodes, colours that do not read, a squeezed touch target, a blank
  name, a language with no strings, a shadowed shortcut. `PantrixFindings` keeps them for a test and for
  `PantrixDiagnosticsInspector`. A release build leaves them `PantrixDiagnostics.Off`, the default.
