package com.example.media.registry.upload;

import com.example.media.contract.port.UploadPermissionRegistry;
import com.example.media.contract.upload.*;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * The upload permission rows.
 *
 * <p>Each method is one operation that joins the caller's transaction. {@link #consume} locks the
 * permission row for the rest of the caller's transaction, so a reservation and the attempt that
 * uses it commit or roll back together: a rejected upload never spends a use.</p>
 */
@Repository
@RequiredArgsConstructor
@NullMarked
public class JpaUploadPermissionRegistry implements UploadPermissionRegistry {
    private final UploadPermissionCrudRepository uploadPermissionCrudRepository;
    private final UploadAttemptCrudRepository uploadAttemptCrudRepository;

    @Override
    @Transactional
    public UploadPermission create(NewUploadPermission permission, String tokenHash) {
        var entity = uploadPermissionCrudRepository.saveAndFlush(UploadPermissionEntity.builder()
                .tokenHash(tokenHash)
                .uploadKey(permission.uploadKey().value())
                .extension(permission.extension())
                .contentType(permission.contentType())
                .maxBytes(permission.maxBytes())
                .eagerTransformationNames(List.copyOf(permission.eagerTransformationNames()))
                .incomingTransformationName(permission.incomingTransformationName())
                .visibility(permission.visibility())
                .expiresAt(permission.expiresAt())
                .maxUses(permission.maxUses())
                .uses(0)
                .status(UploadPermissionStatus.ACTIVE)
                .build());
        return entity.toModel();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UploadPermission> findByTokenHash(String tokenHash) {
        return uploadPermissionCrudRepository.findByTokenHash(tokenHash)
                .map(UploadPermissionEntity::toModel);
    }

    @Override
    @Transactional
    public UploadPermission consume(String tokenHash, Instant now) {
        var permission = uploadPermissionCrudRepository.findByTokenHashForUpdate(tokenHash)
                .orElseThrow(() -> new UploadPermissionDeniedException(
                        "there is no upload permission for this token"));

        // Expiry is checked before the status: an expired permission is reported as expired even
        // when its remaining uses were revoked.
        if (!permission.getExpiresAt().isAfter(now))
            throw new UploadPermissionExpiredException(
                    "the upload permission expired at " + permission.getExpiresAt());
        switch (permission.getStatus()) {
            case EXHAUSTED -> throw new UploadPermissionExhaustedException(
                    "the upload permission has no remaining uses");
            case REVOKED -> throw new UploadPermissionDeniedException(
                    "the upload permission was revoked");
            case ACTIVE -> {
            }
        }
        if (permission.getUses() >= permission.getMaxUses())
            throw new UploadPermissionExhaustedException(
                    "the upload permission has no remaining uses");

        permission.setUses(permission.getUses() + 1);
        if (permission.getUses() >= permission.getMaxUses())
            permission.setStatus(UploadPermissionStatus.EXHAUSTED);
        uploadPermissionCrudRepository.saveAndFlush(permission);
        return permission.toModel();
    }

    @Override
    @Transactional
    public void revokeForPermission(UploadPermissionId permissionId) {
        uploadPermissionCrudRepository.findById(permissionId.value())
                .ifPresent(this::revoke);
    }

    @Override
    @Transactional
    public void revokeForAttempt(AttemptId attemptId) {
        var attempt = uploadAttemptCrudRepository.findById(attemptId.value()).orElse(null);
        if (attempt == null || attempt.getUploadPermission() == null)
            return;
        revokeForPermission(new UploadPermissionId(attempt.getUploadPermission().getId()));
    }

    private void revoke(UploadPermissionEntity permission) {
        if (permission.getStatus() != UploadPermissionStatus.ACTIVE)
            return;
        permission.setStatus(UploadPermissionStatus.REVOKED);
        uploadPermissionCrudRepository.saveAndFlush(permission);
    }
}
