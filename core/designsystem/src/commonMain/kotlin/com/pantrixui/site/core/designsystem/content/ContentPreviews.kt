package com.pantrixui.site.core.designsystem.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pantrixui.site.core.designsystem.SitePreview
import com.pantrixui.site.core.designsystem.previewCode
import com.pantrixui.site.core.designsystem.previewSpans
import com.pantrixui.site.core.model.Block
import com.pantrixui.site.core.model.ContentsEntry
import com.pantrixui.site.core.model.Tone

@Preview(widthDp = PART_WIDTH, heightDp = PART_HEIGHT)
@Composable
private fun PageHeaderPreview() {
    PageHeaderSample(dark = false)
}

@Preview(widthDp = PART_WIDTH, heightDp = PART_HEIGHT)
@Composable
private fun PageHeaderDarkPreview() {
    PageHeaderSample(dark = true)
}

@Preview(widthDp = PART_WIDTH, heightDp = PART_HEIGHT)
@Composable
private fun ContentBlocksPreview() {
    ContentBlocksSample(dark = false)
}

@Preview(widthDp = PART_WIDTH, heightDp = PART_HEIGHT)
@Composable
private fun ContentBlocksDarkPreview() {
    ContentBlocksSample(dark = true)
}

@Preview(widthDp = PART_WIDTH, heightDp = PART_HEIGHT)
@Composable
private fun CodeViewPreview() {
    CodeViewSample(dark = false)
}

@Preview(widthDp = PART_WIDTH, heightDp = PART_HEIGHT)
@Composable
private fun CodeViewDarkPreview() {
    CodeViewSample(dark = true)
}

@Preview(widthDp = PART_WIDTH, heightDp = FILTERS_HEIGHT)
@Composable
private fun FiltersPreview() {
    FiltersSample(dark = false)
}

@Preview(widthDp = PART_WIDTH, heightDp = FILTERS_HEIGHT)
@Composable
private fun FiltersDarkPreview() {
    FiltersSample(dark = true)
}

@Preview(widthDp = PART_WIDTH, heightDp = FILTERS_HEIGHT)
@Composable
private fun OnThisPagePreview() {
    OnThisPageSample(dark = false)
}

@Preview(widthDp = PART_WIDTH, heightDp = FILTERS_HEIGHT)
@Composable
private fun OnThisPageDarkPreview() {
    OnThisPageSample(dark = true)
}

@Preview(widthDp = PART_WIDTH, heightDp = FILTERS_HEIGHT)
@Composable
private fun PageFailedPreview() {
    PageFailedSample(dark = false)
}

@Preview(widthDp = PART_WIDTH, heightDp = FILTERS_HEIGHT)
@Composable
private fun PageFailedDarkPreview() {
    PageFailedSample(dark = true)
}

/** The head of a page of the docs: the trail to it, its title, its lead and the button that copies it. */
@Composable
private fun PageHeaderSample(dark: Boolean) {
    SitePreview(dark) {
        PageHeader(
            trail = listOf(Crumb(label = "Docs", href = "/docs"), Crumb(label = "Getting started", href = "/docs")),
            title = "Installation",
            lead = "Add Pantrix to a Compose Multiplatform project and draw its first screen.",
            copied = false,
            onCopy = {},
            onLink = {},
            modifier = Modifier.padding(PADDING.dp)
        )
    }
}

/** The blocks a page is made of: a heading, a paragraph with code in it, a list, a note and a block of code. */
@Composable
private fun ContentBlocksSample(dark: Boolean) {
    SitePreview(dark) {
        ContentBlocks(blocks = BLOCKS, onLink = {}, modifier = Modifier.padding(PADDING.dp))
    }
}

/** A block of code under the name of its file. */
@Composable
private fun CodeViewSample(dark: Boolean) {
    SitePreview(dark) {
        CodeView(code = previewCode(CODE, file = "App.kt"), modifier = Modifier.padding(PADDING.dp))
    }
}

/** The filter of a list: its field, and its chips with how many each leaves, the first chosen. */
@Composable
private fun FiltersSample(dark: Boolean) {
    SitePreview(dark) {
        Column(Modifier.padding(PADDING.dp), verticalArrangement = Arrangement.spacedBy(GAP.dp)) {
            FilterField(
                field = rememberTextFieldState(),
                placeholder = "Filter the components",
                height = FIELD_HEIGHT.dp,
                tag = "filter"
            )
            Row(horizontalArrangement = Arrangement.spacedBy(GAP.dp)) {
                FilterChip(label = "All", count = 100, selected = true, tag = "all", onClick = {})
                FilterChip(label = "Inputs", count = 11, selected = false, tag = "inputs", onClick = {})
                FilterChip(label = "Charts", count = 23, selected = false, tag = "charts", onClick = {})
            }
        }
    }
}

/** The contents of a page beside it, the part on show marked. */
@Composable
private fun OnThisPageSample(dark: Boolean) {
    SitePreview(dark) {
        OnThisPage(
            entries = listOf(
                ContentsEntry(anchor = "requirements", title = "Requirements"),
                ContentsEntry(anchor = "dependency", title = "Add the dependency"),
                ContentsEntry(anchor = "first-screen", title = "Your first screen")
            ),
            current = "dependency",
            onSelect = {},
            modifier = Modifier.padding(PADDING.dp)
        )
    }
}

/** What a page says where it could not be read, with the button that reads it again. */
@Composable
private fun PageFailedSample(dark: Boolean) {
    SitePreview(dark) { PageFailed(onRetry = {}) }
}

/** A short page of the docs, a block of each kind a page has most. */
private val BLOCKS: List<Block> = listOf(
    Block.Heading(level = 2, anchor = "dependency", text = previewSpans("Add the dependency")),
    Block.Paragraph(previewSpans("Pantrix is one dependency in the shared module's common source set.")),
    Block.ItemList(
        ordered = false,
        items = listOf(
            listOf(Block.Paragraph(previewSpans("Android, iOS, the desktop and the web"))),
            listOf(Block.Paragraph(previewSpans("No Material in the library")))
        )
    ),
    Block.Callout(Tone.Info, listOf(Block.Paragraph(previewSpans("The version is the one on the releases page.")))),
    previewCode("implementation(\"com.pantrixui:components:0.1.1\")")
)

/** The code of a first screen. */
private const val CODE =
    "@Composable\nfun App() {\n    PantrixTheme {\n        PantrixButton(text = \"Hello\", onClick = {})\n    }\n}"

/**
 * How wide and tall a part's preview is, how far in from its edges it stands, and the gaps in it. It is wide enough
 * for a desktop's layout, which is the one the parts are drawn for first.
 */
private const val PART_WIDTH = 900

private const val PART_HEIGHT = 420

private const val FILTERS_HEIGHT = 200

private const val PADDING = 24

private const val GAP = 8

private const val FIELD_HEIGHT = 40
