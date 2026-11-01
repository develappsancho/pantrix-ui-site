# Pantrix UI site

Türkçesi: [README.tr.md](./README.tr.md)

The website of [Pantrix UI](https://github.com/develappsancho/pantrix-ui-cmp-package), built with Pantrix UI: one Compose
Multiplatform app for the web (Wasm), Android, iOS and the desktop. Every page is in English and in Turkish, each at
its own address: `/docs/installation` and `/tr/docs/installation`.

## Running it

Pantrix UI comes from one of two repositories, as the version in `gradle/libs.versions.toml` chooses: a release from
GitHub Packages, which asks for a token that may read packages (`GitHubPackagesUsername` and `GitHubPackagesPassword` in
`~/.gradle/gradle.properties`), and a `-SNAPSHOT` from the local Maven repository, published from the library's own
repository (`./gradlew publishToMavenLocal`).

- Web: `./gradlew :app:webApp:wasmJsBrowserDevelopmentExecutableDistribution`, then serve
  `app/webApp/build/dist/wasmJs/developmentExecutable`: a file as it is, an address with a page its folder's
  `index.html`, any other address `404.html`. Each page has a static `index.html` beside the app, for search engines
  and for a browser that cannot run the app, with `sitemap.xml`, `robots.txt` and `llms.txt` at the root.
- Web while it is developed: `./gradlew :app:webApp:wasmJsBrowserDevelopmentRun`. Webpack's server answers every
  page's address with the app, which opens the page the address names, so a page reloads where it is
  (`app/webApp/webpack.config.d/historyFallback.js`); it has no static pages.
- Desktop: `./gradlew :app:desktopApp:run`
- Android: `./gradlew :app:androidApp:installDebug`
- iOS: open `app/iosApp` in Xcode and run the `app.iosApp` scheme.
- The server, which serves Pantrix UI's MCP server at `/mcp`: `./gradlew :server:bootJar`, then
  `PORT=8080 java -jar server/build/libs/server.jar`. An assistant is given its address:
  `claude mcp add -t http pantrix-ui http://localhost:8080/mcp`.

## Checking it

- `./gradlew detekt lintKotlin`
- `./gradlew jvmTest iosSimulatorArm64Test wasmJsBrowserTest`
- `./gradlew :server:test`
- `./gradlew :app:shared:verifyRoborazziJvm`: one page of each kind at a desktop's, a tablet's and a phone's size, light
  and dark, against the images in `app/shared/screenshots/jvm/` (`recordRoborazziJvm` writes them again)

## Putting it up

The site runs as two containers in one Docker Compose stack: its files behind nginx, and the server beside it, which
nginx alone reaches, at `/mcp`. Both images are built on this machine from what Gradle makes; nothing is compiled on
the host.

- On this machine: `deploy/deploy.sh local` builds the site and the server, makes the two images, runs the stack at
  `http://localhost:4004` and checks it with `deploy/check.sh`; `deploy/deploy.sh stop` stops it. It needs Docker, and
  the token above for GitHub Packages.
- On the host, `pantrixui.com`: Coolify, from the same images on GitHub's container registry (to come).

The site's address is written in one place: `site.url` in `gradle.properties`, which the build gives the static pages,
the content (as `{{site}}`), the share images and the apps. A running stack is served at `SITE_URL` instead, which the
web container writes into the site's files as it starts, and the web app reads the address it was opened at.

The guides, in English and in Turkish: [ARCHITECTURE_GUIDE.en.md](./docs/deploy/ARCHITECTURE_GUIDE.en.md) draws what
runs where and why (nginx, the images, the stack, compression and caching, the site's address), and
[DEPLOY_GUIDE.en.md](./docs/deploy/DEPLOY_GUIDE.en.md) has the steps.

## The content

The pages are Markdown in `content/<language>/`, with the site's own pieces (`:::steps`, `:::facts`, `:::tabs`, …). The
content tool (`tools/content`) turns them into JSON at build time; the app never reads Markdown. The guide pages are
written from the library's guides: `./gradlew :core:content:writeGuidePages` after the library is published.

A component's page shows the demo's examples, or the site's own where it has written some:
`content/examples/<language>/<key>.md`, in the shape of the demo's pages, each example a title, what it says and its
code. The build runs the code it shows, and draws the site's own examples as wide as their frame.

## Coding assistants

The site is built with what Pantrix UI gives an app's assistant: its skill in `.claude/skills/pantrix-ui`, and its MCP
server in `.mcp.json`, which runs the library's jar from `PANTRIX_MCP_JAR`. Each machine sets it, in the shell's
profile for example: to the jar attached to the library's release, or to the one a local publish puts in the local
Maven repository.

```bash
curl -fL --create-dirs -o ~/.pantrix/mcp-<version>-all.jar https://github.com/develappsancho/pantrix-ui-cmp-package/releases/download/v<version>/mcp-<version>-all.jar
export PANTRIX_MCP_JAR=$HOME/.pantrix/mcp-<version>-all.jar
# Or after publishToMavenLocal in the library's repository
export PANTRIX_MCP_JAR=$HOME/.m2/repository/com/pantrixui/mcp/<version>/mcp-<version>-all.jar
```

`CLAUDE.md` has the project's map for an assistant, and `.claude/skills/commit` proposes a change's commit in the
repository's style.

## Contributing

How a change comes in, the site's rules and the checks before a pull request are in
[CONTRIBUTING.md](./CONTRIBUTING.md), and what changed is in [CHANGELOG.md](./CHANGELOG.md). A vulnerability is reported
as [SECURITY.md](./SECURITY.md) says, never in an issue. Everyone taking part follows the
[code of conduct](./CODE_OF_CONDUCT.md).

## Licence

The site is licensed under the [Apache License 2.0](./LICENSE). JetBrains Mono, which its code is set in, comes under the
SIL Open Font License, whose text the site carries:
`core/designsystem/src/commonMain/composeResources/files/licenses/jetbrains_mono_ofl.txt`.
