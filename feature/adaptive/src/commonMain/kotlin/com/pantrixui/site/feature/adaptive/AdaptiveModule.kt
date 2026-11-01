package com.pantrixui.site.feature.adaptive

import com.pantrixui.site.core.model.Language
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** What the adaptive page is made of: its model, given its language. */
val adaptiveModule: Module = module {
    viewModel { (language: Language) -> AdaptiveViewModel(language, get()) }
}
