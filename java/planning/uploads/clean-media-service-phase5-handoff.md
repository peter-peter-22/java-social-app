# Clean media service — cleanup plan handoff (phases 1–5 done)

> Outcome: Phase 5 (step 10) was completed in the follow-up session. All remaining tasks
> below were executed, the unit and integration suites are green, and the step is ticked in
> `clean-media-service.md`. The document below is kept as the record of the session's verified
> findings.

This is a resumption handoff for the 5-phase clean-code plan for `modules/media`
(excluding `media-contract` production code). Phases 1–4 are committed and green.
Phase 5 (= step 10 of `clean-media-service.md`) was in progress: state gathering is
complete (all facts below were re-verified against the working tree), but **no Phase 5
edits have been made**. The working tree is clean at commit `c35bd59`.

## Final modified files of the session (all committed)

Git state: branch `master`, HEAD `c35bd59`, `git status` clean (only IDE metadata
outside the repo). `git show --stat <hash>` gives exact file lists.

- `2f74620` Phase 1: remove dead code and sync documentation with the code
  - `modules/media/object-storage/**` (dead code: `GlobalObjectRepositoryTests`,
    `getHomeRegion`, `findWithReferencesByUploadKey`; TODO→comment fixes;
    `MinioConfig.homeRegion` later removed in 3b)
  - doc sync: `modules/media/object-storage/AGENTS.md`, `guides/gradle/modules.md`,
    `modules/media/deployment/docker test.md`
  - deployment: `modules/media/deployment/{bash manual.md,build.bash,run.bash,
    entrypoint.bash,compose.yaml,media.Dockerfile}` — strict mode, HOST_UID/GID
    checks, chown + `setpriv` drop, `gradle-cache:/gradle-home` mount
- `86bfd9c` Phase 2: trim tests to what each layer owns
  - `HttpImageTransformerTest`, `ImageTransformControllerTest` (dropped jsonPath
    shape duplicates), `MediaJsonFormatMapperTest` (round-trips only),
    `CockroachIntegrationTest`, `MinioIntegrationTest` (dropped `testConnection`/
    `contextLoads`), `media-api` `ApplicationTest` (owns `contextLoads`)
- `0dfb1f2` Phase 3a: use `RetryTemplate.invoke()` and share the `StoredFile` mapping
  - `HttpImageTransformer`, `ApiCompletionClient`, `StoredFile.open(asset, objectStore)`
- `8a36b88` Phase 3b: collapse the object-storage stack into `MinioObjectStore` (−364 lines)
  - `MinioObjectStore` now at `com.example.object_storage` translating the
    `ObjectStore` port directly to Minio; deleted `GlobalObjectRepository`,
    `RegionalObjectRepositories`, `ObjectStorageRepository(Impl)`, 9 `regional.args`
    classes, `RegionalObjectRepositoriesIT` (merged into `MinioObjectStoreIT`),
    `MinioConfig.homeRegion`; dropped unused `webmvc-test` dep
- `c35bd59` Phase 4: one REST client and one queue-name home
  - new `media-contract` `com.example.media.contract.worker.MediaQueue` constants
    (local copies deleted from both deployables); `EndToEndTest` `RestTemplate`→
    `RestClient` (catch-all no-op `defaultStatusHandler` keeps 4xx assertable);
    **bug fixed**: `run.bash` relative `cd` made compose use stale `java/compose.yaml`
    (deleted — it collided on `container_name`), now anchored on `DEPLOYMENT_DIR`
  - `EndToEndTest` live: 4/4 green (blocking, async, corrupt-source, context); worker
    PID 1 runs as uid 1000, `/gradle-home` chowned

## Phase 5 verified findings (this session — do not re-derive)

1. **A4 scope correction**: `test-fixtures-conventions` is applied by **no** build
   file. The earlier "used by object-storage & cockroach-db" note misread the built-in
   `` `java-test-fixtures` `` plugin accessor (which those two build files do use) as
   the convention. `templates/` only reference `java-library`/`spring-library`/
   `spring-project` conventions. Therefore A4 deletes **all three**:
   `web-library-conventions`, `web-project-conventions`, `test-fixtures-conventions`,
   plus the openapi-generator + springdoc pins in `buildSrc/build.gradle.kts`.
   Also update `guides/gradle/copying dependencies.md` (its "(Implemented in the
   test-fixtures-conventions gradle convention)" line becomes stale) and note
   `modules/uploads/video-transformer-legacy/build.gradle.kts` (out of build) still
   references `web-project-conventions` — legacy, fine to leave broken/out-of-build.
   `mockwebserver3` pin lives in `web-library-conventions` (dies with it).
2. **A5 defect confirmed via dependency reports** (`./gradlew :X:dependencies
   --configuration testCompileClasspath`, needs >30s timeout — see gotchas):
   - `:media-contract` resolves purely from java-conventions pins: junit-jupiter
     5.11.3, assertj 3.27.7, mockito-core 5.23.0, lombok 1.18.46.
   - `:media-api` (Spring, BOM-managed) shows the two-source conflict:
     `junit-jupiter:6.0.3 -> 5.11.3` (forced-down conflict arrow),
     `mockito-core:5.20.0 -> 5.23.0`, and `mockito-junit-jupiter:5.20.0` — a
     **mixed Mockito family** (core 5.23.0 + junit-jupiter 5.20.0). The
     `mockitoAgent` config pins `mockito-core:5.23.0` independently again.
   - Fix direction (approved): single-source the test-lib versions; let the BOM own
     Spring-module test libs, keep explicit pins only where no BOM applies (or
     populate `gradle/libs.versions.toml`); **keep mockito agent and core on one
     version** and align `mockito-junit-jupiter` with them.
