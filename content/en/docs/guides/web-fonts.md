---
title: A typeface that arrives late on the web
description: Why, on Compose Multiplatform's web target, an app's own typeface arrives a moment late, how we measured it and how we cured it. The problem is not Pantrix's own: it is seen the same way in every Compose Multiplatform project that has a web target and uses its own font file.
---

## 1. The problem {#problem}

### Symptom

*   When the page is first opened in the browser, or reloaded, the text is first drawn in another typeface. Less than a second later all the letters change, the texts are laid out again and the lines shift.
*   When the typeface is changed while the app runs (in ours, "Typeface" in "Settings"), the same happens once more.
*   On Android, iOS and the desktop it is never seen.

On web pages this is called FOUT (*flash of unstyled text*).

:::sketch web-fonts
:::

### Cause

In Compose Multiplatform a font file is read with `Font(Res.font.…)`. This function does two separate things by platform (Compose Multiplatform 1.12.1, `components-resources`):

:::table
| Platform | Reading | In the first frame |
| :--- | :--- | :--- |
| Android, iOS, desktop | Synchronous: the file is read the moment it is asked for (`ResourceState.blocking.kt`) | The typeface is ready |
| Web (`js`, `wasmJs`) | Asynchronous: the file is downloaded with `fetch` (`ResourceState.web.kt`) | There is no typeface |
:::

On the web `Font(…)` returns a **fallback** until the file arrives: the Roboto that Skiko carries inside it (`FontResources.skiko.kt`, `defaultFont`). When the file arrives the value it returns changes, everything that reads it is set up again and every text is laid out again. Three details make the problem bigger:

1.  **The reading starts late.** `Font(…)` is a composable: the download starts only when the app is first being set up, that is, after the Wasm has been downloaded and compiled.
2.  **Every weight is a separate font.** The cache's key is the file's path, its weight, its style and its axis settings. A single variable font registered in seven weights is read seven times and can arrive at seven separate moments: the theme is set up from the start several times.
3.  **It is asked for again at every load.** The resources' cache in the browser (the Cache API) is reset at every page load (`ResourceWebCache.web.kt`); the file is given by the browser's own HTTP cache.

### Measurement

The production bundle (`wasmJsBrowserDistribution`) was opened from a local server; the times were read with a measuring script added to the page (see section 6). The numbers are milliseconds after the page opened.

