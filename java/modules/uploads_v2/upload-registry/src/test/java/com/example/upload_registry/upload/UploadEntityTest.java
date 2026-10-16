package com.example.upload_registry.upload;

import com.example.upload_registry.stored_asset.StoredAssetEntity;
import com.example.uploads_api.uploads.UploadStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UploadEntityTest {

    @Test
    void storesTheStatusByNameAndMapsItToTheBusinessModel() throws NoSuchFieldException {
        var asset = StoredAssetEntity.builder()
                .id(UUID.randomUUID())
                .homeRegion("eu-central-1")
                .objectBucket("uploads")
                .objectPath("images/example.jpg")
                .directory(false)
                .entryPoint("index")
                .mimeType("image/jpeg")
                .extension("jpg")
                .bytes(42)
                .deliveryProtocol("https")
                .build();
        var entity = UploadEntity.builder()
                .id(UUID.randomUUID())
                .uploadKey("example")
                .status(UploadStatus.UPLOADED)
                .storedAsset(asset)
                .build();

        var status = UploadEntity.class.getDeclaredField("status");
        var upload = entity.toModel();

        assertThat(status.getAnnotation(Enumerated.class).value()).isEqualTo(EnumType.STRING);
        assertThat(upload.getStatus()).isEqualTo(UploadStatus.UPLOADED);
        assertThat(upload.getStoredAsset().getId()).isEqualTo(asset.getId());
    }
}
