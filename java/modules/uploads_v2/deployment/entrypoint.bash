#!/bin/bash
# Starts as root so a named Gradle volume can be made writable, then runs the
# given command as the host user. Gradle must not stay root: it writes build
# outputs into the bind-mounted project, and root-owned files block IDE builds.

set -euo pipefail

: "${HOST_UID:?HOST_UID is required}"
: "${HOST_GID:?HOST_GID is required}"

gradle_home="${GRADLE_USER_HOME:-/gradle-home}"
mkdir -p "${gradle_home}"

# Docker creates a new volume as root:root. chown only when that is still true,
# including a cache previously filled by a root container. Later starts skip the walk.
if [[ "$(stat -c '%u' "${gradle_home}")" != "${HOST_UID}" || "$(stat -c '%g' "${gradle_home}")" != "${HOST_GID}" ]]; then
  chown -R "${HOST_UID}:${HOST_GID}" "${gradle_home}"
fi

# The image default HOME is /root, which is not writable after the privilege drop.
home_dir=/home/dev
mkdir -p "${home_dir}"
chown "${HOST_UID}:${HOST_GID}" "${home_dir}"
export HOME="${home_dir}"

exec setpriv \
  --reuid="${HOST_UID}" \
  --regid="${HOST_GID}" \
  --clear-groups \
  --inh-caps=-all \
  "$@"
