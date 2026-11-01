package com.pantrixui.site.feature.adaptive

import com.pantrixui.site.core.designsystem.device.SimDevice
import com.pantrixui.site.core.model.Words

/** The adaptive page's words, in each language. */
internal object AdaptiveSay {
    val Kicker = Words("Adaptive UI", "Uyarlanabilir arayüz")
    val Title = Words("One screen, every window. Folds too.", "Tek ekran, her pencere. Katlananlar da.")
    val Lead = Words(
        "Pantrix reads the window, not the device: its width class, its height, a foldable's posture and the " +
            "pointer. The scaffolds move the navigation, split the panes and meet at the fold by themselves. Pick a " +
            "window below and watch one app lay itself out.",
        "Pantrix cihazı değil pencereyi okur: genişlik sınıfını, yüksekliğini, katlanan bir cihazın duruşunu ve " +
            "işaretçiyi. İskeletler gezinmeyi taşır, bölmeleri ayırır ve katlanma yerinde kendiliğinden buluşur. " +
            "Aşağıdan bir pencere seç ve bir uygulamanın kendini nasıl yerleştirdiğine bak."
    )
    val LeadShort = Words(
        "Pantrix reads the window, not the device. Pick a window and watch one app lay itself out.",
        "Pantrix cihazı değil pencereyi okur. Bir pencere seç ve bir uygulamanın kendini yerleştirişine bak."
    )

    // The simulator.
    val Inbox = Words("Inbox", "Gelen kutusu")
    val VideoCall = Words("Video call", "Görüntülü arama")
    val WhyItLooks = Words("Why it looks like this: ", "Neden böyle görünüyor: ")

    /** What a window of the simulator is called. */
    fun device(device: SimDevice): Words = when (device) {
        SimDevice.Phone -> Words("Phone", "Telefon")
        SimDevice.PhoneSide -> Words("Phone on its side", "Yan duran telefon")
        SimDevice.Book -> Words("Foldable, book", "Katlanan, kitap")
        SimDevice.Table -> Words("Foldable, table", "Katlanan, masa")
        SimDevice.Tablet -> Words("Tablet", "Tablet")
        SimDevice.Laptop -> Words("Laptop", "Dizüstü")
        SimDevice.Desktop -> Words("Wide desktop", "Geniş masaüstü")
    }

    // The inbox the simulator draws.
    val Navigation = listOf(
        Words("Inbox", "Gelenler"),
        Words("Orders", "Siparişler"),
        Words("People", "Kişiler"),
        Words("Settings", "Ayarlar")
    )
    val ShopAdmin = Words("Shop admin", "Mağaza yönetimi")
    val History = Words("History", "Geçmiş")
    val TrackTheParcel = Words("Track the parcel", "Kargoyu izle")
    val Reply = Words("Reply", "Yanıtla")
    val Shipped = Words("Shipped", "Kargoya verildi")
    val Packed = Words("Packed", "Paketlendi")
    val Paid = Words("Paid", "Ödendi")
    val Today = Words("Today, 09:24", "Bugün, 09:24")
    val YesterdayLate = Words("Yesterday, 17:02", "Dün, 17:02")
    val YesterdayEarly = Words("Yesterday, 16:40", "Dün, 16:40")
    val ChooseAMessage = Words("Choose a message", "Bir ileti seç")

