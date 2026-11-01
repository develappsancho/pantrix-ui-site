package com.pantrixui.site.feature.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pantrixui.components.elements.button.ButtonVariant
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.components.elements.segmentbutton.PantrixSegmentButton
import com.pantrixui.components.elements.segmentbutton.SegmentOption
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.platform.PlatformType
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.foundation.token.color.PantrixColors
import com.pantrixui.site.core.designsystem.PantrixPreset
import com.pantrixui.site.core.designsystem.SiteLayout
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.designsystem.content.CodeView
import com.pantrixui.site.core.designsystem.content.PageAnchors
import com.pantrixui.site.core.designsystem.content.RichText
import com.pantrixui.site.core.designsystem.content.RoundTag
import com.pantrixui.site.core.designsystem.content.siteName
import com.pantrixui.site.core.designsystem.sized
import com.pantrixui.site.core.i18n.LocalLanguage
import com.pantrixui.site.core.model.Block
import com.pantrixui.site.core.model.ChangeKind
import com.pantrixui.site.core.model.ComponentPage
import com.pantrixui.site.core.model.ComponentType
import com.pantrixui.site.core.model.Language
import com.pantrixui.site.core.model.LibraryFacts
import com.pantrixui.site.core.model.Span
import com.pantrixui.site.core.model.Token
import com.pantrixui.site.core.model.TokenKind
import com.pantrixui.site.core.model.Words
import com.pantrixui.site.feature.components.live.liveExampleOf

// The examples.

/**
 * A component's examples, in the demo's order: what each shows and says, drawn live where the site has it running,
 * and its code.
 */
@Composable
internal fun ExamplesSection(
    page: ComponentPage,
    onLink: (String) -> Unit
) {
    val language = LocalLanguage.current
    Column(verticalArrangement = Arrangement.spacedBy(36.dp)) {
        page.examples.forEach { example ->
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PantrixText(
                    text = example.title,
                    style = SiteTheme.type.guideSubheading,
                    color = PantrixTheme.colors.textPrimary,
                    modifier = Modifier.semantics { heading() }
                )
                example.description.forEach { paragraph -> RichText(paragraph, onLink, codeSize = 13.sp) }
                val live = remember(language, page.card.key, example.name) {
                    liveExampleOf(language, page.card.key, example.name)?.let { LiveExample(it, example.wide) }
                }
                if (live != null) ExampleFrame(live, Modifier.padding(top = 4.dp))
                CodeView(example.code.copy(file = KOTLIN), Modifier.padding(top = if (live == null) 4.dp else 0.dp))
            }
        }
    }
}

// The API.

