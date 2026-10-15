package com.example.upload_registry.upload;

import com.example.cockroach_db.CockroachIntegrationTest;
import com.example.upload_registry.stored_asset.StoredAssetCrudRepository;
import com.example.upload_registry.stored_asset.StoredAssetEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class UploadCrudRepositoryIT extends CockroachIntegrationTest {
    @Autowired
    private UploadCrudRepository uploadRepository;
    @Autowired
    private ImageMetadataCrudRepository imageMetadataRepository;
    @Autowired
    private VideoMetadataCrudRepository videoMetadataRepository;
    @Autowired
    private StoredAssetCrudRepository storedAssetRepository;

    private UploadEntity insertUpload() {
        var storedAsset = storedAssetRepository.save(
                StoredAssetEntity.builder()
                        .homeRegion("eu-1")
                        .objectBucket("uploads")
                        .objectPath("my-object")
                        .directory(false)
                        .mimeType("image/jpeg")
                        .bytes(1000)
                        .build()
        );
        return uploadRepository.save(
                UploadEntity.builder()
                        .uploadKey("my_image" + UUID.randomUUID())
                        .storedAsset(storedAsset)
                        .build()
        );
    }

    @Test
    void theInsertedUploadShouldBeSaved() {
        var insertedUpload = insertUpload();
        var foundUpload = uploadRepository.getByUploadKey(insertedUpload.getUploadKey());
        assertThat(foundUpload.isPresent()).isTrue();
        assertThat(foundUpload.get().getUploadKey()).isEqualTo(insertedUpload.getUploadKey());
    }

    @Test
    void theInsertedImageMetadataShouldBeSaved() {
        var insertedUpload = insertUpload();
        var referenceAsset = storedAssetRepository.getReferenceById(insertedUpload.getStoredAsset().getId());
        var insertedMetadata = imageMetadataRepository.save(
                ImageMetadataEntity.builder()
                        .storedAsset(referenceAsset)
                        .resolutionX(1920)
                        .resolutionY(1080)
                        .build()
        );
        var foundUpload = uploadRepository.getByUploadKeyWithImageMetadata(insertedUpload.getUploadKey());

        assertThat(foundUpload.isPresent()).isTrue();
        assertThat(foundUpload.get().getStoredAsset().getImageMetadata().getAssetId())
                .isEqualTo(insertedMetadata.getAssetId());
    }

    @Test
    void theInsertedVideoMetadataShouldBeSaved() {
        var insertedUpload = insertUpload();
        var referenceAsset = storedAssetRepository.getReferenceById(insertedUpload.getStoredAsset().getId());
        var insertedMetadata = videoMetadataRepository.save(
                VideoMetadataEntity.builder()
                        .storedAsset(referenceAsset)
                        .resolutionX(1920)
                        .resolutionY(1080)
                        .durationSeconds(12.5)
                        .build()
        );
        var foundUpload = uploadRepository.getByUploadKeyWithVideoMetadata(insertedUpload.getUploadKey());

        assertThat(foundUpload).isPresent();
        assertThat(foundUpload.orElseThrow().getStoredAsset().getVideoMetadata().getAssetId())
                .isEqualTo(insertedMetadata.getAssetId());
    }

    @Test
    void findsUploadAndStoredAssetWithoutMetadata() {
        var insertedUpload = insertUpload();

        var foundUpload = uploadRepository.getByUploadKeyWithStoredAsset(insertedUpload.getUploadKey());

        assertThat(foundUpload).isPresent();
        assertThat(foundUpload.orElseThrow().getStoredAsset().getId())
                .isEqualTo(insertedUpload.getStoredAsset().getId());
    }
}
