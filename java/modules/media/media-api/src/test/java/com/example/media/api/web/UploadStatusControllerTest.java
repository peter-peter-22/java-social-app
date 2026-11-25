package com.example.media.api.web;

import com.example.media.api.upload.ReadUploadStatus;
import com.example.media.api.upload.UploadNotFoundException;
import com.example.media.contract.upload.UploadAttemptStatus;
import com.example.media.contract.upload.UploadKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The status endpoint only maps HTTP: the path becomes the use case's request and the attempt
 * status becomes the response body. A key without an attempt is 404, an unexpected failure is 500.
 */
@WebMvcTest(UploadStatusController.class)
class UploadStatusControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ReadUploadStatus readUploadStatus;

    @Test
    void reportsTheAttemptStatus() throws Exception {
        when(readUploadStatus.read(any()))
                .thenReturn(new ReadUploadStatus.Result(new UploadKey("cover"), UploadAttemptStatus.PENDING));

        mockMvc.perform(get("/api/upload/status/cover"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uploadKey").value("cover"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void reportsTheTerminalStatus() throws Exception {
        when(readUploadStatus.read(any()))
                .thenReturn(new ReadUploadStatus.Result(new UploadKey("cover"), UploadAttemptStatus.SUCCESS));

        mockMvc.perform(get("/api/upload/status/cover"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void missingUploadIsNotFound() throws Exception {
        when(readUploadStatus.read(any())).thenThrow(new UploadNotFoundException("missing"));

        mockMvc.perform(get("/api/upload/status/missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void unexpectedFailureIsServerError() throws Exception {
        when(readUploadStatus.read(any())).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/api/upload/status/cover"))
                .andExpect(status().isInternalServerError());
    }
}
