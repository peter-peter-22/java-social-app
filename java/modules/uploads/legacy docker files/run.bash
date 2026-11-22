#!/bin/bash
# Mount the project and run the image-transformer integration tests.
# The container starts as root only long enough for the entrypoint to make the
# Gradle volume writable. Gradle itself runs as the host user, so build outputs
# and verification images are not root-owned. GRADLE_USER_HOME stays on the
# volume, so the wrapper distribution and dependencies survive restarts.

docker run --rm -it \
  -e HOST_UID="$(id -u)" \
  -e HOST_GID="$(id -g)" \
  -e GRADLE_USER_HOME=/gradle-home \
  -v "$PWD/../../../..":/workspace \
  -v java-image-test-gradle:/gradle-home \
  -w /workspace \
  java-image-test \
  ./gradlew --no-daemon :image-transformer:integrationTest
