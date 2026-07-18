package com.example.uploads_service.webhook;

import com.example.uploads_api.transformations.lazy_transformation_store.LazyTransformationStore;
import com.example.uploads_api.transformations.webhook.WebhookCall;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/media/callback")
@RequiredArgsConstructor
public class WebhookController {
    private final LazyTransformationStore lazyTransformationStore;

    @PostMapping("/mark_as_ready")
    void markAsReady(@RequestBody WebhookCall webhook) {
        lazyTransformationStore.markLazyTransformationAsComplete(webhook.uploadId(), webhook.transformationName());
    }
}
