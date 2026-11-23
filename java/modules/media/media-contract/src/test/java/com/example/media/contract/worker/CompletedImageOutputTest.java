package com.example.media.contract.worker;

import com.example.media.contract.asset.AssetId;
import com.example.media.contract.asset.ImageMetadata;
import com.example.media.contract.object.ObjectPrefix;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CompletedImageOutputTest {

    private static final AssetId ID = AssetId.generate();
    private static final ImageMetadata METADATA = new ImageMetadata(10, 10);

    @Test
    void acceptsAPrefixThatIsTheAssetId() {
        var output = new CompletedImageOutput(
                ID,
                new ObjectPrefix("eu-west", "uploads", ID.value().toString()),
                "default.webp",
                List.of("default.webp"),
                1,
                "image/webp",
                METADATA);

        assertThatCode(() -> output.objects()).doesNotThrowAnyException();
    }

    @Test
    void rejectsAPrefixOfAnotherAsset() {
        assertThatThrownBy(() -> new CompletedImageOutput(
                ID,
                new ObjectPrefix("eu-west", "uploads", AssetId.generate().value().toString()),
                "default.webp",
                List.of("default.webp"),
                1,
                "image/webp",
                METADATA))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsObjectPathsThatEscapeTheAssetPrefix() {
        var prefix = new ObjectPrefix("eu-west", "uploads", ID.value().toString());

        assertThatThrownBy(() -> new CompletedImageOutput(
                ID, prefix, "/absolute.webp", List.of("/absolute.webp"), 1, "image/webp", METADATA))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CompletedImageOutput(
                ID, prefix, "../escape.webp", List.of("../escape.webp"), 1, "image/webp", METADATA))
                .isInstanceOf(IllegalArgumentException.class);
    }
}