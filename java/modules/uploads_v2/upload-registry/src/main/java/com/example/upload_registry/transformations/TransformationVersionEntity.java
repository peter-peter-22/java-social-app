package com.example.upload_registry.transformations;

import com.example.uploads_api.transformations.PolymorphicTransformationOperations;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transformation_versions")
@Getter
@NoArgsConstructor
@SuperBuilder
public final class TransformationVersionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "named_transformation_id")
    private NamedTransformationEntity namedTransformationEntity;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private PolymorphicTransformationOperations operations;
    @Column(nullable = false)
    @CreationTimestamp
    private Instant createdAt;
}