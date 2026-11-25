package com.example.media.api.security;

import com.example.media.contract.port.UploadPermissionRegistry;
import com.example.media.contract.upload.UploadPermission;
import com.example.media.contract.upload.UploadPermissionDeniedException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;
import java.util.Objects;

/**
 * The start-image-upload use case's permission check: validates the presented token against the
 * upload, then reserves one use of it inside the caller's transaction.
 *
 * <p>The stored permission is authoritative. The upload must match its upload key, extension,
 * content type, maximum size, eager transformation names including their order, and incoming
 * transformation name. A request that does not match is denied without spending a use; a request
 * that passes spends one use and then uploads under the permission's key and visibility.</p>
 *
 * <p>A request without a token is allowed only when {@code media.upload-permission.required} is
 * false; the upload is then public and carries no permission. A denial is an
 * {@link UploadPermissionDeniedException} (or its expired and exhausted subclasses), which the web
 * layer delivers as 403, never as the 400 of a malformed request.</p>
 */
@Service
@RequiredArgsConstructor
@NullMarked
public class UploadPermissionValidator {
    private final UploadPermissionRegistry uploadPermissionRegistry;
    private final UploadPermissionProperties properties;
    private final Clock clock;

    /**
     * Validates the token against the upload and reserves one use of the permission.
     *
     * @param token       the raw token the client presented, if any
     * @param constraints the upload the request asks for
     * @return the consumed permission, or null when the request presents no token and no permission
     * is required
     * @throws UploadPermissionDeniedException when the token is unknown, revoked, or does not match
     *                                         the upload, and when a required token is missing
     */
    public @Nullable UploadPermission consume(@Nullable String token, Constraints constraints) {
        if (token == null) {
            if (properties.required())
                throw new UploadPermissionDeniedException("an upload permission is required");
            return null;
        }
        var hash = UploadTokenHash.of(token);
        // The constraints never change, so validating the readable row first is safe: consume then
        // locks the row and re-checks the expiry, the status, and the remaining uses.
        var permission = uploadPermissionRegistry.findByTokenHash(hash)
                .orElseThrow(() -> new UploadPermissionDeniedException(
                        "there is no upload permission for this token"));
        validate(permission, constraints);
        return uploadPermissionRegistry.consume(hash, clock.instant());
    }

    private static void validate(UploadPermission permission, Constraints constraints) {
        // An omitted upload key means "use the permission's key"; a present one must match it.
        if (constraints.uploadKey() != null
                && !permission.uploadKey().value().equals(constraints.uploadKey()))
            throw denied("the upload key", constraints.uploadKey(), permission.uploadKey().value());
        if (!permission.extension().equals(constraints.extension()))
            throw denied("the extension", constraints.extension(), permission.extension());
        if (permission.contentType() != null && !permission.contentType().equals(constraints.contentType()))
            throw denied("the content type", constraints.contentType(), permission.contentType());
        if (permission.maxBytes() != null && constraints.bytes() > permission.maxBytes())
            throw new UploadPermissionDeniedException(
                    "the uploaded file is larger than the permission allows: "
                            + constraints.bytes() + " > " + permission.maxBytes());
        if (!permission.eagerTransformationNames().equals(constraints.eagerTransformationNames()))
            throw denied("the eager transformations", constraints.eagerTransformationNames(),
                    permission.eagerTransformationNames());
        if (!Objects.equals(permission.incomingTransformationName(), constraints.incomingTransformationName()))
            throw denied("the incoming transformation", constraints.incomingTransformationName(),
                    permission.incomingTransformationName());
    }

    private static UploadPermissionDeniedException denied(
            String what, @Nullable Object requested, @Nullable Object allowed) {
        return new UploadPermissionDeniedException(
                "%s does not match the upload permission: requested %s, permitted %s"
                        .formatted(what, requested, allowed));
    }

    /**
     * The upload values the stored permission must match. The upload key is optional: when the
     * request leaves it out, the permission's key names the upload. The remaining values are the
     * validated request values, so a malformed request is already a 400 before this check runs.
     */
    public record Constraints(
            @Nullable String uploadKey,
            String extension,
            String contentType,
            long bytes,
            List<String> eagerTransformationNames,
            @Nullable String incomingTransformationName
    ) {
        public Constraints {
            Objects.requireNonNull(extension, "extension");
            Objects.requireNonNull(contentType, "contentType");
            Objects.requireNonNull(eagerTransformationNames, "eagerTransformationNames");
            eagerTransformationNames = List.copyOf(eagerTransformationNames);
        }
    }
}
