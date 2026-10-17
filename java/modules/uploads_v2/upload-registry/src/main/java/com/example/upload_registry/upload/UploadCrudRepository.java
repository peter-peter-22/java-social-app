package com.example.upload_registry.upload;

import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

@NullMarked
interface UploadCrudRepository extends JpaRepository<UploadEntity, UUID> {
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
