package com.pantrixui.site.feature.ai

import com.pantrixui.site.core.designsystem.BySize
import com.pantrixui.site.core.model.Words

/** The words of what the server answers and the rules it keeps, in each language and each size. */
internal object ServerSay {
    val ToolsTitle = Words("What the server answers", "Sunucu neyi yanıtlar")
    val ToolsLead = Words(
        "Tools for asking, resources for reading whole, prompts that carry the rules in themselves.",
        "Sormak için araçlar, bütün okumak için kaynaklar, kuralları içinde taşıyan istemler."
    )
    val Tools = listOf(
        "search_components" to BySize(
            Words(
                "The components found by what they are for: \"toast\", \"bottom sheet\".",
                "Ne için olduklarına göre bulunan bileşenler: \"toast\", \"bottom sheet\"."
            ),
            tablet = Words("Components found by what they are for.", "Ne için olduklarına göre bulunan bileşenler."),
            phone = Words("Components by what they are for.", "Ne için olduklarına göre bileşenler.")
        ),
        "get_component" to BySize(
            Words(
                "A component’s page: its features, its examples with their imports, its API.",
                "Bir bileşenin sayfası: özellikleri, importlarıyla örnekleri, API'si."
            ),
            tablet = Words(
                "A component’s page: features, examples with imports, API.",
                "Bir bileşenin sayfası: özellikler, importlarıyla örnekler, API."
            ),
            phone = Words("A component’s page, its examples, its API.", "Bir bileşenin sayfası, örnekleri, API'si.")
        ),
        "get_component_metadata" to BySize(
            Words(
                "A component as data: its import, artifact, keywords, tags and every form.",
                "Veri olarak bir bileşen: importu, yapıtı, anahtar sözcükleri, etiketleri ve her biçimi."
            ),
            tablet = Words(
                "A component as data: import, artifact, keywords, tags.",
                "Veri olarak bir bileşen: import, yapıt, anahtar sözcükler, etiketler."
            ),
            phone = Words("A component as data.", "Veri olarak bir bileşen.")
        ),
        "search_api · get_api" to BySize(
            Words(
                "A declaration found by its name or what it does, and the whole of it.",
                "Adına ya da yaptığı işe göre bulunan bir bildirim ve bütünü."
            ),
            tablet = Words(
                "A declaration by name or purpose, and the whole of it.",
                "Adına ya da amacına göre bir bildirim ve bütünü."
            ),
            phone = Words("A declaration, and the whole of it.", "Bir bildirim ve bütünü.")
        ),
        "search_icons" to BySize(Words("The icons of PantrixIcons.", "PantrixIcons'un simgeleri.")),
        "search_documentation" to BySize(
            Words("Every page, guide and reference of the skill.", "Her sayfa, rehber ve becerinin her başvurusu."),
            phone = Words("Pages, guides, the skill’s references.", "Sayfalar, rehberler, becerinin başvuruları.")
        ),
        "get_documentation_page" to BySize(
            Words(
                "One of them whole: guide/adaptive-usage, skill/forms.",
                "Onlardan biri bütün olarak: guide/adaptive-usage, skill/forms."
            ),
            tablet = Words("One of them whole.", "Onlardan biri, bütün olarak.")
        ),
        "list_examples · get_example" to BySize(
            Words("A page’s examples, and the whole code of one.", "Bir sayfanın örnekleri ve birinin bütün kodu."),
            tablet = Words("A page’s examples, and the code of one.", "Bir sayfanın örnekleri ve birinin kodu."),
            phone = Words("A page’s examples, and one’s code.", "Bir sayfanın örnekleri ve birinin kodu.")
        ),
        "get_changelog" to BySize(
            Words("The changelog, or one version’s part of it.", "Değişiklik günlüğü ya da bir sürümün payı."),
            tablet = Words("The changelog, or one version’s part.", "Değişiklik günlüğü ya da bir sürümün payı."),
            phone = Words("The changelog, or one version.", "Değişiklik günlüğü ya da bir sürüm.")
        ),
        "get_rules" to BySize(
            Words(
                "The skill itself, for an assistant that has not installed it.",
                "Beceriyi kurmamış bir asistan için becerinin kendisi."
            ),
            tablet = Words("The skill itself.", "Becerinin kendisi.")
        )
    )
    val Resources = Words("Resources", "Kaynaklar")
    val ResourceUris = listOf(
        "pantrix://components",
        "pantrix://api",
        "pantrix://examples",
        "pantrix://documentation",
        "pantrix://icons",
        "pantrix://llms.txt",
        "pantrix://llms-full.txt",
        "pantrix://schema",
        "pantrix://skill"
    )
    val Prompts = Words("Prompts", "İstemler")
    val PromptList = listOf(
        "find_component_for_use_case" to Words(
            "the component for a need, and how it is used",
            "bir ihtiyacın bileşeni ve nasıl kullanıldığı"
        ),
        "implement_screen" to Words("a screen, with its rules", "kurallarıyla bir ekran"),
        "write_ui_test" to Words("a component's test, with its tags", "etiketleriyle bir bileşenin testi")
    )

