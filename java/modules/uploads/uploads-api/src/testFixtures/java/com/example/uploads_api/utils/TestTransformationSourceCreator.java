package com.example.uploads_api.utils;

import com.example.uploads_api.transformations.sources.ImageTransformationSource;
import com.example.uploads_api.transformations.sources.VideoTransformationSource;
import com.example.uploads_api.v2.transformations.operations.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

public class TestTransformationSourceCreator {

    public static @NonNull ImageTransformationSource createImageTransformation(@Nullable Consumer<ImageTransformationSource.ImageTransformationSourceBuilder<?, ?>> customizer) {
        var builder = ImageTransformationSource.builder()
                .name("images"+ UUID.randomUUID())
                .outputBucket("bucket")
                .operations(
                        ImageTransformationOperations.builderWithDefaults()
                                .limitWidth(new LimitResolution(640, LimitResolution.Mode.KEEP_ASPECT_RATIO))
                                .aspectRatio(new AspectRatio(4, 3, AspectRatio.Mode.CONTAIN))
                                .encoding(ImageEncodings.Jpeg.builderWithDefaults().build())
                                .build()
                );

        if (customizer != null)
            customizer.accept(builder);

        return builder.build();
    }

    public static @NonNull ImageTransformationSource createImageTransformation() {
        return createImageTransformation(null);
    }

    public static @NonNull VideoTransformationSource createVideoTransformation(@Nullable Consumer<VideoTransformationSource.VideoTransformationSourceBuilder<?, ?>> customizer) {
        var builder = VideoTransformationSource.builder()
                .name("videos"+ UUID.randomUUID())
                .outputBucket("bucket")
                .operations(
                        VideoTransformationOperations.builder()
                                .encoding(VideoEncodings.Mp4.builderWithDefaults().build())
                                .build()
                );

        if (customizer != null)
            customizer.accept(builder);

        return builder.build();
    }

    public static @NonNull VideoTransformationSource createVideoTransformation() {
        return createVideoTransformation(null);
    }

}
