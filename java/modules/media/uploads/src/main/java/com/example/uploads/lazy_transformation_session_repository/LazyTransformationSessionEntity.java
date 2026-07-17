package com.example.uploads.lazy_transformation_session_repository;

import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("lazy_transformation_sessions")
record LazyTransformationSessionEntity(
        UUID uploadId,
        Instant createdAt
) {
}
