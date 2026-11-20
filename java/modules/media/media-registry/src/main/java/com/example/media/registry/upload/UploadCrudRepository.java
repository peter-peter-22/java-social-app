package com.example.media.registry.upload;

import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

@NullMarked
public interface UploadCrudRepository extends JpaRepository<UploadEntity, UUID> {

    @EntityGraph(attributePaths = "asset")
    @Query("SELECT u FROM UploadEntity u WHERE u.uploadKey = :key")
    Optional<UploadEntity> findByUploadKeyWithAsset(String key);
}
