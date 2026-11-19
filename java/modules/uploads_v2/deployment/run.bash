#!/bin/bash
# Compose cannot call `id` itself. Export the host ids, then forward to
# docker compose so Gradle drops to this user (see entrypoint.bash).
# Usage: ./run.bash up
#        ./run.bash logs -f upload-service

set -euo pipefail

cd "$(dirname "$0")"
cd ../../../
echo "work directory:"
pwd

./gradlew --no-daemon :uploads-service:bootJar :image-transformer:bootJar

cd "$(dirname "$0")"

# TODO investigate what this means
if [[ $# -eq 0 ]]; then
  set -- up
fi

exec docker compose "$@"
