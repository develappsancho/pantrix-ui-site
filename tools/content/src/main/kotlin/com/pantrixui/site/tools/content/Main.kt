package com.pantrixui.site.tools.content

import com.pantrixui.site.core.model.Changelog
import com.pantrixui.site.core.model.ComponentGroups
import com.pantrixui.site.core.model.ComponentIndex
import com.pantrixui.site.core.model.ComponentPage
import com.pantrixui.site.core.model.ContentFiles
import com.pantrixui.site.core.model.GuideCatalogue
import com.pantrixui.site.core.model.GuideTopic
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.LibraryFacts
import com.pantrixui.site.core.model.Page
import com.pantrixui.site.core.model.References
import com.pantrixui.site.core.model.SearchIndex
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import java.io.File
import kotlin.math.ceil

/**
 * Writes the site's content for the app under `--out`: each page of `--pages` (`<language>/<path>.md`), the guides
 * among them with how long each takes to read, and from the library's documents in `--docs`, the `com.pantrixui:mcp`
 * jar, the changelog and the components, their list and each one's page, in both languages. A component the site
 * writes examples of its own for, under `--pages`' `examples` ([SiteExamples]), shows those. The apps built with
 * Pantrix UI come from `--pages`' `references.json` ([ReferencesReader]), one file a language. A page names the site's
 * own address as `{{site}}`, written as `--site` gives it, and how many components the library has as
 * `{{components}}` ([LibraryFacts.COMPONENTS], which the build counts in the same jar). What `--out` held before is
 * replaced, so that a page taken out of the site is gone from it too.
 */
fun main(args: Array<String>) {
    val options = args.toList().chunked(2).associate { it.first() to it.last() }
    val folder = File(options.getValue("--pages"))
    val documents = LibraryDocuments(File(options.getValue("--docs")))
    val out = File(options.getValue("--out"))
    val guides = SiteGuides.read(folder)
    val writer = ContentWriter(out)
    val sitePages = SitePages.read(folder, options.getValue("--site"))
        .map(guides::withMinutes)
        .map(SiteGuides::withCatalogue)
    val written = writer.write(sitePages)
    writer.writeMinutes(guides.minutes)
    val english = ChangelogReader(Language.English).read(documents.text(LibraryDocuments.ENGLISH_CHANGELOG))
    val turkish = ChangelogReader(Language.Turkish, english).read(documents.text(LibraryDocuments.TURKISH_CHANGELOG))
    val upcoming = documents.version()
    writer.writeChangelogs(listOf(english, turkish).map { it.copy(upcoming = upcoming) })
    val library = documents.text(LibraryDocuments.LIBRARY)
    val components = ComponentReader(
        json = library,
        pages = Language.entries.associateWith(documents::pages),
        changelogs = mapOf(Language.English to english, Language.Turkish to turkish),
        siteExamples = SiteExamples.read(File(folder, SiteExamples.FOLDER))
    )
    check(components.functions.size == LibraryFacts.COMPONENTS) {
        "The library lists ${components.functions.size} components and the build counted ${LibraryFacts.COMPONENTS}"
    }
    Language.entries.forEach { writer.writeReferences(it, ReferencesReader.read(folder, it, components.functions)) }
    val indexes = Language.entries.associateWith(components::index)
    val componentPages = Language.entries.associateWith(components::pages)
    val count = Language.entries.sumOf { language ->
        writer.writeComponents(indexes.getValue(language), componentPages.getValue(language))
    }
    val search = SearchIndexer(library, sitePages, indexes, componentPages)
    Language.entries.forEach { writer.writeSearch(search.index(it)) }
    println(
        "Wrote ${written.size} pages, ${guides.minutes.size} guides among them, the changelog and " +
            "${count / Language.entries.size} components to $out"
    )
}

/**
 * The pages the site writes itself, from a folder of `<language>/<path>.md` files. A page names the site's own address
 * as [SITE]: it is written as [site] gives it, so that a new address is one line of `gradle.properties`. It names how
 * many components the library has as [COMPONENTS], so that a release with more says so on every page, and how many
 * groups the site shows them in as [GROUPS], a word in the page's language.
 */
