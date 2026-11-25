package com.example.media.contract.upload;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UploadKeyTest {

    @Test
    void acceptsAPlainKey() {
        assertThat(new UploadKey("test-123").value()).isEqualTo("test-123");
    }

    @Test
    void acceptsAKeyOf100Characters() {
        assertThat(new UploadKey("x".repeat(100)).value()).hasSize(100);
    }

    @Test
    void rejectsABlankKey() {
        assertThatThrownBy(() -> new UploadKey("  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsAKeyLongerThanTheColumn() {
        assertThatThrownBy(() -> new UploadKey("x".repeat(101)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsAKeyWithAPathSeparatorOrDot() {
        assertThatThrownBy(() -> new UploadKey("cover/123"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new UploadKey("cover.123"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsAKeyOutsideTheUrlSafeCharset() {
        assertThatThrownBy(() -> new UploadKey("cover 123"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new UploadKey("cöver"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsANullKey() {
        assertThatThrownBy(() -> new UploadKey(null))
                .isInstanceOf(NullPointerException.class);
    }
}