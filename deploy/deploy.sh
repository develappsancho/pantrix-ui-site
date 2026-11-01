#!/usr/bin/env bash
# Puts Pantrix UI's site up from this machine: Gradle builds the site and its server, Docker makes their two images,
# and Compose runs them as one stack. docs/deploy/DEPLOY_GUIDE.md has the steps, and
# docs/deploy/ARCHITECTURE_GUIDE.md what runs where.
#
#   deploy/deploy.sh local              builds, runs the stack at http://localhost:4004 and checks it (check.sh)
#   deploy/deploy.sh local --no-gradle  the same with what Gradle made last, for a change to deploy/ alone
#   deploy/deploy.sh stop               stops the stack and removes its containers; the images stay
#
# SITE_PORT gives the stack another port on this machine.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
DEPLOY="$ROOT/deploy"
WEB="$ROOT/app/webApp/build/dist/wasmJs/productionExecutable"
JAR="$ROOT/server/build/libs/server.jar"
export SITE_PORT="${SITE_PORT:-4004}"

# The images are named by the commit they are built from, marked when the tree holds changes not yet committed.
revision() {
    local commit
    commit="$(git -C "$ROOT" rev-parse --short HEAD)"
    if [ -n "$(git -C "$ROOT" status --porcelain)" ]; then
        echo "$commit-dirty"
    else
        echo "$commit"
    fi
}

# Compose on this machine: the stack with the parts that build its images here and publish its port.
compose_local() {
    docker compose -f "$DEPLOY/compose.yaml" -f "$DEPLOY/compose.local.yaml" "$@"
}

need_docker() {
    if ! docker info > /dev/null 2>&1; then
        echo "Docker is not running: start Docker Desktop, then run this again." >&2
        exit 1
    fi
}

local_up() {
    local gradle=true
    for argument in "$@"; do
        case "$argument" in
            --no-gradle) gradle=false ;;
            *) echo "Unknown option: $argument" >&2; exit 2 ;;
        esac
    done
    need_docker
    if $gradle; then
        echo "== Gradle: the site's web distribution, with its static pages, and the server's jar"
        "$ROOT/gradlew" -p "$ROOT" :app:webApp:wasmJsBrowserDistribution :server:bootJar --console=plain -q
    fi
    [ -f "$WEB/index.html" ] || { echo "No web distribution in $WEB: run without --no-gradle." >&2; exit 1; }
    [ -f "$JAR" ] || { echo "No server jar at $JAR: run without --no-gradle." >&2; exit 1; }

    SITE_TAG="$(revision)"
    export SITE_TAG
    echo "== Docker: the images of $SITE_TAG, and the stack"
    compose_local up --build --detach --wait --remove-orphans
    compose_local images

    echo "== The checks"
    "$DEPLOY/check.sh" "http://localhost:$SITE_PORT"
    echo "The site is at http://localhost:$SITE_PORT"
}

case "${1:-}" in
    local)
        shift
        local_up "$@"
        ;;
    stop)
        need_docker
        compose_local down --remove-orphans
        ;;
    *)
        sed -n '2,10p' "$0" | sed 's/^# \{0,1\}//'
        exit 2
        ;;
esac
