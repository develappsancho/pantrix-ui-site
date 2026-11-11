---
title: Uygulamanın kendi sözleri
description: Okurunun dilini seçtiği bir Compose Multiplatform uygulamasında bileşenler seçilen dile geçerken uygulamanın kendi sözlerinin neden cihazın dilinde kaldığı ve uygulamanın sözlerini de geçirmenin iki yolunu. Sorun Pantrix'e özgü değildir: okurunun cihazınkinden başka bir dil seçmesine izin veren her Compose Multiplatform uygulamasında aynı biçimde görülür.
---

Compose'un kaynakları hakkında söylenenler Compose Multiplatform 1.12.2'nin kaynaklarında (`components-resources`) okundu ya da kütüphaneyle yapılan bir uygulamada görüldü; neyin nerede görüldüğü 6. bölümde yazılıdır.

## 1. Sorun {#problem}

:::aside language
### Belirti

*   Okur ayarlarda Türkçeyi seçer. Bileşenler hemen "Kapat" ve "Yükleniyor" der; uygulamanın kendi düğmeleri ve başlıkları cihazın dili olan İngilizcede kalır.
*   Web'de ekran Türkçeye döner, uygulamanın ekranın dışında okuduğu bir metin, bir dosyaya yazılan bir yorum, İngilizce kalır.
*   Bir test ekranı Türkçe çizer (`PantrixTestTheme(locale = PantrixLocale.Turkish)`), içindeki uygulama sözleri yine İngilizcedir.
:::

### Neden

*   **Temanın dili bileşenlerindir.** `PantrixTheme(locale = …)` dili Pantrix'in kendisinin söylediklerine (`PantrixStrings`) ve yazının yönüne verir. Uygulamanın başka bir yerden okuduğuna ulaşmaz.
*   **Compose'un kaynakları platformun dilini okur.** `stringResource` ve `getString` dili cihazdan alır: Android'de ve JVM'de varsayılan `Locale`'den, iOS'ta öncelikli dillerden, web'de tarayıcıdan. `PantrixTheme.locale`'i görmezler.
*   **Web iki değer okur.** Bileşimin içinde kaynaklar `navigator.languages`'ı alır; dışında, `getString`'in ortamı `navigator.language`'ı alır (bir uygulamada görüldü). Bunlardan birini değiştiren bir uygulama sözlerinin yarısını değiştirir.

## 2. Çözüm {#fix}

:::table
| # | Yol | Ne verir | Neye mal olur |
| :--- | :--- | :--- | :--- |
| A | **Uygulamanın sözleri Kotlin'de, temanın diliyle seçilir.** Her dil için bir söz nesnesi, `PantrixStrings` gibi | Dil hemen ve her platformda aynı biçimde değişir; bir test onu temayla seçer; platformun hiçbir şeyi değiştirilmez | Sözler Compose'un kaynaklarından çıkar |
| B | **Compose'un kaynakları, platformun dili değiştirilerek.** Her platformun kendi dil değeri okurun seçimine ayarlanır ve bileşim baştan kurulur | Sözler kaynaklarda kalır | Her platform için bir parça, web'de iki değer, ve bütün uygulama için değişen platform dili |
:::

## 3. Pantrix ile: sözler kotlin'de {#with-pantrix-the-words-in-kotlin}

### 3.1. Sözler

Bir arayüz uygulamanın söylediklerini tutar; her dil için bir nesne onları söyler. Temanın dili nesneyi seçer:

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

/** En yakın temanın dilindeki sözler. */
val appWords: AppWords
    @Composable @ReadOnlyComposable
    get() = if (PantrixTheme.locale.language == "tr") TurkishWords else EnglishWords