internal object SitePages {
    /** How a page names the site's own address: `{{site}}/mcp`. */
    const val SITE = "{{site}}"

    /** How a page names how many components the library has: `{{components}} components`. */
    const val COMPONENTS = "{{components}}"

    /** How a page names how many groups the site shows the components in, as a word: `in {{groups}} groups`. */
    const val GROUPS = "{{groups}}"

    /** [text] with the library's count of components where it names it. */
    fun withCount(text: String): String = text.replace(COMPONENTS, LibraryFacts.COMPONENTS.toString())

    /** [text], a page in [language], with the counts it names: its components', and its groups' as a word. */
    fun withCounts(
        text: String,
        language: Language
    ): String = withCount(text).replace(GROUPS, inWords(ComponentGroups.keys.size, language))

    /** [count] as a word in [language], as a page writes a number up to twelve: "eight", "sekiz"; its digits above. */
    fun inWords(
        count: Int,
        language: Language
    ): String {
        val words = if (language == Language.Turkish) TURKISH_NUMBERS else ENGLISH_NUMBERS
        return words.getOrNull(count - 1) ?: count.toString()
    }

    private val ENGLISH_NUMBERS = listOf(
        "one",
        "two",
        "three",
        "four",
        "five",
        "six",
        "seven",
        "eight",
        "nine",
        "ten",
        "eleven",
        "twelve"
    )
    private val TURKISH_NUMBERS = listOf(
        "bir",
        "iki",
        "üç",
        "dört",
        "beş",
        "altı",
        "yedi",
        "sekiz",
        "dokuz",
        "on",
        "on bir",
        "on iki"
    )

    fun read(
        folder: File,
        site: String
    ): List<Page> = Language.entries.flatMap { language ->
        val root = File(folder, language.code)
        root.walkTopDown().filter { it.isFile && it.extension == "md" }.sortedBy { it.path }.map { file ->
            val name = file.relativeTo(root).invariantSeparatorsPath.removeSuffix(".md")
            val path = if (name == "index") "/" else "/$name"
            PageReader(language, path).read(withCounts(file.readText().replace(SITE, site), language))
        }.toList()
    }
}

/**
 * The guides the site writes itself, `<language>/docs/guides/<key>.md`: each is one the site lists
 * ([GuideCatalogue]), under the name it lists it by, in both languages, or the build stops saying which is not. How
 * long a guide takes to read is counted on its English, and is the same in both.
 *
 * @property minutes how long each guide takes to read, by its key.
 */
internal class SiteGuides(val minutes: Map<String, Int>) {
    /** [page] with how long it takes to read, where it is a guide. */
    fun withMinutes(page: Page): Page {
        if (!page.path.startsWith(GUIDES)) return page
        val key = page.path.removePrefix(GUIDES)
        val entry =
            checkNotNull(GuideCatalogue.of(key)) { "The site lists no guide called $key: add it to GuideCatalogue" }
        check(page.title == entry.title.of(page.language)) {
            "The guide $key in ${page.language.code} is called \"${page.title}\"; GuideCatalogue calls it " +
                "\"${entry.title.of(page.language)}\""
        }
        return page.copy(minutes = minutes.getValue(key))
    }

