package com.example.upload_registry.upload_process;

import com.example.upload_registry.stored_asset.StoredAssetEntity;
import com.example.upload_registry.upload.UploadEntity;
import com.example.uploads_api.uploads.upload_process.UploadAttempt;
import com.example.uploads_api.uploads.upload_process.UploadProcess;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
@NullMarked
public class UploadProcessPersistenceImpl implements UploadProcessPersistence {
    private final UploadProcessCrudRepository uploadProcessCrudRepository;
    private final UploadAttemptCrudRepository uploadAttemptCrudRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public UploadProcess save(UploadProcess process) {
        var entity = uploadProcessCrudRepository.findById(process.getUploadKey())
                .orElseGet(() -> UploadProcessEntity.builder()
                        .uploadKey(process.getUploadKey())
                        .build());
        entity.setActiveAttempt(reference(UploadAttemptEntity.class, process.getActiveAttemptId()));
        entity.setUpload(reference(UploadEntity.class, process.getUploadId()));
        return uploadProcessCrudRepository.saveAndFlush(entity).toModel();
    }

    @Override
    @Transactional(readOnly = true)
    public UploadProcess findProcess(String uploadKey) {
        return uploadProcessCrudRepository.findWithReferencesByUploadKey(uploadKey)
                .orElseThrow()
                .toModel();
    }

    @Override
    @Transactional
    public UploadAttempt save(UploadAttempt attempt) {
        var entity = attempt.getAttemptId() == null
                ? UploadAttemptEntity.builder().build()
                : uploadAttemptCrudRepository.findById(attempt.getAttemptId()).orElseThrow();
        entity.setUploadProcess(entityManager.getReference(UploadProcessEntity.class, attempt.getUploadKey()));
        entity.setEagerTransformationNames(attempt.getEagerTransformationNames());
        entity.setIncomingTransformationName(attempt.getIncomingTransformationName());
        entity.setOriginalAsset(entityManager.getReference(StoredAssetEntity.class, attempt.getOriginalAssetId()));
        entity.setStatus(attempt.getStatus());
        entity.setFinishedAt(attempt.getFinishedAt());
        return uploadAttemptCrudRepository.saveAndFlush(entity).toModel();
    }

    @Override
    @Transactional(readOnly = true)
    public UploadAttempt findAttempt(UUID attemptId) {
        return uploadAttemptCrudRepository.findWithReferencesByAttemptId(attemptId)
                .orElseThrow()
                .toModel();
    }

    private <T> T reference(Class<T> type, UUID id) {
        return id == null ? null : entityManager.getReference(type, id);
    }
}
