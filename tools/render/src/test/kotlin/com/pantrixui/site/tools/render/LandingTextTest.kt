package com.pantrixui.site.tools.render

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runComposeUiTest
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.site.core.designsystem.content.CodeExample
import com.pantrixui.site.core.designsystem.content.live
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A page's words read from its semantics: its headings, text and code, and none of its controls, its live parts or
 * what it hides from a screen reader.
 */
@OptIn(ExperimentalTestApi::class)
class LandingTextTest {
    @Test
    fun headings_text_and_code_are_read_and_controls_live_parts_and_hidden_pictures_are_left_out() = runComposeUiTest {
        setContent {
            Column {
                PantrixText("Kicker")
                PantrixText("One codebase <four>", modifier = Modifier.semantics { heading() })
                PantrixText("A lead & more")
                PantrixText("Get started", modifier = Modifier.semantics { role = Role.Button })
                Column(Modifier.live()) { PantrixText("Write a review") }
                Column(Modifier.clearAndSetSemantics {}) { PantrixText("Save") }
                Column(Modifier.semantics { this[CodeExample] = Unit }) { PantrixText("val a = 1\n    val b = \"x\"") }
                PantrixText("Second", modifier = Modifier.semantics { heading() })
            }
        }
        val words = LandingText.of(onRoot(useUnmergedTree = true).fetchSemanticsNode())
        assertEquals(
            listOf(
                "<p>Kicker</p>",
                "<h1>One codebase &lt;four&gt;</h1>",
                "<p>A lead &amp; more</p>",
                "<pre><code>val a = 1\n    val b = &quot;x&quot;</code></pre>",
                "<h2>Second</h2>"
            ).joinToString("\n"),
            words
        )
    }
}
