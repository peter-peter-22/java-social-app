package com.example.media.image_transformer;

import com.example.media_api.transformations.api.UploadTransformationDTO;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import app.photofox.vipsffm.VImage;
import app.photofox.vipsffm.Vips;
import app.photofox.vipsffm.VipsOption;

@Service
public class TransformationService {
    void ApplyTransformations(@NotNull UploadTransformationDTO upload) {
        Vips.run(arena -> {
            String resources = "src/test/resources/";
            String input = resources + "image.jpg";
            String outputBase = resources + "output";

            System.out.println("Testing thumbnail...");
            var thumb = VImage.thumbnail(
                    arena,
                    input,
                    400,
                    VipsOption.Boolean("auto-rotate", true)
            );
            thumb.writeToFile(outputBase + "_thumb.jpg");
            System.out.println("Thumbnail created successfully.");
        });
    }
}
