#!/usr/bin/env bash
# Puts Pantrix UI's site up from this machine: Gradle builds the site, its server and its desktop app, Docker makes the
# site's two images, and they run as one stack, here or on the host through Coolify. docs/deploy/DEPLOY_GUIDE.md has
# the steps, and docs/deploy/ARCHITECTURE_GUIDE.md what runs where.
#
#   deploy/deploy.sh local [--no-gradle]       the stack on this machine at http://localhost:4004, checked (check.sh)
#   deploy/deploy.sh stop                      stops it and removes its containers; the images stay
#   deploy/deploy.sh prepare [version]         names the changelog's Unreleased section the release's, to be committed
#   deploy/deploy.sh web [version] [options]   the images to GHCR, Coolify puts them up, the host checked
#   deploy/deploy.sh desktop [version] [opts]  the macOS app signed and notarized, on the release's GitHub page
#   deploy/deploy.sh release [version] [opts]  web, then desktop, from a tree that is committed and pushed
#
# A version is the release's date, YY.M.D (26.10.31): today's when none is given.
# Options: --dry-run builds everything and sends nothing, to GHCR, Coolify, GitHub or Apple: its DMG is signed and not
# notarized; --no-gradle takes what Gradle made last; --no-coolify pushes the images and leaves Coolify alone, as the
# first push does before the site's resource is made. SITE_PORT gives the local stack another port.
#
# Coolify's address, its API token and the site's resource are read from ~/.gradle/gradle.properties: CoolifyUrl,
# CoolifyToken, CoolifySiteUuid. The token never leaves that file but for the request's header.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
DEPLOY="$ROOT/deploy"
WEB="$ROOT/app/webApp/build/dist/wasmJs/productionExecutable"
JAR="$ROOT/server/build/libs/server.jar"
OUT="$ROOT/build/deploy"
REGISTRY="ghcr.io/develappsancho"
REPOSITORY="develappsancho/pantrix-ui-site"
DMG_NAME="PantrixUI-macOS.dmg"
GRADLE_PROPERTIES="$HOME/.gradle/gradle.properties"
export SITE_PORT="${SITE_PORT:-4004}"

fail() {
    echo "deploy: $*" >&2
    exit 1
}

step() {
    echo "== $*"
}

# --- What every command reads ---------------------------------------------------------------------------------------

# The images and the local stack are named by the commit they are built from, marked when the tree holds changes.
revision() {
    local commit
    commit="$(git -C "$ROOT" rev-parse --short HEAD)"
    if [ -n "$(git -C "$ROOT" status --porcelain)" ]; then
        echo "$commit-dirty"
    else
        echo "$commit"
    fi
}

# Today as a release's version, YY.M.D, with no leading zeros: three numbers, the first above 0, as macOS and MSI take.
today_version() {
    echo "$(date +%y).$((10#$(date +%m))).$((10#$(date +%d)))"
}

# The date a version names: 26.10.31 is 2026-10-31.
version_date() {
    local y m d
    IFS=. read -r y m d <<< "$1"
    printf '20%02d-%02d-%02d' "$((10#$y))" "$((10#$m))" "$((10#$d))"
}

check_version() {
    [[ "$1" =~ ^[0-9]{2}\.[0-9]{1,2}\.[0-9]{1,2}$ ]] || fail "a version is a date, YY.M.D (26.10.31), not \"$1\""
}

# A key of ~/.gradle/gradle.properties, or nothing.
gradle_property() {
    [ -f "$GRADLE_PROPERTIES" ] || return 0
    sed -n "s/^[[:space:]]*$1[[:space:]]*=[[:space:]]*//p" "$GRADLE_PROPERTIES" | tail -n 1 | tr -d '\r' | sed 's/[[:space:]]*$//'
}

# The site's address, site.url in gradle.properties.
site_url() {
    sed -n 's/^site\.url=//p' "$ROOT/gradle.properties" | tr -d '\r'
}

need_docker() {
    docker info > /dev/null 2>&1 || fail "Docker is not running: start Docker Desktop, then run this again"
}

