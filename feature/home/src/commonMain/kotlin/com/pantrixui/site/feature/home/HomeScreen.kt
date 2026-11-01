package com.pantrixui.site.feature.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.scrollbar.pantrixVerticalScroll
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.platform.PantrixWindowTitle
import com.pantrixui.foundation.platform.rememberPantrixClipboard
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.base.BaseScreen
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.inCapitals
import com.pantrixui.site.core.designsystem.sized
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.Route
import com.pantrixui.site.core.routing.SiteRoutes
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * The landing page, with its model: what Pantrix UI is, drawn live in the conventions of the platform the reader
 * picks, and the way into the rest of the site.
 *
 * @param onNavigate what opening a page of the site does, given its path.
 */
@Composable
fun HomeRoute(onNavigate: (String) -> Unit) {
    val language = LocalLanguage.current
    val viewModel = koinViewModel<HomeViewModel>(key = "home_${language.code}") { parametersOf(language) }
    val uriHandler = LocalUriHandler.current
    val clipboard = rememberPantrixClipboard()
    BaseScreen(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                is HomeEffect.Navigate -> onNavigate(effect.path)
                is HomeEffect.OpenUrl -> uriHandler.openUri(effect.url)
                is HomeEffect.Copy -> viewModel.onAction(HomeAction.Copied(clipboard.copyText(effect.text)))
            }
        }
    ) { state, onAction -> HomeScreen(state, onAction) }
}

/**
 * The landing page, drawn from its [state]: its sections from the top, each in the layout the window is wide enough
 * for, and the site's foot under them.
 *
 * @param state what the page's model holds.
 * @param onAction where what the user does goes.
 * @param modifier the screen's modifier.
 */
@Composable
fun HomeScreen(
    state: HomeUiState,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val language = LocalLanguage.current
    val open: (Route) -> Unit = { onAction(HomeAction.OpenLink(SiteRoutes.addressOf(it, language))) }
    PantrixWindowTitle(HomeSay.WindowTitle.of(language))
    Column(modifier.fillMaxSize().pantrixVerticalScroll(rememberScrollState())) {
        Hero(state, onAction, open)
        Numbers()
        Platforms()
        WriteOnce(state, onAction)
        WhatComesWithIt(open)
        ForAssistants()
        YourBrand(open)
        StartHere(open)
        HomeFoot(open, onLink = { onAction(HomeAction.OpenLink(it)) })
    }
}

/** The room at a section's sides, and the widest its content grows, in the layout on show. */
internal val SiteLayout.gutter: Dp
    get() = if (this == SiteLayout.Phone) 16.dp else 24.dp

/** The small capitals over a section's heading: what the section is about. */
@Composable
internal fun Kicker(
    text: String,
    color: Color = PantrixTheme.colors.primary.onContainer
) {
    val phone = SiteTheme.layout == SiteLayout.Phone
    PantrixText(
        text = text.inCapitals(),
        style = display(if (phone) 12.sp else 13.sp, line = 1.4f, spacing = 0.08f),
        color = color
    )
}

/** A section's heading, as large as the layout has it. */
@Composable
internal fun SectionHeading(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = PantrixTheme.colors.textPrimary
) {
    PantrixText(
        text = text,
        style = sectionHeadingStyle(),
        color = color,
        modifier = modifier.semantics { heading() }
    )
}

/** The type of a section's heading in the layout on show. */
@Composable
internal fun sectionHeadingStyle(): TextStyle = when (SiteTheme.layout) {
    SiteLayout.Desktop -> display(40.sp, line = 1.1f, spacing = -0.03f)
    SiteLayout.Tablet -> display(34.sp, line = 1.12f, spacing = -0.03f)
    SiteLayout.Phone -> display(28.sp, line = 1.15f, spacing = -0.03f)
}

/** The sentence under a section's heading, with a name of the code in it where [code] is given. */
@Composable
internal fun SectionLead(
    start: String,
    code: String? = null,
    end: String = "",
    modifier: Modifier = Modifier,
    size: TextUnit = leadSize()
) {
    PantrixText(
        text = withCode(start, code, end, (size.value - CODE_SMALLER).sp),
        style = SiteTheme.type.body.sized(size),
        color = PantrixTheme.colors.textSecondary,
        modifier = modifier
    )
}

/** How large a section's sentence is in the layout on show. */
@Composable
internal fun leadSize(): TextUnit = when (SiteTheme.layout) {
    SiteLayout.Desktop -> 17.sp
    SiteLayout.Tablet -> 16.sp
    SiteLayout.Phone -> 15.sp
}

/** [start], [code] in the site's code type in the text's own colour, and [end]: a sentence that names the code. */
@Composable
internal fun withCode(
    start: String,
    code: String?,
    end: String,
    codeSize: TextUnit
): AnnotatedString {
    val mono = SiteTheme.type.inlineCode.fontFamily
    val color = PantrixTheme.colors.textPrimary
    return buildAnnotatedString {
        append(start)
        if (code != null) {
            withStyle(SpanStyle(fontFamily = mono, fontSize = codeSize, color = color)) { append(code) }
        }
        append(end)
    }
}

/**
 * The site's sans type at [size], its line [line] times its size and its letters [spacing] ems apart, in the design's
 * extra bold: a heading the site's own styles have no size for.
 */
@Composable
internal fun display(
    size: TextUnit,
    line: Float,
    spacing: Float,
    weight: FontWeight = FontWeight.ExtraBold
): TextStyle = SiteTheme.type.pageTitle.copy(
    fontSize = size,
    lineHeight = (size.value * line).sp,
    letterSpacing = (size.value * spacing).sp,
    fontWeight = weight
)

/** The ink of the call at the page's end, the deepest of its blues: the white button's words. */
internal val CALL_INK = Color(0xFF1D3362)

/** How much smaller a name of the code is than the sentence it stands in. */
private const val CODE_SMALLER = 3f
