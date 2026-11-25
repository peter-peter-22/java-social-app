package com.example.media.api.security;

import org.jspecify.annotations.NullMarked;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.util.Base64;

/**
 * The download token settings ({@code media.download-token}): the HMAC secret and key id that sign
 * read credentials, and the issuance limits.
 *
 * <p>{@link #secret()} is base64; its decoded bytes are the HMAC-SHA256 signing key and must be at
 * least 32 bytes (256 bits), which is checked at startup so a deployment that misconfigures the
 * secret fails fast instead of issuing weakly signed tokens. {@link #keyId()} names the key inside
 * each token so a later rotation can overlap old and new secrets.</p>
 *
 * <p>The issuance clamps a requested TTL to {@link #maxTtl()}; an omitted TTL takes {@link #ttl()}.
 * Unlike upload permissions, a download token has no usage budget: it is valid until it expires.</p>
 */
@ConfigurationProperties(prefix = "media.download-token")
@NullMarked
public record DownloadTokenProperties(
        String secret,
        @DefaultValue("k1") String keyId,
        @DefaultValue("15m") Duration ttl,
        @DefaultValue("1h") Duration maxTtl
) {
    public DownloadTokenProperties {
        byte[] key;
        try {
            key = Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("media.download-token.secret must be valid base64", e);
        }
        if (key.length < 32)
            throw new IllegalArgumentException(
                    "media.download-token.secret must decode to at least 32 bytes, got " + key.length);
        if (keyId.isBlank())
            throw new IllegalArgumentException("media.download-token.key-id must not be blank");
        if (ttl.isZero() || ttl.isNegative())
            throw new IllegalArgumentException("media.download-token.ttl must be positive: " + ttl);
        if (maxTtl.compareTo(ttl) < 0)
            throw new IllegalArgumentException(
                    "media.download-token.max-ttl must not be shorter than ttl: " + maxTtl);
    }
}