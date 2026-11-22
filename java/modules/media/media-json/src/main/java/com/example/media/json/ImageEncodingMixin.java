package com.example.media.json;

import com.example.media.contract.transformation.ImageEncoding;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Carries the polymorphic JSON shape of {@link ImageEncoding} so the domain type stays free of
 * Jackson. The type property is {@code "type"}, and the subtype names are the stored discriminator
 * values. Do not rename them: rows in the registry and queued messages carry them.
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
abstract class ImageEncodingMixin {

    /** The media type is derived from the encoding, not stored. */
    @JsonIgnore
    abstract String contentType();
}