package com.example.media.imageworker.transform;

import app.photofox.vipsffm.VImage;
import app.photofox.vipsffm.Vips;
import app.photofox.vipsffm.VipsOption;
import app.photofox.vipsffm.enums.VipsCompassDirection;
import com.example.media.contract.asset.ImageMetadata;
import com.example.media.contract.transformation.AspectRatio;
import com.example.media.contract.transformation.ImageEncoding;
import com.example.media.contract.transformation.ImageTransformationSpec;
import com.example.media.contract.transformation.LimitResolution;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The libvips image processing of this worker.
 * It reads the dimensions of an image and applies one resolved transformation to a file: crop or
 * pad to the aspect ratio, limit the resolution, and encode the result. The entry file of an output
 * is named after its encoding, the same as the prototype's ({@code default.jpg} and
 * {@code default.webp}).
 */
@Component
public class ImageProcessor implements ImageProcessing {

    /** Reads the dimensions of an image file. */
    @Override
    public ImageMetadata readMetadata(Path file) {
        var result = new AtomicReference<@Nullable ImageMetadata>();
        Vips.run(arena -> {
            var image = VImage.newFromFile(arena, file.toAbsolutePath().toString());
            result.set(new ImageMetadata(image.getWidth(), image.getHeight()));
        });
        return Objects.requireNonNull(result.get(), "the image metadata could not be read: " + file);
    }

    /**
     * Applies the resolved operations to the source image and writes the encoded result into the
     * output directory. The aspect ratio is applied before the resolution limit, the same as in
     * the prototype.
     */
    @Override
    public ProcessedFile process(Path source, Path outputDir, ImageTransformationSpec spec) {
        var result = new AtomicReference<@Nullable ProcessedFile>();
        Vips.run(arena -> {
            VImage image = VImage.newFromFile(arena, source.toAbsolutePath().toString());
            image = applyAspectRatio(image, spec.aspectRatio());
            image = applyLimitResolution(image, spec.limitWidth(), spec.limitHeight());
            var entryPoint = encode(image, outputDir, spec.encoding());
            result.set(new ProcessedFile(entryPoint, new ImageMetadata(image.getWidth(), image.getHeight())));
        });
        return Objects.requireNonNull(result.get(), "the image could not be transformed: " + source);
    }

    /** Crops or pads the image to the requested aspect ratio. */
    private static VImage applyAspectRatio(VImage image, @Nullable AspectRatio aspectRatio) {
        if (aspectRatio == null)
            return image;
        return switch (aspectRatio.mode()) {
            case FILL -> cropToRatio(image, aspectRatio);
            case CONTAIN -> padToRatio(image, aspectRatio);
        };
    }

    /** Crops the image around its center until it matches the ratio. */
    private static VImage cropToRatio(VImage image, AspectRatio aspectRatio) {
        var sourceWidth = image.getWidth();
        var sourceHeight = image.getHeight();
        var targetRatio = ratio(aspectRatio);
        var sourceRatio = (double) sourceWidth / sourceHeight;

        int targetWidth = sourceWidth;
        int targetHeight = sourceHeight;
        if (sourceRatio > targetRatio) {
            targetWidth = Math.max(1, (int) Math.round(sourceHeight * targetRatio));
        } else if (sourceRatio < targetRatio) {
            targetHeight = Math.max(1, (int) Math.round(sourceWidth / targetRatio));
        }

        var left = Math.max(0, (sourceWidth - targetWidth) / 2);
        var top = Math.max(0, (sourceHeight - targetHeight) / 2);
        return image.extractArea(left, top, targetWidth, targetHeight);
    }

    /** Pads the image with black until it matches the ratio. */
    private static VImage padToRatio(VImage image, AspectRatio aspectRatio) {
        var sourceWidth = image.getWidth();
        var sourceHeight = image.getHeight();
        var targetRatio = ratio(aspectRatio);
        var sourceRatio = (double) sourceWidth / sourceHeight;

        int targetWidth = sourceWidth;
        int targetHeight = sourceHeight;
        if (sourceRatio > targetRatio) {
            targetHeight = Math.max(1, (int) Math.round(sourceWidth / targetRatio));
        } else if (sourceRatio < targetRatio) {
            targetWidth = Math.max(1, (int) Math.round(sourceHeight * targetRatio));
        }

        return image.gravity(
                VipsCompassDirection.COMPASS_DIRECTION_CENTRE,
                targetWidth,
                targetHeight,
                VipsOption.ArrayDouble("background", List.of(0.0, 0.0, 0.0))
        );
    }

    private static double ratio(AspectRatio aspectRatio) {
        return (double) aspectRatio.width() / aspectRatio.height();
    }

    /** Scales the image down when it exceeds one of the limits. */
    private static VImage applyLimitResolution(
            VImage image,
            @Nullable LimitResolution limitWidth,
            @Nullable LimitResolution limitHeight
    ) {
        var widthScale = axisScale(image.getWidth(), limitWidth);
        var heightScale = axisScale(image.getHeight(), limitHeight);
        var scale = Math.min(widthScale, heightScale);
        return scale < 1.0 ? image.resize(scale, VipsOption.Double("vscale", scale)) : image;
    }

    private static double axisScale(int currentPixels, @Nullable LimitResolution limit) {
        return limit == null ? 1.0 : Math.min(1.0, (double) limit.pixels() / currentPixels);
    }

    /** Writes the encoded image into the output directory and returns the entry file. */
    private static Path encode(VImage image, Path outputDir, ImageEncoding encoding) {
        return switch (encoding) {
            case ImageEncoding.Jpeg jpegEncoding -> {
                var entryPoint = outputDir.resolve("default.jpg");
                image.jpegsave(entryPoint.toString(), VipsOption.Int("Q", jpegEncoding.quality()));
                yield entryPoint;
            }
            case ImageEncoding.Webp webpEncoding -> {
                var entryPoint = outputDir.resolve("default.webp");
                image.webpsave(entryPoint.toString(), VipsOption.Int("Q", webpEncoding.quality()));
                yield entryPoint;
            }
        };
    }
}