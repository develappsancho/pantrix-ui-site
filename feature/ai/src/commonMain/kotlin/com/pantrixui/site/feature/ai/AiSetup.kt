package com.pantrixui.site.feature.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.content.Hairline
import com.pantrixui.site.core.designsystem.content.LandingBand
import com.pantrixui.site.core.designsystem.content.LandingHeading
import com.pantrixui.site.core.designsystem.content.LandingLead
import com.pantrixui.site.core.designsystem.content.landingBandPadding
import com.pantrixui.site.core.designsystem.content.landingCard
import com.pantrixui.site.core.designsystem.content.withCode
import com.pantrixui.site.core.designsystem.siteClickable
import com.pantrixui.site.core.designsystem.sized
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.TokenKind

/**
 * "Four ways in, one source": four cards and what to give when on a desktop, four smaller cards on a tablet, and a
 * list on a phone.
 */
@Composable
internal fun WaysIn(modifier: Modifier) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val gap = when (layout) {
        SiteLayout.Desktop -> 28.dp
        SiteLayout.Tablet -> 20.dp
        SiteLayout.Phone -> 14.dp
    }
    LandingBand(landingBandPadding(), modifier, ground = SiteTheme.colors.canvas, gap = gap) {
        Column(Modifier.widthIn(max = 720.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            LandingHeading(AiSay.WaysTitle.of(language))
            if (layout != SiteLayout.Phone) LandingLead(AiSay.WaysLead.of(layout).of(language))
        }
        when (layout) {
            SiteLayout.Desktop -> {
                WayCards(columns = AiSay.Ways.size)
                WhatToGive()
            }

            SiteLayout.Tablet -> {
                WayCards(columns = 2)
            }

            SiteLayout.Phone -> {
                WayList()
            }
        }
    }
}

/** The four ways in as cards, [columns] to a row: a desktop's with their marks. */
@Composable
private fun WayCards(columns: Int) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val desktop = layout == SiteLayout.Desktop
    val colors = PantrixTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(if (desktop) 14.dp else 12.dp)) {
        AiSay.Ways.chunked(columns).forEach { row ->
            Row(
                Modifier.height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(if (desktop) 14.dp else 12.dp)
            ) {
                row.forEach { way ->
                    Column(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .landingCard(colors.surface, colors.border, corner = 16.dp)
                            .padding(if (desktop) 20.dp else 18.dp),
                        verticalArrangement = Arrangement.spacedBy(if (desktop) 10.dp else 6.dp)
                    ) {
                        if (desktop) WayMark(way.mark)
                        PantrixText(
                            text = way.title.of(language),
                            style = if (desktop) strong(17.sp) else strong(16.sp, FontWeight.Bold),
                            color = colors.textPrimary
                        )
                        PantrixText(
                            text = way.body.of(layout).of(language),
                            style = small(14.sp),
                            color = colors.textSecondary
                        )
                        if (desktop) Spacer(Modifier.weight(1f))
                        PantrixText(text = way.where, style = mono(12.sp), color = SiteTheme.colors.muted)
                    }
                }
            }
        }
    }
}

/** A way's sign on its card: a few letters of code on the brand's light ground. */
@Composable
private fun WayMark(mark: String) {
    val colors = PantrixTheme.colors
    Box(
        Modifier.size(40.dp).background(colors.primary.container, RoundedCornerShape(11.dp)),
        contentAlignment = Alignment.Center
    ) {
        PantrixText(text = mark, style = mono(13.sp, FontWeight.ExtraBold), color = colors.primary.onContainer)
    }
}

/** The four ways in as a phone lists them: each one's name and what it is, in one card. */
@Composable
private fun WayList() {
    val language = LocalLanguage.current
    val colors = PantrixTheme.colors
    Column(Modifier.fillMaxWidth().landingCard(colors.surface, colors.border, corner = 16.dp)) {
        AiSay.Ways.forEachIndexed { index, way ->
            if (index > 0) Hairline()
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                PantrixText(
                    text = way.title.of(language),
                    style = strong(16.sp, FontWeight.Bold),
                    color = colors.textPrimary
                )
                PantrixText(
                    text = way.body.phone.of(language),
                    style = small(14.sp),
                    color = colors.textSecondary
                )
            }
        }
    }
}

