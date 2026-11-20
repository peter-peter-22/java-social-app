# Media contract

Pure Java types and ports for the media service. No Spring, JPA, or Minio.

The prototype code must not use these types until a later slice switches it over.
The Minio adapter in `uploads_v2/object-storage` implements the `ObjectStore` port behind an
`implementation` dependency, so no prototype code can compile against the contract.