/**
 * A component's API: the parameters of the form chosen, the first few or all of them, and the library's types they
 * take. A component with several forms has a switch between them by what each starts with.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ApiSection(
    page: ComponentPage,
    state: ComponentUiState,
    onAction: (ComponentAction) -> Unit,
    anchors: PageAnchors,
    onLink: (String) -> Unit
) {
    val language = LocalLanguage.current
    val form = page.forms.getOrElse(state.form) { page.forms.first() }
    SectionHeading(Section.Api, anchors) {
        if (page.forms.size > 1) {
            PantrixSegmentButton(
                options = page.forms.mapIndexed { index, each ->
                    SegmentOption(index, each.label, tagKey = each.label)
                },
                selected = page.forms.indexOf(form),
                onSelect = { onAction(ComponentAction.PickForm(it)) },
                fullWidth = false,
                tagKey = "forms"
            )
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // A form's documentation that says what the page's lead says is not said twice.
        if (form.description.isNotEmpty() && form.description != page.lead) {
            RichText(form.description, onLink, codeSize = 13.sp)
        }
        val shown = if (state.allParameters) form.parameters else form.parameters.take(FIRST_PARAMETERS)
        GridTable(
            header = ApiSay.Header.map { it.of(language) },
            weights = API_WEIGHTS,
            rows = shown.map { parameter ->
                listOf(
                    { Mono(parameter.name, PantrixTheme.colors.textPrimary, bold = true) },
                    { Mono(parameter.type, PantrixTheme.colors.primary.onContainer) },
                    {
                        if (parameter.default == null) {
                            PantrixText(
                                text = ApiSay.Required.of(language),
                                style = SiteTheme.type.bodySmall.sized(12.5.sp),
                                color = SiteTheme.colors.muted
                            )
                        } else {
                            Mono(parameter.default.orEmpty(), PantrixTheme.colors.textSecondary)
                        }
                    },
                    { RichText(parameter.description, onLink, style = SiteTheme.type.bodySmall, codeSize = 12.sp) }
                )
            }
        )
        if (form.parameters.size > FIRST_PARAMETERS) {
            PantrixButton(
                text = if (state.allParameters) {
                    ApiSay.FirstOnly.of(language)
                } else {
                    ApiSay.showAll(form.parameters.size).of(language)
                },
                onClick = { onAction(ComponentAction.ToggleParameters) },
                variant = ButtonVariant.Text,
                tagKey = "allParameters"
            )
        }
        if (page.types.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                itemVerticalAlignment = Alignment.CenterVertically
            ) {
                PantrixText(
                    text = ApiSay.TypesItTakes.of(language),
                    style = SiteTheme.type.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = PantrixTheme.colors.textSecondary
                )
                page.types.forEach { type ->
                    RoundTag(
                        text = type,
                        container = PantrixTheme.colors.primary.container,
                        content = PantrixTheme.colors.primary.onContainer,
                        textStyle = SiteTheme.type.inlineCode.sized(12.sp)
                    )
                }
            }
        }
    }
}

/**
 * A table of [rows] under [header], its columns as wide as [weights] share the room: lines between the rows, and the
 * header on a softer ground. A phone has no room for the columns side by side, and shows each row as a card of its
 * cells, each under its column's name.
 */
@Composable
internal fun GridTable(
    header: List<String>,
    weights: List<Float>,
    rows: List<List<@Composable () -> Unit>>
) {
    val shape = RoundedCornerShape(12.dp)
    val phone = SiteTheme.layout == SiteLayout.Phone
    val colors = PantrixTheme.colors
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, colors.border, shape)
            .background(colors.surface)
    ) {
        if (!phone) {
            Row(
                Modifier.fillMaxWidth().background(
                    SiteTheme.colors.surfaceAlt
                ).padding(horizontal = 12.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                header.forEachIndexed { index, title ->
                    PantrixText(
                        text = title,
                        style = SiteTheme.type.bodySmall.sized(12.5.sp).copy(fontWeight = FontWeight.Bold),
                        color = SiteTheme.colors.muted,
                        modifier = Modifier.weight(weights[index])
                    )
                }
            }
        }
        rows.forEachIndexed { at, cells ->
            if (at > 0 || !phone) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.divider))
            if (phone) {
                Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    cells.forEachIndexed { index, cell ->
                        if (index > 0) {
                            PantrixText(
                                text = header[index],
                                style = SiteTheme.type.bodySmall.sized(11.5.sp).copy(fontWeight = FontWeight.Bold),
                                color = SiteTheme.colors.muted
                            )
                        }
                        cell()
                    }
                }
            } else {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    cells.forEachIndexed { index, cell -> Box(Modifier.weight(weights[index])) { cell() } }
                }
            }
        }
    }
}

/** A cell's code: a name, a type, a value. */
@Composable
internal fun Mono(
    text: String,
    color: Color,
    bold: Boolean = false
) {
    PantrixText(
        text = text,
        style = SiteTheme.type.inlineCode.sized(
            12.sp
        ).copy(fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium),
        color = color
    )
}

// Styles and colours.

/**
 * A component's style, each field on each platform as its preset has it, and its colours, each field with the theme's
 * role it is and what that role is in the site's light and dark themes.
 */
