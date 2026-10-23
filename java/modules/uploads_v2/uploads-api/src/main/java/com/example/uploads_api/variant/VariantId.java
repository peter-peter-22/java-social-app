package com.example.uploads_api.variant;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@Builder
@NullMarked
@EqualsAndHashCode
@Getter
public class VariantId {
    private final UUID uploadId;
    private final UUID namedTransformationId;

    public VariantId(UUID uploadId, UUID namedTransformationId) {
        this.uploadId = uploadId;
        this.namedTransformationId = namedTransformationId;
    }
}
