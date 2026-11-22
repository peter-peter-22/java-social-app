# Media service

This tree is the media service: it registers named transformations, accepts image uploads, transforms them through a worker, and serves the originals and named variants.

The plan is `planning/uploads/clean-media-service.md`.

- `media-api` owns the registry and the upload lifecycle. It runs the registry's migrations and finalizes each upload.
- `image-worker` only transforms files and moves them through object storage. No database and no migrations.
- `media-registry` implements the registry ports with JPA and Liquibase.
- Shared code is plain Java in `media-contract` and `media-json`, not a Spring service library. The JSON shape of the contract types is defined by the mixins in `media-json`; the domain carries no serialization framework.
