package com.pantrixui.site.feature.ai

import com.pantrixui.site.core.designsystem.BySize
import com.pantrixui.site.core.model.AskScope
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Words

/** The words of the page that asks the server, in both of the site's languages, as its design writes them. */
internal object AskSay {
    val Here = Words("Ask the server", "Sunucuya sor")
    val Kicker = Words("Live · no model, no key", "Canlı · modelsiz, anahtarsız")
    val Title = Words("Ask the server yourself", "Sunucuya kendin sor")

    /** The lead, around the server's address, which is written as code. */
    val Lead: BySize<Pair<Words, Words>> = BySize(
        desktop = Words("This box calls the tools of ", "Bu kutu ") to Words(
            " for you: it finds the component, the code of its example and its documents. Every answer comes from " +
                "the library's own documents, with the tools it called beside it.",
            " adresinin araçlarını senin için çağırır: bileşeni, örneğinin kodunu ve belgelerini bulur. Her cevap " +
                "kütüphanenin kendi belgelerinden gelir; çağırdığı araçlar yanında durur."
        ),
        tablet = Words("This box calls the tools of ", "Bu kutu ") to Words(
            " for you: the component, the code of its example and its documents.",
            " adresinin araçlarını senin için çağırır: bileşeni, örneğinin kodunu ve belgelerini."
        )
    )

    val TabServer = Words("Ask the server", "Sunucuya sor")
    val TabAssistant = Words("In your own assistant", "Kendi asistanında")
    val TabKey = Words("With your own key", "Kendi anahtarınla")
    val PhaseTwo = Words("Phase 2", "2. aşama")
    val Tabs = Words("How to ask", "Nasıl sorulur")

    val Question = Words("Your question", "Sorun")
    val Placeholder: BySize<Words> = BySize(
        desktop = Words("A toast, a dropdown, a comment box…", "Bir bildirim, bir açılır liste, bir yorum kutusu…"),
        phone = Words("A toast, a dropdown…", "Bir bildirim, bir açılır liste…")
    )
    val Ask = Words("Ask", "Sor")
    val Where = Words("Where", "Nerede")
    val Try = Words("Try", "Dene")

    /** Each scope's name. */
    fun scope(scope: AskScope): Words = when (scope) {
        AskScope.All -> Words("All", "Tümü")
        AskScope.Components -> Words("Components", "Bileşenler")
        AskScope.Api -> Words("API", "API")
        AskScope.Icons -> Words("Icons", "İkonlar")
        AskScope.Documents -> Words("Documents", "Belgeler")
    }

    /** The questions to try, in each language: each finds something, and two look outside the components. */
    fun examples(language: Language): List<Pair<String, AskScope>> = when (language) {
        Language.English -> listOf(
            "toast" to AskScope.All,
            "a comment box with several lines" to AskScope.All,
            "snackbar" to AskScope.Api,
            "arrow" to AskScope.Icons
        )

        Language.Turkish -> listOf(
            "bildirim" to AskScope.All,
            "çok satırlı yorum kutusu" to AskScope.All,
            "snackbar" to AskScope.Api,
            "kapat" to AskScope.Icons
        )
    }

    /** What a question that found nothing offers in its place. */
    fun suggestions(language: Language): List<String> = when (language) {
        Language.English -> listOf("notification", "toast", "snackbar")
        Language.Turkish -> listOf("bildirim", "geri al", "snackbar")
    }

    /** The three things the box does, before anything is asked: a title and a line each. */
    val Promises: List<Pair<Words, Words>> = listOf(
        Words("Finds the component", "Bileşeni bulur") to Words(
            "By its name, another system's name for it, or what it is for.",
            "Adıyla, başka bir sistemin ona dediği adla ya da ne için olduğuyla."
        ),
        Words("Brings its code", "Kodunu getirir") to
            Words("With its imports, ready to copy.", "Importlarıyla, kopyalanmaya hazır."),
        Words("Leads to its documents", "Belgelerine götürür") to
            Words("Its page, its API, its guide.", "Sayfası, API'si, rehberi.")
    )

    /** The line under the box, around the server's address. */
    val Kept = Words("What you type goes to ", "Yazdığın, ararken ") to Words(
        " while it searches; the site keeps none of it.",
        " adresine gider; site hiçbirini tutmaz."
    )

    val AssistantLead = Words(
        "Add the server to your own assistant and ask there, with your own model.",
        "Sunucuyu kendi asistanına ekle ve orada, kendi modelinle sor."
    )
    val CopyAddress = Words("Copy the address", "Adresi kopyala")
    val CopyCommand = Words("Copy the command", "Komutu kopyala")
    val ClaudeCode = Words("The plugin brings the server and the skill.", "Eklenti sunucuyu ve beceriyi getirir.")
    val Cursor = Words("In one click, or the address in mcpServers.", "Tek tıkla, ya da adres mcpServers'ta.")
    val AddToCursor = Words("Add to Cursor ↗", "Cursor'a ekle ↗")
    val VsCode = Words(
        "One command, or the address in the project's MCP settings.",
        "Tek bir komutla, ya da adres projenin MCP ayarlarında."
    )
    val OtherClients = Words("Claude Desktop and others", "Claude Desktop ve ötekiler")
    val Others = Words(
        "By the address; a client that takes none starts the jar.",
        "Adresle; adres almayan bir istemci jar'ı başlatır."
    )
    val WholeSetup = Words("The whole setup →", "Bütün kurulum →")

    val KeyTitle = Words("Ask a free question with your own key", "Kendi anahtarınla serbest bir soru sor")
    val KeyLead = Words(
        "The model reads the question, picks the server's tools itself and writes an answer with code, in the " +
            "page's language: the same cards as here, with the tools it called beside them and the tokens it cost " +
            "under them.",
        "Model soruyu okur, sunucunun araçlarını kendisi seçer ve kodlu bir cevap yazar, sayfanın dilinde: buradaki " +
            "kartların aynısı, yanlarında çağırdığı araçlar, altlarında harcadığı token'lar."
    )
    val KeyPoints = listOf(
        Words(
            "Your own key, never the site's: Anthropic first, other providers after it.",
            "Senin anahtarın, hiçbir zaman sitenin değil: önce Anthropic, ardından öteki sağlayıcılar."
        ),
        Words(
            "The key goes from this device straight to the provider, never through this site's server.",
            "Anahtar bu cihazdan doğrudan sağlayıcıya gider, hiçbir zaman bu sitenin sunucusundan geçmez."
        ),
        Words(
            "It is kept for the visit, remembered on this device only if you ask, and forgotten whenever you say.",
            "Ziyaret boyunca tutulur, yalnızca istersen bu cihazda hatırlanır ve ne zaman dersen unutulur."
        )
    )
    val UntilThen = Words("Until then, ask in your own assistant →", "O zamana kadar kendi asistanında sor →")
}
