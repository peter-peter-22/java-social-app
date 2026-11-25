package com.example.media.api.web;

import com.example.media.api.read.ReadCacheProperties;
import com.example.media.api.read.ReadOriginal;
import com.example.media.api.read.ReadVariant;
import com.example.media.api.read.StoredFile;
import com.example.media.api.read.VariantNotFoundException;
import com.example.media.api.security.DownloadCredential;
import com.example.media.api.upload.UploadNotFoundException;
import com.example.media.contract.upload.UploadKey;
import com.example.media.contract.upload.UploadVisibility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The read endpoints only map HTTP: the path and the {@code v}/​{@code e}/​{@code s}/​{@code k}
 * query parameters become the use case's request, and the stored file becomes a streamed response
 * with a cache header chosen by its visibility. A missing upload or variant is 404, bad input is
 * 400, and an unexpected failure is 500.
 */
@WebMvcTest(ImageReadController.class)
@EnableConfigurationProperties(ReadCacheProperties.class)
class ImageReadControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ReadOriginal readOriginal;

    @MockitoBean
    ReadVariant readVariant;

    @Test
    void readsTheOriginalWithThePublicCacheHeader() throws Exception {
        when(readOriginal.read(any(), any(), any())).thenReturn(
                new StoredFile("image/webp", "original.webp", new ByteArrayInputStream(new byte[3]),
                        UploadVisibility.PUBLIC));

        mockMvc.perform(get("/api/get/image/cover.jpg"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/webp"))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"original.webp\""))
                .andExpect(header().string("Cache-Control", "public, max-age=31536000, immutable"));
    }

    @Test
    void readsAPrivateOriginalWithThePrivateCacheHeader() throws Exception {
        when(readOriginal.read(any(), any(), any())).thenReturn(
                new StoredFile("image/webp", "original.webp", new ByteArrayInputStream(new byte[3]),
                        UploadVisibility.PRIVATE));

        mockMvc.perform(get("/api/get/image/cover.jpg?v=123&e=1790000000&s=sig&k=k1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "private, max-age=300"));
    }

    @Test
    void mapsTheQueryParametersToTheUseCaseRequest() throws Exception {
        when(readOriginal.read(any(), any(), any())).thenReturn(
                new StoredFile("image/webp", "original.webp", new ByteArrayInputStream(new byte[3]),
                        UploadVisibility.PRIVATE));

        mockMvc.perform(get("/api/get/image/cover.jpg?v=123&e=1790000000&s=sig&k=k1"))
                .andExpect(status().isOk());

        verify(readOriginal).read(new UploadKey("cover"), "123",
                new DownloadCredential(1790000000L, "sig", "k1"));
    }

    @Test
    void readsTheVariant() throws Exception {
        when(readVariant.read(any(), any(), any(), any())).thenReturn(
                new StoredFile("image/webp", "profile.webp", new ByteArrayInputStream(new byte[3]),
                        UploadVisibility.PUBLIC));

        mockMvc.perform(get("/api/get/image/cover/profile.webp"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/webp"))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"profile.webp\""));
    }

    @Test
    void missingUploadIsNotFound() throws Exception {
        when(readOriginal.read(any(), any(), any())).thenThrow(new UploadNotFoundException("missing"));

        mockMvc.perform(get("/api/get/image/missing.jpg"))
                .andExpect(status().isNotFound());
    }

    @Test
    void missingVariantIsNotFound() throws Exception {
        when(readVariant.read(any(), any(), any(), any())).thenThrow(new VariantNotFoundException("missing"));

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
        when(readOriginal.read(any(), any(), any())).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/api/get/image/cover.jpg"))
                .andExpect(status().isInternalServerError());
    }
}
