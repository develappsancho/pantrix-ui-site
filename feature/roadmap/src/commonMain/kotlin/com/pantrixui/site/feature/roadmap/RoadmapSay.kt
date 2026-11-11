package com.pantrixui.site.feature.roadmap

import com.pantrixui.site.core.designsystem.BySize
import com.pantrixui.site.core.model.LibraryFacts
import com.pantrixui.site.core.model.Route
import com.pantrixui.site.core.model.Words

/** The roadmap's words, in each language, and in each size where the design shortens them. */
internal object RoadmapSay {
    val Kicker = Words("Roadmap", "Yol haritası")
    val Title = Words("What is built, and what comes next.", "Neyin yapıldığı, sırada ne olduğu.")
    val Lead = BySize(
        Words(
            "Every item starts as a design proposal: what it is for, a sketch of its API, the options weighed and " +
                "the questions still open. Code follows once it is agreed. Each item names the version it comes in; " +
                "a planned version is a plan, not a promise.",
            "Her madde bir tasarım önerisiyle başlar: ne için olduğu, API'sinin bir taslağı, tartılan seçenekler " +
                "ve hâlâ açık sorular. Kod, öneri kabul edilince gelir. Her madde geleceği sürümü söyler; " +
                "planlanan bir sürüm bir plandır, söz değil."
        ),
        tablet = Words(
            "Every item starts as a design proposal; code follows once it is agreed. Each item names the version it " +
                "comes in.",
            "Her madde bir tasarım önerisiyle başlar; kod, öneri kabul edilince gelir. Her madde geleceği sürümü " +
                "söyler."
        ),
        phone = Words(
            "Every item starts as a design proposal, and names the version it comes in.",
            "Her madde bir tasarım önerisiyle başlar ve geleceği sürümü söyler."
        )
    )
    val Suggest = Words("Suggest an item", "Bir madde öner")

    /** Where an item is suggested: the library's issues. */
    const val ISSUES = "${LibraryFacts.REPOSITORY}/issues"

    val All = Words("All", "Tümü")