    // Rules and checks.
    val RulesTitle = Words("The rules it keeps", "Tuttuğu kurallar")
    val RulesLeadStart = Words(
        "The skill's core rules are the library's own. ",
        "Becerinin temel kuralları kütüphanenin kendi kurallarıdır. "
    )
    val RulesLeadEnd = Words(
        " and the prompts say the same, from one place.",
        " ve istemler tek bir yerden aynı şeyi söyler."
    )

    /** The rules: a title, then what it asks. */
    val Rules = listOf(
        BySize(Words("No Material", "Material yok")) to BySize(
            Words(
                "Pantrix’s components and PantrixTheme, never Material’s.",
                "Pantrix'in bileşenleri ve PantrixTheme, asla Material'ınkiler değil."
            ),
            tablet = Words("Pantrix’s components and PantrixTheme.", "Pantrix'in bileşenleri ve PantrixTheme.")
        ),
        BySize(Words("Text through Pantrix", "Metin Pantrix'ten")) to BySize(
            Words(
                "PantrixText and Pantrix’s fields, never BasicText or BasicTextField.",
                "PantrixText ve Pantrix'in alanları, asla BasicText ya da BasicTextField değil."
            ),
            tablet = Words("Never BasicText or BasicTextField.", "Asla BasicText ya da BasicTextField değil.")
        ),
        BySize(Words("Values from the theme", "Değerler temadan")) to BySize(
            Words(
                "Colours, type, gaps and corners from PantrixTheme, not written into a screen.",
                "Renkler, yazı, boşluklar ve köşeler PantrixTheme'den; bir ekrana yazılmaz."
            ),
            tablet = Words("Colours, type, gaps and corners.", "Renkler, yazı, boşluklar ve köşeler.")
        ),
        BySize(Words("Every control a tagKey", "Her kontrole bir tagKey")) to BySize(
            Words(
                "The same in every language: Button_save. Never from visible text.",
                "Her dilde aynı: Button_save. Asla görünen metinden değil."
            ),
            tablet = Words("Never from visible text.", "Asla görünen metinden değil.")
        ),
        BySize(Words("An image says what it shows", "Bir görsel ne gösterdiğini söyler")) to BySize(
            Words(
                "A contentDescription, or null for decoration; never empty.",
                "Bir contentDescription, süs içinse null; asla boş değil."
            ),
            tablet = Words("Or null for decoration.", "Ya da süs için null.")
        ),
        BySize(
            Words("Start and end, not left and right", "Sol ve sağ değil, baş ve son"),
            tablet = Words("Start and end", "Baş ve son")
        ) to BySize(
            Words(
                "So a screen turns round in a language read from right to left.",
                "Böylece bir ekran sağdan sola okunan bir dilde döner."
            ),
            tablet = Words("Never left and right.", "Asla sol ve sağ değil.")
        ),
        BySize(Words("Imports from the documents", "Importlar belgelerden")) to BySize(
            Words(
                "Every component lives in a package of its own; take the import given.",
                "Her bileşen kendi paketinde yaşar; verilen importu al."
            ),
            tablet = Words("Every component has a package of its own.", "Her bileşenin kendi paketi var."),
            phone = Words("Take the import given.", "Verilen importu al.")
        ),
        BySize(Words("Parameters from the documents", "Parametreler belgelerden")) to BySize(
            Words(
                "Each parameter, what it takes and its default before a call; arguments by name.",
                "Bir çağrıdan önce her parametre, ne aldığı ve varsayılanı; argümanlar adıyla."
            ),
            tablet = Words("Arguments by name, as documented.", "Belgelendiği gibi, argümanlar adıyla.")
        ),
        BySize(Words("Words from resources", "Sözler kaynaklardan")) to BySize(
            Words(
                "What the app shows from its resources; Pantrix's components say their own.",
                "Uygulamanın gösterdiği kaynaklarından; Pantrix'in bileşenleri kendi sözlerini söyler."
            ),
            tablet = Words("stringResource, not a literal.", "Düz metin değil, stringResource.")
        ),
        BySize(Words("Windows, not devices", "Cihaz değil, pencere")) to BySize(
            Words(
                "A layout reads the window it is drawn in, its size and its folds.",
                "Bir düzen çizildiği pencereyi, boyunu ve katlanma yerlerini okur."
            ),
            tablet = Words("The window's size and folds.", "Pencerenin boyu ve katlanma yerleri.")
        ),
        BySize(Words("Tests on Pantrix's helpers", "Testler Pantrix'in yardımcılarıyla")) to BySize(
            Words(
                "PantrixTestTheme, and a component found by its tag.",
                "PantrixTestTheme, ve etiketiyle bulunan bir bileşen."
            ),
            tablet = Words("PantrixTestTheme and tags.", "PantrixTestTheme ve etiketler.")
        )
    )

