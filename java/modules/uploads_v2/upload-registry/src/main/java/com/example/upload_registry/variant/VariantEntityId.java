package com.example.upload_registry.variant;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@NoArgsConstructor
@SuperBuilder
public final class VariantEntityId implements Serializable {
    private UUID uploadId;
    private UUID namedTransformationId;
}
