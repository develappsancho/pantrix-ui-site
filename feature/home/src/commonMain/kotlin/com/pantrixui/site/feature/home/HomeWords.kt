package com.pantrixui.site.feature.home

import com.pantrixui.foundation.platform.PlatformType
import com.pantrixui.site.core.model.LibraryFacts
import com.pantrixui.site.core.model.Route
import com.pantrixui.site.core.model.Words

/** The landing page's words, in each language. */
internal object HomeSay {
    val News = Words("New", "Yeni")
    val NewsLine = Words(
        "Text areas, a form's fields and a tap that puts the keyboard away",
        "Metin kutuları, formun alanları ve klavyeyi kapatan bir dokunuş"
    )
    val TitleLines = listOf(
        Words("One codebase.", "Tek kod tabanı."),
        Words("Four platforms.", "Dört platform."),
        Words("Each one at home.", "Her biri kendi evinde.")
    )
    val Lead = Words(
        "Pantrix UI is a design system for Compose Multiplatform: 100 components on Compose Foundation, with no " +
            "Material dependency, and one theme that follows the conventions of Android, iOS, the desktop and the web.",
        "Pantrix UI, Compose Multiplatform için bir tasarım sistemi: Compose Foundation üstünde 100 bileşen, " +
            "Material'a hiçbir bağımlılık olmadan, ve Android'in, iOS'un, masaüstünün ve web'in alışkanlıklarını " +
            "izleyen tek bir tema."
    )
    val NewsLineShort = Words("Text areas and forms", "Metin kutuları ve formlar")
    val LeadShort = Words(
        "A design system for Compose Multiplatform: 100 components on Compose Foundation, no Material, and one theme " +
            "that follows each platform's conventions.",
        "Compose Multiplatform için bir tasarım sistemi: Compose Foundation üstünde 100 bileşen, Material yok, ve " +
            "her platformun alışkanlıklarını izleyen tek bir tema."
    )
    val GetStarted = Words("Get started", "Başla")
    val Browse = Words("Browse 100 components", "100 bileşene göz at")
    const val INSTALL = "implementation(\"com.pantrixui:components:${LibraryFacts.VERSION}\")"
    val Copy = Words("Copy", "Kopyala")
    val Copied = Words("Copied", "Kopyalandı")
    val CopyInstall = Words("Copy the dependency", "Bağımlılığı kopyala")

    // The showcase.
    val WriteReview = Words("Write a review", "Yorum yaz")
    val Verified = Words("Verified order", "Onaylı sipariş")
    val Title = Words("Title", "Başlık")
    val TitleValue = Words("Soft fabric, true to size", "Yumuşak kumaş, tam beden")
    val Comment = Words("Comment", "Yorum")
    val CommentValue = Words(
        "The fabric is softer than I expected, and the size guide was right.",
        "Kumaş beklediğimden yumuşak, beden tablosu da doğruydu."
    )
    val PostWithName = Words("Post with my name", "Adımla yayınla")
    val Cancel = Words("Cancel", "Vazgeç")
    val Send = Words("Send review", "Yorumu gönder")
    val ReviewsThisWeek = Words("Reviews this week", "Bu haftaki yorumlar")
    val Chips = listOf(
        Words("Fits well", "Tam oturuyor"),
        Words("Fast delivery", "Hızlı teslimat"),
        Words("Good value", "Fiyatına değer")
    )
    val ReviewSent = Words("Review sent", "Yorum gönderildi")
    val Undo = Words("Undo", "Geri al")

    /** What a platform's conventions are, under the showcase. */
    fun note(platform: PlatformType): Words = when (platform) {
        PlatformType.Android -> Words(
            "touch targets of 48dp, pill buttons with a state layer, fields 48dp tall.",
            "48dp dokunma hedefleri, durum katmanlı hap düğmeler, 48dp yüksekliğinde alanlar."
        )

        PlatformType.Ios -> Words(
            "touch targets of 44pt, rounded buttons that press in to 97%, fields 44pt tall.",
            "44pt dokunma hedefleri, basınca %97'ye küçülen yuvarlak düğmeler, 44pt yüksekliğinde alanlar."
        )

        PlatformType.Desktop -> Words(
            "a pointer’s targets, hover states and keyboard shortcuts, fields 40dp tall.",
            "işaretçinin hedefleri, üstüne gelme hâlleri ve klavye kısayolları, 40dp yüksekliğinde alanlar."
        )

        PlatformType.Web -> Words(
            "a mouse’s targets on a desk and a finger’s on a phone, a focus ring for the keyboard, fields 40dp tall.",
            "masada farenin, telefonda parmağın hedefleri, klavye için odak halkası, 40dp yüksekliğinde alanlar."
        )
    }

