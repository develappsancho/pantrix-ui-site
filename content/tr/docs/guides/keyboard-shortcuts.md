---
title: Klavye kısayolları
description: Compose Multiplatform'da bütün pencereye ait bir klavye kısayolunun (Cmd+K ya da Ctrl+K gibi) neden tek bir onPreviewKeyEvent ile çalışmadığı, kütüphanenin bunun için ne verdiği, bir kısayolun her sistemde nasıl yazıldığı ve bunun nasıl sınandığı. Sorun Pantrix'e özgü değildir: ortak kodunda bir uygulama kısayolu olan her Compose Multiplatform projesinde aynı şekilde görülür.
---

Compose hakkında burada söylenen her şey Compose Multiplatform 1.12.1'in kaynaklarında okunmuş ya da bu depoda koşan bir testte, bir cihazda veya tarayıcıda görülmüştür; hangisinin nerede görüldüğü 7. ve 8. bölümlerde yazılıdır.

## 1. Sorun {#problem}

### Belirti

*   Uygulamanın köküne konan `Modifier.onPreviewKeyEvent { … }` kısayolu yalnızca bir şey odaktayken duyar. Uygulama yeni açılmışken, ya da bir alandan odak kalkmışken, Cmd+K hiçbir şey yapmaz.
*   Bir kısayol Apple'da Command ile, başka yerde Control ile başlar: `isMetaPressed` ile `isCtrlPressed`'ten hangisine bakılacağı sisteme göre değişir. Masaüstünde ve tarayıcıda hangi sistemde olunduğu `PlatformType`'tan anlaşılmaz.
*   Kısayolu `KeyDown`'da çalıştırıp `true` dönen bir dinleyici, tuşun kalkışını (`KeyUp`) odaktaki kontrole bırakır. Enter'ı `KeyUp`'ta basan bir düğme, kendi başlamadığı bir basışın sonunu görür.
*   Kısayolun yazılışı da sisteme göre değişir: Mac'te `⇧⌘K`, Windows'ta ve Linux'ta `Ctrl+Shift+K`.

:::sketch keyboard-shortcuts
:::

### Neden

Bir tuş olayı, odaktaki düğüme ve onun atalarına verilir. Hiçbir şey odakta değilken Compose olayı, kök odak düğümünün **üstündeki** dinleyicilere verir: içerikteki hiçbir dinleyici onlardan biri değildir. `FocusOwnerImpl.dispatchKeyEvent` (Compose Multiplatform 1.12.1, `commonMain/androidx/compose/ui/focus/FocusOwnerImpl.kt`):

```kotlin
val activeFocusTarget = findFocusTargetNode()
val focusedKeyInputNode =
    activeFocusTarget?.lastLocalKeyInputNode()
        ?: activeFocusTarget?.nearestAncestorIncludingSelf(Nodes.KeyInput)?.node
        ?: rootFocusNode.nearestAncestor(Nodes.KeyInput)?.node
```

`rootFocusNode.nearestAncestor(…)` platformun kendi düğümleridir; uygulamanın kökündeki `onPreviewKeyEvent` onların altındadır ve atlanır. Bir kısayolun her zaman duyulması için, hiçbir şey odakta değilken kökün kendisinin odakta olması gerekir.

Bunu yapan bir düğüm de iki şeye dikkat etmelidir:

*   **Tab.** Odakta olan bir ata, Tab'ın tek boyutlu aramasında bir duraktır. Kök odaktayken Tab'a basılınca da Compose aramayı odaktakinin kardeşlerinde yapar; kökün kardeşi yoktur ve odak kökte kalır.
*   **Başa dönüş.** Tab son kontrolden ilkine dönerken Compose önce odağı bırakır, sonra ilk kontrolü arar (`FocusOwnerImpl.moveFocus`: `clearFocus(…)` sonra `takeFocus(focusDirection, …)`). Odak bırakıldığı anda kendini odaklanabilir yapan bir kök, o aramada "ilk" olur.

## 2. Çözüm: üç parça {#fix}

