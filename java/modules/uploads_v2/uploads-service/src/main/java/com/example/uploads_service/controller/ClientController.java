package com.example.uploads_service.controller;

import com.example.upload_services.stored_asset_service.StoredAssetService;
import com.example.upload_services.upload_service.UploadService;
import com.example.uploads_api.dto.ImageUploadRequest;
import com.example.uploads_service.uploads.ImageUploadProcessingService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@NullMarked
public class ClientController {
    private final ImageUploadProcessingService imageUploadProcessingService;
    private final UploadService uploadService;
    private final StoredAssetService storedAssetService;

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

    @GetMapping(path = "/get/image/{uploadKey}.{extension}")
    ResponseEntity<InputStreamResource> downloadImage(
            @PathVariable String uploadKey,
            @PathVariable String extension, // the extension is not used. it's for making sure that the browser displays the image instead of attempting to download when opened directly.
            HttpServletResponse response
    ) throws IOException {
        var upload = uploadService.findUploadByKey(uploadKey);

        InputStream inputStream = storedAssetService.getMainFileStream(upload.getStoredAsset());

        // TODO this is not tested
        response.setContentType(upload.getStoredAsset().getContentType());
        InputStreamResource resource = new InputStreamResource(inputStream);

        // Maybe it would be possible to redirect behind the future nginx proxy to make this more efficient
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"%s\"".formatted(upload.getStoredAsset().getMainObjectKey()))
                .body(resource);
    }
}