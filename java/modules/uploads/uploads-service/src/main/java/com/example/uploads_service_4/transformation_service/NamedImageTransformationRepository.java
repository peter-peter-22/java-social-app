package com.example.uploads_service_4.transformation_service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
@RequiredArgsConstructor
public class NamedImageTransformationRepository {
    public Collection<NamedImageTransformation> getByName(Collection<String> names) {
        return null;
    }
}
