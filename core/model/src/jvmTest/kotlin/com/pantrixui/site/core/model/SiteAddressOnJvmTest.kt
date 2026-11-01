package com.pantrixui.site.core.model

import kotlin.test.Test
import kotlin.test.assertEquals

class SiteAddressOnJvmTest {
    @Test
    fun an_app_gives_the_address_the_site_is_built_for() {
        assertEquals(BUILT_SITE_ADDRESS, SiteAddress.origin)
        assertEquals("$BUILT_SITE_ADDRESS/mcp", SiteAddress.mcp)
    }
}
