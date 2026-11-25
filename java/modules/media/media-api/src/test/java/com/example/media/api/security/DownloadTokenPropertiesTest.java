package com.example.media.api.security;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The download token configuration is the signing material of a bearer credential, so a
 * misconfiguration fails startup: a weak secret, an incomplete or ambiguous rotation pair, or a
 * negative clock skew never reaches runtime.
 */
class DownloadTokenPropertiesTest {

    /** base64 of "0123456789abcdef0123456789abcdef" (32 bytes). */
    private static final String SECRET = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";
    /** base64 of "fedcba9876543210fedcba9876543210" (32 bytes). */
    private static final String OTHER_SECRET = "ZmVkY2JhOTg3NjU0MzIxMGZlZGNiYTk4NzY1NDMyMTA=";
    private static final Duration TTL = Duration.ofMinutes(15);
    private static final Duration MAX_TTL = Duration.ofHours(1);
    private static final Duration CLOCK_SKEW = Duration.ofSeconds(5);

    @Test
    void resolvesTheCurrentAndThePreviousKey() {
        var properties = properties("k1", OTHER_SECRET);

        assertThat(properties.secretFor("k2")).isEqualTo(SECRET);
        assertThat(properties.secretFor("k1")).isEqualTo(OTHER_SECRET);
    }

    @Test
    void anUnknownKeyIdHasNoSecret() {
        assertThat(properties(null, null).secretFor("k1")).isNull();
    }

    @Test
    void blankPreviousValuesCountAsNoRotation() {
        var properties = properties(" ", "");

        assertThat(properties.previousKeyId()).isNull();
        assertThat(properties.secretFor("k1")).isNull();
    }

    @Test
    void aPreviousKeyWithoutASecretIsRejected() {
        assertThatThrownBy(() -> properties("k1", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be set together");
    }

    @Test
    void aPreviousSecretWithoutAKeyIdIsRejected() {
        assertThatThrownBy(() -> properties(null, OTHER_SECRET))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be set together");
    }

    @Test
    void thePreviousKeyIdMustDifferFromTheCurrentOne() {
        assertThatThrownBy(() -> properties("k2", OTHER_SECRET))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must differ");
    }

    @Test
    void aWeakPreviousSecretIsRejected() {
        assertThatThrownBy(() -> properties("k1", "c2hvcnQ="))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 32 bytes");
    }

    @Test
    void aWeakOrMalformedCurrentSecretIsRejected() {
        assertThatThrownBy(() -> properties("c2hvcnQ=", "k1", null, null, TTL, MAX_TTL, CLOCK_SKEW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 32 bytes");
        assertThatThrownBy(() -> properties("not base64!", "k1", null, null, TTL, MAX_TTL, CLOCK_SKEW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("base64");
    }

    @Test
    void aNegativeClockSkewIsRejected() {
        assertThatThrownBy(() -> properties(SECRET, "k1", null, null, TTL, MAX_TTL, Duration.ofSeconds(-1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("clock-skew");
    }

    @Test
    void aMaximumTtlShorterThanTheTtlIsRejected() {
        assertThatThrownBy(() -> properties(SECRET, "k1", null, null, MAX_TTL, TTL, CLOCK_SKEW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("max-ttl");
    }

    private static DownloadTokenProperties properties(@Nullable String previousKeyId, @Nullable String previousSecret) {
        return properties(SECRET, "k2", previousKeyId, previousSecret, TTL, MAX_TTL, CLOCK_SKEW);
    }

    private static DownloadTokenProperties properties(
            String secret, String keyId, @Nullable String previousKeyId, @Nullable String previousSecret,
            Duration ttl, Duration maxTtl, Duration clockSkew) {
        return new DownloadTokenProperties(secret, keyId, previousKeyId, previousSecret, ttl, maxTtl, clockSkew);
    }
}
