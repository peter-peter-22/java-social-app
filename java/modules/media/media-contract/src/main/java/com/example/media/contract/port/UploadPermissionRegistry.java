package com.example.media.contract.port;

import com.example.media.contract.upload.AttemptId;
import com.example.media.contract.upload.NewUploadPermission;
import com.example.media.contract.upload.UploadPermission;
import com.example.media.contract.upload.UploadPermissionDeniedException;
import com.example.media.contract.upload.UploadPermissionExhaustedException;
import com.example.media.contract.upload.UploadPermissionExpiredException;
import com.example.media.contract.upload.UploadPermissionId;

import java.time.Instant;
import java.util.Optional;

/**
 * The upload permission rows: issued tokens, their constraints, and their usage budget.
 *
 * <p>Tokens are looked up by their SHA-256 hash; the raw token never reaches this port. Each method
 * is a single operation that joins the caller's transaction, like {@link UploadRegistry}. Methods
 * documented as atomic are safe to rely on even outside a wrapping transaction.</p>
 */
public interface UploadPermissionRegistry {

    /**
     * Stores a permission in status {@code ACTIVE} with no uses and returns it.
     * Atomic on its own.
     *
     * @param tokenHash the SHA-256 hash of the raw token, as lowercase hex
     */
    UploadPermission create(NewUploadPermission permission, String tokenHash);

    /** Returns the permission of the token hash without changing it. */
    Optional<UploadPermission> findByTokenHash(String tokenHash);

    /**
     * Reserves one use of the permission and returns it with the incremented use count.
     * Atomic on its own: the row is locked for the rest of the caller's transaction, so a
     * concurrent consume waits and sees the committed count. The permission becomes
     * {@code EXHAUSTED} when the last use is reserved.
     *
     * @param now the instant to compare the expiry against
     * @throws UploadPermissionDeniedException    when the token is unknown or the permission was
     *                                            revoked
     * @throws UploadPermissionExpiredException   when the permission expired
     * @throws UploadPermissionExhaustedException when no use is left
     */
    UploadPermission consume(String tokenHash, Instant now);

    /**
     * Revokes the remaining uses of the permission. A missing permission is left alone, so
     * revocation is idempotent. Atomic on its own.
     */
    void revokeForPermission(UploadPermissionId permissionId);

    /**
     * Revokes the remaining uses of the permission the attempt consumed, if it consumed one.
     * An attempt without a permission, or an unknown attempt, is left alone. Atomic on its own.
     */
    void revokeForAttempt(AttemptId attemptId);
}
