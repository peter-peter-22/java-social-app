#!/bin/bash

# Starts as root so the Gradle volume can be made writable for the host user,
# then drops to HOST_UID:HOST_GID before running the container command.
# The debug output is the only other effect. Launch through run.bash so the ids
# match the host user (see "bash strict error handling manual.md").

# Set strict error handling
set -euo pipefail

# Check if the variables are unset or empty
: "${HOST_UID:?HOST_UID is required}"
: "${HOST_GID:?HOST_GID is required}"

echo "entrypoint workdir:"
pwd
echo "workdir contents:"
ls
echo "available jar files:"
find "./" -type f -name "*.jar"

# Create the gradle home (this directory should be mounted to a volume)
mkdir -p "${GRADLE_USER_HOME:-/gradle-home}"
# Set the ownership of the gradle home to the user
chown "${HOST_UID}:${HOST_GID}" "${GRADLE_USER_HOME:-/gradle-home}"

# Drop the root privileges and run the container command as the host user
exec setpriv \
  --reuid="${HOST_UID}" \
  --regid="${HOST_GID}" \
  --clear-groups \
  --inh-caps=-all \
  "$@"
