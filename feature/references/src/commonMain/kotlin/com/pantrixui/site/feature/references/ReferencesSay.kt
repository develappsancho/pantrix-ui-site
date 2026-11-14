package com.pantrixui.site.feature.references

import com.pantrixui.site.core.model.PlatformKind
import com.pantrixui.site.core.model.SiteDownloads
import com.pantrixui.site.core.model.Words

/** The references page's words, in each language. What an app is and does is its own, in `content/references.json`. */
internal object ReferencesSay {
    val Kicker = Words("References", "Referanslar")
    val Title = Words("Built with Pantrix UI", "Pantrix UI ile yapıldı")
    val Lead = Words(
        "Apps that draw their screens with Pantrix UI: what each one is, where it runs and which of the library's " +
            "components it uses. The first is the site you are reading.",
        "Ekranlarını Pantrix UI ile çizen uygulamalar: her birinin ne olduğu, nerede çalıştığı ve kütüphanenin hangi " +
            "bileşenlerini kullandığı. İlki, şu an okuduğun site."
    )
    val Featured = Words("Featured", "Öne çıkan")
    val OpenSource = Words("Open source", "Açık kaynak")
    val WhereItRuns = Words("Where it runs", "Nerede çalışır")

    /** The first fact: how many of the library's components its own screens are drawn with. */
    val ComponentsFact =
        Words("of the library's components in its own screens", "kütüphane bileşeni, kendi ekranlarında")

    /** The second: how many languages it speaks. */
    val LanguagesFact = Words("languages, each page at its own address", "dil, her sayfa kendi adresinde")

    /** The third: the one codebase every platform is built from. */
    val CodebaseFact = Words("Kotlin codebase for all of them", "Kotlin kod tabanı, hepsi için")

    val Source = Words("Its source on GitHub", "Kaynağı GitHub'da")
    val DownloadForMac = Words("Download for macOS", "macOS için indir")

    /** What the button that visits an app says, with its [address]. */
    fun visit(address: String): Words = Words("Visit $address", "$address adresine git")

    /** What a picture of an app on a desktop and a phone is called, for a screen reader. */
    fun picture(address: String): Words =
        Words("$address on a desktop and on a phone", "$address bir masaüstünde ve bir telefonda")

    /** What the picture of an app's own screen on its card is called, for a screen reader. */
    fun screen(address: String): Words = Words("$address on a desktop", "$address bir masaüstünde")

    val ComponentsTitle = Words("The components it draws with", "Çizdiği bileşenler")

    /** What stands under the components' title: how many of them there are. */
    fun componentsLead(count: Int): Words = Words(
        "$count of the library's components draw its own screens.",
        "Kendi ekranlarını kütüphanenin $count bileşeni çiziyor."
    )

    val MadeTitle = Words("How it is made", "Nasıl yapıldı")
    val YoursTitle = Words("Yours can be next.", "Sıradaki seninki olabilir.")
    val Yours = Words(
        "Built something with Pantrix UI? Open an issue on the site's repository with its name, a link, the " +
            "platforms it runs on and a picture of it. It is added here once you say its name and logo may be shown.",
        "Pantrix UI ile bir şey mi yaptın? Sitenin deposunda adını, bir bağlantısını, çalıştığı platformları ve bir " +
            "resmini veren bir issue aç. Adının ve logosunun gösterilmesine izin verdiğini söyleyince buraya eklenir."
    )
    val AddYours = Words("Add your project", "Projeni ekle")

    val AllTitle = Words("All projects", "Bütün projeler")

    /** How many apps the list shows. */
    fun count(count: Int): Words = Words(
        if (count == 1) "1 project" else "$count projects",
        "$count proje"
    )

    val All = Words("All", "Hepsi")
    val Platform = Words("Platform", "Platform")

    /** How many of the library's components an app's card says it uses. */
    fun components(count: Int): Words = Words(
        if (count == 1) "1 Pantrix component" else "$count Pantrix components",
        "$count Pantrix bileşeni"
    )

    val Visit = Words("Visit", "Aç")
    val Download = Words("Download", "İndir")
    val YoursHereTitle = Words("Your project here", "Senin projen burada")
    val YoursHere = Words(
        "Open an issue on the site's repository with its name, a link, the platforms it runs on and a picture. It is " +
            "added once you say its name and logo may be shown.",
        "Sitenin deposunda adını, bir bağlantısını, çalıştığı platformları ve bir resmini veren bir issue aç. Adının " +
            "ve logosunun gösterilmesine izin verdiğini söyleyince eklenir."
    )

    /** What the filter calls [kind]. */
    fun platform(kind: PlatformKind): Words = when (kind) {
        PlatformKind.Android -> Words("Android", "Android")
        PlatformKind.Ios -> Words("iOS", "iOS")
        PlatformKind.Desktop -> Words("Desktop", "Masaüstü")
        PlatformKind.Web -> Words("Web", "Web")
    }

    /** Where a project is added: a new issue on the site's repository, from its form for references. */
    const val ADD_PROJECT = "${SiteDownloads.REPOSITORY}/issues/new?template=reference.yml"
}
