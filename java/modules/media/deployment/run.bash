#!/bin/bash
# Compose cannot call `id` itself. Export the host ids, then forward to
# docker compose so the container process runs as this user (see entrypoint.bash).
# Usage: ./run.bash up
#        ./run.bash logs -f image-worker

set -euo pipefail

export HOST_UID="$(id -u)"
export HOST_GID="$(id -g)"

cd "$(dirname "$0")"
cd ../../../
echo "work directory:"
pwd

./gradlew --no-daemon :media-api:bootJar :image-worker:bootJar

cd "$(dirname "$0")"

# If the number of the parameters provided for the script is 0, default to "up", to make the script directly runnable.
# This will result in running "docker compose up" when the script is started without arguments.
if [[ $# -eq 0 ]]; then
  set -- up
fi

exec docker compose "$@"
