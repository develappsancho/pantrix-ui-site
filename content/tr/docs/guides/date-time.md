---
title: Tarih ve saat
description: Compose Multiplatform'da bir günün (yıl, ay, gün) ve günün bir saatinin ortak kodda neden kendiliğinden bulunmadığı, kütüphanenin bunun için ne verdiği, bir günün ve bir saatin bir uygulamanın kendi türüne nasıl çevrildiği, bir gün ile bir saatin nasıl bir an olduğu, dile göre nasıl yazıldıkları (12 ya da 24 saat dahil), "bugün"ün nasıl bilindiği ve saatle birlikte kayan bir aralığın ("son 15 dakika") nasıl tutulup okunduğu. Sorun Pantrix'e özgü değildir: bir tarih gösteren ya da seçtiren her Compose Multiplatform projesinde aynı şekilde görülür.
---

Burada söylenen her şey Kotlin 2.4.21'in standart kütüphanesinde ve bu depoda koşan testlerde görülmüştür; hangisinin nerede görüldüğü 8. bölümde yazılıdır.

## 1. Sorun {#problem}

:::aside date-time
### Belirti

*   Ortak kodda bir "gün" türü yoktur. Standart kütüphane bir **anı** bilir (`kotlin.time.Instant`, `Clock.System.now()`), ama bir anın hangi gün olduğu saatin nerede okunduğuna bağlıdır: İstanbul'da 17 Ekim 00:30 olan an, Londra'da hâlâ 16 Ekim'dir.
*   Bir tarih kütüphanesi seçmek uygulamanın her katmanına yayılır. `kotlinx-datetime`'ın `LocalDate`'i bir tasarım sisteminin API'sine girerse, onu kullanmayan bir uygulama da o kütüphaneyi almak zorunda kalır; `java.time` yalnızca JVM'de ve Android'dedir, `NSDate` yalnızca iOS'ta, tarayıcının `Date`'i yalnızca web'de.
*   Takvim hesabı kolay yanlış yapılır: ayın uzunluğu, artık yıllar (1900 artık değildir, 2000 artıktır), haftanın günü, "bir ay sonra" (31 Ocak'tan bir ay sonra Şubat'ın son günüdür).
*   Bir günü yazmak dile bağlıdır: ay adları, sıra ("Oct 17, 2026" ile "17 Eki 2026"), rakamlar (Arapçada ١٧), haftanın hangi gün başladığı (İngilizcede Pazar, Türkçede Pazartesi, Arapçada Cumartesi).
*   Uzun süre açık kalan bir ekranda "bugün" gece yarısı değişir; bir kez okunan gün ertesi gün yanlıştır.
:::

### Neden

Kotlin 2.4.21'in standart kütüphanesi `kotlin.time` paketinde `Instant`, `Clock` ve `Duration` verir; deneysel değillerdir, opt-in istemezler. Bir takvim günü, saat dilimleri ya da bir günü yazmak standart kütüphanede yoktur:

:::table
| Ne | Nerede bulunur |
| :--- | :--- |
| Bir an, şimdi | Standart kütüphane: `kotlin.time.Instant`, `Clock.System.now()` |
| Takvim günü, saat dilimleri | `kotlinx-datetime` (ayrı bir bağımlılık), `java.time` (JVM, Android), `NSCalendar` (iOS), `Date` (web) |
| Cihazın saatinin UTC'den farkı | Her platformun kendi API'si: `java.util.TimeZone`, `NSTimeZone`, `Date.getTimezoneOffset()` |
| Ay ve gün adları | Her platformun yerelleştirmesi; ortak kodda yok |
:::

## 2. Çözüm: dokuz parça {#fix}

Foundation'da (`com.pantrixui.foundation.date` ve `com.pantrixui.foundation.l10n`):

