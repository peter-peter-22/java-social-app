package com.example.uploads_service.controller;

import com.example.uploads_api.dto.ImageUploadRequest;
import com.example.uploads_service.uploads.ImageUploadProcessingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@NullMarked
public class ClientController {
    private final ImageUploadProcessingService imageUploadProcessingService;

    // this endpoint exists only to check if mockMVC works
    @GetMapping
    void home() {
    }

    @PostMapping(path = "/upload/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    void uploadImage(
            @Valid @RequestPart("body") ImageUploadRequest body,
            @RequestPart("file") MultipartFile file
    ) {
        imageUploadProcessingService.processUpload(body, file);
    }
}