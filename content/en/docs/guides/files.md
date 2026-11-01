---
title: Picking and dropping files
description: Why, in Compose Multiplatform, a user's giving the app a file (by choosing it in the platform's picker, or by dragging it over an area and dropping it) cannot be done in common code directly, what the library gives for it, how the file is read, and how it is tested. The problem is not Pantrix's own: it shows the same way in every Compose Multiplatform project that takes files from its user.
---

Everything said here of Compose was read in the sources of Compose Multiplatform 1.12.1, or seen in a test that runs in this repository, on a device or in a browser; which was seen where is written in sections 7 and 8.

## 1. The problem {#problem}

:::aside files
### Symptom

*   Common Compose has no file picker. Behind a "Choose a file" button there is something else on each platform: an activity's result on Android (`ACTION_OPEN_DOCUMENT` or the photo picker), a presented `UIDocumentPickerViewController` or `PHPickerViewController` on iOS, the operating system's window on the desktop, an `<input type="file">` on the web.
*   The file chosen is a different type on each platform too: a content `Uri` on Android, an `NSURL` on iOS, a `java.io.File` on the desktop, the browser's `File` on the web. Common code knows none of them.
*   For dropping, Compose has a common `Modifier.dragAndDropTarget`, but the `DragAndDropEvent` it gives carries different things on each platform, and its files cannot be read from common code.
*   Dragging a file in is a pointer's job: on a phone there is most often nothing to drag. An area that takes files by dropping alone cannot be used on a phone or with a keyboard.
:::

### Cause

Compose's drop event wraps a type of its own on each platform (Compose Multiplatform 1.12.1, the `ui` module):

:::table
| Platform | What `DragAndDropEvent` carries | How the files are read |
| :--- | :--- | :--- |
| Android | `toAndroidDragEvent()`: a `DragEvent` | The content `Uri`s in the items of `clipData`. Another app's can be read only if `Activity.requestDragAndDropPermissions(event)` is asked for while the drop lasts. |
| iOS | `items`: `UIDragItem`s (`@ExperimentalComposeUiApi`) | Each item's `itemProvider` loads its file with `loadFileRepresentation…`. The file it gives is deleted once the loading handler returns: it has to be copied inside it. |
| Desktop | `dragData()`: a `DragData` (`@ExperimentalComposeUiApi`) | `DragData.FilesList.readFiles()`: the addresses of the files on the disk (`file:/…`). |
| Web | `transferData?.domDataTransferOrNull`: the browser's `DataTransfer` | `files`. The browser gives them only inside the `drop` event: they cannot be read later. |
:::

For pickers there is nothing common at all; each opens with the platform's own code and gives its result in its own type.

## 2. The solution: four parts {#fix}

In foundation (`com.pantrixui.foundation.files`):

1.  **`PantrixFile`**: its name, its size (`null` where it is not known), its MIME type and `suspend fun readBytes()`. Nothing is read until `readBytes` is called. The platform's own type is there too: `uri` on Android, `url` on iOS, `file` on the desktop, `domFile` on the web. For tests and for files the app makes itself it has a builder from memory: `PantrixFile(name, bytes, mimeType)`.
2.  **`PantrixFileType`**: a MIME type (its subtype may be `*`) and extensions. The ready ones: `Any`, `Images`, `Videos`, `Audio`, `Pdf`, `Text`. A file fits a type when its MIME type fits **or** its extension is in the list; a file whose type the platform does not say is known by its name too.
3.  **`FilePicker` and `rememberFilePicker()`**: `pick(FilePickRequest(accept, multiple)) { files -> … }`. It opens the platform's document picker; where only images are asked for (`FilePickRequest.imagesOnly`), Android and iOS open the photo picker. None asks for a permission. `FilePicker` is an interface of one method: a test gives its own picker. Where **`LocalFilePicker`** gives a picker, `rememberFilePicker()` returns it in place of the platform's: a test or a preview gives all the areas of a screen the same picker at once.
4.  **`Modifier.fileDropTarget(enabled, onOver) { files -> … }`**: turns what is dropped into a list of `PantrixFile`. On Android it asks for the read permission, on iOS it copies the files into the app's temporary folder, on the web it takes the files while the drop lasts. `onOver` says `true` when something that can hold files comes over it, and `false` when it leaves.

