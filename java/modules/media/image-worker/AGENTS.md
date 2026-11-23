# Image worker

Spring Boot application that transforms images and writes the results to object storage.

Every execution allocates a fresh asset id and prefix for each output, so a retried or redelivered command never overwrites files from an earlier execution.

No database, no Liquibase, and no dependency on the media API or the registry. The API owns finalization.
