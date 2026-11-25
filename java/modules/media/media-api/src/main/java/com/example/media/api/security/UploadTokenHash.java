package com.example.media.api.security;

import org.jspecify.annotations.NullMarked;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * The registry key of an upload token: the SHA-256 of the raw token as lowercase hex.
 *
 * <p>Issuance hashes the token it hands out and enforcement hashes the token it accepts, and the two
 * must never drift. This class is the one place that turns an upload token into the value the
 * registry stores.</p>
 */
@NullMarked
final class UploadTokenHash {
    private UploadTokenHash() {
    }

    static String of(String token) {
        try {
            var digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
