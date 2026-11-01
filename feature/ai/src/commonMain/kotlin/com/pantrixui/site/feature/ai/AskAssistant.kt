package com.pantrixui.site.feature.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.button.ButtonSize
import com.pantrixui.components.elements.button.ButtonVariant
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.components.elements.icon.PantrixIcon
import com.pantrixui.components.elements.link.PantrixLink
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.icons.PantrixIcons
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.content.landingCard
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.LibraryFacts
import com.pantrixui.site.core.model.Route
import com.pantrixui.site.core.model.SiteAddress
import com.pantrixui.site.core.routing.SiteRoutes
import kotlin.io.encoding.Base64

/**
 * The server in the reader's own assistant: its address to copy, and the way into four of them. Cursor's link and
 * VS Code's command are the ones their documents give: VS Code documents no link a page opens.
 */
@Composable
internal fun AssistantPanel(onAction: (AskAction) -> Unit) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val colors = PantrixTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(if (layout == SiteLayout.Phone) 12.dp else 16.dp)) {
        PantrixText(text = AskSay.AssistantLead.of(language), style = small(15.sp), color = colors.textSecondary)
        Address(onAction)
        val cards: List<@Composable (Modifier) -> Unit> = listOf(
            {
                ClientCard("Claude Code", AskSay.ClaudeCode.of(language), it) {
                    Command(CLAUDE_CODE, onAction, "claudeCode")
                }
            },
            {
                ClientCard("Cursor", AskSay.Cursor.of(language), it) {
                    PantrixButton(
                        text = AskSay.AddToCursor.of(language),
                        onClick = { onAction(AskAction.OpenUrl(cursorLink())) },
                        variant = ButtonVariant.Outline,
                        size = waySize(),
                        tagKey = "askCursor"
                    )
                }
            },
            {
                ClientCard("VS Code", AskSay.VsCode.of(language), it) {
                    Command(listOf(vsCodeCommand()), onAction, "vsCode")
                }
            },
            {
                ClientCard(AskSay.OtherClients.of(language), AskSay.Others.of(language), it) {
                    PantrixLink(
                        text = AskSay.WholeSetup.of(language),
                        onClick = { onAction(AskAction.Open(SiteRoutes.addressOf(Route.Ai, language) + "#$SETUP")) },
                        style = small(14.sp).copy(fontWeight = FontWeight.Bold),
                        tagKey = "askSetup",
                        modifier = Modifier.heightIn(min = if (layout == SiteLayout.Desktop) 0.dp else TOUCH)
                    )
                }
            }
        )
        if (layout == SiteLayout.Phone) {
            cards.forEach { it(Modifier.fillMaxWidth()) }
        } else {
            cards.chunked(2).forEach { pair ->
                Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    pair.forEach { it(Modifier.weight(1f).fillMaxHeight()) }
                }
            }
        }
    }
}

/** The server's address, large, with a button that copies it. */
@Composable
private fun Address(onAction: (AskAction) -> Unit) {
    val language = LocalLanguage.current
    val phone = SiteTheme.layout == SiteLayout.Phone
    val colors = PantrixTheme.colors
    val copy: @Composable () -> Unit = {
        PantrixButton(
            text = AskSay.CopyAddress.of(language),
            onClick = { onAction(AskAction.Copy(SiteAddress.mcp)) },
            variant = ButtonVariant.Outline,
            size = if (SiteTheme.layout == SiteLayout.Desktop) ButtonSize.Small else ButtonSize.Medium,
            fullWidth = phone,
            tagKey = "askCopyAddress"
        )
    }
    val box = Modifier
        .fillMaxWidth()
        .landingCard(colors.primary.container, SiteTheme.colors.accentLine, corner = 12.dp)
        .padding(start = if (phone) 12.dp else 14.dp, end = if (phone) 12.dp else 6.dp, top = 6.dp, bottom = 6.dp)
    val address: @Composable (Modifier) -> Unit = {
        PantrixText(
            text = SiteAddress.mcp,
            style = mono(15.sp, FontWeight.SemiBold),
            color = colors.primary.onContainer,
            modifier = it
        )
    }
    if (phone) {
        Column(box.padding(vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            address(Modifier)
            copy()
        }
    } else {
        Row(box, horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            address(Modifier.weight(1f))
            copy()
        }
    }
}

/** An assistant's card: its name, how it takes the server, and the way in. */
@Composable
private fun ClientCard(
    name: String,
    line: String,
    modifier: Modifier,
    way: @Composable () -> Unit
) {
    val colors = PantrixTheme.colors
    Column(
        modifier.landingCard(colors.surface, colors.border, corner = 16.dp).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PantrixText(text = name, style = strong(15.sp, FontWeight.Bold), color = colors.textPrimary)
        PantrixText(text = line, style = small(14.sp), color = colors.textSecondary)
        Spacer(Modifier.weight(1f, fill = false))
        way()
    }
}

/** Commands to run, as code, with a button that copies them. */
@Composable
private fun Command(
    lines: List<String>,
    onAction: (AskAction) -> Unit,
    tag: String
) {
    val language = LocalLanguage.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        lines.forEach { PantrixText(text = it, style = mono(12.sp), color = PantrixTheme.colors.textPrimary) }
        PantrixButton(
            text = AskSay.CopyCommand.of(language),
            onClick = { onAction(AskAction.Copy(lines.joinToString("\n"))) },
            variant = ButtonVariant.Outline,
            size = waySize(),
            tagKey = "askCopy_$tag"
        )
    }
}

