package com.example.upload_registry.transformations;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "named_transformations")
@Getter
@NoArgsConstructor
@SuperBuilder
public final class NamedTransformationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    @CreationTimestamp
    private Instant createdAt;
    @Column(nullable = false)
    @UpdateTimestamp
    private Instant lastUpdatedAt;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_version_id")
    @Setter
    private TransformationVersionEntity currentVersionEntity;
}