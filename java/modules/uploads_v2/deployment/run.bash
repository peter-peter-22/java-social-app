#!/bin/bash
# Compose cannot call `id` itself. Export the host ids, then forward to
# docker compose so Gradle drops to this user (see entrypoint.bash).
# Usage: ./run.bash up
#        ./run.bash logs -f upload-service

set -euo pipefail

cd "$(dirname "$0")"
export HOST_UID="$(id -u)"
export HOST_GID="$(id -g)"

if [[ $# -eq 0 ]]; then
  set -- up
fi

exec docker compose "$@"
