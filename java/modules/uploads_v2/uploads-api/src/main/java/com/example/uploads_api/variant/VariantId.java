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
}
