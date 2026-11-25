# Compose deployment for the media service
This directory contains a docker compose file and some bash scripts for starting the image worker, the database, and the object storage.

Starting the media API in addition makes the whole network work together, making the end-to-end test runnable.

## Usage
- Build the JDK + libvips development image that runs the worker (the `media.Dockerfile` with its `build.bash`; the end-to-end test and `test-transformations.bash` build it automatically).
- Build the media API and image worker jars and start the compose file with the `run.bash` in this directory.
- Start the `media-api` application or run the end-to-end integration test in it (`EndToEndTest` in `media-api`).

## Tests
- `./gradlew :media-api:integrationTest` (from the Gradle root) runs the end-to-end test: it builds the worker jar and this directory's dev image, boots this compose file with Testcontainers, and drives the API through the `media-client` module.
- `./test-transformations.bash` runs the image transformation output tests in the dev image (the tests need the native libvips library).
- The compose file must not set `container_name:` — Testcontainers rejects fixed names.