    /** The board's columns, a stage each, in the order a desktop and a tablet show them. */
    val Columns = listOf(
        Column(
            Stage.Shipped,
            Words("Shipped", "Yayımlandı"),
            Words(
                "From 0.1.0, the first release; the latest is ${LibraryFacts.RELEASE}.",
                "İlk sürüm 0.1.0'dan bu yana; en son sürüm ${LibraryFacts.RELEASE}."
            ),
            listOf(
                Item(
                    Area.Components,
                    BySize(Words("Components for documents and code", "Belgeler ve kod için bileşenler")),
                    BySize(
                        Words(
                            "A code editor, a lazy tree, keycaps, a graph, a toolbar, a status bar, a diff view and " +
                                "an empty state.",
                            "Bir kod editörü, tembel bir ağaç, tuş kapakları, bir çizge, bir araç çubuğu, bir durum " +
                                "çubuğu, bir fark görünümü ve boş bir durum."
                        ),
                        tablet = Words(
                            "A code editor, a lazy tree, a graph, a diff view and four more.",
                            "Bir kod editörü, tembel bir ağaç, bir çizge, bir fark görünümü ve dört bileşen daha."
                        ),
                        phone = Words(
                            "A code editor, a graph and six more.",
                            "Bir kod editörü, bir çizge ve altı bileşen daha."
                        )
                    ),
                    SECOND,
                    link = Link.Page(Route.Components)
                ),
                Item(
                    Area.Library,
                    BySize(
                        Words("Sharing, the clipboard and previews", "Paylaşım, pano ve önizlemeler"),
                        phone = Words("Sharing and previews", "Paylaşım ve önizlemeler")
                    ),
                    BySize(
                        Words(
                            "A share sheet, the clipboard’s text, a paste a node hears, and a theme that draws a " +
                                "preview as each platform would.",
                            "Bir paylaşım sayfası, panodaki metin, bir düğümün duyduğu yapıştırma ve bir önizlemeyi " +
                                "her platformun çizeceği gibi çizen bir tema."
                        ),
                        tablet = Words(
                            "A share sheet, the clipboard’s text and a theme for previews.",
                            "Bir paylaşım sayfası, panodaki metin ve önizlemeler için bir tema."
                        ),
                        phone = Words("A share sheet and a theme for previews.", "Paylaşım sayfası ve önizleme teması.")
                    ),
                    SECOND,
                    link = Link.Page(Route.Guide("sharing"))
                ),
                Item(
                    Area.Docs,
                    BySize(Words("This website", "Bu web sitesi")),
                    BySize(
                        Words(
                            "Built with Pantrix itself in Compose Multiplatform: the web, and desktop, Android and " +
                                "iOS apps from the same code. Its texts come from the assistants’ documents.",
                            "Pantrix'in kendisiyle, Compose Multiplatform'da yapıldı: web ile masaüstü, Android ve " +
                                "iOS uygulamaları aynı koddan. Metinleri asistanların belgelerinden gelir."
                        ),
                        tablet = Words(
                            "In Compose Multiplatform with Pantrix: the web, desktop, Android and iOS from the same " +
                                "code.",
                            "Compose Multiplatform'da Pantrix'le: web, masaüstü, Android ve iOS aynı koddan."
                        )
                    ),
                    Words("With 0.1.0", "0.1.0'la"),
                    link = Link.Page(Route.Home)
                ),
                Item(
                    Area.Tooling,
                    BySize(
                        Words(
                            "Documents for coding assistants and an MCP server",
                            "Kodlama asistanları için belgeler ve bir MCP sunucusu"
                        ),
                        tablet = Words(
                            "Documents for assistants and an MCP server",
                            "Asistanlar için belgeler ve bir MCP sunucusu"
                        )
                    ),
                    BySize(
                        Words(
                            "llms.txt, a skill, pantrix-ui.json and its schema, and a server of twelve tools.",
                            "llms.txt, bir beceri, pantrix-ui.json ve şeması, on iki araçlı bir sunucu."
                        ),
                        tablet = Words(
                            "llms.txt, a skill, pantrix-ui.json and twelve tools.",
                            "llms.txt, bir beceri, pantrix-ui.json ve on iki araç."
                        ),
                        phone = Words("llms.txt, a skill and twelve tools.", "llms.txt, bir beceri ve on iki araç.")
                    ),
                    FIRST,
                    link = Link.Page(Route.Ai)
                ),
                Item(
                    Area.Library,
                    BySize(Words("A bridge to Material 3", "Material 3'e bir köprü")),
                    BySize(
                        Words(
                            "Each theme gives its colours, type and shapes to the other, both ways.",
                            "Her tema renklerini, yazısını ve şekillerini ötekine verir, iki yönde."
                        ),
                        tablet = Words(
                            "Each theme gives its colours, type and shapes to the other.",
                            "Her tema renklerini, yazısını ve şekillerini ötekine verir."
                        ),
                        phone = Words("Both themes, both ways.", "İki tema, iki yönde.")
                    ),
                    FIRST
                ),
                Item(
                    Area.Docs,
                    BySize(Words("An analytics sample", "Bir analitik örneği")),
                    BySize(
                        Words(
                            "The components’ events connected to an analytics product, with a tracking plan.",
                            "Bileşenlerin olayları bir analitik ürününe bağlı, bir izleme planıyla."
                        ),
                        tablet = Words(
                            "The components’ events connected to a product.",
                            "Bileşenlerin olayları bir ürüne bağlı."
                        ),
                        phone = Words("Events connected to a product.", "Olaylar bir ürüne bağlı.")
                    ),
                    FIRST
                ),
                Item(
                    Area.Library,
                    BySize(Words("Test helpers", "Test yardımcıları")),
                    BySize(
                        Words(
                            "com.pantrixui:testing: finding by tag, PantrixTestTheme and doubles of the platform’s " +
                                "services.",
                            "com.pantrixui:testing: etikete göre bulmak, PantrixTestTheme ve platformun " +
                                "servislerinin " +
                                "dublörleri."
                        ),
                        tablet = Words(
                            "Finding by tag, PantrixTestTheme, doubles of services.",
                            "Etikete göre bulmak, PantrixTestTheme, servislerin dublörleri."
                        ),
                        phone = Words("Finding by tag, PantrixTestTheme.", "Etikete göre bulmak, PantrixTestTheme.")
                    ),
                    FIRST
                ),
                Item(
                    Area.Library,
                    BySize(Words("Diagnostics", "Tanılama")),
                    BySize(
                        Words(
                            "Duplicate tags, unreadable colours, squeezed targets and blank names, on a running " +
                                "screen.",
                            "Çalışan bir ekranda yinelenen etiketler, okunmayan renkler, sıkışmış hedefler ve boş " +
                                "adlar."
                        ),
                        tablet = Words(
                            "What only a running screen shows.",
                            "Yalnızca çalışan bir ekranın gösterdikleri."
                        )
                    ),
                    FIRST
                ),
                Item(
                    Area.Tooling,
                    BySize(Words("Rules for detekt", "detekt kuralları")),
                    BySize(
                        Words(
                            "The library’s rules, and the ones an app keeps, published as com.pantrixui:detekt-rules.",
                            "Kütüphanenin kuralları ve bir uygulamanın tuttukları, com.pantrixui:detekt-rules olarak."
                        ),
                        tablet = Words(
                            "The library’s rules, and an app’s.",
                            "Kütüphanenin kuralları ve bir uygulamanınkiler."
                        )
                    ),
                    FIRST
                ),
                Item(
                    Area.Docs,
                    BySize(
                        Words("Changelog, contributing and security", "Değişiklik günlüğü, katkı ve güvenlik"),
                        tablet = Words("Changelog, contributing, security", "Değişiklik günlüğü, katkı, güvenlik")
                    ),
                    BySize(
                        Words(
                            "How a change comes in, what it carries, and how to report a vulnerability.",
                            "Bir değişikliğin nasıl geldiği, ne taşıdığı ve bir açığın nasıl bildirildiği."
                        ),
                        tablet = Words(
                            "How a change comes in and is checked.",
                            "Bir değişikliğin nasıl geldiği ve denetlendiği."
                        ),
                        phone = Words("How a change comes in.", "Bir değişikliğin nasıl geldiği.")
                    ),
                    FIRST
                )
            )
        ),
        Column(
            Stage.Building,
            Words("Building", "Yapılıyor"),
            Words("Being made now.", "Şu an yapılıyor."),
            listOf(
                Item(
                    Area.Native,
                    BySize(Words("Pantrix for Jetpack Compose", "Jetpack Compose için Pantrix")),
                    BySize(
                        Words(
                            "For a native Android project: the same library, on Jetpack Compose.",
                            "Yerel bir Android projesi için: aynı kütüphane, Jetpack Compose üzerinde."
                        ),
                        phone = Words(
                            "For a native Android project, on Jetpack Compose.",
                            "Yerel bir Android projesi için, Jetpack Compose üzerinde."
                        )
                    ),
                    OWN_FIRST,
                    link = Link.Section(NATIVE),
                    progress = Progress(ANDROID_PROGRESS, Words("3 of 12 steps", "12 adımdan 3'ü"))
                )
            )
        ),
        Column(
            Stage.Next,
            Words("Next", "Sırada"),
            Words(
                "Planned for ${LibraryFacts.PLANNED_VERSION}, waiting to start.",
                "${LibraryFacts.PLANNED_VERSION} için planlandı, başlamayı bekliyor."
            ),
            listOf(
                Item(
                    Area.Library,
                    BySize(Words("Server-driven UI", "Sunucu güdümlü UI")),
                    BySize(
                        Words(
                            "Screens a server describes, checked and drawn with Pantrix’s components.",
                            "Bir sunucunun tarif ettiği ekranlar, Pantrix'in bileşenleriyle denetlenip çizilir."
                        ),
                        tablet = Words(
                            "Screens a server describes, checked and drawn with Pantrix.",
                            "Bir sunucunun tarif ettiği ekranlar, Pantrix'le denetlenip çizilir."
                        )
                    ),
                    Words("${LibraryFacts.PLANNED_VERSION} · planned", "${LibraryFacts.PLANNED_VERSION} · planlandı"),
                    link = Link.Section(SDUI),
                    progress = Progress(0, Words("0 of 7 steps", "7 adımdan 0'ı"))
                )
            )
        ),
        Column(
            Stage.Later,
            Words("Later", "Sonra"),
            Words(
                "Decided, or being weighed; no version number yet.",
                "Karar verildi ya da tartılıyor; henüz sürüm yok."
            ),
            listOf(
                Item(
                    Area.Native,
                    BySize(Words("Pantrix for SwiftUI", "SwiftUI için Pantrix")),
                    BySize(
                        Words(
                            "For a native iOS project: the same library, written in Swift as SwiftUI views.",
                            "Yerel bir iOS projesi için: aynı kütüphane, Swift'te SwiftUI görünümleri olarak."
                        ),
                        tablet = Words(
                            "For a native iOS project: the same library, in Swift and SwiftUI.",
                            "Yerel bir iOS projesi için: aynı kütüphane, Swift ve SwiftUI'da."
                        ),
                        phone = Words(
                            "For a native iOS project, in Swift and SwiftUI.",
                            "Yerel bir iOS projesi için, Swift ve SwiftUI'da."
                        )
                    ),
                    LATER,
                    link = Link.Section(NATIVE)
                ),
                Item(
                    Area.Tooling,
                    BySize(Words("Design tokens as JSON", "JSON olarak tasarım tokenları")),
                    BySize(
                        Words(
                            "One file a brand’s look is written in, with Kotlin, Swift and CSS made from it.",
                            "Bir markanın görünüşünün yazıldığı tek dosya; Kotlin, Swift ve CSS ondan üretilir."
                        ),
                        tablet = Words(
                            "One file, with Kotlin, Swift and CSS made from it.",
                            "Tek dosya; Kotlin, Swift ve CSS ondan üretilir."
                        ),
                        phone = Words("Kotlin, Swift and CSS from one file.", "Tek dosyadan Kotlin, Swift ve CSS.")
                    ),
                    WEIGHED
                ),
                Item(
                    Area.Components,
                    BySize(Words("New components", "Yeni bileşenler")),
                    BySize(
                        Words(
                            "Slider, Stepper, a linear progress, FAB, Drawer, KeyValue.",
                            "Slider, Stepper, doğrusal bir ilerleme, FAB, Drawer, KeyValue."
                        ),
                        tablet = Words(
                            "Slider, Stepper, a linear progress, FAB, Drawer…",
                            "Slider, Stepper, doğrusal ilerleme, FAB, Drawer…"
                        )
                    ),
                    WEIGHED
                ),
                Item(
                    Area.Tooling,
                    BySize(Words("A BOM and benchmarks", "Bir BOM ve ölçümler")),
                    BySize(
                        Words(
                            "One version for every artifact, and measured performance.",
                            "Her yayın için tek sürüm ve ölçülmüş başarım."
                        ),
                        tablet = Words(
                            "One version for every artifact, and measured speed.",
                            "Her yayın için tek sürüm ve ölçülmüş hız."
                        )
                    ),
                    WEIGHED
                )
            )
        )
    )

