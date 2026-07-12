package com.example.image_transformer.storage;

import com.example.media_api.transformations.operations.ImageTransformationOperations;
import com.example.media_api.transformations.task.UploadTransformationTask;
import com.example.media_api.uploads.UploadId;
import com.example.object_storage.repository.ObjectStorageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void readReturnsObjectStorageStream() {
        var storage = new ObjectStorageTransformationStorage(objectStorageRepository);
        var upload = uploadTask();
        var expectedStream = new ByteArrayInputStream(new byte[]{1, 2, 3});

        when(objectStorageRepository.getObject(eq(ORIGINAL.bucket()), eq(ORIGINAL.objectPath())))
                .thenReturn(expectedStream);

        var actualStream = storage.read(upload);

        assertSame(expectedStream, actualStream);
    }

    @Test
    void writeUploadsInputStreamAsJpeg() throws Exception {
        var storage = new ObjectStorageTransformationStorage(objectStorageRepository);
        var upload = uploadTask();
        var payload = new byte[]{10, 20, 30, 40};

        storage.write(new ByteArrayInputStream(payload), upload);

        var streamCaptor = ArgumentCaptor.forClass(InputStream.class);
        var lengthCaptor = ArgumentCaptor.forClass(Long.class);
        var outputId = upload.getOutputId();
        verify(objectStorageRepository).putObject(
                eq(outputId.bucket()),
                eq(outputId.objectPath()),
                streamCaptor.capture(),
                lengthCaptor.capture(),
                eq("image/jpeg")
        );

        assertArrayEquals(payload, streamCaptor.getValue().readAllBytes());
        assertEquals(payload.length, lengthCaptor.getValue());
    }

    private UploadTransformationTask uploadTask() {
        return UploadTransformationTask.builder()
                .name("duplicate")
                .outputBucket(OUTPUT_BUCKET)
                .original(ORIGINAL)
                .lazy(false)
                .operations(ImageTransformationOperations.builder().quality(100).build())
                .build();
    }
}
