package com.pantrixui.site.feature.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.icon.PantrixIcon
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.icons.PantrixIcons
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.BySize
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.content.Hairline
import com.pantrixui.site.core.designsystem.content.LandingBand
import com.pantrixui.site.core.designsystem.content.LandingHeading
import com.pantrixui.site.core.designsystem.content.LandingLead
import com.pantrixui.site.core.designsystem.content.landingBandPadding
import com.pantrixui.site.core.designsystem.content.landingCard
import com.pantrixui.site.core.designsystem.content.landingGutter
import com.pantrixui.site.core.designsystem.content.landingHeading
import com.pantrixui.site.core.designsystem.content.withCode
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.Words

/**
 * "What the server answers": its tools two to a row with its resources and prompts on a desktop, a list and the
 * prompts on a tablet, and a list alone on a phone.
 */
@Composable
internal fun ServerAnswers() {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val gap = when (layout) {
        SiteLayout.Desktop -> 24.dp
        SiteLayout.Tablet -> 16.dp
        SiteLayout.Phone -> 12.dp
    }
    LandingBand(landingBandPadding(), ground = SiteTheme.colors.canvas, gap = gap) {
        Column(Modifier.widthIn(max = 680.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            LandingHeading(ServerSay.ToolsTitle.of(language))
            if (layout == SiteLayout.Desktop) LandingLead(ServerSay.ToolsLead.of(language))
        }
        if (layout == SiteLayout.Desktop) {
            ToolCards()
            ResourcesAndPrompts()
        } else {
            ToolList()
            if (layout == SiteLayout.Tablet) PromptChips()
        }
    }
}

/** A desktop's tools, two to a row: each one's name beside what it answers. */
@Composable
private fun ToolCards() {
    val language = LocalLanguage.current
    val colors = PantrixTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ServerSay.Tools.chunked(2).forEach { row ->
            Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { (name, what) ->
                    Row(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .landingCard(colors.surface, colors.border)
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        ToolName(name, Modifier.width(DESKTOP_TOOL_WIDTH))
                        PantrixText(
                            text = what.desktop.of(language),
                            style = small(14.sp),
                            color = colors.textSecondary
                        )
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

/**
 * A tablet's and a phone's tools, one under another in a card: beside their names on a tablet, under them on a
 * phone.
 */
@Composable
private fun ToolList() {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val colors = PantrixTheme.colors
    val phone = layout == SiteLayout.Phone
    Column(Modifier.fillMaxWidth().landingCard(colors.surface, colors.border)) {
        ServerSay.Tools.forEachIndexed { index, (name, what) ->
            if (index > 0) Hairline()
            val text: @Composable () -> Unit = {
                PantrixText(
                    text = what.of(layout).of(language),
                    style = small(if (phone) 13.5.sp else 14.sp),
                    color = colors.textSecondary
                )
            }
            if (phone) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp)) {
                    ToolName(name)
                    text()
                }
            } else {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ToolName(name, Modifier.width(TABLET_TOOL_WIDTH))
                    text()
                }
            }
        }
    }
}

/** A tool's name, in the code type and the brand's colour. */
@Composable
private fun ToolName(
    name: String,
    modifier: Modifier = Modifier
) {
    val size = if (SiteTheme.layout == SiteLayout.Desktop) 13.sp else 12.5.sp
    PantrixText(
        text = name,
        style = mono(size, FontWeight.SemiBold),
        color = PantrixTheme.colors.primary.onContainer,
        modifier = modifier
    )
}

/** A desktop's resources and prompts, side by side. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ResourcesAndPrompts() {
    val language = LocalLanguage.current
    val colors = PantrixTheme.colors
    val card = @Composable { modifier: Modifier, content: @Composable () -> Unit ->
        Column(
            modifier
                .landingCard(colors.surface, colors.border, corner = 16.dp)
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) { content() }
    }
    Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        card(Modifier.weight(1f).fillMaxHeight()) {
            PantrixText(text = ServerSay.Resources.of(language), style = strong(16.sp), color = colors.textPrimary)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ServerSay.ResourceUris.forEach { uri ->
                    PantrixText(
                        text = uri,
                        style = mono(12.sp),
                        color = colors.textPrimary,
                        modifier = Modifier
                            .background(SiteTheme.colors.hover, RoundedCornerShape(7.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
        card(Modifier.weight(1f).fillMaxHeight()) {
            PantrixText(text = ServerSay.Prompts.of(language), style = strong(16.sp), color = colors.textPrimary)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ServerSay.PromptList.forEach { (name, what) ->
                    PantrixText(
                        text = withCode("", name, " " + what.of(language), 12.5.sp),
                        style = small(14.sp),
                        color = colors.textSecondary
                    )
                }
            }
        }
    }
}

/** A tablet's prompts: their names in a row of chips after the word. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PromptChips() {
    val language = LocalLanguage.current
    val colors = PantrixTheme.colors
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        itemVerticalAlignment = Alignment.CenterVertically
    ) {
        PantrixText(
            text = ServerSay.Prompts.of(language),
            style = strong(14.sp),
            color = colors.textPrimary,
            modifier = Modifier.padding(end = 4.dp)
        )
        ServerSay.PromptList.forEach { (name, _) ->
            PantrixText(
                text = name,
                style = mono(12.sp),
                color = colors.textPrimary,
                modifier = Modifier
                    .background(colors.surface, RoundedCornerShape(7.dp))
                    .border(1.dp, colors.border, RoundedCornerShape(7.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

/**
 * The rules the skill keeps, and why the documents do not drift from the code: side by side on a desktop, one under
 * the other on a tablet, and a phone's rules with a note after them.
 */
@Composable
internal fun RulesAndChecks() {
    val layout = SiteTheme.layout
    val padding = when (layout) {
        SiteLayout.Phone -> PaddingValues(start = landingGutter, end = landingGutter, top = 40.dp, bottom = 48.dp)
        else -> landingBandPadding()
    }
    LandingBand(padding, gap = if (layout == SiteLayout.Phone) 12.dp else 16.dp) {
        when (layout) {
            SiteLayout.Desktop -> {
                Row(horizontalArrangement = Arrangement.spacedBy(40.dp)) {
                    DesktopRules(Modifier.weight(RULES_SHARE))
                    Drift(Modifier.weight(DRIFT_SHARE))
                }
            }

            SiteLayout.Tablet -> {
                LandingHeading(ServerSay.RulesTitle.of(LocalLanguage.current))
                RuleGrid()
                TabletDrift()
            }

            SiteLayout.Phone -> {
                LandingHeading(ServerSay.RulesTitle.of(LocalLanguage.current))
                ServerSay.Rules.forEachIndexed { index, rule -> RuleCard(index, rule, Modifier.fillMaxWidth()) }
                PhoneDrift()
            }
        }
    }
}

/** A desktop's rules: the heading, where they come from, and the seven. */
@Composable
private fun DesktopRules(modifier: Modifier) {
    val language = LocalLanguage.current
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LandingHeading(ServerSay.RulesTitle.of(language), desktop = 32.sp)
        PantrixText(
            text = buildAnnotatedString {
                append(withCode(ServerSay.RulesLeadStart.of(language), "llms.txt", ", ", 13.5.sp))
                append(withCode("", "get_rules", ServerSay.RulesLeadEnd.of(language), 13.5.sp))
            },
            style = SiteTheme.type.body,
            color = PantrixTheme.colors.textSecondary
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ServerSay.Rules.forEachIndexed { index, rule -> RuleCard(index, rule, Modifier.fillMaxWidth()) }
        }
    }
}

