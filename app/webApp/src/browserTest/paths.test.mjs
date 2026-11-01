// The app's main paths in Chrome, on the site as it is built, as the Maestro flows in app/maestro go them on a phone:
// the menu and the header's links lead to the pages, the language and the theme change, and the search opens a
// component's page. In a browser the address goes with the page: it is written as a page opens, Back goes to the one
// before, and an address opened afresh draws its own page. After a menu, a sheet or a dialog the page is read by its
// address, its title and its language (chrome.mjs says why), and the last test holds what a screen reader is left with.
// `./gradlew :app:webApp:browserTest` builds the site and runs this with Node's own test runner (chrome.mjs says what
// it is given).
import { test, before, after } from 'node:test';
import assert from 'node:assert/strict';
import { DRAWN, SKIP, open, serve } from './chrome.mjs';

const PHONE = { width: 390, height: 844 };
const TABLET = { width: 834, height: 1194 };
const DESKTOP = { width: 1440, height: 900 };
// How long a test that holds a fault still there waits for it to be gone.
const TODO_WAIT = 10_000;

let site;

before(async () => { if (!SKIP) site = await serve(); });

after(() => site?.close());

// Opens `path` at `size`, waits for the app's header, and runs `check` on the page.
async function onPage(path, size, check) {
    const page = await open({ base: site.base, path, ...size });
    try {
        assert.equal(await page.until(DRAWN), true, `the app drew its first frame: ${page.report()}`);
        await page.shown('IconButton_theme');
        await check(page);
    } finally { await page.close(); }
}

test('the menu leads to a page, the address follows it and Back returns', { skip: SKIP }, () =>
    onPage('/', PHONE, async (page) => {
        await page.click('IconButton_openmenu');
        await page.click('Site_nav_installation');
        await page.waitFor('the Installation page', async () =>
            (await page.path()) === '/docs/installation' && (await page.title()) === 'Installation · Pantrix UI');
        await page.back();
        await page.waitFor('the home page again', async () =>
            (await page.path()) === '/' && (await page.title()) !== 'Installation · Pantrix UI');
    }));

test('the header\'s links lead to their parts on a desktop', { skip: SKIP }, () =>
    onPage('/', DESKTOP, async (page) => {
        await page.click('Button_nav_components');
        await page.shown('TextField_componentfilter');
        assert.equal(await page.path(), '/components');
        await page.click('Button_nav_references');
        await page.waitFor('the references', async () => (await page.path()) === '/references');
        await page.click('Site_home');
        await page.shown('Button_homebrowse');
        assert.equal(await page.path(), '/');
    }));

test('a tablet\'s header opens the search from its box', { skip: SKIP }, () =>
    onPage('/', TABLET, async (page) => {
        await page.click('Site_search');
        await page.click('TextField_search');
        await page.type('button');
        await page.shown('ListItem_searchresult_0');
    }));

test('the language menu turns the words and the address into Turkish and back', { skip: SKIP }, () =>
    onPage('/docs/installation', PHONE, async (page) => {
        await page.click('Button_language');
        await page.click('Menu_language.MenuItem_language_tr');
        await page.waitFor('the Turkish page', async () =>
            (await page.path()) === '/tr/docs/installation' && (await page.title()) === 'Kurulum · Pantrix UI');
        // The Turkish address, opened afresh, is drawn in Turkish, and its page says so.
        await page.goto('/tr/docs/installation');
        await page.waitFor('the Turkish words', async () => (await page.label('IconButton_search')) === 'Belgelerde ara');
        assert.equal(await page.lang(), 'tr');
        await page.click('Button_language');
        await page.click('Menu_language.MenuItem_language_en');
        await page.waitFor('the English page', async () =>
            (await page.path()) === '/docs/installation' && (await page.title()) === 'Installation · Pantrix UI');
    }));

test('the page says the language the app has turned to', {
    skip: SKIP,
    todo: 'Pantrix UI 0.1.0 writes the window\'s title in the document but not its language: <html lang> stays the one ' +
        'the address was opened in'
}, () =>
    onPage('/docs/installation', PHONE, async (page) => {
        await page.click('Button_language');
        await page.click('Menu_language.MenuItem_language_tr');
        await page.waitFor('the Turkish page', async () => (await page.path()) === '/tr/docs/installation');
        await page.waitFor('the document in Turkish', async () => (await page.lang()) === 'tr', TODO_WAIT);
    }));

test('the theme button turns the dark theme on and off', { skip: SKIP }, () =>
    onPage('/', PHONE, async (page) => {
        assert.equal(await page.label('IconButton_theme'), 'Switch to the dark theme');
        await page.click('IconButton_theme');
        await page.waitFor('the dark theme', async () => (await page.label('IconButton_theme')) === 'Switch to the light theme');
        await page.click('IconButton_theme');
        await page.waitFor('the light theme', async () => (await page.label('IconButton_theme')) === 'Switch to the dark theme');
    }));

test('the search finds a component, keeps the components and opens its page', { skip: SKIP }, () =>
    onPage('/', PHONE, async (page) => {
        await page.click('IconButton_search');
        await page.click('TextField_search');
        await page.type('date picker');
        await page.shown('ListItem_searchresult_0');
        await page.waitFor('an API among the results', () => page.has('ListItem_searchresult_3'));
        await page.click('Site_searchFilter_component');
        await page.waitFor('the components alone', async () =>
            (await page.has('ListItem_searchresult_2')) && !(await page.has('ListItem_searchresult_3')));
        await page.click('ListItem_searchresult_0');
        await page.waitFor('Date Picker\'s page', async () =>
            (await page.path()) === '/components/date-picker' && (await page.title()) === 'Date Picker · Pantrix UI');
    }));

test('an address opened afresh draws its own page, its examples in view as the page scrolls', { skip: SKIP }, () =>
    onPage('/components/button', PHONE, async (page) => {
        await page.shown('Breadcrumb_page.Item_current');
        assert.equal(await page.label('Breadcrumb_page.Item_current'), 'Button');
        await page.scrollTo('SegmentButton_exampleplatform.Segment_ios');
        await page.click('SegmentButton_exampleplatform.Segment_ios');
    }));

test('once a menu has closed, what a screen reader reads is the page again', {
    skip: SKIP,
    todo: 'Compose 1.12.2 keeps the copy of the semantics as the closed layer left it (ComposeWebSemanticsListener)'
}, () =>
    onPage('/', PHONE, async (page) => {
        await page.click('IconButton_openmenu');
        await page.click('Site_nav_installation');
        await page.waitFor('the Installation page', async () => (await page.path()) === '/docs/installation');
        await page.shown('Button_codetab0', TODO_WAIT);
    }));
