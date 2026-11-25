package com.example.media.api.web;

import com.example.media.api.transformation.RegisterTransformation;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The admin's named-transformation endpoint, guarded by the {@link AdminKeyFilter}. It only maps
 * HTTP: the body becomes the use case's request and the registered transformation becomes the
 * response. Bad input is 400, an unexpected failure is 500.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@NullMarked
public class AdminTransformationController {
    private final RegisterTransformation registerTransformation;

    @PostMapping(path = "/transformation", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<AdminTransformationResponse> register(@RequestBody AdminTransformationRequest body) {
        var registered = registerTransformation.register(body.name(), body.spec());
        return ResponseEntity.ok(new AdminTransformationResponse(registered.name(), registered.spec()));
    }
}
