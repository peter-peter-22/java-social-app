package com.example.media.variant;

import com.example.media.identity.NamedTransformationId;
import com.example.media.identity.UploadId;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;

/** Matches the current variant primary key: upload id plus named transformation id. */
@NullMarked
public record VariantId(UploadId uploadId, NamedTransformationId namedTransformationId) {
    public VariantId {
        Objects.requireNonNull(uploadId, "uploadId");
        Objects.requireNonNull(namedTransformationId, "namedTransformationId");
    }
}
