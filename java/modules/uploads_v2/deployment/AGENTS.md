# Compose deployment for the media service
This directory contains a docker compose file and some bash scripts for starting the image worker, the database, and the object storage.

Starting the media API in addition makes the whole network work together, making the end-to-end test runnable.

## Usage
- Build the JDK + libvips development image that runs the worker (the `media.Dockerfile` with its `build.bash`; both are being migrated into this tree with the docker tests).
- Build the media API and image worker jars and start the compose file with the `run.bash` in this directory.
- Start the `media-api` application or run the end-to-end integration test in it (`EndToEndTest` in `media-api`).