package com.pantrixui.site.core.model

import kotlinx.serialization.Serializable

/**
 * A page of the site, in no language: the same route is shown in English and in Turkish. A route is what the back
 * stack holds, and what an address is made of (`:core:routing`).
 */
@Serializable
sealed interface Route {
    /** The landing page. */
    @Serializable
    data object Home : Route

    /** A page of the documentation the site writes itself: Installation, Introduction, Platforms. */
    @Serializable
    data class Doc(val slug: String) : Route

    /** The guides, by topic. */
    @Serializable
    data object Guides : Route

    /** One of the library's guides, by its key: `keyboard-dismissal`. */
    @Serializable
    data class Guide(val key: String) : Route

    /** The components, by group. */
    @Serializable
    data object Components : Route

    /** One component's page, by its key: `text-area`. */
    @Serializable
    data class Component(val key: String) : Route

    /** The theme's tokens: colours, type, spacing, shapes, motion. */
    @Serializable
    data object Foundations : Route

    /** Adaptive layouts, with the window simulator. */
    @Serializable
    data object Adaptive : Route

    /** The theme builder. */
    @Serializable
    data object Theme : Route

    /** The page for coding assistants. */
    @Serializable
    data object Ai : Route

    /** The roadmap. */
    @Serializable
    data object Roadmap : Route

    /** The desktop app, to download. */
    @Serializable
    data object Download : Route

    /** The changelog. */
    @Serializable
    data object Changelog : Route

    /** An address the site has no page for: [path] is what was asked for. */
    @Serializable
    data class NotFound(val path: String) : Route
}
