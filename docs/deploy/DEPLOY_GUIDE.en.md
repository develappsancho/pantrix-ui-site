# 🚀 Deploy Guide

Türkçesi: [DEPLOY_GUIDE.md](./DEPLOY_GUIDE.md)

This document gives the steps of putting Pantrix UI's site up: first on this machine, with Docker Compose; then in Coolify. What runs where, and why it is set up so, is in [ARCHITECTURE_GUIDE.en.md](./ARCHITECTURE_GUIDE.en.md).

## 1. Summary

| Command | What it does |
| :--- | :--- |
| `deploy/deploy.sh local` | Builds the site and the server, makes the two images, runs the stack at `http://localhost:4004` and checks it |
| `deploy/deploy.sh local --no-gradle` | The same with what Gradle made last: for a change to `deploy/` alone |
| `deploy/deploy.sh stop` | Stops the stack and removes its containers; the images stay |
| `deploy/check.sh [address]` | Runs the checks at an address: on this machine or on the host |
| `deploy/deploy.sh web [version]` | Pushes the two images, for both architectures, to GHCR, has Coolify put them up and checks the live address |
| `deploy/deploy.sh desktop [version]` | Makes the macOS app a signed DMG notarized by Apple, and puts it on the release's GitHub page |
| `deploy/deploy.sh release [version]` | `web`, then `desktop`; it needs the version's section in the changelog |
| `deploy/deploy.sh prepare [version]` | Gives the changelog's `Unreleased` section, and its Turkish copy's `Yayınlanmamış`, the version's name and date |

A version is the release's date: `YY.M.D` (`26.10.31`), today's when none is given. `--dry-run` builds everything and sends nothing anywhere, to GHCR, Coolify, GitHub or Apple; `--no-gradle` takes what Gradle made last; `--no-coolify` pushes the images and leaves Coolify alone.

## 2. What It Needs

*   **Docker Desktop**, running. `docker compose` comes with it.
*   **JDK 21**, and Gradle itself (`./gradlew`).
*   **The key to GitHub Packages:** a release of Pantrix UI comes from GitHub Packages, and GitHub asks for a key even for public packages. In `~/.gradle/gradle.properties`, `GitHubPackagesUsername`, and a token with `read:packages` as `GitHubPackagesPassword`. A `-SNAPSHOT` comes from the local Maven repository, and needs no key.

## 3. On This Machine

```bash
deploy/deploy.sh local
```

The command does four things:

1.  **Gradle:** `:app:webApp:wasmJsBrowserDistribution` writes the site's production build and each page's static HTML (`app/webApp/build/dist/wasmJs/productionExecutable`), and `:server:bootJar` makes the server's jar (`server/build/libs/server.jar`). The first build takes a few minutes; optimising the Wasm is the longest step.
2.  **Docker:** makes the two images and names them by the short hash of the commit they are built from; where the tree holds changes not yet committed, the name ends in `-dirty`: `ghcr.io/develappsancho/pantrix-ui-site-web:fbc4fe2-dirty`.
3.  **Compose:** runs the stack and waits until both containers are `healthy`.
4.  **The checks:** runs `check.sh` at `http://localhost:4004` (section 4).

At the end, the site is at `http://localhost:4004`, and opens from this machine alone. For another port:

```bash
SITE_PORT=4100 deploy/deploy.sh local
```

The containers' logs:

```bash
docker compose -f deploy/compose.yaml -f deploy/compose.local.yaml logs -f
```

To stop it:

```bash
deploy/deploy.sh stop
```

## 4. The Checks

`check.sh` tries the site as a browser and an assistant reach it; each check writes a line, and the script fails when one does.

| Part | What is tried |
| :--- | :--- |
| Pages | The home page; `/docs/installation` with no redirect, giving the address tried as its own; its Turkish; a component's page; an address with no page, as the 404 page with a 404 status; `llms.txt` as plain UTF-8 text that leads to the address tried; `sitemap.xml` at the address tried; `robots.txt` |
| Compression and caching | A page gzipped and `no-cache`; `site.js` `no-cache`; each Wasm file as `application/wasm`, gzipped and cached for a year; no version of the server in the headers, and `nosniff` |
| The second name | The name under `www` goes to the address tried for good (on this machine, with a `Host` header). On a test address that has no such name, `CHECK_WWW=no` leaves it out |
| The MCP server | `initialize` answered by pantrix-ui; `tools/list`; a tool's call; a GET refused (405); a request from the site's own page answered; one from another site's page refused (403); a message over 64 KB refused (413) |

It runs at any address. The address given is the stack's `SITE_URL`: the checks also try that the pages lead to it.

```bash
deploy/check.sh https://pantrixui.com
```

## 5. The Site's Address

The address is given in two places, and written nowhere in the code ([ARCHITECTURE_GUIDE.en.md](./ARCHITECTURE_GUIDE.en.md), section 6.2):

| Setting | Where | When it changes |
| :--- | :--- | :--- |
| `site.url` | `gradle.properties` | When the site's lasting address changes; the site is built again |
| `SITE_URL` | Compose's environment variable: on this machine `compose.local.yaml` (`http://localhost:4004`), in Coolify the resource's environment variables | When the stack is served at another address; the container is restarted |

A content page names the address as `{{site}}`: `claude mcp add -t http pantrix-ui {{site}}/mcp`.

A new domain:

1.  `site.url=https://new-domain.com` in `gradle.properties`.
2.  Build and try it with `deploy/deploy.sh local`.
3.  Give `SITE_URL=https://new-domain.com` in Coolify, and deploy.

## 6. Troubleshooting

| What you see | Why, and what to do |
| :--- | :--- |
| `Docker is not running` | Start Docker Desktop, and run the command again |
| `port is already allocated` | Something else uses 4004: `SITE_PORT=4100 deploy/deploy.sh local` |
| Gradle cannot find the `com.pantrixui` packages | The key to GitHub Packages is missing, or has no `read:packages` (section 2) |
| `/mcp` answers 502 | The server is not up: `docker compose … logs server` |
| A new change does not show | `--no-gradle` uses the last build; run the command without it |
| Links lead to another address | The stack was given no `SITE_URL`, or the wrong one: `docker compose … exec web env \| grep SITE_` |
| Old images take up room | List them with `docker image ls 'ghcr.io/develappsancho/pantrix-ui-site-*'`, and delete them with `docker image rm` |

## 7. Deploying in Coolify

This section is written in the next step: pushing the images to GHCR, setting up the resource in Coolify, checking it at a test address, moving the domain, and taking the old site down.
