# Docker end-to-end test

The media service application is can be tested with docker compose.
First, the containers must be started, then the `integrationTest` gradle test of the media-api module must be run.
This task will communicate with the real media workers and other services to test the complete behavior.

## How to run

This directory contains a `build.bash` script for building the required docker image, and a `run.bash` script
for running the containers with docker compose.

- Run `build.bash` to build the docker image (if not build yet)
- Run `run.bash` to start the containers.
- Run `gradle :media-api:integrationTest` to run the integration tests.

## Future

The video and async transformations will need to be tested as well when they will be implemented.

The way these tests are run is not based on known standards, there might be a better way to do it.