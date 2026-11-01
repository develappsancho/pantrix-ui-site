# Pantrix UI site

The website of [Pantrix UI](https://github.com/develappsancho/pantrix-ui-cmp), built with Pantrix UI: one Compose
Multiplatform app for the web (Wasm), Android, iOS and the desktop. Every page is in English and in Turkish, each at
its own address: `/docs/installation` and `/tr/docs/installation`.

## Running it

Pantrix UI comes from `mavenLocal`: publish it from the library's repository first.

- Web: `./gradlew :app:webApp:wasmJsBrowserDevelopmentExecutableDistribution`, then serve
  `app/webApp/build/dist/wasmJs/developmentExecutable`: a file as it is, an address with a page its folder's
  `index.html`, any other address `404.html`. Each page has a static `index.html` beside the app, for search engines
  and for a browser that cannot run the app, with `sitemap.xml`, `robots.txt` and `llms.txt` at the root.
- Desktop: `./gradlew :app:desktopApp:run`
- Android: `./gradlew :app:androidApp:installDebug`
- iOS: open `app/iosApp` in Xcode and run the `app.iosApp` scheme.

## Checking it

- `./gradlew detekt lintKotlin`
- `./gradlew jvmTest iosSimulatorArm64Test wasmJsBrowserTest`

## The content

The pages are Markdown in `content/<language>/`, with the site's own pieces (`:::steps`, `:::facts`, `:::tabs`, …). The
content tool (`tools/content`) turns them into JSON at build time; the app never reads Markdown. The guide pages are
written from the library's guides: `./gradlew :core:content:writeGuidePages` after the library is published.

A component's page shows the demo's examples, or the site's own where it has written some:
`content/examples/<language>/<key>.md`, in the shape of the demo's pages, each example a title, what it says and its
code. The build runs the code it shows, and draws the site's own examples as wide as their frame.

`CLAUDE.md` has the project's map and its rules.
