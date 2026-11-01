---
title: Date and time
description: Why a day (year, month, day) and a time of day are not found by themselves in common code in Compose Multiplatform, what the library gives for it, how a day and a time are turned into an app's own type, how a day and a time become a moment, how they are written by language (12 or 24 hours included), how "today" is known, and how a range that slides with the clock ("the last 15 minutes") is kept and read. The problem is not Pantrix's own: it is seen the same way in every Compose Multiplatform project that shows a date or has one chosen.
---

Everything said here was seen in the standard library of Kotlin 2.4.21 and in tests that run in this repository; which was seen where is written in section 8.

## 1. The problem {#problem}

:::aside date-time
### Symptom

*   There is no "day" type in common code. The standard library knows a **moment** (`kotlin.time.Instant`, `Clock.System.now()`), but which day a moment is depends on where the clock is read: the moment that is 17 October 00:30 in Istanbul is still 16 October in London.
*   Choosing a date library spreads into every layer of the app. If `kotlinx-datetime`'s `LocalDate` goes into a design system's API, an app that does not use it has to take that library too; `java.time` is only on the JVM and Android, `NSDate` only on iOS, the browser's `Date` only on the web.
*   Calendar arithmetic is easily done wrong: the month's length, leap years (1900 is not a leap year, 2000 is), the day of the week, "a month later" (a month after 31 January is the last day of February).
*   Writing a day depends on the language: the month names, the order ("Oct 17, 2026" and "17 Eki 2026"), the digits (١٧ in Arabic), which day the week starts on (Sunday in English, Monday in Turkish, Saturday in Arabic).
*   On a screen left open for a long time, "today" changes at midnight; a day read once is wrong the next day.
:::

### Cause

Kotlin 2.4.21's standard library gives `Instant`, `Clock` and `Duration` in the `kotlin.time` package; they are not experimental, and ask for no opt-in. A calendar day, time zones and writing a day are not in the standard library:

:::table
| What | Where it is found |
| :--- | :--- |
| A moment, now | The standard library: `kotlin.time.Instant`, `Clock.System.now()` |
| A calendar day, time zones | `kotlinx-datetime` (a separate dependency), `java.time` (JVM, Android), `NSCalendar` (iOS), `Date` (web) |
| The device clock's offset from UTC | Each platform's own API: `java.util.TimeZone`, `NSTimeZone`, `Date.getTimezoneOffset()` |
| Month and day names | Each platform's localisation; not in common code |
:::

## 2. The solution: nine pieces {#fix}

In foundation (`com.pantrixui.foundation.date` and `com.pantrixui.foundation.l10n`):

1.  **`PantrixDate(year, month, day)`**: a day with no time and no time zone. It is validated when built (a 13th month, 30 February are refused), can be ordered, and is written and read in ISO 8601 form (`2026-10-17`, `PantrixDate.parse`). Its arithmetic: `dayOfWeek` (ISO: 1 Monday, 7 Sunday), `epochDays` and `ofEpochDays` (the number of days from 1970), `plusDays`, `plusMonths` and `plusYears` (the month's last day where the month is short), `daysInMonth`, `isLeapYear`.
2.  **`PantrixDateRange(start, end)`**: a range of days with both ends included; its end cannot be before its start. `day in range`.
3.  **Today and the screen's clock**: `PantrixDate.today()` is the day the device's clock shows; `PantrixDate.of(moment, offset)` is which day a moment is on a clock `offset` ahead of UTC. On a screen, **`rememberToday()`**: the day of the screen's clock, renewed by itself at midnight. The screen's clock is **`PantrixClock(clock, utcOffset)`**, a `Clock` and an offset function; **`LocalPantrixClock`** gives it, and its default is the device's (`PantrixClock.System`). `rememberToday()`, the time field's picker, the elastic date picker and the charts' moments read it; a test or a preview gives its own clock (3.4, 6).
4.  **`systemUtcOffset(moment)`** (`com.pantrixui.foundation.platform`): how far the device's clock is ahead of UTC at that moment, summer time included. With each platform's own API.
5.  **The date words of `PantrixStrings`**, in each language: short and long month names (`month`, `longMonth`), the days of the week (`weekday`, `shortWeekday`), a day (`date`: "Oct 17, 2026"), a full day with the day of the week (`longDate`: "Saturday, October 17, 2026"), a month (`monthYear`, `longMonthYear`: "October 2026").
6.  **`PantrixTime(hour, minute, second = 0)`**: a 24-hour time with no day and no time zone. It is validated when built, can be ordered, and is written and read in ISO 8601 form (`14:05`, `14:05:30`, `PantrixTime.parse`); `secondOfDay` and `ofSecondOfDay`, `PantrixTime.of(moment, offset)`, `PantrixTime.now()`. With **`PantrixDate.at(time)`**, a day and a time become a moment (`Instant`) on the device's clock, the nights the clock is put forward and back included (3.7).
7.  **The time words of `PantrixStrings`**: `time(hour, minute, is24Hour = …)` writes a time in the layout the caller asks for: `true` 24 hours (`22:00`, like `clock`), `false` 12 hours with the language's words for the half of the day ("10:00 PM", "ÖS 10:00", "٢:٠٥ م"). The pack does not know which layout to use; the caller says. `hour`, `minute`, `period`, `am`, `pm` and `twelveHourTemplate` are a time picker's words.

8.  **`PantrixTimePoint` and `PantrixTimeRange`**: a point in time that can slide with the clock, and two ends. A point is now (`Now`), a span from now (`Relative(-15, Minutes)`, which can be rounded to a unit: `Relative(-1, Days, roundTo = Days)` is yesterday) or a moment (`Absolute(instant)`). `resolve(now)` turns the points into the two `Instant`s of that moment: the start is rounded down, the end up; a range whose end falls before its start is `null`. The units are `PantrixTimeUnit`: from the second to the hour, elapsed time; from the day to the year, the calendar's (the summer-time night included). The ends are written and read in Elasticsearch's and Kibana's **date math**: `toDateMath()` (`now-15m`, `now/d`, `2026-10-17T14:15:00+03:00`), `PantrixTimePoint.parseDateMath`.
9.  **`PantrixTimeWords`** (`PantrixStrings.timeWords`): the words of a span, with each language's plural rules: "~ 15 minutes ago", "Last 15 minutes", "Next 2 hours", ", rounded to the day". English (one and many), Turkish (a single form) and Arabic (CLDR's six forms, Arabic digits).

