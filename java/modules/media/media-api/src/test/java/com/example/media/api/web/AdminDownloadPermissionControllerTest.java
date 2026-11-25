package com.example.media.api.web;

import com.example.media.api.security.DownloadTokenService;
import com.example.media.api.upload.UploadNotFoundException;
import com.example.media.contract.upload.UploadKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The admin download-permission endpoint only maps HTTP: the body becomes the issuance request and
 * the issued token becomes the response. The {@link AdminKeyFilter} guards it: a missing or wrong key
 * is 401. An unknown upload is 404, bad input is 400, an unexpected failure is 500. The response
 * carries the credential itself, so it must be {@code no-store}.
 */
@WebMvcTest(AdminDownloadPermissionController.class)
@TestPropertySource(properties = "media.admin-key=test-key")
class AdminDownloadPermissionControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    DownloadTokenService downloadTokenService;

    @Test
    void issuesADownloadPermission() throws Exception {
        when(downloadTokenService.issue(any(), any()))
                .thenReturn(new DownloadTokenService.IssuedDownloadToken("1790000000123", 1790000000L, "sig", "k1"));

        var request = new AdminDownloadPermissionRequest("cover_123", 900L);

        mockMvc.perform(post("/api/admin/download-permission")
                        .header(AdminKeyFilter.HEADER, "test-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                // The body carries the credential itself: it must not be cached.
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.uploadKey").value("cover_123"))
                .andExpect(jsonPath("$.version").value("1790000000123"))
                .andExpect(jsonPath("$.expires").value(1790000000L))
                .andExpect(jsonPath("$.signature").value("sig"))
                .andExpect(jsonPath("$.keyId").value("k1"))
                .andExpect(jsonPath("$.pathPrefix").value("/cdn/cover_123"));

        verify(downloadTokenService).issue(new UploadKey("cover_123"), 900L);
    }

    @Test
    void aMissingKeyIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/admin/download-permission")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"uploadKey\":\"cover_123\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void aWrongKeyIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/admin/download-permission")
                        .header(AdminKeyFilter.HEADER, "wrong")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"uploadKey\":\"cover_123\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void aMissingUploadKeyIsBadRequest() throws Exception {
        mockMvc.perform(post("/api/admin/download-permission")
                        .header(AdminKeyFilter.HEADER, "test-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void anUnknownUploadIsNotFound() throws Exception {
        when(downloadTokenService.issue(any(), any())).thenThrow(new UploadNotFoundException("missing"));

        mockMvc.perform(post("/api/admin/download-permission")
                        .header(AdminKeyFilter.HEADER, "test-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"uploadKey\":\"missing\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void badInputIsBadRequest() throws Exception {
        when(downloadTokenService.issue(any(), any()))
                .thenThrow(new IllegalArgumentException("the download token TTL must be positive: -1"));

        mockMvc.perform(post("/api/admin/download-permission")
                        .header(AdminKeyFilter.HEADER, "test-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"uploadKey\":\"cover_123\",\"expiresInSeconds\":-1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unexpectedFailureIsServerError() throws Exception {
        when(downloadTokenService.issue(any(), any())).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(post("/api/admin/download-permission")
                        .header(AdminKeyFilter.HEADER, "test-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"uploadKey\":\"cover_123\"}"))
                .andExpect(status().isInternalServerError());
    }
}
