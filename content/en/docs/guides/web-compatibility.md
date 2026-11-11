---
title: An app that opens in every browser
description: Why a Compose Multiplatform web app does not open at all in some browsers, how we found out, and how we mended it. The problem is not Pantrix's: it shows the same way in every Compose Multiplatform app whose web target is Wasm alone (wasmJs).
---

## 1. The problem {#problem}

### Symptom

*   The page opens, but the app does not come. What shows depends on the page:
    *   A page that writes nothing but the app stays **blank and white**.
    *   A page that shows a loading mark shows it **for ever**.
    *   On a site that writes each page's static HTML (as Pantrix's does), the **unstyled text** of that HTML shows in the app's place: links, headings, plain text.
*   All is well in a desktop browser and in the iOS simulator; the problem comes in the Android emulator, on an old phone, or on some real iPhones.
*   The console has one of these lines:

```text
CompileError: WebAssembly.Module(): Unknown type code 0x5e, enable with --experimental-wasm-gc @+11
Please make sure that your runtime environment supports the latest version of Wasm GC and Exception-Handling proposals.
```

:::sketch web-compatibility
:::

### Cause

*   Kotlin/Wasm's output rests on two Wasm proposals: **garbage collection** (GC) and **exceptions** (exception handling). The first versions that have them:

:::table
| Browser | First version |
| :--- | :--- |
| Chrome, Edge, Chrome and WebView on Android | 119 |
| Firefox | 120 |
| Safari, every browser on iOS (WebKit) | 18.2 |
:::

*   An older browser cannot compile the module, and the app never starts. The Chrome in the Android emulator's system images is often old (Chrome 113 in the Android 14 image); on iPhones that cannot move to iOS 18.2, Safari and Chrome are without Wasm GC.
*   In a browser that has the features, the Wasm build can still fail to open. A phone's browser can go past the memory it sets aside for compiling a module. A Compose web app is two large modules (14.5 MB of app and 8.6 MB of Skia for us). A module the simulator compiles with the Mac's memory can be refused on a phone.
*   **On WebKit 27.2 the Wasm build stays blank.** In the WebKit of Safari 27.2 and iOS 27.2, the Wasm compiler, once warm, compiles Compose's measuring of text wrong: the app draws its first frame, then stops on a `NullPointerException` in `SkiaParagraph.getBoundingBox` as it asks for the box of a text with a link (`LinkAnnotation`), and the page stays blank. The same app's JS build draws in the same browser; on iOS 27.0's WebKit the Wasm build draws too. The error comes after the first frame, as the app's own and not Wasm's, so the loader does not go over to JS (5.2).
*   `composeCompatibilityBrowserDistribution`, in Compose's own Gradle plugin, joins the JS and Wasm distributions and tests the browser once, as the page opens. Where the Wasm build passes the test and fails after, it does not go over to JS: it does nothing for the second case.

### Diagnosis

*   **The version of Chrome in the Android emulator:**

```bash
adb shell dumpsys package com.android.chrome | grep versionName
```

*   **The console of the emulator's Chrome,** from the Mac over the DevTools protocol: the forward first, then `http://127.0.0.1:9333/json/list` lists the pages, and each page's `webSocketDebuggerUrl` is connected to.

```bash
adb forward tcp:9333 localabstract:chrome_devtools_remote
```

*   **A local server in the emulator:** `adb reverse tcp:4004 tcp:4004`; the emulator takes `http://localhost:4004` to the server on the Mac.
*   **The console of a real iPhone:** on the iPhone, Settings → Safari → Advanced → Web Inspector, the phone joined to the Mac by cable, and the phone's page chosen in the Develop menu of the Mac's Safari.
*   **Which proposals the Wasm rests on:** Node's V8 is run with one proposal turned off; where the module does not compile, it rests on it. Kotlin 2.4.21's output uses the old exceptions proposal (`legacy-eh`), not the new one (`exnref`).

```bash
node --no-experimental-wasm-legacy-eh -e "new WebAssembly.Module(require('fs').readFileSync('app.wasm'))"
```

## 2. The solution: two builds and a loader {#fix}

:::table
| Part | What it does |
| :--- | :--- |
| A `js` target | The app's same code is built with Kotlin/JS too. Skia is still Wasm, but plain Wasm with no GC: every browser runs it |
| Two scripts of their own names | `site-wasm.js` and `site-js.js`; both go in the same folder |
| A loader (`site.js`) | Tests the browser with Compose's own module and chooses the build; where the Wasm build fails with a Wasm error before it has drawn its first frame, it starts the JS build; where that fails too, it shows the static text |
| A mark on the page | `<html class="app">` in a browser that has Wasm: the static text is hidden, the app's box is shown |
:::

