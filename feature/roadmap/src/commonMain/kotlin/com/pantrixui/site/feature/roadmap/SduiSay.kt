package com.pantrixui.site.feature.roadmap

import com.pantrixui.site.core.designsystem.BySize
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.LibraryFacts
import com.pantrixui.site.core.model.TokenKind
import com.pantrixui.site.core.model.Words

/** The words of the part about server-driven UI, in each language and each size. */
internal object SduiSay {
    val Pill = BySize(
        Words(
            "Next · ${LibraryFacts.PLANNED_VERSION}, planned · Server-driven UI",
            "Sırada · ${LibraryFacts.PLANNED_VERSION}, planlandı · Sunucu güdümlü UI"
        ),
        phone = Words(
            "Next · ${LibraryFacts.PLANNED_VERSION}, planned",
            "Sırada · ${LibraryFacts.PLANNED_VERSION}, planlandı"
        )
    )
    val Title = BySize(
        Words(
            "Screens a server describes, drawn safely with Pantrix",
            "Bir sunucunun tarif ettiği ekranlar, Pantrix'le güvenle çizilir"
        ),
        phone = Words(
            "Screens a server describes, drawn safely",
            "Bir sunucunun tarif ettiği ekranlar, güvenle çizilir"
        )
    )
    val Lead = BySize(
        Words(
            "Change a form, a campaign card or a settings page without shipping a new version, with the app's own " +
                "components. The description is data the app checks, never code it runs.",
            "Bir formu, bir kampanya kartını ya da bir ayar sayfasını yeni bir sürüm çıkarmadan, uygulamanın kendi " +
                "bileşenleriyle değiştir. Tarif, uygulamanın denetlediği veridir; asla çalıştırdığı kod değil."
        ),
        tablet = Words(
            "Change a form or a campaign card without shipping a new version. The description is data the app " +
                "checks, never code it runs.",
            "Bir formu ya da bir kampanya kartını yeni bir sürüm çıkarmadan değiştir. Tarif, uygulamanın " +
                "denetlediği veridir; asla çalıştırdığı kod değil."
        ),
        phone = Words(
            "The description is data the app checks, never code it runs.",
            "Tarif, uygulamanın denetlediği veridir; asla çalıştırdığı kod değil."
        )
    )

    /** What each sample is called on its tab. */
    fun sampleName(sample: Sample): Words = when (sample) {
        Sample.Campaign -> Words("A campaign card", "Bir kampanya kartı")
        Sample.Form -> Words("A form", "Bir form")
    }

    /** What each sample's screen is called in its bar. */
    fun screenTitle(sample: Sample): Words = when (sample) {
        Sample.Campaign -> Words("Offers", "Kampanyalar")
        Sample.Form -> Words("Delivery", "Teslimat")
    }

    val FromServer = Words("From the server", "Sunucudan")
    val Sketch = BySize(
        Words("A sketch: the format is not designed yet", "Bir taslak: biçim henüz tasarlanmadı"),
        tablet = Words("A sketch", "Bir taslak")
    )
    val CheckedThenDrawn = Words("checked, then drawn", "denetlenir, sonra çizilir")
    val Checks = listOf(
        Words("✓ signed", "✓ imzalı"),
        Words("✓ allowed", "✓ izinli"),
        Words("✓ data only", "✓ yalnızca veri")
    )

    // The campaign card.
    val EndsSunday = Words("Ends Sunday", "Pazar biter")
    val CardTitle = BySize(
        Words("Free delivery on every order", "Her siparişte ücretsiz teslimat"),
        phone = Words("Free delivery…", "Ücretsiz teslimat…")
    )
    val CardText = BySize(
        Words("For orders over €40 until Sunday night.", "Pazar gecesine kadar 40 € üzeri siparişlerde."),
        tablet = Words("For orders over €40…", "40 € üzeri siparişlerde…")
    )
    val ShopNow = Words("Shop now", "Alışverişe başla")
    val NotNow = Words("Not now", "Şimdi değil")

    // The form.
    val Name = Words("Name on the parcel", "Paketteki ad")
    val NamePlaceholder = Words("As on your ID", "Kimliğindeki gibi")
    val Neighbour = Words("Leave with a neighbour", "Komşuya bırak")
    val NobodyAnswers = Words("If nobody answers", "Kimse açmazsa")
    val Note = Words("Note to the courier", "Kuryeye not")
    val NotePlaceholder = Words("The bell is broken…", "Zil bozuk…")
    val Save = Words("Save", "Kaydet")
    val Signed = Words(
        "// signed by shop.example, checked before it is drawn",
        "// shop.example imzalı, çizilmeden önce denetlenir"
    )

    /** The description of [sample] as the server sends it, in [language], as long as [layout] has room for. */
    fun lines(
        sample: Sample,
        layout: SiteLayout,
        language: Language
    ): List<CodeLine> = when (sample) {
        Sample.Campaign -> campaign(layout, language)
        Sample.Form -> form(layout, language)
    }

