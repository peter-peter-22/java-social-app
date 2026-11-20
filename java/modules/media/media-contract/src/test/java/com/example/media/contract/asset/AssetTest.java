package com.example.media.contract.asset;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssetTest {

    private static final AssetPlacement PLACEMENT = new AssetPlacement("eu-west", "uploads");

    private static NewAsset newAsset(AssetId id, List<String> objects, String entryPoint) {
        return new NewAsset(
                id,
                PLACEMENT,
                AssetDeliveryProtocol.IMAGE,
                "image/jpeg",
                entryPoint,
                "jpg",
                false,
                10,
                objects
        );
    }

    @Test
    void copiesTheObjectList() {
        var objects = new ArrayList<>(List.of("original.jpg"));
        var asset = new Asset(
                AssetId.generate(), PLACEMENT, AssetStatus.UPLOADING, AssetDeliveryProtocol.IMAGE,
                "image/jpeg", "original.jpg", "jpg", false, 10, objects, null, null, Instant.now());

        objects.add("smuggled.jpg");

        assertThat(asset.objects()).containsExactly("original.jpg");
    }

    @Test
    void rejectsAnEntrypointOutsideTheRegisteredObjects() {
        assertThatThrownBy(() -> newAsset(AssetId.generate(), List.of("a.jpg"), "b.jpg"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsAnEmptyObjectList() {
        assertThatThrownBy(() -> newAsset(AssetId.generate(), List.of(), "a.jpg"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNegativeBytes() {
        assertThatThrownBy(() -> new NewAsset(
                AssetId.generate(), PLACEMENT, AssetDeliveryProtocol.IMAGE,
                "image/jpeg", "original.jpg", "jpg", false, -1, List.of("original.jpg")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void prefixUsesTheAssetId() {
        var id = AssetId.generate();
        var asset = new Asset(
                id, PLACEMENT, AssetStatus.UPLOADING, AssetDeliveryProtocol.IMAGE,
                "image/jpeg", "original.jpg", "jpg", false, 10, List.of("original.jpg"),
                new ImageMetadata(100, 50), null, Instant.now());

        assertThat(asset.prefix().resolve(asset.entryPoint()).key())
                .isEqualTo(id.value() + "/original_jpg");
    }
}