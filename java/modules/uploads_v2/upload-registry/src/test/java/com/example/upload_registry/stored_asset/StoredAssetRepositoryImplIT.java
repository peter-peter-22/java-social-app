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

    @Test
    void managesStoredAssetAndMetadata() {
        var asset = storedAssetRepository.insert(insert());
        var imageMetadata = ImageMetadata.builder().resolutionX(1920).resolutionY(1080).build();
        var videoMetadata = VideoMetadata.builder()
                .resolutionX(1920)
                .resolutionY(1080)
                .durationMilliseconds(1_000)
                .build();

        assertThat(storedAssetRepository.findById(asset.getId())).isEqualTo(asset);

        storedAssetRepository.setImageMetadata(asset.getId(), imageMetadata);
        storedAssetRepository.setVideoMetadata(asset.getId(), videoMetadata);

        var foundAsset = storedAssetRepository.findById(asset.getId());
        assertThat(foundAsset).isNotNull();
        assertThat(foundAsset.getImageMetadata()).isEqualTo(imageMetadata);
        assertThat(foundAsset.getVideoMetadata()).isEqualTo(videoMetadata);

        storedAssetRepository.delete(asset.getId());

        assertThat(storedAssetRepository.findById(asset.getId())).isNull();
    }

    private InsertStoredAsset insert() {
        return InsertStoredAsset.builder()
                .homeRegion("eu-central-1")
                .objectBucket("uploads")
                .objectPath("images/" + UUID.randomUUID() + ".jpg")
                .directory(false)
                .entryPoint("index")
                .mimeType("image/jpeg")
                .extension("jpg")
                .bytes(42)
                .deliveryProtocol("https")
                .build();
    }
}
