#!/bin/bash
# Starts as root so a new Docker volume can be made writable, then runs the
# given command as the host user. Gradle must not stay root: it writes build
# outputs and verification images into the bind-mounted project.

# Set strict error handling
set -euo pipefail

# Check if the variables are unset or empty
: "${HOST_UID:?HOST_UID is required}"
: "${HOST_GID:?HOST_GID is required}"

# Create the gradle home (this directory should be mounted to a volume)
mkdir -p "${GRADLE_USER_HOME:-/gradle-home}"
# Set the ownership of the gradle home to the user
chown "${HOST_UID}:${HOST_GID}" "${GRADLE_USER_HOME:-/gradle-home}"

# Set the permissions of the currently selected user
exec setpriv \
  --reuid="${HOST_UID}" \
  --regid="${HOST_GID}" \
  --clear-groups \
  --inh-caps=-all \
  "$@"
