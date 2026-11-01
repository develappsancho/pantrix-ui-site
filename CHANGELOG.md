# Changelog

Türkçesi: [CHANGELOG.tr.md](./CHANGELOG.tr.md)

What changed on Pantrix UI's site, `pantrixui.com`, and in its apps, for someone who reads it or builds it.

- **Format:** this file follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/). A release is a deploy that
  changes what a visitor sees, and it is named by its date as three numbers, `YY.M.D` (`26.11.2`): the version its
  desktop app carries too.
- **What gets a line:** [CONTRIBUTING.md](./CONTRIBUTING.md#the-changelog) says which changes get one, and how it is
  written.

## [Unreleased]

### Security

- **A limit on the MCP server:** an address may ask `/mcp` ten times a second, with a burst of twenty; past that the
  site answers 429 (Too Many Requests), so that no one visitor can keep the server busy for everyone else.

## [26.11.1] - 2026-11-01

The site built with Pantrix UI 0.1.0, the library's first release, with the apps built with it, the home page's
adaptive tour and the page that asks the server; the desktop app's second release.

### Added

- **References:** `/references` shows the apps built with Pantrix UI, the site first: what it is, where it runs, the
  34 of the library's components its own screens draw with and how it is made, and where another app is added, an
  issue on the site's repository. Under it, by platform, it lists the others, the library's demo catalogue first, with
  its web build and its macOS app. It is the header's last link, and the menu's, and the home page's foot and a band
  on the home page lead to it; a desktop's header sets its parts, and its search, its language, GitHub and the theme
  most of all, closer together to make room for it.
- **The adaptive tour:** the home page walks a recipe app through six windows, a phone, a foldable open and on a
  table, a tablet, a desktop and a browser tab, each drawn by Pantrix in its own platform's conventions: the
  navigation moves from a bar to a rail and a sidebar, a recipe opens beside its list, the foldable cooks with its
  buttons below the fold, and a large window shows the week's shopping beside the recipe. It turns by itself while it
  is in view, stops where the reader picks a step, and never turns where the system asks for less motion. Its
  dishes' photos are from Unsplash and Pexels, named under it.
- **The page that asks the server:** `/ai/ask` asks Pantrix UI's MCP server itself, with no model and no key: a question
  in English or in Turkish finds a component with its code and the documents that speak of it, the library's
  declarations, its icons or its documents, with the tools it called beside the answer. It sets the reader's own
  assistant up too, and says what comes with the reader's own key. The page for coding assistants leads to it from its
  top and from the end of its conversation.
- **The site's source on GitHub:** the header has GitHub's mark beside the theme's button, which opens the site's
  repository; a phone narrower than the design has no room for it there, and the menu leads to it as well.

### Changed

- **Pantrix UI 0.1.0:** the lines that install the library, on the home page and in Installation, and the files for
  coding assistants name its first version after six previews, whose Claude Code plugin brings the MCP server at
  `pantrixui.com/mcp` too, and whose server searches in Turkish as well and gives its answers as data; its guides are
  the site's, with the one on an app that opens in every browser and its live sketch, and the web fonts' guide says to
  give the web a monospace typeface for code.
- **The page for coding assistants:** Claude Code is set up with Pantrix UI's plugin alone, which brings the MCP server
  and the skill together, so the server is no longer added twice.
- **The page for coding assistants, its files:** under the setup, every file it names opens or downloads from where it
  is: the MCP server's jar and the skill's zip from the release on GitHub, with the line that uses each, and `SKILL.md`,
  `llms.txt`, `llms-full.txt`, `pantrix-ui.json` and its schema from the site; the steps' and the cards' names of them
  are links too. Cursor is set up with the server's address, and the jar is left to a client that starts a command.
- **The home page between a tablet and a desktop:** in a window from 840 to about 1100dp wide, as a foldable open on its
  side, the home page's words fill the width in the tablet's single column, where they left the right side empty.

### Fixed

- **The menu of a tablet or a phone:** under Pantrix UI it lists every part of the site the header does, in the header's
  order, the page for coding assistants among them, which no menu led to.
- **The skill and the API's references in a browser:** the site's Markdown files are shown as text, where a browser used
  to save them as files; what to give for one component names its page on the site, `/components/data-table`, in place
  of a file the site does not have.
- **Older browsers and phones:** a browser that cannot run the site's Wasm build, as Chrome before 119 and Safari
  before 18.2, or one that fails to start it, is given the same app built as JavaScript: it used to show the page's
  plain words, or a white page.

### Security

- **HTTPS alone:** the site asks a browser to come back over HTTPS alone (`Strict-Transport-Security`), for a short
  while to begin with, and reports what its pages load against a content security policy, which it does not enforce
  yet.
- **No fonts from Google:** the site draws the marks of a Mac's keys, its own marks and its Arabic in fonts it carries,
  built with Pantrix UI 0.1.0-alpha04, and a visitor's browser asks Google for no font any more.
- **No path of the build machine:** the site's scripts no longer hold the path of the machine they were built on, with
  its user name and folders, which Skia's code wrote into them.

## [26.10.31] - 2026-10-31

The site's first version, built with Pantrix UI 0.1.0-alpha03, online at `pantrixui.com`, and its desktop app's first
release.

### Added

- **Pages in English and Turkish,** each at its own address (`/docs/installation`, `/tr/docs/installation`):
  - the home page;
  - the components: a grid of the library's 100 components, and a page for each, with its examples running live, its
    API, its styles and colours on each platform, its accessibility, its tags and its changes;
  - the docs: getting started, 24 guides with live sketches, and the library's changelog;
  - foundations, read live from the theme;
  - the theme builder, which writes a `DesignPreset` as Kotlin;
  - the adaptive UI simulator, which draws the library's scaffolds in windows of each size;
  - the page for coding assistants, the roadmap, ⌘K search, and a page for an address with none;
  - the download page of the desktop app, with the latest release's version, size and SHA-256.
- **Every page as static HTML** beside the app, for search engines and for a browser that cannot run Wasm: its title,
  description, canonical address, share image and words. With them, `sitemap.xml`, `robots.txt`, `llms.txt` and
  `llms-full.txt`.
- **Pantrix UI's MCP server** at `/mcp`, over Streamable HTTP: a coding assistant is given the address alone.
- **The same site as apps** for Android, iOS and the desktop. The macOS app, signed with a Developer ID and
  notarized by Apple, is downloaded from the site's releases on GitHub.
- **Putting it up:** the site behind nginx and its server, as two Docker images in one Compose stack.
  `deploy/deploy.sh local` runs it at `http://localhost:4004`, and the address a stack is served at is a setting,
  `SITE_URL`. `deploy/deploy.sh web` puts it on the host through Coolify, `deploy/deploy.sh desktop` releases the
  macOS app, and `deploy/deploy.sh release` does both.