@Composable
internal fun StylesSection(
    page: ComponentPage,
    onLink: (String) -> Unit
) {
    val language = LocalLanguage.current
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        page.style?.let { style ->
            if (style.description.isNotEmpty()) RichText(style.description, onLink, codeSize = 13.sp)
            val values = remember(style.name) { presetValues(style.name) }
            if (values != null) StyleTable(style, values)
        }
        page.colors?.let { colors ->
            if (colors.description.isNotEmpty()) RichText(colors.description, onLink, codeSize = 13.sp)
            ColorsTable(colors, language)
        }
    }
}

/** [style]'s fields, a row each, and their values on each platform, a column each. */
@Composable
private fun StyleTable(
    style: ComponentType,
    values: Map<PlatformType, Map<String, String>>
) {
    // The fields the presets give, in the order the style declares them, a style inside it by its own fields.
    val names = values.values.flatMap { it.keys }.distinct()
    val ordered = style.fields.flatMap { field -> names.filter { it == field.name || it.startsWith("${field.name}.") } }
        .ifEmpty { names }
    GridTable(
        header = listOf(style.name) + PlatformType.entries.map { it.siteName },
        weights = STYLE_WEIGHTS,
        rows = ordered.map { name ->
            listOf<@Composable () -> Unit>({ Mono(name, PantrixTheme.colors.textPrimary, bold = true) }) +
                PlatformType.entries.map { platform ->
                    { Mono(values[platform]?.get(name) ?: StyleValues.NONE, PantrixTheme.colors.textSecondary) }
                }
        }
    )
}

/**
 * [colors]' fields, each with the theme's role it is by default and that role's colour in the site's light and dark
 * themes, side by side with a swatch of each.
 */
@Composable
private fun ColorsTable(
    colors: ComponentType,
    language: Language
) {
    val light = remember { PantrixPreset.colors(dark = false) }
    val dark = remember { PantrixPreset.colors(dark = true) }
    val shape = RoundedCornerShape(12.dp)
    val phone = SiteTheme.layout == SiteLayout.Phone
    Column(
        Modifier
            .fillMaxWidth()
            .border(1.dp, PantrixTheme.colors.border, shape)
            .background(PantrixTheme.colors.surface, shape)
    ) {
        ColorRow(
            first = colors.name,
            role = ApiSay.ThemeRole.of(language),
            light = null to ApiSay.Light.of(language),
            dark = null to ApiSay.Dark.of(language),
            header = true,
            phone = phone
        )
        colors.fields.forEach { field ->
            val role = field.role
            ColorRow(
                first = field.name,
                role = role ?: StyleValues.NONE,
                light = role?.let { roleColor(light, it) }.let { it to (it?.let(::hex) ?: StyleValues.NONE) },
                dark = role?.let { roleColor(dark, it) }.let { it to (it?.let(::hex) ?: StyleValues.NONE) },
                header = false,
                phone = phone
            )
        }
    }
}

@Composable
private fun ColorRow(
    first: String,
    role: String,
    light: Pair<Color?, String>,
    dark: Pair<Color?, String>,
    header: Boolean,
    phone: Boolean
) {
    val text = if (header) {
        SiteTheme.type.bodySmall.sized(12.5.sp).copy(fontWeight = FontWeight.Bold)
    } else {
        SiteTheme.type.inlineCode.sized(12.sp)
    }
    val muted = if (header) SiteTheme.colors.muted else PantrixTheme.colors.textPrimary
    Row(
        Modifier
            .fillMaxWidth()
            .background(if (header) SiteTheme.colors.surfaceAlt else Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PantrixText(text = first, style = text, color = muted, modifier = Modifier.weight(FIELD_WEIGHT))
        PantrixText(
            text = role,
            style = text,
            color = if (header) muted else PantrixTheme.colors.primary.onContainer,
            modifier = Modifier.weight(1f)
        )
        if (!phone || header) {
            listOf(light, dark).forEach { (color, said) ->
                Row(
                    Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    color?.let { Swatch(it) }
                    PantrixText(text = said, style = text, color = muted)
                }
            }
        } else {
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                light.first?.let { Swatch(it) }
                dark.first?.let { Swatch(it) }
            }
        }
    }
}

@Composable
private fun Swatch(color: Color) {
    val shape = RoundedCornerShape(4.dp)
    Box(Modifier.size(14.dp).background(color, shape).border(1.dp, PantrixTheme.colors.border, shape))
}

