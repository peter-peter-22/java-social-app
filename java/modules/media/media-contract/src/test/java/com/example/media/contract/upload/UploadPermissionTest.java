package com.example.media.contract.upload;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UploadPermissionTest {

    private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z");
    private static final UploadKey KEY = new UploadKey("cover");

    @Test
    void anActivePermissionWithRemainingUsesIsUsable() {
        var permission = permission(UploadPermissionStatus.ACTIVE, 3, 2);

        assertThat(permission.isUsableAt(NOW)).isTrue();
    }

    @Test
    void anExpiredPermissionIsNotUsable() {
        var permission = permission(UploadPermissionStatus.ACTIVE, 3, 0);

        assertThat(permission.isUsableAt(NOW.plusSeconds(599))).isTrue();
        assertThat(permission.isUsableAt(NOW.plusSeconds(600))).isFalse();
    }

    @Test
    void anExhaustedPermissionIsNotUsable() {
        assertThat(permission(UploadPermissionStatus.EXHAUSTED, 3, 3).isUsableAt(NOW)).isFalse();
    }

    @Test
    void aRevokedPermissionIsNotUsableEvenWithRemainingUses() {
        assertThat(permission(UploadPermissionStatus.REVOKED, 3, 0).isUsableAt(NOW)).isFalse();
    }

    @Test
    void aPermissionWithoutRemainingUsesIsNotUsable() {
        assertThat(permission(UploadPermissionStatus.ACTIVE, 3, 3).isUsableAt(NOW)).isFalse();
    }

    @Test
    void copyOfTheTransformationNamesIsDefensive() {
        var names = new ArrayList<>(List.of("profile"));
        var permission = new UploadPermission(
                new UploadPermissionId(java.util.UUID.randomUUID()),
                "a".repeat(64),
                KEY,
                "jpg",
                "image/jpeg",
                1024L,
                names,
                null,
                UploadVisibility.PUBLIC,
                NOW.plusSeconds(600),
                1,
                0,
                UploadPermissionStatus.ACTIVE,
                NOW);

        names.add("thumb");

        assertThat(permission.eagerTransformationNames()).containsExactly("profile");
        assertThatThrownBy(() -> permission.eagerTransformationNames().add("thumb"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void rejectsMoreUsesThanTheBudgetAllows() {
        assertThatThrownBy(() -> permission(UploadPermissionStatus.ACTIVE, 1, 2))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNonPositiveBudgets() {
        assertThatThrownBy(() -> new NewUploadPermission(
                KEY, "jpg", null, 1024L, List.of(), null, UploadVisibility.PUBLIC, NOW, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new NewUploadPermission(
                KEY, "jpg", null, 0L, List.of(), null, UploadVisibility.PUBLIC, NOW, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsABlankExtension() {
        assertThatThrownBy(() -> new NewUploadPermission(
                KEY, " ", null, 1024L, List.of(), null, UploadVisibility.PUBLIC, NOW, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requiresAVisibility() {
        assertThatThrownBy(() -> new NewUpload(
                KEY, new AttemptId(java.util.UUID.randomUUID()),
                com.example.media.contract.asset.AssetId.generate(), null))
                .isInstanceOf(NullPointerException.class);
    }

    private static UploadPermission permission(
            UploadPermissionStatus status, int maxUses, int uses) {
        return new UploadPermission(
                new UploadPermissionId(java.util.UUID.randomUUID()),
                "a".repeat(64),
                KEY,
                "jpg",
                "image/jpeg",
                1024L,
                List.of("profile"),
                "optimize",
                UploadVisibility.PRIVATE,
                NOW.plusSeconds(600),
                maxUses,
                uses,
                status,
                NOW);
    }
}
