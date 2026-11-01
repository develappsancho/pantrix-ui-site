// The built site in a Chrome of its own, for the tests that run in a browser: a server that serves the site as its host
// does, and a page driven over Chrome's DevTools protocol. A test finds what it presses as the other tests of the app
// do, by the tag Pantrix UI gives a component: Compose draws on a canvas, and writes its semantics for a screen reader
// into a hidden copy in the DOM (`#cmp_a11y_root`, in the shadow root of the canvas's host), each element with the
// node's tag as its id, at the node's place, and with no size where the node is out of view. A press is the mouse's,
// on the canvas at the element's middle; words are typed as a keyboard's.
//
// Compose 1.12.2 writes that copy from the newest of a window's layers: a menu, a sheet or a dialog takes it over, and
// once one has closed the copy stays as the layer left it. After such a layer a test reads the page by its address,
// its title and its language, or opens the page again.
//
// SITE_DIST is the built site's folder, and CHROME the browser, where it is not at its usual place on a Mac. Without
// either the tests are skipped, and say so.
import { spawn } from 'node:child_process';
import { existsSync, mkdtempSync, readFileSync, rmSync, statSync } from 'node:fs';
import { createServer } from 'node:http';
import { tmpdir } from 'node:os';
import { extname, join, normalize } from 'node:path';

export const DIST = process.env.SITE_DIST;
const CHROME = process.env.CHROME || '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome';
export const SKIP = !DIST ? 'SITE_DIST names no built site' : !existsSync(CHROME) ? `no Chrome at ${CHROME}` : false;
const TYPES = {
    '.html': 'text/html; charset=utf-8', '.js': 'application/javascript', '.mjs': 'application/javascript',
    '.wasm': 'application/wasm', '.css': 'text/css', '.json': 'application/json', '.svg': 'image/svg+xml',
    '.png': 'image/png', '.ttf': 'font/ttf', '.otf': 'font/otf', '.woff2': 'font/woff2', '.txt': 'text/plain'
};
// How long a page is given to reach what a test waits for, and how often it is asked meanwhile.
const WAIT = 90_000;
const POLL = 100;

// The app has drawn its first frame: it takes the loading mark away (main.kt).
export const DRAWN = '!document.getElementById("loading")';

// The copy of the semantics, wherever Compose has put the canvas's host.
const MIRROR = `(() => {
    for (const host of document.querySelectorAll('*')) {
        const root = host.shadowRoot && host.shadowRoot.querySelector('#cmp_a11y_root');
        if (root) return root;
    }
    return null;
})()`;

export const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

// Waits for `promise` for `ms` at most, with a timer that keeps the run open no longer than the promise does.
function within(promise, ms) {
    let timer;
    const timeout = new Promise((resolve) => { timer = setTimeout(resolve, ms); });
    return Promise.race([promise, timeout]).finally(() => clearTimeout(timer));
}

// Serves the built site as its host does: a file as it is, an address with a page its folder's index.html, any other
// address 404.html with a 404. Hands back its address and a way to stop it.
export async function serve(root = DIST) {
    const server = createServer((request, response) => {
        const path = normalize(decodeURIComponent(new URL(request.url, 'http://site').pathname)).replace(/^(\.\.[/\\])+/, '');
        let file = join(root, path);
        if (existsSync(file) && statSync(file).isDirectory()) file = join(file, 'index.html');
        const found = existsSync(file) && statSync(file).isFile();
        if (!found) file = join(root, '404.html');
        response.writeHead(found ? 200 : 404, { 'Content-Type': TYPES[extname(file)] || 'application/octet-stream' });
        response.end(readFileSync(file));
    });
    await new Promise((resolve) => server.listen(0, '127.0.0.1', resolve));
    return { base: `http://127.0.0.1:${server.address().port}`, close: () => server.close() };
}