In the components (`com.pantrixui.components.elements.filedrop`), **`PantrixFileDrop`** (the area that opens the picker when pressed, takes what is dropped on it, and screens what comes by type, size and number) and **`PantrixFileItem`** (the row of a file taken) stand on these.

## 3. With Pantrix {#with}

### 3.1. a form's attachments

```kotlin
var attachments by remember { mutableStateOf(emptyList<PantrixFile>()) }
var refused by remember { mutableStateOf(emptyList<RejectedFile>()) }

PantrixFileDrop(
    onFiles = { files -> attachments += files },
    accept = listOf(PantrixFileType.Pdf, PantrixFileType.Images),
    multiple = true,
    maxSize = 10_000_000,
    onRejected = { refused = it },
    supportingText = if (refused.isEmpty()) "A PDF or an image, 10 MB at most" else "Not taken: ${refused.first().file.name}",
    error = refused.isNotEmpty(),
    tagKey = "attachments"
)
attachments.forEach { file ->
    PantrixFileItem(name = file.name, size = file.size, onRemove = { attachments -= file })
}
```

*   The area holds no state: the app keeps what is taken and lays it out as rows.
*   What does not fit goes to `onRejected` with its reasons (`FileRejection.Type`, `Size`, `Count`); what is said of it is the app's.
*   A screen reader is told, politely, how many files were taken and how many were not.

### 3.2. reading the file

```kotlin
scope.launch {
    val bytes = file.readBytes()          // off the main thread on Android, the desktop and iOS
    upload(file.name, file.mimeType, bytes)
}
```

Whoever wants to read a large file as a stream uses the platform's own type: `contentResolver.openInputStream(file.uri!!)` on Android, `file.file!!.inputStream()` on the desktop.

### 3.3. without the area

An app with a design of its own can use foundation's parts alone:

```kotlin
val picker = rememberFilePicker()
var over by remember { mutableStateOf(false) }
Box(
    Modifier
        .fileDropTarget(onOver = { over = it }) { files -> attach(files) }
        .clickable { picker.pick(FilePickRequest(listOf(PantrixFileType.Images))) { attach(it) } }
)
```

On the web the picker opens only inside a user's press: `pick` has to be called from a click or a key.

## 4. Without Pantrix {#without}

What follows are the shortest forms that can be copied into a project without the library; Pantrix's code (`ui/foundation/src/*Main/kotlin/com/pantrixui/foundation/files/`) does the same with its tests and its edge cases.

**Android, the picker** (needs `androidx.activity`; Compose UI brings it already, but at run time alone: to compile, it has to be written out):

```kotlin
val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris -> /* … */ }
launcher.launch(arrayOf("application/pdf", "image/*"))
// Images alone: ActivityResultContracts.PickVisualMedia(), PickVisualMediaRequest(PickVisualMedia.ImageOnly)
```

**Android, dropping:**

```kotlin
Modifier.dragAndDropTarget(
    shouldStartDragAndDrop = { event -> event.toAndroidDragEvent().clipDescription?.hasMimeType("*/*") == true },
    target = object : DragAndDropTarget {
        override fun onDrop(event: DragAndDropEvent): Boolean {
            val drag = event.toAndroidDragEvent()
            activity.requestDragAndDropPermissions(drag)   // while the drop lasts
            val uris = (0 until drag.clipData.itemCount).mapNotNull { drag.clipData.getItemAt(it).uri }
            return uris.isNotEmpty()
        }
    }
)
```

