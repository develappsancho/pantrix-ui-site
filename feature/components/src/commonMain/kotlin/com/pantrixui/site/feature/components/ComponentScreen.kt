package com.pantrixui.site.feature.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.button.ButtonVariant
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.platform.PantrixWindowTitle
import com.pantrixui.foundation.platform.rememberPantrixClipboard
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.base.BaseScreen
import com.pantrixui.site.core.base.LoadState
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.content.ArticlePage
import com.pantrixui.site.core.designsystem.content.CodeView
import com.pantrixui.site.core.designsystem.content.Crumb
import com.pantrixui.site.core.designsystem.content.LinkCard
import com.pantrixui.site.core.designsystem.content.PageAnchors
import com.pantrixui.site.core.designsystem.content.PageFailed
import com.pantrixui.site.core.designsystem.content.PageHeader
import com.pantrixui.site.core.designsystem.content.PageTag
import com.pantrixui.site.core.designsystem.content.PageTagTone
import com.pantrixui.site.core.designsystem.content.RichText
import com.pantrixui.site.core.designsystem.content.contentsBeside
import com.pantrixui.site.core.designsystem.content.pageAnchor
import com.pantrixui.site.core.designsystem.content.rememberScrollToAnchor
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.i18n.LocalSiteStrings
import com.pantrixui.site.core.model.Block
import com.pantrixui.site.core.model.ComponentFeature
import com.pantrixui.site.core.model.ComponentGroups
import com.pantrixui.site.core.model.ComponentLink
import com.pantrixui.site.core.model.ComponentPage
import com.pantrixui.site.core.model.ContentsEntry
import com.pantrixui.site.core.model.LibraryFacts
import com.pantrixui.site.core.model.Route
import com.pantrixui.site.core.model.Token
import com.pantrixui.site.core.model.TokenKind
import com.pantrixui.site.core.model.Words
import com.pantrixui.site.core.routing.SiteRoutes
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * A component's page, with its model: what the site's navigation shows for [Route.Component].
 *
 * @param key the key of the component's page.
 * @param onNavigate what opening a page of the site does, given its path.
 */
@Composable
fun ComponentRoute(
    key: String,
    onNavigate: (String) -> Unit
) {
    val language = LocalLanguage.current
    val viewModel = koinViewModel<ComponentViewModel>(key = "component_${key}_${language.code}") {
        parametersOf(key, language)
    }
    val clipboard = rememberPantrixClipboard()
    val uriHandler = LocalUriHandler.current
    BaseScreen(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                is ComponentsEffect.Copy -> viewModel.onAction(ComponentAction.Copied(clipboard.copyText(effect.text)))
                is ComponentsEffect.Navigate -> onNavigate(effect.path)
                is ComponentsEffect.OpenUrl -> uriHandler.openUri(effect.url)
            }
        }
    ) { state, onAction -> ComponentScreen(state, onAction) }
}

/**
 * A component's page, drawn from its [state]: the page once it is read, nothing while it is, what to do when it could
 * not be, and a way back to every component for a component the library does not have.
 *
 * @param state what the page's model holds.
 * @param onAction where what the user does goes.
 * @param modifier the screen's modifier.
 */
@Composable
fun ComponentScreen(
    state: ComponentUiState,
    onAction: (ComponentAction) -> Unit,
    modifier: Modifier = Modifier
) {
    when (val page = state.page) {
        // A page is a few kilobytes read from the app's own files: there is nothing to show for the moment it takes.
        LoadState.Loading -> {
            Box(modifier.fillMaxSize())
        }

        is LoadState.Failed -> {
            PageFailed(onRetry = { onAction(ComponentAction.Retry) }, modifier = modifier)
        }

        is LoadState.Loaded -> {
            val value = page.value
            if (value == null) {
                NoSuchComponent(onAction, modifier)
            } else {
                ComponentArticle(value, state, onAction, modifier)
            }
        }
    }
}