/** What the role named [role] is in [colors]: `surface`, `primary`, a role's own part, `primary.container`. */
private fun roleColor(
    colors: PantrixColors,
    role: String
): Color? {
    val roles = mapOf(
        "primary" to colors.primary,
        "success" to colors.success,
        "info" to colors.info,
        "warning" to colors.warning,
        "danger" to colors.danger,
        "neutral" to colors.neutral
    )
    roles[role.substringBefore('.')]?.let { color ->
        return when (role.substringAfter('.', "color")) {
            "onColor" -> color.onColor
            "container" -> color.container
            "onContainer" -> color.onContainer
            else -> color.color
        }
    }
    return when (role) {
        "background" -> colors.background
        "surface" -> colors.surface
        "onSurface" -> colors.onSurface
        "disabledContainer" -> colors.disabledContainer
        "disabledContent" -> colors.disabledContent
        "textPrimary" -> colors.textPrimary
        "textSecondary" -> colors.textSecondary
        "textDisabled" -> colors.textDisabled
        "textInverse" -> colors.textInverse
        "border" -> colors.border
        "borderHover" -> colors.borderHover
        "borderStrong" -> colors.borderStrong
        "divider" -> colors.divider
        "focusRing" -> colors.focusRing
        "scrim" -> colors.scrim
        else -> null
    }
}

/** [color] as its hex code, `#2264FF`, its transparency first where it has some. */
@Suppress("MagicNumber") // A colour's code is its bytes: their widths and its base are what it is written in.
private fun hex(color: Color): String {
    val argb = color.toArgb().toLong() and 0xFFFFFFFFL
    val alpha = (argb shr 24).toInt()
    val rgb = (argb and 0xFFFFFFL).toString(16).uppercase().padStart(6, '0')
    return if (alpha == 0xFF) "#$rgb" else "#" + alpha.toString(16).uppercase().padStart(2, '0') + rgb
}

// Testing.

/**
 * How a test finds the component: its tags, from its `tagKey` and never from what it shows, and a test and a Maestro
 * step that find it by them.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TestingSection(
    page: ComponentPage,
    onLink: (String) -> Unit
) {
    val language = LocalLanguage.current
    val key = page.tagKey ?: DEFAULT_KEY
    var tab by rememberSaveable(page.card.key) { mutableIntStateOf(0) }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        RichText(TestSay.lead(language), onLink, codeSize = 13.sp)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            page.tags.forEach { tag ->
                Row(
                    Modifier
                        .border(1.dp, PantrixTheme.colors.border, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tag.label?.let {
                        PantrixText(
                            text = it,
                            style = SiteTheme.type.bodySmall.sized(12.5.sp),
                            color = SiteTheme.colors.muted
                        )
                    }
                    PantrixText(
                        text = "${tag.prefix}_$key",
                        style = SiteTheme.type.inlineCode.sized(12.sp).copy(fontWeight = FontWeight.Bold),
                        color = PantrixTheme.colors.textPrimary
                    )
                }
            }
        }
        val first = page.tags.first().prefix
        val code = if (tab ==
            0
        ) {
            composeTest(page.card.function.removePrefix(PANTRIX), first, key)
        } else {
            maestro(first, key)
        }
        CodeView(code, tabs = TestSay.Tabs, selectedTab = tab, onSelectTab = { tab = it })
    }
}

private fun composeTest(
    name: String,
    component: String,
    key: String
): Block.Code = kotlin(
    "@Test",
    "fun finds$name() = runComposeUiTest {",
    "    setContent { PantrixTestTheme { Screen() } }",
    "    onPantrixNode(\"$component\", \"$key\").assertExists()",
    "}"
)

private fun maestro(
    component: String,
    key: String
): Block.Code = Block.Code(
    language = "yaml",
    lines = listOf(
        listOf(Token("- "), Token("tapOn", TokenKind.Keyword), Token(":")),
        listOf(Token("    id: "), Token("\"${component}_$key\"", TokenKind.StringLiteral))
    )
)

/** [lines] of Kotlin, told apart as the site's own code is not: plain, in the block's colours. */
private fun kotlin(vararg lines: String): Block.Code =
    Block.Code(language = "kotlin", lines = lines.map { listOf(Token(it)) })

