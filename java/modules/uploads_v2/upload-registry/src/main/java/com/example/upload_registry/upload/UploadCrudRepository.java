package com.example.upload_registry.upload;

import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

@NullMarked
interface UploadCrudRepository extends JpaRepository<UploadEntity, UUID> {
    Optional<UploadEntity> getByUploadKey(String key);

    @EntityGraph(attributePaths = "imageMetadata")
    @Query("SELECT u FROM UploadEntity u WHERE u.uploadKey = :key")
    Optional<UploadEntity> getByUploadKeyWithImageMetadata(String key);
}