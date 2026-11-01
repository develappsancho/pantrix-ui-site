package com.pantrixui.site.feature.docs

import com.pantrixui.site.core.base.LoadState
import com.pantrixui.site.core.base.valueOrNull
import com.pantrixui.site.core.model.Block
import com.pantrixui.site.core.model.ChangeEntry
import com.pantrixui.site.core.model.ChangeGroup
import com.pantrixui.site.core.model.ChangeKind
import com.pantrixui.site.core.model.ChangeSection
import com.pantrixui.site.core.model.Changelog
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Release
import com.pantrixui.site.core.model.Span
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
class ChangelogViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun entry(
        label: String,
        breaking: Boolean = false
    ) = ChangeEntry(label, listOf(Block.Paragraph(listOf(Span.Text("$label.")))), breaking)

    private val changelog = Changelog(
        language = Language.English,
        title = "Changelog",
        description = listOf(Span.Text("What changed.")),
        releases = listOf(
            Release(
                sections = listOf(
                    ChangeSection(
                        ChangeKind.Added,
                        "Added",
                        listOf(
                            ChangeGroup("Foundation", "foundation", entries = listOf(entry("Theme"), entry("Tokens"))),
                            ChangeGroup("Tooling", "tooling", entries = listOf(entry("MCP")))
                        )
                    ),
                    ChangeSection(
                        ChangeKind.Removed,
                        "Removed",
                        listOf(ChangeGroup("Foundation", "foundation", entries = listOf(entry("Spacing", true))))
                    )
                )
            )
        ),
        markdown = "# Changelog",
        upcoming = "0.1.0"
    )

    private fun model() = ChangelogViewModel(Language.English, FakeContentRepository(changelogs = listOf(changelog)))

    @Test
    fun the_changelog_s_modules_are_counted_across_its_kinds_in_the_order_it_names_them() {
        assertEquals(
            listOf(ChangeModule("foundation", "Foundation", 3), ChangeModule("tooling", "Tooling", 1)),
            changelog.modules()
        )
    }

    @Test
    fun a_module_and_the_breaking_switch_leave_only_their_lines_and_drop_what_is_left_empty() = runTest(dispatcher) {
        val model = model()
        advanceUntilIdle()
        assertEquals(LoadState.Loaded(changelog), model.state.value.changelog)
        val release = model.state.value.changelog.valueOrNull!!.releases.single()
        assertEquals(listOf(ChangeKind.Added, ChangeKind.Removed), model.state.value.shown(release).map { it.kind })
        model.onAction(ChangelogAction.PickModule("tooling"))
        advanceUntilIdle()
        assertEquals(
            listOf("MCP"),
            model.state.value.shown(release).flatMap {
                it.groups
            }.flatMap { it.entries }.map { it.label }
        )
        model.onAction(ChangelogAction.ToggleBreaking)
        advanceUntilIdle()
        assertTrue(model.state.value.shown(release).isEmpty(), "Tooling breaks nothing")
        model.onAction(ChangelogAction.PickModule(null))
        advanceUntilIdle()
        val shown = model.state.value.shown(release)
        assertEquals(listOf(ChangeKind.Removed), shown.map { it.kind })
        assertEquals(listOf("Spacing"), shown.flatMap { it.groups }.flatMap { it.entries }.map { it.label })
    }
}
