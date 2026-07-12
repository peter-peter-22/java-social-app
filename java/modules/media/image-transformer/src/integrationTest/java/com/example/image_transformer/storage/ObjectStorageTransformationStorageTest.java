package com.example.image_transformer.storage;

import app.photofox.vipsffm.VImage;
import com.example.media_api.transformations.task.UploadTransformationTask;
import com.example.media_api.transformations.operations.ImageTransformationOperations;
import com.example.media_api.uploads.UploadId;
import com.example.object_storage.repository.ObjectStorageRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.lang.foreign.Arena;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObjectStorageTransformationStorageTest {

    private static final UploadId ORIGINAL = new UploadId("image.jpg", "input-bucket");
    private static final String OUTPUT_BUCKET = "output-bucket";

    @Mock
    private ObjectStorageRepository objectStorageRepository;

    @Test
    void readAndWriteRoundTripPreservesImageResolution() throws Exception {
        var storage = new ObjectStorageTransformationStorage(objectStorageRepository);
        var upload = UploadTransformationTask.builder()
                .name("duplicate")
                .outputBucket(OUTPUT_BUCKET)
                .original(ORIGINAL)
                .lazy(false)
                .operations(ImageTransformationOperations.builder().quality(100).build())
                .build();

        when(objectStorageRepository.getObject(eq(ORIGINAL.bucket()), eq(ORIGINAL.objectPath())))
                .thenReturn(testImageStream());

        try (var arena = Arena.ofConfined()) {
            var original = storage.read(arena, upload);
            assertEquals(1280, original.getWidth());
            assertEquals(852, original.getHeight());

            storage.write(original, upload, ImageTransformationOperations.builder().quality(100).build());

            var streamCaptor = ArgumentCaptor.forClass(InputStream.class);
            var lengthCaptor = ArgumentCaptor.forClass(Long.class);
            verify(objectStorageRepository).putObject(
                    eq(OUTPUT_BUCKET),
                    eq("output_output-bucket_duplicate.jpg"),
                    streamCaptor.capture(),
                    lengthCaptor.capture(),
                    eq("image/jpeg")
            );

            var uploadedBytes = streamCaptor.getValue().readAllBytes();
            assertEquals(uploadedBytes.length, lengthCaptor.getValue());

            try (var duplicateArena = Arena.ofConfined()) {
                var duplicate = VImage.newFromStream(
                        duplicateArena,
                        new java.io.ByteArrayInputStream(uploadedBytes),
                        app.photofox.vipsffm.VipsOption.Boolean("autorotate", true)
                );
                Assertions.assertEquals(original.getWidth(), duplicate.getWidth());
                Assertions.assertEquals(original.getHeight(), duplicate.getHeight());
            }
        }
    }

    private InputStream testImageStream() throws Exception {
        try (var inputStream = getClass().getResourceAsStream("/image.jpg")) {
            assertNotNull(inputStream, "Expected test image at src/test/resources/image.jpg");
            return new java.io.ByteArrayInputStream(inputStream.readAllBytes());
        }
    }
}
