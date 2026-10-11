package com.example.upload_registry.transformations;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface NamedTransformationCrudRepository extends CrudRepository<NamedTransformationEntity, UUID> {
    @EntityGraph(attributePaths = "currentVersionEntity")
    Optional<NamedTransformationEntity> findByName(String name);
}