    private fun campaign(
        layout: SiteLayout,
        language: Language
    ): List<CodeLine> {
        val tag = EndsSunday.of(language)
        val title = CardTitle.of(layout).of(language)
        val text = CardText.of(layout).of(language)
        val shop = ShopNow.of(language)
        return when (layout) {
            SiteLayout.Desktop -> listOf(
                CodeLine("{"),
                CodeLine("  \"screen\": \"offers\",", TokenKind.StringLiteral),
                CodeLine("  \"body\": ["),
                CodeLine("    { \"component\": \"Card\",", TokenKind.Keyword),
                CodeLine("      \"image\": \"asset://campaign/delivery\","),
                CodeLine("      \"tag\": \"$tag\","),
                CodeLine("      \"title\": \"$title\","),
                CodeLine("      \"text\": \"$text\","),
                CodeLine(
                    "      \"action\": { \"button\": \"$shop\", \"navigate\": \"catalogue\" } },",
                    TokenKind.Number
                ),
                CodeLine("    { \"component\": \"Button\", \"variant\": \"Outline\",", TokenKind.Keyword),
                CodeLine(
                    "      \"text\": \"${NotNow.of(language)}\", \"action\": { \"dismiss\": true } }",
                    TokenKind.Number
                ),
                CodeLine("  ]"),
                CodeLine("}"),
                CodeLine(Signed.of(language), TokenKind.Comment)
            )

            SiteLayout.Tablet -> listOf(
                CodeLine("{ \"screen\": \"offers\",", TokenKind.StringLiteral),
                CodeLine("  \"body\": ["),
                CodeLine("    { \"component\": \"Card\",", TokenKind.Keyword),
                CodeLine("      \"tag\": \"$tag\","),
                CodeLine("      \"title\": \"$title\","),
                CodeLine("      \"text\": \"$text\","),
                CodeLine("      \"action\": { \"button\": \"$shop\",", TokenKind.Number),
                CodeLine("                  \"navigate\": \"catalogue\" } }", TokenKind.Number),
                CodeLine("  ] }")
            )

            SiteLayout.Phone -> listOf(
                CodeLine("{ \"screen\": \"offers\",", TokenKind.StringLiteral),
                CodeLine("  \"body\": ["),
                CodeLine("    { \"component\": \"Card\",", TokenKind.Keyword),
                CodeLine("      \"tag\": \"$tag\","),
                CodeLine("      \"title\": \"$title\","),
                CodeLine("      \"action\": {", TokenKind.Number),
                CodeLine("        \"button\": \"$shop\",", TokenKind.Number),
                CodeLine("        \"navigate\": \"catalogue\" } }", TokenKind.Number),
                CodeLine("  ] }")
            )
        }
    }

    private fun form(
        layout: SiteLayout,
        language: Language
    ): List<CodeLine> {
        val name = Name.of(language)
        val save = Save.of(language)
        val request = CodeLine("      \"action\": { \"request\": \"delivery.save\" } }", TokenKind.Number)
        return when (layout) {
            SiteLayout.Desktop -> listOf(
                CodeLine("{"),
                CodeLine("  \"screen\": \"delivery\",", TokenKind.StringLiteral),
                CodeLine("  \"body\": ["),
                CodeLine("    { \"component\": \"TextField\", \"field\": \"name\",", TokenKind.Keyword),
                CodeLine("      \"label\": \"$name\", \"placeholder\": \"${NamePlaceholder.of(language)}\" },"),
                CodeLine("    { \"component\": \"SwitchButton\", \"field\": \"neighbour\",", TokenKind.Keyword),
                CodeLine(
                    "      \"label\": \"${Neighbour.of(
                        language
                    )}\", \"supporting\": \"${NobodyAnswers.of(language)}\" },"
                ),
                CodeLine("    { \"component\": \"TextArea\", \"field\": \"note\",", TokenKind.Keyword),
                CodeLine("      \"label\": \"${Note.of(language)}\", \"maxLength\": $NOTE_LENGTH },"),
                CodeLine("    { \"component\": \"Button\", \"text\": \"$save\",", TokenKind.Keyword),
                request,
                CodeLine("  ]"),
                CodeLine("}")
            )

            SiteLayout.Tablet -> listOf(
                CodeLine("{ \"screen\": \"delivery\",", TokenKind.StringLiteral),
                CodeLine("  \"body\": ["),
                CodeLine("    { \"component\": \"TextField\", \"field\": \"name\",", TokenKind.Keyword),
                CodeLine("      \"label\": \"$name\" },"),
                CodeLine("    { \"component\": \"SwitchButton\",", TokenKind.Keyword),
                CodeLine("      \"field\": \"neighbour\" },"),
                CodeLine("    { \"component\": \"Button\", \"text\": \"$save\",", TokenKind.Keyword),
                request,
                CodeLine("  ] }")
            )

