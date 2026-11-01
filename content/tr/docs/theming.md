---
title: Tema
description: Bir ekranın temanın renklerini, yazısını, boşluklarını ve köşelerini nasıl okuduğu, bir markanın kendi görünüşünü tek bir hazır ayarla nasıl verdiği ve bir ekranın bir parçasının nasıl farklı göründüğü. Bir ekranda hiçbir şey bir rengi ya da bir ölçüyü adıyla anmaz.
section: Başlarken
---

## Temanın tuttukları {#values}

:::table Bir composable'ın okudukları
- `PantrixTheme.colors`: Renk rolleri: `primary.color`, `surface`, `textSecondary`, `divider`
- `PantrixTheme.typography`: Yazı ölçeği: `title`, `body`, `caption`
- `PantrixTheme.spacing`: Boşluklar: `xs` 4, `sm` 8, `md` 12, `lg` 16, `xl` 24, `xxl` 32dp, yoğunlukla ölçeklenir
- `PantrixTheme.shapes`: Köşeler: `xs` 2, `sm` 4, `md` 8, `lg` 12, `xl` 16dp, bir de `none` ve `full`
- `PantrixTheme.motion`: Süreler, `fast` 100, `normal` 200 ve `slow` 350ms; sistem hareketi azalttığında sıfır
- `PantrixTheme.style(ButtonStyle)`: Bir bileşenin o yerde geçerli olan stili
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

## İşine göre renkler {#colors}

Bir bileşen bir rengi adıyla değil işiyle ister: açık ve koyu tema kodu değil rengi değiştirir.

:::rows
- **Roller**: `primary` markanındır: asıl eylem, seçili olan. `success`, `info`, `warning` ve `danger` bir durumu söyler, `neutral` göz çekmez. Her birinin `color`'ı, `container`'ı ve üstlerine yazılan için `onColor` ve `onContainer`'ı vardır.
- **Zeminler ve yazı**: `background` uygulamanın arka planı, `surface` bir kartın ya da bir sayfanın zemini. Yazı `textPrimary`, `textSecondary`, `textDisabled` ve `textInverse`'tir.
- **Çizgiler**: `border` bir kutunun yumuşak kenarı, `borderStrong` yalnızca kenardan ibaret bir kontrolün kenarı, `divider` öğeler arasındaki çizgi.
- **İçerik rengi**: Bir dolgu üstüne yazılana hangi rengi alacağını söyler: renk verilmeyen bir yazı ya da ikon onu alır.
:::

## Yazı {#type}

Dokuz stil: başlıklar için `display`, `headline` ve `title`; yazı için `callout`, `body`, `label`, `footnote` ve `caption`; eş aralıklı yazı için `code`. Stil verilmeyen yazı `label`'dır. Bir stil bir rol değil bir görünüştür: bir başlık ekran okuyucu için `role = TextRole.Heading(level)` ile başlıktır. Boyutlar sp'dir; yazı sistemin yazı boyutuyla büyür: [Tipografi](/docs/guides/typography).

:::steps
## Markanın yazı tipini adlandır {#typeface toc="Yazı tipi"}

```kotlin BrandType.kt
val Brand = PantrixTypeface(
    PantrixFont(Res.font.brand_regular),
    PantrixFont(Res.font.brand_bold, FontWeight.Bold)
)
```

## Temaya ver {#scale toc="Ölçek"}

```kotlin App.kt
PantrixTheme(typography = rememberPantrixTypography(Brand)) { App() }
```

`rememberPantrixTypography(Brand, codeTypeface = BrandMono)` kodu kendi yazı tipiyle yazar.
:::

## Tek bir hazır ayarda bir markanın görünüşü {#preset}

Bir `DesignPreset` bir markanın açık ve koyu renklerini, yazısını, şekillerini ve bileşenlerine özel ayarlarını tutar. Renkleri bir kez, burada yazılır: bir `Color(0xFF…)` bir hazır ayarda durur, asla bir ekranda. [Tema oluşturucu](/theme) sizin için bir tane yazar.

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

## Açık ve koyu {#dark}

Tema, söylenmedikçe sistemin koyu modunu izler. Kendi düğmesi olan bir uygulama okurun seçimini verir; Android'de ve iOS'ta sistemin çubukları, klavyesi ve kontrolleri de onu izler: [Temayı izleyen sistem çubukları](/docs/guides/system-bars).

```kotlin App.kt
PantrixTheme(preset = Harbor, colors = rememberPantrixColors(Harbor, darkTheme = settings.dark)) { App() }
```

## Farklı görünen bir parça {#nested}

Bir başkasının içindeki `PantrixTheme` yalnızca kendisine verileni değiştirir, gerisini dıştakinden alır.

:::rows
- **Koyu bir parça**: Açık bir ekranda `PantrixTheme(darkTheme = true) { … }`. Zemini içindeki yüzey ya da karttır: tema zemin çizmez.
- **Sıkışık bir tablo**: `PantrixTheme(density = PantrixDensity.Compact) { … }`. Dokunma hedefi küçülmez.
- **Başka bir dil**: `PantrixTheme(locale = PantrixLocale.Arabic) { … }` Arapça konuşur ve sağdan sola okunur.
- **Başka bir platform**: `PantrixTheme(platform = PantrixPlatform.forType(PlatformType.Ios)) { … }` iOS'un görünüşünü her yerde çizer.
:::

## Bileşen stilleri {#styles}

Bir bileşenin ölçüleri ve biçimi onun stilidir; her platformda bir hazır stili vardır. Üstüne markanın ayarları, temanın stilleri, bir bölümünkiler ve en son bileşenin kendi `style`'ı gelir. Bir stil yalnızca değiştirdiğini anar.

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

## Sırada ne var {#next}

:::cards
- [Platformlar](/docs/platforms): Her platformun giriş noktası ve değişenler.
- [Formlar](/docs/forms): Alanlar, kurallar ve bir formun gönderilmesi.
- [Tipografi](/docs/guides/typography): Ölçek ve yazılanın boyu.
- [Material 3 köprüsü](/docs/guides/material3): Aynı renkleri konuşan iki tema.
:::
