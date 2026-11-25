package com.example.media.api.web;

import java.time.Instant;

/**
 * An issued download permission. The {@code signature}, {@code expires}, {@code keyId}, and
 * {@code version} are the {@code s}, {@code e}, {@code k}, and {@code v} query parameters of a read
 * URL; {@code pathPrefix} is the CDN path prefix the token authorizes. The token is not encrypted:
 * the claims are public and the HMAC signature prevents tampering.
 */
public record AdminDownloadPermissionResponse(
        String uploadKey,
        String version,
        Instant expiresAt,
        long expires,
        String signature,
        String keyId,
        String pathPrefix
) {
}