package com.example.media.api.web;

import java.time.Instant;

/**
 * An issued upload permission. {@code permission} is the raw bearer token: it is returned exactly
 * once, and only its hash is stored, so the caller must keep it.
 */
public record AdminUploadPermissionResponse(
        String permissionId,
        String uploadKey,
        String permission,
        Instant expiresAt,
        int maxUses
) {
}
