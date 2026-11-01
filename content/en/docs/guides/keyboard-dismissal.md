---
title: Putting the keyboard away
description: Why a field’s software keyboard cannot be put away on an iPhone in Compose Multiplatform, what the library gives for it, and how it is tested. The problem is not Pantrix’s own: every screen with a field of several lines, or of numbers, has it.
---
## 1. The problem {#problem}

:::aside keyboard-dismissal
### What is seen

- On an iPhone the key at the keyboard’s bottom right starts a new line. No key puts the keyboard away, and a tap outside the field does not either: the “Send” button under it cannot be reached.
- The number pad has neither an action key nor a key that closes it.
- On Android the back gesture closes the keyboard; a tap outside still does not. On a desktop and on the web a click on empty room leaves the focus in the field.
- No unit test shows it: the keyboard is the system’s. Maestro’s `hideKeyboard` cannot put it away from a Compose field on iOS either.
:::

:::callout why
**Why.** In a native iOS app the screen puts the keyboard away, not the field: a tap ends the view’s editing (`endEditing`), or a scroll takes the keyboard down. In Compose the keyboard stays with the focused field, and Compose does not let the focus go on a tap outside: a tap that nothing takes goes nowhere.
:::

## 2. The fix {#fix}

A listener for taps goes on the screen’s root, under everything it shows. On a tap that **nothing under it takes**, it clears the focus (`FocusManager.clearFocus()`): the field gives its focus up, and the keyboard goes with it.

:::table Who takes the press
| The user | Compose | What follows |
| --- | --- | --- |
| A tap on a button | The button takes it | It does its work; the focus stays where it was |
| A tap in the field | The field takes it | The caret moves; the keyboard stays |
| A drag that scrolls | What scrolls takes it | The focus stays |
| A tap on empty room {highlight} | Nothing takes it | `clearFocus()`: the keyboard goes |
:::

## 3. With Pantrix {#with}

One modifier, in `com.pantrixui.foundation.keyboard`, on the screen’s root or the app’s:

```kotlin ClearFocusOnTap.kt
public fun Modifier.clearFocusOnTap(): Modifier
```

```kotlin CommentScreen.kt
Column(Modifier.fillMaxSize().clearFocusOnTap()) {
    PantrixTextArea(state = comment, label = stringResource(Res.string.comment))
    PantrixButton(text = stringResource(Res.string.send), onClick = ::send)
}
```

:::rows
- iOS: The way the keyboard is put away.
- Android: A way beside the system’s back gesture.
- Desktop and web: A click on empty room lets the focus go, as in a browser.
:::

## 4. Without Pantrix {#without}

A modifier node with `pointerInput` and `LocalFocusManager`, in common code. A project that would rather not write a node does the same inside a composable.

:::tabs commonMain
### A modifier node

```kotlin
fun Modifier.clearFocusOnTap(): Modifier = this then ClearFocusOnTapElement

private data object ClearFocusOnTapElement : ModifierNodeElement<ClearFocusOnTapNode>() {
    override fun create() = ClearFocusOnTapNode()
    override fun update(node: ClearFocusOnTapNode) = Unit
}

private class ClearFocusOnTapNode : DelegatingNode(), CompositionLocalConsumerModifierNode {
    init {
        delegate(SuspendingPointerInputModifierNode {
            detectTapGestures(onTap = { currentValueOf(LocalFocusManager).clearFocus() })
        })
    }
}
```

### Inside a composable

```kotlin
val focusManager = LocalFocusManager.current
Column(
    Modifier
        .fillMaxSize()
        .pointerInput(focusManager) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
) {
    // the screen's content
}
```
:::

:::note
`detectTapGestures` is in `androidx.compose.foundation.gestures`; the node’s types are in `androidx.compose.ui.node` and `…ui.input.pointer`.
:::

## 5. What the helper does not do {#limits toc="5. What it does not do"}

:::points
- **It does not take the keyboard down on a scroll.** iOS’s `keyboardDismissMode` has no counterpart here: a scroll leaves the focus where it is.
- **It puts no “Done” bar above the keyboard.** That is native iOS’s other way (`inputAccessoryView`). It was not tried here.
- **It does not hear the taps of a layer with a window of its own.** A `Popup` or `Dialog` that holds a field takes the modifier on its own content too.
- **It does not take the focus back from a button that takes it.** On a desktop and on the web a pressed button may take the focus itself: that is the button’s doing.
:::

## 6. Testing {#testing}

:::boxes
### A unit test, on five targets

`ClearFocusOnTapTest`: a tap on empty room takes the focus away; a tap in the field, by a finger or a mouse, keeps it; a button that takes no focus does its work; a drag that scrolls keeps the focus.

### Maestro, in place of `hideKeyboard`

On a screen whose root has the modifier, a step that taps empty room does the same.

```yaml
- tapOn: "A note for the courier"  # empty room: the keyboard goes
- tapOn:
    id: "Send"
```
:::

:::note
That the keyboard really goes is only seen on a device or a simulator.
:::

## 7. Verification {#verification}

:::checks
### Seen on a simulator, an emulator and the web {seen}

- iPhone 18 Pro Max simulator, iOS 27.0: the keyboard’s bottom-right key started a new line; a tap on the description closed the keyboard, a tap in the field opened it again.
- Android emulator, Pixel 7 Pro, API 33: a tap on the description closed the keyboard (`mInputShown=false`); a second tap in the field kept it open and moved the caret.
- Web, Chrome, Wasm: a click on empty room brought the field’s border back to rest; a click in the field kept the focus.

### Read in the sources {read}

- `detectTapGestures` waits for the first press and does not count one that was taken (foundation 1.12.2, TapGestureDetector.kt).
- A text field takes its press in `detectTapAndPress`, on every platform (TextFieldSelectionState).

### Not checked {unchecked}

- An iPad, and an iPhone with a hardware keyboard.
- A real device, and older versions of iOS.
- The web in a phone’s browser.
:::

## 8. Where it is in the repository {#repository toc="8. In the repository"}

:::labels
- The modifier: [`foundation/keyboard/ClearFocusOnTap.kt`](https://github.com/developersancho/pantrix-ui-cmp/blob/main/ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/keyboard/ClearFocusOnTap.kt)
- Tests: `ClearFocusOnTapTest`, in `commonTest`
- Who uses it: The demo app’s root; `PantrixTextArea` and `PantrixFilledTextArea` name it
- Demo: [**“A tap outside puts the keyboard away”**](/components/text-area) on the Text Area page
:::
