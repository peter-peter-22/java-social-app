package com.example.media.api;

import com.example.cockroach_db.CockroachIntegrationTest;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * The context smoke test: the fully wired media API context loads against a testcontainer database.
 * The driven adapters (the JPA registry, the Minio object store, and the blocking HTTP worker call)
 * are the real ones; the external services are not contacted at startup. The Compose-backed
 * end-to-end behavior lives in the {@code integrationTest} suite, not in the normal test run.
 */
@SpringBootTest
class ApplicationTest extends CockroachIntegrationTest {
    // Inherits contextLoads and testConnection from CockroachIntegrationTest.
}
