package com.pantrixui.site.core.model

/**
 * The counts the site's frame says of the library: the sidebar's card, the guides' link, the start cards of the
 * page for an address with no page. Until the content tool reads the library's components and guides, they are
 * written here, as the library's README says them.
 */
object LibraryFacts {
    /** How many components the library has. */
    const val COMPONENTS: Int = 100

    /** How many of its guides are for apps, in both languages. */
    const val GUIDES: Int = 24

    /** The Compose Multiplatform the library was last checked with, which a guide's foot names. */
    const val COMPOSE_VERSION: String = "1.12.2"

    /** The library's first version, the one every component came in, as a component's page says. */
    const val VERSION: String = "0.1.0"

    /** The version the roadmap plans after the first, which the changelog's foot leads to. */
    const val PLANNED_VERSION: String = "0.2.0"

    /** The library's repository, where its sources and guides are read and its problems reported. */
    const val REPOSITORY: String = "https://github.com/developersancho/pantrix-ui-cmp"
}
