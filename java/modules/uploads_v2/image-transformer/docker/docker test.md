# Docker test

The `integrationTests` gradle test can run only in docker because it requires libvips.
This directory contains a `build.bash` script for building the required docker image, and a `run.bash` script
for running the test in a container.

## Usage
After building, the test should pass when the run script is used.