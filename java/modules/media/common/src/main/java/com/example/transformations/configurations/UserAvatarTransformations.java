package com.example.transformations.configurations;

import com.example.transformations.UploadTransformation;
import com.example.transformations.operations.ImageTransformationOperations;
import com.example.transformations.operations.LimitResolution;
import com.example.transformations.TransformationFilter;
import com.example.transformations.TransformationFilters;
import com.example.uploads.FileType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserAvatarTransformations {
    @Bean
    public UploadTransformation avatarTransformation() {
        return UploadTransformation.builder()
                .name("avatar_fullscreen")
                .bucketName("transformations")
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