# A release is made from what the repository holds: no change left out of a commit, and the commit on GitHub.
need_published_tree() {
    [ -z "$(git -C "$ROOT" status --porcelain)" ] || fail "the tree has changes that are not committed"
    local head remote
    head="$(git -C "$ROOT" rev-parse HEAD)"
    remote="$(git -C "$ROOT" ls-remote origin refs/heads/main | cut -f1)"
    [ "$head" = "$remote" ] || fail "main on GitHub is not this commit: push it first"
}

# GitHub's CLI, signed in as the account the images and the releases belong to, which may write packages.
need_github() {
    command -v gh > /dev/null || fail "GitHub's CLI, gh, is not installed"
    local login scopes
    login="$(gh api user --jq .login 2> /dev/null)" || fail "gh is not signed in: gh auth login"
    [ "$login" = "${REGISTRY#ghcr.io/}" ] || fail "gh is signed in as $login; switch to ${REGISTRY#ghcr.io/}: gh auth switch"
    scopes="$(gh api -i user 2> /dev/null | tr -d '\r' | sed -n 's/^[Xx]-[Oo][Aa]uth-[Ss]copes: //p')"
    [[ ",${scopes// /}," == *",write:packages,"* ]] || fail "gh may not write packages: gh auth refresh -h github.com -s write:packages"
}

gradle() {
    "$ROOT/gradlew" -p "$ROOT" --console=plain -q "$@"
}

# --- Coolify ---------------------------------------------------------------------------------------------------------

COOLIFY_HEADER=""

need_coolify() {
    COOLIFY_URL="$(gradle_property CoolifyUrl)"
    COOLIFY_URL="${COOLIFY_URL%/}"
    COOLIFY_UUID="$(gradle_property CoolifySiteUuid)"
    local token
    token="$(gradle_property CoolifyToken)"
    [ -n "$COOLIFY_URL" ] || fail "CoolifyUrl is not in $GRADLE_PROPERTIES"
    [ -n "$token" ] || fail "CoolifyToken is not in $GRADLE_PROPERTIES"
    [ -n "$COOLIFY_UUID" ] || fail "CoolifySiteUuid is not in $GRADLE_PROPERTIES: the site's resource in Coolify"
    # The token goes to curl in a file of its own, so that it is in no command's arguments.
    COOLIFY_HEADER="$(mktemp)"
    chmod 600 "$COOLIFY_HEADER"
    printf 'Authorization: Bearer %s\n' "$token" > "$COOLIFY_HEADER"
}

coolify() {
    curl -fsS --max-time 30 -H "@$COOLIFY_HEADER" -H 'Accept: application/json' "$COOLIFY_URL/api/v1/$1"
}

# What changes something in Coolify is asked for by POST: a deploy has been one since Coolify 4.5.
coolify_post() {
    curl -fsS --max-time 30 -X POST -H "@$COOLIFY_HEADER" -H 'Accept: application/json' "$COOLIFY_URL/api/v1/$1"
}

json() {
    python3 -c "import json, sys; data = json.load(sys.stdin); print($1)"
}

# Asks Coolify to put the site's resource up again, with the images just pushed, and waits until it is done.
coolify_deploy() {
    local answer deployment status last=""
    answer="$(coolify_post "deploy?uuid=$COOLIFY_UUID&force=false")" || fail "Coolify did not start the deploy"
    deployment="$(json "data['deployments'][0]['deployment_uuid']" <<< "$answer")"
    echo "Coolify's deployment $deployment"
    for _ in $(seq 1 120); do
        status="$(coolify "deployments/$deployment" | json "data.get('status', '')")" || status="unknown"
        if [ "$status" != "$last" ]; then echo "  $status"; last="$status"; fi
        case "$status" in
            finished) return 0 ;;
            failed | cancelled*) fail "Coolify's deployment $deployment ended $status: its log is in Coolify" ;;
        esac
        sleep 10
    done
    fail "Coolify's deployment $deployment did not end within 20 minutes"
}

