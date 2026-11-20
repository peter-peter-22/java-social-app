package com.example.media.contract.variant;

import com.example.media.contract.transformation.NamedTransformationId;
import com.example.media.contract.upload.UploadId;

import java.util.Objects;

/**
 * The identity of a variant: one named transformation of one upload.
 * Matches the registry's composite primary key.
 */
public record VariantId(UploadId uploadId, NamedTransformationId namedTransformationId) {
    public VariantId {
        Objects.requireNonNull(uploadId, "uploadId");
        Objects.requireNonNull(namedTransformationId, "namedTransformationId");
    }
}