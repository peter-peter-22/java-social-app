package com.example.uploads_api.transformations.operations;

import com.example.uploads_api.uploads.FileType;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@UtilityClass
@NullMarked
public class VideoEncodings {

//    @JsonTypeInfo(
//            use = JsonTypeInfo.Id.NAME,
//            include = JsonTypeInfo.As.PROPERTY,
//            property = "type"
//    )
//    @JsonSubTypes({
//            @JsonSubTypes.Type(value = Hls.class, name = "hls"),
//            @JsonSubTypes.Type(value = Mp4.class, name = "mp4")
//    })
    public sealed interface VideoEncoding permits Mp4, Hls {
        FileType getFileType();
    }

    @Builder
    @Getter
    public final class Mp4 implements VideoEncoding, HasVideoQuality {
        @Nullable Integer videoBitrate;
        @Nullable Integer audioBitrate;
        @Nullable Integer videoFrameRate;
        @Nullable Integer constantRateFactor;
        @Nullable VideoQualityPreset videoQualityPreset;

        VideoEncodingPreset encodingPreset;

        @Override
        public FileType getFileType() {
            return FileType.MP4;
        }
    }

    @Builder
    public final class Hls implements VideoEncoding {
        @Override
        public FileType getFileType() {
            return FileType.HLS;
        }
    }
}
