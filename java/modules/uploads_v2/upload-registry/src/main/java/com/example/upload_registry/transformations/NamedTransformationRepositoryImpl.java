package com.example.upload_registry.transformations;

import com.example.uploads_api.transformations.named_transformation_repository.InsertNamedImageTransformation;
import com.example.uploads_api.transformations.named_transformation_repository.NamedImageTransformation;
import com.example.uploads_api.transformations.named_transformation_repository.NamedTransformationNotFoundException;
import com.example.uploads_api.transformations.named_transformation_repository.NamedTransformationRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@NullMarked
public class NamedTransformationRepositoryImpl implements NamedTransformationRepository {
    private final NamedTransformationCrudRepository crud;


    @Override
    public NamedImageTransformation findByName(String name) throws NamedTransformationNotFoundException {
        throw new NamedTransformationNotFoundException(name);
    }

    @Override
    public void set(InsertNamedImageTransformation value) {

    }
}
