package com.pantrixui.site.feature.ai

import com.pantrixui.site.core.designsystem.BySize
import com.pantrixui.site.core.model.SiteAddress
import com.pantrixui.site.core.model.TokenKind
import com.pantrixui.site.core.model.Words

/** The page for coding assistants' words, in each language, and in each size where the design shortens them. */
internal object AiSay {
    val Kicker = Words("For coding assistants", "Kodlama asistanları için")
    val Title = Words("Your assistant writes Pantrix, not Material.", "Asistanın Material değil, Pantrix yazar.")

    /** The page's lead: the words before the name of `llms.txt`, and after it. */
    val Lead = BySize(
        desktop = Words(
            "Pantrix writes its own documents for coding assistants from its sources at every build: an MCP server, " +
                "a skill, ",
            "Pantrix kodlama asistanları için belgelerini her derlemede kendi kaynaklarından yazar: bir MCP " +
                "sunucusu, bir beceri, "
        ) to Words(
            " and the whole library as JSON. An assistant asks for what it needs and gets today's API, not a guess " +
                "from Material.",
            " ve bütün kütüphane JSON olarak. Asistan gerekeni sorar ve Material'dan bir tahmin değil, bugünün " +
                "API'sini alır."
        ),
        tablet = Words("An MCP server, a skill, ", "Bir MCP sunucusu, bir beceri, ") to Words(
            " and the library as JSON, written from its sources at every build. The assistant asks for what it " +
                "needs and gets today's API.",
            " ve JSON olarak kütüphane; her derlemede kaynaklarından yazılır. Asistan gerekeni sorar ve bugünün " +
                "API'sini alır."
        ),
        phone = Words("An MCP server, a skill and ", "Bir MCP sunucusu, bir beceri ve ") to Words(
            ", written from the library's sources at every build.",
            "; her derlemede kütüphanenin kaynaklarından yazılır."
        )
    )
    val SetItUp = Words("Set it up", "Kur")
    val ReadLlms = Words("Read llms.txt", "llms.txt'yi oku")

    /** The page's numbers; a phone shows none. */
    val Stats = listOf(
        "12" to BySize(
            Words("tools on the MCP server", "MCP sunucusunda araç"),
            tablet = Words("tools on the server", "sunucuda araç")
        ),
        "3" to BySize(
            Words("prompts: a component, a screen, a test", "istem: bir bileşen, bir ekran, bir test"),
            tablet = Words("prompts", "istem")
        ),
        "7" to BySize(Words("rules the skill keeps", "becerinin tuttuğu kural"))
    )

    // The conversation.
    val Conversation = BySize(
        Words("An assistant with the Pantrix server, step by step", "Pantrix sunucusuyla bir asistan, adım adım"),
        tablet = Words("An assistant with the Pantrix server", "Pantrix sunucusuyla bir asistan"),
        phone = Words("An assistant, step by step", "Bir asistan, adım adım")
    )
    val Replay = Words("Play again", "Yeniden oynat")
    val Asked = Words(
        "Add a comment box under each order, 280 characters at most.",
        "Her siparişin altına en çok 280 karakterlik bir yorum kutusu ekle."
    )

    /** The tools the assistant calls; a tablet and a phone show the first two. */
    val ToolCalls = listOf(
        ToolCall(
            "search_components(\"comment, several lines\")",
            BySize(
                Words(
                    "PantrixTextArea · PantrixFilledTextArea · PantrixTextField",
                    "PantrixTextArea · PantrixFilledTextArea · PantrixTextField"
                ),
                phone = Words("PantrixTextArea · PantrixFilledTextArea", "PantrixTextArea · PantrixFilledTextArea")
            )
        ),
        ToolCall(
            "get_component(\"PantrixTextArea\")",
            BySize(
                Words(
                    "Its page: the examples with their imports, its parameters, the tags it writes",
                    "Sayfası: importlarıyla örnekleri, parametreleri, yazdığı etiketler"
                ),
                tablet = Words(
                    "Its examples with their imports, its parameters, its tags",
                    "İmportlarıyla örnekleri, parametreleri, etiketleri"
                ),
                phone = Words(
                    "The page of PantrixTextArea, with its imports",
                    "PantrixTextArea'nın sayfası, importlarıyla"
                )
            )
        ),
        ToolCall(
            "get_rules()",
            BySize(
                Words(
                    "No Material · text through Pantrix · values from the theme · a tagKey on every control",
                    "Material yok · metin Pantrix'ten · değerler temadan · her kontrolde bir tagKey"
                )
            )
        )
    )

