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
            url = uri("https://maven.pkg.github.com/developersancho/pantrix-ui-cmp")
            credentials(PasswordCredentials::class)
        }
        google()
        mavenCentral()
    }
}
```

:::callout warning
**GitHub Packages, herkese açık bir paket için bile bir token ister.** `GitHubPackagesUsername` ile `GitHubPackagesPassword`'ü, `read:packages` izinli bir token'la, `~/.gradle/gradle.properties` dosyasına koyun, hiçbir zaman projeye değil.
:::

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

Ortak kaynak kümesinde tek bir satır. Gradle her hedefin derlemesini kendisi seçer ve `foundation` onunla birlikte gelir, böylece `PantrixTheme` hemen elinizin altındadır.

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

:::table Öteki modüller
- `com.pantrixui:testing`: Etiketle bulma, `PantrixTestTheme`, platformun hizmetlerinin yerine geçenler ve doğrulamalar
- `com.pantrixui:material3`: Material 3'le iki yönlü köprü, ekranlarını birer birer taşıyan bir uygulama için
- `com.pantrixui:detekt-rules`: Bir uygulamanın uyduğu kurallar, `detektPlugins` için
- `com.pantrixui:mcp`: Kodlama asistanları için MCP sunucusu
:::

## Uygulamayı temaya sarın {#theme toc="Tema"}

Tema platformu, sistemin koyu kipini, azaltılmış hareketini ve dilini okur. Altındaki her bileşen onlara uyar.

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

## Markanızı verin {#preset toc="Markanız" optional}

Bir `DesignPreset` bir markanın açık ve koyu renklerini, yazısını, şekillerini ve bileşenlere özel ayarlarını tutar. [Tema oluşturucu](/theme) sizin için bir tane yazar.

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

## Her platformda {#platforms}

:::rows
- **iOS**: Katlanma köprüsü kütüphaneyle birlikte gelir: size ait bir cinterop da `enableCInteropCommonization` da gerekmez.
- **Web**: Plus Jakarta Sans uygulamanın kaynaklarında gelir. Onu Compose başlamadan önce yükleyen bir sayfada yazı tipi hiç değişmez: [Web yazı tipleri](/docs/guides/web-fonts).
- **Android**: Eklenecek bir şey yok: en dıştaki tema, etiketleri Maestro'nun ve UI Automator'ın bulduğu kaynak kimliklerine çevirir. Uygulamanın kendi `Popup`'ı ya da `Dialog`'u kökünde `Modifier.pantrixAutomationRoot()` taşır.
:::

## Sırada ne var {#next}

:::cards
- [İlk formunuz](/components/text-area): Metin alanları, bir metin kutusu ve FormState.
- [Tema](/foundations): Renk rolleri, yazı, şekiller ve hareket.
- [Uyarlanabilir yerleşim](/adaptive): Boyut sınıfları, paneller ve katlanmalar.
- [Test](/docs/guides/testing): Etiketler, PantrixTestTheme ve Maestro.
:::
