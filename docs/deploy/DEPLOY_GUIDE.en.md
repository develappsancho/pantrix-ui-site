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
*   **For the host** (section 7): GitHub's command line tool, `gh`, with `write:packages`; Coolify's address, its API token and the resource's UUID in `~/.gradle/gradle.properties`.
*   **For the desktop app's release:** Xcode's command line tools, the Developer ID certificate in the keychain, and the details Apple's notarization needs, stored as `pantrix-notary` (section 7.1).

## 3. On This Machine

```bash
deploy/deploy.sh local
```

The command does four things:

1.  **Gradle:** `:app:webApp:wasmJsBrowserDistribution` writes the site's production build, its JavaScript build beside it (`site-js.js`, for a browser that cannot run the Wasm one) and each page's static HTML (`app/webApp/build/dist/wasmJs/productionExecutable`), and `:server:bootJar` makes the server's jar (`server/build/libs/server.jar`). The first build takes a few minutes; optimising the Wasm is the longest step.
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
| Compression and caching | A page gzipped and `no-cache`; the loader `site.js` `no-cache`, naming both builds; `site-wasm.js` `no-cache`, naming its Wasm; `site-js.js` `no-cache`, drawing with the Wasm build's Skia; neither build holding a `file:///` path of the machine that built it; each Wasm file as `application/wasm`, gzipped and cached for a year; no version of the server in the headers, and `nosniff` |
| Security | `Strict-Transport-Security` over HTTPS, and none over plain HTTP; the content security policy reported, with the hash of the pages' inline script |
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

On the host, the site runs on a VPS that Coolify manages, at `https://pantrixui.com`. The images are built on this machine and pushed to GHCR; Coolify reads `deploy/compose.yaml` from the repository, pulls the images and starts the stack. Nothing is compiled on the VPS. The desktop app is released with the same version, on the site's release page on GitHub.

### 7.1 Setting It Up Once

1.  **Leave to push to GHCR.** The images go out under the account `gh` is signed in with: `develappsancho`. The scope is added once:

    ```bash
    gh auth refresh -h github.com -s write:packages
    ```

2.  **Coolify's API token.** In Coolify, **Keys & Tokens** → **API Tokens** makes a token with `read` and `deploy`. The panel's address and the token go into `~/.gradle/gradle.properties`, never into the repository:

    ```properties
    CoolifyUrl=https://coolify.example.com
    CoolifyToken=…
    ```

    This token cannot read the environment variables' values: the API shows them empty. The panel shows them.
3.  **The first push.** The resource in Coolify cannot deploy before the images are in GHCR. The first push leaves Coolify alone:

    ```bash
    deploy/deploy.sh web --no-coolify
    ```

    The two packages, `pantrix-ui-site-web` and `pantrix-ui-site-server`, are made private. They link themselves to the repository by the `org.opencontainers.image.source` on the images' index. For each, on GitHub: the package's page → **Package settings** → **Danger Zone** → **Change visibility** → **Public**. Coolify pulls them with no key.
4.  **The resource in Coolify.** In **Projects**, the project's environment → **+ New** → **Public Repository**:

    | Field | Value |
    | :--- | :--- |
    | Repository URL | `https://github.com/develappsancho/pantrix-ui-site` |
    | Branch | `main` |
    | Build Pack | Docker Compose |
    | Base Directory | `/` |
    | Docker Compose Location | `/deploy/compose.yaml` |
    | `web`'s domains | `https://pantrixui.com,https://www.pantrixui.com` |
    | `server`'s domain | Empty: it stands behind nginx alone |
    | Environment variable | `SITE_URL=https://pantrixui.com` |

    **Save** keeps it; the script starts the first deploy.
5.  **The resource's UUID.** In the panel it is the last part of the resource's address: `…/application/<uuid>`. The id in the resource's name is not its UUID: the name is `pantrix-ui-site:main-cntrex…` while the UUID is `sfyuo3…`. The UUID goes into `~/.gradle/gradle.properties` too:

    ```properties
    CoolifySiteUuid=sfyuo3lhlbdznx7whlflkz3f
    ```

6.  **DNS.** `pantrixui.com` and `www.pantrixui.com` lead to the VPS's address. Traefik gets their certificates from Let's Encrypt at the first deploy.
7.  **Apple's notarization.** The Developer ID certificate must be in the keychain. The Apple account details notarization needs are stored in the keychain once, under the name `pantrix-notary`, and the script reads them by that name:

    ```bash
    xcrun notarytool store-credentials pantrix-notary
    ```