    /** What each area of the library is called on an item's tag. */
    fun areaName(area: Area): Words = when (area) {
        Area.Library -> Words("Library", "Kütüphane")
        Area.Tooling -> Words("Tooling", "Araçlar")
        Area.Docs -> Words("Docs", "Belgeler")
        Area.Native -> Words("Native", "Yerel")
        Area.Components -> Words("Components", "Bileşenler")
    }

    // The versions.
    val VersionsTitle = Words("Versions", "Sürümler")
    val Versions = listOf(
        Version(
            Stage.Shipped,
            Words("0.1.0 · the first release", "0.1.0 · ilk sürüm"),
            Words("The whole library", "Bütün kütüphane"),
            Words(
                "${LibraryFacts.COMPONENTS} components on six targets, adaptive layouts and folds, three languages, " +
                    "the " +
                    "test helpers, diagnostics, the detekt rules, the Material 3 bridge and the documents for " +
                    "assistants. This website comes with it.",
                "Altı hedefte ${LibraryFacts.COMPONENTS} bileşen, uyarlanabilir düzenler ve katlanan ekranlar, üç " +
                    "dil, " +
                    "test yardımcıları, tanılama, detekt kuralları, Material 3 köprüsü ve asistanlar için belgeler. " +
                    "Bu web sitesi de onunla gelir."
            )
        ),
        Version(
            Stage.Shipped,
            Words("0.1.1 · fixes", "0.1.1 · düzeltmeler"),
            Words("Xcode 27.2 and the page’s language", "Xcode 27.2 ve sayfanın dili"),
            Words(
                "The shared iOS code builds with Xcode 27.2, a web page tells a screen reader the app’s language, " +
                    "and the klibs no longer carry the paths of the machine that built them.",
                "Ortak iOS kodu Xcode 27.2'yle derlenir, bir web sayfası ekran okuyucuya uygulamanın dilini söyler " +
                    "ve klib'ler onları derleyen makinenin yollarını artık taşımaz."
            )
        ),
        Version(
            Stage.Shipped,
            Words("0.2.0 · documents and code", "0.2.0 · belgeler ve kod"),
            Words("Eight components and their tools", "Sekiz bileşen ve araçları"),
            Words(
                "A code editor, a lazy tree, keycaps, a graph, a toolbar, a status bar, a diff view and an empty " +
                    "state; a share sheet, the clipboard's text, a theme for previews and a page in the middle.",
                "Bir kod editörü, tembel bir ağaç, tuş kapakları, bir çizge, bir araç çubuğu, bir durum çubuğu, bir " +
                    "fark görünümü ve boş bir durum; bir paylaşım sayfası, panodaki metin, önizlemeler için bir tema " +
                    "ve ortada duran bir sayfa."
            )
        ),
        Version(
            Stage.Shipped,
            Words("0.2.1 · fixes", "0.2.1 · düzeltmeler"),
            Words("A far drag of the scroll bar", "Kaydırma çubuğunda uzun bir sürükleme"),
            Words(
                "A long list goes straight where a drag of its scroll thumb asks, and the assistants’ documents " +
                    "carry the code editor’s examples whole.",
                "Uzun bir liste, kaydırma çubuğunun sürüklendiği yere doğrudan gider; asistanların belgeleri kod " +
                    "editörünün örneklerini bütün taşır."
            )
        ),
        Version(
            Stage.Building,
            Words("Jetpack Compose · its own 0.1.0", "Jetpack Compose · kendi 0.1.0'ı"),
            Words("The library for native Android", "Yerel Android için kütüphane"),
            Words(
                "The same library for an Android-only app, on Jetpack Compose, in a project of its own and with " +
                    "versions of its own: its skeleton, its foundation and the first of the components’ six stages " +
                    "are done.",
                "Yalnızca Android'de çalışan bir uygulama için aynı kütüphane, Jetpack Compose üzerinde, kendi " +
                    "projesinde ve kendi sürümleriyle: iskeleti, temeli ve bileşenlerin altı aşamasından ilki yapıldı."
            )
        ),
        Version(
            Stage.Next,
            Words("${LibraryFacts.PLANNED_VERSION} · planned", "${LibraryFacts.PLANNED_VERSION} · planlandı"),
            Words("Server-driven UI", "Sunucu güdümlü UI"),
            Words(
                "Screens a server describes, checked and drawn with Pantrix’s components.",
                "Bir sunucunun tarif ettiği ekranlar, Pantrix'in bileşenleriyle denetlenip çizilir."
            )
        ),
        Version(
            Stage.Later,
            LATER,
            Words("SwiftUI", "SwiftUI"),
            Words(
                "The library for iOS alone, in Swift and in a project of its own; design tokens as JSON are weighed " +
                    "with it.",
                "Yalnızca iOS için kütüphane, Swift'te ve kendi projesinde; JSON olarak tasarım tokenları onunla " +
                    "birlikte tartılıyor."
            )
        )
    )
}