@Composable
private fun ComponentArticle(
    page: ComponentPage,
    state: ComponentUiState,
    onAction: (ComponentAction) -> Unit,
    modifier: Modifier
) {
    val language = LocalLanguage.current
    val strings = LocalSiteStrings.current
    val anchors = remember(page) { PageAnchors() }
    val scrollState = rememberScrollState()
    val scrollTo = rememberScrollToAnchor(anchors, scrollState)
    val onLink: (String) -> Unit = { onAction(ComponentAction.OpenLink(it)) }
    val contents = remember(page, language) { contentsOf(page).map { ContentsEntry(it.anchor, it.title.of(language)) } }
    val gap = when (SiteTheme.layout) {
        SiteLayout.Desktop -> 40.dp
        SiteLayout.Tablet -> 36.dp
        SiteLayout.Phone -> 32.dp
    }
    PantrixWindowTitle(page.card.name)
    ArticlePage(
        contents = contents,
        modifier = modifier,
        scrollState = scrollState,
        anchors = anchors,
        contentsFooter = {
            if (LibraryFacts.REPOSITORY_IS_OPEN) {
                Spacer(Modifier.height(16.dp))
                FooterLink(strings.reportAProblem, "${LibraryFacts.REPOSITORY}/issues", "issues", onLink)
            }
        }
    ) {
        val components = SiteRoutes.addressOf(Route.Components, language)
        PageHeader(
            trail = listOf(
                Crumb(strings.components, components),
                Crumb(ComponentGroups.title(page.card.group).of(language), components)
            ),
            title = page.card.name,
            lead = null,
            copied = state.copied,
            onCopy = { onAction(ComponentAction.CopyPage) },
            onLink = onLink,
            tags = tagsOf(page),
            // Where "On this page" stands beside the article, the head has no buttons of its own for the parts.
            contents = if (contentsBeside(contents)) emptyList() else contents,
            onContents = scrollTo,
            fullTrail = true,
            leadText = page.lead
        )
        Spacer(Modifier.height(20.dp))
        ImportLine(page)
        if (page.examples.isNotEmpty()) {
            Spacer(Modifier.height(gap))
            SectionHeading(Section.Examples, anchors)
            ExamplesSection(page, onLink)
        }
        if (page.features.isNotEmpty()) {
            Spacer(Modifier.height(gap))
            SectionHeading(Section.HowItWorks, anchors)
            FeatureList(page.features, onLink)
        }
        if (page.forms.isNotEmpty()) {
            Spacer(Modifier.height(gap))
            ApiSection(page, state, onAction, anchors, onLink)
        }
        if (page.style != null || page.colors != null) {
            Spacer(Modifier.height(gap))
            SectionHeading(Section.Styles, anchors)
            StylesSection(page, onLink)
        }
        if (page.accessibility.isNotEmpty()) {
            Spacer(Modifier.height(gap))
            SectionHeading(Section.Accessibility, anchors)
            FeatureList(page.accessibility, onLink)
        }
        if (page.tags.isNotEmpty()) {
            Spacer(Modifier.height(gap))
            SectionHeading(Section.Testing, anchors)
            TestingSection(page, onLink)
        }
        if (page.changes.isNotEmpty()) {
            Spacer(Modifier.height(gap))
            SectionHeading(Section.Changelog, anchors)
            ChangesSection(page, onLink)
        }
        Spacer(Modifier.height(gap))
        Neighbours(page.previous, page.next, onLink)
    }
}

/** The parts of a component's page that its "On this page" lists, in order. */
internal enum class Section(
    val anchor: String,
    val title: Words
) {
    Examples("examples", Words("Examples", "Örnekler")),
    HowItWorks("how-it-works", Words("How it works", "Nasıl çalışır")),
    Api("api", Words("API", "API")),
    Styles("styles", Words("Styles and colours", "Stiller ve renkler")),
    Accessibility("accessibility", Words("Accessibility", "Erişilebilirlik")),
    Testing("testing", Words("Testing", "Test")),
    Changelog("changelog", Words("Changelog", "Değişiklik günlüğü"))
}

/** The parts [page] has, in order: a part with nothing to say is left out. */
private fun contentsOf(page: ComponentPage): List<Section> = buildList {
    if (page.examples.isNotEmpty()) add(Section.Examples)
    if (page.features.isNotEmpty()) add(Section.HowItWorks)
    if (page.forms.isNotEmpty()) add(Section.Api)
    if (page.style != null || page.colors != null) add(Section.Styles)
    if (page.accessibility.isNotEmpty()) add(Section.Accessibility)
    if (page.tags.isNotEmpty()) add(Section.Testing)
    if (page.changes.isNotEmpty()) add(Section.Changelog)
}

/** The tags under a component's lead: its group, the platforms it is drawn on, a form's field, its version. */
@Composable
private fun tagsOf(page: ComponentPage): List<PageTag> {
    val language = LocalLanguage.current
    val phone = SiteTheme.layout == SiteLayout.Phone
    return listOfNotNull(
        PageTag(ComponentGroups.title(page.card.group).of(language), PageTagTone.Accent),
        PageTag(if (phone) ComponentSay.FourPlatforms.of(language) else PLATFORMS),
        PageTag(ComponentSay.BindsToForm.of(language)).takeIf { page.bindsToForm },
        PageTag(ComponentSay.since(LibraryFacts.VERSION).of(language)).takeIf { !phone }
    )
}