# Coolify calls a deployment finished once its containers have started, and its proxy can answer 503 for a while
# after, until the new site takes requests: the checks wait for the home page, two minutes at most.
wait_for_site() {
    local address="$1" code=""
    for _ in $(seq 1 24); do
        code="$(curl -s -o /dev/null -w '%{http_code}' "$address/")" || code="000"
        [ "$code" = 200 ] && return 0
        sleep 5
    done
    echo "  $address/ still answers $code after two minutes"
}

# The address the site's resource is served at in Coolify: the web service's first domain. A Docker Compose resource
# keeps its domains by service, in docker_compose_domains, not in fqdn.
coolify_address() {
    coolify "applications/$COOLIFY_UUID" | python3 -c '
import json, sys
data = json.load(sys.stdin)
domains = data.get("docker_compose_domains") or {}
if isinstance(domains, str):
    domains = json.loads(domains or "{}")
domain = (domains.get("web") or {}).get("domain") or data.get("fqdn") or ""
print(domain.split(",")[0].strip().rstrip("/"))'
}

# --- Commands --------------------------------------------------------------------------------------------------------

local_up() {
    local gradle=true
    for argument in "$@"; do
        case "$argument" in
            --no-gradle) gradle=false ;;
            *) fail "unknown option: $argument" ;;
        esac
    done
    need_docker
    if $gradle; then
        step "Gradle: the site's web distribution, with its static pages, and the server's jar"
        gradle :app:webApp:wasmJsBrowserDistribution :server:bootJar
    fi
    [ -f "$WEB/index.html" ] || fail "no web distribution in $WEB: run without --no-gradle"
    [ -f "$JAR" ] || fail "no server jar at $JAR: run without --no-gradle"

    SITE_TAG="$(revision)"
    export SITE_TAG
    step "Docker: the images of $SITE_TAG, and the stack"
    compose_local up --build --detach --wait --remove-orphans
    compose_local images

    step "The checks"
    "$DEPLOY/check.sh" "http://localhost:$SITE_PORT"
    echo "The site is at http://localhost:$SITE_PORT"
}

# Compose on this machine: the stack with the parts that build its images here and publish its port.
compose_local() {
    docker compose -f "$DEPLOY/compose.yaml" -f "$DEPLOY/compose.local.yaml" "$@"
}

# Names the changelog's Unreleased section, in both languages, the release's, and opens a new one above it.
prepare() {
    local version="$1"
    python3 - "$ROOT" "$version" "$(version_date "$version")" << 'EOF'
import sys
root, version, date = sys.argv[1:]
for name, unreleased in (("CHANGELOG.md", "## [Unreleased]"), ("CHANGELOG.tr.md", "## [Yayınlanmamış]")):
    path = f"{root}/{name}"
    text = open(path, encoding="utf-8").read()
    if f"## [{version}]" in text:
        sys.exit(f"{name} names {version} already")
    if text.count(unreleased) != 1:
        sys.exit(f"{name} has no {unreleased} section")
    text = text.replace(unreleased, f"{unreleased}\n\n## [{version}] - {date}")
    open(path, "w", encoding="utf-8").write(text)
    print(f"{name}: {unreleased} is now [{version}] - {date}")
EOF
    echo "Look at both files, write the release's first line where it needs one, then commit and push them."
}

