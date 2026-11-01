package com.pantrixui.site.feature.download

import androidx.compose.ui.graphics.vector.ImageVector
import com.pantrixui.site.core.designsystem.BySize
import com.pantrixui.site.core.designsystem.SiteIcons
import com.pantrixui.site.core.model.Words

/** The download page's words, in each language, and in each size where the design shortens them. */
internal object DownloadSay {
    val Kicker = Words("Download", "İndir")
    val Title = Words("Pantrix UI, on your desktop.", "Pantrix UI, masaüstünüzde.")
    val Lead = BySize(
        Words(
            "The whole site as an app: every page in English and Turkish, the components running live in each " +
                "platform’s preset, the theme builder and the adaptive simulator. Built from the same code as this " +
                "site, with Pantrix itself.",
            "Sitenin tamamı bir uygulama olarak: her sayfa İngilizce ve Türkçe, her platformun ön ayarında canlı " +
                "çalışan bileşenler, tema oluşturucu ve uyarlanabilir simülatör. Bu siteyle aynı koddan, Pantrix'in " +
                "kendisiyle yapıldı."
        ),
        phone = Words(
            "The whole site as an app: every page in both languages, the components live, the theme builder and " +
                "the adaptive simulator.",
            "Sitenin tamamı bir uygulama olarak: her sayfa iki dilde, bileşenler canlı, tema oluşturucu ve " +
                "uyarlanabilir simülatör."
        )
    )
    val DownloadForMac = Words("Download for macOS", "macOS için indir")
    val ReleaseNotes = Words("Release notes", "Sürüm notları")

    /** What the app runs on, the end of the line under the buttons. */
    val Runs = Words("Apple silicon · macOS 13 or later", "Apple silicon · macOS 13 ve sonrası")
    val Version = Words("Version", "Sürüm")
    val Signed = Words("Signed and notarized by Apple", "İmzalı ve Apple'ın onayından geçmiş")

    val PlatformsTitle = Words("Every platform it comes to", "Geldiği her platform")
    val PlatformsLead = BySize(
        Words(
            "macOS first. Windows and Linux follow, each built and signed on its own system. On a phone, this site " +
                "is the app.",
            "Önce macOS. Windows ve Linux ardından gelir, her biri kendi sisteminde derlenip imzalanır. Bir " +
                "telefonda bu site uygulamanın kendisidir."
        ),
        phone = Words(
            "macOS first; Windows and Linux follow. On a phone, this site is the app.",
            "Önce macOS; Windows ve Linux ardından gelir. Bir telefonda bu site uygulamanın kendisidir."
        )
    )
    val Available = Words("Available", "Hazır")
    val ComingLater = Words("Coming later", "Sonra gelecek")
    val DownloadDmg = Words("Download .dmg", ".dmg'yi indir")
    val WatchReleases = Words("Watch the releases", "Sürümleri izle")

    /** The platforms the app comes to, in the order the page shows them: macOS, the one it is on now, first. */
    val Platforms = listOf(
        Platform(
            name = "macOS",
            icon = SiteIcons.Laptop,
            body = Words(
                "Apple silicon (M1 and later), macOS 13 or later.",
                "Apple silicon (M1 ve sonrası), macOS 13 ve sonrası."
            ),
            file = null,
            available = true
        ),
        Platform(
            name = "Windows",
            icon = SiteIcons.Panes,
            body = Words(
                "An installer (.msi) for Windows 10 and 11, built and signed on Windows.",
                "Windows 10 ve 11 için bir kurulum dosyası (.msi), Windows'ta derlenip imzalanır."
            ),
            file = ".msi",
            available = false
        ),
        Platform(
            name = "Linux",
            icon = SiteIcons.Terminal,
            body = Words("A package (.deb) for Debian and Ubuntu.", "Debian ve Ubuntu için bir paket (.deb)."),
            file = ".deb",
            available = false
        )
    )

    val InstallTitle = Words("Install it on macOS", "macOS'a kurun")