/** A tablet's rules, two to a row. */
@Composable
private fun RuleGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ServerSay.Rules.withIndex().chunked(2).forEach { row ->
            Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { (index, rule) -> RuleCard(index, rule, Modifier.weight(1f).fillMaxHeight()) }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

/** A rule: its number on the brand's light ground, its title and what it asks. */
@Composable
private fun RuleCard(
    index: Int,
    rule: Pair<BySize<Words>, BySize<Words>>,
    modifier: Modifier
) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val colors = PantrixTheme.colors
    val (title, body) = rule
    Row(
        modifier
            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
            .padding(horizontal = if (layout == SiteLayout.Phone) 12.dp else 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            Modifier.size(26.dp).background(colors.primary.container, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            PantrixText(text = "${index + 1}", style = strong(13.sp), color = colors.primary.onContainer)
        }
        Column {
            PantrixText(
                text = title.of(layout).of(language),
                style = when (layout) {
                    SiteLayout.Desktop -> strong(16.sp)
                    SiteLayout.Tablet -> strong(16.sp, FontWeight.Bold)
                    SiteLayout.Phone -> strong(14.5.sp, FontWeight.Bold)
                },
                color = colors.textPrimary
            )
            PantrixText(
                text = body.of(layout).of(language),
                style = small(if (layout == SiteLayout.Desktop) 14.sp else 13.5.sp),
                color = colors.textSecondary
            )
        }
    }
}

/** A desktop's "Why it does not drift": what stops the build, and the page's own copy for an assistant. */
@Composable
private fun Drift(modifier: Modifier) {
    val language = LocalLanguage.current
    val colors = PantrixTheme.colors
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LandingHeading(ServerSay.DriftTitle.of(language), desktop = 32.sp)
        PantrixText(text = ServerSay.DriftLead.of(language), style = SiteTheme.type.body, color = colors.textSecondary)
        DriftList(ServerSay.Drift.desktop)
        Row(
            Modifier
                .fillMaxWidth()
                .landingCard(SiteTheme.colors.canvas, SiteTheme.colors.hairline)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PantrixIcon(
                imageVector = PantrixIcons.Copy,
                contentDescription = null,
                tint = colors.primary.onContainer,
                size = 20.dp
            )
            PantrixText(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = colors.textPrimary, fontWeight = FontWeight.Bold)) {
                        append(ServerSay.CopyPage.of(language))
                    }
                    append(ServerSay.CopyPageEnd.of(language))
                },
                style = small(14.sp),
                color = colors.textSecondary
            )
        }
    }
}

