#!/bin/bash
# Runs the image transformation tests in the JDK + libvips development image.
# The tests need the native libvips library, which the host JDK does not provide;
# the image is the same one that runs the image worker in compose.
# Usage: ./test-transformations.bash [extra gradle arguments...]

set -euo pipefail

DEPLOYMENT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$DEPLOYMENT_DIR/../../.." && pwd)"

# Build the image when missing; docker build is cached and cheap when it exists.
"$DEPLOYMENT_DIR/build.bash"

# The entrypoint drops root to HOST_UID:HOST_GID (so build outputs stay host-owned)
# and keeps the Gradle cache on a named volume across runs.
docker run --rm \
  -e HOST_UID="$(id -u)" \
  -e HOST_GID="$(id -g)" \
  -e GRADLE_USER_HOME=/gradle-home \
  -v "$ROOT_DIR":/workspace \
  -v java-image-test-gradle:/gradle-home \
  -v "$DEPLOYMENT_DIR/entrypoint.bash":/entrypoint.bash:ro \
  -w /workspace \
  --entrypoint /entrypoint.bash \
  java-image-test \
  ./gradlew --no-daemon :image-worker:integrationTest "$@"