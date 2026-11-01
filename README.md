# Pantrix UI site

The website of [Pantrix UI](https://github.com/developersancho/pantrix-ui-cmp), built with Pantrix UI: one Compose
Multiplatform app for the web (Wasm), Android, iOS and the desktop. Every page is in English and in Turkish, each at
its own address: `/docs/installation` and `/tr/docs/installation`.

## Running it

Pantrix UI comes from `mavenLocal`: publish it from the library's repository first.

- Web: `./gradlew :app:webApp:wasmJsBrowserDevelopmentExecutableDistribution`, then serve
  `app/webApp/build/dist/wasmJs/developmentExecutable` with a server that answers every address with `index.html`.
- Desktop: `./gradlew :app:desktopApp:run`
- Android: `./gradlew :app:androidApp:installDebug`
- iOS: open `app/iosApp` in Xcode and run the `app.iosApp` scheme.

## Checking it

- `./gradlew detekt lintKotlin`
- `./gradlew jvmTest iosSimulatorArm64Test wasmJsBrowserTest`

## The content

The pages are Markdown in `content/<language>/`, with the site's own pieces (`:::steps`, `:::facts`, `:::tabs`, …). The
content tool (`tools/content`) turns them into JSON at build time; the app never reads Markdown.

`CLAUDE.md` has the project's map and its rules.
