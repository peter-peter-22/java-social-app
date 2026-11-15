package com.example.upload_registry.stored_asset;

import com.example.cockroach_db.CockroachIntegrationTest;
import com.example.uploads_api.stored_asset.AssetDeliveryProtocol;
import com.example.uploads_api.stored_asset.ImageMetadata;
import com.example.uploads_api.stored_asset.InsertStoredAsset;
import com.example.uploads_api.stored_asset.StoredAsset;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class StoredAssetRepositoryImplIT extends CockroachIntegrationTest {
    @Autowired
    private StoredAssetRepositoryImpl storedAssetRepository;
    @Autowired
    private ImageMetadataCrudRepository imageMetadataCrudRepository;

    @Test
    void setImageMetadataInsertsRowAndDeleteCascadesIt() {
        var storedAsset = insertAsset();
        var metadata = ImageMetadata.builder().resolutionX(1920).resolutionY(1080).build();

        storedAssetRepository.setImageMetadata(storedAsset.getId(), metadata);

        var inserted = imageMetadataCrudRepository.findById(storedAsset.getId());
        assertThat(inserted).isPresent();
        assertThat(ImageMetadataEntity.toModel(inserted.orElseThrow())).isEqualTo(metadata);

        storedAssetRepository.deleteAll(List.of(storedAsset.getId()));

        assertThat(storedAssetRepository.findById(storedAsset.getId())).isNull();
        assertThat(imageMetadataCrudRepository.findById(storedAsset.getId())).isEmpty();
    }

    private StoredAsset insertAsset() {
        return storedAssetRepository.insert(InsertStoredAsset.builder()
                .homeRegion("eu-central-1")
                .objectBucket("uploads")
                .directory(false)
                .entryPoint("original.jpg")
                .contentType("image/jpeg")
                .extension("jpg")
                .bytes(42)
                .objects(List.of("original.jpg"))
                .deliveryProtocol(AssetDeliveryProtocol.IMAGE)
                .build());
    }
}
