package com.example.image_transformer.transformation;

import app.photofox.vipsffm.VImage;
import app.photofox.vipsffm.Vips;
import app.photofox.vipsffm.VipsOption;
import com.example.uploads_api.stored_asset.ImageMetadata;
import com.example.uploads_api.transformations.ImageEncodings;
import com.example.uploads_api.transformations.ImageTransformationOperations;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
@NullMarked
public class ImageTransformationService {
    private final ImageTransformationPipeline pipeline;

    /**
     * The entrypoint and the media metadata of the result of a transformation.
     */
    public record TransformationResult(
            Path entryPoint,
            ImageMetadata metadata
    ) {
    }

    /**
     * Write the resulting files of a transformation into the work directory, return the entrypoint and the metadata.
     * // TODO remove the location metadata added by android
     */
    public TransformationResult transformFile(String inputFilePath, Path workDir, ImageTransformationOperations operations) {
        AtomicReference<@Nullable TransformationResult> result = new AtomicReference<>();

        Vips.run(arena -> {
            var inputVImage = VImage.newFromFile(arena, inputFilePath);
            var outputVImage = pipeline.apply(inputVImage, operations);

            var metadata = ImageMetadata.builder()
                    .resolutionX(outputVImage.getWidth())
                    .resolutionY(outputVImage.getHeight())
                    .build();

            var entryPoint = applyEncoding(outputVImage, workDir, operations.encoding());

            result.set(new TransformationResult(entryPoint, metadata));
        });

        return Objects.requireNonNull(result.get());
    }

    /**
     * Create the files of an image transformation by using the encoding and it's parameters.
     * Return the entrypoint.
     */
    private Path applyEncoding(VImage outputVImage, Path workDir, ImageEncodings.ImageEncoding encoding) {
        switch (encoding) {
            case ImageEncodings.Jpeg jpegEncoding -> {
                var path = workDir.resolve(Path.of("default.jpg"));
                outputVImage.jpegsave(
                        path.toString(),
                        VipsOption.Int("Q", jpegEncoding.getQuality())
                );
                return path;
            }
            case ImageEncodings.Webp webpEncoding -> {
                var path = workDir.resolve(Path.of("default.webp"));
                outputVImage.webpsave(
                        path.toString(),
                        VipsOption.Int("Q", webpEncoding.getQuality())
                );
                return path;
            }
        }
    }
}
