package com.pantrixui.site

import com.pantrixui.site.core.content.AskRepository
import com.pantrixui.site.core.content.ContentRepository
import com.pantrixui.site.core.content.GitHubReleaseRepository
import com.pantrixui.site.core.content.McpAskRepository
import com.pantrixui.site.core.content.ReleaseRepository
import com.pantrixui.site.core.content.ResourceContentRepository
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.feature.adaptive.adaptiveModule
import com.pantrixui.site.feature.ai.aiModule
import com.pantrixui.site.feature.components.componentsModule
import com.pantrixui.site.feature.docs.docsModule
import com.pantrixui.site.feature.download.downloadModule
import com.pantrixui.site.feature.foundations.foundationsModule
import com.pantrixui.site.feature.home.homeModule
import com.pantrixui.site.feature.roadmap.roadmapModule
import com.pantrixui.site.feature.search.searchModule
import com.pantrixui.site.feature.theme.themeModule
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.mp.KoinPlatform

/**
 * What the site's frame is made of: where its content and the desktop app's latest release come from, and its own
 * model.
 */
val siteModule: Module = module {
    single<ContentRepository> { ResourceContentRepository() }
    single<ReleaseRepository> { GitHubReleaseRepository() }
    single<AskRepository> { McpAskRepository() }
    viewModel { (language: Language) -> SiteViewModel(language) }
}

/** Every module of the site: the frame's and each feature's. */
val siteModules: List<Module> = listOf(
    siteModule,
    docsModule,
    componentsModule,
    homeModule,
    foundationsModule,
    themeModule,
    adaptiveModule,
    aiModule,
    roadmapModule,
    downloadModule,
    searchModule
)

/**
 * Starts the site's dependency graph, once for the process: each platform's entry point calls it before it draws the
 * site, and an Android activity created again finds it started.
 *
 * @param overrides modules whose definitions take the place of the site's own, as a tool that draws the site without
 *   the network gives where the desktop app's latest release comes from.
 */
fun startSiteKoin(vararg overrides: Module) {
    if (KoinPlatform.getKoinOrNull() == null) {
        startKoin {
            allowOverride(true)
            modules(siteModules + overrides)
        }
    }
}
