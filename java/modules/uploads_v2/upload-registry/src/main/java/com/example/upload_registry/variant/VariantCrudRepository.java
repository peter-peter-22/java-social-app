package com.example.upload_registry.variant;

import com.example.uploads_api.variant.VariantStatus;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

@NullMarked
public interface VariantCrudRepository extends JpaRepository<VariantEntity, VariantEntityId> {

    @Modifying
    @Query("UPDATE VariantEntity v SET v.status = :status " +
           "WHERE v.id.uploadId = :uploadId AND v.id.namedTransformationId = :namedTransformationId")
    int updateStatus(UUID uploadId, UUID namedTransformationId, VariantStatus status);

    @EntityGraph(attributePaths = {"storedAsset", "transformationVersionEntity"})
    @Query("SELECT v FROM VariantEntity v " +
           "WHERE v.uploadEntity.uploadKey = :uploadKey " +
           "AND v.namedTransformationEntity.name = :transformationName " +
           "AND v.status = :status")
    Optional<VariantEntity> findByUploadKeyAndTransformationNameAndStatus(
            String uploadKey, String transformationName, VariantStatus status);

    @EntityGraph(attributePaths = {"storedAsset", "transformationVersionEntity", "uploadEntity"})
    @Query("SELECT v FROM VariantEntity v WHERE v.id = :id")
    Optional<VariantEntity> findByIdForUpdate(VariantEntityId id);
}
