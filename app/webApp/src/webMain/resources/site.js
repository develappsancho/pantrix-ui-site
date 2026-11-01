// Starts the site's app, in the build the browser can run.
//
// Where the browser runs Wasm with its garbage collection, the Wasm build (site-wasm.js). Where it does not, as
// Chrome before 119 and Safari before 18.2, the JavaScript build (site-js.js), which needs only plain Wasm, for Skiko.
// The Wasm build can fail where the test below passes: a phone's browser can run out of memory for its code, or refuse
// a module. A failure of Wasm before the app has drawn its first frame starts the JavaScript build instead. Where that
// fails too, or the browser has no Wasm at all, the page's static words are shown: index.html's `app` mark is taken
// away, and its styles show the words the build wrote for the page.
//
// WebKit, Safari and every browser on iOS, is given the JavaScript build whatever it runs. From WebKit 27.2 on its
// Wasm compiler, once warm, gets the Wasm build's measuring of text wrong: the app draws its first frame and stops on
// an error, and the page stays blank. Which WebKit a browser is cannot be told on iOS, whose browsers name no version
// of it, so WebKit of every version is given the build that draws there.
(function () {
    "use strict";

    // The module Compose's own loader tests a browser with (WebCompatibilityTask): a struct type and a `try`, so a
    // browser that takes it has garbage collection and exception handling, both of which the Wasm build needs.
    var FEATURES = new Uint8Array([
        0, 97, 115, 109, 1, 0, 0, 0, 1, 8, 2, 95, 1, 120, 0, 96, 0, 0, 3, 3, 2, 1, 1, 10, 14, 2, 6, 0, 6, 64, 25, 11,
        11, 5, 0, 208, 112, 26, 11, 0, 45, 4, 110, 97, 109, 101, 1, 15, 2, 0, 5, 102, 117, 110, 99, 48, 1, 5, 102, 117,
        110, 99, 49, 4, 8, 1, 0, 5, 116, 121, 112, 101, 48, 10, 11, 1, 0, 1, 0, 6, 102, 105, 101, 108, 100, 48
    ]);
    var WASM = "site-wasm.js";
    var JS = "site-js.js";

    // The build that is starting: "wasm" or "js", and null once there is none left to try.
    var starting = null;

    // The app removes the loading mark once it has drawn its first frame (main.kt): a failure after that is the app's.
    function drawn() {
        return !document.getElementById("loading");
    }

    function load(src, onFailure) {
        var script = document.createElement("script");
        script.type = "application/javascript";
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
        // What the Wasm build may have set in the app's box before it failed.
        var box = document.getElementById("app");
        while (box && box.firstChild) box.removeChild(box.firstChild);
        load(JS, showStaticWords);
    }

    // A failure of the browser's Wasm, as opposed to one of the app: a module it cannot compile or link, a trap, or no
    // memory left for it.
    function wasmFailed(error) {
        var wasm = typeof WebAssembly === "object";
        return error instanceof RangeError || wasm && (error instanceof WebAssembly.CompileError ||
            error instanceof WebAssembly.LinkError || error instanceof WebAssembly.RuntimeError);
    }

    // Whether the browser draws with WebKit: Safari, and each browser on iOS. Chrome and the browsers built on it name
    // Chrome in their user agent, and WebKit as well.
    function webKit() {
        var agent = typeof navigator === "object" && navigator.userAgent || "";
        return /AppleWebKit\//.test(agent) && !/Chrom(e|ium)\//.test(agent);
    }

    // Whether the error's stack runs through the script: what the Wasm build still throws as it stops, after the
    // JavaScript build has started, is the Wasm build's failure and not the JavaScript one's.
    function thrownBy(error, script) {
        return String(error && error.stack || "").indexOf(script) >= 0;
    }

    function failed(error) {
        if (starting === null || drawn() || !wasmFailed(error)) return;
        if (starting === "wasm") {
            console.warn("Pantrix UI: the Wasm build failed before it was drawn, the JavaScript build starts.", error);
            startJavaScript();
        } else if (!thrownBy(error, WASM)) {
            console.warn("Pantrix UI: the JavaScript build failed too, the page's words are shown.", error);
            showStaticWords();
        }
    }

    window.addEventListener("error", function (event) {
        failed(event.error);
    });
    window.addEventListener("unhandledrejection", function (event) {
        failed(event.reason);
    });

    var collected = false;
    try {
        collected = typeof WebAssembly === "object" && WebAssembly.validate(FEATURES);
    } catch (unsupported) {
        collected = false;
    }
    if (collected && !webKit()) {
        starting = "wasm";
        load(WASM, startJavaScript);
    } else if (typeof WebAssembly === "object") {
        startJavaScript();
    } else {
        showStaticWords();
    }
})();
