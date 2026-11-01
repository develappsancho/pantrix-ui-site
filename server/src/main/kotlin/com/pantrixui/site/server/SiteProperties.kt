package com.pantrixui.site.server

import org.springframework.boot.context.properties.ConfigurationProperties
import java.net.URI

/**
 * The site the server serves, from `pantrix.site` in the application's properties (`application.properties`). It has
 * no defaults of its own: a default would give the class a second constructor, and Spring binds a class with one.
 *
 * @property url the address the site is served at, `SITE_URL`: `http://localhost:4004` on this machine. Empty where
 *   the stack gives none, and the site is served at [builtUrl].
 * @property builtUrl the address the site is built for, `site.url` in `gradle.properties`, which the build writes in
 *   the properties: `https://pantrixui.com`.
 */
@ConfigurationProperties("pantrix.site")
data class SiteProperties(
    val url: String,
    val builtUrl: String
) {
    /** The address the site is served at: [url], or [builtUrl] where it gives none. */
    val address: URI
        get() = URI(url.ifBlank { builtUrl })

    /**
     * The hosts whose web pages may call the MCP server from a browser: the site's own, and the same under `www`. A
     * request from a page of any other host is refused; an assistant's own process sends no origin, and is answered.
     */
    val hosts: Set<String>
        get() = address.host.lowercase().let { setOf(it, "www.$it") }
}
