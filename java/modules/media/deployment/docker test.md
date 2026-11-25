# Docker end-to-end test

The media service is tested end-to-end with docker compose. One command runs everything from the
Gradle root:

```bash
./gradlew :media-api:integrationTest
```

The task builds the image worker's jar and the JDK + libvips dev image (`build.bash`), the test
boots the compose stack with Testcontainers (local compose, the same `compose.yaml` the development
workflow uses), and drives the API through the `media-client` module over HTTP only. The containers
are reaped when the test JVM exits. The compose file publishes fixed host ports, so stop the
development stack first (`./run.bash down`); the test fails fast with this message when the ports
are taken.

## Development workflow

`build.bash` builds the dev image, `run.bash` starts the containers, then start the `media-api`
application (set `MINIO_RECONCILIATION_ENABLED=true` so it provisions the uploads bucket) or run
the test suite above.

## Transformation tests

`./test-transformations.bash` runs the image transformation output tests in the dev image (they
need the native libvips library).

## Async coverage

The async transformations are tested by the same end-to-end suite: the queue runs in RabbitMQ, the
API boots in-process on a real port, and the worker calls the completion endpoint back through the
host gateway (`host.docker.internal`), so the compose file must map the API's port and add the
`extra_hosts` entry.
