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
| `core/model` | `Route`, `Language`, `Page` and its blocks, `ContentFiles`, `LibraryFacts`; `GuideCatalogue` (each guide's topic, summary, tags and platforms, kept in the site), `Changelog` |
| `core/routing` | `SiteRoutes`: the one table of addresses (`/docs/installation`, `/tr/docs/installation`) |
| `core/i18n` | `SiteStrings`: the frame's words in English and Turkish |
| `core/designsystem` | `ShopeersPreset`, `SiteTheme`, `SiteType`, `SiteColors`, `SiteTypefaces`; `chrome/` (header, sidebar, menu, search box), `content/` (blocks, a guide's pieces, code, article page, page header, "On this page", a page's foot, the frames of a sketch: `PhoneFrame`, `WindowFrame`, `SketchControls`) |
| `core/content` | `ContentRepository`: the pages the content tool wrote, read from the resources |
| `core/testing` | Test doubles: `FakeContentRepository` |
| `feature/docs` | A page of the documentation (`DocViewModel`, `DocScreen`, `DocRoute`), a guide (`GuideArticle`), the guides (`GuidesScreen`), the changelog (`ChangelogScreen`) and the page for an address with no page; `sketch/`: each guide's live sketch, Pantrix's own components running on the page (`GuideSketches` maps them to the guides' keys) |
| `tools/content` | The content tool: `content/<language>/**.md` with the site's pieces (`:::steps`, `:::facts`, `:::aside`, `:::sketch`, …) to JSON at build time, and the library's changelog read from the `com.pantrixui:mcp` jar; the guides are the site's own text, in `content/<language>/docs/guides/` |
| `content` | The pages, in English and Turkish |

## Checks

- `./gradlew detekt lintKotlin` (`formatKotlin` fixes the layout).
- Tests on every target: `./gradlew jvmTest iosSimulatorArm64Test wasmJsBrowserTest`. Common tests do not run in the
  Android host tree, as in the library.
- The web: `./gradlew :app:webApp:wasmJsBrowserDevelopmentExecutableDistribution`, the output copied out of the tree and
  served with a server that answers every address with `index.html` (the site's addresses are paths, not `#…`).
