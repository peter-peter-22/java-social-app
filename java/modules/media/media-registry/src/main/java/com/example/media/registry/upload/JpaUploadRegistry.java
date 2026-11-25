package com.example.media.registry.upload;

import com.example.cockroach_db.SQLErrorCodes;
import com.example.media.contract.port.UploadRegistry;
import com.example.media.contract.upload.*;
import com.example.media.contract.variant.NewVariant;
import com.example.media.contract.variant.Variant;
import com.example.media.registry.asset.AssetEntity;
import com.example.media.registry.transformation.NamedTransformationEntity;
import com.example.media.registry.transformation.TransformationVersionEntity;
import com.example.media.registry.variant.VariantCrudRepository;
import com.example.media.registry.variant.VariantEntity;
import com.example.media.registry.variant.VariantEntityId;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * The upload lifecycle rows: processes, attempts, uploads, and variants.
 *
 * <p>Each method is one operation that joins the caller's transaction. The finalize transaction
 * boundary belongs to the application service, not to this adapter: the finalize sequence is the
 * caller's ordering of {@link #registerUpload}, {@link #registerVariants}, the asset status update,
 * and {@link #finishAttempt} inside one commit.</p>
 */
@Repository
@RequiredArgsConstructor
@NullMarked
public class JpaUploadRegistry implements UploadRegistry {
    private final UploadProcessCrudRepository uploadProcessCrudRepository;
    private final UploadAttemptCrudRepository uploadAttemptCrudRepository;
    private final UploadCrudRepository uploadCrudRepository;
    private final VariantCrudRepository variantCrudRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public UploadProcess openOrGetProcess(UploadKey key) {
        uploadProcessCrudRepository.insertIfNotExists(key.value());
        // The native insert creates the row when it is missing and never collides with a concurrent
        // opener; the locked read then returns the row with its references under the caller's lock.
        return uploadProcessCrudRepository.findByUploadKeyForUpdate(key.value())
                .orElseThrow()
                .toModel();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UploadProcess> findProcess(UploadKey key) {
        return uploadProcessCrudRepository.findByUploadKey(key.value())
                .map(UploadProcessEntity::toModel);
    }

    @Override
    @Transactional
    public AttemptId createAttempt(NewUploadAttempt insert) {
        var entity = uploadAttemptCrudRepository.saveAndFlush(UploadAttemptEntity.builder()
                .uploadProcess(entityManager.getReference(UploadProcessEntity.class, insert.key().value()))
                .eagerTransformationNames(List.copyOf(insert.eagerTransformationNames()))
                .incomingTransformationName(insert.incomingTransformationName())
                .visibility(insert.visibility())
                .uploadPermission(insert.uploadPermissionId() == null
                        ? null
                        : entityManager.getReference(
                                UploadPermissionEntity.class, insert.uploadPermissionId().value()))
                .originalAsset(entityManager.getReference(AssetEntity.class, insert.originalAssetId().value()))
                .status(UploadAttemptStatus.PENDING)
                .build());
        return new AttemptId(entity.getAttemptId());
    }

    @Override
    @Transactional
    public void activateAttempt(UploadKey key, AttemptId attemptId) throws UploadAlreadyFinishedException {
        var process = uploadProcessCrudRepository.findByUploadKeyForUpdate(key.value()).orElseThrow();
        if (process.getUpload() != null) {
            throw new UploadAlreadyFinishedException(
                    "the upload process " + key.value() + " is already finished, its attempt cannot be replaced");
        }
        process.setActiveAttempt(entityManager.getReference(UploadAttemptEntity.class, attemptId.value()));
        uploadProcessCrudRepository.saveAndFlush(process);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UploadAttempt> findAttempt(AttemptId id) {
        return uploadAttemptCrudRepository.findWithReferencesByAttemptId(id.value())
                .map(UploadAttemptEntity::toModel);
    }

    @Override
    @Transactional
    public void finishAttempt(AttemptId id, AttemptOutcome outcome) {
        var attempt = uploadAttemptCrudRepository.findByAttemptIdForUpdate(id.value()).orElseThrow();
        attempt.setStatus(outcome == AttemptOutcome.SUCCESS
                ? UploadAttemptStatus.SUCCESS
                : UploadAttemptStatus.FAILED);
        attempt.setFinishedAt(Instant.now());
        uploadAttemptCrudRepository.saveAndFlush(attempt);
    }

    @Override
    @Transactional
    public Upload registerUpload(NewUpload insert) throws UploadKeyTakenException, AttemptNotActiveException {
        var process = uploadProcessCrudRepository.findByUploadKeyForUpdate(insert.key().value())
                .orElseThrow(() -> new AttemptNotActiveException(
                        "there is no upload process with key " + insert.key().value()));
        if (process.getUpload() != null) {
            throw new AttemptNotActiveException(
                    "the upload process " + insert.key().value() + " already has its upload");
        }
        var activeAttemptId = process.getActiveAttempt() == null
                ? null
                : process.getActiveAttempt().getAttemptId();
        if (!insert.attemptId().value().equals(activeAttemptId)) {
            throw new AttemptNotActiveException(
                    "the attempt %s is not the active attempt of the upload process %s"
                            .formatted(insert.attemptId().value(), insert.key().value()));
        }

        UploadEntity upload;
        try {
            upload = uploadCrudRepository.saveAndFlush(UploadEntity.builder()
                    .uploadKey(insert.key().value())
                    .asset(entityManager.getReference(AssetEntity.class, insert.assetId().value()))
                    .visibility(insert.visibility())
                    .build());
        } catch (DataIntegrityViolationException e) {
            if (isUploadKeyTaken(e)) {
                throw new UploadKeyTakenException("an upload already exists with key: " + insert.key().value());
            }
            throw e;
        }

        process.setUpload(upload);
        uploadProcessCrudRepository.saveAndFlush(process);
        return upload.toModel();
    }

    @Override
    @Transactional
    public void registerVariants(Collection<NewVariant> variants) {
        for (var variant : variants) {
            try {
                variantCrudRepository.saveAndFlush(variantEntity(variant));
            } catch (DataIntegrityViolationException e) {
                throw new IllegalStateException("variant already exists for " + variant.id(), e);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Upload> findUpload(UploadKey key) {
        return uploadCrudRepository.findByUploadKeyWithAsset(key.value()).map(UploadEntity::toModel);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Variant> findVariant(UploadKey key, String transformationName) {
        return variantCrudRepository.findByUploadKeyAndTransformationName(key.value(), transformationName)
                .map(VariantEntity::toModel);
    }

    private VariantEntity variantEntity(NewVariant variant) {
        return VariantEntity.builder()
                .id(new VariantEntityId(
                        variant.id().uploadId().value(),
                        variant.id().namedTransformationId().value()))
                .uploadEntity(entityManager.getReference(UploadEntity.class, variant.id().uploadId().value()))
                .namedTransformationEntity(entityManager.getReference(
                        NamedTransformationEntity.class, variant.id().namedTransformationId().value()))
                .transformationVersionEntity(entityManager.getReference(
                        TransformationVersionEntity.class, variant.versionId().value()))
                .asset(entityManager.getReference(AssetEntity.class, variant.assetId().value()))
                .build();
    }

    private static boolean isUploadKeyTaken(DataIntegrityViolationException exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sqlException
                    && SQLErrorCodes.UNIQUE_VIOLATION.equals(sqlException.getSQLState())
                    && sqlException.getMessage().contains("upload_key_unique")) {
                return true;
            }
        }
        return false;
    }
}
