package com.pantrixui.site.core.i18n

import com.pantrixui.site.core.model.GuidePlatform
import com.pantrixui.site.core.model.Language
import kotlin.test.Test
import kotlin.test.assertEquals

class SiteStringsTest {
    @Test
    fun each_language_has_its_own_words() {
        assertEquals(SiteStrings.English, SiteStrings.of(Language.English))
        assertEquals(SiteStrings.Turkish, SiteStrings.of(Language.Turkish))
        assertEquals("Docs", SiteStrings.English.docs)
        assertEquals("Belgeler", SiteStrings.Turkish.docs)
    }

    @Test
    fun counts_and_the_language_button_are_said_in_the_language() {
        assertEquals("All 22 guides", SiteStrings.English.allGuides(22))
        assertEquals("22 rehberin hepsi", SiteStrings.Turkish.allGuides(22))
        assertEquals("98 bileşen", SiteStrings.Turkish.componentCount(98))
        assertEquals("Language: EN, English", SiteStrings.English.languageButton("EN", "English"))
        assertEquals("Dil: TR, Türkçe", SiteStrings.Turkish.languageButton("TR", Language.Turkish.ownName))
    }

    @Test
    fun a_guide_s_platforms_are_listed_as_a_sentence_and_the_turkish_ending_goes_on_the_last() {
        val three = listOf(GuidePlatform.Ios, GuidePlatform.Android, GuidePlatform.Web)
        assertEquals("Seen on iOS, Android and the web", SiteStrings.English.seenOn(three))
        assertEquals("iOS, Android ve web'de görülür", SiteStrings.Turkish.seenOn(three))
        assertEquals("Seen on the web", SiteStrings.English.seenOn(listOf(GuidePlatform.Web)))
        assertEquals("Web'de görülür", SiteStrings.Turkish.seenOn(listOf(GuidePlatform.Web)))
        val two = listOf(GuidePlatform.Desktop, GuidePlatform.Ios)
        assertEquals("Masaüstü ve iOS'ta görülür", SiteStrings.Turkish.seenOn(two))
        assertEquals("6 min read", SiteStrings.English.minutesToRead(6))
        assertEquals("6 dk okuma", SiteStrings.Turkish.minutesToRead(6))
        assertEquals("Compose Multiplatform 1.12.2 ile denetlendi", SiteStrings.Turkish.checkedWith("1.12.2"))
    }
}
