---
title: Kurulum
description: Pantrix UI'ı bir Compose Multiplatform projesine ekleyin, uygulamayı temasına sarın ve ilk bileşeni çizin. Ortak kaynak kümesindeki tek bir bağımlılık her hedefe yeter.
section: Başlarken
---

## Ne gerekir {#requirements}

:::facts
- Kotlin: 2.4.21 ya da daha yeni
- Compose Multiplatform: 1.12.2 ya da daha yeni
- Android: API 30, Android 11
- Android derlemesi: paketlerin istediği compileSdk 37; AGP 9.5.1 ile derlendi
- iOS: Arm64, simülatör
- Masaüstü ve web: JVM, Wasm, JS
:::

:::steps
## Depoyu ekleyin {#repository toc="Depo"}

`settings.gradle.kts` dosyasında, Google'ın ve Maven Central'ın yanına:

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
**GitHub Packages, herkese açık bir paket için bile bir token ister.** `GitHubPackagesUsername` ile `GitHubPackagesPassword`'ü, `read:packages` izinli bir token'la, `~/.gradle/gradle.properties` dosyasına koyun, hiçbir zaman projeye değil. Paketler, her sürümün notları ve demo uygulaması [paket deposundadır](https://github.com/develappsancho/pantrix-ui-cmp-package).
:::

Token'ı almak için: GitHub'da Settings → Developer settings → Personal access tokens altında [yeni bir klasik token](https://github.com/settings/tokens/new?scopes=read:packages&description=Pantrix%20UI) açın, yalnızca `read:packages` işaretli kalsın, oluşturup kopyalayın. Sonra:

```properties ~/.gradle/gradle.properties
GitHubPackagesUsername=github-kullanici-adiniz
GitHubPackagesPassword=ghp_tokeniniz
```

### Yerel Maven

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

## Bağımlılığı ekleyin {#dependency toc="Bağımlılık"}

Ortak kaynak kümesinde tek bir satır. Gradle her hedefin derlemesini kendisi seçer ve `foundation` onunla birlikte gelir, böylece `PantrixTheme` hemen elinizin altındadır. Testler `testing`'i, `kotlin("test")`'i ve masaüstünde çizmek için Compose'un oradaki çalışma zamanını alır.

:::tabs
### Koordinatlar

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
            // runComposeUiTest masaüstünde çizer.
            implementation(compose.desktop.currentOs)
        }
    }
}
```

### Sürüm kataloğu

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
            // runComposeUiTest masaüstünde çizer.
            implementation(compose.desktop.currentOs)
        }
    }
}
```
:::

:::table Öteki modüller
- `com.pantrixui:testing`: Etiketle bulma, `PantrixTestTheme`, platformun hizmetlerinin yerine geçenler ve doğrulamalar
- `com.pantrixui:material3`: Material 3'le iki yönlü köprü, ekranlarını birer birer taşıyan bir uygulama için
- `com.pantrixui:detekt-rules`: Bir uygulamanın uyduğu kurallar, `detektPlugins` için
- `com.pantrixui:mcp`: Kodlama asistanları için MCP sunucusu
:::

## Uygulamayı temaya sarın {#theme toc="Tema"}

Tema platformu, sistemin koyu kipini, azaltılmış hareketini ve dilini okur. Altındaki her bileşen onlara uyar.

```kotlin App.kt
import androidx.compose.runtime.Composable
import com.pantrixui.components.elements.button.PantrixButton
import com.pantrixui.foundation.theme.PantrixTheme
import org.jetbrains.compose.resources.stringResource
// Res ve save modülün kendisinindir, generated.resources paketinden.
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
    <string name="save">Kaydet</string>
</resources>
```

:::callout
**Android Studio'nun sihirbazından gelen bir proje Material'la başlar:** `App.kt`'si `MaterialTheme`, `Button` ve `Text` çizer, `app/shared` da `compose-material3`'e bağımlıdır. O satırı bağımlılıklarından çıkarın ya da ekranları birer birer taşımak için `com.pantrixui:material3`'ün yanında tutun ([köprü](/docs/guides/material3)); `App.kt`'yi yukarıdaki gibi yazın; sihirbazın `Greeting.kt`'sini ve `compose_multiplatform` görselini silin.
:::

## Markanızı verin {#preset toc="Markanız" optional}

Bir `DesignPreset` bir markanın açık ve koyu renklerini, yazısını, şekillerini ve bileşenlere özel ayarlarını tutar. [Tema oluşturucu](/theme) sizin için bir tane yazar.

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

## Her platformda {#platforms}

:::rows
- **iOS**: Katlanma köprüsü kütüphaneyle birlikte gelir: size ait bir cinterop da `enableCInteropCommonization` da gerekmez.
- **Web**: Plus Jakarta Sans uygulamanın kaynaklarında gelir. Onu Compose başlamadan önce yükleyen bir sayfada yazı tipi hiç değişmez: [Web yazı tipleri](/docs/guides/web-fonts). Tarayıcının çalıştırabildiği yerde Wasm sürümünü, başka yerde, Safari 27.2 de içinde, JavaScript sürümünü başlatan bir yükleyici: [Yükleyici ve sayfa](/docs/guides/web-compatibility#the-loader-and-the-page).
- **Android**: Eklenecek bir şey yok: en dıştaki tema, etiketleri Maestro'nun ve UI Automator'ın bulduğu kaynak kimliklerine çevirir. Uygulamanın kendi `Popup`'ı ya da `Dialog`'u kökünde `Modifier.pantrixAutomationRoot()` taşır.
:::

## Sırada ne var {#next}

:::cards
- [İlk formunuz](/components/text-area): Metin alanları, bir metin kutusu ve FormState.
- [Tema](/foundations): Renk rolleri, yazı, şekiller ve hareket.
- [Uyarlanabilir yerleşim](/adaptive): Boyut sınıfları, paneller ve katlanmalar.
- [Test](/docs/guides/testing): Etiketler, PantrixTestTheme ve Maestro.
:::
