package com.pantrixui.site.core.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import com.pantrixui.site.core.model.GuidePlatform
import com.pantrixui.site.core.model.Language

/**
 * The words of the site's own frame, in one language: its header, menus, sidebar and the parts every page shares.
 * What a page says is its content, written by the content tool in each language; these are what is around it.
 *
 * @property docs the documentation, in the header and as the first step of a breadcrumb.
 * @property components the components.
 * @property foundations the theme's tokens.
 * @property adaptive the page about adaptive layouts, in the header.
 * @property adaptiveUi the same page, in a menu.
 * @property theme the theme builder, in the header.
 * @property themeBuilder the same page, in a menu.
 * @property forAi the page for coding assistants, in the header.
 * @property forAiAssistants the same page, in a menu.
 * @property roadmap the roadmap.
 * @property changelog the changelog.
 * @property searchTheDocs what the search box says before anything is typed.
 * @property switchToLight the theme button, in the dark theme.
 * @property switchToDark the theme button, in the light theme.
 * @property language the language menu's name.
 * @property eachPageInBothLanguages the note under the language menu.
 * @property openTheMenu the button that opens the menu on a narrow window.
 * @property closeTheMenu the button that closes it.
 * @property gettingStarted the first group of the documentation.
 * @property introduction the documentation's first page.
 * @property installation the page about adding the library.
 * @property theming the page about the theme.
 * @property platforms the page about each platform.
 * @property forms the page about forms.
 * @property testing the page about tests.
 * @property fittingTheWindow the guide to fitting the window, in the sidebar.
 * @property dateAndTime the guide to dates and times.
 * @property puttingTheKeyboardAway the guide to putting the keyboard away.
 * @property keyboardShortcuts the guide to keyboard shortcuts.
 * @property aTypefaceThatArrivesLate the guide to a typeface that arrives late on the web.
 * @property theMaterial3Bridge the guide to the Material 3 bridge.
 * @property runtimeDiagnostics the guide to runtime diagnostics.
 * @property testingAScreen the guide to testing a screen.
 * @property guides the guides.
 * @property project the group of pages about the project itself.
 * @property reportAProblem the link to the project's issues.
 * @property browseByGroup the line under the count of components.
 * @property copyPage the button that copies a page as Markdown.
 * @property copied what that button says once it has.
 * @property onThisPage the title of a page's list of sections.
 * @property optional the mark of a step that may be left out.
 * @property pageNotOpened what a page says when its content could not be read.
 * @property tryAgain the button that reads it again.
 * @property error404 the mark above the heading of the page for an address with no page.
 * @property pageNotHere that page's heading.
 * @property pageNotHereText that page's first paragraph.
 * @property address the label of the field that shows the address asked for.
 * @property noPageAtAddress what that field's error says.
 * @property orStartFrom the title of that page's links.
 * @property didYouMean the label of the card that guesses the page meant.
 * @property startInstallation what the start card of the installation says.
 * @property startGuides what the start card of the guides says.
 * @property startChangelog what the start card of the changelog says.
 * @property linkBroughtYou the sentence before the link that reports a broken link.
 * @property tellUs that link.
 * @property withThePage what follows it.
 * @property everyComposeApp the tag of a guide whose problem every Compose Multiplatform app has.
 * @property alsoWithoutPantrix the tag of a guide that gives its fix without the library too.
 * @property editOnGitHub the link at a guide's foot to the text it is written from.
 * @property previous the label of the card that leads to the guide before.
 * @property next the label of the card that leads to the guide after.
 * @property filterTheGuides what the guides page's filter says before anything is typed.
 * @property allTopics the topic chip that shows every guide.
 * @property topic what a screen reader calls the row of topic chips.
 * @property tryAnotherWord the line under the guides page's note that nothing matched.
 * @property guidesForAssistants the title of the guides page's card about coding assistants.
 * @property guidesForAssistantsText that card's line.
 * @property semverBefore the changelog's lead after the library's own words, up to the word that marks a break.
 * @property breaking the word that marks a change that breaks a call or a behaviour.
 * @property semverAfter what follows that word in the lead.
 * @property module what a screen reader calls the row of module chips.
 * @property onlyBreaking the switch that shows breaking changes alone.
 * @property followTheReleases the link to the feed of the library's releases.
 * @property notReleasedYet the mark of a version still to come.
 * @property unreleased what stands in a version's date while it has none.
 * @property publishedAs the label of a version's artifacts.
 * @property targets the label of a version's platforms.
 * @property versions the title of the changelog's list of versions.
 * @property forAssistants the heading of the list's link for coding assistants.
 * @property unreleasedNote what follows a version still to come in that list.
 * @property planned what follows a planned version.
 * @property nothingBreakingText the line under the note that a version breaks nothing.
 * @property plannedTitle what the next planned version brings.
 * @property plannedText the line under it.
 */
