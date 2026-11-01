// The page's loader, site.js, in Chrome, with the site as it is built: the Wasm build starts where the browser runs it,
// the JavaScript build where it has no Wasm garbage collection or where the app's Wasm is broken, and the page's static
// words where the browser has no Wasm at all. `./gradlew :app:webApp:browserTest` builds the site and runs this with
// Node's own test runner (chrome.mjs says what it is given).
import { test, before, after } from 'node:test';
import assert from 'node:assert/strict';
import { DRAWN, SKIP, open, serve } from './chrome.mjs';

const STATIC_WORDS = '!document.documentElement.classList.contains("app")';
// Safari 27.2's user agent on a Mac.
const SAFARI = 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) ' +
    'Version/27.2 Safari/605.1.15';

let site;

before(async () => { if (!SKIP) site = await serve(); });

after(() => site?.close());

test('a browser that runs Wasm garbage collection starts the Wasm build alone, which draws', { skip: SKIP }, async () => {
    const page = await open({ base: site.base });
    try {
        assert.equal(await page.until(DRAWN), true, `the app drew its first frame: ${page.report()}`);
        assert.ok(page.loaded.includes('/site-wasm.js'));
        assert.ok(!page.loaded.includes('/site-js.js'));
    } finally { await page.close(); }
});

test('a browser without Wasm garbage collection starts the JavaScript build, which draws', { skip: SKIP }, async () => {
    const page = await open({ base: site.base, prepare: 'WebAssembly.validate = () => false;' });
    try {
        assert.equal(await page.until(DRAWN), true, `the app drew its first frame: ${page.report()}`);
        assert.ok(page.loaded.includes('/site-js.js'));
        assert.ok(!page.loaded.includes('/site-wasm.js'));
    } finally { await page.close(); }
});

test('a browser that says it is Safari starts the JavaScript build, which draws', { skip: SKIP }, async () => {
    const page = await open({ base: site.base, userAgent: SAFARI });
    try {
        assert.equal(await page.until(DRAWN), true, `the app drew its first frame: ${page.report()}`);
        assert.ok(page.loaded.includes('/site-js.js'));
        assert.ok(!page.loaded.includes('/site-wasm.js'));
    } finally { await page.close(); }
});

test('a broken Wasm of the app starts the JavaScript build, which draws', { skip: SKIP }, async () => {
    const page = await open({ base: site.base, breakAppWasm: true });
    try {
        assert.equal(await page.until(DRAWN), true, `the app drew its first frame: ${page.report()}`);
        assert.ok(page.loaded.includes('/site-wasm.js'));
        assert.ok(page.loaded.includes('/site-js.js'));
    } finally { await page.close(); }
});

test('a browser with no Wasm starts no build and shows the page\'s static words', { skip: SKIP }, async () => {
    const page = await open({ base: site.base, prepare: 'delete globalThis.WebAssembly;' });
    try {
        assert.equal(await page.until(STATIC_WORDS), true, `the static words are shown: ${page.report()}`);
        assert.ok(!page.loaded.some((path) => path === '/site-wasm.js' || path === '/site-js.js'));
        assert.ok(await page.ask('document.body.innerText.trim().length > 0'), 'the words have something to say');
    } finally { await page.close(); }
});
