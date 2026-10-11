package com.example.upload_registry.transformations;

import com.example.uploads_api.transformations.operations.PolymorphicTransformationOperations;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface TransformationVersionRepository extends CrudRepository<TransformationVersionEntity, UUID> {
    long countByNamedTransformationEntity_Name(String name);
    Optional<TransformationVersionEntity> findByNamedTransformationEntityAndOperations(
            NamedTransformationEntity namedTransformationEntity,
            PolymorphicTransformationOperations operations
    );
}
