package com.example.image_transformer.controller;

import com.example.image_transformer.transformation.TransformationService;
import com.example.media_api.transformations.task.UploadTransformationTask;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transform")
@RequiredArgsConstructor
public class TransformationController {
    private final TransformationService transformationService;

    @PostMapping()
    void markAsReady(@RequestParam UploadTransformationTask body) {
        transformationService.applyTransformations(body);
        // TODO: add webmvc test
    }
}
