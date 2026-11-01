package com.pantrixui.site.core.model

/**
 * The sections of the foundations page, in its order: each a kind of token the theme holds. The sidebar beside the
 * page and the page itself name them alike.
 *
 * @property anchor the section's address on the page: `/foundations#colour`.
 * @property title what it is called.
 */
enum class FoundationSection(
    val anchor: String,
    val title: Words
) {
    Colour("colour", Words("Colour", "Renk")),
    Type("type", Words("Typography", "Yazı")),
    Spacing("spacing", Words("Spacing", "Boşluk")),
    Shape("shape", Words("Shape", "Şekil")),
    Elevation("elevation", Words("Elevation", "Yükselti")),
    Motion("motion", Words("Motion", "Hareket")),
    Density("density", Words("Density", "Yoğunluk"))
}

/**
 * The pages about the theme the foundations page leads to from its sidebar, each a part of a page of the
 * documentation, and the theme builder.
 *
 * @property key its automation key.
 * @property title what the link says.
 * @property route the page it opens.
 * @property section the part of that page it opens at, or none.
 */
enum class ThemingLink(
    val key: String,
    val title: Words,
    val route: Route,
    val section: String?
) {
    DesignPresets("presets", Words("Design presets", "Tasarım hazır ayarları"), Route.Doc("theming"), "preset"),
    PlatformPresets("platforms", Words("Platform presets", "Platform hazır ayarları"), Route.Doc("platforms"), null),
    ComponentStyles("styles", Words("Component styles", "Bileşen stilleri"), Route.Doc("theming"), "styles"),
    NestedThemes("nested", Words("Nested themes", "İç içe temalar"), Route.Doc("theming"), "nested"),
    ThemeBuilder("builder", Words("Theme builder", "Tema oluşturucu"), Route.Theme, null)
}
