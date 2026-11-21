# Clean media service

The prototype in `modules/uploads_v2` stays until the new path passes the same end-to-end test. Do not clean it up in place.

## Status

- [x] Record this plan and add empty `media-contract`, `media-api`, and `image-worker` modules.
- [x] Move domain types and ports into `media-contract`.
- [x] Move the registry adapter.
- [x] Hide Minio behind an `ObjectStore` port.
- [ ] Build the API use cases and the slim worker.
- [ ] Point Compose and `EndToEndTest` at the new jars, then remove unused `uploads_v2` modules.
- [ ] Add async on the same file path.
- [ ] Conventions pass: names, Gradle, ArchUnit.

## Decision

Two deployables.

- The media API owns the registry and the upload lifecycle.
- Image workers only transform files and move them through object storage.
- Blocking waits on HTTP.
- Async uses the same file path, a queue, and a completion call back to the API.

A separate registry service is not the target. It would add a network hop without removing a file copy, and the lifecycle transaction needs one writer. Keeping the shared database and letting the worker finalize is the prototype's problem, not the cleanup.

## What is wrong now

The happy path works. The boundaries do not.

- `uploads-service` accepts the upload, writes the original, and calls the worker. `image-transformer` then downloads, transforms, writes objects, inserts assets, removes expiration, and finalizes the upload. Both processes write the same lifecycle.
- `upload-services` is a shared Spring library used by both apps. It pulls Minio and the registry into the worker.
- `uploads-api` mixes HTTP DTOs, domain models, and repository ports. `ImageProcessingTask` carries a persisted `StoredAsset` instead of a file location and resolved operations.
- Gradle names do not match directories. `:upload-api` is `uploads-api`. `uploads-service` and `upload-services` differ by one letter. Packages use underscores, such as `com.example.uploads_service`.
- The worker's `application.yaml` is a copy of the API config, including Liquibase. The API has Liquibase disabled, so the worker currently owns migrations.
- Read errors are caught and returned as 404. Async methods are empty. Cleanup is specified but not implemented.

`EndToEndTest` is the behavior lock: register `optimize` and `profile`, upload an image with an incoming and an eager transformation, then read the original as WebP and the named variant.

## Target modules

```text
modules/media/
  media-contract/     pure Java. No Spring, JPA, or Minio.
  media-registry/     JPA + Liquibase adapter. Implements contract ports.
  media-api/          Spring Boot app. HTTP in, registry and object storage out.
  image-worker/       Spring Boot app. HTTP in, libvips, object storage out. No database.
  deployment/         Compose for Cockroach, Minio, and the worker.
```

The existing `modules/uploads_v2/object-storage` module stays as the Minio adapter. Its Minio argument types must not leak into application services.

## Dependency direction

- `media-contract` depends on nothing but Jackson annotations, which define the JSON shape of the transformation specs and worker messages.
- `media-registry` and the Minio adapter depend on the contract, not on each other.
- `media-api` depends on the contract, the registry, and the Minio adapter.
- `image-worker` depends on the contract and the Minio adapter. It must not depend on `media-registry`, `media-api`, or a shared Spring service module.
- Cockroach and Minio remain infrastructure, not services we write.

Inside `media-api`:

- Driving adapters: client upload and read, worker completion, admin named transformations.
- Application services: one use case per action.
- Domain: upload, attempt, asset, variant, transformation spec.
- Driven ports: `UploadRegistry`, `AssetStore` (asset rows and metadata), `TransformationCatalog`, `ObjectStore`, `ImageTransformer`.

ArchUnit is a guard after the move, not a new architecture. The contract cannot import Spring, JPA, or Minio. The worker cannot import the registry.

Package root: `com.example.media`. No underscores.

## File and control flow

Blocking and async use the same bytes path. Only the control message changes.

1. The API validates the request and starts an attempt. It inserts the original asset in an uploading state and writes the file to object storage with the expiration tag. The asset id is the object prefix, so keys cannot collide.
2. The API resolves named transformations and reserves result asset ids. It sends the worker a command containing the source object location, the reserved prefixes, and the resolved operations. It does not send repository entities.
3. The worker downloads the source, transforms it, and uploads the results under those prefixes with expiration tags. It returns metadata and the object list. It does not insert rows, change tags to permanent, or finalize the attempt.
4. The API removes the expiration tags, checks that the objects exist, and then commits one transaction: assets become ready, the upload and variant rows are inserted, and the attempt becomes successful. If that transaction fails, the objects expire and the existing cleanup rules apply.
5. Blocking waits for the worker HTTP call, then runs step 4 before responding.
6. Async publishes the same command after the attempt is marked processing, returns immediately, and runs step 4 when the worker calls the API completion endpoint. Completion is idempotent on `attemptId`.

