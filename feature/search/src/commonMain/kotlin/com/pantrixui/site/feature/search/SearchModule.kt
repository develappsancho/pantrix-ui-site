package com.pantrixui.site.feature.search

import com.pantrixui.site.core.model.Language
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** What the search is made of: its model, opened in a language. */
val searchModule: Module = module {
    viewModel { (language: Language) -> SearchViewModel(language, get()) }
}