:::table
| Parça | Ne yapar |
| :--- | :--- |
| `PantrixShortcut(key, command, shift, alt)` | Bir tuş ve değiştiriciler. `command` Apple'da Command, ötekilerde Control'dür. `isPressedBy(event, system)`: tuşu inerken tam bu değiştiriciler basılı mı, başkası değil. `keysOn(system)`, `writtenOn(system)`: sistemin yazdığı gibi. |
| `Modifier.onShortcut(shortcut) { … }` | Odak bu düğümdeyken ya da içindeyken kısayolu, odaktaki kontrolden önce duyar (`onPreKeyEvent`). Çalıştırdığı tuşun kalkışını da yutar. |
| `Modifier.shortcutRoot()` | İçinde hiçbir şey odakta değilken odağı tutar; Tab için durak değildir. |
:::

`LocalOperatingSystem` (`foundation/platform`), kısayolun hangi sistemde basıldığını söyler: varsayılanı `currentOperatingSystem()`'dır; bir test başka bir sistem verir.

## 3. Pantrix ile {#with}

### 3.1. Bütün pencerenin kısayolu

```kotlin
var palette by rememberSaveable { mutableStateOf(false) }
Box(Modifier.shortcutRoot().onShortcut(PantrixShortcut(Key.K, command = true)) { palette = true }) {
    App()
    PantrixCommandPalette(visible = palette, onDismissRequest = { palette = false }, …)
}
```

`shortcutRoot` pencerenin kökünde bir kez durur. Kısayollar onunla aynı düğümde ya da onun üstünde olmalıdır: odak kökteyken yalnızca kök ve atalarının dinleyicileri duyar.

### 3.2. Komutların kendi kısayolları

`Modifier.commandShortcuts(commands, onRun)` listedeki her çalıştırılabilir komutun kısayolunu bağlar. Paletin gösterdiği ile klavyenin çalıştırdığı aynı listedir:

```kotlin
Box(
    Modifier
        .shortcutRoot()
        .onShortcut(PantrixShortcut(Key.K, command = true)) { palette = true }
        .commandShortcuts(commands, onRun = { run(it.key) })
) { … }
```

`enabled = false` olan bir komut tuşunu geçirir; aynı kısayolu taşıyan iki komuttan ilki çalışır. Bir Pantrix modal'ı açıkken tuşlar onundur: altındaki kısayollar duyulmaz.

### 3.3. Kısayolun yazılışı

```kotlin
val shortcut = PantrixShortcut(Key.R, command = true, shift = true)
shortcut.writtenOn(OperatingSystem.MacOs)   // "⇧⌘R"
shortcut.writtenOn(OperatingSystem.Windows) // "Ctrl+Shift+R"
shortcut.keysOn(LocalOperatingSystem.current) // tuş kapakları için, birer birer
```

Apple'ın sırası Option, Shift, Command ve tuştur; ötekilerinki Ctrl, Alt, Shift ve tuş. Harflerin, rakamların, okların, F1–F12'nin, Enter'ın, Escape'in, Tab'ın, boşluğun, Backspace'in, Delete'in, Home ile End'in, sayfa tuşlarının ve yaygın işaretlerin adı vardır; başka bir tuş `#numarası` diye yazılır.

## 4. Pantrix olmadan {#without}

Kökün düğümü, `ShortcutRootNode` (`ui/foundation/.../keyboard/Shortcuts.kt`), kütüphanesiz şöyle kurulur:

