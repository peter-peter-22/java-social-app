package com.example.upload_registry.stored_asset;

import com.example.uploads_api.stored_asset.StoredAssetStatus;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@NullMarked
public interface StoredAssetCrudRepository extends JpaRepository<StoredAssetEntity, UUID> {
    // TODO decide if we really need the metadata here
    @EntityGraph(attributePaths = {"imageMetadata", "videoMetadata"})
    @Query("SELECT s FROM StoredAssetEntity s WHERE s.id = :id")
    Optional<StoredAssetEntity> findByIdWithMetadata(UUID id);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE StoredAssetEntity s SET s.status = :status WHERE s.id IN :ids")
    int updateStatusAll(Collection<UUID> ids, StoredAssetStatus status);
}
