---
title: Giriş
description: Pantrix UI, Compose Multiplatform için bir tasarım sistemi ve uyarlanabilir arayüz kütüphanesidir: Android, iOS, masaüstü ve web için tek bir Kotlin kod tabanı; Compose Foundation üzerine kurulu, Material bağımlılığı yok.
section: Başlarken
---

## Ne olduğu {#what}

:::facts
- Bileşenler: {{components}}, {{groups}} grupta
- Hedefler: Android, iOS, JVM, Wasm, JS
- Bağımlılık: yalnızca Compose Foundation
- Diller: İngilizce, Türkçe, Arapça
- Lisans: Apache 2.0
:::

Tek bir tema her platformun kendi alışkanlıklarını izler: dokunma hedeflerini, odak halkalarını, bileşenlerinin geometrisini. Bir düğme Android'de parmağa 48dp, iOS'ta 44pt, masaüstünün imlecinin altında 32dp yüksekliktedir ve ekranın kodunda bunu söyleyen hiçbir şey yoktur.

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

## İçinde ne var {#modules}

:::table Modüller
- `com.pantrixui:components`: {{components}} bileşen: öğeler, girdiler, tarihler, kaplar, gezinme, katmanlar, veri ve grafikler. `foundation` ve `annotations`'ı da getirir
- `com.pantrixui:foundation`: Tema ve hazır ayarları, token'lar, pencere ve katları, diller ve platformun hizmetleri: pano, dosyalar, kısayollar, dokunsal geri bildirim
- `com.pantrixui:testing`: Bir uygulamanın testleri için: etiketle bulmak, her makinede aynı olan bir tema, platform hizmetlerinin yerine geçenler
- `com.pantrixui:material3`: Material 3'e iki yönlü köprü, ekran ekran geçen bir uygulama için
- `com.pantrixui:detekt-rules`: Bir uygulamanın uyduğu kurallar, kendi detekt'i için
- `com.pantrixui:mcp`: Pantrix'i bir kodlama asistanına öğreten MCP sunucusu ve belgeler
:::

## Nelere uyar {#principles}

:::rows
- **Material yok**: Kütüphane yalnızca Compose Foundation'a dayanır. Material 3'ü tutan bir uygulama iki temayı `com.pantrixui:material3` ile köprüler.
- **Cihaz değil pencere**: Bir düzen, çalıştığı cihazı değil çizildiği pencereyi okur: boyut sınıfını, duruşunu, katlarını.
- **Her kontrole bir etiket**: Her kontrol `tagKey`'inden `Component_key` diye etiketlenir, her dilde aynı: bir test ve bir Maestro akışı onu bununla bulur.
- **Sesli okunur**: Her bileşen ekran okuyucuya ne olduğunu, ne taşıdığını ve ne yaptığını söyler.
- **Sol ve sağ değil, baş ve son**: Her bileşen sağdan sola okunan bir dilde kendiliğinden döner.
:::

## Sırada ne var {#next}

:::cards
- [Kurulum](/docs/installation): Depo, bağımlılık ve tema.
- [Tema](/docs/theming): Renk rolleri, yazı, şekiller ve bir markanın hazır ayarı.
- [Platformlar](/docs/platforms): Her platformun giriş noktası neyi çizer, ne değişir.
- [Rehberler](/docs/guides): Her uygulamanın karşılaştığı sorunlara yanıtlar.
:::