    // The numbers.
    val Stats = listOf(
        "100" to Words("components", "bileşen"),
        "6" to Words("Kotlin targets, from Android to Wasm", "Kotlin hedefi, Android'den Wasm'a"),
        "4" to Words("platform presets in one theme", "platform hazır ayarı, tek temada"),
        "3" to Words("languages, right-to-left included", "dil, sağdan sola dahil"),
        "0" to Words("Material dependencies", "Material bağımlılığı")
    )

    /** The numbers' shorter words, where a tablet and a phone have less room. */
    val StatsShort = listOf(
        Words("components", "bileşen"),
        Words("Kotlin targets", "Kotlin hedefi"),
        Words("platform presets", "platform hazır ayarı"),
        Words("languages, RTL too", "dil, RTL de"),
        Words("Material dependencies", "Material bağımlılığı")
    )

    // The platforms.
    val PlatformsKicker = Words("One theme, four sets of manners", "Tek tema, dört ayrı alışkanlık")
    val PlatformsTitle = Words("The same button is not the same everywhere.", "Aynı düğme her yerde aynı değil.")
    val PlatformsLead = Words(
        "A theme knows the platform it runs on. Touch targets, the shape of a button, the way it answers a press and " +
            "the height of a field follow that platform, and ",
        "Tema üzerinde çalıştığı platformu bilir. Dokunma hedefleri, düğmenin biçimi, basışa verdiği karşılık ve " +
            "alanın yüksekliği o platformu izler; "
    )
    val PlatformsLeadEnd = Words(
        " asks for another one in a preview or a test.",
        " bir önizlemede ya da testte başka birini ister."
    )
    val PlatformsKickerShort = Words("Four sets of manners", "Dört ayrı alışkanlık")
    val PlatformsLeadShortStart = Words(
        "Touch targets, the shape of a button, the way it answers a press and the height of a field follow the " +
            "platform; ",
        "Dokunma hedefleri, düğmenin biçimi, basışa verdiği karşılık ve alanın yüksekliği platformu izler; "
    )
    val PlatformsLeadPhone = Words(
        "Touch targets, a button's shape, its press and a field's height follow the platform. Swipe through the four.",
        "Dokunma hedefleri, düğmenin biçimi, basışı ve alanın yüksekliği platformu izler. Dördünü kaydırarak gez."
    )
    val Email = Words("E-mail address", "E-posta adresi")
    val Continue = Words("Continue", "Devam")

    /** The four facts of a platform's card, each a label and a value. */
    fun facts(platform: PlatformType): List<Pair<Words, Words>> = when (platform) {
        PlatformType.Android -> listOf(
            Words("Touch target", "Dokunma hedefi") to Words("48dp", "48dp"),
            Words("Button", "Düğme") to Words("Pill", "Hap"),
            Words("Press", "Basış") to Words("State layer", "Durum katmanı"),
            Words("Field", "Alan") to Words("48dp", "48dp")
        )

        PlatformType.Ios -> listOf(
            Words("Touch target", "Dokunma hedefi") to Words("44pt", "44pt"),
            Words("Button", "Düğme") to Words("Rounded", "Yuvarlak"),
            Words("Press", "Basış") to Words("Scales to 97%", "%97'ye küçülür"),
            Words("Field", "Alan") to Words("44pt", "44pt")
        )

        PlatformType.Desktop -> listOf(
            Words("Touch target", "Dokunma hedefi") to Words("A pointer's", "İşaretçininki"),
            Words("Button", "Düğme") to Words("Pill, hover", "Hap, üstüne gelme"),
            Words("Keys", "Tuşlar") to Words("Shortcuts", "Kısayollar"),
            Words("Field", "Alan") to Words("40dp", "40dp")
        )

        PlatformType.Web -> listOf(
            Words("Touch target", "Dokunma hedefi") to Words("Mouse or finger", "Fare ya da parmak"),
            Words("Button", "Düğme") to Words("Pill", "Hap"),
            Words("Focus", "Odak") to Words("Ring for keys", "Tuşlar için halka"),
            Words("Field", "Alan") to Words("40dp", "40dp")
        )
    }

