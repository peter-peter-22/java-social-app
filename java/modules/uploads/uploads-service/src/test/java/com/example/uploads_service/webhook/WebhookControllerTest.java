package com.example.uploads_service.webhook;

import com.example.uploads_service.lazy_transformation_session_service.LazyTransformationSessionService;
import com.example.uploads_api.transformations.webhook.WebhookCall;
import com.example.uploads_api.uploads.UploadId;
import com.example.uploads_service.webhook.WebhookController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class WebhookControllerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private LazyTransformationSessionService sessions;

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(new WebhookController(sessions)).build();
    }

    @Test
    void markAsReadyCallsServiceWithRequestBody() throws Exception {
        var uploadId = new UploadId(UUID.randomUUID());
        var webhook = new WebhookCall( uploadId,"resize");

        mockMvc().perform(post("/api/media/callback/mark_as_ready")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(webhook)))
                .andExpect(status().isOk());

        verify(sessions).markLazyTransformationAsComplete(uploadId, "resize");
    }
}
