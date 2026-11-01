package com.pantrixui.site.feature.ai

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.scrollbar.pantrixVerticalScroll
import com.pantrixui.foundation.platform.PantrixWindowTitle
import com.pantrixui.site.core.base.BaseScreen
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.chrome.SiteFoot
import com.pantrixui.site.core.designsystem.content.FollowPageSection
import com.pantrixui.site.core.designsystem.content.PageAnchors
import com.pantrixui.site.core.designsystem.content.pageAnchor
import com.pantrixui.site.core.designsystem.content.pageAnchorRoot
import com.pantrixui.site.core.designsystem.content.rememberScrollToAnchor
import com.pantrixui.site.core.i18n.LandingHeads
import com.pantrixui.site.core.i18n.LocalLanguage
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * The page for coding assistants, with its model: how an assistant reads Pantrix, a conversation that shows it, the
 * four ways in, their setup, what the server answers, and the rules it keeps.
 */
@Composable
fun AiRoute() {
    val language = LocalLanguage.current
    val viewModel = koinViewModel<AiViewModel>(key = "ai_${language.code}") { parametersOf(language) }
    BaseScreen(viewModel) { state, onAction -> AiScreen(state, onAction) }
}

/**
 * The page for coding assistants, drawn from its [state], as its design lays it out in each size: a tablet and a
 * phone say it shorter and leave some of it out.
 *
 * @param state what the page's model holds.
 * @param onAction where what the user does goes.
 * @param modifier the screen's modifier.
 */
@Composable
fun AiScreen(
    state: AiUiState,
    onAction: (AiAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val language = LocalLanguage.current
    val scroll = rememberScrollState()
    val anchors = remember { PageAnchors() }
    val scrollTo = rememberScrollToAnchor(anchors, scroll)
    FollowPageSection(anchors, scroll)
    PantrixWindowTitle(LandingHeads.Ai.title.of(language))
    Column(modifier.fillMaxSize().pantrixVerticalScroll(scroll)) {
        Column(Modifier.fillMaxWidth().pageAnchorRoot(anchors)) {
            Hero(state, onAction, scrollTo)
            WaysIn(Modifier.pageAnchor(anchors, DOCUMENTS))
            Setup(state, onAction, Modifier.pageAnchor(anchors, SETUP))
            ServerAnswers()
            RulesAndChecks()
            // The design ends a tablet's and a phone's page with its rules; the header keeps the languages there.
            if (SiteTheme.layout == SiteLayout.Desktop) SiteFoot()
        }
    }
}

/** The site's body type at [size] in [weight], its lines as far apart as the page's. */
@Composable
internal fun strong(
    size: TextUnit,
    weight: FontWeight = FontWeight.ExtraBold
): TextStyle = SiteTheme.type.body.copy(fontSize = size, lineHeight = (size.value * LINE).sp, fontWeight = weight)

/** The site's small type at [size], its lines as far apart as the page's. */
@Composable
internal fun small(size: TextUnit): TextStyle =
    SiteTheme.type.bodySmall.copy(fontSize = size, lineHeight = (size.value * LINE).sp)

/** The site's code type at [size] in [weight]. */
@Composable
internal fun mono(
    size: TextUnit,
    weight: FontWeight = FontWeight.Normal
): TextStyle = SiteTheme.type.inlineCode.copy(fontSize = size, fontWeight = weight)

/** The part of the page "Set it up" leads to. */
internal const val SETUP = "setup"

/** The part of the page "Read llms.txt" leads to: the four ways in. */
internal const val DOCUMENTS = "documents"

/** How far apart the page's lines are, in their size. */
private const val LINE = 1.5f
