package com.pantrixui.site.core.model

/**
 * The site's address, which its pages give for the site itself, its MCP server and its `llms.txt`. In the browser it
 * is the address the page was opened at, so that the site on this machine (`http://localhost:4004`) or at a test
 * address leads to itself; in an app, which is opened at no address, it is the one the site is built for, `site.url`
 * in `gradle.properties`.
 */
object SiteAddress {
    /** The address with its scheme and no slash at its end: `https://pantrixui.com`. */
    val origin: String
        get() = servedOrigin()?.takeIf { it.startsWith("https://") || it.startsWith("http://") } ?: BUILT_SITE_ADDRESS

    /** The address without its scheme, with its port where it has one, as a sentence or an address bar gives it. */
    val host: String
        get() = origin.substringAfter("://")

    /** Where the site serves the library's MCP server over HTTP, which an assistant is given instead of the jar. */
    val mcp: String
        get() = "$origin/mcp"

    /** The index of the documents for coding assistants: the site's own `llms.txt`. */
    val llms: String
        get() = "$origin/llms.txt"
}

/** The address the page the app runs in was opened at, in the browser; null in an app. */
internal expect fun servedOrigin(): String?
