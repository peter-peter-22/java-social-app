package com.example.media.api.web;

import com.example.media.api.security.DownloadTokenService;
import com.example.media.contract.upload.UploadKey;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * The admin's download-permission endpoint, guarded by the {@link AdminKeyFilter}. It only maps HTTP:
 * the body becomes the issuance request and the issued token becomes the response.
 *
 * <p>The endpoint loads the finalized upload, computes its cache version, and returns the signed
 * token together with the query parameters a client needs to read it. The response carries the
 * credential itself, so it is {@code Cache-Control: no-store}. An unknown upload is 404, bad input
 * is 400, an unexpected failure is 500.</p>
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@NullMarked
public class AdminDownloadPermissionController {
    /** The CDN path prefix a download token authorizes: the upload key's subtree. */
    private static final String CDN_PREFIX = "/cdn/";

    private final DownloadTokenService downloadTokenService;

    @PostMapping(path = "/download-permission", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<AdminDownloadPermissionResponse> issue(@RequestBody AdminDownloadPermissionRequest body) {
        if (body.uploadKey() == null)
            throw new IllegalArgumentException("the upload key is required");
        var key = new UploadKey(body.uploadKey());
        var issued = downloadTokenService.issue(key, body.expiresInSeconds());
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new AdminDownloadPermissionResponse(
                        key.value(),
                        issued.version(),
                        Instant.ofEpochSecond(issued.expires()),
                        issued.expires(),
                        issued.signature(),
                        issued.keyId(),
                        CDN_PREFIX + key.value()));
    }
}