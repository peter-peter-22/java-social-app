package com.example.media.registry.transformation;

import com.example.media.contract.transformation.TransformationSpec;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * The {@code transformation_versions} table: one recorded spec of a named transformation.
 * The {@code operations} column is the jsonb shape locked by the contract's spec tests.
 */
@Getter
@Entity
@NoArgsConstructor
@SuperBuilder
@Table(name = "transformation_versions")
public final class TransformationVersionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "named_transformation_id")
    private NamedTransformationEntity namedTransformationEntity;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private TransformationSpec operations;
    @Column(nullable = false)
    @CreationTimestamp
    private Instant createdAt;
}