@Immutable
@Suppress("LongParameterList") // A pack of words has a field for each of them.
class SiteStrings(
    val docs: String,
    val components: String,
    val foundations: String,
    val adaptive: String,
    val adaptiveUi: String,
    val theme: String,
    val themeBuilder: String,
    val forAi: String,
    val forAiAssistants: String,
    val roadmap: String,
    val changelog: String,
    val searchTheDocs: String,
    val switchToLight: String,
    val switchToDark: String,
    val language: String,
    val eachPageInBothLanguages: String,
    val openTheMenu: String,
    val closeTheMenu: String,
    val gettingStarted: String,
    val introduction: String,
    val installation: String,
    val theming: String,
    val platforms: String,
    val forms: String,
    val testing: String,
    val fittingTheWindow: String,
    val dateAndTime: String,
    val puttingTheKeyboardAway: String,
    val keyboardShortcuts: String,
    val aTypefaceThatArrivesLate: String,
    val theMaterial3Bridge: String,
    val runtimeDiagnostics: String,
    val testingAScreen: String,
    val guides: String,
    val project: String,
    val reportAProblem: String,
    val browseByGroup: String,
    val copyPage: String,
    val copied: String,
    val onThisPage: String,
    val optional: String,
    val pageNotOpened: String,
    val tryAgain: String,
    val error404: String,
    val pageNotHere: String,
    val pageNotHereText: String,
    val address: String,
    val noPageAtAddress: String,
    val orStartFrom: String,
    val didYouMean: String,
    val startInstallation: String,
    val startGuides: String,
    val startChangelog: String,
    val linkBroughtYou: String,
    val tellUs: String,
    val withThePage: String,
    val everyComposeApp: String,
    val alsoWithoutPantrix: String,
    val editOnGitHub: String,
    val previous: String,
    val next: String,
    val filterTheGuides: String,
    val allTopics: String,
    val topic: String,
    val tryAnotherWord: String,
    val guidesForAssistants: String,
    val guidesForAssistantsText: String,
    val semverBefore: String,
    val breaking: String,
    val semverAfter: String,
    val module: String,
    val onlyBreaking: String,
    val followTheReleases: String,
    val notReleasedYet: String,
    val unreleased: String,
    val publishedAs: String,
    val targets: String,
    val versions: String,
    val forAssistants: String,
    val unreleasedNote: String,
    val planned: String,
    val nothingBreakingText: String,
    val plannedTitle: String,
    val plannedText: String,
    private val allGuides: (Int) -> String,
    private val componentCount: (Int) -> String,
    private val startComponents: (Int) -> String,
    private val languageButton: (code: String, name: String) -> String,
    private val minutesToRead: (Int) -> String,
    private val seenOn: (List<GuidePlatform>) -> String,
    private val checkedWith: (String) -> String,
    private val noGuideSays: (String) -> String,
    private val entries: (Int) -> String,
    private val nothingBreaking: (String) -> String
) {
    /** The link to every guide: "All 23 guides". */
    fun allGuides(count: Int): String = allGuides.invoke(count)

    /** The count of components: "98 components". */
    fun componentCount(count: Int): String = componentCount.invoke(count)

    /** What the start card of the components says: "All 98, by group." */
    fun startComponents(count: Int): String = startComponents.invoke(count)

    /** What a screen reader says of the language button: "Language: EN, English". */
    fun languageButton(
        code: String,
        name: String
    ): String = languageButton.invoke(code, name)

    /** How long a page takes to read: "6 min read". */
    fun minutesToRead(minutes: Int): String = minutesToRead.invoke(minutes)

    /** The tag of a guide whose problem is seen on some platforms only: "Seen on iOS, Android and the web". */
    fun seenOn(platforms: List<GuidePlatform>): String = seenOn.invoke(platforms)

    /** What a guide's foot says it was checked with: "Checked with Compose Multiplatform 1.12.2". */
    fun checkedWith(version: String): String = checkedWith.invoke(version)

    /** What the guides page says when no guide matches its filter: "No guide says “clipboard”". */
    fun noGuideSays(query: String): String = noGuideSays.invoke(query)

    /** How many lines a kind of change has: "8 entries". */
    fun entries(count: Int): String = entries.invoke(count)

    /** What the changelog says of a version none of whose changes breaks anything: "Nothing breaking in 0.1.0". */
    fun nothingBreaking(version: String): String = nothingBreaking.invoke(version)

    companion object {
        /** The site's words in English. */
        val English = SiteStrings(
            docs = "Docs",
            components = "Components",
            foundations = "Foundations",
            adaptive = "Adaptive",
            adaptiveUi = "Adaptive UI",
            theme = "Theme",
            themeBuilder = "Theme builder",
            forAi = "For AI",
            forAiAssistants = "For AI assistants",
            roadmap = "Roadmap",
            changelog = "Changelog",
            searchTheDocs = "Search the docs",
            switchToLight = "Switch to the light theme",
            switchToDark = "Switch to the dark theme",
            language = "Language",
            eachPageInBothLanguages = "Every page in both languages, each at its own address.",
            openTheMenu = "Open the menu",
            closeTheMenu = "Close the menu",
            gettingStarted = "Getting started",
            introduction = "Introduction",
            installation = "Installation",
            theming = "Theming",
            platforms = "Platforms",
            forms = "Forms",
            testing = "Testing",
            fittingTheWindow = "Fitting the window",
            dateAndTime = "Date and time",
            puttingTheKeyboardAway = "Putting the keyboard away",
            keyboardShortcuts = "Keyboard shortcuts",
            aTypefaceThatArrivesLate = "A typeface that arrives late",
            theMaterial3Bridge = "The Material 3 bridge",
            runtimeDiagnostics = "Runtime diagnostics",
            testingAScreen = "Testing a screen",
            guides = "Guides",
            project = "Project",
            reportAProblem = "Report a problem",
            browseByGroup = "Browse them by group",
            copyPage = "Copy page",
            copied = "Copied",
            onThisPage = "On this page",
            optional = "optional",
            pageNotOpened = "This page could not be opened.",
            tryAgain = "Try again",
            error404 = "Error 404",
            pageNotHere = "This page is not here",
            pageNotHereText = "The address may have a typo, or the page may have moved. The search and the links " +
                "below lead on.",
            address = "Address",
            noPageAtAddress = "No page lives at this address.",
            orStartFrom = "Or start from",
            didYouMean = "Did you mean",
            startInstallation = "Add the library and draw the first component.",
            startGuides = "Answers to problems every app meets.",
            startChangelog = "What changed, version by version.",
            linkBroughtYou = "Did a link bring you here?",
            tellUs = "Tell us about it",
            withThePage = ", with the page it was on.",
            everyComposeApp = "Every Compose Multiplatform app",
            alsoWithoutPantrix = "Also without Pantrix",
            editOnGitHub = "Edit this page on GitHub",
            previous = "← Previous",
            next = "Next →",
            filterTheGuides = "Filter the guides",
            allTopics = "All",
            topic = "Topic",
            tryAnotherWord = "Try another word, or search the whole site.",
            guidesForAssistants = "Every guide reaches your assistant too",
            guidesForAssistantsText =
                "The MCP server gives them to Claude, Copilot or Cursor, in English and in Turkish.",
            semverBefore = "The versions follow Semantic Versioning: until 1.0.0 a minor version may break the API, " +
                "and a change that does is marked ",
            breaking = "Breaking",
            semverAfter = ".",
            module = "Module",
            onlyBreaking = "Only breaking changes",
            followTheReleases = "Follow the releases",
            notReleasedYet = "Not released yet",
            unreleased = "Unreleased",
            publishedAs = "Published as",
            targets = "Targets",
            versions = "Versions",
            forAssistants = "For assistants",
            unreleasedNote = "unreleased",
            planned = "planned",
            nothingBreakingText = "A change that breaks a call or a behaviour is marked here, in the version that " +
                "makes it.",
            plannedTitle = "Server-driven UI",
            plannedText = "Screens a server describes, drawn with Pantrix. The roadmap has the rest.",
            allGuides = { "All $it guides" },
            componentCount = { "$it components" },
            startComponents = { "All $it, by group." },
            languageButton = { code, name -> "Language: $code, $name" },
            minutesToRead = { "$it min read" },
            seenOn = { platforms -> "Seen on " + listed(platforms.map { it.englishName }, " and ") },
            checkedWith = { "Checked with Compose Multiplatform $it" },
            noGuideSays = { "No guide says “$it”" },
            entries = { if (it == 1) "1 entry" else "$it entries" },
            nothingBreaking = { "Nothing breaking in $it" }
        )

        /** The site's words in Turkish. */
        val Turkish = SiteStrings(
            docs = "Belgeler",
            components = "Bileşenler",
            foundations = "Temeller",
            adaptive = "Uyarlanabilir",
            adaptiveUi = "Uyarlanabilir arayüz",
            theme = "Tema",
            themeBuilder = "Tema oluşturucu",
            forAi = "Yapay zekâ",
            forAiAssistants = "Yapay zekâ asistanları için",
            roadmap = "Yol haritası",
            changelog = "Değişiklik günlüğü",
            searchTheDocs = "Belgelerde ara",
            switchToLight = "Açık temaya geç",
            switchToDark = "Koyu temaya geç",
            language = "Dil",
            eachPageInBothLanguages = "Her sayfa iki dilde, her biri kendi adresinde.",
            openTheMenu = "Menüyü aç",
            closeTheMenu = "Menüyü kapat",
            gettingStarted = "Başlarken",
            introduction = "Giriş",
            installation = "Kurulum",
            theming = "Tema",
            platforms = "Platformlar",
            forms = "Formlar",
            testing = "Test",
            fittingTheWindow = "Pencereye uyum",
            dateAndTime = "Tarih ve saat",
            puttingTheKeyboardAway = "Klavyeyi kapatma",
            keyboardShortcuts = "Klavye kısayolları",
            aTypefaceThatArrivesLate = "Geç gelen yazı tipi",
            theMaterial3Bridge = "Material 3 köprüsü",
            runtimeDiagnostics = "Çalışma anı tanılaması",
            testingAScreen = "Bir ekranı test etmek",
            guides = "Rehberler",
            project = "Proje",
            reportAProblem = "Bir sorun bildir",
            browseByGroup = "Gruplarına göre gezin",
            copyPage = "Sayfayı kopyala",
            copied = "Kopyalandı",
            onThisPage = "Bu sayfada",
            optional = "isteğe bağlı",
            pageNotOpened = "Bu sayfa açılamadı.",
            tryAgain = "Yeniden dene",
            error404 = "Hata 404",
            pageNotHere = "Bu sayfa burada değil",
            pageNotHereText = "Adreste bir yazım hatası olabilir ya da sayfa taşınmış olabilir. Arama ve aşağıdaki " +
                "bağlantılar yol gösterir.",
            address = "Adres",
            noPageAtAddress = "Bu adreste bir sayfa yok.",
            orStartFrom = "Ya da buradan başla",
            didYouMean = "Bunu mu aradın",
            startInstallation = "Kütüphaneyi ekle, ilk bileşeni çiz.",
            startGuides = "Her uygulamanın karşılaştığı sorunlara yanıtlar.",
            startChangelog = "Sürüm sürüm, neyin değiştiği.",
            linkBroughtYou = "Buraya bir bağlantı mı getirdi?",
            tellUs = "Bize bildir",
            withThePage = ", bağlantının durduğu sayfayla birlikte.",
            everyComposeApp = "Her Compose Multiplatform uygulaması",
            alsoWithoutPantrix = "Pantrix olmadan da",
            editOnGitHub = "Bu sayfayı GitHub'da düzenle",
            previous = "← Önceki",
            next = "Sonraki →",
            filterTheGuides = "Rehberleri süz",
            allTopics = "Hepsi",
            topic = "Konu",
            tryAnotherWord = "Başka bir sözcük dene ya da bütün sitede ara.",
            guidesForAssistants = "Her rehber asistanına da ulaşır",
            guidesForAssistantsText =
                "MCP sunucusu onları Claude'a, Copilot'a ya da Cursor'a İngilizce ve Türkçe verir.",
            semverBefore = "Sürümler Anlamsal Sürümlemeyi izler: 1.0.0'a dek bir ara sürüm API'yi kırabilir; kıran " +
                "değişiklik ",
            breaking = "Kırıcı",
            semverAfter = " diye işaretlenir.",
            module = "Modül",
            onlyBreaking = "Yalnızca kırıcı değişiklikler",
            followTheReleases = "Sürümleri izle",
            notReleasedYet = "Henüz yayınlanmadı",
            unreleased = "Yayınlanmamış",
            publishedAs = "Yayımlandığı adlar",
            targets = "Hedefler",
            versions = "Sürümler",
            forAssistants = "Asistanlar için",
            unreleasedNote = "yayınlanmamış",
            planned = "planlandı",
            nothingBreakingText =
                "Bir çağrıyı ya da davranışı kıran değişiklik, onu getiren sürümde burada işaretlenir.",
            plannedTitle = "Sunucu güdümlü arayüz",
            plannedText = "Bir sunucunun tarif ettiği ekranlar, Pantrix ile çizilir. Gerisi yol haritasında.",
            allGuides = { "$it rehberin hepsi" },
            componentCount = { "$it bileşen" },
            startComponents = { "$it bileşen, gruplarına göre." },
            languageButton = { code, name -> "Dil: $code, $name" },
            minutesToRead = { "$it dk okuma" },
            // The case ending goes on the last name only: "iOS, Android ve web'de görülür". A sentence that starts
            // with a common word starts with its capital; iOS keeps its own.
            seenOn = { platforms ->
                val names = platforms.dropLast(1).map { it.turkishName } + platforms.takeLast(1).map { it.turkishWhere }
                val sentence = listed(names, " ve ") + " görülür"
                if (platforms.firstOrNull() ==
                    GuidePlatform.Ios
                ) {
                    sentence
                } else {
                    sentence.replaceFirstChar { it.uppercase() }
                }
            },
            checkedWith = { "Compose Multiplatform $it ile denetlendi" },
            noGuideSays = { "Hiçbir rehber “$it” demiyor" },
            entries = { "$it madde" },
            nothingBreaking = { "$it sürümünde kırıcı bir değişiklik yok" }
        )

        /** The words of [language]. */
        fun of(language: Language): SiteStrings = when (language) {
            Language.English -> English
            Language.Turkish -> Turkish
        }
    }
}

