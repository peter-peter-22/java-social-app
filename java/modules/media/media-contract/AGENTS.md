# Media contract

Pure Java types and ports for the media service. No Spring, JPA, or Minio.

The Minio object-storage adapter implements the `ObjectStore` port behind an `implementation`
dependency, so its Minio argument types stay behind the port and never leak into application
services.
