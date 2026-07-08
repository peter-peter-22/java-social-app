package com.example.media.image_transformer.controller;

import com.example.media.image_transformer.transformation.TransformationService;
import com.example.media_api.transformations.api.MediaTransformerEndpoints;
import com.example.media_api.transformations.api.UploadTransformationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(MediaTransformerEndpoints.TRANSFORM)
@RequiredArgsConstructor
public class TransformationController {
    private final TransformationService transformationService;

    @PostMapping()
    void markAsReady(@RequestParam UploadTransformationDTO body) {
        transformationService.applyTransformations(body);
        // TODO: replace local file with minio
        // TODO: add webmvc test
    }
}
