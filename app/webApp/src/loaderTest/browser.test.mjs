// The page's loader, site.js, in Chrome, with the site as it is built: the Wasm build starts where the browser runs it,
// the JavaScript build where it has no Wasm garbage collection or where the app's Wasm is broken, and the page's static
// words where the browser has no Wasm at all. `./gradlew :app:webApp:loaderBrowserTest` builds the site and runs this
// with Node's own test runner: SITE_DIST is the built site's folder, and CHROME the browser, where it is not at its
// usual place on a Mac. Without a Chrome the tests are skipped, and say so.
import { test, before, after } from 'node:test';
import assert from 'node:assert/strict';
import { spawn } from 'node:child_process';
import { existsSync, mkdtempSync, readFileSync, rmSync, statSync } from 'node:fs';
import { createServer } from 'node:http';
import { tmpdir } from 'node:os';
import { extname, join, normalize } from 'node:path';

const DIST = process.env.SITE_DIST;
const CHROME = process.env.CHROME || '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome';
const SKIP = !DIST ? 'SITE_DIST names no built site' : !existsSync(CHROME) ? `no Chrome at ${CHROME}` : false;
const TYPES = {
    '.html': 'text/html; charset=utf-8', '.js': 'application/javascript', '.mjs': 'application/javascript',
    '.wasm': 'application/wasm', '.css': 'text/css', '.json': 'application/json', '.svg': 'image/svg+xml',
    '.png': 'image/png', '.ttf': 'font/ttf', '.otf': 'font/otf', '.woff2': 'font/woff2', '.txt': 'text/plain'
};
// How long a page is given to reach what a test waits for: the development builds are large.
const WAIT = 90_000;

let server;
let base;

// Serves the built site as its host does: a file as it is, an address with a page its folder's index.html, any other
// address 404.html with a 404.
function serve(root) {
    return createServer((request, response) => {
        const path = normalize(decodeURIComponent(new URL(request.url, 'http://site').pathname)).replace(/^(\.\.[/\\])+/, '');
        let file = join(root, path);
        if (existsSync(file) && statSync(file).isDirectory()) file = join(file, 'index.html');
        const found = existsSync(file) && statSync(file).isFile();
        if (!found) file = join(root, '404.html');
        response.writeHead(found ? 200 : 404, { 'Content-Type': TYPES[extname(file)] || 'application/octet-stream' });
        response.end(readFileSync(file));
    });
}

before(async () => {
    if (SKIP) return;
    server = serve(DIST);
    await new Promise((resolve) => server.listen(0, '127.0.0.1', resolve));
    base = `http://127.0.0.1:${server.address().port}`;
});

after(() => server?.close());

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

// Waits for `promise` for `ms` at most, with a timer that keeps the run open no longer than the promise does.
function within(promise, ms) {
    let timer;
    const timeout = new Promise((resolve) => { timer = setTimeout(resolve, ms); });
    return Promise.race([promise, timeout]).finally(() => clearTimeout(timer));
}

