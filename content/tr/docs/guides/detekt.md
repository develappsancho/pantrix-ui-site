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
| Kütüphane modülleri (`ui/foundation`, `ui/components`, `ui/annotations`, `ui/testing`, `ui/material3`) | [`config/detekt/detekt.yml`](https://github.com/developersancho/pantrix-ui-cmp/blob/main/config/detekt/detekt.yml) | detekt'in kuralları ve projenin kütüphane kuralları. Her bulgu derlemeyi durdurur. |
| Demo (`app/*`) | [`config/detekt/demo.yml`](https://github.com/developersancho/pantrix-ui-cmp/blob/main/config/detekt/demo.yml) | Yalnızca projenin bir uygulamanın da uyduğu dört kuralı: `NoMaterialImport`, `FixedSideLayout`, `ImageWithoutDescription`, `UntaggedControl`. Son ikisi bildirir ama derlemeyi durdurmaz. |
:::

detekt'in kendi kurallarından `UndocumentedPublicClass`, `UndocumentedPublicFunction` ve `UndocumentedPublicProperty` kütüphanede açıktır: bir sınıf, bir fonksiyon ve bir özellik KDoc'suz kalmaz. Bir enum'un girdisi de özellik sayılır. Parametreleri projenin `UndocumentedParameter`'ı tutar (3. bölüm).

Kurulum `build-logic`'teki iki eklentidedir: kütüphane için `pantrix.codequality`'nin uyguladığı `DetektSetupPlugin`, demo için `pantrix.demo.detekt`. Testler projenin kurallarının dışındadır.

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

Kurallar `com.pantrixui:detekt-rules` olarak yayınlanır. Bir uygulama onları detekt'e eklenti olarak verir:

```kotlin
dependencies {
    detektPlugins("com.pantrixui:detekt-rules:0.1.0-SNAPSHOT")
}
```

detekt kendi varsayılan ayarının üstüne kuruluyorsa (`buildUponDefaultConfig = true`), kural setinin kendi ayarı üç kuralı açar: `FixedSideLayout`, `ImageWithoutDescription`, `UntaggedControl`. Kütüphaneye özgü kurallar kapalı başlar; bir uygulama isterse kendi ayarında açar. Önerilen ayar, son ikisini bildirip derlemeyi durdurmayan haliyle:

```yaml
build:
  weights:
    UntaggedControl: 0
    ImageWithoutDescription: 0

PantrixUIRule:
  # Uygulama Material kullanmıyorsa:
  NoMaterialImport:
    active: true
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
