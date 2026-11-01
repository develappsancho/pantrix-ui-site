---
title: The Material 3 bridge
description: How an app that uses Material 3 can move to Pantrix, or use the two together: the two themes speaking the same colours, type and shapes. The problem is not Pantrix's own.
---

The library itself stays free of Material: the bridge is the one module that uses Material, and no library module depends on it. Everything said here about Material 3 was read in the sources of Material 3 1.9.0 and 1.11.0-alpha07, or seen in a test that runs in this repository (section 11).

## 1. The problem {#problem}

*   **Moving screen by screen:** when a screen written with Pantrix is added to a Material app, the two themes live side by side. Pantrix's blue stands beside Material's purple; edges, corners and type sizes change from screen to screen.
*   **A component Pantrix does not have:** when a Material component (a slider, a date range picker) is put into a Pantrix app, the component comes with Material's default colours and looks foreign in the app.
*   **The text of a mixed screen:** the two systems read the colour of text and icons, and the text style, from separate places. Material reads `LocalContentColor` and `LocalTextStyle`, Pantrix `LocalPantrixContentColor` and `LocalPantrixTextStyle`. A Material `Text` inside a dark Pantrix theme is drawn in black, Material's default, unless a Material surface wraps it.

:::sketch material3
:::

## 2. Setting up {#setting-up}

```kotlin
commonMain.dependencies {
    implementation("com.pantrixui:material3:0.1.1")
}
```

The module is compiled against Material 3's latest stable version, `1.9.0`. This is a lower bound: if the app has a newer Material 3, Gradle picks it and the bridge works with it. After 1.9.0, Material 3's versions for CMP came out only as alphas and betas; the bridge depends on the stable one so as not to force an alpha on its user. The theme API is the same in 1.9.0 and 1.11.0-alpha07: 48 colours, 15 text styles, 5 shapes.

## 3. Two directions {#two-directions}

### Pantrix in a Material app

```kotlin
MaterialTheme(colorScheme = appScheme) {
    Scaffold { padding ->
        PantrixThemeFromMaterial {
            // Pantrix's components in the colours, type and shapes of the app's Material theme
            PantrixButton(text = "Save", onClick = {}, modifier = Modifier.padding(padding))
        }
    }
}
```

`PantrixThemeFromMaterial` reads the `MaterialTheme` around it and sets up a `PantrixTheme`. The colours are the scheme's `toPantrixColors()`, and the theme is as dark as the scheme. The type is built from Material's scale, on top of Pantrix's own scale; the shapes are taken by their radii. Everything else (spacing, motion, language, analytics) is Pantrix's own, or what a `PantrixTheme` inside gives.

As it is the screen's outermost Pantrix theme, it makes Android's bars' icons and iOS's status bar, keyboard and controls follow the scheme's dark (the [system bars](/docs/guides/system-bars) guide). A Material app that sets its bars itself, or whose bars are coloured, writes `PantrixThemeFromMaterial(followSystemBars = false) { … }`.

### Material in a Pantrix app

```kotlin
PantrixTheme {
    MaterialThemeFromPantrix {
        // A Material component Pantrix does not have, in Pantrix's theme
        Slider(value = volume, onValueChange = { volume = it })
    }
}
```

`MaterialThemeFromPantrix` reads the Pantrix theme around it and sets up a `MaterialTheme`: the scheme `toColorScheme()`, the type scale `toMaterialTypography()`, the shapes `toMaterialShapes()`.

### Plain conversions

Under the two wrappers there are six functions; an app that sets up its theme itself uses them:

:::table
| Material → Pantrix | Pantrix → Material |
| :--- | :--- |
| `ColorScheme.toPantrixColors(base)` | `PantrixColors.toColorScheme()` |
| `Typography.toPantrixTypography(base)` | `PantrixTypography.toMaterialTypography()` |
| `Shapes.toPantrixShapes(base)` | `PantrixShapes.toMaterialShapes(fallback)` |
:::

### The sample app

