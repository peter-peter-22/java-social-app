# Compose deployment for the media service
This directory contains a docker compose file and some bash scripts for starting the image worker, the database, and the object storage.

Starting the media API in addition makes the whole network work together, making the end-to-end test runnable.

## Usage
- Build the `media.Dockerfile` with the `build.bash` script (found in the image-transformer module).
- Build the media API and image worker jars and start the compose file with the `run.bash` in this directory.
- Start the `media-api` application or run the end-to-end integration test in it (`EndToEndTest` in `media-api`).

## More info
In the `image-transformer` module, more info is available about the `media.Dockerfile` and another docker test.