// Opens `path` of the site at `base` in a Chrome of its own, in a window `width` by `height`, with `prepare` run in the
// page before any of its scripts, the app's Wasm answered with broken bytes where `breakAppWasm`, and the browser
// saying it is `userAgent` where one is given. Hands back what the page loaded and the ways a test asks the page and
// acts on it.
export async function open({
    base, path = '/', width = 1280, height = 800, prepare = '', breakAppWasm = false, userAgent = null
}) {
    const profile = mkdtempSync(join(tmpdir(), 'site-browser-'));
    const port = 9400 + Math.floor(Math.random() * 400);
    const chrome = spawn(CHROME, ['--headless=new', '--no-first-run', '--no-default-browser-check', '--hide-scrollbars',
        `--user-data-dir=${profile}`, `--remote-debugging-port=${port}`, `--window-size=${width},${height}`,
        'about:blank'], { stdio: 'ignore' });
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
    const nextLoad = () => new Promise((resolve) => { pageLoaded = resolve; });
    let loadedPage = nextLoad();
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
    await send('Emulation.setDeviceMetricsOverride', { width, height, deviceScaleFactor: 1, mobile: false });
    if (userAgent) await send('Emulation.setUserAgentOverride', { userAgent });
    if (prepare) await send('Page.addScriptToEvaluateOnNewDocument', { source: prepare });
    if (appWasm) await send('Fetch.enable', { patterns: [{ urlPattern: `*/${appWasm}` }] });
    await send('Page.navigate', { url: `${base}${path}` });
    // What a test asks is asked of the site's page, not of the blank one before it.
    await within(loadedPage, WAIT);

    const ask = async (expression) =>
        (await send('Runtime.evaluate', { expression, returnByValue: true }))?.result?.value;
    const until = async (expression) => {
        for (const start = Date.now(); Date.now() - start < WAIT; await sleep(POLL)) if (await ask(expression)) return true;
        return false;
    };
    const close = async () => {
        socket.close();
        chrome.kill();
        await sleep(300);
        rmSync(profile, { recursive: true, force: true });
    };
    const report = () => `loaded ${loaded.join(', ')}; problems: ${problems.join(' | ').slice(0, 1500) || 'none'}`;

    // The mirror's element of the node tagged `tag`: its place in the window and what it says, or null.
    const element = (tag) => ask(`(() => {
        const element = ${MIRROR}?.querySelector(${JSON.stringify(`[id="${tag}"]`)});
        if (!element) return null;
        const box = element.getBoundingClientRect();
        return { x: box.x, y: box.y, width: box.width, height: box.height,
            label: element.getAttribute('aria-label') ?? element.textContent };
    })()`);
    const inView = (box) => box && box.width > 0 && box.height > 0 && box.x >= 0 && box.y >= 0 &&
        box.x + box.width <= width && box.y + box.height <= height;
    // Waits `wait` at most until `check` holds, asked again every POLL; fails with `what` and the page's problems where
    // it never does.
    const waitFor = async (what, check, wait = WAIT) => {
        for (const start = Date.now(); Date.now() - start < wait; await sleep(POLL)) if (await check()) return;
        throw new Error(`${what} did not come: ${report()}`);
    };
    // Waits `wait` at most until the node tagged `tag` is in the window and has stopped moving: a menu or a sheet slides
    // in.
    const shown = async (tag, wait = WAIT) => {
        let last = null;
        let box = null;
        await waitFor(`${tag} in view`, async () => {
            box = await element(tag);
            const still = inView(box) && last && box.x === last.x && box.y === last.y;
            last = box;
            return still;
        }, wait);
        return box;
    };
    const mouse = async (type, x, y, extra = {}) =>
        send('Input.dispatchMouseEvent', { type, x, y, button: 'left', buttons: type === 'mouseMoved' ? 0 : 1, ...extra });
    return {
        loaded, problems, ask, until, close, report, waitFor, shown,
        // What the node tagged `tag` says, its accessible name or its words; null where there is none.
        label: async (tag) => (await element(tag))?.label ?? null,
        // Whether a node tagged `tag` is on the screen's tree at all, in view or not.
        has: async (tag) => (await element(tag)) !== null,
        // Presses the node tagged `tag` with the mouse, once it is in view and still.
        click: async (tag) => {
            const box = await shown(tag);
            const x = box.x + box.width / 2;
            const y = box.y + box.height / 2;
            await mouse('mouseMoved', x, y);
            await mouse('mousePressed', x, y, { clickCount: 1 });
            await mouse('mouseReleased', x, y, { clickCount: 1 });
        },
        // Types `text` into what has the focus, as a keyboard's words.
        type: (text) => send('Input.insertText', { text }),
        // Turns the mouse's wheel over the page until the node tagged `tag` is in view.
        scrollTo: async (tag) => {
            for (let i = 0; i < 40; i++) {
                if (inView(await element(tag))) return;
                await send('Input.dispatchMouseEvent',
                    { type: 'mouseWheel', x: width / 2, y: height / 2, deltaX: 0, deltaY: height / 2 });
                await sleep(200);
            }
            throw new Error(`${tag} never came into view: ${report()}`);
        },
        // The address the page shows, its title and its language.
        path: () => ask('location.pathname'),
        title: () => ask('document.title'),
        lang: () => ask('document.documentElement.lang'),
        // Opens `to` in the same tab, afresh, and waits until the app has drawn it.
        goto: async (to) => {
            loadedPage = nextLoad();
            await send('Page.navigate', { url: `${base}${to}` });
            await within(loadedPage, WAIT);
            for (const start = Date.now(); Date.now() - start < WAIT; await sleep(POLL)) if (await ask(DRAWN)) return;
            throw new Error(`${to} was not drawn: ${report()}`);
        },
        // What the window shows, as PNG bytes.
        screenshot: async () => Buffer.from((await send('Page.captureScreenshot', { format: 'png' })).data, 'base64'),
        back: () => ask('history.back()')
    };
}

// The app's own Wasm: the one the Wasm build names that the JavaScript build, which draws with Skiko alone, does not.
function appWasmFile() {
    const named = (script) => new Set(readFileSync(join(DIST, script), 'utf8').match(/[0-9a-f]{16,}\.wasm/g) ?? []);
    const js = named('site-js.js');
    return [...named('site-wasm.js')].find((file) => !js.has(file));
}
