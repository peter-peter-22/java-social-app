package com.example.image_transformer.operations;

import app.photofox.vipsffm.VImage;
import app.photofox.vipsffm.VipsOption;
import app.photofox.vipsffm.enums.VipsCompassDirection;
import com.example.uploads_api.transformations.operations.AspectRatio;
import com.example.uploads_api.transformations.operations.ImageTransformationOperations;
import org.jspecify.annotations.NonNull;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(1)
class AspectRatioTransformation implements ImageTransformation {
    @NonNull
    public VImage apply(@NonNull VImage image, @NonNull ImageTransformationOperations operations) {
        var aspectRatio = operations.getAspectRatio();
        if (aspectRatio == null) {
            return image;
        }

        return switch (aspectRatio.mode()) {
            case FILL -> cropToRatio(image, aspectRatio);
            case CONTAIN -> padToRatio(image, aspectRatio);
        };
    }

    private VImage cropToRatio(VImage image, AspectRatio aspectRatio) {
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

    private VImage padToRatio(VImage image, AspectRatio aspectRatio) {
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

    private double ratio(AspectRatio aspectRatio) {
        return (double) aspectRatio.width() / aspectRatio.height();
    }
}
