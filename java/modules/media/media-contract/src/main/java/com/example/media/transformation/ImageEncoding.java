package com.example.media.transformation;

import com.example.media.asset.DeliveryProtocol;
import org.jspecify.annotations.NullMarked;

/**
 * Image output format. Video encodings are intentionally absent; a video worker gets its own spec.
 * Current encodings always write one object, so an incoming transformation cannot be a directory.
 */
@NullMarked
public sealed interface ImageEncoding permits ImageEncoding.Jpeg, ImageEncoding.Webp {
    int quality();

    String contentType();

    String extension();

    default boolean directory() {
        return false;
    }

    default DeliveryProtocol deliveryProtocol() {
        return DeliveryProtocol.IMAGE;
    }

    record Jpeg(int quality) implements ImageEncoding {
        public Jpeg {
            requireQuality(quality);
        }

        @Override
        public String contentType() {
            return "image/jpeg";
        }

        @Override
        public String extension() {
            return "jpg";
        }
    }

    record Webp(int quality) implements ImageEncoding {
        public Webp {
            requireQuality(quality);
        }

        @Override
        public String contentType() {
            return "image/webp";
        }

        @Override
        public String extension() {
            return "webp";
        }
    }

    private static void requireQuality(int quality) {
        if (quality < 1 || quality > 100) {
            throw new IllegalArgumentException("Image quality must be between 1 and 100");
        }
    }
}
