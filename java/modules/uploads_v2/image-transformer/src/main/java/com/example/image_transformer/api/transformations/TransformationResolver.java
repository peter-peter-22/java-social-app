package com.example.image_transformer.api.transformations;


import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@NullMarked
@RequiredArgsConstructor
public class TransformationResolver {
    public List<ImageTransformationConfiguration> resolve(String name) {
        return List.of();
    }
}
