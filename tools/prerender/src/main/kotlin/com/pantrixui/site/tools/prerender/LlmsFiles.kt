package com.pantrixui.site.tools.prerender

import com.pantrixui.site.core.model.ContentFiles
import com.pantrixui.site.core.model.DemoPages
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.util.zip.ZipFile

/**
 * The library's documents for coding assistants at the site's root: `llms.txt` and `llms-full.txt`, as the
 * `com.pantrixui:mcp` jar holds them, with `llms.txt`'s links led to the site's pages, so an assistant reads what a
 * reader sees. A document `llms.txt` leads to that the site has no page for (a module's API, the skill and what it
 * refers to, the library as JSON) is served under `/llms/`, as the jar holds it.
 *
 * @param jar the `com.pantrixui:mcp` jar.
 * @param site the site's address, with no slash at its end.
 */
internal class LlmsFiles(
    private val jar: File,
    private val site: String
) {
    /** Writes the files under [out], the web app's folder. */
    fun write(out: File) = ZipFile(jar).use { zip ->
        fun text(name: String) = zip.getInputStream(zip.getEntry(ROOT + name)).readBytes().decodeToString()
        val index = relinked(text(INDEX), componentPages(text(LIBRARY)))
        File(out, INDEX).writeText(index)
        File(out, FULL).writeText(text(FULL))
        // What the index leads to under /llms/, a file, or the skill's folder with the references its file leads to.
        val served = SERVED.findAll(index).map { it.groupValues[1] }
            .map { if (it.startsWith(SKILLS)) it.substringBeforeLast('/') + "/" else it }
            .toSet()
        zip.entries().asSequence()
            .filter { entry -> !entry.isDirectory && served.any { entry.name.removePrefix(ROOT).startsWith(it) } }
            .forEach { entry ->
                File(out, LLMS + entry.name.removePrefix(ROOT)).apply { parentFile.mkdirs() }
                    .writeBytes(zip.getInputStream(entry).readBytes())
            }
    }

    /** [text] with each of its links led to the site: the component pages by [pages], the demo's page key to its. */
    fun relinked(
        text: String,
        pages: Map<String, String>
    ): String {
        var inCode = false
        return text.lines().joinToString("\n") { line ->
            val fence = line.trimStart().startsWith(FENCE)
            val kept = if (fence || inCode) {
                line
            } else {
                LINK.replace(line) { found -> "[${found.groupValues[1]}](${addressOf(found.groupValues[2], pages)})" }
            }
            if (fence) inCode = !inCode
            kept
        }
    }

    /** Where [target], a link of `llms.txt`, leads on the site. */
    fun addressOf(
        target: String,
        pages: Map<String, String>
    ): String {
        if ("://" in target) return target
        val key = target.substringAfterLast('/').removeSuffix(MARKDOWN)
        val path = when {
            target == FULL -> "/$FULL"

            target == CHANGELOG -> "/changelog"

            target.startsWith(GUIDES) -> "/docs/guides/$key"

            target.startsWith(PAGES) -> pages[key]?.let { "/components/$it" }
                ?: DemoPages.placeOf(key)?.let { (place, section) -> place + section?.let { "#$it" }.orEmpty() }
                ?: "/$LLMS$target"

            else -> "/$LLMS$target"
        }
        return site + path
    }

    /** Each demo page's key to the key of the first component it documents, from the library as JSON. */
    private fun componentPages(json: String): Map<String, String> =
        ContentFiles.json.parseToJsonElement(json).jsonObject.getValue("components").jsonArray
            .map { it.jsonObject }
            .groupBy { it.getValue("page").jsonPrimitive.content }
            .mapValues { (_, components) ->
                DemoPages.componentKey(components.first().getValue("name").jsonPrimitive.content)
            }

    private companion object {
        const val ROOT = "pantrix-ui/"
        const val LLMS = "llms/"
        const val INDEX = "llms.txt"
        const val FULL = "llms-full.txt"
        const val LIBRARY = "pantrix-ui.json"
        const val CHANGELOG = "CHANGELOG.md"
        const val GUIDES = "guides/"
        const val PAGES = "pages/"
        const val MARKDOWN = ".md"
        const val FENCE = "```"
        const val SKILLS = "skills/"
        val LINK = Regex("""(?<!!)\[([^\]]*)]\(([^)\s]+)\)""")
        val SERVED = Regex("""\]\([^)\s]*/$LLMS([^)\s]+)\)""")
    }
}
