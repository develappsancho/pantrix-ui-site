package com.pantrixui.site.feature.download

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** What the download page is made of: its model, which reads the desktop app's latest release. */
val downloadModule: Module = module {
    viewModel { DownloadViewModel(get()) }
}
