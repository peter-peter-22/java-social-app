package com.example.upload_registry.transformations;

import com.example.uploads_api.transformations.ImageEncodings;
import com.example.uploads_api.transformations.ImageTransformationOperations;
import com.example.uploads_api.transformations.PolymorphicTransformationOperations;
import com.example.uploads_api.transformations.operations.AspectRatio;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

public class PolymorphicTransformationTests {
    ObjectMapper objectMapper = new ObjectMapper();

    ImageTransformationOperations imageOperations = ImageTransformationOperations.builder()
            .aspectRatio(new AspectRatio(1, 1, AspectRatio.Mode.FILL))
            .encoding(
                    ImageEncodings.Webp.builder().build()
            )
            .build();

    @Test
    public void imageOperations() {
        var serialized = objectMapper.writeValueAsString(imageOperations);
        objectMapper.readValue(serialized, ImageTransformationOperations.class);
    }

    @Test
    public void polymorphic() {
        PolymorphicTransformationOperations polyOperations=imageOperations;
        var serialized = objectMapper.writeValueAsString(polyOperations);
        objectMapper.readValue(serialized, PolymorphicTransformationOperations.class);
    }
}