In the components, on top of these stand **`PantrixDatePicker`** (`date.datepicker`), a calendar that has a day or a range chosen; **`PantrixTimePicker`** (`date.timepicker`), a picker that has a time chosen from columns; **`PantrixElasticDatePicker`** (`date.elasticdatepicker`), a field like Elastic UI's `EuiSuperDatePicker` that has a range chosen that slides with the clock: the quick menu, the range's name or its two ends, Refresh and Update; **`PantrixDateTextField`** and **`PantrixDateFilledTextField`** (`date.datetextfield`, `date.datefilledtextfield`), text fields that have a day typed in digits or chosen from the calendar their button opens (3.10); and **`PantrixTimeTextField`** and **`PantrixTimeFilledTextField`** (`date.timetextfield`, `date.timefilledtextfield`), fields that have a time typed the same way or chosen from the time picker (3.11). The four fields share one inner core (`date/common/PickerField.kt`); each gives only its own digit rules, errors and picker.

No date dependency was added to the library. `PantrixDate` and `PantrixTime` are three numbers each; an app turns them into its own type in one line (3.2, 3.7).

## 3. With Pantrix {#with}

### 3.1. Having a day chosen

```kotlin
var due by remember { mutableStateOf<PantrixDate?>(null) }
PantrixDatePicker(
    selected = due,
    onSelect = { due = it },
    minDate = rememberToday(),
    tagKey = "due"
)
```

The calendar does not keep the selection: `selected` is the app's, and the day pressed comes to `onSelect`. Opening it in a popover, a dialog or a sheet is the app's job; the demo shows all four, the page included (`DatePickerSamples.kt`).

