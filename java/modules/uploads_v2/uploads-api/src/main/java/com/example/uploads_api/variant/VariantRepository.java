package com.example.uploads_api.variant;

import java.util.Optional;

public interface VariantRepository {
    /** Saves or overwrites (upsert by PK). Returns the stable VariantId. */
    VariantId save(InsertVariant insert);

    void updateStatus(VariantId variantId, VariantStatus status);

    /** Returns only READY variants. Throws if not found (404 semantics). */
    Variant findByUploadKeyAndTransformationName(String uploadKey, String transformationName);

    /** For locking during retries/transform processing (pessimistic). */
    Optional<Variant> findForUpdate(VariantId variantId);

    /** Deletes variant row (cascades to StoredAsset). Caller must delete objects from storage. */
    void delete(VariantId variantId);
}
