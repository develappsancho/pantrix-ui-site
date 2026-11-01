package com.pantrixui.site.feature.docs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.pantrixui.foundation.platform.PantrixWindowTitle
import com.pantrixui.foundation.platform.rememberPantrixClipboard
import com.pantrixui.site.core.base.BaseScreen
import com.pantrixui.site.core.base.LoadState
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.content.ArticlePage
import com.pantrixui.site.core.designsystem.content.ContentBlocks
import com.pantrixui.site.core.designsystem.content.Crumb
import com.pantrixui.site.core.designsystem.content.PageAnchors
import com.pantrixui.site.core.designsystem.content.PageFailed
import com.pantrixui.site.core.designsystem.content.PageHeader
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.GuideCatalogue
import com.pantrixui.site.core.model.GuideEntry
import com.pantrixui.site.core.model.Page
import com.pantrixui.site.core.model.Route
import com.pantrixui.site.core.routing.SiteRoutes
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * A page of the documentation, with its model: what the site's navigation shows for [route], a page or a guide.
 *
 * @param route the page: a [Route.Doc] or a [Route.Guide].
 * @param onNavigate what opening a page of the site does, given its path.
 * @param onSearch what opening the search does, from the page shown where the site has no such page.
 */
@Composable
fun DocRoute(
    route: Route,
    onNavigate: (String) -> Unit,
    onSearch: () -> Unit
) {
    val language = LocalLanguage.current
    // A page in another language is another model: the one in this entry's store for it, or a new one.
    val viewModel = koinViewModel<DocViewModel>(key = "doc_${language.code}") { parametersOf(route, language) }
    val clipboard = rememberPantrixClipboard()
    val uriHandler = LocalUriHandler.current
    BaseScreen(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                is DocEffect.Copy -> viewModel.onAction(DocAction.Copied(clipboard.copyText(effect.text)))
                is DocEffect.Navigate -> onNavigate(effect.path)
                is DocEffect.OpenUrl -> uriHandler.openUri(effect.url)
            }
        }
    ) { state, onAction ->
        DocScreen(
            state = state,
            path = SiteRoutes.pathOf(route),
            onAction = onAction,
            onNavigate = onNavigate,
            onSearch = onSearch,
            guide = (route as? Route.Guide)?.let { GuideCatalogue.of(it.key) }
        )
    }
}

/**
 * A page of the documentation, drawn from its [state]: the page once it is read, nothing while it is, and what to do
 * when it could not be.
 *
 * @param state what the page's model holds.
 * @param path the page's address, which the page for an address with no page shows.
 * @param onAction where what the user does goes.
 * @param onNavigate what opening a page of the site does, from the page for an address with no page.
 * @param onSearch what opening the search does, from the same page.
 * @param guide what the site says of the guide the page is, where it is one: its tags and the guides beside it.
 * @param modifier the screen's modifier.
 */
@Composable
fun DocScreen(
    state: DocUiState,
    path: String,
    onAction: (DocAction) -> Unit,
    onNavigate: (String) -> Unit,
    onSearch: () -> Unit,
    guide: GuideEntry? = null,
    modifier: Modifier = Modifier
) {
    when (val page = state.page) {
        // A page is a few kilobytes read from the app's own files: there is nothing to show for the moment it takes.
        LoadState.Loading -> {
            Box(modifier.fillMaxSize())
        }

        is LoadState.Failed -> {
            PageFailed(onRetry = { onAction(DocAction.Retry) }, modifier = modifier)
        }

        is LoadState.Loaded -> {
            val value = page.value
            when {
                value == null -> NotFoundScreen(path, onNavigate, onSearch, modifier = modifier)
                guide != null -> GuideArticle(value, guide, state.copied, onAction, modifier)
                else -> DocArticle(value, state.copied, onAction, modifier)
            }
        }
    }
}

@Composable
private fun DocArticle(
    page: Page,
    copied: Boolean,
    onAction: (DocAction) -> Unit,
    modifier: Modifier
) {
    val anchors = remember(page) { PageAnchors() }
    val onLink: (String) -> Unit = { onAction(DocAction.OpenLink(it)) }
    val language = LocalLanguage.current
    PantrixWindowTitle(page.title)
    ArticlePage(contents = page.contents, modifier = modifier, anchors = anchors) {
        PageHeader(
            trail = listOfNotNull(
                page.section?.let { Crumb(it, SiteRoutes.addressOf(Route.Doc(FIRST_PAGE), language)) }
            ),
            title = page.title,
            lead = page.description.ifEmpty { null },
            copied = copied,
            onCopy = { onAction(DocAction.CopyPage) },
            onLink = onLink
        )
        Spacer(
            Modifier.height(
                when (SiteTheme.layout) {
                    SiteLayout.Desktop -> 40.dp
                    SiteLayout.Tablet -> 36.dp
                    SiteLayout.Phone -> 32.dp
                }
            )
        )
        ContentBlocks(page.blocks, onLink, anchors = anchors)
    }
}

/** The first page of the documentation, where a breadcrumb's first step leads. */
internal const val FIRST_PAGE = "introduction"