/** [names] as a sentence lists them: "a, b and c", with [and] before the last. */
private fun listed(
    names: List<String>,
    and: String
): String = if (names.size < 2) names.joinToString() else names.dropLast(1).joinToString() + and + names.last()

/** What an English sentence calls a platform: "Android", "the web". */
private val GuidePlatform.englishName: String
    get() = when (this) {
        GuidePlatform.Android -> "Android"
        GuidePlatform.Ios -> "iOS"
        GuidePlatform.Desktop -> "the desktop"
        GuidePlatform.Web -> "the web"
    }

/** What a Turkish sentence calls a platform. */
private val GuidePlatform.turkishName: String
    get() = when (this) {
        GuidePlatform.Android -> "Android"
        GuidePlatform.Ios -> "iOS"
        GuidePlatform.Desktop -> "masaüstü"
        GuidePlatform.Web -> "web"
    }

/** A platform as the place something happens, in Turkish: "Android'de", "iOS'ta". */
private val GuidePlatform.turkishWhere: String
    get() = when (this) {
        GuidePlatform.Android -> "Android'de"
        GuidePlatform.Ios -> "iOS'ta"
        GuidePlatform.Desktop -> "masaüstünde"
        GuidePlatform.Web -> "web'de"
    }

/** The language the site is shown in, for everything under it. */
val LocalLanguage: ProvidableCompositionLocal<Language> = staticCompositionLocalOf { Language.English }

/** The site's words in that language. */
val LocalSiteStrings: ProvidableCompositionLocal<SiteStrings> = staticCompositionLocalOf { SiteStrings.English }

/** Shows [content] in [language]: its words and the language it says it is in. */
@Composable
fun ProvideLanguage(
    language: Language,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalLanguage provides language,
        LocalSiteStrings provides SiteStrings.of(language),
        content = content
    )
}

/** The name a language goes by in itself, whichever language the site is in: a picker shows it so. */
val Language.ownName: String
    get() = when (this) {
        Language.English -> "English"
        Language.Turkish -> "Türkçe"
    }