    /** The steps of installing the app, each its title and what is done, the file's name between its two parts. */
    val Steps = listOf(
        Step(
            title = Words("Open the file", "Dosyayı açın"),
            start = Words("Open ", "İndirilenler klasörünüzden "),
            file = true,
            end = Words(" from your Downloads.", " dosyasını açın.")
        ),
        Step(
            title = Words("Move it to Applications", "Uygulamalar'a taşıyın"),
            start = Words(
                "Drag Pantrix UI onto the Applications folder in the window that opens.",
                "Açılan pencerede Pantrix UI'ı Uygulamalar klasörünün üzerine sürükleyin."
            )
        ),
        Step(
            title = Words("Open it", "Açın"),
            start = Words(
                "Open Pantrix UI from Applications. The first time, macOS asks whether to open an app from the " +
                    "internet: choose Open. Apple has checked it.",
                "Pantrix UI'ı Uygulamalar'dan açın. İlk açılışta macOS, internetten gelen bir uygulamanın açılıp " +
                    "açılmayacağını sorar: Aç'ı seçin. Apple onu denetlemiştir."
            )
        )
    )

    val CheckTitle = Words("Check the download", "İndirileni doğrulayın")
    val CheckBody = Words(
        "Signed with a Developer ID and notarized by Apple. To be sure the file is the one released, compare its " +
            "SHA-256 with the release’s:",
        "Bir Developer ID ile imzalı ve Apple'ın onayından geçmiştir. Dosyanın yayımlanan dosya olduğundan emin " +
            "olmak için SHA-256'sını sürümünkiyle karşılaştırın:"
    )
    val ReadingRelease = Words("Reading the release…", "Sürüm okunuyor…")
    val HashOnReleasePage = Words("The release’s page has its SHA-256.", "SHA-256'sı sürümün sayfasında.")
    val Copy = Words("Copy", "Kopyala")
    val Copied = Words("Copied", "Kopyalandı")
    val CopyHash = Words("Copy the SHA-256", "SHA-256'yı kopyala")

    val FeaturesTitle = Words("What is in the app", "Uygulamada neler var")
    val Features = listOf(
        Feature(
            SiteIcons.Book,
            Words("Every page, offline", "Her sayfa, çevrimdışı"),
            Words(
                "The docs, the guides and the changelog come with the app: nothing to wait for.",
                "Belgeler, rehberler ve değişiklik günlüğü uygulamayla gelir: beklenecek bir şey yok."
            )
        ),
        Feature(
            SiteIcons.Blocks,
            Words("Components, live", "Bileşenler, canlı"),
            Words(
                "Each component’s examples run in its platform’s preset, as on this site.",
                "Her bileşenin örnekleri, bu sitede olduğu gibi kendi platformunun ön ayarında çalışır."
            )
        ),
        Feature(
            SiteIcons.Palette,
            Words("The theme builder", "Tema oluşturucu"),
            Words(
                "Shape a DesignPreset, and copy it as Kotlin.",
                "Bir DesignPreset biçimlendirin, Kotlin olarak kopyalayın."
            )
        ),
        Feature(
            SiteIcons.Globe,
            Words("Both languages", "İki dil"),
            Words("English and Turkish, each page in both.", "İngilizce ve Türkçe, her sayfa ikisinde de.")
        )
    )

    /** The words of the picture of the app's window: the page of a button, as the app shows it. */
    val WindowSave = Words("Save", "Kaydet")
    val WindowCancel = Words("Cancel", "Vazgeç")
}

/**
 * A platform the desktop app comes to.
 *
 * @property name its name, the same in every language.
 * @property icon its mark.
 * @property body what the app asks of it.
 * @property file the kind of file it comes in, where it does not come yet; the one that does names its own file.
 * @property available whether it can be downloaded now.
 */
internal class Platform(
    val name: String,
    val icon: ImageVector,
    val body: Words,
    val file: String?,
    val available: Boolean
)

/**
 * A step of installing the app.
 *
 * @property title what is done, in a few words.
 * @property start how it is done, or its start where the file's name follows.
 * @property file whether the file's name follows [start].
 * @property end what follows the file's name.
 */
internal class Step(
    val title: Words,
    val start: Words,
    val file: Boolean = false,
    val end: Words = Words("", "")
)

/**
 * Something the app has.
 *
 * @property icon its mark.
 * @property title what it is.
 * @property body what it is for.
 */
internal class Feature(
    val icon: ImageVector,
    val title: Words,
    val body: Words
)
