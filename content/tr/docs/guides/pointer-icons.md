---
title: Boyutlandırma imleçleri
description: Compose Multiplatform'da sürüklenince bir şeyi büyüten ya da küçülten bir denetimin üstünde imlecin neden kendiliğinden değişmediği, kütüphanenin bunun için ne verdiği ve bunun nasıl sınandığı. Sorun Pantrix'e özgü değildir: iki panel arasında bir tutamacı, genişliği sürüklenen bir tablo kolonu ya da kenarından büyütülen bir paneli olan her Compose Multiplatform projesinde aynı şekilde görülür.
---

Compose hakkında burada söylenenler Compose Multiplatform 1.12.2'nin kaynaklarında (`PointerIcon.kt`, `PointerIcon.desktop.kt`, `PointerIcon.web.kt`, `PointerIcon.ios.kt`, `RootNodeOwner.skiko.kt`) ve Android'de `androidx.compose.ui`'nin `PointerIcon.android.kt`'sinde okunmuştur. Doğrulanamayanlar 7. bölümde ayrıca sayılır.

## 1. Sorun {#problem}

### Belirti

*   İki panel arasındaki tutamaç fareyle sürüklenir, panellerin genişliği değişir; ama tutamacın üstünde imleç hep oktur. Kullanıcı tutamacı ancak tesadüfen bulur.
*   Ortak kodda `PointerIcon.` yazınca dört seçenek çıkar: `Default`, `Crosshair`, `Text`, `Hand`. Çift yönlü bir ok yoktur.
*   Her platformun kendi boyutlandırma imleci vardır, ama her biri o platformun kendi türüyle ister: ortak koddan hiçbiri kurulamaz.

:::sketch pointer-icons
:::

### Neden

`PointerIcon` ortak kodda yalnızca bir arayüzdür; dört hazır değeri platformların `actual`'larıdır. Başka bir imleci kurmanın yolu her platformda başkadır:

:::table
| Platform | Compose'un hazır dördü | Başka bir imleç nasıl kurulur | Boyutlandırma imleci |
| :--- | :--- | :--- | :--- |
| Android | `AndroidPointerIconType(TYPE_ARROW)`, `TYPE_CROSSHAIR`, `TYPE_TEXT`, `TYPE_HAND` | `PointerIcon(pointerIconType: Int)` ya da `PointerIcon(android.view.PointerIcon)` | `TYPE_HORIZONTAL_DOUBLE_ARROW`, `TYPE_VERTICAL_DOUBLE_ARROW` |
| Masaüstü (JVM) | `AwtCursor(Cursor(Cursor.DEFAULT_CURSOR))` ve ötekiler | `PointerIcon(cursor: java.awt.Cursor)` | `Cursor.E_RESIZE_CURSOR`, `Cursor.N_RESIZE_CURSOR` |
| Web (`js`, `wasmJs`) | `BrowserCursor("default")`, `"crosshair"`, `"text"`, `"pointer"` | `PointerIcon.fromKeyword(keyword: String)`, `@ExperimentalComposeUiApi` | CSS'in `col-resize` ve `row-resize`'ı |
| iOS | `UIKitCursor("default")` ve ötekiler | Yok: sınıf `private` | Yok |
:::

İki ayrıntı işi büyütür:

1.  **iOS'ta Compose hiçbir imleci şekillendirmez.** Ortak sahne imleci `platformContext.setPointerIcon(…)`'a verir (`RootNodeOwner.skiko.kt`); bu çağrının varsayılanı boştur (`PlatformContext.skiko.kt`) ve iOS onu karşılamaz. Compose'un kendi dördü de iPad'in imlecini değiştirmez. iOS'un karşılığı ancak bir ad olarak yazılır.
2.  **Web'in kurucusu deneyseldir.** `fromKeyword` `@ExperimentalComposeUiApi` taşır; kullanan yer `@OptIn` ister. Verilen kelime tuvalin `style.cursor`'ına olduğu gibi yazılır (`ComposeWindowInternal.web.kt`): yanlış yazılan bir kelime hata vermez, imleç ok kalır.

