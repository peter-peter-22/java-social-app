package com.example.media.imageworker.web;

import com.example.media.contract.asset.AssetId;
import com.example.media.contract.asset.AssetPlacement;
import com.example.media.contract.asset.ImageMetadata;
import com.example.media.contract.object.ObjectRef;
import com.example.media.contract.transformation.ImageEncoding;
import com.example.media.contract.transformation.ImageTransformationSpec;
import com.example.media.contract.upload.AttemptId;
import com.example.media.contract.worker.CompletedImageOutput;
import com.example.media.contract.worker.ImageTransformCommand;
import com.example.media.contract.worker.ImageTransformResult;
import com.example.media.contract.worker.PlannedImageOutput;
import com.example.media.imageworker.MediaJsonConfiguration;
import com.example.media.imageworker.transform.TransformImage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The transform endpoint only maps HTTP: the command in the contract's JSON shape becomes the use
 * case's input and the result becomes the response body. A command that can never succeed as sent
 * is 400 and an unexpected failure is 500.
 */
@WebMvcTest(ImageTransformController.class)
// The slice does not scan the application's configuration; the JSON customizer must be imported
// so the mapper speaks the shared media shape.
@Import(MediaJsonConfiguration.class)
class ImageTransformControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    TransformImage transformImage;

    private static final AttemptId ATTEMPT_ID = new AttemptId(UUID.randomUUID());
    private static final AssetPlacement PLACEMENT = new AssetPlacement("eu-west", "uploads");

    @Test
    void transformsAnImage() throws Exception {
        var command = new ImageTransformCommand(
                ATTEMPT_ID,
                new ObjectRef("eu-west", "uploads", "3f2b/original_jpg"),
                List.of(new PlannedImageOutput(
                        null,
                        PLACEMENT,
                        new ImageTransformationSpec(null, null, null, new ImageEncoding.Webp(50)))));
        var outputId = AssetId.generate();
        var result = new ImageTransformResult(
                ATTEMPT_ID,
                List.of(new CompletedImageOutput(
                        outputId,
                        PLACEMENT.prefixOf(outputId),
                        "default.webp",
                        List.of("default.webp"),
                        3,
                        "image/webp",
                        new ImageMetadata(100, 50))),
                new ImageMetadata(400, 300));
        when(transformImage.transform(any())).thenReturn(result);

        mockMvc.perform(post("/api/transform/image")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attemptId.value").value(ATTEMPT_ID.value().toString()));

        verify(transformImage).transform(command);
    }

    @Test
    void badInputIsBadRequest() throws Exception {
        // the contract rejects a command without planned outputs
        var body = """
                {"attemptId":{"value":"%s"},"source":{"region":"eu-west","bucket":"uploads","key":"3f2b/original_jpg"},"outputs":[]}
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/transform/image")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unexpectedFailureIsServerError() throws Exception {
        var command = new ImageTransformCommand(
                ATTEMPT_ID,
                new ObjectRef("eu-west", "uploads", "3f2b/original_jpg"),
                List.of(new PlannedImageOutput(
                        null,
                        PLACEMENT,
                        new ImageTransformationSpec(null, null, null, new ImageEncoding.Webp(50)))));
        when(transformImage.transform(any())).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(post("/api/transform/image")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isInternalServerError());
    }
}