# Contributing to Pantrix UI's site

Türkçesi: [CONTRIBUTING.tr.md](./CONTRIBUTING.tr.md)

This file says how a change comes into the site, what it carries and how it is checked. Its rules are the project's
own, and every change follows them, whoever writes it.

## Before you start

- A bug or an idea starts as an issue: what you saw or want, on which page and platform, and how to see it.
- A larger change starts as a proposal in its issue, before any code. A new page or section, a new module, and a change
  to how the site is built or put up are larger changes. The proposal says:
  - what the change is for, and what it covers and leaves out;
  - the options weighed, and why one was chosen;
  - the questions still open.

  Code follows once the proposal is agreed.
- A vulnerability is never reported in an issue: [SECURITY.md](./SECURITY.md) says how to report one.
- Everyone taking part follows the [code of conduct](./CODE_OF_CONDUCT.md).

## Setting up

- JDK 21 or newer. The Gradle wrapper (`./gradlew`) brings its own Gradle.
- A token that may read packages, for Pantrix UI from GitHub Packages, in `~/.gradle/gradle.properties`:
  `GitHubPackagesUsername` and `GitHubPackagesPassword`. GitHub asks for one even for a public package. A `-SNAPSHOT`
  of the library comes from the local Maven repository instead, published from the library's own repository.
- For the Android app, the Android SDK with platform 37. The app runs on API 30 and newer.
- For the iOS app, a Mac with Xcode.
- To put the site up on your machine, Docker.

The [README](./README.md) says how to run the site on each platform, and how to put it up.

## The site's rules

- **An app of Pantrix UI.** The site is built on the library's public API, with no Material. Where a Pantrix component
  cannot draw what a page needs, the library is revised, not worked around: a gap is reported to the library and fixed
  there, under its own rules, and a part the site draws for itself meanwhile says so in its KDoc.
- **The design is matched one to one,** at a desktop's width (1440), a tablet's (834) and a phone's (390).
- **MVI,** with every base in one module, `:core:base`: a screen has its state, its actions and its effects.
- **Navigation is the site's own** (`:core:navigation`), written so that another project can take it.
- **Build logic is Kotlin classes:** a convention plugin is a `Plugin<Project>` in `build-logic/src/main/kotlin`,
  never a precompiled `.gradle.kts` script.
- **The site's address is never written by hand.** `site.url` in `gradle.properties` is the one place: a page names it
  as `{{site}}`, the app reads it from `SiteAddress`, and a running stack is served at `SITE_URL`.
- **A page is content, not code:** Markdown in `content/<language>/`, the same page in English and in Turkish, with the
  site's own pieces (`:::steps`, `:::tabs`, …). A guide page is written from the library's guides by
  `./gradlew :core:content:writeGuidePages`.
- **Every screen has its previews:** four in a `…ScreenPreviews.kt` beside it, the desktop's page and the phone's, each
  in the light and the dark theme. A part of the design system has two, light and dark.
- **A backend is Spring Boot,** without the Ktor the MCP SDK brings.

## Before you open a pull request

Run what the change can affect.

- **Style and static analysis:** `./gradlew detekt lintKotlin`. `formatKotlin` fixes the layout.
- **Tests, on every target:** `./gradlew jvmTest iosSimulatorArm64Test wasmJsBrowserTest`. The common tests do not run
  in the Android host tree.
- **Previews:** `SitePreviewsTest`, a plain `jvmTest` of `app/shared`, fails while a screen lacks one, and draws them
  all.
- **The server:** `./gradlew :server:test` asks every kind of MCP message over real HTTP.
- **Screenshots,** when a page's look changes: `./gradlew :app:shared:verifyRoborazziJvm`. A change that is meant is
  recorded again with `recordRoborazziJvm`; look at the dark images too.
- **Putting it up,** when `deploy/`, the server or the web build changes: `deploy/deploy.sh local` builds both images,
  runs the stack at `http://localhost:4004` and checks it with `deploy/check.sh`.
- **The library's version:** `pantrix` in `gradle/libs.versions.toml`, `LibraryFacts.RELEASE` once it is released, the
  skill written again with the new jar, and the rough sizes of the AI page's files (`FilesSay`).
- **The library's guides:** `./gradlew :core:content:writeGuidePages`, and the diff of `content/*/docs/guides/` read
  before the change is handed over.

In the pull request, say what was verified, on which platforms, and what was not.

## The changelog

[CHANGELOG.md](./CHANGELOG.md), and its Turkish copy [CHANGELOG.tr.md](./CHANGELOG.tr.md), say what changed for someone
who reads the site or uses its apps.

- **What gets a line:** a change a visitor can see gets a line under `Unreleased`, in the same commit: a new or changed
  page, a change in behaviour or look, a fix they would notice. Build logic, tests and documentation alone get none.
- **Where:** the line goes in the section of its kind: `Added`, `Changed`, `Deprecated`, `Removed`, `Fixed` or
  `Security`.
- **How:** it names the page or the part, and says in one sentence what changed for the visitor.
- **Both files:** the same line goes in the Turkish copy, in its section.
- **Before the site goes online,** the `Unreleased` entry describes the first version: a change to what it lists
  updates that description, rather than adding a line under `Changed` or `Removed`.
- **A deploy** that changes what a visitor sees renames `Unreleased` to its version, its date as `YY.M.D`, with the
  date, in both files: `deploy/deploy.sh prepare` does it.

## Commits

A commit message is one line, `[TYPE][Summary]`, and nothing else: no body and no trailers.

| Type | For |
| :--- | :--- |
| `DEV` | New or changed behaviour: pages, features, refactors, build logic, dependencies, tooling, putting it up |
| `FIX` | Behaviour that was wrong and is now right: a bug, a crash, a wrong colour |
| `TEST` | Only tests, and the setup they run on |
| `DOCS` | Only documentation |
| `CI` | Only what the CI service runs |
| `RELEASE` | Preparing or marking a deploy: the changelog, the library's version |

- **Mixed commits:** a commit that mixes takes the type of its main purpose. A fix with its regression test is `FIX`;
  a feature with its tests and docs is `DEV`.
- **The summary:**
  - It is English, imperative, and starts with a capitalised verb: `[DEV][Add the theme builder]`,
    `[FIX][Draw the sidebar's scroll bar in the dark theme]`.
  - It says what changed, not how the work went.
  - Several changes are listed in it, each starting with its own capitalised verb, with commas and `and` before the
    last: `[DEV][Add …, Draw … and Read …]`.
  - It has no full stop. A single change keeps to about 72 characters; a list of changes runs as long as it needs.

## Languages

- **Code, KDoc and commit messages** are in English.
- **The pages** are in English and in Turkish: `content/en` and `content/tr` hold the same pages.
- **The guides** in `docs/` are written in Turkish, each with its English beside it (`X_GUIDE.en.md`), and the two are
  changed together:
  - Each links the other on its third line, after the title: `English: [X_GUIDE.en.md](./X_GUIDE.en.md)` and
    `Türkçesi: [X_GUIDE.md](./X_GUIDE.md)`.
  - The English has the Turkish's shape: the same headings with their numbers, the same tables, blocks of code and
    pictures.
- **The files at the root** (this one, the README, the changelog, the security policy and the code of conduct) are in
  English. Each has a Turkish copy next to it (`*.tr.md`), and the two are changed together.

## Licence

The site is licensed under the [Apache License 2.0](./LICENSE). A contribution is made under the same licence: there is
no separate agreement to sign and no sign-off line in commits.