`samples/pantrix-material3` shows the two directions on one screen. The same form is drawn once with Material's components and once with Pantrix's. The app starts as a Material app (with its brand's scheme, `PantrixThemeFromMaterial`). With a button it turns into an app written with Pantrix's own theme (`MaterialThemeFromPantrix`). The screen is carried to the other theme with its values: only the look changes. Its README says how to run it.

## 4. Mapping tables {#mapping-tables}

### Colours: Material → Pantrix

:::table
| Pantrix | Material |
| :--- | :--- |
| The `primary` role (`color`, `onColor`, `container`, `onContainer`) | `primary`, `onPrimary`, `primaryContainer`, `onPrimaryContainer` |
| The `danger` role | `error`, `onError`, `errorContainer`, `onErrorContainer` |
| The `neutral` role | `secondary`, `onSecondary`, `secondaryContainer`, `onSecondaryContainer` |
| `background` | `background` |
| `surface` | `surfaceContainerLow` |
| `onSurface` | `onSurface` |
| `textPrimary` | `onBackground` |
| `textSecondary` | `onSurfaceVariant` |
| `textInverse` | `inverseOnSurface` |
| `border`, `borderStrong` | `outline` |
| `divider` | `outlineVariant` |
| `scrim` | The colour of `scrim`, with the opacity of `base`'s |
| `success`, `info`, `warning`, `borderHover`, `focusRing`, `disabledContainer`, `disabledContent`, `textDisabled`, `chart` | No counterpart in Material: from `base` (section 5) |
:::

Two of the mappings were chosen on purpose:

*   **The surface:** in Material's baseline scheme `surface` and `background` are the same colour; the `surfaceContainer…` steps set the card apart from the page. Pantrix's `surface` is therefore `surfaceContainerLow`: the card stands apart from the ground, as it does in Material.
*   **The box's edge:** Material draws a field's edge and a control's mark in the same colour, in `outline`. Pantrix's two separate colours (`border` the soft edge, `borderStrong` the mark) both become `outline` here: Pantrix's fields are drawn like the app's Material fields.

### Colours: Pantrix → Material

:::table
| Material | Pantrix |
| :--- | :--- |
| `primary`, `onPrimary`, `primaryContainer`, `onPrimaryContainer` | The `primary` role |
| `secondary` and its partners | The `neutral` role |
| `tertiary` and its partners | The `info` role |
| `error` and its partners | The `danger` role |
| `background`, `surface` | `background` (the two the same, as in Material's own schemes) |
| `onBackground` | `textPrimary` |
| `onSurface` | `onSurface` |
| `onSurfaceVariant` | `textSecondary` |
| `surfaceContainerLow` | `surface` |
| `surfaceContainer`, `surfaceContainerHigh`, `surfaceContainerHighest` | 4%, 8% and 12% of `textPrimary` over `surface` |
| `surfaceContainerLowest` | The ground farther from the text: `surface` in the light theme, `background` in the dark one |
| `surfaceDim`, `surfaceBright` | The two ends: the one towards the text at 16%, the other the farthest ground |
| `surfaceVariant` | `surfaceContainerHighest` |
| `surfaceTint` | `primary.color` |
| `inverseSurface`, `inverseOnSurface` | `neutral.color`, `textInverse` |
| `inversePrimary` | `primary.container` |
| `outline`, `outlineVariant` | `borderStrong`, `border` |
| `scrim` | `scrim`, fully opaque (a Material component brings it down to 32% itself when it uses it) |
| `primaryFixed`, `primaryFixedDim`, `onPrimaryFixed`, `onPrimaryFixedVariant`, and their partners for `secondary` and `tertiary` | The role's `container` and `onContainer` |
:::

`outlineVariant` comes from the box's edge, not from Pantrix's divider. Material 3 1.9.0 draws the outlined button, the unselected chip and the outlined card in this colour. With Pantrix's divider this edge stayed at 1.24:1 against the surface in both light and dark, and was hard to make out. With Pantrix's own field and chip edge (`border`) it is 1.47:1 in light and 1.62:1 in dark; in Material's baseline scheme the same edge is 1.57:1 and 1.82:1. The price: Material's dividers (`HorizontalDivider` uses this colour too) are drawn a shade darker than Pantrix's.

### Type

Each style takes its counterpart by size. Pantrix's scale: display 36, headline 28, title 22, callout 18, body 16, label 14, footnote 12, caption 11, and the monospaced code 14. Material's baseline scale: display 57/45/36, headline 32/28/24, title 22/16/14, body 16/14/12, label 14/12/11.

:::table
| Pantrix | Material → Pantrix | Pantrix → Material |
| :--- | :--- | :--- |
| `display` | `displaySmall` | `displayLarge`, `displayMedium`, `displaySmall` |
| `headline` | `headlineMedium` | `headlineLarge`, `headlineMedium` |
| `title` | `titleLarge` | `headlineSmall`, `titleLarge` |
| `callout` | The typeface and weight of `bodyLarge`, at the callout size of `base` | — |
| `body` | `bodyLarge` | `titleMedium`, `bodyLarge` |
| `label` | `labelLarge` | `titleSmall`, `bodyMedium`, `labelLarge` |
| `footnote` | `bodySmall` | `bodySmall`, `labelMedium` |
| `caption` | `labelSmall` | `labelSmall` |
| `code` | The code of `base` | — |
:::

Material's emphasized styles (`…Emphasized`) are the same as their plain styles. In 1.9.0 the constructor that gives the emphasized styles separately is `internal`; the public constructor sets up an app's scale this way.

### Shapes

The steps are mapped by their radii, not by their names. Material's scale is one step rounder than Pantrix's: Material's small step is 8 dp, Pantrix's 4 dp. Had they been mapped by name, every Pantrix component in a Material theme would be twice as round; an 18 dp checkbox would turn into a circle with 8 dp corners and look like a radio button. Mapped by radius, Material's own scale gives Pantrix's own scale; a component is as round in the other system's theme as in its own.

:::table
| Pantrix | Material → Pantrix | Pantrix → Material |
| :--- | :--- | :--- |
| `xs` (2 dp) | The `xs` of `base` | — |
| `sm` (4 dp) | `extraSmall` (4 dp) | `extraSmall` |
| `md` (8 dp) | `small` (8 dp) | `small` |
| `lg` (12 dp) | `medium` (12 dp) | `medium` |
| `xl` (16 dp) | `large` (16 dp) | `large` and `extraLarge` |
:::

Pantrix's `xs` is less round than any of Material's steps; it comes from `base`. Material's `extraLarge` (28 dp) is rounder than any of Pantrix's steps: in the Material → Pantrix direction it has no counterpart, and in the other direction it takes the roundest step, `xl`. So in a Pantrix brand with sharp corners, Material's dialogs and bottom sheets have sharp corners too.

`none` (square) and `full` (fully round) are the same in both systems. Material takes only shapes made of corners (`CornerBasedShape`): a Pantrix shape not made of corners takes the shape of `fallback` at that step (by default, Material's own scale). Material's three experimental shapes (`largeIncreased`, `extraLargeIncreased`, `extraExtraLarge`) are not mapped.

## 5. What has no counterpart, and `base` {#what-has-no-counterpart-and-base}

Material's scheme has no success, info or warning role, no hovered edge, no focus ring, no disabled colours and no chart colours. `toPantrixColors(base)` takes them from `base`. The default `base` is Pantrix's own colours, as dark as the scheme: `PantrixColors.Dark` for a dark scheme. An app that wants to give its own success green gives its own `base`:

```kotlin
val colors = appScheme.toPantrixColors(base = PantrixColors.Light.copy(success = brandGreen))
```

In type, too, the callout's size and code come from `base`; the scale given to `toPantrixTypography(base)` is usually `rememberPantrixTypography()`. In shapes `xs` comes from `base`; the default of `toPantrixShapes(base)` is Pantrix's own scale.

## 6. Round trips {#round-trips}

*   **Pantrix → Material → Pantrix,** given the first theme as `base`, gives back the same Pantrix theme: every colour, the nine text roles and the five shapes. The two exceptions are in the lines. The box's edge has no place of its own in Material: `border` comes back as `borderStrong`. Material draws the box's edge and the divider in one colour: `divider` comes back as `border`.
*   **Material → Pantrix → Material** keeps every colour that has a place in Pantrix: the roles' four colours, the ground, the text colours, `surfaceContainerLow` and `outline`. `outlineVariant` comes back as `outline`: Pantrix keeps it in its divider, and gives Material the box's edge, which comes from Material's `outline`. In a nested bridge (Pantrix in a Material app, and Material again inside it) the inner Material's outlined components and dividers are drawn darker than the outer one's. Material's colours that have no counterpart in Pantrix (`tertiary`, the surface steps) are derived again. In shapes, too, `extraLarge` has no counterpart in Pantrix: it comes back the same as `large`.

## 7. A mixed screen: content colour and text style {#a-mixed-screen-content-colour-and-text-style}

The two wrappers fill in the other system's values at the root too:

*   **`MaterialThemeFromPantrix`:** makes Material's content colour and text style the colour and style a Pantrix text would take there. A Material `Text` in a dark Pantrix theme is drawn in the theme's text colour, not in black.
*   **`PantrixThemeFromMaterial`:** makes Pantrix's text style the style a Material text would take there: the scheme's large body text (`bodyLarge`). It does not change the content colour. At the root of a `MaterialTheme`, Material's content colour is its own default, black, and only a Material surface changes it. Pantrix's own colour, on the theme's grounds, is already the scheme's text colour (`textPrimary` ← `onBackground`).

For a boundary deep in the screen there are two more small wrappers:

```kotlin
Surface(color = MaterialTheme.colorScheme.primaryContainer) {
    // Pantrix content on Material's surface takes the surface's content colour
    PantrixContentFromMaterial { PantrixText("Draft saved") }
}

PantrixSurface(color = PantrixTheme.colors.primary.color, contentColor = PantrixTheme.colors.primary.onColor) {
    // Material content on Pantrix's fill takes the fill's content colour
    MaterialContentFromPantrix { Text("New") }
}
```

## 8. Material's baseline scheme by Pantrix's rules {#material-s-baseline-scheme-by-pantrix-s-rules}

Material's baseline schemes (`lightColorScheme()`, `darkColorScheme()`) were converted into Pantrix colours and measured by the rules Pantrix applies to its own colours (WCAG 2.2: text 4.5:1, a control's mark and the focus ring 3:1):

*   The roles, the text, the control's mark and the focus ring all keep the rules, in the light and the dark theme, on the ground and on the surface.
*   The one rule not kept is the order of the lines. In Pantrix the lines grow stronger from the divider to the control's mark (`divider` < `border` < `borderHover` < `borderStrong`). In Material the box's edge and the mark are the same colour, so the edge is as strong as the mark.

`MaterialContrastTest` holds this result: if the mapping or Material's scheme changes, the test fails.

## 9. What is not in the bridge {#what-is-not-in-the-bridge}

*   **Motion:** Material's `MotionScheme` is experimental; Pantrix's motion (and the reduced motion setting) does not pass to Material.
*   **Dynamic colour:** the colours from Android's wallpaper (`dynamicLightColorScheme`) are not in the first version. An app can call it itself and give it to `toPantrixColors()`.
*   **Elevation:** Material's tonal elevation (`surfaceTint`) and Pantrix's shadows are not converted into each other.
*   **Spacing and density:** they have no counterparts in Material.

## 10. Testing {#testing}

The bridge's tests are in `ui/material3/src/commonTest`, and run on five targets: the mappings field by field, the round trips, the two wrappers and the mixed screen's content colour, and the contrast measurement of Material's baseline scheme. Since the common tests do not run in Android's host tree, a test of its own draws the two wrappers in Robolectric (`MaterialThemesOnAndroidTest`).

An app tests its own theme like this:

```kotlin
@Test
fun pantrix_s_screen_takes_the_app_s_colours() = runComposeUiTest {
    lateinit var colors: PantrixColors
    setContent { MaterialTheme(colorScheme = appScheme) { PantrixThemeFromMaterial { colors = PantrixTheme.colors } } }
    waitForIdle()
    assertEquals(appScheme.primary, colors.primary.color)
}
```

## 11. What was verified and what was not {#verification}

### Verified

*   Material 3's versions for CMP were read from Maven Central's list: the latest stable version is 1.9.0.
*   The `ColorScheme`, `Typography`, `Shapes` and `MaterialTheme` of 1.9.0 and of 1.11.0-alpha07 were read in the sources in the Gradle cache. That the constructor that gives the emphasized text styles separately is `internal` in 1.9.0 was seen there.
*   The bridge compiles with Compose Multiplatform 1.12.2: Material 3 1.9.0 resolves raised to Compose's 1.12.2. Its tests passed on the JVM, on Android (Robolectric), in the iOS simulator, on JS and on Wasm.
*   **A newer Material 3:** with the bridge compiled against 1.9.0, the JVM tests were run with 1.11.0-alpha07 at run time, and all 24 tests passed. The published bridge is binary-compatible with the app's newer Material 3 (at least with this version).
*   **The sample app:** it was run in the iOS simulator, in the Android emulator, on the web (Wasm) and on the desktop (drawn off screen), and the two themes were compared in pictures, in light and in dark. Its tests passed on the JVM, on iOS, on JS, on Wasm and in the Android emulator. The pictures showed the flaw in the shape mapping ("Shapes" in section 4).
*   detekt's `NoMaterialImport` rule is off in the bridge, and still catches a Material import in `ui/components`.

### Not verified

*   The bridge was not run with a Material 3 newer than 1.11.0-alpha07 (1.12.0-alpha03, 1.13.0-beta01); with 1.11.0-alpha07, too, it was run only on the JVM.
*   The sample app's iOS was not tried from Android Studio's run configuration: the Xcode project was built with `xcodebuild` and opened in the simulator.
*   Material's experimental APIs (emphasized styles, increased shapes, `MotionScheme`) are not in the bridge; using them together was not tried.
