package com.example.uploads.lazy_transformation_session_repository;

import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("lazy_transformation_sessions")
record LazyTransformationSessionEntity(
        String uploadId,
        Instant createdAt
) {
}
