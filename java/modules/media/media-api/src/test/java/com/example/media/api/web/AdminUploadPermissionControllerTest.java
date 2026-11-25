package com.example.media.api.web;

import com.example.media.api.security.UploadPermissionService;
import com.example.media.contract.upload.UploadKey;
import com.example.media.contract.upload.UploadPermission;
import com.example.media.contract.upload.UploadPermissionId;
import com.example.media.contract.upload.UploadPermissionStatus;
import com.example.media.contract.upload.UploadVisibility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The admin upload-permission endpoint only maps HTTP: the body becomes the use case's request and
 * the issued permission becomes the response. The {@link AdminKeyFilter} guards it: a missing or
 * wrong key is 401. Bad input is 400, an unexpected failure is 500. The response carries the raw
 * token, so it must be {@code no-store}.
 */
@WebMvcTest(AdminUploadPermissionController.class)
@TestPropertySource(properties = "media.admin-key=test-key")
class AdminUploadPermissionControllerTest {

    private static final UUID PERMISSION_ID = UUID.fromString("6c8c5f1e-5d0b-4f6a-9a3e-8b2f7d1a4c11");
    private static final UploadPermission PERMISSION = new UploadPermission(
            new UploadPermissionId(PERMISSION_ID), "hash", new UploadKey("cover_123"), "jpg",
            "image/jpeg", 10485760L, List.of("profile", "thumb"), "optimize",
            UploadVisibility.PRIVATE, Instant.parse("2026-11-25T12:10:00Z"), 1, 0,
            UploadPermissionStatus.ACTIVE, Instant.parse("2026-11-25T12:00:00Z"));

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    UploadPermissionService uploadPermissionService;

    @Test
    void issuesAPermission() throws Exception {
        when(uploadPermissionService.issue(any()))
                .thenReturn(new UploadPermissionService.IssuedPermission(PERMISSION, "up1_token"));

        var request = new AdminUploadPermissionRequest("cover_123", "jpg", "image/jpeg", 10485760L,
                List.of("profile", "thumb"), "optimize", UploadVisibility.PRIVATE, 600L, 1);

        mockMvc.perform(post("/api/admin/upload-permission")
                        .header(AdminKeyFilter.HEADER, "test-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                // The body carries the credential itself: it must not be cached.
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.permissionId").value(PERMISSION_ID.toString()))
                .andExpect(jsonPath("$.uploadKey").value("cover_123"))
                .andExpect(jsonPath("$.permission").value("up1_token"))
                .andExpect(jsonPath("$.expiresAt").value("2026-11-25T12:10:00Z"))
                .andExpect(jsonPath("$.maxUses").value(1));

        verify(uploadPermissionService).issue(argThat(mapped ->
                mapped.uploadKey().equals("cover_123")
                        && mapped.extension().equals("jpg")
                        && mapped.eagerTransformations().equals(List.of("profile", "thumb"))
                        && mapped.visibility() == UploadVisibility.PRIVATE
                        && mapped.expiresInSeconds().equals(600L)
                        && mapped.maxUses().equals(1)));
    }

    @Test
    void aMissingKeyIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/admin/upload-permission")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"extension\":\"jpg\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void aWrongKeyIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/admin/upload-permission")
                        .header(AdminKeyFilter.HEADER, "wrong")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"extension\":\"jpg\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void badInputIsBadRequest() throws Exception {
        when(uploadPermissionService.issue(any()))
                .thenThrow(new IllegalArgumentException("the extension of an upload permission is required"));

        mockMvc.perform(post("/api/admin/upload-permission")
                        .header(AdminKeyFilter.HEADER, "test-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void anUnreadableBodyIsBadRequest() throws Exception {
        mockMvc.perform(post("/api/admin/upload-permission")
                        .header(AdminKeyFilter.HEADER, "test-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"extension\":\"jpg\",\"visibility\":\"SECRET\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unexpectedFailureIsServerError() throws Exception {
        when(uploadPermissionService.issue(any())).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(post("/api/admin/upload-permission")
                        .header(AdminKeyFilter.HEADER, "test-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"extension\":\"jpg\"}"))
                .andExpect(status().isInternalServerError());
    }
}