## 2. Çözüm {#fix}

İki boyutlandırma imleci ortak kodda bir `expect` değeri olarak bildirilir, her platform kendi türüyle karşılar. Kullanan yer yalnızca ortak adı görür ve onu Compose'un `pointerHoverIcon` değiştiricisine verir. Değiştirici imleci, işaretçi düğümün üstüne girince gösterir, çıkınca bırakır.

## 3. Pantrix ile {#with}

```kotlin
import com.pantrixui.foundation.platform.PantrixPointerIcons

Box(
    Modifier
        .width(12.dp)
        .fillMaxHeight()
        .pointerHoverIcon(PantrixPointerIcons.ResizeHorizontal)
        .draggable(rememberDraggableState { by -> width += with(density) { by.toDp() } }, Orientation.Horizontal)
)
```

*   `PantrixPointerIcons.ResizeHorizontal`: yana sürüklenip bir şeyi genişleten ya da daraltan denetimin üstünde. İki panelin arasındaki tutamaç, bir tablo kolonunun ucu.
*   `PantrixPointerIcons.ResizeVertical`: yukarı aşağı sürüklenip bir şeyi uzatan ya da kısaltan denetimin üstünde. Bir panelin alt kenarı.

Kütüphanede kullanıldığı yerler:

:::table
| Bileşen | Nerede |
| :--- | :--- |
| `PantrixListDetailScaffold(resizable = true)` | Liste ile ayrıntı arasındaki tutamaç |
| `PantrixDataTable` | Genişliği değişen bir kolonun başlığının ucu; yalnızca fareyle kullanılan platformda, çünkü parmak bu şeridi sürüklemez |
| Grafiklerin gezgini (`ChartZoom(navigator = true)`) | Görünen parçanın iki ucu ve bir uç sürüklenirken. Yatay çubuk grafiğin dikey gezgininde `ResizeVertical` |
:::

### 3.1. konuma göre değişen imleç

Gezgin tek bir düğümdür: imleç düğümün her yerinde aynı değildir. Görünen parçanın ucunun üstünde boyutlandırma imleci, ortasında ve dışında oktur. Bunun için işaretçinin konumu dinlenir ve imleç bir durumdan okunur:

```kotlin
var hovered by remember { mutableStateOf<Grip?>(null) }
Box(
    Modifier
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    val x = event.changes.first().position.x
                    hovered = if (event.type == PointerEventType.Exit) null else gripUnder(x)
                }
            }
        }
        .pointerHoverIcon(if (held ?: hovered != null) PantrixPointerIcons.ResizeHorizontal else PointerIcon.Default)
)
```

