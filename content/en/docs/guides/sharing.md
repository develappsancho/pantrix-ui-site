---
title: Sending a text to another app
description: Why, in Compose Multiplatform, sending a text or a link to another app, a message, a mail, a note, is other code on each platform, what the library gives for it, and how it is tested. The problem is not Pantrix's own: it is seen the same way in every Compose Multiplatform app that says "Send with another app".
---

What is said of the platforms rests on the documentation of Android's `Intent.createChooser`, UIKit's `UIActivityViewController` and the browsers' Web Share API (`navigator.share`), or was seen in a test that runs in this repository; what was seen where is written in section 6.

## 1. The problem {#problem}

:::aside sharing
### Symptom

*   A "Send" button cannot be written in common code: Compose's common API has no share sheet.
*   The app writes an `expect`/`actual` of its own for each platform: an `Intent` on Android, a view controller on iOS, `navigator.share` on the web; the desktop has nothing like it.
*   On an iPad `UIActivityViewController` opens as a popover; where it is not told what it opens from, the app crashes.
*   Firefox on a desktop has no `navigator.share`, and every browser refuses a share called outside a press.
*   A test that presses the button either opens the system's sheet or can check nothing.
:::

### Cause

*   **The share sheet is the platform's.** Each takes its own type and opens from its own place: Android from a `Context`, iOS from the view controller on top, a browser from inside a press.
*   **What it answers differs too.** Android's chooser does not tell the app what its reader chose; iOS says completed or closed once the sheet has closed; a browser resolves a promise, or rejects it with `AbortError` where the sheet was closed and `NotAllowedError` where it refused.

## 2. The solution {#fix}

:::table
| # | Piece | What it gives |
| :--- | :--- | :--- |
| 1 | **One common interface, and its implementation for each platform** | The button is written once in common code |
| 2 | **None where there is none** | The desktop and a browser without `navigator.share` offer to copy in place of sending |
| 3 | **One type of result** | Shared, closed, shown or not shown: the screen reads it the same way on every platform |
| 4 | **A sheet of the test's own** | No system sheet opens, and what was sent is read |
:::

## 3. With Pantrix {#with}

### 3.1. The sheet

`rememberShareSheet()` is in `com.pantrixui:foundation` (`com.pantrixui.foundation.platform`). It gives a `ShareSheet`, or `null` on a platform without one:

```kotlin
val sheet = rememberShareSheet()
val clipboard = rememberPantrixClipboard()
val scope = rememberCoroutineScope()
if (sheet != null) {
    PantrixButton(
        text = appWords.sendWithAnotherApp,
        onClick = { sheet.share(link, title = documentName) { result -> onShared(result) } },
        tagKey = "send"
    )
} else {
    // On the desktop and in a browser without navigator.share: copying the link.
    PantrixButton(text = appWords.copyLink, onClick = { scope.launch { clipboard.copyText(link) } }, tagKey = "copy")
}
```

:::table
| Platform | Sheet | `ShareResult` |
| :--- | :--- | :--- |
| Android | The system's chooser, `Intent.ACTION_SEND` with `text/plain`; the title is the subject (`EXTRA_SUBJECT`) and the chooser's heading | `Started`: the chooser does not say what its reader chose |
| iOS | `UIActivityViewController`; on an iPad a popover with no arrow in the middle of the window; the title is a mail's subject | `Shared` or `Cancelled` |
| Web | `navigator.share`, the title the page's title | `Shared`; `Cancelled` where it was closed; `Failed` where it was refused |
| Desktop | None: `rememberShareSheet()` gives `null` | — |
:::

*   **A link is sent as its text.** Every app the sheet lists takes it so.
*   **A press on the web.** `share` is called from a button's `onClick`, with no wait in between: a browser refuses a sheet no press asked for.
*   **The result may be `Started`.** On Android "sent" is not said: the reader may have closed the sheet.

### 3.2. Testing

`PantrixTestTheme` gives the screen a `TestShareSheet` of its own: no system sheet opens. A test that reads what was sent gives its own inside the theme, and a test of a platform without a sheet gives none:

```kotlin
val sheet = TestShareSheet()
setContent {
    PantrixTestTheme { CompositionLocalProvider(LocalShareSheet provides sheet) { ShareScreen(link) } }
}
onPantrixNode("Button", "send").performClick()
assertEquals(listOf(SharedText(link, title = "Orders")), sheet.shared)

// The desktop's screen, which copies the link:
setContent { PantrixTestTheme { CompositionLocalProvider(LocalShareSheet provides null) { ShareScreen(link) } } }
onPantrixNode("Button", "copy").assertExists()
```

