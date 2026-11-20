package com.example.media.registry.variant;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.UUID;

/**
 * Matches the composite primary key of the {@code variant} table.
 */
@Embeddable
@Getter
@NoArgsConstructor
@SuperBuilder
public final class VariantEntityId implements Serializable {
    private UUID uploadId;
    private UUID namedTransformationId;

    public VariantEntityId(UUID uploadId, UUID namedTransformationId) {
        this.uploadId = uploadId;
        this.namedTransformationId = namedTransformationId;
    }
}