    /** The inbox's messages: who from, when, what about and what they say. */
    val Mail = listOf(
        Mail(
            Words("Shop", "Mağaza"),
            Words("09:24", "09:24"),
            Words("Order PX-2048 has shipped", "PX-2048 siparişi kargoya verildi"),
            Words(
                "Your parcel left the warehouse this morning and reaches you on Thursday. The courier will ring " +
                    "between 10:00 and 14:00; if nobody answers, it goes to the neighbour you named.",
                "Paketin bu sabah depodan çıktı ve perşembe sana ulaşıyor. Kurye 10:00 ile 14:00 arasında zili " +
                    "çalacak; kimse açmazsa adını verdiğin komşuya bırakacak."
            ),
            0xFF2264FF
        ),
        Mail(
            Words("Returns", "İadeler"),
            Words("Yesterday", "Dün"),
            Words("Your return is approved", "İaden onaylandı"),
            Words(
                "We have your return. The refund reaches the card you paid with within five working days.",
                "İaden bize ulaştı. Para, ödediğin karta beş iş günü içinde döner."
            ),
            0xFF1A8556
        ),
        Mail(
            Words("Billing", "Fatura"),
            Words("Mon", "Pzt"),
            Words("Invoice for October", "Ekim faturası"),
            Words(
                "Your invoice for October is ready. It lists 14 orders and the delivery costs of each.",
                "Ekim faturan hazır. 14 siparişi ve her birinin teslimat ücretini listeliyor."
            ),
            0xFFB95D15
        ),
        Mail(
            Words("Security", "Güvenlik"),
            Words("Sun", "Paz"),
            Words("A new sign-in on a Mac", "Bir Mac'ten yeni giriş"),
            Words(
                "Someone signed in to your shop on a Mac. If it was you, there is nothing to do.",
                "Birisi mağazana bir Mac'ten giriş yaptı. Sen idiysen yapacak bir şey yok."
            ),
            0xFFC42A55
        ),
        Mail(
            Words("Analytics", "Analiz"),
            Words("Sat", "Cmt"),
            Words("Your weekly report", "Haftalık raporun"),
            Words(
                "Visits rose this week; the comment box under each order was used 412 times.",
                "Bu hafta ziyaretler arttı; her siparişin altındaki yorum kutusu 412 kez kullanıldı."
            ),
            0xFF264795
        ),
        Mail(
            Words("Support", "Destek"),
            Words("Fri", "Cum"),
            Words("How did we do?", "Nasıldık?"),
            Words(
                "Tell us how your last conversation with our support went.",
                "Destekle son konuşmanın nasıl geçtiğini anlat."
            ),
            0xFF63647B
        )
    )

    // The video call.
    val Meeting = Words("Design review · 12:48", "Tasarım değerlendirmesi · 12:48")
    val CallButtons = listOf(
        Words("Mute", "Sessiz"),
        Words("Camera", "Kamera"),
        Words("Share", "Paylaş"),
        Words("Leave", "Ayrıl")
    )

    // Why the simulator's app looks as it does.
    val WhyVideoTable = Words(
        "On a table, PantrixTabletopLayout puts the picture above the fold and the controls, drawn large, below it.",
        "Masa duruşunda PantrixTabletopLayout görüntüyü katlanma yerinin üstüne, büyük çizilen denetimleri " +
            "altına koyar."
    )
    val WhyVideoBook = Words(
        "Held like a book, the picture is on the start side of the fold and the controls on the other.",
        "Kitap gibi tutulunca görüntü katlanma yerinin baş tarafında, denetimler öbür taraftadır."
    )
    val WhyVideoFlat = Words(
        "Flat, the picture takes the whole window and the controls ride over its bottom: flatControls = Overlay.",
        "Düz dururken görüntü bütün pencereyi alır, denetimler altının üstünde durur: flatControls = Overlay."
    )
    val WhyInboxTable = Words(
        "On a table the scaffold keeps the navigation at the bottom, where the hands are. For content and controls, " +
            "switch the app to Video call.",
        "Masa duruşunda iskelet gezinmeyi altta, ellerin olduğu yerde tutar. İçerik ve denetimler için uygulamayı " +
            "Görüntülü aramaya çevir."
    )
    val WhyInboxBook = Words(
        "Held like a book, the list and the detail make two panes at every width and part exactly at the fold; its " +
            "strip stays empty.",
        "Kitap gibi tutulunca liste ve ayrıntı her genişlikte iki bölme olur ve tam katlanma yerinde ayrılır; " +
            "şeridi boş kalır."
    )

    /** Why a flat inbox looks as it does: its room beside the navigation, the class of it, and the panes that fit. */
    fun whyInbox(
        room: Int,
        navigation: Words,
        boxClass: String,
        panes: Int,
        short: Boolean
    ): Words = Words(
        "The list and detail scaffold measures its own box, not the window: ${room}dp beside the " +
            "${navigation.english} is $boxClass, so $panes ${if (panes == 1) "pane" else "panes"}." +
            (if (panes == 1) " Open a message: it slides in over the list, and back brings the list again." else "") +
            (if (short) " The height is Compact: no bar at the bottom." else ""),
        "Liste ve ayrıntı iskeleti pencereyi değil kendi kutusunu ölçer: ${navigation.turkish} yanındaki ${room}dp " +
            "$boxClass, bu yüzden $panes bölme." +
            (if (panes == 1) " Bir ileti aç: listenin üstüne kayar, geri hareketi listeyi getirir." else "") +
            (if (short) " Yükseklik Compact: altta çubuk yok." else "")
    )

