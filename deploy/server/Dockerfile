# syntax=docker/dockerfile:1
# The server's image: the jar that runs by itself, which Gradle makes on this machine (`./gradlew :server:bootJar`),
# given to the build as the context `jar` (compose.local.yaml), on Java 21's runtime.
FROM eclipse-temurin:21-jre-alpine
ARG REVISION=unknown
LABEL org.opencontainers.image.title="Pantrix UI site server" \
      org.opencontainers.image.description="The website of Pantrix UI's server: Pantrix UI's MCP server at /mcp" \
      org.opencontainers.image.source="https://github.com/develappsancho/pantrix-ui-site" \
      org.opencontainers.image.revision="$REVISION"
WORKDIR /app
COPY --from=jar server.jar /app/server.jar
# The server writes nothing but its temporary files, and runs as a user with no rights of its own.
USER 65534:65534
EXPOSE 8080
# Alive is answering an MCP message, the ping, as an assistant would send it.
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD wget -q -O /dev/null --header='Content-Type: application/json' \
        --header='Accept: application/json, text/event-stream' \
        --post-data='{"jsonrpc":"2.0","id":1,"method":"ping"}' http://127.0.0.1:8080/mcp || exit 1
# The heap takes three quarters of what the container is given, and a server out of memory stops, to be started again.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-XX:+ExitOnOutOfMemoryError", "-jar", "/app/server.jar"]