            SiteLayout.Phone -> listOf(
                CodeLine("{ \"screen\": \"delivery\",", TokenKind.StringLiteral),
                CodeLine("  \"body\": ["),
                CodeLine("    { \"component\": \"TextField\",", TokenKind.Keyword),
                CodeLine("      \"field\": \"name\" },"),
                CodeLine("    { \"component\": \"SwitchButton\",", TokenKind.Keyword),
                CodeLine("      \"field\": \"neighbour\" },"),
                CodeLine("    { \"component\": \"Button\",", TokenKind.Keyword),
                request,
                CodeLine("  ] }")
            )
        }
    }

    /** What keeps a description safe: a title, then how. */
    val Principles = listOf(
        BySize(Words("Its sender is known", "Göndereni bellidir")) to BySize(
            Words(
                "The app checks where a description came from before it draws it, with cryptography measured on all " +
                    "six targets first, the browser's Wasm and JavaScript too.",
                "Uygulama bir tarifin nereden geldiğini çizmeden önce denetler; şifreleme önce altı hedefin " +
                    "hepsinde, " +
                    "tarayıcının Wasm'ında ve JavaScript'inde de ölçülür."
            ),
            tablet = Words(
                "Checked before it is drawn, on all six targets.",
                "Çizilmeden önce, altı hedefin hepsinde denetlenir."
            ),
            phone = Words("Checked before it is drawn.", "Çizilmeden önce denetlenir.")
        ),
        BySize(Words("It runs no code", "Kod çalıştırmaz")) to BySize(
            Words(
                "A description carries text, numbers, colours and lists. A button's press is a named action the app " +
                    "decides on: a navigation, a request, a change of state.",
                "Bir tarif metin, sayı, renk ve liste taşır. Bir düğmeye basmak, uygulamanın karar verdiği adlı bir " +
                    "eylemdir: bir gezinme, bir istek, bir durum değişikliği."
            ),
            tablet = Words(
                "A press is a named action the app decides on.",
                "Basmak, uygulamanın karar verdiği adlı bir eylemdir."
            ),
            phone = Words("A press is a named action.", "Basmak adlı bir eylemdir.")
        ),
        BySize(
            Words("Only what the app allows", "Yalnızca uygulamanın izin verdiği"),
            tablet = Words("Only what is allowed", "Yalnızca izin verilen")
        ) to BySize(
            Words(
                "Components and addresses come from the app's own list. A description is outside input: its depth, " +
                    "length and size are bounded, and reading it never crashes.",
                "Bileşenler ve adresler uygulamanın kendi listesinden gelir. Tarif dışarıdan bir girdidir: " +
                    "derinliği, uzunluğu ve boyutu sınırlıdır; okumak asla çökmez."
            ),
            tablet = Words(
                "Components and addresses from the app's own list.",
                "Bileşenler ve adresler uygulamanın kendi listesinden."
            ),
            phone = Words("From the app's own list.", "Uygulamanın kendi listesinden.")
        )
    )

    // A desktop's steps and what assistants may get.
    val StepsTitle = Words("The steps", "Adımlar")

    /** The steps of the library's plan for it, its part 11 in order. */
    val Steps = listOf(
        Words("Measure the cryptography on all six targets", "Şifrelemeyi altı hedefin hepsinde ölçmek"),
        Words("A design proposal, and the answers to its questions", "Bir tasarım önerisi ve sorularının yanıtları"),
        Words("The modules, each with its tests", "Modüller, her biri testleriyle"),
        Words("A page in the demo", "Demoda bir sayfa"),
        Words("What assistants and tools need", "Asistanların ve araçların gerekenleri"),
        Words("The guide", "Rehber"),
        Words("Publishing, the API dumps and the README", "Yayın, API dökümleri ve README")
    )
    val AssistantsTitle = Words("For assistants, being considered", "Asistanlar için, düşünülenler")
    val AssistantsStart = Words("A JSON Schema of the description, made from ", "Tarifin JSON Şeması, ")
    val AssistantsEnd = Words(
        ", and two tools on the MCP server:",
        "'dan üretilerek; MCP sunucusunda da iki araç:"
    )
    val Tools = listOf(
        "validate_screen" to Words(
            " checks a description against the schema and the app's list",
            " bir tarifi şemaya ve uygulamanın listesine karşı denetler"
        ),
        "get_sdui_component" to Words(
            " says what a component accepts in a description",
            " bir bileşenin tarifte neyi kabul ettiğini söyler"
        )
    )
}

/** A sample description the server may send. */
enum class Sample { Campaign, Form }

/**
 * A line of code in one colour.
 *
 * @property text what it says.
 * @property kind what it is, which says its colour.
 */
internal class CodeLine(
    val text: String,
    val kind: TokenKind = TokenKind.Plain
)

/** How long the courier's note may be, in characters. */
internal const val NOTE_LENGTH = 120