/** What to give an assistant, and when: a desktop's table. */
@Composable
private fun WhatToGive() {
    val language = LocalLanguage.current
    val colors = PantrixTheme.colors
    Column(Modifier.fillMaxWidth().landingCard(colors.surface, colors.border)) {
        PantrixText(
            text = AiSay.WhatToGive.of(language),
            style = strong(14.sp),
            color = colors.textPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .background(SiteTheme.colors.surfaceAlt)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )
        AiSay.Give.forEachIndexed { index, give ->
            // The caption stands over a line of the table's own; the rows have hairlines between them.
            if (index == 0) Hairline(color = colors.border) else Hairline()
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 11.dp)) {
                PantrixText(
                    text = give.case.of(language),
                    style = strong(14.sp, FontWeight.Bold),
                    color = colors.textPrimary,
                    modifier = Modifier.weight(GIVE_WHEN)
                )
                PantrixText(
                    text = withCode(give.start?.of(language).orEmpty(), give.code, give.end.of(language), 13.sp),
                    style = small(14.sp),
                    color = colors.textSecondary,
                    modifier = Modifier.weight(1f - GIVE_WHEN)
                )
            }
        }
    }
}

/**
 * "Set it up in a minute": the steps beside each assistant's setup on a desktop; a tablet and a phone have the setup
 * under a line of their own.
 */
@Composable
internal fun Setup(
    state: AiUiState,
    onAction: (AiAction) -> Unit,
    modifier: Modifier
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    if (layout != SiteLayout.Desktop) {
        val phone = layout == SiteLayout.Phone
        LandingBand(landingBandPadding(), modifier, gap = if (phone) 12.dp else 16.dp) {
            LandingHeading(SetupSay.SetupTitle.of(layout).of(language))
            PantrixText(
                text = SetupSay.SetupLead.of(layout).of(language),
                style = SiteTheme.type.body.sized(if (phone) 14.5.sp else 16.sp),
                color = PantrixTheme.colors.textSecondary
            )
            Clients(state, onAction, Modifier.fillMaxWidth())
        }
        return
    }
    LandingBand(landingBandPadding(), modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(40.dp)) {
            SetupSteps(Modifier.weight(SETUP_WORDS))
            Clients(state, onAction, Modifier.weight(SETUP_CODE))
        }
    }
}

/** A desktop's way to set it up: its heading, the steps and the note on fewer tools. */
@Composable
private fun SetupSteps(modifier: Modifier) {
    val language = LocalLanguage.current
    val colors = PantrixTheme.colors
    Column(modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        LandingHeading(SetupSay.SetupTitle.desktop.of(language))
        LandingLead(SetupSay.SetupLead.desktop.of(language))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SetupSay.Steps.forEachIndexed { index, (words, code) ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PantrixText(text = "${index + 1}.", style = SiteTheme.type.body, color = colors.textSecondary)
                    PantrixText(
                        text = withCode(words.of(language), code, if (code != null) "." else "", 13.sp),
                        style = SiteTheme.type.body,
                        color = colors.textSecondary
                    )
                }
            }
        }
        PantrixText(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = colors.textPrimary, fontWeight = FontWeight.Bold)) {
                    append(SetupSay.Fewer.of(language))
                }
                append(withCode("", "--tools search_components,get_component", SetupSay.FewerEnd.of(language), 12.5.sp))
            },
            style = small(14.sp),
            color = colors.textSecondary,
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.primary.container, RoundedCornerShape(12.dp))
                .border(1.dp, SiteTheme.colors.accentLine, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        )
    }
}