# The site's images for the host, both architectures, pushed to GHCR; Coolify puts them up and the host is checked.
web() {
    local version="$1" dry="$2" gradle_build="$3" ask_coolify="$4"
    need_docker
    if ! $dry; then
        need_published_tree
        need_github
        if $ask_coolify; then need_coolify; fi
    fi
    if $gradle_build; then
        step "Gradle: the site's web distribution, with its static pages, and the server's jar"
        gradle :app:webApp:wasmJsBrowserDistribution :server:bootJar
    fi
    [ -f "$WEB/index.html" ] || fail "no web distribution in $WEB: run without --no-gradle"
    [ -f "$JAR" ] || fail "no server jar at $JAR: run without --no-gradle"

    local commit
    commit="$(revision)"
    local output=(--push)
    if $dry; then
        # Built for both architectures, and kept nowhere: what a push would send.
        output=(--output type=cacheonly)
    else
        step "GHCR: signing in as ${REGISTRY#ghcr.io/}"
        gh auth token | docker login ghcr.io -u "${REGISTRY#ghcr.io/}" --password-stdin > /dev/null
    fi
    local image
    for image in web server; do
        step "The $image image, $version ($commit), for linux/amd64 and linux/arm64"
        local context=(--build-context "site=$WEB")
        [ "$image" = server ] && context=(--build-context "jar=$(dirname "$JAR")")
        # Both architectures make an index, and GHCR reads the repository a package belongs to from the index's
        # annotations: the Dockerfile's labels are each architecture's.
        docker buildx build --platform linux/amd64,linux/arm64 \
            -f "$DEPLOY/$image/Dockerfile" "${context[@]}" --build-arg "REVISION=$commit" \
            --annotation "index:org.opencontainers.image.source=https://github.com/$REPOSITORY" \
            --annotation "index:org.opencontainers.image.licenses=Apache-2.0" \
            -t "$REGISTRY/pantrix-ui-site-$image:$version" -t "$REGISTRY/pantrix-ui-site-$image:$commit" \
            -t "$REGISTRY/pantrix-ui-site-$image:latest" \
            "${output[@]}" "$DEPLOY/$image"
    done
    if $dry; then
        echo "Dry run: nothing pushed, and Coolify not asked."
        return
    fi
    if ! $ask_coolify; then
        echo "Pushed, and Coolify not asked (--no-coolify)."
        return
    fi

    step "Coolify: the site's resource, with the images just pushed"
    coolify_deploy
    local address
    address="$(coolify_address)"
    [ -n "$address" ] || fail "the site's resource in Coolify has no domain"
    wait_for_site "$address"
    step "The checks, at $address"
    local site_host
    site_host="$(site_url)"
    site_host="${site_host#*://}"
    local host="${address#*://}"
    # The second name, www, is the lasting domain's alone: a test address has none.
    if [ "${host%%[:/]*}" = "$site_host" ]; then
        "$DEPLOY/check.sh" "$address"
    else
        CHECK_WWW=no "$DEPLOY/check.sh" "$address"
    fi
}

# The desktop app for macOS, signed and notarized, on the release's GitHub page with its notes from the changelog.
desktop() {
    local version="$1" dry="$2" gradle_build="$3"
    [ "$(uname)" = Darwin ] || fail "the macOS app is built on a Mac"
    if ! $dry; then
        need_published_tree
        need_github
    fi
    if $gradle_build; then
        step "Gradle: the desktop app, $version"
        gradle :app:desktopApp:createDistributable "-Psite.version=$version"
    fi
    local built="$ROOT/app/desktopApp/build/compose/binaries/main/app/Pantrix UI.app"
    [ -d "$built" ] || fail "no app at $built: run without --no-gradle"
    local plist="$built/Contents/Info.plist"
    local built_version
    built_version="$(/usr/libexec/PlistBuddy -c 'Print :CFBundleShortVersionString' "$plist")"
    [ "$built_version" = "$version" ] || fail "the app is $built_version, not $version: run without --no-gradle"

    step "The DMG: signed with the Developer ID$($dry || echo ', and notarized by Apple')"
    local work="$OUT/desktop"
    rm -rf "$work"
    mkdir -p "$work"
    ditto "$built" "$work/Pantrix UI.app"
    # A release's DMG is always notarized, or Gatekeeper stops it; a dry run sends nothing to Apple.
    local notarization=()
    $dry && notarization=(--no-notarize)
    "$DEPLOY/desktop/macos-dmg.sh" "$work/Pantrix UI.app" "$work/$DMG_NAME" ${notarization[@]+"${notarization[@]}"}
    local sha size
    sha="$(shasum -a 256 "$work/$DMG_NAME" | cut -d' ' -f1)"
    # Its bytes, in MB as GitHub shows them: du counts the disk's blocks.
    size="$(stat -f %z "$work/$DMG_NAME" | awk '{ printf "%.1f MB", $1 / 1000000 }')"
    echo "$DMG_NAME: $size, SHA-256 $sha"

    local notes="$work/notes.md"
    release_notes "$version" "$sha" > "$notes"
    if $dry; then
        echo "Dry run: the release v$version is not made. Its notes:"
        cat "$notes"
        return
    fi
    step "GitHub: the release v$version of $REPOSITORY"
    if gh release view "v$version" -R "$REPOSITORY" > /dev/null 2>&1; then
        gh release upload "v$version" "$work/$DMG_NAME" --clobber -R "$REPOSITORY"
        gh release edit "v$version" --notes-file "$notes" -R "$REPOSITORY"
    else
        gh release create "v$version" "$work/$DMG_NAME" -R "$REPOSITORY" --target "$(git -C "$ROOT" rev-parse HEAD)" \
            --title "$version" --notes-file "$notes" --latest
    fi
    echo "https://github.com/$REPOSITORY/releases/tag/v$version"
}