:::table
| Event | Before the fix | After the fix |
| :--- | :--- | :--- |
| The typeface files were asked for | 408 (after the app was set up; only the theme's file) | 71 (while the code downloads, `<link rel="preload">`; all fifteen files) |
| All the fonts were read | 440 | 274 |
| The app's first frame | 401, **in the fallback typeface** | 456, **in its own typeface** |
| The letters changed | About 500 | Never |
| Another typeface was chosen in "Settings" | Seven files are asked for at that moment; the fallback typeface until they arrive | No new request; it changes in one frame |
:::

Locally there is no network latency, and the difference is a hundred milliseconds; on a real network it lasts as long as the typeface is late. The first frame itself took between 175 and 240 ms in both measurements: waiting for the typeface did not delay the opening, because the files had come down long before that frame was set up.

## 2. The solution: four pieces {#fix}

:::table
| # | Piece | What it gives |
| :--- | :--- | :--- |
| 1 | **Download early.** `<link rel="preload">` in `index.html` | The file downloads while the app's code downloads; when the app asks for it, it is in the browser's cache |
| 2 | **Read first, draw after.** Put the app behind a gate | The app draws its first frame in its own typeface; the letters do not change |
| 3 | **Keep the loading mark.** Set up the app in its own box, not in the body | While waiting, the page's loading mark shows, not an empty page |
| 4 | **Stay with the old one when the typeface changes.** Change once, when the new one arrives | Changing the typeface while the app runs shows no fallback typeface either |
:::

The second piece is the heart of the work, and rests on a property of Compose: **a font read stays in memory**, and the first setup that asks for it later finds it at once. So it is enough to ask for the typeface somewhere first (`preloadFont`) and to set up the app after that; nothing changes in the app itself.

## 3. With Pantrix {#with}

The library gives the Kotlin side of the four pieces (`com.pantrixui.foundation.token.typography`):

:::table
| API | What it does |
| :--- | :--- |
| `PantrixFont(resource, weight, style, variationSettings)` | A font: the file in the resources, and the weight and style the family registers it at. `variationSettings` is its place on a variable font's axes; if it is not given, it is what the weight and the style say. Reading ahead asks with the same settings |
| `PantrixTypeface(vararg fonts)` | A typeface, with its font files. `PantrixTypeface.Default` is the library's own typeface |
| `rememberPantrixTypography(typeface)` | Pantrix's scale, in that typeface. While the typeface changes it stays with the old one, and changes once when the new one arrives |
| `rememberPantrixFontFamily(typeface)` | Only the family (`FontFamily`) |
| `areTypefacesLoaded(vararg typefaces)` | Whether all the fonts of the typefaces given have been read. Asking starts the reading |
| `isPantrixTypefaceLoaded()` | The same, for the library's own typeface |
| `PantrixTypefaceGate(loaded, timeoutMillis, onShown, content)` | Draws the content after the typeface has been read; waits at most `timeoutMillis` |
:::

On Android, iOS and the desktop they all answer at once: the same code is written on every platform, and it waits only on the web.

### 3.1. Say the typeface once

```kotlin
// commonMain
val Brand = PantrixTypeface(
    PantrixFont(Res.font.brand_regular),
    PantrixFont(Res.font.brand_medium, FontWeight.Medium),
    PantrixFont(Res.font.brand_bold, FontWeight.Bold)
)

@Composable
fun App() {
    PantrixTheme(typography = rememberPantrixTypography(Brand)) {
        // …
    }
}
```

The fonts are counted in one place; the family, and whether they have been read in the browser, are both asked of that list. If the two are counted in separate places, one day they will not match (see section 5, "The same key").

An app that uses only the library's own typeface need not say anything: `PantrixTheme { … }` is enough.

### 3.2. The Web entry

```kotlin
// webMain
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // On a page without the box (an old index.html the browser kept), it is set up in the body.
    val box = "app".takeIf { document.getElementById(it) != null }
    ComposeViewport(viewportContainerId = box) {
        PantrixTypefaceGate(
            loaded = areTypefacesLoaded(Brand),
            onShown = { document.getElementById("loading")?.remove() }
        ) {
            App()
        }
    }
}
```

For the library's own typeface `loaded` is not given: `PantrixTypefaceGate(onShown = …) { App() }`.

### 3.3. The page

```html
<head>
    <!-- The typeface is downloaded while the app's code downloads. Without `crossorigin` the browser does not
         count this download as the app's own request, and the file comes down twice. -->
    <link rel="preload" as="fetch" type="font/ttf" crossorigin
          href="composeResources/<package>.generated.resources/font/brand_regular.ttf">
</head>
<body>
    <!-- The loading mark: it stands over the app until the app's first frame is drawn. -->
    <div id="loading">…</div>
    <!-- The box the app is set up in. -->
    <div id="app"></div>
    <script type="application/javascript" src="webApp.js"></script>
</body>
```

```css
html, body { width: 100%; height: 100%; margin: 0; padding: 0; overflow: hidden; }

#app { width: 100%; height: 100%; }

#loading {
    position: fixed;
    inset: 0;
    z-index: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    pointer-events: none;
}
```

The `<package>` in the address is the package of the generated `Res` class of the module that carries the resources. The address of the library's own typeface is `composeResources/com.pantrixui.foundation.generated.resources/font/plus_jakarta_sans.ttf`. To be sure, build the bundle once and look in the `build/dist/…/composeResources` folder.

### 3.4. A typeface that changes while the app runs

`rememberPantrixTypography(typeface)` solves this itself: when `typeface` changes, it goes on giving the old typography until the new one has been read, and gives the new one once, when all of it has arrived.

```kotlin
val typeface = if (settings.brand) Brand else PantrixTypeface.Default
PantrixTheme(typography = rememberPantrixTypography(typeface)) { /* … */ }
```

Between the choice and the change, as much time passes as the files take to download. If that too is not wanted, the typefaces are read ahead: they are all given to the gate together, and the page downloads them all ahead. The sample app in this repository does so, because it has a page that shows three typefaces side by side:

```kotlin
PantrixTypefaceGate(loaded = areTypefacesLoaded(PantrixTypeface.Default, PlusJakartaSans, Poppins)) { App() }
```

The price is the bytes downloaded: in the sample app fifteen files, 2.0 MB. For a product the right thing is mostly to read ahead only the first screen's typeface, and to leave the others to the change.

### 3.5. Characters the fonts lack

In a browser, Compose draws a character none of the app's fonts has (an arrow, `⌘`, an Arabic letter) in a Noto font it fetches from Google's servers (`NotoFontDownloader`, `https://fonts.gstatic.com/s/`). That has three costs:

*   Every visitor's browser asks Google for it.
*   A page that is offline leaves the character blank.
*   A content security policy (CSP) must put `https://fonts.gstatic.com` in its `connect-src`.

Compose has no general setting that turns it off. But it installs what it fetched in a public way, `FontFamily.Resolver.preload`; where a font that has the character was installed that way before, Compose sees no missing character and fetches nothing.

A `PantrixTypeface`'s `fallbacks` are those fonts. They are read with the typeface's fonts and installed in the resolver, and `areTypefacesLoaded` (the gate too) waits for them as well. The library's own typeface, `PantrixTypeface.Default`, carries Pantrix Symbols for the marks a shortcut is written in on Apple's systems and Plus Jakarta Sans lacks: `⌘ ⇧ ⌥ ⌫ ⌦ ⇥ ⇞ ⇟ ⎋`, 3 KB cut from three of Noto's fonts. An app that gives its own typeface adds it itself; an app that writes Arabic gives its own Arabic font too:

```kotlin
val Brand = PantrixTypeface(
    fonts = listOf(PantrixFont(Res.font.brand_regular), PantrixFont(Res.font.brand_bold, FontWeight.Bold)),
    fallbacks = PantrixTypeface.Default.fallbacks + PantrixFont(Res.font.noto_sans_arabic)
)
```

On Android, iOS and the desktop the system's fonts draw such a character; the fallbacks are not read there. Which characters are fetched shows, while the page is open, in `performance.getEntriesByType('resource')` as `fonts.gstatic.com` addresses.

## 4. Without Pantrix {#without}

The same solution, with Compose Multiplatform's own API only. The code below can be copied into any project; it is a simplified form of its counterparts in the library.

### 4.1. Common code (`commonMain`)

```kotlin
/** A font: the file in the resources, and the weight and style the family registers it at. */
data class AppFont(
    val resource: FontResource,
    val weight: FontWeight = FontWeight.Normal,
    val style: FontStyle = FontStyle.Normal
)

/** The fonts' family. In the browser, a font that has not arrived yet is the fallback typeface in the family. */
@Composable
fun rememberFontFamily(fonts: List<AppFont>): FontFamily {
    val read = key(fonts) { fonts.map { Font(it.resource, it.weight, it.style) } }
    return remember(read) { FontFamily(read) }
}

/** Whether the fonts have all been read. Asking starts the reading; outside the web it is always true. */
@Composable
expect fun areFontsLoaded(fonts: List<AppFont>): Boolean

/** [value] itself if it has been read; if not, the last one read. If none has been read, [value] still. */
@Composable
fun <T> lastLoaded(value: T, loaded: Boolean): T {
    var last by remember { mutableStateOf<T?>(null) }
    if (!loaded) return last ?: value
    SideEffect { last = value }
    return value
}

/** Draws [content] after the typeface has been read; waits at most [timeoutMillis]. */
@Composable
fun FontGate(
    loaded: Boolean,
    timeoutMillis: Long = 3000,
    onShown: () -> Unit = {},
    content: @Composable () -> Unit
) {
    var waiting by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(timeoutMillis)
        waiting = false
    }
    // A gate once opened stays open: an app that has been drawn is not set up again.
    var opened by remember { mutableStateOf(false) }
    if (opened || loaded || !waiting) {
        SideEffect { opened = true }
        content()
        val shown by rememberUpdatedState(onShown)
        LaunchedEffect(Unit) {
            // The first frame after the frame that carries the content: that frame has been drawn.
            withFrameNanos { }
            shown()
        }
    }
}
```

### 4.2. Platform counterparts

```kotlin
// webMain
@OptIn(ExperimentalResourceApi::class)
@Composable
actual fun areFontsLoaded(fonts: List<AppFont>): Boolean {
    // All are asked for first, then looked at: the fonts are read side by side, not one after another.
    val read = key(fonts) { fonts.map { preloadFont(it.resource, it.weight, it.style).value } }
    return read.none { it == null }
}
```

```kotlin
// androidMain, iosMain, jvmMain
@Composable
actual fun areFontsLoaded(fonts: List<AppFont>): Boolean = true
```

`preloadFont` exists only in the web source sets and carries `@ExperimentalResourceApi`; it cannot be called from common code. That is why `expect`/`actual` is needed. `webMain` is the source set Kotlin sets up by itself when the `js` and `wasmJs` targets are both defined.

### 4.3. Use

```kotlin
// commonMain
val Brand = listOf(
    AppFont(Res.font.brand_regular),
    AppFont(Res.font.brand_bold, FontWeight.Bold)
)

@Composable
fun App() {
    val family = rememberFontFamily(Brand)
    // For a typeface that changes while the app runs: it stays with the old one until the new one has been read.
    val shown = lastLoaded(family, loaded = areFontsLoaded(Brand))
    // typographyIn: your own function that sets up your typography in the family given.
    MaterialTheme(typography = typographyIn(shown)) { /* … */ }
}
```

```kotlin
// webMain
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val box = "app".takeIf { document.getElementById(it) != null }
    ComposeViewport(viewportContainerId = box) {
        FontGate(
            loaded = areFontsLoaded(Brand),
            onShown = { document.getElementById("loading")?.remove() }
        ) {
            App()
        }
    }
}
```

The page and the style are the same as those in 3.3.

### 4.4. Characters the fonts lack

A fallback font is installed in the window's font resolver once it is read; it is the way Compose installs what it fetches itself:

```kotlin
// webMain
@OptIn(ExperimentalResourceApi::class)
@Composable
fun installFallbacks(fallbacks: List<FontResource>): Boolean {
    val read = fallbacks.map { preloadFont(it).value }
    val resolver = LocalFontFamilyResolver.current
    var installed by remember(fallbacks, resolver) { mutableStateOf(false) }
    LaunchedEffect(read, resolver) {
        val fonts = read.filterNotNull()
        if (fonts.size == fallbacks.size) {
            fonts.forEach { resolver.preload(FontFamily(it)) }
            installed = true
        }
    }
    return fallbacks.isEmpty() || installed
}
```

It is given to the gate with the fonts: `FontGate(loaded = areFontsLoaded(Brand) && installFallbacks(Symbols))`. The `&&` here does not hold the second call back, as both are called at every composition and the read starts at the first call; taking each into a value and joining them after is clearer.

## 5. Pitfalls {#pitfalls}

:::table
| Pitfall | What happens | What is done |
| :--- | :--- | :--- |
| **The same key** | If the font read ahead and the font the theme asks for are not the same file **with the same weight and the same style**, it is not found in the cache; the app is drawn with the fallback again | Count the fonts in one list; set up both the family and the question from that list |
| **Asking with `&&`** | `areLoaded(a) && areLoaded(b)`: the second is not asked at all until the first arrives; the files come down one after the other | Give them all in one call |
| **A preload without `crossorigin`** | The browser does not count what it downloaded as the app's `fetch` request; the file comes down twice and a warning falls into the console | `as="fetch"` and `crossorigin` together |
| **An app set up in the body** | `ComposeViewport` clears the inside of the box it is set up in: the loading mark goes before the app's first frame, and an empty page is left between | Set up the app in its own box; remove the mark after the first frame |
| **A gate with no timeout** | If the file does not arrive (a wrong address, a broken network), the page stays empty for ever | Wait a while, then draw with the fallback |
| **A gate that can close** | If the answer turns back to "not read" later, the app is removed and loses all its state | Let a gate once opened stay open |
| **An old `index.html`** | If the browser kept the old page, there is no box; an app that looks for the box never opens | If there is no box, set up in the body |
| **Addresses written by hand** | When the module's or the file's name changes, the preload downloads for nothing; nothing breaks, the file only comes down late | A test that compares the addresses with the font folders (ours is `WebFontPreloadTest`) |
| **A hidden tab** | The browser gives no frames to a hidden tab; the app is not set up until the tab is visible | It is not a bug; while measuring, see that the tab is visible |
| **Browser tests** | Karma does not serve the resources: in tests the font never arrives, and the question always says "not read" | Give the test your own reader (`LocalResourceReader`) |
| **Reading everything ahead** | Typefaces that will never be used come down to every visitor too | Read ahead only the first screen's typeface; leave the others to the change |
| **An array in `webMain`** | In the source set that `js` and `wasmJs` share, the editor cannot resolve an array's extension: `flatMap` on a `vararg` parameter gives the warning "Cannot access 'kotlin.Cloneable' which is a supertype of 'Array'". The build passes; the warning is only in the editor | Turn the `vararg` into a list in common code; let the platform counterpart take a list (like `areFontsLoaded` in 4.1) |
| **No monospace typeface given** | `FontFamily.Monospace` resolves to no typeface in a browser (Compose on the web reaches no system font): `typography.code`, code blocks and the JSON view are drawn in a proportional fallback, and columns do not line up | Put a monospace typeface in the resources and give it with `rememberPantrixTypography(typeface, codeTypeface = …)`; read it ahead too |
| **A character the font lacks** | Compose fetches a Noto font from Google for it; offline the character stays blank, and a CSP must let Google in | Give a font that has the character as a fallback (3.5): `PantrixTypeface.Default.fallbacks` carries the marks of a Mac's keys |
:::

## 6. Verification {#verification}

### The measuring script

In the browser, exactly when a frame is drawn cannot be seen from outside; the order of the events can. A small script, loaded in `index.html` before the app's script, is added to a copy of the production bundle:

```js
(function () {
  const log = (m) => console.log('[font-probe] ' + Math.round(performance.now()) + 'ms ' + m);
  const isFont = (s) => s.indexOf('/font/') >= 0;
  const origFetch = window.fetch;
  window.fetch = function (u) {
    const s = String((u && u.url) || u);
    if (isFont(s)) log('fetch ' + s.split('/').pop());
    return origFetch.apply(this, arguments);
  };
  // Frames that last longer than a frame: a page set up from the start shows here.
  let last = performance.now();
  const tick = (now) => {
    if (now - last > 24) log('long frame ' + Math.round(now - last) + 'ms');
    last = now;
    if (now < 6000) requestAnimationFrame(tick);
  };
  requestAnimationFrame(tick);
})();
```

The order expected: the fonts have been read **before** the app's first long frame, and after that frame no new request for a font is seen. `performance.getEntriesByType('resource')` tells who asked for the file: if the preload works, `initiatorType` is `"link"` and the file comes down once.

### Tests

:::table
| Test | What it looks at |
| :--- | :--- |
| `ui/foundation`, `PantrixTypefaceWebTest` (in the browser) | A typeface has been read when all its fonts have been read; those asked for together are read together; what has been read is ready at the next first setup; the scale stays with the old typeface until the new one arrives |
| `ui/foundation`, `PantrixTypefaceTest` | Outside the web a typeface has been read from the start; one on its way is not given, the last one read is given |
| `ui/foundation`, `PantrixTypefaceGateTest` | The gate opens when the typeface arrives, stays open once opened, tells after the content has been drawn on the page, and gives up waiting after a while |
| `app/shared`, `WebFontPreloadTest` | The files the page downloads ahead are the same as the files in the font folders; every line carries `as="fetch"` and `crossorigin` |
| `ui/foundation`, `PantrixTypefaceWebTest` (in a browser), fallbacks | Fallbacks are asked for with the fonts; a typeface is read once its fallback is read and installed; a fallback that cannot be read keeps it waiting; a fallback that is no font breaks nothing |
| `ui/foundation`, `PantrixSymbolsOnTheDesktopTest` | Every character a shortcut is written in on Apple's systems is in Plus Jakarta Sans or in Pantrix Symbols |
:::

## 7. Where it is in this repository {#repository toc="7. In the repository"}

:::table
| File | What |
| :--- | :--- |
| `ui/foundation/src/commonMain/kotlin/com/pantrixui/foundation/token/typography/PantrixTypeface.kt` | `PantrixFont`, `PantrixTypeface`, `rememberPantrixFontFamily`, `areTypefacesLoaded`, `isPantrixTypefaceLoaded` |
| `ui/foundation/src/webMain/…/PantrixTypeface.web.kt` | The browser's part of `areTypefacesLoaded`: `areFontsLoaded` (`preloadFont`). The typefaces' fonts are gathered into one list in common code; this part only takes the list |
| `ui/foundation/src/commonMain/…/PantrixTypography.kt` | `rememberPantrixTypography(typeface)`: stays with the old typeface until the new one arrives |
| `ui/foundation/src/commonMain/…/PantrixTypefaceGate.kt` | The gate |
| `app/shared/src/commonMain/kotlin/com/pantrixui/cmp/demo/theme/Typefaces.kt` | The sample app's own two typefaces |
| `app/webApp/src/webMain/kotlin/com/pantrixui/cmp/demo/main.kt` | The web entry: the box, the gate, the removal of the loading mark |
| `app/webApp/src/webMain/resources/index.html`, `styles.css` | The preload lines, `#app` and `#loading` |
| `ui/foundation/src/commonMain/composeResources/font/pantrix_symbols.ttf` | Pantrix Symbols, the library's fallback; its licence beside it: `files/licenses/pantrix_symbols_ofl.txt` |
| `tooling/fonts/pantrix_symbols.py` | Cuts Pantrix Symbols from three of Noto's fonts (fontTools) |
:::

## 8. What is left open {#open}

The items seen in this work and not touched are in `docs/OPEN_ITEMS.md`. The chief ones:

*   The library's typeface is one file, but in the browser it is read seven times; in an app that does not wait for the typeface, the weights arrive one by one.
*   `preloadFont` is experimental (`@ExperimentalResourceApi`); it can change in a later version of Compose Multiplatform.
*   It was seen only in the built-in browser and on the Wasm target; it was not looked at in real browsers or on a slow network.
*   The loading mark stalls while the Wasm compiles, and opens on a white ground on a device with a dark theme too.