// The changelog.

/** The changelog's lines about the component, each with its version and its kind. */
@Composable
internal fun ChangesSection(
    page: ComponentPage,
    onLink: (String) -> Unit
) {
    val language = LocalLanguage.current
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        page.changes.forEach { change ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RoundTag(
                    text = change.version ?: LibraryFacts.VERSION,
                    container = SiteTheme.colors.hover,
                    content = PantrixTheme.colors.textSecondary,
                    textStyle = SiteTheme.type.inlineCode.sized(12.sp)
                )
                val entry = change.entry
                val lead = listOfNotNull(
                    entry.label?.let { Span.Text("$it: ", strong = true) }
                )
                val first = entry.blocks.filterIsInstance<Block.Paragraph>().firstOrNull()?.text.orEmpty()
                Column(Modifier.weight(1f).widthIn(max = 640.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    PantrixText(
                        text = change.kind.title.of(language),
                        style = SiteTheme.type.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = PantrixTheme.colors.textPrimary
                    )
                    RichText(lead + first, onLink, codeSize = 13.sp)
                }
            }
        }
    }
}

/** The words of the API, the styles and the tests, in each language. */
private object ApiSay {
    val Header = listOf(
        Words("Parameter", "Parametre"),
        Words("Type", "Tip"),
        Words("Default", "Varsayılan"),
        Words("What it is for", "Ne için")
    )
    val Required = Words("required", "zorunlu")
    val FirstOnly = Words("Show the first few", "İlklerini göster")
    val TypesItTakes = Words("Types it takes:", "Aldığı tipler:")
    val ThemeRole = Words("Theme's role", "Temadaki rolü")
    val Light = Words("Pantrix, light", "Pantrix, açık")
    val Dark = Words("Pantrix, dark", "Pantrix, koyu")

    fun showAll(count: Int): Words = Words("Show all $count parameters", "$count parametrenin hepsini göster")
}

/** The words of the tests' part, in each language. */
private object TestSay {
    val Tabs = listOf("Compose test", "Maestro")

    fun lead(language: Language): List<Span> = when (language) {
        Language.English -> listOf(
            Span.Text("Tags come from "),
            Span.Code("tagKey"),
            Span.Text(
                ", in a form from the field's id, and never from visible text: a test finds the component by them."
            )
        )

        Language.Turkish -> listOf(
            Span.Text("Etiketler "),
            Span.Code("tagKey"),
            Span.Text("'den, bir formda alanın kimliğinden gelir, görünen metinden asla: test bileşeni onlarla bulur.")
        )
    }
}

/** What a change of each kind is called, in each language. */
private val ChangeKind.title: Words
    get() = when (this) {
        ChangeKind.Added -> Words("Added", "Eklendi")
        ChangeKind.Changed -> Words("Changed", "Değişti")
        ChangeKind.Deprecated -> Words("Deprecated", "Kullanımdan kalkıyor")
        ChangeKind.Removed -> Words("Removed", "Kaldırıldı")
        ChangeKind.Fixed -> Words("Fixed", "Düzeltildi")
        ChangeKind.Security -> Words("Security", "Güvenlik")
    }

/** How wide a colour's field is beside its role and its two colours. */
private const val FIELD_WEIGHT = 1.3f

/** How the API's and the style's tables share their width among their columns. */
private val API_WEIGHTS = listOf(1.1f, 1.2f, 0.9f, 2.4f)
private val STYLE_WEIGHTS = listOf(1.6f, 1f, 1f, 1f, 1f)

/** How many parameters the API table shows before "Show all". */
private const val FIRST_PARAMETERS = 12

private const val KOTLIN = "Kotlin"
private const val PANTRIX = "Pantrix"

/** The key a test is shown with where the component's examples give it none. */
private const val DEFAULT_KEY = "key"