# The release's notes: the changelog's section of the version, where it has one, and how to check the DMG. The
# changelog wraps its lines at 120 columns, and GitHub breaks a release's lines where they break: each paragraph and
# each item of a list is joined into one line.
release_notes() {
    local version="$1" sha="$2"
    python3 - "$ROOT/CHANGELOG.md" "$version" << 'EOF'
import re, sys
text = open(sys.argv[1], encoding="utf-8").read()
match = re.search(rf"^## \[{re.escape(sys.argv[2])}\][^\n]*\n(.*?)(?=^## \[|\Z)", text, re.S | re.M)
section = match.group(1).strip() if match else f"Pantrix UI's site, {sys.argv[2]}."
lines = []
for line in section.split("\n"):
    words = line.strip()
    starts = not words or words.startswith(("#", "- ", "* ", "```", "|")) or re.match(r"\d+\. ", words)
    if lines and lines[-1].strip() and not lines[-1].lstrip().startswith("#") and not starts:
        lines[-1] += " " + words
    else:
        lines.append(line)
print("\n".join(lines))
EOF
    printf '\n### The desktop app\n\n`%s`: for macOS 13 or later on Apple silicon, signed with a Developer ID and notarized by Apple. Its SHA-256:\n\n```\n%s\n```\n' "$DMG_NAME" "$sha"
}

# --- The command line ------------------------------------------------------------------------------------------------

main() {
    local command="${1:-}"
    [ $# -gt 0 ] && shift
    case "$command" in
        local) local_up "$@"; return ;;
        stop) need_docker; compose_local down --remove-orphans; return ;;
        prepare | web | desktop | release) ;;
        # The header above, whatever its length: the comment lines after the first line.
        *) awk 'NR > 1 && !/^#/ { exit } NR > 1 { sub(/^# ?/, ""); print }' "$0"; exit 2 ;;
    esac
    local version="" dry=false gradle_build=true ask_coolify=true
    for argument in "$@"; do
        case "$argument" in
            --dry-run) dry=true ;;
            --no-gradle) gradle_build=false ;;
            --no-coolify) ask_coolify=false ;;
            -*) fail "unknown option: $argument" ;;
            *) version="$argument" ;;
        esac
    done
    version="${version:-$(today_version)}"
    check_version "$version"
    case "$command" in
        prepare) prepare "$version" ;;
        web) web "$version" "$dry" "$gradle_build" "$ask_coolify" ;;
        desktop) desktop "$version" "$dry" "$gradle_build" ;;
        release)
            if ! $dry; then
                grep -q "^## \[$version\]" "$ROOT/CHANGELOG.md" ||
                    fail "CHANGELOG.md has no section $version: deploy/deploy.sh prepare $version, then commit and push"
            fi
            web "$version" "$dry" "$gradle_build" "$ask_coolify"
            desktop "$version" "$dry" "$gradle_build"
            ;;
    esac
}

trap 'if [ -n "$COOLIFY_HEADER" ]; then rm -f "$COOLIFY_HEADER"; fi' EXIT
main "$@"
