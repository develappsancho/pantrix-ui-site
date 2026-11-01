---
title: A number field and its action key
description: Why a field in Compose Multiplatform that a number is typed into, and that does something once it is typed, cannot be sent on an iPhone, what the library gives for it, and how it is tested. The problem is not Pantrix's own: it shows the same way in every Compose Multiplatform project that asks for an ImeAction with KeyboardType.Number in its common code.
---

Which of what is said here was seen in a simulator, which was read in the sources of Compose Multiplatform 1.12.1, and which was not verified, is written out in section 7.

## 1. The problem {#problem}

:::aside number-keyboard
### Symptom

*   A field in common code asks for the number keyboard and an action together: `KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Go)`. A page number, a quantity, a verification code.
*   On Android the number keyboard opens with the action key at its bottom right; a press of the key calls `onKeyboardAction`. On the desktop and the web Enter does the same.
*   On an iPhone the number pad opens: ten digits and a delete key. **There is no action key.** The number is typed but cannot be sent; what the field is for cannot be done on that platform at all.
*   No test shows it. The keyboard is the system's and is not drawn on the test's stage; a test calls "the keyboard's action" directly (`performImeAction()`), and it passes on every platform.
:::

### Cause

The iPhone's number pad (`UIKeyboardType.numberPad`) is designed for entering a PIN and carries no return key. Compose gives a field that asks for `KeyboardType.Number` this pad on iOS. `ImeAction` is, in UIKit, the type of the return key (`returnKeyType`); on a keyboard with no return key it has nowhere to show.

Native iOS apps solve this in one of two ways: they put a bar with a "Done" button above the keyboard (`inputAccessoryView`), or they ask for a keyboard that has a return key. Compose does neither by itself.

## 2. The solution {#fix}

On iOS, a number that has an action asks for the **numbers and punctuation keyboard** (`UIKeyboardType.numbersAndPunctuation`): it opens on its digits and has a return key that shows the action.

Compose gives a way for it: `KeyboardOptions.platformImeOptions`. On iOS, `PlatformImeOptions { keyboardType(…) }` says the keyboard's type in UIKit's own type, and overrides the one derived from `KeyboardType`.

The API carries `@ExperimentalComposeUiApi` and has no stable counterpart: none of Compose's `KeyboardType`s is turned into this keyboard on iOS (section 7), and every setting of `PlatformImeOptions`'s builder carries the same mark. The library leans on it in one file alone, in `iosMain`; no public signature holds an experimental type. If its signature changes in an upgrade of Compose, that file does not compile; if its behaviour changes, `NumberKeyboardOnIosTest` fails. The keyboard itself is seen only in a simulator: after an upgrade, the field is opened once and looked at.

Three details:

:::table
| # | Detail | What is done |
| :--- | :--- | :--- |
| 1 | That keyboard is for writing text: above its keys it shows a bar that offers words | `autoCorrectEnabled = false`: the bar goes |
| 2 | The same keyboard has signs and, one press away, letters too | The field itself turns down what is not a number: `InputTransformation` |
| 3 | A number without an action (`ImeAction.Default`, `ImeAction.None`) lacks nothing on the number pad | It is left alone: on iOS too it gets the number pad |
:::

## 3. With Pantrix {#with}

The library gives one function (`com.pantrixui.foundation.platform`):

```kotlin
public expect fun numberKeyboardOptions(
    imeAction: ImeAction = ImeAction.Default,
    decimal: Boolean = false
): KeyboardOptions
```

On Android, on the desktop and on the web it is the type and the action asked for, and no more. On iOS, for a number that has an action, it asks for the numbers and punctuation keyboard, with correction off. `decimal = true` asks for `KeyboardType.Decimal` in place of `KeyboardType.Number`.

```kotlin
val quantity = rememberTextFieldState()
PantrixTextField(
    state = quantity,
    label = "Quantity",
    keyboardOptions = numberKeyboardOptions(ImeAction.Done),
    onKeyboardAction = { onQuantity(quantity.text.toString().toIntOrNull()) },
    inputTransformation = DigitsOnly
)

/** Turns down, whole, a change that holds anything but a digit. */
private val DigitsOnly = InputTransformation {
    if (!asCharSequence().all { it.isDigit() }) revertAllChanges()
}
```

`PantrixPageJump` sets up its field with it. The `keyboardOptions` parameter of `PantrixTextField` points here too.

## 4. Without Pantrix {#without}

An `expect` function, and a counterpart for each platform.

### 4.1. Common code (`commonMain`)

```kotlin
expect fun numberKeyboardOptions(
    imeAction: ImeAction = ImeAction.Default,
    decimal: Boolean = false
): KeyboardOptions

internal fun plainNumberKeyboardOptions(
    imeAction: ImeAction,
    decimal: Boolean
): KeyboardOptions = KeyboardOptions(
    keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number,
    imeAction = imeAction
)
```

### 4.2. iOS (`iosMain`)

```kotlin
@OptIn(ExperimentalComposeUiApi::class)
actual fun numberKeyboardOptions(
    imeAction: ImeAction,
    decimal: Boolean
): KeyboardOptions {
    val plain = plainNumberKeyboardOptions(imeAction, decimal)
    val needsAKey = imeAction != ImeAction.Default && imeAction != ImeAction.None
    return if (!needsAKey) {
        plain
    } else {
        plain.copy(
            autoCorrectEnabled = false,
            platformImeOptions = PlatformImeOptions { keyboardType(UIKeyboardTypeNumbersAndPunctuation) }
        )
    }
}
```

`PlatformImeOptions` and `keyboardType`: `androidx.compose.ui.text.input`; `UIKeyboardTypeNumbersAndPunctuation`: `platform.UIKit`.

### 4.3. The other platforms (`androidMain`, `jvmMain`, web)

