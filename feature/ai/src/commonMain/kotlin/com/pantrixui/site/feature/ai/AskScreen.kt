package com.pantrixui.site.feature.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.chip.ChipStyle
import com.pantrixui.components.elements.chip.PantrixChip
import com.pantrixui.components.elements.icon.PantrixIcon
import com.pantrixui.components.elements.link.PantrixLink
import com.pantrixui.components.elements.scrollbar.pantrixVerticalScroll
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.components.inputs.textfield.PantrixTextField
import com.pantrixui.components.inputs.textfield.TextFieldColors
import com.pantrixui.components.inputs.textfield.TextFieldStyle
import com.pantrixui.foundation.icons.PantrixIcons
import com.pantrixui.foundation.platform.PantrixWindowTitle
import com.pantrixui.foundation.platform.rememberPantrixClipboard
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.foundation.token.shape.ShapeSpec
import com.pantrixui.site.core.base.BaseScreen
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.chrome.SiteFoot
import com.pantrixui.site.core.designsystem.content.LandingBand
import com.pantrixui.site.core.designsystem.content.LandingButton
import com.pantrixui.site.core.designsystem.content.LandingKicker
import com.pantrixui.site.core.designsystem.content.landingCard
import com.pantrixui.site.core.designsystem.content.landingHeading
import com.pantrixui.site.core.designsystem.content.withCode
import com.pantrixui.site.core.designsystem.siteClickable
import com.pantrixui.site.core.designsystem.sized
import com.pantrixui.site.core.i18n.LandingHeads
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.i18n.LocalSiteStrings
import com.pantrixui.site.core.model.AskScope
import com.pantrixui.site.core.model.Route
import com.pantrixui.site.core.model.SiteAddress
import com.pantrixui.site.core.routing.SiteRoutes
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** The page that asks the library's MCP server, with its model, which calls the server's tools. */
@Composable
fun AskRoute(onNavigate: (String) -> Unit) {
    val language = LocalLanguage.current
    val viewModel = koinViewModel<AskViewModel>(key = "ask_${language.code}") { parametersOf(language) }
    val uriHandler = LocalUriHandler.current
    val clipboard = rememberPantrixClipboard()
    BaseScreen(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                is AskEffect.Navigate -> onNavigate(effect.address)
                is AskEffect.OpenUrl -> uriHandler.openUri(effect.url)
                is AskEffect.Copy -> clipboard.copyText(effect.text)
            }
        }
    ) { state, onAction -> AskScreen(state, onAction) }
}

/**
 * The page that asks the library's MCP server, drawn from its [state], as its design lays it out in each size: the
 * question and where it is looked for, the tools called, and the answer; the reader's own assistant; and the model
 * that comes with the reader's own key.
 *
 * @param state what the page's model holds.
 * @param onAction where what the reader does goes.
 * @param modifier the screen's modifier.
 */
@Composable
fun AskScreen(
    state: AskUiState,
    onAction: (AskAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    PantrixWindowTitle(LandingHeads.Ask.title.of(language))
    val padding = when (layout) {
        SiteLayout.Desktop -> PaddingValues(start = 24.dp, end = 24.dp, top = 40.dp, bottom = 80.dp)
        SiteLayout.Tablet -> PaddingValues(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 64.dp)
        SiteLayout.Phone -> PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 48.dp)
    }
    val gap = when (layout) {
        SiteLayout.Desktop -> 24.dp
        SiteLayout.Tablet -> 18.dp
        SiteLayout.Phone -> 14.dp
    }
    Column(modifier.fillMaxSize().pantrixVerticalScroll(rememberScrollState())) {
        LandingBand(padding, gap = gap, lineUnder = false) {
            Head(onAction)
            Tabs(state.tab, onAction)
            when (state.tab) {
                AskTab.Server -> ServerPanel(state, onAction)
                AskTab.Assistant -> AssistantPanel(onAction)
                AskTab.Key -> KeyPanel(onAction)
            }
        }
        if (layout == SiteLayout.Desktop) SiteFoot()
    }
}

