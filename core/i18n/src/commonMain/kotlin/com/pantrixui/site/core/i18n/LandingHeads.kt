package com.pantrixui.site.core.i18n

import com.pantrixui.site.core.model.LibraryFacts
import com.pantrixui.site.core.model.Route
import com.pantrixui.site.core.model.Words

/**
 * What a landing page is called and what it is about: the head of a page whose words are the site's code, not one of
 * its `content` files.
 *
 * @property title what the page is called: its window's title, before the site's name.
 * @property description what the page is about, in a sentence or two: what a search engine shows under its title.
 */
data class LandingHead(
    val title: Words,
    val description: Words
)

/**
 * The heads of the site's landing pages, in both languages. Each page names its window with its [LandingHead.title],
 * and the static page the build writes for its address takes both for its head: the two cannot part.
 */
object LandingHeads {
    /** The landing page, `/`. */
    val Home = LandingHead(
        title = Words("Compose Multiplatform design system", "Compose Multiplatform tasarım sistemi"),
        description = Words(
            "A design system and adaptive UI library for Compose Multiplatform: ${LibraryFacts.COMPONENTS} " +
                "components on Compose Foundation, with no Material dependency, and one theme that follows the " +
                "conventions of Android, iOS, the desktop and the web.",
            "Compose Multiplatform için bir tasarım sistemi ve uyarlanabilir arayüz kütüphanesi: Compose Foundation " +
                "üstünde ${LibraryFacts.COMPONENTS} bileşen, Material'a hiçbir bağımlılık olmadan, ve Android'in, " +
                "iOS'un, masaüstünün ve web'in alışkanlıklarını izleyen tek bir tema."
        )
    )

    /** The components by group, `/components`. */
    val Components = LandingHead(
        title = Words("Components", "Bileşenler"),
        description = Words(
            "All ${LibraryFacts.COMPONENTS} components of Pantrix UI by group, each with live examples, its API and " +
                "its styles on every platform.",
            "Pantrix UI'ın ${LibraryFacts.COMPONENTS} bileşeni grup grup; her biri canlı örnekleri, API'si ve her " +
                "platformdaki stilleriyle."
        )
    )

    /** The tokens, `/foundations`. */
    val Foundations = LandingHead(
        title = Words("Foundations", "Temeller"),
        description = Words(
            "The tokens every Pantrix component is drawn with: colours, type, spacing, shapes, elevation, motion and " +
                "density, shown in the Pantrix preset.",
            "Her Pantrix bileşeninin çizildiği tokenlar: renkler, yazı, boşluk, şekiller, yükselti, hareket ve " +
                "yoğunluk, Pantrix hazır ayarında."
        )
    )

    /** The theme builder, `/theme`. */
    val Theme = LandingHead(
        title = Words("Theme builder", "Tema oluşturucu"),
        description = Words(
            "Build a DesignPreset for Pantrix UI: a brand's colour, corners and typeface, checked for contrast, " +
                "previewed on every platform and taken away as Kotlin.",
            "Pantrix UI için bir DesignPreset kurun: markanın rengi, köşeleri ve yazı tipi; kontrastı denetlenir, " +
                "her platformda önizlenir ve Kotlin olarak alınır."
        )
    )

    /** Adaptive UI, `/adaptive`. */
    val Adaptive = LandingHead(
        title = Words("Adaptive UI", "Uyarlanabilir arayüz"),
        description = Words(
            "How a Pantrix screen fits every window, a foldable's too: width classes, posture and folds, and the " +
                "scaffolds that lay a screen out by them.",
            "Bir Pantrix ekranının her pencereye, katlanan bir cihazınkine de nasıl uyduğu: genişlik sınıfları, " +
                "duruş ve katlar, ve ekranı onlara göre yerleştiren iskeleler."
        )
    )

    /** The page for coding assistants, `/ai`. */
    val Ai = LandingHead(
        title = Words("For coding assistants", "Kodlama asistanları için"),
        description = Words(
            "How a coding assistant reads Pantrix: an MCP server, a skill, llms.txt and the library as JSON, all " +
                "written from its sources.",
            "Bir kodlama asistanının Pantrix'i nasıl okuduğu: bir MCP sunucusu, bir beceri, llms.txt ve JSON olarak " +
                "kütüphane; hepsi kaynaklarından yazılır."
        )
    )

    /** The page that asks the library's MCP server, `/ai/ask`. */
    val Ask = LandingHead(
        title = Words("Ask the server", "Sunucuya sor"),
        description = Words(
            "Ask Pantrix UI's MCP server yourself, with no model and no key: the component, the code of its example " +
                "and its documents, in English or in Turkish.",
            "Pantrix UI'ın MCP sunucusuna kendin sor, modelsiz ve anahtarsız: bileşeni, örneğinin kodunu ve " +
                "belgelerini, İngilizce ya da Türkçe."
        )
    )

    /** The roadmap, `/roadmap`. */
    val Roadmap = LandingHead(
        title = Words("Roadmap", "Yol haritası"),
        description = Words(
            "What Pantrix UI has shipped, what is being built and what comes next: each item with its proposal and " +
                "the version it comes in.",
            "Pantrix UI'da neyin çıktığı, neyin yapıldığı ve sırada ne olduğu: her madde önerisiyle ve geleceği " +
                "sürümle."
        )
    )

    /** The page of the desktop app, `/download`. */
    val Download = LandingHead(
        title = Words("Download", "İndir"),
        description = Words(
            "Pantrix UI's site as a desktop app for macOS, signed and notarized: every page in both languages, the " +
                "components live, the theme builder and the adaptive simulator.",
            "Pantrix UI'ın sitesi, imzalı ve Apple'ın onayından geçmiş bir macOS uygulaması olarak: her sayfa iki " +
                "dilde, bileşenler canlı, tema oluşturucu ve uyarlanabilir simülatör."
        )
    )

    /** The apps built with Pantrix UI, `/references`. */
    val References = LandingHead(
        title = Words("Built with Pantrix UI", "Pantrix UI ile yapıldı"),
        description = Words(
            "Apps that draw their screens with Pantrix UI, the Compose Multiplatform design system: what each one " +
                "is, where it runs and which of the library's components it uses, Pantrix UI's own site first.",
            "Ekranlarını Compose Multiplatform tasarım sistemi Pantrix UI ile çizen uygulamalar: her birinin ne " +
                "olduğu, nerede çalıştığı ve kütüphanenin hangi bileşenlerini kullandığı; ilki Pantrix UI'ın " +
                "kendi sitesi."
        )
    )

    /** The head of [route], or null where the route's page is one of the site's `content` files or a component's. */
    fun of(route: Route): LandingHead? = when (route) {
        Route.Home -> Home
        Route.Components -> Components
        Route.Foundations -> Foundations
        Route.Theme -> Theme
        Route.Adaptive -> Adaptive
        Route.Ai -> Ai
        Route.Ask -> Ask
        Route.Roadmap -> Roadmap
        Route.Download -> Download
        Route.References -> References
        else -> null
    }
}
