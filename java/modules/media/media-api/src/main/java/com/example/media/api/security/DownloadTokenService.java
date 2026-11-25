package com.example.media.api.security;

import com.example.media.api.upload.UploadNotFoundException;
import com.example.media.contract.port.UploadRegistry;
import com.example.media.contract.upload.Upload;
import com.example.media.contract.upload.UploadKey;
import com.example.media.contract.upload.UploadVisibility;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;

/**
 * Issues and verifies the stateless download tokens that grant read access to one private upload.
 *
 * <p>A token is an HMAC-SHA256 signature over a canonical string that binds the signing key id, the
 * upload key, the cache version, and the expiry:</p>
 *
 * <pre>{@code
 * canonical = "m1\nread\n" + keyId + "\n" + uploadKey + "\n" + version + "\n" + expiresEpochSeconds
 * signature = base64url(HMAC-SHA256(secret[keyId], canonical))
 * }</pre>
 *
 * <p>The version is {@link Upload#version()} (the upload's creation instant as epoch milliseconds),
 * so a token minted for one version cannot read a replaced upload at the same key. Because the
 * signature covers the whole upload-key prefix, one token grants the original and every variant of
 * that version. The claims are public and unencrypted; the signature prevents tampering. Because
 * there is no server-side state, a token is valid until it expires and cannot be revoked earlier.</p>
 *
 * <p>Issuance loads the finalized upload to compute its version and clamps the requested TTL to the
 * configured maximum. Enforcement ({@link #authorizeRead}) serves a {@code PUBLIC} upload to anyone
 * and requires a valid, current-version token for a {@code PRIVATE} one. A denial is a
 * {@link DownloadDeniedException}, which the web layer delivers as 403.</p>
 */
@Service
@RequiredArgsConstructor
@NullMarked
public class DownloadTokenService {
    /** The domain-separated prefix: a download token can never be confused with another credential. */
    private static final String CANONICAL_PREFIX = "m1\nread\n";
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final UploadRegistry uploadRegistry;
    private final DownloadTokenProperties properties;
    private final Clock clock;

    /**
     * Issues a download token for the upload's current version.
     *
     * @throws UploadNotFoundException when there is no finalized upload for the key
     */
    public IssuedDownloadToken issue(UploadKey key, @Nullable Long expiresInSeconds) {
        var upload = uploadRegistry.findUpload(key)
                .orElseThrow(() -> new UploadNotFoundException(
                        "could not find upload with key: " + key.value()));
        var version = upload.version();
        var expires = clock.instant().getEpochSecond() + ttlSeconds(expiresInSeconds);
        return new IssuedDownloadToken(version, expires, sign(key, version, expires), properties.keyId());
    }

    /**
     * Authorizes a read of the upload. A public upload is readable by anyone and the credential is
     * ignored. A private upload requires a credential whose signature matches the current version
     * and which has not expired; anything else is denied.
     *
     * @throws DownloadDeniedException when a private read is not authorized
     */
    public void authorizeRead(
            Upload upload,
            UploadKey key,
            @Nullable String version,
            @Nullable DownloadCredential credential
    ) {
        if (upload.visibility() == UploadVisibility.PUBLIC)
            return;
        if (version == null || credential == null)
            throw new DownloadDeniedException("a read of a private upload requires a download token");
        if (!upload.version().equals(version))
            throw new DownloadDeniedException("the download token is not for the current version of the upload");
        if (!verify(key, version, credential))
            throw new DownloadDeniedException("the download token is invalid or expired");
    }

    /** Signs the canonical string for the upload key, version, and expiry. */
    String sign(UploadKey key, String version, long expiresEpochSecond) {
        var canonical = CANONICAL_PREFIX
                + properties.keyId() + "\n"
                + key.value() + "\n"
                + version + "\n"
                + expiresEpochSecond;
        return base64Url(hmac(canonical));
    }

    /**
     * Verifies the presented credential's signature and expiry. The signature is compared in
     * constant time with {@link MessageDigest#isEqual} so a wrong signature leaks no timing hint.
     */
    boolean verify(UploadKey key, String version, DownloadCredential credential) {
        if (!properties.keyId().equals(credential.keyId()))
            return false;
        var expected = sign(key, version, credential.expires());
        if (!MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                credential.signature().getBytes(StandardCharsets.UTF_8)))
            return false;
        return clock.instant().isBefore(Instant.ofEpochSecond(credential.expires()));
    }

    private byte[] hmac(String canonical) {
        try {
            var mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(Base64.getDecoder().decode(properties.secret()), HMAC_ALGORITHM));
            return mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC-SHA256 is not available", e);
        }
    }

    /** The token lifetime: the default when the request leaves it out, clamped to the maximum. */
    private long ttlSeconds(@Nullable Long expiresInSeconds) {
        if (expiresInSeconds == null)
            return properties.ttl().toSeconds();
        if (expiresInSeconds <= 0)
            throw new IllegalArgumentException("the download token TTL must be positive: " + expiresInSeconds);
        return Math.min(expiresInSeconds, properties.maxTtl().toSeconds());
    }

    private static String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * An issued download token: the version it is bound to, its expiry as epoch seconds, its
     * signature, and the signing key id. Together with the upload key these are the {@code v},
     * {@code e}, {@code s}, and {@code k} query parameters of a read URL.
     */
    public record IssuedDownloadToken(String version, long expires, String signature, String keyId) {
    }
}