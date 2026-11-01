package com.pantrixui.site.feature.foundations

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** What the foundations page is made of: its model. */
val foundationsModule: Module = module {
    viewModel { FoundationsViewModel() }
}
