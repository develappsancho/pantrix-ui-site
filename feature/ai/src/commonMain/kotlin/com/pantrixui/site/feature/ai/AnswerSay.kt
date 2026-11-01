package com.pantrixui.site.feature.ai

import com.pantrixui.site.core.model.Words

/**
 * The words of the server's answers, in both of the site's languages, as the page's design writes them: the tools it
 * called and what each got, a component's card, the declarations, the icons, the documents, and the cards of a
 * question that found nothing and of a server that did not answer.
 */
internal object AnswerSay {
    val ToolsCalled = Words("Tools called", "Çağrılan araçlar")
    val Asking = Words("Asking…", "Soruluyor…")

    /** How many tools a phone's folded list holds. */
    fun toolsCalled(count: Int): Words = Words(
        if (count == 1) "1 tool called" else "$count tools called",
        "$count araç çağrıldı"
    )

    /** What a step got, in the page's words. */
    fun result(result: AskResult): Words = when (result) {
        is AskResult.Components -> when {
            result.total == 0 -> Words("No component", "Bileşen yok")

            result.total == 1 -> Words("1 component: ${result.first}", "1 bileşen: ${result.first}")

            else -> Words(
                "${result.total} components: ${result.first} first",
                "${result.total} bileşen: önce ${result.first}"
            )
        }

        is AskResult.Documents -> if (result.total == 0) {
            Words("No page", "Sayfa yok")
        } else {
            val titles = result.titles.take(LISTED).joinToString(", ") { it.substringBefore(':') }
            Words("${result.total} pages: $titles", "${result.total} sayfa: $titles")
        }

        AskResult.Card -> Words(
            "Its import, description, keywords and short examples",
            "Importu, açıklaması, anahtar sözcükleri ve kısa örnekleri"
        )

        is AskResult.Examples -> Words("${result.count} examples", "${result.count} örnek")

        is AskResult.Declarations -> if (result.shown < result.total) {
            Words(
                "${result.total} declarations; the first ${result.shown} shown",
                "${result.total} bildirim; ilk ${result.shown} tanesi gösterildi"
            )
        } else {
            Words("${result.total} declarations", "${result.total} bildirim")
        }

        AskResult.Declaration -> Words("Its signature and what it holds", "İmzası ve içindekiler")

        is AskResult.Icons -> Words(
            "${result.total} icons: ${result.names.take(LISTED).joinToString()}",
            "${result.total} ikon: ${result.names.take(LISTED).joinToString()}"
        )

        AskResult.Nothing -> Words("Nothing found", "Bir şey bulunamadı")

        AskResult.NoAnswer -> Words("No answer", "Cevap gelmedi")
    }

    val OpenPage = Words("Open its page →", "Sayfasını aç →")
    val CopyImport = Words("Copy the import", "Importu kopyala")
    val Example = Words("Example", "Örnek")
    val Copy = Words("Copy", "Kopyala")
    val AlsoFound = Words("Also found as", "Şöyle de bulunur")
    val ApiReference = Words("API reference", "API başvurusu")
    val InDocuments = Words("In the documents", "Belgelerde")
    val AlsoMatches = Words("Also matches", "Bunlar da uyuyor")

    /** A page's examples, by how many there are. */
    fun examples(count: Int): Words = Words("$count examples", "$count örnek")

    val Declarations = Words("Declarations", "Bildirimler")
    val Entries = Words("Entries", "Değerleri")
    val Parameters = Words("Parameters", "Parametreler")
    val OnItsPage = Words("On its component's page →", "Bileşeninin sayfasında →")

    /** How many declarations were found, and how many are shown. */
    fun declarations(
        total: Int,
        shown: Int
    ): Words = if (shown < total) {
        Words("$total declarations · the first $shown", "$total bildirim · ilk $shown")
    } else {
        Words("$total declarations", "$total bildirim")
    }

    /** The button that shows the rest of the declarations. */
    fun showOthers(count: Int): Words = Words("Show the other $count", "Kalan $count tanesini göster")

    val Icons = Words("Icons", "İkonlar")

    /** How many icons were found. */
    fun icons(count: Int): Words = Words(if (count == 1) "1 icon" else "$count icons", "$count ikon")

    val Mirrors = Words("Mirrors in RTL", "Sağdan sola aynalanır")
    val NeverMirrors = Words("Never mirrors", "Hiç aynalanmaz")

    /** The name of the button that copies an icon's [reference]. */
    fun copyIcon(reference: String): Words = Words("Copy $reference", "$reference kopyala")

    /** The line under the icons, around the names written as code. */
    val IconsDrawn = Triple(
        Words("Each one drawn here by the library itself: ", "Her biri burada kütüphanenin kendisiyle çizildi: "),
        Words(", from ", ", "),
        Words(".", " paketinden.")
    )

    /** How many documents were found. */
    fun pages(count: Int): Words = Words(if (count == 1) "1 page" else "$count pages", "$count sayfa")

    /** What a document is. */
    fun kind(kind: String): Words = when (kind) {
        "guide" -> Words("Guide", "Rehber")
        "reference" -> Words("Skill reference", "Beceri referansı")
        else -> Words("Page", "Sayfa")
    }

    /** The title of the card of a question that found nothing. */
    fun nothingFor(question: String): Words = Words(
        "Nothing found for “$question”",
        "“$question” için bir şey bulunamadı"
    )

    val NothingLine = Words(
        "Nothing in the library's documents matches it. Try other words, or one of these:",
        "Kütüphanenin belgelerinde ona uyan bir şey yok. Başka sözcükler dene ya da bunlardan birini:"
    )

    val NoAnswer = Words("The server did not answer", "Sunucu cevap vermedi")

    /** Why the server did not answer, around its address. */
    val NoAnswerLine = Words("", "") to Words(
        " could not be reached in time: the network may be down, or the server busy. Nothing you typed was kept; " +
            "ask again in a moment.",
        " adresine zamanında ulaşılamadı: ağ kopmuş ya da sunucu meşgul olabilir. Yazdığın hiçbir şey " +
            "tutulmadı; birazdan yeniden sor."
    )
    val AskAgain = Words("Ask again", "Yeniden sor")
    val InYourAssistant = Words("Ask it in your own assistant →", "Kendi asistanında sor →")

    /** How many names a step's line lists at most. */
    private const val LISTED = 3
}
