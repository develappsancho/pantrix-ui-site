#!/bin/sh
# Writes the address the stack is served at, SITE_URL, into the site's files in place of the one they give,
# SITE_FILES_URL (05-site-address.envsh): its pages' own links, its sitemap, llms.txt and the app's content. Only the
# whole address is written, with its scheme: a host alone is a word, which the guides' examples use too. A file's .gz
# is made again beside it. Nothing is done where the two are the same, as on the host the site is built for.
set -eu
root=/usr/share/nginx/html
if [ -z "$SITE_FILES_URL" ] || [ "$SITE_URL" = "$SITE_FILES_URL" ]; then
    exit 0
fi
case "$SITE_URL" in
    http://* | https://*) ;;
    *) echo "$0: SITE_URL is \"$SITE_URL\": give an address such as https://pantrixui.com" >&2; exit 1 ;;
esac
echo "$0: writing $SITE_URL in place of $SITE_FILES_URL"
from=$(printf '%s' "$SITE_FILES_URL" | sed 's/[.]/\\./g')
find "$root" -type f \( -name '*.html' -o -name '*.json' -o -name '*.txt' -o -name '*.md' -o -name '*.xml' \) \
    -exec grep -lF "$SITE_FILES_URL" {} + | while read -r file; do
    # The address and what follows it, so that https://pantrixui.com is not found in a longer host.
    sed -E -i "s#$from([^A-Za-z0-9.-]|\$)#$SITE_URL\\1#g" "$file"
    if [ -f "$file.gz" ] || [ "$(wc -c < "$file")" -gt 1024 ]; then
        gzip -9 -k -n -f "$file"
    fi
done
