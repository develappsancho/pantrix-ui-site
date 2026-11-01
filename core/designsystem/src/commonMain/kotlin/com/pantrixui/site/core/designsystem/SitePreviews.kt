package com.pantrixui.site.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.site.core.model.Block
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Span
import com.pantrixui.site.core.model.Token

/** How wide a desktop's first screen is in a page's preview: the width the site's designs are drawn at. */
const val PREVIEW_DESKTOP_WIDTH: Int = 1440

/** How tall a desktop's first screen is in a page's preview. */
const val PREVIEW_DESKTOP_HEIGHT: Int = 900

/** How wide a phone held upright is in a page's preview: the width of the site's phone designs. */
const val PREVIEW_PHONE_WIDTH: Int = 390

/** How tall a phone held upright is in a page's preview. */
const val PREVIEW_PHONE_HEIGHT: Int = 844

/**
 * What a preview of the site draws [content] in: the site's theme in English, in its light colours or, with [dark],
 * its dark ones, on the page's background. The theme lays a page out by the preview's width, as the site does by its
 * window's: a preview 1440 wide is the desktop's page, and one 390 wide the phone's.
 *
 * @param dark whether the theme is dark.
 * @param content what the preview shows.
 */
@Composable
fun SitePreview(
    dark: Boolean,
    content: @Composable () -> Unit
) {
    SiteTheme(dark = dark, language = Language.English) {
        Box(Modifier.fillMaxSize().background(PantrixTheme.colors.background)) { content() }
    }
}

/**
 * [text] as a block of code in [language], each of its lines a single plain token: what a preview's sample content
 * shows where the site would show code read from its pages.
 *
 * @param text the code, a line to each of its lines.
 * @param language the language the block names.
 * @param file the name of the file it stands for, written over it where the block has a header.
 */
fun previewCode(
    text: String,
    language: String = "kotlin",
    file: String? = null
): Block.Code = Block.Code(language, file, text.lines().map { listOf(Token(it)) })

/** [text] as the words of a preview's sample content: a single plain span. */
fun previewSpans(text: String): List<Span> = listOf(Span.Text(text))