Do not add a broker during the structural move. `ImageTransformer` has `transform` for the blocking adapter. `enqueue` is added later. Async should use a normal broker plus a transactional outbox so the database state and the queued command cannot diverge.

Failure policy, already stated in `upload-lifecycle.md`: retry recoverable worker failures, and mark the attempt failed when retries are exhausted or the error is unrecoverable.



## What stays and what goes

Keep:

- The lifecycle rules in `planning/uploads/upload-lifecycle.md`.
- The current tables and Liquibase changelogs. Do not redesign the schema while moving code.
- Cockroach, Minio expiration tags, named transformations, and the incoming-versus-eager distinction.
- The object key rules: the asset id is the prefix, `.` becomes `_` in keys, and `ObjectPrefix.resolve` is the single policy the API and the workers share.
- The JSON shape of the transformation specs and worker messages. The `media-contract` tests are the lock; the only deliberate change is the video discriminator, fixed from `webp` to `video` (no video rows exist to migrate).
- Testcontainers fixtures for registry and object-storage tests.
- The slow infrastructure checks in `planning/roadmap.md` as a separate suite, not part of the normal build.

Remove only after the new path passes `EndToEndTest`:

- The `upload-services` module.
- The worker's dependency on the registry and its Liquibase config.
- Repository interfaces living next to HTTP DTOs.
- The catch-all that turns server errors into 404.
- `System.out.println` and generic names such as `ClientController`, `TaskService`, and `FileUtils`.

Leave room for, but do not build yet: video worker, periodic cleanup, upload permissions, private reads, on-demand transformations, and completion webhooks. A video worker is another deployable with the same command shape, not another shared Spring module.

## Steps

Each step must leave the current end-to-end path runnable. Do not change behavior and move code in the same step.

1. Freeze the prototype. This plan is the record of the current path and the known gaps.
2. Add empty modules from `templates/`. `media-contract` is a plain Java library. `media-api` and `image-worker` are Spring Boot apps that boot and do nothing else. Gradle project names match their directories.
3. Put immutable domain types and ports in `media-contract`. Use records or Lombok `@Value`, and `@NullMarked`. Include upload, attempt, asset, variant, transformation spec, and the worker command and result. No JPA entities, Minio argument classes, or Spring repositories.
4. Move the registry adapter into `media-registry`. Move the existing repository integration tests with it. The application service, not the repository, owns the finalize transaction boundary.
5. Hide Minio behind `ObjectStore`. The adapter keeps regions, tags, and reconciliation.
6. Build the API use cases: start image upload, complete image upload, read original, and read variant. The controller only maps HTTP. A missing upload is 404. Bad input is 400. An unexpected failure is 500.
7. Slim the worker to download, transform, upload under the given prefixes, and return the result. Remove the database, Liquibase, and `upload-services` dependencies. Keep libvips and the native-access flag inside this deployable.
8. Point Compose and `EndToEndTest` at the new jars. The API runs Liquibase. The worker does not. Only after this test passes, remove the unused `uploads_v2` modules.
9. Add the async port without changing the file path. The completion endpoint calls the same finalize use case. Retries must be safe.
10. Conventions pass: `buildSrc` conventions instead of copied plugin versions, ArchUnit checks, and no duplicated `application.yaml`. Now that no prototype code shares the Minio adapter, rework its tag types: one non-null tag representation instead of `ObjectTag` and the nullable tag arguments, and pinned replace-or-merge semantics for `setTags`. The expiration tags and the lifecycle rule stay.

Verification for every step is the existing registry and object-storage integration tests, plus `EndToEndTest` once the new API is wired. Do not fold the Compose-backed test into the normal unit-test run.

## Next slice

Step 7. Slim the worker to download, transform, upload under the given prefixes, and return the result. Remove the database, Liquibase, and `upload-services` dependencies. Keep libvips and the native-access flag inside this deployable.

Step 6 is done: `media-api` has the start-image-upload, complete-image-upload, read-original, and read-variant use cases with controllers that only map HTTP. The blocking worker call goes through the `ImageTransformer` port; the HTTP client adapter lands with the worker's endpoint in the next steps, and the Spring wiring of the adapters lands with step 8.

