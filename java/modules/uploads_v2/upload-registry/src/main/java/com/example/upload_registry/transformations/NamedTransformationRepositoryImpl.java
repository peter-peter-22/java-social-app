package com.example.upload_registry.transformations;

import com.example.uploads_api.transformations.ImageTransformationOperations;
import com.example.uploads_api.transformations.named_transformation_repository.InsertNamedImageTransformation;
import com.example.uploads_api.transformations.named_transformation_repository.NamedImageTransformation;
import com.example.uploads_api.transformations.named_transformation_repository.NamedTransformationNotFoundException;
import com.example.uploads_api.transformations.named_transformation_repository.NamedTransformationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@NullMarked
public class NamedTransformationRepositoryImpl implements NamedTransformationRepository {
    private final NamedTransformationCrudRepository namedTransformationCrudRepository;
    private final TransformationVersionRepository transformationVersionRepository;

    // Get a named transformation and its operations if it exists, otherwise throw
    @Override
    public NamedImageTransformation get(String name) throws NamedTransformationNotFoundException {
        var named = namedTransformationCrudRepository.findByName(name)
                .orElseThrow(() -> new NamedTransformationNotFoundException("Could not find named transformation with name: " + name));

        var version = named.getCurrentVersionEntity();
        if (version == null) throw new RuntimeException("Could not find current version entity");

        return toImageTransformation(named);
    }

    @Override
    @Transactional
    public List<NamedImageTransformation> getAll(Collection<String> names) throws NamedTransformationNotFoundException {
        var found = namedTransformationCrudRepository.findAllByNameIn(names).stream()
                .collect(Collectors.toMap(NamedTransformationEntity::getName, Function.identity()));
        var missing = names.stream().filter(name -> !found.containsKey(name)).toList();
        if (!missing.isEmpty()) {
            throw new NamedTransformationNotFoundException("Could not find named transformations: " + missing);
        }
        return names.stream().map(name -> toImageTransformation(found.get(name))).toList();
    }

    private static NamedImageTransformation toImageTransformation(NamedTransformationEntity named) {
        var version = named.getCurrentVersionEntity();
        if (version == null) throw new RuntimeException("Could not find current version entity");

        if (version.getOperations() instanceof ImageTransformationOperations imageOperations) {
            return NamedImageTransformation.builder()
                    .id(named.getId())
                    .name(named.getName())
                    .operations(imageOperations)
                    .currentVersionId(version.getId())
                    .build();
        }
        throw new RuntimeException("This is not an image transformation");
    }

    @Override
    @Transactional
    public void set(InsertNamedImageTransformation insert) {
        var named = namedTransformationCrudRepository.findByName(insert.name())
                .orElseGet(() -> namedTransformationCrudRepository.save(
                        NamedTransformationEntity.builder()
                                .name(insert.name())
                                .build()
                ));
        var version = transformationVersionRepository
                .findByNamedTransformationEntityAndOperations(named, insert.operations())
                .orElseGet(() -> transformationVersionRepository.save(
                        TransformationVersionEntity.builder()
                                .namedTransformationEntity(named)
                                .operations(insert.operations())
                                .build()
                ));
        named.setCurrentVersionEntity(version);
        namedTransformationCrudRepository.save(named);
    }
}
