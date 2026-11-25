package com.example.media.api.security;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.util.Base64;

/**
 * The download token settings ({@code media.download-token}): the HMAC keys that sign and verify
 * read credentials, the issuance limits, and the clock-skew tolerance.
 *
 * <p>{@link #secret()} is base64; its decoded bytes are the HMAC-SHA256 signing key of
 * {@link #keyId()} and must be at least 32 bytes (256 bits), which is checked at startup so a
 * deployment that misconfigures the secret fails fast instead of issuing weakly signed tokens.</p>
 *
 * <p>A rotation keeps the old key in {@link #previousKeyId()}/{@link #previousSecret()}: new tokens
 * are signed with the current key, and both keys verify, so outstanding tokens stay valid until
 * they expire. Both previous values must be set together, the ids must differ, and the secret must
 * be as strong as the current one. {@link #clockSkew()} is the tolerance for the clock difference
 * between this API and a verifier (the nginx edge); the edge must use the same tolerance.</p>
 *
 * <p>The issuance clamps a requested TTL to {@link #maxTtl()}; an omitted TTL takes {@link #ttl()}.
 * Unlike upload permissions, a download token has no usage budget: it is valid until it expires.</p>
 */
@ConfigurationProperties(prefix = "media.download-token")
@NullMarked
public record DownloadTokenProperties(
        String secret,
        @DefaultValue("k1") String keyId,
        @Nullable String previousKeyId,
        @Nullable String previousSecret,
        @DefaultValue("15m") Duration ttl,
        @DefaultValue("1h") Duration maxTtl,
        @DefaultValue("5s") Duration clockSkew
) {
    public DownloadTokenProperties {
        requireStrongSecret(secret, "media.download-token.secret");
        if (keyId.isBlank())
            throw new IllegalArgumentException("media.download-token.key-id must not be blank");
        previousKeyId = blankToNull(previousKeyId);
        previousSecret = blankToNull(previousSecret);
        if ((previousKeyId == null) != (previousSecret == null))
            throw new IllegalArgumentException(
                    "media.download-token.previous-key-id and previous-secret must be set together");
        if (previousKeyId != null) {
            if (previousKeyId.equals(keyId))
                throw new IllegalArgumentException(
                        "media.download-token.previous-key-id must differ from the current key-id: " + keyId);
            requireStrongSecret(previousSecret, "media.download-token.previous-secret");
        }
        if (ttl.isZero() || ttl.isNegative())
            throw new IllegalArgumentException("media.download-token.ttl must be positive: " + ttl);
        if (maxTtl.compareTo(ttl) < 0)
            throw new IllegalArgumentException(
                    "media.download-token.max-ttl must not be shorter than ttl: " + maxTtl);
        if (clockSkew.isNegative())
            throw new IllegalArgumentException(
                    "media.download-token.clock-skew must not be negative: " + clockSkew);
    }

    /**
     * The base64 secret that verifies a token signed with {@code keyId}: the current key or the
     * previous one of a rotation. {@code null} when the key id is unknown, so a token can never be
     * verified with a key it does not name.
     */
    public @Nullable String secretFor(String keyId) {
        if (this.keyId.equals(keyId))
            return secret;
        if (previousKeyId != null && previousKeyId.equals(keyId))
            return previousSecret;
        return null;
    }

    private static void requireStrongSecret(@Nullable String secret, String property) {
        if (secret == null)
            throw new IllegalArgumentException(property + " is required");
        byte[] key;
        try {
            key = Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(property + " must be valid base64", e);
        }
        if (key.length < 32)
            throw new IllegalArgumentException(
                    property + " must decode to at least 32 bytes, got " + key.length);
    }

    private static @Nullable String blankToNull(@Nullable String value) {
        return value == null || value.isBlank() ? null : value;
    }
}