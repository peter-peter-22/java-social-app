package com.example.media.api.read;

import com.example.media.api.security.DownloadDeniedException;
import com.example.media.api.security.DownloadTokenService;
import com.example.media.api.upload.UploadNotFoundException;
import com.example.media.contract.asset.Asset;
import com.example.media.contract.asset.AssetDeliveryProtocol;
import com.example.media.contract.asset.AssetId;
import com.example.media.contract.asset.AssetPlacement;
import com.example.media.contract.asset.AssetStatus;
import com.example.media.contract.object.ObjectRef;
import com.example.media.contract.port.AssetStore;
import com.example.media.contract.port.ObjectStore;
import com.example.media.contract.port.UploadRegistry;
import com.example.media.contract.upload.Upload;
import com.example.media.contract.upload.UploadId;
import com.example.media.contract.upload.UploadKey;
import com.example.media.contract.upload.UploadVisibility;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReadOriginalTest {

    private final UploadRegistry uploadRegistry = mock();
    private final AssetStore assetStore = mock();
    private final ObjectStore objectStore = mock();
    private final DownloadTokenService downloadTokenService = mock();
    private final ReadOriginal useCase =
            new ReadOriginal(uploadRegistry, assetStore, objectStore, downloadTokenService);

    private static final UploadKey KEY = new UploadKey("cover");
    private static final AssetId ASSET_ID = AssetId.generate();

    private final Asset asset = new Asset(ASSET_ID, new AssetPlacement("eu-west", "uploads"),
            AssetStatus.READY, AssetDeliveryProtocol.IMAGE, "image/webp", "original.webp", "webp",
            false, 3, List.of("original.webp"), null, null, Instant.now());

    private final Upload upload = new Upload(new UploadId(UUID.randomUUID()), KEY, ASSET_ID,
            UploadVisibility.PUBLIC, Instant.now());

    @Test
    void deliversTheMainFileOfTheUpload() {
        when(uploadRegistry.findUpload(KEY)).thenReturn(Optional.of(upload));
        when(assetStore.findAsset(ASSET_ID)).thenReturn(Optional.of(asset));
        var content = new ByteArrayInputStream(new byte[3]);
        when(objectStore.get(any())).thenReturn(content);

        var file = useCase.read(KEY, null, null);

        assertThat(file.contentType()).isEqualTo("image/webp");
        assertThat(file.fileName()).isEqualTo("original.webp");
        assertThat(file.content()).isSameAs(content);
        assertThat(file.visibility()).isEqualTo(UploadVisibility.PUBLIC);
        verify(objectStore).get(new ObjectRef("eu-west", "uploads", ASSET_ID.value() + "/original_webp"));
        // The read consults the download authorizer before serving.
        verify(downloadTokenService).authorizeRead(upload, KEY, null, null);
    }

    @Test
    void missingUploadIsNotFound() {
        when(uploadRegistry.findUpload(KEY)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.read(KEY, null, null)).isInstanceOf(UploadNotFoundException.class);
    }

    @Test
    void aDeniedReadPropagates() {
        when(uploadRegistry.findUpload(KEY)).thenReturn(Optional.of(
                new Upload(upload.id(), KEY, ASSET_ID, UploadVisibility.PRIVATE, upload.createdAt())));
        doThrow(new DownloadDeniedException("denied"))
                .when(downloadTokenService).authorizeRead(any(), any(), any(), any());

        assertThatThrownBy(() -> useCase.read(KEY, "123", null)).isInstanceOf(DownloadDeniedException.class);
    }
}
