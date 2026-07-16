package com.example.image_transformer.storage;

import com.example.media_api.uploads.ObjectLocation;
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
    private static final ObjectLocation LOCATION = new ObjectLocation("posts/original.123.jpg", "bucket");

    @Mock
    private ObjectStorageRepository objectStorageRepository;

    @Test
    void readReturnsObjectStorageStream() {
        var storage = new ObjectStorageStream(objectStorageRepository);
        var expectedStream = new ByteArrayInputStream(new byte[]{1, 2, 3});

        when(objectStorageRepository.getObject(eq(LOCATION.bucket()), eq(LOCATION.path())))
                .thenReturn(expectedStream);

        var actualStream = storage.read(LOCATION);

        assertSame(expectedStream, actualStream);
    }

    @Test
    void writeUploadsInputStreamAsJpeg() throws Exception {
        var storage = new ObjectStorageStream(objectStorageRepository);
        var payload = new byte[]{10, 20, 30, 40};

        storage.write(new ByteArrayInputStream(payload), LOCATION);

        var streamCaptor = ArgumentCaptor.forClass(InputStream.class);
        var lengthCaptor = ArgumentCaptor.forClass(Long.class);

        verify(objectStorageRepository).putObject(
                eq(LOCATION.bucket()),
                eq(LOCATION.path()),
                streamCaptor.capture(),
                lengthCaptor.capture(),
                eq("image/jpeg")
        );

        assertArrayEquals(payload, streamCaptor.getValue().readAllBytes());
        assertEquals(payload.length, lengthCaptor.getValue());
    }
}
