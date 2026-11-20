package com.example.media.contract.transformation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * The output encoding of an image transformation.
 * The JSON shape is persisted in the registry and sent to workers — it is part of this contract.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ImageEncoding.Jpeg.class, name = "jpeg"),
        @JsonSubTypes.Type(value = ImageEncoding.Webp.class, name = "webp")
})
public sealed interface ImageEncoding permits ImageEncoding.Jpeg, ImageEncoding.Webp {

    /** The media type of the encoded file. Derived from the encoding, not stored. */
    @JsonIgnore
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