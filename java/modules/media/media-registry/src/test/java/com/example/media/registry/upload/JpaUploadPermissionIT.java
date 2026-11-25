package com.example.media.registry.upload;

import com.example.cockroach_db.CockroachIntegrationTest;
import com.example.media.contract.asset.*;
import com.example.media.contract.port.AssetStore;
import com.example.media.contract.port.UploadPermissionRegistry;
import com.example.media.contract.port.UploadRegistry;
import com.example.media.contract.upload.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class JpaUploadPermissionIT extends CockroachIntegrationTest {
    private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z");

    @Autowired
    private UploadPermissionRegistry uploadPermissionRegistry;
    @Autowired
    private UploadRegistry uploadRegistry;
    @Autowired
    private AssetStore assetStore;

    @Test
    void createsAndReadsAPermission() {
        var hash = tokenHash();
        var key = key("permission");

        var created = uploadPermissionRegistry.create(newPermission(key, "profile"), hash);

        assertThat(created.status()).isEqualTo(UploadPermissionStatus.ACTIVE);
        assertThat(created.uses()).isZero();
        assertThat(created.tokenHash()).isEqualTo(hash);
        assertThat(created.uploadKey()).isEqualTo(key);
        assertThat(created.extension()).isEqualTo("jpg");
        assertThat(created.contentType()).isEqualTo("image/jpeg");
        assertThat(created.maxBytes()).isEqualTo(1024L);
        assertThat(created.eagerTransformationNames()).containsExactly("profile");
        assertThat(created.incomingTransformationName()).isEqualTo("optimize");
        assertThat(created.visibility()).isEqualTo(UploadVisibility.PRIVATE);
        assertThat(created.maxUses()).isEqualTo(2);
        assertThat(created.createdAt()).isNotNull();

        var found = uploadPermissionRegistry.findByTokenHash(hash).orElseThrow();
        assertThat(found.id()).isEqualTo(created.id());
        assertThat(found.expiresAt()).isEqualTo(created.expiresAt());
        assertThat(uploadPermissionRegistry.findByTokenHash(tokenHash())).isEmpty();
    }

    @Test
    void consumeReservesOneUseAndExhaustsThePermission() {
        var hash = tokenHash();
        uploadPermissionRegistry.create(newPermission(key("consume"), null), hash);

        var first = uploadPermissionRegistry.consume(hash, NOW);

        assertThat(first.uses()).isEqualTo(1);
        assertThat(first.status()).isEqualTo(UploadPermissionStatus.ACTIVE);

        var second = uploadPermissionRegistry.consume(hash, NOW);

        assertThat(second.uses()).isEqualTo(2);
        assertThat(second.status()).isEqualTo(UploadPermissionStatus.EXHAUSTED);
        assertThatThrownBy(() -> uploadPermissionRegistry.consume(hash, NOW))
                .isInstanceOf(UploadPermissionExhaustedException.class);
        var stored = uploadPermissionRegistry.findByTokenHash(hash).orElseThrow();
        assertThat(stored.uses()).isEqualTo(2);
        assertThat(stored.status()).isEqualTo(UploadPermissionStatus.EXHAUSTED);
    }

    @Test
    void anExpiredPermissionCannotBeConsumed() {
        var hash = tokenHash();
        uploadPermissionRegistry.create(newPermission(key("expired"), null), hash);

        assertThatThrownBy(() -> uploadPermissionRegistry.consume(hash, NOW.plusSeconds(601)))
                .isInstanceOf(UploadPermissionExpiredException.class);

        var stored = uploadPermissionRegistry.findByTokenHash(hash).orElseThrow();
        assertThat(stored.uses()).isZero();
        assertThat(stored.status()).isEqualTo(UploadPermissionStatus.ACTIVE);
    }

    @Test
    void anUnknownTokenIsDenied() {
        assertThatThrownBy(() -> uploadPermissionRegistry.consume(tokenHash(), NOW))
                .isInstanceOf(UploadPermissionDeniedException.class);
    }

    @Test
    void aRevokedPermissionCannotBeConsumed() {
        var hash = tokenHash();
        var created = uploadPermissionRegistry.create(newPermission(key("revoked"), null), hash);

        uploadPermissionRegistry.revokeForPermission(created.id());

        assertThatThrownBy(() -> uploadPermissionRegistry.consume(hash, NOW))
                .isInstanceOf(UploadPermissionDeniedException.class);
        assertThat(uploadPermissionRegistry.findByTokenHash(hash).orElseThrow().status())
                .isEqualTo(UploadPermissionStatus.REVOKED);
    }

    @Test
    void revokeForPermissionIsIdempotentAndIgnoresUnknownPermissions() {
        var hash = tokenHash();
        var created = uploadPermissionRegistry.create(newPermission(key("idempotent"), null), hash);

        uploadPermissionRegistry.revokeForPermission(created.id());
        uploadPermissionRegistry.revokeForPermission(created.id());
        uploadPermissionRegistry.revokeForPermission(new UploadPermissionId(UUID.randomUUID()));

        assertThat(uploadPermissionRegistry.findByTokenHash(hash).orElseThrow().status())
                .isEqualTo(UploadPermissionStatus.REVOKED);
    }

    @Test
    void revokeForAttemptRevokesTheConsumedPermission() {
        var hash = tokenHash();
        var key = key("attempt-revoke");
        var permission = uploadPermissionRegistry.create(newPermission(key, null), hash);
        var attemptId = startAttempt(key, permission.id());

        uploadPermissionRegistry.revokeForAttempt(attemptId);

        assertThatThrownBy(() -> uploadPermissionRegistry.consume(hash, NOW))
                .isInstanceOf(UploadPermissionDeniedException.class);
        assertThat(uploadPermissionRegistry.findByTokenHash(hash).orElseThrow().status())
                .isEqualTo(UploadPermissionStatus.REVOKED);
    }

    @Test
    void revokeForAttemptIgnoresAnAttemptWithoutAPermission() {
        var attemptId = startAttempt(key("no-permission"), null);

        uploadPermissionRegistry.revokeForAttempt(attemptId);
        uploadPermissionRegistry.revokeForAttempt(new AttemptId(UUID.randomUUID()));
    }

    @Test
    void twoConcurrentConsumesWithOneUseAllowExactlyOne() throws Exception {
        var hash = tokenHash();
        uploadPermissionRegistry.create(
                new NewUploadPermission(
                        key("concurrent"), "jpg", "image/jpeg", 1024L, List.of(), null,
                        UploadVisibility.PUBLIC, NOW.plusSeconds(600), 1),
                hash);
        var start = new CountDownLatch(1);
        var outcomes = Collections.synchronizedList(new ArrayList<>());

        var threads = List.of(
                consumeInThread(hash, start, outcomes),
                consumeInThread(hash, start, outcomes));
        threads.forEach(Thread::start);
        start.countDown();
        for (var thread : threads)
            thread.join(Duration.ofSeconds(30));

        assertThat(outcomes).hasSize(2);
        assertThat(outcomes.stream().filter(outcome -> outcome instanceof UploadPermission))
                .hasSize(1);
        assertThat(outcomes.stream()
                .filter(outcome -> outcome instanceof UploadPermissionExhaustedException))
                .hasSize(1);
        var stored = uploadPermissionRegistry.findByTokenHash(hash).orElseThrow();
        assertThat(stored.uses()).isEqualTo(1);
        assertThat(stored.status()).isEqualTo(UploadPermissionStatus.EXHAUSTED);
    }

    /** A thread that consumes the token once both threads are released together. */
    private Thread consumeInThread(String hash, CountDownLatch start, List<Object> outcomes) {
        return new Thread(() -> {
            try {
                start.await();
                outcomes.add(uploadPermissionRegistry.consume(hash, NOW));
            } catch (Throwable failure) {
                outcomes.add(failure);
            }
        });
    }

    /** Opens the process and registers an active attempt, optionally linked to a permission. */
    private AttemptId startAttempt(UploadKey key, UploadPermissionId permissionId) {
        var original = assetStore.registerAsset(new NewAsset(
                AssetId.generate(),
                new AssetPlacement("eu-central-1", "uploads"),
                AssetDeliveryProtocol.IMAGE,
                "image/jpeg",
                "original.jpg",
                "jpg",
                false,
                42,
                List.of("original.jpg"))).id();
        uploadRegistry.openOrGetProcess(key);
        var attemptId = uploadRegistry.createAttempt(new NewUploadAttempt(
                key, original, List.of(), null, UploadVisibility.PRIVATE, permissionId));
        uploadRegistry.activateAttempt(key, attemptId);
        return attemptId;
    }

    private NewUploadPermission newPermission(UploadKey key, String eagerName) {
        return new NewUploadPermission(
                key,
                "jpg",
                "image/jpeg",
                1024L,
                eagerName == null ? List.of() : List.of(eagerName),
                "optimize",
                UploadVisibility.PRIVATE,
                NOW.plusSeconds(600),
                2);
    }

    private UploadKey key(String prefix) {
        return new UploadKey(prefix + "-" + UUID.randomUUID());
    }

    /** A unique 64-character token hash, the width of the registry column. */
    private String tokenHash() {
        return UUID.randomUUID().toString().replace("-", "").repeat(2);
    }
}

