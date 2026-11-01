package com.pantrixui.site.feature.components

import com.pantrixui.site.core.model.Language
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** What the components' pages are made of: a model for the list, given its language, and one for a component's page. */
val componentsModule: Module = module {
    viewModel { (language: Language) -> ComponentsViewModel(language, get()) }
    viewModel { (key: String, language: Language) -> ComponentViewModel(key, language, get()) }
}
