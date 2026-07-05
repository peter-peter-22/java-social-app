package com.example.media.uploads.transformations;

import com.example.media.common.transformations.api.UploadTransformationDTO;
import com.example.media.common.transformations.operations.ImageTransformationOperations;
import com.example.media.common.uploads.UploadId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ParralelTransformationApiTest {

    @Test
    void transformCallsEachTransformationOnce() {
        var api = mock(ParralelTransformationApi.class, CALLS_REAL_METHODS);
        var first = transformation("first");
        var second = transformation("second");

        api.transform(List.of(first, second));

        verify(api).call(first);
        verify(api).call(second);
        verify(api, times(2)).call(any());
    }

    private UploadTransformationDTO transformation(String name) {
        return new UploadTransformationDTO(
                name,
                "transformations",
                new UploadId(name + ".jpg", "uploads"),
                null,
                ImageTransformationOperations.builder().build()
        );
    }
}
