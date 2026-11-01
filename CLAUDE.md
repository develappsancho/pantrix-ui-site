# Pantrix UI site

The website of Pantrix UI, `pantrixui.com`, as a Compose Multiplatform app: the web (Wasm) first, and the same site on
Android, iOS and the desktop. It is built on Pantrix UI itself, from `mavenLocal` (`com.pantrixui`, the version in
`gradle/libs.versions.toml`). Group: `com.pantrixui.site`.

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
- **Local until done.** Pantrix comes from `mavenLocal`; publishing to GitHub Packages and the deploy (Coolify on a VPS)
  come at the end.
- **A backend, if one is ever needed, is Spring Boot**, not Ktor.
- **Nothing of yours in `.claude/`**; `local.properties` holds a token and is never copied or printed.

## Project map

| Path | What it is |
| :--- | :--- |
| `app/shared` | The composition root: `SiteApp` (theme, header, sidebar, menu, `NavDisplay`), `SiteViewModel`, the site's links (`SiteNavigation.kt`), Koin (`SiteKoin.kt`); `wasmJsMain/WebSite.kt` (address, browser history, typeface gate); `iosMain/MainViewController.kt` |
| `app/webApp`, `app/desktopApp`, `app/androidApp`, `app/iosApp` | The four entry points; each starts Koin with `startSiteKoin()` and draws `SiteApp` |
| `core/base` | MVI: `UiState`, `UiAction`, `UiEffect`, `BaseViewModel`, `BaseScreen`, `LoadState` |
| `core/navigation` | `Navigator`, `NavigationHistory`, `BrowserNavigationHistory` (History API) |
| `core/model` | `Route`, `Language`, `Page` and its blocks, `ContentFiles`, `LibraryFacts` (the library's counts the frame says: 100 components, 24 guides); `GuideCatalogue` (each guide's topic, summary, tags and platforms, kept in the site), `Changelog`, `ComponentIndex` and `ComponentPage`, `FoundationSection`, `SearchIndex`, `DemoPages` (a component's key, where the demo's other pages stand), `ShareImage` |
| `core/routing` | `SiteRoutes`: the one table of addresses (`/docs/installation`, `/tr/docs/installation`) |
| `core/i18n` | `SiteStrings`: the frame's words in English and Turkish; `LandingHeads`: the landing pages' titles (their windows' too) and descriptions |
| `core/designsystem` | `ShopeersPreset`, `SiteTheme`, `SiteType`, `SiteColors`, `SiteTypefaces`, `SiteIcons`, `BySize`; `chrome/` (header, sidebar and its `NavList`, menu, foot), `content/` (blocks, a guide's pieces, code, article page, page header, "On this page", a page's foot, a landing page's bands, the live stage of an example, the frames of a sketch: `PhoneFrame`, `WindowFrame`, `SketchControls`) |
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
| `content` | The pages, in English and Turkish; `examples/<language>/<key>.md`: a component's examples the site writes itself, in place of the demo's (the data table's) |

## Checks

- `./gradlew detekt lintKotlin` (`formatKotlin` fixes the layout).
- Tests on every target: `./gradlew jvmTest iosSimulatorArm64Test wasmJsBrowserTest`. Common tests do not run in the
  Android host tree, as in the library.
- The web: `./gradlew :app:webApp:wasmJsBrowserDevelopmentExecutableDistribution`, the output copied out of the tree and
  served as the host will: a file as it is, an address with a page its folder's `index.html` with no redirect, any
  other address `404.html` with a 404. The static pages are written into the same folder after the distribution; a
  browser with scripts off shows them.
- When the library's guides change: publish the library to `mavenLocal`, run `./gradlew :core:content:writeGuidePages`
  and look at the diff of `content/*/docs/guides/` before handing it over. A new guide needs its line in
  `GuideCatalogue` and its sketch in `GuideSketches` first.
