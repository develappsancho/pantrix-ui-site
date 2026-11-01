package com.pantrixui.site.feature.home

import com.pantrixui.site.core.model.Language
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** What the landing page is made of: its model, given its language. */
val homeModule: Module = module {
    viewModel { (language: Language) -> HomeViewModel(language, get()) }
}
