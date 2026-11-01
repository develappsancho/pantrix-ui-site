package com.pantrixui.site.feature.download

import com.pantrixui.site.core.base.LoadState
import com.pantrixui.site.core.content.ReleaseRepository
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.model.DesktopRelease
import com.pantrixui.site.core.model.SiteDownloads
import com.pantrixui.site.core.model.Words
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DownloadTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun the_page_reads_the_latest_release_as_it_opens() = runTest(dispatcher) {
        val model = DownloadViewModel { RELEASE }
        assertEquals(LoadState.Loading, model.state.value.release)
        advanceUntilIdle()
        assertEquals(LoadState.Loaded(RELEASE), model.state.value.release)
    }

    @Test
    fun a_release_that_cannot_be_read_leaves_the_page_whole_without_it() = runTest(dispatcher) {
        val model = DownloadViewModel { error("No network") }
        advanceUntilIdle()
        assertIs<LoadState.Failed>(model.state.value.release)
        model.onAction(DownloadAction.CopyHash)
        model.onAction(DownloadAction.OpenUrl(SiteDownloads.MAC_DOWNLOAD))
        // The copy asks for nothing without a hash: the first effect is the link's.
        assertEquals(DownloadEffect.OpenUrl(SiteDownloads.MAC_DOWNLOAD), model.effects.first())
    }

    @Test
    fun the_hash_is_copied_and_the_button_says_so_for_a_while() = runTest(dispatcher) {
        val model = DownloadViewModel(ReleaseRepository { RELEASE })
        advanceUntilIdle()
        model.onAction(DownloadAction.CopyHash)
        assertEquals(DownloadEffect.Copy(HASH), model.effects.first())
        model.onAction(DownloadAction.Copied(done = false))
        assertFalse(model.state.value.copied, "a copy the platform did not take is not said to be done")
        model.onAction(DownloadAction.Copied(done = true))
        assertTrue(model.state.value.copied)
        advanceTimeBy(COPIED_FOR_MILLISECONDS + 1)
        assertFalse(model.state.value.copied)
    }

    @Test
    fun the_page_says_everything_in_both_languages_in_every_size() {
        val sized = listOf(DownloadSay.Lead, DownloadSay.PlatformsLead).flatMap { words ->
            SiteLayout.entries.map { words.of(it) }
        }
        val words: List<Words> = sized + listOf(
            DownloadSay.Kicker,
            DownloadSay.Title,
            DownloadSay.DownloadForMac,
            DownloadSay.ReleaseNotes,
            DownloadSay.Runs,
            DownloadSay.Version,
            DownloadSay.Signed,
            DownloadSay.PlatformsTitle,
            DownloadSay.Available,
            DownloadSay.ComingLater,
            DownloadSay.DownloadDmg,
            DownloadSay.WatchReleases,
            DownloadSay.InstallTitle,
            DownloadSay.CheckTitle,
            DownloadSay.CheckBody,
            DownloadSay.ReadingRelease,
            DownloadSay.HashOnReleasePage,
            DownloadSay.Copy,
            DownloadSay.Copied,
            DownloadSay.CopyHash,
            DownloadSay.FeaturesTitle,
            DownloadSay.WindowSave,
            DownloadSay.WindowCancel
        ) + DownloadSay.Platforms.map { it.body } + DownloadSay.Steps.flatMap { listOf(it.title, it.start) } +
            DownloadSay.Features.flatMap { listOf(it.title, it.body) }
        words.forEach { assertTrue(it.english.isNotBlank() && it.turkish.isNotBlank(), it.english) }
        assertEquals(listOf(true, false, false), DownloadSay.Platforms.map { it.available }, "macOS alone comes now")
        assertEquals(1, DownloadSay.Steps.count { it.file }, "one step names the file")
    }

    private companion object {
        const val HASH = "0e6bb0079ca77d507192d5b8530f5897002a62ae4ab8d02dde71863f766707a5"
        const val COPIED_FOR_MILLISECONDS = 2_000L

        val RELEASE = DesktopRelease(
            version = "26.10.31",
            year = 2026,
            month = 10,
            day = 31,
            notes = "${SiteDownloads.RELEASES}/tag/v26.10.31",
            macSize = 92_143_171,
            macSha256 = HASH
        )
    }
}
