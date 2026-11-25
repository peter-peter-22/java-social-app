# Media service

This tree is the media service: it registers named transformations, accepts image uploads, transforms them through a worker, and serves the originals and named variants.

## Design

Two deployables.

- `media-api` owns the registry and the upload lifecycle. It runs the registry's migrations and finalizes each upload.
- `image-worker` only transforms files and moves them through object storage. No database and no migrations.
- `media-registry` implements the registry ports with JPA and Liquibase.
- `media-client` is the client SDK: two REST facades (`MediaAdminClient`, `MediaClient`) over the shared media JSON shape. The end-to-end test drives the stack through it. The TypeScript browser client in `../typescript` is deprecated and will be generated from the future OpenAPI definition instead (`../typescript/media-client/AGENTS.md`).
- Shared code is plain Java in `media-contract` and `media-json`, not a Spring service library. The JSON shape of the contract types is defined by the mixins in `media-json`; the domain carries no serialization framework.

A separate registry service is not the target: it would add a network hop without removing a file
copy, and the lifecycle transaction needs one writer. One shared database is acceptable here.

Blocking waits on the worker's HTTP call. Async uses the same file path plus a queue and a completion
call back to the API: the API publishes the same worker command to RabbitMQ and returns immediately,
and when the worker calls the API completion endpoint, the API runs the same finalize. The upload
response carries an `async` boolean, so the client knows whether the upload exists yet.

## Dependency direction

- `media-contract` depends on nothing. The domain types carry no serialization framework.
- `media-json` depends on the contract and Jackson 3.
- `media-registry` and the Minio adapter (`object-storage`) depend on the contract, not on each other. The registry depends on `media-json` for the jsonb shape.
- `media-api` depends on the contract, `media-json`, the registry, and the Minio adapter.
- `media-client` depends on the contract and `media-json` (plus the Spring RestClient); it never depends on the registry, the API, the worker, or the Minio adapter (its `ArchitectureTest` guards this).
- `image-worker` depends on the contract, `media-json`, and the Minio adapter. It must not depend on `media-registry`, `media-api`, or a shared Spring service module.
- Cockroach and Minio remain infrastructure, not services we write.

Package root: `com.example.media`. No underscores.

## Upload flow

Blocking and async share the same bytes path; only the control message changes.

0. The main application mints an upload permission (`POST /api/admin/upload-permission`) and hands the raw `up1_` token to the client. Only its SHA-256 hash is stored.
1. The API validates the request and the presented permission, reserves one use of it in the same transaction that starts the attempt, and uses the permission's upload key and visibility. It inserts the original asset in an uploading state and writes the file to object storage with the expiration tag. The asset id is the object prefix, so keys cannot collide. A request without a permission is rejected unless `media.upload-permission.required=false`; a failed attempt revokes what is left of the permission's uses.
2. The API resolves named transformations and sends the worker a command with the source object location, the result placement, and the resolved operations — no repository entities, and no reserved result asset ids.
3. The worker downloads the source, transforms it, allocates a fresh asset id and prefix for every output of that execution, and uploads the results with expiration tags. It returns metadata and the object list; it does not insert rows, change tags to permanent, or finalize the attempt.
4. The API removes the expiration tags (which fails when a kept object is missing), then commits one transaction: assets become ready, the upload and variant rows are inserted, and the attempt becomes successful. If that transaction fails, the objects expire and the existing cleanup rules apply.
5. Blocking waits for the worker HTTP call, then runs step 4 before responding. Async publishes the same command and runs step 4 when the worker calls the API completion endpoint.

Failure policy (`planning/uploads/upload-lifecycle.md` has the lifecycle rules): retry recoverable
worker failures, and mark the attempt failed when retries are exhausted or the error is
unrecoverable. Fresh result asset ids per execution are what make retries safe for both blocking and
async delivery. Completion is idempotent on `attemptId`. The attempt has no processing status; a
dead-lettered command marks the attempt failed.

## Reads, visibility, and the CDN edge

An upload is `PUBLIC` or `PRIVATE`; the visibility is chosen by the upload permission and stored on
the attempt and the finalized upload. Read URLs carry the upload's cache version
(`upload.createdAt.toEpochMilli()` as a decimal string, `v`): a public read ignores it and serves
anyone, and a private read requires it together with the signed download token. A replaced upload
gets a new `createdAt`, so its URLs change and caches miss.

The private-read token is stateless: base64url HMAC-SHA256 over `"m1\nread\n" + keyId + "\n" +
uploadKey + "\n" + version + "\n" + expires` (`DownloadTokenService` issues and verifies it; the
admin endpoint is `POST /api/admin/download-permission`). The version is signed, so a token for one
upload version cannot read another. Media responses stay cacheable — `public, max-age=31536000,
immutable` for public reads and `private, max-age=300` for private reads — and only the credential
issuance responses are `Cache-Control: no-store`.

The deployment's minimal nginx edge verifies the same signature at `/cdn/` before proxying, so the
token contract is exercised at the edge; the API remains authoritative for the visibility, the
current version, and the expiry (the edge has no database). Details, including the log-redaction
rule: `deployment/AGENTS.md`.

## Tests

`EndToEndTest` in `media-api` is the behavior lock of the whole path. It boots the API in-process
and drives the compose stack of `deployment` through the Java client (`media-client`) over HTTP
only. One command runs it: `./gradlew :media-api:integrationTest` — the task builds the image
worker's jar and the dev images, and the test boots the compose file with Testcontainers. The CDN
journeys use a plain `RestClient` against the edge, because the client speaks the web API and
`/cdn/` is the edge's URL space. Do not fold the compose-backed test into the normal unit-test run.

The transformation output is locked by `ImageProcessorTest` in the `image-worker`'s
`integrationTest` suite (real libvips, run through `deployment/test-transformations.bash`).

## Future work (planned, not built)

Leave room for, but do not build yet: video worker, periodic cleanup, on-demand transformations,
completion webhooks, and the full CDN (edge caching and direct Minio proxying). A video worker is
another deployable with the same command shape, not another shared Spring module.
