# Java social media application

This is a learning project about java spring boot, scalable architecture, large applications and social media related algorithms (e.g., ranking, fanout, uploads, notifications).

## Important locations
- Plans and descriptions: `/planning` contains a high level overview of the functions.
- Modules: `/modules` contains all Java code modules. `media` is the media service: it registers named transformations, accepts image uploads, transforms them through a worker, and serves the originals and named variants. See `modules/media/AGENTS.md`.
- Templates: `/templates` modules templates. Copying a template is a quick way to create a new module.

## Conventions

Binding rules for the whole project. Each rule has one home: it is enforced in the build or the code where possible, and the other documents link here instead of restating it. When work defines a new convention, record it here before committing the change. Plans and handoff journals are history, not the home of a rule.

### One tool per task
- Outbound HTTP uses the Spring `RestClient`, shared by the whole app (no `RestTemplate`, Feign, or other HTTP clients, and no per-module copies of shared client setup).
- Outbound HTTP is tested with `MockRestServiceServer` only (no MockWebServer, no WireMock).
- One test stack: JUnit 5 + AssertJ + Mockito (the `java-test` bundle of the catalog, or the Spring Boot BOM in the Spring modules). `MockMvc` for MVC endpoints, `@SpringBootTest` for wiring, Testcontainers for real dependencies in integration tests.
- Queue names come only from `MediaQueue` in `media-contract`; there are no local copies of the constants.
- JSON is Jackson 3 (`tools.jackson`); the JSON shape of the media messages lives only in `media-json`.

### One version home
- `gradle/libs.versions.toml` is the single home of the dependency and plugin versions. Never inline a `version` in a module build file.
- Libraries the Spring Boot BOM manages are declared without a version; the catalog pins only what no BOM owns (see the catalog header).
- Plugin versions are pinned as buildSrc classpath dependencies from the catalog and applied by id in the convention plugins.
- The Mockito agent is the `mockito-core` of the same test classpath, so the agent and the core can never differ. Keep the whole Mockito family on one version.

### Build logic in buildSrc
- Modules apply the buildSrc convention plugins (`java-library-conventions`, `spring-library-conventions`, `spring-project-conventions`) instead of copying build snippets between build files. Mechanics and commands: `guides/gradle/modules.md`.

### Module boundaries
- The modules follow hexagonal architecture: domain (plain Java), adapter (port implementations and applications), shared infrastructure (`guides/gradle/modules.md`).
- ArchUnit guards the media boundaries per module: `media-contract` cannot import Spring, JPA, Minio, or Jackson; `media-json` cannot import Spring, JPA, or Minio; the worker and the pure modules cannot depend on `media-registry`. See the per-module `ArchitectureTest` classes and `modules/media/AGENTS.md`.

### Testing
- The compose-backed end-to-end test drives the stack only through the client modules (`media-client`; the TypeScript browser client in `../typescript` is deprecated and will be generated from the future OpenAPI definition). The nginx-edge journeys are the one exception: they use a plain `RestClient` against `/cdn/`, because that URL space belongs to the edge while the client speaks the web API. End-to-end tests never set up or assert through in-process ports or registry internals; the admin web API and the upload status endpoint exist so the tests stay black-box.
- The image transformation output is locked at the image processing seam (`ImageProcessorTest` in the `image-worker`'s `integrationTest` suite). It asserts structure — dimensions, encodings, sampled pixels — never byte-exact goldens (the libvips version drifts), and runs in the JDK + libvips dev image (`modules/media/deployment/test-transformations.bash`), not in the normal unit-test run. The compose-backed end-to-end test stays out of the normal run too.
- `modules/media/deployment/compose.yaml` is the single stack definition for both the development workflow and the end-to-end test (Testcontainers local compose boots the same file). It must not set `container_name`. It publishes fixed host ports, so the end-to-end test runs with the development stack stopped (`run.bash down`) and fails fast otherwise.

### Configuration
- Shared configuration has one home (e.g. the `minio-defaults.yaml` of object-storage, imported by both deployables) and is overridden through environment variables. Do not duplicate yaml blocks verbatim between deployables.