**iOS, the picker** (`asCopy = true`: the file is copied into the app's folder, and no security scope is needed):

```kotlin
val picker = UIDocumentPickerViewController(forOpeningContentTypes = listOf(UTTypePDF), asCopy = true)
picker.delegate = delegate              // UIKit holds the delegate weakly: it has to be kept somewhere
LocalUIViewController.current.presentViewController(picker, animated = true, completion = null)
```

**Desktop, the picker:**

```kotlin
val dialog = FileDialog(null as Frame?, "Choose a file", FileDialog.LOAD).apply {
    isMultipleMode = true
    filenameFilter = FilenameFilter { _, name -> name.endsWith(".pdf", ignoreCase = true) }
    file = "*.pdf"                       // Windows calls no filter; it reads the name
    isVisible = true                     // waits until the user closes it
}
val files = dialog.files
```

**Web, the picker and dropping:**

```kotlin
val input = document.createElement("input") as HTMLInputElement
input.type = "file"; input.accept = "application/pdf,.pdf"; input.multiple = true
input.addEventListener("change", { /* input.files */ })
input.click()                            // inside a user's press

// Dropping: event.transferData?.domDataTransferOrNull?.files, read inside the drop event.
```

## 5. What the helper does not do {#limits toc="5. What it does not do"}

*   **Reading as a stream.** `readBytes()` takes the whole file into memory. For a stream, the platform's own type is used (3.2).
*   **The camera.** The photo picker shows the user's pictures; it does not have a photo taken there and then.
*   **Saving a file.** This document tells only of taking. The demo's saving is in `app/shared/.../platform/FileSaver.kt` (`docs/guide/chart-export/CHART_EXPORT_GUIDE.md`, section 3).
*   **A lasting permission.** On Android the read permission of a file chosen lasts while the app runs; it is not made lasting with `takePersistableUriPermission`.
*   **A result that comes when the activity is built again.** If Android kills the activity and builds it again while the picker is open, the result comes to the new registration, but since there is no `onPicked` waiting it is let go.
*   **Checking the type while dragging.** The area does not tell apart what does not fit while it is dragged; it refuses it when it is dropped (not every platform says what is being dragged).

## 6. Testing {#testing}

*   **Replacing the picker.** `PantrixFileDrop(picker = …)` takes a test's picker: `FilePicker { request, onPicked -> onPicked(listOf(PantrixFile("a.pdf", bytes))) }`. The types and the number asked for are read in `request`. Apps test their own forms this way too.
*   **The picker in an iOS test.** Compose's iOS tests have no app and no `LocalUIViewController`; there `rememberFilePicker()` returns a picker that never answers, like a picker the user closed. A test that has a file chosen has to give its own picker.
*   **The whole screen's picker.** A test whose areas stand deep in a screen writes `CompositionLocalProvider(LocalFilePicker provides picker) { Screen() }` rather than giving the picker one by one: every place that calls `rememberFilePicker()`, `PantrixFileDrop`'s default among them, takes that picker, and the platform's picker does not open.
*   **Dropping.** Compose's tests cannot make a drop event. The code that turns what is dropped into files is tested in the platforms' tests with made-up data: an implementation of `DragData.FilesList` on the desktop, `ClipData` and `ClipDescription` on Android, a real `DataTransfer` built in the browser on the web.
*   **The platforms' pickers.** On Android, Robolectric keeps the intent the activity started and gives the system's answer (`shadowOf(activity).nextStartedActivityForResult`, `receiveResult`): the action the picker opens, its MIME types and the files that come are tested so. On the web the `<input>` the picker puts in the page is read and its `change` sent by hand. On iOS the choice of uniform types and the copying into the temporary folder are tested; the window presented itself is not.

## 7. Pitfalls {#pitfalls}

*   **Windows's window calls no filter.** The `filenameFilter` of AWT's `FileDialog` works on macOS and Linux, and is not called on Windows. There Pantrix writes the types into the name field as a pattern (`*.pdf;*.png`); the user can still choose another file, and the area refuses it.
*   **Android's photo picker does not give the real name.** The name of a file that comes from the photo picker is, for privacy, a media number (`1000000648.png`). iOS's photo picker gives the suggested name. Seen in the emulator.
*   **A borrowed file on iOS.** The file a loader gives is deleted as soon as the loading handler returns. Pantrix copies it into the app's temporary folder inside the handler.
*   **On the web, dropped files are read only while the event lasts.** `DataTransfer.files` has to be read inside the `drop` event; later it is empty.
*   **On Wasm the bytes are copied.** In Kotlin/JS a `ByteArray` is the browser's `Int8Array` and is taken as it is; in Kotlin/Wasm the bytes are copied one by one, so a large file is slow.
*   **The position in drag events built by code.** In a browser, the `offsetX` of a `DragEvent` built with JavaScript comes as half of `clientX` on a screen of pixel density 2; Compose takes the drop to be in the wrong place. A real drag does not do this; in a trial, `offsetX` and `offsetY` have to be written on the event with the right values.
*   **Cutting in the middle.** `TextOverflow.MiddleEllipsis` cuts in the middle on Android alone; on iOS, the desktop and the web it cuts at the end (CMP-6716). The file's row measures the name itself and cuts it in the middle, and tells a screen reader the full name.

## 8. Verification {#verification}

### Tests

:::table
| What | Where |
| :--- | :--- |
| Matching types, the MIME table, the file in memory, the picker's request, the picker `LocalFilePicker` gives | `ui/foundation/src/commonTest/.../files/` (JVM, iOS, JS, Wasm) |
| The desktop's filter, the Windows pattern, the list of dropped files | `FilesOnTheDesktopTest` |
| Android's document and photo pickers (Robolectric), the picker `LocalFilePicker` gives taking their place, the types and addresses of a drop | `FilesOnAndroidTest` |
| The `<input>` in a browser, dropping with `DataTransfer`, reading the bytes | `FilesOnTheWebTest` (JS and Wasm) |
| Uniform types, copying into the temporary folder | `FilesOnIosTest` |
| The area: press and keyboard, screening, words, announcement, analytics; the row; colours; styles | `ui/components/src/commonTest/.../File*Test.kt`, `MiddleCutTest` |
:::

### Looked at by hand

*   **The web:** dropping with a real `DataTransfer`; the area lighting up while dragging; a refusal and its reason; the row and the progress; the `accept` of the `<input>` a press opens; disabled, and colours of its own; the Turkish words.
*   **Android emulator (API 34):** the document picker (the filter: a text file did not show), choosing several, the photo picker and the picture read being shown, the accessibility tree.
*   **iOS simulator:** Files (the filter: a text file could not be chosen), the photo picker and the picture read, the accessibility tree.

### Not verified

*   Dropping from another app on Android and on an iPad, choosing and dropping by hand in the desktop app, the windows of Windows and Linux, a screen reader's voice.

## 9. Where it is in this repository {#repository toc="9. In the repository"}

*   Foundation: `ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/files/` (`PantrixFile.kt`, `PantrixFileType.kt`, `FilePicker.kt`, `FileDropTarget.kt`) and their counterparts in each platform's source set.
*   Components: `ui/components/src/commonMain/kotlin/com/pantrixui/components/elements/filedrop/`, the cutting in the middle in `components/common/MiddleCut.kt`.
*   Words: `dropFilesHere`, `chooseFiles`, `orChooseFiles`, `orDropHere`, `dropToAdd`, `filesAdded`, `filesRejected`, `fileSize` in `PantrixStrings`.
*   The demo: `app/shared/src/commonMain/kotlin/com/pantrixui/cmp/demo/samples/FileDropSamples.kt`.

## 10. What is left open {#open}

In `docs/OPEN_ITEMS.md`: reading as a stream, the camera, checking the type while dragging, `PantrixText`'s cutting in the middle working on every platform, dropping on devices.
