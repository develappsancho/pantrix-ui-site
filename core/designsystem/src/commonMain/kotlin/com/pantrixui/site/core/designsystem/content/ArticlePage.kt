package com.pantrixui.site.core.designsystem.content

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.pantrixui.components.elements.scrollbar.pantrixVerticalScroll
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.i18n.LocalSiteStrings
import com.pantrixui.site.core.model.ContentsEntry
import kotlinx.coroutines.launch

/**
 * A page of the documentation as its design lays it out: the article, [articleWidth] wide at most on a desktop and as
 * wide as a tablet's or a phone's window leaves it, and beside it, where [contentsBeside] says there is room, its "On
 * this page" list. The list stays in view as the article scrolls, marks the heading being read and scrolls to the one
 * chosen.
 *
 * @param contents the page's "On this page" list; the article marks each heading with [Modifier.pageAnchor].
 * @param modifier the page's modifier.
 * @param scrollState how far the page is scrolled.
 * @param anchors where the article's headings stand, as [Modifier.pageAnchor] records them.
 * @param articleWidth the widest the article is laid out on a desktop: 760 for a page, 780 for a guide.
 * @param contentsTitle what the list beside the article is called, where it is not "On this page".
 * @param contentsFooter what the list holds after the headings: links of the page's own.
 * @param article what the page says: its head, then its content.
 */
@Composable
fun ArticlePage(
    contents: List<ContentsEntry>,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    anchors: PageAnchors = remember { PageAnchors() },
    articleWidth: Dp = ARTICLE_WIDTH,
    contentsTitle: String? = null,
    contentsFooter: @Composable ColumnScope.() -> Unit = {},
    article: @Composable ColumnScope.() -> Unit
) {
    val layout = SiteTheme.layout
    val showContents = contentsBeside(contents)
    val readingLine = with(LocalDensity.current) { READING_LINE.roundToPx() }
    val current by remember(contents, anchors, scrollState) {
        derivedStateOf { currentHeading(contents, anchors, scrollState, readingLine) }
    }
    val scrollTo = rememberScrollToAnchor(anchors, scrollState)
    Box(modifier.fillMaxSize().pantrixVerticalScroll(scrollState)) {
        Row(
            Modifier
                .fillMaxWidth()
                .pageAnchorRoot(anchors)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                .padding(layout.pagePadding)
        ) {
            // A desktop's article has a widest; a tablet's and a phone's are as wide as the window leaves them.
            val width = if (layout == SiteLayout.Desktop) {
                Modifier.widthIn(max = articleWidth)
            } else {
                Modifier.fillMaxWidth()
            }
            Column(Modifier.weight(1f, fill = false).then(width), content = article)
            if (showContents) {
                Spacer(Modifier.width(CONTENTS_GAP))
                OnThisPage(
                    entries = contents,
                    current = current,
                    onSelect = scrollTo,
                    title = contentsTitle ?: LocalSiteStrings.current.onThisPage,
                    footer = contentsFooter,
                    // It stays where it is as the page scrolls under it: a sticky column, as on a web page.
                    modifier = Modifier
                        .width(CONTENTS_WIDTH)
                        .offset { IntOffset(0, scrollState.value) }
                        .padding(top = 8.dp)
                )
            }
        }
    }
}

/**
 * Whether a page's "On this page" list of [contents] stands beside its article: on a desktop window at least 1180
 * wide, where it leaves the article its room. Elsewhere a page that has the list shows it in its head.
 */
@Composable
fun contentsBeside(contents: List<ContentsEntry>): Boolean = SiteTheme.layout == SiteLayout.Desktop &&
    PantrixTheme.window.size.width >= CONTENTS_FROM && contents.isNotEmpty()

/**
 * What choosing a section of a page does: [scrollState] scrolls until the section's heading, as [anchors] records it,
 * stands a little above the line a heading is read at.
 */
@Composable
fun rememberScrollToAnchor(
    anchors: PageAnchors,
    scrollState: ScrollState
): (String) -> Unit {
    val readingLine = with(LocalDensity.current) { READING_LINE.roundToPx() }
    val scope = rememberCoroutineScope()
    return remember(anchors, scrollState, readingLine, scope) {
        { anchor ->
            anchors.offsetOf(anchor)?.let { offset ->
                scope.launch { scrollState.animateScrollTo((offset - readingLine / 2).coerceAtLeast(0)) }
            }
        }
    }
}

/**
 * The heading being read: the last whose top has passed the reading line, the first while none has, and the last of
 * all at the page's end.
 */
private fun currentHeading(
    contents: List<ContentsEntry>,
    anchors: PageAnchors,
    scrollState: ScrollState,
    readingLine: Int
): String? {
    if (scrollState.maxValue > 0 && scrollState.value >= scrollState.maxValue) return contents.lastOrNull()?.anchor
    val line = scrollState.value + readingLine
    val passed = contents.lastOrNull { entry -> anchors.offsetOf(entry.anchor)?.let { it <= line } == true }
    return (passed ?: contents.firstOrNull())?.anchor
}

/** The room around a page's article, as each layout's design leaves it. */
val SiteLayout.pagePadding: PaddingValues
    get() = when (this) {
        SiteLayout.Desktop -> PaddingValues(start = 40.dp, end = 40.dp, top = 32.dp, bottom = 80.dp)
        SiteLayout.Tablet -> PaddingValues(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 64.dp)
        SiteLayout.Phone -> PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 48.dp)
    }

/** The widest an article is laid out. */
private val ARTICLE_WIDTH = 760.dp

/** The width of the "On this page" list. */
private val CONTENTS_WIDTH = 200.dp

/** The room between the article and the list. */
private val CONTENTS_GAP = 48.dp

/** The narrowest window the list is shown in: in a narrower one it would squeeze the article. */
private val CONTENTS_FROM = 1180.dp

/** How far below the top of the window a heading is being read once its top has passed. */
private val READING_LINE = 96.dp
