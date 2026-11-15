package com.example.upload_registry.stored_asset;

import com.example.uploads_api.stored_asset.StoredAssetStatus;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.UUID;

@NullMarked
public interface StoredAssetCrudRepository extends JpaRepository<StoredAssetEntity, UUID> {
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE StoredAssetEntity s SET s.status = :status WHERE s.id IN :ids")
    int updateStatusAll(Collection<UUID> ids, StoredAssetStatus status);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM StoredAssetEntity s WHERE s.id IN :ids")
    void deleteAll(Collection<UUID> ids);
}