/** Where an item stands: what its column is and the colour of its dot. */
enum class Stage { Shipped, Building, Next, Later }

/** The part of the library an item is about, which says its tag's colours. */
internal enum class Area { Library, Tooling, Docs, Native, Components }

/**
 * A column of the board: the items at one [stage].
 *
 * @property stage where its items stand.
 * @property name what it is called.
 * @property note what a desktop says under its name.
 * @property items its items, in order.
 */
internal class Column(
    val stage: Stage,
    val name: Words,
    val note: Words,
    val items: List<Item>
)

/**
 * An item of the board.
 *
 * @property area the part of the library it is about.
 * @property title what it is.
 * @property body what it does.
 * @property version the version it comes in.
 * @property link where its card leads, if anywhere.
 * @property progress how far it has come, for one being made or about to be.
 */
internal class Item(
    val area: Area,
    val title: BySize<Words>,
    val body: BySize<Words>,
    val version: Words,
    val link: Link? = null,
    val progress: Progress? = null
)

/** Where an item's card leads. */
internal sealed interface Link {
    /** A page of the site. */
    data class Page(val route: Route) : Link

    /** A part of this page, by its anchor. */
    data class Section(val anchor: String) : Link
}

/**
 * How far an item has come.
 *
 * @property percent the share done, out of a hundred.
 * @property label what is done and what is next.
 */