    /** What the answer was checked for; a phone shows the first three. */
    val Checks = listOf(
        BySize(Words("No Material import", "Material importu yok")),
        BySize(
            Words("A tagKey, the same in every language", "Her dilde aynı bir tagKey"),
            tablet = Words("A tagKey", "Bir tagKey"),
            phone = Words("A tagKey, the same in every language", "Her dilde aynı bir tagKey")
        ),
        BySize(Words("The label from the strings", "Etiket metin kaynaklarından")),
        BySize(Words("Imports from the documents", "Importlar belgelerden"))
    )

    // Four ways in.
    val WaysTitle = Words("Four ways in, one source", "Dört giriş, tek kaynak")

    /** What the four have in common; a phone says nothing under the heading. */
    val WaysLead = BySize(
        Words(
            "All four are written by the same build from the library's KDoc, its components' annotations, the " +
                "demo's pages and the guides. They cannot drift from the code: the build stops when they would.",
            "Dördü de aynı derlemeyle kütüphanenin KDoc'undan, bileşenlerinin işaretlerinden, demonun sayfalarından " +
                "ve rehberlerden yazılır. Koddan ayrılamazlar: ayrılacakları an derleme durur."
        ),
        tablet = Words(
            "All four come from the same build: the KDoc, the components' annotations, the demo's pages and the " +
                "guides.",
            "Dördü de aynı derlemeden gelir: KDoc, bileşenlerin işaretleri, demonun sayfaları ve rehberler."
        )
    )
    val Ways = listOf(
        Way(
            "MCP",
            Words("The MCP server", "MCP sunucusu"),
            BySize(
                Words(
                    "At this site's address, or in one jar that runs by itself. The assistant chooses what to ask " +
                        "and takes only what it needs.",
                    "Bu sitenin adresinde ya da kendi başına çalışan tek bir jar'da. Asistan ne soracağını seçer ve " +
                        "yalnızca gerekeni alır."
                ),
                tablet = Words(
                    "At an address or in one jar; the assistant takes only what it needs.",
                    "Bir adreste ya da tek bir jar'da; asistan yalnızca gerekeni alır."
                ),
                phone = Words(
                    "An address or a jar; it takes only what it needs.",
                    "Bir adres ya da jar; yalnızca gerekeni alır."
                )
            ),
            SiteAddress.host + "/mcp"
        ),
        Way(
            "SK",
            Words("The skill", "Beceri"),
            BySize(
                Words(
                    "How to work with Pantrix: its rules, the steps of writing and reviewing a screen, Material's " +
                        "names in Pantrix.",
                    "Pantrix'le nasıl çalışılır: kuralları, bir ekranı yazmanın ve gözden geçirmenin adımları, " +
                        "Material'ın adlarının Pantrix'teki karşılıkları."
                ),
                tablet = Words(
                    "The rules, the steps of writing and reviewing a screen, Material's names in Pantrix.",
                    "Kurallar, bir ekranı yazmanın ve gözden geçirmenin adımları, Material'ın adlarının karşılıkları."
                ),
                phone = Words(
                    "The rules and the steps of writing a screen.",
                    "Kurallar ve bir ekranı yazmanın adımları."
                )
            ),
            "pantrix-ui@pantrix-ui"
        ),
        Way(
            "txt",
            Words("llms.txt", "llms.txt"),
            BySize(
                Words(
                    "The index an assistant reads first, about 8,500 tokens. llms-full.txt holds everything in one " +
                        "file.",
                    "Asistanın önce okuduğu dizin, yaklaşık 8.500 token. llms-full.txt her şeyi tek dosyada tutar."
                ),
                tablet = Words(
                    "The index read first, about 8,500 tokens; llms-full.txt holds everything.",
                    "Önce okunan dizin, yaklaşık 8.500 token; llms-full.txt her şeyi tutar."
                ),
                phone = Words("The index read first, about 8,500 tokens.", "Önce okunan dizin, yaklaşık 8.500 token.")
            ),
            SiteAddress.host + "/llms.txt"
        ),
        Way(
            "{ }",
            Words("pantrix-ui.json", "pantrix-ui.json"),
            BySize(
                Words(
                    "The library as data, with its JSON Schema: for tools, code generators and this site.",
                    "JSON Şeması'yla birlikte veri olarak kütüphane: araçlar, kod üreticileri ve bu site için."
                ),
                tablet = Words(
                    "The library as data with its JSON Schema, for tools and this site.",
                    "JSON Şeması'yla veri olarak kütüphane; araçlar ve bu site için."
                ),
                phone = Words("The library as data, with its schema.", "Şemasıyla birlikte veri olarak kütüphane.")
            ),
            "pantrix-ui.schema.json"
        )
    )

