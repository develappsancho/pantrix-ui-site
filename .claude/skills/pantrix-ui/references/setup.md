# Setting up an app

How a Compose Multiplatform project takes Pantrix UI in: the repository, the dependencies, the theme at its root,
each platform's entry point, and the rules an app's own detekt keeps.

Contents: Repository · Dependencies · The theme at the root · Entry points · The rules an app keeps

## Repository

Pantrix UI is published to two Maven repositories, and the app's `settings.gradle.kts` names the one it takes it from.

The local Maven repository, after `./gradlew clean publishToMavenLocal` in Pantrix's own repository:

```kotlin
dependencyResolutionManagement {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }
}
```

GitHub Packages, from the package repository `https://github.com/develappsancho/pantrix-ui-cmp-package`. GitHub asks
for a token even to read a package: the properties `GitHubPackagesUsername` and `GitHubPackagesPassword` (a token with
`read:packages`) stand in `~/.gradle/gradle.properties`, never in the project.

```kotlin
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

## Dependencies

The shared module's `build.gradle.kts`:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.pantrixui:components:0.1.0-alpha04")
        }
        commonTest.dependencies {
            implementation("com.pantrixui:testing:0.1.0-alpha04")
            implementation(kotlin("test"))
        }
        // runComposeUiTest draws on the desktop: the test JVM needs this machine's Skiko.
        jvmTest.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}
```

- `components` brings `foundation` and `annotations` with it: `PantrixTheme`, the tokens and the platform services
  need no line of their own.
- `testing` goes into test source sets alone: its Android side declares the activity a Compose test starts.
- The library is built with Kotlin 2.4.21 and Compose Multiplatform 1.12.2, and runs on Android 11 (API 30) and newer:
  an app uses these versions or newer ones.
- No Material dependency is needed. An app that keeps Material 3 adds `com.pantrixui:material3`
  ([material3.md](material3.md)).

## The theme at the root

Everything Pantrix draws stands inside a `PantrixTheme`. The app's root composable puts it there once:

```kotlin
import androidx.compose.runtime.Composable
import com.pantrixui.foundation.theme.PantrixTheme

@Composable
fun App() {
    PantrixTheme {
        AppNavigation()
    }
}
```

`PantrixTheme { … }` is Pantrix's own look, light or dark as the system is. A brand's look is
`PantrixTheme(preset = Brand) { … }` ([theming.md](theming.md)). The theme tells the content the window it is drawn
in, the platform's conventions, the language and the system's settings by itself: nothing else is set up for them.

## Entry points

Each platform's entry point draws the same `App()`.

```kotlin
// androidMain: the app's activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}
```

```kotlin
// iosMain: what the Swift side shows
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController { App() }
```

```kotlin
// jvmMain: the desktop
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "My app") {
        App()
    }
}
```

```kotlin
// webMain: Wasm and JS
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.pantrixui.foundation.token.typography.PantrixTypefaceGate
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(viewportContainerId = "app") {
        PantrixTypefaceGate(onShown = { document.getElementById("loading")?.remove() }) {
            App()
        }
    }
}
```

- **Edge to edge.** On Android `enableEdgeToEdge()` lets the app draw behind the system bars: `PantrixScaffold` and
  its bars keep their content clear of them by the window's insets ([layout.md](layout.md)).
- **The web's typeface.** A browser fetches a typeface, and draws text in a fallback face until it has come.
  `PantrixTypefaceGate` draws the app once the theme's typeface has come, and waits three seconds at the most. An app
  in a typeface of its own gives it that typeface's answer: `PantrixTypefaceGate(loaded = areTypefacesLoaded(Brand))`.

The web page that loads the app holds the box it is drawn in, the sign shown while it loads, and a preload of
Pantrix's typeface, so that the typeface is fetched while the app's code is. Without `crossorigin` the browser fetches
it twice.

```html
<head>
    <link rel="preload" as="fetch" type="font/ttf" crossorigin
          href="composeResources/com.pantrixui.foundation.generated.resources/font/plus_jakarta_sans.ttf">
</head>
<body>
    <div id="loading">…</div>
    <div id="app"></div>
</body>
```

## The rules an app keeps

The rules of [SKILL.md](../SKILL.md) that a machine can check are published as detekt rules,
`com.pantrixui:detekt-rules`:

```kotlin
dependencies {
    detektPlugins("com.pantrixui:detekt-rules:0.1.0-alpha04")
}
```

Built upon detekt's default configuration (`buildUponDefaultConfig = true`), the rule set turns three rules on:
`FixedSideLayout` (start and end, not left and right), `ImageWithoutDescription` (an image says what it shows) and
`UntaggedControl` (every control a `tagKey`). An app with no Material turns on `NoMaterialImport` as well:

```yaml
PantrixUIRule:
  NoMaterialImport:
    active: true
```

The rule classes are compiled for JVM 17: the JDK that runs detekt is 17 or newer.
