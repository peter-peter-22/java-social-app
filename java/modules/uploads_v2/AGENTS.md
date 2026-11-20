# The upload modules

## Overview

These modules handle the behavior of the file uploads.

## Projects
- Uploads service: takes upload and read requests from the client.
- Image worker: transforms images and saves the results.
- Video worker (future): transforms videos and saves the results.

## Do not clean this tree in place
The clean service is being built under `modules/media`. This prototype stays until its end-to-end test passes there. See `planning/uploads/clean-media-service.md`.