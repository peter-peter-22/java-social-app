package com.example.uploads_service.controller;

import com.example.uploads_api.dto.ImageUploadRequest;
import com.example.uploads_service.uploads.ImageUploadProcessingService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping
@RequiredArgsConstructor
@NullMarked
public class ClientController {
    private final ImageUploadProcessingService imageUploadProcessingService;

    @PostMapping(path = "/upload/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Validated
    void uploadImage(@RequestBody ImageUploadRequest body, @RequestParam("file") MultipartFile file) {
        imageUploadProcessingService.processUpload(body, file);
    }
}