    val BottomBar = Words("bottom bar", "alt çubuğun")
    val Sidebar = Words("sidebar", "kenar çubuğunun")
    val Rail = Words("rail", "rayın")

    // The classes.
    val ClassesTitle = Words("Five widths, three heights", "Beş genişlik, üç yükseklik")
    val ClassesLead = Words(
        "The breakpoints are Android's window size classes, the same on every platform. What is measured is the " +
            "window: an app in a split screen, a resized desktop window or a browser tab is as wide as that window. " +
            "The marker is the window chosen above.",
        "Sınırlar Android'in pencere boyut sınıflarıdır, her platformda aynı. Ölçülen pencerenin kendisidir: " +
            "bölünmüş ekrandaki bir uygulama, boyutu değişmiş bir masaüstü penceresi ya da bir tarayıcı sekmesi o " +
            "pencere kadar geniştir. İşaret, yukarıda seçilen penceredir."
    )
    val Under = Words("under", "altı")
    val AndUp = Words("and up", "ve üstü")
    val Heights = listOf(
        Triple(
            Words("Height Compact", "Yükseklik Compact"),
            Words("under 480dp", "480dp altı"),
            Words(
                "A phone on its side: no bar at the bottom, the main action in the header.",
                "Yan duran telefon: altta çubuk yok, asıl eylem başlıkta."
            )
        ),
        Triple(
            Words("Height Medium", "Yükseklik Medium"),
            Words("480–899dp", "480–899dp"),
            Words("A phone upright, a tablet on its side.", "Dik duran telefon, yan duran tablet.")
        ),
        Triple(
            Words("Height Expanded", "Yükseklik Expanded"),
            Words("900dp and up", "900dp ve üstü"),
            Words("A tablet upright, a desktop window.", "Dik duran tablet, bir masaüstü penceresi.")
        )
    )

    // The window in code.
    val CodeTitle = Words("The window comes with the theme", "Pencere temayla gelir")
    val CodeLeadStart = Words("No wrapper, no setup. Every screen under ", "Sarmalayıcı yok, kurulum yok. ")
    val CodeLeadEnd = Words(
        " finds the window ready, with the page's margin and gutter for its class.",
        " altındaki her ekran pencereyi, sınıfına göre sayfa kenar boşluğu ve aralığıyla hazır bulur."
    )
    val WhichHelper = Words("Which helper, when", "Hangi yardımcı, ne zaman")
    val YouNeed = Words("You need", "Gereken")
    val Write = Words("Write", "Yaz")
    val Helpers = listOf(
        Words("A value by width", "Genişliğe göre bir değer") to "window.byWidth(compact = 2, medium = 4)",
        Words("The same in a composable", "Aynısı bir composable'da") to
            "responsive(compact = 1, medium = 2, large = 3)",
        Words("The page’s margin and gutter", "Sayfanın kenar boşluğu ve aralığı") to
            "PantrixTheme.layout.margin / .gutter",
        Words("A value by height", "Yüksekliğe göre bir değer") to "window.byHeight(compact = 44.dp, medium = 56.dp)",
        Words("A value by posture", "Duruşa göre bir değer") to "window.byPosture(flat = 1, book = 2, tabletop = 2)",
        Words("\"The phone layout?\"", "\"Telefon düzeni mi?\"") to "window.isNarrow",
        Words("\"A bar fits at the bottom?\"", "\"Altta çubuk sığar mı?\"") to "window.isShort",
        Words("From this class on", "Bu sınıftan itibaren") to "widthClass >= WindowWidthClass.Expanded",
        Words("A threshold of your own", "Kendi eşiğin") to "rememberWindowIsAtLeast(1000.dp)",
        Words("The component’s own room", "Bileşenin kendi yeri") to "BoxWithPantrixWindow { box -> … }",
        Words("A finger or a mouse", "Parmak mı fare mi") to "PantrixTheme.platform.isTouch"
    )

