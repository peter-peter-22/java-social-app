# Media registry

JPA and Liquibase adapter for the registry ports of `media-contract`: `UploadRegistry`, `AssetStore`, and `TransformationCatalog`.

The tables and migrations are the registry's schema; do not redesign them here.

Every adapter method is one operation that joins the caller's transaction. The finalize transaction boundary belongs to the application service in `media-api`, not to this module: there is no composite finalize operation here.
