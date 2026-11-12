package com.example.image_transformer.transformation;

import app.photofox.vipsffm.VImage;
import app.photofox.vipsffm.VipsOption;
import com.example.uploads_api.transformations.ImageTransformationOperations;
import com.example.uploads_api.transformations.operations.LimitResolution;
import org.jspecify.annotations.NonNull;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
class LimitResolutionTransformation implements ImageTransformationOperation {
    @NonNull
    public VImage apply(@NonNull VImage image, @NonNull ImageTransformationOperations operations) {
        var scale = scaleFor(image, operations);
        return scale < 1.0 ? image.resize(scale, VipsOption.Double("vscale", scale)) : image;
    }

    private double scaleFor(VImage image, ImageTransformationOperations operations) {
        var widthScale = axisScale(image.getWidth(), operations.limitWidth());
        var heightScale = axisScale(image.getHeight(), operations.limitHeight());

        return Math.min(widthScale, heightScale);
    }

    private double axisScale(int currentPixels, LimitResolution limit) {
        return limit == null ? 1.0 : Math.min(1.0, (double) limit.pixels() / currentPixels);
    }
}
