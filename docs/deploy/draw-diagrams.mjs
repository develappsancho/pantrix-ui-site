// Draws the architecture guide's diagrams as pictures: each Mermaid source in ARCHITECTURE_GUIDE.md and its English,
// ARCHITECTURE_GUIDE.en.md, is drawn by Mermaid itself in headless Chrome and saved as the PNG its picture link above
// names, at twice its size. Run it after a diagram's source changes:
//
//   node docs/deploy/draw-diagrams.mjs
//
// It needs Node 24 or newer and Google Chrome (CHROME names another browser of Chromium's), and reads Mermaid from
// jsDelivr.
import { spawn } from 'node:child_process';
import { mkdtempSync, readFileSync, rmSync, writeFileSync } from 'node:fs';
import { createServer } from 'node:http';
import { tmpdir } from 'node:os';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const here = dirname(fileURLToPath(import.meta.url));
const GUIDES = ['ARCHITECTURE_GUIDE.md', 'ARCHITECTURE_GUIDE.en.md'];
const chromePath = process.env.CHROME ?? '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome';
const MERMAID = 'https://cdn.jsdelivr.net/npm/mermaid@11.4.1/dist/mermaid.esm.min.mjs';
const SCALE = 2;
const PADDING = 24;

// Each picture link, the folded source under it: ![…](images/name.png) … ```mermaid … ```
const diagrams = GUIDES.flatMap((name) => {
  const guide = readFileSync(join(here, name), 'utf8');
  const found = [...guide.matchAll(/!\[[^\]]*\]\((images\/[^)]+\.png)\)\s*<details>[\s\S]*?```mermaid\n([\s\S]*?)```/g)]
    .map(([, file, source]) => ({ file, source }));
  const sources = guide.match(/```mermaid\n/g)?.length ?? 0;
  if (found.length !== sources) {
    throw new Error(`${name}: ${sources} diagrams, ${found.length} with a picture above them: each needs one`);
  }
  return found;
});

// The site's colours, so that the pictures look like the site in a presentation.
const theme = {
  theme: 'base',
  themeVariables: {
    fontFamily: 'Helvetica Neue, Helvetica, Arial, sans-serif',
    fontSize: '16px',
    background: '#FFFFFF',
    primaryColor: '#EEF3FF',
    primaryBorderColor: '#2264FF',
    primaryTextColor: '#141A26',
    lineColor: '#5B6475',
    secondaryColor: '#F4F6FA',
    tertiaryColor: '#FFFFFF',
    clusterBkg: '#F7F9FC',
    clusterBorder: '#C5CFE0',
    edgeLabelBackground: '#FFFFFF',
    actorBkg: '#EEF3FF',
    actorBorder: '#2264FF',
    actorTextColor: '#141A26',
    actorLineColor: '#C5CFE0',
    signalColor: '#3A4252',
    signalTextColor: '#141A26',
    noteBkgColor: '#FFF6DB',
    noteBorderColor: '#E0B43C',
    sequenceNumberColor: '#FFFFFF',
  },
  flowchart: { useMaxWidth: false, padding: 16, wrappingWidth: 360 },
  sequence: { useMaxWidth: false },
};

const page = `<!doctype html><meta charset="utf-8">
<style>body { margin: 0; background: #FFFFFF } #out { display: inline-block; padding: ${PADDING}px; background: #FFFFFF }</style>
<div id="out"></div>
<script type="module">
  import mermaid from '${MERMAID}';
  mermaid.initialize({ startOnLoad: false, ...${JSON.stringify(theme)} });
  window.draw = async (source, index) => {
    const { svg } = await mermaid.render('diagram' + index, source);
    const out = document.getElementById('out');
    out.innerHTML = svg;
    await document.fonts.ready;
    const box = out.getBoundingClientRect();
    return { width: Math.ceil(box.width), height: Math.ceil(box.height) };
  };
  window.ready = true;
</script>`;
const server = createServer((_, response) => response.writeHead(200, { 'Content-Type': 'text/html' }).end(page));
await new Promise((resolve) => server.listen(0, '127.0.0.1', resolve));
const address = `http://127.0.0.1:${server.address().port}/`;

const profile = mkdtempSync(join(tmpdir(), 'draw-diagrams-'));
const port = 9300 + Math.floor(Math.random() * 600);
const chrome = spawn(chromePath, [
  '--headless=new', '--no-first-run', '--no-default-browser-check', `--user-data-dir=${profile}`,
  `--remote-debugging-port=${port}`, '--hide-scrollbars', 'about:blank'], { stdio: 'ignore' });
const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
try {
  let targets = [];
  for (let i = 0; i < 50 && !targets.length; i++) {
    try { targets = (await (await fetch(`http://127.0.0.1:${port}/json/list`)).json()).filter((t) => t.type === 'page'); } catch {}
    await sleep(200);
  }
  if (!targets.length) throw new Error(`Chrome did not open: ${chromePath}`);
  const socket = new WebSocket(targets[0].webSocketDebuggerUrl);
  await new Promise((resolve) => socket.addEventListener('open', resolve));
  let id = 0;
  const pending = new Map();
  socket.addEventListener('message', (event) => {
    const message = JSON.parse(event.data);
    pending.get(message.id)?.(message);
    pending.delete(message.id);
  });
  const send = (method, params = {}) => new Promise((resolve) => {
    pending.set(++id, resolve);
    socket.send(JSON.stringify({ id, method, params }));
  });
  const evaluate = async (expression) => {
    const answer = await send('Runtime.evaluate', { expression, awaitPromise: true, returnByValue: true });
    if (answer.result?.exceptionDetails) throw new Error(answer.result.exceptionDetails.exception?.description);
    return answer.result?.result?.value;
  };

  await send('Emulation.setDeviceMetricsOverride', { width: 2400, height: 1600, deviceScaleFactor: SCALE, mobile: false });
  await send('Page.navigate', { url: address });
  for (let i = 0; i < 100 && !(await evaluate('window.ready === true')); i++) await sleep(100);

  for (const [index, { file, source }] of diagrams.entries()) {
    const size = await evaluate(`window.draw(${JSON.stringify(source)}, ${index})`);
    const shot = await send('Page.captureScreenshot', {
      format: 'png',
      clip: { x: 0, y: 0, width: size.width, height: size.height, scale: 1 },
      captureBeyondViewport: true,
    });
    writeFileSync(join(here, file), Buffer.from(shot.result.data, 'base64'));
    console.log(`${file}: ${size.width}×${size.height}`);
  }
  socket.close();
} finally {
  chrome.kill();
  server.close();
  await sleep(300);
  rmSync(profile, { recursive: true, force: true });
}