The calendar stands at its own width (the room of seven days; on a phone, as much as the platform's touch target), so it does not widen a popover or a dialog needlessly. In a sheet, or at a page's width, give `Modifier.fillMaxWidth()`: the calendar fills that width, its columns widen up to `DatePickerStyle.maxCellWidth` (one and a half times the cell), and its rows and the days' circles keep their height; in a wider room it stands in the middle.

### 3.2. Turning it into your own date type

`kotlinx-datetime` (0.7 and later; in older versions `monthNumber` in place of `month.number`, and `dayOfMonth` in place of `day`):

```kotlin
fun LocalDate.toPantrix(): PantrixDate = PantrixDate(year, month.number, day)
fun PantrixDate.toLocalDate(): LocalDate = LocalDate(year, month, day)
```

`java.time` (JVM, Android):

```kotlin
fun java.time.LocalDate.toPantrix(): PantrixDate = PantrixDate(year, monthValue, dayOfMonth)
fun PantrixDate.toJavaLocalDate(): java.time.LocalDate = java.time.LocalDate.of(year, month, day)
```

With a server or a JSON, by ISO 8601 text: `PantrixDate.parse("2026-10-17")` and `date.toString()`.

By a number (a database, preferences): `date.epochDays` and `PantrixDate.ofEpochDays(days)`.

### 3.3. Writing a day

```kotlin
val strings = PantrixTheme.strings
strings.date(day.day, day.month, day.year)       // "Oct 17, 2026", "17 Eki 2026", "١٧ أكتوبر ٢٠٢٦"
strings.longDate(day.day, day.month, day.year)   // "Saturday, October 17, 2026", "17 Ekim 2026 Cumartesi"
strings.longMonthYear(day.month, day.year)       // "October 2026", "Ekim 2026"
strings.weekday(day.dayOfWeek)                   // "Saturday", "Cumartesi"
```

An app's own pack gives these fields in its own language: `PantrixStrings(longMonthNames = …, weekdayNames = …)`. A pack without twelve months and seven days is refused. The day the week starts on is not a word but a habit of the language: it is in `PantrixLocale.firstDayOfWeek` (Monday in Turkish, Saturday in Arabic, Sunday in English and in the other languages), and a locale can give it itself too: `PantrixLocale("de", LayoutDirection.Ltr, firstDayOfWeek = 1)`.

### 3.4. Today

*   On a screen, **`rememberToday()`**: it is read again at midnight, and what reads it is drawn again. If the clock is changed by hand, or the device moves to another time zone, the new day shows when the screen is set up again, or at the next midnight.
*   Once: **`PantrixDate.today()`**.
*   **The screen's clock**: a test or a preview shows the whole screen on one day and at one time. `rememberToday()`, the time field's picker, the elastic date picker and the charts that have moments read this clock:

    ```kotlin
    val tenPastTwo = object : Clock { override fun now() = Instant.parse("2026-10-17T11:10:00Z") }
    CompositionLocalProvider(LocalPantrixClock provides PantrixClock(tenPastTwo) { 3.hours }) {
        Screen()                                     // 17 October 2026, 14:10 in Istanbul
    }
    ```
*   A moment's day: **`PantrixDate.of(instant)`** on the device's clock; **`PantrixDate.of(instant, 3.hours)`** on a clock three hours ahead of UTC.

### 3.5. A range

```kotlin
var stay by remember { mutableStateOf<PantrixDateRange?>(null) }
PantrixDatePicker(selectedRange = stay, onRangeSelect = { stay = it }, tagKey = "stay")
val nights = stay?.let { it.end.epochDays - it.start.epochDays }
```

### 3.6. Having a time chosen

```kotlin
var at by remember { mutableStateOf<PantrixTime?>(null) }
PantrixTimePicker(
    selected = at,
    onSelect = { at = it },
    minTime = PantrixTime(9, 0),
    maxTime = PantrixTime(17, 45),
    minuteStep = 15,
    is24Hour = true,
    tagKey = "at"
)
```

The picker does not keep the selection either: pressing a value gives a whole time to `onSelect`, and the other column stays as it is. `is24Hour` gives the picker's shape: `true` two columns (00–23 and the minutes, `22:00`), `false` three columns (12–11, the minutes and AM/PM, `10:00 PM`). The default is 24 hours in every language. The time chosen is a 24-hour `PantrixTime` in both cases; only its showing changes. When writing it on the screen, give the same: `strings.time(at.hour, at.minute, is24Hour = …)`.

If `minTime` is after `maxTime`, the times that can be chosen cross midnight (22:00–06:00 is a night). The bounds are read by their hours and minutes, not by their seconds.

### 3.7. A moment from a day and a time

```kotlin
val moment: Instant = day.at(time)                  // on the device's clock
val back = PantrixDate.of(moment) to PantrixTime.of(moment)
val inIstanbul = day.at(time) { 3.hours }           // on a fixed clock three hours ahead of UTC
```

A time never shown on the night the clock is put forward (02:30 on the night two turns into three) is ahead by as much as the hour skipped: 03:30. A time shown twice on the night the clock is put back is the first of the two. Both are the moment `java.time` takes with `ZonedDateTime.of`.

With `kotlinx-datetime` and `java.time`:

```kotlin
fun LocalTime.toPantrix(): PantrixTime = PantrixTime(hour, minute, second)
fun PantrixTime.toLocalTime(): LocalTime = LocalTime(hour, minute, second)
fun java.time.LocalTime.toPantrix(): PantrixTime = PantrixTime(hour, minute, second)
fun PantrixTime.toJavaLocalTime(): java.time.LocalTime = java.time.LocalTime.of(hour, minute, second)
```

### 3.8. A range that slides with the clock

```kotlin
val lastQuarterHour = PantrixTimeRange(PantrixTimePoint.Relative(-15, PantrixTimeUnit.Minutes), PantrixTimePoint.Now)
var range by remember { mutableStateOf(lastQuarterHour) }
PantrixElasticDatePicker(
    range = range,
    onRangeChange = { range = it },
    recentlyUsed = rememberRecentRanges(),
    onRefresh = { reload(range) },
    tagKey = "dashboard"
)

// While fetching the data, each time relative to now:
val window: ClosedRange<Instant>? = range.resolve()
```

The range is a value, not a moment: "the last 15 minutes" is read again at every `resolve()`. The two `Instant`s that go to a query must be read at every refresh, inside `onRefresh`; a window read once and kept does not slide.

*   The field shows a range with a name by its name ("Last 15 minutes", "Today"); pressing the name shows the two ends and opens the start's panel. A range with no name is the two ends straight away.
*   The panels open the same way on every platform: by default a popover; with `presentation = DatePickerPresentation.Sheet` a sheet; with `presentation = DatePickerPresentation.Dialog` a dialog in the middle of the window, as wide as the popover (it narrows in a narrow window). None has a title above it: the sheet's content starts under the handle, the dialog's at the very top.
*   The panels are tight, like Elastic UI's: an end's popover is wide enough for the calendar at its own width with the times beside it (372dp on the desktop and the web, 464dp on Android, 436pt on iOS; in a narrow window the days narrow), and the quick menu is 400dp. The text fields, selects, tabs and buttons in them are 8 lower than a form's (Android 40dp, iOS 36pt, desktop and web 32dp), and the times' rows are 8 lower than the platform's touch target too. Apply stands at the end of the moment's field and does not go down at any width: if the field and its text do not fit side by side, it turns into the ✓ icon alone, as in the quick menu (the screen reader still says "Apply"), and the field takes the room left. The fields and the selects still keep the platform's touch area; a touch made right beside the tabs and the buttons is taken by Compose to the nearest one.
*   In the quick menu the four controls (direction, amount, unit, Apply) are one row at every width. If the room they want is not there, Apply turns into the ✓ icon alone (the screen reader still says "Apply") and the selects keep the room of the text they show, the unit first; in a narrower room still, the direction's text is cut. On a 360dp phone it is so in English and Turkish, on a 411dp one in Turkish. The commonly and the recently used ranges are lined up with a line of text and 8dp between them (28dp; 25px in EUI); a name that runs onto two lines lengthens its own row, and the one beside it stays aligned with it.
*   An end's edit waits (the field in the success colour); it is given when Update is pressed. With `updateButton = ElasticDatePickerUpdateButton.None`, every edit is given at once.
*   Into the field in the Absolute tab, a moment can be written in the language's own way ("Oct 17, 2026 14:15", "17 Eki 2026 ÖS 2:15") or in ISO 8601.
*   The day the week starts on decides the `/w` rounding too: if `firstDayOfWeek` is not given, it is the language's day (Sunday in English, Monday in Turkish).

#### Auto refresh, the time window and the menu's parts

```kotlin
var refresh by remember { mutableStateOf(PantrixAutoRefresh(every = 30)) }
PantrixElasticDatePicker(
    range = range,
    onRangeChange = { range = it },
    onRefresh = { reload(range) },
    tagKey = "dashboard",
    autoRefresh = refresh,
    onAutoRefreshChange = { refresh = it },
    minAutoRefresh = 5.seconds,
    timeWindowButtons = ElasticTimeWindowButtons()
)
```

*   **Auto refresh** is Elastic UI's refresh interval: `PantrixAutoRefresh(every, unit, paused)`. While it is not paused, the picker counts the time itself and does Refresh at every round: `onRefresh` (the next round waits for it to finish), or, if there is none, `onRangeChange` with the same range. At the end of the field it writes briefly how often ("⟳ 30 s"); pressing it opens the quick menu. The value is the caller's: when `onAutoRefreshChange` is given, a "Refresh every" section comes up in the quick menu, and every change is given at once; nothing shorter than `minAutoRefresh` is taken.
*   **The time window buttons** (`timeWindowButtons`) stand beside Update: ‹ and › slide the range by its length, zooming out adds `zoomFactor / 2` of the length to each end (by default 0.5), and zooming in (with `zoomIn = true`) takes the same away. A range with no length widens by half a second at each end.
*   **The field's layout** (`layout`): `Full`, `QuickSelectOnly` (only the quick menu's button; the range is elsewhere on the page) or `AutoRefreshOnly` (only the auto refresh, for a page with no time data).
*   **The quick menu's parts**: `customQuickPanels` puts panels of its own after the recently used ones; `quickMenu` builds the menu in the caller's order (`QuickSelect()`, `CommonlyUsed()`, `RecentlyUsed()`, `CustomPanels()`, `AutoRefresh()`, `Panel(title) { … }`), and the menu draws the lines itself.
*   **The time zone**: `utcOffset` is the offset by which moments are read and written (if it is not given, the device's); `timeZoneName` names the zone, with its offset, in the ends' panels ("Time zone: Europe/Istanbul (UTC+03:00)"), and changes nothing.
*   **A disabled picker's text**: with `enabled = false`, `disabledText = "All time"` shows that text in the range's place.

### 3.9. Date math in an address

```kotlin
val (from, to) = range.toDateMath()                         // "now-15m", "now"
val url = "/logs?from=$from&to=$to"
val back = PantrixTimeRange(
    PantrixTimePoint.parseDateMath(from) ?: PantrixTimePoint.Now,
    PantrixTimePoint.parseDateMath(to) ?: PantrixTimePoint.Now
)
```

What is read: `now`, then one operation (`now-15m`, `now+2h`; with no number, like Kibana, `now-d`) and one rounding (`now-1d/d`, `now/w`, `/1d` like Elasticsearch); the units `y M w d h m s`, and `H` for the hour. A moment is the form ISO 8601 writes with the UTC offset or with `Z`; the fraction of a second is kept in groups of three digits (`14:15:00.250+03:00`). Everything else is `null`: two operations (`now-1d/d+8h`), a date with `||`, an ISO text with no offset or with only a day.

### 3.10. Having a day typed: the date field

A day the user knows (a birthday, the start of a contract) is typed rather than looked for in a calendar. `PantrixDateTextField` is a text field: the day is typed in digits, and the button at the end of the box opens the calendar.

```kotlin
var birthday by remember { mutableStateOf<PantrixDate?>(null) }
PantrixDateTextField(
    value = birthday,
    onValueChange = { birthday = it },
    label = stringResource(Res.string.birthday),
    maxDate = rememberToday(),
    tagKey = "birthday"
)
```

*   **Digits only.** The field takes nothing but digits, and not more than eight. The separator comes by itself as each part is completed: in Turkish `18102026` is typed and `18.10.2026` shows. The separators are not part of the text (an `OutputTransformation`), so Backspace after a separator deletes the digit before it. A pasted day is read as it is written (`readWritten`): first in the field's order, and if that fails, with the year first, in ISO 8601's order (`2026-10-18`, `2026/10/18`, `20261018`). The parts can be separated by any mark other than a letter, and the day and the month can be single digits (`1.2.2026`). A two-digit year is the year that ends in those two digits in the hundred years between 80 years before today and 19 years after (as Unicode's date formats read it): in 2026, `18.10.26` is 2026 and `18.10.50` is 1950. The day read takes the place of the one in the field. A paste that is not read so, for example `10/18/2026` in a `DD.MM.YYYY` field, or a text with a word in it, goes in by its digits; it does not guess the places of the month and the day. To tell what was pasted, foundation's list of changes (`TextFieldBuffer.changes`) is read; it is experimental in Compose 1.12. The digits of every script (`١٨`) are taken as `0`–`9`, and the field shows them in the language's digits. On a phone the number keyboard opens; the keyboard's key is "done". Since the iPhone's number pad has no such key, the numbers and punctuation keyboard opens there (`numberKeyboardOptions`, docs/guide/number-keyboard/NUMBER_KEYBOARD_GUIDE.en.md): otherwise a user who chooses a day from the calendar cannot close the keypad that opens again when the focus comes back to the field.
*   **The format.** `format: DateFieldFormat` gives the parts' order (`DateFieldOrder`) and the separator. Its default is `DateFieldFormat.of(PantrixTheme.locale)`: Turkish, German and Russian `DD.MM.YYYY`; British English, French and Arabic `DD/MM/YYYY`; American English and English with no region `MM/DD/YYYY`; Canada and an unknown language ISO 8601's `YYYY-MM-DD`. The order is a setting, not a word: the strings pack gives only the layout's letters (`dayLetters`, `monthLetters`, `yearLetters`: "DD", "MM", "YYYY").
*   **The value.** `onValueChange` is called every time the field's day changes: a whole day the calendar takes, or `null` when the field is empty, half typed, or holds digits that are no day (31 February) or a day it does not take. The field keeps what is typed itself: while `value` is `null`, a day half typed stays on the screen. Another day that comes from outside is written into the field, and `null` empties a field that holds a day. The report comes one frame after what was typed.
*   **The state.** `PantrixDateTextField(state = rememberDateTextFieldState())`: `state.date` is the field's day (writing it fills the field), `state.text` the digits typed (`1810`); the two together tell an empty field from a half-typed one. `DateTextFieldState.Saver` keeps the half text too.
*   **A form.** `PantrixDateTextField(field = form.field<PantrixDate?>(…))`: the value goes to the form's field; on leaving the field (moving to the calendar does not count as leaving), the field is checked. Digits that make no day leave the value `null`: for a required day, give `Rules.required(message)`.
*   **Its own error.** The field writes what is typed wrong under the box: an unfinished date (on leaving the field), a day not in the calendar, a day outside `minDate`–`maxDate`, a day `isDateEnabled` does not take. The app's `error` comes first; its message is the `supportingText`.
*   **The calendar.** It opens with the button, with Alt+↓, by pressing the box of a read-only (`readOnly`) field, and with Enter or Space. `presentation: DatePickerPresentation` (shared with the Elastic picker): a popover under the box, a sheet or a dialog; the same on every platform. Pressing a day takes the day and closes the calendar. Opened with the keyboard, the focus goes to the selected day, or else to today; on closing, it comes back to the field.
*   **A filled box.** `PantrixDateFilledTextField` is the same field, drawn like `PantrixFilledTextField`: the label inside the box, and the layout showing when the label goes up.

### 3.11. Having a time typed: the time field

A time the user knows (the start of a meeting, an alarm) is typed rather than scrolled in columns. `PantrixTimeTextField` is the date field's counterpart for a time: the time is typed in digits, and the button at the end of the box opens `PantrixTimePicker`.

```kotlin
var start by remember { mutableStateOf<PantrixTime?>(null) }
PantrixTimeTextField(
    value = start,
    onValueChange = { start = it },
    label = stringResource(Res.string.meeting_start),
    minuteStep = 15,
    tagKey = "start"
)
```

*   **Digits only.** At most four digits; the colon comes by itself when the hour is completed (`1430` → `14:30`). A first digit that cannot begin an hour is a single-digit hour: when 3–9 is typed in 24 hours, or 2–9 in 12 hours, a `0` is put before it (`930` → `09:30`). The rule applies only to the first digit typed into the field: when the `0` of `09:30` is deleted, `930` stays half typed, and the zero does not come again. The keyboard is the same as in the date field (`numberKeyboardOptions(ImeAction.Done)`).
*   **12 hours.** `is24Hour` is `true` by default, as in the picker. While it is `false` the hours are written 01–12, and the half of the day is a button in the box beside the digits (`strings.am`/`strings.pm`: AM/PM, ÖÖ/ÖS): pressing it, or Space while it has the focus, turns it; on the desktop and the web, A and P typed into the field set it in every language. The screen reader hears the button by the name `strings.period`, and the half at that moment as its state. The half of the day is part of what is typed: turning it changes the value, like a typed time.
*   **Pasting.** A written time is read as it is written (`readWrittenTime`): `14:30`, `14.30`, `1430`, `930`; a second after the minute is dropped (`14:30:15`); the half of the day can come before or after, in the language's word or in English's `AM`/`PM`/`A.M.`/`P.M.` (`2:30 PM`, `ÖS 2:30`). A time whose half is said is 1–12; one whose half is not said is 0–23. If a time from 1 to 12 is pasted into a 12-hour field without its half, the half in the box is kept; 13–23 is the afternoon, and 0 is midnight. Another text with letters in it (`at 9`) goes in by its digits. `13:00` typed key by key says "There is no such time" in a 12-hour field; `13:00` pasted becomes 01:00 PM.
*   **The value and the seconds.** `onValueChange` is called as in the date field; a time half typed stays on the screen while `value` is `null`. The seconds of a value given from outside are kept until its hour and minute are typed again; the seconds of a typed time are `0`.
*   **Its own error.** An unfinished time (on leaving the field: "Enter the time as HH:MM"), a time not on the clock (`25:00`, and `13:00` in 12 hours: "There is no such time"), a time outside `minTime`–`maxTime` ("Choose a time from 09:00 to 18:00", or after or before). If `minTime` is after `maxTime`, the times cross midnight (`22:00`–`06:00`), as in the picker (`TimeBounds`). `minuteStep` decides only the picker's minute column; a minute in between can be typed.
*   **The picker.** It opens like the date field (the button, Alt+↓, a press on a read-only one, Enter, Space) and takes the same `presentation`. The difference: a press writes the time at once but does not close the picker, since the hour and the minute are chosen apart; Enter on a value, Escape, a press outside, back, or pressing the button again closes the picker. While nothing is selected, the columns stand around the current time, and when the picker is opened with the keyboard the focus comes there (the picker's inner `around` parameter, `TimePickerAround`); the general `PantrixTimePicker` does not have it.
*   **The state and a form.** `rememberTimeTextFieldState()` (`time`, `text`, `clear()`; `Saver` keeps the half text and the half of the day too) and `PantrixTimeTextField(field = form.field<PantrixTime?>(…))`; for a required time, `Rules.required(message)`.
*   **A filled box.** `PantrixTimeFilledTextField` is the same field, drawn like `PantrixFilledTextField`.

## 4. Without Pantrix {#without}

The smallest pieces for a project without the library. The day arithmetic is Howard Hinnant's count (the one the standard libraries of C++ and Rust use), exact for every Gregorian day:

```kotlin
/** The number of days from 1970-01-01 to this day; before it, negative. */
fun daysFromCivil(year: Int, month: Int, day: Int): Long {
    val y = if (month <= 2) year.toLong() - 1 else year.toLong()
    val era = y.floorDiv(400)
    val yearOfEra = y - era * 400
    val dayOfYear = (153 * (month + (if (month > 2) -3 else 9)) + 2) / 5 + day - 1
    val dayOfEra = yearOfEra * 365 + yearOfEra / 4 - yearOfEra / 100 + dayOfYear
    return era * 146_097 + dayOfEra - 719_468
}

/** ISO 8601's day: 1 Monday, 7 Sunday. 1970-01-01 was a Thursday. */
fun dayOfWeek(year: Int, month: Int, day: Int): Int = (daysFromCivil(year, month, day) + 3).mod(7) + 1
```

The device clock's offset from UTC, an `actual` on each platform:

```kotlin
// commonMain
expect fun systemUtcOffset(moment: Instant): Duration

// jvmMain and androidMain
actual fun systemUtcOffset(moment: Instant): Duration =
    java.util.TimeZone.getDefault().getOffset(moment.toEpochMilliseconds()).milliseconds

// iosMain
actual fun systemUtcOffset(moment: Instant): Duration =
    NSTimeZone.localTimeZone.secondsFromGMTForDate(NSDate.dateWithTimeIntervalSince1970(moment.epochSeconds.toDouble())).seconds

// webMain (js and wasmJs): Date says how many minutes UTC is ahead, with the sign reversed
@OptIn(ExperimentalWasmJsInterop::class)
private fun minutesUtcIsAhead(milliseconds: Double): Double = js("new Date(milliseconds).getTimezoneOffset()")
actual fun systemUtcOffset(moment: Instant): Duration =
    -minutesUtcIsAhead(moment.toEpochMilliseconds().toDouble()).roundToInt().minutes
```

Today: it is the day `(Clock.System.now() + systemUtcOffset(now)).epochSeconds.floorDiv(86_400)`; the reverse arithmetic that turns this number into a year, a month and a day is `civilFromDays` in `PantrixDate.kt`.

## 5. What the helper does not do {#limits toc="5. What it does not do"}

*   **Time zone names.** There are no named time zones ("Europe/Istanbul") and none of their rules, only the device's offset at the moment or an offset function the caller gives; `timeZoneName` only shows the name given. If another zone's day or time is wanted, `kotlinx-datetime`.
*   **The device's and the language's 12- or 24-hour layout.** The picker's default is 24 hours in every language; neither Android's and iOS's "24-hour" switch nor a region's habit (12 hours in `en-US`) is read. An app that wants 12 hours gives `is24Hour = false`.
*   **Other calendars.** Only the Gregorian calendar; there are no Hijri, Hebrew or Japanese calendars.
*   **Reading a day from text by language.** `parse` reads only ISO 8601; "17.10.2026" and "Oct 17" are not read.
*   **Relative dates.** A day itself is not written as "yesterday" or "3 days ago"; a span is written with `PantrixTimeWords` ("~ 3 days ago"), and a range given a name by its name ("Yesterday").
*   **All of date math.** At most one operation and one rounding at a point; dates with `||`, the millisecond unit and ISO texts with only a day are not read (3.9).
*   **Reading now again by itself.** `PantrixElasticDatePicker` reads "now" at the moment it is drawn; its words and the ‹ › arithmetic are refreshed at the next drawing. `autoRefresh` has the range read again at intervals (`onRefresh`); it does not draw the picker itself again.

## 6. Testing {#testing}

*   **A fixed day.** `PantrixDatePicker(today = PantrixDate(2026, 10, 17))`: tests and screenshots do not depend on the device's day.
*   **A fixed clock.** `LocalPantrixClock provides PantrixClock(clock, offsetAt)` has the whole screen read by a test clock: today, the time at which the time field's picker opens, the elastic date picker's relative ends and the charts' moments (3.4). Let the test's offset be other than the device's: on a machine in a time zone, a test written with the same offset passes on a component that does not read the screen's clock too (`ChartScreenClockTest` reads with two offsets).
*   **Midnight.** The inside of `rememberToday` is tested with a `Clock` and an offset function (`TodayTest`): a fake clock is set to one second before midnight, and the test's clock (`mainClock.advanceTimeBy`) is advanced.
*   **The time zone.** In the desktop tests the JVM's default zone is changed (`SystemTimeOnTheDesktopTest`): Istanbul, New York's summer and winter time, Kolkata's half hour, the moment the clock is put forward in Berlin.
*   **The nights the clock is put forward and back.** `PantrixDate.at(time, offsetAt)` is tested with a fake offset function (`PantrixTimeTest`): a clock one hour ahead in winter and two in summer; the skipped times and the times shown twice.
*   **A sliding range.** `range.resolve(now = Instant.parse(…), offsetAt = { 3.hours })` is read with a fixed moment and a fixed clock; the test's result does not depend on the day or the time. `PantrixElasticDatePicker` reads the screen's clock: a test given a fixed clock can look at the words of the rounded ends ("~ in 10 hours") and at the moment in the absolute tab too; if `utcOffset` is given, it comes before the screen's.

## 7. Pitfalls {#pitfalls}

*   **A moment's day depends on where the clock is read.** `PantrixDate.of(instant)` uses the device's clock; if the day, in another zone, of a moment that comes from a server is wanted, the offset is given openly.
*   **The screen's clock is not the device's.** `PantrixDate.today()` and `PantrixTime.now()` read the device's clock, not `LocalPantrixClock`: when a test gives a clock, code that calls them stays on the device's day. On a screen, `rememberToday()` or `LocalPantrixClock.current.today()`.
*   **Summer-time nights.** Clocks are put forward or back after midnight; `rememberToday` reckons the next midnight with the offset at that moment, and if the day has not turned, looks again a second later.
*   **"A month later".** `plusMonths` falls on the month's last day where the month is short: 31 January + 1 month = 28 (or 29) February; going back does not give 31 January.
*   **The number of the day of the week.** Pantrix uses ISO 8601 (1 is Monday). `java.util.Calendar` counts 1 as Sunday, and JavaScript's `getDay()` 0 as Sunday: take care when converting.
*   **12 hours is a way of showing.** `PantrixTime` always counts 24 hours; `10:00 PM` is `PantrixTime(22, 0)`. Send a server `toString()` (`22:00`), not the text on the screen.
*   **A day and a time are not the same moment everywhere.** `day.at(time)` uses the device's clock; if another place's clock is wanted, the offset function is given openly.
*   **A rounded end is the unit's last millisecond.** From `now/d` to `now/d` is the whole of today: from 00:00:00.000 to 23:59:59.999, as a range query reads it with `gte` and `lte`. An end going to a query whose end is not included (`lt`) is one millisecond short.
*   **A moment in date math is written with the device's offset.** By default `toDateMath()` writes a moment on the device's clock (`+03:00`); the same moment is read the same everywhere, but the text in the address changes with the device. For a fixed text, `toDateMath { Duration.ZERO }`.
*   **A press outside the Elastic picker's popover goes to what is under it too.** `PantrixPopover`'s rule is different: the first press outside only closes it. `PantrixElasticDatePicker` behaves like Elastic UI: while one end's panel is open, pressing the other end, Update or something else on the page both closes the panel and does what was pressed; pressing the open end's button closes its panel. Pressing a link on the page while the panel is open opens the link too. With `presentation = Sheet` and with `Dialog` there is a scrim; pressing it only closes the sheet or the dialog.
*   **Arabic month names.** The pack gives the Gregorian names used in Egypt and the Gulf ("أكتوبر"); the names of the Levant ("تشرين الأول") differ.

*   **A round of auto refresh waits for `onRefresh`.** A new round is not counted until a long refresh finishes; if a round fails, the refresh stops, and this is reported to `onAutoRefreshChange` with `paused = true`. The error itself is the caller's: the picker swallows it, and recording it is the job of `onRefresh`.
*   **‹, › and zooming turn the range into moments.** "The last 15 minutes", once slid, becomes two fixed moments and no longer slides with the clock; it is so in Elastic UI too.

## 8. Verification {#verification}

### Tests

:::table
| What | Where |
| :--- | :--- |
| The validation of a day, the day of the week, the number of days from 1970 (±800,000 days), adding months and years, ordering, ISO writing and reading, a moment's day, the range | `ui/foundation/src/commonTest/.../date/PantrixDateTest.kt` (JVM, Android, iOS, JS, Wasm) |
| Waiting until midnight, the day that turns at midnight | `TodayTest` |
| The screen's clock: its day and time, the moment summer time ends, equality, its default, the clock `rememberToday` reads and a changing clock | `PantrixClockTest` |
| The components that read the screen's clock: the time field's picker, the elastic date picker's public API, the charts (cartesian, polar, radar, Gantt) | `TimeTextFieldTest`, `ElasticDatePickerTest`, `ChartScreenClockTest` |
| Month and day names, the long date, the week's first day, the refusal of wrong packs | `PantrixStringsTest` |
| The device's clock offset | `SystemTimeTest`, `SystemTimeOnTheDesktopTest` |
| The validation of a time, ordering, ISO writing and reading, a moment's time, a moment from a day and a time (summer-time nights included) | `ui/foundation/src/commonTest/.../date/PantrixTimeTest.kt` |
| 12- and 24-hour writing, the time words | `PantrixStringsTest` |
| The time picker: the columns, the bounds, the night range, the press, the keys, the digits typed, the Tab stops, the centring, the screen reader, the colours, the style | `ui/components/src/commonTest/.../TimePicker*Test.kt`, `TimeColumnsTest.kt` |
| The calendar: the month's places, the keys, the Tab stop, the range, the years, the screen reader, the colours, the style | `ui/components/src/commonTest/.../DatePicker*Test.kt`, `CalendarMonthTest.kt` |
| The date field: digits and separators, the formats, Arabic digits, a half day, its own errors, the app's error, a value from outside, the state, choosing from the calendar and the events, Alt+↓, Escape, Enter, read-only, disabled, sheet and dialog, how the layout is read out, a form, the filled box | `ui/components/src/commonTest/.../DateTextFieldTest.kt`, `DateFieldFormatTest.kt` |
| The time field: the colon, the zero before, its own errors, the bounds and a range across midnight, a value from outside, 12 hours and the button for the half of the day, A and P, every press in the picker and the events, the focus on the current time with Alt+↓, closing with Enter, read-only, pasting, the state, a form, the filled box, how the layout is read out | `ui/components/src/commonTest/.../TimeTextFieldTest.kt`, `TimeFieldTextTest.kt` |
| The charts' time axis (it uses the same day arithmetic) | `ChartTimeTest`, `ChartTimeAxisTest` |
| Sliding points: shifting, rounding, the week's start (1, 6, 7), the summer-time night, the reading of the range, writing and reading date math, the fraction of a second | `ui/foundation/src/commonTest/.../date/PantrixTimePointTest.kt` |
| The words of a span and their plurals (English, Turkish, Arabic) | `PantrixTimeWordsTest` |
| The elastic date picker: the range's name and ends, pressing the name, the quick menu, ‹ ›, a waiting edit, ends out of order, `None`, Refresh and `onRefresh`, `loading`, `enabled`, a new range, the tab turning the end, the calendar, the half-hour times and the moment typed, 12 hours, sheet, dialog, a panel with no title, the icon in a narrow room, the colours, the style, the recently used, writing and reading a moment | `ui/components/src/commonTest/.../ElasticDatePicker*Test.kt` |
:::

### Looked at by hand

*   **The web:** the calendar on the page, in a popover, in a dialog and in a sheet; range selection and its preview under the pointer, the year grid, the keyboard (Tab, the arrows, Page Down, Enter), Turkish and the dark theme.
*   **The Android emulator (API 33):** the demo's "Date" section, the range on the page, the calendar in a sheet and the year grid scrolled by finger, the writing of the day chosen; in the accessibility tree, every day by its full name ("Monday, October 12, 2026").
*   **The iOS simulator:** the "Date" section, the Turkish words, choosing in a dialog and OK.
*   **The web (the elastic date picker):** the range's name, the two ends and the start's panel on pressing the name, a waiting edit in the relative tab (the green field, the mark, the green Update), the calendar and the half-hour times in the absolute tab, Update, the quick menu and the recently used, a panel opened in a sheet, Refresh with its icon in a narrow room.

### Not verified

*   A screen reader's voice; iOS's accessibility tree (Maestro could not connect to the simulator); the calendar on a device in a right-to-left language (tried in tests); midnight turning on a real device (tried in a test with a fake clock).

## 9. Where it is in this repository {#repository toc="9. In the repository"}

*   Foundation: `ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/date/` (`PantrixDate.kt`, `PantrixTime.kt`, `Today.kt`, `PantrixClock.kt`), `platform/SystemTime.kt` and each platform's counterpart, `l10n/PantrixStrings.kt`.
*   The calendar: `ui/components/src/commonMain/kotlin/com/pantrixui/components/date/datepicker/`.
*   The date and time fields: `ui/components/src/commonMain/kotlin/com/pantrixui/components/date/common/` (`PickerField.kt` the shared core, `PickerFieldBindings.kt` the three forms, `PickerBoxes.kt` the two boxes, `FieldDigits.kt` the digit helpers; for the date `DateField.kt`, `DateFieldBindings.kt`, `DateFieldText.kt`, `DateFieldPaste.kt`, `DateFieldFormat.kt`, `DateTextFieldState.kt`; for the time `TimeField.kt`, `TimeFieldBindings.kt`, `TimeFieldText.kt`, `TimeTextFieldState.kt`; shared, `DatePickerPresentation.kt`), `date/datetextfield/`, `date/datefilledtextfield/`, `date/timetextfield/`, `date/timefilledtextfield/`.
*   The time picker: `ui/components/src/commonMain/kotlin/com/pantrixui/components/date/timepicker/`.
*   The sliding range: in foundation, `date/PantrixTimePoint.kt`, `PantrixTimeUnit.kt`, `DateMath.kt` (shifting, rounding), `DateMathText.kt` (writing, reading), `l10n/PantrixTimeWords.kt`; the component, `ui/components/src/commonMain/kotlin/com/pantrixui/components/date/elasticdatepicker/`.
*   The charts' time axis: `ui/components/.../chart/time/CivilTime.kt` takes the day arithmetic from `PantrixDate`.
*   The demo: `app/shared/src/commonMain/kotlin/com/pantrixui/cmp/demo/samples/DatePickerSamples.kt`, `TimePickerSamples.kt`, `ElasticDatePickerSamples.kt`, `DateTextFieldSamples.kt`, `DateFilledTextFieldSamples.kt`, `TimeTextFieldSamples.kt`, `TimeFilledTextFieldSamples.kt`.

## 10. What is left open {#open}

In `docs/OPEN_ITEMS.md`: the time fields (`PantrixTimeTextField`, `PantrixTimeFilledTextField`; their skeletons stand), a date field with two ends, named time zones, the device's 12- or 24-hour setting, reading dates and times from text by language; in the elastic date picker, the error of a Refresh pressed by hand, and making general the popover option that lets a press outside through.
