package com.pantrixui.site.feature.download

import com.pantrixui.site.core.base.BaseViewModel
import com.pantrixui.site.core.base.LoadState
import com.pantrixui.site.core.base.UiAction
import com.pantrixui.site.core.base.UiEffect
import com.pantrixui.site.core.base.UiState
import com.pantrixui.site.core.base.loadState
import com.pantrixui.site.core.base.valueOrNull
import com.pantrixui.site.core.content.ReleaseRepository
import com.pantrixui.site.core.model.DesktopRelease
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

/**
 * The download page, as its screen draws it.
 *
 * @property release the desktop app's latest release: its version, its day, and the size and hash of its file. The
 *   page is whole without it, its links leading to whatever release is the latest.
 * @property copied whether the hash was copied a moment ago, which its button says for a while.
 */
data class DownloadUiState(
    val release: LoadState<DesktopRelease> = LoadState.Loading,
    val copied: Boolean = false
) : UiState

/** What the user does on the download page. */
sealed interface DownloadAction : UiAction {
    /** Opens [url]: the file to download, or a page of the releases. */
    data class OpenUrl(val url: String) : DownloadAction

    /** Copies the latest release's hash, where it is known. */
    data object CopyHash : DownloadAction

    /** Tells the model whether the platform took the copy: [done] where it did. */
    data class Copied(val done: Boolean) : DownloadAction
}

/** What the download page asks of the platform. */
sealed interface DownloadEffect : UiEffect {
    /** Opens [url] outside the app: in a new tab on the web, in the browser elsewhere. */
    data class OpenUrl(val url: String) : DownloadEffect

    /** Puts [text] on the clipboard. */
    data class Copy(val text: String) : DownloadEffect
}

/**
 * The model of the download page: it reads the desktop app's latest release from [releases] as it opens, and copies
 * its hash.
 *
 * @param releases where the latest release is read from.
 */
class DownloadViewModel(
    private val releases: ReleaseRepository
) : BaseViewModel<DownloadUiState, DownloadAction, DownloadEffect>(DownloadUiState()) {
    private var copiedReset: Job? = null

    init {
        launch {
            val release = loadState { releases.latest() }
            updateState { copy(release = release) }
        }
    }

    override fun handle(action: DownloadAction) {
        when (action) {
            is DownloadAction.OpenUrl -> sendEffect(DownloadEffect.OpenUrl(action.url))

            DownloadAction.CopyHash -> currentState.release.valueOrNull?.macSha256?.let { hash ->
                sendEffect(DownloadEffect.Copy(hash))
            }

            is DownloadAction.Copied -> if (action.done) showCopied()
        }
    }

    private fun showCopied() {
        copiedReset?.cancel()
        updateState { copy(copied = true) }
        copiedReset = launch {
            delay(COPIED_FOR)
            updateState { copy(copied = false) }
        }
    }

    private companion object {
        /** How long the button says the hash was copied. */
        val COPIED_FOR = 2.seconds
    }
}