    /** A platform's facts in one line, as a tablet's and a phone's cards say them. */
    fun factsLine(platform: PlatformType): Words = when (platform) {
        PlatformType.Android -> Words(
            "Touch target 48dp · a pill · a state layer on press · fields 48dp",
            "Dokunma hedefi 48dp · hap · basışta durum katmanı · alanlar 48dp"
        )

        PlatformType.Ios -> Words(
            "Touch target 44pt · rounded · presses in to 97% · fields 44pt",
            "Dokunma hedefi 44pt · yuvarlak · basınca %97'ye küçülür · alanlar 44pt"
        )

        PlatformType.Desktop -> Words(
            "A pointer’s target · a pill with hover · shortcuts · fields 40dp",
            "İşaretçinin hedefi · üstüne gelmeli hap · kısayollar · alanlar 40dp"
        )

        PlatformType.Web -> Words(
            "Mouse or finger · a pill · a ring for the keyboard · fields 40dp",
            "Fare ya da parmak · hap · klavye için halka · alanlar 40dp"
        )
    }

    // Write it once.
    val OnceKicker = Words("Write it once", "Bir kez yaz")
    val OnceTitle = Words("Kotlin you would write anyway.", "Zaten yazacağın Kotlin.")
    val OnceLeadStart = Words(
        "Every component takes a state, a label and a ",
        "Her bileşen bir durum, bir etiket ve bir "
    )
    val OnceLeadEnd = Words(
        ". The tag finds it in a test and in a Maestro flow; the label is read by the screen reader; the rest comes " +
            "from the theme.",
        " alır. Test etiketi onu bir testte ve bir Maestro akışında bulur; görünen etiketi ekran okuyucu okur; " +
            "gerisi temadan gelir."
    )
    val OnceLeadShortStart = Words("A state, a label and a ", "Bir durum, bir etiket ve bir ")
    val OnceLeadShortEnd = Words(
        ". The tag finds it in a test; the label is read by the screen reader; the rest comes from the theme.",
        ". Test etiketi onu bir testte bulur; görünen etiketi ekran okuyucu okur; gerisi temadan gelir."
    )
    val OnceChecks = listOf(
        Words(
            "Text areas that grow, then scroll, with a count of what may still be typed",
            "Uzayan, sonra kayan, daha ne kadar yazılabileceğini sayan metin kutuları"
        ),
        Words("A tap outside puts the keyboard away on iOS", "iOS'ta dışarıya bir dokunuş klavyeyi kapatır"),
        Words(
            "Tags like TextArea_comment, never from visible text",
            "TextArea_comment gibi etiketler, görünen metinden asla"
        )
    )

    // What comes with it.
    val ProblemsKicker = Words("What comes with it", "Yanında gelenler")
    val ProblemsTitle = Words(
        "The problems every Compose Multiplatform app meets, solved once.",
        "Her Compose Multiplatform uygulamasının karşılaştığı sorunlar, bir kez çözülmüş."
    )
    val ProblemsTitleShort = Words(
        "What every Compose Multiplatform app meets, solved once.",
        "Her Compose Multiplatform uygulamasının karşılaştığı, bir kez çözülmüş."
    )
    val ReadTheGuides = Words("Read the guides", "Rehberleri oku")