1.  **`PantrixDate(year, month, day)`**: saati ve saat dilimi olmayan bir gün. Kurulurken doğrulanır (13. ay, 30 Şubat reddedilir), sıralanabilir, ISO 8601 biçiminde yazılır ve okunur (`2026-10-17`, `PantrixDate.parse`). Hesapları: `dayOfWeek` (ISO: 1 Pazartesi, 7 Pazar), `epochDays` ve `ofEpochDays` (1970'ten gün sayısı), `plusDays`, `plusMonths` ve `plusYears` (ayın kısa olduğu yerde ayın son günü), `daysInMonth`, `isLeapYear`.
2.  **`PantrixDateRange(start, end)`**: iki ucu dahil bir gün aralığı; sonu başından önce olamaz. `day in range`.
3.  **Bugün ve ekranın saati**: `PantrixDate.today()` cihazın saatinin gösterdiği gün; `PantrixDate.of(moment, offset)` bir anın, UTC'den `offset` ilerideki bir saatte hangi gün olduğu. Bir ekranda **`rememberToday()`**: ekranın saatinin günü, gece yarısı kendiliğinden yenilenir. Ekranın saati **`PantrixClock(clock, utcOffset)`**'tur, bir `Clock` ve bir fark işlevi; **`LocalPantrixClock`** onu verir, varsayılanı cihazınkidir (`PantrixClock.System`). `rememberToday()`, saat alanının seçicisi, elastic date picker ve grafiklerin anları onu okur; bir test ya da önizleme kendi saatini verir (3.4, 6).
4.  **`systemUtcOffset(moment)`** (`com.pantrixui.foundation.platform`): cihazın saatinin o anda UTC'den ne kadar ileride olduğu; yaz saati dahil. Her platformun kendi API'siyle.
5.  **`PantrixStrings`'in tarih sözleri**, her dilde: kısa ve uzun ay adları (`month`, `longMonth`), haftanın günleri (`weekday`, `shortWeekday`), bir gün (`date`: "17 Eki 2026"), haftanın günüyle tam bir gün (`longDate`: "17 Ekim 2026 Cumartesi"), bir ay (`monthYear`, `longMonthYear`: "Ekim 2026").
6.  **`PantrixTime(hour, minute, second = 0)`**: günü ve saat dilimi olmayan, 24 saatlik bir saat. Kurulurken doğrulanır, sıralanabilir, ISO 8601 biçiminde yazılır ve okunur (`14:05`, `14:05:30`, `PantrixTime.parse`); `secondOfDay` ve `ofSecondOfDay`, `PantrixTime.of(moment, offset)`, `PantrixTime.now()`. Bir gün ile bir saat **`PantrixDate.at(time)`** ile cihazın saatinde bir an (`Instant`) olur; saatin ileri ve geri alındığı geceler dahil (3.7).
7.  **`PantrixStrings`'in saat sözleri**: `time(hour, minute, is24Hour = …)` bir saati çağıranın istediği düzenle yazar: `true` 24 saat (`22:00`, `clock` gibi), `false` 12 saat ve dilin sözleriyle günün yarısı ("10:00 PM", "ÖS 10:00", "٢:٠٥ م"). Hangi düzenin kullanılacağını paket bilmez, çağıran söyler. `hour`, `minute`, `period`, `am`, `pm` ve `twelveHourTemplate` bir saat seçicinin sözleridir.

8.  **`PantrixTimePoint` ve `PantrixTimeRange`**: saatle birlikte kayabilen bir zaman noktası ve iki ucu. Bir nokta şimdidir (`Now`), şimdiden bir süredir (`Relative(-15, Minutes)`, bir birime yuvarlanabilir: `Relative(-1, Days, roundTo = Days)` dün) ya da bir andır (`Absolute(instant)`). `resolve(now)` noktaları o anki iki `Instant`'a çevirir: başlangıç aşağı, son yukarı yuvarlanır; sonu başından önce düşen bir aralık `null`'dır. Birimler `PantrixTimeUnit`'tir: saniyeden saate geçen zaman, günden yıla takvimin (yaz saati gecesi dahil). Uçlar Elasticsearch ve Kibana'nın **tarih matematiğiyle** yazılır ve okunur: `toDateMath()` (`now-15m`, `now/d`, `2026-10-17T14:15:00+03:00`), `PantrixTimePoint.parseDateMath`.
9.  **`PantrixTimeWords`** (`PantrixStrings.timeWords`): bir sürenin sözleri, her dilin çoğul kurallarıyla: "~ 15 dakika önce", "Son 15 dakika", "Önümüzdeki 2 saat", ", güne yuvarlanmış". İngilizce (bir/çok), Türkçe (tek biçim) ve Arapça (CLDR'ın altı biçimi, Arap rakamları).

Bileşenlerde bunların üstünde **`PantrixDatePicker`** (`date.datepicker`) durur, bir gün ya da bir aralık seçtiren takvim; **`PantrixTimePicker`** (`date.timepicker`), sütunlardan bir saat seçtiren seçici; **`PantrixElasticDatePicker`** (`date.elasticdatepicker`), Elastic UI'ın `EuiSuperDatePicker`'ı gibi saatle kayan bir aralık seçtiren alan: hızlı menü, aralığın adı ya da iki ucu, Yenile ve Güncelle; **`PantrixDateTextField`** ile **`PantrixDateFilledTextField`** (`date.datetextfield`, `date.datefilledtextfield`), günü rakamlarla yazdıran ya da düğmesinin açtığı takvimden seçtiren metin alanları (3.10); ve **`PantrixTimeTextField`** ile **`PantrixTimeFilledTextField`** (`date.timetextfield`, `date.timefilledtextfield`), saati aynı biçimde yazdıran ya da saat seçiciden seçtiren alanlar (3.11). Dört alan tek bir iç çekirdeği paylaşır (`date/common/PickerField.kt`); her biri yalnız kendi rakam kurallarını, hatalarını ve seçicisini verir.

Kütüphaneye bir tarih bağımlılığı eklenmedi. `PantrixDate` ve `PantrixTime` üçer sayıdır; bir uygulama kendi türüne bir satırla çevirir (3.2, 3.7).

## 3. Pantrix ile {#with}

### 3.1. Bir gün seçtirmek

```kotlin
var due by remember { mutableStateOf<PantrixDate?>(null) }
PantrixDatePicker(
    selected = due,
    onSelect = { due = it },
    minDate = rememberToday(),
    tagKey = "due"
)
```

Takvim seçimi tutmaz: `selected` uygulamanındır, basılan gün `onSelect`'e gelir. Bir popover'da, bir dialog'da ya da bir sheet'te açmak uygulamanın işidir; demo dördünü de gösterir (`DatePickerSamples.kt`).

Takvim kendi genişliğinde durur (yedi günün yeri; telefonda platformun dokunma hedefi kadar), bu yüzden bir popover'ı ve bir dialog'u gereksiz genişletmez. Bir sheet'te ya da bir sayfanın genişliğinde `Modifier.fillMaxWidth()` verin: takvim o genişliği doldurur, sütunları `DatePickerStyle.maxCellWidth`'e (hücrenin bir buçuk katı) kadar genişler, satırları ve günlerin daireleri boyunu korur; daha geniş bir yerde ortada durur.

### 3.2. Kendi tarih türüne çevirmek

`kotlinx-datetime` (0.7 ve sonrası; daha eski sürümlerde `month.number` yerine `monthNumber`, `day` yerine `dayOfMonth`):

```kotlin
fun LocalDate.toPantrix(): PantrixDate = PantrixDate(year, month.number, day)
fun PantrixDate.toLocalDate(): LocalDate = LocalDate(year, month, day)
```

`java.time` (JVM, Android):

```kotlin
fun java.time.LocalDate.toPantrix(): PantrixDate = PantrixDate(year, monthValue, dayOfMonth)
fun PantrixDate.toJavaLocalDate(): java.time.LocalDate = java.time.LocalDate.of(year, month, day)
```

Bir sunucuyla ya da bir JSON'la ISO 8601 metniyle: `PantrixDate.parse("2026-10-17")` ve `date.toString()`.

Bir sayıyla (veritabanı, tercihler): `date.epochDays` ve `PantrixDate.ofEpochDays(days)`.

### 3.3. Bir günü yazmak

```kotlin
val strings = PantrixTheme.strings
strings.date(day.day, day.month, day.year)       // "Oct 17, 2026", "17 Eki 2026", "١٧ أكتوبر ٢٠٢٦"
strings.longDate(day.day, day.month, day.year)   // "Saturday, October 17, 2026", "17 Ekim 2026 Cumartesi"
strings.longMonthYear(day.month, day.year)       // "October 2026", "Ekim 2026"
strings.weekday(day.dayOfWeek)                   // "Saturday", "Cumartesi"
```

Bir uygulamanın kendi paketi bu alanları kendi diliyle verir: `PantrixStrings(longMonthNames = …, weekdayNames = …)`. On iki ay ve yedi gün olmayan bir paket reddedilir. Haftanın başladığı gün bir sözcük değil, dilin bir alışkanlığıdır: `PantrixLocale.firstDayOfWeek`'tedir (Türkçede Pazartesi, Arapçada Cumartesi, İngilizcede ve öteki dillerde Pazar) ve bir yerel ayar onu kendi de verebilir: `PantrixLocale("de", LayoutDirection.Ltr, firstDayOfWeek = 1)`.

### 3.4. Bugün

*   Bir ekranda **`rememberToday()`**: gece yarısı yeniden okunur ve onu okuyan yeniden çizilir. Saat elle değiştirilirse ya da cihaz başka bir saat dilimine geçerse, yeni gün ekran yeniden kurulunca ya da bir sonraki gece yarısında görünür.
*   Bir kez: **`PantrixDate.today()`**.
*   **Ekranın saati**: bir test ya da önizleme bütün ekranı bir günde ve bir saatte gösterir. `rememberToday()`, saat alanının seçicisi, elastic date picker ve anları yer olan grafikler bu saati okur:

    ```kotlin
    val tenPastTwo = object : Clock { override fun now() = Instant.parse("2026-10-17T11:10:00Z") }
    CompositionLocalProvider(LocalPantrixClock provides PantrixClock(tenPastTwo) { 3.hours }) {
        Screen()                                     // 17 Ekim 2026, İstanbul'da 14:10
    }
    ```
*   Bir anın günü: **`PantrixDate.of(instant)`** cihazın saatinde; **`PantrixDate.of(instant, 3.hours)`** UTC'den üç saat ileride bir saatte.

### 3.5. Bir aralık

```kotlin
var stay by remember { mutableStateOf<PantrixDateRange?>(null) }
PantrixDatePicker(selectedRange = stay, onRangeSelect = { stay = it }, tagKey = "stay")
val nights = stay?.let { it.end.epochDays - it.start.epochDays }
```

### 3.6. Bir saat seçtirmek

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

Seçici de seçimi tutmaz: bir değere basmak bütün bir saati `onSelect`'e verir, öteki sütun olduğu gibi kalır. `is24Hour` seçicinin şeklini verir: `true` iki sütun (00–23 ve dakikalar, `22:00`), `false` üç sütun (12–11, dakikalar ve AM/PM, `10:00 PM`). Varsayılan her dilde 24 saattir. Seçilen saat her iki durumda da 24 saatlik bir `PantrixTime`'dır; yalnızca gösterimi değişir. Ekranda yazarken aynısını verin: `strings.time(at.hour, at.minute, is24Hour = …)`.

`minTime` `maxTime`'dan sonraysa seçilebilen saatler gece yarısını geçer (22:00–06:00 bir gece). Sınırlar saat ve dakikalarıyla okunur, saniyeleri değil.

### 3.7. Gün ve saatten bir an

```kotlin
val moment: Instant = day.at(time)                  // cihazın saatinde
val back = PantrixDate.of(moment) to PantrixTime.of(moment)
val inIstanbul = day.at(time) { 3.hours }           // UTC'den üç saat ileride sabit bir saatte
```

Saatin ileri alındığı gecede hiç gösterilmeyen bir saat (ikiden üçe geçilen gecede 02:30), atlanan saat kadar ilerisidir: 03:30. Saatin geri alındığı gecede iki kez gösterilen bir saat ikisinden ilkidir. İkisi de `java.time`'ın `ZonedDateTime.of` ile aldığı andır.

`kotlinx-datetime` ve `java.time` ile:

```kotlin
fun LocalTime.toPantrix(): PantrixTime = PantrixTime(hour, minute, second)
fun PantrixTime.toLocalTime(): LocalTime = LocalTime(hour, minute, second)
fun java.time.LocalTime.toPantrix(): PantrixTime = PantrixTime(hour, minute, second)
fun PantrixTime.toJavaLocalTime(): java.time.LocalTime = java.time.LocalTime.of(hour, minute, second)
```

### 3.8. Saatle kayan bir aralık

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

// Veriyi çekerken, her seferinde şimdiye göre:
val window: ClosedRange<Instant>? = range.resolve()
```

Aralık bir değerdir, bir an değil: "son 15 dakika" her `resolve()`'da yeniden okunur. Bir sorguya giden iki `Instant` her yenilemede, `onRefresh` içinde okunmalıdır; bir kez okunup saklanan pencere kaymaz.

*   Alan, adı olan bir aralığı adıyla gösterir ("Son 15 dakika", "Bugün"); ada basınca iki uç görünür ve başlangıcın paneli açılır. Adı olmayan bir aralık doğrudan iki uçtur.
*   Paneller her platformda aynı biçimde açılır: varsayılan popover, `presentation = DatePickerPresentation.Sheet` ile sheet, `presentation = DatePickerPresentation.Dialog` ile pencerenin ortasında, popover kadar geniş bir dialog (dar bir pencerede daralır). Hiçbirinin üstünde başlık yoktur: sheet'in içeriği tutamağın altından, dialog'unki en üstten başlar.
*   Paneller Elastic UI'ınkiler gibi sıkıdır: bir ucun popover'ı takvimi kendi genişliğinde ve saatler yanında olacak kadar geniştir (masaüstünde ve web'de 372dp, Android'de 464dp, iOS'ta 436pt; dar bir pencerede günler daralır), hızlı menü 400dp'dir. İçlerindeki metin alanları, select'ler, sekmeler ve düğmeler bir formunkinden 8 alçaktır (Android 40dp, iOS 36pt, masaüstü ve web 32dp), saatlerin satırı da platformun dokunma hedefinden 8 alçaktır. Uygula, anın alanının sonunda durur ve hiçbir genişlikte alta inmez: alanla yazısı yan yana sığmazsa hızlı menüdeki gibi yalnız ✓ ikonuna döner (ekran okuyucu yine "Uygula" der), alan kalan yeri alır. Alanlar ve select'ler platformun dokunma alanını yine de ayırır; sekmelerin ve düğmelerin hemen yanına yapılan bir dokunuşu Compose en yakınına götürür.
*   Hızlı menüde dört kontrol (yön, miktar, birim, Uygula) her genişlikte tek satırdır. İstedikleri yer yoksa Uygula yalnız ✓ ikonuna döner (ekran okuyucu yine "Uygula" der) ve seçiciler gösterdikleri yazının yerini korur, önce birim; daha da dar bir yerde yönün yazısı kesilir. 360dp'lik bir telefonda İngilizcede ve Türkçede, 411dp'lik birinde Türkçede böyledir. Sık ve son kullanılan aralıklar bir yazı satırı ve 8dp aralıkla dizilir (28dp, EUI'de 25px); iki satıra kayan bir ad kendi satırını uzatır, yanındaki onunla hizalı kalır.
*   Bir ucun düzenlemesi bekler (alan başarı renginde); Güncelle'ye basılınca verilir. `updateButton = ElasticDatePickerUpdateButton.None` ile her düzenleme hemen verilir.
*   Mutlak sekmesindeki alana bir an dilin kendi yazılışıyla ("Oct 17, 2026 14:15", "17 Eki 2026 ÖS 2:15") ya da ISO 8601 ile yazılabilir.
*   Haftanın başladığı gün `/w` yuvarlamasını da belirler: `firstDayOfWeek` verilmezse dilin günüdür (İngilizcede Pazar, Türkçede Pazartesi).

#### Otomatik yenileme, zaman penceresi ve menünün parçaları

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

*   **Otomatik yenileme** Elastic UI'ın yenileme aralığıdır: `PantrixAutoRefresh(every, unit, paused)`. Duraklatılmamışken seçici zamanı kendisi sayar ve her turda Yenile'yi yapar: `onRefresh` (bir sonraki tur onun bitmesini bekler), o yoksa aynı aralıkla `onRangeChange`. Alanın sonunda ne sıklıkla olduğu kısaca yazar ("⟳ 30 sn"); ona basmak hızlı menüyü açar. Değer çağıranındır: `onAutoRefreshChange` verilince hızlı menüde "Yenileme aralığı" bölümü çıkar, her değişiklik hemen verilir; `minAutoRefresh`'ten kısası alınmaz.
*   **Zaman penceresi düğmeleri** (`timeWindowButtons`) Güncelle'nin yanında durur: ‹ ve › aralığı uzunluğu kadar kaydırır, uzaklaştırma her uca uzunluğun `zoomFactor / 2`'sini ekler (varsayılan 0.5), yakınlaştırma (`zoomIn = true` ile) aynı kadar çıkarır. Uzunluğu olmayan bir aralık her uçta yarım saniye genişler.
*   **Alanın düzeni** (`layout`): `Full`, `QuickSelectOnly` (yalnız hızlı menünün düğmesi; aralık sayfanın başka bir yerinde) ya da `AutoRefreshOnly` (yalnız otomatik yenileme, zaman verisi olmayan bir sayfa için).
*   **Hızlı menünün parçaları**: `customQuickPanels` kendi panellerini son kullanılanlardan sonra koyar; `quickMenu` menüyü çağıranın sırasıyla kurar (`QuickSelect()`, `CommonlyUsed()`, `RecentlyUsed()`, `CustomPanels()`, `AutoRefresh()`, `Panel(title) { … }`), çizgileri menü kendisi çeker.
*   **Saat dilimi**: `utcOffset` anların okunup yazıldığı farktır (verilmezse cihazınki); `timeZoneName` uçların panellerinde dilimi adıyla ve farkıyla söyler ("Saat dilimi: Europe/Istanbul (UTC+03:00)"), bir şey değiştirmez.
*   **Kapalı bir seçicinin yazısı**: `enabled = false` ile `disabledText = "Tüm zamanlar"` aralığın yerine o yazıyı gösterir.

### 3.9. Bir adreste tarih matematiği

```kotlin
val (from, to) = range.toDateMath()                         // "now-15m", "now"
val url = "/logs?from=$from&to=$to"
val back = PantrixTimeRange(
    PantrixTimePoint.parseDateMath(from) ?: PantrixTimePoint.Now,
    PantrixTimePoint.parseDateMath(to) ?: PantrixTimePoint.Now
)
```

Okunanlar: `now`, ardından bir işlem (`now-15m`, `now+2h`; Kibana gibi sayısız `now-d`) ve bir yuvarlama (`now-1d/d`, `now/w`, Elasticsearch gibi `/1d`); birimler `y M w d h m s` ve saat için `H`. Bir an ISO 8601'in UTC farkıyla ya da `Z` ile yazdığı biçimdir; saniyenin kesri üçerli basamaklarla korunur (`14:15:00.250+03:00`). Başka her şey `null`'dır: iki işlem (`now-1d/d+8h`), `||`'li bir tarih, farksız ya da yalnız gün olan bir ISO metni.

### 3.10. Bir günü yazdırmak: tarih alanı

Kullanıcının bildiği bir gün (doğum günü, bir sözleşmenin başlangıcı) takvimde aranmaktan çok yazılır. `PantrixDateTextField` bir metin alanıdır: gün rakamlarla yazılır, kutunun sonundaki düğme takvimi açar.

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

*   **Yalnız rakam.** Alan rakamdan başka bir şey almaz, sekizden fazlasını da. Her parça tamamlanınca ayraç kendiliğinden gelir: Türkçede `18102026` yazılır, `18.10.2026` görünür. Ayraçlar metnin parçası değildir (bir `OutputTransformation`), bu yüzden ayraçtan sonra Backspace önündeki rakamı siler. Yapıştırılan bir gün yazıldığı gibi okunur (`readWritten`): önce alanın sırasıyla, olmazsa yılı başta, ISO 8601'in sırasıyla (`2026-10-18`, `2026/10/18`, `20261018`). Parçaları harf dışında herhangi bir işaret ayırabilir, gün ve ay tek haneli olabilir (`1.2.2026`). İki haneli bir yıl, bugünden 80 yıl önceyle 19 yıl sonrası arasındaki yüz yılın o iki haneyle biten yılıdır (Unicode'un tarih biçimlerinin okuduğu gibi): 2026'da `18.10.26` 2026, `18.10.50` 1950. Okunan gün alandakinin yerini alır. Böyle okunmayan bir yapıştırma, örneğin `GG.AA.YYYY` alanında `10/18/2026` ya da içinde sözcük olan bir metin, rakamlarıyla girer; ay ile günün yerini tahmin etmez. Yapıştırılanı ayırmak için Foundation'ın değişiklik listesi (`TextFieldBuffer.changes`) okunur; Compose 1.12'de deneyseldir. Her yazının rakamları (`١٨`) `0`–`9` olarak alınır, alan dilin rakamlarıyla gösterir. Telefonda rakam klavyesi açılır; klavyenin tuşu "bitti"dir. iPhone'un rakam tuş takımında böyle bir tuş olmadığı için orada rakam ve noktalama klavyesi açılır (`numberKeyboardOptions`, docs/guide/number-keyboard/NUMBER_KEYBOARD_GUIDE.md): yoksa takvimden gün seçen kullanıcı, odak alana dönünce yeniden açılan tuş takımını kapatamaz.
*   **Biçim.** `format: DateFieldFormat` parçaların sırasını (`DateFieldOrder`) ve ayracı verir. Varsayılanı `DateFieldFormat.of(PantrixTheme.locale)`: Türkçe, Almanca, Rusça `GG.AA.YYYY`; İngiltere İngilizcesi, Fransızca, Arapça `DD/MM/YYYY`; ABD ve bölgesiz İngilizce `MM/DD/YYYY`; Kanada ve bilinmeyen bir dil ISO 8601 `YYYY-MM-DD`. Sıra bir ayardır, söz değildir: strings paketi yalnız düzenin harflerini verir (`dayLetters`, `monthLetters`, `yearLetters`: "GG", "AA", "YYYY").
*   **Değer.** `onValueChange` alanın günü her değiştiğinde çağrılır: takvimin aldığı tam bir gün, ya da alan boş, yarım, gün olmayan rakamlar (31 Şubat) veya almadığı bir gün tuttuğunda `null`. Alan yazılanı kendisi tutar: `value` `null` iken yarım yazılmış bir gün ekranda kalır. Dışarıdan gelen başka bir gün alana yazılır, `null` gün tutan bir alanı boşaltır. Bildirim yazılandan bir kare sonra gelir.
*   **Durum.** `PantrixDateTextField(state = rememberDateTextFieldState())`: `state.date` alanın günü (yazmak alanı doldurur), `state.text` yazılan rakamlar (`1810`); ikisi birlikte boş alanı yarım yazılmış alandan ayırır. `DateTextFieldState.Saver` yarım metni de saklar.
*   **Form.** `PantrixDateTextField(field = form.field<PantrixDate?>(…))`: değer formun alanına gider; alandan çıkınca (takvime geçmek çıkmak sayılmaz) alan denetlenir. Gün yapmayan rakamlar değeri `null` bırakır: zorunlu bir gün için `Rules.required(message)` verin.
*   **Kendi hatası.** Alan yanlış yazılanı kutunun altına yazar: tamamlanmamış bir tarih (alandan çıkınca), takvimde olmayan bir gün, `minDate`–`maxDate` dışında bir gün, `isDateEnabled`'ın almadığı bir gün. Uygulamanın `error`'u önce gelir, mesajı `supportingText`'idir.
*   **Takvim.** Düğmeyle, Alt+↓ ile, salt okunur (`readOnly`) alanda kutuya basınca, Enter ya da Boşluk ile açılır. `presentation: DatePickerPresentation` (Elastic seçiciyle ortak): kutunun altında bir popover, bir sheet ya da bir dialog; her platformda aynı. Bir güne basmak günü alır ve takvimi kapatır. Klavyeyle açılınca odak seçili güne, yoksa bugüne gider; kapanınca alana döner.
*   **Dolu kutu.** `PantrixDateFilledTextField` aynı alandır, `PantrixFilledTextField` gibi çizilir: etiket kutunun içinde, düzen etiket yukarı çıkınca görünür.

### 3.11. Bir saati yazdırmak: saat alanı

Kullanıcının bildiği bir saat (bir toplantının başlangıcı, bir alarm) sütunlarda kaydırılmaktan çok yazılır. `PantrixTimeTextField` tarih alanının saat için olanıdır: saat rakamlarla yazılır, kutunun sonundaki düğme `PantrixTimePicker`'ı açar.

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

*   **Yalnız rakam.** En çok dört rakam; saat tamamlanınca iki nokta kendiliğinden gelir (`1430` → `14:30`). Saatin başı olamayan bir ilk rakam tek haneli bir saattir: 24 saatte 3–9, 12 saatte 2–9 yazılınca önüne `0` konur (`930` → `09:30`). Kural yalnız alana yazılan ilk rakama uygulanır: `09:30`'un `0`'ı silinince `930` yarım kalır, sıfır yeniden gelmez. Klavye tarih alanındakiyle aynıdır (`numberKeyboardOptions(ImeAction.Done)`).
*   **12 saat.** `is24Hour` seçicideki gibi varsayılan `true`. `false` iken saatler 01–12 yazılır, günün yarısı kutuda rakamların yanında bir düğmedir (`strings.am`/`strings.pm`: AM/PM, ÖÖ/ÖS): basmak ya da odaktayken Boşluk çevirir; masaüstünde ve web'de alana yazılan A ve P her dilde onu belirler. Ekran okuyucu düğmeyi `strings.period` adıyla, o anki yarıyı durumu olarak duyar. Günün yarısı yazılanın parçasıdır: çevirmek, yazılmış bir saat gibi değeri değiştirir.
*   **Yapıştırma.** Yazılmış bir saat yazıldığı gibi okunur (`readWrittenTime`): `14:30`, `14.30`, `1430`, `930`; dakikadan sonraki saniye bırakılır (`14:30:15`); günün yarısı dilin sözüyle ya da İngilizcenin `AM`/`PM`/`A.M.`/`P.M.`'siyle önde ya da arkada olabilir (`2:30 PM`, `ÖS 2:30`). Yarısı söylenen saat 1–12'dir; söylenmeyen 0–23. 12 saatlik bir alana yarısı söylenmeden 1–12 arası bir saat yapıştırılırsa kutudaki yarı korunur; 13–23 öğleden sonradır, 0 gece yarısıdır. Harf içeren başka bir metin (`saat 9`) rakamlarıyla girer. Tuş tuş yazılan `13:00` 12 saatlik alanda "Böyle bir saat yok" der; yapıştırılan `13:00` ÖS 01:00 olur.
*   **Değer ve saniye.** `onValueChange` tarih alanındaki gibi çağrılır; yarım yazılan saat `value` `null` iken ekranda kalır. Dışarıdan verilen bir değerin saniyesi, saati ve dakikası yeniden yazılana dek korunur; yazılan bir saatin saniyesi `0`'dır.
*   **Kendi hatası.** Tamamlanmamış bir saat (alandan çıkınca: "Saat SS:DD biçiminde olmalı"), saatte olmayan bir saat (`25:00`, 12 saatte `13:00`, "Böyle bir saat yok"), `minTime`–`maxTime` dışında bir saat ("09:00 ile 18:00 arasında bir saat seçin", ya da sonrası/öncesi). `minTime` `maxTime`'dan sonraysa saatler gece yarısını geçer (`22:00`–`06:00`), seçicide olduğu gibi (`TimeBounds`). `minuteStep` yalnız seçicinin dakika sütununu belirler; aradaki bir dakika yazılabilir.
*   **Seçici.** Tarih alanı gibi açılır (düğme, Alt+↓, salt okunurda basış, Enter, Boşluk) ve aynı `presentation`'ı alır. Farkı: bir basış saati hemen yazar ama seçiciyi kapatmaz, saat ve dakika ayrı seçildiği için; seçiciyi bir değerde Enter, Escape, dışarı basmak, geri ya da düğmeye yeniden basmak kapatır. Hiçbir şey seçili değilken sütunlar şimdiki saatin çevresinde durur ve klavyeyle açılınca odak oraya gelir (seçicinin iç `around` parametresi, `TimePickerAround`); genel `PantrixTimePicker`'da yoktur.
*   **Durum ve form.** `rememberTimeTextFieldState()` (`time`, `text`, `clear()`, `Saver` yarım metni ve günün yarısını da saklar) ve `PantrixTimeTextField(field = form.field<PantrixTime?>(…))`, zorunlu bir saat için `Rules.required(message)`.
*   **Dolu kutu.** `PantrixTimeFilledTextField` aynı alandır, `PantrixFilledTextField` gibi çizilir.

## 4. Pantrix olmadan {#without}

Kütüphanesiz bir proje için en küçük parçalar. Gün hesabı Howard Hinnant'ın sayımıdır (C++ ve Rust'ın standart kütüphanelerinin kullandığı), her miladi gün için kesindir:

```kotlin
/** 1970-01-01'den bu güne kadar geçen gün sayısı; öncesi eksi. */
fun daysFromCivil(year: Int, month: Int, day: Int): Long {
    val y = if (month <= 2) year.toLong() - 1 else year.toLong()
    val era = y.floorDiv(400)
    val yearOfEra = y - era * 400
    val dayOfYear = (153 * (month + (if (month > 2) -3 else 9)) + 2) / 5 + day - 1
    val dayOfEra = yearOfEra * 365 + yearOfEra / 4 - yearOfEra / 100 + dayOfYear
    return era * 146_097 + dayOfEra - 719_468
}

/** ISO 8601'in günü: 1 Pazartesi, 7 Pazar. 1970-01-01 bir Perşembe'ydi. */
fun dayOfWeek(year: Int, month: Int, day: Int): Int = (daysFromCivil(year, month, day) + 3).mod(7) + 1
```

Cihazın saatinin UTC'den farkı, her platformda bir `actual`:

```kotlin
// commonMain
expect fun systemUtcOffset(moment: Instant): Duration

// jvmMain ve androidMain
actual fun systemUtcOffset(moment: Instant): Duration =
    java.util.TimeZone.getDefault().getOffset(moment.toEpochMilliseconds()).milliseconds

// iosMain
actual fun systemUtcOffset(moment: Instant): Duration =
    NSTimeZone.localTimeZone.secondsFromGMTForDate(NSDate.dateWithTimeIntervalSince1970(moment.epochSeconds.toDouble())).seconds

// webMain (js ve wasmJs): Date UTC'nin kaç dakika ileride olduğunu söyler, işareti ters
@OptIn(ExperimentalWasmJsInterop::class)
private fun minutesUtcIsAhead(milliseconds: Double): Double = js("new Date(milliseconds).getTimezoneOffset()")
actual fun systemUtcOffset(moment: Instant): Duration =
    -minutesUtcIsAhead(moment.toEpochMilliseconds().toDouble()).roundToInt().minutes
```

Bugün: `(Clock.System.now() + systemUtcOffset(now)).epochSeconds.floorDiv(86_400)` günüdür; bu sayıyı yıla, aya, güne çeviren ters hesap `PantrixDate.kt`'deki `civilFromDays`'tir.

## 5. Yardımcının yapmadıkları {#limits toc="5. Yapmadıkları"}

*   **Saat dilimi adı.** Adlı saat dilimleri ("Europe/Istanbul") ve onların kuralları yoktur, yalnızca cihazın o anki farkı ya da çağıranın verdiği bir fark işlevi vardır; `timeZoneName` yalnızca verilen adı gösterir. Başka bir dilimin günü ya da saati isteniyorsa `kotlinx-datetime`.
*   **Cihazın ve dilin 12/24 saat düzeni.** Seçicinin varsayılanı her dilde 24 saattir; Android'in ve iOS'un "24 saat" anahtarı da, bir bölgenin alışkanlığı (`en-US` 12 saat) da okunmaz. 12 saati isteyen uygulama `is24Hour = false` verir.
*   **Başka takvimler.** Yalnızca miladi takvim; hicri, İbrani, Japon takvimleri yok.
*   **Bir günü metinden dile göre okumak.** `parse` yalnızca ISO 8601'i okur; "17.10.2026" ya da "Oct 17" okunmaz.
*   **Göreli tarih.** Bir günün kendisi "dün" ya da "3 gün önce" diye yazılmaz; bir süre `PantrixTimeWords` ile yazılır ("~ 3 gün önce"), bir ad verilmiş aralık adıyla ("Dün").
*   **Tarih matematiğinin tamamı.** Bir noktada en çok bir işlem ve bir yuvarlama; `||`'li tarihler, milisaniye birimi ve yalnız gün olan ISO metinleri okunmaz (3.9).
*   **Şimdiyi kendiliğinden yeniden okumak.** `PantrixElasticDatePicker` "şimdi"yi çizildiği anda okur; sözleri ve ‹ › hesabı bir sonraki çizimde tazelenir. `autoRefresh` aralığı belli aralıklarla yeniden okutur (`onRefresh`), seçicinin kendisini yeniden çizmez.

## 6. Test etmek {#testing}

*   **Sabit bir gün.** `PantrixDatePicker(today = PantrixDate(2026, 10, 17))`: testler ve ekran görüntüleri cihazın gününe bağlı kalmaz.
*   **Sabit bir saat.** `LocalPantrixClock provides PantrixClock(clock, offsetAt)` bütün ekranı bir test saatiyle okutur: bugün, saat alanının seçicisinin açıldığı saat, elastic date picker'ın göreli uçları ve grafiklerin anları (3.4). Testin farkı cihazınkinden başka olsun: bir saat dilimindeki bir makinede aynı farkla yazılmış bir test, ekranın saatini okumayan bir bileşende de geçer (`ChartScreenClockTest` iki farkla okur).
*   **Gece yarısı.** `rememberToday`'in içi bir `Clock` ve bir fark işleviyle sınanır (`TodayTest`): sahte bir saat gece yarısının bir saniye öncesine kurulur, testin saati (`mainClock.advanceTimeBy`) ilerletilir.
*   **Saat dilimi.** Masaüstü testlerinde JVM'in varsayılan dilimi değiştirilir (`SystemTimeOnTheDesktopTest`): İstanbul, New York'un yaz ve kış saati, Kalküta'nın yarım saati, Berlin'de saatin ileri alındığı an.
*   **Saatin ileri ve geri alındığı geceler.** `PantrixDate.at(time, offsetAt)` sahte bir fark işleviyle sınanır (`PantrixTimeTest`): kışın bir, yazın iki saat ileride bir saat; atlanan ve iki kez gösterilen saatler.
*   **Kayan bir aralık.** `range.resolve(now = Instant.parse(…), offsetAt = { 3.hours })` sabit bir an ve sabit bir saatle okunur; testin sonucu gün ve saatten bağımsızdır. `PantrixElasticDatePicker` ekranın saatini okur: sabit bir saat verilen bir test yuvarlanmış uçların sözlerine ("~ 10 saat sonra") ve mutlak sekmedeki ana da bakabilir; `utcOffset` verilirse ekranınkinin önüne geçer.

## 7. Tuzaklar {#pitfalls}

*   **Bir anın günü, saatin okunduğu yere bağlıdır.** `PantrixDate.of(instant)` cihazın saatini kullanır; sunucudan gelen bir anın başka bir dilimdeki günü isteniyorsa fark açıkça verilir.
*   **Ekranın saati cihazınki değildir.** `PantrixDate.today()` ve `PantrixTime.now()` cihazın saatini okur, `LocalPantrixClock`'u okumaz: bir test saati verdiğinde bunları çağıran kod cihazın gününde kalır. Bir ekranda `rememberToday()` ya da `LocalPantrixClock.current.today()`.
*   **Yaz saati geceleri.** Saatler gece yarısından sonra ileri ya da geri alınır; `rememberToday` bir sonraki gece yarısını o anki farkla hesaplar ve gün dönmediyse bir saniye sonra yeniden bakar.
*   **"Bir ay sonra".** `plusMonths` ayın kısa olduğu yerde ayın son gününe düşer: 31 Ocak + 1 ay = 28 (ya da 29) Şubat; geri dönmek 31 Ocak'ı vermez.
*   **Haftanın günü numarası.** Pantrix ISO 8601'i kullanır (1 Pazartesi). `java.util.Calendar` 1'i Pazar sayar, JavaScript'in `getDay()`'i 0'ı Pazar: çevirirken dikkat.
*   **12 saat bir gösterimdir.** `PantrixTime` her zaman 24 saat sayar; `10:00 PM` `PantrixTime(22, 0)`'dır. Bir sunucuya `toString()` (`22:00`) gönderin, ekrandaki yazıyı değil.
*   **Bir gün ve bir saat her yerde aynı an değildir.** `day.at(time)` cihazın saatini kullanır; başka bir yerin saati isteniyorsa fark işlevi açıkça verilir.
*   **Yuvarlanmış bir son, birimin son milisaniyesidir.** `now/d`'den `now/d`'ye bugünün tamamıdır: 00:00:00.000'dan 23:59:59.999'a, bir aralık sorgusunun `gte` ve `lte` ile okuduğu gibi. Sonu dahil olmayan (`lt`) bir sorguya giden son bir milisaniye eksiktir.
*   **Tarih matematiğindeki bir an cihazın farkıyla yazılır.** `toDateMath()` bir anı varsayılan olarak cihazın saatinde yazar (`+03:00`); aynı an her yerde aynı okunur, ama adresteki metin cihaza göre değişir. Sabit bir metin için `toDateMath { Duration.ZERO }`.
*   **Elastic seçicinin popover'ının dışına basış altındakine de gider.** `PantrixPopover`'ın kuralı başkadır: dışarıdaki ilk basış yalnızca kapatır. `PantrixElasticDatePicker` Elastic UI gibi davranır: bir ucun paneli açıkken öteki uca, Güncelle'ye ya da sayfada başka bir şeye basmak hem paneli kapatır hem basılanı yapar; açık ucun düğmesine basmak panelini kapatır. Panel açıkken sayfadaki bir bağlantıya basmak bağlantıyı da açar. `presentation = Sheet`'te ve `Dialog`'da bir scrim vardır; ona basmak yalnızca sheet'i ya da dialog'u kapatır.
*   **Arapça ay adları.** Paket Mısır ve Körfez'de kullanılan miladi adları verir ("أكتوبر"); Şam bölgesinin adları ("تشرين الأول") farklıdır.

*   **Otomatik yenilemenin bir turu `onRefresh`'i bekler.** Uzun süren bir yenileme bitmeden yeni tur sayılmaz; bir tur hata verirse yenileme durur ve `onAutoRefreshChange`'e `paused = true` ile bildirilir. Hata kendisi çağıranındır: seçici onu yutar, kaydetmek `onRefresh`'in işidir.
*   **‹, › ve yakınlaştırma aralığı anlara çevirir.** "Son 15 dakika" bir kez kaydırılınca iki sabit an olur ve artık saatle kaymaz; Elastic UI'da da öyle.

## 8. Doğrulama {#verification}

### Testler

:::table
| Ne | Nerede |
| :--- | :--- |
| Günün doğrulanması, haftanın günü, 1970'ten gün sayısı (±800.000 gün), ay ve yıl ekleme, sıralama, ISO yazma ve okuma, bir anın günü, aralık | `ui/foundation/src/commonTest/.../date/PantrixDateTest.kt` (JVM, Android, iOS, JS, Wasm) |
| Gece yarısına kadar bekleme, gece yarısı dönen gün | `TodayTest` |
| Ekranın saati: günü ve saati, yaz saatinin bittiği an, eşitlik, varsayılanı, `rememberToday`'in okuduğu saat ve değişen saat | `PantrixClockTest` |
| Ekranın saatini okuyan bileşenler: saat alanının seçicisi, elastic date picker'ın açık API'si, grafikler (kartezyen, polar, radar, Gantt) | `TimeTextFieldTest`, `ElasticDatePickerTest`, `ChartScreenClockTest` |
| Ay ve gün adları, uzun tarih, haftanın ilk günü, yanlış paketlerin reddi | `PantrixStringsTest` |
| Cihazın saat farkı | `SystemTimeTest`, `SystemTimeOnTheDesktopTest` |
| Saatin doğrulanması, sıralama, ISO yazma ve okuma, bir anın saati, gün ve saatten bir an (yaz saati geceleri dahil) | `ui/foundation/src/commonTest/.../date/PantrixTimeTest.kt` |
| 12 ve 24 saatlik yazım, saat sözleri | `PantrixStringsTest` |
| Saat seçici: sütunlar, sınırlar, gece aralığı, basış, tuşlar, yazılan rakamlar, Tab durakları, ortalama, ekran okuyucu, renkler, stil | `ui/components/src/commonTest/.../TimePicker*Test.kt`, `TimeColumnsTest.kt` |
| Takvim: ayın yerleri, tuşlar, Tab durağı, aralık, yıllar, ekran okuyucu, renkler, stil | `ui/components/src/commonTest/.../DatePicker*Test.kt`, `CalendarMonthTest.kt` |
| Tarih alanı: rakamlar ve ayraçlar, biçimler, Arap rakamları, yarım gün, kendi hataları, uygulamanın hatası, dışarıdan gelen değer, durum, takvimden seçim ve olaylar, Alt+↓, Escape, Enter, salt okunur, devre dışı, sheet ve dialog, düzenin söylenişi, form, dolu kutu | `ui/components/src/commonTest/.../DateTextFieldTest.kt`, `DateFieldFormatTest.kt` |
| Saat alanı: iki nokta, öne sıfır, kendi hataları, sınırlar ve gece yarısını geçen aralık, dışarıdan gelen değer, 12 saat ve günün yarısının düğmesi, A/P, seçicide her basış ve olaylar, Alt+↓ ile şimdiki saate odak, Enter'la kapanma, salt okunur, yapıştırma, durum, form, dolu kutu, düzenin söylenişi | `ui/components/src/commonTest/.../TimeTextFieldTest.kt`, `TimeFieldTextTest.kt` |
| Grafiklerin zaman ekseni (aynı gün hesabını kullanır) | `ChartTimeTest`, `ChartTimeAxisTest` |
| Kayan noktalar: taşıma, yuvarlama, haftanın başı (1, 6, 7), yaz saati gecesi, aralığın okunması, tarih matematiği yazma ve okuma, saniyenin kesri | `ui/foundation/src/commonTest/.../date/PantrixTimePointTest.kt` |
| Sürenin sözleri ve çoğulları (İngilizce, Türkçe, Arapça) | `PantrixTimeWordsTest` |
| Elastic date picker: aralığın adı ve uçları, ada basmak, hızlı menü, ‹ ›, bekleyen düzenleme, sırası bozuk uçlar, `None`, Yenile ve `onRefresh`, `loading`, `enabled`, yeni aralık, sekmenin ucu çevirmesi, takvim, yarım saatlik saatler ve yazılan an, 12 saat, sheet, dialog, başlıksız panel, dar yerde simge, renkler, stil, son kullanılanlar, bir anın yazılıp okunması | `ui/components/src/commonTest/.../ElasticDatePicker*Test.kt` |
:::

### Elle bakılanlar

*   **Web:** takvim sayfada, popover'da, dialog'da ve sheet'te; aralık seçimi ve işaretçinin altındaki önizlemesi, yıl ızgarası, klavye (Tab, oklar, Page Down, Enter), Türkçe ve karanlık tema.
*   **Android emülatörü (API 33):** demo'nun "Date" bölümü, sayfadaki aralık, sheet'te takvim ve parmakla kaydırılan yıl ızgarası, seçilen günün yazılması; erişilebilirlik ağacında her gün tam adıyla ("Monday, October 12, 2026").
*   **iOS simülatörü:** "Date" bölümü, Türkçe sözler, dialog'da seçim ve Tamam.
*   **Web (elastic date picker):** aralığın adı, ada basınca iki uç ve başlangıcın paneli, göreli sekmede bekleyen düzenleme (yeşil alan, işaret, yeşil Güncelle), mutlak sekmede takvim ve yarım saatlik saatler, Güncelle, hızlı menü ve son kullanılanlar, sheet'te açılan panel, dar yerde simgeli Yenile.

### Doğrulanmayanlar

*   Bir ekran okuyucunun sesi; iOS'un erişilebilirlik ağacı (Maestro simülatöre bağlanamadı); sağdan sola bir dilde cihazda takvim (testlerde sınandı); gece yarısının gerçek bir cihazda dönmesi (testte sahte bir saatle sınandı).

## 9. Bu depoda nerede {#repository toc="9. Depoda"}

*   Foundation: `ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/date/` (`PantrixDate.kt`, `PantrixTime.kt`, `Today.kt`, `PantrixClock.kt`), `platform/SystemTime.kt` ve her platformun karşılığı, `l10n/PantrixStrings.kt`.
*   Takvim: `ui/components/src/commonMain/kotlin/com/pantrixui/components/date/datepicker/`.
*   Tarih ve saat alanları: `ui/components/src/commonMain/kotlin/com/pantrixui/components/date/common/` (`PickerField.kt` ortak çekirdek, `PickerFieldBindings.kt` üç biçim, `PickerBoxes.kt` iki kutu, `FieldDigits.kt` rakam yardımcıları; tarih için `DateField.kt`, `DateFieldBindings.kt`, `DateFieldText.kt`, `DateFieldPaste.kt`, `DateFieldFormat.kt`, `DateTextFieldState.kt`; saat için `TimeField.kt`, `TimeFieldBindings.kt`, `TimeFieldText.kt`, `TimeTextFieldState.kt`; ortak `DatePickerPresentation.kt`), `date/datetextfield/`, `date/datefilledtextfield/`, `date/timetextfield/`, `date/timefilledtextfield/`.
*   Saat seçici: `ui/components/src/commonMain/kotlin/com/pantrixui/components/date/timepicker/`.
*   Kayan aralık: foundation'da `date/PantrixTimePoint.kt`, `PantrixTimeUnit.kt`, `DateMath.kt` (taşıma, yuvarlama), `DateMathText.kt` (yazma, okuma), `l10n/PantrixTimeWords.kt`; bileşen `ui/components/src/commonMain/kotlin/com/pantrixui/components/date/elasticdatepicker/`.
*   Grafiklerin zaman ekseni: `ui/components/.../chart/time/CivilTime.kt` gün hesabını `PantrixDate`'ten alır.
*   Demo: `app/shared/src/commonMain/kotlin/com/pantrixui/cmp/demo/samples/DatePickerSamples.kt`, `TimePickerSamples.kt`, `ElasticDatePickerSamples.kt`, `DateTextFieldSamples.kt`, `DateFilledTextFieldSamples.kt`, `TimeTextFieldSamples.kt`, `TimeFilledTextFieldSamples.kt`.

## 10. Açık kalanlar {#open}

`docs/OPEN_ITEMS.md`'de: saat alanları (`PantrixTimeTextField`, `PantrixTimeFilledTextField`; iskeletleri duruyor), iki uçlu bir tarih alanı, adlı saat dilimleri, cihazın 12/24 saat ayarı, metinden dile göre tarih ve saat okumak; elastic date picker'da elle basılan Yenile'nin hatası ve dışarı basışı geçiren popover seçeneğinin genel yapılması.
