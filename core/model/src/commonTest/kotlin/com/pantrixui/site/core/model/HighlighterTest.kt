package com.pantrixui.site.core.model

import kotlin.test.Test
import kotlin.test.assertEquals

class HighlighterTest {
    private fun kinds(code: String) = Highlighter.lines(code, "kotlin").flatten()
        .filter { it.kind != TokenKind.Plain }
        .map { it.text to it.kind }

    @Test
    fun keywords_strings_numbers_comments_and_annotations_take_their_kinds() {
        assertEquals(
            listOf(
                "@Composable" to TokenKind.Annotation,
                "fun" to TokenKind.Keyword,
                "App" to TokenKind.Function,
                "val" to TokenKind.Keyword,
                "\"x\"" to TokenKind.StringLiteral,
                "120" to TokenKind.Number,
                "/* … */" to TokenKind.Comment
            ),
            kinds("@Composable\nfun App() {\n    val a = \"x\" + 120 /* … */\n}")
        )
    }

    @Test
    fun a_capitalised_call_is_a_name_to_look_for_and_a_small_one_is_plain() {
        assertEquals(
            listOf("PantrixTheme" to TokenKind.Function, "PantrixButton" to TokenKind.Function),
            kinds("PantrixTheme {\n    PantrixButton(text = stringResource(Res.string.save))\n}")
        )
    }

    @Test
    fun a_type_after_a_colon_is_plain_and_class_after_two_colons_is_a_keyword() {
        assertEquals(
            listOf("object" to TokenKind.Keyword, "Brand" to TokenKind.Function, "class" to TokenKind.Keyword),
            kinds("object Brand : DesignPreset {\n    credentials(PasswordCredentials::class)\n}")
        )
    }

    @Test
    fun a_comment_over_two_lines_is_cut_into_its_lines() {
        assertEquals(
            listOf(listOf(Token("/* one", TokenKind.Comment)), listOf(Token("two */", TokenKind.Comment))),
            Highlighter.lines("/* one\ntwo */", "kotlin")
        )
    }

    @Test
    fun a_maestro_command_is_a_keyword_its_strings_strings_and_its_comment_a_comment() {
        assertEquals(
            listOf(
                listOf(
                    Token("- "),
                    Token("tapOn", TokenKind.Keyword),
                    Token(": "),
                    Token("\"A note # for\"", TokenKind.StringLiteral),
                    Token("  "),
                    Token("# empty room", TokenKind.Comment)
                ),
                listOf(Token("    id: "), Token("\"Send\"", TokenKind.StringLiteral))
            ),
            Highlighter.lines("- tapOn: \"A note # for\"  # empty room\n    id: \"Send\"", "yaml")
        )
    }

    @Test
    fun a_language_it_does_not_read_is_plain() {
        assertEquals(listOf(listOf(Token("./gradlew run"))), Highlighter.lines("./gradlew run", "bash"))
    }
}
