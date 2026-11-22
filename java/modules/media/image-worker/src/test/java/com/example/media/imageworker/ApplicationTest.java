package com.example.media.imageworker;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * The context smoke test: the fully wired worker context loads with the real Minio object store.
 * The object storage is not contacted at startup (reconciliation is off and the Minio clients
 * connect lazily). The worker has no database and no migrations.
 */
@SpringBootTest
class ApplicationTest {

    @Test
    void contextLoads() {
    }
}
