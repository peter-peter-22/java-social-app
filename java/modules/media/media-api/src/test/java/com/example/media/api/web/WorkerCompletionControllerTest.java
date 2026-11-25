package com.example.media.api.web;

import com.example.media.api.MediaJsonConfiguration;
import com.example.media.api.upload.CompleteImageUpload;
import com.example.media.api.upload.UploadNotFoundException;
import com.example.media.contract.asset.AssetId;
import com.example.media.contract.upload.AttemptId;
import com.example.media.contract.upload.Upload;
import com.example.media.contract.upload.UploadId;
import com.example.media.contract.upload.UploadKey;
import com.example.media.contract.upload.UploadVisibility;
import com.example.media.contract.worker.ImageTransformResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The completion endpoint only maps HTTP: the worker's result becomes the finalize use case's
 * input. Success and a duplicate delivery are 204; a result the attempt can never accept is a 4xx
 * so the worker stops retrying; an unexpected failure is 500.
 */
@WebMvcTest(WorkerCompletionController.class)
// The slice does not scan the application's configuration; the JSON customizer must be imported
// so the mapper speaks the shared media shape.
@Import(MediaJsonConfiguration.class)
class WorkerCompletionControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    CompleteImageUpload completeImageUpload;

    private static final AttemptId ATTEMPT_ID = new AttemptId(UUID.randomUUID());

    @Test
    void completesAnAttempt() throws Exception {
        var result = new ImageTransformResult(ATTEMPT_ID, List.of(), null);
        var upload = new Upload(new UploadId(UUID.randomUUID()), new UploadKey("cover"),
                AssetId.generate(), UploadVisibility.PUBLIC, Instant.now());
        when(completeImageUpload.complete(ATTEMPT_ID, result)).thenReturn(upload);

        mockMvc.perform(post("/api/completion/image")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(result)))
                .andExpect(status().isNoContent());

        verify(completeImageUpload).complete(ATTEMPT_ID, result);
    }

    @Test
    void anUnacceptableAttemptIsConflict() throws Exception {
        var result = new ImageTransformResult(ATTEMPT_ID, List.of(), null);
        when(completeImageUpload.complete(any(), any()))
                .thenThrow(new IllegalStateException("the attempt failed and cannot be completed"));

        mockMvc.perform(post("/api/completion/image")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(result)))
                .andExpect(status().isConflict());
    }

    @Test
    void aResultForADifferentAttemptIsBadRequest() throws Exception {
        var result = new ImageTransformResult(ATTEMPT_ID, List.of(), null);
        when(completeImageUpload.complete(any(), any()))
                .thenThrow(new IllegalArgumentException("the worker result is for a different attempt"));

        mockMvc.perform(post("/api/completion/image")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(result)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void anUnknownAttemptIsNotFound() throws Exception {
        var result = new ImageTransformResult(ATTEMPT_ID, List.of(), null);
        when(completeImageUpload.complete(any(), any()))
                .thenThrow(new UploadNotFoundException("could not find upload attempt"));

        mockMvc.perform(post("/api/completion/image")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(result)))
                .andExpect(status().isNotFound());
    }

    @Test
    void anUnexpectedFailureIsServerError() throws Exception {
        var result = new ImageTransformResult(ATTEMPT_ID, List.of(), null);
        when(completeImageUpload.complete(any(), any())).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(post("/api/completion/image")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(result)))
                .andExpect(status().isInternalServerError());
    }
}
