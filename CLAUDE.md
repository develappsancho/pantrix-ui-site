# Pantrix UI site

The website of Pantrix UI, `pantrixui.com`, as a Compose Multiplatform app: the web (Wasm) first, and the same site on
Android, iOS and the desktop. It is built on Pantrix UI itself (`com.pantrixui`, the version in
`gradle/libs.versions.toml`): a release from GitHub Packages, a `-SNAPSHOT` from `mavenLocal`. Group:
`com.pantrixui.site`.

## How I want you to work

You are my eyes on the code. If something catches your attention while reading code or logs — a bug, a risk, a smell,
something surprising — flag it, even if it is unrelated to the task at hand.

## The owner's rules

- **The owner commits.** Never a git command that writes; read-only git is fine. Each hand-over ends with one commit
  message, one line `[TYPE][Summary]` as in the library (`pantrix-ui-cmp/CONTRIBUTING.md#commits`), and the files that
  go in it, by path.
- **Reports to the owner in Turkish.** Code, KDoc, the content's Markdown in English, and the Turkish pages in Turkish.
- **The design is matched one to one** (the design canvas: desktop 1440, tablet 834, phone 390 for each page). Where a
  Pantrix component cannot draw it, the library is revised, not worked around for good: a part the site draws for
  itself meanwhile says so in its KDoc.
- **The site is a usability test of Pantrix.** A bug or a gap found in the library goes into the library's
  `docs/OPEN_ITEMS.md` and the report, and is fixed in the library under its own rules (`pantrix-ui-cmp/CONTRIBUTING.md`).
- **build-logic's convention plugins are Kotlin classes** (`Plugin<Project>` in `build-logic/src/main/kotlin`), never
  precompiled `.gradle.kts` scripts.
- **MVI**, with every base in one module, `:core:base`.
- **Navigation is the site's own API** (`:core:navigation`), written to be reused by other projects; no
  navigation3-browser.
- **Web fonts**: the library's fix for a typeface that arrives late (preloads in `index.html`, `PantrixTypefaceGate`).
- **Pantrix from both repositories** (owner, 2026-10-30): a release from GitHub Packages, with the token in
  `~/.gradle/gradle.properties` (`GitHubPackagesUsername`, `GitHubPackagesPassword`), a `-SNAPSHOT` from `mavenLocal`,
  published from the library's repository; the catalogue's version chooses (`settings.gradle.kts`). The deploy (Coolify
  on a VPS) comes at the end.
- **Nothing of yours in `.claude/`**, but the library's skill `pantrix-ui` in `.claude/skills/` (owner, 2026-10-30):
  the site is the library's first app, and is built with the skill and the MCP server an app is given (`.mcp.json`).
  `local.properties` holds a token and is never copied or printed.
- **The site's address is never written by hand** (owner, 2026-10-30): `site.url` in `gradle.properties` is the one
  place, which the build gives the static pages, the content (a page writes `{{site}}`), the share images and the apps
  (`BUILT_SITE_ADDRESS`); the app gives `SiteAddress` (the web app the address it was opened at); a running stack is
  served at `SITE_URL`.
- **A backend is Spring Boot**: `:server`, which serves the library's MCP server at `/mcp` (`PantrixMcp.http()`),
  without the Ktor the MCP SDK brings.

## Project map

