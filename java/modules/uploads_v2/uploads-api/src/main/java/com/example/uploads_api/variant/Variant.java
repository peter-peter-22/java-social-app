package com.example.uploads_api.variant;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jspecify.annotations.NullMarked;

import java.time.Instant;
import java.util.UUID;

@NullMarked
@Getter
@Builder
@EqualsAndHashCode
public class Variant {
    private final VariantId id;
    private final UUID transformationVersionId;
    private final Instant createdAt;
    private final VariantStatus status;
    private final UUID storedAssetId;
}
