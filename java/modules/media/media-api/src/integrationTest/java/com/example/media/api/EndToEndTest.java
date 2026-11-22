package com.example.media.api;

import com.example.media.api.web.ImageUploadRequest;
import com.example.media.contract.port.TransformationCatalog;
import com.example.media.contract.transformation.AspectRatio;
import com.example.media.contract.transformation.ImageEncoding;
import com.example.media.contract.transformation.ImageTransformationSpec;
import com.example.media.contract.transformation.NewNamedTransformation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The behavior lock of the media API. To run it the image worker, the database, and the object
 * storage must be running (see the {@code deployment} folder). It boots the API in-process and
 * drives it over HTTP: register {@code optimize} and {@code profile}, upload an image with an
 * incoming and an eager transformation, then read the original as WebP and the named variant.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class EndToEndTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    TransformationCatalog transformationCatalog;

    @Test
    void contextLoads() {
    }

    /**
     * Upload a file, request an incoming and an eager transformation.
     * The processing is blocking, so the results are ready when the upload responds.
     */
    @Test
    void uploadThenRead() throws Exception {

        // create the used transformations
        transformationCatalog.register(new NewNamedTransformation(
                "optimize",
                new ImageTransformationSpec(null, null, null, new ImageEncoding.Webp(50))));

        transformationCatalog.register(new NewNamedTransformation(
                "profile",
                new ImageTransformationSpec(
                        null,
                        null,
                        new AspectRatio(1, 1, AspectRatio.Mode.FILL),
                        new ImageEncoding.Webp(80))));

        // upload
        var uploadKey = "test" + UUID.randomUUID();

        var params = new ImageUploadRequest(
                uploadKey,
                "jpg",
                null,
                null,
                List.of("profile"),
                "optimize");

        var jsonPart = new MockMultipartFile(
                "body",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(params));

        // Must be a real image. The worker downloads this object and libvips rejects unknown formats.
        var filePart = new MockMultipartFile(
                "file",
                "image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new ClassPathResource("image.jpg").getContentAsByteArray());

        mockMvc.perform(multipart("/api/upload/image")
                        .file(jsonPart)
                        .file(filePart))
                .andExpect(status().isOk());

        // read the original (the incoming "optimize" transformation made it WebP)
        mockMvc.perform(get("/api/get/image/%s.jpg".formatted(uploadKey)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/webp"));

        // try to read a not existing upload key
        mockMvc.perform(get("/api/get/image/1.jpg"))
                .andExpect(status().isNotFound());

        // read the profile transformation
        mockMvc.perform(get("/api/get/image/%s/profile.webp".formatted(uploadKey)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/webp"));

        // try to read a non-existent variant
        mockMvc.perform(get("/api/get/image/%s/profile1.webp".formatted(uploadKey)))
                .andExpect(status().is4xxClientError());
    }
}