Compose, imleç değişince işaretçi düğümün üstündeyse yenisini hemen gösterir (`HoverIconModifierNode.icon`'un atayıcısı). Sürüklenen uç (`held`) sürükleme bitene kadar tutulur: uç bir sınıra dayanıp işaretçi ondan uzaklaşsa da imleç boyutlandırma imleci kalır.

### 3.2. klavye ve ekran okuyucu

İmleç yalnızca fareyi tutana söyler. Gerçek bir tutamaç klavyeye ve ekran okuyucuya da açıktır: odak alır, ok tuşlarıyla adım adım, Home ve End ile uçlarına gider ve ekran okuyucuya ayarlanabilir bir değer olarak söylenir (`progressBarRangeInfo` ve `setProgress`). `PantrixListDetailScaffold`'un tutamacı ve `PantrixDataTable`'ın kolon ucu öyledir; demo'nun "Platform ve sistem" sayfasındaki örnek, imleci tek başına göstermek için bunları yazmaz.

## 4. Pantrix olmadan {#without}

### 4.1. ortak kod (`commonMain`)

```kotlin
package com.example.pointer

import androidx.compose.ui.input.pointer.PointerIcon

object ResizePointers {
    val Horizontal: PointerIcon get() = resizeHorizontal
    val Vertical: PointerIcon get() = resizeVertical
}

internal expect val resizeHorizontal: PointerIcon
internal expect val resizeVertical: PointerIcon
```

### 4.2. platform karşılıkları

`androidMain`:

```kotlin
internal actual val resizeHorizontal: PointerIcon = PointerIcon(android.view.PointerIcon.TYPE_HORIZONTAL_DOUBLE_ARROW)
internal actual val resizeVertical: PointerIcon = PointerIcon(android.view.PointerIcon.TYPE_VERTICAL_DOUBLE_ARROW)
```

`jvmMain`:

```kotlin
import java.awt.Cursor

internal actual val resizeHorizontal: PointerIcon = PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR))
internal actual val resizeVertical: PointerIcon = PointerIcon(Cursor(Cursor.N_RESIZE_CURSOR))
```

`webMain` (`js` ile `wasmJs`'nin ortak kaynak kümesi; `fromKeyword` ikisinde de aynı bildirimdir):

```kotlin
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.pointer.fromKeyword

@OptIn(ExperimentalComposeUiApi::class)
internal actual val resizeHorizontal: PointerIcon = PointerIcon.fromKeyword("col-resize")

@OptIn(ExperimentalComposeUiApi::class)
internal actual val resizeVertical: PointerIcon = PointerIcon.fromKeyword("row-resize")
```

`iosMain`:

```kotlin
internal actual val resizeHorizontal: PointerIcon = PointerIcon.Default
internal actual val resizeVertical: PointerIcon = PointerIcon.Default
```

Web'de `ew-resize` ve `ns-resize` de olur. CSS `col-resize` ile `row-resize`'ı bir kolonun ya da satırın boyutunun değişeceğini söyleyen imleç olarak tanımlar; iki panelin arasındaki tutamaç ve bir kolonun ucu tam bu iştir.

## 5. Test etmek {#testing}

İmleç anlambilimde (semantics) yoktur: bir testte `SemanticsProperties` ile okunamaz. Ama `pointerHoverIcon`'un ürettiği öğe bir `data class`'tır (`PointerHoverIconModifierElement`), eşitliği imlecinkidir; düğümün değiştiricileri arasında aranabilir:

```kotlin
fun SemanticsNodeInteraction.showsPointer(icon: PointerIcon): Boolean =
    fetchSemanticsNode().layoutInfo.getModifierInfo().any { it.modifier == Modifier.pointerHoverIcon(icon) }

onNodeWithTag("ListDetailHandle_mail").showsPointer(PantrixPointerIcons.ResizeHorizontal)
```

Bu, değiştiricinin etiketle aynı düğümde olmasını ister: `pantrixTestTag` ile `pointerHoverIcon` aynı `Box`'ta. Konuma göre değişen bir imleç için önce fare oraya götürülür:

```kotlin
onNodeWithTag(NAVIGATOR).performMouseInput { moveTo(Offset(x, centerY)) }
waitForIdle()
assertTrue(onNodeWithTag(NAVIGATOR).showsPointer(PantrixPointerIcons.ResizeHorizontal))
```

Platformun gerçek imleci de sınanır: her platformun testi `PantrixPointerIcons`'ın o platformun beklenen imleci olduğuna bakar. Masaüstünde `PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR))`'a eşittir (`AwtCursor` eşitliği imlecin türüyle kurar), Android'de `PointerIcon(TYPE_HORIZONTAL_DOUBLE_ARROW)`'a, web'de `PointerIcon.fromKeyword("col-resize")`'a, iOS'ta `PointerIcon.Default`'a.

## 6. Tuzaklar {#pitfalls}

*   **Parmak.** Dokunmatik bir ekranda imleç yoktur; değiştirici bir şey yapmaz. Dar bir şerit parmak için küçükse (`PantrixDataTable`'ın kolon ucu gibi) dokunmada sürükleme hiç kurulmaz, imleç de verilmez.
*   **Android'de fare.** Android imleci yalnızca bir fare ya da dokunmatik yüzey varken çizer: ChromeOS, Samsung DeX, fare bağlı bir tablet. Emülatörde bilgisayarın faresi çoğu zaman dokunma olarak gelir ve imleç görünmez.
*   **Bir ata önce gelir.** `pointerHoverIcon(…, overrideDescendants = true)` olan bir ata, altındaki her imleci bastırır.
*   **Sürüklerken düğümden çıkmak.** İmleç Compose'un `Enter` ve `Exit` olaylarına göre gösterilip bırakılır. Basılı bir işaretçi düğümün dışına çıkınca `Exit`'in ne zaman geldiği doğrulanmadı (7. bölüm); gezgin, sürüklenen ucu kendisi tuttuğu için bundan etkilenmez.
*   **Masaüstünde her sistem kendi çizer.** `E_RESIZE_CURSOR` bir türdür; onu sistemin hangi resmiyle çizeceğine AWT karar verir.

## 7. Doğrulama {#verification}

### Testler

:::table
| Ne | Nerede | Hedef |
| :--- | :--- | :--- |
| Her platformun imleci | `ui/foundation/src/{jvmTest,androidHostTest,webTest,iosTest}/…/platform/PointerIconsOn…Test.kt` | JVM, Android, JS, Wasm, iOS |
| Tutamacın imleci | `ListDetailScaffoldTest.the_handle_makes_the_list_wider_and_narrower` | Ortak testler: JVM, iOS, JS, Wasm |
| Kolon ucunun imleci, parmakta imleç yok | `DataTableTest.the_end_of_a_resizable_header_is_dragged_to_a_width`, `a_finger_does_not_drag_a_header_s_end_and_a_keyboard_still_moves_it` | Ortak testler |
| Gezginde uca göre değişen imleç, sürüklerken kalması | `ChartZoomToolsTest.a_pointer_over_an_end_of_the_part_in_view_is_one_that_resizes`, `ChartTurnedZoomTest.the_part_in_view_is_dragged_down_the_navigator_of_a_horizontal_chart` | Ortak testler |
:::

### Elle bozulan davranışlar

Yedi mutant elle denendi, yedisi de testlerle öldü: tutamaçtan ve kolon ucundan değiştiricinin silinmesi, gezginin konumu dinlememesi, dikey gezginin yatay imleç vermesi, uç yakınlığının sıfırlanması, sürüklenen ucun imlece katılmaması ve sürükleme bitince bırakılmaması.

### Doğrulanmayanlar

*   İmlecin ekranda nasıl göründüğü: masaüstünde macOS, Windows ve Linux'ta, tarayıcıda ve Android'de fareyle. Testler Compose'a hangi imlecin verildiğini sınar, sistemin onu nasıl çizdiğini değil.
*   Basılı bir işaretçi düğümün dışına çıkınca Compose'un `Exit`'i hemen mi, bırakılınca mı gönderdiği.

## 8. Bu depoda nerede {#repository toc="8. Depoda"}

*   API: `ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/platform/PantrixPointerIcons.kt`, platform karşılıkları aynı paketin `androidMain`, `jvmMain`, `webMain` ve `iosMain` dosyaları.
*   Kullananlar: `container/listdetail/ListDetailScaffold.kt` (`PaneHandle`), `data/datatable/DataTableHeader.kt` (`Grip`), `chart/cartesian/PlotNavigator.kt` ve `NavigatorHand.kt` (`gripUnder`, `held`).
*   Testteki yardımcı: `ui/components/src/commonTest/kotlin/com/pantrixui/components/TestSupport.kt`, `showsPointer`.
*   Demo: "Platform ve sistem" sayfasının "Yeniden boyutlandırma imleci" örneği (`app/shared/…/samples/PlatformSamples.kt`).
