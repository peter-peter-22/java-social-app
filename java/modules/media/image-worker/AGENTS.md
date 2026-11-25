# Image worker

Spring Boot application that transforms images and writes the results to object storage.

Every execution allocates a fresh asset id and prefix for each output, so a retried or redelivered command never overwrites files from an earlier execution.

No database, no Liquibase, and no dependency on the media API or the registry. The API owns finalization.

## Tests

The transformation output is locked by `ImageProcessorTest` in the `integrationTest` suite: real
libvips against generated fixture images, asserting dimensions of crop and pad, resolution limits,
and encoding round-trips. It needs the native libvips library, so it runs in the JDK + libvips dev
image through `deployment/test-transformations.bash`, not in the normal unit-test run. The suite
copies every output to `build/reports/transformations/` for visual inspection (the purpose of the
removed docker test). `TransformImageTest` covers the use case orchestration with an in-memory
`ImageProcessing` fake.
