package com.pantrixui.site.feature.docs

import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Route
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * What the documentation's pages are made of: a model for each page, given its route and its language, and one for
 * the guides page and the changelog, given their language.
 */
val docsModule: Module = module {
    viewModel { (route: Route, language: Language) -> DocViewModel(route, language, get()) }
    viewModel { (language: Language) -> GuidesViewModel(language, get()) }
    viewModel { (language: Language) -> ChangelogViewModel(language, get()) }
}
