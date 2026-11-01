package com.pantrixui.site.feature.theme

import com.pantrixui.site.core.base.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

/** The model of the theme builder: the preset being made, how its preview is shown, and its Kotlin to copy. */
class ThemeViewModel : BaseViewModel<ThemeUiState, ThemeAction, ThemeEffect>(ThemeUiState()) {
    private var copiedReset: Job? = null

    override fun handle(action: ThemeAction) {
        when (action) {
            is ThemeAction.Name -> updateState { copy(name = action.name) }

            // A half-typed code is kept in the field; the colour changes once it is a whole one.
            is ThemeAction.Hex -> updateState {
                val argb = parseHex(action.text)
                copy(hexInput = action.text, primary = argb ?: primary)
            }

            is ThemeAction.PickColour -> updateState { copy(primary = action.argb, hexInput = hexOf(action.argb)) }

            is ThemeAction.PickCorners -> updateState { copy(corners = action.corners) }

            is ThemeAction.PickFace -> updateState { copy(face = action.face) }

            is ThemeAction.PickDensity -> updateState { copy(density = action.density) }

            is ThemeAction.PickPlatform -> updateState { copy(platform = action.type) }

            is ThemeAction.ShowDark -> updateState { copy(dark = action.dark) }

            ThemeAction.Reset -> updateState { ThemeUiState(dark = dark, platform = platform) }

            ThemeAction.CopyKotlin -> sendEffect(ThemeEffect.Copy(presetText(currentState)))

            is ThemeAction.Copied -> if (action.done) showCopied()

            is ThemeAction.ShowCode -> updateState { copy(codeOpen = action.open) }
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
        /** How long the button says the Kotlin was copied. */
        val COPIED_FOR = 2.seconds
    }
}
