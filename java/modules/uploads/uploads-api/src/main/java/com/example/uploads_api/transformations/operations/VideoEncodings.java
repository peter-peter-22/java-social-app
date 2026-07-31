package com.example.uploads_api.transformations.operations;

import com.example.uploads_api.uploads.FileType;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Builder;
import lombok.experimental.UtilityClass;
import org.jspecify.annotations.NonNull;

@UtilityClass
public class VideoEncodings {

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type"
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(value = Hls.class, name = "hls"),
            @JsonSubTypes.Type(value = Mp4.class, name = "mp4")
    })
    // Sealed is used here to ensure that the polymorphic JSON knows all implementations of the interface
    // and to remove the need of the default switch case.
    public sealed interface VideoEncoding permits Mp4, Hls {
        @NonNull
        FileType fileType();
    }

    @Builder
    public record Mp4(
            float videoBitrate,
            float audioBitrate
    ) implements VideoEncoding {
        @Override
        public @NonNull FileType fileType() {
            return FileType.MP4;
        }

        public static @NonNull Mp4Builder builderWithDefaults() {
            return builder()
                    .audioBitrate(1)
                    .videoBitrate(1);
        }
    }

    @Builder
    public record Hls(
            // implement
    ) implements VideoEncoding {
        @Override
        public @NonNull FileType fileType() {
            return FileType.HLS;
        }

        public static @NonNull HlsBuilder builderWithDefaults() {
            return builder();
        }
    }
}
