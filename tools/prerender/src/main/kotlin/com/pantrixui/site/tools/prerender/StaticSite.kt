package com.pantrixui.site.tools.prerender

import com.pantrixui.site.core.model.Language
import java.io.File

/**
 * The site's static files, written beside the web app: a page's `index.html` in the folder of its address, the
 * `404.html` for an address with no page, and the `sitemap.xml` and `robots.txt` search engines read.
 *
 * @param pages each language's pages.
 * @param document the app's `index.html` that each page's file is written from.
 * @param site the site's address, with no slash at its end: `https://pantrixui.com`.
 */
internal class StaticSite(
    private val pages: Map<Language, List<StaticPage>>,
    private val document: StaticDocument,
    private val site: String
) {
    init {
        val routes = pages.values.map { list -> list.map { it.route }.toSet() }
        require(routes.all { it == routes.first() }) { "Every page is to be written in every language" }
    }

    /** Writes the files under [out], the web app's folder, and answers how many pages it wrote. */
    fun write(out: File): Int {
        var written = 0
        pages.forEach { (language, list) ->
            list.forEach { page ->
                fileOf(out, page.address).apply { parentFile.mkdirs() }.writeText(document.of(page, otherOf(page)))
                written++
            }
            if (language == Language.English) File(out, "404.html").writeText(document.notFound(language))
        }
        File(out, "sitemap.xml").writeText(sitemap())
        File(out, "robots.txt").writeText("User-agent: *\nAllow: /\n\nSitemap: $site/sitemap.xml\n")
        return written
    }

    /** Every page's address, each with its address in each language. */
    fun sitemap(): String {
        val urls = pages.values.flatten().joinToString("") { page ->
            val links = listOf(page, otherOf(page)).sortedBy { it.language }.joinToString("") {
                "\n    <xhtml:link rel=\"alternate\" hreflang=\"${it.language.code}\" " +
                    "href=\"${Html.text(site + it.address)}\"/>"
            }
            "\n  <url>\n    <loc>${Html.text(site + page.address)}</loc>$links\n  </url>"
        }
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\" " +
            "xmlns:xhtml=\"http://www.w3.org/1999/xhtml\">$urls\n</urlset>\n"
    }

    private fun otherOf(page: StaticPage): StaticPage {
        val other = Language.entries.first { it != page.language }
        return pages.getValue(other).first { it.route == page.route }
    }

    companion object {
        /** The file of the page at [address] under [out]: `/` is `index.html`, `/tr/docs/forms` in `tr/docs/forms`. */
        fun fileOf(
            out: File,
            address: String
        ): File = File(out, address.trim('/').let { if (it.isEmpty()) "index.html" else "$it/index.html" })
    }
}
