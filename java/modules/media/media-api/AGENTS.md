# Media API

Spring Boot application that owns upload and read requests, the registry, and the upload lifecycle. It runs the registry's migrations and finalizes each upload (blocking on the image worker).

The blocking worker adapter retries recoverable transport and worker 5xx failures. Every worker execution allocates fresh result asset ids, so a retry writes to new object keys; the API registers the reported assets at completion.

The application scans `com.example.media.api`. The driven adapters — the JPA registry and the Minio object store — are wired in `MediaPersistenceConfiguration`, which scans `com.example.media.registry` and `com.example.object_storage` and enables the registry's JPA repositories. Keep the scans narrow; do not blanket-scan `com.example`.
