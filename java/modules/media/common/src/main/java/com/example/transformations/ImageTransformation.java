package com.example.transformations;

import com.example.uploads.FileType;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.Nullable;

@SuperBuilder
public class ImageTransformation extends UploadTransformation {

    @Builder
    @Getter
    public static class Operations {
        @Nullable
        private final LimitResolution limitWidth;
        @Nullable
        private final LimitResolution limitHeight;
        @Nullable
        private final FileType format;
        @Builder.Default
        private final Integer quality = 100;
        @Nullable
        private final AspectRatio aspectRatio;
    }

    public final Operations operations;
}
