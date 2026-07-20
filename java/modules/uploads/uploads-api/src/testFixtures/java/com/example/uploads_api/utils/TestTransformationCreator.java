package com.example.uploads_api.utils;

import com.example.uploads_api.transformations.operations.AspectRatio;
import com.example.uploads_api.transformations.operations.ImageTransformationOperations;
import com.example.uploads_api.transformations.operations.LimitResolution;
import com.example.uploads_api.transformations.operations.VideoTransformationOperations;
import com.example.uploads_api.transformations.sources.ImageTransformationSource;
import com.example.uploads_api.transformations.sources.VideoTransformationSource;
import com.example.uploads_api.uploads.FileType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

public class TestTransformationCreator {

    public static @NonNull ImageTransformationSource createImageTransformation(@Nullable Consumer<ImageTransformationSource.ImageTransformationSourceBuilder<?, ?>> customizer) {
        var builder = ImageTransformationSource.builder()
                .name("images"+ UUID.randomUUID())
                .outputBucket("bucket")
                .operations(
                        ImageTransformationOperations.builder()
                                .limitWidth(new LimitResolution(640, LimitResolution.Mode.KEEP_ASPECT_RATIO))
                                .format(FileType.JPEG)
                                .quality(85)
                                .aspectRatio(new AspectRatio(4, 3, AspectRatio.Mode.CONTAIN))
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
                                .limitWidth(new LimitResolution(640, LimitResolution.Mode.KEEP_ASPECT_RATIO))
                                .format(FileType.JPEG)
                                .quality(85)
                                .aspectRatio(new AspectRatio(4, 3, AspectRatio.Mode.CONTAIN))
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
