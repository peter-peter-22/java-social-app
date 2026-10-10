package com.example.upload_registry.transformations;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;

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
    @JoinColumn(name = "namedTransformationId")
    private NamedTransformationEntity namedTransformationEntity;
    @Column(columnDefinition = "jsonb")
    private String parameters;
    @Column(nullable = false)
    @CreationTimestamp
    private Instant createdAt;
}