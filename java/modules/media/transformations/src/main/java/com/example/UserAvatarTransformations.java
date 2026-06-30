package com.example.transformations.implementations;

import com.example.api.FileType;
import com.example.api.TransformationFilter;
import com.example.api.filters.TransformationFilters;
import com.example.transformations.ImageTransformation;
import com.example.transformations.LimitResolution;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserAvatarTransformations {
    @Bean
    public ImageTransformation avatarTransformation() {
        return ImageTransformation.builder()
                .name("avatar_fullscreen")
                .bucketName("transformations")
                .filters(new TransformationFilter[]{new TransformationFilters.PathPrefix("a")})
                .operations(
                        ImageTransformation.Operations.builder()
                                .format(FileType.JPEG)
                                .limitHeight(new LimitResolution(1080, LimitResolution.Mode.KEEP_ASPECT_RATION))
                                .limitWidth(new LimitResolution(1920, LimitResolution.Mode.KEEP_ASPECT_RATION))
                                .quality(90)
                                .build()
                )
                .build();
    }
}