/** The assistants' setups: a tab for each, its lines, and what to know after; a phone leaves the last out. */
@Composable
private fun Clients(
    state: AiUiState,
    onAction: (AiAction) -> Unit,
    modifier: Modifier
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val code = SiteTheme.colors.code
    val desktop = layout == SiteLayout.Desktop
    val phone = layout == SiteLayout.Phone
    val clients = if (phone) SetupSay.Clients.filter { it.onPhone } else SetupSay.Clients
    // A setup picked in a wider window that a phone leaves out shows the first.
    val picked = state.client.takeIf { it in clients.indices } ?: 0
    val client = clients[picked]
    val corner = when (layout) {
        SiteLayout.Desktop -> 18.dp
        SiteLayout.Tablet -> 18.dp
        SiteLayout.Phone -> 16.dp
    }
    val side = when (layout) {
        SiteLayout.Desktop -> 20.dp
        SiteLayout.Tablet -> 18.dp
        SiteLayout.Phone -> 14.dp
    }
    val panel = if (desktop) modifier.shadow(PANEL_SHADOW, RoundedCornerShape(corner), clip = false) else modifier
    Column(panel.landingCard(code.background, code.background, corner = corner)) {
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(if (desktop) 8.dp else 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            clients.forEachIndexed { index, each ->
                ClientTab(each.name.of(layout).of(language), on = index == picked, tag = "client_$index") {
                    onAction(AiAction.PickClient(index))
                }
            }
        }
        Hairline(color = code.line)
        Column(
            Modifier.fillMaxWidth().horizontalScroll(
                rememberScrollState()
            ).padding(vertical = if (desktop) 6.dp else 8.dp)
        ) {
            client.lines.of(layout).forEach { line -> SetupCode(line, Modifier.padding(horizontal = side)) }
        }
        Hairline(color = code.line)
        PantrixText(
            text = client.note.of(layout).of(language),
            style = small(if (phone) 12.5.sp else 13.sp),
            color = code.comment,
            modifier = Modifier.fillMaxWidth().padding(horizontal = side, vertical = if (phone) 10.dp else 12.dp)
        )
    }
}

/** An assistant's tab over its setup: small on a desktop, a finger's height elsewhere. */
@Composable
private fun ClientTab(
    name: String,
    on: Boolean,
    tag: String,
    onClick: () -> Unit
) {
    val code = SiteTheme.colors.code
    val desktop = SiteTheme.layout == SiteLayout.Desktop
    val shape = RoundedCornerShape(if (desktop) 8.dp else 9.dp)
    PantrixText(
        text = name,
        style = SiteTheme.type.button.sized(13.sp).copy(fontWeight = FontWeight.Bold),
        color = if (on) code.onSelectedTab else code.comment,
        maxLines = 1,
        modifier = Modifier
            .background(if (on) code.selectedTab else code.background, shape)
            .siteClickable(tag = tag, role = Role.Tab, shape = shape, onClick = onClick)
            .height(if (desktop) 34.dp else 44.dp)
            .wrapContentHeight()
            .padding(horizontal = 12.dp)
    )
}

/** A line of an assistant's setup, in the colour of what it is. */
@Composable
private fun SetupCode(
    line: SetupLine,
    modifier: Modifier
) {
    val language = LocalLanguage.current
    val code = SiteTheme.colors.code
    val size = when (SiteTheme.layout) {
        SiteLayout.Desktop -> 13.sp
        SiteLayout.Tablet -> 12.5.sp
        SiteLayout.Phone -> 11.5.sp
    }
    PantrixText(
        text = line.comment?.of(language) ?: line.code,
        style = SiteTheme.type.code.copy(fontSize = size, lineHeight = (size.value * SETUP_LINE).sp),
        color = when {
            line.comment != null -> code.comment
            line.kind == TokenKind.Keyword -> code.keyword
            line.kind == TokenKind.StringLiteral -> code.literal
            else -> code.text
        },
        maxLines = 1,
        softWrap = false,
        modifier = modifier
    )
}

private const val GIVE_WHEN = 0.38f
private const val SETUP_WORDS = 450f
private const val SETUP_CODE = 750f

/** How far apart the lines of an assistant's setup are, in their size. */
private const val SETUP_LINE = 1.8f

/** How far an assistant's setup stands off a desktop's page. */
private val PANEL_SHADOW = 16.dp
