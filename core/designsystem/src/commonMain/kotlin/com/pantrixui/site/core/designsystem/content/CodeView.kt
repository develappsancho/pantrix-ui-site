package com.pantrixui.site.core.designsystem.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.data.codeblock.CodeBlockColors
import com.pantrixui.components.data.codeblock.CodeBlockStyle
import com.pantrixui.components.data.codeblock.CodeToken
import com.pantrixui.components.data.codeblock.CodeTokenKind
import com.pantrixui.components.data.codeblock.PantrixCodeBlock
import com.pantrixui.components.elements.button.ButtonColors
import com.pantrixui.components.elements.button.ButtonSize
import com.pantrixui.components.elements.button.ButtonSizeStyle
import com.pantrixui.components.elements.button.ButtonStyle
import com.pantrixui.components.elements.button.ButtonVariant
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.token.shape.ShapeSpec
import com.pantrixui.site.core.designsystem.CodeColors
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.model.Block
import com.pantrixui.site.core.model.Token
import com.pantrixui.site.core.model.TokenKind

/**
 * A block of the site's code, dark in both themes: the file it is from or its tabs over it, the code in its colours
 * under it, in the site's code type.
 *
 * @param code the code and its tokens.
 * @param modifier the block's modifier.
 * @param tabs the views a header offers to switch between, the same file for two repositories: their labels.
 * @param selectedTab which of [tabs] is on show.
 * @param onSelectTab what choosing a tab does.
 * @param label what the tabs are views of, at the header's start, the tabs then small at its end: `commonMain`.
 * @param fileHeaderHeight how tall the header is where it holds the file alone, where the page's design says.
 * @param compact whether the block is a small one inside a box: no header, smaller code, closer to its edges.
 * @param shape the block's corners: rounded unless it fills a panel of its own, edge to edge.
 */
@Composable
fun CodeView(
    code: Block.Code,
    modifier: Modifier = Modifier,
    tabs: List<String> = emptyList(),
    selectedTab: Int = 0,
    onSelectTab: (Int) -> Unit = {},
    label: String? = null,
    fileHeaderHeight: Dp? = null,
    compact: Boolean = false,
    shape: Shape = CodeShape
) {
    val colors = SiteTheme.colors.code
    val layout = SiteTheme.layout
    val (text, tokens) = remember(code) { code.lines.toCodeAndTokens() }
    val tabbed = tabs.isNotEmpty()
    if (compact) {
        SmallCode(code, modifier)
        return
    }
    if (tabbed && label != null) {
        LabelledCode(text, tokens, label, tabs, selectedTab, onSelectTab, colors, modifier)
        return
    }
    // A desktop's tabs have the file beside them, at the header's end; a tablet's and a phone's leave it out.
    val file = code.file?.takeIf { !tabbed || layout == SiteLayout.Desktop }
    PantrixCodeBlock(
        code = text,
        modifier = modifier.fillMaxWidth(),
        tokens = tokens,
        copyable = false,
        trailing = if (tabbed && file != null) ({ FileName(file, colors, Modifier.padding(end = 6.dp)) }) else null,
        colors = colors.forCodeBlock(),
        style = codeStyle(tabbed, layout, fileHeaderHeight, shape),
        leading = when {
            tabbed -> ({ CodeTabs(tabs, selectedTab, onSelectTab, colors, layout) })
            file != null -> ({ FileName(file, colors) })
            else -> null
        },
        textStyle = SiteTheme.type.code
    )
}

/** The block's room: its header as tall as the design has it, a file's alone or the tabs', and its edges. */
private fun codeStyle(
    tabbed: Boolean,
    layout: SiteLayout,
    fileHeaderHeight: Dp?,
    shape: Shape
) = CodeBlockStyle(
    shape = ShapeSpec.Fixed(shape),
    horizontalPadding = if (layout == SiteLayout.Phone) 16.dp else 18.dp,
    verticalPadding = if (layout == SiteLayout.Phone) 14.dp else 16.dp,
    borderWidth = 0.dp,
    headerHeight = when {
        tabbed && layout != SiteLayout.Desktop -> 48.dp
        !tabbed && fileHeaderHeight != null -> fileHeaderHeight
        !tabbed && layout == SiteLayout.Phone -> 40.dp
        else -> 42.dp
    },
    headerLineWidth = 1.dp,
    headerHorizontalPadding = when {
        !tabbed -> 14.dp
        layout == SiteLayout.Desktop -> 8.dp
        else -> 6.dp
    }
)