```kotlin
actual fun numberKeyboardOptions(
    imeAction: ImeAction,
    decimal: Boolean
): KeyboardOptions = plainNumberKeyboardOptions(imeAction, decimal)
```

## 5. What the helper does not do {#limits toc="5. What it does not do"}

*   **It does not open what the keyboard covers.** On iOS the window does not shrink with the keyboard: whatever shows the action's result at the bottom of the screen has to count the keyboard's share itself. `PantrixScaffold`'s snackbars count it: on iOS they come up at the top, and where they are asked to show at the bottom they stand above the keyboard (both seen in the simulator). The first version of this guide said that the snackbar stayed behind the keyboard; that was a wrong observation, drawn from a screenshot that lagged.
*   **It does not keep out what is not a number.** On iOS's keyboard signs and letters can be typed; Android's number keyboard has a full stop, a comma and a hyphen too. What the field takes is the field's to say: `InputTransformation`.
*   **It does not put a "Done" bar above the keyboard.** That is native iOS's other way (`inputAccessoryView`). Compose's `PlatformImeOptions` takes that too; a project that wants to keep the number pad and add a bar builds it from there. It was not tried here.
*   **It does not choose the decimal separator.** On iOS `decimal = true` gives the numbers and punctuation keyboard too: the full stop and the comma are both there. Which one is valid depends on the language, and is the field's job.
*   **It is not for a telephone number.** `KeyboardType.Phone` is a separate pad (`phonePad`); it has no return key either. A telephone field with an action hits the same wall; it was not solved here.

## 6. Testing {#testing}

A test cannot see the keyboard; what it can see is what the field asks of the keyboard.

*   **On every platform:** the options returned are the number keyboard and the action named (`NumberKeyboardTest`, a common test).
*   **On iOS:** a number with an action asks for the numbers and punctuation keyboard, with correction off; one without an action keeps the number pad (`NumberKeyboardOnIosTest`, `iosTest`). What is read is `platformImeOptions?.keyboardType` (the extension `androidx.compose.ui.text.input.keyboardType`).
*   **The field's behaviour:** `performImeAction()` calls the action; what the field does with it is tested on every platform (`PageJumpTest`).

That the keyboard really opens with the key, and that the key does the job, is seen only on a device or in a simulator. **Every new field that a number is typed into and that has an action should be opened in a simulator.**

## 7. Verification {#verification}

### Seen

*   iPhone 18 Pro Max simulator, iOS 27.0, Compose Multiplatform 1.12.1 (2026-10-11): a field that asks for `KeyboardType.Number` + `ImeAction.Go` opened the number pad; it had no return key, and the number typed could not be sent.
*   In the same simulator, with `numbersAndPunctuation`: the keyboard opened with its row of digits and a blue action key; the key called `onKeyboardAction`. Tried with `ImeAction.Go` (the field that goes to a page) and with `ImeAction.Done` (the demo's quantity field). Before `autoCorrectEnabled = false` a bar of words stood above the keys; after it, none did.
*   Android emulator, API 34 (2026-10-11): the number keyboard opened with its action key, and the key did the job, with both actions.
*   The web (a desktop browser): Enter did the job.

### Read in the sources

*   `PlatformImeOptions`'s declaration on iOS and its `keyboardType(UIKeyboardType?)` setting: `ui-text` 1.12.1, `iosMain/androidx/compose/ui/text/input/PlatformImeOptions.ios.kt`. Its documentation says that the values given there override those derived from `ImeOptions`. The same file has `returnKeyType`, `inputAccessoryView` and the rest too; `PlatformImeOptionsConfiguration` and each of its settings carry `@ExperimentalComposeUiApi`.
*   What `KeyboardType` is turned into on iOS: `ui` 1.12.1, `iosMain/androidx/compose/ui/platform/SkikoUITextInputTraits.ios.kt`. `Number` is the number pad (`UIKeyboardTypeNumberPad`), `Decimal` the decimal pad (`UIKeyboardTypeDecimalPad`), `Phone` the phone pad (`UIKeyboardTypePhonePad`); the others (`Text`, `Ascii`, `Uri`, `Email`, `Password`) are keyboards that open on letters. None is turned into the numbers and punctuation keyboard.

### Not verified

*   **iPad.** On an iPad the number pad opens as a full keyboard; it may have a return key there. Not tried. The helper asks for the numbers and punctuation keyboard on an iPad too.
*   **An iPhone with a hardware keyboard.** In the simulator the hardware keys did not reach the app.
*   **A real device**, and earlier versions of iOS. The action key's text ("go", "done") or its icon may change with the version.
*   **The pads of `KeyboardType.Decimal` and `KeyboardType.Phone` on iOS.** Which pad they are turned into was read in the sources; that they have no return key is UIKit's known behaviour, and was not opened and looked at here.
*   **The web in a phone's browser.** What the virtual keyboard gives a number field was not looked at.

## 8. Where it is in this repository {#repository toc="8. In the repository"}

:::table
| What | Where |
| :--- | :--- |
| The function and its common helper | `ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/platform/NumberKeyboard.kt` |
| The iOS counterpart | `ui/foundation/src/iosMain/kotlin/com/pantrixui/foundation/platform/NumberKeyboard.ios.kt` |
| The other counterparts | `NumberKeyboard.android.kt`, `NumberKeyboard.jvm.kt`, `NumberKeyboard.web.kt` in the same package |
| Tests | `NumberKeyboardTest` (`commonTest`), `NumberKeyboardOnIosTest` (`iosTest`) |
| The component that uses it | `PantrixPageJump` (`ui/components/.../data/pagination/PageJump.kt`) |
| The demo | "A field of numbers and the action key" on the `TextField` page; the table's footer on the `Pagination` page |
:::
