package com.example.upload_registry.variant;

import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

@NullMarked
public interface VariantCrudRepository extends JpaRepository<VariantEntity, VariantEntityId> {

    @EntityGraph(attributePaths = {"storedAsset", "transformationVersionEntity"})
    @Query("SELECT v FROM VariantEntity v " +
           "WHERE v.uploadEntity.uploadKey = :uploadKey " +
           "AND v.namedTransformationEntity.name = :transformationName ")
    Optional<VariantEntity> findByUploadKeyAndTransformationNameAndStatus(
            String uploadKey, String transformationName);

    @EntityGraph(attributePaths = {"storedAsset", "transformationVersionEntity", "uploadEntity"})
    @Query("SELECT v FROM VariantEntity v WHERE v.id = :id")
    Optional<VariantEntity> findByIdForUpdate(VariantEntityId id);
}