/**
 * A small block of the site's code inside a box: the code alone, in its colours, [textSize] and its lines [line]
 * times that apart, [horizontalPadding] and [verticalPadding] from its edges and rounded [corner], as the box's design
 * has it.
 *
 * @param code the code and its tokens.
 * @param modifier the block's modifier.
 * @param textSize the size of its code.
 * @param line how far apart its lines are, in its size.
 * @param horizontalPadding the room at its sides.
 * @param verticalPadding the room over and under it.
 * @param corner how round its corners are.
 */
@Composable
fun SmallCode(
    code: Block.Code,
    modifier: Modifier = Modifier,
    textSize: TextUnit = COMPACT_CODE_SIZE,
    line: Float = CODE_LINE,
    horizontalPadding: Dp = 16.dp,
    verticalPadding: Dp = 14.dp,
    corner: Dp = 14.dp
) {
    val (text, tokens) = remember(code) { code.lines.toCodeAndTokens() }
    PantrixCodeBlock(
        code = text,
        modifier = modifier.fillMaxWidth(),
        tokens = tokens,
        copyable = false,
        colors = SiteTheme.colors.code.forCodeBlock(),
        style = CodeBlockStyle(
            shape = ShapeSpec.Fixed(RoundedCornerShape(corner)),
            horizontalPadding = horizontalPadding,
            verticalPadding = verticalPadding,
            borderWidth = 0.dp
        ),
        textStyle = SiteTheme.type.code.copy(fontSize = textSize, lineHeight = textSize * line)
    )
}

/**
 * Views of one piece of code whose header names what they are views of at its start, `commonMain`, and the views at
 * its end, in small tabs.
 */
@Composable
private fun LabelledCode(
    text: String,
    tokens: List<CodeToken>,
    label: String,
    tabs: List<String>,
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
    colors: CodeColors,
    modifier: Modifier
) {
    val layout = SiteTheme.layout
    PantrixCodeBlock(
        code = text,
        modifier = modifier.fillMaxWidth(),
        tokens = tokens,
        copyable = false,
        // The header keeps 14 at its start and 8 at its end: the label stands 6 further in than the tabs. The label
        // keeps its width and the tabs share the rest, their words wrapping where a phone leaves them too little.
        leading = {
            FileName(label, colors, Modifier.padding(start = 6.dp))
            Row(
                Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, tab ->
                    CodeTab(
                        label = tab,
                        selected = index == selectedTab,
                        onClick = { onSelectTab(index) },
                        colors = colors,
                        layout = SiteLayout.Desktop,
                        tag = "codeTab$index",
                        modifier = Modifier.weight(1f, fill = false),
                        small = true
                    )
                }
            }
        },
        colors = colors.forCodeBlock(),
        style = CodeBlockStyle(
            shape = ShapeSpec.Fixed(CodeShape),
            horizontalPadding = if (layout == SiteLayout.Phone) 16.dp else 18.dp,
            verticalPadding = if (layout == SiteLayout.Phone) 14.dp else 16.dp,
            borderWidth = 0.dp,
            headerHeight = 44.dp,
            headerLineWidth = 1.dp,
            headerHorizontalPadding = 8.dp
        ),
        textStyle = SiteTheme.type.code
    )
}

/** The header's tabs, side by side; a phone's share its width between them. */
@Composable
private fun RowScope.CodeTabs(
    tabs: List<String>,
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
    colors: CodeColors,
    layout: SiteLayout
) {
    val phone = layout == SiteLayout.Phone
    Row(
        modifier = if (phone) Modifier.weight(1f) else Modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEachIndexed { index, label ->
            CodeTab(
                label = label,
                selected = index == selectedTab,
                onClick = { onSelectTab(index) },
                colors = colors,
                layout = layout,
                tag = "codeTab$index",
                modifier = if (phone) Modifier.weight(1f) else Modifier
            )
        }
    }
}

