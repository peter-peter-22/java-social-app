package com.example.upload_registry.variant;

import com.example.uploads_api.variant.VariantId;
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

    public VariantEntityId(UUID uploadId, UUID namedTransformationId) {
        this.uploadId = uploadId;
        this.namedTransformationId = namedTransformationId;
    }

    public VariantId toModel() {
        return VariantId.builder()
                .uploadId(uploadId)
                .namedTransformationId(namedTransformationId)
                .build();
    }
}
