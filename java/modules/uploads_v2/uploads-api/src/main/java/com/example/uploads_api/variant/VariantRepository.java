package com.example.uploads_api.variant;

public interface VariantRepository {
    /** Insert if not exists, throw otherwise. */
    VariantId insert(InsertVariant insert);

    /**  */
    Variant findByUploadKeyAndTransformationName(String uploadKey, String transformationName);

    /** Deletes variant row and marks the assets for deletion. */
    void delete(VariantId variantId);
}