The moment the app draws its first frame is the moment it takes the loading mark away: from it, the loader tells an error of the app's opening from one of some work after it.

## 3. Gradle {#gradle}

### 3.1. The targets

The app's web module is built for two targets, with two script names. Every multiplatform module the app uses gets the `js` target too.

```kotlin
kotlin {
    js {
        browser {
            commonWebpackConfig { outputFileName = "site-js.js" }
        }
        binaries.executable()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig { outputFileName = "site-wasm.js" }
        }
        binaries.executable()
    }
}
```

Where the common tests of the app's modules already run in a browser on Wasm, the browser tests on JS can be turned off (`js { browser { testTask { enabled = false } } }`): the JS build is the same code. That the tests compile for JS is still checked (`compileTestKotlinJs`).

### 3.2. One folder

The JS distribution's script and Skia's Wasm, named by its hash, are copied into the Wasm distribution's folder; the page, the styles and the resources are in the Wasm distribution already. Skia's Wasm is the same file in both builds, has the same name and is downloaded once.

```kotlin
val copyJs = tasks.register<Copy>("copyJsApp") {
    mustRunAfter("jsBrowserDistribution")
    from(layout.buildDirectory.dir("dist/js/productionExecutable")) {
        include("site-js.js", "*.wasm")
        exclude("skiko.wasm")
    }
    into(layout.buildDirectory.dir("dist/wasmJs/productionExecutable"))
}
tasks.named("wasmJsBrowserDistribution") {
    dependsOn("jsBrowserDistribution")
    finalizedBy(copyJs)
}
```

*   The JS distribution is a **dependency** of the Wasm distribution, not of the copy. Where the copy is a finalizer that depends on the JS distribution, Gradle stops with "Unable to make progress running work": Kotlin's JS tasks have finalizers of their own, and Gradle cannot run a finalizer that waits on tasks with finalizers.
*   `skiko.wasm` (the name without a hash) is asked for by the script only where the page gives its own `locateFile`; the script asks for the copy named by its hash.

### 3.3. The webpack plugin that takes the paths out

The web module's `webpack.config.d/stripFilePaths.js`, which Kotlin's webpack setup takes in by itself (section 7):

```javascript
// Takes the path of the machine that built a script out of it.
//
// Skiko's skiko.mjs reads `import.meta.url`, and webpack writes it into the script as the module's own path where it
// was built: "file:///Users/<name>/…/kotlin/skiko.mjs", the builder's user name and folders in a public file. The
// script reads it only for the folder it was loaded from, which it asks for nothing here: it finds Skiko's Wasm by the
// address webpack gives it. Each such string becomes an empty one, which the script takes for no path at all. It is
// done after the script is minified and before its source map is written, through a ReplaceSource, so that the map
// still matches the script. A development build writes each module into a string that it evaluates, where the path's
// quotes are escaped, `\"file:///…\"`: the empty string keeps their escape, or it would end the module's string.
config.plugins.push({
    apply(compiler) {
        const { Compilation, sources } = compiler.webpack;
        compiler.hooks.thisCompilation.tap("StripFilePaths", (compilation) => {
            compilation.hooks.processAssets.tap(
                { name: "StripFilePaths", stage: Compilation.PROCESS_ASSETS_STAGE_OPTIMIZE_SIZE + 1 },
                () => {
                    for (const asset of compilation.getAssets()) {
                        if (!/\.m?js$/.test(asset.name)) continue;
                        const text = asset.source.source().toString();
                        const paths = [...text.matchAll(/(\\?)"file:\/\/\/[^"\\]*\1"/g)];
                        if (paths.length === 0) continue;
                        const stripped = new sources.ReplaceSource(asset.source);
                        for (const path of paths) {
                            const quote = `${path[1]}"`;
                            stripped.replace(path.index, path.index + path[0].length - 1, quote + quote);
                        }
                        compilation.updateAsset(asset.name, stripped);
                    }
                }
            );
        });
    }
});
```

## 4. The code {#the-code}

*   **`wasmJsMain` → `webMain`.** In Kotlin 2.4, most code that speaks to the browser (`JsAny`, `JsString`, `toJsString`, `js("...")`, `kotlinx.browser.window`) is shared by the two web targets and moves to `webMain` as it is. The resources too (`index.html`, styles, icons) go to `webMain/resources`.
*   **Something only Wasm has** becomes an `expect`/`actual`. For example, the code that gives Compose's resource reader the Wasm memory (`kotlin.wasm.unsafe.wasmMemory`): its real form in `wasmJsMain`, an empty `actual` in `jsMain`.
*   **`fetch`:** JavaScript's declaration of `window.fetch` has no default for its second parameter: `window.fetch(url, RequestInit())`.
*   **`JsException` is an alias of `Throwable` in JavaScript.** `catch (e: JsException)` catches the coroutine's cancellation too in JS; the cancellation is sent up first:

```kotlin
} catch (cancelled: CancellationException) {
    throw cancelled
} catch (failure: JsException) {
    null
}
```

*   **`JsString.toString()`** is a redundant conversion in JS (`JsString` is Kotlin's `String` there) and warns; in Wasm it is needed. The warning is suppressed where it stands, with its reason: `@Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")`.

