package com.example.media.api.web;

import com.example.media.api.security.UploadPermissionService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The admin's upload-permission endpoint, guarded by the {@link AdminKeyFilter}. It only maps HTTP:
 * the body becomes the use case's request and the issued permission becomes the response.
 *
 * <p>The response carries the raw token, so it is {@code Cache-Control: no-store}; a credential must
 * not be cached. Bad input is 400, an unexpected failure is 500.</p>
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@NullMarked
public class AdminUploadPermissionController {
    private final UploadPermissionService uploadPermissionService;

    @PostMapping(path = "/upload-permission", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<AdminUploadPermissionResponse> issue(@RequestBody AdminUploadPermissionRequest body) {
        var issued = uploadPermissionService.issue(new UploadPermissionService.Request(
                body.uploadKey(),
                body.extension(),
                body.contentType(),
                body.maxBytes(),
                body.eagerTransformations(),
                body.incomingTransformation(),
                body.visibility(),
                body.expiresInSeconds(),
                body.maxUses()));
        var permission = issued.permission();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new AdminUploadPermissionResponse(
                        permission.id().value().toString(),
                        permission.uploadKey().value(),
                        issued.token(),
                        permission.expiresAt(),
                        permission.maxUses()));
    }
}
