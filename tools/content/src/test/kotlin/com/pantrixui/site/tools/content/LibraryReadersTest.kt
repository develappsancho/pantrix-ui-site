package com.pantrixui.site.tools.content

import com.pantrixui.site.core.model.Block
import com.pantrixui.site.core.model.ChangeKind
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.Span
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The guides and the changelog as the site reads them from the library's documents, written here by hand. */
class LibraryReadersTest {
    @Test
    fun a_guide_takes_a_minute_for_each_two_hundred_words_outside_its_code() {
        assertEquals(1, SiteGuides.minutesOf("# A guide\n\nA few words."))
        val long = (1..401).joinToString(" ") { "word" } + "\n```\n" + (1..999).joinToString(" ") { "x" } + "\n```"
        assertEquals(3, SiteGuides.minutesOf(long))
    }

    private val english =
        """
        # Changelog

        Türkçesi: [CHANGELOG.tr.md](./CHANGELOG.tr.md)

        What changed in Pantrix UI.

        - **Format:** Keep a Changelog.

        ## [Unreleased]

        The first version.

        - Android, API 30 and newer;
        - the JVM.

        It will carry the following.

        ### Added

        #### Foundation

        - **Theme:** `PantrixTheme`, and a list:
          - colour roles;
          - type.
        - **Breaking:** **Tokens:** spacing is gone.

        #### Tooling

        - `com.pantrixui:mcp`, for assistants.

        ## [0.0.9] - 2026-10-01

        ### Fixed

        #### Components

        - **Button:** drawn right.
        """.trimIndent()

    private val turkish =
        """
        # Değişiklik Günlüğü

        English: [CHANGELOG.md](./CHANGELOG.md)

        Pantrix UI'da nelerin değiştiği.

        ## [Yayınlanmamış]

        İlk sürüm.

        *   Android, API 30 ve üstü;
        *   JVM.

        ### Eklenenler

        #### Foundation

        *   **Tema:** `PantrixTheme`.
        *   **Kırıcı:** **Tokenlar:** boşluk gitti.

        #### Araçlar

        *   `com.pantrixui:mcp`, asistanlar için.

        ## [0.0.9] - 2026-10-01

        ### Düzeltilenler

        #### Bileşenler

        *   **Düğme:** doğru çizilir.
        """.trimIndent()

    @Test
    fun a_changelog_has_its_versions_their_targets_their_kinds_their_modules_and_their_lines() {
        val changelog = ChangelogReader(Language.English).read(english)
        assertEquals("Changelog", changelog.title)
        assertEquals(listOf(Span.Text("What changed in Pantrix UI.")), changelog.description)
        val (next, last) = changelog.releases
        assertNull(next.version)
        assertEquals(listOf("Android, API 30 and newer", "the JVM"), next.targets.map { it.plainText() })
        assertEquals(listOf(Block.Paragraph(listOf(Span.Text("The first version.")))), next.intro)
        assertEquals(listOf(Block.Paragraph(listOf(Span.Text("It will carry the following.")))), next.lead)
        val (foundation, tooling) = next.sections.single().groups
        assertEquals(ChangeKind.Added, next.sections.single().kind)
        assertEquals("foundation", foundation.key)
        assertEquals(listOf("com.pantrixui:foundation"), foundation.artifacts)
        assertEquals(listOf("com.pantrixui:detekt-rules", "com.pantrixui:mcp"), tooling.artifacts)
        val (theme, tokens) = foundation.entries
        assertEquals("Theme", theme.label)
        assertTrue(theme.blocks.any { it is Block.ItemList }, "the list under a line stays with it")
        assertEquals("Tokens", tokens.label)
        assertTrue(tokens.breaking)
        assertNull(tooling.entries.single().label)
        assertEquals("0.0.9", last.version)
        assertEquals("2026-10-01", last.date)
        assertEquals(ChangeKind.Fixed, last.sections.single().kind)
    }

    @Test
    fun the_turkish_changelog_takes_its_modules_keys_from_the_english_one() {
        val changelog = ChangelogReader(Language.Turkish, ChangelogReader(Language.English).read(english)).read(turkish)
        val next = changelog.releases.first()
        assertNull(next.version)
        val (foundation, tooling) = next.sections.single().groups
        assertEquals("Eklenenler", next.sections.single().title)
        assertEquals("Araçlar", tooling.module)
        assertEquals("tooling", tooling.key)
        assertEquals("foundation", foundation.key)
        assertTrue(foundation.entries.last().breaking)
        assertEquals("Tokenlar", foundation.entries.last().label)
        assertEquals("components", changelog.releases.last().sections.single().groups.single().key)
    }
}