    // The patterns.
    val PatternsTitle = Words("Six patterns, ready made", "Altı hazır kalıp")
    val PatternsLead = Words(
        "Each is a component that measures its own room, keeps its state while it moves, and knows the fold.",
        "Her biri kendi yerini ölçen, yer değiştirirken durumunu koruyan ve katlanma yerini bilen bir bileşen."
    )
    val Patterns = listOf(
        Pattern(
            "1 · PantrixScaffold",
            Words("Navigation that moves with the width", "Genişlikle yer değiştiren gezinme"),
            Words(
                "Along the bottom while the scaffold is narrower than 600dp, along the start edge from there on. " +
                    "On a foldable set on a table it stays at the bottom, where the hands are. The scaffold measures " +
                    "its own width, not the window's.",
                "İskelet 600dp'den darken altta, oradan sonra baş kenarda. Masaya konmuş katlanan bir cihazda " +
                    "ellerin olduğu yerde, altta kalır. İskelet pencerenin değil kendi genişliğini ölçer."
            ),
            "scaffold",
            listOf(
                Words("Bottom bar", "Alt çubuk"),
                Words("Rail, from 600dp", "Ray, 600dp'den"),
                Words("Sidebar, your choice", "Kenar çubuğu, senin seçimin")
            )
        ),
        Pattern(
            "2 · PantrixListDetailScaffold",
            Words("List and detail", "Liste ve ayrıntı"),
            Words(
                "An inbox, settings, contacts. In one pane the detail slides in over the list and the back gesture " +
                    "brings it back, Android's predictive back and iOS's edge swipe included. The focus follows the " +
                    "pane, and the list's width can be dragged.",
                "Bir gelen kutusu, ayarlar, kişiler. Tek bölmede ayrıntı listenin üstüne kayar ve geri hareketi " +
                    "listeyi getirir; Android'in öngörülü geri hareketi ve iOS'un kenar kaydırması da. Odak bölmeyi " +
                    "izler, listenin genişliği sürüklenebilir."
            ),
            "list-detail",
            listOf(
                Words("One pane", "Tek bölme"),
                Words("Two, from Expanded", "İki, Expanded'dan"),
                Words("Three, from Large", "Üç, Large'dan")
            )
        ),
        Pattern(
            "3 · PantrixSupportingPaneScaffold",
            Words("Main content and a supporting pane", "Ana içerik ve bir destek bölmesi"),
            Words(
                "A document and its comments, a chart and its filters. Beside the content where there is room; in a " +
                    "narrow room a sheet from the bottom on a phone and from the end on a tablet, or the content's " +
                    "own place. A comment typed stays while the pane moves.",
                "Bir belge ve yorumları, bir grafik ve süzgeçleri. Yer varsa içeriğin yanında; dar yerde telefonda " +
                    "alttan, tablette sondan bir sayfa ya da içeriğin kendi yeri. Yazılan bir yorum bölme yer " +
                    "değiştirirken kalır."
            ),
            "supporting",
            listOf(
                Words("A sheet, when narrow", "Darken bir sayfa"),
                Words("Beside, from Expanded", "Yanında, Expanded'dan")
            )
        ),
        Pattern(
            "4 · PantrixTwoPane · PantrixTabletopLayout",
            Words("Folds: two panes and the tabletop", "Katlanma: iki bölme ve masa duruşu"),
            Words(
                "Two panes part exactly at a hinge that separates them, and the hinge's strip stays empty. On a " +
                    "table, what is looked at fills the half above the fold and the controls take the half below. " +
                    "Choose a foldable in the preview above to see both.",
                "İki bölme onları ayıran menteşede tam ayrılır, menteşenin şeridi boş kalır. Masada bakılan şey " +
                    "katlanma yerinin üstündeki yarıyı doldurur, denetimler alttaki yarıyı alır. İkisini görmek için " +
                    "yukarıdaki önizlemede katlanan bir cihaz seç."
            ),
            "tabletop",
            listOf(
                Words("Book: panes meet at the fold", "Kitap: bölmeler katlanma yerinde buluşur"),
                Words("Table: look above, touch below", "Masa: üstte bak, altta dokun")
            )
        ),
        Pattern(
            "5 · PantrixTheme.layout",
            Words("Columns, margins and the content's width", "Sütunlar, kenar boşlukları ve içeriğin genişliği"),
            Words(
                "Margins of 16, 24 and 32dp, gutters of 16 and 24dp, and 4, 8 or 12 columns come with the class. A " +
                    "part spans columns; nothing is chosen by hand.",
                "16, 24 ve 32dp kenar boşlukları, 16 ve 24dp aralıklar ve 4, 8 ya da 12 sütun sınıfla gelir. Bir " +
                    "parça sütunlara yayılır; hiçbir şey elle seçilmez."
            ),
            "columns",
            emptyList()
        ),
        Pattern(
            "6 · BoxWithPantrixWindow",
            Words("A component asks its own room", "Bir bileşen kendi yerini sorar"),
            Words(
                "The window is for decisions about the page. A component inside a narrow panel would draw its wide " +
                    "layout if it asked the window: it asks the box it is given instead, with the window's " +
                    "breakpoints.",
                "Pencere sayfayla ilgili kararlar içindir. Dar bir paneldeki bileşen pencereye sorsa geniş düzenini " +
                    "çizerdi: bunun yerine kendisine verilen kutuya, pencerenin sınırlarıyla sorar."
            ),
            "own-box",
            emptyList()
        )
    )
    val ColumnsNote = Words("From Large the content keeps to 1040dp: ", "Large'dan itibaren içerik 1040dp'de kalır: ")
    val InTheMainArea = Words("In the main area: a table", "Ana alanda: bir tablo")
    val InASidePanel = Words("In a side panel: a list", "Yan panelde: bir liste")
    val TableHeads =
        listOf(Words("Order", "Sipariş"), Words("Date", "Tarih"), Words("Status", "Durum"), Words("Total", "Toplam"))
    val Open = Words("Open", "Açık")

