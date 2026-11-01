package com.pantrixui.site.feature.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.button.ButtonColors
import com.pantrixui.components.elements.button.ButtonSize
import com.pantrixui.components.elements.button.ButtonSizeStyle
import com.pantrixui.components.elements.button.ButtonStyle
import com.pantrixui.components.elements.button.ButtonVariant
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.components.elements.icon.PantrixIcon
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.icons.PantrixIcons
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.base.valueOrNull
import com.pantrixui.site.core.designsystem.BySize
import com.pantrixui.site.core.designsystem.SiteIcons
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.content.LandingBand
import com.pantrixui.site.core.designsystem.content.LandingButton
import com.pantrixui.site.core.designsystem.content.LandingButtonKind
import com.pantrixui.site.core.designsystem.content.LandingKicker
import com.pantrixui.site.core.designsystem.content.SmallCode
import com.pantrixui.site.core.designsystem.content.landingCard
import com.pantrixui.site.core.designsystem.content.landingHeading
import com.pantrixui.site.core.designsystem.content.withCode
import com.pantrixui.site.core.designsystem.sized
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.Words

/**
 * The page's words, its ways in and its numbers, beside the conversation on a desktop and over it elsewhere; a phone
 * keeps one way in and no numbers.
 */
@Composable
internal fun Hero(
    state: AiUiState,
    onAction: (AiAction) -> Unit,
    onSection: (String) -> Unit
) {
    val layout = SiteTheme.layout
    val padding = when (layout) {
        SiteLayout.Desktop -> PaddingValues(start = 24.dp, end = 24.dp, top = 64.dp, bottom = 56.dp)
        SiteLayout.Tablet -> PaddingValues(start = 24.dp, end = 24.dp, top = 44.dp, bottom = 32.dp)
        SiteLayout.Phone -> PaddingValues(start = 16.dp, end = 16.dp, top = 28.dp, bottom = 28.dp)
    }
    LandingBand(padding, gap = if (layout == SiteLayout.Phone) 16.dp else 20.dp) {
        if (layout == SiteLayout.Desktop) {
            Row(horizontalArrangement = Arrangement.spacedBy(48.dp), verticalAlignment = Alignment.Top) {
                Column(
                    Modifier.weight(WORDS_SHARE).padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(22.dp)
                ) {
                    HeroWords(onSection)
                    Stats(Modifier.padding(top = 6.dp))
                }
                Conversation(state, onAction, Modifier.weight(CONVERSATION_SHARE))
            }
        } else {
            HeroWords(onSection)
            if (layout == SiteLayout.Tablet) Stats()
            Conversation(state, onAction, Modifier.fillMaxWidth())
        }
    }
}

/** The kicker, the title, the lead and the ways in, as each size sets them. */
@Composable
private fun HeroWords(onSection: (String) -> Unit) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val colors = PantrixTheme.colors
    val (title, titleLine) = when (layout) {
        SiteLayout.Desktop -> 54.sp to DESKTOP_TITLE_LINE
        SiteLayout.Tablet -> 46.sp to TABLET_TITLE_LINE
        SiteLayout.Phone -> 36.sp to PHONE_TITLE_LINE
    }
    val (lead, code) = when (layout) {
        SiteLayout.Desktop -> 18.sp to 15.sp
        SiteLayout.Tablet -> 17.sp to 14.5.sp
        SiteLayout.Phone -> 16.sp to 14.sp
    }
    val gap = if (layout == SiteLayout.Phone) {
        16.dp
    } else if (layout == SiteLayout.Tablet) {
        20.dp
    } else {
        22.dp
    }
    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        LandingKicker(AiSay.Kicker.of(language))
        PantrixText(
            text = AiSay.Title.of(language),
            style = landingHeading(title, line = titleLine, spacing = TITLE_SPACING),
            color = colors.textPrimary,
            modifier = Modifier.semantics { heading() }
        )
        val (start, end) = AiSay.Lead.of(layout)
        PantrixText(
            text = withCode(start.of(language), "llms.txt", end.of(language), code),
            style = SiteTheme.type.lead.sized(lead),
            color = colors.textSecondary
        )
        if (layout == SiteLayout.Phone) {
            LandingButton(
                text = AiSay.SetItUp.of(language),
                onClick = { onSection(SETUP) },
                tag = "aiSetUp",
                height = 50.dp,
                fill = true
            )
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LandingButton(text = AiSay.SetItUp.of(language), onClick = { onSection(SETUP) }, tag = "aiSetUp")
                LandingButton(
                    text = AiSay.ReadLlms.of(language),
                    onClick = { onSection(DOCUMENTS) },
                    tag = "aiReadLlms",
                    kind = LandingButtonKind.Outline
                )
            }
        }
    }
}

