package com.pantrixui.site.feature.ai

import com.pantrixui.site.core.model.Language
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** What the page for coding assistants is made of: its model, given its language. */
val aiModule: Module = module {
    viewModel { (language: Language) -> AiViewModel(language, get()) }
}
