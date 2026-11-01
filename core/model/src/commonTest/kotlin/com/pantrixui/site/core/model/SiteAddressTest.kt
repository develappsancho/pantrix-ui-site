package com.pantrixui.site.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SiteAddressTest {
    @Test
    fun the_site_s_addresses_are_written_from_its_origin() {
        val origin = SiteAddress.origin
        assertTrue(origin.startsWith("https://") || origin.startsWith("http://"), origin)
        assertFalse(origin.endsWith("/"), origin)
        assertEquals("$origin/mcp", SiteAddress.mcp)
        assertEquals("$origin/llms.txt", SiteAddress.llms)
        assertEquals(origin.substringAfter("://"), SiteAddress.host)
    }

    @Test
    fun the_address_the_site_is_built_for_is_whole() {
        assertTrue(Regex("https?://[A-Za-z0-9.-]+(:[0-9]+)?").matches(BUILT_SITE_ADDRESS), BUILT_SITE_ADDRESS)
    }
}
