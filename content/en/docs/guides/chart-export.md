---
title: Exporting a chart as a file
description: How a Pantrix chart is exported as PNG, SVG and PDF, what the library does and what it leaves to the app, how the file made reaches the user on each platform, and the limits of the three kinds of file.
---

The library **makes** the file and **hands** it to the app; it does not save it, share it or download it. The reason is in section 3: getting a file to the user is four separate jobs on four platforms, and on Android it needs a file provider.

Everything said here of the library was seen in tests that run in this repository, or by looking at the files made; which was seen where is written in section 6. The platform code in section 3 is the demo's own code (the package `com.pantrixui.cmp.demo.platform` in `app/shared`), and compiles on four platforms; where it was tried is in section 6. The one sketch there, Android's save dialog, was not compiled.

## 1. Use {#use}

Exporting is a parameter: in a chart given no `export`, nothing shows.

```kotlin
PantrixLineChart(
    data = months,
    x = { it.name },
    values = revenue,
    export = ChartExport(fileName = "revenue") { file ->
        // file.name = "revenue.svg", file.mimeType = "image/svg+xml", file.bytes = the content
        share(file)
    }
)
```

Given `export`, an export button comes up among the chart's own buttons (beside the zoom buttons where there are any). The reader presses the button and chooses the kind; the chart makes the file and hands it to `onExport`.

:::table
| Field | What it says | Default |
| :--- | :--- | :--- |
| `formats` | The kinds the reader chooses from, in their order. Where one kind is given, no choice opens: the button makes that file | All three: PNG, SVG, PDF |
| `fileName` | The file's name, without its extension | `"chart"` |
| `scale` | The PNG's pixels to a dp. It does not depend on the screen's density | `2f` |
| `background` | The picture's ground. `Color.Unspecified` is the theme's `background`; `Color.Transparent` draws no ground | The theme's ground |
| `padding` | The room the picture leaves around the chart, on each side. The ground is under the room too | `12.dp` |
| `onExport` | The `ChartExportFile` made is handed to it | Required |
:::

`export` is in `PantrixChart` and the six short charts above it (line, area, bar, range, waterfall, candlestick), and in every one of the other fourteen kinds: pie, gauge, progress circle, meter group, status tracker, sparkline, scatter, radar, polar, heatmap, treemap, funnel, sankey and Gantt. In each it is at the end of the signature; in `PantrixProgressCircle` alone it comes before `content`, so that the last lambda stays the content inside the ring.

The button stands in a row above the chart, on the side where the lines end. In a chart with axes it is in the same row as the zoom buttons. A chart given no height grows by this row; a chart given a height keeps its height, and its drawing is that much shorter.

:::sketch chart-export
:::

## 2. What is in the file {#what-is-in-the-file}

The file shows the chart **as it is on the screen**:

*   A series taken out from the legend is out in the file too (it stands faint in the legend).
*   Of a chart that is looked at closer (`zoom`), the part in view goes into the file.
*   The place pointed at, the tooltip, the zoom buttons and the navigator's strip are **not** in the file.
*   The legend, wherever it stands on the screen, is under the plot in the file. So is the colour scale of a heatmap and of a treemap with a scale: the least value, the colours' run and the greatest value, from the side where the lines start.
*   Content the caller gives as a composable is not in the file: the `center` in a pie's hole, the `center` in a gauge's middle, the `content` inside a progress circle. The value the gauge writes in its middle itself is in the file. The line under a status tracker that writes the name of the block shown is not in the file either.
*   There is `padding`'s room around the chart. The chart's box on the screen ends where its text ends (the room around it is given by the card it stands in); in a picture with no room, an axis's numbers stand right at the picture's edge.
*   A file taken while the entry animation runs shows the chart's end state too.

The three kinds are made from the same drawing; the differences between them are these:

:::table
| Kind | What | Text | Size (a chart of two series over twelve months) |
| :--- | :--- | :--- | :--- |
| PNG | A picture of pixels; as sharp as `scale` | In the app's typeface, as on the screen | Tens of kilobytes |
| SVG | Lines and shapes; sharp at every size | Real text (`<text>`), in the sans-serif typeface of whoever opens the file | A few kilobytes |
| PDF | One page, lines and shapes; sharp at every size | Real text in Helvetica; a line Helvetica cannot write as a picture | A few kilobytes |
:::

