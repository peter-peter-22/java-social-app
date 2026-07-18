package com.example.uploads_service.webhook;

import com.example.uploads_api.transformations.webhook.WebhookCall;
import com.example.uploads_api.uploads.UploadId;
import com.example.uploads_persistence.lazy_transformation_repository.LazyTransformationRepository;
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
    private LazyTransformationRepository repo;

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(new WebhookController(repo)).build();
    }

    @Test
    void markAsReadyCallsServiceWithRequestBody() throws Exception {
        var uploadId = new UploadId(UUID.randomUUID());
        var webhook = new WebhookCall( uploadId,"resize");

        mockMvc().perform(post("/api/media/callback/mark_as_ready")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(webhook)))
                .andExpect(status().isOk());

        verify(repo).markLazyTransformationAsComplete(uploadId, "resize");
    }
}
