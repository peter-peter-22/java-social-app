# Clean media service

This tree is the clean media service. It is built beside `modules/uploads_v2`, not by editing that prototype.

The plan is `planning/uploads/clean-media-service.md`.

- `media-api` owns the registry and the upload lifecycle.
- `image-worker` only transforms files and moves them through object storage.
- `media-registry` implements the registry ports with JPA and Liquibase. The prototype keeps its own copy of the adapter until the new path passes the end-to-end test.
- Shared code is plain Java in `media-contract`, not a Spring service library.
