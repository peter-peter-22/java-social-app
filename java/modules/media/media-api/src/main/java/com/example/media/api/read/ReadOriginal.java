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
 * The read-original use case: deliver the main file of a finalized upload.
 * The main asset is the incoming result when the upload requested one, the uploaded original
 * otherwise. A missing upload is an {@link UploadNotFoundException} (404).
 *
 * <p>A public upload is readable by anyone. A private upload requires a download credential bound to
 * the upload's current version; a missing, foreign, or expired credential is denied (403) by the
 * {@link DownloadTokenService}.</p>
 */
@Service
@RequiredArgsConstructor
@NullMarked
public class ReadOriginal {
    private final UploadRegistry uploadRegistry;
    private final AssetStore assetStore;
    private final ObjectStore objectStore;
    private final DownloadTokenService downloadTokenService;

    public StoredFile read(
            UploadKey key,
            @Nullable String version,
            @Nullable DownloadCredential credential
    ) {
        var upload = uploadRegistry.findUpload(key)
                .orElseThrow(() -> new UploadNotFoundException(
                        "could not find upload with key: " + key.value()));
        downloadTokenService.authorizeRead(upload, key, version, credential);
        var asset = assetStore.findAsset(upload.assetId())
                .orElseThrow(() -> new IllegalStateException(
                        "the asset of upload " + key.value() + " is missing"));
        return StoredFile.open(asset, objectStore, upload.visibility());
    }
}