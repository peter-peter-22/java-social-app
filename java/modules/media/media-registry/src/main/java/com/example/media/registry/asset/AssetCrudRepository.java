package com.example.media.registry.asset;

import com.example.media.contract.asset.AssetStatus;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.UUID;

@NullMarked
public interface AssetCrudRepository extends JpaRepository<AssetEntity, UUID> {
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE AssetEntity a SET a.status = :status WHERE a.id IN :ids")
    int updateStatusAll(Collection<UUID> ids, AssetStatus status);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM AssetEntity a WHERE a.id IN :ids")
    void deleteAll(Collection<UUID> ids);
}
