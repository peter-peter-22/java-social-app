package com.example.media.api.web;

import com.example.media.api.upload.StartImageUpload;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * The client's image upload endpoint. It only maps HTTP: the multipart parts become the use case's
 * request and file, and the accepted upload becomes the response body. A blocking upload is 200 with
 * the finalized upload; a queued upload is 202 with the {@code async} flag set, so the client knows
 * it cannot read the upload yet.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@NullMarked
public class ImageUploadController {
    private final StartImageUpload startImageUpload;

    @PostMapping(path = "/upload/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ImageUploadResponse> uploadImage(
            @RequestPart("body") ImageUploadRequest body,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        var result = startImageUpload.start(
                new StartImageUpload.Request(
                        body.permission(),
                        body.uploadKey(),
                        body.extension(),
                        body.desiredContentType(),
                        body.desiredBytes() == null ? null : body.desiredBytes().longValue(),
                        body.eagerTransformations(),
                        body.incomingTransformation(),
                        body.async()),
                file.getInputStream(),
                file.getSize(),
                file.getContentType());
        if (result.upload() != null)
            return ResponseEntity.ok(
                    new ImageUploadResponse(result.key().value(), result.upload().createdAt(), false));
        return ResponseEntity.accepted()
                .body(new ImageUploadResponse(result.key().value(), result.acceptedAt(), true));
    }
}