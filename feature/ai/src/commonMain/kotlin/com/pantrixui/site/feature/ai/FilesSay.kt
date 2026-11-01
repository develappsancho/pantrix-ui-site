package com.pantrixui.site.feature.ai

import com.pantrixui.site.core.model.LibraryFacts
import com.pantrixui.site.core.model.SiteAddress
import com.pantrixui.site.core.model.Words

/**
 * The words of the setup's files: every file the page names, where it is downloaded from or read, and what is done
 * with it. The release's files come from its page on GitHub, the documents from this site. Their sizes are the
 * release's, said roughly: they are said again when the site takes a new release.
 */
internal object FilesSay {
    val Title = Words("The files of Pantrix UI ${LibraryFacts.RELEASE}", "Pantrix UI ${LibraryFacts.RELEASE} dosyaları")
    val Download = Words("Download", "İndir")
    val Open = Words("Open", "Aç")

    /** The files, the release's first and then the site's documents, and the release itself last. */
    val Files: List<FileRow>
        get() = listOf(
            FileRow(
                title = Words("The MCP server's jar", "MCP sunucusunun jar'ı"),
                name = LibraryFacts.MCP_JAR,
                size = Words("about 19 MB", "yaklaşık 19 MB"),
                how = Words(
                    "A client that starts a command runs it, on JDK 17 or newer: ",
                    "Komut başlatan bir istemci onu çalıştırır, JDK 17 ya da üstünde: "
                ),
                code = "java -jar ${LibraryFacts.MCP_JAR}",
                url = LibraryFacts.releaseFile(LibraryFacts.MCP_JAR),
                download = true
            ),
            FileRow(
                title = Words("The skill", "Beceri"),
                name = LibraryFacts.SKILL_ZIP,
                size = Words("about 50 KB", "yaklaşık 50 KB"),
                how = Words(
                    "Unzipped into the project, where the team gets it through git: ",
                    "Projeye açılır, ekip onu git'ten alır: "
                ),
                code = "unzip ${LibraryFacts.SKILL_ZIP} -d .claude/skills",
                url = LibraryFacts.releaseFile(LibraryFacts.SKILL_ZIP),
                download = true
            ),
            FileRow(
                title = Words("The skill, to read", "Beceri, okumak için"),
                name = "SKILL.md",
                size = Words("about 11 KB", "yaklaşık 11 KB"),
                how = Words(
                    "Its rules, and the references beside it that it leads to.",
                    "Kuralları ve yanında durup götürdüğü referanslar."
                ),
                url = SiteAddress.skill
            ),
            FileRow(
                title = Words("The index", "Dizin"),
                name = "llms.txt",
                size = Words("about 40 KB", "yaklaşık 40 KB"),
                how = Words(
                    "What an assistant reads first; its links lead to this site's pages.",
                    "Bir asistanın önce okuduğu; bağlantıları bu sitenin sayfalarına gider."
                ),
                url = SiteAddress.llms
            ),
            FileRow(
                title = Words("Every document", "Bütün belgeler"),
                name = "llms-full.txt",
                size = Words("about 4.7 MB", "yaklaşık 4,7 MB"),
                how = Words(
                    "Every page, guide and API reference in one file, for a wide context.",
                    "Her sayfa, rehber ve API başvurusu tek dosyada, geniş bir bağlam için."
                ),
                url = SiteAddress.llmsFull
            ),
            FileRow(
                title = Words("The library as data", "Veri olarak kütüphane"),
                name = "pantrix-ui.json",
                size = Words("about 5.4 MB", "yaklaşık 5,4 MB"),
                how = Words(
                    "Its pages, its components and every public declaration, for tools and code generators.",
                    "Sayfaları, bileşenleri ve her genel bildirimi; araçlar ve kod üreticileri için."
                ),
                url = SiteAddress.json
            ),
            FileRow(
                title = Words("Its JSON Schema", "JSON Şeması"),
                name = "pantrix-ui.schema.json",
                size = Words("about 19 KB", "yaklaşık 19 KB"),
                how = Words("What pantrix-ui.json holds.", "pantrix-ui.json'ın ne tuttuğu."),
                url = SiteAddress.schema
            ),
            FileRow(
                title = Words("The release", "Sürüm"),
                name = "v${LibraryFacts.RELEASE}",
                size = Words("5 files", "5 dosya"),
                how = Words(
                    "Every file of the release, with its notes, on GitHub.",
                    "Sürümün her dosyası, notlarıyla, GitHub'da."
                ),
                url = LibraryFacts.RELEASE_PAGE
            )
        )
}

/**
 * A file of the setup.
 *
 * @property title what it is.
 * @property name its name.
 * @property size how large it is, roughly.
 * @property how what is done with it, before [code] where it names one.
 * @property code the line that uses it.
 * @property url where it is downloaded from or read.
 * @property download whether it is downloaded, or opened and read.
 */
internal class FileRow(
    val title: Words,
    val name: String,
    val size: Words,
    val how: Words,
    val code: String? = null,
    val url: String,
    val download: Boolean = false
)
