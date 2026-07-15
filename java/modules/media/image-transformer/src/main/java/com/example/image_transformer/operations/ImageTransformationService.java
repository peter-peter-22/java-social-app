package com.example.image_transformer.operations;

import app.photofox.vipsffm.Vips;
import com.example.media_api.transformations.operations.ImageTransformationOperations;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class ImageTransformationService {
    private final VImageStreamConverter converter;
    private final ImageTransformationPipeline pipeline;

    public @NonNull InputStream transformFile(@NonNull InputStream inputStream, @NonNull ImageTransformationOperations operations) {
        AtomicReference<InputStream> result = new AtomicReference<>();
        Vips.run(arena -> {
            var inputVImage = converter.fromStream(arena, inputStream);
            var outputVImage = pipeline.apply(inputVImage, operations);
            result.set(converter.toStream(outputVImage, operations.getFormat(), operations.getQuality()));
        });
        if (result.get() == null)
            throw new IllegalStateException("The result is null");
        return result.get();
    }
}