internal class Progress(
    val percent: Int,
    val label: Words
)

/**
 * A version on the timeline.
 *
 * @property stage where it stands, which says the colour of its dot.
 * @property name the version and what it is.
 * @property title what it brings.
 * @property body what is in it.
 */
internal class Version(
    val stage: Stage,
    val name: Words,
    val title: Words,
    val body: Words
)

/** The first release. */
private val FIRST = Words("0.1.0", "0.1.0")

/** The release of the components for documents and code. */
private val SECOND = Words("0.2.0", "0.2.0")

/** The first version of a library of its own, with numbers of its own, as the native libraries have. */
private val OWN_FIRST = Words("Its own 0.1.0", "Kendi 0.1.0'ı")

/** An item decided on, with no version yet. */
private val LATER = Words("Later", "Sonra")

/** An item being weighed: out of the plan until the owner asks for it (the library's ROADMAP.md, part 14). */
private val WEIGHED = Words("Considered", "Tartılıyor")

/**
 * How far the library for Jetpack Compose has come, out of a hundred: three of its plan's twelve steps (the library's
 * ROADMAP.md, part 12), its skeleton, its foundation and the first of the components’ six stages.
 */
private const val ANDROID_PROGRESS = 25

/** The part of the page about server-driven UI. */
internal const val SDUI = "sdui"

/** The part of the page about the native libraries. */
internal const val NATIVE = "native"
