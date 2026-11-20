package com.example.media.registry.transformation;

import com.example.media.contract.transformation.NamedTransformation;
import com.example.media.contract.transformation.NamedTransformationId;
import com.example.media.contract.transformation.TransformationVersionId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * The {@code named_transformations} table: the stable name of a transformation.
 */
@Getter
@Entity
@NoArgsConstructor
@SuperBuilder
@Table(name = "named_transformations")
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

    public NamedTransformation toModel() {
        var version = currentVersionEntity;
        if (version == null) {
            throw new IllegalStateException("the named transformation " + name + " has no current version");
        }
        return new NamedTransformation(
                new NamedTransformationId(id),
                name,
                version.getOperations(),
                new TransformationVersionId(version.getId())
        );
    }
}
