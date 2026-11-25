package com.example.media.api.security;

import com.example.media.contract.port.UploadPermissionRegistry;
import com.example.media.contract.upload.NewUploadPermission;
import com.example.media.contract.upload.UploadKey;
import com.example.media.contract.upload.UploadPermission;
import com.example.media.contract.upload.UploadVisibility;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;

/**
 * The admin's issue-upload-permission use case: mints the opaque token a client presents before an
 * upload and stores its constraints and usage budget.
 *
 * <p>The token is {@code up1_} followed by 32 random bytes in unpadded base64url. It is returned once,
 * in clear, and only its SHA-256 hash (lowercase hex) is stored: the registry never sees the token
 * itself, so a leaked row cannot be replayed as a credential.</p>
 *
 * <p>The request may omit the upload key, the TTL, and the use budget. A generated key is 16 random
 * bytes in unpadded base64url, so it is URL-safe and short enough for the registry column. The TTL
 * and the budget default to the configured values, and a value above the configured maximum is
 * clamped rather than rejected: the maximum is a property of this deployment, not of the request.</p>
 *
 * <p>The constraints are stored, not enforced here. The upload use case validates a presented
 * permission against the actual upload, consumes one use, and uses the permission's upload key and
 * visibility. Bad input is an {@link IllegalArgumentException}, which the web layer delivers as 400.</p>
 */
@Service
@RequiredArgsConstructor
@NullMarked
public class UploadPermissionService {
    /** Tells an upload token apart from every other bearer credential of the service. */
    private static final String TOKEN_PREFIX = "up1_";
    /** The full 256 bits of the token's entropy. */
    private static final int TOKEN_BYTES = 32;
    /** 128 bits, which makes a generated upload key unique enough for its purpose. */
    private static final int GENERATED_KEY_BYTES = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UploadPermissionRegistry uploadPermissionRegistry;
    private final UploadPermissionProperties properties;
    private final Clock clock;

    /** Issues a permission and returns it with the raw token, which is never stored. */
    public IssuedPermission issue(Request request) {
        var extension = requiredText(request.extension(), "the extension of an upload permission is required");
        var eagerNames = validatedEagerNames(request.eagerTransformations());
        var incomingName = validatedIncomingName(request.incomingTransformationName());
        if (incomingName != null && eagerNames.contains(incomingName))
            throw new IllegalArgumentException(
                    "the incoming transformation must not be repeated as an eager transformation: " + incomingName);
        var maxBytes = validatedMaxBytes(request.maxBytes());
        var key = request.uploadKey() == null
                ? new UploadKey(generatedUploadKey())
                : new UploadKey(requiredText(request.uploadKey(), "the upload key must not be blank"));
        var visibility = request.visibility() == null ? UploadVisibility.PUBLIC : request.visibility();
        var now = clock.instant();
        var token = generateToken();
        var permission = uploadPermissionRegistry.create(
                new NewUploadPermission(
                        key,
                        extension,
                        request.contentType(),
                        maxBytes,
                        eagerNames,
                        incomingName,
                        visibility,
                        now.plus(ttlOf(request.expiresInSeconds())),
                        maxUsesOf(request.maxUses())),
                tokenHash(token));
        return new IssuedPermission(permission, token);
    }

    /**
     * The TTL of the permission: the default when the request leaves it out, otherwise the requested
     * value clamped to the configured maximum.
     */
    private Duration ttlOf(@Nullable Long expiresInSeconds) {
        if (expiresInSeconds == null)
            return properties.defaultTtl();
        if (expiresInSeconds <= 0)
            throw new IllegalArgumentException("the permission TTL must be positive: " + expiresInSeconds);
        var requested = Duration.ofSeconds(expiresInSeconds);
        return requested.compareTo(properties.maxTtl()) > 0 ? properties.maxTtl() : requested;
    }

    /**
     * The use budget of the permission: the default when the request leaves it out, otherwise the
     * requested value clamped to the configured maximum.
     */
    private int maxUsesOf(@Nullable Integer maxUses) {
        if (maxUses == null)
            return properties.defaultMaxUses();
        if (maxUses <= 0)
            throw new IllegalArgumentException("the permission use budget must be positive: " + maxUses);
        return Math.min(maxUses, properties.maxUses());
    }

    private static String requiredText(@Nullable String value, String message) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException(message);
        return value;
    }

    private static List<String> validatedEagerNames(@Nullable List<String> eagerTransformations) {
        if (eagerTransformations == null)
            return List.of();
        if (eagerTransformations.stream().anyMatch(name -> name == null || name.isBlank()))
            throw new IllegalArgumentException("the eager transformation names must not be blank");
        if (eagerTransformations.size() != new HashSet<>(eagerTransformations).size())
            throw new IllegalArgumentException("the eager transformation names must be unique");
        return List.copyOf(eagerTransformations);
    }

    private static @Nullable String validatedIncomingName(@Nullable String incomingTransformationName) {
        if (incomingTransformationName == null)
            return null;
        return requiredText(incomingTransformationName,
                "the incoming transformation name must not be blank");
    }

    private static @Nullable Long validatedMaxBytes(@Nullable Long maxBytes) {
        if (maxBytes != null && maxBytes <= 0)
            throw new IllegalArgumentException("the maximum size must be positive: " + maxBytes);
        return maxBytes;
    }

    private static String generateToken() {
        return TOKEN_PREFIX + base64Url(randomBytes(TOKEN_BYTES));
    }

    private static String generatedUploadKey() {
        return base64Url(randomBytes(GENERATED_KEY_BYTES));
    }

    private static byte[] randomBytes(int count) {
        var bytes = new byte[count];
        RANDOM.nextBytes(bytes);
        return bytes;
    }

    private static String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** The registry's lookup key: the SHA-256 of the raw token as lowercase hex. */
    private static String tokenHash(String token) {
        try {
            var digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    /** An issued permission together with its raw token, which is not recoverable later. */
    public record IssuedPermission(UploadPermission permission, String token) {
    }

    /**
     * The admin's issue-permission request as this use case takes it. The upload key, the TTL, the
     * use budget, and the visibility are optional; nulls take the documented defaults. Inconsistent
     * values are 400s.
     */
    public record Request(
            @Nullable String uploadKey,
            @Nullable String extension,
            @Nullable String contentType,
            @Nullable Long maxBytes,
            @Nullable List<String> eagerTransformations,
            @Nullable String incomingTransformationName,
            @Nullable UploadVisibility visibility,
            @Nullable Long expiresInSeconds,
            @Nullable Integer maxUses
    ) {
    }
}
