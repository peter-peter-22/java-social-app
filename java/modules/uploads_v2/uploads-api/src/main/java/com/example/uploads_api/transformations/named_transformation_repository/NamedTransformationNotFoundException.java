package com.example.uploads_api.transformations.named_transformation_repository;

public class NamedTransformationNotFoundException extends RuntimeException {
    public NamedTransformationNotFoundException(String message) {
        super(message);
    }
}
