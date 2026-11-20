package com.example.media.registry.asset;

import com.example.cockroach_db.CockroachIntegrationTest;
import com.example.media.contract.asset.*;
import com.example.media.contract.port.AssetStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JpaAssetStoreIT extends CockroachIntegrationTest {
    @Autowired
    private AssetStore assetStore;
    @Autowired
    private ImageMetadataCrudRepository imageMetadataCrudRepository;

    @Test
    void registersTheAssetInUploadingStatus() {
        var id = AssetId.generate();

        var asset = assetStore.registerAsset(newAsset(id));

        assertThat(asset.id()).isEqualTo(id);
        assertThat(asset.status()).isEqualTo(AssetStatus.UPLOADING);
        assertThat(asset.placement()).isEqualTo(new AssetPlacement("eu-central-1", "uploads"));
        assertThat(asset.deliveryProtocol()).isEqualTo(AssetDeliveryProtocol.IMAGE);
        assertThat(asset.contentType()).isEqualTo("image/jpeg");
        assertThat(asset.entryPoint()).isEqualTo("original.jpg");
        assertThat(asset.extension()).isEqualTo("jpg");
        assertThat(asset.directory()).isFalse();
        assertThat(asset.bytes()).isEqualTo(42);
        assertThat(asset.objects()).containsExactly("original.jpg");
        assertThat(asset.image()).isNull();
        assertThat(asset.video()).isNull();
        assertThat(asset.createdAt()).isNotNull();
        assertThat(assetStore.findAsset(id)).isPresent();
    }

    @Test
    void setImageMetadataAttachesTheRecordAndDeletingTheAssetCascadesIt() {
        var id = AssetId.generate();
        assetStore.registerAsset(newAsset(id));
        var metadata = new ImageMetadata(1920, 1080);

        assetStore.setImageMetadata(id, metadata);

        var inserted = imageMetadataCrudRepository.findById(id.value());
        assertThat(inserted).isPresent();
        assertThat(inserted.orElseThrow().toModel()).isEqualTo(metadata);
        assertThat(assetStore.findAsset(id).orElseThrow().image()).isEqualTo(metadata);

        assetStore.deleteAssets(List.of(id));

        assertThat(assetStore.findAsset(id)).isEmpty();
        assertThat(imageMetadataCrudRepository.findById(id.value())).isEmpty();
    }

    @Test
    void setVideoMetadataAttachesTheRecord() {
        var id = AssetId.generate();
        assetStore.registerAsset(newAsset(id));
        var metadata = new VideoMetadata(1280, 720, 1_000);

        assetStore.setVideoMetadata(id, metadata);

        assertThat(assetStore.findAsset(id).orElseThrow().video()).isEqualTo(metadata);
    }

    @Test
    void setStatusMovesEveryGivenAssetToTheSameStatus() {
        var first = AssetId.generate();
        var second = AssetId.generate();
        assetStore.registerAsset(newAsset(first));
        assetStore.registerAsset(newAsset(second));

        assetStore.setStatus(List.of(first, second), AssetStatus.READY);

        assertThat(assetStore.findAsset(first).orElseThrow().status()).isEqualTo(AssetStatus.READY);
        assertThat(assetStore.findAsset(second).orElseThrow().status()).isEqualTo(AssetStatus.READY);
    }

    private NewAsset newAsset(AssetId id) {
        return new NewAsset(
                id,
                new AssetPlacement("eu-central-1", "uploads"),
                AssetDeliveryProtocol.IMAGE,
                "image/jpeg",
                "original.jpg",
                "jpg",
                false,
                42,
                List.of("original.jpg"));
    }
}
