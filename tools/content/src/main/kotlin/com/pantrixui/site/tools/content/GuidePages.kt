package com.pantrixui.site.tools.content

import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.LibraryFacts
import com.pantrixui.site.tools.content.GuideText.CODE_SPAN
import com.pantrixui.site.tools.content.GuideText.LINK
import com.pantrixui.site.tools.content.GuideText.capitalised
import com.pantrixui.site.tools.content.GuideText.collapsed
import com.pantrixui.site.tools.content.GuideText.lower
import com.pantrixui.site.tools.content.GuideText.nominal
import com.pantrixui.site.tools.content.GuideText.pieces
import com.pantrixui.site.tools.content.GuideText.plain
import com.pantrixui.site.tools.content.GuideText.symptomOf

/**
 * The library's guides as the site's guide pages, `content/<language>/docs/guides/<key>.md`: the site's head, with
 * the site's title and a lead cut from the guide's first paragraph; numbered sections whose ids are the same in both
 * languages; headings in sentence case; tables as the site's table piece; links to the site's own guide pages; and
 * the guide's live sketch, beside its symptom or under it.
 *
 * @param guides the keys of the site's guides: a link to one of them leads to its page on the site.
 */
internal class GuidePages(private val guides: Set<String>) {
    /**
     * The page of the guide [key] in [language].
     *
     * @param text the guide in [language], as the library's documents hold it.
     * @param english the guide in English, whose numbered headings name the sections' ids in both languages.
     * @param title what the site calls the guide in [language].
     */
    fun page(
        key: String,
        language: Language,
        text: String,
        english: String,
        title: String
    ): String = collapsed(withSketch(key, converted(key, language, text, headingsOf(english), title), language))

    /** The guide's text in the site's pieces, without its sketch. */
    private fun converted(
        key: String,
        language: Language,
        text: String,
        englishHeadings: List<String>,
        title: String
    ): String {
        val lines = text.split("\n")
        val rest = lines.drop(lines.indexOfFirst { it.startsWith("# ") } + 1)
            .filterNot { OTHER_LANGUAGE.containsMatchIn(it) }
            .dropWhile { it.isBlank() }
        val first = rest.takeWhile { it.isNotBlank() }.map { it.trim() }
        val description = lead(plain(first.joinToString(" ")), language, key).replace("yazıldıklarını", "yazıldıkları")
        val out = mutableListOf("---", "title: $title", "description: $description", "---")
        val used = mutableSetOf<String>()
        var inCode = false
        var section = 0
        var table = false
        for (line in rest.drop(first.size)) {
            if (line.startsWith(FENCE) || inCode) {
                if (line.startsWith(FENCE)) inCode = !inCode
                out += line
                continue
            }
            val row = line.startsWith("|")
            if (row != table) {
                out += if (row) ":::table" else ":::"
                table = row
            }
            val numbered = SECTION.matchEntire(line)
            val heading = SUBHEADING.matchEntire(line)
            out += when {
                numbered != null -> {
                    val (number, words) = numbered.destructured
                    val english = englishHeadings[section++]
                    val toc = shortToc(number, english, language)?.let { " toc=\"$it\"" }.orEmpty()
                    "## $number. ${sentence(words.trim(), language)} {#${sectionId(english, used)}$toc}"
                }

                heading != null -> {
                    "${heading.groupValues[1]} ${sentence(heading.groupValues[2].trim(), language)}"
                }

                else -> {
                    links(line)
                }
            }
        }
        if (table) out += ":::"
        return collapsed(out.joinToString("\n").trimEnd() + "\n")
    }

    /**
     * [text] with the guide's sketch: a phone's beside the first section's symptom, in an `:::aside`; a window's, or
     * any where the section has no symptom, as a `:::sketch` under it.
     */
    private fun withSketch(
        key: String,
        text: String,
        language: Language
    ): String {
        val lines = text.split("\n")
        val first = lines.indexOfFirst { it.startsWith("## 1.") }
        val end = (first + 1 until lines.size).firstOrNull { lines[it].startsWith("## ") } ?: lines.size
        val symptom = (first until end).firstOrNull { lines[it].trim() == symptomOf(language) }
        val placed = if (symptom != null) {
            var after = (symptom + 1 until end).firstOrNull { lines[it].startsWith("### ") } ?: end
            while (after > symptom && lines[after - 1].isBlank()) after--
            if (key in PHONE_SKETCHES) {
                lines.take(symptom) + ":::aside $key" + lines.subList(symptom, after) + ":::" + lines.drop(after)
            } else {
                lines.take(after) + listOf("", ":::sketch $key", ":::") + lines.drop(after)
            }
        } else {
            var at = end
            while (at > first && lines[at - 1].isBlank()) at--
            lines.take(at) + listOf("", ":::sketch $key", ":::") + lines.drop(at)
        }
        return placed.joinToString("\n")
    }

