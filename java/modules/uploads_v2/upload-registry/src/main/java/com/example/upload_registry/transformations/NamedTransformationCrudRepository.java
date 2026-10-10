package com.example.upload_registry.transformations;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface NamedTransformationCrudRepository extends CrudRepository<NamedTransformationEntity, UUID> {
    Optional<NamedTransformationEntity> findFirstByNameOrderByCreatedAtDesc(String name);
}
