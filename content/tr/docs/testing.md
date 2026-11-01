---
title: Test
description: Bir uygulamanın arayüz testinin Pantrix'in bileşenlerini etiketleriyle nasıl bulduğu, ekranı her makinede aynı nasıl çizdiği, makinenin hizmetlerini işin dışında nasıl tuttuğu ve aynı etiketlerle bir cihaza nasıl ulaştığı.
section: Başlarken
---

## Metin değil etiket {#tags}

Her bileşen, adından ve kendisine verilen `tagKey`'den kurulan, her dilde aynı bir etiket taşır. Bir test ya da bir Maestro akışı onu bununla bulur; bir sözcük çevrildiğinde bozulmaz.

:::table Bir etiket nasıl kurulur
- `PantrixButton(tagKey = "pay")`: `Button_pay`
- `TestScope("Checkout") { … }`: `Checkout.Button_pay`; bölümler iç içe geçer: `Checkout.Payment.Button_pay`
- `form.field("email", …)`: `TextField_email`: bağlı bir bileşen alanın `id`'siyle etiketlenir
- `PantrixSelect(tagKey = "country")`: `Select_country.SelectOption_tr`: bir kontrolün parçaları etiketinin içinde adlanır
:::

## Bir test {#test}

Yardımcılar `com.pantrixui:testing`'dedir, her hedefte.

```kotlin CheckoutScreenTest.kt
class CheckoutScreenTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun paying_reports_the_press_and_the_screen_has_no_findings() = runComposeUiTest {
        val findings = PantrixFindings()
        val analytics = RecordingAnalytics()
        setContent {
            PantrixTestTheme(findings = findings, analytics = analytics) { CheckoutScreen() }
        }
        onPantrixNode("TextField", "card_number").requestFocus().performTextInput("4242424242424242")
        onPantrixNode("Button", "pay").performClick()
        assertEquals(listOf("click"), analytics.eventsOf("Button").map { it.action })
        assertNoFindings(findings)
    }
}
```

## Her makinede aynı ekran {#theme}

`PantrixTestTheme` bir ekranın makineden alacaklarını sabitler: platformu, açık ya da koyu oluşu, dili, yoğunluğu, yazının ölçeğini, hareketi. Platform hizmetlerinin yerine geçenleri parametre olarak alır.

:::table Yerine geçenler
- `RecordingAnalytics()`: Bileşenlerin olayları, sırasıyla
- `RecordingHaptics()`: Çalınan dokunsal geri bildirimler
- `TestClipboard()`: Ekranın kopyaladıkları, bir de başka bir platformun panosu
- `TestFilePicker(files)`: Her seçime verilen dosyalarla yanıt verir
- `TestClock(now, utcOffset)`: Ekranın saati; yalnızca test onu ilerlettiğinde ilerler
- `PantrixFindings()`: Tanılamanın buldukları, `assertNoFindings` için
:::

:::callout
**Bir tarih iki farkla okunur.** Tarih gösteren bir ekranın testi ona bir `TestClock` verir ve onu UTC'den iki farkla okur: tek bir fark makinenin kendisininki olabilir.
:::

## Tek testte her platform {#platforms}

`onEachPlatform` ekranı her platformun alışkanlıklarıyla birer kez çizer. `assertTouchTargetOf(platform)` Android'de 48dp'yi, iOS'ta 44pt'yi, fareyle 32dp'yi denetler.

```kotlin SaveButtonTest.kt
@OptIn(ExperimentalTestApi::class)
@Test
fun the_button_is_a_finger_s_size_on_every_platform() = runComposeUiTest {
    onEachPlatform({ platform -> PantrixTestTheme(platform = platform) { SaveButton() } }) { platform ->
        onPantrixNode("Button", "save").assertTouchTargetOf(platform)
    }
}
```

## Bir cihazda {#device}

Etiketler cihazın erişilebilirlik ağacına ulaşır: Android'de `resource-id`, iOS'ta `accessibilityIdentifier` olarak. Uygulamanın kendi `Popup`'ı ya da `Dialog`'u kökünde `Modifier.pantrixAutomationRoot()` taşır.

```yaml checkout.yaml
appId: ${APP_ID}
---
- launchApp
- tapOn:
    id: "Button_browse"
- tapOn:
    id: "TextField_search"
- inputText: "invoice"
```

## Sırada ne var {#next}

:::cards
- [Bir ekranı test etmek](/docs/guides/testing): Her yardımcı ve bir testin neden makineden makineye değiştiği.
- [Çalışma anı tanılaması](/docs/guides/diagnostics): Yalnızca çalışan bir ekranda görülen kusurlar.
- [Uygulamanızda detekt kuralları](/docs/guides/detekt): Bir makinenin tutabileceği kurallar.
- [Formlar](/docs/forms): Bir formun alanları ve etiketleri.
:::
