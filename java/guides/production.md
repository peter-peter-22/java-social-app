# Required changes for production

## Object storage:
The object storage is not configured for multi region usage.
The upload entry in the database will possibly need to store
the source region.

## CDN
Using CDN for the object storage.

# Useful optimizations

## Internal communication
Replace json with protocol buffer for faster serialization in the internal communication.

Replace http with grpc for faster communication.

## Changelogs
Separate database migrations, minio reconciliations and upload transformation presets from
the api module of these services to prevent unnecessary imported data.

## Webflux
Use webflux instead of spring webmvc for IO bound servers.

## Micro optimizations
Some minor optimizations are marked with "optimization:" in the javadoc.