    /** What to give, and when: a desktop's alone. */
    val WhatToGive = Words("What to give, and when", "Ne zaman ne verilir")
    val Give = listOf(
        GiveRow(
            Words("Writing a screen or a feature", "Bir ekran ya da özellik yazmak"),
            code = "llms.txt",
            end = Words(
                ": the assistant reads the page or the module it needs",
                ": asistan gereken sayfayı ya da modülü okur"
            )
        ),
        GiveRow(
            Words("The work is with one component", "İş tek bir bileşenle"),
            start = Words("That component's page, ", "O bileşenin sayfası, "),
            code = "pages/data-table.md",
            end = Words(
                ": its examples with their imports, every parameter",
                ": importlarıyla örnekleri, her parametresi"
            )
        ),
        GiveRow(
            Words("Everything at once", "Her şey bir arada"),
            code = "llms-full.txt",
            end = Words(
                ", about 1.3 million tokens: for a wide context or a document index",
                ", yaklaşık 1,3 milyon token: geniş bir bağlam ya da bir belge dizini için"
            )
        ),
        GiveRow(
            Words("A tool reads Pantrix", "Bir araç Pantrix'i okur"),
            code = "pantrix-ui.json",
            end = Words(" and its schema", " ve şeması")
        )
    )
}

/**
 * A tool the assistant calls in the conversation.
 *
 * @property call the call, as the assistant writes it.
 * @property result what the server answers, in short.
 */
internal class ToolCall(
    val call: String,
    val result: BySize<Words>
) {
    /** The tool's name. */
    val tool: String get() = call.substringBefore('(')
}

/**
 * A way an assistant reads Pantrix.
 *
 * @property mark the sign of its card.
 * @property title what it is.
 * @property body what it does.
 * @property where where it is.
 */
internal class Way(
    val mark: String,
    val title: Words,
    val body: BySize<Words>,
    val where: String
)

/**
 * An assistant's setup.
 *
 * @property name what the assistant is called.
 * @property note what to know once it is set up.
 * @property lines its lines, one under another.
 * @property onPhone whether a phone shows it.
 */
internal class Client(
    val name: BySize<Words>,
    val note: BySize<Words>,
    val lines: BySize<List<SetupLine>>,
    val onPhone: Boolean = true
)

/**
 * A line of an assistant's setup: code as it is written, or a comment in each language.
 *
 * @property code what it says where it is code; an empty line has none.
 * @property comment what it says where it is a comment.
 * @property kind what its code is, which says its colour.
 */
internal class SetupLine(
    val code: String = "",
    val comment: Words? = null,
    val kind: TokenKind = TokenKind.Plain
)

/**
 * A row of "what to give, and when": when, then what, a name of the code in the middle of its words.
 *
 * @property case when it is given.
 * @property start what comes before the code's name.
 * @property code the name of the code given.
 * @property end what comes after it.
 */
internal class GiveRow(
    val case: Words,
    val start: Words? = null,
    val code: String,
    val end: Words
)
