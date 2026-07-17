package com.example.uploads_persistence.webhook;

import com.example.uploads_persistence.lazy_transformation_session_service.LazyTransformationSessionService;
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
    private final LazyTransformationSessionService service;

    @PostMapping("/mark_as_ready")
    void markAsReady(@RequestBody WebhookCall webhook) {
        service.markLazyTransformationAsComplete(webhook.uploadId(), webhook.transformationName());
    }
}
