package com.example.media.api.security;

import com.example.media.contract.port.UploadPermissionRegistry;
import com.example.media.contract.upload.UploadKey;
import com.example.media.contract.upload.UploadPermission;
import com.example.media.contract.upload.UploadPermissionDeniedException;
import com.example.media.contract.upload.UploadPermissionExhaustedException;
import com.example.media.contract.upload.UploadPermissionId;
import com.example.media.contract.upload.UploadPermissionStatus;
import com.example.media.contract.upload.UploadVisibility;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The upload use case's permission check: a missing token is denied only while a permission is
 * required, a token is looked up by its SHA-256 hash, every stored constraint must match the upload,
 * and only a matching upload reserves a use. The registry is a mock, so the test asserts exactly
 * what the checker asks of it.
 */
class UploadPermissionValidatorTest {

    private static final Instant NOW = Instant.parse("2026-11-25T12:00:00Z");
    private static final String TOKEN = "up1_token";
    private static final UploadKey KEY = new UploadKey("cover_123");
    private static final UploadPermissionId PERMISSION_ID = new UploadPermissionId(UUID.randomUUID());

    private final UploadPermissionRegistry registry = mock();

    private final UploadPermissionValidator requiredValidator = validator(true);
    private final UploadPermissionValidator optionalValidator = validator(false);

    @Test
    void deniesAMissingPermissionWhenItIsRequired() {
        assertThatThrownBy(() -> requiredValidator.consume(null, constraints()))
                .isInstanceOf(UploadPermissionDeniedException.class);
        verify(registry, never()).findByTokenHash(anyString());
    }

    @Test
    void allowsAPermissionlessUploadWhenItIsNotRequired() {
        assertThat(optionalValidator.consume(null, constraints())).isNull();
        verify(registry, never()).findByTokenHash(anyString());
    }

    @Test
    void deniesAnUnknownToken() {
        when(registry.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requiredValidator.consume(TOKEN, constraints()))
                .isInstanceOf(UploadPermissionDeniedException.class);
        verify(registry, never()).consume(anyString(), any());
    }

    @Test
    void looksTheTokenUpByItsSha256Hash() throws Exception {
        when(registry.findByTokenHash(anyString())).thenReturn(Optional.of(permission()));
        when(registry.consume(anyString(), any())).thenAnswer(_ -> permission());

        requiredValidator.consume(TOKEN, constraints());

        var hash = ArgumentCaptor.forClass(String.class);
        verify(registry).findByTokenHash(hash.capture());
        assertThat(hash.getValue()).isEqualTo(sha256Hex(TOKEN));
        verify(registry).consume(hash.getValue(), NOW);
    }

    @Test
    void returnsTheConsumedPermission() {
        var permission = permission();
        when(registry.findByTokenHash(anyString())).thenReturn(Optional.of(permission));
        when(registry.consume(anyString(), any())).thenAnswer(_ -> permission);

        var consumed = requiredValidator.consume(TOKEN, constraints());

        assertThat(consumed).isSameAs(permission);
    }

    @Test
    void validatesEveryStoredConstraintBeforeSpendingAUse() {
        when(registry.findByTokenHash(anyString())).thenReturn(Optional.of(permission()));

        var mismatches = List.of(
                constraints("other_key", "jpg", "image/jpeg", 3, List.of("profile"), "optimize"),
                constraints(KEY.value(), "png", "image/jpeg", 3, List.of("profile"), "optimize"),
                constraints(KEY.value(), "jpg", "image/png", 3, List.of("profile"), "optimize"),
                constraints(KEY.value(), "jpg", "image/jpeg", 1025, List.of("profile"), "optimize"),
                constraints(KEY.value(), "jpg", "image/jpeg", 3, List.of("thumb"), "optimize"),
                constraints(KEY.value(), "jpg", "image/jpeg", 3, List.of("profile"), null),
                constraints(KEY.value(), "jpg", "image/jpeg", 3, List.of("thumb", "profile"), "optimize"));

        for (var constraints : mismatches) {
            assertThatThrownBy(() -> requiredValidator.consume(TOKEN, constraints))
                    .as("constraints %s", constraints)
                    .isInstanceOf(UploadPermissionDeniedException.class);
        }
        verify(registry, never()).consume(anyString(), any());
    }

    @Test
    void doesNotCheckConstraintsThePermissionLeavesOpen() {
        var open = permission(null, null, List.of(), null);
        when(registry.findByTokenHash(anyString())).thenReturn(Optional.of(open));
        when(registry.consume(anyString(), any())).thenAnswer(_ -> open);

        var consumed = requiredValidator.consume(TOKEN,
                constraints(KEY.value(), "jpg", "application/octet-stream", 4294967296L, List.of(), null));

        assertThat(consumed).isSameAs(open);
        verify(registry).consume(anyString(), eq(NOW));
    }

    @Test
    void propagatesTheRegistryExpiryAndExhaustion() {
        when(registry.findByTokenHash(anyString())).thenReturn(Optional.of(permission()));
        when(registry.consume(anyString(), any()))
                .thenThrow(new UploadPermissionExhaustedException("no uses left"));

        assertThatThrownBy(() -> requiredValidator.consume(TOKEN, constraints()))
                .isInstanceOf(UploadPermissionExhaustedException.class);
    }

    private UploadPermissionValidator validator(boolean required) {
        return new UploadPermissionValidator(
                registry,
                new UploadPermissionProperties(required, Duration.ofMinutes(10), Duration.ofHours(1), 1, 10),
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    /** A permission for {@link #KEY} with every constraint set. */
    private static UploadPermission permission() {
        return permission("image/jpeg", 1024L, List.of("profile"), "optimize");
    }

    private static UploadPermission permission(
            String contentType, Long maxBytes, List<String> eagerNames, String incomingName) {
        return new UploadPermission(
                PERMISSION_ID, "hash", KEY, "jpg", contentType, maxBytes, eagerNames, incomingName,
                UploadVisibility.PRIVATE, NOW.plus(Duration.ofMinutes(10)), 1, 0,
                UploadPermissionStatus.ACTIVE, NOW);
    }

    private static UploadPermissionValidator.Constraints constraints() {
        return constraints(KEY.value(), "jpg", "image/jpeg", 3, List.of("profile"), "optimize");
    }

    private static UploadPermissionValidator.Constraints constraints(
            String uploadKey,
            String extension,
            String contentType,
            long bytes,
            List<String> eagerNames,
            String incomingName
    ) {
        return new UploadPermissionValidator.Constraints(
                uploadKey, extension, contentType, bytes, eagerNames, incomingName);
    }

    private static String sha256Hex(String token) throws Exception {
        var digest = MessageDigest.getInstance("SHA-256")
                .digest(token.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(digest);
    }
}