    /** The landing page's cards of what comes with the library: a mark, a title, a line and the API it is. */
    val Features = listOf(
        Feature(
            "▦",
            Words("Adaptive, foldables too", "Uyarlanabilir, katlananlar da"),
            Words(
                "Window size classes, columns and page margins; list-detail, supporting pane and tabletop layouts " +
                    "that keep off the fold.",
                "Pencere boyut sınıfları, sütunlar ve sayfa kenar boşlukları; katlanma yerinden uzak duran " +
                    "liste-ayrıntı, destek paneli ve masa duruşu yerleşimleri."
            ),
            "PantrixListDetailScaffold",
            Route.Adaptive
        ),
        Feature(
            "ع",
            Words("Right to left, three languages", "Sağdan sola, üç dil"),
            Words(
                "Turkish, English and Arabic strings, layouts that mirror, digits and alphabetical order by language.",
                "Türkçe, İngilizce ve Arapça sözler, aynalanan yerleşimler, dile göre rakamlar ve alfabe sırası."
            ),
            "PantrixStrings",
            Route.Guide("text-order")
        ),
        Feature(
            "◎",
            Words("Accessible as built", "Kurulduğu gibi erişilebilir"),
            Words(
                "Focus rings for the keyboard, touch targets each platform asks for, modal layers and names for " +
                    "screen readers.",
                "Klavye için odak halkaları, her platformun istediği dokunma hedefleri, modal katmanlar ve ekran " +
                    "okuyucular için adlar."
            ),
            "PantrixTheme.platform",
            Route.Doc("platforms")
        ),
        Feature(
            "#",
            Words("Tags for every test", "Her test için etiketler"),
            Words(
                "Component_key tags from tagKey, finders and assertions in com.pantrixui:testing, and a Maestro flow " +
                    "for each page.",
                "tagKey'den Component_key etiketleri, com.pantrixui:testing'de bulucular ve doğrulamalar, her sayfa " +
                    "için bir Maestro akışı."
            ),
            "onPantrixNode(\"TextArea\", \"comment\")",
            Route.Guide("testing")
        ),
        Feature(
            "∿",
            Words("Charts that export", "Dışa aktarılan grafikler"),
            Words(
                "Twenty-one charts from line to Sankey, with zoom and a navigator; the drawing on screen is the PNG " +
                    "and the vector.",
                "Çizgiden Sankey'e yirmi bir grafik, yakınlaştırma ve gezinme şeridiyle; ekrandaki çizim PNG'nin ve " +
                    "vektörün kendisi."
            ),
            "PantrixLineChart",
            Route.Guide("chart-export")
        ),
        Feature(
            "31",
            Words("Dates without a date library", "Tarih kütüphanesi olmadan tarihler"),
            Words(
                "Date and time pickers, masked date and time fields, and formats that follow the language.",
                "Tarih ve saat seçiciler, maskeli tarih ve saat alanları ve dili izleyen biçimler."
            ),
            "PantrixDatePicker",
            Route.Guide("date-time")
        ),
        Feature(
            "M3",
            Words("A bridge to Material 3", "Material 3'e bir köprü"),
            Words(
                "Move a Material app a screen at a time: each theme gives its colours, type and shapes to the other.",
                "Bir Material uygulamasını ekran ekran taşı: her tema renklerini, yazısını ve şekillerini ötekine " +
                    "verir."
            ),
            "PantrixThemeFromMaterial",
            Route.Guide("material3")
        ),
        Feature(
            "!",
            Words("Diagnostics while it runs", "Çalışırken tanılama"),
            Words(
                "One tag on two nodes, colours that do not read, a squeezed touch target or a blank name, found on a " +
                    "running screen.",
                "İki düğümde tek etiket, okunmayan renkler, sıkışmış bir dokunma hedefi ya da boş bir ad; çalışan " +
                    "ekranda bulunur."
            ),
            "PantrixDiagnostics",
            Route.Guide("diagnostics")
        ),
        Feature(
            "kt",
            Words("Rules for detekt", "detekt kuralları"),
            Words(
                "The library’s own rules, and the ones an app keeps: no Material import, text through Pantrix’s " +
                    "wrappers.",
                "Kütüphanenin kendi kuralları ve bir uygulamanın tuttukları: Material importu yok, metin Pantrix'in " +
                    "sarmalayıcılarından."
            ),
            "com.pantrixui:detekt-rules",
            Route.Guide("detekt")
        )
    )