/** A part's heading, which "On this page" jumps to. */
@Composable
internal fun SectionHeading(
    section: Section,
    anchors: PageAnchors,
    trailing: @Composable () -> Unit = {}
) {
    val language = LocalLanguage.current
    Row(
        Modifier.fillMaxWidth().padding(bottom = 16.dp).pageAnchor(anchors, section.anchor),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PantrixText(
            text = section.title.of(language),
            style = SiteTheme.type.guideHeading,
            color = PantrixTheme.colors.textPrimary,
            modifier = Modifier.weight(1f).semantics { heading() }
        )
        trailing()
    }
}

/** How the component is brought into a file: its import, with a button that copies it. */
@Composable
private fun ImportLine(page: ComponentPage) {
    val line = remember(page) {
        Block.Code(
            language = "kotlin",
            lines = listOf(
                listOf(
                    Token("import", TokenKind.Keyword),
                    Token(" ${page.packageName}.${page.card.function}")
                )
            )
        )
    }
    CodeView(line, compact = true)
}

/** The lines of what a component does, each with its lead in bold. */
@Composable
internal fun FeatureList(
    features: List<ComponentFeature>,
    onLink: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        features.forEach { feature ->
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                feature.label?.let { label ->
                    PantrixText(
                        text = label,
                        style = SiteTheme.type.body.copy(fontWeight = FontWeight.Bold),
                        color = PantrixTheme.colors.textPrimary
                    )
                }
                RichText(feature.text, onLink, codeSize = 13.sp)
            }
        }
    }
}

/** The cards that lead to the components before and after this one, in the menu's order. */
@Composable
private fun Neighbours(
    previous: ComponentLink?,
    next: ComponentLink?,
    onLink: (String) -> Unit
) {
    val language = LocalLanguage.current
    val strings = LocalSiteStrings.current
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf(previous to strings.previous, next to strings.next).forEachIndexed { index, (link, label) ->
            if (link == null) {
                Spacer(Modifier.weight(1f))
                return@forEachIndexed
            }
            val align = if (index == 0) TextAlign.Start else TextAlign.End
            LinkCard(
                onClick = { onLink(componentAddress(link.key, language)) },
                tagKey = if (index == 0) "previous" else "next",
                modifier = Modifier.weight(1f).fillMaxHeight(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = if (index == 0) Alignment.Start else Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    PantrixText(
                        text = label,
                        style = SiteTheme.type.tag,
                        color = SiteTheme.colors.muted,
                        textAlign = align
                    )
                    PantrixText(
                        text = link.name,
                        style = SiteTheme.type.body.copy(fontWeight = FontWeight.ExtraBold),
                        color = PantrixTheme.colors.textPrimary,
                        textAlign = align
                    )
                }
            }
        }
    }
}

@Composable
private fun FooterLink(
    text: String,
    href: String,
    tag: String,
    onLink: (String) -> Unit
) {
    PantrixButton(
        text = text,
        onClick = { onLink(href) },
        variant = ButtonVariant.Text,
        tagKey = tag
    )
}

/** What the page says for a component the library does not have, with the way back to every component. */
@Composable
private fun NoSuchComponent(
    onAction: (ComponentAction) -> Unit,
    modifier: Modifier
) {
    val language = LocalLanguage.current
    Box(modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            PantrixText(
                text = ComponentSay.NoSuchComponent.of(language),
                style = SiteTheme.type.lead,
                color = PantrixTheme.colors.textPrimary
            )
            PantrixButton(
                text = ComponentsSay.Title.of(language),
                onClick = { onAction(ComponentAction.OpenLink(SiteRoutes.addressOf(Route.Components, language))) },
                variant = ButtonVariant.Outline,
                tagKey = "allComponents"
            )
        }
    }
}

/** The platforms every component is drawn on, as a tag says them. */
private const val PLATFORMS = "Android · iOS · Desktop · Web"

/** A component page's own words, in each language. */
internal object ComponentSay {
    val FourPlatforms = Words("4 platforms", "4 platform")
    val BindsToForm = Words("Binds to a form", "Forma bağlanır")
    val NoSuchComponent = Words("The library has no such component.", "Kütüphanede böyle bir bileşen yok.")

    /** The tag of the version a component came in. */
    fun since(version: String): Words = Words("Since $version", "$version sürümünden beri")
}
