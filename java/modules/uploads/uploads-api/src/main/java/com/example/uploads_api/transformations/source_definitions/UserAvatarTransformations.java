package com.example.uploads_api.transformations.source_definitions;

import com.example.uploads_api.transformations.filters.TransformationFilter;
import com.example.uploads_api.transformations.filters.TransformationFilters;
import com.example.uploads_api.transformations.sources.ImageTransformationSource;
import com.example.uploads_api.v2.transformations.operations.ImageEncodings;
import com.example.uploads_api.v2.transformations.operations.ImageTransformationOperations;
import com.example.uploads_api.v2.transformations.operations.LimitResolution;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// TODO move to separate module
@Configuration
public class UserAvatarTransformations {
    @Bean
    public ImageTransformationSource avatarTransformation() {
        return ImageTransformationSource.builder()
                .name("avatar_fullscreen")
                .outputBucket("transformations")
                .filters(new TransformationFilter[]{new TransformationFilters.KeyPrefix("a")})
                .operations(
                        ImageTransformationOperations.builder()
                                .limitHeight(new LimitResolution(1080, LimitResolution.Mode.KEEP_ASPECT_RATIO))
                                .limitWidth(new LimitResolution(1920, LimitResolution.Mode.KEEP_ASPECT_RATIO))
                                .encoding(
                                        ImageEncodings.Jpeg.builderWithDefaults()
                                                .quality(85)
                                                .build()
                                )
                                .build()
                )
                .build();
    }
}
