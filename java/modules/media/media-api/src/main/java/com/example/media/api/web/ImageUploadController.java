package com.example.media.api.web;

import com.example.media.api.upload.StartImageUpload;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * The client's image upload endpoint. It only maps HTTP: the multipart parts become the use case's
 * request and file, and the finalized upload becomes the response body.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@NullMarked
public class ImageUploadController {
    private final StartImageUpload startImageUpload;

    @PostMapping(path = "/upload/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ImageUploadResponse uploadImage(
            @RequestPart("body") ImageUploadRequest body,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        var upload = startImageUpload.start(
                new StartImageUpload.Request(
                        body.uploadKey(),
                        body.extension(),
                        body.desiredContentType(),
                        body.desiredBytes() == null ? null : body.desiredBytes().longValue(),
                        body.eagerTransformations(),
                        body.incomingTransformation()),
                file.getInputStream(),
                file.getSize(),
                file.getContentType());
        return new ImageUploadResponse(upload.key().value(), upload.createdAt());
    }
}