---
title: Installation
description: Add Pantrix UI to a Compose Multiplatform project, wrap the app in its theme and draw the first component. One dependency in the common source set serves every target.
section: Getting started
---

## What it needs {#requirements}

:::facts
- Kotlin: 2.4.21 or newer
- Compose Multiplatform: 1.12.2 or newer
- Android: API 30, Android 11
- iOS: Arm64, simulator
- Desktop and web: JVM, Wasm, JS
:::

:::steps
## Add the repository {#repository toc="The repository"}

In `settings.gradle.kts`, next to Google's and Maven Central:

:::tabs
### GitHub Packages

```kotlin settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/developersancho/pantrix-ui-cmp")
            credentials(PasswordCredentials::class)
        }
        google()
        mavenCentral()
    }
}
```

:::callout warning
**GitHub Packages asks for a token, even for a public package.** Put `GitHubPackagesUsername` and `GitHubPackagesPassword`, a token with `read:packages`, in `~/.gradle/gradle.properties`, never in the project.
:::

### Local Maven

```kotlin settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }
}
```
:::

## Add the dependency {#dependency toc="The dependency"}

One line in the common source set. Gradle picks each target's build, and `foundation` comes with it, so `PantrixTheme` is there at once.

```kotlin build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.pantrixui:components:0.1.0")
        }
        commonTest.dependencies {
            implementation("com.pantrixui:testing:0.1.0")
        }
    }
}
```

:::table The other modules
- `com.pantrixui:testing`: Finding by tag, `PantrixTestTheme`, doubles of the platform's services and assertions
- `com.pantrixui:material3`: The bridge to Material 3, both ways, for an app that moves a screen at a time
- `com.pantrixui:detekt-rules`: The rules an app keeps, for `detektPlugins`
- `com.pantrixui:mcp`: The MCP server, for coding assistants
:::

## Wrap the app in the theme {#theme toc="The theme"}

The theme reads the platform, the system's dark mode, its reduced motion and its language. Every component under it follows them.

```kotlin App.kt
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.foundation.theme.PantrixTheme

@Composable
fun App() {
    PantrixTheme {
        PantrixButton(text = stringResource(Res.string.save), onClick = { /* … */ }, tagKey = "save")
    }
}
```

## Give it your brand {#preset toc="Your brand" optional}

A `DesignPreset` holds a brand's colours for light and dark, its type, its shapes and any component's overrides. The [theme builder](/theme) writes one for you.

```kotlin PantrixPreset.kt
object PantrixPreset : DesignPreset {
    override val name = "Pantrix"
    override fun colors(dark: Boolean) = if (dark) PantrixDark else PantrixLight

    @Composable override fun typography() = rememberPantrixTypography()
    @Composable override fun shapes() = rememberPantrixShapes()
}

PantrixTheme(preset = PantrixPreset) { App() }
```
:::

## On each platform {#platforms}

:::rows
- **iOS**: The fold's bridge comes with the library: no cinterop of your own and no `enableCInteropCommonization`.
- **Web**: Plus Jakarta Sans ships in the app's resources. A page that loads it before Compose starts shows no change of type: [Web fonts](/docs/guides/web-fonts).
- **Android**: Nothing to add: the outermost theme makes tags the resource ids Maestro and UI Automator find. A `Popup` or `Dialog` of the app's own puts `Modifier.pantrixAutomationRoot()` on its root.
:::

## Where next {#next}

:::cards
- [Your first form](/components/text-area): Text fields, a text area and FormState.
- [Theming](/foundations): Colour roles, type, shapes and motion.
- [Adaptive layout](/adaptive): Size classes, panes and folds.
- [Testing](/docs/guides/testing): Tags, PantrixTestTheme and Maestro.
:::
