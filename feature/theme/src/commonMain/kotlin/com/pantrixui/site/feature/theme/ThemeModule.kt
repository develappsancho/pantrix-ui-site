package com.pantrixui.site.feature.theme

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** What the theme builder is made of: its model. */
val themeModule: Module = module {
    viewModel { ThemeViewModel() }
}
