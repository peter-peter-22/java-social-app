package com.example.media.api.security;

/**
 * The download credential a client presents to read a private upload: the {@code e}, {@code s}, and
 * {@code k} query parameters of the read URL. The version travels separately as {@code v} because
 * it is also an optional cache key for public reads.
 *
 * <p>The signature covers the key id, the upload key, the version, and the expiry, so the token
 * grants every variant and file of that upload version and nothing else. The claims are public and
 * unencrypted; the HMAC signature prevents tampering.</p>
 *
 * @param expires   the expiry as decimal epoch seconds ({@code e})
 * @param signature the base64url HMAC-SHA256 signature ({@code s})
 * @param keyId     the signing key id ({@code k})
 */
public record DownloadCredential(long expires, String signature, String keyId) {
}