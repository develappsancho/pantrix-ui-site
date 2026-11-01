# An app that keeps Material 3

How an app that already has Material 3 takes Pantrix in, a screen at a time, or keeps both: one theme made into the
other. The bridge is `com.pantrixui:material3`, in the package `com.pantrixui.material3`; no other module of the library
depends on it, and the library itself has no Material.

```kotlin
commonMain.dependencies {
    implementation("com.pantrixui:material3:0.1.0-alpha05")
}
```

It is built against Material 3 1.9.0, the latest stable one: an app with a newer Material 3 keeps its own.

## Pantrix's components in a Material app

`PantrixThemeFromMaterial` reads the `MaterialTheme` around it and sets up a `PantrixTheme`: its colours are the
scheme's, as dark as the scheme, its type is built from Material's scale and its shapes from Material's by their radii.
The rest (spacing, motion, the language, analytics) is Pantrix's own, or a `PantrixTheme` inside it gives it. As the
outermost Pantrix theme it sets the system bars' icons by the scheme's dark; an app that sets them itself writes
`PantrixThemeFromMaterial(followSystemBars = false) { … }`.

```kotlin
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.pantrixui.material3.PantrixThemeFromMaterial

@Composable
fun MovedScreen(scheme: ColorScheme) {
    MaterialTheme(colorScheme = scheme) {
        PantrixThemeFromMaterial {
            // Pantrix's components here, in the app's Material colours, type and shapes.
            CheckoutScreen()
        }
    }
}
```

## Material's components in a Pantrix app

`MaterialThemeFromPantrix` reads the Pantrix theme around it and sets up a `MaterialTheme`: for a Material component
Pantrix does not have.

```kotlin
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.material3.MaterialThemeFromPantrix

@Composable
fun VolumeSlider(volume: Float, onVolumeChange: (Float) -> Unit) {
    PantrixTheme {
        MaterialThemeFromPantrix {
            Slider(value = volume, onValueChange = onVolumeChange)
        }
    }
}
```

## Mixed screens and plain conversions

- **Content colour.** Inside a Material `Surface`, `PantrixContentFromMaterial { … }` gives Pantrix's text the
  surface's content colour; inside a Pantrix fill, `MaterialContentFromPantrix { … }` gives Material's text the fill's.
- **The conversions.** An app that sets up its themes itself uses the six functions under the wrappers:
  `ColorScheme.toPantrixColors(base)` and `PantrixColors.toColorScheme()`, `Typography.toPantrixTypography(base)` and
  `PantrixTypography.toMaterialTypography()`, `Shapes.toPantrixShapes(base)` and `PantrixShapes.toMaterialShapes()`.
  `base` gives what the other system has no value for.
- **Dynamic colour.** Android's wallpaper colours are the app's to read: `dynamicLightColorScheme(context)
  .toPantrixColors()`.
- **Not bridged.** Motion, Material's tonal elevation, and Pantrix's spacing and density have no counterpart.

## What still holds

The rules of [SKILL.md](../SKILL.md) hold for the screens Pantrix draws. Rule 1 means a screen does not mix the two
systems for the same thing: a screen moved to Pantrix uses `PantrixButton` and `PantrixText`, not Material's, and the
Material components that stay are the ones Pantrix does not have. The app's detekt keeps `NoMaterialImport` off while
it keeps Material ([setup.md](setup.md)).