/** The page's numbers, three cards in a row. */
@Composable
private fun Stats(modifier: Modifier = Modifier) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val colors = PantrixTheme.colors
    Row(modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AiSay.Stats.forEach { (number, label) ->
            Column(Modifier.weight(1f).fillMaxHeight().landingCard(colors.background, colors.border).padding(14.dp)) {
                PantrixText(
                    text = number,
                    style = landingHeading(24.sp, line = NUMBER_LINE, spacing = 0f),
                    color = colors.textPrimary
                )
                PantrixText(text = label.of(layout).of(language), style = small(13.sp), color = SiteTheme.colors.muted)
            }
        }
    }
}

/**
 * The conversation, a step at a time when it is played again: the ask, the tools called, the answer and its checks.
 * A desktop shows three tools; a tablet and a phone two, a phone without their calls.
 */
@Composable
private fun Conversation(
    state: AiUiState,
    onAction: (AiAction) -> Unit,
    modifier: Modifier
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val tools = if (layout == SiteLayout.Desktop) AiSay.ToolCalls else AiSay.ToolCalls.take(2)
    // The ask, each tool, the answer and its checks.
    val steps = 1 + tools.size + 2
    val (corner, padding) = when (layout) {
        SiteLayout.Desktop -> 22.dp to 18.dp
        SiteLayout.Tablet -> 20.dp to 16.dp
        SiteLayout.Phone -> 18.dp to 12.dp
    }
    Column(
        modifier.landingCard(SiteTheme.colors.canvas, SiteTheme.colors.hairline, corner = corner).padding(padding),
        verticalArrangement = Arrangement.spacedBy(if (layout == SiteLayout.Desktop) 12.dp else 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PantrixText(
                text = AiSay.Conversation.of(layout).of(language),
                style = small(if (layout == SiteLayout.Phone) 12.5.sp else 13.sp).copy(fontWeight = FontWeight.Bold),
                color = PantrixTheme.colors.textSecondary,
                modifier = Modifier.weight(1f)
            )
            ReplayButton { onAction(AiAction.Replay(steps)) }
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Step(state.step >= 0) { Ask() }
            tools.forEachIndexed { index, call -> Step(state.step >= index + 1) { ToolCard(call) } }
            Step(state.step >= tools.size + 1) { Answer(state) }
            Step(state.step >= tools.size + 2) { Checks() }
        }
    }
}