    companion object {
        private const val GUIDES = "/docs/guides/"

        /**
         * [page] as it is copied, where it is the guides page: its head, then each topic with its guides, linked and
         * with what each answers, as the page lists them from [GuideCatalogue].
         */
        fun withCatalogue(page: Page): Page {
            if (page.path != GUIDES.trimEnd('/')) return page
            val prefix = if (page.language == Language.English) "" else "/${page.language.code}"
            val topics = GuideTopic.entries.joinToString("\n\n") { topic ->
                val guides = GuideCatalogue.entries.filter { it.topic == topic }.joinToString("\n") { guide ->
                    "- [${guide.title.of(
                        page.language
                    )}]($prefix$GUIDES${guide.key}): ${guide.summary.of(page.language)}"
                }
                "## ${topic.title.of(page.language)}\n\n${topic.note.of(page.language)}\n\n$guides"
            }
            return page.copy(markdown = listOfNotNull(page.markdown, topics).joinToString("\n\n"))
        }

        /** How many words a reader reads in a minute, what a guide's time to read is counted by. */
        private const val WORDS_A_MINUTE = 200.0

        private val FENCED_CODE = Regex("```.*?```", RegexOption.DOT_MATCHES_ALL)
        private val WORD = Regex("""\w+""")

        /** The guides of [folder]: every one there in both languages, or the build stops. */
        fun read(folder: File): SiteGuides {
            fun keys(language: Language) = File(folder, "${language.code}$GUIDES").listFiles().orEmpty()
                .filter { it.extension == "md" }.associate { it.nameWithoutExtension to it }
            val english = keys(Language.English)
            val turkish = keys(Language.Turkish)
            check(english.keys == turkish.keys) {
                "Guides in one language only: ${(english.keys - turkish.keys) + (turkish.keys - english.keys)}"
            }
            val unwritten = GuideCatalogue.entries.map { it.key } - english.keys
            if (unwritten.isNotEmpty()) println("Guides the site lists and has not written yet: $unwritten")
            return SiteGuides(english.mapValues { (_, file) -> minutesOf(file.readText()) }.toSortedMap())
        }

        /** How many minutes [source], a guide, takes to read: its words outside its code, at least one minute. */
        fun minutesOf(source: String): Int {
            val words = WORD.findAll(FENCED_CODE.replace(source, "")).count()
            return maxOf(1, ceil(words / WORDS_A_MINUTE).toInt())
        }
    }
}

/** Writes the content where the app reads it. */
internal class ContentWriter(private val out: File) {
    /** Writes [pages], in place of everything written before. */
    fun write(pages: List<Page>): List<File> {
        out.deleteRecursively()
        return pages.map { page ->
            File(out, ContentFiles.pageFile(page.language, page.path)).apply {
                parentFile.mkdirs()
                writeText(ContentFiles.json.encodeToString(Page.serializer(), page))
            }
        }
    }

    /** Writes how long each guide takes to read, by its key. */
    fun writeMinutes(minutes: Map<String, Int>) {
        File(out, ContentFiles.GUIDE_MINUTES).apply { parentFile.mkdirs() }.writeText(
            ContentFiles.json.encodeToString(MapSerializer(String.serializer(), Int.serializer()), minutes)
        )
    }

    /** Writes the list of the components in a language, [index], and each one's page of [pages]; says how many. */
    fun writeComponents(
        index: ComponentIndex,
        pages: List<ComponentPage>
    ): Int {
        File(out, ContentFiles.componentIndexFile(index.language)).apply { parentFile.mkdirs() }
            .writeText(ContentFiles.json.encodeToString(ComponentIndex.serializer(), index))
        pages.forEach { page ->
            File(out, ContentFiles.componentFile(page.language, page.card.key)).apply { parentFile.mkdirs() }
                .writeText(ContentFiles.json.encodeToString(ComponentPage.serializer(), page))
        }
        return pages.size
    }

    /** Writes what the site's search finds in one language. */
    fun writeSearch(index: SearchIndex) {
        File(out, ContentFiles.searchFile(index.language)).apply { parentFile.mkdirs() }
            .writeText(ContentFiles.json.encodeToString(SearchIndex.serializer(), index))
    }

    /** Writes the apps built with Pantrix UI in [language]. */
    fun writeReferences(
        language: Language,
        references: References
    ) {
        File(out, ContentFiles.referencesFile(language)).apply { parentFile.mkdirs() }
            .writeText(ContentFiles.json.encodeToString(References.serializer(), references))
    }

    /** Writes each of [changelogs], one a language. */
    fun writeChangelogs(changelogs: List<Changelog>) {
        changelogs.forEach { changelog ->
            File(out, ContentFiles.changelogFile(changelog.language)).apply { parentFile.mkdirs() }
                .writeText(ContentFiles.json.encodeToString(Changelog.serializer(), changelog))
        }
    }
}