/** A tablet's "Why it does not drift": a smaller heading over four of the checks. */
@Composable
private fun TabletDrift() {
    PantrixText(
        text = ServerSay.DriftTitle.of(LocalLanguage.current),
        style = landingHeading(22.sp, line = DRIFT_TITLE_LINE, spacing = 0f),
        color = PantrixTheme.colors.textPrimary,
        modifier = Modifier.padding(top = 16.dp)
    )
    DriftList(ServerSay.Drift.tablet)
}

/** What stops the build, each with a cross, in one card. */
@Composable
private fun DriftList(items: List<Words>) {
    val language = LocalLanguage.current
    val colors = PantrixTheme.colors
    Column(Modifier.fillMaxWidth().border(1.dp, colors.border, RoundedCornerShape(14.dp))) {
        items.forEachIndexed { index, words ->
            if (index > 0) Hairline()
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PantrixText(text = "✗", style = strong(14.sp), color = colors.danger.onContainer)
                PantrixText(text = words.of(language), style = small(14.sp), color = colors.textPrimary)
            }
        }
    }
}

/** A phone's checks: one sentence in a note under the rules. */
@Composable
private fun PhoneDrift() {
    val language = LocalLanguage.current
    val colors = PantrixTheme.colors
    PantrixText(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = colors.textPrimary, fontWeight = FontWeight.Bold)) {
                append(ServerSay.DriftPhoneStart.of(language))
            }
            append(ServerSay.DriftPhone.of(language))
        },
        style = small(13.5.sp),
        color = colors.textSecondary,
        modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxWidth()
            .landingCard(SiteTheme.colors.canvas, SiteTheme.colors.hairline, corner = 12.dp)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    )
}

private const val RULES_SHARE = 650f
private const val DRIFT_SHARE = 550f
private const val DRIFT_TITLE_LINE = 1.3f
private val DESKTOP_TOOL_WIDTH: Dp = 210.dp
private val TABLET_TOOL_WIDTH: Dp = 220.dp
