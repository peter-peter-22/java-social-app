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

    @GetMapping(path = "/get/image/{uploadKey}/{transformation}.{extension}")
    ResponseEntity<InputStreamResource> downloadTransformedImage(
            @PathVariable String uploadKey,
            @PathVariable String transformation,
            @PathVariable String extension, // the extension is not used. it's for making sure that the browser displays the image instead of attempting to download when opened directly.
            HttpServletResponse response
    ) {
        try{
            var variant = uploadService.findVariant(uploadKey, transformation);
            if (variant == null) {
                return ResponseEntity.notFound().build();
            }

            // TODO the stored asset of the variant should be retrieved with entity graph similarly to the upload's asset to reduce round trips
            var asset = storedAssetService.getStoredAsset(variant.getStoredAssetId());
            // the asset should never be null because of the database foreign key
            if (asset == null) {
                return ResponseEntity.notFound().build();
            }

            InputStream inputStream = storedAssetService.getMainFileStream(asset);

            response.setContentType(asset.getContentType());
            InputStreamResource resource = new InputStreamResource(inputStream);

            // Maybe it would be possible to redirect behind the future nginx proxy to make this more efficient
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"%s\"".formatted(asset.getMainObjectKey()))
                    .body(resource);
        }
        catch (Exception e) {
            // TODO add a global error handle and formal null checks. currently an error stops the server instead of returning the 5xx or 4xx errors
            return ResponseEntity.notFound().build();
        }
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
        try {
            var upload = uploadService.findUploadByKey(uploadKey);
// TODO missing null check here

            InputStream inputStream = storedAssetService.getMainFileStream(upload.getStoredAsset());

            // TODO here the content type of the asset is used. this is one value per asset. for assets those contain multiple objects, this wont work.
            response.setContentType(upload.getStoredAsset().getContentType());
            InputStreamResource resource = new InputStreamResource(inputStream);

            // Maybe it would be possible to redirect behind the future nginx proxy to make this more efficient
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"%s\"".formatted(upload.getStoredAsset().getMainObjectKey()))
                    .body(resource);
        } catch (Exception e) {
            // TODO add a global error handle and formal null checks. currently an error stops the server instead of returning the 5xx or 4xx errors
            return ResponseEntity.notFound().build();
        }
    }
}