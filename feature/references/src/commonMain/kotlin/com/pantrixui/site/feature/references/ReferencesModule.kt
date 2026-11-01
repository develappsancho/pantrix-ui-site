package com.pantrixui.site.feature.references

import com.pantrixui.site.core.model.Language
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** What the references page is made of: its model, which reads the apps in the page's language. */
val referencesModule: Module = module {
    viewModel { (language: Language) -> ReferencesViewModel(language, get()) }
}
