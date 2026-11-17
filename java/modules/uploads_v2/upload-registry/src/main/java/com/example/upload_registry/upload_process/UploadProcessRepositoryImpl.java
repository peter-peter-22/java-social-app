package com.example.upload_registry.upload_process;

import com.example.upload_registry.stored_asset.StoredAssetCrudRepository;
import com.example.upload_registry.stored_asset.StoredAssetEntity;
import com.example.upload_registry.transformations.NamedTransformationCrudRepository;
import com.example.upload_registry.transformations.NamedTransformationEntity;
import com.example.upload_registry.transformations.TransformationVersionEntity;
import com.example.upload_registry.upload.UploadCrudRepository;
import com.example.upload_registry.upload.UploadEntity;
import com.example.upload_registry.variant.VariantCrudRepository;
import com.example.upload_registry.variant.VariantEntity;
import com.example.upload_registry.variant.VariantEntityId;
import com.example.uploads_api.stored_asset.StoredAssetStatus;
import com.example.uploads_api.uploads.Upload;
import com.example.uploads_api.uploads.upload_process.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@NullMarked
public class UploadProcessRepositoryImpl implements UploadProcessRepository {
    private final UploadProcessCrudRepository uploadProcessCrudRepository;
    private final UploadAttemptCrudRepository uploadAttemptCrudRepository;
    private final UploadCrudRepository uploadCrudRepository;
    private final VariantCrudRepository variantCrudRepository;
    private final StoredAssetCrudRepository storedAssetCrudRepository;
    private final NamedTransformationCrudRepository namedTransformationCrudRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public UploadProcess createProcessIfNotExistsThenLock(String uploadKey) {
        return lockProcess(uploadKey).toModel();
    }

    @Override
    @Transactional
    public UUID createAttempt(InsertUploadAttempt insert) {
        return uploadAttemptCrudRepository.saveAndFlush(UploadAttemptEntity.builder()
                .uploadProcess(entityManager.getReference(UploadProcessEntity.class, insert.getUploadKey()))
                .eagerTransformationNames(insert.getEagerTransformationNames())
                .incomingTransformationName(insert.getIncomingTransformationName())
                .originalAsset(entityManager.getReference(StoredAssetEntity.class, insert.getOriginalAssetId()))
                .status(UploadAttemptStatus.PENDING)
                .build()).getAttemptId();
    }

    @Override
    @Transactional
    public void setAttemptAsActive(String uploadKey, UUID attemptId) throws UploadAlreadyExistsException {
        var process = uploadProcessCrudRepository.findByUploadKeyForUpdate(uploadKey).orElseThrow();
        if (process.getUpload() != null) {
            throw new UploadAlreadyExistsException(
                    "Upload with uploadKey %s is already finished, cannot start a new attempt.".formatted(uploadKey));
        }
        process.setActiveAttempt(entityManager.getReference(UploadAttemptEntity.class, attemptId));
        uploadProcessCrudRepository.saveAndFlush(process);
    }

    @Override
    @Transactional
    public Upload finalizeAttempt(
            UUID attemptId,
            String uploadKey,
            @Nullable Collection<RegisterVariantArgs> variants,
            UUID originalAssetId
    ) throws UploadAlreadyExistsException, AttemptNotActiveException {
        var process = lockProcess(uploadKey);
        var activeAttemptId = process.getActiveAttempt() == null ? null : process.getActiveAttempt().getAttemptId();
        if (process.getUpload() != null) {
            throw new UploadAlreadyExistsException(
                    "Upload with uploadKey %s is already finished, cannot start a new attempt.".formatted(uploadKey));
        }
        if (!attemptId.equals(activeAttemptId)) {
            throw new AttemptNotActiveException(
                    "Attempt %s is not the active attempt of upload process %s.".formatted(attemptId, uploadKey));
        }

        var upload = uploadCrudRepository.save(UploadEntity.builder()
                .uploadKey(uploadKey)
                .storedAsset(entityManager.getReference(StoredAssetEntity.class, originalAssetId))
                .build());

        var assetIds = new ArrayList<UUID>();
        assetIds.add(originalAssetId);
        if (variants != null) {
            for (var variant : variants) {
                variantCrudRepository.save(variantEntity(upload, variant));
                assetIds.add(variant.storedAssetId());
            }
        }
        storedAssetCrudRepository.updateStatusAll(assetIds, StoredAssetStatus.READY);

        var attempt = uploadAttemptCrudRepository.findByAttemptIdForUpdate(attemptId)
                .orElseThrow(() -> new AttemptNotActiveException("Upload attempt %s does not exist.".formatted(attemptId)));
        attempt.setStatus(UploadAttemptStatus.SUCCESS);
        attempt.setFinishedAt(Instant.now());
        uploadAttemptCrudRepository.save(attempt);

        // Status updates clear the persistence context, so lock the process again before publishing it.
        var lockedProcess = uploadProcessCrudRepository.findByUploadKeyForUpdate(uploadKey).orElseThrow();
        if (lockedProcess.getUpload() != null) {
            throw new UploadAlreadyExistsException(
                    "Upload with uploadKey %s is already finished, cannot start a new attempt.".formatted(uploadKey));
        }
        if (lockedProcess.getActiveAttempt() == null
                || !attemptId.equals(lockedProcess.getActiveAttempt().getAttemptId())) {
            throw new AttemptNotActiveException(
                    "Attempt %s is not the active attempt of upload process %s.".formatted(attemptId, uploadKey));
        }
        lockedProcess.setUpload(entityManager.getReference(UploadEntity.class, upload.getId()));
        uploadProcessCrudRepository.saveAndFlush(lockedProcess);

        return uploadCrudRepository.getByUploadKeyWithStoredAsset(uploadKey).orElseThrow().toModel();
    }

    private UploadProcessEntity lockProcess(String uploadKey) {
        uploadProcessCrudRepository.insertIfNotExists(uploadKey);
        // TODO this looks weird. maybe it can be simplified
        return uploadProcessCrudRepository.findByUploadKeyForUpdate(uploadKey).orElseThrow();
    }

    private VariantEntity variantEntity(
            UploadEntity upload,
            RegisterVariantArgs variant
    ) {
        return VariantEntity.builder()
                .id(new VariantEntityId(upload.getId(), variant.transformation().getId()))
                .uploadEntity(upload)
                .namedTransformationEntity(entityManager.getReference(
                        NamedTransformationEntity.class, variant.transformation().getId()))
                .transformationVersionEntity(entityManager.getReference(
                        TransformationVersionEntity.class, variant.transformation().getCurrentVersionId()))
                .storedAsset(entityManager.getReference(StoredAssetEntity.class, variant.storedAssetId()))
                .build();
    }
}
