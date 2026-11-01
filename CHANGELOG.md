# Changelog

Türkçesi: [CHANGELOG.tr.md](./CHANGELOG.tr.md)

What changed on Pantrix UI's site, `pantrixui.com`, and in its apps, for someone who reads it or builds it.

- **Format:** this file follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/). The site has no version
  numbers: a release is a deploy that changes what a visitor sees, and it is named by its date (`2026-11-02`).
- **What gets a line:** [CONTRIBUTING.md](./CONTRIBUTING.md#the-changelog) says which changes get one, and how it is
  written.

## [Unreleased]

The site's first version, built with Pantrix UI 0.1.0-alpha03. It is not online yet.

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
