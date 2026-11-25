# Media contract

Pure Java types and ports for the media service. No Spring, JPA, Minio, or Jackson. The JSON shape
of these types is defined by the mixins in `media-json`.

The Minio object-storage adapter implements the `ObjectStore` port behind an `implementation`
dependency, so its Minio argument types stay behind the port and never leak into application
services.

The domain types are immutable (records or Lombok `@Value`) and `@NullMarked`: upload, attempt,
asset, variant, and transformation spec, plus the worker command and result. No JPA entities, Minio
argument classes, or Spring repositories.

Object keys: the asset id is the key prefix, `.` becomes `_` in keys, and `ObjectPrefix.resolve` is
the single key policy the API and the workers share.
