package com.example.media.api.security;

import com.example.media.contract.port.UploadPermissionRegistry;
import com.example.media.contract.upload.NewUploadPermission;
import com.example.media.contract.upload.UploadPermission;
import com.example.media.contract.upload.UploadPermissionId;
import com.example.media.contract.upload.UploadPermissionStatus;
import com.example.media.contract.upload.UploadVisibility;
import org.junit.jupiter.api.Test;
import org.jspecify.annotations.Nullable;
import org.mockito.ArgumentCaptor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The issue-upload-permission use case: the token format, the stored hash, the defaults, and the
 * clamping of the TTL and the use budget. The registry is a mock, so the test asserts exactly what
 * the use case asks it to store.
 */
class UploadPermissionServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-25T12:00:00Z");
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(10);
    private static final Duration MAX_TTL = Duration.ofHours(1);

    private final UploadPermissionRegistry registry = mock();
    private final UploadPermissionProperties properties =
            new UploadPermissionProperties(true, DEFAULT_TTL, MAX_TTL, 1, 10);
    private final UploadPermissionService service = new UploadPermissionService(
            registry, properties, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void issuesATokenBoundToTheRequestedConstraints() throws Exception {
        when(registry.create(any(), any())).thenAnswer(invocation ->
                permissionFor(invocation.getArgument(0), invocation.getArgument(1)));

        var issued = service.issue(new UploadPermissionService.Request(
                "cover_123", "jpg", "image/jpeg", 10485760L, List.of("profile", "thumb"), "optimize",
                UploadVisibility.PRIVATE, 600L, 3));

        // up1_ + 32 bytes in unpadded base64url: 4 + 43 characters, no padding.
        assertThat(issued.token()).startsWith("up1_").doesNotContain("=");
        assertThat(issued.token().substring("up1_".length())).hasSize(43);

        var created = ArgumentCaptor.forClass(NewUploadPermission.class);
        var storedHash = ArgumentCaptor.forClass(String.class);
        verify(registry).create(created.capture(), storedHash.capture());
        assertThat(created.getValue().uploadKey().value()).isEqualTo("cover_123");
        assertThat(created.getValue().extension()).isEqualTo("jpg");
        assertThat(created.getValue().contentType()).isEqualTo("image/jpeg");
        assertThat(created.getValue().maxBytes()).isEqualTo(10485760L);
        assertThat(created.getValue().eagerTransformationNames()).containsExactly("profile", "thumb");
        assertThat(created.getValue().incomingTransformationName()).isEqualTo("optimize");
        assertThat(created.getValue().visibility()).isEqualTo(UploadVisibility.PRIVATE);
        assertThat(created.getValue().expiresAt()).isEqualTo(NOW.plusSeconds(600));
        assertThat(created.getValue().maxUses()).isEqualTo(3);

        // Only the SHA-256 of the raw token reaches the registry.
        assertThat(storedHash.getValue()).isEqualTo(sha256Hex(issued.token()));
        assertThat(issued.permission().tokenHash()).isEqualTo(sha256Hex(issued.token()));
    }

    @Test
    void generatesAUrlSafeUploadKeyWhenTheRequestOmittedIt() {
        when(registry.create(any(), any())).thenAnswer(invocation ->
                permissionFor(invocation.getArgument(0), invocation.getArgument(1)));

        var first = service.issue(request(null, null, null, null));
        var second = service.issue(request(null, null, null, null));

        assertThat(first.permission().uploadKey().value()).matches("[A-Za-z0-9_-]{1,100}");
        assertThat(second.permission().uploadKey().value()).matches("[A-Za-z0-9_-]{1,100}");
        assertThat(first.permission().uploadKey()).isNotEqualTo(second.permission().uploadKey());
        assertThat(first.token()).isNotEqualTo(second.token());
    }

    @Test
    void defaultsTheTtlTheUseBudgetAndTheVisibility() {
        when(registry.create(any(), any())).thenAnswer(invocation ->
                permissionFor(invocation.getArgument(0), invocation.getArgument(1)));

        var issued = service.issue(new UploadPermissionService.Request(
                "cover", "jpg", null, null, null, null, null, null, null));

        assertThat(issued.permission().expiresAt()).isEqualTo(NOW.plus(DEFAULT_TTL));
        assertThat(issued.permission().maxUses()).isEqualTo(1);
        assertThat(issued.permission().visibility()).isEqualTo(UploadVisibility.PUBLIC);
        assertThat(issued.permission().eagerTransformationNames()).isEmpty();
        assertThat(issued.permission().incomingTransformationName()).isNull();
    }

    @Test
    void clampsTheTtlAndTheUseBudgetToTheConfiguredMaximums() {
        when(registry.create(any(), any())).thenAnswer(invocation ->
                permissionFor(invocation.getArgument(0), invocation.getArgument(1)));

        var issued = service.issue(request("cover", 7200L, 99, null));

        assertThat(issued.permission().expiresAt()).isEqualTo(NOW.plus(MAX_TTL));
        assertThat(issued.permission().maxUses()).isEqualTo(10);
    }

    @Test
    void rejectsBadInputBeforeReachingTheRegistry() {
        var invalid = List.<UploadPermissionService.Request>of(
                new UploadPermissionService.Request(null, null, null, null, null, null, null, null, null),
                new UploadPermissionService.Request(null, " ", null, null, null, null, null, null, null),
                new UploadPermissionService.Request(" ", "jpg", null, null, null, null, null, null, null),
                new UploadPermissionService.Request("cover", "jpg", null, 0L, null, null, null, null, null),
                new UploadPermissionService.Request("cover", "jpg", null, null, null, null, null, 0L, null),
                new UploadPermissionService.Request("cover", "jpg", null, null, null, null, null, null, 0),
                new UploadPermissionService.Request("cover", "jpg", null, null,
                        List.of("profile", "profile"), null, null, null, null),
                new UploadPermissionService.Request("cover", "jpg", null, null,
                        List.of(" "), null, null, null, null),
                new UploadPermissionService.Request("cover", "jpg", null, null,
                        null, " ", null, null, null),
                new UploadPermissionService.Request("cover", "jpg", null, null,
                        List.of("optimize"), "optimize", null, null, null));

        for (var request : invalid) {
            assertThatThrownBy(() -> service.issue(request))
                    .as("request %s", request)
                    .isInstanceOf(IllegalArgumentException.class);
        }
        verify(registry, never()).create(any(), any());
    }

    private static UploadPermissionService.Request request(
            @Nullable String uploadKey, @Nullable Long expiresInSeconds, @Nullable Integer maxUses,
            @Nullable UploadVisibility visibility) {
        return new UploadPermissionService.Request(
                uploadKey, "jpg", null, null, List.of("profile"), null, visibility,
                expiresInSeconds, maxUses);
    }

    private static UploadPermission permissionFor(NewUploadPermission requested, String tokenHash) {
        return new UploadPermission(
                new UploadPermissionId(UUID.randomUUID()),
                tokenHash,
                requested.uploadKey(),
                requested.extension(),
                requested.contentType(),
                requested.maxBytes(),
                requested.eagerTransformationNames(),
                requested.incomingTransformationName(),
                requested.visibility(),
                requested.expiresAt(),
                requested.maxUses(),
                0,
                UploadPermissionStatus.ACTIVE,
                NOW);
    }

    private static String sha256Hex(String token) throws Exception {
        var digest = MessageDigest.getInstance("SHA-256")
                .digest(token.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(digest);
    }
}
