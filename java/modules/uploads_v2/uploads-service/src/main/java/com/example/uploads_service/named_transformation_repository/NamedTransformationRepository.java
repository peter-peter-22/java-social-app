package com.example.uploads_service.named_transformation_repository;

import com.example.uploads_service.api.transformations.NamedImageTransformation;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
@NullMarked
@RequiredArgsConstructor
public class NamedTransformationRepository {
    public List<NamedImageTransformation> findByName(Collection<String> names) throws NamedTransformationNotFoundException {
        return List.of();
    }

    public NamedImageTransformation insert(InsertNamedImageTransformation insert) {
        throw new RuntimeException(".");
    }
}
