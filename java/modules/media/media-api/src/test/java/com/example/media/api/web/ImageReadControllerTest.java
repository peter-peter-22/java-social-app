package com.example.media.api.web;

import com.example.media.api.read.ReadOriginal;
import com.example.media.api.read.ReadVariant;
import com.example.media.api.read.StoredFile;
import com.example.media.api.read.VariantNotFoundException;
import com.example.media.api.upload.UploadNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The read endpoints only map HTTP: the path becomes the use case's request and the stored file a
 * streamed response. A missing upload or variant is 404, bad input is 400, and an unexpected
 * failure is 500.
 */
@WebMvcTest(ImageReadController.class)
class ImageReadControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ReadOriginal readOriginal;

    @MockitoBean
    ReadVariant readVariant;

    @Test
    void readsTheOriginal() throws Exception {
        when(readOriginal.read(any())).thenReturn(
                new StoredFile("image/webp", "original.webp", new ByteArrayInputStream(new byte[3])));

        mockMvc.perform(get("/api/get/image/cover.jpg"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/webp"))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"original.webp\""));
    }

    @Test
    void readsTheVariant() throws Exception {
        when(readVariant.read(any(), any())).thenReturn(
                new StoredFile("image/webp", "profile.webp", new ByteArrayInputStream(new byte[3])));

        mockMvc.perform(get("/api/get/image/cover/profile.webp"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/webp"))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"profile.webp\""));
    }

    @Test
    void missingUploadIsNotFound() throws Exception {
        when(readOriginal.read(any())).thenThrow(new UploadNotFoundException("missing"));

        mockMvc.perform(get("/api/get/image/missing.jpg"))
                .andExpect(status().isNotFound());
    }

    @Test
    void missingVariantIsNotFound() throws Exception {
        when(readVariant.read(any(), any())).thenThrow(new VariantNotFoundException("missing"));

        mockMvc.perform(get("/api/get/image/cover/profile1.webp"))
                .andExpect(status().isNotFound());
    }

    @Test
    void tooLongUploadKeyIsBadRequest() throws Exception {
        var tooLong = "k".repeat(101);

        mockMvc.perform(get("/api/get/image/%s.jpg".formatted(tooLong)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unexpectedFailureIsServerError() throws Exception {
        when(readOriginal.read(any())).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/api/get/image/cover.jpg"))
                .andExpect(status().isInternalServerError());
    }
}