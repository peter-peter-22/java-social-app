package com.example.uploads_service.api.transformations.operations;

import com.example.image_transformer.api.uploads.FileType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.jspecify.annotations.NullMarked;

@UtilityClass
@NullMarked
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
    public sealed interface ImageEncoding permits Jpeg, Webp {
        FileType getFileType();
    }

    @Builder
    @Getter
    @EqualsAndHashCode
    public final class Jpeg implements ImageEncoding {
        @Builder.Default
        private final int quality=100;

        @Override
        public FileType getFileType() {
            return FileType.JPEG;
        }
    }

    @Builder
    @Getter
    @EqualsAndHashCode
    public final class Webp implements ImageEncoding {
        @Builder.Default
        private final int quality=100;

        @Override
        public FileType getFileType() {
            return FileType.WEBP;
        }
    }
}
