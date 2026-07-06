package com.example.media.image_transformer.transformation;

import app.photofox.vipsffm.VImage;
import app.photofox.vipsffm.VipsOption;
import com.example.media_api.transformations.operations.ImageTransformationOperations;
import com.example.media_api.transformations.operations.LimitResolution;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
class LimitResolutionTransformation implements ImageTransformation {
    @NotNull
    public VImage apply(@NotNull VImage image, @NotNull ImageTransformationOperations operations) {
        var scale = scaleFor(image, operations);
        return scale < 1.0 ? image.resize(scale, VipsOption.Double("vscale", scale)) : image;
    }

    private double scaleFor(VImage image, ImageTransformationOperations operations) {
        var widthScale = axisScale(image.getWidth(), operations.getLimitWidth());
        var heightScale = axisScale(image.getHeight(), operations.getLimitHeight());

        return Math.min(widthScale, heightScale);
    }

    private double axisScale(int currentPixels, LimitResolution limit) {
        return limit == null ? 1.0 : Math.min(1.0, (double) limit.pixels() / currentPixels);
    }
}
