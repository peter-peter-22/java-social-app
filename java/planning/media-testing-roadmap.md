# Media testing roadmap

The plan: revise the test architecture of the media cloud before the clients are built, so the
clients are locked by tests instead of changing them. History of the decision; the binding rules it
produces live in the root `AGENTS.md` (Conventions).

## Problems this plan fixes

1. `EndToEndTest` (media-api `integrationTest`) is grey-box: it registers transformations through the
   in-process `TransformationCatalog` bean and reads async attempt status through the `UploadRegistry`
   bean, so it locks internal wiring, not the public contract. It cannot be re-hosted on the planned
   clients as-is.
2. The end-to-end orchestration is a manual runbook (build image → build jars → `run.bash` →
   `./gradlew :media-api:integrationTest`): stateful, order-sensitive, not one command, not CI-ready.
3. The transformation output is tested nowhere: `TransformImageTest` mocks the libvips wrapper, and
   the removed docker test (transform everything into a mounted dir and eyeball) has no automated
   replacement. The end-to-end reads assert only the content type.

## Target state

- **Transformation output lock** (`image-worker` `integrationTest`): real-libvips tests against
  generated fixture images, asserting structure — dimensions of crop (FILL) vs pad (CONTAIN),
  resolution limits, encoding round-trips (magic bytes, JPEG quality effect), metadata — not
  byte-exact goldens (libvips version drift). The suite writes every output under
  `build/reports/transformations/` for visual inspection, reviving the removed docker test's purpose.
  A `ImageProcessing` seam replaces the Mockito mock of the concrete libvips class in
  `TransformImageTest`.
- **Contract lock** (`media-api` MockMvc tests): exact JSON shapes and status codes of every endpoint
  the clients bind to, including the new admin and status endpoints, tested before the clients are
  written. `media-json` stays the single home of the wire shape.
- **Admin & status HTTP endpoints** (`media-api`): named-transformation registration and queued-upload
  status over HTTP (the `UploadAttemptStatus` TODO). Minimal admin key auth; signatures and upload
  permissions stay future work.
- **Java client module** (`media-client`): `RestClient`-based, two facades — `MediaAdminClient`
  (transformations, later signatures/admin upload) and `MediaClient` (upload/read/status) — tested
  with `MockRestServiceServer`. This is the Java SDK and the E2E driver.
- **Black-box end-to-end**: the same three journeys (blocking upload+read, async upload+status+read,
  corrupt source → FAILED) driven only through `media-client` over HTTP. Orchestration moves to
  Testcontainers `ComposeContainer` in local-compose mode on `deployment/compose.yaml` (single source
  of truth); `:media-api:integrationTest` builds the worker jar and the dev image first — one command.
  Reads decode the returned image and assert dimensions, closing the output gap at journey level.
- **TypeScript browser client** (`../typescript`): fetch-based upload/read client with unit tests and
  a smoke journey against the same stack.

## Order (dependency-driven)

1. Transformation output lock — no prerequisites, biggest coverage hole.
2. Admin + status endpoints — blocks the Java client and the black-box E2E.
3. `media-client` — blocks the E2E rework.
4. E2E rework in one pass (orchestration + black-box driver) — replaces `EndToEndTest`; its scenarios
   are preserved, its implementation is not. Until this step the existing suite stays the behavior
   lock and keeps passing.
5. TS client + smoke test.
6. Later, not this plan: upload signatures and permissions (extend the E2E with auth journeys then),
   media-api containerization for deployment parity, video worker, periodic cleanup.

## Status

Steps 1–5 are implemented and validated. The binding rules they produced are recorded in the root
`AGENTS.md` section Conventions (Testing); the runbooks live in `modules/media/deployment`.