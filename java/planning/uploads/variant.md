# Upload variants plan (RESOLVED)

This file has been updated with the concrete implementation decisions from the plan in session/plan.md.
The undecided questions (storage, CRUD, atomicity for eager uploads, idempotency for retries, locking) are now decided and implemented.

## Resolved Storage & Identity
- **Variant identity**: Composite PK `(upload_id, named_transformation_id)` on `VariantEntity` + `VariantEntityId`. Exactly matches "upload key + transformation name".
- **Uniqueness/overwrite**: Enforced by PK. `save(InsertVariant)` on existing PK overwrites previous variant + linked `StoredAssetEntity`.
- **Data**: `VariantEntity` links to exact `TransformationVersionEntity` (for migration tracking; unaffected by named transformation current_version changes), `StoredAssetEntity` (supports directory + entryPoint for HLS etc.), status (`PROCESSING`/`READY`), `createdAt`.
- **Object paths**: Deterministic via `GlobalObjectRepository.variantObjectKey(uploadKey, transformationName, versionHash, filename)`. Prefix for directories. Delete under DB row lock to prevent orphans (see `deleteVariantObjects` helper).

See `VariantEntity.java`, `VariantCrudRepository.java`, `GlobalObjectRepository.java`.

## Resolved CRUD & Access
- `VariantRepository` (extended): `save(InsertVariant)` (upsert), `updateStatus`, `findByUploadKeyAndTransformationName` (READY only, else 404-like), `findForUpdate` (pessimistic lock), `delete`.
- Queries in `VariantCrudRepository` use `@EntityGraph` for efficient joins, status filter, and lock support.
- Public reads return only `READY` variants.

See `uploads-api/variant/*` and `upload-registry/variant/VariantRepositoryImpl.java`.

## Resolved Eager Upload Sequence (Atomic + Idempotent)
The upload with eager transformations follows this flow (orchestrated by uploads-service, registry calls in scope):

1. Upload to **staging object** (`GlobalObjectRepository`).
2. **Single transaction**: Insert `UploadEntity` (PROCESSING) + `VariantEntity`(s) with status=PROCESSING (invisible).
3. Process transformations (sync/async):
   - `findForUpdate(variantId)` (pessimistic lock).
   - If already READY, skip (idempotent).
   - Transform, upload output to deterministic key via `GlobalObjectRepository.uploadAll` or `putObject`.
   - **Final transaction**: `updateStatus(READY)` + link final `StoredAssetEntity`. Update upload status when all complete.
4. All variants become visible **together** only on final commit → atomic.
5. On unrecoverable error: fail whole upload.
6. Retries: Restart from staging check; PK + locks + status guards ensure no duplicate/overwritten visible artifacts or concurrent runs. No separate pending table needed.

Manual/admin transformations and deletion follow the same upsert/final-READY + locked-delete pattern.

**Eager retries**: Fully idempotent. Previous transformation guarded by lock/status. Whole upload retriable while staging file exists.

See session plan.md for full sequence and `VariantRepositoryImpl`.

## Deletion & Migration
- Deletion: Lock variant row → `delete(id)` (cascades asset) → `GlobalObjectRepository.deleteVariantObjects(...)` (prefix for directories).
- Migration: Main app queries variants by stored `transformationVersionId` vs current; uses admin API to re-generate. No registry changes needed.

## Implementation Status
- All changes in allowed modules only.
- `VariantRepositoryImpl` added with `@Transactional`, `EntityManager` for locks, following existing `*RepositoryImpl` patterns.
- DB indexes added (`001-add-indexes.sql`).
- Helpers in `GlobalObjectRepository`.
- Tests to be extended (see todo in plan.md).
- The original undecided points are resolved without new tables or breaking existing stable entities.

The variants are now stable, atomic, and idempotent as required by the overall media plan.

## Original Content (for reference)
(Previous sections follow below...)

### Getting
One variant can be queried based on the upload key and transformation name those were used for its creation.

### Identity and uniqueness
Only one combination of the same upload key and transformation name can exist at once.
When a new variant for this combination is created, the previous one is overwritten/deleted.
The unique identity of a variant is upload key + transformation name.

### Transformation versions
When a named transformation changes parameters, changing the active transformation version of this name,
the created variants are not affected, and they continue to be visible.
The variants save which transformation version was used for their creation to track what happened
and how to apply migrations, but this is handled outside the media cloud.

### Eager transformations
The variants created by the eager transformations are processed together, and they are considered ready when all of them are complete
regardless of the synchronous or asynchronous processing.

### Eager transformation retries
When an eager transformation is retried due to some error, the process must ensure that the previous transformation is not running and won't accidentally overwrite
the new one.
Some sort of unique identity and progress tracking will be necessary for the eager transformation of the uploads.
A separate table that tracks the pending transformation and uploads will might be necessary.
A unique key to each attempt will might be necessary to check to make an asynchronous process to know
that it was supposed to be canceled and cannot write the result.

### Eager retry steps and requirements
An upload and it's eager transformations are retriable as long as the file is saved.
The file upload is saved into a temporary staging file soon after the upload process starts.
If the process is retried, it checks if the file exists. If not, the upload is failed, and it returns an error.
The retries don't have other retry points after the upload, the whole thing restarts so it's must be idempotent.

### Eager transformation unrecoverable error
When an error does not cause a retry, the whole upload is considered failed
because it can be ready only after all transformation are done.
The 

### Manual variant creation
Transformations can be started manually from the admin API.
When a transformation is requested from the admin API for an existing transformation name + upload key pair, the
variant is overwritten in the registry only when the whole thing is complete.
The access for this variant won't be blocked during the process.

## Practical use cases

### Upload request with eager transformations
A signed upload from the client can contain eager transformations.
These are processed after the upload and the upload are considered ready only after they are done.
These can be blocking or asynchronous.
This means that all variants will be readable when the upload is ready.

### Tracking
In an ideal case, the main application tracks the created signatures, and the upload completion webhook calls to
determined what uploads exist, and optionally to track the variants and their transformation versions.

### Reading
A variant can be read from the client by combining the upload key with the transformation name.
This returns the file(s) those were created by the active version of the given transformation name
during the upload.
Requesting a non-existent upload key + transformation name combination results in 404 error. 

### Variant formats
A variant can be a single file, e.g., an image, or a directory, e.g., a HLS file.
In the case of the directory, the reading URL contains the relative path in the directory.

The directory variants also register an entry-point. 
The entrypoint tells where the main file is in a directory (typically the master.m3u8 file of a HLS video)
The entrypoint might not be necessary if the client knows how to read a HLS directory.

### Deletion
When a variant is deleted or overwritten then all files are removed from the object storage.
In the case of a directory variant, the object storage API must first filter all existing objects with
the object key prefix of the directory.
To avoid race conditions, the processed upload key or variant identity must be somehow locked to avoid
accidental orphan objects.

### Migration
When a named transformation changes, the application will might want to update the existing variants.
The main application must track the transformation versions of the variants and use the admin API to request the newer versions.
There could be an endpoint in the admin API that provides a list of the variants those were made with the older transformation versions.

The full migration work could be in the upload service, but this would be more complex.
This would require to prevent the running migration work that is excepted to be long from overwhelming the upload service.