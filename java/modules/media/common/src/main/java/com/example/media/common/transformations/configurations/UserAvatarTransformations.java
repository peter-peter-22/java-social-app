package com.example.media.common.transformations.configurations;

import com.example.media.common.transformations.UploadTransformation;
import com.example.media.common.transformations.operations.ImageTransformationOperations;
import com.example.media.common.transformations.operations.LimitResolution;
import com.example.media.common.transformations.TransformationFilter;
import com.example.media.common.transformations.TransformationFilters;
import com.example.media.common.uploads.FileType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserAvatarTransformations {
    @Bean
    public UploadTransformation avatarTransformation() {
        return UploadTransformation.builder()
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