| Path | What it is |
| :--- | :--- |
| `app/shared` | The composition root: `SiteApp` (theme, header, sidebar, menu, `PageDisplay`: Navigation 3's `NavDisplay` with the demo's page transitions, `PageTransitions.kt`), `SiteViewModel`, the site's links (`SiteNavigation.kt`), Koin (`SiteKoin.kt`); `wasmJsMain/WebSite.kt` (address, browser history, typeface gate); `iosMain/MainViewController.kt`; `jvmTest/PageScreenshotTest.kt` and `screenshots/jvm/`: the pages' screenshots |
| `app/webApp`, `app/desktopApp`, `app/androidApp`, `app/iosApp` | The four entry points; each starts Koin with `startSiteKoin()` and draws `SiteApp` |
| `core/base` | MVI: `UiState`, `UiAction`, `UiEffect`, `BaseViewModel`, `BaseScreen`, `LoadState` |
| `core/navigation` | `Navigator`, `NavigationHistory`, `BrowserNavigationHistory` (History API: path addresses, each entry keeping the stack it was written with, so a reload and Back or Forward reopen it) |
| `core/model` | `SiteAddress` (the site's address: on the web the page's own, in an app `site.url`, written by `site.address` as `BUILT_SITE_ADDRESS`), `Route`, `Language`, `Page` and its blocks, `ContentFiles`, `LibraryFacts` (the library's counts the frame says: 100 components, 24 guides); `GuideCatalogue` (each guide's topic, summary, tags and platforms, kept in the site), `Changelog`, `ComponentIndex` and `ComponentPage`, `FoundationSection`, `SearchIndex`, `DemoPages` (a component's key, where the demo's other pages stand), `ShareImage` |
| `core/routing` | `SiteRoutes`: the one table of addresses (`/docs/installation`, `/tr/docs/installation`) |
| `core/i18n` | `SiteStrings`: the frame's words in English and Turkish; `LandingHeads`: the landing pages' titles (their windows' too) and descriptions |
| `core/designsystem` | `PantrixPreset`, `SiteTheme`, `SiteType`, `SiteColors`, `SiteTypefaces`, `SiteIcons`, `BySize`, `SitePreview` (what a preview is drawn in, and its sizes); `chrome/` (header, sidebar and its `NavList`, menu, foot), `content/` (blocks, a guide's pieces, code, article page, page header, "On this page", a page's foot, a landing page's bands, the live stage of an example, the frames of a sketch: `PhoneFrame`, `WindowFrame`, `SketchControls`) |
| `core/content` | `ContentRepository`: the pages, components and search index the content tool wrote, read from the resources |
| `core/testing` | Test doubles: `FakeContentRepository` |
| `feature/docs` | A page of the documentation (`DocViewModel`, `DocScreen`, `DocRoute`), a guide (`GuideArticle`), the guides (`GuidesScreen`), the changelog (`ChangelogScreen`) and the page for an address with no page; `sketch/`: each guide's live sketch, Pantrix's own components running on the page (`GuideSketches` maps them to the guides' keys) |
| `feature/components` | The components' grid (`ComponentsScreen`) and a component's page (`ComponentScreen`): its examples live and as code (`ExamplePreview`, from the live examples the build writes), how it works, its API, its styles and colours on each platform (`StyleValues`), accessibility, tags and changes |
| `feature/home` | The landing page: hero, live showcase, sections, its end (`HomeWords` holds its words) |
| `feature/foundations` | Foundations: colours, type, spacing, shapes, elevation, motion and density, each read live from the theme |
| `feature/theme` | The theme builder: controls, a live preview and the `DesignPreset` it writes as Kotlin, copied or downloaded (`ThemeBuilder`, `PresetCode`) |
| `feature/adaptive` | Adaptive UI: the window simulator, which draws the library's real scaffolds in a `PantrixWindowFrame` (`Simulator`, `SimApps`) |
| `feature/ai` | The page for coding assistants: setup, the MCP server and what it answers |
| `feature/roadmap` | The roadmap: its board, the server-driven UI preview and the native libraries |
| `feature/search` | ⌘K search: a `PantrixModal` with its results (`SearchResults`, pure and tested) over the index the content tool writes |
| `tools/content` | Programs that run at build time or by hand. `MainKt`: `content/<language>/**.md` with the site's pieces (`:::steps`, `:::facts`, `:::aside`, `:::sketch`, …) to JSON, with the changelog and the components read from the `com.pantrixui:mcp` jar, and the search index. `LiveMainKt`: the examples the site runs, as Kotlin: the demo's, or the site's own where it writes some (`SiteExamples`). `GuidesMainKt`: the guide pages in `content/<language>/docs/guides/` from the library's guides in the jar (`GuidePages`), run by hand with `./gradlew :core:content:writeGuidePages`; `keyboard-dismissal` is written by hand and left as it is |
| `tools/prerender` | Run after each web distribution: every page's static `index.html` in the folder of its address (its head, its other language, what a shared link shows, and its words as plain HTML, hidden where the browser runs the app), the `404.html`, `sitemap.xml`, `robots.txt`, and `llms.txt` with its links led to the site's pages |
| `tools/render` | Draws the app with Compose on the JVM at build time: the landing pages' words read from their semantics (`LandingMainKt`; parts marked `Modifier.live()` and the controls are left out) and each page's share image (`ShareMainKt`); both run only when what they read has changed |
| `server` | The site's server, Spring Boot on the JVM, which runs beside the site on its host: the library's MCP server over Streamable HTTP at `/mcp` (`McpEndpoint`, `PantrixMcp.http()`), its allowed origins in `application.properties` (`pantrix.mcp.origins`); `./gradlew :server:bootJar` makes the jar that runs by itself, on `PORT` or 8080 |
| `.claude/skills/pantrix-ui`, `.mcp.json` | The library's skill and its MCP server, as an app of the library takes them: the skill written by the library's jar (`java -jar "$PANTRIX_MCP_JAR" skill .claude/skills`), the server the same jar, which each machine names in `PANTRIX_MCP_JAR`: the jar attached to the library's release, or `~/.m2/repository/com/pantrixui/mcp/<version>/mcp-<version>-all.jar` after a local publish |
| `content` | The pages, in English and Turkish; `examples/<language>/<key>.md`: a component's examples the site writes itself, in place of the demo's (the data table's) |
| `deploy` | Putting the site up: two images built on this machine from Gradle's outputs, the site's files behind nginx (`web/Dockerfile`, `web/default.conf`) and the server (`server/Dockerfile`), in one Compose stack (`compose.yaml`; `compose.local.yaml` builds them here, opens port 4004 and serves the site at `http://localhost:4004`); `SITE_URL`, the address a stack is served at, is written into the files as the web container starts (`web/05-site-address.envsh`, `web/40-site-address.sh`) and allowed by the server; `deploy.sh local` builds, runs and checks it, `check.sh` checks any address. Nothing is compiled on the host |
| `docs/deploy` | The deploy's guides, in Turkish: `ARCHITECTURE_GUIDE.md` (what runs where and why: each diagram a picture in `images/`, its Mermaid source folded under it) and `DEPLOY_GUIDE.md` (the steps). A diagram's source changed, `node docs/deploy/draw-diagrams.mjs` draws the pictures again with Mermaid in headless Chrome |

## Checks

- `./gradlew detekt lintKotlin` (`formatKotlin` fixes the layout).
- Previews: every screen has four in a `…ScreenPreviews.kt` beside it, the desktop's page and the phone's in the light
  and the dark theme, drawn from a sample state in `SitePreview`; a part of the design system has two, light and dark.
  `SitePreviewsTest` (a plain `jvmTest` of `app/shared`) fails while a screen lacks one, and draws every preview.
- Tests on every target: `./gradlew jvmTest iosSimulatorArm64Test wasmJsBrowserTest`. Common tests do not run in the
  Android host tree, as in the library.
- The web: `./gradlew :app:webApp:wasmJsBrowserDevelopmentExecutableDistribution`, the output copied out of the tree and
  served as the host will: a file as it is, an address with a page its folder's `index.html` with no redirect, any
  other address `404.html` with a 404. The static pages are written into the same folder after the distribution; a
  browser with scripts off shows them.
- The server: `./gradlew :server:test` asks every kind of MCP message over real HTTP.
- The deploy, when `deploy/`, the server or the web build's output changes: `deploy/deploy.sh local` builds both images,
  runs the stack at `http://localhost:4004` and runs `deploy/check.sh` on it (pages, 404, compression, caching, `www`,
  `/mcp`); `deploy/deploy.sh stop` stops it. The owner's other containers run in the same Docker: touch only the
  stack `pantrix-ui-site`.
- The pages' screenshots, when a page's look changes: `./gradlew :app:shared:verifyRoborazziJvm` (about six minutes).
  `PageScreenshotTest` draws one page of each kind (14, and the home page and Installation in Turkish too) at 1440×900,
  834×1194 and 390×844, light and dark, on the desktop's Skia: 96 images in `app/shared/screenshots/jvm/`. A change
  that is meant is recorded again with `recordRoborazziJvm`, and the dark images are looked at too. A plain `jvmTest`
  leaves them out (`site.screenshots`). The system's theme and the screen's clock are fixed in the test, so a picture
  changes with neither the machine nor the day.
- When the library's version changes: `pantrix` in `gradle/libs.versions.toml`, `LibraryFacts.RELEASE` once it is
  released, and the skill written again with the new jar (`java -jar "$PANTRIX_MCP_JAR" skill .claude/skills`).
- When the library's guides change: take its new version (a release, or a `-SNAPSHOT` published to `mavenLocal`), run
  `./gradlew :core:content:writeGuidePages`
  and look at the diff of `content/*/docs/guides/` before handing it over. A new guide needs its line in
  `GuideCatalogue` and its sketch in `GuideSketches` first.
- The library's repository is private (owner, 2026-10-29): `LibraryFacts.REPOSITORY_IS_OPEN` is false, and the site
  leads no link to it. "Report a problem", "Edit this page on GitHub", the changelog's feed and file, the roadmap's
  suggestion and the foot's GitHub wait for it; a guide's link to one of its files is the file's name alone. When it
  opens, the switch is set and `writeGuidePages` is run again.
- Three of the repository's files have a copy on the site, which a guide's link to them leads to
  (`GuidePages.SITE_COPIES`): `config/detekt/detekt.yml` and `demo.yml` in `content/*/docs/detekt-settings.md` (the
  English with its comments translated), and `docs/guide/adaptive/adaptive-rules.md` in
  `content/*/docs/adaptive-rules.md` (in English too, without its picture). When one changes in the library, its page
  is written again by hand.
