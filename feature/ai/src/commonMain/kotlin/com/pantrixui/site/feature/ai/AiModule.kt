package com.pantrixui.site.feature.ai

import com.pantrixui.site.core.model.Language
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** What the pages for coding assistants are made of: their models, each given its language. */
val aiModule: Module = module {
    viewModel { (language: Language) -> AiViewModel(language, get()) }
    viewModel { (language: Language) -> AskViewModel(language, get()) }
}