    /**
     * [line] with its links led to the site: a guide's to its page, an address as it is, a section's to its text. A
     * link whose words are code is one too; a link written inside code is code, and stays as it is.
     */
    private fun links(line: String): String = pieces(line, LINK_OR_CODE).joinToString("") { (part, matched) ->
        val link = if (matched) LINK.matchEntire(part) else null
        if (link == null) {
            part
        } else {
            val (words, target) = link.destructured
            targetOf(target)?.let { "[$words]($it)" } ?: words
        }
    }

    /**
     * Where a link of a guide leads on the site, or null where it is left as its text: a link within the guide, and
     * one to a file of the library's repository that the site holds no copy of while the repository is private.
     */
    private fun targetOf(target: String): String? = when {
        target.startsWith(REPOSITORY_FILE) -> {
            val path = target.removePrefix(REPOSITORY_FILE)
            SITE_COPIES[path.substringBefore('#')] ?: target.takeIf { LibraryFacts.REPOSITORY_IS_OPEN }
        }

        "://" in target -> {
            target
        }

        target.startsWith("#") -> {
            null
        }

        else -> {
            val guide = GUIDE_LINK.find(target.substringBefore('#'))?.groupValues?.get(1)
            require(guide != null && guide in guides) { "A guide's link leads nowhere on the site: $target" }
            // A link of the site names no language: the site keeps the one it is read in.
            "/docs/guides/$guide"
        }
    }

