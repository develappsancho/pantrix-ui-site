package com.pantrixui.site.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A page of content in one language, as the content tool writes it at build time and the app draws it: Markdown is
 * never read while the site runs.
 *
 * @property path the page's address, without the language: `/docs/installation`.
 * @property language the language it is written in.
 * @property title what the page is called: its heading and its document title.
 * @property description its first paragraph: under the heading, and what a search engine shows.
 * @property section the part of the site it belongs to, the first step of its breadcrumb: "Getting started".
 * @property blocks what it says, in order.
 * @property contents its "On this page" list: the headings a reader jumps to.
 * @property markdown the page as Markdown, what its "Copy page" button copies: for a reader's notes or an assistant.
 * @property minutes how long it takes to read, for a page long enough to say so: a guide.
 */
@Serializable
data class Page(
    val path: String,
    val language: Language,
    val title: String,
    val description: String,
    val section: String? = null,
    val blocks: List<Block>,
    val contents: List<ContentsEntry> = emptyList(),
    val markdown: String? = null,
    val minutes: Int? = null
)

/**
 * One line of a page's "On this page" list.
 *
 * @property anchor the id of the heading it jumps to.
 * @property title what the list says, which may be shorter than the heading.
 * @property note what follows the title more quietly: `unreleased` after a version.
 */
@Serializable
data class ContentsEntry(
    val anchor: String,
    val title: String,
    val note: String? = null
)

/** A piece of a page: a heading, a paragraph, a block of code, and the site's own pieces such as steps and cards. */
@Serializable
sealed interface Block {
    /**
     * A heading.
     *
     * @property level 2 for a section, 3 for a part of one.
     * @property anchor its id, what an address after `#` names.
     * @property text what it says.
     * @property contentsTitle what the page's "On this page" list calls it, where that is not its text.
     */
    @Serializable
    @SerialName("heading")
    data class Heading(
        val level: Int,
        val anchor: String,
        val text: List<Span>,
        val contentsTitle: String? = null
    ) : Block

    /** A paragraph of running text. */
    @Serializable
    @SerialName("paragraph")
    data class Paragraph(val text: List<Span>) : Block

    /**
     * A block of code, its lines already split into coloured tokens.
     *
     * @property language what the code is written in: `kotlin`, `bash`, `yaml`.
     * @property file the name its header shows: `settings.gradle.kts`.
     * @property lines the code's lines, each a run of tokens.
     */
    @Serializable
    @SerialName("code")
    data class Code(
        val language: String,
        val file: String? = null,
        val lines: List<List<Token>>
    ) : Block

    /**
     * A list.
     *
     * @property ordered whether its items are numbered.
     * @property items its items, each a run of blocks.
     */
    @Serializable
    @SerialName("list")
    data class ItemList(
        val ordered: Boolean,
        val items: List<List<Block>>
    ) : Block

    /**
     * A table.
     *
     * @property caption what it is called, above it.
     * @property header its column headings, or null for a table of rows alone.
     * @property rows its rows, each a list of cells.
     * @property highlighted the rows set apart from the others, by their places among [rows]: the answer of a table.
     * @property style how its first column is written.
     */
    @Serializable
    @SerialName("table")
    data class Table(
        val caption: String? = null,
        val header: List<List<Span>>? = null,
        val rows: List<List<List<Span>>>,
        val highlighted: List<Int> = emptyList(),
        val style: TableStyle = TableStyle.Paths
    ) : Block

    /**
     * A note set apart from the text.
     *
     * @property tone what kind of note it is.
     * @property blocks what it says.
     */
    @Serializable
    @SerialName("callout")
    data class Callout(
        val tone: Tone,
        val blocks: List<Block>
    ) : Block

    /**
     * Views of the same thing, one shown at a time: the same file for two repositories.
     *
     * @property tabs the views.
     * @property label what the views are of, at the header's start where it is given, the tabs then at its end:
     *   `commonMain`.
     */
    @Serializable
    @SerialName("tabs")
    data class Tabs(
        val tabs: List<Tab>,
        val label: String? = null
    ) : Block

    /** Numbered steps that are done in order. */
    @Serializable
    @SerialName("steps")
    data class Steps(val steps: List<Step>) : Block

    /** Short facts side by side: a label and its value, such as "Kotlin" and "2.4.21 or newer". */
    @Serializable
    @SerialName("facts")
    data class Facts(val facts: List<Fact>) : Block

    /** Links to other pages, each a card with a title and a line about it. */
    @Serializable
    @SerialName("cards")
    data class Cards(val cards: List<Card>) : Block

    /** Rows of a label and what it says: "iOS" and a sentence about it. */
    @Serializable
    @SerialName("rows")
    data class Rows(val rows: List<Row>) : Block

    /**
     * What stands beside a live sketch: a guide's first section, with the sketch that shows its problem beside it.
     *
     * @property sketch the key of the sketch, the guide's own: `keyboard-dismissal`.
     * @property blocks what the section says beside it.
     */
    @Serializable
    @SerialName("aside")
    data class Aside(
        val sketch: String,
        val blocks: List<Block>
    ) : Block

