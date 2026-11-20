package com.example.uploads_service;

import com.example.uploads_api.dto.ImageUploadRequest;
import com.example.uploads_api.transformations.ImageEncodings;
import com.example.uploads_api.transformations.ImageTransformationOperations;
import com.example.uploads_api.transformations.named_transformation_repository.InsertNamedImageTransformation;
import com.example.uploads_api.transformations.named_transformation_repository.NamedTransformationRepository;
import com.example.uploads_api.transformations.operations.AspectRatio;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * To run this test, the image-worker, database, and object storage services must run.
 * See the `uploads_v2/deployment` folder to check how to start these.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class EndToEndTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    NamedTransformationRepository namedTransformationRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void testConnection() throws Exception {
        mockMvc
                // indicate what MockMvc should do
                .perform(
                        // the get method and path for the request
                        get("/api"))
                // print the response
                .andDo(print())
                // the response should have status 200 OK
                .andExpect(status().isOk());
    }

    /**
     * Upload a file, request an incoming and an eager transformation.
     * The processing is blocking, so the transformation.
     */
    @Test
    void uploadThenRead() throws Exception {

        // create the used transformations
        var optimize = new InsertNamedImageTransformation(
                "optimize",
                ImageTransformationOperations.builder()
                        .encoding(
                                ImageEncodings.Webp.builder().quality(50).build()
                        )
                        .build()
        );

        var profile = new InsertNamedImageTransformation(
                "profile",
                ImageTransformationOperations.builder()
                        .aspectRatio(new AspectRatio(1, 1, AspectRatio.Mode.FILL))
                        .encoding(
                                ImageEncodings.Webp.builder().build()
                        )
                        .build()
        );

        namedTransformationRepository.set(optimize);
        namedTransformationRepository.set(profile);


        // upload
        var uploadKey = "test" + UUID.randomUUID().toString();

        var params = ImageUploadRequest.builder()
                .uploadKey(uploadKey)
                .extension("jpg")
                .eagerTransformations(List.of(profile.name()))
                .incomingTransformation(optimize.name())
                .build();

        var jsonPart = new MockMultipartFile(
                "body",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(params)
        );

        // Must be a real image. The worker downloads this object and libvips rejects unknown formats.
        var filePart = new MockMultipartFile(
                "file",
                "image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new ClassPathResource("image.jpg").getContentAsByteArray()
        );

        mockMvc.perform(multipart("/api/upload/image")
                        .file(jsonPart)
                        .file(filePart))
                .andExpect(status().isOk());


        // read the original
        mockMvc.perform(get("/api/get/image/%s.jpg".formatted(uploadKey)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/webp"));

        // try to read not existing upload key
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
