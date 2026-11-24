#!/bin/bash
# Build the docker image used by `run.bash`

set -euo pipefail

cd "$(dirname "$0")"
docker build -t java-image-test -f media.Dockerfile .
