package com.example.media.contract.transformation;

/**
 * The output encoding of an image transformation.
 * The JSON shape is persisted in the registry and sent to workers — it is part of this contract.
 * The shape is defined by the mixins in {@code media-json}: the type property is "type", and the
 * discriminator values are "jpeg" and "webp".
 */
public sealed interface ImageEncoding permits ImageEncoding.Jpeg, ImageEncoding.Webp {

    /** The media type of the entry point of the output. Derived from the encoding. */
    String contentType();

    record Jpeg(int quality) implements ImageEncoding {
        public Jpeg {
            if (quality < 1 || quality > 100)
                throw new IllegalArgumentException("quality must be between 1 and 100: " + quality);
        }

        @Override
        public String contentType() {
            return "image/jpeg";
        }
    }

    record Webp(int quality) implements ImageEncoding {
        public Webp {
            if (quality < 1 || quality > 100)
                throw new IllegalArgumentException("quality must be between 1 and 100: " + quality);
        }

        @Override
        public String contentType() {
            return "image/webp";
        }
    }
}