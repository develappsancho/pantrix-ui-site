# Theming

How a screen reads the theme's values, how a brand gives its own look, and how a part of a screen looks different.
The pages `colors`, `typography`, `spacing`, `shapes`, `elevation`, `motion`, `density`, `component-styles` and
`theme-inside-another` have every example.

Contents: Reading the theme · Colours · Type · A brand's look · Light and dark · Component styles · A part that looks
different

## Reading the theme

A composable reads the nearest theme's values from `PantrixTheme`:

| Value | What it holds |
| :--- | :--- |
| `PantrixTheme.colors` | The colour roles: `primary.color`, `surface`, `textSecondary`, `divider`… |
| `PantrixTheme.typography` | The type scale: `title`, `body`, `caption`… |
| `PantrixTheme.spacing` | The gaps and paddings: `none` 0, `xs` 4, `sm` 8, `md` 12, `lg` 16, `xl` 24, `xxl` 32dp, scaled by the density |
| `PantrixTheme.shapes` | The corners: `xs` 2, `sm` 4, `md` 8, `lg` 12, `xl` 16dp, and `none` and `full` |
| `PantrixTheme.elevation` | The shadows: `none` 0, `sm` 1, `md` 2, `lg` 4, `xl` 8dp |
| `PantrixTheme.motion` | The durations, `fast` 100, `normal` 200 and `slow` 350ms, and the easings |
| `PantrixTheme.density` | `Compact` (0.85), `Comfortable` (1, the default) or `Spacious` (1.15) |
| `PantrixTheme.platform` | The platform's conventions: `type`, `isTouch`, `minTouchTarget` |
| `PantrixTheme.window`, `PantrixTheme.layout` | The window and the page's measures ([layout.md](layout.md)) |
| `PantrixTheme.locale`, `PantrixTheme.strings` | The language and the components' own words ([platform.md](platform.md)) |
| `PantrixTheme.style(ButtonStyle)` | A component's style as it holds at that place |

```kotlin
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pantrixui.components.container.surface.PantrixSurface
import com.pantrixui.components.elements.text.PantrixText
import com.pantrixui.foundation.theme.PantrixTheme

@Composable
fun Summary(title: String, detail: String) {
    PantrixSurface(border = PantrixTheme.colors.border, shape = PantrixTheme.shapes.lg) {
        Column(
            modifier = Modifier.padding(PantrixTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(PantrixTheme.spacing.xs)
        ) {
            PantrixText(text = title, style = PantrixTheme.typography.title)
            PantrixText(text = detail, color = PantrixTheme.colors.textSecondary)
        }
    }
}
```

## Colours

A component asks for a colour by its job, never by its name: the light and the dark theme change the colour, not the
code.

- **Six roles.** `primary` is the brand's: the main action, what is selected. `success`, `info`, `warning` and `danger`
  say a status; `neutral` draws no eye. Each is a `PantrixColor`: `color` at full strength, `container` softened, and
  `onColor` and `onContainer` for what is written on them.
- **Grounds and text.** `background` is the app's backdrop, `surface` the ground of a card, a sheet, a menu. Text is
  `textPrimary`, `textSecondary`, `textDisabled`, and `textInverse` on a fill of the opposite tone.
- **Lines.** `border` is a box's soft edge, `borderStrong` the edge of a control that is only its edge, `divider` the
  line between items.
- **Content colour.** A fill tells what is inside it which colour is written on it: a text or an icon given no colour
  takes it. `PantrixSurface(color = …)` provides the pair of its fill; `ProvidePantrixContentColor` does for a fill of
  the app's own, and `PantrixTheme.colors.contentColorFor(fill)` finds a fill's pair.
- **One colour changed.** `PantrixColors` is a data class: `PantrixTheme(colors = PantrixTheme.colors.copy(primary =
  …)) { … }`. `PantrixColors.Light` and `PantrixColors.Dark` are Pantrix's two sets; each is imported by its name,
  `com.pantrixui.foundation.token.color.Light`.
- **The palette.** `PantrixPalette` has eighteen colours (`Blue`, `Teal`…) in eleven steps, `shade50` to `shade950`. A
  brand builds its roles from it; components read the roles, never the palette.

## Type

- **Nine styles.** `display`, `headline` and `title` for headings; `callout`, `body`, `label`, `footnote` and
  `caption` for text; `code` for monospace.
- **Text given no style is `label`.** `PantrixText` takes its style from `LocalPantrixTextStyle`, which the theme
  starts at `label`; `ProvidePantrixTextStyle` gives a part another one.
- **A style is a look, not a role.** Text in a heading's style is a heading to a screen reader only with
  `role = TextRole.Heading(level)`.
