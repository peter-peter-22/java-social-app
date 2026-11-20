package com.example.media;

import com.example.media.asset.Asset;
import com.example.media.asset.AssetLocation;
import com.example.media.asset.AssetStatus;
import com.example.media.asset.DeliveryProtocol;
import com.example.media.identity.AssetId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssetLocationTest {
    private static final UUID ASSET_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @Test
    void objectKeyUsesTheAssetIdAndRewritesDots() {
        var location = new AssetLocation("home", "uploads", new AssetId(ASSET_UUID), List.of("original.jpg", "nested/file.webp"));

        assertThat(location.objectKey("original.jpg")).isEqualTo(ASSET_UUID + "/original_jpg");
        assertThat(location.objectKey("nested/file.webp")).isEqualTo(ASSET_UUID + "/nested/file_webp");
        assertThat(location.objectKeys()).containsExactly(ASSET_UUID + "/original_jpg", ASSET_UUID + "/nested/file_webp");
    }

    @Test
    void locationRejectsUnregisteredAndExternalMutation() {
        var paths = new ArrayList<String>();
        paths.add("original.jpg");
        var location = new AssetLocation("home", "uploads", new AssetId(ASSET_UUID), paths);
        paths.add("other.jpg");

        assertThat(location.relativePaths()).containsExactly("original.jpg");
        assertThatThrownBy(() -> location.objectKey("other.jpg")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AssetLocation(" ", "uploads", new AssetId(ASSET_UUID), List.of("original.jpg")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void assetRequiresItsEntryPointAndASingleObjectUnlessItIsADirectory() {
        assertThatThrownBy(() -> asset("missing.jpg", false, List.of("original.jpg")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> asset("original.jpg", false, List.of("original.jpg", "extra.jpg")))
                .isInstanceOf(IllegalArgumentException.class);

        var directory = asset("index.m3u8", true, List.of("index.m3u8", "segment.ts"));
        assertThat(directory.location().objectKey("segment.ts")).isEqualTo(ASSET_UUID + "/segment_ts");
    }

    private static Asset asset(String entryPoint, boolean directory, List<String> objects) {
        return new Asset(
                new AssetId(ASSET_UUID),
                "home",
                "uploads",
                directory,
                entryPoint,
                "jpg",
                "image/jpeg",
                Instant.parse("2026-01-01T00:00:00Z"),
                10,
                DeliveryProtocol.IMAGE,
                AssetStatus.UPLOADING,
                objects,
                null,
                null
        );
    }
}
