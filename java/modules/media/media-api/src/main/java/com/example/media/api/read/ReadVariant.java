package com.example.media.api.read;

import com.example.media.api.security.DownloadCredential;
import com.example.media.api.security.DownloadTokenService;
import com.example.media.api.upload.UploadNotFoundException;
import com.example.media.contract.port.AssetStore;
import com.example.media.contract.port.ObjectStore;
import com.example.media.contract.port.UploadRegistry;
import com.example.media.contract.upload.UploadKey;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

/**
 * The read-variant use case: deliver one named transformed result of a finalized upload.
 * A missing variant is a {@link VariantNotFoundException} (404).
 *
 * <p>Like the original read, a public upload is readable by anyone and a private upload requires a
 * download credential bound to the upload's current version. The upload is loaded first so the same
 * visibility and version rules guard every variant of the key.</p>
 */
@Service
@RequiredArgsConstructor
@NullMarked
public class ReadVariant {
    private final UploadRegistry uploadRegistry;
    private final AssetStore assetStore;
    private final ObjectStore objectStore;
    private final DownloadTokenService downloadTokenService;

    public StoredFile read(
            UploadKey key,
            String transformationName,
            @Nullable String version,
            @Nullable DownloadCredential credential
    ) {
        var upload = uploadRegistry.findUpload(key)
                .orElseThrow(() -> new UploadNotFoundException(
                        "could not find upload with key: " + key.value()));
        downloadTokenService.authorizeRead(upload, key, version, credential);
        var variant = uploadRegistry.findVariant(key, transformationName)
                .orElseThrow(() -> new VariantNotFoundException(
                        "could not find variant " + transformationName + " of upload " + key.value()));
        var asset = assetStore.findAsset(variant.assetId())
                .orElseThrow(() -> new IllegalStateException(
                        "the asset of variant " + transformationName + " of upload " + key.value() + " is missing"));
        return StoredFile.open(asset, objectStore, upload.visibility());
    }
}