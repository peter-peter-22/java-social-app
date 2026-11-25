package com.example.media.api.web;

import com.example.media.api.upload.ReadUploadStatus;
import com.example.media.contract.upload.UploadKey;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The client's queued-upload status endpoint: after an async upload the client polls this until the
 * attempt reaches its terminal status. It only maps HTTP: the path becomes the use case's request,
 * and the attempt status becomes the response body. A key without an attempt is 404.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@NullMarked
public class UploadStatusController {
    private final ReadUploadStatus readUploadStatus;

    @GetMapping(path = "/upload/status/{uploadKey}")
    ResponseEntity<UploadStatusResponse> status(@PathVariable String uploadKey) {
        var result = readUploadStatus.read(new UploadKey(uploadKey));
        return ResponseEntity.ok(new UploadStatusResponse(result.key().value(), result.status()));
    }
}
