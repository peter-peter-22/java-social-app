package com.example.media.registry.transformation;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NamedTransformationCrudRepository extends CrudRepository<NamedTransformationEntity, UUID> {
    @EntityGraph(attributePaths = "currentVersionEntity")
    Optional<NamedTransformationEntity> findByName(String name);

    @EntityGraph(attributePaths = "currentVersionEntity")
    List<NamedTransformationEntity> findAllByNameIn(Collection<String> names);
}
