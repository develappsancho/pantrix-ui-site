package com.pantrixui.site.feature.adaptive

import androidx.compose.ui.unit.Density
import com.pantrixui.foundation.adaptive.WindowWidthClass
import com.pantrixui.foundation.adaptive.paneCount
import com.pantrixui.site.core.base.LoadState
import com.pantrixui.site.core.base.valueOrNull
import com.pantrixui.site.core.designsystem.device.SimDevice
import com.pantrixui.site.core.designsystem.device.SimFold
import com.pantrixui.site.core.designsystem.device.windowOf
import com.pantrixui.site.core.model.Block
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Page
import com.pantrixui.site.core.model.Span
import com.pantrixui.site.core.model.Token
import com.pantrixui.site.core.testing.FakeContentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AdaptiveTest {
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
    fun a_simulated_window_is_classed_as_pantrix_classes_it_and_a_foldable_is_folded() {
        val density = Density(2f)
        assertEquals(WindowWidthClass.Compact, windowOf(SimDevice.Phone, density).widthClass)
        assertEquals(WindowWidthClass.Large, windowOf(SimDevice.Laptop, density).widthClass)
        val book = windowOf(SimDevice.Book, density)
        assertTrue(book.posture.isBook)
        assertEquals(2, book.paneCount(), "a book is two panes at every width")
        assertTrue(windowOf(SimDevice.Table, density).posture.isTabletop)
        assertEquals(1, windowOf(SimDevice.Phone, density).paneCount())
    }

    @Test
    fun the_inbox_s_navigation_moves_with_the_width_and_stays_down_on_a_table() {
        assertEquals(SimNavigation.Bottom, navigationOf(SimDevice.Phone))
        assertEquals(SimNavigation.Bottom, navigationOf(SimDevice.Table), "the hands are at the bottom")
        assertEquals(SimNavigation.Rail, navigationOf(SimDevice.Tablet))
        assertEquals(SimNavigation.Sidebar, navigationOf(SimDevice.Laptop))
    }

    @Test
    fun the_why_says_the_room_beside_the_navigation_and_what_fits_in_it() {
        val laptop = why(AdaptiveUiState(device = SimDevice.Laptop))
        assertTrue("1032dp beside the sidebar is Expanded, so 2 panes." in laptop.english, laptop.english)
        val side = why(AdaptiveUiState(device = SimDevice.PhoneSide))
        assertTrue("The height is Compact" in side.english, side.english)
        assertEquals(AdaptiveSay.WhyVideoTable, why(AdaptiveUiState(device = SimDevice.Table, app = SimApp.VideoCall)))
        assertEquals(3, panesIn(room = 1432, fold = SimFold.Flat))
    }

    @Test
    fun the_code_is_read_under_its_headings_in_the_page_s_language() = runTest(dispatcher) {
        val page = Page(
            path = "/adaptive",
            language = Language.Turkish,
            title = "Uyarlanabilir",
            description = "",
            blocks = listOf(
                Block.Heading(3, "window", listOf(Span.Text("Pencere"))),
                Block.Code("kotlin", lines = listOf(listOf(Token("val window = PantrixTheme.window")))),
                Block.Heading(3, "frame", listOf(Span.Text("Çerçeve"))),
                Block.Code("kotlin", lines = listOf(listOf(Token("PantrixWindowFrame(book)"))))
            )
        )
        val model = AdaptiveViewModel(Language.Turkish, FakeContentRepository(listOf(page)))
        advanceUntilIdle()
        assertEquals(setOf("window", "frame"), model.state.value.code.valueOrNull?.keys)
        model.onAction(AdaptiveAction.PickDevice(SimDevice.Book))
        model.onAction(AdaptiveAction.PickApp(SimApp.VideoCall))
        assertEquals(SimDevice.Book, model.state.value.device)
        val missing = AdaptiveViewModel(Language.English, FakeContentRepository(listOf(page)))
        advanceUntilIdle()
        assertTrue(missing.state.value.code is LoadState.Failed, "no English page: no code, and the page says so")
    }
}