`TestShareSheet(result = ShareResult.Cancelled)` answers as a reader who closes the sheet.

## 4. Without Pantrix {#without}

In a project without the library each platform makes its own call, and common code reaches it through an `expect fun`:

:::table
| Platform | Call | Mind |
| :--- | :--- | :--- |
| Android | `context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text), title))` | Outside an activity it needs `FLAG_ACTIVITY_NEW_TASK` |
| iOS | `top.presentViewController(UIActivityViewController(listOf(text), null), true, null)` | `top` is the controller presented on top; on an iPad `popoverPresentationController` is given a `sourceView` and a `sourceRect` |
| Web | `navigator.share({ text: text, title: title })` | From inside a press; in a browser without it `typeof navigator.share` is `"undefined"` |
| Desktop | None | Copying to the clipboard |
:::

## 5. Pitfalls {#pitfalls}

*   **A popover with no source on an iPad.** A `UIActivityViewController` given no `sourceView` crashes the app on an iPad. Pantrix places it in the middle of the window.
*   **A share with no press.** `navigator.share` called after a wait or a network request is rejected with `NotAllowedError`: `Failed`. The text to share is ready before the press.
*   **"Sent" is not said on Android.** The chooser does not tell the reader's choice; a screen does not show `Started` as a success.
*   **A file is another matter.** Sharing a file on Android needs a `FileProvider` in the manifest; its pieces are in section 3 of [the chart export guide](/docs/guides/chart-export).

## 6. Verification {#verification}

### Tests

:::table
| What | Where |
| :--- | :--- |
| The sheet given is the screen's, and none where none is given | `ui/foundation/src/commonTest/.../platform/ShareSheetTest.kt` (JVM, Android, iOS, JS, Wasm) |
| Android: the chooser's `ACTION_SEND` intent, `text/plain`, the text, the subject and the chooser's heading; no subject for a text without a title; `Started` | `ui/foundation/src/androidHostTest/.../platform/ShareSheetOnAndroidTest.kt` (Robolectric) |
| iOS: `Cancelled` in a test binary that has no application | `ui/foundation/src/iosTest/.../platform/ShareSheetOnIosTest.kt` |
| Web: the text and title handed to `navigator.share`, a text without a title; `Shared`, `Cancelled`, `Failed`; no sheet without `navigator.share` (with the test's own `navigator.share`) | `ui/foundation/src/webTest/.../platform/ShareSheetOnTheWebTest.kt` (JS, Wasm) |
| Desktop: no sheet | `ui/foundation/src/jvmTest/.../platform/ShareSheetOnTheDesktopTest.kt` |
| `TestShareSheet` and the test theme's own sheet | `ui/testing/src/commonTest/.../TestDoublesTest.kt`, `PantrixTestThemeTest.kt` |
:::

### Looked at by hand

*   **On the Android emulator (2026-11-11):** "Sending to another app" on the demo's Clipboard page: the chooser opened with the link, under the system's title ("Sharing link"); closed with the back key, the card said "The share sheet opened", not knowing the choice.
*   **In the iOS simulator (iPhone 18 Pro Max, 2026-11-11):** in the same example `UIActivityViewController` opened with the link; with "Copy" chosen the card said "Sent", and with a press outside the sheet "The sheet was closed".
*   **On the web (the built-in browser, 2026-11-11):** there was no `navigator.share`, and the button offered to copy the link. With a stand-in `navigator.share` given to the page, the button became the share button, the stand-in got the text and the title, and the card said "Sent"; with the switch on, the copy button came back.

### Not verified

*   The popover on an iPad.
*   The absence of `navigator.share` in Firefox: tested by hiding it.

## 7. Where it is in this repository {#repository toc="7. In the repository"}

*   `ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/platform/ShareSheet.kt`, and the platforms' `ShareSheet.*.kt`.
*   `ui/testing/src/commonMain/kotlin/com/pantrixui/testing/TestShareSheet.kt`.
*   The demo: `app/shared/src/commonMain/kotlin/com/pantrixui/cmp/demo/samples/ClipboardSamples.kt` ("Sending to another app").
