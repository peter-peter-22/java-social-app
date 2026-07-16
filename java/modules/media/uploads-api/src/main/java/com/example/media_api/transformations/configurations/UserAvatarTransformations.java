package com.example.media_api.transformations.configurations;

import com.example.media_api.transformations.sources.ImageTransformationSource;
import com.example.media_api.transformations.operations.ImageTransformationOperations;
import com.example.media_api.transformations.operations.LimitResolution;
import com.example.media_api.transformations.filters.TransformationFilter;
import com.example.media_api.transformations.filters.TransformationFilters;
import com.example.media_api.uploads.FileType;
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
                .filters(new TransformationFilter[]{new TransformationFilters.PathPrefix("a")})
                .operations(
                        ImageTransformationOperations.builder()
                                .format(FileType.JPEG)
                                .limitHeight(new LimitResolution(1080, LimitResolution.Mode.KEEP_ASPECT_RATIO))
                                .limitWidth(new LimitResolution(1920, LimitResolution.Mode.KEEP_ASPECT_RATIO))
                                .quality(90)
                                .build()
                )
                .build();
    }
}