    // Testing and pitfalls.
    val FrameTitle = Words("A fold in a preview and a test", "Önizlemede ve testte bir katlanma yeri")
    val FrameLeadEnd = Words(
        " makes a box a window of its own, with a hinge measured from its corner. The demo's cards draw the iPhone " +
            "Duo's inner screen this way, as a book and on a table.",
        " bir kutuyu, köşesinden ölçülen bir menteşesiyle kendi başına bir pencere yapar. Demonun kartları iPhone " +
            "Duo'nun iç ekranını böyle, kitap gibi ve masada çizer."
    )
    val PitfallsTitle = Words("Pitfalls", "Tuzaklar")
    val NotThis = Words("Not this", "Böyle değil")
    val ButThis = Words("But this", "Şöyle")
    val Pitfalls = listOf(
        Words("Calling the same content from two places by class", "Aynı içeriği sınıfa göre iki yerden çağırmak") to
            Words(
                "Call it from one place; give what changes by parameter, or move it",
                "Tek yerden çağır; değişeni parametreyle ver ya da taşı"
            ),
        Words("Asking the window inside a component", "Bir bileşenin içinde pencereye sormak") to
            Words("Ask its own box with BoxWithPantrixWindow", "BoxWithPantrixWindow ile kendi kutusuna sor"),
        Words("Reading window.size for a layout", "Bir düzen için window.size okumak") to
            Words(
                "Read the class; a threshold of your own with rememberWindowIsAtLeast",
                "Sınıfı oku; kendi eşiğin için rememberWindowIsAtLeast"
            ),
        Words(
            "Choosing the margin by hand: responsive(16.dp, 24.dp, 32.dp)",
            "Kenar boşluğunu elle seçmek: responsive(16.dp, 24.dp, 32.dp)"
        ) to Words("PantrixTheme.layout.margin", "PantrixTheme.layout.margin"),
        Words("Taking isNarrow for \"a phone\"", "isNarrow'u \"telefon\" sanmak") to
            Words(
                "A phone on its side is Expanded: isShort tells it",
                "Yan duran telefon Expanded'dır: isShort söyler"
            ),
        Words("Looking at the platform for \"touch\"", "\"Dokunma\" için platforma bakmak") to
            Words(
                "PantrixTheme.platform.isTouch: a phone’s browser is the web",
                "PantrixTheme.platform.isTouch: telefonun tarayıcısı web'dir"
            )
    )
    val ReadTheGuides = Words("Read the two adaptive guides", "İki uyarlanabilirlik rehberini oku")
}

/**
 * A message of the simulator's inbox.
 *
 * @property from who sent it.
 * @property time when.
 * @property subject what about.
 * @property body what it says.
 * @property avatar the colour of its sender's circle.
 */
internal class Mail(
    val from: Words,
    val time: Words,
    val subject: Words,
    val body: Words,
    val avatar: Long
)

/**
 * A pattern the page shows: the component it is, what it does, the code it is written with and its sketches.
 *
 * @property api the component, numbered.
 * @property title what it does.
 * @property body how it does it.
 * @property code the anchor of its code in the page's content.
 * @property sketches what each of its sketches is called.
 */
internal class Pattern(
    val api: String,
    val title: Words,
    val body: Words,
    val code: String,
    val sketches: List<Words>
)