    companion object {
        /** The guides whose sketch is a phone's, which stands beside the symptom; the others' are windows or wider. */
        val PHONE_SKETCHES = setOf(
            "clipboard",
            "date-time",
            "diagnostics",
            "files",
            "modal",
            "number-format",
            "number-keyboard",
            "scroll-padding",
            "system-bars",
            "testing",
            "text-order"
        )

        /** The numbered headings of [english], the guide in English, in their order. */
        fun headingsOf(english: String): List<String> =
            english.split("\n").mapNotNull { SECTION.matchEntire(it)?.groupValues?.get(2)?.trim() }

        /**
         * [text], a heading, in sentence case: its first word and its names keep their capitals, the rest are small.
         * A heading's number is not its first word, and code keeps its case.
         */
        fun sentence(
            text: String,
            language: Language
        ): String {
            val out = StringBuilder()
            var first = true
            pieces(text, CODE_SPAN).forEach { (part, code) ->
                if (code) {
                    out.append(part)
                    first = false
                    return@forEach
                }
                pieces(part, SPACE).forEach { (word, space) ->
                    val bare = word.trim { it in BARE_EDGES }
                    out.append(
                        when {
                            space || word.isEmpty() -> word
                            first && NUMBER.matches(word) -> word
                            first -> word.also { first = false }
                            bare in KEEP || (bare.length > 1 && bare.uppercase() == bare) -> word
                            CAMEL.containsMatchIn(bare) -> word
                            else -> lower(word, language)
                        }
                    )
                }
            }
            return out.toString()
        }

        /** The id of a section, from its English heading: the same in both languages, and once in a page. */
        fun sectionId(
            english: String,
            used: MutableSet<String>
        ): String {
            val heading = english.lowercase()
            val id = when {
                heading == "the problem" -> "problem"
                heading.startsWith("the solution") || heading == "the fix" -> "fix"
                heading == "with pantrix" -> "with"
                heading.startsWith("without") -> "without"
                "not do" in heading -> "limits"
                heading == "testing" || heading == "tests" -> "testing"
                heading == "pitfalls" -> "pitfalls"
                heading == "verification" || "verified" in heading -> "verification"
                "repository" in heading -> "repository"
                "left open" in heading -> "open"
                else -> heading.replace("`", "").replace(NOT_ID, "-").trim('-')
            }
            var unique = id
            var n = 2
            while (unique in used) unique = "$id-${n++}"
            used += unique
            return unique
        }

        /** A shorter name in the list beside the page, for the headings that are long in every guide. */
        private fun shortToc(
            number: String,
            english: String,
            language: Language
        ): String? {
            val heading = english.lowercase()
            val words = when {
                "not do" in heading -> if (language == Language.English) "What it does not do" else "Yapmadıkları"
                "repository" in heading -> if (language == Language.English) "In the repository" else "Depoda"
                else -> return null
            }
            return "$number. $words"
        }

        /** The guide's first paragraph as the site's lead: what it answers, then whose problem it is, and no more. */
        private fun lead(
            text: String,
            language: Language,
            key: String
        ): String {
            LEADS[key to language]?.let { return it }
            val found = SENTENCE_END.split(text).map { it.trim() }.filter { it.isNotEmpty() }
            var first = found.first()
            val whose: String
            if (language == Language.English) {
                OPENINGS.firstOrNull { first.startsWith(it) }?.let { first = first.removePrefix(it).capitalised() }
                whose = "The problem is not Pantrix"
            } else {
                TURKISH_OPENING.matchEntire(first)?.let { first = (nominal(it.groupValues[1]) + ".").capitalised() }
                whose = "Sorun Pantrix"
            }
            return (listOf(first) + found.drop(1).take(1).filter { it.startsWith(whose) }).joinToString(" ")
        }

        /** The words that keep their capitals inside a heading: names, and words that are capitals throughout. */
        private val KEEP = (
            "Pantrix Compose Multiplatform Android iOS JVM Wasm JS MCP Material Kotlin Maestro Gradle Kotlinter " +
                "Roborazzi Kover PNG SVG PDF API UI Cmd Ctrl Apple Chrome Skia Skiko AWT Robolectric Claude Copilot " +
                "Cursor Unicode ICU CLDR GitHub Xcode macOS Windows Linux Web Pantrix's Pantrix'in Pantrix'e " +
                "Material's Detekt MVI SDK URL HTML CSS WCAG Safari Firefox Figma iPhone iPad Mac RTL LTR II III I"
        ).split(" ").toSet()

        /** Leads that the rules above do not cut well, written by hand. */
        private val LEADS = mapOf(
            ("adaptive-usage" to Language.English) to
                "Which window helper to use, and how, while writing a screen or a component, worked through three " +
                "sample cases.",
            ("adaptive" to Language.Turkish) to
                "PantrixTheme'in içeriğine verdiği pencere bilgisi: ne olduğu, nereden geldiği, nasıl okunduğu ve " +
                "testte nasıl sabitlendiği.",
            ("ai" to Language.Turkish) to
                "Kodlama asistanlarının (Claude, Copilot, Cursor…) Pantrix'i tahminle değil kütüphanenin kendi " +
                "kaynaklarından öğrenmesi için yazılan belgeler.",
            ("chart-export" to Language.Turkish) to
                "Bir Pantrix grafiğinin PNG, SVG ve PDF olarak nasıl dışa aktarıldığı, kütüphanenin neyi yapıp neyi " +
                "uygulamaya bıraktığı, üretilen dosyanın kullanıcıya her platformda nasıl ulaştırılacağı ve üç dosya " +
                "türünün sınırları.",
            ("detekt" to Language.Turkish) to
                "Projede detekt'in neyi denetlediği, nerede çalıştığı, projenin kendi kuralları, bir bulgunun nasıl " +
                "ele alındığı ve Pantrix UI'ı kullanan bir uygulamanın bu kuralları nasıl açacağı.",
            ("material3" to Language.Turkish) to
                "Material 3 kullanan bir uygulamanın Pantrix'e nasıl geçebileceği ya da ikisini bir arada nasıl " +
                "kullanabileceği: iki temanın aynı renkleri, yazıları ve şekilleri konuşması. Sorun Pantrix'e özgü " +
                "değildir.",
            ("web-fonts" to Language.Turkish) to
                "Compose Multiplatform'un web hedefinde bir uygulamanın kendi yazı tipinin neden bir an geç geldiği, " +
                "bunu nasıl ölçtüğümüz ve nasıl giderdiğimiz. Sorun Pantrix'e özgü değildir: web hedefi olan ve " +
                "kendi font dosyasını kullanan her Compose Multiplatform projesinde aynı şekilde görülür."
        )

        private val OPENINGS = listOf("This document explains ", "This document describes ", "This document says ")
        private const val FENCE = "```"
        private const val BARE_EDGES = "():?,.!\"“”"
        private val OTHER_LANGUAGE = Regex("^(Türkçesi|English):")
        private val SECTION = Regex("""## (\d+)\.\s+(.*)""")
        private val SUBHEADING = Regex("""(###+) (.*)""")
        private val GUIDE_LINK = Regex("""(?:^|/)guides/([a-z0-9-]+)\.md$""")

        /** A link or a span of code, whichever starts first: a link inside code is the code's. */
        private val LINK_OR_CODE = Regex("${LINK.pattern}|${CODE_SPAN.pattern}")

        /** Where a link to a file of the library's repository starts: the file's path in it follows. */
        private const val REPOSITORY_FILE = "${LibraryFacts.REPOSITORY}/blob/main/"

        /**
         * The repository's files the site holds a copy of, by their paths in it, and the page of the site that shows
         * each: a guide's link to one of them leads there, whether the repository is open or not. The copies are taken
         * by hand: when one of these files changes in the library, its page is written again.
         */
        val SITE_COPIES = mapOf(
            "config/detekt/detekt.yml" to "/docs/detekt-settings#detekt-yml",
            "config/detekt/demo.yml" to "/docs/detekt-settings#demo-yml",
            "docs/guide/adaptive/adaptive-rules.md" to "/docs/adaptive-rules"
        )
        private val SPACE = Regex("""(?U)\s+""")
        private val NUMBER = Regex("""\d+(\.\d+)*\.?""")
        private val CAMEL = Regex("[a-zçğıöşü][A-Z]")
        private val NOT_ID = Regex("[^a-z0-9]+")
        private val SENTENCE_END = Regex("""(?U)(?<=[.!?])\s+(?=[A-ZÇĞİÖŞÜ0-9])""")
        private val TURKISH_OPENING = Regex("""Bu belge,? (.*?)( açıklar| anlatır| söyler)\.""")
    }
}
