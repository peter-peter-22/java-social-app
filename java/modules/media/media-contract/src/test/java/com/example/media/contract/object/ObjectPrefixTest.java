package com.example.media.contract.object;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ObjectPrefixTest {

    @Test
    void resolvesKeyUnderPrefix() {
        var prefix = new ObjectPrefix("eu-west", "uploads", "3f2b");

        assertThat(prefix.resolve("original.jpg"))
                .isEqualTo(new ObjectRef("eu-west", "uploads", "3f2b/original_jpg"));
    }

    @Test
    void replacesDotsInNestedRelativePaths() {
        var prefix = new ObjectPrefix("eu-west", "uploads", "3f2b");

        assertThat(prefix.resolve("hls/segment.1.ts"))
                .isEqualTo(new ObjectRef("eu-west", "uploads", "3f2b/hls/segment_1_ts"));
    }

    @Test
    void keysOfDifferentPrefixesCannotCollide() {
        var first = new ObjectPrefix("eu-west", "uploads", "3f2b");
        var second = new ObjectPrefix("eu-west", "uploads", "9a01");

        assertThat(first.resolve("original.jpg")).isNotEqualTo(second.resolve("original.jpg"));
    }
}