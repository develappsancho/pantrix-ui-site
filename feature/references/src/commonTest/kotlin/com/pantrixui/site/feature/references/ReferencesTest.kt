package com.pantrixui.site.feature.references

import com.pantrixui.site.core.base.LoadState
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.PlatformKind
import com.pantrixui.site.core.model.Reference
import com.pantrixui.site.core.model.ReferencePlatform
import com.pantrixui.site.core.model.References
import com.pantrixui.site.core.testing.FakeContentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/** The references page's model: what it reads, how it narrows the list, and where an address leads. */
@OptIn(ExperimentalCoroutinesApi::class)
class ReferencesTest {
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun the_apps_of_the_page_s_language_are_read_as_it_opens() = runTest {
        val model = ReferencesViewModel(Language.Turkish, repository())
        val projects = assertIs<LoadState.Loaded<List<Reference>>>(model.state.value.projects).value
        assertEquals(listOf("site", "notes", "atlas"), projects.map { it.key })
        assertEquals("Site tr", projects.first().name)
    }

    /** The list is every app but the featured first, narrowed to a platform, and all of them again. */
    @Test
    fun the_list_leaves_the_featured_app_out_and_narrows_by_platform() = runTest {
        val model = ReferencesViewModel(Language.English, repository())
        assertEquals(listOf("notes", "atlas"), model.state.value.listed.map { it.key })
        model.onAction(ReferencesAction.PickPlatform(PlatformKind.Web))
        assertEquals(listOf("atlas"), model.state.value.listed.map { it.key })
        model.onAction(ReferencesAction.PickPlatform(PlatformKind.Desktop))
        assertEquals(emptyList(), model.state.value.listed.map { it.key })
        model.onAction(ReferencesAction.PickPlatform(null))
        assertEquals(listOf("notes", "atlas"), model.state.value.listed.map { it.key })
    }

    /** An address of the site opens its page in the page's language; any other leaves the site. */
    @Test
    fun an_address_of_the_site_opens_its_page_and_any_other_leaves_it() = runTest {
        val model = ReferencesViewModel(Language.Turkish, repository())
        model.onAction(ReferencesAction.Open("/download"))
        assertEquals(ReferencesEffect.Navigate("/tr/download"), model.effects.first())
        model.onAction(ReferencesAction.Open("https://notes.app"))
        assertEquals(ReferencesEffect.OpenUrl("https://notes.app"), model.effects.first())
    }

    @Test
    fun apps_that_cannot_be_read_are_read_again_on_retry() = runTest {
        val content = repository()
        content.failure = IllegalStateException("offline")
        val model = ReferencesViewModel(Language.English, content)
        assertIs<LoadState.Failed>(model.state.value.projects)
        content.failure = null
        model.onAction(ReferencesAction.Retry)
        assertIs<LoadState.Loaded<List<Reference>>>(model.state.value.projects)
    }

    private fun repository() = FakeContentRepository(
        references = Language.entries.associateWith { language ->
            References(
                listOf(
                    app("site", language, PlatformKind.Web, PlatformKind.Desktop),
                    app("notes", language, PlatformKind.Android, PlatformKind.Ios),
                    app("atlas", language, PlatformKind.Web)
                )
            )
        }
    )

    private fun app(
        key: String,
        language: Language,
        vararg kinds: PlatformKind
    ) = Reference(
        key = key,
        name = if (key == "site") "Site ${language.code}" else key,
        address = "$key.app",
        line = "A line.",
        about = "About it.",
        url = "https://$key.app",
        platforms = kinds.map { ReferencePlatform(it, it.name, "On ${it.name}.") }
    )
}
