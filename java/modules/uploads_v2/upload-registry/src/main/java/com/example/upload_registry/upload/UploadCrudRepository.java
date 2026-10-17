package com.example.upload_registry.upload;

import com.example.uploads_api.uploads.UploadStatus;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

@NullMarked
interface UploadCrudRepository extends JpaRepository<UploadEntity, UUID> {
    @Modifying
    @Query("UPDATE UploadEntity u SET u.status = :status WHERE u.id = :uploadId")
    int updateStatus(UUID uploadId, UploadStatus status);

    @EntityGraph(attributePaths = "storedAsset")
    @Query("SELECT u FROM UploadEntity u WHERE u.uploadKey = :key")
    Optional<UploadEntity> getByUploadKeyWithStoredAsset(String key);

    @EntityGraph(attributePaths = {"storedAsset", "storedAsset.imageMetadata"})
    @Query("SELECT u FROM UploadEntity u WHERE u.uploadKey = :key")
    Optional<UploadEntity> getByUploadKeyWithImageMetadata(String key);

    @EntityGraph(attributePaths = {"storedAsset", "storedAsset.videoMetadata"})
    @Query("SELECT u FROM UploadEntity u WHERE u.uploadKey = :key")
    Optional<UploadEntity> getByUploadKeyWithVideoMetadata(String key);
}