/** "Play again": small with a mark on a desktop, a finger's height elsewhere. */
@Composable
private fun ReplayButton(onClick: () -> Unit) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val colors = PantrixTheme.colors
    val desktop = layout == SiteLayout.Desktop
    PantrixButton(
        onClick = onClick,
        contentDescription = AiSay.Replay.of(language),
        variant = ButtonVariant.Outline,
        size = ButtonSize.Medium,
        style = ButtonStyle(
            medium = ButtonSizeStyle(
                height = if (desktop) 34.dp else 44.dp,
                horizontalPadding = if (layout == SiteLayout.Tablet) 14.dp else 12.dp
            )
        ),
        shape = RoundedCornerShape(if (desktop) 9.dp else 10.dp),
        colors = ButtonColors(container = colors.surface, content = colors.textPrimary, border = colors.borderHover),
        tagKey = "aiReplay"
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            if (desktop) PantrixIcon(imageVector = PantrixIcons.Play, contentDescription = null, size = 13.dp)
            PantrixText(
                text = AiSay.Replay.of(language),
                style = SiteTheme.type.button.sized(13.sp).copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

/** A step of the conversation, shown once the replay has reached it. */
@Composable
private fun Step(
    shown: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(visible = shown, enter = fadeIn()) { content() }
}

/** What the reader asked, at the conversation's end side. */
@Composable
private fun Ask() {
    val language = LocalLanguage.current
    val phone = SiteTheme.layout == SiteLayout.Phone
    val colors = PantrixTheme.colors
    val share = when (SiteTheme.layout) {
        SiteLayout.Desktop -> DESKTOP_ASK_SHARE
        SiteLayout.Tablet -> TABLET_ASK_SHARE
        SiteLayout.Phone -> PHONE_ASK_SHARE
    }
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
        PantrixText(
            text = AiSay.Asked.of(language),
            style = strong(if (phone) 14.sp else 14.5.sp, FontWeight.SemiBold),
            color = colors.primary.onColor,
            modifier = Modifier
                .fillMaxWidth(share)
                .wrapContentWidth(Alignment.End)
                .background(colors.primary.color, RoundedCornerShape(14.dp, 14.dp, 4.dp, 14.dp))
                .padding(horizontal = if (phone) 12.dp else 14.dp, vertical = 10.dp)
        )
    }
}

/** A tool the assistant called: the server and the tool, the call where there is room for it, and what it got. */
@Composable
private fun ToolCard(call: ToolCall) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val colors = PantrixTheme.colors
    val muted = SiteTheme.colors.muted
    Column(
        Modifier
            .fillMaxWidth()
            .landingCard(colors.surface, colors.border, corner = 12.dp)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(
            when (layout) {
                SiteLayout.Desktop -> 6.dp
                SiteLayout.Tablet -> 4.dp
                SiteLayout.Phone -> 3.dp
            }
        )
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (layout == SiteLayout.Desktop) {
                PantrixIcon(imageVector = SiteIcons.Wrench, contentDescription = null, tint = muted, size = 14.dp)
            }
            PantrixText(
                text = "pantrix-ui · " + call.tool,
                style = small(if (layout == SiteLayout.Phone) 11.5.sp else 12.sp).copy(fontWeight = FontWeight.Bold),
                color = muted
            )
        }
        if (layout != SiteLayout.Phone) {
            PantrixText(text = call.call, style = mono(12.5.sp), color = colors.textPrimary)
        }
        PantrixText(
            text = call.result.of(layout).of(language),
            style = small(if (layout == SiteLayout.Phone) 13.sp else 12.5.sp),
            color = colors.textSecondary
        )
    }
}

/** The code the assistant wrote, as long as each size has room for. */
@Composable
private fun Answer(state: AiUiState) {
    val layout = SiteTheme.layout
    val answer = state.answers.valueOrNull?.get(
        when (layout) {
            SiteLayout.Desktop -> ANSWER
            SiteLayout.Tablet -> ANSWER_TABLET
            SiteLayout.Phone -> ANSWER_PHONE
        }
    ) ?: return
    val phone = layout == SiteLayout.Phone
    SmallCode(
        answer,
        textSize = if (phone) 11.5.sp else 12.5.sp,
        line = ANSWER_LINE,
        horizontalPadding = if (phone) 12.dp else 14.dp,
        verticalPadding = 12.dp,
        corner = 12.dp
    )
}

/** What the answer was checked for: in a row that wraps, or a phone's three one under another. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Checks() {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val colors = PantrixTheme.colors
    val modifier = Modifier
        .fillMaxWidth()
        .background(colors.success.container, RoundedCornerShape(12.dp))
        .padding(horizontal = 12.dp, vertical = 10.dp)
    val check: @Composable (BySize<Words>) -> Unit = {
        PantrixText(
            text = "✓ " + it.of(layout).of(language),
            style = small(13.sp).copy(fontWeight = FontWeight.Bold),
            color = colors.success.onContainer
        )
    }
    if (layout == SiteLayout.Phone) {
        Column(modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            AiSay.Checks.take(PHONE_CHECKS).forEach { check(it) }
        }
    } else {
        FlowRow(
            modifier,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) { AiSay.Checks.forEach { check(it) } }
    }
}

private const val DESKTOP_TITLE_LINE = 1.05f
private const val TABLET_TITLE_LINE = 1.06f
private const val PHONE_TITLE_LINE = 1.08f
private const val TITLE_SPACING = -0.035f
private const val NUMBER_LINE = 1.3f
private const val WORDS_SHARE = 507f
private const val CONVERSATION_SHARE = 685f
private const val DESKTOP_ASK_SHARE = 0.8f
private const val TABLET_ASK_SHARE = 0.78f
private const val PHONE_ASK_SHARE = 0.86f
private const val ANSWER_LINE = 1.7f

/** How many of the checks a phone shows. */
private const val PHONE_CHECKS = 3
