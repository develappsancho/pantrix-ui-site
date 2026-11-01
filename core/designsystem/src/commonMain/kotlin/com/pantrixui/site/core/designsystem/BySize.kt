package com.pantrixui.site.core.designsystem

/**
 * What a page of the site says or shows in each of its layouts, where its design says it shorter, or shows less, in
 * a smaller window.
 *
 * @property desktop on a desktop.
 * @property tablet on a tablet: the desktop's unless given.
 * @property phone on a phone: the tablet's unless given.
 */
class BySize<T>(
    val desktop: T,
    val tablet: T = desktop,
    val phone: T = tablet
) {
    /** What it is in [layout]. */
    fun of(layout: SiteLayout): T = when (layout) {
        SiteLayout.Desktop -> desktop
        SiteLayout.Tablet -> tablet
        SiteLayout.Phone -> phone
    }
}