/** The file the code is from, as the header writes it. */
@Composable
private fun FileName(
    file: String,
    colors: CodeColors,
    modifier: Modifier = Modifier
) {
    PantrixText(
        text = file,
        color = colors.comment,
        style = SiteTheme.type.button.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
        maxLines = 1,
        modifier = modifier
    )
}

/** One of a block's tabs: 28 tall on a desktop, 36 on a tablet, 38 on a phone, where it is as wide as its share. */
@Composable
private fun CodeTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    colors: CodeColors,
    layout: SiteLayout,
    tag: String,
    modifier: Modifier,
    small: Boolean = false
) {
    val desktop = layout == SiteLayout.Desktop
    PantrixButton(
        onClick = onClick,
        contentDescription = label,
        modifier = modifier.semantics { this.selected = selected },
        variant = ButtonVariant.Text,
        size = ButtonSize.Medium,
        style = ButtonStyle(
            medium = ButtonSizeStyle(
                height = when (layout) {
                    SiteLayout.Desktop -> 28.dp
                    SiteLayout.Tablet -> 36.dp
                    SiteLayout.Phone -> 38.dp
                },
                horizontalPadding = if (desktop) 10.dp else 12.dp
            )
        ),
        shape = RoundedCornerShape(if (desktop) 6.dp else 8.dp),
        colors = ButtonColors(
            container = if (selected) colors.selectedTab else Color.Transparent,
            content = if (selected) colors.onSelectedTab else colors.comment
        ),
        tagKey = tag
    ) {
        PantrixText(
            text = label,
            style = SiteTheme.type.button.copy(
                fontSize = when {
                    small -> 12.sp
                    desktop -> 12.5.sp
                    else -> 13.sp
                },
                fontWeight = FontWeight.Bold,
                lineHeight = if (small) 16.sp else TextUnit.Unspecified
            ),
            // A small tab beside a label wraps its words rather than cutting them, as the design's does on a phone.
            maxLines = if (small) 2 else 1,
            textAlign = TextAlign.Center
        )
    }
}

/** The block's colours for Pantrix's code block, each kind in the site's own. */
private fun CodeColors.forCodeBlock(): CodeBlockColors = CodeBlockColors(
    container = background,
    text = text,
    comment = comment,
    keyword = keyword,
    type = function,
    string = literal,
    number = number,
    punctuation = text,
    annotation = annotation,
    lineNumber = comment,
    border = line
)

/** The lines as one text and its tokens, counted from the start of the whole. */
internal fun List<List<Token>>.toCodeAndTokens(): Pair<String, List<CodeToken>> {
    val text = StringBuilder()
    val tokens = mutableListOf<CodeToken>()
    forEachIndexed { index, line ->
        if (index > 0) text.append('\n')
        line.forEach { token ->
            val start = text.length
            text.append(token.text)
            token.kind.codeKind?.let { tokens += CodeToken(start, text.length, it) }
        }
    }
    return text.toString() to tokens
}

private val TokenKind.codeKind: CodeTokenKind?
    get() = when (this) {
        TokenKind.Plain -> null
        TokenKind.Keyword -> CodeTokenKind.Keyword
        TokenKind.StringLiteral -> CodeTokenKind.StringLiteral
        TokenKind.Number -> CodeTokenKind.Number
        TokenKind.Comment -> CodeTokenKind.Comment
        TokenKind.Annotation -> CodeTokenKind.Annotation
        TokenKind.Function, TokenKind.Type -> CodeTokenKind.Type
    }

private val CodeShape = RoundedCornerShape(14.dp)

/** The size of the code in a small block inside a box, and the line it takes. */
private val COMPACT_CODE_SIZE = 12.sp
private const val CODE_LINE = 1.75f
