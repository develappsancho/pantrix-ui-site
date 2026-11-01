# syntax=docker/dockerfile:1
# The site's image: its files behind nginx. The web distribution is given to the build as the context `site`
# (compose.local.yaml): Gradle has made it on this machine, and nothing here compiles.

# Every file worth it is compressed once, here, on the machine that builds and in its own architecture, so that nginx
# sends the .gz beside a file instead of compressing it again for each request. The source map stays out.
FROM --platform=$BUILDPLATFORM alpine:3.24 AS files
COPY --from=site / /site/
RUN rm -f /site/*.map \
    && find /site -type f -size +1k \( -name '*.html' -o -name '*.css' -o -name '*.js' -o -name '*.wasm' \
        -o -name '*.json' -o -name '*.txt' -o -name '*.md' -o -name '*.xml' -o -name '*.svg' -o -name '*.ttf' \) \
        -exec gzip -9 -k -n {} +

FROM nginx:1.30-alpine
ARG REVISION=unknown
LABEL org.opencontainers.image.title="Pantrix UI site" \
      org.opencontainers.image.description="The website of Pantrix UI: its files, served by nginx" \
      org.opencontainers.image.source="https://github.com/develappsancho/pantrix-ui-site" \
      org.opencontainers.image.revision="$REVISION"
# The address the stack is served at, SITE_URL, is written as the container starts: into nginx's settings from the
# template, and into the site's files (05-site-address.envsh, 40-site-address.sh). Only SITE_ variables are written
# into the template, so that nginx's own, such as $uri, stay as they are.
ENV NGINX_ENVSUBST_FILTER=^SITE_
COPY default.conf.template /etc/nginx/templates/default.conf.template
COPY --chmod=755 05-site-address.envsh 40-site-address.sh /docker-entrypoint.d/
COPY --from=files /site/ /usr/share/nginx/html/
HEALTHCHECK --interval=30s --timeout=5s --start-period=5s --retries=3 \
    CMD wget -q -O /dev/null http://127.0.0.1/ || exit 1
