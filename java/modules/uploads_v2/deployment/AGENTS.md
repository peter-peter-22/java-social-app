# Compose deployment for the upload service
This directory contains a docker compose file and some bash scripts for starting the image worker, the database, and the object storage.

Starting the upload service in addition makes the whole network to work together, making the end-to-end test runnable.

## Usage
- Build the `media.Dockerfile` with the `build.bash` script (found in the image-transformer module).
- Build the image worker jar and start the compose file with the `run.bash` in this directory.
- Start the uploads-service application or run the integration test in it.

## More info
In the `image transformer` module, more info is available about the `media.Dockerfile` and another docker test.