```kotlin
fun Modifier.shortcutRoot(): Modifier = this then ShortcutRootElement

private object ShortcutRootElement : ModifierNodeElement<ShortcutRootNode>() {
    override fun create() = ShortcutRootNode()
    override fun update(node: ShortcutRootNode) = Unit
    override fun hashCode() = "shortcutRoot".hashCode()
    override fun equals(other: Any?) = other === this
}

private class ShortcutRootNode : DelegatingNode(), KeyInputModifierNode, CompositionLocalConsumerModifierNode {
    private val target = delegate(
        FocusTargetModifierNode(focusability = Focusability.Always, onFocusChange = { _, state -> changed(state) })
    )

    override fun onAttach() = takeIfNothingHasIt()

    override fun onPreKeyEvent(event: KeyEvent) = false

    // Kök odaktayken Tab'ı kendisi alır ve içindekilere götürür.
    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.type != KeyEventType.KeyDown || event.key != Key.Tab || !target.focusState.isFocused) return false
        val focus = currentValueOf(LocalFocusManager)
        focus.moveFocus(FocusDirection.Enter)
        if (event.isShiftPressed) focus.moveFocus(FocusDirection.Previous)
        return true
    }

    private fun changed(state: FocusState) {
        // İçeride bir şey odaktayken kök Tab'ın durağı değildir; başa dönüş odağı bırakıp ilkini aradığı için de
        // kök, odaklanabilirliğini ancak her şey yatıştıktan sonra geri alır.
        if (state.hasFocus && !state.isFocused) target.focusability = Focusability.Never
        if (!state.hasFocus) takeIfNothingHasIt()
    }

    private fun takeIfNothingHasIt() {
        coroutineScope.launch {
            if (!target.focusState.hasFocus) {
                target.focusability = Focusability.Always
                target.requestFocus()
            }
        }
    }
}
```

Eşleşme kuralı tek satırdır; sistem için `navigator.platform` (tarayıcı) ya da `os.name` (JVM) okunur:

```kotlin
val lead = if (apple) event.isMetaPressed else event.isCtrlPressed
val stray = if (apple) event.isCtrlPressed else event.isMetaPressed
val pressed = event.type == KeyEventType.KeyDown && event.key == key && lead == command && !stray &&
    event.isShiftPressed == shift && event.isAltPressed == alt
```

## 5. Yardımcının yapmadıkları {#limits toc="5. Yapmadıkları"}

*   **Uygulama odakta değilken.** Kısayol yalnızca uygulamanın penceresi odaktayken duyulur; sistem genelinde bir kısayol (uygulama arkadayken) yoktur.
*   **Sıralı kısayollar.** "G, sonra I" gibi iki basışlı kısayollar yoktur.
*   **Basılı tutma.** Tuş basılı tutulup yinelenince kısayol her yinelemede yeniden çalışır; yinelemeyi ayıran ortak bir işaret yoktur.
*   **Çakışma.** İki kısayolun aynı tuşu aldığı bildirilmez: dıştaki çalışır, `commandShortcuts`'ta listedeki ilki.
*   **Tarayıcının kendi kısayolları.** Ctrl+W, Ctrl+T, Cmd+Q gibi tarayıcının ya da sistemin tuttuğu kısayollar sayfaya hiç gelmez.
*   **iPad'in kısayol listesi.** iPad'de ⌘ basılı tutulunca çıkan liste (`UIKeyCommand`) doldurulmaz.

## 6. Test etmek {#testing}

```kotlin
setContent {
    CompositionLocalProvider(LocalOperatingSystem provides OperatingSystem.MacOs) {
        Box(Modifier.testTag("field").onShortcut(PantrixShortcut(Key.K, command = true)) { runs++ }.focusable())
    }
}
onNodeWithTag("field").requestFocus()
onNodeWithTag("field").performKeyInput { withKeyDown(Key.MetaLeft) { pressKey(Key.K) } }
```

*   Sistemi testte `LocalOperatingSystem` ile ver: testin koştuğu makineye bırakılan bir test, Mac'te ve Linux'ta başka tuş bekler.
*   Kökün odakta olup olmadığı, `shortcutRoot`'tan **önce** konan bir `Modifier.onFocusChanged { … }` ile okunur: kökün kendi anlambilimi yoktur, `assertIsFocused` onu göremez.
*   Odak bırakmayı `LocalFocusManager.current.clearFocus()` ile, `runOnIdle { … }` içinde dene.

## 7. Tuzaklar {#pitfalls}