/** The way back to the page for coding assistants, the kicker, the title and the lead. */
@Composable
private fun Head(onAction: (AskAction) -> Unit) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val colors = PantrixTheme.colors
    val (title, line) = when (layout) {
        SiteLayout.Desktop -> 46.sp to DESKTOP_TITLE_LINE
        SiteLayout.Tablet -> 40.sp to DESKTOP_TITLE_LINE
        SiteLayout.Phone -> 32.sp to PHONE_TITLE_LINE
    }
    Column(
        Modifier.widthIn(max = HEAD_WIDTH),
        verticalArrangement = Arrangement.spacedBy(if (layout == SiteLayout.Phone) 10.dp else 12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            PantrixLink(
                text = LocalSiteStrings.current.forAi,
                onClick = { onAction(AskAction.Open(SiteRoutes.addressOf(Route.Ai, language))) },
                style = small(14.sp).copy(fontWeight = FontWeight.SemiBold),
                color = SiteTheme.colors.muted,
                tagKey = "askBack",
                modifier = Modifier.heightIn(min = if (layout == SiteLayout.Desktop) 0.dp else TOUCH)
            )
            PantrixText(text = "/", style = small(14.sp), color = SiteTheme.colors.muted)
            PantrixText(
                text = AskSay.Here.of(language),
                style = small(14.sp).copy(fontWeight = FontWeight.SemiBold),
                color = colors.textSecondary
            )
        }
        LandingKicker(AskSay.Kicker.of(language))
        PantrixText(
            text = AskSay.Title.of(language),
            style = landingHeading(title, line = line, spacing = TITLE_SPACING),
            color = colors.textPrimary,
            modifier = Modifier.semantics { heading() }
        )
        val (start, end) = AskSay.Lead.of(layout)
        PantrixText(
            text = withCode(
                start.of(language),
                mcpHost(),
                end.of(language),
                if (layout ==
                    SiteLayout.Phone
                ) {
                    13.sp
                } else {
                    14.5.sp
                }
            ),
            style = SiteTheme.type.lead.sized(if (layout == SiteLayout.Desktop) 16.5.sp else 15.sp),
            color = colors.textSecondary
        )
    }
}

/**
 * The ways of asking, a row of tabs, the second way's badge in it. Drawn by the site: neither `PantrixTabs`, whose
 * badge is a count or a dot, nor `PantrixSegmentButton`, whose options have no badge, writes a word beside a tab's
 * label (the library's docs/OPEN_ITEMS.md). A tab picked on a phone, where the row scrolls, is brought whole into view.
 */
@Composable
private fun Tabs(
    current: AskTab,
    onAction: (AskAction) -> Unit
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val colors = PantrixTheme.colors
    val shape = RoundedCornerShape(12.dp)
    val scope = rememberCoroutineScope()
    val row = Modifier
        .clip(shape)
        .background(SiteTheme.colors.surfaceAlt)
        .border(1.dp, colors.border, shape)
        .padding(4.dp)
    val tabs: @Composable (Modifier) -> Unit = { each ->
        AskTab.entries.forEach { tab ->
            val on = tab == current
            val tabShape = RoundedCornerShape(9.dp)
            val whole = remember { BringIntoViewRequester() }
            Row(
                modifier = each
                    .bringIntoViewRequester(whole)
                    .height(if (layout == SiteLayout.Desktop) 40.dp else TOUCH)
                    .then(if (on) Modifier.shadow(1.dp, tabShape) else Modifier)
                    .siteClickable(tag = "askTab_${tab.key}", role = Role.Tab, shape = tabShape) {
                        onAction(AskAction.PickTab(tab))
                        scope.launch { whole.bringIntoView() }
                    }
                    .semantics { selected = on }
                    .background(if (on) colors.surface else Color.Transparent)
                    .padding(horizontal = if (layout == SiteLayout.Phone) 14.dp else 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PantrixText(
                    text = tabLabel(tab).of(language),
                    style = small(14.sp).copy(fontWeight = if (on) FontWeight.ExtraBold else FontWeight.SemiBold),
                    color = if (on) colors.textPrimary else colors.textSecondary,
                    maxLines = 1
                )
                if (tab == AskTab.Key) Badge(AskSay.PhaseTwo.of(language))
            }
        }
    }
    when (layout) {
        SiteLayout.Desktop -> Row(row, horizontalArrangement = Arrangement.spacedBy(4.dp)) { tabs(Modifier) }

        SiteLayout.Tablet -> Row(row.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            tabs(Modifier.weight(1f))
        }

        SiteLayout.Phone -> Row(
            row.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) { tabs(Modifier) }
    }
}

private fun tabLabel(tab: AskTab) = when (tab) {
    AskTab.Server -> AskSay.TabServer
    AskTab.Assistant -> AskSay.TabAssistant
    AskTab.Key -> AskSay.TabKey
}

/** A small word in a pill of the primary role's container: `Phase 2`. */
@Composable
internal fun Badge(text: String) {
    val colors = PantrixTheme.colors
    PantrixText(
        text = text,
        style = small(11.sp).copy(fontWeight = FontWeight.ExtraBold),
        color = colors.primary.onContainer,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(PILL))
            .background(colors.primary.container)
            .padding(horizontal = 7.dp, vertical = 2.dp)
    )
}

