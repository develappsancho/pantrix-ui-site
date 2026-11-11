package com.pantrixui.site.feature.ai

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The page's rules are the skill's core rules: as many, in the order and by the names `SKILL.md` gives them. The skill
 * in `.claude/skills` is written by the library's jar with each new version, and a rule it gains shows here first.
 */
class SkillRulesTest {
    @Test
    fun the_page_lists_the_skill_s_core_rules_by_their_names() {
        val skill = File(ROOT, ".claude/skills/pantrix-ui/SKILL.md").readText()
        val core = skill.substringAfter("## Core rules").substringBefore("\n## ")
        val named = RULE.findAll(core).map { it.groupValues[1].replace("`", "").trimEnd('.') }.toList()
        assertEquals(named, ServerSay.Rules.map { (title, _) -> title.desktop.english })
    }

    private companion object {
        /** The site's root, from the module's folder the test runs in. */
        val ROOT = File("../..")

        /** A core rule's line: its number and its name in bold. */
        val RULE = Regex("""^\d+\. \*\*([^*]+)\*\*""", RegexOption.MULTILINE)
    }
}
