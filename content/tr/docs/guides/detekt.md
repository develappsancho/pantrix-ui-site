---
title: Uygulamanızda detekt kuralları
description: Projede detekt'in neyi denetlediği, nerede çalıştığı, projenin kendi kuralları, bir bulgunun nasıl ele alındığı ve Pantrix UI'ı kullanan bir uygulamanın bu kuralları nasıl açacağı.
---

## 1. Detekt nedir? {#what-is-detekt}

Detekt, Kotlin için bir statik kod analizi aracıdır. Kotlinter kodun nasıl göründüğüne bakar; detekt kodun ne yaptığına ve nasıl kurulduğuna bakar: fazla karmaşık bir fonksiyon, gereksiz bir kod, bir kuralın çiğnendiği bir yer. Bu projede iki tür kural çalışır:

*   **detekt'in kendi kuralları:** karmaşıklık, adlandırma, olası hatalar, stil.
*   **Projenin kendi kural seti, `PantrixUIRule`:** `tooling/detekt-rules` modülünde. CONTRIBUTING.md'deki kütüphane kurallarının ve erişilebilirlik, dil ve token kurallarının makine hali.

:::sketch detekt
:::

## 2. Nerede çalışır? {#where-does-it-run}

:::table
| Yer | Ayar | Ne çalışır |
| :--- | :--- | :--- |
| Kütüphane modülleri (`ui/foundation`, `ui/components`, `ui/annotations`, `ui/testing`, `ui/material3`) | Bu depodaki `config/detekt/detekt.yml` | detekt'in kuralları ve projenin kütüphane kuralları. Her bulgu derlemeyi durdurur. |
| Demo (`app/*`) | Bu depodaki `config/detekt/demo.yml`, 5. bölümde yazılı | Yalnızca projenin bir uygulamanın da uyduğu dört kuralı: `NoMaterialImport`, `FixedSideLayout`, `ImageWithoutDescription`, `UntaggedControl`. Son ikisi bildirir ama derlemeyi durdurmaz. |
:::

detekt'in kendi kurallarından `UndocumentedPublicClass`, `UndocumentedPublicFunction` ve `UndocumentedPublicProperty` kütüphanede açıktır: bir sınıf, bir fonksiyon ve bir özellik KDoc'suz kalmaz. Bir enum'un girdisi de özellik sayılır. Parametreleri projenin `UndocumentedParameter`'ı tutar (3. bölüm).

Kurulum, kütüphane için de demo için de, bu deponun kendi derleme mantığındadır; bir uygulamanın aynısı için yazdığı 5. bölümdedir. Testler projenin kurallarının dışındadır.

Çalıştırmak:

*   **`./gradlew detekt`:** her modülde, her hedefin kendi görevini çalıştırır. Kotlin Multiplatform'da analiz hedef hedef yapılır (`detektMetadataCommonMain`, `detektJvmMain`, `detektAndroidMain`, `detektIosArm64Main`…); düz `detekt` görevi hepsine bağlanmıştır, yoksa hiçbir şeye bakmadan yeşil geçerdi.
*   **`./gradlew :ui:components:detekt`:** tek bir modülde.
*   **Kurallar değiştikten sonra** (`tooling/detekt-rules`): `./gradlew detekt --no-daemon`. Çalışmakta olan bir daemon eski kural sınıflarını kullanmaya devam eder. `./gradlew --stop` kullanılmaz: IDE'nin daemon'ını da durdurur.

Raporlar her modülün `build/reports/detekt/` klasöründedir: HTML (okumak için) ve SARIF (araçlar için).

## 3. Projenin kuralları {#the-project-s-rules}

