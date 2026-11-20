package com.example.media.registry.transformation;

import com.example.media.contract.transformation.TransformationSpec;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface TransformationVersionCrudRepository extends CrudRepository<TransformationVersionEntity, UUID> {
    long countByNamedTransformationEntity_Name(String name);

    Optional<TransformationVersionEntity> findByNamedTransformationEntityAndOperations(
            NamedTransformationEntity namedTransformationEntity,
            TransformationSpec operations
    );
}
