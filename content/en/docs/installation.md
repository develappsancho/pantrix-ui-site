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
- Android build: compileSdk 37, which the packages ask for; built with AGP 9.5.0
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
            url = uri("https://maven.pkg.github.com/develappsancho/pantrix-ui-cmp-package")
            credentials(PasswordCredentials::class)
        }
        google()
        mavenCentral()
    }
}
```

:::callout warning
**GitHub Packages asks for a token, even for a public package.** Put `GitHubPackagesUsername` and `GitHubPackagesPassword`, a token with `read:packages`, in `~/.gradle/gradle.properties`, never in the project. The packages, each version's notes and the demo app are in [the package repository](https://github.com/develappsancho/pantrix-ui-cmp-package).
:::

To make the token: on GitHub, open [a new classic token](https://github.com/settings/tokens/new?scopes=read:packages&description=Pantrix%20UI) under Settings → Developer settings → Personal access tokens, leave `read:packages` alone ticked, generate it and copy it. Then:

```properties ~/.gradle/gradle.properties
GitHubPackagesUsername=your-github-user
GitHubPackagesPassword=ghp_your-token
```

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

One line in the common source set. Gradle picks each target's build, and `foundation` comes with it, so `PantrixTheme` is there at once. The tests take `testing`, `kotlin("test")` and, to draw on the desktop, Compose's own runtime for it.

:::tabs
### Coordinates

```kotlin build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.pantrixui:components:0.2.1")
        }
        commonTest.dependencies {
            implementation("com.pantrixui:testing:0.2.1")
            implementation(kotlin("test"))
        }
        jvmTest.dependencies {
            // runComposeUiTest draws on the desktop.
            implementation(compose.desktop.currentOs)
        }
    }
}
```

### Version catalog

```toml gradle/libs.versions.toml
[versions]
pantrix = "0.2.1"

[libraries]
pantrix-components = { module = "com.pantrixui:components", version.ref = "pantrix" }
pantrix-testing = { module = "com.pantrixui:testing", version.ref = "pantrix" }
```

```kotlin build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.pantrix.components)
        }
        commonTest.dependencies {
            implementation(libs.pantrix.testing)
            implementation(kotlin("test"))
        }
        jvmTest.dependencies {
            // runComposeUiTest draws on the desktop.
            implementation(compose.desktop.currentOs)
        }
    }
}
```
:::

:::table The other modules
- `com.pantrixui:testing`: Finding by tag, `PantrixTestTheme`, doubles of the platform's services and assertions
- `com.pantrixui:material3`: The bridge to Material 3, both ways, for an app that moves a screen at a time
- `com.pantrixui:detekt-rules`: The rules an app keeps, for `detektPlugins`
- `com.pantrixui:mcp`: The MCP server, for coding assistants
:::

## Wrap the app in the theme {#theme toc="The theme"}

The theme reads the platform, the system's dark mode, its reduced motion and its language. Every component under it follows them.

```kotlin App.kt
import androidx.compose.runtime.Composable
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.foundation.theme.PantrixTheme
import org.jetbrains.compose.resources.stringResource
// Res and save are the module's own, from its generated.resources package.
import myapp.shared.generated.resources.Res
import myapp.shared.generated.resources.save

@Composable
fun App() {
    PantrixTheme {
        PantrixButton(text = stringResource(Res.string.save), onClick = { /* … */ }, tagKey = "save")
    }
}
```

```xml composeResources/values/strings.xml
<resources>
    <string name="save">Save</string>
</resources>
```

:::callout
**A project from Android Studio's wizard starts with Material:** its `App.kt` draws `MaterialTheme`, `Button` and `Text`, and `app/shared` depends on `compose-material3`. Take that line out of its dependencies, or keep it beside `com.pantrixui:material3` to move a screen at a time ([the bridge](/docs/guides/material3)); write `App.kt` as above; and delete the wizard's `Greeting.kt` and its `compose_multiplatform` image.
:::

## Give it your brand {#preset toc="Your brand" optional}

A `DesignPreset` holds a brand's colours for light and dark, its type, its shapes and any component's overrides. The [theme builder](/theme) writes one for you.

```kotlin Harbor.kt
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.pantrixui.foundation.theme.PantrixTheme
import com.pantrixui.foundation.theme.preset.DesignPreset
import com.pantrixui.foundation.token.color.Dark
import com.pantrixui.foundation.token.color.Light
import com.pantrixui.foundation.token.color.PantrixColor
import com.pantrixui.foundation.token.color.PantrixColors
import com.pantrixui.foundation.token.color.PantrixPalette
import com.pantrixui.foundation.token.shape.PantrixShapes
import com.pantrixui.foundation.token.shape.rememberPantrixShapes
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
    override fun shapes(): PantrixShapes = rememberPantrixShapes()
}

PantrixTheme(preset = Harbor) { App() }
```
:::

## On each platform {#platforms}

:::rows
- **iOS**: The fold's bridge comes with the library: no cinterop of your own and no `enableCInteropCommonization`.
- **Web**: Plus Jakarta Sans ships in the app's resources. A page that loads it before Compose starts shows no change of type: [Web fonts](/docs/guides/web-fonts). A page whose loader starts the Wasm build where the browser runs it, and the JavaScript build elsewhere, Safari 27.2 among them: [The loader and the page](/docs/guides/web-compatibility#the-loader-and-the-page).
- **Android**: Nothing to add: the outermost theme makes tags the resource ids Maestro and UI Automator find. A `Popup` or `Dialog` of the app's own puts `Modifier.pantrixAutomationRoot()` on its root.
:::

## Where next {#next}

:::cards
- [Your first form](/components/text-area): Text fields, a text area and FormState.
- [Theming](/foundations): Colour roles, type, shapes and motion.
- [Adaptive layout](/adaptive): Size classes, panes and folds.
- [Testing](/docs/guides/testing): Tags, PantrixTestTheme and Maestro.
:::
