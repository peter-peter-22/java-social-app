package com.example.upload_registry.stored_asset;

import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

@NullMarked
public interface StoredAssetCrudRepository extends JpaRepository<StoredAssetEntity, UUID> {
    // TODO decide if we really need the metadata here
    @EntityGraph(attributePaths = {"imageMetadata", "videoMetadata"})
    @Query("SELECT s FROM StoredAssetEntity s WHERE s.id = :id")
    Optional<StoredAssetEntity> findByIdWithMetadata(UUID id);
}
