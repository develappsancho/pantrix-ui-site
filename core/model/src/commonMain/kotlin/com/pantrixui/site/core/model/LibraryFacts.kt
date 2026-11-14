package com.pantrixui.site.core.model

/**
 * The counts the site says of the library: the sidebar's card, the guides' link, the start cards of the page for an
 * address with no page, the home page's and the landing pages' lines, and the content's (`{{components}}`). The
 * components are counted by the build in the library's own jar; the guides are written here, as the library's README
 * says them.
 */
object LibraryFacts {
    /**
     * How many components the library has: the components of the `com.pantrixui:mcp` jar of the catalogue's version,
     * the list its MCP server answers with, counted at build time (`writeLibraryFacts`). No page writes the number.
     */
    const val COMPONENTS: Int = BUILT_COMPONENT_COUNT

    /** How many of its guides are for apps, in both languages. */
    const val GUIDES: Int = 29

    /** The Compose Multiplatform the library was last checked with, which a guide's foot names. */
    const val COMPOSE_VERSION: String = "1.12.2"

    /** The library's first version, the one each of the first release's components came in, as its page says. */
    const val VERSION: String = "0.1.0"

    /**
     * The library's latest release, which the site's lines install and the files for coding assistants come from:
     * raised with each release. The site's server needs `PantrixMcp.http()`, which came in 0.1.0-alpha03, so the site
     * goes online with that release or a later one.
     */
    const val RELEASE: String = "0.2.1"

    /** The repository the library is distributed from: its packages, its releases and Claude Code's plugin for it. */
    const val PACKAGES_REPOSITORY: String = "develappsancho/pantrix-ui-cmp-package"

    /** The page of the [RELEASE] in [PACKAGES_REPOSITORY]: every file attached to it, and its notes. */
    const val RELEASE_PAGE: String = "https://github.com/$PACKAGES_REPOSITORY/releases/tag/v$RELEASE"

    /** The library's MCP server as one jar that runs by itself, on JDK 17 or newer, attached to the [RELEASE]. */
    const val MCP_JAR: String = "mcp-$RELEASE-all.jar"

    /** The skill `pantrix-ui`, its folder in a zip, attached to the [RELEASE]. */
    const val SKILL_ZIP: String = "pantrix-ui-skill-$RELEASE.zip"

    /** Where [file], one attached to the [RELEASE] such as [MCP_JAR], is downloaded from. */
    fun releaseFile(file: String): String = "https://github.com/$PACKAGES_REPOSITORY/releases/download/v$RELEASE/$file"

    /** The version the roadmap plans after the first, which the changelog's foot leads to. */
    const val PLANNED_VERSION: String = "0.3.0"

    /** The library's repository, where its sources and guides are read and its problems reported. */
    const val REPOSITORY: String = "https://github.com/develappsancho/pantrix-ui-cmp"

    /**
     * Whether anyone can read the [REPOSITORY]. While it is private, the site leads no link to it: no way to report a
     * problem or to edit a page there, no feed of its releases, and a guide's link to one of its files is the file's
     * name alone, unless the site holds a copy of the file.
     */
    const val REPOSITORY_IS_OPEN: Boolean = false
}