- **A brand's typeface.** `PantrixTypeface(PantrixFont(Res.font.brand_regular), PantrixFont(Res.font.brand_bold,
  FontWeight.Bold))` names it by its files; `PantrixTheme(typography = rememberPantrixTypography(Brand))` sets the
  scale in it, and `rememberPantrixTypography(Brand, codeTypeface = BrandMono)` sets code in a face of its own.
  `buildPantrixTypography(fontFamily, headingFontFamily, monoFamily)` keeps the scale and changes the families. The
  sizes are in sp: text grows with the system's font size.

## A brand's look

A `DesignPreset` gives a brand's colours (light and dark), type, shapes and component overrides in one place, and
`PantrixTheme(preset = Brand) { … }` draws with it:

```kotlin
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pantrixui.foundation.theme.preset.DesignPreset
import com.pantrixui.foundation.token.color.Dark
import com.pantrixui.foundation.token.color.Light
import com.pantrixui.foundation.token.color.PantrixColor
import com.pantrixui.foundation.token.color.PantrixColors
import com.pantrixui.foundation.token.color.PantrixPalette
import com.pantrixui.foundation.token.shape.PantrixShapes
import com.pantrixui.foundation.token.shape.ShapeRole
import com.pantrixui.foundation.token.style.ComponentTokenOverrides
import com.pantrixui.foundation.token.typography.PantrixTypography
import com.pantrixui.foundation.token.typography.rememberPantrixTypography

object Harbor : DesignPreset {
    override val name: String = "Harbor"

    override fun colors(dark: Boolean): PantrixColors {
        val teal = PantrixPalette.Teal
        return if (dark) {
            PantrixColors.Dark.copy(
                primary = PantrixColor(
                    color = teal.shade400,
                    onColor = teal.shade950,
                    container = teal.shade900,
                    onContainer = teal.shade100
                )
            )
        } else {
            PantrixColors.Light.copy(
                primary = PantrixColor(
                    color = teal.shade700,
                    onColor = Color.White,
                    container = teal.shade100,
                    onContainer = teal.shade900
                )
            )
        }
    }

    @Composable
    override fun typography(): PantrixTypography = rememberPantrixTypography()

    @Composable
    override fun shapes(): PantrixShapes = remember {
        PantrixShapes(md = RoundedCornerShape(4.dp), lg = RoundedCornerShape(6.dp), xl = RoundedCornerShape(8.dp))
    }

    override fun componentOverrides(): ComponentTokenOverrides =
        ComponentTokenOverrides(shapes = mapOf("button.shape" to ShapeRole.Small))
}
```

- A preset's colours are written once, here: `Color(0xFF…)` belongs in a preset, never in a screen.
- `rememberPantrixColors(Harbor)` gives the brand's colours outside a theme, as light or dark as the system is: for a
  system bar's colour.

## Light and dark

The theme follows the system's dark mode unless it is told: `PantrixTheme(darkTheme = settings.dark) { … }`, or with
a preset `PantrixTheme(preset = Harbor, colors = rememberPantrixColors(Harbor, darkTheme = settings.dark)) { … }`. A
theme is dark when its background is, and a theme inside it is as dark unless it is told otherwise. Nothing else in the
code changes between the two. The outermost theme makes Android's bars and iOS's status bar, keyboard and controls
follow it, not the system: an app that colours its bars itself turns that off with `followSystemBars = false`.

## Component styles

A component's measures and form are its style: `ButtonStyle`, `TextFieldStyle`, `CardStyle`… Each has a preset on each
platform; on top of it come the brand's overrides, the theme's styles, a section's styles and, last, the component's
own `style` parameter. A style names only the fields it changes.

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import com.pantrixui.components.elements.button.ButtonStyle
import com.pantrixui.components.elements.icon.IconStyle
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.foundation.token.shape.ShapeRole
import com.pantrixui.foundation.token.style.pantrixStyles

@Composable
fun StyledApp(content: @Composable () -> Unit) {
    val styles = remember {
        pantrixStyles(
            ButtonStyle.override(
                all = ButtonStyle(shape = ShapeRole.Small),
                ios = ButtonStyle(shape = ShapeRole.Large)
            ),
            IconStyle.override(all = IconStyle(size = 28.dp))
        )
    }
    PantrixTheme(styles = styles, content = content)
}
```

- `override`'s `android`, `ios`, `desktop` and `web` go on top of `all`.
- `ProvidePantrixStyles(styles) { … }` gives styles to one section alone.
- A shape in a style is a step of the brand's scale, `ShapeRole` (`ExtraSmall`, `Small`, `Medium`, `Large`, `Full`),
  or `ShapeSpec.Fixed(shape)` off the scale.
- An app's own component takes part in the same order with a `PantrixStyle` and its `PantrixStyleKey`: the page
  `component-styles` has one.

## A part that looks different

A `PantrixTheme` inside another changes only what it is given, and takes the rest from the outer one:

- `PantrixTheme(darkTheme = true) { … }` is a dark part of a light screen.
- `PantrixTheme(density = PantrixDensity.Compact) { … }` is a compact table or toolbar; the touch target does not
  shrink.
- `PantrixTheme(locale = PantrixLocale.Arabic) { … }` speaks Arabic and reads from right to left.
- `PantrixTheme(platform = PantrixPlatform.forType(PlatformType.Ios)) { … }` draws another platform's look.
- A theme draws no ground: a dark part's ground is the `PantrixSurface` or the card inside it. A theme is not a layout
  either: its content is laid out by the layout the theme stands in.

In an app a section changes by a nested theme, not by providing one of the theme's locals: the theme fits its values
to each other and scales the spacing by the density.
