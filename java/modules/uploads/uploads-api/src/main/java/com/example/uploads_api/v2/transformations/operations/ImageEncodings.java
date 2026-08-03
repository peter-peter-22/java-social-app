package com.example.uploads_api.v2.transformations.operations;

import com.example.uploads_api.uploads.FileType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Builder;
import lombok.experimental.UtilityClass;
import org.jspecify.annotations.NonNull;

@UtilityClass
public class ImageEncodings {
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type"
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(value = Jpeg.class, name = "jpeg"),
            @JsonSubTypes.Type(value = Webp.class, name = "webp")
    })
    // Sealed is used here to ensure that the polymorphic JSON knows all implementations of the interface
    // and to remove the need of the default switch case.
    public sealed interface ImageEncoding permits Jpeg, Webp {
        @NonNull FileType fileType();
    }

    @Builder
    public record Jpeg(
            int quality
    ) implements ImageEncoding {
        @Override
        public @NonNull FileType fileType() {
            return FileType.JPEG;
        }

        public static @NonNull JpegBuilder builderWithDefaults() {
            return builder()
                    .quality(100);
        }
    }

    @Builder
    public record Webp(
            int quality
    ) implements ImageEncoding {
        @Override
        public @NonNull FileType fileType() {
            return FileType.WEBP;
        }

        public static @NonNull WebpBuilder builderWithDefaults() {
            return builder()
                    .quality(100);
        }
    }
}
