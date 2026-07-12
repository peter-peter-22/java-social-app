package com.example.uploads.webhook;

import com.example.uploads.transformations.TransformationService;
import com.example.media_api.transformations.webhook.WebhookCall;
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
    void markAsReady(@RequestBody WebhookCall webhook) {
        transformationService.markLazyTransformationAsComplete(webhook.uploadId(), webhook.transformationName());
    }
}
