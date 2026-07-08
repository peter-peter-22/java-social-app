package com.example.uploads.webhook;

import com.example.uploads.transformations.TransformationService;
import com.example.media_api.transformations.api.WebhookDTO;
import com.example.media_api.uploads.UploadId;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// CLEANING: This looks a little weird
@ExtendWith(MockitoExtension.class)
class WebhookControllerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TransformationService transformationService;

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(new WebhookController(transformationService)).build();
    }

    @Test
    void markAsReadyCallsServiceWithRequestBody() throws Exception {
        var uploadId = new UploadId("image.jpg", "bucket");
        var webhook = new WebhookDTO("resize", uploadId);

        mockMvc().perform(post("/api/media/callback/mark_as_ready")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(webhook)))
                .andExpect(status().isOk());

        verify(transformationService).markLazyTransformationAsComplete(uploadId, "resize");
    }
}
