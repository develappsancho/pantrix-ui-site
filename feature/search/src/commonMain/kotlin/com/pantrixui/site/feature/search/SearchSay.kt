package com.pantrixui.site.feature.search

import com.pantrixui.site.core.designsystem.BySize
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.SearchKind
import com.pantrixui.site.core.model.Words

/** The search's words, in each language, and in each size where the design shortens them. */
internal object SearchSay {
    val Dialog = Words("Search the docs", "Belgelerde ara")
    val Placeholder = BySize(
        Words("Search components, the API and the guides", "Bileşenlerde, API'de ve rehberlerde ara"),
        phone = Words("Search the docs", "Belgelerde ara")
    )
    val Close = Words("Close the search", "Aramayı kapat")
    val Cancel = Words("Cancel", "Vazgeç")
    const val ESC = "esc"
    val All = Words("All", "Tümü")
    val Popular = Words("Where people start", "Başlanacak yerler")
    val Move = Words("to move", "gezinmek için")
    val Open = Words("to open", "açmak için")
    val Keys = Words("↑ ↓ and Enter with a keyboard", "Klavyeyle ↑ ↓ ve Enter")
    val Source = Words("From the documents your assistant reads, too", "Asistanının da okuduğu belgelerden")
    val PagesIn = Words("Search the pages in", "Sayfaları şu dilde ara")
    val ResultsLanguage = Words("Language of the results", "Sonuçların dili")

    /** What the search says when [query] finds nothing. */
    fun nothingFor(query: String) = Words("Nothing for \"$query\"", "\"$query\" için bir şey yok")

    val TryAnother = BySize(
        Words(
            "Try a component's name, a parameter or what you want to do: \"comment box\", \"fold\", \"export\".",
            "Bir bileşenin adını, bir parametreyi ya da yapmak istediğini dene: \"yorum kutusu\", \"katlanma\", " +
                "\"dışa aktarma\"."
        ),
        tablet = Words(
            "Try a component's name, a parameter or what you want to do.",
            "Bir bileşenin adını, bir parametreyi ya da yapmak istediğini dene."
        ),
        phone = Words(
            "Try a component's name or what you want to do.",
            "Bir bileşenin adını ya da yapmak istediğini dene."
        )
    )

    /** What a kind of result is called, on its filter and over its group. */
    fun kindName(kind: SearchKind): Words = when (kind) {
        SearchKind.Component -> Words("Components", "Bileşenler")
        SearchKind.Api -> Words("API", "API")
        SearchKind.Section -> Words("Pages", "Sayfalar")
        SearchKind.Guide -> Words("Guides", "Rehberler")
    }

    /** What a language is called on its button, and to a screen reader. */
    fun languageName(language: Language): Pair<String, Words> = when (language) {
        Language.English -> "EN" to Words("English", "English")
        Language.Turkish -> "TR" to Words("Türkçe", "Türkçe")
    }
}