### Why the text differs from the screen's

SVG and PDF do not carry the app's typeface: the library does not have the typeface's file. In SVG the text is written in the sans-serif typeface of the program that opens the file, in PDF in Helvetica, which every PDF reader has. These typefaces' letters can be narrower or wider than the app's; each line is written in the **middle** of the room it takes on the screen. A number beside an axis may therefore stand a pixel or two closer to the axis, or farther from it.

In PDF, Helvetica writes the letters of the Western European languages and of Turkish (for Turkish's six letters the file carries an encoding of its own). A line that holds a letter Helvetica cannot write (an Arabic name, the sign `₺`) is put on the page as **its own picture**: four times as sharp as the screen's density, but not endlessly sharp like everything else, and it cannot be selected as text.

## 3. Getting the file to the user {#getting-the-file-to-the-user}

`onExport` takes a `ChartExportFile`: `name`, `mimeType`, `format`, `bytes`. The rest is the platform's job.

:::table
| Platform | The usual way | What it asks for |
| :--- | :--- | :--- |
| Android | The share sheet (`Intent.ACTION_SEND`) or "save as" (`CreateDocument`) | To share, a `FileProvider` in the manifest; to save, an `Activity`'s result |
| iOS | The share sheet (`UIActivityViewController`) | A temporary file, and a view controller to present the sheet |
| Desktop | The save dialog | Nothing |
| Web | The browser's download | Nothing |
:::

For now the library does not do this itself. On Android sharing needs a `FileProvider` and a file of paths; a library can declare them in its own manifest (it merges into the app's), but then every app that uses the library gets a provider. Whether to take this into the library is an open decision (`docs/OPEN_ITEMS.md`, section 2).

The demo writes these ways in the package `com.pantrixui.cmp.demo.platform` of `app/shared`: one declaration in common code, and its counterpart in each platform's source set (`FileSaver.android.kt`, `FileSaver.ios.kt`, `FileSaver.jvm.kt`, `FileSaver.web.kt`). The export example among the chart examples (`ChartExported`) first shows the file made, and hands it over with the button "Save the file" too.

```kotlin
// commonMain
internal fun interface FileSaver {
    fun save(name: String, mimeType: String, bytes: ByteArray)
}

@Composable
internal expect fun rememberFileSaver(): FileSaver

// In the chart:
val saver = rememberFileSaver()
PantrixLineChart(
    // ...
    export = ChartExport { file -> saver.save(file.name, file.mimeType, file.bytes) }
)
```

The declaration is in a package of its own, not in an example's file: each of the demo's example files is copied into another package with its English twin, and the copy of an `expect` declaration has no counterpart. The pieces below are from those files.

### Android: the share sheet

```kotlin
val context = LocalContext.current
// save:
val folder = File(context.cacheDir, "exports").apply { mkdirs() }
val file = File(folder, name).apply { writeBytes(bytes) }
val uri = FileProvider.getUriForFile(context, context.packageName + ".files", file)
val send = Intent(Intent.ACTION_SEND)
    .setType(mimeType)
    .putExtra(Intent.EXTRA_STREAM, uri)
    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
context.startActivity(Intent.createChooser(send, name))
```

The app chosen in the sheet reads the file by the address the `FileProvider` gives. The provider is declared in the app's manifest and opens only one folder of the cache:

```xml
<!-- AndroidManifest.xml, inside <application> -->
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.files"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

```xml
<!-- res/xml/file_paths.xml -->
<paths>
    <cache-path name="exports" path="exports/" />
</paths>
```

`FileProvider` is in the library `androidx.core` (the demo adds `androidx.core.ktx` to `androidMain`). The manifest's `authorities` has to be the same as the address in the code; if it is not, `getUriForFile` throws. So it does if the file is outside the folder.

### Android: the save dialog

It is not in the demo; **this piece was not compiled.** The reader chooses where the file is written, and no provider is needed:

```kotlin
val pending = remember { mutableStateOf<ChartExportFile?>(null) }
val saver = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri ->
    val file = pending.value
    if (uri != null && file != null) {
        context.contentResolver.openOutputStream(uri)?.use { it.write(file.bytes) }
    }
    pending.value = null
}
// onExport:
{ file -> pending.value = file; saver.launch(file.name) }
```

### iOS: the share sheet

```kotlin
val path = NSTemporaryDirectory() + name
val data = if (bytes.isEmpty()) {
    NSData()
} else {
    bytes.usePinned { NSData.create(bytes = it.addressOf(0), length = bytes.size.toULong()) }
}
data.writeToFile(path, atomically = true)
var top = UIApplication.sharedApplication.keyWindow?.rootViewController
while (top?.presentedViewController != null) top = top.presentedViewController
val sheet = UIActivityViewController(listOf(NSURL.fileURLWithPath(path)), null)
sheet.popoverPresentationController?.sourceView = top?.view
top?.presentViewController(sheet, animated = true, completion = null)
```

"Save to Files" is in the sheet too. The sheet is presented from the topmost view controller: a controller that is presenting another refuses a second presentation. On an iPad the sheet is presented as a popover, and asks what it is anchored to (`popoverPresentationController.sourceView`). The address of an empty array cannot be given to `NSData.create`: an empty file is written apart.

### Desktop: the save dialog

```kotlin
val dialog = FileDialog(null as Frame?, name, FileDialog.SAVE).apply {
    file = name
    isVisible = true
}
val chosen = dialog.file
if (chosen != null) File(dialog.directory, chosen).writeBytes(bytes)
```

If the dialog is closed, `dialog.file` is empty and nothing is written.

### Web: the download

```kotlin
val scope = rememberCoroutineScope()
// save:
val url = URL.createObjectURL(blobOf(bytes, mimeType))
val link = document.createElement("a") as HTMLAnchorElement
link.href = url
link.download = name
link.click()
scope.launch {
    delay(40.seconds)
    URL.revokeObjectURL(url)
}
```

The address is not taken back right after the press: the download starts with the press but reads the file later, and some browsers cut short a download whose address is taken back at once.

`blobOf` is written apart on the `js` and `wasmJs` targets; the source set the two share (`webMain`) cannot turn a `ByteArray` into the browser's array of bytes:

```kotlin
// jsMain: in Kotlin/JS a ByteArray is an Int8Array already
internal actual fun blobOf(bytes: ByteArray, type: String): Blob =
    Blob(arrayOf(bytes.unsafeCast<Int8Array>()), BlobPropertyBag(type = type))

// wasmJsMain: the bytes are copied into the browser's memory one by one
internal actual fun blobOf(bytes: ByteArray, type: String): Blob {
    val array = Uint8Array(bytes.size)
    bytes.forEachIndexed { index, byte -> array[index] = byte }
    val parts = JsArray<JsAny?>()
    parts[0] = array
    return Blob(parts, BlobPropertyBag(type = type))
}
```

## 4. What to know {#what-to-know}

*   **The file is made with the press, on the main thread.** In a chart of twelve places it is not noticed. The vector file of a chart of tens of thousands of points, with its thinned line, can be hundreds of kilobytes, and making it can take longer than a frame; how long was not measured.
*   **An area fill with a gradient** is a real gradient in SVG, and in PDF is written as the colours with a grey mask beside them: the bars under it look as they do on the screen.
*   **Every number of the PDF is written in the page's own unit**; the page is not scaled and moved as a whole. macOS's drawing of PDF (Preview, Quick Look) drew a mask set up under a scaled page at the wrong size, and the gradient fill vanished; this would have shown in a file made on every device whose screen density is not 1 (every phone).
*   **The file of a chart with no values says "No data".** Where the chart is given content of its own with `empty`, that content is on the screen alone: a composable cannot be drawn into a file, so the file writes the theme's words.
*   **In the dark theme** the file's ground is dark too, and its text light. For a file to be printed on paper, `background = Color.White` is not enough: the text's colour comes from the theme. Such a file should be made in the light theme.
*   **Motion and the look.** The file is the size the chart last settled at on the screen: a file made on a narrow phone is narrow, one made in a wide window wide.
*   **SVG and PDF cannot be made in Android's host tests (Robolectric).** A drawing's path is read step by step with Compose's path iterator, which on Android loads the native library of `androidx.graphics.path`. The library comes with the app (it is there on a device and an emulator), and not on the machine the tests run on: a Robolectric test that presses the button and chooses SVG or PDF fails with `UnsatisfiedLinkError`. A PNG is made there too.

## 5. Automation and reporting {#automation-and-reporting}

The export button's tag is `.Export` after the chart's own tag: `Chart_<tagKey>.Export`, `PieChart_<tagKey>.Export`, `Gantt_<tagKey>.Export` and the others. Where a choice of kind opens, its rows' tags are `.Export_png`, `.Export_svg` and `.Export_pdf`. When a file is made, an `export` event is reported with the chart's own component name (`Chart`, `PieChart`, `Gauge`, `Gantt` …); its `format` property is the file's extension (`png`, `svg`, `pdf`). The file's name and content are not reported.

## 6. How it was verified {#verification}

:::table
| What | Where |
| :--- | :--- |
| The drawing kept as lines, shapes and words; how SVG and PDF are written; the PDF's table of parts and the length of its streams | `ChartExportFilesTest` (the desktop, the iOS simulator and the two browser targets) |
| The button, the choice of kind and its closing, the file's name and type, the PNG's size in pixels, the ground, the room, the screen's density, the part in view, a series taken out, dashed lines, marks, value labels, an axis's title, the picture of an Arabic name in the PDF | `ChartExportTest` (the same four targets) |
| The picture's legend: lines, centring, sample and name, right to left, a line by the tallest name | `ChartExportLegendTest` (the same four targets) |
| The other fourteen kinds: the button's place, each kind's file's name, event and text, the place pointed at not being in the file, the middle of the pie and of the gauge, the heatmap's scale and right to left, the button row's relation to the height | `ChartExportKindsTest` (the same four targets) |
| The other kinds on the web | On 2026-10-15 the PNGs of a pie, a heatmap, a gauge, a progress circle, a sparkline and a Gantt were made in the web demo and compared with the screen (the tooltip of the cell under the mouse was not in the file); their SVGs and PDFs were not opened in a browser |
| The PNG's pixels: the ground around, the chart inside | `ChartExportDesktopTest` (the desktop, read back with Skia), `ChartExportOnAndroidTest` (Android, Robolectric: with Android's own bitmap) |
| The look of the three kinds | The files made on the desktop were looked at with macOS's drawing (Quick Look, `sips`): screen densities 1 and 2.75 |
| Making them on Android | In the emulator (screen density 3.5) the three kinds were made; the PNG was shown back in the demo, and the three files were taken off the device and drawn on macOS |
| Making them on iOS | In the simulator (screen density 3) the three kinds were made; the PNG was shown back in the demo, and the three files were taken off the simulator and drawn on macOS |
| The demo code in section 3 | Compiled on four platforms. In the iOS simulator, PDF was chosen and "Save the file" pressed: the share sheet opened with the file's name, type and size ("visits", PDF, 5 KB), and "Save to Files" was in the sheet; it was not saved anywhere |
:::

Not verified: the files of a chart with axes could not be looked at on the web (the browser pane was not on the screen; the tests run in a browser, but a file made was not opened in a browser); of the other kinds, only the PNG was looked at on the web, and their files were not looked at on Android and iOS; the PDF was not looked at in the drawing of Adobe Acrobat, Chrome and Firefox (only in macOS's); the SVG was not looked at in a browser and in a drawing program (only in macOS's Quick Look); the demo code in section 3 was only compiled on Android, the desktop and the web, not tried (Android's emulator was then used for another job; the desktop's save dialog was not opened; the web could not be looked at); the sketch of Android's save dialog was not compiled; printed output was not looked at; it was not tried on a real phone.
