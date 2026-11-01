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

- **HTTPS alone:** the site asks a browser to come back over HTTPS alone (`Strict-Transport-Security`), for a short
  while to begin with, and reports what its pages load against a content security policy, which it does not enforce
  yet.
- **No fonts from Google:** the site draws the marks of a Mac's keys, its own marks and its Arabic in fonts it carries,
  built with Pantrix UI 0.1.0-alpha04, and a visitor's browser asks Google for no font any more, but on the guide to
  text order, whose check marks come with the library's next release.

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
