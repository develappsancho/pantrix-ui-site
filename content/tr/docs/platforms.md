---
title: Platformlar
description: Her platformun giriş noktası aynı App()'i çizer ve tema onu platforma kendiliğinden uydurur. Platformdan platforma neyin değiştiği ve bir projenin her biri için ne eklediği.
section: Başlarken
---

## Tek uygulama, dört giriş noktası {#entry}

:::tabs
### Android

```kotlin MainActivity.kt
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}
```

### iOS

```kotlin MainViewController.kt
fun MainViewController(): UIViewController = ComposeUIViewController { App() }
```

### Masaüstü

```kotlin Main.kt
fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "My app") {
        App()
    }
}
```

### Web

```kotlin Main.kt
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(viewportContainerId = "app") {
        PantrixTypefaceGate(onShown = { document.getElementById("loading")?.remove() }) {
            App()
        }
    }
}
```
:::

## Temanın uydurdukları {#conventions}

:::table Platforma göre
| | Android | iOS | Masaüstü | Web |
| --- | --- | --- | --- | --- |
| Dokunma hedefi | 48dp | 44pt | İmleçle 32dp | 32dp, parmakla 48dp |
| Odak halkası | Klavye için | Klavye için | Klavye için | Klavye için |
| Bileşen stilleri | Android'inki | iOS'unki | Masaüstününkü | Web'inki |
| Dokunsal geri bildirim | Çalar | Çalar | Yok | Yok |
| İmleç biçimleri | Fareyle | Yok | Var | Var |
:::

`PantrixTheme.platform` hangi alışkanlıkların geçerli olduğunu söyler: `type`'ı, dokunmayla kullanılıp kullanılmadığı, en küçük dokunma hedefi. Bir telefonun tarayıcısı dokunmayla kullanılan web'dir; tema bunu kendisi bilir.

## Her platformda {#each}

:::rows
- **Android**: API 30 ve üstü. `enableEdgeToEdge()` uygulamanın sistem çubuklarının arkasına çizmesine izin verir, `PantrixScaffold` içeriğini onlardan uzak tutar. En dıştaki tema etiketleri Maestro'nun ve UI Automator'ın bulduğu kaynak kimlikleri yapar.
- **iOS**: Cihazlar ve Apple silikonda simülatör. Kütüphane iOS 27.1 SDK'sıyla derlenir ve katlanmanın köprüsünü getirir: projenin kendine ait bir cinterop'u gerekmez. Durum çubuğu, klavye ve kontroller temayı izler: [Sistem çubukları](/docs/guides/system-bars).
- **Masaüstü**: JVM. Pencerenin başlığı `PantrixWindowTitle` ile ekranı izler; bütün pencerenin kısayolu her sistem için bir kez yazılır: [Klavye kısayolları](/docs/guides/keyboard-shortcuts).
- **Web**: Wasm ve JS. `PantrixTypefaceGate` uygulamayı yazı tipi geldikten sonra çizer; yazı okurun gözü önünde hiç değişmez: [Web yazı tipleri](/docs/guides/web-fonts).
:::

:::callout
**Bir düzen cihazı değil pencereyi okur.** Bölünmüş ekrandaki bir tablet bir telefon genişliğindedir, bir masaüstü penceresi dar olabilir. `PantrixTheme.window` boyut sınıfını ve duruşunu verir: [Pencereye uyum](/docs/guides/adaptive).
:::

## Sırada ne var {#next}

:::cards
- [Formlar](/docs/forms): Alanlar, kurallar ve bir formun gönderilmesi.
- [Test](/docs/testing): Her makinede aynı ekran.
- [Pencereye uyum](/docs/guides/adaptive): Boyut sınıfları, paneller ve katlar.
- [Web yazı tipleri](/docs/guides/web-fonts): Geç gelen bir yazı tipi, giderilmiş.
:::
