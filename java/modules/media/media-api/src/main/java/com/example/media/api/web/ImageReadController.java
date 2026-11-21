package com.example.media.api.web;

import com.example.media.api.read.ReadOriginal;
import com.example.media.api.read.ReadVariant;
import com.example.media.api.read.StoredFile;
import com.example.media.contract.upload.UploadKey;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The client's image read endpoints. They only map HTTP: the path becomes the use case's request,
 * and the stored file becomes a streamed response. The extension in the path is cosmetic; it makes
 * browsers display the image instead of downloading it when the URL is opened directly.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@NullMarked
public class ImageReadController {
    private final ReadOriginal readOriginal;
    private final ReadVariant readVariant;

    @GetMapping(path = "/get/image/{uploadKey}.{extension}")
    ResponseEntity<InputStreamResource> readOriginal(
            @PathVariable String uploadKey,
            @PathVariable String extension
    ) {
        return toResponse(readOriginal.read(new UploadKey(uploadKey)));
    }

    @GetMapping(path = "/get/image/{uploadKey}/{transformation}.{extension}")
    ResponseEntity<InputStreamResource> readVariant(
            @PathVariable String uploadKey,
            @PathVariable String transformation,
            @PathVariable String extension
    ) {
        return toResponse(readVariant.read(new UploadKey(uploadKey), transformation));
    }

    private static ResponseEntity<InputStreamResource> toResponse(StoredFile file) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"%s\"".formatted(file.fileName()))
                .body(new InputStreamResource(file.content()));
    }
}