:::table
| Kural | Neyi tutar | Nerede açık |
| :--- | :--- | :--- |
| `NoMaterialImport` | Kütüphanede Material yok: bir Material import'u ya da tam adıyla yazılmış bir Material adı | Kütüphane (Material 3 köprüsü `ui/material3` hariç), demo |
| `NoFoundationPrimitive` | Metin Pantrix'in sarmalayıcılarından geçer: `BasicText`, `BasicTextField`, `BasicSecureTextField` yalnızca sarmalayıcıların kendi dosyalarında | Kütüphane |
| `UnmarkedComponent` | Her public `Pantrix…` composable'ı ya bir bileşendir (`@PantrixComponent` ve iki `@PantrixExample`) ya da bir bileşenin parçasıdır (`@PantrixComponentPart`) | `ui/components` |
| `SettingInStrings` | `PantrixStrings` yalnızca sözcük taşır: bir ayar, bileşenin ya da yerel ayarın parametresidir | Kütüphane |
| `TagFromVisibleText` | Otomasyon etiketi ve anahtarı görünen metinden kurulmaz | Kütüphane |
| `RawTestTag` | Bir bileşen etiketini `Modifier.testTag` ile değil, etiket politikasına ve test kapsamına uyan `Modifier.pantrixTestTag` ile koyar | Kütüphane |
| `EmptyComposableDefault` | Composable bir lambda'nın varsayılanı boş bir lambda değil `null`'dır; boş lambda ABI'ye public bir `ComposableSingletons` sınıfı ekler | Kütüphane |
| `KDocNotASentence` | KDoc'un özeti bir cümledir: büyük harfle başlar, noktayla biter (`iOS` gibi küçük harfle yazılan adlar olabilir) | Kütüphane |
| `UndocumentedParameter` | Genel bir fonksiyonun ya da yapıcının her parametresi açıklanır: KDoc'un metninde bir `[ad]` bağlantısıyla, bir `@param` ya da `@property` etiketiyle, ya da parametrenin kendi KDoc'uyla. Hiç KDoc'u olmayan bir bildirim detekt'in `UndocumentedPublicFunction`'ına ve `UndocumentedPublicClass`'ına kalır. Bir override'ın (yapıcının `override val`'ı da) ve bir `actual`'ın açıklaması override ettiğinde ve `expect`'indedir | Kütüphane |
| `TagKeyLast` | Bir bileşenin `tagKey`'i son parametresidir: kazandığı yeni parametre anahtarın hemen önüne girer, ondan sonra yalnızca çizdiği içerik lambdası, composable olan ya da bir kapsamda çağrılan, gelir. Bileşen, composable bir fonksiyon ya da genel bir sınıfın yapıcısıdır; bir anahtarla çalışan fonksiyon, örneğin `pantrixTestTagOf`, ve bir override dışında kalır | Kütüphane |
| `LiteralTextInLibrary` | Bir bileşenin söylediği `PantrixStrings`'ten gelir; ekrana ya da ekran okuyucuya giden sabit metin olmaz. Animasyon etiketi ve inspector adı gibi araçlara verilen adlar sayılmaz | Kütüphane (metin paketleri hariç) |
| `FixedSideLayout` | Yerleşim okuma yönünü izler: `absolutePadding`, `absoluteOffset`, `AbsoluteAlignment`, `Arrangement.Absolute` yerine başlangıç ve son | Kütüphane, demo |
| `SizeOutsideStyle` | Bir bileşenin ölçüsü stilinde ya da adı konmuş bir sabittedir; kodun içinde `12.dp` yazılmaz. 0 ve 1 serbesttir | `ui/components` (stil dosyaları hariç) |
| `ColorOutsideTokens` | Bir bileşenin rengi temadan ya da kendi renklerinden gelir; `Color(0xFF…)` yazılmaz | `ui/components` (stil dosyaları hariç) |
| `ImageWithoutDescription` | Bir görsel ne gösterdiğini söyler ya da `contentDescription = null` ile süs olduğunu söyler; boş bir açıklama verilmez | Demo (bildirir) |
| `UntaggedControl` | Bir kontrol `tagKey` taşır (ya da form alanına bağlıdır ya da etiketini `modifier`'ında kendisi koyar). `onClick = null` verilmiş, yalnızca bir sözcük gösteren bir chip ve hiç `onClick` verilmemiş, yalnızca okunan bir liste satırı kontrol sayılmaz | Demo (bildirir) |
:::

Kuralların ayarlanabilen yerleri (`allowedPathPattern`, `includes`, `excludes`, ad listeleri) her kuralın KDoc'unda yazılıdır.

## 4. Bir bulguyu ele almak {#dealing-with-a-finding}

Bir bulgu iki yoldan biriyle kapanır:

*   **Düzeltilir.** Çoğu bulgunun cevabı budur.
*   **Durduğu yerde, gerekçesiyle bastırılır.** Bir kural bilerek çiğneniyorsa bastırma en dar yere konur (bir sabit, küçük bir fonksiyon) ve gerekçesi aynı satırda yazılır:

```kotlin
@Suppress("FixedSideLayout") // A JSON document is laid out from the left in every language.
private val COPY_CORNER: Alignment = AbsoluteAlignment.TopRight
```

**Baseline kullanılmaz.** Bir baseline dosyası bulguyu kimsenin bakmadığı bir yere koyar; bulgu oradayken kural tutuluyor gibi görünür.

## 5. Kendi uygulamanda Pantrix kuralları {#pantrix-s-rules-in-your-own-app}

Kurallar `com.pantrixui:detekt-rules` olarak yayınlanır. Bir uygulama onları detekt'e eklenti olarak verir; detekt 1.23 ile (kurallar detekt 1.23.8'in API'sine derlenir) ve, Pantrix'in kendi derlemesinin yaptığı gibi, Kotlin Multiplatform modülünün her hedefine bağlanarak:

```kotlin
// Ortak modülün build.gradle.kts'i.
plugins {
    // Kurallar detekt 1'in API'sine (io.gitlab.arturbosch.detekt.api) yazılır: detekt 2 onları yüklemez.
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
    basePath = rootProject.projectDir.absolutePath
}

dependencies {
    detektPlugins("com.pantrixui:detekt-rules:0.2.1")
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    // Üretilen kaynaklar, Compose'un kaynak erişicileri gibi, uygulamanın denetleyeceği şeyler değildir.
    exclude { it.file.absolutePath.contains("${File.separator}build${File.separator}") }
}

// Kotlin Multiplatform'da düz görev boş bir kaynak kümesine bakar: her hedefin kendi görevine bağlanır.
tasks.named("detekt") {
    dependsOn(tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().matching {
        it.name != "detekt" && !it.name.startsWith("detektBaseline")
    })
}
```

Bu satırlar Pantrix'in kendi modüllerinin kurulumunu bir modülün derleme dosyası için yeniden söyler; burada yazıldıkları hâliyle bir uygulamanın derlemesinde koşturulmadılar. detekt 1.23.8 ve Kotlin 2.4 kullanan bir uygulama detekt'in Kotlin'ini sabitlemez: detekt kendininkini (2.0.21) tutar, kuralların yanındaki `kotlin-stdlib` bir zarar vermedi (bir uygulamada görüldü).

detekt kendi varsayılan ayarının üstüne kuruluyorsa (`buildUponDefaultConfig = true`), kural setinin kendi ayarı üç kuralı açar: `FixedSideLayout`, `ImageWithoutDescription`, `UntaggedControl`. Kütüphaneye özgü kurallar kapalı başlar; bir uygulama isterse kendi ayarında açar. Pantrix'in demo'sunun bir uygulama olarak denetlendiği ayar, son iki kuralı bildirip derlemeyi durdurmayan hâliyle:

```yaml
# config/detekt/detekt.yml: Pantrix'in demo'sunun, kütüphaneyi kullanan bir uygulama olarak denetlendiği ayar.
build:
  # Bu ikisi bildirir ama derlemeyi durdurmaz: ağırlığı sıfır olan bulgu maxIssues'a sayılmaz.
  weights:
    UntaggedControl: 0
    ImageWithoutDescription: 0

# detekt'in kendi kuralları kapalı: yalnızca Pantrix'in bir uygulamanın da uyduğu kuralları çalışır. Onları isteyen bir uygulama bunları yazmaz.
comments:
  active: false
complexity:
  active: false
coroutines:
  active: false
empty-blocks:
  active: false
exceptions:
  active: false
naming:
  active: false
performance:
  active: false
potential-bugs:
  active: false
style:
  active: false

PantrixUIRule:
  active: true
  # Uygulama Material kullanmıyorsa:
  NoMaterialImport:
    active: true
  FixedSideLayout:
    active: true
  ImageWithoutDescription:
    active: true
    excludes: &tests
      - '**/commonTest/**'
      - '**/jvmTest/**'
      - '**/iosTest/**'
      - '**/jsTest/**'
      - '**/wasmJsTest/**'
      - '**/webTest/**'
      - '**/androidHostTest/**'
      - '**/androidDeviceTest/**'
  UntaggedControl:
    active: true
    excludes: *tests
```

Kural sınıfları JVM 17'ye derlenir: detekt'i çalıştıran JDK 17 ya da daha yeni olmalıdır.

## 6. Detekt ve Kotlinter {#detekt-and-kotlinter}

:::table
| | Detekt | Kotlinter |
| :--- | :--- | :--- |
| **Neye bakar** | Kodun kalitesi ve kuralları | Kodun biçimi |
| **Örnek bulgu** | "Bu boyut bileşenin stilinde olmalı" | "Burada bir boşluk eksik" |
| **Cevabı** | Kodu düzeltmek ya da gerekçeli bir istisna | `./gradlew formatKotlin` |
:::
