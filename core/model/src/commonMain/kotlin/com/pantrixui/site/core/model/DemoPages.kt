package com.pantrixui.site.core.model

/**
 * The library's demo pages as the site shows them: a component's key, the last part of its page's address, from its
 * name; and where a page of the demo that is not a component's stands on the site. The content tool's search and
 * the static `llms.txt` both lead to the site's pages by it.
 */
object DemoPages {
    /** A component's key from its name, as its page's address ends: `Text Area` is `text-area`. */
    fun componentKey(name: String): String = name.trim()
        .replace(Regex("""(?<=[a-z0-9])(?=[A-Z])"""), "-")
        .replace(Regex("""[^A-Za-z0-9]+"""), "-")
        .trim('-')
        .lowercase()

    /**
     * Where the demo's page [key] stands on the site, a path and the section of it, or null where it is a
     * component's page or one the site does not show: the foundations, the theming page, the adaptive page, a guide
     * or the platforms.
     */
    fun placeOf(key: String): Pair<String, String?>? = PLACES[key]

    private val PLACES: Map<String, Pair<String, String?>> = mapOf(
        "colors" to ("/foundations" to "colour"),
        "typography" to ("/foundations" to "type"),
        "spacing" to ("/foundations" to "spacing"),
        "shapes" to ("/foundations" to "shape"),
        "elevation" to ("/foundations" to "elevation"),
        "motion" to ("/foundations" to "motion"),
        "density" to ("/foundations" to "density"),
        "component-styles" to ("/docs/theming" to "styles"),
        "theme-inside-another" to ("/docs/theming" to "nested"),
        "clipboard" to ("/docs/guides/clipboard" to null),
        "shortcuts" to ("/docs/guides/keyboard-shortcuts" to null),
        "files" to ("/docs/guides/files" to null),
        "analytics" to ("/docs/guides/analytics" to null),
        "analytics-connector" to ("/docs/guides/analytics" to null),
        "diagnostics" to ("/docs/guides/diagnostics" to null),
        "test-tags" to ("/docs/testing" to null),
        "form" to ("/docs/forms" to null),
        "platform" to ("/docs/platforms" to null),
        "haptics" to ("/docs/platforms" to null),
        "accessibility" to ("/docs/platforms" to null),
        "language" to ("/docs/platforms" to null),
        "building-blocks" to ("/docs/platforms" to null)
    ) + listOf(
        "adaptive-window",
        "adaptive-visibility",
        "adaptive-login",
        "adaptive-table",
        "adaptive-two-pane",
        "adaptive-list-detail",
        "adaptive-supporting-pane",
        "adaptive-tabletop",
        "adaptive-fold-overlays"
    ).associateWith { "/adaptive" to null }
}
