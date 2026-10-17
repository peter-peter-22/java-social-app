package com.example.uploads_api.variant;

public interface VariantRepository {
    VariantId save(InsertVariant upload);

    void updateStatus(VariantId variantId, VariantStatus status);

    /** Find the lastest variant created by the specified named transformation. */
    Variant findByTransformationName(String uploadKey, String transformationName);
}