    val DriftTitle = Words("Why it does not drift", "Neden ayrılmaz")
    val DriftLead = Words(
        "The documents are built with the library, and the build stops on any of these:",
        "Belgeler kütüphaneyle birlikte derlenir ve derleme bunlardan herhangi birinde durur:"
    )

    /** What stops the build, on a desktop; a tablet says four of them as one leads to the next. */
    val Drift = BySize(
        listOf(
            Words("A component no page of the demo documents", "Demonun hiçbir sayfasının anlatmadığı bir bileşen"),
            Words(
                "An example that calls a parameter no form has",
                "Hiçbir biçimin sahip olmadığı bir parametreyi çağıran örnek"
            ),
            Words(
                "The skill naming what the library does not have, or a version it is not published in",
                "Becerinin kütüphanede olmayanı ya da yayımlanmadığı bir sürümü anması"
            ),
            Words(
                "A block of Kotlin that uses a name it does not import",
                "Import etmediği bir adı kullanan Kotlin bloğu"
            ),
            Words(
                "A component without keywords, or a text of the demo without its English",
                "Anahtar sözcüksüz bir bileşen ya da İngilizcesi olmayan bir demo metni"
            ),
            Words("A guide whose English has parted from its Turkish", "İngilizcesi Türkçesinden ayrılmış bir rehber")
        ),
        tablet = listOf(
            Words(
                "A component no page documents stops the build",
                "Hiçbir sayfanın anlatmadığı bir bileşen derlemeyi durdurur"
            ),
            Words(
                "So does an example that calls a parameter no form has",
                "Hiçbir biçimin sahip olmadığı bir parametreyi çağıran örnek de"
            ),
            Words("And the skill naming what the library does not have", "Kütüphanede olmayanı anan beceri de"),
            Words(
                "And a guide whose English has parted from its Turkish",
                "İngilizcesi Türkçesinden ayrılmış bir rehber de"
            )
        )
    )

    /** The checks as a phone says them: [DriftPhoneStart] in bold, then the rest of one sentence. */
    val DriftPhoneStart = Words("It does not drift:", "Ayrılmaz:")
    val DriftPhone = Words(
        " the build stops on a component no page documents, an example that calls the library as it is not, or a " +
            "skill naming what the library does not have.",
        " derleme, hiçbir sayfanın anlatmadığı bir bileşende, kütüphaneyi olmadığı gibi çağıran bir örnekte ya da " +
            "kütüphanede olmayanı anan bir beceride durur."
    )
    val CopyPage = Words(
        "Every page of this site has \"Copy page\". ",
        "Bu sitenin her sayfasında \"Sayfayı kopyala\" var. "
    )
    val CopyPageEnd = Words(
        "It copies the page as Markdown, the same text the server gives, for a chat with any assistant.",
        "Sayfayı Markdown olarak, sunucunun verdiği metnin aynısıyla kopyalar; herhangi bir asistanla bir sohbet için."
    )
}
