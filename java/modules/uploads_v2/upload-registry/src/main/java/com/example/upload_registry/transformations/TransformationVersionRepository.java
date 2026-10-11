package com.example.upload_registry.transformations;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface TransformationVersionRepository extends CrudRepository<TransformationVersionEntity, UUID> {
}
