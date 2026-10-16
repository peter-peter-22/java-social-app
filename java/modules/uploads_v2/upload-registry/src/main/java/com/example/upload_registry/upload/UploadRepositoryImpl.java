package com.example.upload_registry.upload;

import com.example.upload_registry.stored_asset.StoredAssetCrudRepository;
import com.example.upload_registry.stored_asset.StoredAssetEntity;
import com.example.uploads_api.uploads.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@NullMarked
@Repository
@RequiredArgsConstructor
public class UploadRepositoryImpl implements UploadRepository {
    private final UploadCrudRepository uploadCrudRepository;
    private final StoredAssetCrudRepository storedAssetCrudRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public Upload insert(InsertUpload upload) throws UploadKeyTakenException {
        try {
            var storedAssetEntity=entityManager.getReference(StoredAssetEntity.class, upload.getStoredAssetId());
            return uploadCrudRepository.saveAndFlush(UploadEntity.builder()
                    .uploadKey(upload.getUploadKey())
                    .status(upload.getStatus())
                    .storedAsset(storedAssetEntity)
                    .build()).toModel();
        } catch (DataIntegrityViolationException e) {
            throw new UploadKeyTakenException("An upload already exists with key: " + upload.getUploadKey());
        }
    }

    @Override
    @Transactional
    public void updateStatus(UUID uploadId, UploadStatus status) {
        uploadCrudRepository.findById(uploadId).orElseThrow().setStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public Upload findByKey(String uploadKey) {
        return uploadCrudRepository.getByUploadKeyWithStoredAsset(uploadKey).orElseThrow().toModel();
    }

    @Override
    @Transactional(readOnly = true)
    public Upload findImageByKey(String uploadKey) {
        return uploadCrudRepository.getByUploadKeyWithImageMetadata(uploadKey).orElseThrow().toModel();
    }

    @Override
    @Transactional
    public void deleteById(String uploadKey) {
        uploadCrudRepository.deleteByUploadKey(uploadKey);
    }
}
