package com.pantrixui.site.feature.references

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.scrollbar.pantrixVerticalScroll
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.platform.PantrixWindowTitle
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.base.BaseScreen
import com.pantrixui.site.core.base.LoadState
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.chrome.SiteFoot
import com.pantrixui.site.core.designsystem.content.LandingBand
import com.pantrixui.site.core.designsystem.content.LandingKicker
import com.pantrixui.site.core.designsystem.content.PageFailed
import com.pantrixui.site.core.designsystem.content.landingHeading
import com.pantrixui.site.core.designsystem.sized
import com.pantrixui.site.core.i18n.LandingHeads
import com.pantrixui.site.core.i18n.LocalLanguage
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** The references page, with its model, which reads the apps built with Pantrix UI in the page's language. */
@Composable
fun ReferencesRoute(onNavigate: (String) -> Unit) {
    val language = LocalLanguage.current
    val viewModel = koinViewModel<ReferencesViewModel>(key = "references_${language.code}") { parametersOf(language) }
    val uriHandler = LocalUriHandler.current
    BaseScreen(
        viewModel,
        onEffect = { effect ->
            when (effect) {
                is ReferencesEffect.Navigate -> onNavigate(effect.path)
                is ReferencesEffect.OpenUrl -> uriHandler.openUri(effect.url)
            }
        }
    ) { state, onAction -> ReferencesScreen(state, onAction) }
}

/**
 * The references page, drawn from its [state], as its design lays it out in each size: what the page is, the app it
 * features, the site, with what it is made of, and where another is added. With more than one app, every other one
 * is listed under them, narrowed by platform, and the list's last card says where another is added.
 *
 * @param state what the page's model holds.
 * @param onAction where what the user does goes.
 * @param modifier the screen's modifier.
 */
@Composable
fun ReferencesScreen(
    state: ReferencesUiState,
    onAction: (ReferencesAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val language = LocalLanguage.current
    PantrixWindowTitle(LandingHeads.References.title.of(language))
    val desktop = SiteTheme.layout == SiteLayout.Desktop
    when (val projects = state.projects) {
        LoadState.Loading -> Box(modifier.fillMaxSize())

        is LoadState.Failed -> PageFailed(onRetry = { onAction(ReferencesAction.Retry) }, modifier = modifier)

        is LoadState.Loaded -> Column(modifier.fillMaxSize().pantrixVerticalScroll(rememberScrollState())) {
            Intro()
            projects.value.firstOrNull()?.let { first ->
                val several = projects.value.size > 1
                Featured(first, several, onAction)
                MadeOf(first)
                if (several) AllProjects(state, onAction) else Yours(onAction)
            }
            if (desktop) SiteFoot()
        }
    }
}

/** What the page is: its kicker, its title and the line under them. */
@Composable
private fun Intro() {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val colors = PantrixTheme.colors
    val padding = when (layout) {
        SiteLayout.Desktop -> PaddingValues(start = 24.dp, end = 24.dp, top = 64.dp, bottom = 40.dp)
        SiteLayout.Tablet -> PaddingValues(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 28.dp)
        SiteLayout.Phone -> PaddingValues(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 28.dp)
    }
    LandingBand(padding, gap = 14.dp) {
        LandingKicker(ReferencesSay.Kicker.of(language))
        PantrixText(
            text = ReferencesSay.Title.of(language),
            style = landingHeading(
                when (layout) {
                    SiteLayout.Desktop -> 48.sp
                    SiteLayout.Tablet -> 40.sp
                    SiteLayout.Phone -> 32.sp
                },
                line = TITLE_LINE,
                spacing = TITLE_SPACING
            ),
            color = colors.textPrimary,
            modifier = Modifier.semantics { heading() }
        )
        PantrixText(
            text = ReferencesSay.Lead.of(language),
            style = SiteTheme.type.lead.sized(if (layout == SiteLayout.Desktop) 18.sp else 16.sp),
            color = colors.textSecondary,
            modifier = Modifier.widthIn(max = LEAD_WIDTH)
        )
    }
}

/** How wide the line under the title grows. */
private val LEAD_WIDTH = 720.dp

private const val TITLE_LINE = 1.08f
private const val TITLE_SPACING = -0.03f
