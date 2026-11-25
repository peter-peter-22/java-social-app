package com.example.media.api.read;

import com.example.media.contract.port.AssetStore;
import com.example.media.contract.port.ObjectStore;
import com.example.media.contract.port.UploadRegistry;
import com.example.media.contract.upload.UploadKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * The read-variant use case: deliver one named transformed result of a finalized upload.
 * A missing variant is a {@link VariantNotFoundException} (404).
 */
@Service
@RequiredArgsConstructor
public class ReadVariant {
    private final UploadRegistry uploadRegistry;
    private final AssetStore assetStore;
    private final ObjectStore objectStore;

    public StoredFile read(UploadKey key, String transformationName) {
        var variant = uploadRegistry.findVariant(key, transformationName)
                .orElseThrow(() -> new VariantNotFoundException(
                        "could not find variant " + transformationName + " of upload " + key.value()));
        var asset = assetStore.findAsset(variant.assetId())
                .orElseThrow(() -> new IllegalStateException(
                        "the asset of variant " + transformationName + " of upload " + key.value() + " is missing"));
        return StoredFile.open(asset, objectStore);
    }
}