package com.example.uploads.controller;

import com.example.uploads.transformations.TransformationService;
import com.example.media_api.transformations.api.WebhookDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/media/callback")
@RequiredArgsConstructor
public class WebhookController {
    private final TransformationService transformationService;

    @PostMapping("/mark_as_ready")
    void markAsReady(@RequestBody WebhookDTO webhook) {
        transformationService.markLazyTransformationAsComplete(webhook.uploadId(), webhook.transformationName());
    }
}
