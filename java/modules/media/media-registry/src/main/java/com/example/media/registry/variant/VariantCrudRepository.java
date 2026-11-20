package com.example.media.registry.variant;

import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

@NullMarked
public interface VariantCrudRepository extends JpaRepository<VariantEntity, VariantEntityId> {

    @EntityGraph(attributePaths = {"asset", "transformationVersionEntity"})
    @Query("SELECT v FROM VariantEntity v " +
            "WHERE v.uploadEntity.uploadKey = :uploadKey " +
            "AND v.namedTransformationEntity.name = :transformationName ")
    Optional<VariantEntity> findByUploadKeyAndTransformationName(
            String uploadKey, String transformationName);
}
