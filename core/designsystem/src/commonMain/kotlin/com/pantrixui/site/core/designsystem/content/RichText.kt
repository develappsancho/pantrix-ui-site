package com.pantrixui.site.core.designsystem.content

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.takeOrElse
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.model.Span

/**
 * A run of the site's text: bold and slanted words, code inside a sentence, and links, which [onLink] opens with
 * their address.
 *
 * @param spans what it says.
 * @param onLink what a link does, given its address: a path of the site or a web address.
 * @param modifier the text's modifier.
 * @param style its type.
 * @param color its colour; [strongColor] is the colour of what is bold in it.
 * @param strongColor the colour of its bold words, its own colour unless given.
 * @param codeSize the size of its code inside a sentence, the site's own unless given: smaller in a smaller text.
 */
@Composable
fun RichText(
    spans: List<Span>,
    onLink: (String) -> Unit,
    modifier: Modifier = Modifier,
    style: TextStyle = SiteTheme.type.body,
    color: Color = PantrixTheme.colors.textSecondary,
    strongColor: Color = Color.Unspecified,
    codeSize: TextUnit = TextUnit.Unspecified
) {
    val text = richText(spans, onLink, strongColor = strongColor, codeSize = codeSize)
    PantrixText(text = text, modifier = modifier, style = style, color = color)
}

/**
 * [spans] as styled text, its links opening with [onLink]: its code in the code's family and the text's own colour,
 * except inside a link, which keeps the link's colour.
 */
@Composable
fun richText(
    spans: List<Span>,
    onLink: (String) -> Unit,
    strongColor: Color = Color.Unspecified,
    codeSize: TextUnit = TextUnit.Unspecified
): AnnotatedString {
    val code = SpanStyle(
        fontFamily = SiteTheme.type.inlineCode.fontFamily,
        fontSize = codeSize.takeOrElse { SiteTheme.type.inlineCode.fontSize },
        fontWeight = SiteTheme.type.inlineCode.fontWeight,
        color = PantrixTheme.colors.textPrimary
    )
    val linkStyles =
        TextLinkStyles(SpanStyle(color = PantrixTheme.colors.primary.onContainer, fontWeight = FontWeight.Bold))
    return buildAnnotatedString {
        fun append(
            span: Span,
            inLink: Boolean
        ) {
            when (span) {
                is Span.Text -> withStyle(
                    SpanStyle(
                        fontWeight = if (span.strong) FontWeight.Bold else null,
                        fontStyle = if (span.emphasis) FontStyle.Italic else null,
                        color = if (span.strong && !inLink) strongColor else Color.Unspecified
                    )
                ) { append(span.text) }

                is Span.Code -> withStyle(if (inLink) code.copy(color = Color.Unspecified) else code) {
                    append(span.text)
                }

                is Span.Link -> withLink(LinkAnnotation.Clickable(span.href, linkStyles) { onLink(span.href) }) {
                    span.text.forEach { append(it, inLink = true) }
                }
            }
        }
        spans.forEach { append(it, inLink = false) }
    }
}
