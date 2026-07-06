package com.example.media.uploads.controller;

import com.example.media.uploads.transformations.TransformationService;
import com.example.media_api.transformations.api.WebhookDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/media/callback")
@RequiredArgsConstructor
public class WebhookController {
    private final TransformationService transformationService;

    @PostMapping("/mark_as_ready")
    void markAsReady(@RequestParam WebhookDTO webhook) {
        transformationService.markLazyTransformationAsComplete(webhook.uploadId(), webhook.transformationName());
    }
}
