package com.example.upload_registry.stored_asset;

import com.example.cockroach_db.CockroachIntegrationTest;
import com.example.uploads_api.stored_asset.ImageMetadata;
import com.example.uploads_api.stored_asset.InsertStoredAsset;
import com.example.uploads_api.stored_asset.VideoMetadata;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class StoredAssetRepositoryImplIT extends CockroachIntegrationTest {
    @Autowired
    private StoredAssetRepositoryImpl storedAssetRepository;
    @Autowired
    private ImageMetadataCrudRepository imageMetadataCrudRepository;
    @Autowired
    private VideoMetadataCrudRepository videoMetadataCrudRepository;

    @Test
    void managesStoredAssetAndMetadata() {
        // insert
        var inserted=insert();
        var assetId = storedAssetRepository.insert(inserted);
        var foundAsset = storedAssetRepository.findById(assetId);

        assertThat(foundAsset).isNotNull();
        assertThat(foundAsset.getId()).isEqualTo(assetId);
        assertThat(foundAsset.getCreatedAt()).isNotNull();
        assertThat(foundAsset).usingRecursiveComparison()
                .ignoringFields("id", "createdAt", "imageMetadata", "videoMetadata")
                .isEqualTo(inserted);

        // set metadata
        var imageMetadata = ImageMetadata.builder().resolutionX(1920).resolutionY(1080).build();
        var videoMetadata = VideoMetadata.builder().resolutionX(1920).resolutionY(1080).durationMilliseconds(1_000).build();
        storedAssetRepository.setImageMetadata(assetId, imageMetadata);
        storedAssetRepository.setVideoMetadata(assetId, videoMetadata);
        var foundAssetWithMetadata = storedAssetRepository.findById(assetId);

        assertThat(foundAssetWithMetadata).isNotNull();
        assertThat(foundAssetWithMetadata.getImageMetadata()).isEqualTo(imageMetadata);
        assertThat(foundAssetWithMetadata.getVideoMetadata()).isEqualTo(videoMetadata);

        // delete
        storedAssetRepository.delete(assetId);

        assertThat(storedAssetRepository.findById(assetId)).isNull();
        assertThat(imageMetadataCrudRepository.findById(assetId).isPresent()).isFalse();
        assertThat(videoMetadataCrudRepository.findById(assetId).isPresent()).isFalse();
    }

    private InsertStoredAsset insert() {
        // Here we intentionally use an impossible combination of single image file and video directory parameters to test of all parameters work.
        return InsertStoredAsset.builder()
                .homeRegion("eu-central-1")
                .objectBucket("uploads")
                .objectPath("images/" + UUID.randomUUID() + ".jpg")
                .directory(true)
                .entryPoint("master.m3u8")
                .mimeType("image/jpeg")
                .extension("jpg")
                .bytes(42)
                .deliveryProtocol("https")
                .build();
    }
}
