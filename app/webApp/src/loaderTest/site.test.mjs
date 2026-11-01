// The page's loader, site.js, run as it is in a browser made of stand-ins: which build it starts, and what it does when
// a build fails. `./gradlew :app:webApp:loaderTest` runs it with Node's own test runner;
// ../browserTest/loader.test.mjs runs the same loader in Chrome.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import vm from 'node:vm';

const SOURCE = readFileSync(new URL('../webMain/resources/site.js', import.meta.url), 'utf8');
const WASM = 'site-wasm.js';
const JS = 'site-js.js';
// The user agents of Chrome on a Mac, of Safari 27.2 on a Mac, and of Chrome on an iPhone, which draws with WebKit.
const CHROME = 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) ' +
    'Chrome/142.0.0.0 Safari/537.36';
const SAFARI = 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) ' +
    'Version/27.2 Safari/605.1.15';
const CHROME_ON_IOS = 'Mozilla/5.0 (iPhone; CPU iPhone OS 18_7 like Mac OS X) AppleWebKit/605.1.15 ' +
    '(KHTML, like Gecko) CriOS/142.0.0.0 Mobile/15E148 Safari/604.1';

// Runs the loader in a browser whose Wasm is `wasm`: "gc", with garbage collection; "plain", without it; "throws",
// when its test throws; "none", no Wasm at all. The browser says it is `agent`, Chrome unless told. What it hands back
// drives the page and reads what the loader did.
function load(wasm, agent = CHROME) {
    const scripts = [];
    const listeners = {};
    const classes = new Set(['app']);
    let loading = true;
    // The app's box, holding what a build may have drawn into it before it failed.
    const box = {
        children: ['canvas'],
        get firstChild() { return this.children[0] ?? null; },
        removeChild(child) { this.children.splice(this.children.indexOf(child), 1); }
    };
    const document = {
        getElementById: (id) => (id === 'loading' ? (loading ? {} : null) : id === 'app' ? box : null),
        createElement: () => ({}),
        body: { appendChild: (script) => scripts.push(script) },
        documentElement: { classList: { remove: (name) => classes.delete(name) } }
    };
    const warnings = [];
    const context = {
        document,
        window: { addEventListener: (type, listener) => { listeners[type] = listener; } },
        navigator: { userAgent: agent },
        console: { warn: (message) => warnings.push(message) },
        Uint8Array,
        String,
        RangeError
    };
    // A context of Node's has a WebAssembly of its own, which a browser with none must not have.
    context.WebAssembly = undefined;
    if (wasm !== 'none') {
        context.WebAssembly = {
            validate: () => {
                if (wasm === 'throws') throw new TypeError('This browser cannot read the module.');
                return wasm === 'gc';
            },
            CompileError: WebAssembly.CompileError,
            LinkError: WebAssembly.LinkError,
            RuntimeError: WebAssembly.RuntimeError
        };
    }
    vm.createContext(context);
    vm.runInContext(SOURCE, context);
    return {
        started: () => scripts.map((script) => script.src),
        failToLoad: (src) => scripts.find((script) => script.src === src).onerror(),
        throwError: (error) => listeners.error({ error }),
        reject: (reason) => listeners.unhandledrejection({ reason }),
        draw: () => { loading = false; },
        showsStaticWords: () => !classes.has('app'),
        box,
        warnings
    };
}

// An error as a build's script throws it: its stack runs through that script.
function thrownBy(error, script) {
    error.stack = `${error.name}: ${error.message}\n    at https://pantrixui.com/${script}:1:1`;
    return error;
}

test('a browser with Wasm garbage collection starts the Wasm build alone', () => {
    const page = load('gc');
    assert.deepEqual(page.started(), [WASM]);
    assert.equal(page.showsStaticWords(), false);
});

test('Safari starts the JavaScript build though it has Wasm garbage collection', () => {
    const page = load('gc', SAFARI);
    assert.deepEqual(page.started(), [JS]);
    assert.equal(page.showsStaticWords(), false);
});

test('a browser on iOS, which draws with WebKit whatever it is called, starts the JavaScript build', () => {
    assert.deepEqual(load('gc', CHROME_ON_IOS).started(), [JS]);
});

test('a browser with plain Wasm starts the JavaScript build', () => {
    assert.deepEqual(load('plain').started(), [JS]);
});

test('a browser whose test of Wasm throws starts the JavaScript build', () => {
    assert.deepEqual(load('throws').started(), [JS]);
});

test('a browser with no Wasm starts no build and shows the page\'s static words', () => {
    const page = load('none');
    assert.deepEqual(page.started(), []);
    assert.equal(page.showsStaticWords(), true);
});

test('the Wasm build\'s script failing to load starts the JavaScript build in an emptied box', () => {
    const page = load('gc');
    page.failToLoad(WASM);
    assert.deepEqual(page.started(), [WASM, JS]);
    assert.deepEqual(page.box.children, []);
});

test('a failure of Wasm before the first frame starts the JavaScript build', () => {
    const compile = load('gc');
    compile.throwError(new WebAssembly.CompileError('The module is broken.'));
    assert.deepEqual(compile.started(), [WASM, JS]);
    // A browser that runs out of memory for the Wasm build rejects its promise with a RangeError.
    const memory = load('gc');
    memory.reject(new RangeError('Out of memory.'));
    assert.deepEqual(memory.started(), [WASM, JS]);
    assert.equal(memory.warnings.length, 1);
});

test('a failure after the first frame is the app\'s own and starts nothing', () => {
    const page = load('gc');
    page.draw();
    page.throwError(new WebAssembly.RuntimeError('unreachable'));
    assert.deepEqual(page.started(), [WASM]);
    assert.equal(page.showsStaticWords(), false);
});

test('an error that is not one of Wasm starts nothing', () => {
    const page = load('gc');
    page.throwError(new TypeError('A property of undefined.'));
    page.reject(new Error('A request failed.'));
    assert.deepEqual(page.started(), [WASM]);
});

test('the JavaScript build failing to load shows the page\'s static words', () => {
    const page = load('plain');
    page.failToLoad(JS);
    assert.equal(page.showsStaticWords(), true);
});

test('a failure of the JavaScript build shows the static words, but not one the stopping Wasm build throws', () => {
    const page = load('gc');
    page.throwError(new WebAssembly.CompileError('The module is broken.'));
    page.throwError(thrownBy(new WebAssembly.RuntimeError('unreachable'), WASM));
    assert.equal(page.showsStaticWords(), false);
    page.throwError(thrownBy(new RangeError('Out of memory.'), JS));
    assert.equal(page.showsStaticWords(), true);
    // Once the words are shown, nothing more is started.
    page.throwError(new WebAssembly.CompileError('The module is broken.'));
    assert.deepEqual(page.started(), [WASM, JS]);
});
