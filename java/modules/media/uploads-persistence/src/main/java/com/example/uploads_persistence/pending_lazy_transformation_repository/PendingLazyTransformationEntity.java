package com.example.uploads_persistence.pending_lazy_transformation_repository;

import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("pending_lazy_transformations")
record PendingLazyTransformationEntity(
        UUID sessionId,
        String transformationName
) {
}
