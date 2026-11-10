package com.example.uploads_api.transformations.named_transformation_repository;

import org.jspecify.annotations.NullMarked;

import java.util.Collection;
import java.util.List;

@NullMarked
public interface NamedTransformationRepository {
    NamedImageTransformation get(String name) throws NamedTransformationNotFoundException;
    List<NamedImageTransformation> getAll(Collection<String> name) throws NamedTransformationNotFoundException;
    void set(InsertNamedImageTransformation value);
}
