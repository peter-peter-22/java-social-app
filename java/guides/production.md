# Changes for production

## Object storage:
The object storage is not configured for multi region usage.
The upload entry in the database will possibly need to store
the source region.

## CDN
Using CDN for the object storage.

# Useful optional optimizations

## Internal communication
Replace json with protocol buffer for faster serialization in the internal communication.

Replace http with grpc for faster communication.

## Changelogs
Separate database migrations, minio reconciliations and upload transformation presets from
the api module of these services to prevent unnecessary imported data.

## Webflux
Use webflux instead of spring webmvc for IO bound servers.

## Accelerated tmp folder

The tmp folder of the video-transformer is frequently used. Storing this folder in the memory would avoid unnecessary
and slow disk usage. On ubuntu, the tmpfs should be used in the tmp folder by default. Requires verification.

## Small optimizations

Some minor optimizations are marked with "optimization:" in the comments.

## GPU accelerated image transformer

The image transformer is CPU-only, but Cloudinary claims to have GPU accelerated image transformations.

##  

# Update packages
Some are outdated and insecure.