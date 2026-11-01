package com.pantrixui.site.core.model

/**
 * The code a page of the site's own carries for its screen, as its Markdown writes it: the block right under each of
 * its headings, by the heading's anchor. A heading with no code under it has none.
 */
fun Page.codeByHeading(): Map<String, Block.Code> = blocks.withIndex()
    .filter { (_, block) -> block is Block.Heading }
    .mapNotNull { (index, block) ->
        val code = blocks.getOrNull(index + 1) as? Block.Code
        code?.let { (block as Block.Heading).anchor to it }
    }
    .toMap()
