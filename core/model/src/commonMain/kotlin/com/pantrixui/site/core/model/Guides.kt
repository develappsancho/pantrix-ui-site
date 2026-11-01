package com.pantrixui.site.core.model

/**
 * A word or a sentence the site writes itself, in each language it is written in.
 *
 * @property english what it says in English.
 * @property turkish what it says in Turkish.
 */
data class Words(
    val english: String,
    val turkish: String
) {
    /** What it says in [language]. */
    fun of(language: Language): String = when (language) {
        Language.English -> english
        Language.Turkish -> turkish
    }
}

/**
 * The topics the guides page groups the library's guides by.
 *
 * @property title what the topic is called.
 * @property note the line under its heading: what its guides are about.
 */
enum class GuideTopic(
    val title: Words,
    val note: Words
) {
    Layout(
        Words("Layout and windows", "Düzen ve pencereler"),
        Words(
            "Fitting a screen to its window, its panes and its bars.",
            "Bir ekranı penceresine, bölmelerine ve çubuklarına uydurmak."
        )
    ),
    Text(
        Words("Text and input", "Metin ve giriş"),
        Words("Fields, keyboards, type and the order of words.", "Alanlar, klavyeler, yazı ve sözcüklerin sırası.")
    ),
    Platform(
        Words("Platform services", "Platform hizmetleri"),
        Words("What common code cannot reach by itself.", "Ortak kodun kendi başına ulaşamadıkları.")
    ),
    Quality(
        Words("Quality and tooling", "Kalite ve araçlar"),
        Words(
            "Tests, checks and the tools around an app.",
            "Testler, denetimler ve bir uygulamanın çevresindeki araçlar."
        )
    )
}

/** A platform a guide's problem is seen on, where it is not seen on all of them. */
enum class GuidePlatform {
    Android,
    Ios,
    Desktop,
    Web
}

/**
 * What the site says of one of the library's guides, beside the guide itself: the guides come from the library's
 * documents, and what a card says of each is the site's own.
 *
 * @property key the guide's key, its address: `keyboard-dismissal` is `/docs/guides/keyboard-dismissal`.
 * @property topic the group the guides page lists it in.
 * @property title what its card, its page and the menu call it.
 * @property summary its card's line: what it answers.
 * @property menuTitle a shorter name for the narrow sidebar, where [title] is too long for it.
 * @property everyApp whether its problem is every Compose Multiplatform app's, not Pantrix's own.
 * @property seenOn the platforms its problem is seen on, or none where it is seen on every one.
 * @property withoutPantrix whether it gives its fix without the library too, for any project to copy.
 */
data class GuideEntry(
    val key: String,
    val topic: GuideTopic,
    val title: Words,
    val summary: Words,
    val menuTitle: Words? = null,
    val everyApp: Boolean = false,
    val seenOn: Set<GuidePlatform> = emptySet(),
    val withoutPantrix: Boolean = false
) {
    /**
     * Where the library keeps the guide in [language], on its repository: the text the site's page is written from,
     * `docs/guide/keyboard-dismissal/KEYBOARD_DISMISSAL_GUIDE.en.md` in English.
     */
    fun source(language: Language): String {
        val name = key.uppercase().replace('-', '_') + "_GUIDE"
        val suffix = when (language) {
            Language.English -> ".en.md"
            Language.Turkish -> ".md"
        }
        return "${LibraryFacts.REPOSITORY}/blob/main/docs/guide/$key/$name$suffix"
    }
}

/**
 * The library's guides as the site lists them, in the order each topic shows them. A guide the library adds gets a
 * line here, or the content tool stops the build: the site lists every guide the documents hold.
 */
