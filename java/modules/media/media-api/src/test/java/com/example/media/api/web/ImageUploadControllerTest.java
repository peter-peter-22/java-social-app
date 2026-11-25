package com.example.media.api.web;

import com.example.media.api.upload.StartImageUpload;
import com.example.media.api.upload.UploadNotFoundException;
import com.example.media.contract.asset.AssetId;
import com.example.media.contract.upload.Upload;
import com.example.media.contract.upload.UploadId;
import com.example.media.contract.upload.UploadKey;
import com.example.media.contract.upload.UploadPermissionDeniedException;
import com.example.media.contract.upload.UploadPermissionExhaustedException;
import com.example.media.contract.upload.UploadPermissionExpiredException;
import com.example.media.contract.upload.UploadVisibility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The upload endpoint only maps HTTP: the multipart parts become the use case's request and file,
 * and the use case outcomes become 200, 400, 403, and 500 answers. A missing upload is 404.
 */
@WebMvcTest(ImageUploadController.class)
class ImageUploadControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    StartImageUpload startImageUpload;

    @Test
    void uploadsAnImage() throws Exception {
        var upload = new Upload(new UploadId(UUID.randomUUID()), new UploadKey("cover"),
                AssetId.generate(), UploadVisibility.PUBLIC, Instant.parse("2026-01-01T00:00:00Z"));
        when(startImageUpload.start(any(), any(), anyLong(), any())).thenReturn(
                new StartImageUpload.StartResult(new UploadKey("cover"), upload, null));

        mockMvc.perform(multipart("/api/upload/image")
                        .file(bodyPart(new ImageUploadRequest(
                                "up1_token", "cover", "jpg", null, null, List.of("profile"), "optimize", null)))
                        .file(filePart()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uploadKey").value("cover"))
                .andExpect(jsonPath("$.createdAt").value("2026-01-01T00:00:00Z"))
                .andExpect(jsonPath("$.async").value(false))
                .andExpect(jsonPath("$.version").value("1767225600000"));

        verify(startImageUpload).start(
                org.mockito.ArgumentMatchers.argThat(request -> request.permission().equals("up1_token")
                        && request.uploadKey().equals("cover")
                        && request.extension().equals("jpg")
                        && request.eagerTransformationNames().equals(List.of("profile"))
                        && request.incomingTransformationName().equals("optimize")
                        && request.async() == null),
                any(),
                eq(3L),
                eq("image/jpeg"));
    }

    @Test
    void queuesAnAsyncImageUpload() throws Exception {
        when(startImageUpload.start(any(), any(), anyLong(), any())).thenReturn(
                new StartImageUpload.StartResult(new UploadKey("cover"), null, Instant.parse("2026-01-01T00:00:00Z")));

        mockMvc.perform(multipart("/api/upload/image")
                        .file(bodyPart(new ImageUploadRequest(null, "cover", "jpg", null, null, List.of("profile"), "optimize", true)))
                        .file(filePart()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.uploadKey").value("cover"))
                .andExpect(jsonPath("$.createdAt").value("2026-01-01T00:00:00Z"))
                .andExpect(jsonPath("$.async").value(true));

        verify(startImageUpload).start(
                org.mockito.ArgumentMatchers.argThat(request -> Boolean.TRUE.equals(request.async())),
                any(),
                eq(3L),
                eq("image/jpeg"));
    }

    @Test
    void missingUploadIsNotFound() throws Exception {
        when(startImageUpload.start(any(), any(), anyLong(), any()))
                .thenThrow(new UploadNotFoundException("missing"));

        mockMvc.perform(multipart("/api/upload/image")
                        .file(bodyPart(new ImageUploadRequest(null, "missing", "jpg", null, null, null, null, null)))
                        .file(filePart()))
                .andExpect(status().isNotFound());
    }

    @Test
    void badInputIsBadRequest() throws Exception {
        when(startImageUpload.start(any(), any(), anyLong(), any()))
                .thenThrow(new IllegalArgumentException("the uploaded file is empty"));

        mockMvc.perform(multipart("/api/upload/image")
                        .file(bodyPart(new ImageUploadRequest(null, "cover", "jpg", null, null, null, null, null)))
                        .file(filePart()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingFilePartIsBadRequest() throws Exception {
        mockMvc.perform(multipart("/api/upload/image")
                        .file(bodyPart(new ImageUploadRequest(null, "cover", "jpg", null, null, null, null, null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void aDeniedPermissionIsForbidden() throws Exception {
        when(startImageUpload.start(any(), any(), anyLong(), any()))
                .thenThrow(new UploadPermissionDeniedException("the upload permission is unknown"));

        mockMvc.perform(multipart("/api/upload/image")
                        .file(bodyPart(new ImageUploadRequest(null, "cover", "jpg", null, null, null, null, null)))
                        .file(filePart()))
                .andExpect(status().isForbidden());
    }

    @Test
    void anExpiredOrExhaustedPermissionIsForbidden() throws Exception {
        when(startImageUpload.start(any(), any(), anyLong(), any()))
                .thenThrow(new UploadPermissionExpiredException("the upload permission expired"))
                .thenThrow(new UploadPermissionExhaustedException("the upload permission has no remaining uses"));

        for (var attempt = 0; attempt < 2; attempt++) {
            mockMvc.perform(multipart("/api/upload/image")
                            .file(bodyPart(new ImageUploadRequest(null, "cover", "jpg", null, null, null, null, null)))
                            .file(filePart()))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void unexpectedFailureIsServerError() throws Exception {
        when(startImageUpload.start(any(), any(), anyLong(), any()))
                .thenThrow(new RuntimeException("boom"));

        mockMvc.perform(multipart("/api/upload/image")
                        .file(bodyPart(new ImageUploadRequest(null, "cover", "jpg", null, null, null, null, null)))
                        .file(filePart()))
                .andExpect(status().isInternalServerError());
    }

    private MockMultipartFile bodyPart(ImageUploadRequest body) throws Exception {
        return new MockMultipartFile("body", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(body));
    }

    private static MockMultipartFile filePart() {
        return new MockMultipartFile("file", "image.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[3]);
    }
}