    /**
     * A live sketch as wide as the page: a window or a screen that needs more room than beside the text.
     *
     * @property sketch the key of the sketch, the guide's own: `adaptive`.
     */
    @Serializable
    @SerialName("sketch")
    data class Sketch(val sketch: String) : Block

    /** A line in a quieter, smaller type under what it is about: where to find what was named, or what is not seen. */
    @Serializable
    @SerialName("note")
    data class Note(val text: List<Span>) : Block

    /**
     * Points in a bordered list, each a bold line and a sentence under it, marked with a dash: what a helper
     * leaves.
     */
    @Serializable
    @SerialName("points")
    data class Points(val points: List<Point>) : Block

    /** Boxes side by side, each a title and what it holds: the kinds of test a guide gives. */
    @Serializable
    @SerialName("boxes")
    data class Boxes(val boxes: List<Box>) : Block

    /** What was checked, and how, in a column for each kind: seen, read in the sources, not checked. */
    @Serializable
    @SerialName("checks")
    data class Checks(val groups: List<CheckGroup>) : Block
}

/** How a [Block.Table]'s first column is written. */
@Serializable
enum class TableStyle {
    /** As a file's path, in the code's type: a table of files and what each holds. */
    Paths,

    /** As the row's name, in bold: a table of what a row is and what stands for it. */
    Labels
}

/**
 * One of [Block.Points]'s points.
 *
 * @property title its bold line.
 * @property text the sentence under it.
 */
@Serializable
data class Point(
    val title: List<Span>,
    val text: List<Span>
)

/**
 * One of [Block.Boxes]'s boxes.
 *
 * @property title what it is.
 * @property blocks what it says.
 */
@Serializable
data class Box(
    val title: List<Span>,
    val blocks: List<Block>
)

/**
 * A column of [Block.Checks].
 *
 * @property kind how what it lists was checked.
 * @property title what its head says.
 * @property items what it lists.
 */
@Serializable
data class CheckGroup(
    val kind: CheckKind,
    val title: String,
    val items: List<List<Span>>
)

/** How something a guide says was checked. */
@Serializable
enum class CheckKind {
    /** Seen on a device, a simulator, an emulator or in a browser. */
    Seen,

    /** Read in the sources of Compose or of a platform. */
    Read,

    /** Not checked. */
    Unchecked
}

/** What kind of note a [Block.Callout] is. */
@Serializable
enum class Tone {
    Info,
    Success,
    Warning,
    Danger,

    /** Why something is as it is: the cause behind what a guide shows. */
    Why
}

/**
 * One view of a [Block.Tabs].
 *
 * @property label what its tab says.
 * @property blocks what it shows.
 */
@Serializable
data class Tab(
    val label: String,
    val blocks: List<Block>
)

/**
 * One of [Block.Steps].
 *
 * @property anchor its id.
 * @property title what it is called.
 * @property optional whether it may be left out.
 * @property blocks what it says.
 * @property contentsTitle what the page's "On this page" list calls it, where that is not its title.
 */
@Serializable
data class Step(
    val anchor: String,
    val title: String,
    val optional: Boolean = false,
    val blocks: List<Block>,
    val contentsTitle: String? = null
)

/**
 * One of [Block.Facts].
 *
 * @property label what it is about.
 * @property value what it is.
 */
@Serializable
data class Fact(
    val label: String,
    val value: String
)

/**
 * One of [Block.Cards].
 *
 * @property title what the card says first.
 * @property text the line under it.
 * @property href where it leads: a path of the site or a web address.
 */
@Serializable
data class Card(
    val title: String,
    val text: String,
    val href: String
)

/**
 * One of [Block.Rows].
 *
 * @property label what the row is about.
 * @property text what it says.
 */
@Serializable
data class Row(
    val label: String,
    val text: List<Span>
)

/** A run of text inside a paragraph, a heading or a cell. */
@Serializable
sealed interface Span {
    /**
     * Plain text.
     *
     * @property text what it says.
     * @property strong whether it is set in bold.
     * @property emphasis whether it is set in italics.
     */
    @Serializable
    @SerialName("text")
    data class Text(
        val text: String,
        val strong: Boolean = false,
        val emphasis: Boolean = false
    ) : Span

    /** A piece of code inside a sentence: a name, a file, a value. */
    @Serializable
    @SerialName("code")
    data class Code(val text: String) : Span

    /**
     * A link.
     *
     * @property text what it says.
     * @property href where it leads: a path of the site (`/docs/installation`) or a web address.
     */
    @Serializable
    @SerialName("link")
    data class Link(
        val text: List<Span>,
        val href: String
    ) : Span
}

/**
 * A run of code in one colour.
 *
 * @property text what it says.
 * @property kind what it is, which says its colour.
 */
@Serializable
data class Token(
    val text: String,
    val kind: TokenKind = TokenKind.Plain
)

/** What a [Token] of code is. */
@Serializable
enum class TokenKind {
    Plain,
    Keyword,
    StringLiteral,
    Number,
    Comment,
    Annotation,
    Function,
    Type
}
