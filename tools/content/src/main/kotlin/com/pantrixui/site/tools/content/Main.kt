package com.pantrixui.site.tools.content

import com.pantrixui.site.core.model.Changelog
import com.pantrixui.site.core.model.ComponentIndex
import com.pantrixui.site.core.model.ComponentPage
import com.pantrixui.site.core.model.ContentFiles
import com.pantrixui.site.core.model.GuideCatalogue
import com.pantrixui.site.core.model.GuideTopic
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Page
import com.pantrixui.site.core.model.SearchIndex
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import java.io.File
import kotlin.math.ceil

/**
 * Writes the site's content for the app under `--out`: each page of `--pages` (`<language>/<path>.md`), the guides
 * among them with how long each takes to read, and from the library's documents in `--docs`, the `com.pantrixui:mcp`
 * jar, the changelog and the components, their list and each one's page, in both languages. A component the site
 * writes examples of its own for, under `--pages`' `examples` ([SiteExamples]), shows those. What `--out` held before
 * is replaced, so that a page taken out of the site is gone from it too.
 */
fun main(args: Array<String>) {
    val options = args.toList().chunked(2).associate { it.first() to it.last() }
    val folder = File(options.getValue("--pages"))
    val documents = LibraryDocuments(File(options.getValue("--docs")))
    val out = File(options.getValue("--out"))
    val guides = SiteGuides.read(folder)
    val writer = ContentWriter(out)
    val sitePages = SitePages.read(folder).map(guides::withMinutes).map(SiteGuides::withCatalogue)
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

/** The pages the site writes itself, from a folder of `<language>/<path>.md` files. */
internal object SitePages {
    fun read(folder: File): List<Page> = Language.entries.flatMap { language ->
        val root = File(folder, language.code)
        root.walkTopDown().filter { it.isFile && it.extension == "md" }.sortedBy { it.path }.map { file ->
            val name = file.relativeTo(root).invariantSeparatorsPath.removeSuffix(".md")
            val path = if (name == "index") "/" else "/$name"
            PageReader(language, path).read(file.readText())
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

    /** Writes each of [changelogs], one a language. */
    fun writeChangelogs(changelogs: List<Changelog>) {
        changelogs.forEach { changelog ->
            File(out, ContentFiles.changelogFile(changelog.language)).apply { parentFile.mkdirs() }
                .writeText(ContentFiles.json.encodeToString(Changelog.serializer(), changelog))
        }
    }
}
