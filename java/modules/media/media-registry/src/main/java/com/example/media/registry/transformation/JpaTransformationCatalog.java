package com.example.media.registry.transformation;

import com.example.media.contract.port.TransformationCatalog;
import com.example.media.contract.transformation.NamedTransformation;
import com.example.media.contract.transformation.NewNamedTransformation;
import com.example.media.contract.transformation.TransformationNotFoundException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The named transformation catalog.
 * Resolving returns the current version; produced variants pin that version, so later registrations
 * never change existing results.
 */
@Repository
@RequiredArgsConstructor
@NullMarked
public class JpaTransformationCatalog implements TransformationCatalog {
    private final NamedTransformationCrudRepository namedTransformationCrudRepository;
    private final TransformationVersionCrudRepository transformationVersionCrudRepository;

    @Override
    @Transactional(readOnly = true)
    public NamedTransformation resolve(String name) throws TransformationNotFoundException {
        return namedTransformationCrudRepository.findByName(name)
                .orElseThrow(() -> new TransformationNotFoundException(
                        "could not find named transformation with name: " + name))
                .toModel();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NamedTransformation> resolveAll(Collection<String> names) throws TransformationNotFoundException {
        var found = namedTransformationCrudRepository.findAllByNameIn(names).stream()
                .collect(Collectors.toMap(NamedTransformationEntity::getName, Function.identity()));
        var missing = names.stream().filter(name -> !found.containsKey(name)).toList();
        if (!missing.isEmpty()) {
            throw new TransformationNotFoundException("could not find named transformations: " + missing);
        }
        return names.stream().map(name -> found.get(name).toModel()).toList();
    }

    @Override
    @Transactional
    public NamedTransformation register(NewNamedTransformation insert) {
        var named = namedTransformationCrudRepository.findByName(insert.name())
                .orElseGet(() -> namedTransformationCrudRepository.save(NamedTransformationEntity.builder()
                        .name(insert.name())
                        .build()));
        // A spec identical to a recorded version is reused instead of duplicated.
        var version = transformationVersionCrudRepository
                .findByNamedTransformationEntityAndOperations(named, insert.spec())
                .orElseGet(() -> transformationVersionCrudRepository.save(TransformationVersionEntity.builder()
                        .namedTransformationEntity(named)
                        .operations(insert.spec())
                        .build()));
        named.setCurrentVersionEntity(version);
        namedTransformationCrudRepository.save(named);
        return named.toModel();
    }
}