3. **A6**: `settings.gradle.kts` has a dead
   `pluginManagement { plugins { kotlin("jvm") version "2.2.20" } }` block — delete.
4. **D6**: (a) `modules/media/media-registry/build.gradle.kts` line 3 has inline
   `id("org.hibernate.orm") version "7.2.19.Final"` — move to buildSrc conventions
   (plugin pin in `buildSrc/build.gradle.kts`, applied by id in the module).
   (b) `application.yaml` dedup: the `minio:` block is **verbatim identical** in
   `media-api` and `image-worker`; rabbit uses `${SPRING_RABBITMQ_HOST:localhost}`
   style while `datasource`/`minio` hardcode `localhost`. Compose already injects
   `MINIO_REGIONS_0_*`, `MINIO_HOME_REGION`, `MINIO_UPLOADS_CONFIGURATION_*`,
   `SPRING_RABBITMQ_HOST` (relaxed binding) for the worker — externalization is the
   mechanism, so the duplicated/default-valued yaml is the target.
   `media-registry` has no `application.yaml`.
5. **A2/D4**: `.codex/skills/java-testing/SKILL.md` lines 3, 15, 32, 62 still
   prescribe MockWebServer; the code standard is `MockRestServiceServer` (used in
   `ApiCompletionClientTest`, `HttpImageTransformerTest`). Update the skill doc.
6. **B4/tag rework** (step-10 text: "one non-null tag representation instead of
   `ObjectTag` and the nullable tag arguments, and pinned replace-or-merge semantics
   for `setTags`. The expiration tags and the lifecycle rule stay."):
   - `com.example.object_storage.tags.ObjectTag` is still the enum with
     `EXPIRES("expires","enabled")` / `PERMANENT("expires","disabled")` and a
     `toMap(Collection<ObjectTag>)`; `MinioObjectStore` maps `ObjectLifetime`→tag
     via `tagOf`/`tagMap`; `Uploads` uses `ObjectTag.EXPIRES` for the lifecycle
     `RuleFilter(new Tag("expires","enabled"))`.
   - `MinioObjectStore.setLifetime` (around lines 112–133) carries the TODO: pin the
     replace-or-merge semantics of the Minio `setObjectTags` write with a behavior
     test (e.g. write an extra tag, call `setLifetime`, assert whether the extra tag
     survives). `MinioObjectStoreIT` already has `tagsOf(ref)` via `minioConfig`.
7. **ArchUnit** (the actual step-10 item). Rules verbatim from
   `clean-media-service.md` line 72: "The contract cannot import Spring, JPA, Minio,
   or Jackson. `media-json` cannot import Spring, JPA, or Minio. The worker cannot
   import the registry." Packages: `com.example.media.contract.**`,
   `com.example.media.json`, `com.example.media.imageworker.**` must not depend on
   `com.example.media.registry.**`. ArchUnit version was not yet looked up — pick a
   current `com.tngtech.archunit:archunit-junit5` release and pin it via the same
   version-source decision made in A5.

## Remaining tasks (Phase 5), in order

1. A4: delete the three unused buildSrc conventions + openapi/springdoc pins;
   update `guides/gradle/copying dependencies.md`.
2. A5: single-source test-lib versions (catalog or buildSrc vals); align
   `mockitoAgent` with `mockito-core`; fix the `mockito-junit-jupiter` 5.20.0 skew.
3. A6: delete the `pluginManagement` block from `settings.gradle.kts`.
4. D6: hibernate plugin version → buildSrc; dedupe `minio:`/env externalization
   across both `application.yaml`s.
5. A2/D4: SKILL.md MockWebServer → MockRestServiceServer standard.
6. B4: replace `ObjectTag` enum + nullable tag args with one non-null tag
   representation; add the setTags replace-vs-merge behavior test
   (`MinioObjectStore.setLifetime` TODO marks the spot).
7. ArchUnit rules per finding 7 (need to add the dependency + an `ArchitectureTest`;
   where to host it: `media-api` tests can see all modules, or per-module tests).
8. After each change: `./gradlew test`, then `./gradlew :media-api:integrationTest`
   against the live compose stack (cockroach, minio, rabbitmq, image-worker are up).
9. Commit Phase 5; tick step 10 in `planning/uploads/clean-media-service.md` and
   update its `Status` section.

## Scope guardrails (from the approved plan)

- `media-contract` untouched except the Phase 4 `MediaQueue` constants class (A3).
- E2E harness redesign deferred post-step-10; `FailImageUpload` "unnecessary state"
  TODO left as-is.
- Spring 7: `RetryTemplate.invoke(Supplier)` rethrows the last cause on exhaustion.
- `MediaJson.create()` returns `tools.jackson.databind.json.JsonMapper`, not
  `ObjectMapper` — type it explicitly when wiring converters.
- The Minio expiration tags and the bucket lifecycle rule stay.

## Environment gotchas

- `run_commands` has a hard ~30s cap here — Gradle must run via
  `jetbrains-idea__execute_terminal_command` with `cd java && …` and a 180000 ms
  timeout (the IDE terminal starts at the repo root, not `java/`).
- `read_files` returns "[outdated - see the latest file content]" for some
  `MinioObjectStore.java` ranges; use `sed`/`get_file_text_by_path` instead.
- Docker compose dev stack must stay up for integration tests; object-storage ITs use
  Testcontainers (`MinioIntegrationTest`), `EndToEndTest` uses the compose stack.
