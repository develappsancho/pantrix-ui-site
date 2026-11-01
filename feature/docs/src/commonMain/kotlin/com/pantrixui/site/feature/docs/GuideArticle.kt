package com.pantrixui.site.feature.docs

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pantrixui.foundation.platform.PantrixWindowTitle
import com.pantrixui.site.core.designsystem.content.ArticlePage
import com.pantrixui.site.core.designsystem.content.ContentBlocks
import com.pantrixui.site.core.designsystem.content.ContentStyle
import com.pantrixui.site.core.designsystem.content.Crumb
import com.pantrixui.site.core.designsystem.content.LocalSketches
import com.pantrixui.site.core.designsystem.content.Neighbour
import com.pantrixui.site.core.designsystem.content.PageAnchors
import com.pantrixui.site.core.designsystem.content.PageFoot
import com.pantrixui.site.core.designsystem.content.PageHeader
import com.pantrixui.site.core.designsystem.content.PageTag
import com.pantrixui.site.core.designsystem.content.PageTagTone
import com.pantrixui.site.core.designsystem.content.contentsBeside
import com.pantrixui.site.core.designsystem.content.rememberScrollToAnchor
import com.pantrixui.site.core.designsystem.content.sectionGap
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.i18n.LocalSiteStrings
import com.pantrixui.site.core.i18n.SiteStrings
import com.pantrixui.site.core.model.GuideCatalogue
import com.pantrixui.site.core.model.GuideEntry
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.LibraryFacts
import com.pantrixui.site.core.model.Page
import com.pantrixui.site.core.model.Route
import com.pantrixui.site.core.routing.SiteRoutes
import com.pantrixui.site.feature.docs.sketch.GuideSketches

/**
 * One of the library's guides as its design lays it out: the trail through the guides, its tags, its sections with
 * their live sketches, and a foot that leads to the text it is written from and to the guides before and after it.
 */
@Composable
internal fun GuideArticle(
    page: Page,
    guide: GuideEntry,
    copied: Boolean,
    onAction: (DocAction) -> Unit,
    modifier: Modifier
) {
    val anchors = remember(page) { PageAnchors() }
    val scrollState = rememberScrollState()
    val scrollTo = rememberScrollToAnchor(anchors, scrollState)
    val onLink: (String) -> Unit = { onAction(DocAction.OpenLink(it)) }
    val language = LocalLanguage.current
    val strings = LocalSiteStrings.current
    val gap = sectionGap(ContentStyle.Guide)
    PantrixWindowTitle(page.title)
    ArticlePage(
        contents = page.contents,
        modifier = modifier,
        scrollState = scrollState,
        anchors = anchors,
        articleWidth = GUIDE_WIDTH
    ) {
        PageHeader(
            trail = listOf(
                Crumb(strings.docs, SiteRoutes.addressOf(Route.Doc(FIRST_PAGE), language)),
                Crumb(strings.guides, SiteRoutes.addressOf(Route.Guides, language))
            ),
            title = page.title,
            lead = page.description.ifEmpty { null },
            copied = copied,
            onCopy = { onAction(DocAction.CopyPage) },
            onLink = onLink,
            tags = guideTags(guide, page.minutes, strings),
            contents = if (contentsBeside(page.contents)) emptyList() else page.contents,
            onContents = scrollTo,
            style = ContentStyle.Guide,
            fullTrail = true
        )
        Spacer(Modifier.height(gap))
        CompositionLocalProvider(LocalSketches provides GuideSketches) {
            ContentBlocks(page.blocks, onLink, anchors = anchors, style = ContentStyle.Guide)
        }
        Spacer(Modifier.height(gap))
        PageFoot(
            editLabel = strings.editOnGitHub,
            editHref = guide.source(language),
            checkedWith = strings.checkedWith(LibraryFacts.COMPOSE_VERSION),
            previousLabel = strings.previous,
            previous = GuideCatalogue.previousOf(guide.key)?.neighbour(language),
            nextLabel = strings.next,
            next = GuideCatalogue.nextOf(guide.key)?.neighbour(language),
            onLink = onLink
        )
    }
}

/** What a guide's tags say: how long it takes to read, whom its problem is for, where it is seen, how it is fixed. */
internal fun guideTags(
    guide: GuideEntry,
    minutes: Int?,
    strings: SiteStrings
): List<PageTag> = listOfNotNull(
    minutes?.let { PageTag(strings.minutesToRead(it)) },
    PageTag(strings.everyComposeApp, PageTagTone.Accent).takeIf { guide.everyApp },
    guide.seenOn.takeIf { it.isNotEmpty() }?.let { PageTag(strings.seenOn(it.toList()), PageTagTone.Success) },
    PageTag(strings.alsoWithoutPantrix).takeIf { guide.withoutPantrix }
)

private fun GuideEntry.neighbour(language: Language) =
    Neighbour(title.of(language), SiteRoutes.addressOf(Route.Guide(key), language))

/** The widest a guide's article is laid out on a desktop. */
private val GUIDE_WIDTH = 780.dp
