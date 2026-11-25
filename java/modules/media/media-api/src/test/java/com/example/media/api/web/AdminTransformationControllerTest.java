package com.example.media.api.web;

import com.example.media.api.MediaJsonConfiguration;
import com.example.media.api.transformation.RegisterTransformation;
import com.example.media.contract.transformation.ImageEncoding;
import com.example.media.contract.transformation.ImageTransformationSpec;
import com.example.media.contract.transformation.NamedTransformation;
import com.example.media.contract.transformation.NamedTransformationId;
import com.example.media.contract.transformation.TransformationVersionId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The admin transformation endpoint only maps HTTP: the body becomes the use case's request and the
 * registered transformation becomes the response. The {@link AdminKeyFilter} guards it: a missing or
 * wrong key is 401. Bad input is 400, an unexpected failure is 500. The spec travels in the shared
 * media JSON shape, so the deserialized polymorphic spec is asserted on the use case call.
 */
@WebMvcTest(AdminTransformationController.class)
// The request and response carry transformation specs, so the mapper speaks the shared media shape.
@Import(MediaJsonConfiguration.class)
@TestPropertySource(properties = "media.admin-key=test-key")
class AdminTransformationControllerTest {

    private static final ImageTransformationSpec WEBP_50 =
            new ImageTransformationSpec(null, null, null, new ImageEncoding.Webp(50));

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    RegisterTransformation registerTransformation;

    @Test
    void registersATransformation() throws Exception {
        when(registerTransformation.register(any(), any())).thenReturn(new NamedTransformation(
                new NamedTransformationId(UUID.randomUUID()), "optimize", WEBP_50,
                new TransformationVersionId(UUID.randomUUID())));

        mockMvc.perform(post("/api/admin/transformation")
                        .header(AdminKeyFilter.HEADER, "test-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new AdminTransformationRequest("optimize", WEBP_50))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("optimize"))
                .andExpect(jsonPath("$.spec.type").value("image"))
                .andExpect(jsonPath("$.spec.encoding.type").value("webp"))
                .andExpect(jsonPath("$.spec.encoding.quality").value(50));

        verify(registerTransformation).register(
                argThat(name -> name.equals("optimize")),
                argThat(spec -> spec.equals(WEBP_50)));
    }

    @Test
    void aMissingKeyIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/admin/transformation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new AdminTransformationRequest("optimize", WEBP_50))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void aWrongKeyIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/admin/transformation")
                        .header(AdminKeyFilter.HEADER, "wrong")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new AdminTransformationRequest("optimize", WEBP_50))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void badInputIsBadRequest() throws Exception {
        when(registerTransformation.register(any(), any()))
                .thenThrow(new IllegalArgumentException("the transformation name is required"));

        mockMvc.perform(post("/api/admin/transformation")
                        .header(AdminKeyFilter.HEADER, "test-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"spec\":{\"type\":\"image\",\"encoding\":{\"type\":\"webp\",\"quality\":50}}}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void anUnreadableBodyIsBadRequest() throws Exception {
        mockMvc.perform(post("/api/admin/transformation")
                        .header(AdminKeyFilter.HEADER, "test-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"optimize\",\"spec\":{\"type\":\"unknown\"}}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unexpectedFailureIsServerError() throws Exception {
        when(registerTransformation.register(any(), any())).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(post("/api/admin/transformation")
                        .header(AdminKeyFilter.HEADER, "test-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new AdminTransformationRequest("optimize", WEBP_50))))
                .andExpect(status().isInternalServerError());
    }
}
