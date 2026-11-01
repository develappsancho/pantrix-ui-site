#!/usr/bin/env bash
# Checks Pantrix UI's site where it runs, as a browser and an assistant reach it: its pages at their addresses, the
# page for an address with none, what is compressed and how long a browser keeps it, the second name, and the MCP
# server at /mcp. Each check writes a line; the script fails when one does.
#
#   deploy/check.sh                          the site on this machine, http://localhost:4004 (or SITE_PORT)
#   deploy/check.sh https://pantrixui.com    the site on its host
set -uo pipefail

BASE="${1:-http://localhost:${SITE_PORT:-4004}}"
BASE="${BASE%/}"
WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT
passed=0
failed=0

MCP_HEADERS=(-H 'Content-Type: application/json' -H 'Accept: application/json, text/event-stream')
INITIALIZE='{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-11-25","capabilities":{},"clientInfo":{"name":"check","version":"1"}}}'
TOOLS='{"jsonrpc":"2.0","id":2,"method":"tools/list"}'
SEARCH='{"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"search_components","arguments":{"query":"toast"}}}'

# Asks for $2 with the method $1, the rest given to curl as they are: the status, the headers and the body are kept
# for the checks after it. HEAD asks for the headers alone.
fetch() {
    local method="$1" path="$2"
    shift 2
    local how=(-X "$method")
    [ "$method" = HEAD ] && how=(-I)
    curl -s -o "$WORK/body" -D "$WORK/head" -w '%{http_code}' --max-time 30 "${how[@]}" "$@" "$BASE$path" \
        > "$WORK/status" 2> /dev/null || echo "000" > "$WORK/status"
    [ -f "$WORK/body" ] || : > "$WORK/body"
}

status() { cat "$WORK/status"; }
header() { grep -i "^$1:" "$WORK/head" | tail -1 | cut -d: -f2- | tr -d '\r' | sed 's/^ *//'; }
has() { grep -qF -- "$1" "$WORK/body"; }

# Writes the line of the check named $1: passed when the command after it succeeds.
check() {
    local name="$1"
    shift
    if "$@"; then
        passed=$((passed + 1))
        printf '  ✓ %s\n' "$name"
    else
        failed=$((failed + 1))
        printf '  ✗ %s (status %s, %s)\n' "$name" "$(status)" "$(header Content-Type)"
    fi
}

is() { [ "$1" = "$2" ]; }
contains() { case "$1" in *"$2"*) return 0 ;; *) return 1 ;; esac; }

echo "Pantrix UI site at $BASE"

echo "Pages"
fetch GET /
check "/ is the home page" eval 'is "$(status)" 200 && contains "$(header Content-Type)" text/html && has "<title>"'
fetch GET /docs/installation
check "/docs/installation is its page, with no redirect" \
    eval 'is "$(status)" 200 && [ -z "$(header Location)" ] && has "rel=\"canonical\" href=\"https://pantrixui.com/docs/installation\""'
fetch GET /tr/docs/installation
check "/tr/docs/installation is its Turkish page" \
    eval 'is "$(status)" 200 && has "rel=\"canonical\" href=\"https://pantrixui.com/tr/docs/installation\""'
fetch GET /components/button
check "/components/button is a component's page" eval 'is "$(status)" 200 && has "<title>"'
fetch GET /404.html
missing_title="$(grep -o '<title>[^<]*</title>' "$WORK/body" | head -1)"
fetch GET /no/page/here
check "an address with no page is 404.html, with a 404" \
    eval 'is "$(status)" 404 && [ -n "$missing_title" ] && has "$missing_title"'
fetch GET /llms.txt
check "/llms.txt is plain text in UTF-8" \
    eval 'is "$(status)" 200 && contains "$(header Content-Type)" "text/plain; charset=utf-8" && has "# Pantrix UI"'
fetch GET /sitemap.xml
check "/sitemap.xml lists the pages" eval 'is "$(status)" 200 && has "<urlset"'
fetch GET /robots.txt
check "/robots.txt names the sitemap" eval 'is "$(status)" 200 && has "Sitemap:"'

echo "Compression and caching"
fetch HEAD / -H 'Accept-Encoding: gzip'
check "a page is sent compressed, and asked for again each time" \
    eval 'is "$(header Content-Encoding)" gzip && is "$(header Cache-Control)" no-cache && contains "$(header Vary)" Accept-Encoding'
fetch GET /site.js
wasm="$(grep -oE '[0-9a-f]{16,}\.wasm' "$WORK/body" | sort -u)"
check "the app's script is asked for again each time" eval 'is "$(header Cache-Control)" no-cache'
check "the app's script names its WebAssembly" eval '[ -n "$wasm" ]'
for file in $wasm; do
    fetch HEAD "/$file" -H 'Accept-Encoding: gzip'
    check "/$file is WebAssembly, compressed, kept for a year" \
        eval 'is "$(status)" 200 && is "$(header Content-Type)" application/wasm && is "$(header Content-Encoding)" gzip && contains "$(header Cache-Control)" immutable'
done
fetch HEAD /
check "the headers name no version of the server, and forbid guessing a file's type" \
    eval '! header Server | grep -q "[0-9]" && is "$(header X-Content-Type-Options)" nosniff'

echo "Second name"
case "$BASE" in
    http://localhost* | http://127.0.0.1*)
        fetch GET /docs -H 'Host: www.pantrixui.com'
        check "www.pantrixui.com is sent to pantrixui.com for good" \
            eval 'is "$(status)" 301 && is "$(header Location)" https://pantrixui.com/docs'
        ;;
    https://pantrixui.com)
        BASE=https://www.pantrixui.com fetch GET /docs
        check "www.pantrixui.com is sent to pantrixui.com for good" \
            eval 'is "$(status)" 301 && is "$(header Location)" https://pantrixui.com/docs'
        ;;
    *)
        echo "  – left out: the second name is only the domain's"
        ;;
esac

echo "MCP server"
fetch POST /mcp "${MCP_HEADERS[@]}" --data-binary "$INITIALIZE"
check "initialize is answered by pantrix-ui" \
    eval 'is "$(status)" 200 && is "$(header Content-Type)" application/json && has "\"name\":\"pantrix-ui\""'
fetch POST /mcp "${MCP_HEADERS[@]}" --data-binary "$TOOLS"
check "tools/list lists search_components" eval 'is "$(status)" 200 && has "search_components"'
fetch POST /mcp "${MCP_HEADERS[@]}" --data-binary "$SEARCH"
check "search_components finds the snackbar for a toast" eval 'is "$(status)" 200 && has "Snackbar"'
fetch GET /mcp -H 'Accept: text/event-stream'
check "GET is refused: the server opens no stream" eval 'is "$(status)" 405'
fetch POST /mcp "${MCP_HEADERS[@]}" -H 'Origin: https://elsewhere.example' --data-binary "$TOOLS"
check "a page of another site is refused" eval 'is "$(status)" 403'
head -c 70000 /dev/zero | tr '\0' 'a' > "$WORK/large"
fetch POST /mcp "${MCP_HEADERS[@]}" --data-binary "@$WORK/large"
check "a message over 64 KB is refused" eval 'is "$(status)" 413'

echo "$passed passed, $failed failed"
[ "$failed" = 0 ]
