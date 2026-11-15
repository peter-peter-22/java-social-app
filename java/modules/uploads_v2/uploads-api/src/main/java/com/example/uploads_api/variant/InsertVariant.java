package com.example.uploads_api.variant;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@NullMarked
@Getter
@Builder
@Value
public class InsertVariant {
    VariantId id;
    UUID transformationVersionId;
    UUID storedAssetId;
}
