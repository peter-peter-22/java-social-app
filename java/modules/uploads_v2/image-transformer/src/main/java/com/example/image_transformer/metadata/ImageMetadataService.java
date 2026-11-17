package com.example.image_transformer.metadata;

import app.photofox.vipsffm.VImage;
import app.photofox.vipsffm.Vips;
import com.example.uploads_api.stored_asset.ImageMetadata;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicReference;

@NullMarked
@Service
public class ImageMetadataService {

    /**
     * Read the image metadata of a file.
     */
    public ImageMetadata readMetadata(String inputFilePath) {
        AtomicReference<@Nullable ImageMetadata> imageMetadata = new AtomicReference<>();
        Vips.run(arena -> {
            var inputVImage = VImage.newFromFile(arena, inputFilePath);
            imageMetadata.set(ImageMetadata.builder()
                    .resolutionX(inputVImage.getWidth())
                    .resolutionY(inputVImage.getHeight())
                    .build());
        });
        var result = imageMetadata.get();
        if (result == null) {
            throw new IllegalStateException("Image metadata could not be read");
        }
        return result;
    }

}