/** The question, where it is looked for, the questions to try, and what the server answered. */
@Composable
private fun ServerPanel(
    state: AskUiState,
    onAction: (AskAction) -> Unit
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val field = rememberTextFieldState(state.asked.orEmpty())
    val ask = { onAction(AskAction.Ask(field.text.toString())) }
    Column(verticalArrangement = Arrangement.spacedBy(if (layout == SiteLayout.Phone) 14.dp else 16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            PantrixTextField(
                state = field,
                modifier = Modifier.weight(1f),
                placeholder = AskSay.Placeholder.of(layout).of(language),
                leadingIcon = if (layout == SiteLayout.Desktop) {
                    {
                        PantrixIcon(
                            imageVector = PantrixIcons.Search,
                            contentDescription = null,
                            tint = SiteTheme.colors.muted,
                            size = 18.dp
                        )
                    }
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                onKeyboardAction = { ask() },
                style = TextFieldStyle(
                    shape = ShapeSpec.Fixed(RoundedCornerShape(12.dp)),
                    height = FIELD,
                    horizontalPadding = if (layout == SiteLayout.Phone) 14.dp else 16.dp,
                    iconPadding = 12.dp
                ),
                colors = TextFieldColors(border = PantrixTheme.colors.borderHover),
                contentDescription = AskSay.Question.of(language),
                tagKey = "askQuestion"
            )
            LandingButton(
                text = AskSay.Ask.of(language),
                onClick = ask,
                tag = "askSubmit",
                height = FIELD,
                padding = if (layout == SiteLayout.Phone) 18.dp else 24.dp
            )
        }
        ChipLine(AskSay.Where.of(language)) {
            AskScope.entries.forEach { scope ->
                PantrixChip(
                    label = AskSay.scope(scope).of(language),
                    selected = scope == state.scope,
                    onSelectedChange = { onAction(AskAction.PickScope(scope)) },
                    style = ChipStyle(height = if (layout == SiteLayout.Desktop) 32.dp else TOUCH),
                    tagKey = "askScope_${scope.key}"
                )
            }
        }
        ChipLine(AskSay.Try.of(language)) {
            AskSay.examples(language).forEachIndexed { index, (question, scope) ->
                PantrixChip(
                    label = question,
                    onClick = {
                        field.setTextAndPlaceCursorAtEnd(question)
                        onAction(AskAction.Try(question, scope))
                    },
                    style = ChipStyle(height = if (layout == SiteLayout.Desktop) 32.dp else TOUCH),
                    tagKey = "askTry_$index"
                )
            }
        }
        if (state.phase == AskPhase.Empty) Promises() else Results(state, onAction, field)
        PantrixText(
            text = withCode(AskSay.Kept.first.of(language), mcpHost(), AskSay.Kept.second.of(language), 12.sp),
            style = small(13.sp),
            color = SiteTheme.colors.muted
        )
    }
}

/**
 * A line of chips after its [label]: wrapped where there is room, and scrolled sideways on a phone, as the design has
 * them.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipLine(
    label: String,
    chips: @Composable () -> Unit
) {
    val word: @Composable () -> Unit = {
        PantrixText(
            text = label,
            style = small(13.sp).copy(fontWeight = FontWeight.Bold),
            color = SiteTheme.colors.muted,
            modifier = Modifier.padding(end = 4.dp)
        )
    }
    if (SiteTheme.layout == SiteLayout.Phone) {
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            word()
            chips()
        }
    } else {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(Modifier.align(Alignment.CenterVertically)) { word() }
            chips()
        }
    }
}

/** What the box does, before anything is asked: three cards in a row, or a phone's one card of three rows. */
@Composable
private fun Promises() {
    val language = LocalLanguage.current
    val colors = PantrixTheme.colors
    if (SiteTheme.layout == SiteLayout.Phone) {
        Column(Modifier.fillMaxWidth().landingCard(SiteTheme.colors.surfaceAlt, colors.border)) {
            AskSay.Promises.forEachIndexed { index, (title, line) ->
                if (index > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(SiteTheme.colors.hairline))
                Promise(title.of(language), line.of(language), Modifier.padding(horizontal = 16.dp, vertical = 14.dp))
            }
        }
    } else {
        Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AskSay.Promises.forEach { (title, line) ->
                Promise(
                    title.of(language),
                    line.of(language),
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .landingCard(SiteTheme.colors.surfaceAlt, colors.border)
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun Promise(
    title: String,
    line: String,
    modifier: Modifier
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        PantrixText(text = title, style = strong(15.sp, FontWeight.Bold), color = PantrixTheme.colors.textPrimary)
        PantrixText(text = line, style = small(14.sp), color = PantrixTheme.colors.textSecondary)
    }
}

/**
 * The tools called and what they answered: the tools beside the answer on a desktop, over it on a tablet, and folded
 * over it on a phone.
 */
@Composable
private fun Results(
    state: AskUiState,
    onAction: (AskAction) -> Unit,
    field: TextFieldState
) {
    when (SiteTheme.layout) {
        SiteLayout.Desktop -> Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.Top
        ) {
            ToolsCalled(state.steps, Modifier.width(TOOLS_WIDTH))
            Column(Modifier.weight(1f)) { Answer(state, onAction, field) }
        }

        SiteLayout.Tablet -> Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            ToolsCalled(state.steps, Modifier.fillMaxWidth())
            Answer(state, onAction, field)
        }

        SiteLayout.Phone -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ToolsFolded(state.steps, state.toolsOpen, state.phase == AskPhase.Searching) {
                onAction(AskAction.ToggleTools)
            }
            Answer(state, onAction, field)
        }
    }
}

/** What the server answered, as the question stands: a card, a list, nothing, no answer, or one on its way. */
@Composable
private fun ColumnScope.Answer(
    state: AskUiState,
    onAction: (AskAction) -> Unit,
    field: TextFieldState
) {
    val answer = state.answer
    when {
        state.phase == AskPhase.Failed -> NoAnswerCard(onAction)
        state.phase == AskPhase.Nothing -> NothingCard(state.asked.orEmpty(), onAction, field)
        answer == null -> AnswerSkeleton()
        answer is AskAnswer.Component -> ComponentAnswer(answer, onAction)
        answer is AskAnswer.Api -> ApiAnswer(answer, onAction)
        answer is AskAnswer.Icons -> IconsAnswer(answer, onAction)
        answer is AskAnswer.Documents -> DocumentsAnswer(answer.hits, onAction)
    }
}

/** The server's address as the page writes it: the site's host and `/mcp`, `pantrixui.com/mcp`. */
internal fun mcpHost(): String = SiteAddress.mcp.substringAfter("://")

/** A finger's height: what a tablet's and a phone's controls are at least. */
internal val TOUCH = 44.dp

/** How tall the question's field and its button are. */
private val FIELD = 52.dp

/** How wide a desktop's list of the tools called is. */
private val TOOLS_WIDTH = 380.dp

/** How wide the head's words run at most. */
private val HEAD_WIDTH = 760.dp

/** How round a pill is: rounder than it is tall. */
internal val PILL = 999.dp

private const val DESKTOP_TITLE_LINE = 1.08f
private const val PHONE_TITLE_LINE = 1.1f
private const val TITLE_SPACING = -0.035f
