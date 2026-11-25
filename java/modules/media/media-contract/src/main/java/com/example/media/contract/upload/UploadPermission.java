package com.example.media.contract.upload;

import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * An issued upload permission: the database row behind one upload token.
 *
 * <p>The stored constraints are authoritative. A client that presents the token may only start an
 * upload whose key, extension, content type, size, transformations, and visibility match them. The
 * row also carries the usage budget ({@code uses} out of {@code maxUses}) and the terminal
 * {@code status}.</p>
 *
 * <p>Only the SHA-256 hash of the token is stored; the raw token is returned once, when the
 * permission is created.</p>
 */
public record UploadPermission(
        UploadPermissionId id,
        String tokenHash,
        UploadKey uploadKey,
        String extension,
        @Nullable String contentType,
        @Nullable Long maxBytes,
        List<String> eagerTransformationNames,
        @Nullable String incomingTransformationName,
        UploadVisibility visibility,
        Instant expiresAt,
        int maxUses,
        int uses,
        UploadPermissionStatus status,
        Instant createdAt
) {
    public UploadPermission {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(tokenHash, "tokenHash");
        Objects.requireNonNull(uploadKey, "uploadKey");
        Objects.requireNonNull(extension, "extension");
        if (maxBytes != null && maxBytes <= 0)
            throw new IllegalArgumentException("maxBytes must be positive: " + maxBytes);
        Objects.requireNonNull(eagerTransformationNames, "eagerTransformationNames");
        eagerTransformationNames = List.copyOf(eagerTransformationNames);
        Objects.requireNonNull(visibility, "visibility");
        Objects.requireNonNull(expiresAt, "expiresAt");
        if (maxUses <= 0)
            throw new IllegalArgumentException("maxUses must be positive: " + maxUses);
        if (uses < 0)
            throw new IllegalArgumentException("uses must not be negative: " + uses);
        if (uses > maxUses)
            throw new IllegalArgumentException("uses must not exceed maxUses: " + uses + " > " + maxUses);
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(createdAt, "createdAt");
    }

    /** Whether the permission can be consumed at the given instant. */
    public boolean isUsableAt(Instant now) {
        Objects.requireNonNull(now, "now");
        return status == UploadPermissionStatus.ACTIVE
                && uses < maxUses
                && expiresAt.isAfter(now);
    }
}