    // For coding assistants.
    val AssistantsKicker = Words("For coding assistants", "Kodlama asistanları için")
    val AssistantsTitle = Words(
        "Your assistant writes Pantrix, not Material.",
        "Asistanın Material değil, Pantrix yazar."
    )
    val AssistantsLeadStart = Words(
        "The documents are written from the library's own sources at every build. An assistant reads them through " +
            "the MCP server, the skill or ",
        "Belgeler her derlemede kütüphanenin kendi kaynaklarından yazılır. Bir asistan onları MCP sunucusundan, " +
            "beceriden ya da "
    )
    val AssistantsLeadEnd = Words(
        ", and every page here has a \"Copy page\" button for the rest.",
        " üzerinden okur; buradaki her sayfada geri kalanı için bir \"Sayfayı kopyala\" düğmesi var."
    )
    val AssistantsLeadPhoneStart = Words("The MCP server, the skill and ", "MCP sunucusu, beceri ve ")
    val AssistantsLeadPhoneEnd = Words(
        " are written from the library's own sources at every build.",
        " her derlemede kütüphanenin kendi kaynaklarından yazılır."
    )
    val AssistantsChips = listOf("MCP server", "Skill", "llms.txt", "pantrix-ui.json")
    val Terminal = Words("Terminal", "Uçbirim")
    val Asked = Words(
        "A comment box under each order, 280 characters at most",
        "Her siparişin altına en çok 280 karakterlik bir yorum kutusu"
    )
    val AskedShort = Words("A comment box, 280 characters at most", "En çok 280 karakterlik bir yorum kutusu")

    // Your brand.
    val BrandKicker = Words("Your brand", "Senin markan")
    val BrandTitleStart = Words("A brand's look in one ", "Bir markanın görünüşü tek bir ")
    val BrandTitleEnd = Words(".", " içinde.")
    val BrandLead = Words(
        "Colours for light and dark, the type scale, the shapes and any component's overrides. This site wears one " +
            "called Shopeers. Build your own in the theme builder and take it away as Kotlin.",
        "Açık ve koyu için renkler, yazı ölçeği, şekiller ve herhangi bir bileşenin özel ayarları. Bu site Shopeers " +
            "adında birini giyiyor. Kendininkini tema oluşturucuda kur, Kotlin olarak al."
    )
    val BrandLeadShort = Words(
        "Colours for light and dark, the type scale, the shapes and any component's overrides. This site wears one " +
            "called Shopeers.",
        "Açık ve koyu için renkler, yazı ölçeği, şekiller ve herhangi bir bileşenin özel ayarları. Bu site Shopeers " +
            "adında birini giyiyor."
    )
    val OpenThemeBuilder = Words("Open the theme builder", "Tema oluşturucuyu aç")
    val SeeTheTokens = Words("See the tokens", "Tokenlara bak")
    val Palette = Words("Shopeers blue, 50 to 700", "Shopeers mavisi, 50'den 700'e")

    // The call at the end.
    val CallTitle = Words("Start with one line in Gradle.", "Gradle'da tek satırla başla.")
    val CallLead = Words(
        "Android 11 and newer, iOS, the desktop's JVM and the browser's Wasm and JavaScript, from one source set.",
        "Android 11 ve üstü, iOS, masaüstünün JVM'i ve tarayıcının Wasm'ı ile JavaScript'i, tek bir kaynak kümesinden."
    )
    val CallLeadShort = Words(
        "Android 11 and newer, iOS, the desktop's JVM and the browser, from one source set.",
        "Android 11 ve üstü, iOS, masaüstünün JVM'i ve tarayıcı, tek bir kaynak kümesinden."
    )
    val SeeComponentPage = Words("See a component page", "Bir bileşen sayfasına bak")

    // The foot.
    val Tagline = Words(
        "A design system and adaptive UI library for Compose Multiplatform.",
        "Compose Multiplatform için bir tasarım sistemi ve uyarlanabilir arayüz kütüphanesi."
    )
}

/**
 * A card of what comes with the library.
 *
 * @property mark the sign in its corner.
 * @property title what it is.
 * @property body what it does.
 * @property api the API that is it.
 * @property route the page that tells more of it.
 */
internal class Feature(
    val mark: String,
    val title: Words,
    val body: Words,
    val api: String,
    val route: Route
)
