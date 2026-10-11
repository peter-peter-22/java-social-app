package com.example.uploads_api.transformations.operations;

import com.example.uploads_api.uploads.FileType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import lombok.extern.jackson.Jacksonized;
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
        @JsonIgnore
        FileType getFileType();
    }

    @Builder
    @Jacksonized
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
    @Jacksonized
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
