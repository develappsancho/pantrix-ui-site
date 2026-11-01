package com.pantrixui.site.core.designsystem.chrome

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pantrixui.site.core.designsystem.PREVIEW_DESKTOP_WIDTH
import com.pantrixui.site.core.designsystem.PREVIEW_PHONE_WIDTH
import com.pantrixui.site.core.designsystem.SitePreview

@Preview(widthDp = PREVIEW_DESKTOP_WIDTH, heightDp = HEADER_HEIGHT)
@Composable
private fun SiteHeaderPreview() {
    SiteHeaderSample(dark = false)
}

@Preview(widthDp = PREVIEW_DESKTOP_WIDTH, heightDp = HEADER_HEIGHT)
@Composable
private fun SiteHeaderDarkPreview() {
    SiteHeaderSample(dark = true)
}

@Preview(widthDp = PREVIEW_PHONE_WIDTH, heightDp = HEADER_HEIGHT)
@Composable
private fun SiteHeaderPhonePreview() {
    SiteHeaderSample(dark = false)
}

@Preview(widthDp = PREVIEW_PHONE_WIDTH, heightDp = HEADER_HEIGHT)
@Composable
private fun SiteHeaderPhoneDarkPreview() {
    SiteHeaderSample(dark = true)
}

@Preview(widthDp = PART_WIDTH, heightDp = SIDEBAR_HEIGHT)
@Composable
private fun SiteSidebarPreview() {
    SiteSidebarSample(dark = false)
}

@Preview(widthDp = PART_WIDTH, heightDp = SIDEBAR_HEIGHT)
@Composable
private fun SiteSidebarDarkPreview() {
    SiteSidebarSample(dark = true)
}

@Preview(widthDp = PART_WIDTH, heightDp = FOOT_HEIGHT)
@Composable
private fun SiteFootPreview() {
    SiteFootSample(dark = false)
}

@Preview(widthDp = PART_WIDTH, heightDp = FOOT_HEIGHT)
@Composable
private fun SiteFootDarkPreview() {
    SiteFootSample(dark = true)
}

/**
 * The site's header: on a desktop the parts of the site, the search box, the languages and the theme button; on a
 * phone the brand, the search and the menu button.
 */
@Composable
private fun SiteHeaderSample(dark: Boolean) {
    SitePreview(dark) {
        SiteHeader(
            links = listOf(
                HeaderLink(key = "docs", label = "Docs", current = true),
                HeaderLink(key = "components", label = "Components"),
                HeaderLink(key = "foundations", label = "Foundations"),
                HeaderLink(key = "ai", label = "AI")
            ),
            onNavigate = {},
            onSearch = {},
            onLanguage = {},
            addressIn = { "/docs/installation" },
            dark = dark,
            onToggleTheme = {},
            onOpenMenu = {},
            onOpenRepository = {}
        )
    }
}

/** The docs' sidebar: two sections, the page on show among the first, and a count beside a part with pages. */
@Composable
private fun SiteSidebarSample(dark: Boolean) {
    SitePreview(dark) {
        SiteSidebar(
            sections = listOf(
                NavSection(
                    title = "Getting started",
                    items = listOf(
                        NavItem(key = "introduction", label = "Introduction", href = "/docs/introduction"),
                        NavItem(
                            key = "installation",
                            label = "Installation",
                            href = "/docs/installation",
                            current = true
                        ),
                        NavItem(key = "theming", label = "Theming", href = "/docs/theming")
                    )
                ),
                NavSection(
                    title = "Library",
                    items = listOf(
                        NavItem(key = "components", label = "Components", href = "/components", count = 100),
                        NavItem(key = "guides", label = "Guides", href = "/docs/guides")
                    )
                )
            ),
            onSelect = {},
            modifier = Modifier.height(SIDEBAR_HEIGHT.dp)
        )
    }
}

/** The foot of the site's pages: its links and its line of copyright. */
@Composable
private fun SiteFootSample(dark: Boolean) {
    SitePreview(dark) { SiteFoot() }
}

/** How tall the previews of the header, the sidebar and the foot are. */
private const val HEADER_HEIGHT = 72

private const val SIDEBAR_HEIGHT = 400

private const val FOOT_HEIGHT = 80

/** How wide the previews of the sidebar and the foot are: wide enough for a desktop's layout, which they belong to. */
private const val PART_WIDTH = 900