*   **Tarayıcıda ilk tıklama.** Sayfa açılınca DOM odağı Compose'un öğesinde değildir; ilk tıklamaya kadar tuşlar Compose'a hiç gelmez, kök odakta olsa da. Bu tarayıcının sınırıdır; `shortcutRoot` ondan sonrasını çözer (bu depoda tarayıcıda görüldü: kök odaktaydı, tuş olayı kısayol düğümüne tıklamadan sonra geldi).
*   **Değiştiricisi ayrıca basılmayan yapay olaylar.** Bir otomasyon aracı Cmd+K'yı yalnızca `metaKey: true` taşıyan bir K olarak gönderirse Compose web onu Meta'yla basılmış saymaz: değiştiricileri kendi gördüğü tuş inişlerinden okur. Önce Meta'nın, sonra K'nın inişi gönderilince kısayol çalışır. Gerçek bir klavyede sorun yoktur.
*   **`focusProperties` sırası.** `Modifier.focusProperties { canFocus = false }` yalnızca kendisinden **sonra** gelen odak hedefini etkiler. `.clickable(…).focusProperties { … }` tıklanabilirin odak hedefini kapatmaz; önce `focusProperties`, sonra `clickable` yazılmalıdır (komut paletinin satırlarında bulundu: odak alana değil ilk satıra gidiyordu).
*   **Modal içinde odak istemek.** Bir modal'ın içeriğinde `LaunchedEffect` içinde hemen istenen odak sessizce başarısız olur: pencere içeriğini yerleştirmeden önce bir alan odak almaz. Bir kare beklenir (`withFrameNanos {}`), Pantrix'in modal'ı da öyle yapar.

## 8. Doğrulama {#verification}

### Testler

:::table
| Test | Ne tutar | Hedefler |
| :--- | :--- | :--- |
| `foundation/.../keyboard/ShortcutsTest` | Yazılış (Apple ve ötekiler, her adlı tuş), eşitlik, tam değiştiricilerle basılma, odaktakine verilmeyen iniş ve kalkış, dıştakinin kazanması, kökün başta ve odak bırakılınca odağı alması, Tab'ın kökü atlaması | JVM, iOS simülatörü, JS, Wasm |
| `components/.../CommandShortcutsTest` | Her komutun kısayolu, çalıştırılamayanın tuşu geçirmesi, ilkinin kazanması | JVM, iOS, JS, Wasm |
| `components/.../CommandPaletteTest` | Paletin satırlarında kısayolun sistemin yazdığı gibi durum olarak okunması | JVM, iOS, JS, Wasm |
:::

Android'in ana makine testleri `commonTest`'i görmez (bkz. ROBORAZZI_GUIDE.md).

### Elle bakılanlar

*   **Android emülatörü:** uygulama açılır açılmaz, hiçbir şeye dokunmadan `adb shell input keycombination KEYCODE_CTRL_LEFT KEYCODE_K` demo'nun paletini açtı.
*   **Tarayıcı (Chrome, macOS):** bir tıklamadan sonra Cmd+K (önce Meta'nın inişiyle) paleti açtı.

### Doğrulanmayanlar

*   iOS'ta donanım klavyesiyle Cmd+K: simülatörde Cmd+K, simülatörün kendi "yazılım klavyesini aç/kapa" kısayoludur ve uygulamaya gelmez.
*   Masaüstü uygulaması elle.
*   Bir ekran okuyucunun kısayolları seslendirmesi.

## 9. Bu depoda nerede {#repository toc="9. Depoda"}

:::table
| Dosya | İçinde |
| :--- | :--- |
| `ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/keyboard/PantrixShortcut.kt` | `PantrixShortcut`, tuş adları |
| `ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/keyboard/Shortcuts.kt` | `onShortcut`, `shortcutRoot` |
| `ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/platform/OperatingSystem.kt` | `OperatingSystem`, `LocalOperatingSystem` |
| `ui/components/src/commonMain/kotlin/com/pantrixui/components/overlay/commandpalette/CommandShortcuts.kt` | `commandShortcuts` |
| `app/shared/src/commonMain/kotlin/com/pantrixui/cmp/demo/shell/DemoCommands.kt` | Demo'nun kökü ve kendi paleti |
:::

## 10. Açık kalanlar {#open}

`docs/OPEN_ITEMS.md`'de: sıralı kısayollar, basılı tutmanın yinelemesi, iPad'in kısayol listesi ve iOS'ta donanım klavyesiyle doğrulama.