## 5. The loader and the page {#the-loader-and-the-page}

### 5.1. The page

```html
<head>
    <!-- A browser with Wasm runs the app: it hides the static text. -->
    <script>
        if (typeof WebAssembly === "object") document.documentElement.className = "app";
    </script>
</head>
<body>
    <div id="loading">…</div>
    <div id="app"></div>
    <script type="application/javascript" src="site.js"></script>
</body>
```

```css
html.app #static { display: none; }
html:not(.app) #loading,
html:not(.app) #app { display: none; }
```

The app takes `#loading` away once it has drawn its first frame (Compose's `ComposeViewport` is set in `#app`; the mark stands over it).

### 5.2. `site.js`

```javascript
(function () {
    "use strict";
    // The module of Compose's loader: a struct type and a `try`. A browser that takes it has both GC and exceptions.
    var FEATURES = new Uint8Array([
        0, 97, 115, 109, 1, 0, 0, 0, 1, 8, 2, 95, 1, 120, 0, 96, 0, 0, 3, 3, 2, 1, 1, 10, 14, 2, 6, 0, 6, 64, 25, 11,
        11, 5, 0, 208, 112, 26, 11, 0, 45, 4, 110, 97, 109, 101, 1, 15, 2, 0, 5, 102, 117, 110, 99, 48, 1, 5, 102, 117,
        110, 99, 49, 4, 8, 1, 0, 5, 116, 121, 112, 101, 48, 10, 11, 1, 0, 1, 0, 6, 102, 105, 101, 108, 100, 48
    ]);
    var WASM = "site-wasm.js", JS = "site-js.js";
    var starting = null;

    function drawn() { return !document.getElementById("loading"); }
    function load(src, onFailure) {
        var script = document.createElement("script");
        script.src = src;
        script.onerror = onFailure;
        document.body.appendChild(script);
    }
    function showStaticWords() {
        starting = null;
        document.documentElement.classList.remove("app");
    }
    function startJavaScript() {
        starting = "js";
        var box = document.getElementById("app");
        while (box && box.firstChild) box.removeChild(box.firstChild);
        load(JS, showStaticWords);
    }
    function wasmFailed(error) {
        var wasm = typeof WebAssembly === "object";
        return error instanceof RangeError || wasm && (error instanceof WebAssembly.CompileError ||
            error instanceof WebAssembly.LinkError || error instanceof WebAssembly.RuntimeError);
    }
    function thrownBy(error, script) { return String(error && error.stack || "").indexOf(script) >= 0; }
    // WebKit: Safari and every browser on iOS. Those built on Chrome name Chrome in their user agent as well.
    function webKit() {
        var agent = typeof navigator === "object" && navigator.userAgent || "";
        return /AppleWebKit\//.test(agent) && !/Chrom(e|ium)\//.test(agent);
    }
    function failed(error) {
        if (starting === null || drawn() || !wasmFailed(error)) return;
        if (starting === "wasm") startJavaScript();
        else if (!thrownBy(error, WASM)) showStaticWords();
    }

    window.addEventListener("error", function (event) { failed(event.error); });
    window.addEventListener("unhandledrejection", function (event) { failed(event.reason); });

    var collected = false;
    try { collected = typeof WebAssembly === "object" && WebAssembly.validate(FEATURES); } catch (e) {}
    if (collected && !webKit()) { starting = "wasm"; load(WASM, startJavaScript); }
    else if (typeof WebAssembly === "object") startJavaScript();
    else showStaticWords();
})();
```

*   **WebKit is given the JS build,** though it has Wasm GC: WebKit 27.2's Wasm compiler leaves the Wasm build blank (section 1). The browsers on iOS do not say WebKit's version in their user agent (iOS 27's even writes the system as `18_7`), so the rule looks at the engine, not the version.
*   Only **Wasm's own errors** (`CompileError`, `LinkError`, `RuntimeError`) and no memory left (`RangeError`) move to JS. Where one of the app's own requests fails (an API call, say), the build does not change.
*   After the JS build has started, what is left of the Wasm build can throw too: Skia's Wasm, loaded on its own, throws its own error after the app's module has failed. An error whose stack runs through `site-wasm.js` is not counted as the JS build's; were it counted, a JS build that works would stay behind the static text.

## 6. The server {#the-server}

*   **Caching:** Wasm files named by their hash for a year, `immutable`; the scripts (`site.js`, `site-wasm.js`, `site-js.js`) and the pages `no-cache`. So a new deploy is seen at once, and an old script never looks for a Wasm that is no longer there.

```nginx
map $uri $site_cache {
    "~^/[0-9a-f]{16,}\.wasm$" "public, max-age=31536000, immutable";
    default                   "no-cache";
}
```

*   **The content security policy:** `script-src 'self' 'wasm-unsafe-eval'` and the hash of the page's inline script. When the inline script changes, so does its hash. The JS build reported no violation of this policy (measured with the script of section 5, in report-only mode).
*   **Sizes (gzipped):** about 7.2 MB on the Wasm way (app 3.8, Skia 3.3, script 0.06), about 6.7 MB on the JS way (script 3.4, Skia 3.3). A visitor downloads only one.

## 7. Pitfalls {#pitfalls}

:::table
| Pitfall | What happens | What to do |
| :--- | :--- | :--- |
| The tab behind | Where the tab DevTools drives is not the one in front, the browser stops drawing frames: the app draws no first frame, the loading mark stays, and no error is written | The tab tried is brought to the front (`/json/activate/<id>`) |
| The emulator's Chrome | The system image's Chrome is old and stays so, not updated | It is of use for just that reason: it tries the old browser's way |
| A finalizer in Gradle | "Unable to make progress running work" | The JS distribution is a dependency of the Wasm one, the copy only a finalizer (3.2) |
| `JsException` | Catches the cancellation too in JS | The cancellation is sent up first (section 4) |
| Webpack and `import.meta.url` | The `import.meta.url` in Skia's JavaScript is written as the file's path on the machine that builds the script (`file:///Users/…`): a user name and folders in a public file | The script reads it only for the folder it was loaded from, and does not use it here: a webpack plugin turns every `"file:///…"` string into an empty one, after the script is minified and before its map is written (`webpack.config.d/stripFilePaths.js`, section 3.3) |
| The klibs' source paths | Kotlin writes into each module's klib the full path its source files have on the machine that builds it; they reach the Wasm (`/Users/…/src/commonMain/…`): a user name and folders in a public file. The library's klibs write the paths relative to the repository's root from 0.1.1 on | In each of the app's modules that builds for the web, `tasks.withType<Kotlin2JsCompile>().configureEach { compilerOptions.freeCompilerArgs.add("-Xklib-relative-path-base=${rootDir.absolutePath}") }`; `/Users/` is searched for in the Wasm with `strings` (section 8) |
| WebKit 27.2's Wasm compiler | The app draws its first frame, then stops on a `NullPointerException` in `SkiaParagraph.getBoundingBox`; the page stays blank, and the loader does not go over to JS | WebKit is given the JS build (5.2). Any text with a link (`LinkAnnotation`, `PantrixLink`) can set it off; the JS build has none of it |
| The page's language | Where the language changes inside the app, the page's `<html lang>` stays the one it was opened in: a screen reader reads Turkish words with English sounds, and the browser offers to translate from the wrong language | With the library: the outermost `PantrixTheme` writes its `locale`'s language and follows it. Without it: `document.documentElement.setAttribute("lang", tag)` as the language changes |
| The screen reader's copy | Compose 1.12.2 writes the semantics tree into a hidden copy in the DOM (`#cmp_a11y_root`, whose root is `aria-live="polite"`), but from one layer: when a menu, a sheet or a dialog opens the copy turns into its nodes, and when the layer closes it stays so. A snackbar shown after that, and every change of the page, never reaches a screen reader | Compose 1.13.0 keeps every layer; the library upgrades once it is stable. Until then a browser test reads the copy before any layer opens; where none has, a snackbar's message stands in the copy as text and is said |
| A shortcut pressed in a field | While a text field has the focus, Cmd+F, Ctrl+H or F3 that the app answers opens the browser's find bar, its history or its next match as well: Compose handles a key in a field after the browser, and keeps only Tab | With the library: `onShortcut` keeps its own shortcut, and the keys answered otherwise are given to `Modifier.keepFromBrowser(shortcuts)` ([keyboard shortcuts guide](/docs/guides/keyboard-shortcuts), 3.4). Without it: `preventDefault()` for those keys in the window's `keydown`, in the capturing phase, while the focus is in the field |
| `adb screencap` | On an emulator with more than one screen (a foldable), it warns and takes the first | The screen is chosen with `-d <screen>` |
:::

## 8. Verification {#verification}

What was seen (2026-10-31, Pantrix UI's site, on the local stack: the emulator reached it with `adb reverse`, the simulator with `localhost`):

:::table
| Where | Browser | Result |
| :--- | :--- | :--- |
| Android emulator, Android 14 | Chrome 113 (no Wasm GC) | The loader chose `site-js.js`; the home page and Installation were drawn by the JS build |
| Desktop, headless Chrome | Has Wasm GC | Only `site-wasm.js` was loaded, and drew |
| Desktop, headless Chrome, the app's Wasm answered with broken bytes | Has Wasm GC | `CompileError` → the JS build started and drew |
| Desktop, headless Chrome, both Wasm files broken | Has Wasm GC | Neither build could open → the static text showed |
| This repository's demo: the same three ways in the emulator's Chrome 113 and in headless Chrome | All | The Button page was drawn by the JS build; with a broken Wasm it went over to JS; with both Wasm files broken the note "this browser cannot run the demo" stayed |
| iOS simulator, iOS 27.0 | Safari | The Wasm build drew |
| iOS simulator, iOS 27.2 (2026-11-02) | Safari 27.2 | The Wasm build stopped after its first frame on a `NullPointerException` in `SkiaParagraph.getBoundingBox`, and the page stayed blank; the JS build, forced, drew. This repository's demo stayed blank too. With the loader giving WebKit the JS build, both drew |
| Both ways, in report-only mode | Chrome | No violation of the content security policy |
| The library's klibs, with relative paths (2026-11-02) | All | No absolute path is left in `foundation`'s Wasm, JS and iOS klibs, whose sources are written `ui/foundation/src/…`; 0.1.0's Wasm klib held 45 absolute paths |
:::

What was not seen:

*   The certain cause of the white page on a real iPhone: the phone's console was not read. Memory is the strongest explanation; the loader's move to JS covers that case too, but it was not tried on the phone.
*   A real Android phone, Firefox, Safari 18.0–18.1.
*   How long the JS build takes to its first frame on a slow phone.

## 9. Where {#where}

Pantrix UI's site (the `pantrix-ui-site` repository):

:::table
| File | What |
| :--- | :--- |
| `build-logic/.../WebApplicationConventionPlugin.kt` | The two scripts, the JS distribution copied into the Wasm folder |
| `build-logic/.../KmpLibraryConventionPlugin.kt` | The `js` target in every module, its browser tests off |
| `app/webApp/src/webMain/resources/site.js` | The loader |
| `app/webApp/src/webMain/resources/index.html` | The `app` mark and the script that calls the loader |
| `app/webApp/src/{webMain,wasmJsMain,jsMain}/kotlin/.../WasmMemory*.kt` | Code only Wasm has, as `expect`/`actual` |
| `deploy/check.sh` | The live check of the loader, the two builds and the Wasm files |
| `app/webApp/webpack.config.d/stripFilePaths.js` | The webpack plugin that takes the building machine's path out of the scripts |
:::

This repository's web demo (`app/webApp`) has the same solution: `webApp-wasm.js` and `webApp-js.js`, the loader being the `webApp.js` its page loads already. As it has no static text, a note saying so stays in the loading mark's place where neither build can open. The webpack plugin is the same there.

:::table
| File | What |
| :--- | :--- |
| `app/webApp/build.gradle.kts` | The two scripts, the JS distribution copied into the Wasm folder (`copyJsApp`) |
| `app/webApp/src/webMain/resources/webApp.js` | The loader |
| `app/webApp/webpack.config.d/stripFilePaths.js` | Takes the building machine's path out of the scripts |
:::