/** The size of a card's way in: one size in every card, and a finger's reach where the page is touched. */
@Composable
private fun waySize(): ButtonSize = if (SiteTheme.layout == SiteLayout.Desktop) ButtonSize.Medium else ButtonSize.Large

/**
 * What comes with the reader's own key, which is not here yet: what the model will do, and what is kept of the key.
 * Nothing is asked for: no field takes a key before the model is there to use it.
 */
@Composable
internal fun KeyPanel(onAction: (AskAction) -> Unit) {
    val language = LocalLanguage.current
    val layout = SiteTheme.layout
    val colors = PantrixTheme.colors
    Column(
        Modifier
            .then(if (layout == SiteLayout.Desktop) Modifier.widthIn(max = KEY_WIDTH) else Modifier.fillMaxWidth())
            .landingCard(SiteTheme.colors.surfaceAlt, colors.borderHover, corner = 16.dp)
            .padding(if (layout == SiteLayout.Phone) 18.dp else 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Badge(AskSay.PhaseTwo.of(language).uppercase())
        PantrixText(
            text = AskSay.KeyTitle.of(language),
            style = strong(if (layout == SiteLayout.Phone) 19.sp else 22.sp),
            color = colors.textPrimary
        )
        PantrixText(text = AskSay.KeyLead.of(language), style = small(15.sp), color = colors.textSecondary)
        AskSay.KeyPoints.forEach { point ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                PantrixIcon(
                    imageVector = PantrixIcons.Check,
                    contentDescription = null,
                    tint = colors.primary.onContainer,
                    size = 18.dp,
                    modifier = Modifier.padding(top = 2.dp)
                )
                PantrixText(text = point.of(language), style = small(14.5.sp), color = colors.textPrimary)
            }
        }
        PantrixButton(
            text = AskSay.UntilThen.of(language),
            onClick = { onAction(AskAction.PickTab(AskTab.Assistant)) },
            variant = ButtonVariant.Outline,
            size = ButtonSize.Large,
            tagKey = "askUntilThen"
        )
    }
}

/** Cursor's link that adds the server: the server as `mcp.json` has it, in base64 (cursor.com/docs, MCP links). */
internal fun cursorLink(): String {
    val config = """{"url":"${SiteAddress.mcp}"}"""
    return "cursor://anysphere.cursor-deeplink/mcp/install?name=$SERVER&config=" +
        Base64.encode(config.encodeToByteArray())
}

/** VS Code's command that adds the server to the user's profile (code.visualstudio.com/docs, MCP configuration). */
internal fun vsCodeCommand(): String =
    """code --add-mcp '{"name":"$SERVER","type":"http","url":"${SiteAddress.mcp}"}'"""

/** Claude Code's commands that add the plugin, which brings the server and the skill. */
private val CLAUDE_CODE = listOf(
    "claude plugin marketplace add ${LibraryFacts.PACKAGES_REPOSITORY}",
    "claude plugin install pantrix-ui@pantrix-ui"
)

/** The server's name in an assistant's settings. */
private const val SERVER = "pantrix-ui"

/** How wide a desktop's card of the key runs at most. */
private val KEY_WIDTH = 760.dp
