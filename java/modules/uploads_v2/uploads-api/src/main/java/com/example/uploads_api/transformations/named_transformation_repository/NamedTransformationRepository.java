package com.example.uploads_api.transformations.named_transformation_repository;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface NamedTransformationRepository {
    NamedImageTransformation get(String name) throws NamedTransformationNotFoundException;
    void set(InsertNamedImageTransformation value);
}
