---
title: Theming
description: How a screen reads the theme's colours, type, gaps and corners, how a brand gives its own look in one preset, and how a part of a screen looks different. Nothing in a screen names a colour or a size.
section: Getting started
---

## What the theme holds {#values}

:::table What a composable reads
- `PantrixTheme.colors`: The colour roles: `primary.color`, `surface`, `textSecondary`, `divider`
- `PantrixTheme.typography`: The type scale: `title`, `body`, `caption`
- `PantrixTheme.spacing`: The gaps: `xs` 4, `sm` 8, `md` 12, `lg` 16, `xl` 24, `xxl` 32dp, scaled by the density
- `PantrixTheme.shapes`: The corners: `xs` 2, `sm` 4, `md` 8, `lg` 12, `xl` 16dp, and `none` and `full`
- `PantrixTheme.motion`: The durations, `fast` 100, `normal` 200 and `slow` 350ms, nought where the system reduces motion
- `PantrixTheme.style(ButtonStyle)`: A component's style as it holds at that place
:::

```kotlin Summary.kt
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

## Colours by their job {#colors}

A component asks for a colour by its job, never by its name: the light and the dark theme change the colour, not the code.

:::rows
- **Roles**: `primary` is the brand's: the main action, what is chosen. `success`, `info`, `warning` and `danger` say a status, `neutral` draws no eye. Each has `color`, `container`, and `onColor` and `onContainer` for what is written on them.
- **Grounds and text**: `background` is the app's backdrop, `surface` a card's or a sheet's ground. Text is `textPrimary`, `textSecondary`, `textDisabled` and `textInverse`.
- **Lines**: `border` is a box's soft edge, `borderStrong` the edge of a control that is only its edge, `divider` the line between items.
- **Content colour**: A fill tells what is written on it which colour to take: a text or an icon given no colour takes it.
:::

## Type {#type}

Nine styles: `display`, `headline` and `title` for headings; `callout`, `body`, `label`, `footnote` and `caption` for text; `code` for monospace. Text given no style is `label`. A style is a look, not a role: a heading is one to a screen reader with `role = TextRole.Heading(level)`. The sizes are in sp, so text grows with the system's font size: [Typography](/docs/guides/typography).

:::steps
## Name the brand's typeface {#typeface toc="The typeface"}

```kotlin BrandType.kt
val Brand = PantrixTypeface(
    PantrixFont(Res.font.brand_regular),
    PantrixFont(Res.font.brand_bold, FontWeight.Bold)
)
```

## Give it to the theme {#scale toc="The scale"}

```kotlin App.kt
PantrixTheme(typography = rememberPantrixTypography(Brand)) { App() }
```

`rememberPantrixTypography(Brand, codeTypeface = BrandMono)` sets code in a face of its own.
:::

## A brand's look in one preset {#preset}

A `DesignPreset` holds a brand's colours for light and dark, its type, its shapes and its components' overrides. Its colours are written once, here: a `Color(0xFF…)` belongs in a preset, never in a screen. The [theme builder](/theme) writes one for you.

```kotlin Harbor.kt
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
    override fun shapes(): PantrixShapes = remember { PantrixShapes(md = RoundedCornerShape(4.dp)) }
}

PantrixTheme(preset = Harbor) { App() }
```

## Light and dark {#dark}

The theme follows the system's dark mode unless it is told. An app with a switch of its own gives the reader's choice, and the system's bars, keyboard and controls follow it on Android and iOS: [System bars that follow the theme](/docs/guides/system-bars).

```kotlin App.kt
PantrixTheme(preset = Harbor, colors = rememberPantrixColors(Harbor, darkTheme = settings.dark)) { App() }
```

## A part that looks different {#nested}

A `PantrixTheme` inside another changes only what it is given and takes the rest from the outer one.

:::rows
- **A dark part**: `PantrixTheme(darkTheme = true) { … }` in a light screen. Its ground is the surface or the card inside it: a theme draws none.
- **A compact table**: `PantrixTheme(density = PantrixDensity.Compact) { … }`. The touch target does not shrink.
- **Another language**: `PantrixTheme(locale = PantrixLocale.Arabic) { … }` speaks Arabic and reads from right to left.
- **Another platform**: `PantrixTheme(platform = PantrixPlatform.forType(PlatformType.Ios)) { … }` draws iOS's look anywhere.
:::

## Component styles {#styles}

A component's measures and form are its style, with a preset on each platform. On top of it come the brand's overrides, the theme's styles, a section's and, last, the component's own `style`. A style names only what it changes.

```kotlin StyledApp.kt
@Composable
fun StyledApp(content: @Composable () -> Unit) {
    val styles = remember {
        pantrixStyles(
            ButtonStyle.override(
                all = ButtonStyle(shape = ShapeRole.Small),
                ios = ButtonStyle(shape = ShapeRole.Large)
            )
        )
    }
    PantrixTheme(styles = styles, content = content)
}
```

## Where next {#next}

:::cards
- [Platforms](/docs/platforms): Each platform's entry point and what differs.
- [Forms](/docs/forms): Fields, rules and a form's submission.
- [Typography](/docs/guides/typography): The scale and the size of what is typed.
- [The Material 3 bridge](/docs/guides/material3): Two themes that speak the same colours.
:::
