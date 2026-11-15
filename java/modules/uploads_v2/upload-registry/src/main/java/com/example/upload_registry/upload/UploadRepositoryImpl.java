package com.example.upload_registry.upload;

import com.example.cockroach_db.SQLErrorCodes;
import com.example.upload_registry.stored_asset.StoredAssetEntity;
import com.example.uploads_api.uploads.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.UUID;

@NullMarked
@Repository
@RequiredArgsConstructor
public class UploadRepositoryImpl implements UploadRepository {
    private final UploadCrudRepository uploadCrudRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public UUID insert(InsertUpload upload) throws UploadKeyTakenException {
        try {
            var storedAssetEntity = entityManager.getReference(StoredAssetEntity.class, upload.getStoredAssetId());
            return uploadCrudRepository.saveAndFlush(UploadEntity.builder()
                            .uploadKey(upload.getUploadKey())
                            .status(upload.getStatus())
                            .storedAsset(storedAssetEntity)
                            .build())
                    .getId();
        } catch (DataIntegrityViolationException e) {
            if (isUploadKeyTaken(e)) {
                throw new UploadKeyTakenException("An upload already exists with key: " + upload.getUploadKey());
            }
            throw e;
        }
    }

    @Override
    @Transactional
    public void updateStatus(UUID uploadId, UploadStatus status) {
        if (uploadCrudRepository.updateStatus(uploadId, status) == 0) {
            throw new NoSuchElementException("No value present");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Upload findByKey(String uploadKey) {
        return uploadCrudRepository.getByUploadKeyWithStoredAsset(uploadKey).orElseThrow().toModel();
    }

    @Override
    @Transactional(readOnly = true)
    public Upload findImageByKey(String uploadKey) {
        return uploadCrudRepository.getByUploadKeyWithImageMetadata(uploadKey).orElseThrow().toModelWithImageMetadata();
    }

    @Override
    @Transactional(readOnly = true)
    public Upload findVideoByKey(String uploadKey) {
        return uploadCrudRepository.getByUploadKeyWithVideoMetadata(uploadKey).orElseThrow().toModelWithVideoMetadata();
    }

    private static boolean isUploadKeyTaken(DataIntegrityViolationException exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sqlException
                    && SQLErrorCodes.UNIQUE_VIOLATION.equals(sqlException.getSQLState())
                    && sqlException.getMessage().contains("upload_key_ready_unique")) {
                return true;
            }
        }
        return false;
    }
}