object GuideCatalogue {
    val entries: List<GuideEntry> = listOf(
        GuideEntry(
            key = "adaptive",
            topic = GuideTopic.Layout,
            title = Words("Fitting the window", "Pencereye uyum"),
            summary = Words(
                "The window’s size classes, posture and folds that PantrixTheme gives, and how a test fixes them.",
                "PantrixTheme'in verdiği pencere boyut sınıfları, duruş ve katlar, ve bir testin onları nasıl " +
                    "sabitlediği."
            )
        ),
        GuideEntry(
            key = "adaptive-usage",
            topic = GuideTopic.Layout,
            title = Words("Which window helper, when", "Hangi pencere yardımcısı, ne zaman"),
            summary = Words(
                "Three screens worked through: which helper answers which question, and how state survives a " +
                    "resize.",
                "Baştan sona üç ekran: hangi yardımcının hangi soruyu yanıtladığı ve durumun boyut değişiminden " +
                    "nasıl sağ çıktığı."
            )
        ),
        GuideEntry(
            key = "window-title",
            topic = GuideTopic.Layout,
            title = Words("The window’s title", "Pencerenin başlığı"),
            summary = Words(
                "One window and many screens: the window’s name kept in step with what is shown.",
                "Tek pencere, çok ekran: pencerenin adı gösterilenle birlikte değişir."
            ),
            everyApp = true,
            seenOn = setOf(GuidePlatform.Desktop, GuidePlatform.Web),
            withoutPantrix = true
        ),
        GuideEntry(
            key = "scroll-padding",
            topic = GuideTopic.Layout,
            title = Words("Focus beneath a bar", "Bir çubuğun altında kalan odak"),
            summary = Words(
                "Keyboard focus that slides behind a top or bottom bar, and the modifier that keeps it in view.",
                "Üst ya da alt çubuğun arkasına kayan klavye odağı ve onu görünür tutan değiştirici."
            ),
            everyApp = true,
            withoutPantrix = true
        ),
        GuideEntry(
            key = "pointer-icons",
            topic = GuideTopic.Layout,
            title = Words("Resize pointers", "Boyutlandırma imleçleri"),
            summary = Words(
                "The pointer that should change over a handle that resizes, on every platform but iOS.",
                "Boyutlandıran bir tutamacın üstünde değişmesi gereken imleç, iOS dışındaki her platformda."
            ),
            everyApp = true,
            seenOn = setOf(GuidePlatform.Android, GuidePlatform.Desktop, GuidePlatform.Web),
            withoutPantrix = true
        ),
        GuideEntry(
            key = "system-bars",
            topic = GuideTopic.Layout,
            title = Words("System bars that follow the theme", "Temayı izleyen sistem çubukları"),
            summary = Words(
                "The system’s bars, keyboard and controls in the app’s own light or dark, not the system’s.",
                "Sistemin çubukları, klavyesi ve kontrolleri, sistemin değil uygulamanın açık ya da koyu temasında."
            ),
            everyApp = true,
            seenOn = setOf(GuidePlatform.Android, GuidePlatform.Ios),
            withoutPantrix = true
        ),
        GuideEntry(
            key = "modal",
            topic = GuideTopic.Layout,
            title = Words("A modal layer of the app’s own", "Uygulamanın kendi modal katmanı"),
            summary = Words(
                "A scrim over the whole window and content the app draws over it, the same on every platform.",
                "Bütün pencerenin üstünde bir perde ve onun üstünde uygulamanın çizdiği içerik, her platformda aynı."
            ),
            everyApp = true,
            withoutPantrix = true
        ),
        GuideEntry(
            key = "keyboard-dismissal",
            topic = GuideTopic.Text,
            title = Words("Putting the keyboard away", "Klavyeyi kapatma"),
            summary = Words(
                "Why a field’s keyboard stays open on an iPhone, and the tap outside that puts it away.",
                "Bir alanın klavyesi iPhone'da neden açık kalır, ve onu kapatan dışarıya dokunuş."
            ),
            everyApp = true,
            seenOn = setOf(GuidePlatform.Ios, GuidePlatform.Android, GuidePlatform.Web),
            withoutPantrix = true
        ),
        GuideEntry(
            key = "number-keyboard",
            topic = GuideTopic.Text,
            title = Words("A number field and its action key", "Sayı alanı ve eylem tuşu"),
            summary = Words(
                "A number field that cannot be sent on an iPhone, and the action key that sends it.",
                "iPhone'da gönderilemeyen bir sayı alanı ve onu gönderen eylem tuşu."
            ),
            everyApp = true,
            seenOn = setOf(GuidePlatform.Ios),
            withoutPantrix = true
        ),
        GuideEntry(
            key = "keyboard-shortcuts",
            topic = GuideTopic.Text,
            title = Words("Keyboard shortcuts", "Klavye kısayolları"),
            summary = Words(
                "A shortcut for the whole window, Cmd+K or Ctrl+K, written once for each system.",
                "Bütün pencerenin kısayolu, Cmd+K ya da Ctrl+K, her sistem için bir kez yazılır."
            ),
            everyApp = true,
            withoutPantrix = true
        ),
        GuideEntry(
            key = "typography",
            topic = GuideTopic.Text,
            title = Words("Typography", "Tipografi"),
            summary = Words(
                "The type scale, what text with no style is set in, and the size of what is typed.",
                "Yazı ölçeği, stili verilmeyen yazının neyle yazıldığı ve yazılanın boyu."
            ),
            everyApp = true
        ),
        GuideEntry(
            key = "web-fonts",
            topic = GuideTopic.Text,
            title = Words("A typeface that arrives late on the web", "Web'de geç gelen yazı tipi"),
            summary = Words(
                "Why an app’s own typeface comes a moment late on the web, measured and cured.",
                "Bir uygulamanın kendi yazı tipi web'de neden bir an geç gelir, ölçülmüş ve giderilmiş hâliyle."
            ),
            menuTitle = Words("A typeface that arrives late", "Geç gelen yazı tipi"),
            everyApp = true,
            seenOn = setOf(GuidePlatform.Web),
            withoutPantrix = true
        ),
        GuideEntry(
            key = "text-order",
            topic = GuideTopic.Text,
            title = Words("Sorting by a language’s alphabet", "Bir dilin alfabesine göre sıralama"),
            summary = Words(
                "A list of names in the order of the language’s alphabet, not of character codes.",
                "Bir ad listesi, karakter kodlarının değil dilin alfabesinin sırasıyla."
            ),
            everyApp = true,
            withoutPantrix = true
        ),
        GuideEntry(
            key = "number-format",
            topic = GuideTopic.Text,
            title = Words("Formatting numbers", "Sayıları biçimlendirme"),
            summary = Words(
                "An amount that comes out the same on every platform, in each language’s digits.",
                "Her platformda aynı çıkan, her dilin kendi rakamlarıyla yazılan bir tutar."
            ),
            everyApp = true,
            withoutPantrix = true
        ),
        GuideEntry(
            key = "date-time",
            topic = GuideTopic.Platform,
            title = Words("Date and time", "Tarih ve saat"),
            summary = Words(
                "A day and a time of day in common code without a date library, and how they become a moment.",
                "Ortak kodda tarih kütüphanesi olmadan bir gün ve bir saat, ve bir ana nasıl dönüştükleri."
            ),
            everyApp = true,
            withoutPantrix = true
        ),
        GuideEntry(
            key = "clipboard",
            topic = GuideTopic.Platform,
            title = Words("Copying text to the clipboard", "Panoya metin kopyalama"),
            summary = Words(
                "Copying is not one call: what a copy button should say, and when, on each platform.",
                "Kopyalamak tek bir çağrı değildir: bir kopyalama düğmesi her platformda ne zaman ne demeli."
            ),
            everyApp = true,
            withoutPantrix = true
        ),
        GuideEntry(
            key = "files",
            topic = GuideTopic.Platform,
            title = Words("Picking, dropping and saving files", "Dosya seçmek, bırakmak ve kaydetmek"),
            summary = Words(
                "A file the user picks or drops, read in common code, and one the app makes, saved where the user " +
                    "says.",
                "Kullanıcının seçtiği ya da bıraktığı bir dosya ortak kodda okunur; uygulamanın yaptığı bir dosya da " +
                    "kullanıcının dediği yere kaydedilir."
            ),
            everyApp = true,
            withoutPantrix = true
        ),
        GuideEntry(
            key = "chart-export",
            topic = GuideTopic.Platform,
            title = Words("Exporting a chart as a file", "Bir grafiği dosya olarak dışa aktarma"),
            summary = Words(
                "A chart as PNG, SVG or PDF, and how the file reaches the user on each platform.",
                "PNG, SVG ya da PDF olarak bir grafik, ve dosyanın her platformda kullanıcıya nasıl ulaştığı."
            )
        ),
        GuideEntry(
            key = "testing",
            topic = GuideTopic.Quality,
            title = Words("Testing a screen", "Bir ekranı test etmek"),
            summary = Words(
                "Tests that answer the same on every machine: tags, a fixed theme and doubles of the platform.",
                "Her makinede aynı yanıtı veren testler: etiketler, sabit bir tema ve platformun yerine geçenler."
            ),
            everyApp = true,
            withoutPantrix = true
        ),
        GuideEntry(
            key = "diagnostics",
            topic = GuideTopic.Quality,
            title = Words("Runtime diagnostics", "Çalışma anı tanılaması"),
            summary = Words(
                "Flaws seen only on a running screen, caught in the app’s own debug build.",
                "Yalnızca çalışan bir ekranda görülen kusurlar, uygulamanın kendi hata ayıklama derlemesinde yakalanır."
            ),
            everyApp = true,
            withoutPantrix = true
        ),
        GuideEntry(
            key = "detekt",
            topic = GuideTopic.Quality,
            title = Words("detekt rules in your app", "Uygulamanızda detekt kuralları"),
            summary = Words(
                "The rules the library keeps, and the ones an app turns on.",
                "Kütüphanenin uyduğu kurallar ve bir uygulamanın açtıkları."
            )
        ),
        GuideEntry(
            key = "analytics",
            topic = GuideTopic.Quality,
            title = Words("Connecting events to analytics", "Olayları analitiğe bağlamak"),
            summary = Words(
                "The events the components report, what they never carry, and a connector to a product.",
                "Bileşenlerin bildirdiği olaylar, hiçbir zaman taşımadıkları ve bir ürüne bağlayan sınıf."
            ),
            everyApp = true,
            withoutPantrix = true
        ),
        GuideEntry(
            key = "material3",
            topic = GuideTopic.Quality,
            title = Words("The Material 3 bridge", "Material 3 köprüsü"),
            summary = Words(
                "Moving from Material 3 a screen at a time, both themes speaking the same colours.",
                "Material 3'ten ekran ekran geçmek, iki tema aynı renkleri konuşarak."
            ),
            everyApp = true
        ),
        GuideEntry(
            key = "ai",
            topic = GuideTopic.Quality,
            title = Words("Teaching Pantrix to an assistant", "Pantrix'i bir asistana öğretmek"),
            summary = Words(
                "The documents and the MCP server that teach Pantrix to Claude, Copilot or Cursor.",
                "Pantrix'i Claude'a, Copilot'a ya da Cursor'a öğreten belgeler ve MCP sunucusu."
            ),
            everyApp = true
        )
    )

    private val byKey: Map<String, GuideEntry> = entries.associateBy { it.key }

    /** What the site says of the guide [key], or null where it lists no such guide. */
    fun of(key: String): GuideEntry? = byKey[key]

    /** The guide listed before [key], which its page's foot leads back to, or null for the first. */
    fun previousOf(key: String): GuideEntry? = entries.getOrNull(entries.indexOfFirst { it.key == key } - 1)

    /** The guide listed after [key], which its page's foot leads on to, or null for the last and for no guide. */
    fun nextOf(key: String): GuideEntry? {
        val index = entries.indexOfFirst { it.key == key }
        return if (index < 0) null else entries.getOrNull(index + 1)
    }
}
