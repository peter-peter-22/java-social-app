# Required changes for production

## Object storage:
The object storage is not configured for multi region usage.
The upload entry in the database will possibly need to store
the source region.

# Useful optimizations

## CDN
Using CDN to the object storage.

## Internal communication
Replace json with protocol buffer for faster serialization in the internal communication.

Replace http with grpc for faster communication.

## Changelogs
Separate database migrations, minio reconciliations and upload transformation presets from
the api module of these services to prevent unnecessary imported data.