```

*   **Bir ekran `appWords`'ü okur**, bir bileşenin `PantrixTheme.strings`'i okuduğu gibi: `PantrixButton(text = appWords.save, onClick = onSave, tagKey = "save")`.
*   **Otomasyon etiketi hiçbir zaman sözler değildir.** Bir kontrolün `tagKey`'i her dilde aynı kalır: bir test ve bir Maestro akışı onu dil ne olursa olsun bulur.

### 3.2. Seçilen dil

Okurun seçimini uygulama tutar ve en dıştaki temaya verir:

```kotlin
@Composable
fun App(settings: Settings) {
    val chosen by settings.language.collectAsState() // null: cihazınki
    PantrixTheme(locale = chosen ?: systemPantrixLocale()) { Home() }
}
```

Pantrix'in bileşenleri ve uygulamanın sözleri böylece aynı dili konuşur, yazının yönü de onu izler.

### 3.3. Test etmek

`PantrixTestTheme(locale = PantrixLocale.Turkish)` uygulamanın sözlerini de Türkçe çizer, çünkü onlar temanın diliyle seçilir. Bir sözün testi onu bir dilde arar, kontrolü ise her dilde etiketiyle bulur.

## 4. Pantrix'in teması olmadan: compose'un kaynakları {#without}

Compose'un kaynakları platformun dilini izler; bu yüzden uygulama o dili, her platformda kendi yoluyla, okurun seçimine ayarlar ve bileşimi `key(language) { … }` ile baştan kurar:

:::table
| Platform | Dilin ayarlandığı yer |
| :--- | :--- |
| Android | `Locale.setDefault(locale)` ve kaynaklar için o dili taşıyan bir yapılandırma |
| JVM | `Locale.setDefault(locale)` |
| iOS | `NSUserDefaults.standardUserDefaults.setObject(listOf(tag), forKey = "AppleLanguages")` |
| Web | Sayfada `navigator.languages`'a ve `navigator.language`'a seçilen etiketi veren bir betik: ikisi birden, yoksa bileşimin dışında okunan tarayıcının dilinde kalır |
:::

Bu, JetBrains'in Compose Multiplatform'un kaynakları için belgelediği yoldur (`LocalAppLocale`). Pantrix'in kendi sözleri, uygulamanınkiler hangi yolla giderse gitsin, `PantrixTheme.locale`'i izler: uygulama aynı dili temaya da verir.

## 5. Tuzaklar {#pitfalls}

*   **Web'de sözlerin yarısı.** Yalnızca `navigator.languages`'ı değiştirmek bileşimin dışındaki `getString`'i `navigator.language`'ın dilinde bırakır (1. bölüm).
*   **Platformun dili bütün uygulamanındır.** B yolu onu uygulamadaki her ekran ve her kütüphane için değiştirir; iOS'ta `AppleLanguages` uygulamanın yeniden açılışından sonra da kalır.
*   **Bir sayıdan yapılan bir söz.** "1 order", "2 orders": bir dil kendi biçiminde sayar; onu bir ekranın doldurduğu bir kalıp değil, söz nesnesindeki bir fonksiyon söyler.

## 6. Doğrulama {#verification}

### Elle bakılanlar

*   **Bu depoda A yolu:** Pantrix'in demo'su metinlerini Kotlin'de, Türkçe ve İngilizce tutar (`Bilingual`; `LocalDemoLanguage` ile seçilir, onun `locale`'ini demo temasına verir). Ayarları dili hemen değiştirir: kabuğun metinleri, sayfalar, örnekler ve bileşenler birlikte. `DemoPagesTest` her sayfayı iki dilde çizer; kabuğun Maestro akışları dili Android'de ve iOS'ta değiştirir.
*   **İki yol kütüphaneyle yapılan bir uygulamada (Formatico, 2026-11-06):** B yolu Android'de, iOS simülatöründe ve web'de, dil hemen değişerek ve bir sonraki açılışta kalarak; web'in iki değeri o zaman bulundu. Uygulamanın sonra geçtiği A yolu da.

### Doğrulanmayanlar

*   Bu depoda B yolu: tablosu, bir uygulamada görüleni ve JetBrains'in belgelediğini yeniden söyler.

## 7. Bu depoda nerede {#repository toc="7. Depoda"}

*   Pantrix'in sözleri: `ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/l10n/` (`PantrixStrings`, `PantrixLocale`).
*   Demo'nun A yolu: `app/shared/src/commonMain/kotlin/com/pantrixui/cmp/demo/catalog/DemoLanguage.kt`; demo'nun "Dil ve sözler" sayfası, `app/shared/src/commonMain/kotlin/com/pantrixui/cmp/demo/samples/`.
