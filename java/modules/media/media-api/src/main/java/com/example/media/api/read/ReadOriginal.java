package com.example.media.api.read;

import com.example.media.api.upload.UploadNotFoundException;
import com.example.media.contract.port.AssetStore;
import com.example.media.contract.port.ObjectStore;
import com.example.media.contract.port.UploadRegistry;
import com.example.media.contract.upload.UploadKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * The read-original use case: deliver the main file of a finalized upload.
 * The main asset is the incoming result when the upload requested one, the uploaded original
 * otherwise. A missing upload is an {@link UploadNotFoundException} (404).
 */
@Service
@RequiredArgsConstructor
public class ReadOriginal {
    private final UploadRegistry uploadRegistry;
    private final AssetStore assetStore;
    private final ObjectStore objectStore;

    public StoredFile read(UploadKey key) {
        var upload = uploadRegistry.findUpload(key)
                .orElseThrow(() -> new UploadNotFoundException(
                        "could not find upload with key: " + key.value()));
        var asset = assetStore.findAsset(upload.assetId())
                .orElseThrow(() -> new IllegalStateException(
                        "the asset of upload " + key.value() + " is missing"));
        return StoredFile.open(asset, objectStore);
    }
}