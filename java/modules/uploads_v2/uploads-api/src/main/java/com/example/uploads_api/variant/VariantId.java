package com.example.uploads_api.variant;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@Builder
@NullMarked
@Getter
@Value
public class VariantId {
    UUID uploadId;
    UUID namedTransformationId;
}
