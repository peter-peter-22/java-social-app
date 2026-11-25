#!/bin/bash
# Build the docker images the compose stack runs: the JDK + libvips worker image and the nginx edge
# that verifies the download tokens.

set -euo pipefail

cd "$(dirname "$0")"
docker build -t java-image-test -f media.Dockerfile .
docker build -t media-cdn -f nginx.Dockerfile .