### 7.2 Each Release

A release is made from a commit that is on GitHub. With a change left out of a commit, or a local `main` that is not GitHub's, the script does not start.

| What is released | Steps |
| :--- | :--- |
| The site alone | Commit and push, then `deploy/deploy.sh web` |
| The site and the desktop app | `deploy/deploy.sh prepare`, a look at both changelogs, commit and push, then `deploy/deploy.sh release` |
| The desktop app alone | The version's section in the changelog, commit and push, then `deploy/deploy.sh desktop` |

The commands are run at the root of the site's repository. With no version given, it is today's date; another day's is written at the end: `deploy/deploy.sh release 26.11.2`.

*   **The site alone:**

    ```bash
    deploy/deploy.sh web
    ```

*   **The site and the desktop app:** first the changelogs' section is named:

    ```bash
    deploy/deploy.sh prepare
    ```

    Both changelogs are looked at, committed and pushed; then:

    ```bash
    deploy/deploy.sh release
    ```

*   **The desktop app alone:**

    ```bash
    deploy/deploy.sh desktop
    ```

*   **A try first:** everything is built and the DMG signed, and nothing is sent anywhere:

    ```bash
    deploy/deploy.sh release --dry-run
    ```

*   **Checking the host again:**

    ```bash
    deploy/check.sh https://pantrixui.com
    ```

`web`:

1.  Builds the site and the server with Gradle.
2.  Makes both images for `linux/amd64` and `linux/arm64` and pushes them to GHCR with three tags: the version (`26.10.31`), the commit (`08ec2a5`) and `latest`. The VPS pulls its own architecture's.
3.  Asks Coolify for a deploy by POST, reads its status every 10 seconds and waits for it to end (about 30 seconds).
4.  Waits for the home page to answer 200, two minutes at most: Coolify calls a deploy finished once its containers have started, and its proxy can answer 503 for a while, until the new site takes requests. Then runs `check.sh` at the live address. `www` is tried on the lasting domain alone.

`desktop`:

1.  Builds the app with its version (`-Psite.version=26.10.31`).
2.  Signs the DMG with the Developer ID, sends it to Apple for notarization and staples the ticket to it. Notarization takes some minutes.
3.  Opens the release `v<version>` on GitHub and attaches `PantrixUI-macOS.dmg`. Its notes are the changelog's section, each paragraph joined into one line, as GitHub breaks a release's lines where they break. The DMG's SHA-256 ends them.

The file has the same name in every release, so `releases/latest/download/PantrixUI-macOS.dmg` always gives the latest. The download page reads the version, the day, the size and the SHA-256 from GitHub's API in the browser; the day is the one the version names.

### 7.3 Going Back to a Release

The compose file pulls the images as `${SITE_TAG:-latest}`. With `SITE_TAG=26.10.31` (or a commit) added to the resource's environment variables in Coolify and a new deploy, that release runs. With the variable removed, the stack goes back to `latest`.

### 7.4 The Old Site

The previous demo, `pantrix-u-i--demo`, stays stopped in Coolify with the same domains. Started again, two resources would carry one name and Traefik's routing would be muddled; removing its domains, or the resource, prevents that.

A browser that opened the old site can show, at first, only the new site's words written for readers with no JavaScript: the old site's cache keeps the app from starting. A hard reload (Cmd + Shift + R), or **Application** → **Clear site data** in the developer tools, mends it.

### 7.5 Troubleshooting

| What you see | Why, and what to do |
| :--- | :--- |
| `gh may not write packages` | The scope of 7.1 is missing: `gh auth refresh -h github.com -s write:packages` |
| `main on GitHub is not this commit` | The last commit is not pushed |
| Coolify answers `401` or `403` | The token is wrong, or has no `deploy` |
| Coolify answers `404` | `CoolifySiteUuid` is the id in the resource's name; it must be the last part of its address |
| Coolify answers `405` | Since Coolify 4.5 a deploy is asked for by POST; an old copy of the script sends GET |
| Coolify cannot pull an image | The package is still private (7.1, step 3) |
| `www` has no certificate | `www.pantrixui.com` is not among `web`'s domains |
| The download page shows no version | No release yet, GitHub's API has had its hourly limit (60 requests) from that address, or a CSP does not allow `https://api.github.com` |
| macOS does not open the app | The DMG is not notarized: `--dry-run` does not send it to Apple. `spctl -a -vv -t open --context context:primary-signature PantrixUI-macOS.dmg` must say "Notarized Developer ID" |
