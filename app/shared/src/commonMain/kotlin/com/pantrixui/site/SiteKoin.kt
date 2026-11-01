package com.pantrixui.site

import com.pantrixui.site.core.content.ContentRepository
import com.pantrixui.site.core.content.ResourceContentRepository
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.feature.docs.docsModule
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.mp.KoinPlatform

/** What the site's frame is made of: where its content comes from and its own model. */
val siteModule: Module = module {
    single<ContentRepository> { ResourceContentRepository() }
    viewModel { (language: Language) -> SiteViewModel(language) }
}

/** Every module of the site: the frame's and each feature's. */
val siteModules: List<Module> = listOf(siteModule, docsModule)

/**
 * Starts the site's dependency graph, once for the process: each platform's entry point calls it before it draws the
 * site, and an Android activity created again finds it started.
 */
fun startSiteKoin() {
    if (KoinPlatform.getKoinOrNull() == null) startKoin { modules(siteModules) }
}
