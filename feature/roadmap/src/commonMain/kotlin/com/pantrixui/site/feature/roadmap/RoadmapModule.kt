package com.pantrixui.site.feature.roadmap

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** What the roadmap is made of: its model. */
val roadmapModule: Module = module {
    viewModel { RoadmapViewModel() }
}