// Opens the home page in a Chrome of its own, with `prepare` run in the page before any of its scripts, and the app's
// Wasm answered with broken bytes where `breakAppWasm`. Hands back what the page loaded and a way to ask it things.
async function open({ prepare = '', breakAppWasm = false } = {}) {
    const profile = mkdtempSync(join(tmpdir(), 'site-loader-'));
    const port = 9400 + Math.floor(Math.random() * 400);
    const chrome = spawn(CHROME, ['--headless=new', '--no-first-run', '--no-default-browser-check', '--hide-scrollbars',
        `--user-data-dir=${profile}`, `--remote-debugging-port=${port}`, '--window-size=1280,800', 'about:blank'],
    { stdio: 'ignore' });
    let targets = [];
    for (let i = 0; i < 100 && !targets.some((t) => t.type === 'page'); i++) {
        try { targets = await (await fetch(`http://127.0.0.1:${port}/json/list`)).json(); } catch { await sleep(100); }
    }
    const socket = new WebSocket(targets.find((t) => t.type === 'page').webSocketDebuggerUrl);
    await new Promise((resolve) => socket.addEventListener('open', resolve));
    let id = 0;
    const pending = new Map();
    const loaded = [];
    // What the page wrote as an error, and what it failed to load: what a failing test says.
    const problems = [];
    const appWasm = breakAppWasm ? appWasmFile() : null;
    const send = (method, params = {}) => new Promise((resolve) => {
        const n = ++id;
        pending.set(n, resolve);
        socket.send(JSON.stringify({ id: n, method, params }));
    });
    let pageLoaded;
    const loadedPage = new Promise((resolve) => { pageLoaded = resolve; });
    socket.addEventListener('message', (event) => {
        const message = JSON.parse(event.data);
        if (message.method === 'Page.loadEventFired') pageLoaded();
        if (message.id && pending.has(message.id)) { pending.get(message.id)(message.result); pending.delete(message.id); }
        if (message.method === 'Network.requestWillBeSent') loaded.push(new URL(message.params.request.url).pathname);
        if (message.method === 'Runtime.exceptionThrown') {
            problems.push(message.params.exceptionDetails.exception?.description ?? message.params.exceptionDetails.text);
        }
        if (message.method === 'Runtime.consoleAPICalled' && message.params.type === 'error') {
            problems.push(message.params.args.map((arg) => arg.value ?? arg.description ?? '').join(' '));
        }
        if (message.method === 'Network.loadingFailed') problems.push(`${message.params.errorText}: ${message.params.requestId}`);
        if (message.method === 'Fetch.requestPaused') {
            const { requestId } = message.params;
            send('Fetch.fulfillRequest', { requestId, responseCode: 200,
                responseHeaders: [{ name: 'Content-Type', value: 'application/wasm' }],
                body: Buffer.from('not a module').toString('base64') });
        }
    });
    await send('Network.enable');
    await send('Page.enable');
    await send('Runtime.enable');
    if (prepare) await send('Page.addScriptToEvaluateOnNewDocument', { source: prepare });
    if (appWasm) await send('Fetch.enable', { patterns: [{ urlPattern: `*/${appWasm}` }] });
    await send('Page.navigate', { url: `${base}/` });
    // What a test asks is asked of the site's page, not of the blank one before it.
    await within(loadedPage, WAIT);
    const ask = async (expression) =>
        (await send('Runtime.evaluate', { expression, returnByValue: true }))?.result?.value;
    const until = async (expression) => {
        for (const start = Date.now(); Date.now() - start < WAIT; await sleep(250)) if (await ask(expression)) return true;
        return false;
    };
    const close = async () => {
        socket.close();
        chrome.kill();
        await sleep(300);
        rmSync(profile, { recursive: true, force: true });
    };
    const report = () => `loaded ${loaded.join(', ')}; problems: ${problems.join(' | ').slice(0, 1500) || 'none'}`;
    return { loaded, ask, until, close, report };
}

// The app's own Wasm: the one the Wasm build names that the JavaScript build, which draws with Skiko alone, does not.
function appWasmFile() {
    const named = (script) => new Set(readFileSync(join(DIST, script), 'utf8').match(/[0-9a-f]{16,}\.wasm/g) ?? []);
    const js = named('site-js.js');
    return [...named('site-wasm.js')].find((file) => !js.has(file));
}

const DRAWN = '!document.getElementById("loading")';
const STATIC_WORDS = '!document.documentElement.classList.contains("app")';

test('a browser that runs Wasm garbage collection starts the Wasm build alone, which draws', { skip: SKIP }, async () => {
    const page = await open();
    try {
        assert.equal(await page.until(DRAWN), true, `the app drew its first frame: ${page.report()}`);
        assert.ok(page.loaded.includes('/site-wasm.js'));
        assert.ok(!page.loaded.includes('/site-js.js'));
    } finally { await page.close(); }
});

test('a browser without Wasm garbage collection starts the JavaScript build, which draws', { skip: SKIP }, async () => {
    const page = await open({ prepare: 'WebAssembly.validate = () => false;' });
    try {
        assert.equal(await page.until(DRAWN), true, `the app drew its first frame: ${page.report()}`);
        assert.ok(page.loaded.includes('/site-js.js'));
        assert.ok(!page.loaded.includes('/site-wasm.js'));
    } finally { await page.close(); }
});

test('a broken Wasm of the app starts the JavaScript build, which draws', { skip: SKIP }, async () => {
    const page = await open({ breakAppWasm: true });
    try {
        assert.equal(await page.until(DRAWN), true, `the app drew its first frame: ${page.report()}`);
        assert.ok(page.loaded.includes('/site-wasm.js'));
        assert.ok(page.loaded.includes('/site-js.js'));
    } finally { await page.close(); }
});

test('a browser with no Wasm starts no build and shows the page\'s static words', { skip: SKIP }, async () => {
    const page = await open({ prepare: 'delete globalThis.WebAssembly;' });
    try {
        assert.equal(await page.until(STATIC_WORDS), true, `the static words are shown: ${page.report()}`);
        assert.ok(!page.loaded.some((path) => path === '/site-wasm.js' || path === '/site-js.js'));
        assert.ok(await page.ask('document.body.innerText.trim().length > 0'), 'the words have something to say');
    } finally { await page.close(); }
});
